import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Eye } from "lucide-react";
import Sidebar from "@/components/ui/Sidebar";
import Header from "@/components/ui/Header";
import axios from "axios";
import { Input } from "@/components/ui/input";
import { useNavigate } from "react-router-dom";
import { showNotification } from "@/components/Dashboard/NotificationPopup";

const OrderHistoryPage = () => {
    const [selectedTab, setSelectedTab] = useState("all");
    const [userInfo, setUserInfo] = useState({ userId: null, firstName: "", lastName: "", phone: "" });
    const [loading, setLoading] = useState(true);
    const [orders, setOrders] = useState([]);
    const [filteredOrders, setFilteredOrders] = useState([]);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(20);
    const [membershipLevel, setMembershipLevel] = useState("UNRANK");
    const [userPoints, setUserPoints] = useState(0);
    const [searchKeyword, setSearchKeyword] = useState("");
    const [searchError, setSearchError] = useState(null);
    const navigate = useNavigate();
    const API_URL_ORDERS = "http://localhost:6789/api/v1/orders";
    const API_URL_MEMBERSHIPS = "http://localhost:6789/api/v1/memberships";
    const API_URL_PRODUCT_TYPE_BRANCH = "http://localhost:6789/api/v1/products/productTypeBranch";
    const USER_INFO_URL = "http://localhost:6789/api/v1/auth/info";

    const STATUS_TEXT_MAPPING = {
        0: "Pending",
        1: "Confirmed",
        2: "Shipping",
        3: "Delivered",
        4: "Canceled",
        5: "Received",
    };

    const STATUS_NUMBER_MAPPING = {
        Pending: 0,
        Confirmed: 1,
        Shipping: 2,
        Delivered: 3,
        Canceled: 4,
        Received: 5,
    };

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get(USER_INFO_URL, { withCredentials: true });
            if (response.data.success && response.data.content) {
                return response.data.content;
            }
            console.error("No user data found in response:", response.data);
            return null;
        } catch (error) {
            console.error("Error fetching user info:", error.message);
            return null;
        }
    };

    const fetchOrders = async (userId, keyword = "") => {
        if (!userId) return { orders: [] };
        try {
            setSearchError(null);
            let url = keyword
                ? `${API_URL_ORDERS}/user/${userId}/search?keyword=${encodeURIComponent(keyword)}&page=0&size=1000`
                : `${API_URL_ORDERS}/user/${userId}?page=0&size=1000`;

            console.log("Fetching orders with URL:", url);

            const response = await axios.get(url, { withCredentials: true });
            if (response.data.success) {
                const orders = response.data.content.map(order => ({
                    ...order,
                    status: typeof order.status === "number" ? order.status : 0,
                    createdAt: order.createdAt
                        ? new Date(order.createdAt).toLocaleDateString("en-GB", {
                            day: "2-digit",
                            month: "2-digit",
                            year: "numeric",
                        }).split("/").join("-")
                        : "N/A",
                    showDetails: false,
                }));
                return { orders };
            }
            return { orders: [] };
        } catch (error) {
            console.error("Error fetching orders:", error.message);
            const errorMessage = error.response
                ? `API Error: ${error.response.status} - ${error.response.data?.message || error.message}. Please check your access permissions.`
                : `Network Error: ${error.message}. Please ensure the backend server is running on ${API_URL_ORDERS}.`;
            setSearchError(errorMessage);
            return { orders: [] };
        }
    };

    const fetchMemberships = async () => {
        try {
            const response = await axios.get(API_URL_MEMBERSHIPS, { withCredentials: true });
            return response.data.content;
        } catch (error) {
            console.error("Error fetching memberships:", error);
            return [];
        }
    };

    const filterByStatus = (orders, statusFilter) => {
        if (statusFilter === "all") {
            return orders;
        }

        const statusNumber = STATUS_NUMBER_MAPPING[statusFilter.charAt(0).toUpperCase() + statusFilter.slice(1)];
        const filtered = orders.filter(order => order.status === statusNumber);
        console.log(`Filtered orders for status ${statusFilter} (${statusNumber}):`, filtered);
        return filtered;
    };

    // Hàm cập nhật số lượng tồn kho
    const updateProductTypeBranchQuantity = async (orderDetails) => {
        try {
            console.log("Order details for updating quantity:", orderDetails);

            for (const detail of orderDetails) {
                const prodTypeBranchId = detail.prodTypeBranchId?.prodTypeBranchId;
                const quantityToReturn = detail.quantity;

                console.log(`Processing order detail ${detail.ordtId}:`, {
                    prodTypeBranchId,
                    quantityToReturn,
                });

                if (!prodTypeBranchId || !quantityToReturn || quantityToReturn <= 0) {
                    console.warn(`Invalid prodTypeBranchId or quantity for order detail ${detail.ordtId}`);
                    continue;
                }

                // Lấy số lượng hiện tại của sản phẩm tại chi nhánh
                const response = await axios.get(`${API_URL_PRODUCT_TYPE_BRANCH}/${prodTypeBranchId}`, {
                    withCredentials: true,
                });

                console.log(`Response from fetching quantity for prodTypeBranchId ${prodTypeBranchId}:`, response.data);

                if (response.data.success && response.data.content) {
                    const currentQuantity = response.data.content.quantity || 0;
                    const newQuantity = currentQuantity + quantityToReturn;

                    console.log(`Current quantity: ${currentQuantity}, Quantity to return: ${quantityToReturn}, New quantity: ${newQuantity}`);

                    // Cập nhật số lượng mới
                    const updateResponse = await axios.put(
                        `${API_URL_PRODUCT_TYPE_BRANCH}/update`,
                        { prodTypeBranchId, quantity: newQuantity },
                        { withCredentials: true }
                    );

                    console.log(`Update response for prodTypeBranchId ${prodTypeBranchId}:`, updateResponse.data);

                    if (!updateResponse.data.success) {
                        throw new Error(`Failed to update quantity for prodTypeBranchId ${prodTypeBranchId}`);
                    }
                } else {
                    console.warn(`Failed to fetch current quantity for prodTypeBranchId ${prodTypeBranchId}`);
                    throw new Error(`Unable to fetch current quantity for prodTypeBranchId ${prodTypeBranchId}`);
                }
            }
        } catch (error) {
            console.error("Error updating product type branch quantity:", error);
            throw error; // Ném lỗi để rollback trạng thái đơn hàng nếu cần
        }
    };

    const handleCancelOrder = async (orderId) => {
        try {
            // Tìm đơn hàng để lấy orderDetails và kiểm tra trạng thái
            const orderToCancel = orders.find(order => order.orderId === orderId);
            if (!orderToCancel) {
                throw new Error("Order not found");
            }

            // Kiểm tra nếu đơn hàng đã bị hủy trước đó
            if (orderToCancel.status === STATUS_NUMBER_MAPPING["Canceled"]) {
                showNotification("Order has already been canceled.", 3000, "fail");
                return;
            }

            // Lưu trạng thái ban đầu để rollback nếu cần
            const originalStatus = orderToCancel.status;

            // Cập nhật trạng thái đơn hàng thành "Canceled"
            const orderRequest = { status: STATUS_NUMBER_MAPPING["Canceled"] };
            const response = await axios.put(
                `${API_URL_ORDERS}/update?orderId=${orderId}`,
                orderRequest,
                { withCredentials: true }
            );

            if (!response.data.success) {
                throw new Error("Failed to update order status to Canceled");
            }

            const updatedOrder = response.data.content;

            // Cập nhật số lượng tồn kho
            if (orderToCancel.orderDetails && orderToCancel.orderDetails.length > 0) {
                try {
                    await updateProductTypeBranchQuantity(orderToCancel.orderDetails);
                } catch (error) {
                    // Rollback trạng thái đơn hàng nếu cập nhật số lượng thất bại
                    await axios.put(
                        `${API_URL_ORDERS}/update?orderId=${orderId}`,
                        { status: originalStatus },
                        { withCredentials: true }
                    );
                    showNotification("Failed to update inventory quantity. Order status reverted.", 3000, "fail");
                    return;
                }
            } else {
                console.warn(`No order details found for order ${orderId}`);
            }

            // Cập nhật trạng thái đơn hàng trong state
            setOrders((prev) =>
                prev.map((order) =>
                    order.orderId === orderId ? { ...order, status: updatedOrder.status } : order
                )
            );
            setFilteredOrders((prev) =>
                prev.map((order) =>
                    order.orderId === orderId ? { ...order, status: updatedOrder.status } : order
                )
            );

            showNotification("Order canceled successfully. Inventory updated.", 3000, "complete");
        } catch (error) {
            console.error("Error canceling order:", error);
            showNotification("Unable to cancel order. Please try again.", 3000, "fail");
        }
    };

    const handleCompleteOrder = async (orderId) => {
        try {
            const orderRequest = { status: STATUS_NUMBER_MAPPING["Received"] };
            const response = await axios.put(
                `${API_URL_ORDERS}/update?orderId=${orderId}`,
                orderRequest,
                { withCredentials: true }
            );

            const updatedOrder = response.data.content;
            setOrders((prev) =>
                prev.map((order) =>
                    order.orderId === orderId ? { ...order, status: updatedOrder.status } : order
                )
            );
            setFilteredOrders((prev) =>
                prev.map((order) =>
                    order.orderId === orderId ? { ...order, status: updatedOrder.status } : order
                )
            );

            showNotification("Order marked as received successfully.", 3000, "complete");
        } catch (error) {
            console.error("Error completing order:", error);
            showNotification("Unable to complete order. Please try again.", 3000, "fail");
        }
    };

    useEffect(() => {
        const loadData = async () => {
            setLoading(true);

            const user = await fetchUserInfo();
            if (user) {
                setUserInfo({
                    userId: user.userId || null,
                    firstName: user.firstName || "User",
                    lastName: user.lastName || "Name",
                    phone: user.phone || "N/A",
                });

                const ordersData = await fetchOrders(user.userId, searchKeyword);
                setOrders(ordersData.orders);

                const userPointsValue = user.userPoint || 0;
                setUserPoints(userPointsValue);

                const memberships = await fetchMemberships();
                const sortedMemberships = memberships.sort((a, b) => a.mbsPoint - b.mbsPoint);
                let currentLevel = { mbsName: "UNRANK", mbsPoint: 0 };

                for (let i = 0; i < sortedMemberships.length; i++) {
                    if (userPointsValue >= sortedMemberships[i].mbsPoint) {
                        currentLevel = sortedMemberships[i];
                    } else {
                        break;
                    }
                }

                setMembershipLevel(currentLevel.mbsName);

                const filtered = filterByStatus(ordersData.orders, selectedTab);
                const newTotalPages = Math.ceil(filtered.length / pageSize);
                setTotalPages(newTotalPages);

                if (currentPage >= newTotalPages && newTotalPages > 0) {
                    setCurrentPage(newTotalPages - 1);
                } else if (newTotalPages === 0) {
                    setCurrentPage(0);
                }

                const startIndex = currentPage * pageSize;
                const paginatedOrders = filtered.slice(startIndex, startIndex + pageSize);
                setFilteredOrders(paginatedOrders);
            } else {
                setUserInfo({ firstName: "User", lastName: "Name", phone: "N/A" });
                setOrders([]);
                setFilteredOrders([]);
                setTotalPages(0);
                setUserPoints(0);
            }

            setLoading(false);
        };

        loadData();
    }, [currentPage, pageSize, selectedTab, searchKeyword]);

    const handleViewDetails = (orderId) => {
        setOrders(orders.map(order =>
            order.orderId === orderId ? { ...order, showDetails: !order.showDetails } : order
        ));
        setFilteredOrders(filteredOrders.map(order =>
            order.orderId === orderId ? { ...order, showDetails: !order.showDetails } : order
        ));
    };

    const orderTabs = [
        { key: "all", label: "All" },
        { key: "pending", label: "Pending" },
        { key: "confirmed", label: "Confirmed" },
        { key: "shipping", label: "Shipping" },
        { key: "delivered", label: "Delivered" },
        { key: "canceled", label: "Canceled" },
        { key: "Received", label: "Received" },
    ];

    const getFullName = () => {
        return `${userInfo.firstName} ${userInfo.lastName}`.trim() || "User Name";
    };

    const handlePageSizeChange = (event) => {
        const newSize = parseInt(event.target.value);
        setPageSize(newSize);
        setCurrentPage(0);
    };

    const handlePreviousPage = () => {
        if (currentPage > 0) setCurrentPage(currentPage - 1);
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) setCurrentPage(currentPage + 1);
    };

    const renderPagination = () => {
        if (totalPages === 0) return <p className="text-gray-500">No orders available</p>;

        const maxButtons = 5;
        const halfButtons = Math.floor(maxButtons / 2);
        let startPage = Math.max(0, currentPage - halfButtons);
        let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

        if (endPage - startPage + 1 < maxButtons) {
            startPage = Math.max(0, endPage - maxButtons + 1);
        }

        const pageButtons = [];

        pageButtons.push(
            <Button
                key="first"
                variant="outline"
                onClick={() => setCurrentPage(0)}
                disabled={currentPage === 0 || loading}
                className="text-sm px-3 py-1"
            >
                First
            </Button>
        );

        pageButtons.push(
            <Button
                key="prev"
                variant="outline"
                onClick={handlePreviousPage}
                disabled={currentPage === 0 || loading}
                className="text-sm px-3 py-1"
            >
                Previous
            </Button>
        );

        if (startPage > 0) {
            pageButtons.push(
                <Button key="start-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>
                    ...
                </Button>
            );
        }

        for (let i = startPage; i <= endPage; i++) {
            pageButtons.push(
                <Button
                    key={i}
                    variant={currentPage === i ? "default" : "outline"}
                    className={currentPage === i ? "bg-blue-500 text-white" : ""}
                    onClick={() => setCurrentPage(i)}
                    disabled={loading}
                >
                    {i + 1}
                </Button>
            );
        }

        if (endPage < totalPages - 1) {
            pageButtons.push(
                <Button key="end-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>
                    ...
                </Button>
            );
        }

        pageButtons.push(
            <Button
                key="next"
                variant="outline"
                onClick={handleNextPage}
                disabled={currentPage >= totalPages - 1 || loading}
                className="text-sm px-3 py-1"
            >
                Next
            </Button>
        );

        pageButtons.push(
            <Button
                key="last"
                variant="outline"
                onClick={() => setCurrentPage(totalPages - 1)}
                disabled={currentPage >= totalPages - 1 || loading}
                className="text-sm px-3 py-1"
            >
                Last
            </Button>
        );

        return pageButtons;
    };

    const formatPrice = (price) => {
        const formattedPrice = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'VND',
            currencyDisplay: 'code',
        }).format(Math.floor(price));
        return formattedPrice.replace('VND', 'VND');
    };

    return (
        <div className="flex flex-col min-h-screen bg-gray-100" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="h-20 bg-red-600 text-white flex items-center px-4 shadow-md w-full">
                <Header />
            </header>

            <div className="flex flex-1 container mx-auto p-6 space-x-6">
                <Sidebar />

                <div className="flex-1 bg-white shadow-lg rounded-lg p-6">
                    <div className="flex items-center space-x-4">
                        <img
                            src="/logouser.png"
                            alt="Avatar"
                            className="w-16 h-16 rounded-full border-2 border-gray-300"
                        />
                        <div className="flex flex-col">
                            {loading ? (
                                <>
                                    <div className="w-32 h-6 bg-gray-200 rounded animate-pulse mb-2" />
                                    <div className="w-24 h-4 bg-gray-200 rounded animate-pulse" />
                                </>
                            ) : (
                                <>
                                    <div className="flex items-center space-x-2">
                                        <h2 className="text-xl font-semibold text-[#8c1d6b] text-center">
                                            {getFullName()}
                                        </h2>
                                    </div>
                                    <p className="text-gray-600 text-center">{userInfo.phone || "N/A"}</p>
                                </>
                            )}
                            <span className="text-[#8c1d6b] border border-[#8c1d6b] text-sm px-3 py-1 rounded-full text-center mt-2">
                                {loading ? "Loading..." : membershipLevel}
                            </span>
                        </div>
                    </div>

                    <div className="mt-4 flex justify-between bg-white p-6 rounded-lg shadow border">
                        <div className="flex-1 text-center border-r">
                            <p className="text-3xl font-bold">{orders.length}</p>
                            <p className="text-gray-500">Orders</p>
                        </div>
                        <div className="flex-1 text-center border-r">
                            <p className="text-3xl font-bold">
                                {formatPrice(
                                    orders
                                        .filter(order => order.status !== STATUS_NUMBER_MAPPING["Canceled"])
                                        .reduce((sum, order) =>
                                            sum + (order.orderDetails?.reduce((total, detail) =>
                                                total + (detail.unitPrice * detail.quantity * (1 + detail.vatRate)), 0) || 0), 0)
                                )}
                            </p>
                            <p className="text-gray-500">Total accumulated amount</p>
                        </div>
                    </div>

                    <div className="mt-4 flex justify-between items-center">
                        <div className="flex gap-2">
                            {orderTabs.map((tab) => (
                                <button
                                    key={tab.key}
                                    onClick={() => setSelectedTab(tab.key)}
                                    className={`px-4 py-2 rounded-lg ${selectedTab === tab.key ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-700"} transition-colors duration-200 hover:bg-blue-400 hover:text-white`}
                                >
                                    {tab.label}
                                </button>
                            ))}
                        </div>
                        <Input
                            type="text"
                            placeholder="Search orders..."
                            value={searchKeyword}
                            onChange={(e) => {
                                setSearchKeyword(e.target.value);
                                setCurrentPage(0);
                            }}
                            className="w-84 border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                        />
                    </div>

                    {searchError && (
                        <div className="mt-4 text-red-500 bg-red-50 p-3 rounded-lg">
                            {searchError}
                        </div>
                    )}

                    <div className="mt-6">
                        {loading ? (
                            <div className="text-center">
                                <p className="text-gray-500">Loading orders...</p>
                            </div>
                        ) : filteredOrders.length === 0 ? (
                            <div className="flex flex-col items-center">
                                <img
                                    src="https://static.vecteezy.com/system/resources/thumbnails/014/814/311/small/no-order-a-flat-rounded-icon-is-up-for-premium-use-vector.jpg"
                                    alt="No Orders"
                                    className="w-60 h-60"
                                />
                                <p className="text-gray-500 mt-4">
                                    {selectedTab === "all" ? "No orders found." : `No orders found with status "${selectedTab.charAt(0).toUpperCase() + selectedTab.slice(1)}".`}
                                </p>
                            </div>
                        ) : (
                            <>
                                <table className="w-full border-collapse border border-gray-300">
                                    <thead>
                                        <tr className="bg-blue-500">
                                            <th className="border p-2 text-white font-bold">Order ID</th>
                                            <th className="border p-2 text-white font-bold">Branch</th>
                                            <th className="border p-2 text-white font-bold">Payment Method</th>
                                            <th className="border p-2 text-white font-bold">Created At</th>
                                            <th className="border p-2 text-white font-bold">Address</th>
                                            <th className="border p-2 text-white font-bold">Total Price</th>
                                            <th className="border p-2 text-white font-bold">Status</th>
                                            <th className="border p-2 text-white font-bold">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filteredOrders.map((order) => (
                                            <>
                                                <tr key={order.orderId} className="text-center hover:bg-gray-50">
                                                    <td className="border p-2">{order.orderId}</td>
                                                    <td className="border p-2">{order.brchId?.brchName || "N/A"}</td>
                                                    <td className="border p-2">{order.paymentMethod || "N/A"}</td>
                                                    <td className="border p-2">{order.createdAt || "N/A"}</td>
                                                    <td className="border p-2">{order.address || "N/A"}</td>
                                                    <td className="border p-2 font-bold">
                                                        {order.orderDetails
                                                            ? formatPrice(order.orderDetails.reduce(
                                                                (sum, detail) => sum + (detail.unitPrice * detail.quantity * (1 + detail.vatRate / 100)),
                                                                0
                                                            ))
                                                            : "N/A"}
                                                    </td>
                                                    <td className="border p-2">{STATUS_TEXT_MAPPING[order.status] || "Pending"}</td>
                                                    <td className="border p-2">
                                                        <div className="flex items-center justify-center gap-2">
                                                            <Button
                                                                variant="outline"
                                                                size="icon"
                                                                onClick={() => handleViewDetails(order.orderId)}
                                                                className="text-blue-600 hover:text-blue-800 border-blue-600 hover:bg-blue-50 w-8 h-8"
                                                                title="View Details"
                                                            >
                                                                <Eye className="h-4 w-4" />
                                                            </Button>
                                                            {(order.status === 0 || order.status === 1) && (
                                                                <Button
                                                                    onClick={() => handleCancelOrder(order.orderId)}
                                                                    className="bg-red-100 text-red-600 hover:bg-red-200 hover:text-red-700 border border-red-300 rounded-lg px-3 py-1 text-sm transition-colors duration-200"
                                                                    title="Cancel Order"
                                                                >
                                                                    Cancel
                                                                </Button>
                                                            )}
                                                            {order.status === 3 && (
                                                                <Button
                                                                    onClick={() => handleCompleteOrder(order.orderId)}
                                                                    className="bg-green-100 text-green-600 hover:bg-green-200 hover:text-green-700 border border-green-300 rounded-lg px-3 py-1 text-sm transition-colors duration-200"
                                                                    title="Mark as Completed"
                                                                >
                                                                    Received
                                                                </Button>
                                                            )}
                                                        </div>
                                                    </td>
                                                </tr>
                                                {order.showDetails && order.orderDetails && (
                                                    <tr>
                                                        <td colSpan="8" className="border p-2">
                                                            <div className="bg-gray-100 p-4 rounded-lg shadow-sm">
                                                                <h4 className="font-bold mb-2 text-gray-700">Order Detail:</h4>
                                                                <table className="w-full border-collapse border border-gray-200">
                                                                    <thead>
                                                                        <tr className="bg-gray-200">
                                                                            <th className="border p-2 text-gray-700">Order Detail ID</th>
                                                                            <th className="border p-2 text-gray-700">Product Type</th>
                                                                            <th className="border p-2 text-gray-700">Variation</th>
                                                                            <th className="border p-2 text-gray-700">Quantity</th>
                                                                            <th className="border p-2 text-gray-700">Unit Price</th>
                                                                            <th className="border p-2 text-gray-700">VAT Rate</th>
                                                                            <th className="border p-2 text-gray-700">Total Price</th>
                                                                            {order.status === 5 && <th className="border p-2 text-gray-700">Review</th>}
                                                                        </tr>
                                                                    </thead>
                                                                    <tbody>
                                                                        {order.orderDetails.map((detail) => (
                                                                            <tr key={detail.ordtId} className="text-center">
                                                                                <td className="border p-2">{detail.ordtId}</td>
                                                                                <td className="border p-2">
                                                                                    {detail.prodTypeBranchId?.prodTypeId?.prodTypeName || "N/A"}
                                                                                </td>
                                                                                <td className="border p-2">
                                                                                    {detail.variation ? (
                                                                                        <ul className="list-disc list-inside space-y-1">
                                                                                            {detail.variation.split(", ").map((attr, index) => {
                                                                                                const [key, value] = attr.split(": ");
                                                                                                return (
                                                                                                    <li key={index} className="text-gray-700">
                                                                                                        <span className="font-medium">{key}:</span>{" "}
                                                                                                        <span className="text-gray-600">{value}</span>
                                                                                                    </li>
                                                                                                );
                                                                                            })}
                                                                                        </ul>
                                                                                    ) : (
                                                                                        "N/A"
                                                                                    )}
                                                                                </td>
                                                                                <td className="border p-2">{detail.quantity || "N/A"}</td>
                                                                                <td className="border p-2">{detail.unitPrice ? formatPrice(detail.unitPrice) : "N/A"}</td>
                                                                                <td className="border p-2">
                                                                                    {detail.vatRate}% ({formatPrice(((detail.unitPrice) * (1 + detail.vatRate) - (detail.unitPrice)) / 100) || "N/A"})
                                                                                </td>                                                                                <td className="border p-2 font-extrabold">
                                                                                    {formatPrice((detail.unitPrice * detail.quantity + (((detail.unitPrice) * (1 + detail.vatRate) - (detail.unitPrice)) / 100))) || "N/A"}
                                                                                </td>
                                                                                {order.status === 5 && (
                                                                                    <td className="border p-2">
                                                                                        <button
                                                                                            className="bg-yellow-200 text-yellow-700 px-6 py-2 rounded-lg shadow-lg hover:bg-yellow-300 border border-yellow-300 active:scale-95 active:shadow-inner transition-all duration-200 text-sm"
                                                                                            onClick={() =>
                                                                                                navigate(`/product/${detail.prodTypeBranchId?.prodTypeId?.prodTypeId}`, {
                                                                                                    state: { isReviewNow: true },
                                                                                                })
                                                                                            }
                                                                                        >
                                                                                            Go to review
                                                                                        </button>
                                                                                    </td>
                                                                                )}
                                                                            </tr>
                                                                        ))}
                                                                    </tbody>
                                                                </table>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                )}
                                            </>
                                        ))}
                                    </tbody>
                                </table>
                                <div className="flex justify-between items-center mt-4">
                                    <div className="flex items-center gap-2 whitespace-nowrap">
                                        <label htmlFor="pageSize" className="text-sm">
                                            Items per page:
                                        </label>
                                        <select
                                            id="pageSize"
                                            value={pageSize}
                                            onChange={handlePageSizeChange}
                                            className="border p-1 rounded focus:ring-blue-500 focus:border-blue-500"
                                            disabled={loading}
                                        >
                                            <option value={5}>5</option>
                                            <option value={10}>10</option>
                                            <option value={15}>15</option>
                                            <option value={20}>20</option>
                                        </select>
                                    </div>
                                    <div className="flex justify-center items-center gap-2">
                                        {renderPagination()}
                                    </div>
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OrderHistoryPage;