import React from "react";
import { Button } from "@/components/ui/button";
import DatePicker from "react-datepicker"; // Import react-datepicker
import "react-datepicker/dist/react-datepicker.css"; // Import CSS của react-datepicker

const ViewCustomerDetail = ({ isOpen, onClose, customer }) => {
    // Nếu không có dữ liệu khách hàng, sử dụng default
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

    // Sử dụng dữ liệu từ props customer nếu có, nếu không thì dùng default
    const customerData = customer || defaultCustomerData;

    // Xử lý giá trị sex để hiển thị
    const getSexLabel = (sex) => {
        if (sex === 1 || sex === "1") return "Male";
        if (sex === 0 || sex === "0") return "Female";
        return "Not specified";
    };

    // Chuyển đổi dob thành Date object nếu có giá trị
    const dobDate = customerData.dob ? new Date(customerData.dob) : null;

    const handleClose = () => {
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-xl">
                <h2 className="text-lg font-semibold text-left mb-6">View Customer Detail</h2>

                <div className="space-y-5">
                    <div className="grid grid-cols-3 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">First Name</label>
                            <input
                                type="text"
                                value={customerData.firstName}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Last Name</label>
                            <input
                                type="text"
                                value={customerData.lastName}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Sex</label>
                            <input
                                type="text"
                                value={getSexLabel(customerData.sex)}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Username</label>
                            <input
                                type="text"
                                value={customerData.username}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Phone</label>
                            <input
                                type="text"
                                value={customerData.phone}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Email</label>
                            <input
                                type="text"
                                value={customerData.email}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Date of Birth</label>
                            <DatePicker
                                selected={dobDate}
                                dateFormat="dd/MM/yyyy" // Định dạng ngày/tháng/năm
                                className="mt-1 w-65 border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                    </div>
                    <div>
                        <label className="block text-xs font-medium text-gray-600">Address</label>
                        <input
                            type="text"
                            value={customerData.address}
                            className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm bg-gray-100"
                            disabled
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Company Name</label>
                            <input
                                type="text"
                                value={customerData.companyName || "Not specified"}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Tax Code</label>
                            <input
                                type="text"
                                value={customerData.taxCode || "Not specified"}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                    </div>

                    <div className="flex justify-center gap-4 mt-6">
                        <Button type="button" variant="outline" onClick={handleClose} className="px-4 py-1 text-sm">
                            Close
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ViewCustomerDetail;