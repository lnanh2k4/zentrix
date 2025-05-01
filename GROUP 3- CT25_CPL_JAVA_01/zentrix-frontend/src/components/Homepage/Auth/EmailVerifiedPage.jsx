import { CheckCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { showNotification } from "@/components/Dashboard/NotificationPopup";
import { getInfo } from "@/context/ApiContext"; // Để lấy thông tin user và role
import { checkUserRole } from "@/services/InfoService"; // Để kiểm tra role

const EmailVerifiedPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams(); // Sử dụng useSearchParams để lấy query params
  const token = searchParams.get("token");
  const [isProcessing, setIsProcessing] = useState(true); // Trạng thái xử lý
  const [isVerified, setIsVerified] = useState(false); // Trạng thái xác nhận
  const [email, setEmail] = useState(""); // Lưu email từ response hoặc state

  // Gửi request để xác nhận token khi vào trang
  useEffect(() => {
    const verifyEmail = async () => {
      try {
        const response = await fetch(`http://localhost:6789/api/v1/auth/verify-email/${token}`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
        });
        const data = await response.json();

        if (data.success) {
          setIsVerified(true);
          setEmail(data.email || ""); // Giả sử backend trả về email trong response (tùy chỉnh nếu cần)

          // Lấy thông tin user để kiểm tra role
          const user = await getInfo();
          const role = checkUserRole(user.content);

          // Tự động chuyển hướng sau 5 giây dựa trên role
          const timer = setTimeout(() => {
            if (!role.isCustomer()) {
              navigate("/dashboard");
            } else {
              navigate("/");
            }
          }, 5000);

          return () => clearTimeout(timer); // Dọn dẹp timer
        } else {
          setIsProcessing(false);
        }
      } catch (err) {
        setIsProcessing(false);
      }
    };

    verifyEmail();
  }, [token, navigate]);

  const handleRedirectClick = async () => {
    try {
      const user = await getInfo();
      const role = checkUserRole(user.content);
      if (!role.isCustomer()) {
        navigate("/dashboard");
      } else {
        navigate("/");
      }
    } catch (err) {
      navigate("/"); // Nếu lỗi, mặc định về homepage
    }
  };

  return (
    <div
      className="h-screen w-screen flex items-center justify-center p-4 bg-cover bg-center bg-no-repeat bg-fixed overflow-hidden"
      style={{ backgroundImage: "url('/register_background.jpg')" }}
    >
      <div className="bg-white rounded-xl shadow-xl p-8 max-w-md w-full text-center">
        {isProcessing ? (
          // Trạng thái Processing
          <div>
            <div className="flex justify-center mb-8">
              <div className="relative">
                <CheckCircle className="w-24 h-24 text-blue-500 drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)] animate-pulse" />
              </div>
            </div>
            <h2 className="text-5xl font-bold text-blue-600 mb-8 tracking-wide drop-shadow-[0_2px_4px_rgba(0,0,0,0.2)]">
              Processing...
            </h2>
            <p className="text-xl text-gray-600 mb-6">
              Verifying your email. Please wait a moment.
            </p>
          </div>
        ) : isVerified ? (
          // Trạng thái thành công
          <>
            <div className="flex justify-center mb-8">
              <div className="relative">
                <CheckCircle className="w-24 h-24 text-blue-500 drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)]" />
                <div className="absolute top-0 left-0 w-6 h-6 bg-blue-200 rounded-full opacity-50 animate-pulse"></div>
                <div className="absolute bottom-0 right-0 w-6 h-6 bg-blue-200 rounded-full opacity-50 animate-pulse delay-200"></div>
                <div className="absolute top-6 left-6 w-3 h-3 bg-blue-200 rounded-full opacity-50 animate-pulse delay-400"></div>
              </div>
            </div>
            <h2 className="text-5xl font-bold text-blue-600 mb-8 tracking-wide drop-shadow-[0_2px_4px_rgba(0,0,0,0.2)]">
              Email Verified!
            </h2>
            <p className="text-xl text-gray-600 mb-6">
              {email
                ? `Congratulations! Your email ${email} has been successfully verified.`
                : "Congratulations! Your email has been successfully verified."}
            </p>
            <button
              onClick={handleRedirectClick}
              className="w-full py-3 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium"
            >
              Continue
            </button>
            <p className="text-sm text-gray-500 mt-2">
              Redirecting in 5 seconds...
            </p>
          </>
        ) : (
          // Trạng thái thất bại
          <>
            <div className="flex justify-center mb-8">
              <div className="relative">
                <CheckCircle className="w-24 h-24 text-red-500 drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)]" />
              </div>
            </div>
            <h2 className="text-5xl font-bold text-red-600 mb-8 tracking-wide drop-shadow-[0_2px_4px_rgba(0,0,0,0.2)]">
              Verification Failed
            </h2>
            <p className="text-xl text-gray-600 mb-6">
              Something went wrong. Please try again or contact support.
            </p>
            <button
              onClick={() => navigate("/")}
              className="w-full py-3 px-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium"
            >
              Back to Homepage
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default EmailVerifiedPage;