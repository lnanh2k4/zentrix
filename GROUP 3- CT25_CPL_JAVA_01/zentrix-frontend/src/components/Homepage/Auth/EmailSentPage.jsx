import { CheckCircle } from "lucide-react";
import { useState } from "react";
import { useLocation } from "react-router-dom";
import { showNotification } from "@/components/Dashboard/NotificationPopup";

const EmailSentForForgotPasswordPage = () => {
    const location = useLocation();
    const [isResending, setIsResending] = useState(false);

    // Lấy email từ state (nếu có)
    const email = location.state?.email || "";

    const handleResendClick = async (e) => {
        e.preventDefault();
        if (!email) {
            showNotification("No email provided to resend.", 3000, "fail");
            return;
        }

        setIsResending(true);
        try {
            const response = await fetch("http://localhost:6789/api/v1/auth/forgot-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ identifier: email, method: "email" }),
            });
            const data = await response.json();
            setIsResending(false);

            if (data.success) {
                showNotification("Reset link resent successfully.", 3000, "complete");
            } else {
                showNotification(data.content, 3000, "fail");
            }
        } catch (err) {
            setIsResending(false);
            // showNotification("Failed to resend reset link.", 3000, "fail");
            console.log("Failed to resend reset link.", err)
        }
    };

    return (
        <div
            className="h-screen w-screen flex items-center justify-center p-4 bg-cover bg-center bg-no-repeat bg-fixed overflow-hidden"
            style={{ backgroundImage: "url('/register_background.jpg')" }}
        >
            <div className="bg-white rounded-xl shadow-xl p-8 max-w-md w-full text-center">
                {/* Icon animation */}
                <div className="flex justify-center mb-8">
                    <div className="relative">
                        <CheckCircle className="w-24 h-24 text-blue-500 drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)]" />
                        <div className="absolute top-0 left-0 w-6 h-6 bg-blue-200 rounded-full opacity-50 animate-pulse"></div>
                        <div className="absolute bottom-0 right-0 w-6 h-6 bg-blue-200 rounded-full opacity-50 animate-pulse delay-200"></div>
                        <div className="absolute top-6 left-6 w-3 h-3 bg-blue-200 rounded-full opacity-50 animate-pulse delay-400"></div>
                    </div>
                </div>

                {/* Tiêu đề */}
                <h2 className="text-5xl font-bold text-blue-600 mb-8 tracking-wide drop-shadow-[0_2px_4px_rgba(0,0,0,0.2)]">
                    Email Sent!
                </h2>

                {/* Nội dung */}
                <p className="text-xl text-gray-600 mb-6">
                    We’ve sent a verification email to your inbox. Please check your email and click the link in your account.
                </p>

                {/* Liên kết */}
                <p className="text-lg text-gray-500">
                    Didn’t receive it?{" "}
                    <button
                        onClick={handleResendClick}
                        className={`text-blue-500 hover:underline ${isResending ? "opacity-50 cursor-not-allowed" : ""
                            }`}
                        disabled={isResending}
                    >
                        {isResending ? "Resending..." : "Resend Email"}
                    </button>
                </p>
            </div>
        </div>
    );
};

export default EmailSentForForgotPasswordPage;