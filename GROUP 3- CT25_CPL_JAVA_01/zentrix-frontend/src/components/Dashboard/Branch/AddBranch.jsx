import { useState } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { showNotification } from "../NotificationPopup";

const AddBranch = ({ isOpen, onClose, onSubmit }) => {
    const defaultBranchData = {
        brchName: "",
        address: "",
        phone: "",
    };

    const [branchData, setBranchData] = useState(defaultBranchData);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleInputChange = (e) => {
        const { name, value } = e.target;

        if (name === "phone") {
            const allowedCharactersForPhone = /^[0-9]*$/;
            if (allowedCharactersForPhone.test(value)) {
                setBranchData({ ...branchData, [name]: value });
            }
        } else {
            setBranchData({ ...branchData, [name]: value });
        }
    };

    const validateForm = () => {
        const noSpecialCharsForName = /^[A-Za-zÀ-ỹ0-9\s]*$/;
        const noSpecialCharsForAddress = /^[A-Za-zÀ-ỹ0-9\s,]*$/;
        const onlyNumbersForPhone = /^[0-9]*$/;

        if (!branchData.brchName.trim()) {
            showNotification("Branch name is required.", 3000, "fail");
            return false;
        }
        if (!noSpecialCharsForName.test(branchData.brchName)) {
            showNotification(
                "Branch name can only contain letters (including accented Vietnamese letters), numbers, and spaces.",
                3000,
                "fail"
            );
            return false;
        }
        if (!branchData.address.trim()) {
            showNotification("Address is required.", 3000, "fail");
            return false;
        }
        if (!noSpecialCharsForAddress.test(branchData.address)) {
            showNotification(
                "Address can only contain letters (including accented Vietnamese letters), numbers, spaces, and commas.",
                3000,
                "fail"
            );
            return false;
        }
        if (!branchData.phone.trim()) {
            showNotification("Phone is required.", 3000, "fail");
            return false;
        }
        if (!onlyNumbersForPhone.test(branchData.phone)) {
            showNotification("Phone number must contain only digits.", 3000, "fail");
            return false;
        }
        const phoneRegex = /^[0-9]{10,15}$/;
        if (!phoneRegex.test(branchData.phone)) {
            showNotification("Phone number must be between 10 and 15 digits long.", 3000, "fail");
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) return;
        setIsSubmitting(true);
        try {
            const payload = {
                brchName: branchData.brchName,
                address: branchData.address,
                phone: branchData.phone,
                status: 1, // Thêm status với giá trị mặc định là 1 (Active)
            };
            const response = await axios.post("http://localhost:6789/api/v1/branches", payload, {
                headers: { "Content-Type": "application/json" },
            });
            if (response.data.success) {
                showNotification("Branch created successfully!", 3000, "complete");
                onSubmit(response.data.content);
                setBranchData(defaultBranchData);
                onClose();
            } else {
                throw new Error(response.data.message || "Failed to create branch");
            }
        } catch (error) {
            console.error("Error creating branch:", error);
            showNotification(
                "Failed to create branch: " + (error.response?.data?.message || error.message),
                3000,
                "fail"
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleCancel = () => {
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black/30">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-bold">Create Branch</h2>
                    <button
                        onClick={onClose}
                        className="text-gray-500 hover:text-gray-700 text-2xl leading-none"
                        aria-label="Close modal"
                    >
                        ×
                    </button>
                </div>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="brchName" className="block text-sm font-medium text-gray-700">
                            Branch Name
                        </label>
                        <input
                            id="brchName"
                            type="text"
                            name="brchName"
                            value={branchData.brchName}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="address" className="block text-sm font-medium text-gray-700">
                            Address
                        </label>
                        <input
                            id="address"
                            type="text"
                            name="address"
                            value={branchData.address}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div>
                        <label htmlFor="phone" className="block text-sm font-medium text-gray-700">
                            Phone
                        </label>
                        <input
                            id="phone"
                            type="text"
                            name="phone"
                            value={branchData.phone}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    <div className="flex justify-end gap-2">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={handleCancel}
                            disabled={isSubmitting}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            className="bg-blue-500 text-white hover:bg-blue-600"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? (
                                <div className="h-5 w-5 border-t-2 border-white rounded-full animate-spin" />
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

export default AddBranch;