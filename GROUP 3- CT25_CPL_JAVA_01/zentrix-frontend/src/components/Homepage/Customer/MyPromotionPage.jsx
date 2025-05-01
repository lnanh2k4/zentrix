import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "@/components/ui/Sidebar";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import { Ticket } from "lucide-react";
import axios from "axios";

const MyPromotionPage = () => {
    const [promotions, setPromotions] = useState([]);
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const isExpired = (expiryDate) => {
        const today = new Date();
        const expDate = new Date(expiryDate);
        today.setHours(0, 0, 0, 0); // Đặt giờ về 00:00:00 để so sánh ngày
        expDate.setHours(0, 0, 0, 0); // Đặt giờ về 00:00:00 để so sánh ngày
        return expDate < today; // Hết hạn nếu ngày hết hạn nhỏ hơn ngày hôm nay
    };

    // Hàm định dạng ngày thành "date month year"
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const day = date.getDate();
        const month = date.toLocaleString("default", { month: "long" }); // Lấy tên tháng đầy đủ
        const year = date.getFullYear();
        return `${day} ${month} ${year}`; // Ví dụ: "25 April 2025"
    };

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success) {
                setUser(response.data.content);
                return response.data.content.userId;
            }
            console.warn("User info fetch succeeded but returned unsuccessful response:", response.data);
            return null;
        } catch (error) {
            console.error("Error fetching user info:", error.response?.data || error.message);
            setUser(null);
            return null;
        }
    };

    const fetchUserPromotions = async (userId) => {
        if (!userId) {
            setPromotions([]);
            return;
        }
        try {
            const response = await axios.get("http://localhost:6789/api/v1/promotions/my-promotions", {
                params: { userId },
                withCredentials: true,
            });
            if (response.data.success) {
                const activePromotions = response.data.content.filter(
                    (userPromo) => userPromo.status === 1
                );
                const promoIds = activePromotions.map((userPromo) => userPromo.promId.promId);
                await fetchPromotionsByIds(promoIds);
            } else {
                setPromotions([]);
            }
        } catch (error) {
            console.error("Error fetching user promotions:", error.response?.data || error.message);
            if (error.response?.status === 401 || error.response?.status === 403) {
                navigate("/login");
            }
            setPromotions([]);
        }
    };

    const fetchPromotionsByIds = async (promoIds) => {
        if (!promoIds.length) {
            setPromotions([]);
            return;
        }
        try {
            const promoPromises = promoIds.map((promoId) =>
                axios.get(`http://localhost:6789/api/v1/promotions/${promoId}`, {
                    withCredentials: true,
                })
            );
            const responses = await Promise.all(promoPromises);
            const promotionData = responses
                .filter((response) => response.data.success)
                .map((response) => ({
                    promId: response.data.content.promId,
                    promotionName: response.data.content.promName,
                    discount: response.data.content.discount,
                    expiryDate: response.data.content.endDate,
                    quantity: response.data.content.quantity,
                }));
            setPromotions(promotionData);
        } catch (error) {
            console.error("Error fetching promotions by IDs:", error.response?.data || error.message);
            setPromotions([]);
        }
    };

    useEffect(() => {
        const loadPromotions = async () => {
            setLoading(true);
            const userId = await fetchUserInfo();

            if (userId) {
                await fetchUserPromotions(userId);
            } else {
                navigate("/login");
            }

            setLoading(false);
        };

        loadPromotions();
    }, [navigate]);

    const handleApplyPromotion = (promo) => {
        if (promo.quantity > 0 && !isExpired(promo.expiryDate)) {
            navigate("/cart");
        }
    };

    return (
        <div className="bg-[#f8f8fc] text-gray-900 min-h-screen flex flex-col" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="h-20 bg-red-600 text-white flex items-center px-4 shadow-md w-full">
                <Header />
            </header>
            <div className="flex flex-1 container mx-auto p-6 space-x-6">
                <Sidebar />
                <main className="flex-1 flex flex-col space-y-8">
                    <div className="bg-white rounded-lg shadow-md p-6">
                        <div className="flex items-center space-x-3">
                            <Ticket className="w-8 h-8 text-[#8c1d6b]" />
                            <h2 className="text-2xl font-semibold text-[#8c1d6b]">My Promotions</h2>
                        </div>
                    </div>
                    <div className="relative w-full max-w-[60rem] max-h-[90vh] bg-white rounded-lg shadow-[0_4px_20px_rgba(0,0,0,0.1)] overflow-y-auto p-6 mx-0">
                        <h3 className="text-lg font-semibold text-[#8c1d6b] mb-4">
                            Available Offers ({promotions.length})
                        </h3>
                        {loading ? (
                            <p className="text-gray-500 text-center py-4">Loading...</p>
                        ) : promotions.length === 0 ? (
                            <p className="text-gray-500 text-center py-4">
                                No offers available at the moment
                            </p>
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                {promotions.map((promo, index) => {
                                    const expired = isExpired(promo.expiryDate);
                                    return (
                                        <div
                                            key={index}
                                            className="border border-gray-200 rounded-lg p-4 hover:shadow-lg transition-shadow"
                                        >
                                            <h4 className="text-md font-medium text-gray-800 mb-2">
                                                {promo.promotionName}
                                            </h4>
                                            <div className="space-y-1">
                                                <p className="text-sm text-red-500">
                                                    Discount: {promo.discount}%
                                                </p>
                                                <p className="text-sm text-gray-600">
                                                    Expires: {formatDate(promo.expiryDate)}
                                                    {expired && (
                                                        <span className="ml-2 text-red-500 font-medium">
                                                            (Expired)
                                                        </span>
                                                    )}
                                                </p>
                                            </div>
                                            <button
                                                className={`mt-3 w-full px-3 py-1 text-sm rounded transition-colors ${promo.quantity > 0 && !expired
                                                    ? "bg-red-500 text-white hover:bg-red-600"
                                                    : "bg-gray-300 text-gray-500 cursor-not-allowed"
                                                    }`}
                                                disabled={promo.quantity === 0 || expired}
                                                onClick={() => handleApplyPromotion(promo)}
                                            >
                                                {promo.quantity > 0 && !expired ? "Apply Now" : "Not Available"}
                                            </button>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                </main>
            </div>
            <Footer />
        </div>
    );
};

export default MyPromotionPage;