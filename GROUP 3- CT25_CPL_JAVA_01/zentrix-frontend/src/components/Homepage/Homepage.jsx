import { useMemo, useState, useEffect, useRef } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
    Carousel,
    CarouselContent,
    CarouselItem,
    CarouselNext,
    CarouselPrevious,
} from "@/components/ui/carousel";
import {
    NavigationMenu,
    NavigationMenuContent,
    NavigationMenuItem,
    NavigationMenuLink,
    NavigationMenuList,
    NavigationMenuTrigger,
} from "@/components/ui/navigation-menu";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import { useNavigate } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import axios from "axios";
import { showNotification } from "../Dashboard/NotificationPopup";

const Homepage = () => {
    const imageSlides = [
        { id: 1, src: "https://file.hstatic.net/200000722513/file/thang_03_laptop_rtx_5090_800x400.jpg", alt: "Image 1" },
        { id: 2, src: "https://file.hstatic.net/200000722513/file/thang_04_banner_web_slider_800x400.png", alt: "Image 2" },
        { id: 3, src: "https://file.hstatic.net/200000722513/file/thang_03_thu_cu_doi_moi_banner_web_slider_800x400.jpg", alt: "Image 3" },
    ];

    const navigate = useNavigate();
    const [products, setProducts] = useState([]);
    const [selectCategories, setSelectCategories] = useState([]);
    const [posts, setPosts] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const carouselApisRef = useRef({});
    const [isInteracting, setIsInteracting] = useState(false);
    const [imageCarouselApi, setImageCarouselApi] = useState(null);
    const [maxItems, setMaxItems] = useState(6);

    const fetchImageProductType = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, { withCredentials: true });
            if (Array.isArray(response.data) && response.data.length > 0) {
                const firstImage = response.data[0];
                let url = firstImage.imageId?.imageLink || firstImage.imageLink || firstImage || "/images/placeholder.jpg";
                if (url && !url.startsWith("http")) {
                    url = `http://localhost:6789${url.startsWith("/") ? "" : "/"}${url}`;
                }
                return url;
            } else if (!Array.isArray(response.data) && response.data) {
                let url = response.data.imageId?.imageLink || response.data.imageLink || response.data || "/images/placeholder.jpg";
                if (url && !url.startsWith("http")) {
                    url = `http://localhost:6789${url.startsWith("/") ? "" : "/"}${url}`;
                }
                return url;
            }
            return "/images/placeholder.jpg";
        } catch (error) {
            console.error("Error fetching product images:", error);
            return "/images/placeholder.jpg";
        }
    };

    const fetchAllProductTypes = async () => {
        try {
            setIsLoading(true);
            // Sử dụng API có cateId
            const response = await axios.get("http://localhost:6789/api/v1/products?page=0&size=100", {
                withCredentials: true,
            });
            const productsData = Array.isArray(response.data.content) ? response.data.content : [];
            console.log("Raw Product Types from API:", JSON.stringify(productsData, null, 2));

            const activeProducts = productsData.filter((product) => product.status === 1);

            const enrichedProductTypes = await Promise.all(
                activeProducts.flatMap((product) =>
                    product.productTypes
                        .filter((productType) => productType.status === 1)
                        .map(async (productType) => {
                            try {
                                const image = await fetchImageProductType(productType.prodTypeId);
                                return {
                                    prodId: productType.prodTypeId || "unknown-id",
                                    prodName: productType.prodTypeName || "Unknown Product",
                                    prodTypePrice: productType.prodTypePrice || 0,
                                    unitPrice: productType.unitPrice || productType.prodTypePrice || 0,
                                    image: image,
                                    categoryId: product.cateId?.cateId || null,
                                    brand: (productType.prodTypeName || "Unknown").split(" ")[0] || "Unknown",
                                    productTypes: [productType],
                                };
                            } catch (error) {
                                console.error(`Error enriching productType ${productType.prodTypeId}:`, error);
                                return {
                                    prodId: "unknown-id",
                                    prodName: "Unknown Product",
                                    prodTypePrice: 0,
                                    unitPrice: 0,
                                    image: "/images/placeholder.jpg",
                                    categoryId: null,
                                    brand: "Unknown",
                                    productTypes: [productType],
                                };
                            }
                        })
                )
            );

            setProducts(enrichedProductTypes);
            localStorage.setItem("products", JSON.stringify(enrichedProductTypes));
        } catch (error) {
            console.error("Error fetching product types:", error);
            setProducts([]);
        } finally {
            setIsLoading(false);
        }
    };

    const formatPrice = (price) => {
        return Math.floor(price).toLocaleString("vi-VN") + " VNĐ";
    };

    const fetchCategories = async () => {
        try {
            const initialResponse = await axios.get("http://localhost:6789/api/v1/categories", {
                params: { page: 0, size: 1, sort: "cateId,asc" },
                withCredentials: true,
            });

            let allCategories = [];
            if (initialResponse.data.success) {
                const totalPages = initialResponse.data.content?.totalPages || 1;
                const totalCategories = totalPages * 1;

                const response = await axios.get("http://localhost:6789/api/v1/categories", {
                    params: { page: 0, size: totalCategories, sort: "cateId,asc" },
                    withCredentials: true,
                });

                if (response.data.success) {
                    allCategories = response.data.content?.content || [];
                }
            } else {
                throw new Error("Initial fetch failed");
            }

            const categoryMap = new Map();
            allCategories.forEach((category) => {
                if (!category.parentCateId) {
                    const cateName = category.cateName || "Category-" + category.cateId;
                    categoryMap.set(category.cateId, {
                        cateId: category.cateId,
                        title: cateName,
                        href: `/products/${encodeURIComponent(cateName)}`,
                        subCategories: [],
                    });
                }
            });

            allCategories.forEach((category) => {
                if (category.parentCateId && typeof category.parentCateId === "object" && category.parentCateId.cateId) {
                    const parentId = category.parentCateId.cateId;
                    const parentCategory = categoryMap.get(parentId);
                    if (parentCategory) {
                        const subCateName = category.cateName || "SubCategory-" + category.cateId;
                        parentCategory.subCategories.push({
                            cateId: category.cateId,
                            title: subCateName,
                            href: `/products/${encodeURIComponent(parentCategory.title.split("/").pop())}/${encodeURIComponent(subCateName)}`,
                            subSubCategories: [],
                        });
                    }
                }
            });

            allCategories.forEach((category) => {
                if (category.parentCateId && typeof category.parentCateId === "object" && category.parentCateId.cateId) {
                    const parentId = category.parentCateId.cateId;
                    categoryMap.forEach((parentCategory) => {
                        parentCategory.subCategories.forEach((subCategory) => {
                            if (subCategory.cateId === parentId) {
                                const subSubCateName = category.cateName || "SubSubCategory-" + category.cateId;
                                subCategory.subSubCategories.push({
                                    cateId: category.cateId,
                                    title: subSubCateName,
                                    href: `/products/${encodeURIComponent(parentCategory.title.split("/").pop())}/${encodeURIComponent(subCategory.title)}/${encodeURIComponent(subSubCateName)}`,
                                });
                            }
                        });
                    });
                }
            });

            const categories = Array.from(categoryMap.values());
            setSelectCategories(categories);
            localStorage.setItem("selectCategories", JSON.stringify(categories));
        } catch (error) {
            console.error("Error fetching categories:", error);
            setSelectCategories([]);
        }
    };

    const getProductsForCategory = (category) => {
        const allCateIds = [
            category.cateId,
            ...category.subCategories.map((sub) => sub.cateId),
            ...category.subCategories.flatMap((sub) => sub.subSubCategories.map((subSub) => subSub.cateId)),
        ];
        console.log("Category IDs:", allCateIds);
        const filtered = products.filter((product) => {
            console.log("Product categoryId:", product.categoryId);
            return allCateIds.includes(product.categoryId);
        });
        console.log("Filtered products:", filtered);
        return filtered;
    };

    const categorizedProducts = useMemo(() => {
        const allCategorizedProducts = selectCategories.map((category) => ({
            category,
            filteredProducts: getProductsForCategory(category),
        }));
        return allCategorizedProducts.filter(({ filteredProducts }) => filteredProducts.length > 0);
    }, [products, selectCategories]);

    // Filter products for SUPER SALE
    const superSaleProducts = useMemo(() => {
        return products.filter((product) => {
            const savingsPercentage = product.prodTypePrice > 0
                ? ((product.prodTypePrice - product.unitPrice) / product.prodTypePrice) * 100
                : 0;
            return savingsPercentage > 10;
        });
    }, [products]);

    const getImageFromPost = (post) => {
        const extractFirstImageFromDescription = (html) => {
            const div = document.createElement("div");
            div.innerHTML = html;
            const imgTag = div.querySelector("img");
            const src = imgTag?.getAttribute("src");
            if (!src) return null;
            return src.startsWith("http") ? src : `http://${src.startsWith("/") ? src.slice(1) : src}`;
        };

        const descImage = extractFirstImageFromDescription(post.description);
        if (descImage) return descImage;

        const firstImg = post.images?.[0];
        if (!firstImg) return "/fallback.jpg";
        return firstImg.startsWith("http") ? firstImg : `http://${firstImg.startsWith("/") ? firstImg.slice(1) : firstImg}`;
    };

    const fetchPosts = async () => {
        try {
            const response = await fetch("http://localhost:6789/api/v1/posts", { withCredentials: true });
            const data = await response.json();

            if (data.success && data.content) {
                setPosts(data.content);
                console.log(data);
            } else {
                showNotification("Failed to load posts", 3000, 'fail');
            }
        } catch (error) {
            console.error("Error fetching posts:", error);
        }
    };

    useEffect(() => {
        fetchAllProductTypes();
        fetchCategories();
        fetchPosts();
    }, []);

    useEffect(() => {
        const intervals = {};
        if (imageCarouselApi && !isInteracting) {
            intervals["imageCarousel"] = setInterval(() => {
                if (imageCarouselApi.canScrollNext()) {
                    imageCarouselApi.scrollNext();
                } else {
                    imageCarouselApi.scrollTo(0);
                }
            }, 1500);
        }
        return () => {
            Object.values(intervals).forEach((interval) => clearInterval(interval));
        };
    }, [categorizedProducts, imageCarouselApi, isInteracting]);

    const handleProductClick = (prodId) => {
        localStorage.setItem("selectedProdTypeId", prodId);
        navigate(`/product/${encodeURIComponent(prodId)}`);
    };

    const handleNavigationClick = (cateId, href) => {
        localStorage.setItem("selectedCateId", cateId);
        localStorage.setItem("selectedHref", href);
        navigate(href);
    };

    const splitProductsIntoRows = (filteredProducts, singleRow = false) => {
        const groups = [];
        const maxPerRow = 6;
        for (let i = 0; i < filteredProducts.length; i += maxPerRow * (singleRow ? 1 : 2)) {
            const group = filteredProducts.slice(i, i + maxPerRow * (singleRow ? 1 : 2));
            if (group.length > 0) {
                const row1 = group.slice(0, maxPerRow);
                const row2 = singleRow ? [] : group.slice(maxPerRow, maxPerRow * 2);
                groups.push({ row1, row2 });
            }
        }
        return groups;
    };

    useEffect(() => {
        const updateMaxItems = () => {
            const width = window.innerWidth;
            if (width < 640) setMaxItems(2);
            else if (width < 768) setMaxItems(3);
            else if (width < 1024) setMaxItems(4);
            else if (width < 1280) setMaxItems(5);
            else setMaxItems(6);
        };

        updateMaxItems();
        window.addEventListener("resize", updateMaxItems);
        return () => window.removeEventListener("resize", updateMaxItems);
    }, []);

    return (
        <div className="bg-[#f8f8fc] text-gray-900 min-h-screen flex flex-col items-center overflow-hidden" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="h-20 bg-blue-700 text-white flex items-center px-4 shadow-md">
                <Header />
            </header>
            <main className="container mx-auto p-6 flex flex-col space-y-8">
                <div
                    className="flex flex-row items-center justify-between w-full gap-4"
                    style={{
                        backgroundImage: `url('https://wallpapers.com/images/hd/low-poly-blue-background-5o6wzulr6o47bk0a.webp')`,
                        backgroundSize: 'cover',
                        backgroundPosition: 'center',
                        backgroundRepeat: 'no-repeat',
                    }}
                >
                    <div className="w-1/4 p-8 flex justify-center">
                        <img
                            src="https://lh3.googleusercontent.com/QVPaSacCaPAj9c9N792YWuzbpgQFKpnnIphGxv7G5tqruCOkC41lTZzZ7oH248QMQHfUhgjIe2gtXxcRnd6z79cKcg-tKv0=w300-rw"
                            alt="Tổng hợp CTKM - 2025"
                            className="w-60 h-64 object-cover rounded-lg "
                        />
                    </div>

                    <div className="w-2/4">
                        <Carousel
                            setApi={setImageCarouselApi}
                            opts={{ loop: true }}
                            className="w-full items-center"
                            onMouseEnter={() => setIsInteracting(true)}
                            onMouseLeave={() => setTimeout(() => setIsInteracting(false), 2000)}
                        >
                            <CarouselContent>
                                {imageSlides.map((image) => (
                                    <CarouselItem key={image.id}>
                                        <div className="p-1">
                                            <img
                                                src={image.src}
                                                alt={image.alt}
                                                className="w-full h-74 object-cover rounded-lg shadow-md"
                                            />
                                        </div>
                                    </CarouselItem>
                                ))}
                            </CarouselContent>
                        </Carousel>
                    </div>

                    <div className="w-1/4 flex justify-center">
                        <img
                            src="https://lh3.googleusercontent.com/CwJ5UgNxuTJ8EiB4urpJErcIn8QMNw4zfH60l1Gi8iozOicDvHCFx4lf_8L68NZ5nchyjoYnPEPtVV3utK__Iuro4OOHRC-thg=w300-rw"
                            alt="Voucher Học sinh sinh viên"
                            className="w-60 h-64 object-cover rounded-lg "
                        />
                    </div>
                </div>

                <NavigationMenu>
                    <NavigationMenuList>
                        {selectCategories.map((category) => (
                            <NavigationMenuItem key={category.cateId}>
                                <NavigationMenuTrigger
                                    className="cursor-pointer"
                                    onClick={() => handleNavigationClick(category.cateId, category.href)}
                                >
                                    {category.title}
                                </NavigationMenuTrigger>
                                <NavigationMenuContent>
                                    <ul className="w-[300px] p-2 bg-white shadow-lg rounded-md">
                                        {category.subCategories.map((sub) => (
                                            <li key={sub.cateId} className="mb-2">
                                                <NavigationMenuLink
                                                    onClick={() => handleNavigationClick(sub.cateId, sub.href)}
                                                >
                                                    <span className="block px-4 py-2 font-semibold hover:bg-gray-100 rounded-md cursor-pointer">
                                                        {sub.title}
                                                    </span>
                                                </NavigationMenuLink>
                                                {sub.subSubCategories.length > 0 && (
                                                    <ul className="ml-4 mt-1 space-y-1">
                                                        {sub.subSubCategories.map((subSub) => (
                                                            <li key={subSub.cateId}>
                                                                <NavigationMenuLink
                                                                    onClick={() =>
                                                                        handleNavigationClick(subSub.cateId, subSub.href)
                                                                    }
                                                                >
                                                                    <span className="block px-4 py-1 text-sm hover:bg-gray-100 rounded-md cursor-pointer">
                                                                        {subSub.title}
                                                                    </span>
                                                                </NavigationMenuLink>
                                                            </li>
                                                        ))}
                                                    </ul>
                                                )}
                                            </li>
                                        ))}
                                    </ul>
                                </NavigationMenuContent>
                            </NavigationMenuItem>
                        ))}
                    </NavigationMenuList>
                </NavigationMenu>

                {isLoading ? (
                    <p className="text-center text-gray-600">Loading products...</p>
                ) : products.length === 0 ? (
                    <p className="text-center text-gray-600">No products available</p>
                ) : (
                    <div className="space-y-8">
                        {/* SUPER SALE Section with Single Row */}
                        {superSaleProducts.length >= 4 && (
                            <div className="relative p-4 bg-gradient-to-r from-red-500 to-yellow-500 rounded-lg shadow-lg">
                                <div className="flex justify-between items-center mb-4">
                                    <h2 className="text-3xl font-bold text-white">SUPER SALE {"\u003E"} 10%</h2>
                                </div>
                                <Carousel
                                    opts={{ loop: false }}
                                    className="w-full"
                                    onMouseEnter={() => setIsInteracting(true)}
                                    onMouseLeave={() => setTimeout(() => setIsInteracting(false), 2000)}
                                >
                                    <CarouselContent>
                                        {splitProductsIntoRows(superSaleProducts, true).map((group, groupIndex) => (
                                            <CarouselItem key={groupIndex}>
                                                <div className="flex gap-2">
                                                    {group.row1.slice(0, maxItems).map((product) => {
                                                        const savingsAmount = product.prodTypePrice - product.unitPrice;
                                                        return (
                                                            <Card
                                                                key={product.prodId}
                                                                className="w-[240px] h-[372px] cursor-pointer hover:shadow-lg transition flex flex-col relative"
                                                                onClick={() => handleProductClick(product.prodId)}
                                                            >
                                                                <div className="flex-1 flex items-center justify-center p-2 relative">
                                                                    <img
                                                                        src={product.image}
                                                                        alt={product.prodName}
                                                                        className="w-full h-[180px] object-contain"
                                                                        onError={(e) => {
                                                                            if (e.target.src !== "/images/placeholder.jpg") {
                                                                                e.target.src = "/images/placeholder.jpg";
                                                                            }
                                                                        }}
                                                                    />
                                                                    {savingsAmount > 0 && (
                                                                        <div className="absolute bottom-2 left-2 bg-gradient-to-r from-purple-500 to-blue-500 text-xs font-semibold px-2 py-1 rounded flex flex-col items-start">
                                                                            <span className="text-yellow-400">SAVED</span>
                                                                            <span className="text-white">{formatPrice(product.prodTypePrice - product.unitPrice)}</span>
                                                                        </div>
                                                                    )}
                                                                </div>
                                                                <CardHeader className="p-2">
                                                                    <CardTitle className="text-base font-medium line-clamp-1">
                                                                        {product.prodName}
                                                                    </CardTitle>
                                                                </CardHeader>
                                                                <CardContent className="p-2">
                                                                    <p style={{ fontSize: '20px' }} className="text-blue-800 font-semibold">
                                                                        {product.unitPrice ? formatPrice(product.unitPrice) : "N/A"}
                                                                    </p>
                                                                    {product.prodTypePrice !== product.unitPrice && (
                                                                        <div className="flex">
                                                                            <p style={{ fontSize: '17px' }} className="text-gray-400 line-through text-sm">
                                                                                {product.prodTypePrice ? formatPrice(product.prodTypePrice) : "N/A"}
                                                                            </p>
                                                                            <p className="">
                                                                                {product.prodTypePrice && product.unitPrice ? (
                                                                                    <span className="text-sm text-green-500">
                                                                                        {` -${(((product.prodTypePrice - product.unitPrice) / product.prodTypePrice) * 100).toFixed(3)}%`}
                                                                                    </span>
                                                                                ) : null}
                                                                            </p>
                                                                        </div>
                                                                    )}
                                                                </CardContent>
                                                            </Card>
                                                        );
                                                    })}
                                                </div>
                                            </CarouselItem>
                                        ))}
                                    </CarouselContent>
                                    <CarouselPrevious />
                                    <CarouselNext />
                                </Carousel>
                            </div>
                        )}

                        {/* Existing Categorized Products Section */}
                        {categorizedProducts.length === 0 ? (
                            <p className="text-center text-gray-600 ">No categories with products available</p>
                        ) : (
                            categorizedProducts.map(({ category, filteredProducts }) => {
                                const productGroups = splitProductsIntoRows(filteredProducts);
                                const categoryApi = carouselApisRef.current[category.cateId];
                                return (
                                    <div>
                                        <div className="border-t border-gray-300 p-5 "></div>
                                        <div
                                            key={category.cateId}
                                            className="relative p-4 space-y-4 bg-gradient-to-b from-pink-300 to-blue-900 rounded-lg"
                                        >
                                            <div className="space-y-4">
                                                <div className="flex justify-between items-center">
                                                    <h2
                                                        className="text-2xl font-semibold cursor-pointer hover:text-blue-600 text-white"
                                                        onClick={() => handleNavigationClick(category.cateId, category.href)}
                                                    >
                                                        {category.title}
                                                    </h2>
                                                    <div className="flex space-x-2 overflow-x-auto">
                                                        {category.subCategories.map((sub) => (
                                                            <button
                                                                key={sub.cateId}
                                                                onClick={() => handleNavigationClick(sub.cateId, sub.href)}
                                                                className="px-4 py-2 rounded-full text-sm font-medium transition-colors bg-gray-200 text-gray-800 hover:bg-gray-300"
                                                            >
                                                                {sub.title}
                                                            </button>
                                                        ))}
                                                    </div>
                                                </div>

                                                <Carousel
                                                    setApi={(api) => {
                                                        carouselApisRef.current[category.cateId] = api;
                                                    }}
                                                    opts={{ loop: false }}
                                                    className="w-full"
                                                    onMouseEnter={() => setIsInteracting(true)}
                                                    onMouseLeave={() => setTimeout(() => setIsInteracting(false), 2000)}
                                                >
                                                    <CarouselContent>
                                                        {productGroups.map((group, groupIndex) => (
                                                            <CarouselItem key={groupIndex}>
                                                                <div className="space-y-2">
                                                                    <div className="flex gap-2">
                                                                        {group.row1.slice(0, maxItems).map((product) => (
                                                                            <Card
                                                                                key={product.prodId}
                                                                                className="w-[240px] h-[372px] cursor-pointer hover:shadow-lg transition flex flex-col relative"
                                                                                onClick={() => handleProductClick(product.prodId)}
                                                                            >
                                                                                <div className="flex-1 flex items-center justify-center p-2 relative">
                                                                                    <img
                                                                                        src={product.image}
                                                                                        alt={product.prodName}
                                                                                        className="w-full h-[180px] object-contain"
                                                                                        onError={(e) => {
                                                                                            if (e.target.src !== "/images/placeholder.jpg") {
                                                                                                e.target.src = "/images/placeholder.jpg";
                                                                                            }
                                                                                        }}
                                                                                    />
                                                                                    {product.prodTypePrice - product.unitPrice > 0 && (
                                                                                        <div className="absolute bottom-2 left-2 bg-gradient-to-r from-purple-500 to-blue-500 text-xs font-semibold px-2 py-1 rounded flex flex-col items-start">
                                                                                            <span className="text-yellow-400">SAVED</span>
                                                                                            <span className="text-white">{formatPrice(product.prodTypePrice - product.unitPrice)}</span>
                                                                                        </div>
                                                                                    )}
                                                                                </div>
                                                                                <CardHeader className="p-2">
                                                                                    <CardTitle className="text-base font-medium line-clamp-1">
                                                                                        {product.prodName}
                                                                                    </CardTitle>
                                                                                </CardHeader>
                                                                                <CardContent className="p-2">
                                                                                    <p style={{ fontSize: '20px' }} className="text-blue-800 font-semibold">
                                                                                        {product.unitPrice ? formatPrice(product.unitPrice) : "N/A"}
                                                                                    </p>
                                                                                    {product.prodTypePrice !== product.unitPrice && (
                                                                                        <div className="flex">
                                                                                            <p style={{ fontSize: '17px' }} className="text-gray-400 line-through text-sm">
                                                                                                {product.prodTypePrice ? formatPrice(product.prodTypePrice) : "N/A"}
                                                                                            </p>
                                                                                            <p className="">
                                                                                                {product.prodTypePrice && product.unitPrice ? (
                                                                                                    <span className="text-sm text-green-500">
                                                                                                        {` -${(((product.prodTypePrice - product.unitPrice) / product.prodTypePrice) * 100).toFixed(3)}%`}
                                                                                                    </span>
                                                                                                ) : null}
                                                                                            </p>
                                                                                        </div>
                                                                                    )}
                                                                                </CardContent>
                                                                            </Card>
                                                                        ))}
                                                                    </div>
                                                                    <div className="flex gap-2">
                                                                        {group.row2.slice(0, maxItems).map((product) => (
                                                                            <Card
                                                                                key={product.prodId}
                                                                                className="w-[240px] h-[372px] cursor-pointer hover:shadow-lg transition flex flex-col relative"
                                                                                onClick={() => handleProductClick(product.prodId)}
                                                                            >
                                                                                <div className="flex-1 flex items-center justify-center p-2 relative">
                                                                                    <img
                                                                                        src={product.image}
                                                                                        alt={product.prodName}
                                                                                        className="w-full h-[180px] object-contain"
                                                                                        onError={(e) => {
                                                                                            if (e.target.src !== "/images/placeholder.jpg") {
                                                                                                e.target.src = "/images/placeholder.jpg";
                                                                                            }
                                                                                        }}
                                                                                    />
                                                                                    {product.prodTypePrice - product.unitPrice > 0 && (
                                                                                        <div className="absolute bottom-2 left-2 bg-gradient-to-r from-purple-500 to-blue-500 text-xs font-semibold px-2 py-1 rounded flex flex-col items-start">
                                                                                            <span className="text-yellow-400">SAVED</span>
                                                                                            <span className="text-white">{formatPrice(product.prodTypePrice - product.unitPrice)}</span>
                                                                                        </div>
                                                                                    )}
                                                                                </div>
                                                                                <CardHeader className="p-2">
                                                                                    <CardTitle className="text-base font-medium line-clamp-1">
                                                                                        {product.prodName}
                                                                                    </CardTitle>
                                                                                </CardHeader>
                                                                                <CardContent className="p-2">
                                                                                    <p style={{ fontSize: '20px' }} className="text-blue-800 font-semibold">
                                                                                        {product.unitPrice ? formatPrice(product.unitPrice) : "N/A"}
                                                                                    </p>
                                                                                    {product.prodTypePrice !== product.unitPrice && (
                                                                                        <div className="flex">
                                                                                            <p style={{ fontSize: '17px' }} className="text-gray-400 line-through text-sm">
                                                                                                {product.prodTypePrice ? formatPrice(product.prodTypePrice) : "N/A"}
                                                                                            </p>
                                                                                            <p className="">
                                                                                                {product.prodTypePrice && product.unitPrice ? (
                                                                                                    <span className="text-sm text-green-500">
                                                                                                        {` -${(((product.prodTypePrice - product.unitPrice) / product.prodTypePrice) * 100).toFixed(3)}%`}
                                                                                                    </span>
                                                                                                ) : null}
                                                                                            </p>
                                                                                        </div>
                                                                                    )}
                                                                                </CardContent>
                                                                            </Card>
                                                                        ))}
                                                                    </div>
                                                                </div>
                                                            </CarouselItem>
                                                        ))}
                                                    </CarouselContent>
                                                    <CarouselPrevious
                                                        onClick={() => {
                                                            setIsInteracting(true);
                                                            if (categoryApi && categoryApi.canScrollPrev()) {
                                                                categoryApi.scrollPrev();
                                                            }
                                                        }}
                                                        className={categoryApi && !categoryApi.canScrollPrev() ? "opacity-50 cursor-not-allowed" : ""}
                                                    />
                                                    <CarouselNext
                                                        onClick={() => {
                                                            setIsInteracting(true);
                                                            if (categoryApi && categoryApi.canScrollNext()) {
                                                                categoryApi.scrollNext();
                                                            }
                                                        }}
                                                        className={categoryApi && !categoryApi.canScrollNext() ? "opacity-50 cursor-not-allowed" : ""}
                                                    />
                                                </Carousel>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>
                )}

                <div className="space-y-4">
                    <div className="flex flex-col sm:flex-row justify-between items-center gap-4 py-6 px-4 sm:px-6 lg:px-8 bg-gradient-to-r from-gray-50 to-gray-100 rounded-xl shadow-sm">
                        <div className="flex flex-col items-start">
                            <div className="flex items-center gap-3">


                                <h2 className="text-3xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600 animate-gradient">
                                    Tech NEWS
                                </h2>
                            </div>
                            <p className="text-sm text-gray-500 mt-1">
                                Stay updated with the latest in technology • {posts.length} articles
                            </p>
                        </div>

                        <a
                            href="/blog"
                            className="group flex items-center gap-1 text-blue-600 text-[1.2rem] font-medium hover:text-blue-800 hover:underline"
                        >
                            View all
                            <ArrowRight size={30} className="w-5 h-5 transition-transform group-hover:translate-x-1" />
                        </a>
                    </div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 px-4 sm:px-6 lg:px-2">
                        {posts.length > 0 ? (
                            posts
                                .filter((post) => post.status === "ACTIVE"&& post.createdBy?.userId?.status === 1)
                                .slice(0, 3)
                                .map((post) => (
                                    <Card
                                        onClick={() => navigate(`/blog/${post.postId}`)}
                                        key={post.postId}
                                        className="group relative cursor-pointer overflow-hidden rounded-xl bg-white shadow-lg hover:shadow-xl transition-all duration-300 hover:-translate-y-1 border border-gray-100"
                                    >
                                        <div className="relative w-full h-48 overflow-hidden">
                                            <img
                                                src={getImageFromPost(post)}
                                                className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                                                alt="Blog Cover"
                                            />
                                            <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                                            <div className="absolute bottom-4 left-4 text-white opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                                <p className="text-sm font-medium">Read More</p>
                                            </div>
                                        </div>

                                        <CardHeader className="p-4 space-y-2">
                                            <CardTitle className="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition-colors duration-300 line-clamp-2">
                                                {post.title}
                                            </CardTitle>

                                        </CardHeader>
                                        <div className="absolute top-4 right-4 bg-blue-500 text-white text-xs font-medium px-2 py-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                            Tech
                                        </div>
                                    </Card>
                                ))
                        ) : (
                            <div className="col-span-1 sm:col-span-2 lg:col-span-3 flex flex-col items-center justify-center py-12 text-gray-600">
                                <svg
                                    className="w-16 h-16 text-gray-400 mb-4"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth="2"
                                        d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                                    ></path>
                                </svg>
                                <p className="text-lg font-medium text-gray-600">No posts available</p>
                                <p className="text-sm text-gray-400 mt-1">Check back later for more updates!</p>
                            </div>
                        )}
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    );
};

export default Homepage;