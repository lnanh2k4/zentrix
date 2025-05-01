import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useState } from "react";
import { getInfo, login, logout } from "@/context/ApiContext";
import GoogleLoginButton from "./GoogleLoginButton";
import { checkUserRole } from "@/services/InfoService";
import { useNavigate } from "react-router-dom";
import { showNotification } from "../Dashboard/NotificationPopup";
import VerifyEmailPopup from "../Homepage/Auth/VerifyEmailPopUp";
import { FaEye, FaEyeSlash } from "react-icons/fa";

const LoginForm = ({ className, ...props }) => {
  const [error, setError] = useState("");
  const [userInfo, setUserInfo] = useState(null);
  const [showVerifyPopup, setShowVerifyPopup] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const VERIFYING_STATUS = 2;
  const LOCKED_STATUS = [3, 4];

  const handleSubmit = async (event) => {
    event.preventDefault(); // Prevent default form submission

    const formData = {
      username: event.target.username.value,
      password: event.target.password.value,
    };

    try {
      // Send login request only when form is submitted
      await login(formData);
      setError("");
      const user = await getInfo();
      setUserInfo(user.content);
      console.log("User Login", user);

      const status = user.content?.status;

      // Handle locked account
      if (LOCKED_STATUS.includes(status)) {
        showNotification("Your account has been locked. Please contact support.", 3000, "fail");
        await logout();
        return;
      }

      // Handle unverified account
      if (status === VERIFYING_STATUS) {
        setShowVerifyPopup(true);
        await logout();
        return;
      }

      // Navigate based on user role
      const role = checkUserRole(user.content);
      navigate(role.isCustomer() ? "/" : "/dashboard");
    } catch (error) {
      console.error("Login error:", error.message);
      setError(error.message || "Login failed");
      setUserInfo(null);
      showNotification("Username or Password is incorrect", 3000, "fail");
    }
  };

  const handleClosePopup = () => {
    setShowVerifyPopup(false);
  };

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid gap-4">
          <div>
            <Label htmlFor="username" className="block text-sm font-medium text-gray-600 mb-1">
              Username
            </Label>
            <Input
              id="username"
              name="username"
              type="text"
              placeholder="Input Username"
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
            />
          </div>
          <div className="relative">
            <Label htmlFor="password" className="block text-sm font-medium text-gray-600 mb-1">
              Password
            </Label>
            <Input
              id="password"
              name="password"
              type={showPassword ? "text" : "password"}
              autoComplete="current-password"
              placeholder="Input Password"
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200 pr-10"
            />
            <button
              type="button"
              className="absolute right-3 top-10 text-gray-600 hover:text-gray-800"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <FaEyeSlash /> : <FaEye />}
            </button>
            <div className="text-right mt-1">
              <a
                href="/forgot-password/email"
                className="text-sm text-gray-600 underline-offset-4 hover:underline transition duration-200"
              >
                Forgot your password?
              </a>
            </div>
          </div>
        </div>

        <div className="flex justify-center gap-4 pt-4">
          <Button
            type="submit"
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition duration-200 w-full md:w-auto"
          >
            Login
          </Button>
          <GoogleLoginButton />
        </div>

        <div className="mt-4 text-center text-sm text-gray-600">
          Don’t have an account?{" "}
          <a href="/register" className="underline underline-offset-4 hover:text-blue-600 transition duration-200">
            Sign up
          </a>
        </div>
      </form>

      {showVerifyPopup && <VerifyEmailPopup email={userInfo?.email} onClose={handleClosePopup} />}
    </div>
  );
};

export default LoginForm;