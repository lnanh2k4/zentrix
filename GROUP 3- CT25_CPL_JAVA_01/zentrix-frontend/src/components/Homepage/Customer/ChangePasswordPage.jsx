import React, { useState } from "react";
import { Button } from "@/components/ui/button";
import { changePassword } from "@/context/ApiContext";
import { showNotification } from "@/components/Dashboard/NotificationPopup";
import { FaEye, FaEyeSlash } from "react-icons/fa"; // Import eye icons

const ChangePassword = ({ isOpen, onClose, userId }) => {
    const [passwordData, setPasswordData] = useState({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
    });
    const [errors, setErrors] = useState({});
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const handlePasswordChange = (e) => {
        const { name, value } = e.target;
        setPasswordData({ ...passwordData, [name]: value });
        if (errors[name]) {
            setErrors({ ...errors, [name]: null });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!passwordData.currentPassword) {
            newErrors.currentPassword = "Current password is required";
        }

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

        if (!passwordData.confirmPassword) {
            newErrors.confirmPassword = "Please confirm your new password";
        } else if (passwordData.newPassword !== passwordData.confirmPassword) {
            newErrors.confirmPassword = "New password and confirmation do not match";
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
            const response = await changePassword(userId, passwordData.currentPassword, passwordData.newPassword);
            if (response.success) {
                setPasswordData({ currentPassword: "", newPassword: "", confirmPassword: "" });
                setErrors({});
                onClose();
                showNotification("Password changed successfully", 3000, "complete");
            } else {
                showNotification("Failed to change password: Invalid response", 3000, "fail");
            }
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
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
            <div className="bg-white p-6 rounded-lg shadow-md w-96 border border-gray-200">
                <h3 className="text-xl font-bold mb-6 text-[#8c1d6b]">Change Password</h3>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700">Current Password</label>
                        <input
                            type={showCurrentPassword ? "text" : "password"}
                            name="currentPassword"
                            value={passwordData.currentPassword}
                            onChange={handlePasswordChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 pr-10 focus:ring-2 focus:ring-[#8c1d6b] focus:border-transparent"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-9 text-gray-600 hover:text-gray-800"
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
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 pr-10 focus:ring-2 focus:ring-[#8c1d6b] focus:border-transparent"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-9 text-gray-600 hover:text-gray-800"
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
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 pr-10 focus:ring-2 focus:ring-[#8c1d6b] focus:border-transparent"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-9 text-gray-600 hover:text-gray-800"
                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        >
                            {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                        {errors.confirmPassword && (
                            <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>
                        )}
                    </div>
                    <div className="flex justify-end gap-4">
                        <Button
                            type="button"
                            onClick={onClose}
                            variant="outline"
                            className="border-gray-300 text-gray-700 hover:bg-gray-100"
                        >
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