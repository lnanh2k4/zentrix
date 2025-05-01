import { useState, useEffect } from "react";
import Sidebar from "@/components/ui/Sidebar";
import Header from "@/components/ui/Header";
import { User, Ticket, ShoppingBag } from "lucide-react";
import {
    Carousel,
    CarouselContent,
    CarouselItem,
    CarouselNext,
    CarouselPrevious,
} from "@/components/ui/carousel";
import { motion, AnimatePresence } from "framer-motion";
import axios from "axios";
import { Link } from "react-router-dom";

const imageSlides = [
    { id: 1, src: "https://file.hstatic.net/200000722513/file/thang_04_banner_web_slider_800x400.png", alt: "Image 1" },
    { id: 2, src: "https://file.hstatic.net/200000722513/file/thang_02_pc_gvn_banner_web_slider_800x400.jpg", alt: "Image 2" },
    { id: 3, src: "https://file.hstatic.net/200000722513/file/thang_03_laptop_rtx_5090_800x400.jpg", alt: "Image 3" },
];

const API_URL_ORDERS = "http://localhost:6789/api/v1/orders";
const API_URL_MEMBERSHIPS = "http://localhost:6789/api/v1/memberships";
const USER_INFO_URL = "http://localhost:6789/api/v1/auth/info";

// Định nghĩa mapping trạng thái đơn hàng
const STATUS_NUMBER_MAPPING = {
    Pending: 0,
    Confirmed: 1,
    Shipping: 2,
    Delivered: 3,
    Canceled: 4,
    Received: 5,
};

// Fetch memberships
const fetchMemberships = async () => {
    try {
        const response = await axios.get(API_URL_MEMBERSHIPS, { withCredentials: true });
        return response.data.content;
    } catch (error) {
        console.error("Error fetching memberships:", error);
        return [];
    }
};

// Fetch user info
const fetchUserInfo = async () => {
    try {
        const response = await axios.get(USER_INFO_URL, { withCredentials: true });
        if (response.data.success && response.data.content) {
            return response.data.content;
        }
        console.error("No user data found in response:", response.data);
        return null;
    } catch (error) {
        console.error("Error fetching user info:", error.message);
        return null;
    }
};

// Fetch orders
const fetchOrders = async (userId) => {
    if (!userId) return [];
    try {
        const url = `${API_URL_ORDERS}/user/${userId}?page=0&size=1000`;
        const response = await axios.get(url, { withCredentials: true });
        if (response.data.success && response.data.content) {
            return response.data.content.map(order => ({
                ...order,
                createdAt: new Date(order.createdAt).toISOString().split("T")[0],
            }));
        }
        return [];
    } catch (error) {
        console.error("Error fetching orders:", error.message);
        return [];
    }
};

