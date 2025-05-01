import { Link, NavLink, useNavigate } from "react-router-dom";
import { Home, History, Gift, Medal, User, LogOut, LayoutDashboard, Shield, Info } from "lucide-react"; // Added Info for About Us
import axios from "axios";
import { checkUserRole, useUserInfo } from "@/services/InfoService";

const Sidebar = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            const response = await axios.post(
                "http://localhost:6789/api/v1/auth/sign-out",
                {},
                { withCredentials: true }
            );
            console.log("Logout response:", response.data);
            navigate("/");
        } catch (error) {
            console.error("Error during logout:", error.response?.data || error.message);
            navigate("/");
        }
    };
    const { user, loading } = useUserInfo();
    const role = checkUserRole(user);
    return (
        <aside className="w-64 bg-white shadow-md h-screen p-4">
            <nav className="space-y-2">
                <NavItem icon={<Home size={20} />} label="Home" path="/home" />
                <NavItem icon={<History size={20} />} label="Order History" path="/history" />
                <NavItem icon={<Gift size={20} />} label="Promotion" path="/myPromotionPage" />
                {(role.isCustomer()) && (
                    <NavItem icon={<User size={20} />} label="Profile" path="/profile" />
                )}
                <NavItem icon={<Medal size={20} />} label="Membership" path="/membershipPage" />
                <NavItem icon={<Shield size={20} />} label="Warranty" path="/MyWarrantyPage" />
                {(role.isWarehouseStaff() || role.isAdmin() || role.isSellerStaff() || role.isShipperStaff()) && (
                    <NavItem icon={<LayoutDashboard size={20} />} label="DashBoard" path="/dashboard/*" />
                )}
                <NavItem icon={<Info size={20} />} label="About Us" path="/about" />
                <div
                    onClick={handleLogout}
                    className="flex items-center p-3 rounded-lg cursor-pointer transition hover:bg-gray-100 text-gray-700"
                >
                    <LogOut size={20} />
                    <span className="ml-3">Logout</span>
                </div>
            </nav>
        </aside>
    );
};

const NavItem = ({ icon, label, path }) => {
    return (
        <NavLink
            to={path}
            className={({ isActive }) =>
                `flex items-center p-3 rounded-lg cursor-pointer transition ${isActive
                    ? "bg-blue-500 text-white font-bold shadow-lg"
                    : "hover:bg-gray-100 text-gray-700"
                }`
            }
        >
            {icon}
            <span className="ml-3">{label}</span>
        </NavLink>
    );
};

export default Sidebar;