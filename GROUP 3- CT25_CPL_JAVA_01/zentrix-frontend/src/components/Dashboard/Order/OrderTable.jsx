import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Eye } from "lucide-react";
import axios from "axios";
import ProductModal from "./ProductModal";
import UserModal from "./UserModal";
import { Input } from "@/components/ui/input";

const OrderTable = () => {
    const [exportStocks, setExportStocks] = useState([]);
    const [filteredStocks, setFilteredStocks] = useState([]);
    const [exportLoading, setExportLoading] = useState(true);
    const [exportError, setExportError] = useState(null);
    const [selectedStatusFilter, setSelectedStatusFilter] = useState("ALL");
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchKeyword, setSearchKeyword] = useState("");

    const [isProductModalOpen, setIsProductModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [imagePreviews, setImagePreviews] = useState([]);

    const [isUserModalOpen, setIsUserModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);

    const API_URL = "http://localhost:6789/api/v1/orders";

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

    const STATUS_FLOW = {
        0: [1, 4], // Pending -> Confirmed, Canceled
        1: [2, 4], // Confirmed -> Shipping, Canceled
        2: [3, 4], // Shipping -> Delivered, Canceled
        3: [],     // Delivered -> Completed (không cho phép Canceled)
        4: [],     // Canceled -> Không thể thay đổi
        5: [],     // Completed -> Không thể thay đổi
    };

    const getValidNextStatuses = (currentStatus) => {
        const validStatuses = STATUS_FLOW[currentStatus] || [];
        return validStatuses.map(status => STATUS_TEXT_MAPPING[status]);
    };

    const fetchExportStocks = async (page, size, keyword = "") => {
        try {
            setExportLoading(true);
            setExportError(null);
            setExportStocks([]);
            setFilteredStocks([]);

            let url = keyword
                ? `${API_URL}/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`
                : `${API_URL}?page=${page}&size=${size}`;

            console.log("Fetching orders with URL:", url);

            const response = await axios.get(url, { withCredentials: true });

            console.log("API Response:", response.data);

            if (!response.data || !Array.isArray(response.data.content)) {
                throw new Error("Invalid data format from API: 'content' is missing or not an array");
            }

            const orders = response.data.content.map(order => ({
                ...order,
                status: typeof order.status === "number" ? order.status : 0,
                createdAt: order.createdAt
                    ? new Date(order.createdAt).toLocaleDateString("en-GB", {
                        day: "numeric",
                        month: "numeric",
                        year: "numeric",
                    })
                    : "N/A",
                showDetails: false,
            }));

            console.log("Processed orders:", orders);

            setExportStocks(orders);
            return orders;
        } catch (err) {
            console.error("Error fetching orders:", err);
            const errorMessage = err.response
                ? `API Error: ${err.response.status} - ${err.response.data?.message || err.message}. Please check your access permissions.`
                : `Network Error: ${err.message}. Please ensure the backend server is running on ${API_URL}.`;
            setExportError(errorMessage);
            setExportStocks([]);
            setFilteredStocks([]);
            setTotalPages(0);
            return [];
        } finally {
            setExportLoading(false);
        }
    };

    const filterByStatus = (orders, statusFilter) => {
        if (statusFilter === "ALL") {
            return orders;
        }

        const statusNumber = STATUS_NUMBER_MAPPING[statusFilter];
        const filtered = orders.filter(order => order.status === statusNumber);
        console.log(`Filtered orders for status ${statusFilter} (${statusNumber}):`, filtered);
        return filtered;
    };

    const loadAndFilterOrders = async () => {
        const orders = await fetchExportStocks(0, 1000, searchKeyword);
        const filteredOrders = filterByStatus(orders, selectedStatusFilter);

        const newTotalPages = Math.ceil(filteredOrders.length / pageSize);
        setTotalPages(newTotalPages);

        if (currentPage >= newTotalPages && newTotalPages > 0) {
            setCurrentPage(newTotalPages - 1);
        } else if (newTotalPages === 0) {
            setCurrentPage(0);
        }

        const startIndex = currentPage * pageSize;
        const paginatedOrders = filteredOrders.slice(startIndex, startIndex + pageSize);
        setFilteredStocks(paginatedOrders);
    };

    useEffect(() => {
        loadAndFilterOrders();
    }, [currentPage, pageSize, searchKeyword, selectedStatusFilter]);

    const handleStatusFilter = (status) => {
        setSelectedStatusFilter(status);
        setCurrentPage(0);
    };

    const handleUpdateStatus = async (orderId, newStatusText) => {
        try {
            const newStatusNumber = STATUS_NUMBER_MAPPING[newStatusText];
            const orderRequest = { status: newStatusNumber };
            const response = await axios.put(
                `${API_URL}/update?orderId=${orderId}`,
                orderRequest,
                { withCredentials: true }
            );

            const updatedOrder = response.data.content;
            setExportStocks((prev) =>
                prev.map((order) =>
                    order.orderId === orderId ? { ...order, status: updatedOrder.status } : order
                )
            );
            setFilteredStocks((prev) =>
                prev.map((order) =>
                    order.orderId === orderId ? { ...order, status: updatedOrder.status } : order
                )
            );
            if (newStatusNumber === 3) {
                const order = exportStocks.find((o) => o.orderId === orderId);
                const userName = (order.userId?.firstName || "") + " " + (order.userId?.lastName || "");
                const title = `Order #${order.orderId} Delivered - Please confirm received`;
                const description = `The order #${order.orderId} for ${userName} has been delivered successfully.`;
                await createNotification(title, description, order.orderId);
            }
        } catch (error) {
            console.error("Error updating order status:", error);
            alert("Unable to update order status. Please try again.");
        }
    };

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success) {
                return response.data.content.userId;
            } else {
                return null;
            }
        } catch (error) {
            console.error("Error fetching user info:", error.response?.data || error.message);
            return null;
        }
    };

    const createNotification = async (title, description, orderId) => {
        const userId = await fetchUserInfo();
        try {
            const response = await fetch("http://localhost:6789/api/v1/dashboard/notifications/add", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body: JSON.stringify({
                    title,
                    description,
                    status: 1,
                    createdAt: new Date().toISOString(),
                    createdById: userId,
                    orderId,
                }),
                credentials: "include",
            });

            if (response.data) {
                console.log("Notification created successfully:", response.data.content);
            } else {
                console.error("Failed to create notification:", response.data.message);
            }
        } catch (error) {
            console.error("Error creating notification:", error);
        }
    };
    const formatPrice = (price) => {
        const formattedPrice = new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            currencyDisplay: 'code',
        }).format(Math.floor(price));
        return formattedPrice.replace('VND', 'VNĐ');
    };
    const fetchImageProductType = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, { withCredentials: true });
            let images = Array.isArray(response.data)
                ? response.data.map(imageProductType => ({
                    imageProdId: imageProductType.imageProdId,
                    url: imageProductType.imageId?.imageLink || imageProductType.imageLink || imageProductType,
                }))
                : [{
                    imageProdId: response.data.imageProdId,
                    url: response.data.imageId?.imageLink || response.data.imageLink || response.data,
                }];

            setImagePreviews(images.map(image => image.url));
            return images;
        } catch (error) {
            console.error("Error fetching product images:", error);
            setImagePreviews([]);
            return [];
        }
    };

    const handleExportViewDetails = (orderId) => {
        setExportStocks(exportStocks.map(order =>
            order.orderId === orderId ? { ...order, showDetails: !order.showDetails } : order
        ));
        setFilteredStocks(filteredStocks.map(order =>
            order.orderId === orderId ? { ...order, showDetails: !order.showDetails } : order
        ));
    };

    const handleProductClick = (productTypeBranch) => {
        setSelectedProduct(productTypeBranch);
        fetchImageProductType(productTypeBranch?.prodTypeId?.prodTypeId);
        setIsProductModalOpen(true);
    };

    const handleCloseProductModal = () => {
        setIsProductModalOpen(false);
        setSelectedProduct(null);
        setImagePreviews([]);
    };

    const handleUserClick = (user) => {
        setSelectedUser(user);
        setIsUserModalOpen(true);
    };

    const handleCloseUserModal = () => {
        setIsUserModalOpen(false);
        setSelectedUser(null);
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

    const paddedOrders = () => {
        const rows = [...filteredStocks];
        while (rows.length < pageSize) rows.push(null);
        return rows;
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
                disabled={currentPage === 0 || exportLoading}
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
                disabled={currentPage === 0 || exportLoading}
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
                    disabled={exportLoading}
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
                disabled={currentPage >= totalPages - 1 || exportLoading}
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
                disabled={currentPage >= totalPages - 1 || exportLoading}
                className="text-sm px-3 py-1"
            >
                Last
            </Button>
        );

        return pageButtons;
    };

    if (exportError) return <div className="text-red-500">{exportError}</div>;

    return (
        <div className="bg-white w-full p-4 shadow-md rounded-lg overflow-x-auto animate-neonTable">
            <div className="mb-4 flex justify-between items-center">
                <div className="flex space-x-2">
                    {["ALL", "Pending", "Confirmed", "Shipping", "Delivered", "Canceled", "Received"].map((status) => (
                        <Button
                            key={status}
                            onClick={() => handleStatusFilter(status)}
                            className={`${selectedStatusFilter === status
                                ? "bg-blue-500 text-white"
                                : "bg-gray-200 text-gray-700"
                                } hover:bg-blue-600 hover:text-white transition`}
                        >
                            {status}
                        </Button>
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
                    className="w-84"
                />
            </div>

            <div>
                <h2 className="text-xl font-bold mb-4">Orders</h2>
                <div className="overflow-y-auto min-h-[528px]">
                    <table className="w-full min-w-full border-collapse border border-gray-300">
                        <thead>
                            <tr className="bg-blue-500">
                                <th className="border p-2 text-white font-bold w-15">Order ID</th>
                                <th className="border p-2 text-white font-bold w-30">Export Branch</th>
                                <th className="border p-2 text-white font-bold w-30">User Name</th>
                                <th className="border p-2 text-white font-bold w-30">Payment Method</th>
                                <th className="border p-2 text-white font-bold w-20">Created Date</th>
                                <th className="border p-2 text-white font-bold w-40">Address</th>
                                <th className="border p-2 text-white font-bold w-20">Total Price</th>
                                <th className="border p-2 text-white font-bold w-20">Status</th>
                                <th className="border p-2 text-white font-bold w-15">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {exportLoading ? (
                                Array(pageSize)
                                    .fill(null)
                                    .map((_, index) => (
                                        <tr key={index} style={{ height: "53px" }}>
                                            <td className="border p-2 w-15"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-30"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-30"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-30"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-20"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-40"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-20"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-20"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                            <td className="border p-2 w-15"><div className="h-4 bg-gray-200 rounded animate-pulse" /></td>
                                        </tr>
                                    ))
                            ) : paddedOrders().map((order, index) =>
                                order ? (
                                    <>
                                        <tr key={order.orderId} className="text-center" style={{ height: "53px" }}>
                                            <td className="border p-2 text-zinc-950">{order.orderId}</td>
                                            <td className="border p-2 text-zinc-950">{order.brchId?.brchName || "N/A"}</td>
                                            <td
                                                className="border p-2 text-blue-600 hover:text-blue-800 cursor-pointer underline text-zinc-950"
                                                onClick={() => handleUserClick(order.userId)}
                                            >
                                                {(order.userId?.firstName || "") + " " + (order.userId?.lastName || "") || "N/A"}
                                            </td>
                                            <td className="border p-2 text-zinc-950">{order.paymentMethod || "N/A"}</td>
                                            <td className="border p-2 text-zinc-950">{order.createdAt}</td>
                                            <td className="border p-2 text-zinc-950">{order.address}</td>
                                            <td className="border p-2 font-bold text-zinc-950">
                                                {order.orderDetails
                                                    ? order.orderDetails.reduce(
                                                        (sum, detail) => sum + (detail.unitPrice * detail.quantity * (1 + detail.vatRate / 100) || 0),
                                                        0
                                                    ).toLocaleString()
                                                    : "N/A"} VNĐ
                                            </td>
                                            <td className="border p-2 text-zinc-950">
                                                <select
                                                    value={STATUS_TEXT_MAPPING[order.status] || "Pending"}
                                                    onChange={(e) => handleUpdateStatus(order.orderId, e.target.value)}
                                                    className="border p-1 rounded"
                                                    disabled={order.status === 4 || order.status === 5 || order.status === 3}
                                                >
                                                    <option value={STATUS_TEXT_MAPPING[order.status]}>
                                                        {STATUS_TEXT_MAPPING[order.status] || "Pending"}
                                                    </option>
                                                    {getValidNextStatuses(order.status).map((status) => (
                                                        <option key={status} value={status}>
                                                            {status}
                                                        </option>
                                                    ))}
                                                </select>
                                            </td>
                                            <td className="border p-2">
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleExportViewDetails(order.orderId)}
                                                    className="text-blue-600 hover:text-blue-800"
                                                >
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                            </td>
                                        </tr>
                                        {order.showDetails && order.orderDetails && (
                                            <tr>
                                                <td colSpan="9" className="border p-2">
                                                    <div className="bg-gray-100 p-4 rounded">
                                                        <h4 className="font-bold mb-2">Order Detail:</h4>
                                                        <table className="w-full border-collapse border border-gray-200">
                                                            <thead>
                                                                <tr className="bg-gray-200">
                                                                    <th className="border p-2">Order Detail ID</th>
                                                                    <th className="border p-2">Excluding VAT</th>
                                                                    <th className="border p-2">VAT Rate</th>
                                                                    <th className="border p-2">Product Type</th>
                                                                    <th className="border p-2">Quantity</th>
                                                                    <th className="border p-2">Unit Price</th>
                                                                    <th className="border p-2">Total Price</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                                {order.orderDetails.map((detail) => (
                                                                    <tr key={detail.ordtId} className="text-center">
                                                                        <td className="border p-2">{detail.ordtId}</td>
                                                                        <td className="border p-2">{detail.amountNotVat?.toLocaleString() || "N/A"}</td>
                                                                        <td className="border p-2">{detail.vatRate}</td>
                                                                        <td
                                                                            className="border p-2 text-blue-600 hover:text-blue-800 cursor-pointer underline"
                                                                            onClick={() => handleProductClick(detail.prodTypeBranchId)}
                                                                        >
                                                                            {detail.prodTypeBranchId?.prodTypeId?.prodTypeName || "N/A"}
                                                                        </td>
                                                                        <td className="border p-2">{detail.quantity || "N/A"}</td>
                                                                        <td className="border p-2">{detail.unitPrice?.toLocaleString() || "N/A"} VND</td>
                                                                        <td className="border p-2 font-extrabold">
                                                                            {formatPrice(detail.amountNotVat + (((detail.unitPrice) * (1 + detail.vatRate) - (detail.unitPrice)) / 100)) || "N/A"} VND
                                                                        </td>
                                                                    </tr>
                                                                ))}
                                                            </tbody>
                                                        </table>
                                                    </div>
                                                </td>
                                            </tr>
                                        )}
                                    </>
                                ) : (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2" colSpan="9"></td>
                                    </tr>
                                )
                            )}
                        </tbody>
                    </table>
                </div>

                <div className="flex justify-end items-center mt-4">
                    <div className="flex items-center gap-2 whitespace-nowrap">
                        <label htmlFor="pageSize" className="text-sm">Items per page:</label>
                        <select
                            id="pageSize"
                            value={pageSize}
                            onChange={handlePageSizeChange}
                            className="border p-1 rounded"
                            disabled={exportLoading}
                        >
                            <option value={5}>5</option>
                            <option value={10}>10</option>
                            <option value={15}>15</option>
                            <option value={20}>20</option>
                        </select>
                    </div>
                </div>
                <div className="flex justify-center items-center gap-2 mt-4">
                    {renderPagination()}
                </div>
            </div>

            <ProductModal
                isOpen={isProductModalOpen}
                onClose={handleCloseProductModal}
                selectedProduct={selectedProduct}
                imagePreviews={imagePreviews}
            />
            <UserModal
                isOpen={isUserModalOpen}
                onClose={handleCloseUserModal}
                selectedUser={selectedUser}
            />
        </div>
    );
};

export default OrderTable;