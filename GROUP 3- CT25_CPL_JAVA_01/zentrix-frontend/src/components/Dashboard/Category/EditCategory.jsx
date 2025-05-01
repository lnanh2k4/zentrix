import { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";

const EditCategory = ({ isOpen, onClose, onSubmit, category, categories }) => {
    const defaultCategoryData = {
        cateName: "",
        parentCateId: null,
    };

    const [categoryData, setCategoryData] = useState(defaultCategoryData);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (category && isOpen) {
            setCategoryData({
                cateName: category.cateName || "",
                parentCateId: category.parentCateId ? category.parentCateId.cateId : null,
            });
        }
    }, [category, isOpen]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        if (name === "cateName") {
            // Loại bỏ ký tự đặc biệt và dấu, chỉ giữ chữ cái không dấu và khoảng trắng
            const sanitizedValue = value
                .normalize("NFD") // Tách dấu ra khỏi chữ
                .replace(/[\u0300-\u036f]/g, "") // Xóa các dấu
                .replace(/[^a-zA-Z\s]/g, ""); // Chỉ giữ chữ cái và khoảng trắng
            setCategoryData((prev) => ({
                ...prev,
                cateName: sanitizedValue,
            }));
        } else {
            setCategoryData((prev) => ({
                ...prev,
                [name]: name === "parentCateId" ? (value ? parseInt(value) : null) : value,
            }));
        }
    };

    const validateForm = () => {
        if (!categoryData.cateName.trim()) {
            alert("Category name is required.");
            return false;
        }
        if (!/^[a-zA-Z\s]+$/.test(categoryData.cateName)) {
            alert("Category name can only contain letters and spaces, no special characters or accents.");
            return false;
        }
        if (categoryData.parentCateId === category.cateId) {
            alert("A category cannot be its own parent.");
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
                cateName: categoryData.cateName,
                parentCateId: categoryData.parentCateId,
            };
            const response = await axios.put(`http://localhost:6789/api/v1/dashboard/categories/${category.cateId}`, payload, {
                headers: { "Content-Type": "application/json" },
                withCredentials: true,
            });
            if (response.data.success) {
                onSubmit(response.data.content);
                setCategoryData(defaultCategoryData);
                onClose();
            } else {
                throw new Error(response.data.message || "Failed to update category");
            }
        } catch (error) {
            console.error("Error updating category:", error);
            alert("Failed to update category: " + (error.response?.data?.message || error.message));
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleCancel = () => {
        onClose();
    };

    if (!isOpen || !category) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center bg-black/30">
            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
                <div className="flex justify-between items-center mb-4 relative">
                    <h2 className="text-xl font-bold text-left">Edit Category</h2>
                    <button 
                        onClick={onClose} 
                        className="absolute right-0 text-gray-500 hover:text-gray-700 text-2xl leading-none" 
                        aria-label="Close modal"
                    >
                        ×
                    </button>
                </div>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="cateName" className="block text-sm font-medium text-gray-700">Category Name</label>
                        <input
                            id="cateName"
                            type="text"
                            name="cateName"
                            value={categoryData.cateName}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                            required
                            placeholder="Only letters and spaces allowed"
                        />
                    </div>
                    <div>
                        <label htmlFor="parentCateId" className="block text-sm font-medium text-gray-700">Parent Category</label>
                        <select
                            id="parentCateId"
                            name="parentCateId"
                            value={categoryData.parentCateId || ""}
                            onChange={handleInputChange}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="">None</option>
                            {categories
                                .filter((cat) => cat.cateId !== category.cateId)
                                .map((cat) => (
                                    <option key={cat.cateId} value={cat.cateId}>
                                        {cat.cateName}
                                    </option>
                                ))}
                        </select>
                    </div>
                    <div className="flex justify-end gap-2">
                        <Button type="button" variant="outline" onClick={handleCancel} disabled={isSubmitting}>
                            Cancel
                        </Button>
                        <Button type="submit" className="bg-blue-500 text-white hover:bg-blue-600" disabled={isSubmitting}>
                            {isSubmitting ? <div className="h-5 w-5 border-t-2 border-white rounded-full animate-spin" /> : "Save"}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EditCategory;