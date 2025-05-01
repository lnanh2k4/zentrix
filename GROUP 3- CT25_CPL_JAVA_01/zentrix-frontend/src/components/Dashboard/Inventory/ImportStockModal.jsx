import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import axios from "axios";
import * as XLSX from "xlsx";
import { useUserInfo, checkUserRole } from "@/services/InfoService";
import { showNotification } from '../NotificationPopup';
import { Input } from "@/components/ui/input";
import { Search, X } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";


const ImportStockModal = ({ isOpen, onClose, onStockCreated }) => {
    const [branches, setBranches] = useState([]);
    const [suppliers, setSuppliers] = useState([]);
    const [productTypes, setProductTypes] = useState([]);
    const [productTypeBranches, setProductTypeBranches] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isStockInfoLocked, setIsStockInfoLocked] = useState(false);
    const [currentBranch, setCurrentBranch] = useState();
    const [productSearchQuery, setProductSearchQuery] = useState("");
    const [productSearchResults, setProductSearchResults] = useState([]);
    const { user } = useUserInfo();
    const fetchCurrentBranch = async () => {
        const response = await axios.get(
            `http://localhost:6789/api/v1/staffs/findStaff/${user.userId}`, {
            withCredentials: "true"
        });
        setCurrentBranch(response.data.content.brchId || []);
        return response.data.content.brchId;
    };

    // Hàm bao quanh để sử dụng await
    (async () => {
        const branch = await fetchCurrentBranch();
        console.log("Branch hiện tại là: ", branch);
    })();
    // State cho Stock
    const [newStock, setNewStock] = useState({
        brchId: "",
        supplierId: "",
        importDate: new Date().toISOString().split("T")[0],
        createdBy: 1,
    });

    // State cho StockDetails
    const [stockDetails, setStockDetails] = useState([]);
    const [newStockDetail, setNewStockDetail] = useState({
        prodTypeId: "",
        stockQuantity: "",
        importPrice: "",
    });

    // Reset form khi đóng modal
    const resetForm = () => {
        setNewStock({
            brchId: "",
            supplierId: "",
            importDate: new Date().toISOString().split("T")[0],
            createdBy: 1,
        });
        setStockDetails([]);
        setNewStockDetail({
            prodTypeId: "",
            stockQuantity: "",
            importPrice: "",
        });
        setIsStockInfoLocked(false);
        setImportErrors([]);
    };

    // Fetch dữ liệu cần thiết
    useEffect(() => {
        if (!isOpen) {
            resetForm();
            return;
        }

        const fetchData = async () => {
            setIsLoading(true);
            try {
                const [branchesResponse, suppliersResponse, productTypesResponse, productTypeBranchesResponse] = await Promise.all([
                    axios.get("http://localhost:6789/api/v1/branches?page=0&size=200", {
                        withCredentials: "true"
                    }),
                    axios.get("http://localhost:6789/api/v1/suppliers", {
                        withCredentials: "true"
                    }),
                    axios.get("http://localhost:6789/api/v1/products/productTypes?page=0&size=10000", {
                        withCredentials: "true"
                    }),
                    axios.get("http://localhost:6789/api/v1/products/productTypeBranchs?page=0&size=10000", {
                        withCredentials: "true"
                    }),
                ]);
                const enrichedProductTypes = await Promise.all(
                    productTypesResponse.data.content.map(async (pt) => {
                        const image = await fetchImageProductType(pt.prodTypeId); // Fetch image for each product type
                        return { ...pt, image };
                    })
                );

                setBranches(branchesResponse.data.content || []);
                setSuppliers(suppliersResponse.data.content.content || []);
                setProductTypes(enrichedProductTypes || []);
                setProductTypeBranches(productTypeBranchesResponse.data.content || []);
            } catch (error) {
                console.error("Error fetching data:", error);
            } finally {
                setIsLoading(false);
            }
        };

        fetchData();
    }, [isOpen]);
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
            }
            return "/images/placeholder.jpg";
        } catch (error) {
            console.error("Error fetching product image:", error);
            return "/images/placeholder.jpg";
        }
    };
    // Xử lý thay đổi input cho Stock
    const handleStockInputChange = (e) => {
        const { name, value } = e.target;
        setNewStock((prev) => ({ ...prev, [name]: value }));
    };


    const [errors, setErrors] = useState({});

    // Xử lý thay đổi input cho StockDetail
    const handleStockDetailInputChange = (e, stateSetter, stateKey) => {
        const { name, value } = e.target;
        console.log(stateKey, name);

        let newErrors = { ...errors };

        if (stateKey === "newStockDetail") {
            if (name === "importPrice") {
                if (!/^\d*\.?\d{0,2}$/.test(value)) {
                    newErrors.importPrice = "Import price must be a number with up to 2 decimal places";
                } else if (parseFloat(value) < 0) {
                    newErrors.importPrice = "Import price cannot be negative";
                } else {
                    delete newErrors.importPrice;
                    stateSetter((prev) => ({ ...prev, [name]: value }));
                }
            } else if (name === "stockQuantity") {
                if (!value.trim()) {
                    newErrors.stockQuantity = "Quantity cannot be empty";
                } else if (!/^\d+$/.test(value)) {
                    newErrors.stockQuantity = "Quantity must be a positive integer";
                } else if (parseInt(value) <= 0) {
                    newErrors.stockQuantity = "Quantity must be greater than 0";
                } else {
                    delete newErrors.stockQuantity;
                    stateSetter((prev) => ({ ...prev, [name]: value }));
                }
            } else if (name === "prodTypeId") {
                if (!value) {
                    newErrors.prodTypeId = "Please select a product type";
                } else {
                    delete newErrors.prodTypeId;
                    stateSetter((prev) => ({ ...prev, [name]: value }));
                }
            }
        }

        setErrors(newErrors);
    };
    const handleSearchProductType = (query) => {
        setProductSearchQuery(query);
        if (!query.trim()) {
            setProductSearchResults(productTypes.slice(0, 10)); // Show all products when empty
            return;
        }
        const filteredProducts = productTypes.filter((product) =>
            product.prodTypeName.toLowerCase().includes(query.toLowerCase())
        );
        setProductSearchResults(filteredProducts.slice(0, 10));
    };

    const handleSelectProductType = (product) => {
        setNewStockDetail({
            ...newStockDetail,
            prodTypeId: product.prodTypeId,
            prodTypeName: product.prodTypeName,
            image: product.image,
        });
        setProductSearchResults([]);
        setProductSearchQuery("");
        setErrors((prev) => ({ ...prev, prodTypeId: undefined }));
    };

    const handleClearProductType = () => {
        setNewStockDetail({
            ...newStockDetail,
            prodTypeId: "",
            prodTypeName: "",
            image: "",
        });
    };
    // Thêm StockDetail vào danh sách tạm
    const handleAddStockDetail = () => {
        let newErrors = { ...errors };

        // Validation cho prodTypeId
        if (!newStockDetail.prodTypeId) {
            newErrors.prodTypeId = "Please select a product type";
        } else {
            delete newErrors.prodTypeId;
        }

        // Validation cho stockQuantity
        if (!newStockDetail.stockQuantity.trim()) {
            newErrors.stockQuantity = "Quantity cannot be empty";
        } else if (!/^\d+$/.test(newStockDetail.stockQuantity)) {
            newErrors.stockQuantity = "Quantity must be a positive integer";
        } else if (parseInt(newStockDetail.stockQuantity) <= 0) {
            newErrors.stockQuantity = "Quantity must be greater than 0";
        } else {
            delete newErrors.stockQuantity;
        }

        // Validation cho importPrice
        if (!newStockDetail.importPrice.trim()) {
            newErrors.importPrice = "Import price cannot be empty";
        } else if (!/^\d*\.?\d{0,2}$/.test(newStockDetail.importPrice)) {
            newErrors.importPrice = "Import price must be a number with up to 2 decimal places";
        } else if (parseFloat(newStockDetail.importPrice) < 0) {
            newErrors.importPrice = "Import price cannot be negative";
        } else {
            delete newErrors.importPrice;
        }

        // Nếu có lỗi, setErrors và dừng lại
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        // Nếu không có lỗi, thêm stock detail
        setStockDetails((prev) => [
            ...prev,
            {
                prodTypeId: parseInt(newStockDetail.prodTypeId),
                stockQuantity: parseInt(newStockDetail.stockQuantity),
                importPrice: parseFloat(newStockDetail.importPrice),
            },
        ]);

        setIsStockInfoLocked(true);
        setNewStockDetail({ prodTypeId: "", stockQuantity: "", importPrice: "" });
        setErrors({}); // Xóa lỗi sau khi thêm thành công
    };

    // Tạo và tải file Excel mẫu
    const handleDownloadTemplate = () => {
        const link = document.createElement("a");
        link.href = "/templates/StockDetails_Template.xlsx";
        link.download = "StockDetails_Template.xlsx";
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    // Kiểm tra xem đã chọn Branch và Supplier chưa
    const isImportDisabled = !newStock.brchId || !newStock.supplierId;

    // Xử lý import từ file Excel
    const [importErrors, setImportErrors] = useState([]);
    const handleImportExcel = (e) => {
        if (isImportDisabled) {
            setImportErrors([{ message: "Please select a Branch and Supplier before importing from Excel." }]);
            return;
        }

        const file = e.target.files[0];
        if (!file) {
            setImportErrors([{ message: "Please select a file to import." }]);
            return;
        }

        if (
            !(
                file.type === "application/vnd.ms-excel" ||
                file.type === "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        ) {
            setImportErrors([{ message: "Please upload a valid Excel file (.xlsx or .xls)." }]);
            return;
        }

        const reader = new FileReader();
        reader.onload = (event) => {
            const data = new Uint8Array(event.target.result);
            const workbook = XLSX.read(data, { type: "array" });
            const sheetName = workbook.SheetNames[0];
            const worksheet = workbook.Sheets[sheetName];
            const jsonData = XLSX.utils.sheet_to_json(worksheet);

            let errors = [];
            const importedStockDetails = jsonData.map((row, index) => {
                const productType = productTypes.find(
                    (pt) => pt.prodTypeName.toLowerCase() === (row.ProductType || "").toLowerCase()
                );

                if (!productType) {
                    errors.push({
                        message: (
                            <>
                                <span className="text-red-500">Row {index + 1}</span>: Product Type <span className="text-red-500">"{row.ProductType}"</span> not found.
                            </>
                        ),
                    });
                    return null;
                }

                const quantity = row.Quantity;
                if (quantity == null) {
                    errors.push({
                        message: (
                            <>
                                <span className="text-red-500">Row {index + 1}</span>: Quantity for <span className="text-red-500">"{row.ProductType}"</span> is missing.
                            </>
                        ),
                    });
                    return null;
                } else if (isNaN(quantity) || !/^\d+$/.test(quantity.toString())) {
                    errors.push({
                        message: (
                            <>
                                <span className="text-red-500">Row {index + 1}</span>: Quantity for <span className="text-red-500">"{row.ProductType}"</span> must be a positive integer.
                            </>
                        ),
                    });
                    return null;
                } else if (parseInt(quantity) <= 0) {
                    errors.push({
                        message: (
                            <>
                                <span className="text-red-500">Row {index + 1}</span>: Quantity for <span className="text-red-500">"{row.ProductType}"</span> must be greater than 0.
                            </>
                        ),
                    });
                    return null;
                }

                const importPrice = row.ImportPrice;
                if (importPrice == null) {
                    errors.push({
                        message: (
                            <>
                                <span className="text-red-500">Row {index + 1}</span>: Import Price for <span className="text-red-500">"{row.ProductType}"</span> is missing.
                            </>
                        ),
                    });
                    return null;
                } else if (isNaN(importPrice) || !/^\d*\.?\d{0,2}$/.test(importPrice.toString())) {
                    errors.push({
                        message: (
                            <>
                                <span className="text-red-500">Row {index + 1}</span>: Import Price for <span className="text-red-500">"{row.ProductType}"</span> must be a number with up to 2 decimal places.
                            </>
                        ),
                    });
                    return null;
                } else if (parseFloat(importPrice) < 0) {
                    errors.push({
                        message: (
                            <>
                                <span className="text-red-500">Row {index + 1}</span>: Import Price for <span className="text-red-500">"{row.ProductType}"</span> cannot be negative.
                            </>
                        ),
                    });
                    return null;
                }

                return {
                    prodTypeId: productType.prodTypeId,
                    stockQuantity: parseInt(quantity),
                    importPrice: parseFloat(importPrice),
                };
            }).filter((detail) => detail !== null);

            if (errors.length > 0) {
                setImportErrors(errors);
            } else {
                setImportErrors([]);
            }

            if (importedStockDetails.length === 0) {
                if (errors.length === 0) {
                    setImportErrors([{ message: "No valid data found in the Excel file." }]);
                }
                return;
            }

            setStockDetails((prev) => [...prev, ...importedStockDetails]);
            setIsStockInfoLocked(true);
        };

        reader.readAsArrayBuffer(file);
    };

    // Xóa StockDetail khỏi danh sách tạm
    const handleRemoveStockDetail = (index) => {
        setStockDetails((prev) => {
            const updatedDetails = prev.filter((_, i) => i !== index);
            if (updatedDetails.length === 0) {
                setIsStockInfoLocked(false);
            }
            return updatedDetails;
        });
    };

    // Tạo ProductTypeBranch mới nếu chưa tồn tại
    const createProductTypeBranch = async (prodTypeId, brchId) => {
        try {
            const payload = {
                prodTypeId: prodTypeId,
                brchId: brchId,
                quantity: 0,
            };
            const response = await axios.post(
                "http://localhost:6789/api/v1/products/createProductTypeBranch",
                payload,
                {
                    headers: { "Content-Type": "application/json" },
                    withCredentials: "true"

                }
            );
            console.log("Created ProductTypeBranch:", response.data);

            const updatedResponse = await axios.get(
                "http://localhost:6789/api/v1/products/productTypeBranchs?page=0&size=10000", {
                withCredentials: "true"
            }
            );
            setProductTypeBranches(updatedResponse.data.content || []);
            return response.data;
        } catch (error) {
            console.error("Error creating ProductTypeBranch:", error);
            throw new Error("Failed to create ProductTypeBranch: " + (error.response?.data?.message || error.message));
        }
    };

    // Xử lý submit tạo Stock và StockDetails
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!newStock.brchId || !newStock.supplierId) {
            showNotification("Please select a branch and supplier.");
            return;
        }

        if (stockDetails.length === 0) {
            showNotification("Please add at least one Stock Detail.");
            return;
        }

        try {
            const stockPayload = {
                brchId: parseInt(newStock.brchId),
                supplierId: parseInt(newStock.supplierId),
                importDate: newStock.importDate,
                createdBy: newStock.createdBy,
            };
            const stockResponse = await axios.post(
                "http://localhost:6789/api/v1/stocks/create",
                stockPayload,
                {
                    headers: { "Content-Type": "application/json" },
                    withCredentials: "true"
                }
            );
            const stockId = stockResponse.data.stockId;
            console.log("Created Stock:", stockId);

            for (const detail of stockDetails) {
                let productTypeBranch = productTypeBranches.find(
                    (ptb) =>
                        ptb.prodTypeId.prodTypeId === detail.prodTypeId &&
                        ptb.brchId.brchId === parseInt(newStock.brchId)
                );

                if (!productTypeBranch) {
                    productTypeBranch = await createProductTypeBranch(detail.prodTypeId, parseInt(newStock.brchId));
                }

                const stockDetailPayload = {
                    stockId,
                    prodTypeBrchId: productTypeBranch.prodTypeBrchId,
                    stockQuantity: detail.stockQuantity,
                    importPrice: detail.importPrice,
                };
                await axios.post(
                    "http://localhost:6789/api/stock-details/create",
                    stockDetailPayload,
                    {
                        headers: { "Content-Type": "application/json" },
                        withCredentials: "true"
                    }
                );
                console.log("Created StockDetail:", stockDetailPayload);

                const updatedQuantity = productTypeBranch.quantity + detail.stockQuantity;
                const updatePayload = {
                    quantity: updatedQuantity,
                    prodTypeId: productTypeBranch.prodTypeId.prodTypeId,
                    brchId: productTypeBranch.brchId.brchId,
                };
                await axios.put(
                    `http://localhost:6789/api/v1/products/updateProductTypeBranch/${productTypeBranch.prodTypeBrchId}`,
                    updatePayload,
                    {
                        headers: { "Content-Type": "application/json" },
                        withCredentials: "true"

                    }
                );
                console.log("Updated ProductTypeBranch:", productTypeBranch.prodTypeBrchId, updatedQuantity);
            }

            showNotification("Stock and Stock Details created successfully!");
            resetForm();
            onStockCreated();
            onClose();
        } catch (error) {
            console.error("Error creating stock:", error);
            showNotification("Failed to create stock: " + (error.response?.data?.message || error.message));
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            {/* <div className="fixed inset-0 bg-black opacity-50" onClick={onClose}></div> */}
            <div className="bg-white rounded-lg shadow-lg w-full max-w-4xl p-6 max-h-[90vh] overflow-y-auto">
                <h2 className="text-2xl font-bold mb-4">Import Stock</h2>

                {isLoading ? (
                    <div>Loading...</div>
                ) : (
                    <form onSubmit={handleSubmit}>
                        {/* Stock Information */}
                        <div className="mb-4">
                            <h3 className="text-lg font-semibold mb-2">Stock Information</h3>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Branch</label>
                                    <select
                                        name="brchId"
                                        value={newStock.brchId}
                                        onChange={handleStockInputChange}
                                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm"
                                        disabled={isStockInfoLocked}
                                    >
                                        <option value="">Select Branch</option>
                                        {branches.map((branch) => (
                                            <option key={branch.brchId} value={branch.brchId}>
                                                {branch.brchName}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Supplier</label>
                                    <select
                                        name="supplierId"
                                        value={newStock.supplierId}
                                        onChange={handleStockInputChange}
                                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm"
                                        disabled={isStockInfoLocked}
                                    >
                                        <option value="">Select Supplier</option>
                                        {suppliers.map((supplier) => (
                                            <option key={supplier.suppId} value={supplier.suppId}>
                                                {supplier.suppName}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Import Date</label>
                                    <input
                                        type="text"
                                        name="importDate"
                                        value={new Intl.DateTimeFormat('vi-VN', {
                                            day: '2-digit',
                                            month: '2-digit',
                                            year: 'numeric'
                                        }).format(new Date())}
                                        className="mt-1 block w-full border-gray-300 rounded-md shadow-sm"
                                        readOnly={true}
                                    />
                                </div>
                            </div>
                        </div>

                        {/* Stock Details */}
                        <div className="mb-4">
                            <h3 className="text-lg font-semibold mb-2">Stock Details</h3>
                            <div className="grid grid-cols-4 gap-4 mb-4">
                                <div className="col-span-2">
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Product Type</label>
                                    <div className="relative">
                                        {!newStockDetail.prodTypeId ? (
                                            <div className="flex items-center">
                                                <Input
                                                    type="text"
                                                    value={productSearchQuery}
                                                    onChange={(e) => handleSearchProductType(e.target.value)}
                                                    placeholder="Search product type..."
                                                    className={`block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.prodTypeId ? "border-red-500" : "border-gray-300"}`}
                                                    disabled={isImportDisabled}
                                                />
                                                <Search className="absolute right-3 text-gray-400" size={20} />
                                            </div>
                                        ) : (
                                            <div className="flex items-center gap-3 bg-gray-100 border border-gray-300 rounded-md p-2">
                                                <img
                                                    src={newStockDetail.image}
                                                    alt={newStockDetail.prodTypeName}
                                                    className="w-12 h-12 object-cover rounded"
                                                    onError={(e) => (e.target.src = "/images/placeholder.jpg")}
                                                />
                                                <span className="flex-1 text-sm">
                                                    {newStockDetail.prodTypeName} (ID: {newStockDetail.prodTypeId})
                                                </span>
                                                <Button
                                                    type="button"
                                                    variant="ghost"
                                                    className="p-1 text-gray-500 hover:text-red-500"
                                                    onClick={handleClearProductType}
                                                    disabled={isImportDisabled}
                                                >
                                                    <X size={16} />
                                                </Button>
                                            </div>
                                        )}
                                        <AnimatePresence>
                                            {productSearchResults.length > 0 && (
                                                <motion.div
                                                    initial={{ opacity: 0, y: 10 }}
                                                    animate={{ opacity: 1, y: 0 }}
                                                    exit={{ opacity: 0, y: 10 }}
                                                    className="absolute left-0 mt-2 w-full bg-white shadow-lg rounded-md p-2 z-50 max-h-40 overflow-y-auto"
                                                >
                                                    {productSearchResults.map((product) => (
                                                        <div
                                                            key={product.prodTypeId}
                                                            className="flex items-center justify-between p-2 hover:bg-gray-100 rounded cursor-pointer"
                                                            onClick={() => handleSelectProductType(product)}
                                                        >
                                                            <div className="flex items-center gap-3">
                                                                <img
                                                                    src={product.image}
                                                                    alt={product.prodTypeName}
                                                                    className="w-12 h-12 object-cover rounded"
                                                                    onError={(e) => (e.target.src = "/images/placeholder.jpg")}
                                                                />
                                                                <p className="text-sm font-semibold">{product.prodTypeName}</p>
                                                            </div>
                                                            <div className="flex items-center">
                                                                <p className="text-sm font-semibold">
                                                                    {product.prodTypeId ? `ID: ${product.prodTypeId.toLocaleString()}` : "N/A"}
                                                                </p>
                                                            </div>
                                                        </div>
                                                    ))}
                                                </motion.div>
                                            )}
                                        </AnimatePresence>
                                    </div>
                                    {errors.prodTypeId && <p className="text-red-500 text-sm mt-1">{errors.prodTypeId}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Quantity</label>
                                    <input
                                        type="text"
                                        name="stockQuantity"
                                        value={newStockDetail.stockQuantity}
                                        onChange={(e) => handleStockDetailInputChange(e, setNewStockDetail, "newStockDetail")}
                                        className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.stockQuantity ? "border-red-500" : "border-gray-300"}`}
                                        disabled={isImportDisabled}
                                    />
                                    {errors.stockQuantity && <p className="text-red-500 text-sm mt-1">{errors.stockQuantity}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Import Price</label>
                                    <input
                                        type="text"
                                        name="importPrice"
                                        value={newStockDetail.importPrice} // Giá trị thô trong input
                                        onChange={(e) => handleStockDetailInputChange(e, setNewStockDetail, "newStockDetail")}
                                        className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.importPrice ? "border-red-500" : "border-gray-300"
                                            }`}
                                        disabled={isImportDisabled}
                                    />
                                    {/* Thẻ <p> hiển thị giá trị có dấu phẩy */}
                                    <p className="text-gray-600 text-sm mt-1">
                                        {Number(newStockDetail.importPrice).toLocaleString('en-US')} VND
                                    </p>
                                    {errors.importPrice && <p className="text-red-500 text-sm mt-1">{errors.importPrice}</p>}
                                </div>
                            </div>
                            <Button
                                type="button"
                                onClick={handleAddStockDetail}
                                className="bg-blue-500 text-white hover:bg-blue-600 btn-green"
                                disabled={isImportDisabled}
                            >
                                Add Stock Detail
                            </Button>
                            {isImportDisabled && (
                                <p className="text-sm text-red-500 mt-1">
                                    Please select a Branch and Supplier to enable Add Stock Detail.
                                </p>
                            )}
                        </div>
                        {/* Thêm nút Download Template và Import từ Excel */}
                        <div className="flex gap-4 mb-4">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">Import from Excel</label>
                                <input
                                    type="file"
                                    accept=".xlsx, .xls"
                                    onChange={handleImportExcel}
                                    className="mt-1 block w-full border rounded-md p-2 border-gray-300 shadow-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all"
                                    disabled={isImportDisabled}
                                />

                                {isImportDisabled && (
                                    <p className="text-sm text-red-500 mt-1">
                                        Please select a Branch and Supplier to enable Excel import.
                                    </p>
                                )}

                                {importErrors.length > 0 && (
                                    <ul className="mt-2 text-sm text-gray-700 list-disc pl-5">
                                        {importErrors.map((error, index) => (
                                            <li key={index}>{error.message}</li>
                                        ))}
                                    </ul>
                                )}
                                <Button
                                    type="button"
                                    onClick={handleDownloadTemplate}
                                    className="bg-yellow-500 text-white hover:bg-yellow-600 mt-2 px-4 py-2 rounded-md"
                                    disabled={isImportDisabled}
                                >
                                    Download Template
                                </Button>
                            </div>
                        </div>
                        {/* Danh sách Stock Details đã thêm */}
                        {stockDetails.length > 0 && (
                            <div className="mb-4">
                                <h4 className="text-md font-semibold mb-2">Added Stock Details</h4>
                                <table className="w-full border-collapse border border-gray-200">
                                    <thead>
                                        <tr className="bg-gray-200">
                                            <th className="border p-2">Image</th>
                                            <th className="border p-2">Product Type</th>
                                            <th className="border p-2">Quantity</th>
                                            <th className="border p-2">Import Price</th>
                                            <th className="border p-2">Total Price</th>
                                            <th className="border p-2">Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {stockDetails.map((detail, index) => {
                                            const pt = productTypes.find(
                                                (pt) => pt.prodTypeId === detail.prodTypeId
                                            );
                                            return (
                                                <tr key={index} className="text-center">
                                                    <td className="border p-2">
                                                        <img
                                                            src={pt?.image}
                                                            alt={pt?.prodTypeName}
                                                            className="w-12 h-12 object-cover rounded mx-auto"
                                                            onError={(e) => (e.target.src = "/images/placeholder.jpg")}
                                                        />
                                                    </td>
                                                    <td className="border p-2">
                                                        {pt?.prodTypeName || "N/A"}
                                                    </td>
                                                    <td className="border p-2">{detail.stockQuantity}</td>
                                                    <td className="border p-2">{detail.importPrice.toLocaleString()} VND</td>
                                                    <td className="border p-2">
                                                        {(detail.stockQuantity * detail.importPrice).toLocaleString()} VND
                                                    </td>
                                                    <td className="border p-2">
                                                        <Button
                                                            variant="outline"
                                                            onClick={() => handleRemoveStockDetail(index)}
                                                            className="text-red-600 hover:text-red-800"
                                                        >
                                                            Remove
                                                        </Button>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        )}

                        {/* Nút Submit và Cancel */}
                        <div className="flex justify-end gap-4">
                            <Button
                                variant="outline"
                                onClick={onClose}
                                className="text-gray-600 hover:text-gray-800"
                            >
                                Cancel
                            </Button>
                            <Button
                                type="submit"
                                className="bg-green-500 text-white hover:bg-green-600"
                            >
                                Create Stock
                            </Button>
                        </div>
                    </form>
                )}
            </div>
        </div>
    );
};

export default ImportStockModal;

