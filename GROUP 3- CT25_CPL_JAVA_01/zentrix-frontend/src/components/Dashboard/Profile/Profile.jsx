import React, { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { editStaff, getAllBranches, getAllRoles, getStaffByUsername, logout } from "@/context/ApiContext";
import ChangePassword from "./ChangePassword";
import { useUserInfo } from "@/services/InfoService";
import { showNotification } from "../NotificationPopup";
import { useNavigate } from "react-router-dom";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { showConfirm } from "../ConfirmPopup";

const Profile = () => {
    const navigate = useNavigate();
    const [isEditing, setIsEditing] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false);
    const [staffData, setStaffData] = useState({});
    const [initialStaffData, setInitialStaffData] = useState({});
    const [errors, setErrors] = useState({});
    const [branches, setBranches] = useState([]);
    const [roles, setRoles] = useState([]);
    const { user, loading: userLoading, error: userError } = useUserInfo();

    const BRANCH_STATUS_ACTIVE = 1;
    const ROLE_CUSTOMER = 2;
    const ROLE_GUEST = 1;

    useEffect(() => {
        if (!userLoading && user) {
            const username = user?.content?.username || user?.username;
            if (!username) {
                console.error("No username found in user object");
                navigate("/login");
            } else {
                fetchStaff(username);
                fetchBranches();
                fetchRoles();
            }
        }
    }, [user, userLoading]);

    const fetchStaff = async (username) => {
        try {
            setIsLoading(true);
            const response = await getStaffByUsername(username);
            console.log("API Response:", response);
            if (response.success && response.content) {
                const staff = response.content;
                const staffInfo = {
                    username: staff.userId.username || "",
                    email: staff.userId.email || "",
                    firstName: staff.userId.firstName || "",
                    lastName: staff.userId.lastName || "",
                    dob: staff.userId.dob ? new Date(staff.userId.dob) : null,
                    phone: staff.userId.phone || "",
                    address: staff.userId.address || "",
                    sex: staff.userId.sex !== undefined ? String(staff.userId.sex) : "",
                    companyName: staff.userId.companyName || "",
                    taxCode: staff.userId.taxCode || "",
                    brchId: staff.brchId?.brchId || "",
                    roleId: staff.userId.roleId?.roleId || "",
                    staffId: staff.staffId || "",
                    userId: staff.userId.userId || "",
                };
                setStaffData(staffInfo);
                setInitialStaffData(staffInfo);
            } else {
                console.error("API returned unsuccessful response:", response.message);
                setStaffData({});
                setInitialStaffData({});
            }
        } catch (error) {
            console.error("Error fetching staff:", error.message);
            if (error.message === "Unauthorized: Invalid credentials") {
                navigate("/login");
            }
            setStaffData({});
            setInitialStaffData({});
        } finally {
            setIsLoading(false);
        }
    };

    const fetchBranches = async () => {
        try {
            const response = await getAllBranches();
            if (response.success) {
                const activeBranches = response.content.filter((branch) => branch.status === BRANCH_STATUS_ACTIVE);
                setBranches(activeBranches);
            }
            console.log("Fetch Branch: ", response);
        } catch (error) {
            console.error("Error fetching branches:", error.message);
        }
    };

    const fetchRoles = async () => {
        try {
            const response = await getAllRoles();
            if (response.success) {
                const activeRoles = response.content.filter(
                    (role) => role.roleId !== ROLE_CUSTOMER && role.roleId !== ROLE_GUEST
                );
                setRoles(activeRoles);
            }
            console.log("Fetch Role: ", response);
        } catch (error) {
            console.error("Error fetching roles:", error.message);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setStaffData({ ...staffData, [name]: value });
        if (errors[name]) setErrors({ ...errors, [name]: null });
    };

    const handleDateChange = (date) => {
        setStaffData({ ...staffData, dob: date });
        if (errors.dob) setErrors({ ...errors, dob: null });
    };

    const validateForm = () => {
        const newErrors = {};

        if (!staffData.firstName) {
            newErrors.firstName = "First Name is required";
        } else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(staffData.firstName)) {
            newErrors.firstName = "First Name can only contain Vietnamese letters, '-', or '_'";
        }

        if (!staffData.lastName) {
            newErrors.lastName = "Last Name is required";
        } else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(staffData.lastName)) {
            newErrors.lastName = "Last Name can only contain Vietnamese letters, '-', or '_'";
        }

        if (!staffData.email) {
            newErrors.email = "Email is required";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(staffData.email)) {
            newErrors.email = "Invalid email address";
        }

        if (staffData.taxCode && !/^([A-Za-z]{1,3})?[0-9]{10,13}$/.test(staffData.taxCode)) {
            newErrors.taxCode = "Tax Code must be 10-13 digits, optionally preceded by a country code (1-3 letters)";
        }

        if (!/^[A-Za-zÀ-ỹ0-9\s\-_]*$/.test(staffData.companyName)) {
            newErrors.companyName = "Company name can only contain Vietnamese letters, numbers, spaces, '-', or '_'";
        }
        if (!staffData.dob) {
            newErrors.dob = "Date of Birth is required";
        }

        if (!staffData.phone) {
            newErrors.phone = "Phone is required";
        } else if (!/^[0-9]{0,15}$/.test(staffData.phone)) {
            newErrors.phone = "Phone must be a number up to 15 digits";
        }

        if (!staffData.address) {
            newErrors.address = "Address is required";
        }

        if (!staffData.sex) {
            newErrors.sex = "Sex is required";
        }

        if (!staffData.brchId) {
            newErrors.brchId = "Branch is required";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        try {
            const payload = {
                ...staffData,
                sex: parseInt(staffData.sex),
                brchId: parseInt(staffData.brchId),
                roleId: parseInt(staffData.roleId),
                staffId: parseInt(staffData.staffId),
                userId: parseInt(staffData.userId),
                dob: staffData.dob ? staffData.dob.toISOString() : null,
            };

            // Kiểm tra nếu email thay đổi
            if (staffData.email !== initialStaffData.email) {
                const confirmUpdate = await showConfirm("Changing your email will log you out and require you to log in again. Do you want to proceed?", 'fail')
                if (!confirmUpdate) return;
            }

            const response = await editStaff(payload);
            setIsEditing(false);
            setInitialStaffData(staffData);
            showNotification("Profile updated successfully", 3000, "complete");

            // Nếu email thay đổi, đăng xuất và chuyển hướng
            if (staffData.email !== initialStaffData.email) {
                await logout()
                navigate("/login");
            }
        } catch (error) {
            console.error("Error updating profile:", error.message);
            if (error.message === "Unauthorized: Invalid credentials") {
                navigate("/login");
            } else {
                showNotification(
                    `Failed to update profile: ${error.response?.data?.content || error.message}`,
                    3000,
                    "fail"
                );
            }
        }
    };

    const handleCancel = () => {
        const username = user?.content?.username || user?.username;
        if (username) {
            fetchStaff(username);
        }
        setIsEditing(false);
        setErrors({});
    };

    if (userLoading) return <div>Loading user info...</div>;
    if (userError || !user) return <div>Failed to load user info or not logged in.</div>;
    if (isLoading) return <div>Loading staff data...</div>;

    return (
        <div className="bg-white w-full p-4 shadow-md rounded-lg overflow-x-auto">
            <div className="p-6 max-w-4xl mx-auto">
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Staff ID</label>
                            <input
                                type="text"
                                value={staffData.staffId || "Not specified"}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100"
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Username</label>
                            <input
                                type="text"
                                name="username"
                                value={staffData.username || ""}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100"
                                disabled
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">First Name</label>
                            <input
                                type="text"
                                name="firstName"
                                value={staffData.firstName || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            />
                            {errors.firstName && <p className="text-red-500 text-sm mt-1">{errors.firstName}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Last Name</label>
                            <input
                                type="text"
                                name="lastName"
                                value={staffData.lastName || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            />
                            {errors.lastName && <p className="text-red-500 text-sm mt-1">{errors.lastName}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Phone</label>
                            <input
                                type="text"
                                name="phone"
                                value={staffData.phone || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            />
                            {errors.phone && <p className="text-red-500 text-sm mt-1">{errors.phone}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Sex</label>
                            <select
                                name="sex"
                                value={staffData.sex || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            >
                                <option value="">Select sex</option>
                                <option value="1">Male</option>
                                <option value="0">Female</option>
                            </select>
                            {errors.sex && <p className="text-red-500 text-sm mt-1">{errors.sex}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Email</label>
                            <input
                                type="text"
                                name="email"
                                value={staffData.email || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            />
                            {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Date of Birth</label>
                            <DatePicker
                                selected={staffData.dob}
                                onChange={handleDateChange}
                                dateFormat="dd/MM/yyyy"
                                className="block w-105 border border-gray-300 rounded-md p-2"
                                placeholderText="Select date"
                                disabled={!isEditing}
                            />
                            {errors.dob && <p className="text-red-500 text-sm mt-1">{errors.dob}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Branch</label>
                            <select
                                name="brchId"
                                value={staffData.brchId || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            >
                                <option value="">Select branch</option>
                                {branches.map((branch) => (
                                    <option key={branch.brchId} value={branch.brchId}>
                                        {branch.brchName}
                                    </option>
                                ))}
                            </select>
                            {errors.brchId && <p className="text-red-500 text-sm mt-1">{errors.brchId}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Role</label>
                            <select
                                name="roleId"
                                value={staffData.roleId || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled
                            >
                                <option value="">Select role</option>
                                {roles.map((role) => (
                                    <option key={role.roleId} value={role.roleId}>
                                        {role.roleName}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="col-span-2">
                            <label className="block text-sm font-medium text-gray-700">Address</label>
                            <input
                                type="text"
                                name="address"
                                value={staffData.address || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            />
                            {errors.address && <p className="text-red-500 text-sm mt-1">{errors.address}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Company Name</label>
                            <input
                                type="text"
                                name="companyName"
                                value={staffData.companyName || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            />
                            {errors.companyName && <p className="text-red-500 text-sm mt-1">{errors.companyName}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700">Tax Code</label>
                            <input
                                type="text"
                                name="taxCode"
                                value={staffData.taxCode || ""}
                                onChange={handleInputChange}
                                className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                disabled={!isEditing}
                            />
                            {errors.taxCode && <p className="text-red-500 text-sm mt-1">{errors.taxCode}</p>}
                        </div>
                    </div>

                    <div className="flex justify-center gap-4">
                        {!isEditing ? (
                            <>
                                <Button
                                    type="button"
                                    onClick={() => setIsEditing(true)}
                                    className="bg-blue-500 text-white hover:bg-blue-600"
                                >
                                    Edit
                                </Button>
                                <Button
                                    type="button"
                                    onClick={() => setIsChangePasswordOpen(true)}
                                    variant="outline"
                                >
                                    Change Password
                                </Button>
                            </>
                        ) : (
                            <>
                                <Button type="button" onClick={handleCancel} variant="outline">
                                    Cancel
                                </Button>
                                <Button type="submit" className="bg-blue-500 text-white hover:bg-blue-600">
                                    Save
                                </Button>
                            </>
                        )}
                    </div>
                </form>

                <ChangePassword
                    isOpen={isChangePasswordOpen}
                    onClose={() => setIsChangePasswordOpen(false)}
                    userId={staffData.userId}
                />
            </div>
        </div>
    );
};

export default Profile;