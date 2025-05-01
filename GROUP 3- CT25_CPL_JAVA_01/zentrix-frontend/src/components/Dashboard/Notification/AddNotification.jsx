import { useState } from "react";
import { Button } from "@/components/ui/button";
import axios from "axios";
import { showNotification } from '../NotificationPopup';
import { showConfirm } from '../ConfirmPopup';




const AddNotification = ({ onAdd, onClose }) => {
    const [formData, setFormData] = useState({
        title: "",
        description: "",
        status: 1,
    });

    const [errors, setErrors] = useState({
        title: "",
        description: "",
        general: "",
    });
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: name === "status" ? Number(value) : value,
        }));
        setErrors((prev) => ({ ...prev, [name]: "", general: "" }));
    };

    const validateForm = () => {
        let isValid = true;
        const newErrors = { title: "", description: "", general: "" };

        if (!formData.title.trim()) {
            newErrors.title = "Title is required";
            isValid = false;
        } else if (formData.title.length < 3) {
            newErrors.title = "Title must be at least 3 characters long";
            isValid = false;
        } else if (formData.title.length > 100) {
            newErrors.title = "Title cannot exceed 100 characters";
            isValid = false;
        }

        if (!formData.description.trim()) {
            newErrors.description = "Description is required";
            isValid = false;
        } else if (formData.description.length < 10) {
            newErrors.description = "Description must be at least 10 characters long";
            isValid = false;
        } else if (formData.description.length > 1000) {
            newErrors.description = "Description cannot exceed 1000 characters";
            isValid = false;
        }

        setErrors(newErrors);
        return isValid;
    };

    
  const fetchUserInfo = async () => {
    try {
      const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
        withCredentials: true,
      });
      if (response.data.success) {
        return response.data.content.userId;
      } else {
        return null;
      }
    } catch (error) {
      console.error("Error fetching user info:", error.response?.data || error.message);
      return null;
    }
  };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrors((prev) => ({ ...prev, general: "" }));
        setLoading(true);

        const userId = await fetchUserInfo();
        // console.log("USER O DAY NE: ",userId)
            if (!userId) {
              alert("Can not load user!!");
              return;
            }
            const confirmed = await showConfirm(
                `Are you sure you want to save?`,
                'create'
            );
            if (!confirmed) {
                setLoading(false);
                return; 
            }

        if (!validateForm()) {
            setLoading(false);
            return;
        }

       
        if (!userId) {
            setErrors((prev) => ({ ...prev, general: "⚠️ User ID is missing. Please login again." }));
            setLoading(false);
            return;
        }

        const payload = {
            ...formData,
            createdAt: new Date().toISOString(),
            createdById: Number(userId),
        };

        try {
            const response = await fetch("http://localhost:6789/api/v1/dashboard/notifications/add", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: JSON.stringify(payload),
            });

            const result = await response.json();

            if (response.ok && result.success) {
                showNotification("Notification created successfully", 3000, "complete");
                onAdd({
                    notiId: result.content?.notiId || Date.now(), 
                    title: formData.title,
                    description: formData.description,
                    status: formData.status,
                    createdAt: payload.createdAt,
                    createdById: userId,
                });
            } else {
                setErrors((prev) => ({
                    ...prev,
                    general: result.message || "❌ Failed to add notification.",
                }));
            }
        } catch (err) {
            console.error(err);
            setErrors((prev) => ({ ...prev, general: "❌ Server error. Please try again." }));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60 z-50 px-4">
            <div className="bg-white rounded-xl w-full max-w-xl p-6 shadow-xl relative">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-gray-500 hover:text-red-600"
                >
                    ✕
                </button>

                <h2 className="text-xl font-semibold mb-4">Add New Notification</h2>

                {errors.general && (
                    <div className="mb-4 text-sm text-red-600 font-medium">{errors.general}</div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block font-medium text-gray-700">Title</label>
                        <input
                            type="text"
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            className={`w-full p-2 border rounded ${
                                errors.title ? "border-red-500" : "border-gray-300"
                            }`}
                        />
                        {errors.title && (
                            <p className="text-red-500 text-sm mt-1">{errors.title}</p>
                        )}
                    </div>

                    <div>
                        <label className="block font-medium text-gray-700">Description</label>
                        <textarea
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            className={`w-full p-2 border rounded ${
                                errors.description ? "border-red-500" : "border-gray-300"
                            }`}
                            rows={3}
                        />
                        {errors.description && (
                            <p className="text-red-500 text-sm mt-1">{errors.description}</p>
                        )}
                    </div>
{/* 
                    <div>
                        <label className="block font-medium text-gray-700">Status</label>
                        <select
                            name="status"
                            value={formData.status}
                            onChange={handleChange}
                            className="w-full p-2 border rounded border-gray-300"
                        >
                            <option value={1}>Active</option>
                            <option value={0}>Inactive</option>
                        </select>
                    </div> */}

                    <div className="flex justify-end gap-3 pt-2">
                        <Button type="button" onClick={onClose} variant="outline">
                            Cancel
                        </Button>
                        <Button type="submit" disabled={loading} className="bg-blue-600 text-white hover:bg-blue-800">
                            {loading ? "Adding..." : "Submit"}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddNotification;