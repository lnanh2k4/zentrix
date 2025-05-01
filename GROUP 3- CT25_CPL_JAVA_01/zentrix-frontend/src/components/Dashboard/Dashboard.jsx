import { useEffect, useState } from "react";
import {
    Users,
    UserCheck,
    Gift,
    Truck,
    Building,
    Tag,
    Bell,
    Star,
    FileText,
    Package,
    Layers,
    ChevronDown,
    ChevronRight,
    ChevronLeft,
} from "lucide-react";
import userAvatar from "/logouser.png";
import ProductTable from "./Product/ProductTable";
import PromotionTable from "./Promotion/PromotionTable";
import BranchTable from "./Branch/BranchTable";
import PostTable from "./Post/PostTable";
import ReviewTable from "./Review/ReviewTable";
import StaffTable from "./Staff/StaffTable";
import SupplierTable from "./Supplier/SupplierTable";
import MembershipTable from "./Membership/MembershipTable";
import NotificationTable from "./Notification/NotificationTable";
import Header from "@/components/ui/HeaderDashboard";
import Footer from "@/components/ui/FooterDashboard";
import CategoryTable from "./Category/CategoryTable";
import WarrantyTable from "./Warranty/WarrantyTable";
import CustomerTable from "./Customer/CustomerTable";
import InventoryTable from "./Inventory/InventoryTable";
import IncomeStatsPage from "./IncomeStatistic/IncomeStatsPage";
import OrderTable from "./Order/OrderTable";
import Profile from "./Profile/Profile"; // Thêm import Profile
import { useUserInfo, checkUserRole } from "@/services/InfoService";

