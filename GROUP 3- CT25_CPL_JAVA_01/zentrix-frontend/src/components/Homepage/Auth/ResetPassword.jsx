import { useState, useEffect } from "react";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { useLocation } from "react-router-dom";
import { showNotification } from "@/components/Dashboard/NotificationPopup";

const ResetPassword = () => {
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const location = useLocation();

    const handleLogoClick = () => {
        window.location.href = "/";
    };

    const validatePassword = (password) => {
        const minLength = 8;
        const hasUpperCase = /[A-Z]/.test(password);
        const hasLowerCase = /[a-z]/.test(password);
        const hasNumber = /[0-9]/.test(password);
        const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

        if (password.length < minLength) {
            return "Password must be at least 8 characters long.";
        }
        if (!hasUpperCase) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!hasLowerCase) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!hasNumber) {
            return "Password must contain at least one number.";
        }
        if (!hasSpecialChar) {
            return "Password must contain at least one special character (e.g., !@#$%^&*).";
        }
        return "";
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const password = e.target.password.value;
        const confirmPassword = e.target["confirm-password"].value;

        // Validate password
        const passwordError = validatePassword(password);
        if (passwordError) {
            showNotification(passwordError, 3000, "fail");
            return;
        }

        // Check if passwords match
        if (password !== confirmPassword) {
            showNotification("Password and confirm password do not match!", 3000, "fail");
            return;
        }

        const params = new URLSearchParams(location.search);
        const token = params.get("token");
        const method = params.get("method");

        try {
            const response = await fetch("http://localhost:6789/api/v1/auth/reset-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    token: token || null,
                    otp: method === "phone" ? params.get("otp") : null,
                    newPassword: password,
                }),
            });
            const data = await response.json();
            if (data.success) {
                showNotification("Password reset successfully! Redirecting to login...", 3000, "complete");
                setTimeout(() => {
                    window.location.href = "/login";
                }, 2000);
            } else {
                showNotification(data.message || "Failed to reset password.", 3000, "fail");
            }
        } catch (err) {
            showNotification("Something went wrong.", 3000, "fail");
        }
    };

    return (
        <div
            className="h-screen w-screen flex items-center justify-center p-4 bg-cover bg-center bg-no-repeat bg-fixed overflow-hidden"
            style={{ backgroundImage: "url('/register_background.jpg')" }}
        >
            <div className="bg-white rounded-xl shadow-xl p-8 max-w-3xl w-full">
                <div className="mb-1 max-w-3xl w-full flex flex-col items-center">
                    <img
                        alt="Zentrix Logo"
                        className="h-12 w-auto object-contain drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)] cursor-pointer"
                        src="/logo_zentrix.png"
                        style={{ filter: "drop-shadow(0px 10px 10px rgba(0, 0, 0, 0.8))" }}
                        onClick={handleLogoClick}
                    />
                </div>
                <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">Reset Your Password</h2>
                <p className="text-center text-gray-600 mb-6">Enter your new password below</p>

                <form onSubmit={handleSubmit}>
                    <div className="mb-6 relative">
                        <label className="block text-gray-700 mb-2" htmlFor="password">
                            New Password
                        </label>
                        <input
                            type={showPassword ? "text" : "password"}
                            id="password"
                            name="password"
                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                            placeholder="Enter new password"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-10 text-gray-600 hover:text-gray-800"
                            onClick={() => setShowPassword(!showPassword)}
                        >
                            {showPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                    </div>
                    <div className="mb-6 relative">
                        <label className="block text-gray-700 mb-2" htmlFor="confirm-password">
                            Confirm New Password
                        </label>
                        <input
                            type={showConfirmPassword ? "text" : "password"}
                            id="confirm-password"
                            name="confirm-password"
                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 pr-10"
                            placeholder="Confirm new password"
                            required
                        />
                        <button
                            type="button"
                            className="absolute right-3 top-10 text-gray-600 hover:text-gray-800"
                            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        >
                            {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                        </button>
                    </div>
                    <button
                        type="submit"
                        className="w-full py-3 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium"
                    >
                        Reset Password
                    </button>
                </form>

                <p className="text-center text-gray-600 mt-6">
                    Back to{" "}
                    <a href="/login" className="text-blue-600 hover:underline">
                        Login
                    </a>
                </p>
            </div>
        </div>
    );
};

export default ResetPassword;