import LoginForm from "@/components/ui/login-form";
import { getInfo } from "@/context/ApiContext";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const Login = () => {
    const navigate = useNavigate();
    const checkLogin = async () => {
        const user = await getInfo();
        if (user) {
            navigate("/")
        }
    };

    useEffect(() => {
        checkLogin();
    }, []);
    const handleLogoClick = () => {
        navigate("/")
    };
    return (
        <GoogleOAuthProvider clientId="753911456221-h2pdnvqol3h4tf6c40h62at9v3j6v3mi.apps.googleusercontent.com">
            <div
                className="h-screen w-screen flex items-center justify-center p-4 bg-cover bg-center bg-no-repeat bg-fixed overflow-hidden"
                style={{ backgroundImage: "url('/register_background.jpg')" }}
            >
                <div className="bg-white rounded-xl shadow-xl p-8 max-w-3xl w-full">
                    <div className="rp-8 mb-1 max-w-3xl w-full flex flex-col items-center">
                        <img
                            alt="Zentrix Logo"
                            className="h-12 w-auto object-contain drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)] cursor-pointer"
                            src="/logo_zentrix.png"
                            style={{ filter: "drop-shadow(0px 10px 10px rgba(0, 0, 0, 0.8))" }}
                            onClick={handleLogoClick}
                        />
                    </div>
                    <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">Login to Your Account</h2>
                    <LoginForm />
                </div>
            </div>
        </GoogleOAuthProvider>
    );
};

export default Login;