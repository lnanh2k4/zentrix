import { useState, useEffect, useMemo } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import axios from "axios";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Search } from "lucide-react";
import { cn } from "@/lib/utils";
import { AnimatePresence, motion } from "framer-motion";

const ProductPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [products, setProducts] = useState([]);
  const [allProducts, setAllProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingAttributes, setIsLoadingAttributes] = useState(false);
  const [selectedBrands, setSelectedBrands] = useState(new Set());
  const [minPrice, setMinPrice] = useState(0);
  const [maxPrice, setMaxPrice] = useState(0);
  const [priceRangeMax, setPriceRangeMax] = useState(10000000);
  const [priceSort, setPriceSort] = useState(null);
  const [breadcrumbItems, setBreadcrumbItems] = useState([]);
  const [subCategoryIds, setSubCategoryIds] = useState(() => {
    const savedSubCategoryIds = localStorage.getItem("subCategoryIds");
    return savedSubCategoryIds ? JSON.parse(savedSubCategoryIds) : [];
  });
  const [subSubCategoryIds, setSubSubCategoryIds] = useState(() => {
    const savedSubSubCategoryIds = localStorage.getItem("subSubCategoryIds");
    return savedSubSubCategoryIds ? JSON.parse(savedSubSubCategoryIds) : [];
  });
  const [cateId, setCateId] = useState(localStorage.getItem("selectedCateId") || null);
  const [selectedHref, setSelectedHref] = useState(localStorage.getItem("selectedHref") || null);
  const [branchId, setBranchId] = useState(localStorage.getItem("selectedBranchId") || 5);
  const [selectedBranchName, setSelectedBranchName] = useState(localStorage.getItem("selectedBranchName") || null);
  const [targetSubCategoryId, setTargetSubCategoryId] = useState(null);
  const [attributes, setAttributes] = useState([]);
  const [selectedAttributes, setSelectedAttributes] = useState({});
  const [showAllBrands, setShowAllBrands] = useState(false);
  const [availableBranches, setAvailableBranches] = useState([]);
  const [isBranchModalOpen, setIsBranchModalOpen] = useState(false);
  const [branchSearchQuery, setBranchSearchQuery] = useState("");
  const [isLoadingBranches, setIsLoadingBranches] = useState(false);

  const handleNavigation = () => navigate("/compare-products");

  const fetchImageProductType = async (prodTypeId) => {
    try {
      const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, {
        withCredentials: true,
      });
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

  const fetchBranchQuantity = async (prodTypeId, brchId) => {
    try {
      const response = await axios.get(`http://localhost:6789/api/v1/products/productTypeBranch/${prodTypeId}`, {
        withCredentials: true,
      });
      const branchData = response.data.find((item) => item.brchId?.brchId === Number(brchId));
      return branchData ? branchData.quantity : null;
    } catch (error) {
      console.error(`Error fetching branch quantity for prodTypeId ${prodTypeId}:`, error);
      return null;
    }
  };

  const fetchAllProductTypes = async () => {
    try {
      setIsLoading(true);
      const token = localStorage.getItem("token");
      const response = await axios.get("http://localhost:6789/api/v1/products?page=0&size=100", {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
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
              const image = await fetchImageProductType(productType.prodTypeId);
              const quantity = await fetchBranchQuantity(productType.prodTypeId, branchId);
              return {
                prodId: productType.prodTypeId || "unknown-id",
                prodName: productType.prodTypeName || "Unknown Product",
                prodTypePrice: productType.prodTypePrice || 0,
                unitPrice: productType.unitPrice || productType.prodTypePrice || 0,
                image: image,
                categoryId: product.cateId?.cateId || null,
                brand: (productType.prodTypeName || "Unknown").split(" ")[0] || "Unknown",
                productTypes: [productType],
                attributes: [],
                quantity: quantity,
              };
            })
        )
      );

      const availableProducts = enrichedProductTypes.filter((product) => product.quantity !== null);
      setAllProducts(availableProducts);
      localStorage.setItem("products", JSON.stringify(availableProducts));

      const highestPrice = availableProducts.length > 0
        ? Math.max(...availableProducts.map((product) => product.unitPrice || 0))
        : 10000000;
      setMaxPrice(highestPrice);
      setPriceRangeMax(highestPrice);
    } catch (error) {
      console.error("Error fetching product types:", error.response?.data || error);
      setAllProducts([]);
      setMaxPrice(10000000);
      setPriceRangeMax(10000000);
      if (error.response?.status === 401) navigate("/login");
    } finally {
      setIsLoading(false);
    }
  };

  const fetchAllAttributes = async () => {
    if (allProducts.length === 0) {
      console.warn("No products available to fetch attributes.");
      return;
    }
    try {
      setIsLoadingAttributes(true);
      const response = await axios.get("http://localhost:6789/api/v1/products/productTypeAttributes", {
        params: { page: 0, size: 10000 },
        withCredentials: true,
      });
      const attributesData = response.data.content || [];
      console.log("Fetched attributes:", attributesData);
      setAttributes(attributesData);

      const updatedProducts = allProducts.map((product) => {
        const productAttributes = attributesData.filter(
          (attr) => attr.prodTypeId.prodTypeId === product.prodId
        );
        return { ...product, attributes: productAttributes };
      });
      console.log("Updated products with attributes:", updatedProducts);
      setAllProducts([...updatedProducts]);
      setProducts([...updatedProducts]);
    } catch (error) {
      console.error("Error fetching attributes:", error.response?.status, error.response?.data || error.message);
      setAttributes([]);
    } finally {
      setIsLoadingAttributes(false);
    }
  };

  const fetchProductTypeIdsByBranch = async (brchId) => {
    try {
      const response = await axios.get("http://localhost:6789/api/v1/products/productTypeBranchs", {
        params: { page: 0, size: 10000 },
        withCredentials: true,
      });
      const productTypeBranches = Array.isArray(response.data.content) ? response.data.content : [];
      const filteredProductTypeBranches = productTypeBranches.filter((ptb) => {
        const branchId = ptb.brchId?.brchId ?? ptb.brchId;
        return branchId === Number(brchId);
      });

      const prodTypeIds = filteredProductTypeBranches.map((ptb) => {
        return ptb.prodTypeId?.prodTypeId ?? ptb.prodTypeId;
      }).filter((id) => id != null);
      return prodTypeIds;
    } catch (error) {
      console.error("Error fetching product type IDs for branch:", error);
      return [];
    }
  };

  const fetchCateIdFromHref = async (href) => {
    try {
      const response = await axios.get("http://localhost:6789/api/v1/categories", {
        params: { page: 0, size: 10000, sort: "cateId,asc" },
        withCredentials: true,
      });
      const categories = response.data.content?.content || [];

      const pathSegments = href.split("/").filter((segment) => segment && segment !== "products");
      if (pathSegments.length === 0) return null;

      const cateName = decodeURIComponent(pathSegments[0]);
      const subCateName = pathSegments[1] ? decodeURIComponent(pathSegments[1]) : null;

      const parentCategory = categories.find((cat) => cat.cateName === cateName && !cat.parentCateId);
      if (!parentCategory) return null;
      const parentCateId = parentCategory.cateId;

      if (subCateName) {
        const subCategory = categories.find(
          (cat) => cat.cateName === subCateName && cat.parentCateId?.cateId === parentCateId
        );
        if (subCategory) {
          setTargetSubCategoryId(subCategory.cateId);
          return parentCateId;
        }
      }

      setTargetSubCategoryId(null);
      return parentCateId;
    } catch (error) {
      console.error("Error fetching cateId from href:", error);
      return null;
    }
  };

  const fetchSubCategories = async (parentCateId) => {
    if (!parentCateId) return { subCats: [], subSubCats: [] };

    try {
      const response = await axios.get("http://localhost:6789/api/v1/categories", {
        params: { page: 0, size: 10000, sort: "cateId,asc" },
        withCredentials: true,
      });
      const allCategories = response.data.content?.content || [];

      const subCats = allCategories
        .filter((cat) => cat.parentCateId && cat.parentCateId.cateId === Number(parentCateId))
        .map((cat) => cat.cateId);

      const subSubCats = allCategories
        .filter((cat) => cat.parentCateId && subCats.includes(cat.parentCateId.cateId))
        .map((cat) => cat.cateId);

      return { subCats, subSubCats };
    } catch (error) {
      console.error("Error fetching subcategories:", error);
      return { subCats: [], subSubCats: [] };
    }
  };

  const fetchBranchesWithProducts = async () => {
    setIsLoadingBranches(true);
    try {
      const response = await axios.get("http://localhost:6789/api/v1/products/productTypeBranchs", {
        params: { page: 0, size: 10000 },
        withCredentials: true,
      });
      const productTypeBranches = Array.isArray(response.data.content) ? response.data.content : [];

      const branchesWithStock = productTypeBranches
        .reduce((acc, ptb) => {
          const brchId = ptb.brchId?.brchId ?? ptb.brchId;
          const brchName = ptb.brchId?.brchName ?? "Unnamed Branch";
          const address = ptb.brchId?.address ?? "Unknown Address";
          const quantity = ptb.quantity || 0;

          if (!acc[brchId]) {
            acc[brchId] = {
              brchId,
              brchName,
              address,
              hasStock: false,
            };
          }

          if (quantity > 0) {
            acc[brchId].hasStock = true;
          }

          return acc;
        }, {});

      const branches = Object.values(branchesWithStock)
        .filter((branch) => branch.hasStock)
        .map((branch) => ({
          brchId: branch.brchId,
          brchName: branch.brchName,
          address: branch.address,
        }));

      setAvailableBranches(branches);
    } catch (error) {
      console.error("Error fetching branches with products:", error);
      setAvailableBranches([]);
    } finally {
      setIsLoadingBranches(false);
    }
  };

  const searchBranches = async (name) => {
    if (!name.trim()) {
      await fetchBranchesWithProducts();
      return;
    }
    setIsLoadingBranches(true);
    try {
      const response = await axios.get(`http://localhost:6789/api/v1/branches/search/${encodeURIComponent(name)}`, {
        withCredentials: true,
      });
      if (response.data.success) {
        const branchesData = Array.isArray(response.data.content) ? response.data.content : [response.data.content];
        const branchesWithStock = await Promise.all(
          branchesData.map(async (branch) => {
            const prodTypeIds = await fetchProductTypeIdsByBranch(branch.brchId);
            const hasStock = await Promise.all(
              prodTypeIds.map(async (prodTypeId) => {
                const quantity = await fetchBranchQuantity(prodTypeId, branch.brchId);
                return quantity > 0;
              })
            );
            return hasStock.some((stock) => stock)
              ? {
                brchId: branch.brchId,
                brchName: branch.brchName,
                address: branch.address || "Unknown Address",
              }
              : null;
          })
        );
        setAvailableBranches(branchesWithStock.filter((b) => b !== null));
      } else {
        setAvailableBranches([]);
      }
    } catch (error) {
      console.error("Error searching branches:", error);
      setAvailableBranches([]);
    } finally {
      setIsLoadingBranches(false);
    }
  };

  const handleSelectBranch = (branch) => {
    localStorage.setItem("selectedBranchId", branch.brchId);
    localStorage.setItem("selectedBranchName", branch.brchName);
    localStorage.setItem("selectedBranchAddress", branch.address);
    setBranchId(branch.brchId);
    setSelectedBranchName(branch.brchName);
    setIsBranchModalOpen(false);
    window.location.reload();
  };

  useEffect(() => {
    const initializeData = async () => {
      await fetchAllProductTypes();
      if (allProducts.length > 0) await fetchAllAttributes();
    };
    initializeData();
  }, [branchId]);

  useEffect(() => {
    const updateData = async () => {
      const savedCateId = localStorage.getItem("selectedCateId");
      const savedBranchId = localStorage.getItem("selectedBranchId") || branchId;
      const newHref = location.pathname;

      if (newHref.includes("branch")) {
        const newBranchId = newHref.split("/").pop();
        const branchName = localStorage.getItem("selectedBranchName") || "Unknown Branch";
        localStorage.setItem("selectedBranchId", newBranchId);
        localStorage.setItem("selectedCateId", "");
        localStorage.setItem("subCategoryIds", JSON.stringify([]));
        localStorage.setItem("subSubCategoryIds", JSON.stringify([]));
        setBranchId(newBranchId);
        setSelectedBranchName(branchName);
        setSelectedHref(newHref);
        setCateId(null);
        setSubCategoryIds([]);
        setSubSubCategoryIds([]);
        setTargetSubCategoryId(null);
      } else if (newHref.includes("products")) {
        const newCateId = await fetchCateIdFromHref(newHref);
        if (newCateId) {
          localStorage.setItem("selectedCateId", newCateId);
          localStorage.setItem("selectedHref", newHref);
          localStorage.setItem("selectedBranchId", savedBranchId);
          localStorage.setItem("selectedBranchName", selectedBranchName || "");
          setCateId(newCateId);
          setSelectedHref(newHref);
          setBranchId(savedBranchId);

          const { subCats, subSubCats } = await fetchSubCategories(newCateId);
          setSubCategoryIds(subCats);
          setSubSubCategoryIds(subSubCats);
          localStorage.setItem("subCategoryIds", JSON.stringify(subCats));
          localStorage.setItem("subSubCategoryIds", JSON.stringify(subSubCats));
        } else {
          localStorage.setItem("selectedCateId", "");
          localStorage.setItem("selectedBranchId", savedBranchId);
          localStorage.setItem("selectedBranchName", selectedBranchName || "");
          setCateId(null);
          setBranchId(savedBranchId);
          setSelectedBranchName(selectedBranchName || null);
          setSelectedHref(null);
          setSubCategoryIds([]);
          setSubSubCategoryIds([]);
          setTargetSubCategoryId(null);
        }
      } else if (savedCateId) {
        setCateId(savedCateId);
        const { subCats, subSubCats } = await fetchSubCategories(savedCateId);
        setSubCategoryIds(subCats);
        setSubSubCategoryIds(subSubCats);
        localStorage.setItem("subCategoryIds", JSON.stringify(subCats));
        localStorage.setItem("subSubCategoryIds", JSON.stringify(subSubCats));
      }

      if (allProducts.length > 0 && attributes.length === 0 && !isLoadingAttributes) {
        await fetchAllAttributes();
      }

      if (allProducts.length > 0) {
        let filteredProducts = allProducts;

        if (branchId && (!newHref.includes("products") || newHref.includes("branch"))) {
          const prodTypeIds = await fetchProductTypeIdsByBranch(branchId);
          filteredProducts = filteredProducts.filter((product) =>
            prodTypeIds.includes(product.prodId)
          );
        }

        if (cateId) {
          const allCateIds = [Number(cateId), ...subCategoryIds, ...subSubCategoryIds];
          filteredProducts = filteredProducts.filter((product) =>
            allCateIds.includes(product.categoryId)
          );

          if (targetSubCategoryId) {
            filteredProducts = filteredProducts.filter((product) =>
              product.categoryId === targetSubCategoryId
            );
          }
        }

        setProducts(filteredProducts);

        // Check if no products are available after filtering
        if (filteredProducts.length === 0 && !isLoading && !isLoadingAttributes) {
          await fetchBranchesWithProducts();
        }
      }
    };

    updateData();
  }, [location.pathname, allProducts, cateId, targetSubCategoryId]);

  const formatPrice = (price) => {
    return Math.floor(price).toLocaleString("vi-VN") + " VNĐ";
  };

  useEffect(() => {
    const generateBreadcrumb = () => {
      const items = [{ title: "Home", href: "/" }];

      if (selectedHref) {
        const pathSegments = selectedHref
          .split("/")
          .filter((segment) => segment && segment.toLowerCase() !== "products");

        if (pathSegments[0] !== "branch") {
          pathSegments.forEach((segment, index) => {
            const href = `/products/${pathSegments.slice(0, index + 1).join("/")}`;
            items.push({
              title: decodeURIComponent(segment),
              href,
            });
          });
        } else {
          items.push({
            title: "Smartphones",
            href: "/products/Smartphones",
          });
        }
      }

      setBreadcrumbItems(items);
    };

    generateBreadcrumb();
  }, [selectedHref]);

  const handleBreadcrumbClick = async (href) => {
    if (href === "/") {
      setCateId(null);
      setTargetSubCategoryId(null);
      setSubCategoryIds([]);
      setSubSubCategoryIds([]);
      localStorage.setItem("selectedCateId", "");
      localStorage.setItem("subCategoryIds", JSON.stringify([]));
      localStorage.setItem("subSubCategoryIds", JSON.stringify([]));
      setSelectedHref(null);
      navigate("/");
      return;
    }

    const newCateId = await fetchCateIdFromHref(href);
    if (newCateId) {
      setCateId(newCateId);
      localStorage.setItem("selectedCateId", newCateId);
      setSelectedHref(href);

      const { subCats, subSubCats } = await fetchSubCategories(newCateId);
      setSubCategoryIds(subCats);
      setSubSubCategoryIds(subSubCats);
      localStorage.setItem("subCategoryIds", JSON.stringify(subCats));
      localStorage.setItem("subSubCategoryIds", JSON.stringify(subSubCats));

      const pathSegments = href.split("/").filter((segment) => segment && segment !== "products");
      if (pathSegments.length > 1) {
        const subCateName = decodeURIComponent(pathSegments[1]);
        const response = await axios.get("http://localhost:6789/api/v1/categories", {
          params: { page: 0, size: 10000, sort: "cateId,asc" },
          withCredentials: true,
        });
        const categories = response.data.content?.content || [];
        const subCategory = categories.find(
          (cat) => cat.cateName === subCateName && cat.parentCateId?.cateId === newCateId
        );
        setTargetSubCategoryId(subCategory ? subCategory.cateId : null);
      } else {
        setTargetSubCategoryId(null);
      }

      navigate(href);
    }
  };

  const availableBrands = [...new Set(products.map((p) => p.brand))];

  const availableAttributes = useMemo(() => {
    const attrMap = {};
    products.forEach((product) => {
      product.attributes.forEach((attr) => {
        const atbName = attr.atbId.atbName;
        const atbValue = attr.prodAtbValue;
        if (!attrMap[atbName]) {
          attrMap[atbName] = new Set();
        }
        attrMap[atbName].add(atbValue);
      });
    });
    return Object.entries(attrMap).map(([name, values]) => ({
      name,
      values: Array.from(values),
    }));
  }, [products]);

  const toggleBrand = (brand) => {
    setSelectedBrands((prev) => {
      const newSet = new Set(prev);
      newSet.has(brand) ? newSet.delete(brand) : newSet.add(brand);
      return newSet;
    });
  };

  const toggleAttribute = (atbName, atbValue) => {
    setSelectedAttributes((prev) => {
      const currentValues = prev[atbName] || new Set();
      const newValues = new Set(currentValues);
      newValues.has(atbValue) ? newValues.delete(atbValue) : newValues.add(atbValue);
      return { ...prev, [atbName]: newValues };
    });
  };

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const brandMatch = selectedBrands.size === 0 || selectedBrands.has(product.brand);
      const priceMatch = product.unitPrice >= minPrice && product.unitPrice <= maxPrice;
      const attributeMatch = Object.entries(selectedAttributes).every(([atbName, selectedValues]) => {
        if (selectedValues.size === 0) return true;
        const productAttr = product.attributes.find((attr) => attr.atbId.atbName === atbName);
        return productAttr && selectedValues.has(productAttr.prodAtbValue);
      });

      return brandMatch && priceMatch && attributeMatch;
    });
  }, [products, selectedBrands, minPrice, maxPrice, selectedAttributes]);

  const sortedProducts = useMemo(() => {
    const sorted = [...filteredProducts].sort((a, b) => {
      if (a.quantity > 0 && b.quantity === 0) return -1;
      if (a.quantity === 0 && b.quantity > 0) return 1;

      if (priceSort === "asc") {
        return a.unitPrice - b.unitPrice;
      } else if (priceSort === "desc") {
        return b.unitPrice - a.unitPrice;
      }

      return 0;
    });
    return sorted;
  }, [filteredProducts, priceSort]);

  const handleProductClick = async (prodId) => {
    try {
      localStorage.setItem("selectedProdTypeId", prodId);
      navigate(`/product/${encodeURIComponent(prodId)}`);
    } catch (error) {
      console.error("Error during product click:", error);
      localStorage.setItem("selectedProdTypeId", prodId);
      navigate(`/product/${encodeURIComponent(prodId)}`);
    }
  };

  const handleMinPriceChange = (e) => {
    const value = Number(e.target.value);
    if (value <= maxPrice) setMinPrice(value);
  };

  const handleMaxPriceChange = (e) => {
    const value = Number(e.target.value);
    if (value >= minPrice) setMaxPrice(value);
  };

  const handlePriceSortChange = (e) => {
    setPriceSort(e.target.value || null);
  };

  return (
    <div className="bg-[#f8f8fc] text-gray-900 min-w-screen min-h-screen flex flex-col" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
      <header className="h-20 bg-blue-700 text-white flex items-center px-4 shadow-md">
        <Header />
      </header>

      <main className="container mx-auto p-6">
        <nav className="text-gray-700 mb-6 bg-gray-100 rounded-lg p-2 inline-block">
          <ol className="list-reset flex items-center space-x-2">
            {breadcrumbItems.map((item, index) => (
              <li key={item.href} className="flex items-center">
                {index > 0 && <span className="mx-2">/</span>}
                <a
                  href={item.href}
                  className="text-blue-600 hover:underline"
                  onClick={(e) => {
                    e.preventDefault();
                    handleBreadcrumbClick(item.href);
                  }}
                >
                  {item.title}
                </a>
              </li>
            ))}
          </ol>
        </nav>

        <div className="flex">
          <aside className="w-1/4 p-4 bg-white shadow rounded-md">
            <h3 className="text-xl font-semibold mb-4">Filter</h3>
            {isLoading ? (
              <p>Loading filters...</p>
            ) : (
              <>
                <div>
                  <h4 className="font-medium">Manufacturer</h4>
                  <ul className="mt-2 space-y-2">
                    {availableBrands.slice(0, showAllBrands ? availableBrands.length : 5).map((brand) => (
                      <li key={brand}>
                        <label className="flex items-center space-x-2">
                          <input
                            type="checkbox"
                            checked={selectedBrands.has(brand)}
                            onChange={() => toggleBrand(brand)}
                          />
                          <span>{brand}</span>
                        </label>
                      </li>
                    ))}
                  </ul>
                  {availableBrands.length > 5 && (
                    <button
                      className="text-blue-600 hover:underline mt-2 text-sm"
                      onClick={() => setShowAllBrands(!showAllBrands)}
                    >
                      {showAllBrands ? "Show Less" : "Show More"}
                    </button>
                  )}
                </div>

                {isLoadingAttributes ? (
                  <p>Loading attributes...</p>
                ) : availableAttributes.length > 0 ? (
                  availableAttributes.map((attr) => (
                    <div key={attr.name} className="mt-4">
                      <h4 className="font-medium">{attr.name}</h4>
                      <ul className="mt-2 space-y-2">
                        {attr.values.map((value) => (
                          <li key={value}>
                            <label className="flex items-center space-x-2">
                              <input
                                type="checkbox"
                                checked={selectedAttributes[attr.name]?.has(value) || false}
                                onChange={() => toggleAttribute(attr.name, value)}
                              />
                              <span>{value}</span>
                            </label>
                          </li>
                        ))}
                      </ul>
                    </div>
                  ))
                ) : (
                  <p>No attributes available.</p>
                )}

                <div className="mt-4">
                  <h4 className="font-medium mb-2">Sort by Price</h4>
                  <select
                    value={priceSort || ""}
                    onChange={handlePriceSortChange}
                    className="w-full p-2 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">Default</option>
                    <option value="asc">Price: Low to High</option>
                    <option value="desc">Price: High to Low</option>
                  </select>

                  <h4 className="font-medium mb-2 mt-4">Price Range</h4>
                  <div className="space-y-4">
                    <div>
                      <input
                        type="range"
                        min="0"
                        max={priceRangeMax}
                        value={minPrice}
                        onChange={handleMinPriceChange}
                        className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                      />
                      <p className="text-sm text-gray-600 mt-1">
                        Min: {minPrice.toLocaleString('vi-VN')} VNĐ
                      </p>
                    </div>
                    <div>
                      <input
                        type="range"
                        min="0"
                        max={priceRangeMax}
                        value={maxPrice}
                        onChange={handleMaxPriceChange}
                        className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                      />
                      <p className="text-sm text-gray-600 mt-1">
                        Max: {maxPrice.toLocaleString('vi-VN')} VNĐ
                      </p>
                    </div>
                  </div>
                </div>
                <button
                  className="w-full bg-yellow-500 text-white py-2 rounded-lg hover:bg-yellow-600 transition duration-300 text-sm sm:text-base mt-5"
                  onClick={handleNavigation}
                >
                  Compare
                </button>
              </>
            )}
          </aside>

          <div className="w-full md:w-3/4 px-6">
            <div
              className="flex flex-wrap gap-6 justify-start bg-white bg-opacity-50 rounded-lg shadow-md p-6"
              style={{ backdropFilter: "blur(12px)" }}
            >
              {isLoading ? (
                <p className="text-center w-full">Loading products...</p>
              ) : isLoadingAttributes ? (
                <p className="text-center w-full">Loading attributes...</p>
              ) : sortedProducts.length > 0 ? (
                sortedProducts.map((product, index) => {
                  const savingsAmount = product.prodTypePrice - product.unitPrice;
                  return (
                    <Card
                      key={`${product.prodId}-${index}`}
                      className={`w-[235px] h-[400px] cursor-pointer hover:shadow-lg transition flex flex-col relative ${product.quantity === 0 ? "grayscale" : ""}`}
                      onClick={() => handleProductClick(product.prodId)}
                    >
                      <div className="flex-1 flex items-center justify-center p-2 relative">
                        <img
                          src={product.image}
                          alt={product.prodName}
                          className="w-full h-[180px] object-contain"
                          loading="lazy"
                          onError={(e) => {
                            if (e.target.src !== "/images/placeholder.jpg") {
                              e.target.src = "/images/placeholder.jpg";
                            }
                          }}
                        />
                        {product.quantity > 0 && savingsAmount > 0 && (
                          <div className="absolute bottom-2 left-2 bg-gradient-to-r from-purple-500 to-blue-500 text-xs font-semibold px-2 py-1 rounded flex flex-col items-start">
                            <span className="text-yellow-400">SAVED</span>
                            <span className="text-white">{formatPrice(product.prodTypePrice - product.unitPrice)}</span>
                          </div>
                        )}
                      </div>
                      <CardHeader className="p-2">
                        <CardTitle className="text-base font-medium line-clamp-2">
                          {product.prodName}
                        </CardTitle>
                      </CardHeader>
                      <CardContent className="p-2">
                        {product.quantity > 0 ? (
                          <>
                            <p style={{ fontSize: '20px' }} className="text-blue-800 font-semibold">
                              {product.unitPrice ? formatPrice(product.unitPrice) : "N/A"}
                            </p>
                            {product.unitPrice !== product.prodTypePrice && (
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
                          </>
                        ) : (
                          <p className="text-gray-500 font-semibold text-sm">Out of Stock</p>
                        )}
                      </CardContent>
                    </Card>
                  );
                })
              ) : (
                <div className="text-center w-full">
                  <p className="text-gray-700 mb-4">
                    No products available at {selectedBranchName || "this branch"}.
                  </p>
                  <Button
                    className="bg-blue-500 text-white hover:bg-blue-600"
                    onClick={() => setIsBranchModalOpen(true)}
                  >
                    Switch Branch
                  </Button>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>

      <AnimatePresence>
        {isBranchModalOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
          >
            <motion.div
              initial={{ scale: 0.9 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.9 }}
              className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md"
            >
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-bold text-gray-800">Select Branch</h2>
                <button
                  onClick={() => setIsBranchModalOpen(false)}
                  className="text-gray-500 hover:text-red-600 transition-colors duration-200"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              <div className="relative mb-4">
                <input
                  type="text"
                  placeholder="Search for a branch..."
                  value={branchSearchQuery}
                  onChange={(e) => {
                    setBranchSearchQuery(e.target.value);
                    searchBranches(e.target.value);
                  }}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 text-gray-700 placeholder-gray-400 text-sm"
                />
                <Search className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
              </div>
              <ul className="space-y-2 max-h-64 overflow-y-auto">
                {isLoadingBranches ? (
                  <li className="text-center text-gray-500 text-sm py-2">Loading branches...</li>
                ) : availableBranches.length > 0 ? (
                  availableBranches.map((branch) => (
                    <li
                      key={branch.brchId}
                      className={cn(
                        "p-2 rounded-md cursor-pointer transition-all duration-200 text-sm",
                        Number(localStorage.getItem("selectedBranchId")) === branch.brchId
                          ? "bg-blue-100 text-blue-700 font-semibold"
                          : "hover:bg-gray-100 text-gray-700"
                      )}
                      onClick={() => handleSelectBranch(branch)}
                    >
                      <span>{branch.brchName || "Unnamed Branch"}</span>
                      <p className="text-xs text-gray-500">{branch.address || "No address"}</p>
                    </li>
                  ))
                ) : (
                  <li className="text-center text-gray-500 text-sm py-2">No branches with products found</li>
                )}
              </ul>
              <Button
                onClick={() => setIsBranchModalOpen(false)}
                className="mt-4 w-full bg-gray-300 text-gray-800 hover:bg-gray-400"
              >
                Cancel
              </Button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      <Footer />
    </div>
  );
};

export default ProductPage;