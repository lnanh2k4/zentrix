import { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Eye, Pencil, Trash, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import AddCategory from "./AddCategory";
import EditCategory from "./EditCategory";
import ViewCategoryDetail from "./ViewCategoryDetail";
import { showNotification } from "../NotificationPopup";
import { showConfirm } from "../ConfirmPopup"; // Added import

const CategoryTable = () => {
    const [categories, setCategories] = useState([]);
    const [allCategories, setAllCategories] = useState([]);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalItems, setTotalItems] = useState(0);
    const [searchQuery, setSearchQuery] = useState("");
    const [pageSize, setPageSize] = useState(10);

    const fetchAllCategories = async () => {
        try {
            setIsLoading(true);
            setCategories([]);
            const response = await axios.get("http://localhost:6789/api/v1/categories", {
                params: {
                    page: 0,
                    size: 1000,
                },
            });

            if (response.data.success) {
                const categoriesData = response.data.content.content || [];
                setAllCategories(categoriesData);
                updatePagination(categoriesData);
            } else {
                setAllCategories([]);
                setTotalPages(0);
                setTotalItems(0);
                showNotification("No categories found in the response.", 3000, "fail");
            }
        } catch (error) {
            console.error("Error fetching categories:", error);
            setAllCategories([]);
            setTotalPages(0);
            setTotalItems(0);
            showNotification(
                "Failed to fetch categories: " + (error.response?.data?.message || error.message),
                3000,
                "fail"
            );
        } finally {
            setIsLoading(false);
        }
    };

    const updatePagination = (data) => {
        const sortedCategories = [...data].sort((a, b) => b.cateId - a.cateId);
        const trimmedQuery = searchQuery.trim().toLowerCase();
        const filteredCategories = sortedCategories.filter((category) =>
            trimmedQuery
                ? category.cateId.toString().includes(trimmedQuery) ||
                  category.cateName.toLowerCase().includes(trimmedQuery)
                : true
        );

        const start = currentPage * pageSize;
        const end = start + pageSize;
        const paginatedCategories = filteredCategories.slice(start, end);

        setCategories(paginatedCategories);
        setTotalItems(filteredCategories.length);
        setTotalPages(Math.ceil(filteredCategories.length / pageSize));
    };

    useEffect(() => {
        fetchAllCategories();
    }, []);

    useEffect(() => {
        if (!allCategories.length) {
            setCategories([]);
            setTotalPages(0);
            setTotalItems(0);
            return;
        }
        updatePagination(allCategories);
    }, [searchQuery, currentPage, pageSize, allCategories]);

    const handlePageSizeChange = (event) => {
        const newSize = parseInt(event.target.value);
        setPageSize(newSize);
        setCurrentPage(0);
    };

    const handleCreateClick = () => {
        setIsAddModalOpen(true);
    };

    const handleCloseAddModal = () => {
        setIsAddModalOpen(false);
    };

    const handleEditClick = (category) => {
        setSelectedCategory(category);
        setIsEditModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setSelectedCategory(null);
    };

    const handleViewClick = (category) => {
        setSelectedCategory(category);
        setIsViewModalOpen(true);
    };

    const handleCloseViewModal = () => {
        setIsViewModalOpen(false);
        setSelectedCategory(null);
    };

    const handleEditSubmit = () => {
        fetchAllCategories();
        showNotification(
            `Category "${selectedCategory.cateName}" updated successfully!`,
            3000,
            "complete"
        );
        handleCloseEditModal();
    };

    const handleCreateSubmit = () => {
        fetchAllCategories();
        showNotification("Category created successfully!", 3000, "complete");
        handleCloseAddModal();
    };

    const handleDelete = async (category) => {
        const confirmed = await showConfirm(
            `Are you sure you want to delete category "${category.cateName}"?`,
            "fail"
        );

        if (!confirmed) {
            return;
        }

        try {
            setIsDeleting(category.cateId);
            const response = await axios.delete(
                `http://localhost:6789/api/v1/dashboard/categories/${category.cateId}`,
                {
                    withCredentials: true,
                }
            );
            if (response.data.success) {
                if (categories.length === 1 && currentPage > 0) {
                    setCurrentPage(currentPage - 1);
                }
                fetchAllCategories();
                showNotification(
                    `Category "${category.cateName}" deleted successfully!`,
                    3000,
                    "complete"
                );
            } else {
                throw new Error(response.data.message || "Failed to delete category");
            }
        } catch (error) {
            console.error("Error deleting category:", error);
            showNotification(
                "Failed to delete category: " + (error.response?.data?.message || error.message),
                3000,
                "fail"
            );
        } finally {
            setIsDeleting(null);
        }
    };

    const hasChildCategories = (category) => {
        return allCategories.some(
            (cat) => cat.parentCateId && cat.parentCateId.cateId === category.cateId
        );
    };

    const paddedCategories = () => {
        const rows = [...categories];
        while (rows.length < pageSize && rows.length < totalItems) rows.push(null);
        return rows;
    };

    const renderPagination = () => {
        if (totalPages === 0)
            return <p className="text-gray-500">No categories available</p>;

        const maxButtons = 5;
        const halfButtons = Math.floor(maxButtons / 2);
        let startPage = Math.max(0, currentPage - halfButtons);
        let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

        if (endPage - startPage + 1 < maxButtons) {
            startPage = Math.max(0, endPage - maxButtons + 1);
        }

        const pageButtons = [];

        pageButtons.push(
            <Button
                key="first"
                variant="outline"
                onClick={() => setCurrentPage(0)}
                disabled={currentPage === 0 || isLoading}
                className={`text-sm px-3 py-1 ${currentPage === 0 || isLoading ? "opacity-100 text-gray-700 border-gray-300" : ""}`}
            >
                First
            </Button>
        );

        pageButtons.push(
            <Button
                key="prev"
                variant="outline"
                onClick={() => currentPage > 0 && setCurrentPage(currentPage - 1)}
                disabled={currentPage === 0 || isLoading}
                className={`text-sm px-3 py-1 ${currentPage === 0 || isLoading ? "opacity-100 text-gray-700 border-gray-300" : ""}`}
            >
                Previous
            </Button>
        );

        for (let i = startPage; i <= endPage; i++) {
            pageButtons.push(
                <Button
                    key={i}
                    variant={currentPage === i ? "default" : "outline"}
                    className={currentPage === i ? "bg-blue-500 text-white" : ""}
                    onClick={() => setCurrentPage(i)}
                    disabled={isLoading}
                >
                    {i + 1}
                </Button>
            );
        }

        pageButtons.push(
            <Button
                key="next"
                variant="outline"
                onClick={() =>
                    currentPage < totalPages - 1 && setCurrentPage(currentPage + 1)
                }
                disabled={currentPage >= totalPages - 1 || isLoading}
                className={`text-sm px-3 py-1 ${currentPage >= totalPages - 1 || isLoading ? "opacity-100 text-gray-700 border-gray-300" : ""}`}
            >
                Next
            </Button>
        );

        pageButtons.push(
            <Button
                key="last"
                variant="outline"
                onClick={() => setCurrentPage(totalPages - 1)}
                disabled={currentPage >= totalPages - 1 || isLoading}
                className={`text-sm px-3 py-1 ${currentPage >= totalPages - 1 || isLoading ? "opacity-100 text-gray-700 border-gray-300" : ""}`}
            >
                Last
            </Button>
        );

        return pageButtons;
    };

    return (
        <div className="bg-white w-full p-4 shadow-md rounded-lg overflow-x-auto animate-neonTable">
            <div className="flex justify-between mb-4">
                <Button
                    className="bg-blue-500 text-white hover:bg-blue-600"
                    onClick={handleCreateClick}
                >
                    Create Category <Plus className="h-4 w-4 ml-2" />
                </Button>
                <div className="flex items-center gap-2">
                    <Input
                        type="text"
                        placeholder="Search by ID or name..."
                        value={searchQuery}
                        onChange={(e) => {
                            setSearchQuery(e.target.value);
                            setCurrentPage(0);
                            setCategories([]);
                        }}
                        className="w-64"
                    />
                </div>
            </div>
            <AddCategory
                isOpen={isAddModalOpen}
                onClose={handleCloseAddModal}
                onSubmit={handleCreateSubmit}
                categories={categories}
            />
            <EditCategory
                isOpen={isEditModalOpen}
                onClose={handleCloseEditModal}
                onSubmit={handleEditSubmit}
                category={selectedCategory}
                categories={categories}
            />
            <ViewCategoryDetail
                isOpen={isViewModalOpen}
                onClose={handleCloseViewModal}
                category={selectedCategory}
                categories={categories}
            />

            <div className="overflow-y-auto min-h-[528px]">
                <table className="w-full border-collapse border border-gray-300 table-fixed">
                    <thead>
                        <tr className="bg-blue-500 text-white">
                            <th className="border p-2 font-bold w-[80px]">ID</th>
                            <th className="border p-2 font-bold w-[200px]">
                                Category Name
                            </th>
                            <th className="border p-2 font-bold w-[200px]">
                                Parent Category
                            </th>
                            <th className="border p-2 font-bold w-[150px]">
                                Actions
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            Array(pageSize)
                                .fill(null)
                                .map((_, index) => (
                                    <tr key={index} className="h-[53px]">
                                        <td className="border p-2 w-[80px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[200px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[200px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[150px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                    </tr>
                                ))
                        ) : categories.length === 0 && !isLoading ? (
                            <tr>
                                <td
                                    colSpan="4"
                                    className="border p-2 text-center"
                                >
                                    {searchQuery
                                        ? `No categories found for "${searchQuery}"`
                                        : "No categories available"}
                                </td>
                            </tr>
                        ) : (
                            paddedCategories().map((category, index) =>
                                category ? (
                                    <tr
                                        key={category.cateId}
                                        className="text-center h-[53px]"
                                    >
                                        <td className="border p-2">
                                            {category.cateId}
                                        </td>
                                        <td className="border p-2">
                                            {category.cateName}
                                        </td>
                                        <td className="border p-2">
                                            {category.parentCateId
                                                ? category.parentCateId.cateName
                                                : "None"}
                                        </td>
                                        <td className="border p-2">
                                            <div className="flex justify-center gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() =>
                                                        handleViewClick(category)
                                                    }
                                                    aria-label={`View category ${category.cateName}`}
                                                >
                                                    <Eye className="h-4 w-4 text-blue-600" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() =>
                                                        handleEditClick(category)
                                                    }
                                                    aria-label={`Edit category ${category.cateName}`}
                                                >
                                                    <Pencil className="h-4 w-4 text-yellow-600" />
                                                </Button>
                                                {!hasChildCategories(category) && (
                                                    <Button
                                                        variant="outline"
                                                        size="icon"
                                                        onClick={() =>
                                                            handleDelete(category)
                                                        }
                                                        disabled={
                                                            isDeleting ===
                                                            category.cateId
                                                        }
                                                        aria-label={`Delete category ${category.cateName}`}
                                                    >
                                                        {isDeleting ===
                                                        category.cateId ? (
                                                            <div className="h-4 w-4 border-t-2 border-red-600 rounded-full animate-spin" />
                                                        ) : (
                                                            <Trash className="h-4 w-4 text-red-600" />
                                                        )}
                                                    </Button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ) : (
                                    <tr key={index} className="h-[53px]">
                                        <td
                                            className="border p-2"
                                            colSpan="4"
                                        ></td>
                                    </tr>
                                )
                            )
                        )}
                    </tbody>
                </table>
            </div>

            <div className="flex justify-end items-center mt-4">
                <div className="flex items-center gap-2 whitespace-nowrap">
                    <label htmlFor="pageSize" className="text-sm">
                        Items per page:
                    </label>
                    <select
                        id="pageSize"
                        value={pageSize}
                        onChange={handlePageSizeChange}
                        className="border p-1 rounded"
                        disabled={isLoading}
                    >
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={15}>15</option>
                        <option value={20}>20</option>
                    </select>
                </div>
            </div>
            <div className="flex justify-center items-center gap-2 mt-4">
                {renderPagination()}
            </div>
        </div>
    );
};

export default CategoryTable;