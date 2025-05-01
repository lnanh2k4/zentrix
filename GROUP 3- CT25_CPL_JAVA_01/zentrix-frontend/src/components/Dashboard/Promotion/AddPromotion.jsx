import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { CalendarIcon, Loader2 } from "lucide-react";
import { format } from "date-fns";
import { DateRange } from "react-date-range";
import "react-date-range/dist/styles.css";
import "react-date-range/dist/theme/default.css";
import { showNotification } from '../NotificationPopup';

const AddPromotion = ({ isOpen, onClose, onSubmit }) => {
    const defaultPromotionData = {
        promName: "",
        promCode: "",
        discount: "",
        quantity: "",
        startDate: new Date(),
        endDate: new Date(),
        createdBy: null,
    };

    const [promotionData, setPromotionData] = useState(defaultPromotionData);
    const [isDatePickerOpen, setIsDatePickerOpen] = useState(false);
    const [errors, setErrors] = useState({});
    const [userId, setUserId] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [isFormTouched, setIsFormTouched] = useState(false); // Thêm state để kiểm tra form đã được tương tác chưa
    const datePickerRef = useRef(null);

    const promNameRegex = /^[a-zA-Z0-9_ ]+$/;
    const promCodeRegex = /^[a-zA-Z0-9_]+$/;

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success) {
                setUserId(response.data.content.userId);
            } else {
                console.error("No user info found");
                setUserId(null);
            }
        } catch (error) {
            console.error("Error fetching user info:", error.response?.data || error.message);
            setUserId(null);
        }
    };

    const checkPromCodeExists = async (promCode) => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/promotions/check-promcode", {
                params: { promCode: promCode.trim() },
                withCredentials: true,
            });
            return response.data.exists;
        } catch (error) {
            console.error("Error checking promCode:", error.response?.data || error.message);
            return false;
        }
    };

    useEffect(() => {
        if (isOpen) {
            fetchUserInfo();
            setPromotionData(defaultPromotionData);
            setErrors({});
            setIsFormTouched(false); // Reset khi mở form
            setIsDatePickerOpen(false);
        }
    }, [isOpen]);

    const validateForm = async () => {
        const newErrors = {};

        const trimmedPromName = promotionData.promName.trim();
        if (!trimmedPromName) newErrors.promName = "Promotion Name is required";
        else if (!promNameRegex.test(trimmedPromName))
            newErrors.promName = "Promotion Name can only contain letters(a-Z, 0-9), numbers, underscores, and spaces";

        const trimmedPromCode = promotionData.promCode.trim();
        if (!trimmedPromCode) newErrors.promCode = "Promotion Code is required";
        else if (!promCodeRegex.test(trimmedPromCode))
            newErrors.promCode = "Promotion Code can only contain letters, numbers, and underscores";
        else if (await checkPromCodeExists(trimmedPromCode)) newErrors.promCode = "Promotion Code already exists";

        const discountNum = parseFloat(promotionData.discount);
        if (!promotionData.discount) newErrors.discount = "Discount is required";
        else if (isNaN(discountNum) || discountNum < 1 || discountNum > 100) newErrors.discount = "Discount must be between 1 and 100";

        const quantityNum = parseInt(promotionData.quantity);
        if (!promotionData.quantity) newErrors.quantity = "Quantity is required";
        else if (isNaN(quantityNum) || quantityNum < 1) newErrors.quantity = "Quantity must be 1 or greater";

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        let newValue = value;

        if (name === "promCode") {
            newValue = value.toUpperCase().replace(/\s/g, "");
        } else if (name === "discount" || name === "quantity") {
            newValue = name === "discount" ? value.replace(/[^0-9.]/g, "") : value.replace(/[^0-9]/g, "");
        }

        setPromotionData(prev => ({ ...prev, [name]: newValue }));
        setIsFormTouched(true); // Đánh dấu form đã được tương tác
    };

    const handleDateRangeChange = (ranges) => {
        const { startDate, endDate } = ranges.selection;
        setPromotionData({ ...promotionData, startDate, endDate });
        setIsFormTouched(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const isValid = await validateForm();
        if (!isValid) return;

        if (!userId) {
            showNotification("User information not available. Please log in again.", 3000, 'fail');
            return;
        }

        setIsLoading(true);

        try {
            const payload = {
                promName: promotionData.promName.trim(),
                promCode: promotionData.promCode.trim(),
                discount: parseFloat(promotionData.discount),
                quantity: parseInt(promotionData.quantity),
                startDate: promotionData.startDate?.toISOString(),
                endDate: promotionData.endDate?.toISOString(),
                promStatus: 1,
                createdBy: userId,
            };

            const response = await axios.post("http://localhost:6789/api/v1/promotions", payload, {
                headers: { "Content-Type": "application/json" },
                withCredentials: true,
            });

            showNotification("Promotion created successfully!", 3000, 'complete');
            onSubmit(response.data.result?.data || payload);
            setPromotionData(defaultPromotionData);
            setErrors({});
            setIsDatePickerOpen(false);
            onClose();
        } catch (error) {
            console.error("Error creating promotion:", error.response?.data || error.message);
            showNotification(
                `Failed to create promotion: ${error.response?.data?.message || error.message}`,
                3000,
                'fail'
            );
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancel = () => {
        setPromotionData(defaultPromotionData);
        setErrors({});
        setIsFormTouched(false);
        setIsDatePickerOpen(false);
        onClose();
    };

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (datePickerRef.current && !datePickerRef.current.contains(event.target)) {
                setIsDatePickerOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 pointer-events-auto">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md z-50 pointer-events-auto overflow-visible">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold">Create Promotion</h2>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4 overflow-visible">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Promotion Name</label>
                        <input
                            type="text"
                            name="promName"
                            value={promotionData.promName}
                            onChange={handleInputChange}
                            className={`mt-1 block w-full border rounded-md p-2 ${errors.promName ? "border-red-500" : "border-gray-300"} focus:ring-2 focus:ring-blue-500`}
                            required
                            disabled={isLoading}
                        />
                        {errors.promName && <p className="text-red-500 text-xs mt-1">{errors.promName}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Promotion Code</label>
                        <input
                            type="text"
                            name="promCode"
                            value={promotionData.promCode}
                            onChange={handleInputChange}
                            className={`mt-1 block w-full border rounded-md p-2 ${errors.promCode ? "border-red-500" : "border-gray-300"} focus:ring-2 focus:ring-blue-500`}
                            required
                            disabled={isLoading}
                        />
                        {errors.promCode && <p className="text-red-500 text-xs mt-1">{errors.promCode}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Discount (%)</label>
                        <input
                            type="text"
                            name="discount"
                            value={promotionData.discount}
                            onChange={handleInputChange}
                            className={`mt-1 block w-full border rounded-md p-2 ${errors.discount ? "border-red-500" : "border-gray-300"} focus:ring-2 focus:ring-blue-500`}
                            required
                            disabled={isLoading}
                        />
                        {errors.discount && <p className="text-red-500 text-xs mt-1">{errors.discount}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Quantity</label>
                        <input
                            type="text"
                            name="quantity"
                            value={promotionData.quantity}
                            onChange={handleInputChange}
                            className={`mt-1 block w-full border rounded-md p-2 ${errors.quantity ? "border-red-500" : "border-gray-300"} focus:ring-2 focus:ring-blue-500`}
                            required
                            disabled={isLoading}
                        />
                        {errors.quantity && <p className="text-red-500 text-xs mt-1">{errors.quantity}</p>}
                    </div>
                    <div className="relative" ref={datePickerRef}>
                        <label className="block text-sm font-medium text-gray-700">Date Range</label>
                        <Button
                            variant="outline"
                            className="w-full flex justify-between"
                            onClick={(e) => {
                                e.preventDefault();
                                setIsDatePickerOpen(!isDatePickerOpen);
                            }}
                            disabled={isLoading}
                        >
                            {promotionData.startDate && promotionData.endDate
                                ? `${format(promotionData.startDate, "PPP")} - ${format(promotionData.endDate, "PPP")}`
                                : "Pick a date range"}
                            <CalendarIcon className="ml-2 h-4 w-4" />
                        </Button>
                        {isDatePickerOpen && (
                            <div
                                className="absolute bottom-full left-0 mb-1 z-[9999] bg-white border rounded-md shadow-lg"
                                onClick={(e) => e.stopPropagation()}
                            >
                                <DateRange
                                    ranges={[{ startDate: promotionData.startDate, endDate: promotionData.endDate, key: "selection" }]}
                                    onChange={handleDateRangeChange}
                                    moveRangeOnFirstSelection={false}
                                    showDateDisplay={false}
                                    minDate={new Date()}
                                    disabled={isLoading}
                                />
                            </div>
                        )}
                    </div>
                    <div className="flex justify-end gap-2">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleCancel}
                            aria-label="cancel"
                            disabled={isLoading}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className={`text-white flex items-center gap-2 ${isLoading ? "bg-gray-400" : "bg-blue-500 hover:bg-blue-600"}`}
                            aria-label="save"
                            disabled={isLoading}
                        >
                            {isLoading ? (
                                <>
                                    <Loader2 className="animate-spin" size={20} />
                                    Saving...
                                </>
                            ) : (
                                "Save"
                            )}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddPromotion;