import { CheckCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { showNotification } from "@/components/Dashboard/NotificationPopup";

const VerifyEmailPopup = ({ email, onClose }) => {
    const [isProcessing, setIsProcessing] = useState(true); // Trạng thái mặc định là Processing
    const [isResending, setIsResending] = useState(false); // Trạng thái khi resend

    // Hàm xóa cookie
    const deleteCookie = (name) => {
        document.cookie = `${name}=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT`;
    };

    // Gửi request ban đầu khi popup mở
    useEffect(() => {
        const sendInitialRequest = async () => {
            if (!email) {
                showNotification("No email provided.", 3000, "fail");
                setIsProcessing(false);
                return;
            }

            try {
                const response = await fetch(
                    `http://localhost:6789/api/v1/auth/verify-email?email=${encodeURIComponent(email)}`,
                    {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                    }
                );
                const data = await response.json();

                setIsProcessing(false);
                if (data.success) {
                    showNotification("Verification email sent successfully.", 3000, "complete");
                    deleteCookie("jwt"); // Xóa cookie jwt khi gửi email thành công
                }
            } catch (err) {
                setIsProcessing(false);
                showNotification("Failed to send verification email.", 3000, "fail");
            }
        };

        sendInitialRequest();
    }, [email]);

    // Hàm gửi lại email khi nhấn "Resend Email"
    const handleResendClick = async (e) => {
        e.preventDefault();
        if (!email) {
            showNotification("No email provided to resend.", 3000, "fail");
            return;
        }

        setIsResending(true);
        try {
            const response = await fetch(
                `http://localhost:6789/api/v1/auth/verify-email?email=${encodeURIComponent(email)}`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                }
            );
            const data = await response.json();
            setIsResending(false);

            if (data.success) {
                showNotification("Verification email resent successfully.", 3000, "complete");
                deleteCookie("jwt"); // Xóa cookie jwt khi gửi lại email thành công
            } else {
                showNotification(data.content || "Failed to resend email.", 3000, "fail");
            }
        } catch (err) {
            setIsResending(false);
            showNotification("Failed to resend verification email.", 3000, "fail");
        }
    };

    return (
        <div className="fixed inset-0 bg-black/30 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl shadow-xl p-8 max-w-md w-full text-center relative">
                {/* Nút đóng */}
                <button
                    onClick={onClose}
                    className="absolute top-2 right-2 text-gray-500 hover:text-gray-700 text-xl font-bold"
                >
                    ×
                </button>

                {isProcessing ? (
                    // Trạng thái Processing
                    <>
                        <div className="flex justify-center mb-6">
                            <div className="relative">
                                <CheckCircle className="w-16 h-16 text-blue-500 drop-shadow-[0_4px_8px_rgba(0,0,0,0.3)] animate-pulse" />
                            </div>
                        </div>
                        <h2 className="text-3xl font-bold text-blue-600 mb-4 tracking-wide drop-shadow-[0_2px_4px_rgba(0,0,0,0.2)]">
                            Processing...
                        </h2>
                        <p className="text-lg text-gray-600 mb-6">
                            Sending verification email. Please wait.
                        </p>
                    </>
                ) : (
                    // Trạng thái Thành công (mặc định sau khi xử lý)
                    <>
                        <div className="flex justify-center mb-6">
                            <div className="relative">
                                <CheckCircle className="w-16 h-16 text-blue-500 drop-shadow-[0_4px_8px_rgba(0,0,0,0.3)]" />
                                <div className="absolute top-0 left-0 w-4 h-4 bg-blue-200 rounded-full opacity-50 animate-pulse"></div>
                                <div className="absolute bottom-0 right-0 w-4 h-4 bg-blue-200 rounded-full opacity-50 animate-pulse delay-200"></div>
                                <div className="absolute top-4 left-4 w-2 h-2 bg-blue-200 rounded-full opacity-50 animate-pulse delay-400"></div>
                            </div>
                        </div>
                        <h2 className="text-3xl font-bold text-blue-600 mb-4 tracking-wide drop-shadow-[0_2px_4px_rgba(0,0,0,0.2)]">
                            Verify Your Email
                        </h2>
                        <p className="text-lg text-gray-600 mb-6">
                            {email
                                ? `We’ve sent a verification link to ${email}. Please check your email to verify your account.`
                                : "We’ve sent a verification link to your email. Please check your inbox."}
                        </p>
                        <p className="text-md text-gray-500 mb-6">
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
                        <button
                            onClick={onClose}
                            className="w-full py-2 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium"
                        >
                            Close
                        </button>
                    </>
                )}
            </div>
        </div>
    );
};

export default VerifyEmailPopup;