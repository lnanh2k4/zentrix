import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { editStaff, getAllBranches, getAllRoles } from "@/context/ApiContext";
import { showNotification } from "../NotificationPopup";
import DatePicker from "react-datepicker"; // Import react-datepicker
import "react-datepicker/dist/react-datepicker.css"; // Import CSS của react-datepicker

const EditStaff = ({ isOpen, onClose, onSubmit, staff }) => {
    const defaultStaffData = {
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
        brchId: "",
        roleId: "",
        staffId: "",
        userId: "",
    };

    const [staffData, setStaffData] = useState(defaultStaffData);
    const [errors, setErrors] = useState({});
    const [branches, setBranches] = useState([]);
    const [roles, setRoles] = useState([]);
    const BRANCH_STATUS_ACTIVE = 1;
    const ROLE_CUSTOMER = 2;
    const ROLE_GUEST = 1;

    // Khởi tạo dữ liệu khi component mở
    useEffect(() => {
        if (staff) {
            setStaffData({
                username: staff.userId.username || "",
                email: staff.userId.email || "",
                firstName: staff.userId.firstName || "",
                lastName: staff.userId.lastName || "",
                dob: staff.userId.dob ? new Date(staff.userId.dob) : null, // Chuyển thành Date object
                phone: staff.userId.phone || "",
                address: staff.userId.address || "",
                sex: staff.userId.sex !== undefined ? String(staff.userId.sex) : "",
                companyName: staff.userId.companyName || "",
                taxCode: staff.userId.taxCode || "",
                brchId: staff.brchId.brchId || "",
                roleId: staff.userId.roleId.roleId || "",
                staffId: staff.staffId || "",
                userId: staff.userId.userId || "",
            });
        }
        fetchBranches();
        fetchRoles();
    }, [staff]);

    const fetchBranches = async () => {
        try {
            const response = await getAllBranches();
            if (response.success) {
                const branchData = response.content || [];
                const activeBranches = branchData.filter(branch => branch.status === BRANCH_STATUS_ACTIVE);
                setBranches(activeBranches);
            } else {
                setBranches([]);
            }
        } catch (error) {
            console.error("Error fetching branches:", error);
            setBranches([]);
        }
    };

    const fetchRoles = async () => {
        try {
            const response = await getAllRoles();
            if (response.success) {
                const roleData = response.content || [];
                const activeRoles = roleData.filter(role => role.roleId !== ROLE_CUSTOMER && role.roleId !== ROLE_GUEST);
                setRoles(activeRoles);
            } else {
                setRoles([]);
            }
        } catch (error) {
            console.error("Error fetching roles:", error);
            setRoles([]);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setStaffData({ ...staffData, [name]: value });
        if (errors[name]) {
            setErrors({ ...errors, [name]: null });
        }
    };

    const handleDateChange = (date) => {
        setStaffData({ ...staffData, dob: date });
        if (errors.dob) {
            setErrors({ ...errors, dob: null });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!staffData.username) newErrors.username = "Username is required";
        else if (staffData.username.length < 3) newErrors.username = "Username must be at least 3 characters";

        if (!staffData.firstName) newErrors.firstName = "First Name is required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(staffData.firstName))
            newErrors.firstName = "First Name can only contain Vietnamese letters, '-', or '_'";
        if (!staffData.lastName) newErrors.lastName = "Last Name is required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(staffData.lastName))
            newErrors.lastName = "Last Name can only contain Vietnamese letters, '-', or '_'";

        if (!staffData.email) newErrors.email = "Email is required";
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(staffData.email)) newErrors.email = "Invalid email address";

        if (!staffData.dob) newErrors.dob = "Date of Birth is required";

        if (!staffData.phone) newErrors.phone = "Phone is required";
        else if (!/^[0-9]{0,15}$/.test(staffData.phone)) newErrors.phone = "Phone must be a 15-digit number";

        if (!staffData.address) newErrors.address = "Address is required";
        if (!staffData.sex) newErrors.sex = "Sex is required";
        if (!staffData.brchId) newErrors.brchId = "Branch is required";
        if (!staffData.roleId) newErrors.roleId = "Role is required";

        if (!staffData.taxCode) newErrors.taxCode = "Tax code is required";
        else if (staffData.taxCode && !/^([A-Za-z]{1,3})?[0-9]{10,13}$/.test(staffData.taxCode)) {
            newErrors.taxCode = "Tax Code must be 10-13 digits, optionally preceded by a country code (1-3 letters)";
        }
        if (!staffData.companyName) newErrors.companyName = "Company Name is required";
        else if (!/^[A-Za-zÀ-ỹ0-9\s\-_]*$/.test(staffData.companyName)) {
            newErrors.companyName = "Company name can only contain Vietnamese letters, numbers, spaces, '-', or '_'";
        }
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            const payload = {
                username: staffData.username,
                email: staffData.email,
                firstName: staffData.firstName,
                lastName: staffData.lastName,
                dob: staffData.dob ? staffData.dob.toISOString() : null, // Chuyển đổi Date thành ISO string
                phone: staffData.phone,
                address: staffData.address,
                sex: parseInt(staffData.sex),
                companyName: staffData.companyName,
                taxCode: staffData.taxCode,
                brchId: parseInt(staffData.brchId),
                roleId: parseInt(staffData.roleId),
                staffId: parseInt(staffData.staffId),
                userId: parseInt(staffData.userId),
            };

            const response = await editStaff(payload);
            onSubmit(response.result?.data || payload);
            setStaffData(defaultStaffData);
            setErrors({});
            showNotification("Edit staff successfully", 3000, 'complete');
            onClose();
        } catch (error) {
            console.error("Error updating staff:", error);
            showNotification("Failed to update staff: " + (error.response?.data?.content || error.message), 3000, 'fail');
        }
    };

    const handleCancel = () => {
        setStaffData(defaultStaffData);
        setErrors({});
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-xl">
                <h2 className="text-lg font-semibold text-left mb-6">Edit Staff</h2>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div className="grid grid-cols-5 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Staff ID</label>
                            <input
                                type="text"
                                value={staffData.staffId || "Not specified"}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm bg-gray-100"
                                disabled
                            />
                        </div>
                        <div className="col-span-2">
                            <label className="block text-xs font-medium text-gray-600">Username</label>
                            <input
                                type="text"
                                name="username"
                                value={staffData.username}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                                disabled
                            />
                            {errors.username && <p className="text-red-500 text-xs mt-1">{errors.username}</p>}
                        </div>
                        <div className="col-span-2">
                            <label className="block text-xs font-medium text-gray-600">Phone</label>
                            <input
                                type="text"
                                name="phone"
                                value={staffData.phone}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.phone && <p className="text-red-500 text-xs mt-1">{errors.phone}</p>}
                        </div>
                    </div>
                    <div className="grid grid-cols-3 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">First Name</label>
                            <input
                                type="text"
                                name="firstName"
                                value={staffData.firstName}
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
                                value={staffData.lastName}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.lastName && <p className="text-red-500 text-xs mt-1">{errors.lastName}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Sex</label>
                            <select
                                name="sex"
                                value={staffData.sex}
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
                            <label className="block text-xs font-medium text-gray-600">Email</label>
                            <input
                                type="text"
                                name="email"
                                value={staffData.email}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            />
                            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Date of Birth</label>
                            <DatePicker
                                selected={staffData.dob}
                                onChange={handleDateChange}
                                dateFormat="dd/MM/yyyy" // Định dạng ngày/tháng/năm
                                maxDate={new Date()} // Giới hạn tối đa là ngày hiện tại
                                className="mt-1 w-65 border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                                placeholderText="Select date"
                            />
                            {errors.dob && <p className="text-red-500 text-xs mt-1">{errors.dob}</p>}
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Branch</label>
                            <select
                                name="brchId"
                                value={staffData.brchId}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            >
                                <option value="">Select branch</option>
                                {branches.map((branch) => (
                                    <option key={branch.brchId} value={branch.brchId}>
                                        {branch.brchName}
                                    </option>
                                ))}
                            </select>
                            {errors.brchId && <p className="text-red-500 text-xs mt-1">{errors.brchId}</p>}
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-gray-600">Role</label>
                            <select
                                name="roleId"
                                value={staffData.roleId}
                                onChange={handleInputChange}
                                className="mt-1 w-full border border-gray-300 rounded-md p-1.5 text-sm focus:ring-1 focus:ring-blue-500"
                            >
                                <option value="">Select role</option>
                                {roles.map((role) => (
                                    <option key={role.roleId} value={role.roleId}>
                                        {role.roleName}
                                    </option>
                                ))}
                            </select>
                            {errors.roleId && <p className="text-red-500 text-xs mt-1">{errors.roleId}</p>}
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-medium text-gray-600">Address</label>
                        <input
                            type="text"
                            name="address"
                            value={staffData.address}
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
                                value={staffData.companyName}
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
                                value={staffData.taxCode}
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

export default EditStaff;