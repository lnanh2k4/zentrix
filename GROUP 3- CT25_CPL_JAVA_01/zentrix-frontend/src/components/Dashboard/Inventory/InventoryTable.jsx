import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Eye } from "lucide-react";
import axios from "axios";
import ProductModal from "./ProductModal";
import UserModal from "./UserModal";
import InvoiceModal from "./InvoiceModal";
import ImportStockModal from "./ImportStockModal";
import { useUserInfo, checkUserRole } from "@/services/InfoService";

const InventoryTable = () => {
    const [importStocks, setImportStocks] = useState([]);
    const [importLoading, setImportLoading] = useState(true);
    const [importError, setImportError] = useState(null);

    const [exportStocks, setExportStocks] = useState([]);
    const [exportLoading, setExportLoading] = useState(true);
    const [exportError, setExportError] = useState(null);

    const [isProductModalOpen, setIsProductModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [imagePreviews, setImagePreviews] = useState([]);

    const [isUserModalOpen, setIsUserModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);

    const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState(null);

    const [isImportStockModalOpen, setIsImportStockModalOpen] = useState(false);

    const { user } = useUserInfo();
    const role = checkUserRole(user);


    const handleOpenImportStockModal = () => {
        // fetchCurrentBranch(user.userId);
        setIsImportStockModalOpen(true);
    };

    const handleCloseImportStockModal = () => {
        setIsImportStockModalOpen(false);
    };

    const handleExportInvoice = (order) => {
        setSelectedOrder(order);
        setIsInvoiceModalOpen(true);
    };

    const handleCloseInvoiceModal = () => {
        setIsInvoiceModalOpen(false);
        setSelectedOrder(null);
    };

    useEffect(() => {
        const fetchImportStocks = async () => {
            try {
                const response = await axios.get("http://localhost:6789/api/v1/stocks", {
                    withCredentials: "true"
                });
                console.log("Import API Response:", response.data);
                setImportStocks(response.data.content.map(stock => ({ ...stock, showDetails: false })));
                setImportLoading(false);
            } catch (err) {
                setImportError("Failed to fetch import stocks");
                setImportLoading(false);
            }
        };

        fetchImportStocks();
    }, []);

    useEffect(() => {
        const fetchExportStocks = async () => {
            try {
                const response = await axios.get("http://localhost:6789/api/v1/orders?page=0&size=10000", {
                    withCredentials: "true"
                });
                console.log("Export API Response:", response.data);
                setExportStocks(response.data.content.map(order => ({ ...order, showDetails: false })));
                setExportLoading(false);
            } catch (err) {
                setExportError("Failed to fetch export stocks");
                setExportLoading(false);
            }
        };

        fetchExportStocks();
    }, []);

    const fetchImageProductType = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, {
                withCredentials: "true"
            });
            console.log("Image Response: ", response.data);
            if (!response.data) {
                setImagePreviews([]);
                return [];
            }

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
            console.log("Fetched images:", images);
            return images;
        } catch (error) {
            console.error("Error fetching product images:", error);
            setImagePreviews([]);
            return [];
        }
    };

    const handleImportViewDetails = (stockId) => {
        setImportStocks(importStocks.map(stock =>
            stock.stockId === stockId ? { ...stock, showDetails: !stock.showDetails } : stock
        ));
    };

    const handleExportViewDetails = (orderId) => {
        setExportStocks(exportStocks.map(order =>
            order.orderId === orderId ? { ...order, showDetails: !order.showDetails } : order
        ));
    };

    const handleProductClick = (productTypeBranch) => {
        console.log("productDetail selected: ", productTypeBranch);
        setSelectedProduct(productTypeBranch);
        fetchImageProductType(productTypeBranch.prodTypeId.prodTypeId);
        setIsProductModalOpen(true);
    };

    const handleCloseProductModal = () => {
        setIsProductModalOpen(false);
        setSelectedProduct(null);
        setImagePreviews([]);
    };

    const handleUserClick = (user) => {
        console.log("User selected: ", user);
        setSelectedUser(user);
        setIsUserModalOpen(true);
    };

    const handleCloseUserModal = () => {
        setIsUserModalOpen(false);
        setSelectedUser(null);
    };

    // Hàm tính tổng Total Price của StockDetails trong một Stock
    const calculateStockTotalPrice = (stockDetails) => {
        if (!stockDetails || stockDetails.length === 0) return 0;
        return stockDetails.reduce(
            (sum, detail) => sum + (detail.importPrice * detail.stockQuantity || 0),
            0
        );
    };
    const formatPrice = (price) => {
        const formattedPrice = new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            currencyDisplay: 'code',
        }).format(Math.floor(price));
        return formattedPrice.replace('VND', 'VNĐ');
    };

    const STATUS_TEXT_MAPPING = {
        0: "Pending",
        1: "Confirmed",
        2: "Shipping",
        3: "Delivered",
        4: "Canceled",
        5: "Received",
    };
    if (importLoading || exportLoading) return <div>Loading...</div>;
    if (importError) return <div>{importError}</div>;
    if (exportError) return <div>{exportError}</div>;

    return (
        <div className="bg-white p-3 rounded-lg shadow-lg animate-neonTable">
            {/* Bảng Import Stocks */}
            <div className="mb-8">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold">Import Stocks</h2>
                    <Button
                        onClick={handleOpenImportStockModal}
                        className="bg-green-500 text-white hover:bg-green-600"
                    >
                        Import Stock
                    </Button>
                </div>
                <table className="w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-blue-500">
                            <th className="border p-2 text-white font-bold">ID</th>
                            <th className="border p-2 text-white font-bold">Branch Imported</th>
                            <th className="border p-2 text-white font-bold">Supplier</th>
                            <th className="border p-2 text-white font-bold">Created By</th>
                            <th className="border p-2 text-white font-bold">Imported Date</th>
                            <th className="border p-2 text-white font-bold">Total Price</th> {/* Thêm cột Total Price */}
                            <th className="border p-2 text-white font-bold">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {importStocks.map((stock) => {
                            const totalStockPrice = calculateStockTotalPrice(stock.stockDetails); // Tính tổng Total Price
                            return (
                                <>
                                    <tr key={stock.stockId} className="text-center">
                                        <td className="border p-2">{stock.stockId}</td>
                                        <td className="border p-2">{stock.brchId?.brchName || "N/A"}</td>
                                        <td className="border p-2">{stock.supplierId?.suppName || "N/A"}</td>
                                        <td className="border p-2">{stock.createdBy?.userId?.username || "N/A"}</td>
                                        <td className="border p-2">{stock.importDate || "N/A"}</td>
                                        <td className="border p-2 font-bold">
                                            {totalStockPrice.toLocaleString() || "N/A"} VND {/* Hiển thị Total Price */}
                                        </td>
                                        <td className="border p-2">
                                            <Button
                                                variant="outline"
                                                size="icon"
                                                onClick={() => handleImportViewDetails(stock.stockId)}
                                                className="text-blue-600 hover:text-blue-800"
                                            >
                                                <Eye className="h-4 w-4" />
                                            </Button>
                                        </td>
                                    </tr>
                                    {stock.showDetails && stock.stockDetails && (
                                        <tr>
                                            <td colSpan="7" className="border p-2"> {/* Cập nhật colSpan thành 7 */}
                                                <div className="bg-gray-100 p-4 rounded">
                                                    <h4 className="font-bold mb-2">Stock Details:</h4>
                                                    <table className="w-full border-collapse border border-gray-200">
                                                        <thead>
                                                            <tr className="bg-gray-200">
                                                                <th className="border p-2">StockDetail ID</th>
                                                                <th className="border p-2">Product</th>
                                                                <th className="border p-2">Product Type</th>
                                                                <th className="border p-2">Quantity</th>
                                                                <th className="border p-2">Import Price</th>
                                                                <th className="border p-2">Total Price</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            {stock.stockDetails.map((detail) => (
                                                                <tr key={detail.stockDetailId} className="text-center">
                                                                    <td className="border p-2">{detail.stockDetailId}</td>
                                                                    <td className="border p-2 font-bold">
                                                                        {detail.prodTypeBrchId?.prodTypeId?.prodId?.prodName || "N/A"}
                                                                    </td>
                                                                    <td
                                                                        className="border p-2 text-blue-600 hover:text-blue-800 cursor-pointer underline"
                                                                        onClick={() => handleProductClick(detail.prodTypeBrchId)}
                                                                    >
                                                                        {detail.prodTypeBrchId?.prodTypeId?.prodTypeName || "N/A"}
                                                                    </td>
                                                                    <td className="border p-2">{detail.stockQuantity}</td>
                                                                    <td className="border p-2">{detail.importPrice?.toLocaleString() || "N/A"} VND</td>
                                                                    <td className="border p-2 font-bold">{(detail.importPrice * detail.stockQuantity)?.toLocaleString() || "N/A"} VND</td>
                                                                </tr>
                                                            ))}
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            {/* Bảng Export Stocks (Orders) */}
            <div>
                <h2 className="text-xl font-bold mb-4">Export Stocks</h2>
                <table className="w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-blue-500">
                            <th className="border p-2 text-white font-bold">Order ID</th>
                            <th className="border p-2 text-white font-bold">Branch Exported</th>
                            <th className="border p-2 text-white font-bold">Customer</th>
                            <th className="border p-2 text-white font-bold">Created At</th>
                            <th className="border p-2 text-white font-bold">Payment Method</th>
                            <th className="border p-2 text-white font-bold">Total Price</th>
                            <th className="border p-2 text-white font-bold">Export status</th>
                            <th className="border p-2 text-white font-bold">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {exportStocks
                            .filter((order) => [2, 3, 5].includes(order.status))
                            .map((order) => {
                                const totalOrderPrice = order.orderDetails
                                    ? order.orderDetails.reduce(
                                        (sum, detail) => sum + (detail.amountNotVat + (((detail.unitPrice) * (1 + detail.vatRate) - (detail.unitPrice)) / 100)),
                                        0
                                    )
                                    : 0;

                                return (
                                    <>
                                        <tr key={order.orderId} className="text-center">
                                            <td className="border p-2">{order.orderId}</td>
                                            <td className="border p-2">{order.brchId?.brchName || "N/A"}</td>
                                            <td
                                                className="border p-2 text-blue-600 hover:text-blue-800 cursor-pointer underline"
                                                onClick={() => handleUserClick(order.userId)}
                                            >
                                                {(order.userId?.firstName || "") + " " + (order.userId?.lastName || "") || "N/A"}
                                            </td>
                                            <td className="border p-2">{order.createdAt || "N/A"}</td>
                                            <td className="border p-2">{order.paymentMethod || "N/A"}</td>
                                            <td className="border p-2 font-bold">{totalOrderPrice.toLocaleString() || "N/A"} VND</td>
                                            <td className="border p-2 text-zinc-950"> {STATUS_TEXT_MAPPING[order.status]} </td>
                                            <td className="border p-2">
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleExportViewDetails(order.orderId)}
                                                    className="text-blue-600 hover:text-blue-800"
                                                >
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    onClick={() => handleExportInvoice(order)}
                                                    className="text-green-600 hover:text-green-800"
                                                >
                                                    Export
                                                </Button>
                                            </td>
                                        </tr>
                                        {order.showDetails && order.orderDetails && (
                                            <tr>
                                                <td colSpan="7" className="border p-2">
                                                    <div className="bg-gray-100 p-4 rounded">
                                                        <h4 className="font-bold mb-2">Order Details:</h4>
                                                        <table className="w-full border-collapse border border-gray-200">
                                                            <thead>
                                                                <tr className="bg-gray-200">
                                                                    <th className="border p-2">OrderDetail ID</th>
                                                                    <th className="border p-2">Not Vat</th>
                                                                    <th className="border p-2">Vat Rate</th>
                                                                    <th className="border p-2">Product Type</th>
                                                                    <th className="border p-2">Quantity</th>
                                                                    <th className="border p-2">Unit Price</th>
                                                                    <th className="border p-2">Total Price</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>

                                                                {order.orderDetails.map((detail) => (

                                                                    <tr key={detail.orderDetailId} className="text-center">
                                                                        {console.log("debug for export stock: ", detail)}
                                                                        <td className="border p-2">{detail.ordtId}</td>
                                                                        <td className="border p-2">{detail.amountNotVat.toLocaleString()} VND</td>
                                                                        <td className="border p-2">{detail.vatRate}% ( {formatPrice(((detail.unitPrice) * (1 + detail.vatRate) - (detail.unitPrice)) / 100)} )</td>

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
                                );
                            })}
                    </tbody>
                </table>
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
            <InvoiceModal
                isOpen={isInvoiceModalOpen}
                onClose={handleCloseInvoiceModal}
                selectedOrder={selectedOrder}
            />
            <ImportStockModal
                isOpen={isImportStockModalOpen}
                onClose={handleCloseImportStockModal}
                onStockCreated={() => {
                    const fetchImportStocks = async () => {
                        try {
                            const response = await axios.get("http://localhost:6789/api/v1/stocks", {
                                withCredentials: "true"
                            });
                            setImportStocks(response.data.content.map(stock => ({ ...stock, showDetails: false })));
                        } catch (err) {
                            setImportError("Failed to fetch import stocks");
                        }
                    };
                    fetchImportStocks();
                }}
            />
        </div>
    );
};

export default InventoryTable;