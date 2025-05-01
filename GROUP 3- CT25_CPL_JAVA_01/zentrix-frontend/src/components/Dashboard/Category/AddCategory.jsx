import { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";

const AddCategory = ({ isOpen, onClose, onSubmit }) => {
    const defaultCategoryData = {
        cateName: "",
        parentCateId: null,
    };

    const [categoryData, setCategoryData] = useState(defaultCategoryData);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [allCategories, setAllCategories] = useState([]); // State to hold all categories
    const [isLoadingCategories, setIsLoadingCategories] = useState(false);

    // Fetch all categories when the modal opens
    useEffect(() => {
        if (isOpen) {
            const fetchAllCategories = async () => {
                try {
                    setIsLoadingCategories(true);
                    const response = await axios.get("http://localhost:6789/api/v1/categories", {
                        params: {
                            page: 0,
                            size: 1000, // Fetch all categories (adjust size as needed)
                        },
                    });
                    if (response.data.success) {
                        setAllCategories(response.data.content.content || []);
                    } else {
                        setAllCategories([]);
                        console.warn("No categories found in response.");
                    }
                } catch (error) {
                    console.error("Error fetching categories:", error);
                    setAllCategories([]);
                    alert("Failed to load categories: " + (error.response?.data?.message || error.message));
                } finally {
                    setIsLoadingCategories(false);
                }
            };
            fetchAllCategories();
        }
    }, [isOpen]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setCategoryData((prev) => ({
            ...prev,
            [name]: name === "parentCateId" ? (value ? parseInt(value) : null) : value,
        }));
    };

    const validateForm = () => {
        if (!categoryData.cateName.trim()) {
            alert("Category name is required.");
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
            const response = await axios.post("http://localhost:6789/api/v1/dashboard/categories", payload, {
                headers: { "Content-Type": "application/json" },
                withCredentials: true,
            });
            if (response.data.success) {
                onSubmit(response.data.content);
                setCategoryData(defaultCategoryData);
                onClose();
            } else {
                throw new Error(response.data.message || "Failed to create category");
            }
        } catch (error) {
            console.error("Error creating category:", error);
            alert("Failed to create category: " + (error.response?.data?.message || error.message));
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
                    <h2 className="text-xl font-bold">Create Category</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-gray-700 text-2xl leading-none" aria-label="Close modal">
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
                            disabled={isLoadingCategories}
                        >
                            <option value="">None</option>
                            {isLoadingCategories ? (
                                <option>Loading categories...</option>
                            ) : (
                                allCategories.map((cat) => (
                                    <option key={cat.cateId} value={cat.cateId}>
                                        {cat.cateName}
                                    </option>
                                ))
                            )}
                        </select>
                    </div>
                    <div className="flex justify-end gap-2">
                        <Button type="button" variant="outline" onClick={handleCancel} disabled={isSubmitting}>
                            Cancel
                        </Button>
                        <Button type="submit" className="bg-blue-500 text-white hover:bg-blue-600" disabled={isSubmitting || isLoadingCategories}>
                            {isSubmitting ? <div className="h-5 w-5 border-t-2 border-white rounded-full animate-spin" /> : "Save"}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddCategory;