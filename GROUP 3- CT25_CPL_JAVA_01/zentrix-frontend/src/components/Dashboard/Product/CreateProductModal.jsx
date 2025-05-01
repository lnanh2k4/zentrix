import React, { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Loader2, X } from "lucide-react";
import "./ProductTable.css";
import { showNotification } from '../NotificationPopup';
import { showConfirm } from '../ConfirmPopup';

const CreateProductModal = ({
    handleCloseModal,
    handleAddProductType,
    handleSubmitFinal,
    newProductTypes,
    selectedProductId,
    handleProductSelect,
    isLoadingProducts,
    products,
    newProduct,
    handleInputChange,
    setNewProduct,
    isLoadingCategories,
    categories,
    isLoadingSuppliers,
    suppliers,
    newProductType,
    setNewProductType,
    newBranch,
    setNewBranch,
    isLoadingBranches,
    branches,
    handleAddBranch,
    newProductTypeBranches,
    setNewProductTypeBranches,
    newAttribute,
    setNewAttribute,
    isLoadingAttributes,
    attributes,
    setAttributes, // Thêm prop để cập nhật attributes
    handleAddAttribute,
    newAttributeName,
    setNewAttributeName,
    handleCreateAttribute,
    newProductTypeAttributes,
    setNewProductTypeAttributes,
    newVariation,
    setNewVariation,
    isLoadingVariations,
    variations,
    setVariations,
    handleAddVariation,
    newVariationName,
    setNewVariationName,
    handleCreateVariation,
    newProductTypeVariations,
    setNewProductTypeVariations,
    handleImageChange,
    imagePreviews,
    setImagePreviews,
    handleRemoveImage,
    errors,
    isLoadingSaveProduct
}) => {
    const [selectedNewProductTypeIndex, setSelectedNewProductTypeIndex] = React.useState(null);
    const [isModalAttributeManage, setIsModalAttributeManage] = useState(false);
    const [isModalVariationManage, setIsModalVariationManage] = useState(false);
    const [attributeForm, setAttributeForm] = useState({ atbId: null, atbName: "" });
    const [variationForm, setVariationForm] = useState({ variId: null, variName: "" });
    const [errorMessage, setErrorMessage] = useState("");

    const isProductDetailsDisabled = newProductTypes.length > 0;
    const handleNewProductTypeSelect = (index) => {
        setSelectedNewProductTypeIndex(index);
        if (index !== null && newProductTypes[index]) {
            const selectedType = newProductTypes[index];
            setNewProductType({
                prodTypeName: selectedType.prodTypeName || "",
                prodTypePrice: selectedType.prodTypePrice || "",
                unit: selectedType.unit || "",
                unitPrice: selectedType.unitPrice || "",
                status: selectedType.status || 1,
            });
            setNewProductTypeBranches(selectedType.branches || []);
            setNewProductTypeAttributes(selectedType.attributes || []);
            setNewProductTypeVariations(selectedType.variations || []);
            setImagePreviews(selectedType.images || []);
        } else {
            setNewProductType({ prodTypeName: "", prodTypePrice: "", unit: "", unitPrice: "", status: 1 });
            setNewProductTypeBranches([]);
            setNewProductTypeAttributes([]);
            setNewProductTypeVariations([]);
            setImagePreviews([]);
        }
    };

    const onAddProductType = () => {
        const updatedProductType = {
            ...newProductType,
            branches: [...newProductTypeBranches],
            attributes: [...newProductTypeAttributes],
            variations: [...newProductTypeVariations],
            images: [...imagePreviews],
        };

        if (selectedNewProductTypeIndex !== null) {
            const updatedProductTypes = [...newProductTypes];
            updatedProductTypes[selectedNewProductTypeIndex] = updatedProductType;
            handleAddProductType(updatedProductTypes);
        } else {
            handleAddProductType([...newProductTypes, updatedProductType]);
        }

        setSelectedNewProductTypeIndex(null);
    };

    const handleOpenModalManageAttribute = () => {
        setIsModalAttributeManage(true);
    };

    const handleCloseModalManageAttribute = () => {
        setIsModalAttributeManage(false);
        setAttributeForm({ atbId: null, atbName: "" });
        setErrorMessage("");
    };

    const handleOpenModalManageVariation = () => {
        setIsModalVariationManage(true);
    };

    const handleCloseModalManageVariation = () => {
        setIsModalVariationManage(false);
        setVariationForm({ variId: null, variName: "" });
        setErrorMessage("");
    };


    // Attribute CRUD Operations
    const fetchAttributes = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/attributes", {
                withCredentials: true,
            });
            console.log("Fetched attributes:", response.data.content);
            setAttributes(response.data.content ? response.data.content.reverse() : []);
        } catch (error) {
            setErrorMessage("Failed to fetch attributes.");
        }
    };

    const createAttribute = async () => {
        if (!newAttributeName.trim()) {
            showNotification("Attribute name is required.", 3000, 'fail');
            return;
        }

        const isDuplicate = attributes.some(
            (attribute) => attribute.atbName.toLowerCase() === newAttributeName.toLowerCase()
        );

        if (isDuplicate) {
            showNotification("Attribute name already exists!", 3000, 'fail');
            return;
        }

        setIsLoading(true);
        try {
            const response = await axios.post(
                "http://localhost:6789/api/v1/attributes/createAttribute",
                { atbName: newAttributeName },
                { withCredentials: true }
            );
            setNewAttributeName("");
            showNotification("Attribute created successfully!", 3000, 'complete');
            await fetchAttributes();
        } catch (error) {
            console.error("Error creating attribute:", error);
            showNotification("Failed to create attribute.", 3000, 'fail');
        } finally {
            setIsLoading(false);
        }
    };

    const deleteAttribute = async (atbId) => {
        if (!confirm(`Are you sure you want to delete attribute with ID ${atbId}?`)) return;

        try {
            const readyInUsed = await axios.get(`http://localhost:6789/api/v1/products/attributes/${atbId}/usage`, {
                withCredentials: true,
            });
            console.log(readyInUsed.data)
            if (readyInUsed.data) {
                setErrorMessage("Failed to delete attribute. It might be in use.");
                showNotification("Failed to delete attribute. It might be in use.", 3000, 'fail');

            } else {
                const response = await axios.delete(`http://localhost:6789/api/v1/attributes/${atbId}`, {
                    withCredentials: true,
                });
                if (response.status === 200) {
                    await fetchAttributes();
                    setAttributeForm([]);
                }
            }


        } catch (error) {
            setErrorMessage("Failed to delete attribute. It might be in use.");
        }
    };

    // Variation CRUD Operations
    const fetchVariations = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/variations", {
                withCredentials: true,
            });
            // Reverse the array if it exists, otherwise set empty array
            setVariations(response.data.content ? response.data.content.reverse() : []);
        } catch (error) {
            setErrorMessage("Failed to fetch variations.");
        }
    };

    const createVariation = async () => {
        if (!newVariationName.trim()) {
            showNotification("Variation name is required.", 3000, 'fail');

            return;
        }

        const isDuplicate = variations.some(
            (variation) => variation.variName.toLowerCase() === newVariationName.toLowerCase()
        );

        if (isDuplicate) {
            showNotification("Variation already exists!", 3000, 'fail');
            return;
        }

        try {
            await axios.post(
                "http://localhost:6789/api/v1/variations/createVariation",
                { variName: newVariationName },
                { withCredentials: true }
            );
            setNewVariationName("");
            setErrorMessage("");
            showNotification("Variation created successfully!", 3000, 'complete');
            await fetchVariations();
        } catch (error) {
            setErrorMessage("Failed to create variation.");
        }
    };
    const updateAttribute = async () => {
        if (!attributeForm.atbName.trim()) {
            showNotification("Attribute name is required.", 3000, 'fail');
            return;
        }

        // Kiểm tra xem atbName mới có trùng với attribute nào khác không (trừ chính nó)
        const isDuplicate = attributes.some(
            (attribute) =>
                attribute.atbName.toLowerCase() === attributeForm.atbName.toLowerCase() &&
                attribute.atbId !== attributeForm.atbId
        );

        if (isDuplicate) {
            showNotification("Variation already exists!", 3000, 'fail');
            return;
        }

        try {
            await axios.put(
                `http://localhost:6789/api/v1/attributes/${attributeForm.atbId}`,
                { atbName: attributeForm.atbName },
                { withCredentials: true }
            );
            await fetchAttributes();
            setAttributeForm({ atbId: null, atbName: "" });
            setErrorMessage("");
            showNotification("Attribute updated successfully!", 3000, 'complete');
        } catch (error) {
            setErrorMessage("Failed to update attribute.");
        }
    };
    const updateVariation = async () => {
        if (!variationForm.variName.trim()) {
            showNotification("Variation name is required.", 3000, 'fail');

            return;
        }

        const isDuplicate = variations.some(
            (variation) =>
                variation.variName.toLowerCase() === variationForm.variName.toLowerCase() &&
                variation.variId !== variationForm.variId
        );

        if (isDuplicate) {
            showNotification("Variation already exists!", 3000, 'fail');
            return;
        }

        setIsLoading(true);
        try {
            await axios.put(
                `http://localhost:6789/api/v1/variations/${variationForm.variId}`,
                { variName: variationForm.variName },
                { withCredentials: true }
            );
            await fetchVariations();
            setVariationForm({ variId: null, variName: "" });
            setErrorMessage("");
        } catch (error) {
            setErrorMessage("Failed to update variation.");
        } finally {
            setIsLoading(false);
        }
    };

    const deleteVariation = async (variId) => {
        if (!confirm(`Are you sure you want to delete variation with ID ${variId}?`)) return;

        try {
            const readyInUsed = await axios.get(`http://localhost:6789/api/v1/products/variations/${variId}/usage`, {
                withCredentials: true,
            });
            if (readyInUsed.data) {
                setErrorMessage("Failed to delete variation. It might be in use.");
                showNotification("Failed to delete variation. It might be in use.", 3000, 'complete');
            } else {
                const response = await axios.delete(`http://localhost:6789/api/v1/variations/${variId}`, {
                    withCredentials: true,
                });
                if (response.status === 200) {
                    await fetchVariations();
                    setVariationForm([]);
                }
            }
        } catch (error) {
            setErrorMessage("Failed to delete variation.");
        }
    };

    useEffect(() => {
        if (isModalAttributeManage) fetchAttributes();
        if (isModalVariationManage) fetchVariations();
    }, [isModalAttributeManage, isModalVariationManage]);

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            {/* Main Modal Content */}
            <div className="fixed inset-0 bg-black opacity-50" onClick={handleCloseModal}></div>
            <div className="fixed inset-0 flex items-center justify-center z-100">
                <div className="bg-white rounded-lg shadow-lg w-full max-w-[80rem] max-h-[95vh] flex overflow-hidden">
                    {/* Sidebar */}
                    <div className="bg-gray-50 border-r border-gray-200 p-6 overflow-y-auto" style={{ width: '250px' }}>
                        <h3 className="text-lg font-semibold text-gray-800 mb-4">Added Product Types</h3>
                        <div className="space-y-2">
                            <div
                                className={`p-3 rounded-md cursor-pointer transition-all ${selectedNewProductTypeIndex === null ? "bg-blue-100 text-blue-800" : "bg-white hover:bg-gray-100"}`}
                                onClick={() => handleNewProductTypeSelect(null)}
                            >
                                New Product Type
                            </div>
                            {newProductTypes.map((pt, index) => (
                                <div
                                    key={index}
                                    className={`p-3 rounded-md cursor-pointer transition-all ${selectedNewProductTypeIndex === index ? "bg-blue-100 text-blue-800" : "bg-white hover:bg-gray-100"}`}
                                    onClick={() => handleNewProductTypeSelect(index)}
                                >
                                    {pt.prodTypeName || `Product Type ${index + 1}`} - {pt.prodTypePrice ? `${pt.prodTypePrice} VND` : "N/A"}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Main Content */}
                    <div className="flex-1 p-6 overflow-y-auto">
                        <div className="flex items-center justify-between border-b border-gray-200 pb-4">
                            <h2 className="text-2xl font-bold text-gray-800">Create New Product</h2>
                            <div className="flex gap-3">
                                <Button
                                    variant="outline"
                                    onClick={handleCloseModal}
                                    disabled={isLoadingSaveProduct}
                                    className="text-gray-600 hover:text-gray-800"
                                >
                                    {isLoadingSaveProduct ? (
                                        <div className="flex items-center gap-2">
                                            <Loader2 className="animate-spin" size={20} />
                                            Can't not leave while creating product
                                        </div>
                                    ) : (
                                        "Cancel"
                                    )}
                                </Button>
                                <Button
                                    type="button"
                                    onClick={onAddProductType}
                                    disabled={isLoadingSaveProduct}
                                    className="vip-button-pulse bg-blue-500 text-white hover:bg-blue-600 px-6 py-2 rounded-md"
                                >
                                    {isLoadingSaveProduct ? (
                                        <div className="flex items-center gap-2">
                                            <Loader2 className="animate-spin" size={20} />
                                            Can't not add/update while creating product
                                        </div>
                                    ) : (
                                        selectedNewProductTypeIndex !== null ? "Update ProductType" : "Add ProductType"
                                    )}

                                </Button>
                                <Button
                                    type="button"
                                    disabled={newProductTypes.length === 0 || isLoadingSaveProduct} // Disable when loading or no products
                                    className={`px-4 py-2 rounded-md  ${newProductTypes.length === 0 || isLoadingSaveProduct
                                        ? "bg-gray-400 text-gray-700 cursor-not-allowed "
                                        : "bg-blue-500 text-white hover:bg-blue-600 base fire-button"
                                        }`}
                                    onClick={handleSubmitFinal}
                                >
                                    {isLoadingSaveProduct ? (
                                        <div className="flex items-center gap-2 animate-gradientShift">
                                            <Loader2 className="animate-spin" size={20} />
                                            Saving...
                                        </div>
                                    ) : (
                                        "Save Product"
                                    )}
                                </Button>
                            </div>
                        </div>

                        <form onSubmit={handleSubmitFinal} className="mt-6 space-y-6">
                            {/* Product Fields */}
                            <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                <h3 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Product Details</h3>
                                <div className="grid grid-cols-2 gap-6">
                                    <div className="col-span-2 mb-6">
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Select Existing Product or Create New</label>
                                        <select
                                            value={selectedProductId}
                                            onChange={handleProductSelect}
                                            className={`mt-1 block w-full border border-gray-300 rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${isProductDetailsDisabled ? "bg-gray-100 cursor-not-allowed" : ""}`}
                                            disabled={isProductDetailsDisabled}
                                        >
                                            <option value="">Create New Product</option>
                                            {isLoadingProducts ? (
                                                <option>Loading products...</option>
                                            ) : products.length > 0 ? (
                                                products.map((product) => (
                                                    <option key={product.prodId} value={product.prodId}>
                                                        {product.prodName}
                                                    </option>
                                                ))
                                            ) : (
                                                <option>No products available</option>
                                            )}
                                        </select>
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Product Name</label>
                                        <input
                                            type="text"
                                            name="prodName"
                                            value={newProduct.prodName}
                                            onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${isProductDetailsDisabled ? "bg-gray-100 cursor-not-allowed" : ""} ${errors.prodName ? "border-red-500" : "border-gray-300"}`}
                                            disabled={isProductDetailsDisabled}
                                            required
                                        />
                                        {errors.prodName && <p className="text-red-500 text-sm mt-1">{errors.prodName}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
                                        <select
                                            name="cateId"
                                            value={newProduct.cateId}
                                            onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${isProductDetailsDisabled ? "bg-gray-100 cursor-not-allowed" : ""} ${errors.cateId ? "border-red-500" : "border-gray-300"}`}
                                            disabled={isProductDetailsDisabled}
                                            required
                                        >
                                            <option value="">Select a category</option>
                                            {isLoadingCategories ? (
                                                <option>Loading...</option>
                                            ) : categories.length > 0 ? (
                                                categories.map((cat) => (
                                                    <option key={cat.cateId} value={cat.cateId}>
                                                        {cat.cateName}
                                                    </option>
                                                ))
                                            ) : (
                                                <option>No categories available</option>
                                            )}
                                        </select>
                                        {errors.cateId && <p className="text-red-500 text-sm mt-1">{errors.cateId}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Supplier</label>
                                        <select
                                            name="suppId"
                                            value={newProduct.suppId}
                                            onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${isProductDetailsDisabled ? "bg-gray-100 cursor-not-allowed" : ""} ${errors.suppId ? "border-red-500" : "border-gray-300"}`}
                                            disabled={isProductDetailsDisabled}
                                            required
                                        >
                                            <option value="">Select a supplier</option>
                                            {isLoadingSuppliers ? (
                                                <option>Loading...</option>
                                            ) : suppliers.length > 0 ? (
                                                suppliers.map((sup) => (
                                                    <option key={sup.suppId} value={sup.suppId}>
                                                        {sup.suppName}
                                                    </option>
                                                ))
                                            ) : (
                                                <option>No suppliers available</option>
                                            )}
                                        </select>
                                        {errors.suppId && <p className="text-red-500 text-sm mt-1">{errors.suppId}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">VAT</label>
                                        <input
                                            name="vat"
                                            value={newProduct.vat}
                                            onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                            step="0.01"
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${isProductDetailsDisabled ? "bg-gray-100 cursor-not-allowed" : ""} ${errors.vat ? "border-red-500" : "border-gray-300"}`}
                                            disabled={isProductDetailsDisabled}
                                            required
                                        />
                                        {errors.vat && <p className="text-red-500 text-sm mt-1">{errors.vat}</p>}
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Description</label>
                                        <input
                                            type="text"
                                            name="description"
                                            value={newProduct.description}
                                            onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${isProductDetailsDisabled ? "bg-gray-100 cursor-not-allowed" : ""} ${errors.description ? "border-red-500" : "border-gray-300"}`}
                                            disabled={isProductDetailsDisabled}
                                        />
                                    </div>
                                    {selectedProductId && (
                                        <div className="col-span-2 mt-4 text-gray-600">
                                            <p>Current Category: {newProduct.cateId?.cateName || "N/A"}</p>
                                            <p>Current Supplier: {newProduct.suppId?.suppName || "N/A"}</p>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* ProductType Fields */}
                            <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                <h3 className="text-lg font-semibold mb-4 text-gray-800 border-b border-gray-200 pb-2">Product Type</h3>
                                <div className="grid grid-cols-2 gap-6">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Product Type Name</label>
                                        <input
                                            type="text"
                                            name="prodTypeName"
                                            value={newProductType.prodTypeName}
                                            onChange={(e) => handleInputChange(e, setNewProductType, "newProductType")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.prodTypeName ? "border-red-500" : "border-gray-300"}`}
                                        />
                                        {errors.prodTypeName && <p className="text-red-500 text-sm mt-1">{errors.prodTypeName}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Product Type Price</label>
                                        <input
                                            type="text"
                                            name="prodTypePrice"
                                            value={newProductType.prodTypePrice}
                                            onChange={(e) => handleInputChange(e, setNewProductType, "newProductType")}
                                            step="0.01"
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.prodTypePrice ? "border-red-500" : "border-gray-300"}`}
                                        />
                                        {errors.prodTypePrice && <p className="text-red-500 text-sm mt-1">{errors.prodTypePrice}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Unit</label>
                                        <input
                                            type="text"
                                            name="unit"
                                            value={newProductType.unit}
                                            onChange={(e) => handleInputChange(e, setNewProductType, "newProductType")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.unit ? "border-red-500" : "border-gray-300"}`}
                                        />
                                        {errors.unit && <p className="text-red-500 text-sm mt-1">{errors.unit}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Unit Price</label>
                                        <input
                                            type="text"
                                            name="unitPrice"
                                            value={newProductType.unitPrice}
                                            onChange={(e) => handleInputChange(e, setNewProductType, "newProductType")}
                                            step="0.01"
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.unitPrice ? "border-red-500" : "border-gray-300"}`}
                                        />
                                        {errors.unitPrice && <p className="text-red-500 text-sm mt-1">{errors.unitPrice}</p>}
                                    </div>
                                </div>
                            </div>

                            {/* ProductTypeBranch Fields */}
                            <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                <h3 className="text-lg font-semibold mb-4 text-gray-800 border-b border-gray-200 pb-2">Product Type Branches (Optional)</h3>
                                <div className="grid grid-cols-2 gap-6">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Branch</label>
                                        <select
                                            name="brchId"
                                            value={newBranch.brchId}
                                            onChange={(e) => handleInputChange(e, setNewBranch, "newBranch")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.brchId ? "border-red-500" : "border-gray-300"}`}
                                        >
                                            <option value="">Select a branch</option>
                                            {isLoadingBranches ? (
                                                <option>Loading...</option>
                                            ) : (
                                                branches.map((branch) => (
                                                    <option key={branch.brchId} value={branch.brchId}>
                                                        {branch.brchName}
                                                    </option>
                                                ))
                                            )}
                                        </select>
                                        {errors.brchId && <p className="text-red-500 text-sm mt-1">{errors.brchId}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Quantity</label>
                                        <input
                                            type="number"
                                            name="quantity"
                                            value={newBranch.quantity}
                                            onChange={(e) => handleInputChange(e, setNewBranch, "newBranch")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.quantity ? "border-red-500" : "border-gray-300"}`}
                                        />
                                        {errors.quantity && <p className="text-red-500 text-sm mt-1">{errors.quantity}</p>}
                                    </div>
                                    <Button
                                        type="button"
                                        onClick={handleAddBranch}
                                        className="bg-green-500 text-white hover:bg-green-600 mt-2 px-4 py-2 rounded-md btn-green"
                                    >
                                        Add Branch
                                    </Button>
                                    {newProductTypeBranches.length > 0 && (
                                        <div className="col-span-2 mt-4">
                                            <h4 className="text-md font-medium text-gray-700">Added Branches:</h4>
                                            <ul className="list-disc pl-5 mt-2 text-gray-800">
                                                {newProductTypeBranches.map((branch, index) => (
                                                    <li key={index}>
                                                        {branches.find((b) => b.brchId === branch.brchId)?.brchName} - Quantity: {branch.quantity}
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Attributes Fields */}
                            <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                <h3 className="text-lg font-semibold mb-4 text-gray-800 border-b border-gray-200 pb-2">Attributes for Product Type</h3>
                                <div className="grid grid-cols-2 gap-6">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Attribute</label>
                                        <select
                                            name="atbId"
                                            value={newAttribute.atbId}
                                            onChange={(e) => handleInputChange(e, setNewAttribute, "newAttribute")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.atbId ? "border-red-500" : "border-gray-300"}`}
                                        >
                                            <option value="">Select an attribute</option>
                                            {isLoadingAttributes ? (
                                                <option>Loading...</option>
                                            ) : (
                                                attributes.map((attr) => (
                                                    <option key={attr.atbId} value={attr.atbId}>
                                                        {attr.atbName}
                                                    </option>
                                                ))
                                            )}
                                        </select>
                                        {errors.atbId && <p className="text-red-500 text-sm mt-1">{errors.atbId}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Attribute Value</label>
                                        <input
                                            type="text"
                                            name="prodAtbValue"
                                            value={newAttribute.prodAtbValue}
                                            onChange={(e) => handleInputChange(e, setNewAttribute, "newAttribute")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.prodAtbValue ? "border-red-500" : "border-gray-300"}`}
                                        />
                                        {errors.prodAtbValue && <p className="text-red-500 text-sm mt-1">{errors.prodAtbValue}</p>}
                                    </div>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Attribute Description</label>
                                        <input
                                            type="text"
                                            name="atbDescription"
                                            value={newAttribute.atbDescription}
                                            onChange={(e) => handleInputChange(e, setNewAttribute, "newAttribute")}
                                            className="mt-1 block w-full border border-gray-300 rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all"
                                        />
                                    </div>
                                    <Button
                                        type="button"
                                        onClick={handleAddAttribute}
                                        className="bg-green-500 text-white hover:bg-green-600 mt-2 px-4 py-2 rounded-md btn-green"
                                    >
                                        Add Attribute
                                    </Button>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-2">New Attribute Name</label>
                                        <div className="flex gap-2">
                                            <input
                                                type="text"
                                                value={newAttributeName}
                                                onChange={(e) => setNewAttributeName(e.target.value)}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all"
                                            />
                                            <Button
                                                type="button"
                                                onClick={handleCreateAttribute}
                                                className="bg-blue-500 text-white hover:bg-blue-600 px-4 py-2 rounded-md"
                                            >
                                                Create New Attribute
                                            </Button>
                                            <Button
                                                type="button"
                                                onClick={handleOpenModalManageAttribute}
                                                className="bg-blue-500 text-white hover:bg-blue-600 px-4 py-2 rounded-md"
                                            >
                                                Manage Attribute
                                            </Button>
                                        </div>
                                    </div>
                                    {newProductTypeAttributes.length > 0 && (
                                        <div className="col-span-2 mt-4">
                                            <h4 className="text-md font-medium text-gray-700">Added Attributes:</h4>
                                            <ul className="list-disc pl-5 mt-2 text-gray-800">
                                                {newProductTypeAttributes.map((attr, index) => (
                                                    <li key={index}>
                                                        {attributes.find((a) => a.atbId === attr.atbId)?.atbName} - Value: {attr.prodAtbValue}, Description: {attr.atbDescription}
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Variations Fields */}
                            <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                <h3 className="text-lg font-semibold mb-4 text-gray-800 border-b border-gray-200 pb-2">Variations for Product Type</h3>
                                <div className="grid grid-cols-2 gap-6">
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Variation</label>
                                        <select
                                            name="variId"
                                            value={newVariation.variId}
                                            onChange={(e) => handleInputChange(e, setNewVariation, "newVariation")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.variId ? "border-red-500" : "border-gray-300"}`}
                                        >
                                            <option value="">Select a variation</option>
                                            {isLoadingVariations ? (
                                                <option>Loading...</option>
                                            ) : !variations || variations.length === 0 ? (
                                                <option value="">No variations available</option>
                                            ) : (
                                                variations.map((varItem) => (
                                                    <option key={varItem.variId} value={varItem.variId}>
                                                        {varItem.variName}
                                                    </option>
                                                ))
                                            )}
                                        </select>
                                        {errors.variId && <p className="text-red-500 text-sm mt-1">{errors.variId}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium text-gray-700 mb-2">Variation Value</label>
                                        <input
                                            type="text"
                                            name="prodTypeValue"
                                            value={newVariation.prodTypeValue}
                                            onChange={(e) => handleInputChange(e, setNewVariation, "newVariation")}
                                            className={`mt-1 block w-full border rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all ${errors.prodTypeValue ? "border-red-500" : "border-gray-300"}`}
                                        />
                                        {errors.prodTypeValue && <p className="text-red-500 text-sm mt-1">{errors.prodTypeValue}</p>}
                                    </div>
                                    <Button
                                        type="button"
                                        onClick={handleAddVariation}
                                        className="bg-green-500 text-white hover:bg-green-600 mt-2 px-4 py-2 rounded-md btn-green"
                                    >
                                        Add Variation
                                    </Button>
                                    <div className="col-span-2">
                                        <label className="block text-sm font-medium text-gray-700 mb-2">New Variation Name</label>
                                        <div className="flex gap-2">
                                            <input
                                                type="text"
                                                value={newVariationName}
                                                onChange={(e) => setNewVariationName(e.target.value)}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all"
                                            />
                                            <Button
                                                type="button"
                                                onClick={handleCreateVariation}
                                                className="bg-blue-500 text-white hover:bg-blue-600 px-4 py-2 rounded-md"
                                            >
                                                Create New Variation
                                            </Button>
                                            <Button
                                                type="button"
                                                onClick={handleOpenModalManageVariation}
                                                className="bg-blue-500 text-white hover:bg-blue-600 px-4 py-2 rounded-md"
                                            >
                                                Manage Variation
                                            </Button>
                                        </div>
                                    </div>
                                    {newProductTypeVariations.length > 0 && (
                                        <div className="col-span-2 mt-4">
                                            <h4 className="text-md font-medium text-gray-700">Added Variations:</h4>
                                            <div className="mt-2 text-gray-800">
                                                {[...new Set(newProductTypeVariations.map(v => v.variId))].map(variId => {
                                                    const variationName = variations.find(v => v.variId === variId)?.variName || "Unknown Variation";
                                                    const groupedVariations = newProductTypeVariations.filter(v => v.variId === variId);

                                                    return (
                                                        <div key={variId} className="mb-3">
                                                            <strong>{variationName}:</strong>
                                                            <ul className="list-disc pl-5 mt-1">
                                                                {groupedVariations.map((varItem, index) => (
                                                                    <li key={index}>
                                                                        Value: {varItem.prodTypeValue} - Default: {varItem.defaultVari === 1 ? "Yes" : "No"}
                                                                    </li>
                                                                ))}
                                                            </ul>
                                                        </div>
                                                    );
                                                })}
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* Image Upload */}
                            <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                <h3 className="text-lg font-semibold mb-4 text-gray-800 border-b border-gray-200 pb-2">Product Type Images</h3>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">Upload Images</label>
                                    <input
                                        type="file"
                                        accept="image/jpeg, image/png, image/bmp, image/webp, image/jpg"
                                        multiple
                                        onChange={handleImageChange}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-3 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 transition-all"
                                    />
                                    {imagePreviews.length > 0 && (
                                        <div className="mt-4 grid grid-cols-2 gap-4">
                                            {imagePreviews.map((preview, index) => (
                                                <div key={index} className="relative group">
                                                    <img
                                                        src={typeof preview === 'string' ? preview : URL.createObjectURL(preview)}
                                                        alt={`Preview ${index}`}
                                                        className="w-full h-24 object-cover rounded-md shadow-md"
                                                    />
                                                    <button
                                                        type="button"
                                                        onClick={() => handleRemoveImage(index)}
                                                        className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1.5 hover:bg-red-600"
                                                    >
                                                        <X className="h-4 w-4" />
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            {/* Attribute Management Modal */}
            {isModalAttributeManage && (
                <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-[150]">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-[600px] max-h-[80vh] overflow-y-auto">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-semibold">Attribute Management</h2>
                            <button onClick={handleCloseModalManageAttribute} className="text-gray-500 hover:text-gray-700">
                                <X className="h-6 w-6" />
                            </button>
                        </div>

                        {/* Create Attribute Section */}
                        <div className="mb-6 border-b pb-4">
                            <h3 className="text-lg font-medium text-gray-700 mb-2">Create New Attribute</h3>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={newAttributeName}
                                    onChange={(e) => setNewAttributeName(e.target.value)}
                                    className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                                    placeholder="Enter new attribute name"
                                />
                                <Button
                                    onClick={createAttribute}
                                    className="bg-blue-500 text-white hover:bg-blue-600 px-4 py-2 rounded-md whitespace-nowrap"
                                >
                                    Create Attribute
                                </Button>
                            </div>
                        </div>

                        {/* Update Attribute Section */}
                        <div className="mb-6 border-b pb-4">
                            <h3 className="text-lg font-medium text-gray-700 mb-2">Update Existing Attribute</h3>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={attributeForm.atbName}
                                    onChange={(e) => setAttributeForm({ ...attributeForm, atbName: e.target.value })}
                                    className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                                    placeholder="Select an attribute to edit"
                                    disabled={!attributeForm.atbId}
                                />
                                <Button
                                    onClick={updateAttribute}
                                    className="bg-yellow-500 text-white hover:bg-yellow-600 px-4 py-2 rounded-md whitespace-nowrap"
                                    disabled={!attributeForm.atbId}
                                >
                                    Update Attribute
                                </Button>
                            </div>
                        </div>

                        {/* Attribute List */}
                        <div>
                            <h3 className="text-lg font-medium text-gray-700 mb-2">Existing Attributes</h3>
                            {errorMessage && <p className="text-red-500 text-sm mb-2">{errorMessage}</p>}
                            <div className="border border-gray-200 rounded-md">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-4 py-2 text-left text-sm font-medium text-gray-500">ID</th>
                                            <th className="px-4 py-2 text-left text-sm font-medium text-gray-500">Name</th>
                                            <th className="px-4 py-2 text-left text-sm font-medium text-gray-500">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {attributes && attributes.length > 0 ? (
                                            attributes.map((attr) => (
                                                <tr key={attr.atbId}>
                                                    <td className="px-4 py-2 text-sm text-gray-900">{attr.atbId}</td>
                                                    <td className="px-4 py-2 text-sm text-gray-900">{attr.atbName}</td>
                                                    <td className="px-4 py-2 text-sm">
                                                        <Button
                                                            onClick={() => setAttributeForm({ atbId: attr.atbId, atbName: attr.atbName })}
                                                            className="bg-yellow-500 text-white hover:bg-yellow-600 px-3 py-1 rounded-md mr-2"
                                                        >
                                                            Edit
                                                        </Button>
                                                        <Button
                                                            onClick={() => deleteAttribute(attr.atbId)}
                                                            className="bg-red-500 text-white hover:bg-red-600 px-3 py-1 rounded-md"
                                                        >
                                                            Delete
                                                        </Button>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="3" className="px-4 py-2 text-sm text-gray-500 text-center">
                                                    No attributes available.
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Variation Management Modal */}
            {isModalVariationManage && (
                <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-[150]">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-[600px] max-h-[80vh] overflow-y-auto">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-semibold">Variation Management</h2>
                            <button onClick={handleCloseModalManageVariation} className="text-gray-500 hover:text-gray-700">
                                <X className="h-6 w-6" />
                            </button>
                        </div>

                        {/* Create Variation Section */}
                        <div className="mb-6 border-b pb-4">
                            <h3 className="text-lg font-medium text-gray-700 mb-2">Create New Variation</h3>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={newVariationName}
                                    onChange={(e) => setNewVariationName(e.target.value)}
                                    className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                                    placeholder="Enter new variation name"
                                />
                                <Button
                                    onClick={createVariation}
                                    className="bg-blue-500 text-white hover:bg-blue-600 px-4 py-2 rounded-md whitespace-nowrap"
                                >
                                    Create Variation
                                </Button>
                            </div>
                        </div>

                        {/* Update Variation Section */}
                        <div className="mb-6 border-b pb-4">
                            <h3 className="text-lg font-medium text-gray-700 mb-2">Update Existing Variation</h3>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    value={variationForm.variName}
                                    onChange={(e) => setVariationForm({ ...variationForm, variName: e.target.value })}
                                    className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                                    placeholder="Select a variation to edit"
                                    disabled={!variationForm.variId}
                                />
                                <Button
                                    onClick={updateVariation}
                                    className="bg-yellow-500 text-white hover:bg-yellow-600 px-4 py-2 rounded-md whitespace-nowrap"
                                    disabled={!variationForm.variId}
                                >
                                    Update Variation
                                </Button>
                            </div>
                        </div>

                        {/* Variation List */}
                        <div>
                            <h3 className="text-lg font-medium text-gray-700 mb-2">Existing Variations</h3>
                            {errorMessage && <p className="text-red-500 text-sm mb-2">{errorMessage}</p>}
                            <div className="border border-gray-200 rounded-md">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-4 py-2 text-left text-sm font-medium text-gray-500">ID</th>
                                            <th className="px-4 py-2 text-left text-sm font-medium text-gray-500">Name</th>
                                            <th className="px-4 py-2 text-left text-sm font-medium text-gray-500">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {variations.length > 0 ? (
                                            variations.map((varItem) => (
                                                <tr key={varItem.variId}>
                                                    <td className="px-4 py-2 text-sm text-gray-900">{varItem.variId}</td>
                                                    <td className="px-4 py-2 text-sm text-gray-900">{varItem.variName}</td>
                                                    <td className="px-4 py-2 text-sm">
                                                        <Button
                                                            onClick={() => setVariationForm({ variId: varItem.variId, variName: varItem.variName })}
                                                            className="bg-yellow-500 text-white hover:bg-yellow-600 px-3 py-1 rounded-md mr-2"
                                                        >
                                                            Edit
                                                        </Button>
                                                        <Button
                                                            onClick={() => deleteVariation(varItem.variId)}
                                                            className="bg-red-500 text-white hover:bg-red-600 px-3 py-1 rounded-md"
                                                        >
                                                            Delete
                                                        </Button>
                                                    </td>
                                                </tr>
                                            ))
                                        ) : (
                                            <tr>
                                                <td colSpan="3" className="px-4 py-2 text-sm text-gray-500 text-center">
                                                    No variations available.
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CreateProductModal;