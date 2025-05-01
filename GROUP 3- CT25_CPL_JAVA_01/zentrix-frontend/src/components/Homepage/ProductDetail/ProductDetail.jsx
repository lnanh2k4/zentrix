import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import { ChevronLeft, ChevronRight, Search } from "lucide-react";
import { cn } from "@/lib/utils";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import ReviewProduct from "./ReviewProduct";
import { showNotification } from "@/components/Dashboard/NotificationPopup";
import Modal from "react-modal";
import { Loader2 } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";

// Bind modal to your appElement for accessibility
Modal.setAppElement("#root");

const ProductDetail = () => {
    const { prodId } = useParams();
    const location = useLocation();
    const navigate = useNavigate();
    const [images, setImages] = useState([]);
    const [product, setProduct] = useState();
    const [selectedImage, setSelectedImage] = useState(null);
    const [currentIndex, setCurrentIndex] = useState(0);
    const [variations, setVariation] = useState([]);
    const [attributes, setAttributes] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [selectedProdTypeVariation, setSelectedProdTypeVariation] = useState(null);
    const [selectedVariations, setSelectedVariations] = useState({});
    const [userInfo, setUserInfo] = useState({ userId: null });
    const { state } = useLocation();
    const [isReviewNow, setIsReviewNow] = useState(false);
    const reviewComponentRef = useRef();
    const [branchQuantity, setBranchQuantity] = useState(null);
    const [availableBranches, setAvailableBranches] = useState([]); // Updated to store all branches with stock
    const [isBranchModalOpen, setIsBranchModalOpen] = useState(false); // New state for branch modal
    const [branchSearchQuery, setBranchSearchQuery] = useState(""); // Search query for branches
    const [isLoadingBranches, setIsLoadingBranches] = useState(false); // Loading state for branches

    // State for large quantity request modal and form
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [currentItem, setCurrentItem] = useState(null);
    const [formData, setFormData] = useState({
        companyName: "",
        customerName: "",
        phoneNumber: "",
        email: "",
        quantity: 5,
        note: "",
    });
    const [formErrors, setFormErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    // State for My Promotions
    const [userPromotions, setUserPromotions] = useState([]);

    useEffect(() => {
        if (state && state.isReviewNow && reviewComponentRef.current) {
            reviewComponentRef.current.scrollIntoView({
                behavior: "smooth",
                block: "start",
            });
            setIsReviewNow(true);
            setTimeout(() => {
                setIsReviewNow(false);
            }, 4000);
        }
    }, [state]);

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success && response.data.content) {
                setUserInfo({ userId: response.data.content.userId || null });
                return response.data.content.userId;
            } else {
                setUserInfo({ userId: null });
                return null;
            }
        } catch (error) {
            console.error("Error fetching user info:", error);
            setUserInfo({ userId: null });
            return null;
        }
    };

    const fetchBranchQuantity = async (prodTypeId, brchId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/productTypeBranch/${prodTypeId}`, {
                withCredentials: true,
            });
            // Find current branch quantity
            const branchData = response.data.find((item) => item.brchId?.brchId === brchId);
            // Find all branches with stock
            const branchesWithStock = response.data
                .filter((item) => item.quantity > 0)
                .map((item) => ({
                    brchId: item.brchId.brchId,
                    brchName: item.brchId.brchName,
                    address: item.brchId.address || "Unknown Address",
                    quantity: item.quantity,
                }));
            setAvailableBranches(branchesWithStock);
            return branchData ? branchData.quantity : 0;
        } catch (error) {
            console.error("Error fetching branch quantity:", error);
            setAvailableBranches([]);
            return 0;
        }
    };

    const searchBranches = async (name, prodTypeId) => {
        if (!name.trim()) {
            // Re-fetch branches with stock
            await fetchBranchQuantity(prodTypeId, Number(localStorage.getItem("selectedBranchId")) || 5);
            return;
        }
        setIsLoadingBranches(true);
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/branches/search/${encodeURIComponent(name)}`, {
                withCredentials: true,
            });
            if (response.data.success) {
                const branchesData = Array.isArray(response.data.content) ? response.data.content : [response.data.content];
                // Filter branches to only those with stock for this product
                const branchesWithStock = await Promise.all(
                    branchesData.map(async (branch) => {
                        const quantity = await fetchBranchQuantity(prodTypeId, branch.brchId);
                        return quantity > 0 ? { ...branch, quantity } : null;
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
        setIsBranchModalOpen(false);
        window.location.reload();
    };

    const fetchCartItems = async (userId, page = 0, size = 10) => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/cart", {
                params: { userId, page, size },
                withCredentials: true,
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching cart items:", error);
            throw error;
        }
    };

    const fetchCartItemsDetails = async (cartId, userId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/cart/items`, {
                params: { userId, cartId },
                withCredentials: true,
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching cart items details:", error);
            throw error;
        }
    };

    const fetchProduct = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/productTypes/${prodTypeId}`, {
                withCredentials: true,
            });
            if (response.data.status == 3) {
                navigate('/404')
            }
            return [response.data];
        } catch (error) {
            console.error("Error fetching product:", error);
            return null;
        }
    };

    const fetchAllImageProductType = async (id) => {
        try {
            const productId = id || location.pathname.split("/")[2];
            if (!productId) throw new Error("No product ID provided");
            const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${productId}`);
            const imageUrls = [];
            if (Array.isArray(response.data) && response.data.length > 0) {
                response.data.forEach((image) => {
                    let url = image.imageId?.imageLink || image.imageLink || image || "/images/placeholder.jpg";
                    if (url && !url.startsWith("http")) {
                        url = `http://localhost:6789${url.startsWith("/") ? "" : "/"}${url}`;
                    }
                    imageUrls.push(url);
                });
                return imageUrls;
            } else if (!Array.isArray(response.data) && response.data) {
                let url = response.data.imageId?.imageLink || response.data.imageLink || response.data || "/images/placeholder.jpg";
                if (url && !url.startsWith("http")) {
                    url = `http://localhost:6789${url.startsWith("/") ? "" : "/"}${url}`;
                }
                imageUrls.push(url);
                return imageUrls;
            }
            return ["/images/placeholder.jpg"];
        } catch (error) {
            console.error("Error fetching all product images:", error);
            return ["/images/placeholder.jpg"];
        }
    };

    const fetchProductTypeVariation = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/productTypeVariation/${prodTypeId}`);
            return response.data || [];
        } catch (error) {
            console.error("Error fetching product variation:", error);
            return [];
        }
    };

    const fetchProductTypeAttribute = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/productTypeAttribute/${prodTypeId}`);
            return response.data || [];
        } catch (error) {
            console.error("Error fetching product attribute:", error);
            return [];
        }
    };

    const fetchUserPromotions = async (userId) => {
        if (!userId) {
            setUserPromotions([]);
            return;
        }
        try {
            const response = await axios.get("http://localhost:6789/api/v1/promotions/my-promotions", {
                params: { userId },
                withCredentials: true,
            });
            if (response.data.success) {
                const userPromoData = response.data.content.map((userPromo) => ({
                    userPromId: userPromo.userPromId,
                    promId: userPromo.promId.promId,
                    status: userPromo.status,
                }));
                await fetchPromotionsByIds(userPromoData);
            } else {
                setUserPromotions([]);
            }
        } catch (error) {
            console.error("Error fetching user promotions:", error.response?.data || error.message);
            setUserPromotions([]);
        }
    };

    const fetchPromotionsByIds = async (userPromoData) => {
        if (!userPromoData.length) {
            setUserPromotions([]);
            return;
        }
        try {
            const promoPromises = userPromoData.map((userPromo) =>
                axios.get(`http://localhost:6789/api/v1/promotions/${userPromo.promId}`, {
                    withCredentials: true,
                })
            );
            const responses = await Promise.all(promoPromises);
            const promotionData = responses
                .filter((response) => response.data.success)
                .map((response, index) => ({
                    userPromId: userPromoData[index].userPromId,
                    promId: response.data.content.promId,
                    promotionName: response.data.content.promName,
                    discount: response.data.content.discount,
                    expiryDate: response.data.content.endDate,
                    quantity: response.data.content.quantity,
                    promStatus: response.data.content.promStatus,
                    status: userPromoData[index].status,
                }))
                .filter((promo) => promo.status === 1 && promo.promStatus === 1);
            setUserPromotions(promotionData.sort((a, b) => b.discount - a.discount));
        } catch (error) {
            console.error("Error fetching promotions by IDs:", error.response?.data || error.message);
            setUserPromotions([]);
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            try {
                setIsLoading(true);
                const [imageData] = await Promise.all([fetchAllImageProductType(prodId)]);

                const prodTypeId = location.pathname.split("/")[2];
                if (!prodTypeId) throw new Error("No prodTypeId found in product data");

                const [variationData, attributeData, productData] = await Promise.all([
                    fetchProductTypeVariation(prodTypeId),
                    fetchProductTypeAttribute(prodTypeId),
                    fetchProduct(prodTypeId),
                ]);
                const selectedBranchId = Number(localStorage.getItem("selectedBranchId")) || 5;
                const quantity = await fetchBranchQuantity(prodTypeId, selectedBranchId);

                setImages(imageData || []);
                setSelectedImage(imageData?.[0] || "/images/placeholder.jpg");
                setProduct(productData);
                setVariation(variationData || []);
                setAttributes(attributeData || []);
                setBranchQuantity(quantity);

                const grouped = variationData.reduce((acc, variation) => {
                    const variId = variation.variId?.variId;
                    if (!acc[variId]) {
                        acc[variId] = { variName: variation.variId?.variName || "Unknown", values: [] };
                    }
                    acc[variId].values.push(variation);
                    return acc;
                }, {});

                const defaultSelections = {};
                let defaultProdTypeVariId = null;

                Object.entries(grouped).forEach(([variId, group]) => {
                    if (group.values.length === 1) {
                        defaultSelections[variId] = group.values[0].prodTypeValue;
                        defaultProdTypeVariId = group.values[0].prodTypeVariId;
                    } else {
                        const defaultVariation = group.values.find((v) => v.defaultVari === 1);
                        if (defaultVariation) {
                            defaultSelections[variId] = defaultVariation.prodTypeValue;
                            defaultProdTypeVariId = defaultVariation.prodTypeVariId;
                        } else {
                            defaultSelections[variId] = group.values[0].prodTypeValue;
                            defaultProdTypeVariId = group.values[0].prodTypeVariId;
                        }
                    }
                });

                setSelectedVariations(defaultSelections);
                setSelectedProdTypeVariation(defaultProdTypeVariId);
            } catch (error) {
                console.error("Error fetching data:", error);
                setImages(["/images/placeholder.jpg"]);
                setSelectedImage("/images/placeholder.jpg");
                setVariation([]);
                setAttributes([]);
                setBranchQuantity(0);
            } finally {
                setIsLoading(false);
            }
        };

        const initializeData = async () => {
            const userId = await fetchUserInfo();
            if (userId) {
                await fetchUserPromotions(userId);
            }
            await fetchData();
        };
        initializeData();
    }, [prodId, location.pathname]);

    const formatPrice = (price) => {
        return Math.floor(price).toLocaleString("vi-VN") + " VNĐ";
    };

    const fetchImageProductType = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`);
            if (Array.isArray(response.data) && response.data.length > 0) {
                return response.data[0].imageId?.imageLink || response.data[0].imageLink || response.data[0];
            } else if (!Array.isArray(response.data) && response.data) {
                return response.data.imageId?.imageLink || response.data.imageLink || response.data;
            }
            return "";
        } catch (error) {
            console.error("Error fetching product images:", error);
            return "";
        }
    };

    const handleSelectVariation = (variId, prodTypeValue, prodTypeVariId) => {
        setSelectedVariations((prev) => ({ ...prev, [variId]: prodTypeValue }));
        setSelectedProdTypeVariation(prodTypeVariId);
    };

    const validateForm = () => {
        const errors = {};

        if (!formData.companyName.trim()) {
            errors.companyName = "Tên công ty là bắt buộc";
        }

        if (!formData.customerName.trim()) {
            errors.customerName = "Tên quý khách là bắt buộc";
        }

        const phoneRegex = /^0\d{9}$/;
        if (!formData.phoneNumber.trim()) {
            errors.phoneNumber = "Số điện thoại là bắt buộc";
        } else if (!phoneRegex.test(formData.phoneNumber)) {
            errors.phoneNumber = "Vui lòng nhập số điện thoại hợp lệ (ví dụ: 0123456789)";
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!formData.email.trim()) {
            errors.email = "Địa chỉ email là bắt buộc";
        } else if (!emailRegex.test(formData.email)) {
            errors.email = "Vui lòng nhập địa chỉ email hợp lệ";
        }

        if (formData.quantity < 1) {
            errors.quantity = "Số lượng phải lớn hơn 0";
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleFormChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: value,
        }));
        setFormErrors((prev) => ({
            ...prev,
            [name]: "",
        }));
    };

    const handleFormSubmit = async () => {
        if (!validateForm()) {
            return;
        }

        setIsSubmitting(true);
        try {
            const requestData = {
                companyName: formData.companyName,
                customerName: formData.customerName,
                phoneNumber: formData.phoneNumber,
                email: formData.email,
                productTypeId: currentItem.prodTypeId,
                quantity: formData.quantity,
                note: formData.note,
            };

            await axios.post("http://localhost:6789/api/v1/cart/send-large-quantity-request", null, {
                params: requestData,
                withCredentials: true,
            });

            showNotification("Your request has been emailed successfully!", 3000, "complete");
            setIsModalOpen(false);
            setFormData({
                companyName: "",
                customerName: "",
                phoneNumber: "",
                email: "",
                quantity: 5,
                note: "",
            });
        } catch (error) {
            console.error("Error sending large quantity request via email:", error.response?.data || error.message);
            showNotification("Không thể gửi yêu cầu qua email. Vui lòng thử lại sau.", 3000, "fail");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleAddToCart = async () => {
        try {
            if (!variations || variations.length === 0) {
                showNotification("No product variations available to add to cart.", 3000, "fail");
                return;
            }

            const userId = userInfo.userId;
            const quantity = parseInt(document.getElementById("quantityInput")?.value) || 1;

            if (!userId) {
                showNotification("Cannot add to cart: User not logged in. Please log in to continue.", 3000, "fail");
                return;
            }

            if (quantity <= 0) {
                showNotification("Quantity must be greater than 0.", 3000, "fail");
                return;
            }

            const groupedVariations = variations.reduce((acc, variation) => {
                const variId = variation.variId?.variId;
                if (!acc[variId]) {
                    acc[variId] = { variName: variation.variId?.variName || "Unknown", values: [] };
                }
                acc[variId].values.push(variation);
                return acc;
            }, {});

            const selectedProductTypeVariations = [];
            Object.entries(groupedVariations).forEach(([variId, group]) => {
                const selectedValue = selectedVariations[variId];
                const selectedVariation = group.values.find((v) => v.prodTypeValue === selectedValue);
                if (selectedVariation) {
                    selectedProductTypeVariations.push(selectedVariation);
                }
            });

            if (selectedProductTypeVariations.length !== Object.keys(groupedVariations).length) {
                showNotification("Please select a value for each variation type.", 3000, "fail");
                return;
            }

            let cartId;
            try {
                const cartResponse = await fetchCartItems(userId);
                if (cartResponse.success && cartResponse.content.length > 0) {
                    cartId = cartResponse.content[0].cartId;
                } else {
                    const createCartResponse = await axios.post(
                        "http://localhost:6789/api/v1/cart/new",
                        null,
                        {
                            params: { userId },
                            headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                            withCredentials: true,
                        }
                    );

                    if (createCartResponse.data.success) {
                        cartId = createCartResponse.data.content.cartId;
                    } else {
                        throw new Error("Failed to create cart: " + createCartResponse.data.message);
                    }
                }
            } catch (error) {
                console.error("Error fetching or creating cart:", error);
                showNotification("An error occurred while fetching or creating cart.", 3000, "fail");
                return;
            }

            let currentCartItems = [];
            if (cartId) {
                const itemsResponse = await fetchCartItemsDetails(cartId, userId);
                if (itemsResponse.success && itemsResponse.content) {
                    currentCartItems = itemsResponse.content.map((item) => ({
                        prodTypeId: item.prodTypeVariId?.prodTypeId?.prodTypeId,
                        prodTypeVariId: item.prodTypeVariId?.prodTypeVariId,
                        quantity: item.quantity,
                    }));
                }
            }

            const prodTypeId = product?.[0]?.prodTypeId;
            const prodTypeVariId = selectedProductTypeVariations[0]?.prodTypeVariId;
            const existingItems = currentCartItems.filter(
                (item) => item.prodTypeId === prodTypeId && item.prodTypeVariId === prodTypeVariId
            );
            const currentQuantity = existingItems.reduce((sum, item) => sum + item.quantity, 0);
            const totalQuantity = currentQuantity + quantity;

            if (totalQuantity > 5) {
                setCurrentItem({
                    prodTypeId: prodTypeId,
                    name: product?.[0]?.prodTypeName,
                });
                setFormData((prev) => ({
                    ...prev,
                    quantity: totalQuantity,
                }));
                setIsModalOpen(true);
                return;
            }

            let successCount = 0;
            console.log("debug for add to cart: ", selectedProductTypeVariations)
            const variCode = selectedProductTypeVariations
                .map(item => item.prodTypeVariId)
                .join(',');
            console.log("debug for add to cart variCode: ", variCode)
            for (const variation of selectedProductTypeVariations) {
                const productTypeVariId = variation.prodTypeVariId;
                console.log(selectedProductTypeVariations)
                try {
                    const response = await axios.post(
                        "http://localhost:6789/api/v1/cart/add",
                        null,
                        {
                            params: { userId, cartId, productTypeVariId, quantity, variCode },
                            headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                            withCredentials: true,
                        }
                    );
                    if (response.data.success) {
                        successCount++;
                    }
                } catch (error) {
                    console.error(`Error adding variation ${productTypeVariId} to cart:`, error);
                }
            }

            if (successCount === selectedProductTypeVariations.length) {
                showNotification("Added to cart successfully!", 3000, "complete");
                window.dispatchEvent(new Event("cartUpdated"));
            } else {
                showNotification(
                    `Added ${successCount} out of ${selectedProductTypeVariations.length} variations to cart. Some variations failed to add.`,
                    3000,
                    "fail"
                );
            }
        } catch (error) {
            console.error("Error in handleAddToCart:", error);
            showNotification("An error occurred while processing your request.", 3000, "fail");
        }
    };

    const handleBuyNow = async () => {
        try {
            if (!variations || variations.length === 0) {
                showNotification("No product variations available to buy.", 3000, "fail");
                return;
            }

            const userId = userInfo.userId;
            const quantity = parseInt(document.getElementById("quantityInput")?.value) || 1;

            if (!userId) {
                showNotification("Cannot proceed: User not logged in. Please log in to continue.", 3000, "fail");
                return;
            }

            if (quantity <= 0) {
                showNotification("Quantity must be greater than 0.", 3000, "fail");
                return;
            }

            const groupedVariations = variations.reduce((acc, variation) => {
                const variId = variation.variId?.variId;
                if (!acc[variId]) {
                    acc[variId] = { variName: variation.variId?.variName || "Unknown", values: [] };
                }
                acc[variId].values.push(variation);
                return acc;
            }, {});

            const selectedProductTypeVariations = [];
            Object.entries(groupedVariations).forEach(([variId, group]) => {
                const selectedValue = selectedVariations[variId];
                const selectedVariation = group.values.find((v) => v.prodTypeValue === selectedValue);
                if (selectedVariation) {
                    selectedProductTypeVariations.push(selectedVariation);
                }
            });

            if (selectedProductTypeVariations.length !== Object.keys(groupedVariations).length) {
                showNotification("Please select a value for each variation type.", 3000, "fail");
                return;
            }

            const selectedVariation = selectedProductTypeVariations[0];
            const tempCartItem = {
                key: `temp-${product[0].prodTypeId}-${selectedVariation.prodTypeVariId}`,
                baseKey: `${product[0].prodTypeName}-${product[0].prodTypeId}`,
                name: product[0].prodTypeName || "Unnamed Product",
                originalPrice: product[0].prodTypePrice || 0,
                salePrice: product[0].unitPrice || product[0].prodTypePrice || 0,
                quantity: quantity,
                vat: product[0].prodId?.vat || 0,
                prodTypeId: product[0].prodTypeId,
                prodTypeVariId: selectedVariation.prodTypeVariId,
                urlImage: await fetchImageProductType(product[0].prodTypeId),
                variations: selectedProductTypeVariations.map((variation) => ({
                    id: variation.prodTypeVariId,
                    variationName: variation.variId?.variName || "Unknown",
                    variationValue: variation.prodTypeValue || "N/A",
                })),
                cartProductTypeVariationIds: selectedProductTypeVariations.map((v) => v.prodTypeVariId),
            };

            const totalAmount = tempCartItem.salePrice * tempCartItem.quantity;

            navigate("/orderPage", {
                state: { cart: [tempCartItem], userId, totalAmount },
            });
        } catch (error) {
            console.error("Error in handleBuyNow:", error);
            showNotification("Failed to process Buy Now. Please try again.", 3000, "fail");
        }
    };

    const handlePrev = () => {
        const newIndex = (currentIndex - 1 + images.length) % images.length;
        setSelectedImage(images[newIndex]);
        setCurrentIndex(newIndex);
    };

    const handleNext = () => {
        const newIndex = (currentIndex + 1) % images.length;
        setSelectedImage(images[newIndex]);
        setCurrentIndex(newIndex);
    };

    const groupedVariations =
        variations.length > 0
            ? variations.reduce((acc, variation) => {
                const variId = variation.variId?.variId;
                if (!acc[variId]) {
                    acc[variId] = { variName: variation.variId?.variName || "Unknown", values: [] };
                }
                acc[variId].values.push(variation);
                return acc;
            }, {})
            : {};

    const handleAddToCompare = () => {
        const productData = product?.[0];
        if (!productData) return;

        const existing = JSON.parse(localStorage.getItem("compareList")) || [];
        const isExist = existing.find((p) => p.prodTypeId === productData.prodTypeId);

        if (isExist) {
            showNotification("The product is already in the comparison list", 3000, "error");
            return;
        }

        if (existing.length >= 5) {
            showNotification("Only a maximum of 5 products can be compared.", 3000, "error");
            return;
        }

        const productToSave = {
            prodId: productData.prodId,
            prodTypeId: productData.prodTypeId,
            name: productData.prodTypeName,
            price: productData.unitPrice || productData.prodTypePrice,
            image: images?.[0] || null,
        };

        localStorage.setItem("compareList", JSON.stringify([...existing, productToSave]));
        navigate("/compare-products");
    };

    const calculateDiscountedPrice = (originalPrice, discount) => {
        return Math.floor(originalPrice * (1 - discount / 100));
    };

    return (
        <div className="bg-gray-100 min-h-screen" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="h-20 bg-blue-700 text-white flex items-center px-4 shadow-md">
                <Header />
            </header>
            <div className="container mx-auto p-4 sm:p-6">
                <nav className="text-gray-700 mb-4 text-sm sm:text-base bg-gray-100 rounded-lg py-2 px-4 inline-block">
                    <ol className="list-reset flex flex-wrap items-center space-x-2">
                        <li>
                            <a href="/" className="text-blue-600 hover:underline">
                                Homepage
                            </a>
                        </li>
                        <li>
                            <span className="mx-2">/</span>
                        </li>
                        <li>
                            <a className="text-blue-600 hover:underline" onClick={() => navigate("/products/smartphone")}>
                                Smartphone
                            </a>
                        </li>
                        <li>
                            <span className="mx-2">/</span>
                        </li>
                        <li className="text-gray-500">{product?.[0]?.prodTypeName || "Product"}</li>
                    </ol>
                </nav>
                <Card>
                    <CardContent className="p-4 sm:p-6">
                        <div className="flex flex-col md:flex-row gap-6">
                            <div className="flex-1 flex flex-col items-center">
                                <div className="relative flex justify-center items-center bg-gray-100 rounded-lg shadow-md w-full max-w-[500px] h-[300px] sm:h-[400px]">
                                    <img
                                        src={selectedImage || "/images/placeholder.jpg"}
                                        alt="Product"
                                        className="w-full h-full object-contain rounded-lg"
                                    />
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        onClick={handlePrev}
                                        className="absolute left-2 top-1/2 transform -translate-y-1/2 bg-gray-700 text-white p-2 rounded-full hover:bg-gray-900"
                                    >
                                        <ChevronLeft className="w-5 h-5" />
                                    </Button>
                                    <Button
                                        variant="ghost"
                                        size="icon"
                                        onClick={handleNext}
                                        className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-gray-700 text-white p-2 rounded-full hover:bg-gray-900"
                                    >
                                        <ChevronRight className="w-5 h-5" />
                                    </Button>
                                </div>
                                <div className="flex flex-wrap justify-center mt-4 gap-2">
                                    {images.map((img, index) => (
                                        <img
                                            key={index}
                                            src={img}
                                            alt={`Thumbnail ${index}`}
                                            className={cn(
                                                "w-16 h-16 sm:w-20 sm:h-20 object-cover rounded-lg cursor-pointer border-2 transition-all",
                                                selectedImage === img ? "border-blue-500 shadow-md scale-105" : "border-transparent hover:border-gray-300"
                                            )}
                                            onClick={() => {
                                                setSelectedImage(img);
                                                setCurrentIndex(index);
                                            }}
                                        />
                                    ))}
                                </div>
                            </div>

                            <div className="flex-1 mt-4">
                                <div className="flex flex-wrap justify-between items-center gap-4">
                                    <h1 className="text-2xl sm:text-3xl font-semibold">{product?.[0]?.prodTypeName || "Loading..."}</h1>
                                    <div>
                                        <button
                                            className="w-50 bg-yellow-500 text-white py-2 rounded-lg hover:bg-yellow-600 transition duration-300 text-sm sm:text-base"
                                            onClick={handleAddToCompare}
                                        >
                                            Compare
                                        </button>
                                    </div>
                                </div>
                                <div className="mt-4">
                                    {Object.entries(groupedVariations).map(([variId, group]) => (
                                        <div key={variId} className="mb-2">
                                            {group.values.length > 1 ? (
                                                <>
                                                    <p className="mt-2 text-black-700 text-sm sm:text-base font-semibold">
                                                        Selected {group.variName}: <span style={{ color: "#FF6F00" }}>{selectedVariations[variId] || "None"}</span>
                                                    </p>
                                                    <div className="flex flex-wrap gap-2 mt-2">
                                                        {group.values.map((variation) => (
                                                            <button
                                                                key={variation.prodTypeVariId}
                                                                className={cn(
                                                                    "flex items-center h-8 justify-center px-3 py-1 sm:px-4 sm:py-2 rounded-lg border-2 transition-all",
                                                                    selectedVariations[variId] === variation.prodTypeValue
                                                                        ? "border-blue-500 bg-blue-50 text-blue-700 font-semibold scale-105"
                                                                        : "border-gray-200 hover:border-gray-400 bg-gray-50 text-gray-700 hover:bg-gray-100"
                                                                )}
                                                                onClick={() => handleSelectVariation(variId, variation.prodTypeValue, variation.prodTypeVariId)}
                                                            >
                                                                <span className="text-sm sm:text-base">{variation.prodTypeValue}</span>
                                                            </button>
                                                        ))}
                                                    </div>
                                                </>
                                            ) : (
                                                <div className="flex items-center gap-1">
                                                    <h2 className="text-base sm:text-lg font-medium">{group.variName}:</h2>
                                                    <span className="text-sm sm:text-base text-gray-700">{group.values[0].prodTypeValue}</span>
                                                </div>
                                            )}
                                        </div>
                                    ))}
                                </div>
                                <div className="py-2 border-t border-gray-300 flex flex-col md:flex-row gap-6 mt-4">
                                    <div className="flex-1 mt-1">
                                        <p className="text-2xl sm:text-3xl text-blue-800 font-semibold">
                                            {product?.[0]?.unitPrice ? formatPrice(product[0].unitPrice) : "N/A"}
                                        </p>
                                        <div className="flex">
                                            <p style={{ fontSize: "20px" }} className="text-gray-400 line-through text-sm">
                                                {product?.[0]?.prodTypePrice ? formatPrice(product[0].prodTypePrice) : "N/A"}
                                            </p>
                                            <p className="p-1">
                                                {product?.[0]?.prodTypePrice && product?.[0]?.unitPrice ? (
                                                    <span className="text-sm text-green-500 font-bold">
                                                        {` -${(((product[0].prodTypePrice - product[0].unitPrice) / product[0].prodTypePrice) * 100).toFixed(3)}%`}
                                                    </span>
                                                ) : null}
                                            </p>
                                        </div>
                                        <div className="mt-4">
                                            {branchQuantity === 0 ? (
                                                <p className="text-sm sm:text-base text-gray-700">
                                                    Out of stock at {localStorage.getItem("selectedBranchName") || "Selected Branch"}.{" "}
                                                    <button
                                                        className="text-blue-500 hover:underline"
                                                        onClick={() => setIsBranchModalOpen(true)}
                                                    >
                                                        Switch Branch
                                                    </button>
                                                </p>
                                            ) : (
                                                <p className="text-sm sm:text-base text-gray-700">
                                                    Available Quantity at {localStorage.getItem("selectedBranchName") || "Selected Branch"}:{" "}
                                                    <span className="font-semibold">{branchQuantity !== null ? branchQuantity : "Loading..."}</span>
                                                </p>
                                            )}
                                        </div>
                                        <div className="mt-6 flex flex-col sm:flex-row gap-3">
                                            {branchQuantity === 0 ? (
                                                <div className="w-full flex flex-col items-center">
                                                    <button
                                                        className="w-full bg-gray-400 text-white py-2 rounded-lg cursor-not-allowed text-sm sm:text-base"
                                                        disabled
                                                    >
                                                        Out of Stock
                                                    </button>
                                                </div>
                                            ) : (
                                                <>
                                                    <button
                                                        className="w-1/3 bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition duration-300 text-sm sm:text-base"
                                                        onClick={handleAddToCart}
                                                    >
                                                        Add to cart
                                                    </button>
                                                    <button
                                                        className="w-2/3 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition duration-300 text-sm sm:text-base"
                                                        onClick={handleBuyNow}
                                                    >
                                                        Buy now
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </div>
                                    {userInfo.userId && userPromotions.length > 0 && (
                                        <div className="flex-1">
                                            <h3 className="text-lg font-semibold mb-2">My Promotions</h3>
                                            <div className="space-y-4">
                                                {userPromotions.slice(0, 2).map((promo) => {
                                                    const discountedPrice = calculateDiscountedPrice(product?.[0]?.unitPrice || 0, promo.discount);
                                                    return (
                                                        <div key={promo.promId} className="p-3 bg-gray-50 rounded-lg shadow-sm">
                                                            <p className="text-sm font-medium">{promo.promotionName}</p>
                                                            <p className="text-sm text-gray-600">Discount: {promo.discount}%</p>
                                                            <p className="text-sm text-green-600 font-semibold">
                                                                Price after discount: {formatPrice(discountedPrice)}
                                                            </p>
                                                        </div>
                                                    );
                                                })}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
                <Separator className="my-6" />
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
                    <div className="bg-white p-4 sm:p-6 rounded-lg shadow-lg" ref={reviewComponentRef}>
                        <ReviewProduct productId={location.pathname.split("/")[2]} isReviewNow={isReviewNow} />
                    </div>
                    <div className="bg-white p-4 sm:p-6 rounded-lg shadow-lg">
                        <h2 className="text-xl sm:text-2xl font-semibold">Specifications</h2>
                        <table className="mt-4 w-full text-left border-collapse text-sm sm:text-base">
                            <thead>
                                <tr className="border-b">
                                    <th className="py-2 px-2 sm:px-4 font-medium">Attribute</th>
                                    <th className="py-2 px-2 sm:px-4 font-medium">Value</th>
                                </tr>
                            </thead>
                            <tbody>
                                {attributes.length > 0 ? (
                                    attributes.map((attribute) => (
                                        <tr key={attribute.prodAtbId} className="border-b">
                                            <td className="py-2 px-2 sm:px-4 font-medium">{attribute.atbId?.atbName || "N/A"}</td>
                                            <td className="py-2 px-2 sm:px-4">{attribute.prodAtbValue || "N/A"}</td>
                                        </tr>
                                    ))
                                ) : (
                                    <tr>
                                        <td colSpan="3" className="py-2 px-2 sm:px-4 text-center text-gray-500">
                                            No specifications available.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            {/* Modal for large quantity request */}
            <Modal
                isOpen={isModalOpen}
                onRequestClose={() => setIsModalOpen(false)}
                className="relative w-full max-w-md mx-auto my-6 p-6 rounded-xl shadow-2xl overflow-y-auto max-h-[90vh] bg-white/90 backdrop-blur-md border border-gray-200/50"
                overlayClassName="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-start justify-center z-50"
                style={{
                    overlay: {
                        zIndex: 1000,
                    },
                }}
            >
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold text-gray-800">NOTIFICATION</h2>
                    <button
                        onClick={() => setIsModalOpen(false)}
                        className="text-gray-500 hover:text-red-600 transition-colors duration-200"
                    >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <p className="text-gray-700 mb-3 text-sm leading-relaxed">
                    The requested product quantity has reached the maximum limit. Your order will be handled by our B2B Enterprise Department for further assistance. For immediate support, please contact:
                </p>
                <p className="text-gray-700 mb-4 text-sm leading-relaxed">
                    <span className="font-semibold">Mr.Vux:</span> +84 777 800 275<br />
                    <span className="font-semibold">Email:</span> vuxblack@gmail.com
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mb-4">
                    <div>
                        <label className="block text-gray-700 font-medium text-sm mb-1">Company Name (Required)</label>
                        <input
                            type="text"
                            name="companyName"
                            value={formData.companyName}
                            onChange={handleFormChange}
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.companyName ? "border-red-500" : "border-gray-300"
                                } bg-white/50`}
                            placeholder="Enter your company name"
                        />
                        {formErrors.companyName && <p className="text-red-500 text-xs mt-1">{formErrors.companyName}</p>}
                    </div>
                    <div>
                        <label className="block text-gray-700 font-medium text-sm mb-1">Your Name (Required)</label>
                        <input
                            type="text"
                            name="customerName"
                            value={formData.customerName}
                            onChange={handleFormChange}
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.customerName ? "border-red-500" : "border-gray-300"
                                } bg-white/50`}
                            placeholder="Enter your name"
                        />
                        {formErrors.customerName && <p className="text-red-500 text-xs mt-1">{formErrors.customerName}</p>}
                    </div>
                    <div>
                        <label className="block text-gray-700 font-medium text-sm mb-1">Phone Number (Required)</label>
                        <input
                            type="text"
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={handleFormChange}
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.phoneNumber ? "border-red-500" : "border-gray-300"
                                } bg-white/50`}
                            placeholder="Enter your phone number (e.g., 0123456789)"
                        />
                        {formErrors.phoneNumber && <p className="text-red-500 text-xs mt-1">{formErrors.phoneNumber}</p>}
                    </div>
                    <div>
                        <label className="block text-gray-700 font-medium text-sm mb-1">Email Address (Required)</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleFormChange}
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.email ? "border-red-500" : "border-gray-300"
                                } bg-white/50`}
                            placeholder="Enter your email address"
                        />
                        {formErrors.email && <p className="text-red-500 text-xs mt-1">{formErrors.email}</p>}
                    </div>
                </div>

                <div className="mb-4">
                    <label className="block text-gray-700 font-medium text-sm mb-1">Product of Interest</label>
                    <input
                        type="text"
                        value={currentItem ? ` ${currentItem.name}` : ""}
                        className="w-full p-2 border rounded-md text-sm bg-gray-100/50 text-gray-600 cursor-not-allowed"
                        disabled
                    />
                </div>

                <div className="mb-4">
                    <label className="block text-gray-700 font-medium text-sm mb-1">Quantity</label>
                    <input
                        type="number"
                        name="quantity"
                        value={formData.quantity}
                        onChange={handleFormChange}
                        className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.quantity ? "border-red-500" : "border-gray-300"
                            } bg-white/50`}
                        min="1"
                        placeholder="Enter quantity"
                    />
                    {formErrors.quantity && <p className="text-red-500 text-xs mt-1">{formErrors.quantity}</p>}
                </div>

                <div className="mb-4">
                    <label className="block text-gray-700 font-medium text-sm mb-1">Notes</label>
                    <textarea
                        name="note"
                        value={formData.note}
                        onChange={handleFormChange}
                        className="w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 border-gray-300 bg-white/50"
                        rows="3"
                        placeholder="Enter any additional notes (optional)"
                    />
                </div>

                <Button
                    type="submit"
                    className="bg-blue-500 text-white hover:bg-blue-600 flex items-center gap-2 w-full py-2 rounded-md font-semibold text-sm justify-center"
                    disabled={isSubmitting}
                    onClick={handleFormSubmit}
                >
                    {isSubmitting ? (
                        <>
                            <Loader2 className="animate-spin" size={20} />
                            Saving...
                        </>
                    ) : (
                        "Register"
                    )}
                </Button>
            </Modal>

            {/* New Modal for Branch Selection */}
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
                                        searchBranches(e.target.value, product?.[0]?.prodTypeId);
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
                                            <div className="flex justify-between items-center">
                                                <span>{branch.brchName || "Unnamed Branch"}</span>
                                                <span className="text-gray-500 text-xs">Qty: {branch.quantity}</span>
                                            </div>
                                            <p className="text-xs text-gray-500">{branch.address || "No address"}</p>
                                        </li>
                                    ))
                                ) : (
                                    <li className="text-center text-gray-500 text-sm py-2">No branches with stock found</li>
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

export default ProductDetail;