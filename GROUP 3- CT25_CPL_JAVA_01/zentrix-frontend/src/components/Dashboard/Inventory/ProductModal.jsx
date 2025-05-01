import { Button } from "@/components/ui/button";
import ImageGallery from "../Product/ImageGallery";
import { getProductTypeById, getProductTypes } from "@/context/ApiContext";
import { useEffect, useState } from "react";

const ProductModal = ({ isOpen, onClose, selectedProduct, imagePreviews }) => {
    const [productDetail, setProductDetail] = useState(null);

    useEffect(() => {
        if (!isOpen || !selectedProduct || !selectedProduct.prodTypeId?.prodTypeId) return;
    }, [isOpen, selectedProduct]);

    if (!isOpen || !selectedProduct) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <div className="absolute inset-0 bg-black opacity-50" onClick={onClose}></div>
            <div className="fixed bg-white p-6 rounded-lg shadow-lg max-w-md w-full">
                <h2 className="text-xl font-bold mb-4">Product Details</h2>
                <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <p><strong>ProductType ID:</strong> {selectedProduct.prodTypeId?.prodTypeId || "N/A"}</p>
                        <p>
                            <span
                                className={
                                    selectedProduct.prodTypeId?.prodId?.status === 1
                                        ? "text-green-600 bg-green-100 px-2 py-1 rounded"
                                        : selectedProduct.prodTypeId?.prodId?.status === 0
                                            ? "text-red-600 bg-red-100 px-2 py-1 rounded"
                                            : "text-gray-600"
                                }
                            >
                            </span>
                        </p>
                        <p><strong>Name:</strong> {selectedProduct.prodTypeId?.prodTypeName || "N/A"}</p>
                        <p>
                            <strong>Unit Price:</strong>{" "}
                            {selectedProduct.prodTypeId?.unitPrice?.toLocaleString() || "N/A"} VND/
                            {selectedProduct.prodTypeId?.unit || "N/A"}
                        </p>
                    </div>
                    <div className="space-y-2">
                        {selectedProduct.prodTypeId?.prodId?.cateId?.cateName && (
                            <p><strong>Category:</strong> {selectedProduct.prodTypeId.prodId.cateId.cateName}</p>
                        )}
                        {selectedProduct.prodTypeId?.prodId?.suppId?.suppName && (
                            <p><strong>Supplier:</strong> {selectedProduct.prodTypeId.prodId.suppId.suppName}</p>
                        )}
                        <p>
                            <strong>Status:</strong>{" "}
                            <span
                                className={
                                    selectedProduct.prodTypeId?.status === 1
                                        ? "text-green-600 bg-green-100 px-2 py-1 rounded"
                                        : selectedProduct.prodTypeId?.status === 0
                                            ? "text-red-600 bg-red-100 px-2 py-1 rounded"
                                            : "text-gray-600"
                                }
                            >
                                {selectedProduct.prodTypeId?.status === 1
                                    ? "Active"
                                    : selectedProduct.prodTypeId?.status === 0
                                        ? "Inactive"
                                        : "N/A"}
                            </span>
                        </p>
                    </div>
                </div>
                <div className="mt-4">
                    <ImageGallery imagePreviews={imagePreviews} />
                </div>
                <div className="mt-4 flex justify-end">
                    <Button
                        variant="outline"
                        onClick={onClose}
                        className="text-gray-600 hover:text-gray-800"
                    >
                        Cancel
                    </Button>
                </div>
            </div>
        </div>
    );
};

export default ProductModal;