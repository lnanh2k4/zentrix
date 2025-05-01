import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";

const AddMembership = ({ isOpen, onClose, onCreate, initialData, isViewing = false }) => {
    const [membership, setMembership] = useState(
        initialData || { mbsName: "", mbsDescription: "", mbsPoint: "" }
    );
    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (initialData) {
            setMembership(initialData);
            setErrors({});
        }
    }, [initialData]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setMembership((prev) => ({ ...prev, [name]: value }));

        if (name === "mbsName" && value && !/^[a-zA-Z\s]*$/.test(value)) {
            setErrors((prev) => ({
                ...prev,
                mbsName: "Membership Name must contain only letters and spaces",
            }));
        } else if (name === "mbsPoint" && value && !/^\d*$/.test(value)) {
            setErrors((prev) => ({
                ...prev,
                mbsPoint: "Membership Point must contain only numbers",
            }));
        } else if (errors[name]) {
            setErrors((prev) => ({ ...prev, [name]: "" }));
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!membership.mbsName.trim()) {
            newErrors.mbsName = "Membership Name is required";
        } else if (!/^[a-zA-Z\s]*$/.test(membership.mbsName)) {
            newErrors.mbsName = "Membership Name must contain only letters";
        } else if (membership.mbsName.length < 3) {
            newErrors.mbsName = "Membership Name must be at least 3 characters";
        } else if (membership.mbsName.length > 50) {
            newErrors.mbsName = "Membership Name must not exceed 50 characters";
        }

        if (!membership.mbsPoint) {
            newErrors.mbsPoint = "Membership Point is required";
        } else if (!/^\d*$/.test(membership.mbsPoint)) {
            newErrors.mbsPoint = "Membership Point must contain only numbers";
        } else if (isNaN(membership.mbsPoint) || Number(membership.mbsPoint) < 0) {
            newErrors.mbsPoint = "Membership Point must be a positive number";
        } else if (Number(membership.mbsPoint) > 1000000) {
            newErrors.mbsPoint = "Membership Point must not exceed 1,000,000";
        }

        if (!membership.mbsDescription.trim()) {
            newErrors.mbsDescription = "Membership Description is required";
        } else if (membership.mbsDescription.length < 10) {
            newErrors.mbsDescription = "Membership Description must be at least 10 characters";
        } else if (membership.mbsDescription.length > 500) {
            newErrors.mbsDescription = "Membership Description must not exceed 500 characters";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!isViewing && validateForm()) {
            await onCreate(membership);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <div className="absolute inset-0 bg-black opacity-50" onClick={onClose}></div>

            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md z-10">
                <h2 className="text-xl font-bold mb-4">
                    {isViewing ? "Membership Detail" : initialData ? "Update Membership" : "Create Membership"}
                </h2>
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700">Membership Name</label>
                        <input
                            type="text"
                            name="mbsName"
                            value={membership.mbsName}
                            onChange={handleInputChange}
                            className={`mt-1 block w-full border ${errors.mbsName ? "border-red-500" : "border-gray-300"
                                } rounded-md p-2`}
                            readOnly={isViewing}
                        />
                        {errors.mbsName && <p className="text-red-500 text-xs mt-1">{errors.mbsName}</p>}
                    </div>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700">Membership Point</label>
                        <input
                            type="text"
                            name="mbsPoint"
                            value={membership.mbsPoint}
                            onChange={handleInputChange}
                            className={`mt-1 block w-full border ${errors.mbsPoint ? "border-red-500" : "border-gray-300"
                                } rounded-md p-2`}
                            readOnly={isViewing}
                        />
                        {errors.mbsPoint && <p className="text-red-500 text-xs mt-1">{errors.mbsPoint}</p>}
                    </div>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700">Membership Description</label>
                        <textarea
                            name="mbsDescription"
                            value={membership.mbsDescription}
                            onChange={handleInputChange}
                            className={`mt-1 block w-full border ${errors.mbsDescription ? "border-red-500" : "border-gray-300"
                                } rounded-md p-2 focus:outline-none focus:ring-2 focus:ring-blue-400`}
                            rows="5"
                            readOnly={isViewing}
                        />
                        {errors.mbsDescription && (
                            <p className="text-red-500 text-xs mt-1">{errors.mbsDescription}</p>
                        )}
                    </div>

                    <div className="flex justify-end gap-2">
                        <Button type="button" variant="outline" onClick={onClose}>
                            Cancel
                        </Button>
                        {!isViewing && (
                            <Button type="submit" className="bg-blue-500 text-white hover:bg-blue-600">
                                {initialData ? "Update" : "Save"}
                            </Button>
                        )}
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddMembership;