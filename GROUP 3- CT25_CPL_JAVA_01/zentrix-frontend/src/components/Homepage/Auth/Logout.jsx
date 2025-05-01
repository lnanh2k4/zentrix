import { logout } from "@/context/ApiContext";

const Logout = async () => {
    try {
        await logout() // "Logout successful"
        window.location.href = '/'
    } catch (error) {
        throw error.response.data;
    }

}
export default Logout