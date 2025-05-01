import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { CirclePower, Loader2, ToggleLeft, ToggleRight, Trash2, Wrench } from 'lucide-react';
import ImageGallery from './ImageGallery';
import "./ProductTable.css";

const ProductEditModal = ({
    editProduct,
    editProductTypes,
    productTypeBranch,
    productTypeAttribute,
    productTypeVariation,
    imagePreviews,
    handleCloseEditModal,
    handleUpdateProductType,
    handleEditSubmitFinal,
    handleEditProductTypeSelect,
    selectedEditProductType,
    newProduct,
    setNewProduct,
    newProductType,
    setNewProductType,
    newBranch,
    setNewBranch,
    newAttribute,
    setNewAttribute,
    newVariation,
    setNewVariation,
    newProductTypes,
    newProductTypeBranches,
    newProductTypeAttributes,
    newProductTypeVariations,
    handleInputChange,
    handleImageChange,
    handleRemoveImage,
    handleAddBranch,
    handleAddAttribute,
    handleAddVariation,
    handleUpdateBranch,
    handleDeleteBranch,
    handleUpdateAttribute,
    handleDeleteAttribute,
    handleUpdateVariation,
    handleDeleteVariation,
    handleEditBranchQuantity,
    editedBranchQuantities,
    handleEditAttributeValue,
    handleEditAttributeDescription,
    editedAttributeValues,
    editedAttributeDescriptions,
    handleEditVariationValue,
    editedVariationValues,
    handleUnactiveProductType,
    categories,
    suppliers,
    branches,
    attributes,
    variations,
    errors,
    handleSoftDeleteProductType,
    isLoadingSaveProduct

}
) => {
    const [branchSearchTerm, setBranchSearchTerm] = useState("");
    const filteredBranches = productTypeBranch
        ? productTypeBranch
            .filter((branch) =>
                selectedEditProductType && branch.prodTypeId?.prodTypeId === selectedEditProductType.prodTypeId
            )
            .filter((branch) =>
                branch.brchId?.brchName
                    ?.toLowerCase()
                    .includes(branchSearchTerm.toLowerCase())
            )
        : [];

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            {/* Backdrop */}
            <div className="fixed inset-0 bg-black opacity-50" onClick={handleCloseEditModal}></div>

            {/* Modal Content */}
            <div className="fixed inset-0 flex items-center justify-center z-100">
                <div className="bg-white rounded-lg shadow-lg w-full max-w-[80rem] max-h-[95vh] flex overflow-hidden">
                    {/* Sidebar for Product Type Selection */}
                    <div className="bg-gray-50 border-r border-gray-200 p-6 overflow-y-auto fixed-sidebar" style={{ width: '200hv' }}>
                        <h3 className="text-lg font-semibold text-gray-800 mb-4 ">Product Types</h3>
                        <div className="space-y-2">
                            <div
                                className={`p-3 rounded-md cursor-pointer transition-all ${!selectedEditProductType ? "bg-blue-100 text-blue-800" : "bg-white hover:bg-gray-100"}`}
                                onClick={() => handleEditProductTypeSelect("")}
                            >
                                Editing - {editProduct.prodName}
                            </div>
                            {editProductTypes.map((pt) => (
                                <div
                                    key={pt.prodTypeId}
                                    className={`p-3 rounded-md cursor-pointer transition-all flex items-center justify-between gap-4 ${selectedEditProductType?.prodTypeId === pt.prodTypeId
                                        ? "bg-blue-100 text-blue-800"
                                        : "bg-white hover:bg-gray-100"
                                        }`}
                                    onClick={() => handleEditProductTypeSelect(pt.prodTypeId.toString())}
                                >
                                    {/* Cột thông tin sản phẩm */}
                                    <div className="flex-1 truncate">
                                        <span className="font-medium">{pt.prodTypeName}</span> -{" "}
                                        <span>{pt.prodTypePrice.toLocaleString()} VND</span>
                                    </div>

                                    {/* Cột trạng thái */}
                                    <div className="flex-shrink-0">
                                        <span
                                            className={`px-2 py-1 rounded text-sm font-semibold ${pt.status === 1
                                                ? "text-green-600 bg-green-100"
                                                : pt.status === 0
                                                    ? "text-red-600 bg-red-100"
                                                    : "text-gray-600 bg-gray-100"
                                                }`}
                                        >
                                            {pt.status === 1 ? "Available" : pt.status === 0 ? "Unavailable" : "N/A"}
                                        </span>
                                    </div>

                                    {/* Cột nút hành động */}
                                    <div className="flex-shrink-0 flex gap-2">
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className={`${pt.status === 1
                                                ? "text-green-600 hover:text-green-800 border-green-200 hover:border-green-500"
                                                : "text-red-600 hover:text-red-800 border-red-200 hover:border-red-500"
                                                }`}
                                            onClick={(e) => handleUnactiveProductType(pt, e)}
                                        >
                                            {pt.status === 1 ? (
                                                <ToggleRight className="h-4 w-4" />
                                            ) : (
                                                <ToggleLeft className="h-4 w-4" />
                                            )}
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="text-red-600 hover:text-red-800 border-red-200 hover:border-red-500"
                                            onClick={(e) => handleSoftDeleteProductType(pt, e)}
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Main Content */}
                    <div className="w-2/3 p-6 overflow-y-auto main-content">
                        {/* Header */}
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-2xl font-bold text-gray-800">
                                Edit Product: {editProduct.prodName}
                            </h2>
                            <div className="flex gap-4">
                                <Button
                                    variant="outline"
                                    onClick={handleCloseEditModal}
                                    disabled={isLoadingSaveProduct}

                                    className="text-gray-600 hover:text-gray-800"
                                >
                                    {isLoadingSaveProduct ? (
                                        <div className="flex items-center gap-2">
                                            <Loader2 className="animate-spin" size={20} />
                                            Can't leave while saving!
                                        </div>
                                    ) : (
                                        "Cancel"
                                    )}
                                </Button>
                                {selectedEditProductType ? (
                                    <Button
                                        type="button"
                                        onClick={handleUpdateProductType}
                                        disabled={isLoadingSaveProduct}
                                        className="vip-button-pulse bg-blue-500 text-white hover:bg-blue-600 px-6 py-2 rounded-md"
                                    >
                                        {isLoadingSaveProduct ? (
                                            <div className="flex items-center gap-2">
                                                <Loader2 className="animate-spin" size={20} />
                                                Saving...
                                            </div>
                                        ) : (
                                            "Save ProductType"
                                        )}
                                    </Button>
                                ) : (
                                    <Button
                                        type="button"
                                        onClick={handleEditSubmitFinal}
                                        disabled={isLoadingSaveProduct} // Disable button when loading
                                        className={`vip-button-pulse px-6 py-2 rounded-md ${isLoadingSaveProduct
                                            ? "bg-gray-400 text-gray-700 cursor-not-allowed"
                                            : "bg-blue-500 text-white hover:bg-blue-600"
                                            }`}
                                    >
                                        {isLoadingSaveProduct ? (
                                            <div className="flex items-center gap-2">
                                                <Loader2 className="animate-spin" size={20} />
                                                Saving...
                                            </div>
                                        ) : (
                                            "Save Product"
                                        )}
                                    </Button>
                                )}
                            </div>
                        </div>

                        {/* Form Content */}
                        <form className="space-y-6">
                            {/* Product Fields - Always shown, but only when no ProductType is selected */}
                            {!selectedEditProductType && (
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h3 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Product Details</h3>
                                    <div className="grid grid-cols-2 gap-6">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Product Name</label>
                                            <input
                                                type="text"
                                                name="prodName"
                                                value={newProduct.prodName}
                                                onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                                required
                                            />
                                            {errors.prodName && <p className="text-red-500 text-sm mt-1">{errors.prodName}</p>}

                                        </div>
                                        <div className="col-span-2 mt-4 text-gray-600">
                                            <p><strong>Current Category:</strong> {editProduct.cateId?.cateName || "N/A"}</p>
                                            <p><strong>Current Supplier:</strong> {editProduct.suppId?.suppName || "N/A"}</p>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
                                            <select
                                                name="cateId"
                                                value={newProduct.cateId}
                                                onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            >
                                                <option value="">Select a category</option>
                                                {categories.map((cat) => (
                                                    <option key={cat.cateId} value={cat.cateId}>{cat.cateName}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Supplier</label>
                                            <select
                                                name="suppId"
                                                value={newProduct.suppId}
                                                onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            >
                                                <option value="">Select a supplier</option>
                                                {suppliers.map((sup) => (
                                                    <option key={sup.suppId} value={sup.suppId}>{sup.suppName}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">VAT</label>
                                            <input
                                                type="number"
                                                name="vat"
                                                value={newProduct.vat}
                                                onChange={(e) => handleInputChange(e, setNewProduct, "newProduct")}
                                                step="0.01"
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
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
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            />
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Product Type Fields - Shown only when a ProductType is selected */}
                            {selectedEditProductType && (
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h3 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Editing Product Type</h3>
                                    <div className="grid grid-cols-2 gap-6">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Product Type Name</label>
                                            <input
                                                type="text"
                                                name="prodTypeName"
                                                value={newProductType.prodTypeName}
                                                onChange={(e) => handleInputChange(e, setNewProductType, "newProductType")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
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
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
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
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
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
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            />
                                            {errors.unitPrice && <p className="text-red-500 text-sm mt-1">{errors.unitPrice}</p>}

                                        </div>
                                        <div className="col-span-2">
                                            <h4 className="text-md font-semibold text-gray-700 mt-4 mb-2">Images</h4>
                                            <input
                                                type="file"
                                                accept="image/jpeg, image/png, image/bmp, image/webp" // Restrict to specific image formats
                                                multiple
                                                onChange={handleImageChange}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            />
                                            <ImageGallery
                                                imagePreviews={imagePreviews}
                                                isEditMode={true}
                                                onRemove={handleRemoveImage}
                                            />
                                        </div>
                                    </div>
                                </div>
                            )}



                            {/* Attributes - Shown only when a ProductType is selected */}
                            {selectedEditProductType && (
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h3 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Attributes for Product Type</h3>
                                    <div className="grid grid-cols-2 gap-6">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Attribute</label>
                                            <select
                                                name="atbId"
                                                value={newAttribute.atbId}
                                                onChange={(e) => handleInputChange(e, setNewAttribute, "newAttribute")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            >
                                                <option value="">Select an attribute</option>
                                                {attributes.map((attr) => (
                                                    <option key={attr.atbId} value={attr.atbId}>{attr.atbName}</option>
                                                ))}
                                            </select>

                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Attribute Value</label>
                                            <input
                                                type="text"
                                                name="prodAtbValue"
                                                value={newAttribute.prodAtbValue}
                                                onChange={(e) => handleInputChange(e, setNewAttribute, "newAttribute")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            />
                                        </div>
                                        <div className="col-span-2">
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Attribute Description</label>
                                            <input
                                                type="text"
                                                name="atbDescription"
                                                value={newAttribute.atbDescription}
                                                onChange={(e) => handleInputChange(e, setNewAttribute, "newAttribute")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            />
                                        </div>
                                        <Button
                                            type="button"
                                            onClick={handleAddAttribute}
                                            className="bg-green-500 text-white hover:bg-green-600 mt-2 px-4 py-2 rounded-md btn-green"
                                        >
                                            Add Attribute
                                        </Button>
                                        {newProductTypeAttributes.length > 0 && (
                                            <div className="col-span-2 mt-4">
                                                <h4 className="text-md font-medium text-gray-700">Added Attributes:</h4>
                                                <ul className="list-disc pl-5 mt-2 text-gray-800">
                                                    {newProductTypeAttributes.map((attr, index) => (
                                                        <li key={index}>
                                                            {attributes.find(a => a.atbId === attr.atbId)?.atbName} - Value: {attr.prodAtbValue}
                                                        </li>
                                                    ))}
                                                </ul>
                                            </div>
                                        )}
                                        <div className="col-span-2">
                                            <strong>Current Attributes:</strong>
                                            {productTypeAttribute && productTypeAttribute.length > 0 ? (
                                                <table className="w-full mt-2 border-collapse border border-gray-300">
                                                    <thead>
                                                        <tr className="bg-gray-100">
                                                            <th className="border border-gray-300 px-4 py-2 text-left">Attribute Name</th>
                                                            <th className="border border-gray-300 px-4 py-2 text-left">Value</th>
                                                            <th className="border border-gray-300 px-4 py-2 text-left">Description</th>
                                                            <th className="border border-gray-300 px-4 py-2 text-left">Actions</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {productTypeAttribute
                                                            .filter(attr => attr.prodTypeId.prodTypeId === selectedEditProductType.prodTypeId)
                                                            .map((attr, index) => (
                                                                <tr key={index} className="hover:bg-gray-50">
                                                                    <td className="border border-gray-300 px-4 py-2">
                                                                        {attr.atbId.atbName || "Unknown Attribute"}
                                                                    </td>
                                                                    <td className="border border-gray-300 px-4 py-2">
                                                                        <input
                                                                            type="text"
                                                                            value={editedAttributeValues[attr.prodAtbId] ?? attr.prodAtbValue ?? ""}
                                                                            onChange={(e) => handleEditAttributeValue(attr.prodAtbId, e.target.value)}
                                                                            className="w-full border border-gray-300 rounded-md p-1"
                                                                        />
                                                                    </td>
                                                                    <td className="border border-gray-300 px-4 py-2">
                                                                        <input
                                                                            type="text"
                                                                            value={editedAttributeDescriptions[attr.prodAtbId] ?? attr.atbDescription ?? ""}
                                                                            onChange={(e) => handleEditAttributeDescription(attr.prodAtbId, e.target.value)}
                                                                            className="w-full border border-gray-300 rounded-md p-1"
                                                                        />
                                                                    </td>
                                                                    <td className="border border-gray-300 px-4 py-2 flex space-x-2">
                                                                        <Button
                                                                            type="button"
                                                                            onClick={() => handleUpdateAttribute(attr.prodAtbId, attr.prodAtbValue, attr.atbDescription)}
                                                                            className="bg-orange-500 text-white hover:bg-orange-600 px-2 py-1 rounded-md text-sm flex items-center"
                                                                        >
                                                                            Update <Wrench className="ml-1" />
                                                                        </Button>
                                                                        <Button
                                                                            type="button"
                                                                            onClick={() => handleDeleteAttribute(attr.prodAtbId)}
                                                                            className="bg-red-500 text-white hover:bg-red-600 px-2 py-1 rounded-md text-sm flex items-center"
                                                                        >
                                                                            <Trash2 />
                                                                        </Button>
                                                                    </td>
                                                                </tr>
                                                            ))}
                                                    </tbody>
                                                </table>
                                            ) : (
                                                <p className="mt-2">No attributes available</p>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            )}

                            {/* Variations - Shown only when a ProductType is selected */}
                            {selectedEditProductType && (
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h3 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Variations for Product Type</h3>
                                    <div className="grid grid-cols-2 gap-6">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Variation</label>
                                            <select
                                                name="variId"
                                                value={newVariation.variId}
                                                onChange={(e) => handleInputChange(e, setNewVariation, "newVariation")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            >
                                                <option value="">Select a variation</option>
                                                {variations.map((varItem) => (
                                                    <option key={varItem.variId} value={varItem.variId}>{varItem.variName}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Variation Value</label>
                                            <input
                                                type="text"
                                                name="prodTypeValue"
                                                value={newVariation.prodTypeValue}
                                                onChange={(e) => handleInputChange(e, setNewVariation, "newVariation")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            />
                                        </div>
                                        <Button
                                            type="button"
                                            onClick={handleAddVariation}
                                            className="bg-green-500 text-white hover:bg-green-600 mt-2 px-4 py-2 rounded-md btn-green"
                                        >
                                            Add Variation
                                        </Button>
                                        {newProductTypeVariations.length > 0 && (
                                            <div className="col-span-2 mt-4">
                                                <h4 className="text-md font-medium text-gray-700">Added Variations:</h4>
                                                <div className="mt-2 text-gray-800">
                                                    {/* Group variations by variId */}
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
                                        <div className="col-span-2">
                                            <strong>Current Variations:</strong>
                                            <div className="mt-2">
                                                {productTypeVariation && productTypeVariation.length > 0 ? (
                                                    <table className="w-full border-collapse border border-gray-300">
                                                        <thead>
                                                            <tr className="bg-gray-100">
                                                                <th className="border border-gray-300 px-4 py-2 text-left">Variation Name</th>
                                                                <th className="border border-gray-300 px-4 py-2 text-left">Value</th>
                                                                <th className="border border-gray-300 px-4 py-2 text-left">Actions</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            {[...new Set(
                                                                productTypeVariation
                                                                    .filter(vari => vari.prodTypeId.prodTypeId === selectedEditProductType.prodTypeId)
                                                                    .map(v => v.variId?.variId)
                                                            )].map(variId => {
                                                                const variationName = variations.find(v => v.variId === variId)?.variName || "Unknown Variation";
                                                                const groupedVariations = productTypeVariation.filter(
                                                                    vari => vari.variId?.variId === variId && vari.prodTypeId.prodTypeId === selectedEditProductType.prodTypeId
                                                                );

                                                                return groupedVariations.map((vari, index) => (
                                                                    <tr key={`${variId}-${index}`} className="hover:bg-gray-50">
                                                                        {index === 0 && (
                                                                            <td
                                                                                className="border border-gray-300 px-4 py-2 align-top"
                                                                                rowSpan={groupedVariations.length}
                                                                            >
                                                                                {variationName}
                                                                            </td>
                                                                        )}
                                                                        <td className="border border-gray-300 px-4 py-2">
                                                                            <input
                                                                                type="text"
                                                                                value={editedVariationValues[vari.prodTypeVariId] ?? vari.prodTypeValue ?? ""}
                                                                                onChange={(e) => handleEditVariationValue(vari.prodTypeVariId, e.target.value)}
                                                                                className="w-full border border-gray-300 rounded-md p-1"
                                                                            />
                                                                        </td>
                                                                        <td className="border border-gray-300 px-4 py-2 flex space-x-2">
                                                                            <Button
                                                                                type="button"
                                                                                onClick={() => handleUpdateVariation(vari.prodTypeVariId, vari.prodTypeValue)}
                                                                                className="bg-orange-500 text-white hover:bg-orange-600 px-2 py-1 rounded-md text-sm flex items-center"
                                                                            >
                                                                                Update <Wrench className="ml-1" />
                                                                            </Button>
                                                                            <Button
                                                                                type="button"
                                                                                onClick={() => handleDeleteVariation(vari.prodTypeVariId)}
                                                                                className="bg-red-500 text-white hover:bg-red-600 px-2 py-1 rounded-md text-sm flex items-center"
                                                                            >
                                                                                <Trash2 />
                                                                            </Button>
                                                                        </td>
                                                                    </tr>
                                                                ));
                                                            })}
                                                        </tbody>
                                                    </table>
                                                ) : (
                                                    <p className="mt-2">No variations available</p>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            )}
                            {/* Product Type Branches - Shown only when a ProductType is selected */}
                            {selectedEditProductType && (
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h3 className="text-lg font-semibold text-gray-800 mb-4 border-b border-gray-200 pb-2">Product Type Branches</h3>
                                    <div className="grid grid-cols-2 gap-6">
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Branch</label>
                                            <select
                                                name="brchId"
                                                value={newBranch.brchId}
                                                onChange={(e) => handleInputChange(e, setNewBranch, "newBranch")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            >
                                                <option value="">Select a branch</option>
                                                {branches.map((branch) => (
                                                    <option key={branch.brchId} value={branch.brchId}>{branch.brchName}</option>
                                                ))}
                                            </select>
                                        </div>
                                        <div>
                                            <label className="block text-sm font-medium text-gray-700 mb-2">Quantity</label>
                                            <input
                                                type="number"
                                                name="quantity"
                                                value={newBranch.quantity}
                                                onChange={(e) => handleInputChange(e, setNewBranch, "newBranch")}
                                                className="mt-1 block w-full border border-gray-300 rounded-md p-3"
                                            />
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
                                                            {branches.find(b => b.brchId === branch.brchId)?.brchName} - Quantity: {branch.quantity}
                                                        </li>
                                                    ))}
                                                </ul>
                                            </div>
                                        )}
                                        <div className="col-span-2">
                                            <strong>Current Branches:</strong>

                                            {/* Thanh tìm kiếm */}
                                            <div className="mt-2 mb-4">
                                                <input
                                                    type="text"
                                                    placeholder="Search branches by name..."
                                                    className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                                    value={branchSearchTerm}
                                                    onChange={(e) => setBranchSearchTerm(e.target.value)}
                                                />
                                            </div>

                                            {/* Danh sách Current Branches dưới dạng table */}
                                            {filteredBranches && filteredBranches.length > 0 ? (
                                                <table className="w-full border-collapse border border-gray-300">
                                                    <thead>
                                                        <tr className="bg-gray-100">
                                                            <th className="border border-gray-300 px-4 py-2 text-left">Branch Name</th>
                                                            <th className="border border-gray-300 px-4 py-2 text-left">Quantity</th>
                                                            <th className="border border-gray-300 px-4 py-2 text-left">Actions</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        {filteredBranches.map((branch, index) => (
                                                            <tr key={index} className="hover:bg-gray-50">
                                                                <td className="border border-gray-300 px-4 py-2">
                                                                    {branch.brchId.brchName || "Unknown Branch"}
                                                                </td>
                                                                <td className="border border-gray-300 px-4 py-2">
                                                                    <input
                                                                        type="number"
                                                                        value={editedBranchQuantities[branch.prodTypeBrchId] ?? branch.quantity ?? ""}
                                                                        onChange={(e) => handleEditBranchQuantity(branch.prodTypeBrchId, e.target.value)}
                                                                        className="w-full border border-gray-300 rounded-md p-1"
                                                                    />
                                                                </td>
                                                                <td className="border border-gray-300 px-4 py-2 flex space-x-2">
                                                                    <Button
                                                                        type="button"
                                                                        onClick={() => handleUpdateBranch(branch.prodTypeBrchId, branch.quantity)}
                                                                        className="bg-orange-500 text-white hover:bg-orange-600 px-2 py-1 rounded-md text-sm flex items-center"
                                                                    >
                                                                        Update <Wrench className="ml-1" />
                                                                    </Button>
                                                                    <Button
                                                                        type="button"
                                                                        onClick={() => handleDeleteBranch(branch.prodTypeBrchId)}
                                                                        className="bg-red-500 text-white hover:bg-red-600 px-2 py-1 rounded-md text-sm flex items-center"
                                                                    >
                                                                        <Trash2 />
                                                                    </Button>
                                                                </td>
                                                            </tr>
                                                        ))}
                                                    </tbody>
                                                </table>
                                            ) : (
                                                <p className="mt-2">No branches available</p>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            )}
                            {/* New Product Types - Shown only when a ProductType is selected */}
                            {selectedEditProductType && newProductTypes.length > 0 && (
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h4 className="text-md font-medium text-gray-700 mb-2">New Product Types to Add:</h4>
                                    <ul className="list-disc pl-5 text-gray-800">
                                        {newProductTypes.map((pt, index) => (
                                            <li key={index}>{pt.prodTypeName} - Price: {pt.prodTypePrice}, Unit: {pt.unit}</li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProductEditModal;