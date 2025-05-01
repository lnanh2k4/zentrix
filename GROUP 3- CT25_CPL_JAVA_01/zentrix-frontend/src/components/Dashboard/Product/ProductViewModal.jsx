import { useState } from 'react';
import { Button } from '@/components/ui/button'; // Assuming you're using Shadcn Button
import { X } from 'lucide-react'; // Icon for closing the modal from Lucide
import ImageGallery from './ImageGallery'; // Import the ImageGallery component
import "./ProductTable.css";
const ProductViewModal = ({
    selectedProduct,
    productTypes,
    productTypeBranch,
    productTypeAttribute,
    productTypeVariation,
    imagePreviews,
    isLoadingProductTypes,
    handleProductTypeSelect,
    handleCloseViewModal,
    selectedProductType,
}
) => {
    const [branchSearchTerm, setBranchSearchTerm] = useState("");
    const filteredBranches = productTypeBranch
        ? productTypeBranch
            .filter((branch) =>
                selectedProductType && branch.prodTypeId?.prodTypeId === selectedProductType.prodTypeId
            )
            .filter((branch) =>
                branch.brchId?.brchName
                    ?.toLowerCase()
                    .includes(branchSearchTerm.toLowerCase())
            )
        : [];
    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <div className="fixed inset-0 bg-black opacity-50" onClick={handleCloseViewModal}></div>
            {/* Modal content */}
            <div className="fixed inset-0 flex items-center justify-center z-100">

                <div className="bg-white rounded-lg shadow-lg w-full max-w-[80rem] max-h-[95vh] flex overflow-hidden ">
                    {/* Sidebar for Product Type Selection */}
                    <div className="bg-gray-50 border-r border-gray-200 p-5 overflow-y-auto fixed-sidebar">
                        <h3 className="text-lg font-semibold text-blue-50 mb-4">Product Types</h3>
                        {isLoadingProductTypes ? (
                            <p className="text-gray-600">Loading product types...</p>
                        ) : productTypes.length > 0 ? (
                            <div className="space-y-2">
                                {/* Mục mặc định "Select a Product Type" - Không cho nhấn */}
                                <div
                                    className={`p-3 rounded-md transition-all flex items-center justify-between gap-4 ${!selectedProductType ? "bg-blue-100 text-blue-800" : "bg-white text-gray-400"
                                        }`}
                                >
                                    <span className="flex-1">Select a Product Type</span>
                                </div>

                                {/* Danh sách product types */}
                                {productTypes.map((pt) => (
                                    <div
                                        key={pt.prodTypeId}
                                        className={`p-3 rounded-md cursor-pointer transition-all flex items-center justify-between gap-4 ${selectedProductType?.prodTypeId === pt.prodTypeId
                                            ? "bg-blue-100 text-blue-800"
                                            : "bg-white hover:bg-gray-100"
                                            }`}
                                        onClick={() => handleProductTypeSelect(pt.prodTypeId)}
                                    >
                                        {/* Thông tin sản phẩm */}
                                        <div className="flex-1 truncate">
                                            <span className="font-medium">ID: {pt.prodTypeId}</span> -{" "}
                                            <span className="font-medium">{pt.prodTypeName}</span> -{" "}
                                            <span>{pt.prodTypePrice.toLocaleString()} VND</span>
                                        </div>

                                        {/* Trạng thái */}
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
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p className="text-gray-600">No product types available for this product.</p>
                        )}
                    </div>

                    {/* Main Content */}
                    <div className="w-2/3 p-6 overflow-y-auto main-content">
                        {/* Header */}
                        <div className="flex justify-between items-center mb-6">
                            <h2 className="text-2xl font-bold text-white">
                                View Product: {selectedProduct.prodName}
                            </h2>
                            <Button
                                type="button"
                                variant="outline"
                                className="text-black border-none px-6 py-2 rounded-md"
                                onClick={handleCloseViewModal}
                            >
                                X
                            </Button>
                        </div>

                        {/* Product Details */}
                        <div className="grid grid-cols-2 gap-6 mb-8">
                            <div>
                                <p className="text-sm text-gray-600">
                                    <strong className="text-gray-800">Category:</strong>{" "}
                                    {selectedProduct.cateId.cateName || "N/A"}
                                </p>
                                <p className="text-sm text-gray-600">
                                    <strong className="text-gray-800">Supplier:</strong>{" "}
                                    {selectedProduct.suppId.suppName || "N/A"}
                                </p>
                                <p className="text-sm text-gray-600">
                                    <strong className="text-gray-800">Description:</strong>{" "}
                                    {selectedProduct.description || "N/A"}
                                </p>
                            </div>
                            <div>
                                <p className="text-sm text-gray-600">
                                    <strong className="text-gray-800">VAT:</strong> {selectedProduct.vat}%
                                </p>
                                <p className="text-sm text-gray-600">
                                    <strong className="text-gray-800">Status:</strong>{" "}
                                    <span
                                        className={`inline-block px-2 py-1 rounded text-xs font-medium  ${selectedProduct.status === 1
                                            ? "text-green-600 bg-green-100 "
                                            : selectedProduct.status === 0
                                                ? "text-red-600 bg-red-100"
                                                : "text-gray-600 bg-gray-100"
                                            }`}
                                    >
                                        {selectedProduct.status === 1
                                            ? "Available"
                                            : selectedProduct.status === 0
                                                ? "Unavailable"
                                                : "N/A"}
                                    </span>
                                </p>
                            </div>
                        </div>

                        {/* Selected Product Type Details */}
                        {selectedProductType && (
                            <div className="space-y-6">
                                {/* Product Type Info */}
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h4 className="text-lg font-extrabold text-gray-800 mb-4 vip-text-gradient">
                                        {selectedProductType.prodTypeName} ( ID:{selectedProductType.prodTypeId} )
                                    </h4>
                                    <div className="grid grid-cols-2 gap-4  text-sm text-gray-600">
                                        <div>
                                            <p>
                                                <strong className="font-medium text-gray-800">Price:</strong>{" "}
                                                {selectedProductType.prodTypePrice.toLocaleString()} VND
                                            </p>
                                            <p>
                                                <strong className="font-medium text-gray-800">Unit:</strong>{" "}
                                                {selectedProductType.unit}
                                            </p>
                                        </div>
                                        <div>
                                            <p>
                                                <strong className="font-medium text-gray-800">Unit Price:</strong>{" "}
                                                {selectedProductType.unitPrice.toLocaleString()} VND
                                            </p>
                                            <p>
                                                <strong className="font-medium text-gray-800 ">Status:</strong>{" "}
                                                <span
                                                    className={`inline-block px-2 py-1 rounded text-xs font-medium ${selectedProductType.status === 1
                                                        ? "text-green-600 bg-green-100"
                                                        : "text-red-600 bg-red-100"
                                                        }`}
                                                >
                                                    {selectedProductType.status === 1 ? "Available" : "Unavailable"}
                                                </span>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                                {/* Images */}
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm ">
                                    <h5 className="text-md font-semibold text-gray-800 mb-4 ">Images</h5>
                                    <ImageGallery imagePreviews={imagePreviews} />
                                </div>


                                {/* Attributes */}
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h5 className="text-md font-semibold text-gray-800 mb-4">Attributes</h5>
                                    {productTypeAttribute && productTypeAttribute.length > 0 ? (
                                        <ul className="space-y-2">
                                            {productTypeAttribute
                                                .filter(
                                                    (attr) => attr.prodTypeId.prodTypeId === selectedProductType.prodTypeId
                                                )
                                                .map((attr, index) => (
                                                    <li
                                                        key={index}
                                                        className="highlight-underline flex justify-between items-center p-3 bg-gray-50 rounded-md text-sm text-gray-600"
                                                    >
                                                        <span className="font-medium text-gray-800">
                                                            {attr.atbId.atbName || "Unknown Attribute"}
                                                        </span>
                                                        <span className="text-gray-600">
                                                            {attr.prodAtbValue}{" "}
                                                            <span className="text-gray-500">
                                                                ({attr.atbDescription || "N/A"})
                                                            </span>
                                                        </span>
                                                    </li>
                                                ))}
                                        </ul>
                                    ) : (
                                        <p className="text-sm text-gray-600">No attributes available</p>
                                    )}
                                </div>
                                {/* Variations */}
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <h5 className="text-md font-semibold text-gray-800 mb-4">Variations</h5>
                                    {productTypeVariation && productTypeVariation.length > 0 ? (
                                        <div className="space-y-4">
                                            {[...new Set(
                                                productTypeVariation
                                                    .filter(vari => vari.prodTypeId.prodTypeId === selectedProductType.prodTypeId)
                                                    .map(v => v.variId?.variId)
                                            )].map(variId => {
                                                const variationName = productTypeVariation.find(v => v.variId?.variId === variId)?.variId?.variName || "Unknown Variation";
                                                const groupedVariations = productTypeVariation.filter(
                                                    vari => vari.variId?.variId === variId && vari.prodTypeId.prodTypeId === selectedProductType.prodTypeId
                                                );

                                                return (
                                                    <div key={variId}>
                                                        <h4 className="font-medium text-gray-800 mb-2">{variationName}</h4>
                                                        <ul className="space-y-2">
                                                            {groupedVariations.map((varItem, index) => (
                                                                <li
                                                                    key={index}
                                                                    className="highlight-underline flex justify-between items-center p-3 bg-gray-50 rounded-md text-sm text-gray-600"
                                                                >
                                                                    <span className="text-gray-600">{varItem.prodTypeValue}</span>
                                                                </li>
                                                            ))}
                                                        </ul>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    ) : (
                                        <p className="text-sm text-gray-600">No variations available</p>
                                    )}
                                </div>
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm ">
                                    <h5 className="text-md font-semibold text-gray-800 mb-4 ">Branches</h5>

                                    {/* Thanh tìm kiếm */}
                                    <div className="mb-4">
                                        <input
                                            type="text"
                                            placeholder="Search branches by name..."
                                            className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                            value={branchSearchTerm} // State để lưu từ khóa tìm kiếm
                                            onChange={(e) => setBranchSearchTerm(e.target.value)} // Cập nhật từ khóa khi người dùng nhập
                                        />
                                    </div>

                                    {/* Danh sách Branches */}
                                    {filteredBranches && filteredBranches.length > 0 ? (
                                        <ul className="space-y-2">
                                            {filteredBranches.map((branch, index) => (
                                                <li
                                                    key={index}
                                                    className="highlight-underline flex justify-between items-center p-3 bg-gray-50 rounded-md text-sm text-gray-600"
                                                >
                                                    <span className="font-medium text-gray-800">
                                                        {branch.brchId.brchName || "Unknown Branch"}
                                                    </span>
                                                    <span className="text-gray-600 ">Quantity: {branch.quantity}</span>
                                                </li>
                                            ))}
                                        </ul>
                                    ) : (
                                        <p className="text-sm text-gray-600">No branches available</p>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div >
    );
};

export default ProductViewModal;