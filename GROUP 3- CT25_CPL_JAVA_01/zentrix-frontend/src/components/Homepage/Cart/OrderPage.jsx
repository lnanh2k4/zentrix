import { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import axios from "axios";
import jsPDF from "jspdf";
import { Loader2 } from "lucide-react";
import { showNotification } from "@/components/Dashboard/NotificationPopup";

const OrderPage = () => {
    const { state } = useLocation();
    const {
        cart: selectedCart,
        totalAmount,
        userPromotions: initialPromotions = [], // Đổi tên để tránh xung đột
        selectedDiscount = 0,
    } = state || { cart: [], totalAmount: 0, userPromotions: [], selectedDiscount: 0 };
    const navigate = useNavigate();
    const [brchId, setBrchId] = useState(null);
    const [cart, setCart] = useState(
        selectedCart.map((item) => ({
            ...item,
            prodTypeId: item.prodTypeId,
            prodTypeBranchId: item.prodTypeBranchId || item.prodTypeId,
        })) || []
    );
    const [discount, setDiscount] = useState(selectedDiscount);
    const [userInfo, setUserInfo] = useState({
        userId: null,
        fullName: "",
        phoneNumber: "",
        email: "",
        address: "",
        cityDistrict: "",
        note: "",
        customerCode: "S-NULL",
        receiveEmail: false,
        requestOtherPerson: false,
        requestTechnicalSupport: false,
        requestInvoice: false,
    });
    const [userPromotions, setUserPromotions] = useState(initialPromotions); // Khởi tạo state userPromotions
    const [deliveryMethod, setDeliveryMethod] = useState("delivery");
    const [paymentMethod, setPaymentMethod] = useState("");
    const [loading, setLoading] = useState(true);
    const [isPlacingOrder, setIsPlacingOrder] = useState(false);
    const [orderId, setOrderId] = useState(null);
    const [errors, setErrors] = useState({});

    const fetchUserInfo = async () => {
        try {
            setLoading(true);
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success && response.data.content) {
                const user = response.data.content;
                setUserInfo((prev) => ({
                    ...prev,
                    userId: user.userId || null,
                    fullName: user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : "Guest Vu",
                    phoneNumber: user.phone || "N/A",
                    email: user.email || "vuxblack@gmail.com",
                }));
            } else {
                setUserInfo((prev) => ({
                    ...prev,
                    userId: null,
                    fullName: "Guest Vu",
                    phoneNumber: "N/A",
                    email: "vuxblack@gmail.com",
                }));
            }
        } catch (error) {
            console.error("Error fetching user info:", error.message);
            setUserInfo((prev) => ({
                ...prev,
                userId: null,
                fullName: "Guest Vu",
                phoneNumber: "N/A",
                email: "vuxblack@gmail.com",
            }));
        } finally {
            setLoading(false);
        }
    };

    // Hàm fetch user promotions
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

    // Hàm fetch chi tiết promotions
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

            console.log("Valid promotions in OrderPage:", validPromotions);
            setUserPromotions(validPromotions);
        } catch (error) {
            console.error("Error fetching promotions:", error.response?.data || error.message);
            setUserPromotions([]);
        }
    };

    useEffect(() => {
        const loadData = async () => {
            await fetchUserInfo();
            const savedBranchId = localStorage.getItem("selectedBranchId");
            if (savedBranchId) {
                setBrchId(Number(savedBranchId));
            } else {
                setBrchId(5);
                console.warn("No branch ID found in localStorage, using default brchId: 5");
            }
            // Fetch promotions sau khi có userId
            if (userInfo.userId) {
                await fetchUserPromotions(userInfo.userId);
            }
        };
        loadData();
    }, [userInfo.userId]); // Thêm userInfo.userId vào dependency để fetch lại nếu userId thay đổi

    const formatPrice = (price) => {
        const formattedPrice = new Intl.NumberFormat("vi-VN", {
            style: "currency",
            currency: "VND",
            currencyDisplay: "code",
        }).format(price);
        return formattedPrice.replace("VND", "VNĐ");
    };

    const totalPrice = cart.reduce((sum, item) => sum + item.salePrice * item.quantity, 0);
    const originalTotalPrice = cart.reduce((sum, item) => sum + item.originalPrice * item.quantity, 0);
    const discountAmount = (discount / 100) * totalPrice;
    const totalVAT = cart.reduce((sum, item) => {
        const vatRate = (item.vat || 0) / 100;
        const itemPrice = item.salePrice * item.quantity;
        return sum + itemPrice * vatRate;
    }, 0);
    const shippingFee = 0;
    const finalTotalPrice = totalPrice + totalVAT - discountAmount + shippingFee;

    const validateForm = () => {
        const newErrors = {};

        if (!userInfo.fullName.trim()) {
            newErrors.fullName = "Full name is required";
        }

        const phoneRegex = /^0\d{9}$/;
        if (!userInfo.phoneNumber.trim()) {
            newErrors.phoneNumber = "Phone number is required";
        } else if (userInfo.phoneNumber.trim().startsWith("google")) {
            newErrors.phoneNumber = "Please go to your profile and update your phone number";
            showNotification("Please go to your profile and update your phone number", 3000, "fail");
        } else if (!phoneRegex.test(userInfo.phoneNumber)) {
            newErrors.phoneNumber = "Please enter a valid phone number (e.g., 0123456789)";
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!userInfo.email.trim()) {
            newErrors.email = "Email is required";
        } else if (!emailRegex.test(userInfo.email)) {
            newErrors.email = "Please enter a valid email address";
        }

        if (deliveryMethod === "delivery") {
            if (!userInfo.address.trim()) {
                newErrors.address = "Address is required";
                showNotification("Please go to your profile and update your phone number", 3000, "fail");
            }
            if (!userInfo.cityDistrict.trim()) {
                newErrors.cityDistrict = "Province/City, District, Ward/Commune is required";
            }
        }

        if (!paymentMethod) {
            newErrors.paymentMethod = "Please select a payment method";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleUserInfoChange = (e) => {
        const { name, value, type, checked } = e.target;
        setUserInfo((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));

        setErrors((prev) => ({
            ...prev,
            [name]: "",
        }));
    };

    const fetchBranchInfo = async (brchId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/branches/${brchId}`, {
                headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                withCredentials: true,
            });
            return response.data.content?.brchName || "N/A";
        } catch (error) {
            console.error(`Error fetching branch info for brchId ${brchId}:`, error);
            return "N/A";
        }
    };

    const fetchProdTypeBranchId = async (prodTypeId, brchId) => {
        try {
            const response = await axios.get(
                "http://localhost:6789/api/v1/products/productTypeBranch/find",
                {
                    params: { prodTypeId, brchId },
                    headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                    withCredentials: true,
                }
            );
            if (!response.data) throw new Error("Could not find prodTypeBranchId");
            return response.data;
        } catch (error) {
            console.error(`Error fetching prodTypeBranchId for prodTypeId ${prodTypeId}:`, error);
            throw error;
        }
    };

    const handleExportPDF = async () => {
        const doc = new jsPDF();
        const brchName = await fetchBranchInfo(brchId);

        doc.setFontSize(18);
        doc.text(`Invoice - Order #${orderId || "Pending"}`, 14, 20);
        doc.setFontSize(11);
        doc.text(`Branch: ${brchName}`, 14, 30);
        doc.text(`Customer: ${userInfo.fullName || "N/A"}`, 14, 40);
        doc.text(`Date: ${new Date().toLocaleDateString()}`, 14, 50);
        doc.text(`Payment Method: ${paymentMethod || "N/A"}`, 14, 60);

        const tableColumn = ["Product Type", "Quantity", "Unit Price", "Total"];
        const tableRows = cart.map((item) => [
            item.name || "N/A",
            item.quantity.toString(),
            formatPrice(item.salePrice),
            formatPrice(item.salePrice * item.quantity),
        ]);
        tableRows.push(["", "", "     TOTAL:", `  ${formatPrice(finalTotalPrice)}`]);

        let startY = 70;
        const pageWidth = 190;
        const colWidths = [90, 20, 40, 40];
        const rowHeight = 10;

        doc.setFontSize(10);
        doc.setFillColor(22, 160, 133);
        doc.rect(14, startY, pageWidth, rowHeight, "F");
        doc.setTextColor(255, 255, 255);
        let currentX = 14;
        tableColumn.forEach((header, index) => {
            doc.text(header, currentX + 2, startY + 7);
            currentX += colWidths[index];
        });
        doc.setDrawColor(0);
        doc.rect(14, startY, pageWidth, rowHeight);

        doc.setTextColor(0, 0, 0);
        startY += rowHeight;

        tableRows.forEach((row, rowIndex) => {
            currentX = 14;
            if (rowIndex % 2 === 0) {
                doc.setFillColor(240, 240, 240);
                doc.rect(14, startY, pageWidth, rowHeight, "F");
            }
            row.forEach((cell, colIndex) => {
                if (rowIndex === tableRows.length - 1 && colIndex === 2) {
                    doc.text(cell, currentX + 2, startY + 7);
                } else if (rowIndex === tableRows.length - 1 && colIndex === 3) {
                    doc.text(cell, currentX + 2, startY + 7);
                } else if (colIndex === 1 || colIndex === 2 || colIndex === 3) {
                    const textWidth = doc.getTextWidth(cell);
                    const offset = (colWidths[colIndex] - textWidth) / 2;
                    doc.text(cell, currentX + offset, startY + 7);
                } else {
                    doc.text(cell, currentX + 2, startY + 7);
                }
                currentX += colWidths[colIndex];
            });
            doc.rect(14, startY, pageWidth, rowHeight);
            startY += rowHeight;
        });

        currentX = 14;
        for (let i = 0; i <= tableColumn.length; i++) {
            doc.line(currentX, 70, currentX, startY);
            if (i < tableColumn.length) currentX += colWidths[i];
        }

        doc.save(`invoice_${orderId || "pending"}.pdf`);
    };

    const handlePlaceOrder = async () => {
        if (!userInfo.userId) {
            showNotification("Unable to identify user. Please log in again.", 3000, "fail");
            return;
        }

        const isValid = validateForm();
        if (!isValid) {
            return;
        }

        setIsPlacingOrder(true);

        const orderRequest = {
            userId: userInfo.userId,
            address: `${userInfo.address}, ${userInfo.cityDistrict}`,
            status: 0,
            paymentMethod,
            createdAt: new Date().toISOString(),
            promId: discount > 0 ? userPromotions.find((p) => p.discount === discount)?.promId : null,
            brchId: brchId,
        };

        let orderDetailRequests;
        try {
            orderDetailRequests = await Promise.all(
                cart.map(async (item) => {
                    const prodTypeBranchId = await fetchProdTypeBranchId(item.prodTypeId, orderRequest.brchId);
                    if (!prodTypeBranchId) {
                        throw new Error(`Could not find prodTypeBranchId for product ${item.name}`);
                    }
                    return {
                        prodTypeBranchId,
                        quantity: item.quantity,
                        unitPrice: Math.round(item.salePrice),
                        variation: item.variations.map((v) => `${v.variationName}: ${v.variationValue}`).join(", ") || "N/A",
                        amountNotVat: item.salePrice * item.quantity,
                        vatRate: item.vat || 0,
                        cartProductTypeVariationIds: item.cartProductTypeVariationIds,
                    };
                })
            );
        } catch (error) {
            console.error("Error preparing order details:", error.message);
            showNotification("Unable to prepare order details. Please try again.", 3000, "fail");
            setIsPlacingOrder(false);
            return;
        }

        try {
            if (paymentMethod === "Cash on Delivery") {
                let orderId;
                try {
                    const orderResponse = await axios.post(
                        "http://localhost:6789/api/v1/orders/addOrder",
                        orderRequest,
                        {
                            headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                            withCredentials: true,
                        }
                    );
                    const { content: savedOrder } = orderResponse.data;
                    orderId = savedOrder.orderId;
                    setOrderId(orderId);
                } catch (error) {
                    console.error("Error adding order:", error.response?.data || error.message);
                    showNotification("Unable to add order: " + (error.response?.data?.message || error.message), 3000, "fail");
                    setIsPlacingOrder(false);
                    return;
                }

                try {
                    for (const detail of orderDetailRequests) {
                        console.log("Order Detail:", detail);
                        await axios.post(
                            "http://localhost:6789/api/v1/orders/addOrderDetail",
                            { ...detail, orderId },
                            {
                                withCredentials: true,
                            }
                        );
                    }
                } catch (error) {
                    console.error("Error adding order details:", error.response?.data || error.message);
                    showNotification("Unable to add order details. The order will be canceled.", 3000, "fail");
                    await axios.delete(`http://localhost:6789/api/v1/orders/${orderId}`, {
                        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                        withCredentials: true,
                    });
                    setIsPlacingOrder(false);
                    return;
                }

                try {
                    await axios.post(
                        `http://localhost:6789/api/v1/orders/${orderId}/generateInvoiceAndSendEmail`,
                        {},
                        {
                            headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                            withCredentials: true,
                        }
                    );
                    console.log("Invoice generated and sent successfully");
                } catch (error) {
                    console.error("Error generating invoice and sending email:", error.response?.data || error.message);
                    showNotification("Unable to generate invoice and send email: " + (error.response?.data?.message || error.message), 3000, "fail");
                    setIsPlacingOrder(false);
                    return;
                }

                const today = new Date();
                const expectedDelivery = new Date(today);
                expectedDelivery.setDate(today.getDate() + 7);
                const formattedDeliveryDate = expectedDelivery.toLocaleDateString("en-GB");
                const mockTransactionId = Math.floor(Math.random() * 10000000000).toString();

                navigate("/order-success", {
                    state: {
                        transactionId: mockTransactionId,
                        expectedDeliveryDate: formattedDeliveryDate,
                        paymentMethod: "Cash on Delivery",
                        orderId,
                        listProduct: cart,
                        cartProductTypeVariationIds: orderDetailRequests.flatMap(detail => detail.cartProductTypeVariationIds),
                    },
                });
            } else if (paymentMethod === "VNPay") {
                localStorage.setItem("orderRequest", JSON.stringify(orderRequest));
                localStorage.setItem("orderDetailRequests", JSON.stringify(orderDetailRequests));

                const paymentResponse = await axios.post(
                    "http://localhost:6789/api/v1/payment/create",
                    null,
                    {
                        params: { totalAmount: Math.round(finalTotalPrice) },
                        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                        withCredentials: true,
                    }
                );
                const paymentUrl = paymentResponse.data;
                if (paymentUrl) {
                    localStorage.setItem("paymentMethod", "VNPay");
                    window.location.href = paymentUrl;
                }
            } else if (paymentMethod === "MoMo") {
                localStorage.setItem("orderRequest", JSON.stringify(orderRequest));
                localStorage.setItem("orderDetailRequests", JSON.stringify(orderDetailRequests));

                const totalAmount = Math.round(finalTotalPrice);
                const orderInfo = "MoMo Order Payment";
                const paymentResponse = await axios.post(
                    "http://localhost:6789/api/v1/payment/create-momo",
                    null,
                    {
                        params: { totalAmount, orderInfo },
                        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                        withCredentials: true,
                    }
                );
                const paymentUrl = paymentResponse.data;
                if (paymentUrl) {
                    localStorage.setItem("paymentMethod", "MoMo");
                    window.location.href = paymentUrl;
                }
            }
        } catch (error) {
            console.error("Unknown error during order placement:", error);
            showNotification("An error occurred during order placement. Please try again.", 3000, "fail");
        } finally {
            setIsPlacingOrder(false);
        }
    };

    useEffect(() => {
        if (!loading && !userInfo.userId) {
            navigate("/login");
        }
    }, [loading, userInfo.userId, navigate]);

    if (loading) {
        return (
            <div className="flex justify-center items-center min-h-screen bg-gray-100">
                <div className="text-lg font-semibold text-gray-600 animate-pulse">Loading...</div>
            </div>
        );
    }

    return (
        <div className="flex flex-col min-h-screen bg-gray-100">
            <Header className="fixed top-0 left-0 w-full h-16 bg-white shadow-md z-50" />
            <div className="max-w-6xl mx-auto p-8 pt-24 flex-grow grid grid-cols-4 gap-5">
                <div className="col-span-3 space-y-6">
                    <div className="bg-white p-6 rounded-lg">
                        <h2 className="text-lg font-semibold mb-4">Items in Order ({cart.length})</h2>
                        {cart.map((item) => (
                            <div
                                key={item.prodTypeBranchId}
                                className="flex items-center justify-between p-4 bg-white mb-4 border-gray-200"
                            >
                                <div className="flex items-center space-x-4">
                                    <img
                                        src={
                                            item.urlImage
                                                ? item.urlImage.startsWith("http") || item.urlImage.startsWith("blob:")
                                                    ? item.urlImage
                                                    : `http://localhost:6789${item.urlImage.startsWith("/") ? "" : "/"}${item.urlImage}`
                                                : "/images/default-product.jpg"
                                        }
                                        alt={item.name}
                                        className="w-16 h-16 object-cover rounded"
                                        onError={(e) => (e.target.src = "/images/default-product.jpg")}
                                    />
                                    <div>
                                        <p className="text-md font-medium text-gray-800">{item.name}</p>
                                        {item.variations.map((variation) => (
                                            <p key={variation.id} className="text-lg font-normal">
                                                {variation.variationName}: {variation.variationValue}
                                            </p>
                                        ))}
                                        <div className="flex items-center space-x-2">
                                            <p className="text-red-500 font-bold text-md">{formatPrice(item.salePrice)}</p>
                                            <p className="text-gray-400 line-through text-sm">{formatPrice(item.originalPrice)}</p>
                                        </div>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <p className="text-gray-600">Quantity: {item.quantity}</p>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="bg-white p-6 rounded-lg">
                        <h3 className="text-lg font-semibold mb-4">Customer Information</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="col-span-2">
                                <input
                                    type="text"
                                    name="fullName"
                                    value={userInfo.fullName}
                                    onChange={handleUserInfoChange}
                                    placeholder="Full Name"
                                    className={`w-full p-2 border rounded ${errors.fullName ? "border-red-500" : ""}`}
                                    disabled
                                />
                                {errors.fullName && <p className="text-red-500 text-sm mt-1">{errors.fullName}</p>}
                            </div>
                            <div className="col-span-2">
                                <input
                                    type="text"
                                    name="phoneNumber"
                                    value={userInfo.phoneNumber}
                                    onChange={handleUserInfoChange}
                                    placeholder="Phone Number"
                                    className={`w-full p-2 border rounded ${errors.phoneNumber ? "border-red-500" : ""}`}
                                    disabled
                                />
                                {errors.phoneNumber && <p className="text-red-500 text-sm mt-1">{errors.phoneNumber}</p>}
                            </div>
                            <div className="col-span-2">
                                <input
                                    type="text"
                                    name="email"
                                    value={userInfo.email}
                                    onChange={handleUserInfoChange}
                                    placeholder="Email"
                                    className={`w-full p-2 border rounded ${errors.email ? "border-red-500" : ""}`}
                                    disabled
                                />
                                {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
                            </div>
                        </div>
                    </div>

                    <div className="bg-white p-6 rounded-lg">
                        <div className="space-y-4">
                            <div>
                                <h3 className="text-lg font-semibold mb-4">Delivery Method</h3>
                                <label className="mr-4">
                                    <input
                                        type="radio"
                                        name="delivery"
                                        value="delivery"
                                        checked={deliveryMethod === "delivery"}
                                        onChange={() => setDeliveryMethod("delivery")}
                                    />{" "}
                                    Home Delivery
                                </label>
                            </div>

                            {deliveryMethod === "delivery" && (
                                <div className="space-y-4">
                                    <div>
                                        <input
                                            type="text"
                                            name="address"
                                            value={userInfo.address}
                                            onChange={handleUserInfoChange}
                                            placeholder="Address"
                                            className={`w-full p-2 border rounded ${errors.address ? "border-red-500" : ""}`}
                                        />
                                        {errors.address && <p className="text-red-500 text-sm mt-1">{errors.address}</p>}
                                    </div>
                                    <div>
                                        <input
                                            type="text"
                                            name="cityDistrict"
                                            value={userInfo.cityDistrict}
                                            onChange={handleUserInfoChange}
                                            placeholder="Province/City, District, Ward/Commune"
                                            className={`w-full p-2 border rounded ${errors.cityDistrict ? "border-red-500" : ""}`}
                                        />
                                        {errors.cityDistrict && (
                                            <p className="text-red-500 text-sm mt-1">{errors.cityDistrict}</p>
                                        )}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="bg-white p-6 rounded-lg">
                        <h3 className="text-lg font-semibold mb-4">Payment Method</h3>
                        <div className="grid grid-cols-2 gap-4">
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input
                                    type="radio"
                                    name="paymentMethod"
                                    value="Cash on Delivery"
                                    checked={paymentMethod === "Cash on Delivery"}
                                    onChange={() => {
                                        setPaymentMethod("Cash on Delivery");
                                        setErrors((prev) => ({ ...prev, paymentMethod: "" }));
                                    }}
                                />
                                <img
                                    src="https://s3-sgn09.fptcloud.com/ict-payment-icon/payment/cod.png?w=96&q=100"
                                    alt="Cash on Delivery"
                                    className="w-6 h-6"
                                />
                                <span>Cash on Delivery</span>
                            </label>
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input
                                    type="radio"
                                    name="paymentMethod"
                                    value="VNPay"
                                    checked={paymentMethod === "VNPay"}
                                    onChange={() => {
                                        setPaymentMethod("VNPay");
                                        setErrors((prev) => ({ ...prev, paymentMethod: "" }));
                                    }}
                                />
                                <img
                                    src="https://s3-sgn09.fptcloud.com/ict-payment-icon/payment/vnpay.png?w=96&q=100"
                                    alt="VNPay"
                                    className="w-6 h-6"
                                />
                                <span>VNPay</span>
                            </label>
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input
                                    type="radio"
                                    name="paymentMethod"
                                    value="MoMo"
                                    checked={paymentMethod === "MoMo"}
                                    onChange={() => {
                                        setPaymentMethod("MoMo");
                                        setErrors((prev) => ({ ...prev, paymentMethod: "" }));
                                    }}
                                />
                                <img
                                    src="https://s3-sgn09.fptcloud.com/ict-payment-icon/payment/momo.png?w=96&q=100"
                                    alt="MoMo"
                                    className="w-6 h-6"
                                />
                                <span>MoMo</span>
                            </label>
                        </div>
                        {errors.paymentMethod && (
                            <p className="text-red-500 text-sm mt-2">{errors.paymentMethod}</p>
                        )}
                    </div>
                </div>

                <div className="bg-white p-4 rounded-lg shadow-lg w-fit min-w-[300px] h-fit max-h-[400px] sticky top-20 self-start z-10 col-span-1">
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
                            <span className="font-semibold whitespace-nowrap">{formatPrice(originalTotalPrice)}</span>
                        </div>
                        <div className="flex justify-between text-gray-600">
                            <span>Total Discount</span>
                            <span className="text-black font-semibold whitespace-nowrap">
                                -{formatPrice(originalTotalPrice - totalPrice)}
                            </span>
                        </div>
                        <div className="flex justify-between text-gray-600">
                            <span>Additional Discount</span>
                            <span className="text-black font-semibold whitespace-nowrap">
                                -{formatPrice(discountAmount)}
                            </span>
                        </div>
                        <div className="flex justify-between text-gray-600">
                            <span>VAT</span>
                            <span className="text-black font-semibold whitespace-nowrap">{formatPrice(totalVAT)}</span>
                        </div>
                        <hr className="my-2" />
                        <div className="flex justify-between font-semibold text-lg">
                            <span>Final Total</span>
                            <span className="text-red-500 whitespace-nowrap">{formatPrice(finalTotalPrice)}</span>
                        </div>
                    </div>
                    <Button
                        className={`w-full mt-4 py-3 text-lg font-semibold flex items-center justify-center gap-2 ${!userInfo.userId || loading || isPlacingOrder
                            ? "bg-gray-300 text-gray-600 cursor-not-allowed"
                            : "bg-red-500 text-white hover:bg-red-600"
                            } transition-colors duration-200`}
                        disabled={loading || !userInfo.userId || isPlacingOrder}
                        onClick={handlePlaceOrder}
                    >
                        {isPlacingOrder ? (
                            <>
                                <Loader2 className="animate-spin" size={20} />
                                Placing Order...
                            </>
                        ) : (
                            "Place Order"
                        )}
                    </Button>
                </div>
            </div>
            <Footer className="h-12 mt-8" />
        </div>
    );
};

export default OrderPage;