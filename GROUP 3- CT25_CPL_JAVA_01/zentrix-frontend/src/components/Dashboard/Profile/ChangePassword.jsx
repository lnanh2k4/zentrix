import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { changePassword } from "@/context/ApiContext";
import { showNotification } from "../NotificationPopup";
import { FaEye, FaEyeSlash } from "react-icons/fa"; // Import eye icons

const ChangePassword = ({ isOpen, onClose, userId }) => {
    const [passwordData, setPasswordData] = useState({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
    });
    const [errors, setErrors] = useState({});
    const [showCurrentPassword, setShowCurrentPassword] = useState(false); // State cho currentPassword
    const [showNewPassword, setShowNewPassword] = useState(false); // State cho newPassword
    const [showConfirmPassword, setShowConfirmPassword] = useState(false); // State cho confirmPassword

    const handlePasswordChange = (e) => {
        const { name, value } = e.target;
        setPasswordData({ ...passwordData, [name]: value });
        if (errors[name]) {
            setErrors({ ...errors, [name]: null });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        // Kiểm tra mật khẩu hiện tại
        if (!passwordData.currentPassword) {
            newErrors.currentPassword = "Current password is required";
        }

        // Kiểm tra mật khẩu mới
        if (!passwordData.newPassword) {
            newErrors.newPassword = "New password is required";
        } else if (passwordData.newPassword.length < 8) {
            newErrors.newPassword = "New password must be at least 8 characters";
        } else if (
            !/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).+$/.test(
                passwordData.newPassword
            )
        ) {
            newErrors.newPassword =
                "New password must contain at least one uppercase letter, one lowercase letter, one number, and one special character";
        }

        // Kiểm tra xác nhận mật khẩu
        if (!passwordData.confirmPassword) {
            newErrors.confirmPassword = "Please confirm your new password";
        } else if (passwordData.newPassword !== passwordData.confirmPassword) {
            newErrors.confirmPassword = "New password and confirmation do not match";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0; // Trả về true nếu không có lỗi
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            await changePassword(userId, passwordData.currentPassword, passwordData.newPassword);
            setPasswordData({ currentPassword: "", newPassword: "", confirmPassword: "" });
            setErrors({});
            onClose();
            showNotification("Password changed successfully", 3000, "complete");
        } catch (error) {
            console.error("Error changing password:", error);
            if (error.message === "Unauthorized: Invalid credentials") {
                window.location.href = "/login";
            } else {
                showNotification(
                    `Failed to change password: ${error.response?.data?.content || error.message}`,
                    3000,
                    "fail"
                );
            }
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
            <div className="bg-white p-6 rounded-lg shadow-md w-96">
                <h3 className="text-lg font-bold mb-4">Change Password</h3>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700">Current Password</label>
                        <input
                            type={showCurrentPassword ? "text" : "password"}
                            name="currentPassword"
                            value={passwordData.currentPassword}
                            onChange={handlePasswordChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 pr-10"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-10 text-gray-600 hover:text-gray-800"
                            onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                        >
                            {showCurrentPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                        {errors.currentPassword && (
                            <p className="text-red-500 text-xs mt-1">{errors.currentPassword}</p>
                        )}
                    </div>
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700">New Password</label>
                        <input
                            type={showNewPassword ? "text" : "password"}
                            name="newPassword"
                            value={passwordData.newPassword}
                            onChange={handlePasswordChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 pr-10"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-10 text-gray-600 hover:text-gray-800"
                            onClick={() => setShowNewPassword(!showNewPassword)}
                        >
                            {showNewPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                        {errors.newPassword && <p className="text-red-500 text-xs mt-1">{errors.newPassword}</p>}
                    </div>
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700">Confirm New Password</label>
                        <input
                            type={showConfirmPassword ? "text" : "password"}
                            name="confirmPassword"
                            value={passwordData.confirmPassword}
                            onChange={handlePasswordChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 pr-10"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-10 text-gray-600 hover:text-gray-800"
                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        >
                            {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                        {errors.confirmPassword && (
                            <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>
                        )}
                    </div>
                    <div className="flex justify-end gap-2">
                        <Button type="button" onClick={onClose} variant="outline">
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

export default ChangePassword;