const Home = () => {
    const [barcode, setBarcode] = useState("2110");
    const [currentIndex, setCurrentIndex] = useState(0);
    const [direction, setDirection] = useState(1);
    const [userInfo, setUserInfo] = useState({ userId: null, firstName: "", lastName: "", phone: "" });
    const [orders, setOrders] = useState([]);
    const [membershipLevel, setMembershipLevel] = useState("UNRANK");
    const [userPoints, setUserPoints] = useState(0);
    const [loading, setLoading] = useState(true);
    // Fetch user info, orders, and memberships to determine rank
    useEffect(() => {
        const loadData = async () => {
            setLoading(true);

            const user = await fetchUserInfo();
            if (user) {
                setUserInfo({
                    userId: user.userId || null,
                    firstName: user.firstName || "User",
                    lastName: user.lastName || "Name",
                    phone: user.phone || "N/A",
                });

                const ordersData = await fetchOrders(user.userId);
                setOrders(ordersData);

                const userPointsValue = user.userPoint || 0;
                setUserPoints(userPointsValue);

                const memberships = await fetchMemberships();
                const sortedMemberships = memberships.sort((a, b) => a.mbsPoint - b.mbsPoint);
                let currentLevel = { mbsName: "UNRANK", mbsPoint: 0 };

                for (let i = 0; i < sortedMemberships.length; i++) {
                    if (userPointsValue >= sortedMemberships[i].mbsPoint) {
                        currentLevel = sortedMemberships[i];
                    } else {
                        break;
                    }
                }

                setMembershipLevel(currentLevel.mbsName);
            } else {
                setUserPoints(0);
            }

            setLoading(false);
        };

        loadData();
    }, []);

    // Automatically switch slides
    useEffect(() => {
        const timer = setInterval(() => {
            nextSlide();
        }, 3000);
        return () => clearInterval(timer);
    }, []);

    const nextSlide = () => {
        setDirection(1);
        setCurrentIndex((prev) => (prev + 1) % imageSlides.length);
    };

    const prevSlide = () => {
        setDirection(-1);
        setCurrentIndex((prev) => (prev - 1 + imageSlides.length) % imageSlides.length);
    };

    const getFullName = () => {
        return `${userInfo.firstName} ${userInfo.lastName}`.trim() || "User Name";
    };

    // Tính tổng số tiền tích lũy, không bao gồm các đơn hàng có trạng thái Canceled
    const totalAccumulatedAmount = orders
        .filter(order => order.status !== STATUS_NUMBER_MAPPING["Canceled"])
        .reduce((sum, order) => {
            return sum + (order.orderDetails?.reduce((total, detail) => total + (detail.unitPrice * detail.quantity * (1 + detail.vatRate)), 0) || 0);
        }, 0);

    // Format giá tiền với Intl.NumberFormat
    // Format giá tiền với Intl.NumberFormat và thay VND thành VNĐ
    const formattedTotalAccumulatedAmount = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        currencyDisplay: 'code',
    }).format(totalAccumulatedAmount).replace('VND', 'VNĐ');

    const actionItems = [
        { icon: <User size={24} />, label: "Membership Rank", path: "/membershipPage" },
        { icon: <Ticket size={24} />, label: "Discount Code", path: "/myPromotionPage" },
        { icon: <ShoppingBag size={24} />, label: "Purchase History", path: "/history" },
    ];


    return (
        <div className="home-container bg-[#f8f8fc] text-gray-900 min-h-screen flex flex-col" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="home-header h-20 bg-red-600 text-white flex items-center px-4 shadow-md w-full">
                <Header />
            </header>
            <div className="home-content flex flex-1 container mx-auto p-6 space-x-6">
                <Sidebar />
                <main className="flex-1 flex flex-col space-y-8">
                    <div className="home-profile bg-white rounded-lg shadow-md p-6 flex items-center space-x-8 animate-slideInRight">
                        <img
                            src="/logouser.png"
                            alt="Avatar"
                            className="w-16 h-16 rounded-full border-2 border-gray-300"
                        />
                        <div className="flex flex-col">
                            {loading ? (
                                <>
                                    <div className="w-32 h-6 bg-gray-200 rounded animate-pulse mb-2" />
                                    <div className="w-24 h-4 bg-gray-200 rounded animate-pulse" />
                                </>
                            ) : (
                                <>
                                    <h2 className="text-xl font-semibold text-[#8c1d6b] text-center">{getFullName()}</h2>
                                    <p className="text-gray-600 text-center">{userInfo.phone || "N/A"}</p>
                                </>
                            )}
                            <span className="text-[#8c1d6b] border border-[#8c1d6b] text-sm px-3 py-1 rounded-full text-center mt-2">
                                {loading ? "Loading..." : membershipLevel}
                            </span>
                        </div>
                        <div className="flex space-x-8">
                            <div className="text-center">
                                <h3 className="text-lg font-bold">{orders.length}</h3>
                                <p className="text-gray-500">Orders</p>
                            </div>
                            <div className="text-center">
                                <h3 className="text-lg font-bold">{formattedTotalAccumulatedAmount}</h3>
                                <p className="text-gray-500">Total Accumulated Amount</p>
                            </div>
                            <div className="text-center">
                                <h3 className="text-lg font-bold">{userPoints.toLocaleString()}</h3>
                                <p className="text-gray-500">User Points</p>
                            </div>

                        </div>
                    </div>
                    <div className="home-actions bg-white rounded-lg shadow-md p-6 flex justify-around animate-slideInRight">
                        {actionItems.map((item, index) => (
                            <Link
                                key={index}
                                to={item.path}
                                className="flex flex-col items-center space-y-1 hover:opacity-75 transition-opacity"
                            >
                                <div className="p-3 bg-blue-100 rounded-full">{item.icon}</div>
                                <p className="text-sm font-medium">{item.label}</p>
                            </Link>
                        ))}
                    </div>
                    <div className="home-carousel w-245 relative overflow-hidden">
                        <Carousel>
                            <CarouselContent className="relative w-full h-[300px]">
                                <AnimatePresence initial={false} custom={direction}>
                                    <motion.div
                                        key={imageSlides[currentIndex].id}
                                        className="absolute w-full h-full flex justify-center"
                                        initial={{ x: direction * 100, opacity: 0 }}
                                        animate={{ x: 0, opacity: 1 }}
                                        exit={{ x: -direction * 100, opacity: 0 }}
                                        transition={{ duration: 0.5, ease: "easeInOut" }}
                                    >
                                        <img
                                            src={imageSlides[currentIndex].src}
                                            alt={imageSlides[currentIndex].alt}
                                            className="w-auto h-auto object-cover rounded-lg shadow-md"
                                        />
                                    </motion.div>
                                </AnimatePresence>
                            </CarouselContent>
                            <CarouselPrevious onClick={prevSlide} />
                            <CarouselNext onClick={nextSlide} />
                        </Carousel>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default Home;