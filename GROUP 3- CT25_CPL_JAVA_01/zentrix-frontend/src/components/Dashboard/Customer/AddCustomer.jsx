import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { createCustomer } from "@/context/ApiContext";
import { showNotification } from "../NotificationPopup";
import DatePicker from "react-datepicker"; // Import react-datepicker
import "react-datepicker/dist/react-datepicker.css"; // Import CSS của react-datepicker

const AddCustomer = ({ isOpen, onClose, onSubmit }) => {
    const defaultCustomerData = {
        username: "",
        email: "",
        firstName: "",
        lastName: "",
        dob: null, // Thay đổi từ chuỗi "" thành null để phù hợp với DatePicker
        phone: "",
        address: "",
        sex: "",
        companyName: "",
        taxCode: "",
    };

    const [customerData, setCustomerData] = useState(defaultCustomerData);
    const [errors, setErrors] = useState({});

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCustomerData({ ...customerData, [name]: value });
        if (errors[name]) {
            setErrors({ ...errors, [name]: null });
        }
    };

    const handleDateChange = (date) => {
        setCustomerData({ ...customerData, dob: date });
        if (errors.dob) {
            setErrors({ ...errors, dob: null });
        }
    };

    const validateForm = () => {
        const newErrors = {};
        if (!customerData.username) newErrors.username = "Required";
        else if (customerData.username.length < 3) newErrors.username = "Min 3 chars";
        if (!customerData.firstName) newErrors.firstName = "Required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(customerData.firstName)) newErrors.firstName = "Invalid";
        if (!customerData.lastName) newErrors.lastName = "Required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(customerData.lastName)) newErrors.lastName = "Invalid";
        if (!customerData.email) newErrors.email = "Required";
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customerData.email)) newErrors.email = "Invalid";
        if (!customerData.dob) newErrors.dob = "Required";
        if (!customerData.phone) newErrors.phone = "Required";
        else if (!/^[0-9]{0,15}$/.test(customerData.phone)) newErrors.phone = "Invalid";
        if (!customerData.address) newErrors.address = "Required";
        if (!customerData.sex) newErrors.sex = "Required";
        if (customerData.taxCode && !/^([A-Za-z]{1,3})?[0-9]{10,13}$/.test(customerData.taxCode)) {
            newErrors.taxCode = "Tax Code must be 10-13 digits, optionally preceded by a country code (1-3 letters)";
        }
        if (!/^[A-Za-zÀ-ỹ0-9\s\-_]*$/.test(customerData.companyName)) {
            newErrors.companyName = "Company name can only contain Vietnamese letters, numbers, spaces, '-', or '_'";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        try {
            const payload = {
                username: customerData.username,
                email: customerData.email,
                firstName: customerData.firstName,
                lastName: customerData.lastName,
                dob: customerData.dob ? customerData.dob.toISOString() : null, // Chuyển đổi Date thành ISO string
                phone: customerData.phone,
                address: customerData.address,
                sex: parseInt(customerData.sex),
                companyName: customerData.companyName,
                taxCode: customerData.taxCode,
            };
            onSubmit((await createCustomer(payload)) || payload);
            setCustomerData(defaultCustomerData);
            setErrors({});
            showNotification("Customer created successfully", 3000, 'complete');
            onClose();
        } catch (error) {
            console.error("Create customer failed: " + (error.response?.data?.content || error.message));
            showNotification(
                `Create customer failed: ${error.response?.data?.content || error.message}`,
                3000,
                "fail"
            );
        }
    };

    const handleCancel = () => {
        setCustomerData(defaultCustomerData);
        setErrors({});
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-xl">
                <h2 className="text-lg font-semibold text-left mb-6">Create Customer</h2>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div className="grid grid-cols-3 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">First Name</label>
                            <input
                                type="text"
                                name="firstName"
                                value={customerData.firstName}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.firstName && <p className="text-red-500 text-xs mt-1">{errors.firstName}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Last Name</label>
                            <input
                                type="text"
                                name="lastName"
                                value={customerData.lastName}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.lastName && <p className="text-red-500 text-xs mt-1">{errors.lastName}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Sex</label>
                            <select
                                name="sex"
                                value={customerData.sex}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            >
                                <option value="">Select</option>
                                <option value="1">Male</option>
                                <option value="0">Female</option>
                            </select>
                            {errors.sex && <p className="text-red-500 text-xs mt-1">{errors.sex}</p>}
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Username</label>
                            <input
                                type="text"
                                name="username"
                                value={customerData.username}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.username && <p className="text-red-500 text-xs mt-1">{errors.username}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Phone</label>
                            <input
                                type="text"
                                name="phone"
                                value={customerData.phone}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.phone && <p className="text-red-500 text-xs mt-1">{errors.phone}</p>}
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Email</label>
                            <input
                                type="text"
                                name="email"
                                value={customerData.email}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Date of Birth</label>
                            <DatePicker
                                selected={customerData.dob}
                                onChange={handleDateChange}
                                dateFormat="dd/MM/yyyy" // Định dạng ngày/tháng/năm
                                maxDate={new Date()} // Giới hạn tối đa là ngày hiện tại
                                className="mt-1 w-65 border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                                placeholderText="Select date"
                            />
                            {errors.dob && <p className="text-red-500 text-xs mt-1">{errors.dob}</p>}
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-medium text-gray-600">Address</label>
                        <input
                            type="text"
                            name="address"
                            value={customerData.address}
                            onChange={handleInputChange}
                            className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-1 focus:ring-blue-500"
                        />
                        {errors.address && <p className="text-red-500 text-xs mt-1">{errors.address}</p>}
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Company Name</label>
                            <input
                                type="text"
                                name="companyName"
                                value={customerData.companyName}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.companyName && <p className="text-red-500 text-xs mt-1">{errors.companyName}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Tax Code</label>
                            <input
                                type="text"
                                name="taxCode"
                                value={customerData.taxCode}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.taxCode && <p className="text-red-500 text-xs mt-1">{errors.taxCode}</p>}
                        </div>
                    </div>

                    <div className="flex justify-center gap-4 mt-6">
                        <Button type="button" variant="outline" onClick={handleCancel} className="px-4 py-1 text-sm">
                            Cancel
                        </Button>
                        <Button type="submit" className="bg-blue-500 text-white hover:bg-blue-600 px-4 py-1 text-sm">
                            Save
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddCustomer;