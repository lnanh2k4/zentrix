import { LogOut } from "lucide-react"; // Thay đổi import từ User sang LogOut
import { useNavigate } from "react-router-dom";
import './HeaderDashboard.css';
import { useUserInfo } from "@/services/InfoService";

const Header = () => {
  const navigate = useNavigate();
  const { user } = useUserInfo();
  const goToHomePage = () => {
    navigate("/");
  };

  const handleLogout = () => {
    // Thêm logic logout nếu cần (ví dụ: xóa token, clear storage)
    navigate("/logout");
  };

  return (
    <>
      <header className="shadow-lg fixed top-0 left-0 right-0 z-50 bg-[#0044cc] text-white py-4 overflow-visible">
        <div className="container mx-auto flex items-center justify-between px-8">
          <img
            src="/logo_zentrix.png"
            alt="Zentrix Logo"
            className="h-12 w-auto object-contain drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)] animate-logoSpin"
            style={{
              filter: "drop-shadow(0px 10px 10px rgba(0,0,0,0.8))",
            }}
            onClick={goToHomePage}
          />
          <p className="text-white text-3xl font-extrabold tracking-wide drop-shadow-md relative group transition duration-300 hover:text-[#6ac0ecfd] hover:scale-105 animate-subtleGlow">
            Zentrix Dashboard
            <span className="absolute left-0 bottom-[-4px] w-0 h-1 bg-[#A0BFE0] transition-all duration-300 group-hover:w-full"></span>
          </p>
          <nav className="flex items-center space-x-8 text-lg font-semibold">
            <span className="text-red-600 bg-white opacity-90 rounded-2xl p-1 text-xl font-bold tracking-wide drop-shadow-md relative group transition duration-300 hover:text-[#A0BFE0] hover:scale-105 animate-subtleGlow">
              {`${user?.roleId?.roleName}`}
            </span>
            <button
              className="relative group transition duration-300 hover:text-[#A0BFE0] cursor-pointer"
              onClick={handleLogout}
            >
              <LogOut size={35} />
              <span className="absolute left-0 top-11 w-0 h-1 bg-[#A0BFE0] transition-all duration-300 group-hover:w-full"></span>
            </button>
          </nav>
        </div>
      </header>
    </>
  );
};

export default Header;