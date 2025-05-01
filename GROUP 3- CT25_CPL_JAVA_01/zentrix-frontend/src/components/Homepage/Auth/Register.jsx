import React, { useState } from "react";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { showNotification } from "@/components/Dashboard/NotificationPopup";
import { register } from "@/context/ApiContext";
import { useNavigate } from "react-router-dom";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

const Register = ({ onSubmit = () => { } }) => {
    const defaultCustomerData = {
        username: "",
        email: "",
        firstName: "",
        lastName: "",
        dob: null,
        phone: "",
        address: "",
        sex: "",
        companyName: "",
        taxCode: "",
        password: "",
        confirmPassword: "",
    };
    const navigate = useNavigate();
    const [customerData, setCustomerData] = useState(defaultCustomerData);
    const [errors, setErrors] = useState({});

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCustomerData({ ...customerData, [name]: value });
        if (errors[name]) {
            setErrors({ ...errors, [name]: null });
        }
    };

    const handleDateChange = (date) => {
        setCustomerData({ ...customerData, dob: date });
        if (errors.dob) {
            setErrors({ ...errors, dob: null });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!customerData.username) newErrors.username = "Username is required";
        else if (customerData.username.length < 3) newErrors.username = "Username must be at least 3 characters";

        if (!customerData.firstName) newErrors.firstName = "First Name is required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(customerData.firstName))
            newErrors.firstName = "First Name can only contain Vietnamese letters, '-', or '_'";

        if (!customerData.lastName) newErrors.lastName = "Last Name is required";
        else if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(customerData.lastName))
            newErrors.lastName = "Last Name can only contain Vietnamese letters, '-', or '_'";

        if (!customerData.email) newErrors.email = "Email is required";
        else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(customerData.email)) newErrors.email = "Invalid email address";

        if (!customerData.dob) newErrors.dob = "Date of Birth is required";

        if (!customerData.phone) newErrors.phone = "Phone is required";
        else if (!/^[0-9]{0,15}$/.test(customerData.phone)) newErrors.phone = "Phone must be a number up to 15 digits";

        if (!customerData.address) newErrors.address = "Address is required";

        if (!customerData.sex) newErrors.sex = "Sex is required";

        if (!customerData.password) newErrors.password = "Password is required";
        else if (customerData.password.length < 8) newErrors.password = "Password must be at least 8 characters";
        else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).+$/.test(customerData.password))
            newErrors.password =
                "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character";

        if (!customerData.confirmPassword) newErrors.confirmPassword = "Please confirm your password";
        else if (customerData.password !== customerData.confirmPassword)
            newErrors.confirmPassword = "Passwords do not match";
        if (customerData.taxCode && !/^[0-9]{10,13}$/.test(customerData.taxCode))
            newErrors.taxCode = "Tax Code must be a number between 10-13 digits";
        if (!/^[A-Za-zÀ-ỹ\s\-_]*$/.test(customerData.companyName))
            newErrors.companyName = "Company name can only contain Vietnamese letters, '-', or '_'";
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        try {
            const payload = {
                username: customerData.username,
                email: customerData.email,
                firstName: customerData.firstName,
                lastName: customerData.lastName,
                dob: customerData.dob ? customerData.dob.toISOString() : null,
                phone: customerData.phone,
                address: customerData.address,
                sex: parseInt(customerData.sex),
                companyName: customerData.companyName,
                taxCode: customerData.taxCode,
                password: customerData.password,
            };

            const response = await register(payload);
            onSubmit(response.result?.data || payload);
            showNotification("Register account successfully", 3000, "complete");
            setErrors({});
            navigate("/login");
        } catch (error) {
            console.error("Error creating customer:", error.response?.message, error.response?.content);
            showNotification("Failed to register: " + (error.response?.content || error.message), 3000, "fail");
        }
    };

    const handleReset = () => {
        setCustomerData(defaultCustomerData);
        setErrors({});
    };

    const handleLoginRedirect = () => {
        window.location.href = "/login";
    };
    const handleLogoClick = () => {
        window.location.href = "/";
    };

    return (
        <div
            className="h-screen w-screen flex items-center justify-center p-4 bg-cover bg-center bg-no-repeat bg-fixed overflow-hidden"
            style={{ backgroundImage: "url('/register_background.jpg')" }}
        >
            <div className="bg-white rounded-xl shadow-xl p-6 max-w-lg w-full">
                <div className="mb-4 flex flex-col items-center">
                    <img
                        alt="Zentrix Logo"
                        className="h-12 w-auto object-contain drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)] cursor-pointer"
                        src="/logo_zentrix.png"
                        style={{ filter: "drop-shadow(0px 10px 10px rgba(0, 0, 0, 0.8))" }}
                        onClick={handleLogoClick}
                    />
                </div>

                <h2 className="text-2xl font-semibold text-center text-gray-800 mb-6">Register Your Account</h2>

                <form onSubmit={handleSubmit} className="space-y-2">
                    {/* Dòng 1: Username, First Name, Last Name */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Username</Label>
                            <input
                                type="text"
                                name="username"
                                value={customerData.username}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.username && <p className="text-red-500 text-xs mt-1">{errors.username}</p>}
                        </div>
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">First Name</Label>
                            <input
                                type="text"
                                name="firstName"
                                value={customerData.firstName}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.firstName && <p className="text-red-500 text-xs mt-1">{errors.firstName}</p>}
                        </div>
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Last Name</Label>
                            <input
                                type="text"
                                name="lastName"
                                value={customerData.lastName}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.lastName && <p className="text-red-500 text-xs mt-1">{errors.lastName}</p>}
                        </div>
                    </div>

                    {/* Dòng 2: Password, Confirm Password */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Password</Label>
                            <input
                                type="password"
                                name="password"
                                value={customerData.password}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
                        </div>
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Confirm Password</Label>
                            <input
                                type="password"
                                name="confirmPassword"
                                value={customerData.confirmPassword}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
                        </div>
                    </div>

                    {/* Dòng 3: Phone, Sex */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Phone</Label>
                            <input
                                type="text"
                                name="phone"
                                value={customerData.phone}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.phone && <p className="text-red-500 text-xs mt-1">{errors.phone}</p>}
                        </div>
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Sex</Label>
                            <select
                                name="sex"
                                value={customerData.sex}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            >
                                <option value="">Select sex</option>
                                <option value="1">Male</option>
                                <option value="0">Female</option>
                            </select>
                            {errors.sex && <p className="text-red-500 text-xs mt-1">{errors.sex}</p>}
                        </div>
                    </div>

                    {/* Dòng 4: Email, Date of Birth */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Email</Label>
                            <input
                                type="text"
                                name="email"
                                value={customerData.email}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                        </div>
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Date of Birth</Label>
                            <DatePicker
                                selected={customerData.dob}
                                onChange={handleDateChange}
                                dateFormat="dd/MM/yyyy"
                                maxDate={new Date()}
                                className="w-55 px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                                placeholderText="Select date"
                            />
                            {errors.dob && <p className="text-red-500 text-xs mt-1">{errors.dob}</p>}
                        </div>
                    </div>

                    {/* Dòng 5: Address */}
                    <div>
                        <Label className="block text-sm font-medium text-gray-600 mb-1">Address</Label>
                        <input
                            type="text"
                            name="address"
                            value={customerData.address}
                            onChange={handleInputChange}
                            className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                        />
                        {errors.address && <p className="text-red-500 text-xs mt-1">{errors.address}</p>}
                    </div>

                    {/* Dòng 6: Company Name, Tax Code */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Company Name</Label>
                            <input
                                type="text"
                                name="companyName"
                                value={customerData.companyName}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.companyName && <p className="text-red-500 text-xs mt-1">{errors.companyName}</p>}
                        </div>
                        <div>
                            <Label className="block text-sm font-medium text-gray-600 mb-1">Tax Code</Label>
                            <input
                                type="text"
                                name="taxCode"
                                value={customerData.taxCode}
                                onChange={handleInputChange}
                                className="w-full px-3 py-1.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-200"
                            />
                            {errors.taxCode && <p className="text-red-500 text-xs mt-1">{errors.taxCode}</p>}
                        </div>
                    </div>

                    {/* Buttons */}
                    <div className="flex justify-center gap-4 pt-4">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleReset}
                            className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-100 transition duration-200"
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition duration-200"
                        >
                            Register
                        </Button>
                    </div>

                    {/* Already have an account? Login */}
                    <div className="mt-4 text-center text-sm text-gray-600">
                        Already have an account?{" "}
                        <span
                            onClick={handleLoginRedirect}
                            className="underline underline-offset-4 hover:text-blue-600 transition duration-200 cursor-pointer"
                        >
                            Login
                        </span>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Register;