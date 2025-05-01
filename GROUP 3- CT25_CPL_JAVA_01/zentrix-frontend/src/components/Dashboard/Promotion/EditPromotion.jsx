import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { CalendarIcon, Loader2 } from "lucide-react";
import { format } from "date-fns";
import { DateRange } from "react-date-range";
import "react-date-range/dist/styles.css";
import "react-date-range/dist/theme/default.css";
import { showNotification } from '../NotificationPopup';

const EditPromotion = ({ isOpen, onClose, onSubmit, promotion }) => {
    const defaultPromotionData = {
        promName: "",
        promCode: "",
        discount: "",
        quantity: "",
        startDate: new Date(),
        endDate: new Date(),
    };

    const [promotionData, setPromotionData] = useState(defaultPromotionData);
    const [isDatePickerOpen, setIsDatePickerOpen] = useState(false);
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false); // Thêm trạng thái loading
    const datePickerRef = useRef(null);

    const promNameRegex = /^[a-zA-Z0-9_ ]+$/;
    const promCodeRegex = /^[a-zA-Z0-9_]+$/;

    useEffect(() => {
        if (promotion && isOpen) {
            setPromotionData({
                promName: promotion.promName || "",
                promCode: promotion.promCode || "",
                discount: promotion.discount || "",
                quantity: promotion.quantity || "",
                startDate: promotion.startDate ? new Date(promotion.startDate) : new Date(),
                endDate: promotion.endDate ? new Date(promotion.endDate) : new Date(),
            });
            setErrors({});
            setIsDatePickerOpen(false);
        }
    }, [promotion, isOpen]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        if (name === "quantity" || name === "discount") {
            const filteredValue = value.replace(/[^0-9]/g, "");
            setPromotionData({ ...promotionData, [name]: filteredValue });
        } else {
            setPromotionData({ ...promotionData, [name]: value });
        }
    };

    const handleDateRangeChange = (ranges) => {
        const { startDate, endDate } = ranges.selection;
        setPromotionData({ ...promotionData, startDate, endDate });
    };

    const validateForm = () => {
        const newErrors = {};
        const trimmedPromName = promotionData.promName.trim();
        const trimmedPromCode = promotionData.promCode.trim();
        const discountNum = parseFloat(promotionData.discount);
        const quantityNum = parseInt(promotionData.quantity);

        if (!trimmedPromName) {
            newErrors.promName = "Promotion Name is required";
        } else if (!promNameRegex.test(trimmedPromName)) {
            newErrors.promName = "Promotion Name can only contain letters, numbers, underscores, and spaces";
        }

        if (!trimmedPromCode) {
            newErrors.promCode = "Promotion Code is required";
        } else if (!promCodeRegex.test(trimmedPromCode)) {
            newErrors.promCode = "Promotion Code can only contain letters, numbers, and underscores (no spaces)";
        }

        if (!promotionData.discount) {
            newErrors.discount = "Discount is required";
        } else if (isNaN(discountNum) || discountNum < 1 || discountNum > 100) {
            newErrors.discount = "Discount must be between 1 and 100";
        }

        if (!promotionData.quantity) {
            newErrors.quantity = "Quantity is required";
        } else if (isNaN(quantityNum) || quantityNum < 1) {
            newErrors.quantity = "Quantity must be 1 or greater";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;

        setIsLoading(true); // Bật trạng thái loading

        try {
            const updatedData = {
                promName: promotionData.promName.trim(),
                promCode: promotionData.promCode.trim(),
                discount: parseFloat(promotionData.discount),
                quantity: parseInt(promotionData.quantity),
                startDate: promotionData.startDate.toISOString(),
                endDate: promotionData.endDate.toISOString(),
                promStatus: promotion.promStatus,
            };

            await axios.put(
                `http://localhost:6789/api/v1/promotions/${promotion.promId}`,
                updatedData,
                {
                    headers: { "Content-Type": "application/json" },
                    withCredentials: true,
                }
            );

            showNotification("Promotion updated successfully!", 3000, 'complete');
            onSubmit(updatedData);
            setPromotionData(defaultPromotionData);
            setErrors({});
            setIsDatePickerOpen(false);
            onClose();
        } catch (error) {
            console.error("Error updating promotion:", error.response?.data || error.message);
            showNotification(
                `Failed to update promotion: ${error.response?.data?.message || error.message}`,
                3000,
                'fail'
            );
        } finally {
            setIsLoading(false); // Tắt trạng thái loading
        }
    };

    const handleCancel = () => {
        setPromotionData(defaultPromotionData);
        setErrors({});
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
        <div className="fixed inset-0 flex items-center justify-center z-50 bg-black/30 pointer-events-auto">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md overflow-visible pointer-events-auto">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold">Edit Promotion</h2>
                    <button
                        onClick={onClose}
                        className="text-gray-500 hover:text-gray-700 text-2xl leading-none"
                        disabled={isLoading}
                    >
                        ×
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4 overflow-visible">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Promotion Name</label>
                        <input
                            type="text"
                            name="promName"
                            value={promotionData.promName}
                            disabled
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500"
                        />
                        {errors.promName && <p className="text-red-500 text-xs mt-1">{errors.promName}</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Promotion Code</label>
                        <input
                            type="text"
                            name="promCode"
                            value={promotionData.promCode}
                            disabled
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500"
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
                                ? `${format(promotionData.startDate, "dd/MM/yyyy")} - ${format(promotionData.endDate, "dd/MM/yyyy")}`
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
                            disabled={isLoading}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className={`text-white flex items-center gap-2 ${Object.keys(errors).length > 0 || isLoading ? "bg-gray-400" : "bg-blue-500 hover:bg-blue-600"}`}
                            disabled={isLoading || Object.keys(errors).length > 0}
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

export default EditPromotion;