const Dashboard = () => {
    const [selectedTab, setSelectedTab] = useState(null);
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);
    const [expandedMenus, setExpandedMenus] = useState({});
    const [showProfile, setShowProfile] = useState(false); // Thêm state mới
    const { user, loading } = useUserInfo();
    const role = checkUserRole(user);

    useEffect(() => {
        if (!loading && user !== null && role.isCustomer()) {
            window.location.href = '/'
        }
    }, [user, loading, role]);

    const groupedMenuItems = [
        ...(role.isAdmin() || role.isWarehouseStaff() ? [{
            name: "Products & Inventory",
            icon: <Package />,
            children: [
                { name: "Product", icon: <Package /> },
                { name: "Inventory", icon: <Layers /> },
            ],
        }] : []),
        ...(role.isAdmin() ? [{
            name: "Staff & Customers",
            icon: <Users />,
            children: [
                ...(role.isAdmin() ? [{ name: "Customers", icon: <Users /> }] : []),
                ...(role.isAdmin() ? [{ name: "Staffs", icon: <UserCheck /> }] : []),
                { name: "Memberships", icon: <Gift /> },
            ],
        }] : []),
        ...(role.isAdmin() || role.isSellerStaff ? [{
            name: "Operations",
            icon: <Truck />,
            children: [
                { name: "Orders", icon: <Package /> },
                ...(role.isAdmin() ? [{ name: "Suppliers", icon: <Truck /> }] : []),
                ...(role.isAdmin() ? [{ name: "Branches", icon: <Building /> }] : []),
            ],
        }] : []),
        ...(role.isAdmin() || role.isWarehouseStaff() || role.isSellerStaff ? [{
            name: "Content",
            icon: <FileText />,
            children: [
                ...(role.isAdmin() || role.isSellerStaff() ? [{ name: "Promotions", icon: <Tag /> }] : []),
                ...(role.isAdmin() || role.isSellerStaff() ? [{ name: "Notifications", icon: <Bell /> }] : []),
                ...(role.isAdmin() ? [{ name: "Review", icon: <Star /> }] : []),
                ...(role.isAdmin() || role.isSellerStaff() ? [{ name: "Post", icon: <FileText /> }] : []),
                ...(role.isAdmin() || role.isWarehouseStaff() ? [{ name: "Category", icon: <Layers /> }] : []),
                ...(role.isAdmin() || role.isSellerStaff() ? [{ name: "Warranty", icon: <Package /> }] : []),
            ],
        }] : []),
    ];

    const toggleMenu = (menuName) => {
        setExpandedMenus((prev) => ({
            ...prev,
            [menuName]: !prev[menuName],
        }));
    };

    const getTabTitle = (tab) => {
        const titles = {
            Product: "Product Management",
            Promotions: "Promotions Management",
            Customers: "Customer Management",
            Staffs: "Staff Management",
            Memberships: "Membership Management",
            Suppliers: "Suppliers Management",
            Branches: "Branch Management",
            Notifications: "Notification Management",
            Review: "Review Management",
            Post: "Post Management",
            Category: "Category Management",
            Inventory: "Inventory Management",
            Warranty: "Warranty Management",
            Orders: "Order Management",
        };
        return showProfile ? "Profile" : (titles[tab] || "Dashboard Overview");
    };

    const renderTabContent = (tab) => {
        if (showProfile) {
            return <Profile />; // Hiển thị component Profile khi showProfile = true
        }

        if (!tab) {
            return <IncomeStatsPage />;
        }

        const components = {
            Product: <ProductTable />,
            Promotions: <PromotionTable />,
            Branches: <BranchTable />,
            Post: <PostTable posts={[]} />,
            Review: <ReviewTable reviews={[]} />,
            Staffs: <StaffTable />,
            Suppliers: <SupplierTable />,
            Category: <CategoryTable />,
            Memberships: <MembershipTable />,
            Notifications: <NotificationTable notifications={[]} />,
            Warranty: <WarrantyTable />,
            Inventory: <InventoryTable />,
            Customers: <CustomerTable />,
            Orders: <OrderTable />,
        };

        return components[tab];
    };

    return (
        <>
            {!loading && (<div className="min-h-screen flex flex-col">
                <header className="h-16 bg-blue-700 text-white flex items-center px-4 shadow-md">
                    <Header />
                </header>

                <div className="flex flex-1">
                    <aside
                        className={`bg-black text-white p-4 shadow-xl transition-all duration-300 ${isSidebarOpen ? "w-64" : "w-20"
                            } bg-cover bg-center`}
                        style={{ backgroundImage: "url('/sideBarWallpaper.jpg')" }}
                    >
                        <button
                            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                            className="text-white p-2 hover:bg-blue-800 rounded-full transition flex items-center justify-center w-10 h-10"
                        >
                            {isSidebarOpen ? <ChevronLeft className="w-6 h-6" /> : <ChevronRight className="w-6 h-6" />}
                        </button>

                        <div className="flex flex-col items-center my-4">
                            <div
                                className="w-20 h-20 rounded-full bg-gray-400 flex items-center justify-center overflow-hidden border-2 border-white shadow-md cursor-pointer"
                                onClick={() => {
                                    setShowProfile(!showProfile);
                                    setSelectedTab(null); // Reset selectedTab khi show profile
                                }}
                            >
                                <img
                                    src={userAvatar}
                                    alt="User Avatar"
                                    className="w-full h-full object-cover animate-neonTable"
                                />
                            </div>
                            {isSidebarOpen && (
                                <div className="flex flex-col items-center mt-3">
                                    <span className="font-semibold bg-white text-black rounded-2xl">{`${user?.firstName} ${user?.lastName}`}</span>
                                </div>
                            )}
                        </div>

                        <nav className="mt-4">
                            <ul className="space-y-2">
                                {groupedMenuItems.map((item) => (
                                    <li key={item.name}>
                                        <div
                                            className={`opacity-95 bg-blue-200 text-black flex items-center justify-between p-3 rounded-lg cursor-pointer hover:bg-blue-500 transition`}
                                            onClick={() => toggleMenu(item.name)}
                                        >
                                            <div className="flex items-center gap-4">
                                                {item.icon}
                                                {isSidebarOpen && <span>{item.name}</span>}
                                            </div>
                                            {isSidebarOpen &&
                                                (expandedMenus[item.name] ? (
                                                    <ChevronDown className="w-5 h-5" />
                                                ) : (
                                                    <ChevronRight className="w-5 h-5" />
                                                ))}
                                        </div>

                                        <div
                                            className={`overflow-hidden transition-all duration-300 ease-in-out ${expandedMenus[item.name] && isSidebarOpen ? "max-h-96" : "max-h-0"
                                                }`}
                                        >
                                            {item.children && (
                                                <ul className="ml-8 space-y-2 mt-2">
                                                    {item.children.map((child) => (
                                                        <li
                                                            key={child.name}
                                                            className={`opacity-99 bg-blue-100 text-black flex items-center gap-4 p-2 rounded-lg cursor-pointer hover:bg-blue-400 transition ${selectedTab === child.name && !showProfile ? "bg-blue-600 text-white font-bold" : ""
                                                                }`}
                                                            onClick={() => {
                                                                setSelectedTab(selectedTab === child.name ? null : child.name);
                                                                setShowProfile(false); // Tắt profile khi chọn tab khác
                                                            }}
                                                        >
                                                            {child.icon}
                                                            <span>{child.name}</span>
                                                        </li>
                                                    ))}
                                                </ul>
                                            )}
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        </nav>
                    </aside>

                    <main
                        className="flex-1 p-8 shadow-lg overflow-x-auto overflow-y-auto rounded-lg bg-cover bg-center"
                        style={{ backgroundImage: "url('/mainDashboard.jpg')" }}
                    >
                        <h2 className="text-white text-4xl font-extrabold tracking-wide drop-shadow-md relative group transition duration-500 hover:text-[#73fdd8] hover:scale-102 animate-subtleGlow py-7">
                            {getTabTitle(selectedTab)}
                        </h2>
                        {renderTabContent(selectedTab)}
                    </main>
                </div>

                <Footer />
            </div>)}
        </>
    );
};

export default Dashboard;