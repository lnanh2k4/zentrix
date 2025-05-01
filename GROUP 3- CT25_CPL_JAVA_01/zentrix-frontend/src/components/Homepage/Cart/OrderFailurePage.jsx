import { useLocation, useNavigate } from "react-router-dom";
import { AlertCircle, CheckCircle } from "lucide-react";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import { useEffect, useState, useRef } from "react";

const OrderFailurePage = () => {
    const { state } = useLocation();
    const navigate = useNavigate();
    const [isProcessing, setIsProcessing] = useState(true); // Trạng thái để hiển thị "Processing..."
    const timeoutRef = useRef(null); // Ref để lưu setTimeout

    const errorMessage = state?.errorMessage || "An error occurred during payment or order processing.";

    useEffect(() => {
        // Đặt timeout 10 giây để chuyển từ "Processing..." sang giao diện lỗi
        timeoutRef.current = setTimeout(() => {
            setIsProcessing(false); // Sau 10 giây, hiển thị giao diện lỗi
        }, 10000);

        // Cleanup: Hủy timeout nếu component unmount (tức là người dùng rời trang)
        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    const handleTryAgain = () => {
        navigate("/cart");
    };

    // Nếu đang ở trạng thái "Processing..."
    if (isProcessing) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Header className="fixed top-0 left-0 w-full h-16 bg-white shadow-md z-50" />
                <div className="flex-grow flex items-center justify-center p-4 pt-20">
                    <div className="bg-white p-8 rounded-lg shadow-lg text-center max-w-md w-full">
                        <div className="flex justify-center mb-4">
                            <div className="relative">
                                <CheckCircle className="w-16 h-16 text-blue-500" />
                                <div className="absolute top-0 left-0 w-4 h-4 bg-blue-200 rounded-full opacity-50 animate-pulse"></div>
                                <div className="absolute bottom-0 right-0 w-4 h-4 bg-blue-200 rounded-full opacity-50 animate-pulse delay-200"></div>
                                <div className="absolute top-4 left-4 w-2 h-2 bg-blue-200 rounded-full opacity-50 animate-pulse delay-400"></div>
                            </div>
                        </div>
                        <h2 className="text-2xl font-bold text-gray-800 mb-2">Processing...</h2>
                        <p className="text-gray-600 mb-4">Please wait while we process your request.</p>
                    </div>
                </div>
                <Footer className="h-12 mt-8" />
            </div>
        );
    }

    // Nếu hết 10 giây và không chuyển trang, hiển thị giao diện lỗi
    return (
        <div className="flex flex-col min-h-screen bg-gray-100">
            <Header className="fixed top-0 left-0 w-full h-16 bg-white shadow-md z-50" />
            <div className="flex-grow flex items-center justify-center p-4 pt-20">
                <div className="bg-white p-8 rounded-lg shadow-lg text-center max-w-md w-full">
                    <div className="flex justify-center mb-4">
                        <AlertCircle className="w-16 h-16 text-red-500" />
                    </div>
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Order Failed</h2>
                    <p className="text-gray-600 mb-4">{errorMessage}</p>
                    <button
                        onClick={handleTryAgain}
                        className="w-full bg-red-500 text-white py-3 rounded-lg hover:bg-red-600 transition"
                    >
                        Try Again
                    </button>
                </div>
            </div>
            <Footer className="h-12 mt-8" />
        </div>
    );
};

export default OrderFailurePage;