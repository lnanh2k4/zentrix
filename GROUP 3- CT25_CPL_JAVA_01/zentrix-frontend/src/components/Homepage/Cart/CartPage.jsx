import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Trash2, Minus, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import axios from "axios";
import Modal from "react-modal";
import { Search, X, Loader2 } from "lucide-react";
import { showNotification } from "@/components/Dashboard/NotificationPopup";
import { getProductByProductTypeId } from "@/context/ApiContext";

// Bind modal to your appElement for accessibility
Modal.setAppElement("#root");

const ShoppingCart = () => {
    const [cartItems, setCartItems] = useState([]);
    const [selectedItems, setSelectedItems] = useState([]);
    const [discount, setDiscount] = useState(0);
    const [loading, setLoading] = useState(true);
    const [cartId, setCartId] = useState(null);
    const [userPromotions, setUserPromotions] = useState([]);
    const [availableBranches, setAvailableBranches] = useState({});
    const [branchQuantities, setBranchQuantities] = useState({});
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
    const [isLoading, setIsLoading] = useState(false);
    const userId = 2; // Assume userId
    const API_URL = "http://localhost:6789/api/v1/cart";
    const navigate = useNavigate();

    const fetchBranchQuantity = async (prodTypeId, brchId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/productTypeBranch/${prodTypeId}`, {
                withCredentials: true,
            });
            const branchData = response.data.find((item) => item.brchId?.brchId === brchId);
            const available = response.data.find((item) => item.quantity > 0 && item.brchId?.brchId !== brchId);
            if (available) {
                setAvailableBranches((prev) => ({
                    ...prev,
                    [prodTypeId]: {
                        brchId: available.brchId.brchId,
                        brchName: available.brchId.brchName,
                        quantity: available.quantity,
                    },
                }));
            } else {
                setAvailableBranches((prev) => ({
                    ...prev,
                    [prodTypeId]: null,
                }));
            }
            return branchData ? branchData.quantity : 0;
        } catch (error) {
            console.error("Error fetching branch quantity:", error);
            return 0;
        }
    };

    const fetchImageProductType = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, {
                withCredentials: true,
            });
            if (Array.isArray(response.data) && response.data.length > 0) {
                const firstImage = response.data[0];
                return firstImage.imageId?.imageLink || firstImage.imageLink || firstImage || "";
            } else if (!Array.isArray(response.data) && response.data) {
                return response.data.imageId?.imageLink || response.data.imageLink || response.data || "";
            }
            return "";
        } catch (error) {
            console.error("Error fetching product images for prodTypeId", prodTypeId, ":", error);
            return "";
        }
    };

    const fetchCartItems = async (userId, page = 0, size = 10) => {
        try {
            const response = await axios.get(API_URL, {
                params: { userId, page, size },
                withCredentials: true,
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching cart items:", error);
            throw error;
        }
    };

    const formatPrice = (price) => {
        return Math.floor(price).toLocaleString("en-US");
    };

    const fetchCartItemsDetails = async (cartId, userId) => {
        try {
            const response = await axios.get(`${API_URL}/items`, {
                params: { userId, cartId },
                withCredentials: true,
            });

            if (!response.data.success || !response.data.content) {
                console.log("No cart items found:", response.data);
                return { success: false, content: [] };
            }

            // Log raw data for debugging
            console.log("Raw cart items from API:", response.data.content);

            // Add isDeleted property based on prodTypeStatus
            const itemsWithStatus = response.data.content.map((item) => ({
                ...item,
                isDeleted: item.prodTypeVariId?.prodTypeId?.status !== 1,
            }));

            console.log("Cart items with isDeleted flag:", itemsWithStatus);

            return { success: true, content: itemsWithStatus };
        } catch (error) {
            console.error("Error fetching cart items details:", error);
            throw error;
        }
    };

    const updateCartQuantity = async (userId, cartProductTypeVariationId, quantity) => {
        try {
            const response = await axios.put(
                `${API_URL}/update-quantity`,
                null,
                {
                    params: { userId, cartProductTypeVariationId, quantity },
                    withCredentials: true,
                }
            );
            return response.data;
        } catch (error) {
            console.error("Error updating cart quantity:", error);
            throw error;
        }
    };

    const removeFromCart = async (userId, cartProductTypeVariationId) => {
        try {
            const response = await axios.delete(`${API_URL}/remove`, {
                params: { userId, cartProductTypeVariationId },
                withCredentials: true,
            });
            return response.data;
        } catch (error) {
            console.error("Error removing from cart:", error);
            throw error;
        }
    };

    const removeAllItems = async () => {
        try {
            if (cartItems.length === 0) return;

            await Promise.all(
                cartItems.flatMap((item) =>
                    item.cartProductTypeVariationIds.map((cartProductTypeVariationId) =>
                        removeFromCart(userId, cartProductTypeVariationId)
                    )
                )
            );

            setCartItems([]);
            setSelectedItems([]);
        } catch (error) {
            showNotification("Failed to clear the cart: " + error.message, 3000, "fail");
        }
    };

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success) {
                return response.data.content.userId;
            }
            console.warn("User info fetch succeeded but returned unsuccessful response:", response.data);
            return null;
        } catch (error) {
            console.error("Error fetching user info:", {
                message: error.message,
                response: error.response?.data,
            });
            return null;
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
                    startDate: response.data.content.startDate,
                    endDate: response.data.content.endDate,
                    quantity: response.data.content.quantity,
                    promStatus: response.data.content.promStatus,
                    status: userPromoData[index].status,
                }));

            const today = new Date();
            today.setHours(0, 0, 0, 0);

            const validPromotions = promotionData.filter((promo) => {
                const start = new Date(promo.startDate);
                start.setHours(0, 0, 0, 0);
                const end = new Date(promo.endDate);
                end.setHours(23, 59, 59, 999);

                const isValid =
                    promo.status === 1 &&
                    promo.promStatus === 1 &&
                    promo.quantity > 0 &&
                    start <= today &&
                    today <= end;
                console.log(`Checking promo ${promo.promotionName}:`, {
                    start: start.toISOString(),
                    end: end.toISOString(),
                    today: today.toISOString(),
                    isValid,
                });
                return isValid;
            });

            console.log("Valid promotions in ShoppingCart:", validPromotions);
            setUserPromotions(validPromotions);
        } catch (error) {
            console.error("Error fetching promotions:", error.response?.data || error.message);
            setUserPromotions([]);
        }
    };

    useEffect(() => {
        const loadCartAndPromotions = async () => {
            try {
                setLoading(true);

                const fetchedUserId = await fetchUserInfo();
                const effectiveUserId = fetchedUserId || userId;

                const cartResponse = await fetchCartItems(effectiveUserId);
                if (!cartResponse.success || !cartResponse.content || cartResponse.content.length === 0) {
                    setCartItems([]);
                    setCartId(null);
                } else {
                    const firstCart = cartResponse.content[0];
                    const cartId = firstCart.cartId;
                    setCartId(cartId);

                    const itemsResponse = await fetchCartItemsDetails(cartId, effectiveUserId);
                    if (!itemsResponse.success || !itemsResponse.content || itemsResponse.content.length === 0) {
                        setCartItems([]);
                        showNotification("No items in the cart.", 3000, "fail");
                    } else {
                        const selectedBranchId = Number(localStorage.getItem("selectedBranchId")) || 5;

                        // Replace Promise.all with for loop
                        const items = [];
                        for (const item of itemsResponse.content) {
                            const prodTypeId = item.prodTypeVariId?.prodTypeId?.prodTypeId;
                            const imageUrl = prodTypeId ? await fetchImageProductType(prodTypeId) : "";
                            const quantityAtBranch = prodTypeId ? await fetchBranchQuantity(prodTypeId, selectedBranchId) : 0;

                            // Fetch VAT using getProductByProductTypeId
                            let vat = 0;
                            if (prodTypeId) {
                                const productResponse = await getProductByProductTypeId(prodTypeId);
                                console.log("test mạnh: ", productResponse.vat)
                                vat = productResponse.vat || 0;
                            }

                            items.push({
                                id: item.cartProductTypeVariationId,
                                cartId: item.cartId.cartId,
                                name: item.prodTypeVariId?.prodTypeId?.prodTypeName || "Unnamed Product",
                                originalPrice: item.prodTypeVariId?.prodTypeId?.prodTypePrice || 4390000,
                                salePrice: item.prodTypeVariId?.prodTypeId?.unitPrice || item.prodTypeVariId?.prodTypeId?.prodTypePrice || 3290000,
                                quantity: item.quantity,
                                variation: item.prodTypeVariId || null,
                                vat: vat,
                                prodTypeId: prodTypeId,
                                prodTypeVariId: item.prodTypeVariId?.prodTypeVariId,
                                urlImage: imageUrl,
                                createdAt: item.createdAt || new Date().toISOString(),
                                branchQuantity: quantityAtBranch,
                                isDeleted: item.isDeleted,
                                variCode: item.variCode,
                            });
                        }

                        const groupedItems = {};

                        // Group items by prodTypeId and variCode
                        items.forEach((item) => {
                            const groupKey = `${item.prodTypeId}-${item.variCode}`;
                            console.log(item);
                            if (!groupedItems[groupKey]) {
                                groupedItems[groupKey] = {
                                    key: groupKey,
                                    name: item.name,
                                    originalPrice: item.originalPrice,
                                    salePrice: item.salePrice,
                                    urlImage: item.urlImage,
                                    vat: item.vat,
                                    quantity: item.quantity,
                                    prodTypeId: item.prodTypeId,
                                    cartProductTypeVariationIds: [item.id],
                                    variations: [
                                        {
                                            id: item.id,
                                            variationName: item.variation?.variId?.variName || "Unknown",
                                            variationValue: item.variation?.prodTypeValue || "N/A",
                                            quantity: item.quantity,
                                            cartProductTypeVariationId: item.id,
                                            prodTypeVariId: item.prodTypeVariId,
                                        },
                                    ],
                                    createdAt: item.createdAt,
                                    branchQuantity: item.branchQuantity,
                                    isDeleted: item.isDeleted,
                                };
                            } else {
                                groupedItems[groupKey].cartProductTypeVariationIds.push(item.id);
                                groupedItems[groupKey].variations.push({
                                    id: item.id,
                                    variationName: item.variation?.variId?.variName || "Unknown",
                                    variationValue: item.variation?.prodTypeValue || "N/A",
                                    quantity: item.quantity,
                                    cartProductTypeVariationId: item.id,
                                    prodTypeVariId: item.prodTypeVariId,
                                });
                            }
                        });

                        // Sort variations by variationName if needed
                        Object.values(groupedItems).forEach((group) => {
                            group.variations.sort((a, b) => a.variationName.localeCompare(b.variationName));
                        });

                        const uniqueItems = Object.values(groupedItems);
                        setCartItems(uniqueItems);
                        setSelectedItems([]);
                    }
                }

                await fetchUserPromotions(effectiveUserId);
            } catch (error) {
                console.error("Failed to load cart or promotions:", error);
                showNotification("Failed to load the cart.", 3000, "fail");
            } finally {
                setLoading(false);
            }
        };
        loadCartAndPromotions();
    }, []);

    const validateForm = () => {
        const errors = {};

        if (!formData.companyName.trim()) {
            errors.companyName = "Company name is required";
        }

        if (!formData.customerName.trim()) {
            errors.customerName = "Your name is required";
        }

        const phoneRegex = /^0\d{9}$/;
        if (!formData.phoneNumber.trim()) {
            errors.phoneNumber = "Phone number is required";
        } else if (!phoneRegex.test(formData.phoneNumber)) {
            errors.phoneNumber = "Please enter a valid phone number (e.g., 0123456789)";
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!formData.email.trim()) {
            errors.email = "Email address is required";
        } else if (!emailRegex.test(formData.email)) {
            errors.email = "Please enter a valid email address";
        }

        if (formData.quantity < 1) {
            errors.quantity = "Quantity must be greater than 0";
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

        setIsLoading(true);
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
            showNotification("Failed to send the request via email. Please try again later.", 3000, "fail");
        } finally {
            setIsLoading(false);
        }
    };

    const updateQuantity = async (userId, key, newQuantity) => {
        if (newQuantity < 1) {
            console.warn("Quantity must be at least 1");
            return;
        }

        const previousCartItems = [...cartItems];
        try {
            const item = cartItems.find((item) => item.key === key);
            if (!item) return;

            const selectedBranchId = Number(localStorage.getItem("selectedBranchId")) || 5;
            const availableQty = await fetchBranchQuantity(item.prodTypeId, selectedBranchId);

            if (newQuantity > availableQty) {
                showNotification(`Only ${availableQty} items are available at this branch. Please check other branches for more stock.`, 3000, "fail");
                return;
            }

            if (newQuantity > 5) {
                setCurrentItem(item);
                setFormData((prev) => ({
                    ...prev,
                    quantity: newQuantity,
                }));
                setIsModalOpen(true);
                return;
            }

            await Promise.all(
                item.cartProductTypeVariationIds.map((cartProductTypeVariationId) =>
                    updateCartQuantity(userId, cartProductTypeVariationId, newQuantity)
                )
            );

            setCartItems((prevItems) =>
                prevItems.map((item) =>
                    item.key === key ? { ...item, quantity: newQuantity } : item
                )
            );
            setBranchQuantities((prev) => ({
                ...prev,
                [item.prodTypeId]: availableQty,
            }));
        } catch (error) {
            setCartItems(previousCartItems);
            console.error("Failed to update quantity:", error.message || error);
            showNotification(
                error.message || "Failed to update quantity. Please try again later or contact support.",
                3000,
                "fail"
            );
        }
    };

    const removeItem = async (key) => {
        try {
            const item = cartItems.find((item) => item.key === key);
            if (!item) return;

            await Promise.all(
                item.cartProductTypeVariationIds.map((cartProductTypeVariationId) =>
                    removeFromCart(userId, cartProductTypeVariationId)
                )
            );

            setCartItems((prevItems) => prevItems.filter((item) => item.key !== key));
            setSelectedItems((prev) => prev.filter((id) => id !== key));
        } catch (error) {
            showNotification("Failed to remove the item: " + error.message, 3000, "fail");
        }
    };

    const toggleSelect = (key) => {
        setSelectedItems((prev) =>
            prev.includes(key) ? prev.filter((id) => id !== key) : [...prev, key]
        );
    };

    const handleSelectAll = () => {
        if (selectedItems.length === cartItems.filter((item) => item.branchQuantity > 0 && !item.isDeleted).length) {
            setSelectedItems([]);
        } else {
            const availableItems = cartItems
                .filter((item) => item.branchQuantity > 0 && !item.isDeleted) // Only select items that are not disabled
                .map((item) => item.key);
            setSelectedItems(availableItems);
        }
    };

    const selectedCartItems = cartItems
        .filter((item) => selectedItems.includes(item.key))
        .map((item) => ({
            salePrice: item.salePrice,
            originalPrice: item.originalPrice,
            quantity: item.quantity,
            vat: item.vat,
        }));

    const totalPrice = selectedCartItems.reduce(
        (sum, item) => sum + item.salePrice * item.quantity,
        0
    );
    const originalTotalPrice = selectedCartItems.reduce(
        (sum, item) => sum + item.originalPrice * item.quantity,
        0
    );
    const discountAmount = (discount / 100) * totalPrice;
    const totalVAT = selectedCartItems.reduce((sum, item) => {
        const vatRate = (item.vat || 0) / 100;
        const itemPrice = item.salePrice * item.quantity;
        return sum + itemPrice * vatRate;
    }, 0);

    const finalTotalPrice = totalPrice + totalVAT - discountAmount;

    const handleConfirmOrder = () => {
        if (selectedItems.length === 0) {
            showNotification("Please select items to order.", 3000, "fail");
            return;
        }
        const selectedCart = cartItems
            .filter((item) => selectedItems.includes(item.key))
            .map((item) => ({
                ...item,
                cartId,
                prodTypeId: item.prodTypeId,
                quantity: item.quantity,
            }));
        navigate("/orderPage", {
            state: {
                cart: selectedCart,
                userId,
                totalAmount: finalTotalPrice,
                userPromotions,
                selectedDiscount: discount,
            },
        });
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-screen bg-gray-100">
                <div className="text-lg font-semibold text-gray-600 animate-pulse">
                    Loading cart...
                </div>
            </div>
        );
    }

    return (
        <div className="flex flex-col min-h-screen bg-gray-100">
            <Header className="fixed top-0 left-0 w-full h-16 bg-white shadow-md z-50" />
            <div className="max-w-7xl mx-auto p-6 pt-28 flex-grow grid grid-cols-1 lg:grid-cols-5 gap-6 mb-16">
                <div className="lg:col-span-4">
                    <div className="bg-white border border-gray-200 rounded-lg p-4 flex items-center justify-between mb-6">
                        <div className="flex items-center">
                            <input
                                type="checkbox"
                                className="w-5 h-5 accent-blue-600 rounded border-gray-300"
                                checked={selectedItems.length === cartItems.filter((item) => item.branchQuantity > 0 && !item.isDeleted).length && cartItems.length > 0}
                                onChange={handleSelectAll}
                            />
                            <span className="ml-3 text-lg font-medium text-gray-800">
                                Select all products ({selectedItems.length}/{cartItems.length})
                            </span>
                        </div>
                        <Button
                            variant="ghost"
                            size="icon"
                            onClick={removeAllItems}
                            className="text-gray-500 hover:text-red-500 transition-colors"
                            disabled={cartItems.length === 0}
                        >
                            <Trash2 size={20} />
                        </Button>
                    </div>

                    <div className="space-y-4">
                        {cartItems.length === 0 ? (
                            <div className="text-center py-10 bg-white rounded-lg shadow-sm">
                                <p className="text-lg text-gray-500">Your cart is currently empty.</p>
                                <Button
                                    className="mt-4 bg-blue-600 text-white hover:bg-blue-700 rounded-lg"
                                    onClick={() => navigate("/products")}
                                >
                                    Go to shopping
                                </Button>
                            </div>
                        ) : (
                            cartItems.map((item) => (
                                <div
                                    key={item.key}
                                    className={`bg-white p-4 rounded-lg shadow-sm border border-gray-200 ${item.branchQuantity === 0 || item.isDeleted ? "opacity-50" : ""}`}
                                >
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center space-x-4">
                                            <input
                                                type="checkbox"
                                                className="w-5 h-5 accent-blue-600 rounded border-gray-300"
                                                checked={selectedItems.includes(item.key)}
                                                onChange={() => toggleSelect(item.key)}
                                                disabled={item.branchQuantity === 0 || item.isDeleted}
                                            />
                                            <img
                                                src={
                                                    item.urlImage
                                                        ? item.urlImage.startsWith("http") || item.urlImage.startsWith("blob:")
                                                            ? item.urlImage
                                                            : `http://localhost:6789${item.urlImage.startsWith("/") ? "" : "/"}${item.urlImage}`
                                                        : "/images/default-product.jpg"
                                                }
                                                alt={item.name}
                                                className="w-20 h-20 object-contain rounded-lg border border-gray-200"
                                                onError={(e) => (e.target.src = "/images/default-product.jpg")}
                                            />
                                            <div>
                                                <p className="text-base font-medium text-gray-800">{item.name}</p>
                                                {item.variations.map((variation) => (
                                                    <p
                                                        key={`${item.key}-${variation.variationName}`}
                                                        className="text-sm text-gray-600"
                                                    >
                                                        {variation.variationName}:{" "}
                                                        <span className="font-medium">{variation.variationValue}</span>
                                                    </p>
                                                ))}
                                            </div>
                                        </div>
                                        <div className="flex items-center space-x-4">
                                            <div className="flex flex-col items-end">
                                                <p className="text-sm font-bold text-red-600">
                                                    {formatPrice(item.salePrice || 0)} VND
                                                </p>
                                                <p className="text-sm text-gray-400 line-through mt-1">
                                                    {formatPrice(item.originalPrice || 0)} VND
                                                </p>
                                            </div>
                                            <div className="flex items-center space-x-2">
                                                <div className="flex items-center border border-gray-300 rounded-lg overflow-hidden">
                                                    <Button
                                                        variant="outline"
                                                        size="icon"
                                                        onClick={() => updateQuantity(userId, item.key, item.quantity - 1)}
                                                        disabled={item.quantity <= 1 || item.branchQuantity === 0 || item.isDeleted}
                                                        className="h-8 w-8 rounded-none border-none hover:bg-gray-100"
                                                    >
                                                        <Minus size={16} className="text-gray-600" />
                                                    </Button>
                                                    <span className="px-3 text-base font-medium text-gray-800">
                                                        {item.quantity}
                                                    </span>
                                                    <Button
                                                        variant="outline"
                                                        size="icon"
                                                        onClick={() => updateQuantity(userId, item.key, item.quantity + 1)}
                                                        disabled={item.branchQuantity === 0 || item.isDeleted}
                                                        className="h-8 w-8 rounded-none border-none hover:bg-gray-100"
                                                    >
                                                        <Plus size={16} className="text-gray-600" />
                                                    </Button>
                                                </div>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => removeItem(item.key)}
                                                    className="text-gray-500 hover:text-red-500 transition-colors"
                                                >
                                                    <Trash2 size={20} />
                                                </Button>
                                            </div>
                                        </div>
                                    </div>

                                    {item.branchQuantity === 0 && !item.isDeleted && (
                                        <p className="text-sm text-red-600 mt-2">
                                            Out of stock at this branch
                                        </p>
                                    )}
                                    {item.isDeleted && (
                                        <p className="text-sm text-red-600 mt-2">
                                            Product Unavailable
                                        </p>
                                    )}
                                </div>
                            ))
                        )}
                    </div>
                </div>

                <div className="bg-white p-4 rounded-lg shadow-lg w-fit min-w-[300px] h-fit max-h-[400px] sticky top-50 self-start z-10 col-span-1">
                    <h3 className="text-2xl font-semibold mb-4">Order Summary</h3>
                    <div className="mb-4">
                        <label className="text-gray-600 font-medium">Apply Discount Code:</label>
                        <select
                            className="w-full p-2 border rounded mt-2"
                            value={discount}
                            onChange={(e) => setDiscount(e.target.value ? Number(e.target.value) : 0)}
                        >
                            <option value="">No Discount</option>
                            {userPromotions.length > 0 ? (
                                userPromotions.map((promo) => (
                                    <option key={promo.promId} value={promo.discount}>
                                        {promo.promotionName} - {promo.discount}%
                                    </option>
                                ))
                            ) : (
                                <option disabled>No available promotions</option>
                            )}
                        </select>
                    </div>
                    <div className="space-y-2">
                        <div className="flex justify-between text-gray-600">
                            <span>Subtotal</span>
                            <span className="font-semibold whitespace-nowrap">{formatPrice(originalTotalPrice)} VND</span>
                        </div>
                        <div className="flex justify-between text-gray-600">
                            <span>Total Discount</span>
                            <span className="text-black font-semibold whitespace-nowrap">
                                -{formatPrice(originalTotalPrice - totalPrice)} VND
                            </span>
                        </div>
                        <div className="flex justify-between text-gray-600">
                            <span>Additional Discount</span>
                            <span className="text-black font-semibold whitespace-nowrap">
                                -{formatPrice(discountAmount)} VND
                            </span>
                        </div>
                        <div className="flex justify-between text-gray-600">
                            <span>VAT</span>
                            <span className="text-black font-semibold whitespace-nowrap">
                                {formatPrice(totalVAT)} VND
                            </span>
                        </div>
                        <hr className="my-2" />
                        <div className="flex justify-between font-semibold text-lg">
                            <span>Final Total</span>
                            <span className="text-red-500 whitespace-nowrap">
                                {formatPrice(finalTotalPrice)} VND
                            </span>
                        </div>
                    </div>
                    <Button
                        className={`w-full mt-4 py-3 text-lg font-semibold ${selectedItems.length === 0
                            ? "bg-gray-300 text-gray-600 cursor-not-allowed"
                            : "bg-red-500 text-white"
                            } rounded-lg hover:bg-red-600 transition-colors duration-200`}
                        disabled={selectedItems.length === 0}
                        onClick={handleConfirmOrder}
                    >
                        Confirm Order
                    </Button>
                </div>
            </div>

            {/* Modal with loading state for the Save button */}
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
                    The requested product quantity for <strong>{currentItem?.name}</strong>{" "}
                    {currentItem?.variations?.length > 0 && (
                        <span>
                            ({currentItem.variations.map((v) => `${v.variationName}: ${v.variationValue}`).join(", ")})
                        </span>
                    )}{" "}
                    has reached the maximum limit. Your order will be handled by our B2B Enterprise Department for further assistance. For immediate support, please contact:
                </p>
                <p className="text-gray-700 mb-4 text-sm leading-relaxed">
                    <span className="font-semibold">Mr. Vux:</span> +84 777 800 275<br />
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
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.companyName ? "border-red-500" : "border-gray-300"} bg-white/50`}
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
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.customerName ? "border-red-500" : "border-gray-300"} bg-white/50`}
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
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.phoneNumber ? "border-red-500" : "border-gray-300"} bg-white/50`}
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
                            className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.email ? "border-red-500" : "border-gray-300"} bg-white/50`}
                            placeholder="Enter your email address"
                        />
                        {formErrors.email && <p className="text-red-500 text-xs mt-1">{formErrors.email}</p>}
                    </div>
                </div>

                <div className="mb-4">
                    <label className="block text-gray-700 font-medium text-sm mb-1">Product of Interest</label>
                    <input
                        type="text"
                        value={
                            currentItem
                                ? `${currentItem.name} ${currentItem.variations?.length > 0
                                    ? `(${currentItem.variations.map((v) => `${v.variationName}: ${v.variationValue}`).join(", ")})`
                                    : ""
                                }`
                                : ""
                        }
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
                        className={`w-full p-2 border rounded-md text-sm focus:ring-1 focus:ring-red-300 focus:border-red-400 transition-all duration-200 ${formErrors.quantity ? "border-red-500" : "border-gray-300"} bg-white/50`}
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
                    disabled={isLoading}
                    onClick={handleFormSubmit}
                >
                    {isLoading ? (
                        <>
                            <Loader2 className="animate-spin" size={20} />
                            Saving...
                        </>
                    ) : (
                        "Register"
                    )}
                </Button>
            </Modal>

            <Footer className="h-12 mt-8 bg-gray-100" />
        </div>
    );
};

export default ShoppingCart;