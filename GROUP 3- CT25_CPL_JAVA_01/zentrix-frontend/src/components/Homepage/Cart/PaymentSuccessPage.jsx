import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { CheckCircle } from "lucide-react";
import axios from "axios";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";

const PaymentSuccessPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const searchParams = new URLSearchParams(location.search);

    const paymentMethod = localStorage.getItem("paymentMethod") || "unknown";

    const [transactionId, setTransactionId] = useState("N/A");
    const [amount, setAmount] = useState("N/A");

    useEffect(() => {
        const verifyPaymentAndAddOrder = async () => {
            try {
                let response;
                if (paymentMethod === "VNPay") {
                    response = await axios.get("http://localhost:6789/api/v1/payment/return", {
                        params: Object.fromEntries(searchParams),
                        withCredentials: true,
                    });
                } else if (paymentMethod === "MoMo") {
                    response = await axios.get("http://localhost:6789/api/v1/payment/return-momo", {
                        params: Object.fromEntries(searchParams),
                        withCredentials: true,
                    });
                } else {
                    throw new Error("Unsupported payment method");
                }

                const { status, transactionId, amount } = response.data;

                if (status === "success") {
                    setTransactionId(transactionId || "N/A");
                    setAmount(amount || "N/A");

                    // Lấy dữ liệu từ localStorage
                    const orderRequest = JSON.parse(localStorage.getItem("orderRequest"));
                    const orderDetailRequests = JSON.parse(localStorage.getItem("orderDetailRequests"));

                    if (!orderRequest || !orderDetailRequests) {
                        throw new Error("Order data not found in localStorage");
                    }

                    // Thêm đơn hàng
                    let orderId;
                    try {
                        const orderResponse = await axios.post(
                            "http://localhost:6789/api/v1/orders/addOrder",
                            orderRequest,
                            {
                                headers: {
                                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                                    "Content-Type": "application/json",
                                },
                                withCredentials: true,
                            }
                        );
                        const { content: savedOrder } = orderResponse.data;
                        orderId = savedOrder.orderId;
                    } catch (error) {
                        console.error("Error adding order:", error.response?.data || error.message);
                        throw new Error("Failed to add order: " + (error.response?.data?.message || error.message));
                    }

                    // Thêm chi tiết đơn hàng
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
                        await axios.delete(`http://localhost:6789/api/v1/orders/${orderId}`, {
                            headers: {
                                Authorization: `Bearer ${localStorage.getItem("token")}`,
                            },
                            withCredentials: true,
                        });
                        throw new Error("Failed to add order details: " + (error.response?.data?.message || error.message));
                    }

                    // Gọi API để tạo PDF và gửi email (luôn thực hiện)
                    if (!orderId) {
                        throw new Error("Order ID is null. Cannot generate invoice.");
                    }

                    try {
                        await axios.post(
                            `http://localhost:6789/api/v1/orders/${orderId}/generateInvoiceAndSendEmail`,
                            {},
                            {
                                headers: {
                                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                                    "Content-Type": "application/json",
                                },
                                withCredentials: true,
                            }
                        );
                        console.log("Invoice generated and sent successfully");
                    } catch (error) {
                        console.error("Error generating invoice and sending email:", error.response?.data || error.message);
                        throw new Error("Failed to generate invoice and send email: " + (error.response?.data?.message || error.message));
                    }

                    // Tính ngày giao hàng dự kiến
                    const today = new Date();
                    const expectedDelivery = new Date(today);
                    expectedDelivery.setDate(today.getDate() + 7);
                    const formattedDeliveryDate = expectedDelivery.toLocaleDateString("en-GB");

                    // Xóa dữ liệu trong localStorage
                    localStorage.removeItem("orderRequest");
                    localStorage.removeItem("orderDetailRequests");
                    localStorage.removeItem("paymentMethod");

                    // Chuyển hướng đến OrderSuccessPage
                    navigate("/order-success", {
                        state: {
                            transactionId: transactionId || "N/A",
                            expectedDeliveryDate: formattedDeliveryDate,
                            paymentMethod,
                            orderId,
                            amount: amount || "N/A",
                        },
                    });
                } else {
                    navigate("/order-processing");
                }
            } catch (error) {
                console.error("Error verifying payment or adding order:", error);
                navigate("/order-processing");
            }
        };

        if (paymentMethod !== "unknown") {
            verifyPaymentAndAddOrder();
        }
    }, [location.search, navigate, paymentMethod]);

    return (
        <div className="flex flex-col min-h-screen bg-gray-100">
            <Header className="fixed top-0 left-0 w-full h-16 bg-white shadow-md z-50" />
            <div className="flex-grow flex items-center justify-center p-4 pt-20">
                <div className="bg-white p-8 rounded-lg shadow-lg text-center max-w-md w-full">
                    <div className="flex justify-center mb-4">
                        <div className="relative">
                            <CheckCircle className="w-16 h-16 text-blue-500" />
                            <div className="absolute top-0 left-0 w-4 h-4 bg-blue-200 rounded-full opacity-50 animate-pulse"></div>
                            <div className="absolute bottom-0 right-0 w-4 h-4 bg-blue-200 rounded-full opacity-50 animate-pulse delay-200"></div>
                            <div className="absolute top-4 left-4 w-2 h-2 bg-blue-200 rounded-full opacity-50 animate-pulse delay-400"></div>
                        </div>
                    </div>
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Processing Payment...</h2>
                    <p className="text-gray-600 mb-4">Please wait while we verify your payment and create your order.</p>
                </div>
            </div>
            <Footer className="h-12 mt-8" />
        </div>
    );
};

export default PaymentSuccessPage;