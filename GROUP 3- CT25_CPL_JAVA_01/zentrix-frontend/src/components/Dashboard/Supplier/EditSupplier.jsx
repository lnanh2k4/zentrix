import { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";

const EditSupplier = ({ isOpen, onClose, onSubmit, supplier }) => {
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

    const handleInputChange = (e) => {
        const { name, value } = e.target;

        // Allow all input to be entered, validation will happen on submit
        if (name === "phone") {
            // Only allow digits for phone in real-time
            const allowedCharactersForPhone = /^[0-9]*$/;
            if (allowedCharactersForPhone.test(value)) {
                setSupplierData({ ...supplierData, [name]: value });
            }
        } else {
            setSupplierData({ ...supplierData, [name]: value });
        }
    };

    const validateForm = () => {
        const noSpecialCharsForName = /^[A-Za-zÀ-ỹ0-9\s]*$/;
        const noSpecialCharsForAddress = /^[A-Za-zÀ-ỹ0-9\s,]*$/;
        const onlyNumbersForPhone = /^[0-9]*$/;

        if (!supplierData.suppName.trim()) {
            alert("Supplier name is required.");
            return false;
        }
        if (!noSpecialCharsForName.test(supplierData.suppName)) {
            alert("Supplier name can only contain letters (including accented Vietnamese letters), numbers, and spaces.");
            return false;
        }
        if (!supplierData.email.trim()) {
            alert("Email is required.");
            return false;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(supplierData.email)) {
            alert("Invalid email format.");
            return false;
        }
        if (!supplierData.phone.trim()) {
            alert("Phone is required.");
            return false;
        }
        if (!onlyNumbersForPhone.test(supplierData.phone)) {
            alert("Phone number must contain only digits.");
            return false;
        }
        const phoneRegex = /^[0-9]{10,15}$/;
        if (!phoneRegex.test(supplierData.phone)) {
            alert("Phone number must be between 10 and 15 digits long.");
            return false;
        }
        if (supplierData.address && !noSpecialCharsForAddress.test(supplierData.address)) {
            alert("Address can only contain letters (including accented Vietnamese letters), numbers, spaces, and commas.");
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;
        try {
            const payload = {
                suppName: supplierData.suppName,
                email: supplierData.email,
                phone: supplierData.phone,
                address: supplierData.address || null,
            };
            const response = await axios.put(`http://localhost:6789/api/v1/dashboard/suppliers/${supplier.suppId}`, payload, {
                headers: { "Content-Type": "application/json" },
                withCredentials: true,
            });
            if (response.data.success) {
                onSubmit(response.data.content);
                setSupplierData(defaultSupplierData);
                onClose();
            } else {
                throw new Error(response.data.message || "Failed to update supplier");
            }
        } catch (error) {
            console.error("Error updating supplier:", error);
            alert("Failed to update supplier: " + (error.response?.data?.message || error.message));
        }
    };

    const handleCancel = () => {
        onClose();
   
    };

    if (!isOpen || !supplier) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black/30">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                <div className="flex justify-between items-center mb-4">
                    <div className="w-full"> {/* Bỏ text-center để tiêu đề căn trái */}
                        <h2 className="text-xl font-bold">Edit Supplier</h2>
                    </div>
                    <button onClick={onClose} className="text-gray-500 hover:text-gray-700 text-2xl leading-none" aria-label="Close modal">
                        ×
                    </button>
                </div>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="suppName" className="block text-sm font-medium text-gray-700">Supplier Name</label>
                        <input
                            id="suppName"
                            type="text"
                            name="suppName"
                            value={supplierData.suppName}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
                        <input
                            id="email"
                            type="email"
                            name="email"
                            value={supplierData.email}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="phone" className="block text-sm font-medium text-gray-700">Phone</label>
                        <input
                            id="phone"
                            type="text"
                            name="phone"
                            value={supplierData.phone}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="address" className="block text-sm font-medium text-gray-700">Address</label>
                        <input
                            id="address"
                            type="text"
                            name="address"
                            value={supplierData.address}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div className="flex justify-end gap-2">
                        <Button type="button" variant="outline" onClick={handleCancel}>
                            Cancel
                        </Button>
                        <Button type="submit" className="bg-blue-500 text-white hover:bg-blue-600">
                            Save
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditSupplier;