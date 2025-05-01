import { useLocation, useNavigate } from "react-router-dom";
import { CheckCircle } from "lucide-react";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import { useEffect, useState, useRef } from "react";
import axios from "axios";
import jsPDF from "jspdf";

const OrderSuccessPage = () => {
    const { state } = useLocation();
    const navigate = useNavigate();
    const {
        transactionId = "823748734273",
        expectedDeliveryDate = "14/06/2018",
        orderId: initialOrderId,
        paymentMethod,
        amount,
        orderRequest,
        orderDetailRequests,
        listProduct,
        requestInvoice = false,
    } = state || {};

    const [listItems, setListItem] = useState([]);
    const [orderId, setOrderId] = useState(initialOrderId);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const hasProcessed = useRef(false);

    useEffect(() => {
        if (listProduct) {
            setListItem(listProduct);
        }
    }, [listProduct]);

    const fetchBranchInfo = async (brchId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/branches/${brchId}`, {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                    "Content-Type": "application/json",
                },
                withCredentials: true,
            });
            if (response.data && response.data.content) {
                return response.data.content.brchName || "N/A";
            }
            return "N/A";
        } catch (error) {
            console.error(`OrderSuccessPage: Error fetching branch info for brchId ${brchId}:`, error);
            return "N/A";
        }
    };

    const handleExportPDF = async () => {
        const doc = new jsPDF();

        const brchId = orderRequest?.brchId || 5;
        const brchName = await fetchBranchInfo(brchId);

        doc.setFontSize(18);
        doc.text(`Invoice - Order #${orderId || "Pending"}`, 14, 20);

        doc.setFontSize(11);
        doc.text(`Branch: ${brchName}`, 14, 30);
        doc.text(`Customer: ${orderRequest?.userId || "N/A"}`, 14, 40);
        doc.text(`Date: ${new Date().toLocaleDateString()}`, 14, 50);
        doc.text(`Payment Method: ${paymentMethod || "N/A"}`, 14, 60);

        const tableColumn = ["Product Type", "Quantity", "Unit Price", "Total"];
        const tableRows = [];

        listItems.forEach(item => {
            const detailData = [
                item.name || "N/A",
                item.quantity.toString(),
                `${item.salePrice.toLocaleString()} VND`,
                `${(item.salePrice * item.quantity).toLocaleString()} VND`,
            ];
            tableRows.push(detailData);
        });

        const totalPrice = listItems.reduce((sum, item) => sum + item.salePrice * item.quantity, 0);
        const totalVAT = listItems.reduce((sum, item) => {
            const vatRate = item.vat || 0;
            const itemPrice = item.salePrice * item.quantity;
            return sum + itemPrice * vatRate;
        }, 0);
        const finalTotalPrice = totalPrice + totalVAT;

        tableRows.push(["", "", "     TOTAL:", `  ${finalTotalPrice.toLocaleString()} VND`]);

        let startY = 70;
        const pageWidth = 190;
        const colWidths = [90, 20, 40, 40];
        const rowHeight = 10;

        doc.setFontSize(10);
        doc.setFillColor(22, 160, 133);
        doc.rect(14, startY, pageWidth, rowHeight, 'F');
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
                doc.rect(14, startY, pageWidth, rowHeight, 'F');
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

    const addOrder = async () => {
        try {
            if (!orderRequest || !orderDetailRequests) {
                throw new Error("Order data not found in state");
            }

            sessionStorage.setItem("orderRequestBackup", JSON.stringify(orderRequest));
            sessionStorage.setItem("orderDetailRequestsBackup", JSON.stringify(orderDetailRequests));

            let newOrderId;
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
                newOrderId = savedOrder.orderId;
                setOrderId(newOrderId);
            } catch (error) {
                console.error("OrderSuccessPage: Error adding order:", error.response?.data || error.message);
                throw new Error("Failed to add order: " + (error.response?.data?.message || error.message));
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
                console.error("OrderSuccessPage: Error adding order details:", error.response?.data || error.message);
                await axios.delete(`http://localhost:6789/api/v1/orders/${newOrderId}`, {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                    withCredentials: true,
                });
                throw new Error("Failed to add order details: " + (error.response?.data?.message || error.message));
            }

            // Xử lý invoice và email trực tiếp ở đây
            try {
                await axios.post(
                    `http://localhost:6789/api/v1/orders/${newOrderId}/generateInvoiceAndSendEmail`,
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
                showNotification("Unable to generate invoice and send email: " + (error.response?.data?.message || error.message), 3000, "fail");
            }

            // Giữ lại handleExportPDF nếu bạn vẫn muốn xuất PDF
            await handleExportPDF();

            sessionStorage.removeItem("orderRequestBackup");
            sessionStorage.removeItem("orderDetailRequestsBackup");
        } catch (error) {
            setError(error.message);
            navigate("/order-failure", { state: { errorMessage: error.message } });
        } finally {
            setLoading(false);
            hasProcessed.current = true;
        }
    };

    useEffect(() => {
        console.log("OrderSuccessPage: Received state", state);
        if (hasProcessed.current) {
            setLoading(false);
            return;
        }

        if (initialOrderId) {
            setLoading(false);
            hasProcessed.current = true;
        } else if (orderRequest && orderDetailRequests) {
            console.log("OrderSuccessPage: Proceeding to addOrder", { orderRequest, orderDetailRequests });
            addOrder();
        } else {
            const orderRequestBackup = JSON.parse(sessionStorage.getItem("orderRequestBackup"));
            const orderDetailRequestsBackup = JSON.parse(sessionStorage.getItem("orderDetailRequestsBackup"));

            if (orderRequestBackup && orderDetailRequestsBackup) {
                console.log("OrderSuccessPage: Restoring from backup", { orderRequestBackup, orderDetailRequestsBackup });
                navigate("/order-success", {
                    state: {
                        ...state,
                        orderRequest: orderRequestBackup,
                        orderDetailRequests: orderDetailRequestsBackup,
                    },
                    replace: true,
                });
            } else {
                console.error("OrderSuccessPage: Missing order data and no backup available", {
                    orderRequest,
                    orderDetailRequests,
                    state,
                });
                setLoading(false);
                setError("Order data is missing. Please try again.");
                navigate("/order-failure", { state: { errorMessage: "Order data is missing. Please try again." } });
            }
        }
    }, [navigate, orderRequest, orderDetailRequests, initialOrderId]);

    const handleViewOrderStatus = () => {
        navigate("/history", { state: { orderId } });
    };

    if (loading) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Header className="fixed top-0 left-0 w-full h-16 bg-white shadow-md z-50" />
                <div className="flex-grow flex items-center justify-center p-4 pt-20">
                    <div className="bg-white p-8 rounded-lg shadow-lg text-center max-w-md w-full">
                        <h2 className="text-2xl font-bold text-gray-800 mb-2">Processing Order...</h2>
                        <p className="text-gray-600 mb-4">Please wait while we create your order.</p>
                    </div>
                </div>
                <Footer className="h-12 mt-8" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Header className="fixed top-0 left-0 w-full h-16 bg-white shadow-md z-50" />
                <div className="flex-grow flex items-center justify-center p-4 pt-20">
                    <div className="bg-white p-8 rounded-lg shadow-lg text-center max-w-md w-full">
                        <h2 className="text-2xl font-bold text-red-500 mb-2">Order Failed</h2>
                        <p className="text-gray-600 mb-4">{error}</p>
                        <button
                            className="bg-red-500 text-white px-4 py-2 rounded"
                            onClick={() => navigate("/order")}
                        >
                            Try Again
                        </button>
                    </div>
                </div>
                <Footer className="h-12 mt-8" />
            </div>
        );
    }

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
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Order Successfully Placed</h2>
                    <p className="text-gray-600 mb-4">To check order status, check your dashboard</p>
                    <p className="text-gray-600 mb-4">
                        Transaction ID: <span className="font-semibold">{transactionId}</span>
                    </p>
                    <p className="text-gray-600 mb-4">
                        Order ID: <span className="font-semibold">{orderId || "N/A"}</span>
                    </p>
                    {amount && (
                        <p className="text-gray-600 mb-4">
                            Amount: <span className="font-semibold">{parseFloat(amount).toLocaleString()} VND</span>
                        </p>
                    )}
                    <p className="text-yellow-600 font-semibold mb-6">
                        Expected Delivery Date: {expectedDeliveryDate}
                    </p>
                    <button
                        onClick={handleViewOrderStatus}
                        className="w-full bg-gray-800 text-white py-3 rounded-lg hover:bg-gray-900 transition"
                    >
                        View Order History
                    </button>
                </div>
            </div>
            <Footer className="h-12 mt-8" />
        </div>
    );
};

export default OrderSuccessPage;