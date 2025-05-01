import { useEffect, useState } from "react";
import Sidebar from "@/components/ui/Sidebar";
import Header from "@/components/ui/Header";
import { Medal, Star } from "lucide-react";
import axios from "axios";

const API_URL = "http://localhost:6789/api/v1/memberships";
const USER_INFO_URL = "http://localhost:6789/api/v1/auth/info";
const ORDERS_URL = "http://localhost:6789/api/v1/orders";

const fetchMemberships = async () => {
    try {
        const response = await axios.get(API_URL, { withCredentials: true });
        return response.data.content;
    } catch (error) {
        console.error("Error fetching memberships:", error);
        throw error;
    }
};

const fetchUserInfo = async () => {
    try {
        const response = await axios.get(USER_INFO_URL, { withCredentials: true });
        if (response.data.success && response.data.content) {
            return response.data.content;
        }
        throw new Error("No user data found");
    } catch (error) {
        console.error("Error fetching user info:", error);
        throw error;
    }
};

const fetchOrders = async (userId) => {
    if (!userId) return [];

    try {
        const url = `${ORDERS_URL}/user/${userId}?page=0&size=1000`;
        const response = await axios.get(url, { withCredentials: true });
        if (response.data.success && response.data.content) {
            return response.data.content.map(order => ({
                ...order,
                createdAt: new Date(order.createdAt).toISOString().split("T")[0],
                status: order.status,
            }));
        }
        return [];
    } catch (error) {
        console.error("Error fetching orders:", error);
        return [];
    }
};

const autoUpdateMembership = async (userId, accumulatedPoints) => {
    try {
        const response = await axios.post(
            `${API_URL}/auto-update`,
            null,
            {
                params: { userId, accumulatedPoints },
                withCredentials: true,
            }
        );
        return response.data;
    } catch (error) {
        console.error("Error auto-updating membership:", error);
        throw error;
    }
};

