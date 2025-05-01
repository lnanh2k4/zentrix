import React, { useState, useEffect } from "react";
import Sidebar from "@/components/ui/Sidebar";
import Header from "@/components/ui/Header";
import { Button } from "@/components/ui/button";
import axios from "axios";
import { motion } from "framer-motion";
import { showNotification } from "@/components/Dashboard/NotificationPopup";
import { editCustomer, getInfo, logout } from "@/context/ApiContext";
import ChangePassword from "./ChangePasswordPage";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import { useNavigate } from "react-router-dom"; // Thêm import useNavigate
import { showConfirm } from "@/components/Dashboard/ConfirmPopup";

// Hàm định dạng ngày thành "ngày/tháng/năm"
const formatDate = (isoDate) => {
    if (!isoDate) return "";
    const date = new Date(isoDate);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
};

// Fetch user info
const fetchUserInfo = async () => {
    try {
        const response = await getInfo();
        if (response.success && response.content) {
            return response.content;
        }
        return null;
    } catch (error) {
        console.error("Error fetching user info:", error.message);
        return null;
    }
};

const MyProfile = () => {
    const [isEditing, setIsEditing] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false);
    const [userInfo, setUserInfo] = useState({
        userId: null,
        username: "",
        role: "",
        firstName: "",
        lastName: "",
        phone: "",
        email: "",
        dob: "",
        address: "",
        sex: "",
        companyName: "",
        taxCode: "",
    });
    const [initialUserInfo, setInitialUserInfo] = useState(null);
    const [errors, setErrors] = useState({});
    const navigate = useNavigate(); // Khởi tạo useNavigate

    useEffect(() => {
        const loadData = async () => {
            setIsLoading(true);
            const user = await fetchUserInfo();
            if (user) {
                const userData = {
                    userId: user.userId || null,
                    username: user.username || "",
                    role: user.roleId?.roleName || "N/A",
                    firstName: user.firstName || "",
                    lastName: user.lastName || "",
                    phone: user.phone || "",
                    email: user.email || "",
                    dob: user.dob ? new Date(user.dob).toISOString().split("T")[0] : "",
                    address: user.address || "",
                    sex: user.sex !== undefined ? String(user.sex) : "",
                    companyName: user.companyName || "",
                    taxCode: user.taxCode || "",
                };
                setUserInfo(userData);
                setInitialUserInfo(userData);
            }
            setIsLoading(false);
        };
        loadData();
    }, []);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setUserInfo({ ...userInfo, [name]: value });
        if (errors[name]) setErrors({ ...errors, [name]: null });
    };

    const validateForm = () => {
        const newErrors = {};
        if (!userInfo.firstName) newErrors.firstName = "First Name is required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(userInfo.firstName))
            newErrors.firstName = "First Name can only contain Vietnamese letters, '-', or '_'";
        if (!userInfo.lastName) newErrors.lastName = "Last Name is required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(userInfo.lastName))
            newErrors.lastName = "Last Name can only contain Vietnamese letters, '-', or '_'";
        if (!userInfo.email) newErrors.email = "Email is required";
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userInfo.email)) newErrors.email = "Invalid email address";
        if (!userInfo.phone) newErrors.phone = "Phone is required";
        else if (!/^[0-9]{0,15}$/.test(userInfo.phone)) newErrors.phone = "Phone must be a number up to 15 digits";
        if (!userInfo.address) newErrors.address = "Address is required";
        if (!userInfo.sex) newErrors.sex = "Sex is required";
        if (!userInfo.dob) newErrors.dob = "Date of Birth is required";
        if (userInfo.taxCode && !/^([A-Za-z]{1,3})?[0-9]{10,13}$/.test(userInfo.taxCode)) {
            newErrors.taxCode = "Tax Code must be 10-13 digits, optionally preceded by a country code (1-3 letters)";
        }
        if (!/^[A-Za-zÀ-ỹ0-9\s\-_]*$/.test(userInfo.companyName)) {
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
                ...userInfo,
                sex: parseInt(userInfo.sex),
                dob: userInfo.dob ? new Date(userInfo.dob).toISOString() : null,
            };

            // Kiểm tra nếu email thay đổi
            if (userInfo.email !== initialUserInfo.email) {
                const confirmUpdate = await
                    showConfirm("Changing your email will log you out and require you to log in again. Do you want to proceed?", 'fail')
                if (!confirmUpdate) return; // Nếu người dùng hủy, thoát hàm
            }

            const response = await editCustomer(payload);
            if (response.success) {
                setIsEditing(false);
                setInitialUserInfo(userInfo);
                showNotification("Profile updated successfully", 3000, "complete");

                // Nếu email thay đổi, đăng xuất và chuyển hướng
                if (userInfo.email !== initialUserInfo.email) {
                    await logout()
                    navigate("/login")
                }
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

    const handleCancel = async () => {
        setIsLoading(true);
        const user = await fetchUserInfo();
        if (user) {
            const userData = {
                userId: user.userId || null,
                username: user.username || "",
                role: user.roleId?.roleName || "N/A",
                firstName: user.firstName || "",
                lastName: user.lastName || "",
                phone: user.phone || "",
                email: user.email || "",
                dob: user.dob ? new Date(user.dob).toISOString().split("T")[0] : "",
                address: user.address || "",
                sex: user.sex !== undefined ? String(user.sex) : "",
                companyName: user.companyName || "",
                taxCode: user.taxCode || "",
            };
            setUserInfo(userData);
            setInitialUserInfo(userData);
        }
        setErrors({});
        setIsEditing(false);
        setIsLoading(false);
    };

    if (isLoading) return <div>Loading profile data...</div>;

    return (
        <div className="home-container bg-[#f8f8fc] text-gray-900 min-h-screen flex flex-col" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="home-header h-20 bg-red-600 text-white flex items-center px-4 shadow-md w-full">
                <Header />
            </header>
            <div className="home-content flex flex-1 container mx-auto p-6 space-x-6">
                <Sidebar />
                <main className="flex-1 flex flex-col space-y-8">
                    <motion.div
                        className="home-profile bg-white rounded-lg shadow-md p-6"
                        initial={{ opacity: 0, x: 100 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.5 }}
                    >
                        <h1 className="text-2xl font-bold mb-6 text-[#8c1d6b]">My Profile</h1>
                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Username</label>
                                    <input
                                        type="text"
                                        name="username"
                                        value={userInfo.username}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100"
                                        disabled
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Role</label>
                                    <input
                                        type="text"
                                        name="role"
                                        value={userInfo.role}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100"
                                        disabled
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">First Name</label>
                                    <input
                                        type="text"
                                        name="firstName"
                                        value={userInfo.firstName}
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
                                        value={userInfo.lastName}
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
                                        value={userInfo.phone}
                                        onChange={handleInputChange}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                        disabled={!isEditing}
                                    />
                                    {errors.phone && <p className="text-red-500 text-sm mt-1">{errors.phone}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Email</label>
                                    <input
                                        type="email"
                                        name="email"
                                        value={userInfo.email}
                                        onChange={handleInputChange}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                        disabled={!isEditing}
                                    />
                                    {errors.email && <p className="text-red-500 text-sm mt-1">{errors.email}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Date of Birth</label>
                                    {!isEditing ? (
                                        <input
                                            type="text"
                                            value={formatDate(userInfo.dob)}
                                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100"
                                            disabled
                                        />
                                    ) : (
                                        <DatePicker
                                            selected={userInfo.dob ? new Date(userInfo.dob) : null}
                                            onChange={(date) =>
                                                setUserInfo({
                                                    ...userInfo,
                                                    dob: date ? date.toISOString().split("T")[0] : "",
                                                })
                                            }
                                            dateFormat="dd/MM/yyyy"
                                            placeholderText="dd/mm/yyyy"
                                            customInput={
                                                <input
                                                    className="block w-110 border border-gray-300 rounded-md p-2"
                                                />
                                            }
                                        />
                                    )}
                                    {errors.dob && <p className="text-red-500 text-sm mt-1">{errors.dob}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700">Sex</label>
                                    <select
                                        name="sex"
                                        value={userInfo.sex}
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
                                    <label className="block text-sm font-medium text-gray-700">Company Name</label>
                                    <input
                                        type="text"
                                        name="companyName"
                                        value={userInfo.companyName}
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
                                        value={userInfo.taxCode}
                                        onChange={handleInputChange}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                        disabled={!isEditing}
                                    />
                                    {errors.taxCode && <p className="text-red-500 text-sm mt-1">{errors.taxCode}</p>}
                                </div>
                                <div className="col-span-2">
                                    <label className="block text-sm font-medium text-gray-700">Address</label>
                                    <input
                                        type="text"
                                        name="address"
                                        value={userInfo.address}
                                        onChange={handleInputChange}
                                        className="mt-1 block w-full border border-gray-300 rounded-md p-2"
                                        disabled={!isEditing}
                                    />
                                    {errors.address && <p className="text-red-500 text-sm mt-1">{errors.address}</p>}
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
                                            Edit Profile
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
                                        <Button
                                            type="button"
                                            onClick={handleCancel}
                                            variant="outline"
                                            disabled={isLoading}
                                        >
                                            {isLoading ? "Loading..." : "Cancel"}
                                        </Button>
                                        <Button
                                            type="submit"
                                            className="bg-blue-500 text-white hover:bg-blue-600"
                                            disabled={isLoading}
                                        >
                                            Save Changes
                                        </Button>
                                    </>
                                )}
                            </div>
                        </form>
                        <ChangePassword
                            isOpen={isChangePasswordOpen}
                            onClose={() => setIsChangePasswordOpen(false)}
                            userId={userInfo.userId}
                        />
                    </motion.div>
                </main>
            </div>
        </div>
    );
};

export default MyProfile;