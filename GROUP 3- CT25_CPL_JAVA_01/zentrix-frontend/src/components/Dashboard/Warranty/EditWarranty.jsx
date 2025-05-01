import React, { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { showNotification } from '../NotificationPopup';

const EditWarranty = ({ isOpen, onClose, onSubmit, warranty }) => {
    const defaultWarrantyData = {
        status: 1, // Giá trị mặc định là "In Progress"
    };

    const [warrantyData, setWarrantyData] = useState(defaultWarrantyData);
    const [isLoading, setIsLoading] = useState(false); // Thêm trạng thái loading

    useEffect(() => {
        if (warranty && isOpen) {
            setWarrantyData({
                status: warranty.status !== undefined ? warranty.status : 1,
            });
        }
    }, [warranty, isOpen]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        if (name === "status") {
            setWarrantyData({ ...warrantyData, [name]: value });
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);

        try {
            const updatedData = {
                warnEndDate: warranty.warnEndDate,
                status: parseInt(warrantyData.status),
                description: warranty.description,
                receive: warranty.receive,
            };

            await axios.put(
                `http://localhost:6789/api/v1/warranties/${warranty.warnId}/${updatedData.status}`,
                updatedData,
                { headers: { "Content-Type": "application/json" }, withCredentials: true }
            );

            onSubmit(updatedData); // Cập nhật bảng ngay lập tức
            setWarrantyData(defaultWarrantyData);
            onClose();

            // Tách biệt thông báo để không ảnh hưởng tới render
            setTimeout(() => {
                showNotification("Warranty updated successfully!", 3000, 'complete');
            }, 0);
        } catch (error) {
            console.error("Error updating warranty:", error);
            showNotification(
                `Failed to update warranty: ${error.response?.data?.message || error.message}`,
                3000,
                'fail'
            );
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancel = () => {
        setWarrantyData(defaultWarrantyData);
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold">Edit Warranty Status</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-gray-700 text-2xl leading-none">×</button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Status</label>
                        <select
                            name="status"
                            value={warrantyData.status}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            disabled={isLoading} // Vô hiệu hóa khi đang loading
                        >
                            <option value={1}>In Progress</option>
                            <option value={2}>Has Done</option>
                        </select>
                    </div>

                    <div className="flex justify-end gap-2">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleCancel}
                            disabled={isLoading} // Vô hiệu hóa nút Cancel khi đang loading
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className="bg-blue-500 text-white hover:bg-blue-600 flex items-center gap-2"
                            disabled={isLoading} // Vô hiệu hóa nút Save khi đang loading
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

export default EditWarranty;