const MembershipPage = () => {
    const [membershipLevel, setMembershipLevel] = useState("UnRanked");
    const [nextMembershipLevel, setNextMembershipLevel] = useState("S-New");
    const [progress, setProgress] = useState(0);
    const [displayProgress, setDisplayProgress] = useState(0);
    const [totalAccumulatedAmount, setTotalAccumulatedAmount] = useState(0);
    const [memberships, setMemberships] = useState([]);
    const [userInfo, setUserInfo] = useState({ userId: null, firstName: "", lastName: "", phone: "" });
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [nextLevelRequirement, setNextLevelRequirement] = useState(0);
    const [remainingAmount, setRemainingAmount] = useState(0);

    useEffect(() => {
        const loadUserInfo = async () => {
            try {
                setLoading(true);
                const user = await fetchUserInfo();
                setUserInfo({
                    userId: user.userId || null,
                    firstName: user.firstName || "User",
                    lastName: user.lastName || "Name",
                    phone: user.phone || "N/A",
                });
            } catch (error) {
                console.error("Error loading user info:", error);
                setUserInfo({ userId: null, firstName: "User", lastName: "Name", phone: "N/A" });
            } finally {
                setLoading(false);
            }
        };
        loadUserInfo();
    }, []);

    useEffect(() => {
        const loadOrdersAndCalculateProgress = async () => {
            if (!userInfo.userId) return;

            try {
                setLoading(true);
                const ordersData = await fetchOrders(userInfo.userId);
                setOrders(ordersData);

                // Chỉ tính các đơn hàng có status: 3 (DELIVERED) và từ 01/01/2024
                const filteredOrders = ordersData.filter(
                    order => order.status === 5 && new Date(order.createdAt) >= new Date("2024-01-01")
                );
                const totalAmount = filteredOrders.reduce((sum, order) => {
                    return sum + (order.orderDetails?.reduce((total, detail) => total + (detail.unitPrice * detail.quantity * (1 + detail.vatRate)), 0) || 0);
                }, 0);

                setTotalAccumulatedAmount(totalAmount);
                const userPoints = Math.floor(totalAmount / 1000); // Tính điểm từ totalAccumulatedAmount
                setProgress(userPoints);

                // Tự động cập nhật userPoint và membership
                await autoUpdateMembership(userInfo.userId, userPoints);
            } catch (error) {
                console.error("Error loading orders or updating membership:", error);
                setOrders([]);
                setProgress(0);
                setTotalAccumulatedAmount(0);
            } finally {
                setLoading(false);
            }
        };
        loadOrdersAndCalculateProgress();
    }, [userInfo.userId]);

    useEffect(() => {
        const loadMemberships = async () => {
            try {
                setLoading(true);
                const data = await fetchMemberships();
                const sortedMemberships = data.sort((a, b) => a.mbsPoint - b.mbsPoint);
                setMemberships(sortedMemberships);

                let currentLevel = { mbsName: "UnRanked", mbsPoint: 0 };
                let currentLevelIndex = -1;

                for (let i = 0; i < sortedMemberships.length; i++) {
                    if (progress >= sortedMemberships[i].mbsPoint) {
                        currentLevel = sortedMemberships[i];
                        currentLevelIndex = i;
                    } else {
                        break;
                    }
                }

                setMembershipLevel(currentLevel.mbsName);

                let displayPoints = progress - currentLevel.mbsPoint;
                const nextLevelIndex = currentLevelIndex + 1;

                if (nextLevelIndex < sortedMemberships.length) {
                    const nextLevel = sortedMemberships[nextLevelIndex];
                    setNextMembershipLevel(nextLevel.mbsName);
                    const requiredPointsForNextLevel = nextLevel.mbsPoint - currentLevel.mbsPoint;
                    setNextLevelRequirement(requiredPointsForNextLevel);
                    setDisplayProgress(displayPoints);
                    setRemainingAmount(requiredPointsForNextLevel - displayPoints);
                } else {
                    setNextMembershipLevel("Max Level Reached");
                    setNextLevelRequirement(0);
                    setDisplayProgress(0);
                    setRemainingAmount(0);
                }
            } catch (error) {
                console.error("Error loading memberships:", error);
                setMemberships([]);
                setNextMembershipLevel("S-New");
                setNextLevelRequirement(0);
                setDisplayProgress(0);
                setRemainingAmount(0);
            } finally {
                setLoading(false);
            }
        };
        loadMemberships();
    }, [progress]);

    const getFullName = () => {
        return `${userInfo.firstName} ${userInfo.lastName}`.trim() || "User Name";
    };

    return (
        <div className="bg-[#f8f8fc] text-gray-900 min-h-screen flex flex-col" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="h-20 bg-red-600 text-white flex items-center px-4 shadow-md w-full">
                <Header />
            </header>
            <div className="flex flex-1 container mx-auto p-6 space-x-6">
                <Sidebar />
                <main className="flex-1 flex flex-col space-y-8">
                    <div className="bg-white rounded-lg shadow-md p-6 flex items-center justify-between">
                        <div className="flex items-center space-x-4">
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
                                        <h2 className="text-xl font-semibold text-[#8c1d6b]">{getFullName()}</h2>
                                        <p className="text-gray-600">{userInfo.phone}</p>
                                    </>
                                )}
                                {/* Thay đổi hiển thị từ totalAccumulatedAmount sang userPoints (progress) */}
                                <p className="text-red-500 font-bold">{progress.toLocaleString()} points</p>
                            </div>
                        </div>
                        <div className="flex items-center space-x-4">
                            <Medal size={40} className="text-yellow-500" />
                            <div className="flex flex-col text-right">
                                <h2 className="text-xl font-semibold text-[#8c1d6b]">
                                    {loading ? "Loading..." : membershipLevel}
                                </h2>
                            </div>
                        </div>
                    </div>

                    {/* Thanh tiến trình */}
                    <div className="bg-white rounded-lg shadow-md p-6 relative">
                        {loading ? (
                            <p className="text-center text-gray-700">Loading...</p>
                        ) : orders.length === 0 ? (
                            <p className="text-center text-gray-700">
                                Start your membership rank by making an order
                            </p>
                        ) : (
                            <>
                                <div className="relative w-full h-10">
                                    <img
                                        src="https://static.vecteezy.com/system/resources/thumbnails/000/439/863/small/Basic_Ui__28186_29.jpg"
                                        alt="start"
                                        className="absolute top-0 h-10 transition-all duration-500"
                                        style={{
                                            left: nextLevelRequirement > 0 ? `${(displayProgress / nextLevelRequirement) * 100}%` : "100%",
                                            transform: "translateX(-50%)",
                                        }}
                                    />
                                    <img
                                        src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABsAAAAbCAYAAACN1PRVAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAADnSURBVHgB7dQ7CsJAFIXhMyGDBEUsLC1S2OgG3IVgLRYuwY34ALW2DhauwMcKxE5sAiJYJCIBQ5Q8xmhrBpKQBIT8zcDc4oPhMkBAk7mijmfKBgkn8gYVQ5OfjeoAMSIuUwun2yo0VnzcZcIwRBxMwNQ/fjABKURAlkH3aWBnetR3yAZja94kcUwAWfBm3AWxXy60q4moaYZ1QFTM8xgs00bUSqLYh+OMgmZpLEibN0gcI0Br7/8JyAJjgFSmtIMssC/IWDfonrsger25lSjtIW6OEx77VLOsCxIslWfMsRzLsRz7c+wNLD45oCEwMZoAAAAASUVORK5CYII="
                                        alt="goal"
                                        className="absolute top-0 right-0 h-10"
                                        style={{ transform: "translateX(50%)" }}
                                    />
                                </div>
                                <div className="relative w-full h-4 bg-gray-300 rounded-full mt-2">
                                    <div
                                        className="absolute top-0 left-0 h-4 bg-red-500 rounded-full transition-all duration-500"
                                        style={{ width: nextLevelRequirement > 0 ? `${(displayProgress / nextLevelRequirement) * 100}%` : "100%" }}
                                    />
                                </div>
                                <p className="text-center mt-2 text-gray-700">
                                    {nextMembershipLevel === "Max Level Reached" ? (
                                        "You have reached the highest level!"
                                    ) : (
                                        <>
                                            You need to accumulate{" "}
                                            <span className="font-bold">{remainingAmount.toLocaleString()}</span> more points to reach{" "}
                                            <span className="font-bold">{nextMembershipLevel}</span>
                                        </>
                                    )}
                                </p>
                            </>
                        )}
                    </div>

                    <div className="bg-blue-700 text-white font-bold text-lg uppercase px-6 py-3 rounded-full flex items-center justify-between shadow-md border-2 border-blue-900 w-300">
                        <Star className="text-white-400 w-6 h-6 fill-white-400" />
                        Update Zmember membership benefits
                        <Star className="text-white-400 w-6 h-6 fill-white-400" />
                    </div>

                    <div className="bg-white rounded-lg shadow-md p-6">
                        <div className="overflow-x-auto">
                            <table className="w-full border-collapse border border-gray-300">
                                <thead>
                                    <tr className="bg-blue-600 text-white">
                                        <th className="border border-gray-300 px-4 py-2">Rank</th>
                                        <th className="border border-gray-300 px-4 py-2">Point</th>
                                        <th className="border border-gray-300 px-4 py-2">Voucher</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {loading ? (
                                        <tr>
                                            <td colSpan="3" className="text-center p-4">
                                                <p className="text-blue-500">Loading...</p>
                                            </td>
                                        </tr>
                                    ) : memberships.length === 0 ? (
                                        <tr>
                                            <td colSpan="3" className="text-center p-4">
                                                <p className="text-gray-500">No memberships found.</p>
                                            </td>
                                        </tr>
                                    ) : (
                                        memberships.map((membership) => (
                                            <tr key={membership.mbsId} className="text-center bg-gray-100 odd:bg-white">
                                                <td className="border border-gray-300 px-4 py-2 font-semibold text-center">
                                                    {membership.mbsName}
                                                </td>
                                                <td className="border border-gray-300 px-4 py-2 text-center">
                                                    {membership.mbsPoint.toLocaleString()}
                                                </td>
                                                <td className="border border-gray-300 px-4 py-2 text-center">
                                                    {membership.mbsDescription}
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </main>
            </div>
        </div>
    );
};

export default MembershipPage;