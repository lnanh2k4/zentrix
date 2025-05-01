import { useState, useEffect } from "react";
import { getInfo } from "@/context/ApiContext"; // Giả định getInfo đã được định nghĩa

// Định nghĩa các role constants
const GUEST_ROLE = 1;
const CUSTOMER_ROLE = 2;
const ADMIN_ROLE = 3;
const SELLER_STAFF_ROLE = 4;
const WAREHOUSE_STAFF_ROLE = 5;
const SHIPPER_STAFF_ROLE = 6;

// Hook để lấy thông tin người dùng
const useUserInfo = () => {
    const [user, setUser] = useState(null); // Khởi tạo là null thay vì mảng rỗng
    const [loading, setLoading] = useState(true); // Thêm trạng thái loading
    const [error, setError] = useState(null); // Thêm trạng thái lỗi

    const fetchUserInfo = async () => {
        try {
            setLoading(true);
            const response = await getInfo();
            console.log("User data:", response);
            if (response?.content) {
                setUser(response.content);
            } else {
                console.error("No content in user data");
                setError("No user data available");
            }
        } catch (error) {
            console.error("Error fetching user info:", error.message);
            setError(error.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUserInfo();
    }, []); // Chỉ chạy một lần khi mount

    return { user, loading, error, fetchUserInfo };
};

// Các hàm kiểm tra role
const checkUserRole = (user) => {
    return {
        isGuest: () => user?.roleId?.roleId === GUEST_ROLE,
        isCustomer: () => user?.roleId?.roleId === CUSTOMER_ROLE,
        isAdmin: () => user?.roleId?.roleId === ADMIN_ROLE,
        isSellerStaff: () => user?.roleId?.roleId === SELLER_STAFF_ROLE,
        isWarehouseStaff: () => user?.roleId?.roleId === WAREHOUSE_STAFF_ROLE,
        isShipperStaff: () => user?.roleId?.roleId === SHIPPER_STAFF_ROLE,
        hasRole: (role) => user?.roleId?.roleId === role, // Kiểm tra role tùy chỉnh
    };
};


// Export hook và hàm kiểm tra role
export { useUserInfo, checkUserRole };