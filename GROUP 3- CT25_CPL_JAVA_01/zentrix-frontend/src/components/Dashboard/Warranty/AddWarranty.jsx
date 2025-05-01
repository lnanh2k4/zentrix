import React, { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Search, X, Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { motion, AnimatePresence } from "framer-motion";
import jsPDF from "jspdf";
import { showNotification } from '../NotificationPopup';

const ConfirmModal = ({ isOpen, onClose, onConfirm, message }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-sm">
                <h3 className="text-lg font-semibold mb-4">Confirmation</h3>
                <p className="text-sm text-gray-700 mb-6">{message}</p>
                <div className="flex justify-end gap-2">
                    <Button
                        type="button"
                        variant="outline"
                        onClick={onClose}
                        className="border-gray-300 hover:bg-gray-100"
                    >
                        No
                    </Button>
                    <Button
                        type="button"
                        onClick={onConfirm}
                        className="bg-blue-500 text-white hover:bg-blue-600"
                    >
                        Yes
                    </Button>
                </div>
            </div>
        </div>
    );
};

const AddWarranty = ({ isOpen, onClose, onSubmit }) => {
    const defaultWarrantyData = {
        userId: "",
        prodTypeId: "",
        prodTypeName: "",
        prodTypeImage: "/images/placeholder.jpg",
        weeksDuration: "1",
        status: 1,
        fullName: "",
        email: "",
        phone: "",
        description: "",
        receive: "",
    };

    const [warrantyData, setWarrantyData] = useState(defaultWarrantyData);
    const [searchPhone, setSearchPhone] = useState("");
    const [searchResults, setSearchResults] = useState([]);
    const [isSearching, setIsSearching] = useState(false);
    const [productSearchQuery, setProductSearchQuery] = useState("");
    const [allProducts, setAllProducts] = useState([]);
    const [productSearchResults, setProductSearchResults] = useState([]);
    const [userId, setUserId] = useState(null);
    const [createdByFullName, setCreatedByFullName] = useState("");
    const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
    const [pendingSubmit, setPendingSubmit] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [showFullForm, setShowFullForm] = useState(false);

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success) {
                setUserId(response.data.content.userId);
                const fullName = `${response.data.content.firstName || ""} ${response.data.content.lastName || ""}`.trim();
                setCreatedByFullName(fullName || "N/A");
            } else {
                console.error("No user info found");
                setUserId(null);
                setCreatedByFullName("N/A");
            }
        } catch (error) {
            console.error("Error fetching user info:", error.response?.data || error.message);
            setUserId(null);
            setCreatedByFullName("N/A");
        }
    };

    const fetchUserPurchasedProducts = async (userId) => {
        if (!userId) return;
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/orders/user/${userId}?page=0&size=1000`, {
                withCredentials: true,
            });
            if (response.data.success) {
                const orders = response.data.content || [];
                const productTypes = [];
                const seenProdTypeIds = new Set();

                orders.forEach(order => {
                    order.orderDetails?.forEach(detail => {
                        const pt = detail.prodTypeBranchId?.prodTypeId;
                        if (pt && !seenProdTypeIds.has(pt.prodTypeId)) {
                            seenProdTypeIds.add(pt.prodTypeId);
                            productTypes.push({
                                prodTypeId: pt.prodTypeId,
                                prodTypeName: pt.prodTypeName,
                                prodTypePrice: pt.prodTypePrice || 0,
                                unitPrice: detail.unitPrice || pt.prodTypePrice || 0,
                            });
                        }
                    });
                });

                const enrichedProductTypes = await Promise.all(
                    productTypes.map(async (pt) => {
                        const image = await fetchImageProductType(pt.prodTypeId);
                        return { ...pt, image };
                    })
                );
                setAllProducts(enrichedProductTypes);
            } else {
                setAllProducts([]);
            }
        } catch (error) {
            console.error("Error fetching purchased products:", error);
            setAllProducts([]);
            showNotification("Failed to fetch purchased products.", 3000, 'fail');
        }
    };

    useEffect(() => {
        if (isOpen) {
            fetchUserInfo();
            setShowFullForm(false);
            setWarrantyData(defaultWarrantyData);
            setSearchPhone("");
            setSearchResults([]);
            setProductSearchQuery("");
            setProductSearchResults([]);
        }
    }, [isOpen]);

    const handleSearchPhone = async () => {
        if (!searchPhone.trim()) {
            setSearchResults([]);
            showNotification("Please enter a phone number to search.", 3000, 'fail');
            return;
        }
        try {
            setIsSearching(true);
            const response = await axios.get(`http://localhost:6789/api/v1/users/phone/${encodeURIComponent(searchPhone)}`, {
                withCredentials: true,
            });
            if (response.data.success && response.data.content) {
                const content = response.data.content;
                let users;

                // Kiểm tra xem content là mảng hay đối tượng
                if (Array.isArray(content)) {
                    users = content;
                } else {
                    users = [content]; // Chuyển đối tượng thành mảng chứa một phần tử
                }

                if (users.length > 0) {
                    const activeUsers = users.filter(user => user.status === 1);
                    if (activeUsers.length > 0) {
                        setSearchResults(activeUsers);
                    } else {
                        setSearchResults([]);
                        showNotification("No active users found with this phone number.", 3000, 'fail');
                    }
                } else {
                    setSearchResults([]);
                    showNotification("No users found with this phone number.", 3000, 'fail');
                }
            } else {
                setSearchResults([]);
                showNotification(response.data.message || "No users found.", 3000, 'fail');
            }
        } catch (error) {
            console.error("Error searching user by phone:", error);
            setSearchResults([]);
            if (error.response?.status === 403) {
                showNotification("Access denied. Please ensure you are logged in as Seller Staff or Admin.", 3000, 'fail');
            } else {
                showNotification(error.response?.data?.message || "An error occurred while searching for the user.", 3000, 'fail');
            }
        } finally {
            setIsSearching(false);
        }
    };

    const handleSelectUser = (user) => {
        setWarrantyData({
            ...warrantyData,
            userId: user.userId,
            fullName: `${user.firstName || ""} ${user.lastName || ""}`.trim(),
            email: user.email || "",
            phone: user.phone || "",
        });
        setSearchResults([]);
        setSearchPhone("");
        setShowFullForm(true);
        fetchUserPurchasedProducts(user.userId);
    };

    const handleClearUser = () => {
        setWarrantyData(defaultWarrantyData);
        setShowFullForm(false);
        setAllProducts([]);
    };

    const handleCancelSearch = () => {
        setSearchPhone("");
        setSearchResults([]);
        onClose();
    };

    const fetchImageProductType = async (prodTypeId) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, null, {
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
            console.error("Error fetching product image:", error);
            return "/images/placeholder.jpg";
        }
    };

    const handleSearchProductType = (query) => {
        setProductSearchQuery(query);
        if (!query.trim()) {
            setProductSearchResults([]);
            return;
        }
        const filteredProducts = allProducts.filter((product) =>
            product.prodTypeName.toLowerCase().includes(query.toLowerCase())
        );
        setProductSearchResults(filteredProducts.slice(0, 10));
    };

    const handleSelectProductType = (product) => {
        setWarrantyData({
            ...warrantyData,
            prodTypeId: product.prodTypeId,
            prodTypeName: product.prodTypeName,
            prodTypeImage: product.image,
        });
        setProductSearchResults([]);
        setProductSearchQuery("");
    };

    const handleClearProductType = () => {
        setWarrantyData({
            ...warrantyData,
            prodTypeId: "",
            prodTypeName: "",
            prodTypeImage: "/images/placeholder.jpg",
        });
    };

    const calculateEndDate = (weeks) => {
        const startDate = new Date();
        const endDate = new Date(startDate);
        endDate.setDate(endDate.getDate() + weeks * 7);
        return endDate;
    };

    const handleExportWarrantyPDF = (warranty) => {
        const doc = new jsPDF();
        doc.setFont("times", "normal");

        doc.setFontSize(18);
        doc.text("WARRANTY RECEIPT", doc.internal.pageSize.width / 2, 20, { align: "center" });

        doc.setFontSize(11);
        doc.text("ZENTRIX INFORMATION TECHNOLOGY SOLUTIONS TRADING SERVICES COMPANY LIMITED", 14, 30);
        doc.text("Address: 600, Nguyen Van Cu Street, An Binh Ward, Ninh Kieu District, Cantho City, Vietnam", 14, 35);
        doc.text("Hotline: 0393.510.720", 14, 40);
        doc.setLineWidth(0.5);
        doc.line(14, 45, 196, 45);

        doc.setFontSize(14);
        doc.text("REPAIR RECEIPT - WARRANTY", 14, 55);

        doc.setFontSize(11);
        doc.text(`Customer: ${warrantyData.fullName || "N/A"}`, 14, 65);
        doc.text(`Phone: ${warrantyData.phone || "N/A"}`, 14, 70);
        doc.text(`Product: ${warrantyData.prodTypeName || "N/A"}`, 14, 75);
        doc.text(doc.splitTextToSize(`Description: ${warrantyData.description || "N/A"}`, 180), 14, 80);
        doc.text(doc.splitTextToSize(`Received Items: ${warrantyData.receive || "N/A"}`, 180), 14, 90);
        doc.text(`Start date: ${warranty.warnStartDate ? new Date(warranty.warnStartDate).toLocaleDateString("vi-VN") : "N/A"}`, 14, 100);
        doc.text(`End date: ${warranty.warnEndDate ? new Date(warranty.warnEndDate).toLocaleDateString("vi-VN") : "N/A"}`, 14, 105);
        const weeksDuration = warranty.warnEndDate && warranty.warnStartDate
            ? Math.ceil((new Date(warranty.warnEndDate) - new Date(warranty.warnStartDate)) / (1000 * 60 * 60 * 24 * 7))
            : warrantyData.weeksDuration || "N/A";
        doc.text(`Warranty period: ${weeksDuration} weeks`, 14, 110);

        doc.setFontSize(10);
        doc.text("NOTE:", 14, 130);
        doc.text("NOT RECEIVED CUSTOMER INFORMATION", 14, 135);
        doc.text("Bring the repair receipt when picking up the device.", 14, 140);
        doc.text("Warranty period from 10-15 days excluding holidays", 14, 145);
        doc.setLineWidth(0.5);
        doc.line(14, 150, 196, 150);

        doc.setFontSize(10);
        doc.text("CUSTOMER SIGN FOR EQUIPMENT", 14, 165);
        doc.text("Technician", 150, 165);

        doc.setFontSize(9);
        doc.setTextColor(255, 0, 0);
        doc.text("ZENSTIX.STORE", 150, 175);
        doc.text(`Created by: ${createdByFullName}`, 130, 180);
        doc.text("RECEIVED", 150, 185);

        doc.save(`warranty_${warrantyData.userId || "unknown"}_${warrantyData.prodTypeId || "unknown"}.pdf`);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!warrantyData.userId) {
            showNotification("Please select a user before submitting.", 3000, 'fail');
            return;
        }
        if (!warrantyData.prodTypeId) {
            showNotification("Please select a product type before submitting.", 3000, 'fail');
            return;
        }
        if (!userId) {
            showNotification("User information not available. Please log in again.", 3000, 'fail');
            return;
        }

        try {
            setIsLoading(true);
            const weeks = parseInt(warrantyData.weeksDuration);
            const warnStartDate = new Date();
            const warnEndDate = calculateEndDate(weeks);

            const updatedWarrantyData = {
                ...warrantyData,
                warnStartDate: warnStartDate.toISOString(),
                warnEndDate: warnEndDate.toISOString(),
            };
            setWarrantyData(updatedWarrantyData);

            const payload = {
                userId: parseInt(warrantyData.userId),
                prodTypeId: parseInt(warrantyData.prodTypeId),
                warnStartDate: warnStartDate.toISOString(),
                warnEndDate: warnEndDate.toISOString(),
                status: parseInt(warrantyData.status),
                description: warrantyData.description,
                receive: warrantyData.receive,
            };

            const response = await axios.post(
                `http://localhost:6789/api/v1/warranties?createdById=${userId}`,
                payload,
                {
                    headers: { "Content-Type": "application/json" },
                    withCredentials: true,
                }
            );

            setPendingSubmit(response.data.content || payload);
            setIsConfirmModalOpen(true);
        } catch (error) {
            console.error("Error creating warranty:", error);
            if (error.response?.status === 401) {
                showNotification("Unauthorized: Please log in again.", 3000, 'fail');
            } else if (error.response) {
                showNotification(`Failed to create warranty: ${error.response.data?.message || error.response.statusText}`, 3000, 'fail');
            } else if (error.request) {
                showNotification("Network error: Unable to reach the server.", 3000, 'fail');
            } else {
                showNotification("Error: " + error.message, 3000, 'fail');
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleConfirmPrint = () => {
        if (pendingSubmit) {
            handleExportWarrantyPDF(pendingSubmit);
            showNotification("Warranty created successfully!", 3000, 'complete');
            onSubmit(pendingSubmit);
            setWarrantyData(defaultWarrantyData);
            setSearchPhone("");
            setSearchResults([]);
            setProductSearchQuery("");
            setProductSearchResults([]);
            setPendingSubmit(null);
            setIsConfirmModalOpen(false);
            setShowFullForm(false);
            onClose();
        }
    };

    const handleCancelPrint = () => {
        if (pendingSubmit) {
            showNotification("Warranty created successfully!", 3000, 'complete');
            onSubmit(pendingSubmit);
            setWarrantyData(defaultWarrantyData);
            setSearchPhone("");
            setSearchResults([]);
            setProductSearchQuery("");
            setProductSearchResults([]);
            setPendingSubmit(null);
            setIsConfirmModalOpen(false);
            setShowFullForm(false);
            onClose();
        }
    };

    const handleCancel = () => {
        setWarrantyData(defaultWarrantyData);
        setSearchPhone("");
        setSearchResults([]);
        setProductSearchQuery("");
        setProductSearchResults([]);
        setShowFullForm(false);
        onClose();
    };

    if (!isOpen) return null;

    return (
        <>
            <div className="fixed inset-0 flex items-center justify-center bg-black/30 z-50">
                <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="text-xl font-bold">Create Warranty</h2>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="relative">
                            <label className="block text-sm font-medium text-gray-700">Customer Information</label>
                            <div className="mt-1 relative">
                                {!warrantyData.userId ? (
                                    <div className="flex gap-2">
                                        <Input
                                            type="text"
                                            value={searchPhone}
                                            onChange={(e) => setSearchPhone(e.target.value)}
                                            placeholder="Enter phone number"
                                            className="block w-full border border-gray-300 rounded-md p-2"
                                        />
                                        <Button
                                            type="button"
                                            onClick={handleSearchPhone}
                                            disabled={isSearching}
                                            className="bg-blue-500 text-white hover:bg-blue-600"
                                        >
                                            {isSearching ? "Searching..." : <Search size={20} />}
                                        </Button>
                                        <Button
                                            type="button"
                                            variant="outline"
                                            onClick={handleCancelSearch}
                                            disabled={isSearching}
                                            className="border-gray-300 hover:bg-gray-100"
                                        >
                                            Cancel
                                        </Button>
                                    </div>
                                ) : (
                                    <div className="bg-gray-100 border border-gray-300 rounded-md p-2 space-y-2">
                                        <div className="flex items-center justify-between">
                                            <span className="text-sm font-medium">Full Name: {warrantyData.fullName}</span>
                                            <Button
                                                type="button"
                                                variant="ghost"
                                                className="p-1 text-gray-500 hover:text-red-500"
                                                onClick={handleClearUser}
                                            >
                                                <X size={16} />
                                            </Button>
                                        </div>
                                        <div className="text-sm">Email: {warrantyData.email}</div>
                                        <div className="text-sm">Phone: {warrantyData.phone}</div>
                                    </div>
                                )}
                            </div>
                            <AnimatePresence>
                                {searchResults.length > 0 && (
                                    <motion.div
                                        initial={{ opacity: 0, y: 10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        exit={{ opacity: 0, y: 10 }}
                                        className="absolute left-0 mt-2 w-full bg-white shadow-lg rounded-md p-2 z-50 max-h-40 overflow-y-auto"
                                    >
                                        {searchResults.map((user) => (
                                            <div
                                                key={user.userId}
                                                className="p-2 hover:bg-gray-100 cursor-pointer"
                                                onClick={() => handleSelectUser(user)}
                                            >
                                                <p><strong>Username:</strong> {user.username}</p>
                                                <p><strong>Full Name:</strong> {user.firstName} {user.lastName}</p>
                                                <p><strong>Phone:</strong> {user.phone}</p>
                                            </div>
                                        ))}
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>

                        {showFullForm && (
                            <>
                                <div className="relative">
                                    <label className="block text-sm font-medium text-gray-700">Product Type</label>
                                    <div className="mt-1 relative">
                                        {!warrantyData.prodTypeId ? (
                                            <div className="flex items-center">
                                                <Input
                                                    type="text"
                                                    value={productSearchQuery}
                                                    onChange={(e) => handleSearchProductType(e.target.value)}
                                                    placeholder="Search purchased product type..."
                                                    className="block w-full border border-gray-300 rounded-md p-2"
                                                />
                                                <Search className="absolute right-3 text-gray-400" size={20} />
                                            </div>
                                        ) : (
                                            <div className="flex items-center gap-3 bg-gray-100 border border-gray-300 rounded-md p-2">
                                                <img
                                                    src={warrantyData.prodTypeImage}
                                                    alt={warrantyData.prodTypeName}
                                                    className="w-12 h-12 object-cover rounded"
                                                    onError={(e) => (e.target.src = "/images/placeholder.jpg")}
                                                />
                                                <span className="flex-1 text-sm">
                                                    {warrantyData.prodTypeName} (ID: {warrantyData.prodTypeId})
                                                </span>
                                                <Button
                                                    type="button"
                                                    variant="ghost"
                                                    className="p-1 text-gray-500 hover:text-red-500"
                                                    onClick={handleClearProductType}
                                                >
                                                    <X size={16} />
                                                </Button>
                                            </div>
                                        )}
                                    </div>
                                    <AnimatePresence>
                                        {productSearchQuery && productSearchResults.length > 0 && (
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

                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Warranty Duration (Weeks)</label>
                                    <select
                                        name="weeksDuration"
                                        value={warrantyData.weeksDuration}
                                        onChange={(e) => setWarrantyData({ ...warrantyData, [e.target.name]: e.target.value })}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                                        required
                                    >
                                        {[...Array(10)].map((_, i) => (
                                            <option key={i + 1} value={i + 1}>
                                                {i + 1} Week{i + 1 > 1 ? "s" : ""}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Description</label>
                                    <Textarea
                                        name="description"
                                        value={warrantyData.description}
                                        onChange={(e) => setWarrantyData({ ...warrantyData, [e.target.name]: e.target.value })}
                                        placeholder="Enter warranty description"
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                                        rows={3}
                                        maxLength={255}
                                    />
                                    <p className="text-sm text-gray-500 mt-1">
                                        {255 - warrantyData.description.length} characters remaining
                                    </p>
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Received Items</label>
                                    <Textarea
                                        name="receive"
                                        value={warrantyData.receive}
                                        onChange={(e) => setWarrantyData({ ...warrantyData, [e.target.name]: e.target.value })}
                                        placeholder="Enter received items (e.g., Phone+cable)"
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                                        rows={3}
                                        maxLength={255}
                                    />
                                    <p className="text-sm text-gray-500 mt-1">
                                        {255 - warrantyData.receive.length} characters remaining
                                    </p>
                                </div>

                                <div className="flex justify-end gap-2">
                                    <Button type="button" variant="outline" onClick={handleCancel} disabled={isLoading}>
                                        Cancel
                                    </Button>
                                    <Button
                                        type="submit"
                                        className="bg-blue-500 text-white hover:bg-blue-600 flex items-center gap-2"
                                        disabled={isLoading}
                                    >
                                        {isLoading ? (
                                            <>
                                                <Loader2 className="animate-spin" size={20} />
                                                Saving...
                                            </>
                                        ) : (
                                            "Save"
                                        )}
                                    </Button>
                                </div>
                            </>
                        )}
                    </form>
                </div>
            </div>

            <ConfirmModal
                isOpen={isConfirmModalOpen}
                onClose={handleCancelPrint}
                onConfirm={handleConfirmPrint}
                message="Warranty created successfully! Would you like to print the warranty receipt as a PDF?"
            />
        </>
    );
};

export default AddWarranty;