import { showNotification } from "@/components/Dashboard/NotificationPopup";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

const ForgotPasswordEmail = () => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);

    const handleLogoClick = () => {
        window.location.href = "/";
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const email = e.target.email.value;
        setIsLoading(true);

        try {
            const response = await fetch("http://localhost:6789/api/v1/auth/forgot-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ identifier: email, method: "email" }),
            });
            const data = await response.json();
            setIsLoading(false);

            if (data.success) {
                showNotification("Please check your email for a reset link.", 3000, "complete");
                navigate("/forgot-password/email-sent", { state: { email } }); // Truyền email qua state
            } else {
                showNotification(data.content, 3000, "fail");
            }
        } catch (err) {
            setIsLoading(false);
            showNotification("Failed to send reset request.", 3000, "fail");
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
                <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">
                    Reset Password via Email
                </h2>
                <p className="text-center text-gray-600 mb-6">
                    Enter your email address to receive a reset link
                </p>

                <form onSubmit={handleSubmit}>
                    <div className="mb-6">
                        <label className="block text-gray-700 mb-2" htmlFor="email">
                            Email Address
                        </label>
                        <input
                            type="email"
                            id="email"
                            className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter your email"
                            required
                            disabled={isLoading}
                        />
                    </div>
                    <button
                        type="submit"
                        className={`w-full py-3 px-4 rounded-lg transition-colors duration-200 font-medium ${isLoading
                            ? "bg-gray-400 cursor-not-allowed"
                            : "bg-blue-600 text-white hover:bg-blue-700"
                            }`}
                        disabled={isLoading}
                    >
                        {isLoading ? "Processing..." : "Send Reset Link"}
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

export default ForgotPasswordEmail;