import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";

const ViewSupplierDetail = ({ isOpen, onClose, supplier }) => {
    const defaultSupplierData = {
        suppName: "",
        email: "",
        phone: "",
        address: "",
    };

    const [supplierData, setSupplierData] = useState(defaultSupplierData);

    useEffect(() => {
        if (supplier && isOpen) {
            setSupplierData({
                suppName: supplier.suppName || "",
                email: supplier.email || "",
                phone: supplier.phone || "",
                address: supplier.address || "",
            });
        }
    }, [supplier, isOpen]);

    const handleClose = () => {
        onClose();
    };

    if (!isOpen || !supplier) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black/30">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                <div className="flex justify-between items-center mb-4">
                    <div className="w-full">
                        <h2 className="text-xl font-bold">Supplier Detail</h2>
                    </div>
                    <button 
                        onClick={onClose} 
                        className="text-gray-500 hover:text-gray-700 text-2xl leading-none" 
                        aria-label="Close modal"
                    >
                        ×
                    </button>
                </div>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Supplier Name</label>
                        <div className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100">
                            {supplierData.suppName || "N/A"}
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Email</label>
                        <div className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100">
                            {supplierData.email || "N/A"}
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Phone</label>
                        <div className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100">
                            {supplierData.phone || "N/A"}
                        </div>
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Address</label>
                        <div className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100">
                            {supplierData.address || "N/A"}
                        </div>
                    </div>
                    <div className="flex justify-end">
                        <Button 
                            type="button" 
                            variant="outline" 
                            onClick={handleClose}
                        >
                            Close
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ViewSupplierDetail;