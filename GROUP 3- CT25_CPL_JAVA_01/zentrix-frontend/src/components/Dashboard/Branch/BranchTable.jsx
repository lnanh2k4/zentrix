import { useState, useEffect, useMemo, useCallback } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Eye, Pencil, Trash, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import AddBranch from "./AddBranch";
import EditBranch from "./EditBranch";
import ViewBranchDetail from "./ViewBranchDetail";
import { showNotification } from "../NotificationPopup";
import { showConfirm } from "../ConfirmPopup"; // Added import

const BranchTable = () => {
    const [branches, setBranches] = useState([]);
    const [allBranches, setAllBranches] = useState([]);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedBranch, setSelectedBranch] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalItems, setTotalItems] = useState(0);
    const [searchQuery, setSearchQuery] = useState("");
    const [debouncedSearchQuery, setDebouncedSearchQuery] = useState("");
    const [pageSize, setPageSize] = useState(10);
    const [error, setError] = useState(null);
    const [branchProductCounts, setBranchProductCounts] = useState({});

    // Debounce search query
    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedSearchQuery(searchQuery);
        }, 300);
        return () => clearTimeout(handler);
    }, [searchQuery]);

    const fetchAllBranches = async () => {
        try {
            setIsLoading(true);
            setError(null);
            const response = await axios.get("http://localhost:6789/api/v1/branches", {
                params: {
                    page: 0,
                    size: 1000,
                },
            });

            if (response.data.success) {
                const branchesData = response.data.content || [];
                setAllBranches(branchesData);
                await fetchProductCounts(branchesData);
            } else {
                setBranches([]);
                setAllBranches([]);
                setTotalPages(0);
                setTotalItems(0);
                showNotification("No branches found in the response.", 3000, "fail");
            }
        } catch (error) {
            console.error("Error fetching branches:", error);
            setBranches([]);
            setAllBranches([]);
            setTotalPages(0);
            setTotalItems(0);
            const errorMessage = error.response?.data?.message || error.message;
            setError("Failed to fetch branches: " + errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    const fetchProductCounts = async (branches) => {
        try {
            const response = await axios.get(`http://localhost:6789/api/v1/products/productTypeBranchs`, {
                params: {
                    page: 0,
                    size: 1000,
                },
                withCredentials: true,
            });
            if (response.data.success) {
                const productTypeBranches = response.data.content || [];
                const counts = {};
                branches.forEach((branch) => {
                    counts[branch.brchId] = 0;
                });
                productTypeBranches.forEach((ptb) => {
                    if (ptb.brchId?.brchId) {
                        counts[ptb.brchId.brchId] = (counts[ptb.brchId.brchId] || 0) + 1;
                    }
                });
                setBranchProductCounts(counts);
            } else {
                const counts = {};
                branches.forEach((branch) => {
                    counts[branch.brchId] = 0;
                });
                setBranchProductCounts(counts);
            }
        } catch (error) {
            console.error("Error fetching product counts:", error);
            const counts = {};
            branches.forEach((branch) => {
                counts[branch.brchId] = 0;
            });
            setBranchProductCounts(counts);
        }
    };

    const { filteredBranches, paginatedBranches } = useMemo(() => {
        const trimmedQuery = debouncedSearchQuery.trim().toLowerCase();
        const filtered = allBranches.filter((branch) =>
            trimmedQuery
                ? branch.brchId.toString().includes(trimmedQuery) ||
                branch.brchName.toLowerCase().includes(trimmedQuery)
                : true
        );

        filtered.sort((a, b) => b.brchId - a.brchId);

        const start = currentPage * pageSize;
        const end = start + pageSize;
        const paginated = filtered.slice(start, end);

        return { filteredBranches: filtered, paginatedBranches: paginated };
    }, [debouncedSearchQuery, allBranches, currentPage, pageSize]);

    useEffect(() => {
        setBranches(paginatedBranches);
        setTotalItems(filteredBranches.length);
        setTotalPages(Math.ceil(filteredBranches.length / pageSize));
    }, [paginatedBranches, filteredBranches]);

    useEffect(() => {
        fetchAllBranches();
    }, []);

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

    const handleEditClick = (branch) => {
        setSelectedBranch(branch);
        setIsEditModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setSelectedBranch(null);
    };

    const handleViewClick = (branch) => {
        setSelectedBranch(branch);
        setIsViewModalOpen(true);
    };

    const handleCloseViewModal = () => {
        setIsViewModalOpen(false);
        setSelectedBranch(null);
    };

    const handleEditSubmit = (updatedBranch) => {
        setAllBranches((prevBranches) =>
            prevBranches.map((branch) =>
                branch.brchId === updatedBranch.brchId ? updatedBranch : branch
            )
        );
        fetchProductCounts(allBranches);
        handleCloseEditModal();
    };

    const handleCreateSubmit = (newBranch) => {
        setAllBranches((prevBranches) => [newBranch, ...prevBranches]);
        fetchProductCounts([newBranch, ...allBranches]);
        setCurrentPage(0);
        handleCloseAddModal();
    };

    const handleDelete = async (branch) => {
        if (!branch || !branch.brchId) {
            showNotification("Please ensure ALL branches have a valid branch ID.", 3000, "fail");
            return;
        }

        const confirmed = await showConfirm(
            `Are you sure you want to delete branch "${branch.brchName}"?`,
            "fail"
        );

        if (!confirmed) {
            return;
        }

        try {
            setIsDeleting(branch.brchId);
            const response = await axios.delete(`http://localhost:6789/api/v1/branches/${branch.brchId}`);
            if (response.data.success) {
                if (branches.length === 1 && currentPage > 0) {
                    setCurrentPage(currentPage - 1);
                }
                fetchAllBranches();
                showNotification(`Branch "${branch.brchName}" deleted successfully!`, 3000, "complete");
            } else {
                throw new Error(response.data.message || "Failed to delete branch");
            }
        } catch (error) {
            console.error("Error deleting branch:", error);
            const errorMessage = error.response?.data?.message || error.message;
            showNotification(
                `Failed to delete branch: ${errorMessage}. This branch might be associated with other records.`,
                3000,
                "fail"
            );
        } finally {
            setIsDeleting(null);
        }
    };

    const paddedBranches = useCallback(() => {
        const rows = [...branches];
        while (rows.length < pageSize && rows.length < totalItems) rows.push(null);
        return rows;
    }, [branches, pageSize, totalItems]);

    const renderPagination = () => {
        if (totalPages === 0) return <p className="text-gray-500">No branches available</p>;

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
                onClick={() => currentPage < totalPages - 1 && setCurrentPage(currentPage + 1)}
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

    if (error) {
        return (
            <div className="bg-white w-full p-4 shadow-md rounded-lg">
                <div className="text-red-500 text-center">{error}</div>
                <Button onClick={() => fetchAllBranches()} className="mt-4">
                    Retry
                </Button>
            </div>
        );
    }

    return (
        <div className="bg-white w-full p-4 shadow-md rounded-lg overflow-x-auto animate-neonTable">
            <div className="flex justify-between mb-4">
                <div className="flex gap-2">
                    <Button className="bg-blue-500 text-white hover:bg-blue-600" onClick={handleCreateClick}>
                        Create <Plus className="h-4 w-4 ml-2" />
                    </Button>
                </div>
                <div className="flex items-center gap-2">
                    <Input
                        type="text"
                        placeholder="Search branches by ID or name..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-64"
                    />
                </div>
            </div>

            <AddBranch isOpen={isAddModalOpen} onClose={handleCloseAddModal} onSubmit={handleCreateSubmit} />
            <EditBranch isOpen={isEditModalOpen} onClose={handleCloseEditModal} onSubmit={handleEditSubmit} branch={selectedBranch} />
            <ViewBranchDetail isOpen={isViewModalOpen} onClose={handleCloseViewModal} branch={selectedBranch} />

            <div className="overflow-y-auto min-h-[528px]">
                <table className="w-full border-collapse border border-gray-300 table-fixed">
                    <thead>
                        <tr className="bg-blue-500 text-white">
                            <th className="border p-2 font-bold w-[80px]">ID</th>
                            <th className="border p-2 font-bold w-[200px]">Branch Name</th>
                            <th className="border p-2 font-bold w-[200px]">Address</th>
                            <th className="border p-2 font-bold w-[120px]">Phone</th>
                            <th className="border p-2 font-bold w-[150px]">Actions</th>
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
                                        <td className="border p-2 w-[120px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[150px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                    </tr>
                                ))
                        ) : branches.length === 0 ? (
                            <tr>
                                <td colSpan="5" className="border p-2 text-center">
                                    {debouncedSearchQuery ? `No branches found for "${debouncedSearchQuery}".` : "No branches found."}
                                </td>
                            </tr>
                        ) : (
                            paddedBranches().map((branch, index) =>
                                branch ? (
                                    <tr key={branch.brchId || index} className="text-center h-[53px]">
                                        <td className="border p-2">{branch.brchId ?? "N/A"}</td>
                                        <td className="border p-2">{branch.brchName || "Unknown"}</td>
                                        <td className="border p-2">{branch.address || "None"}</td>
                                        <td className="border p-2">{branch.phone || "None"}</td>
                                        <td className="border p-2">
                                            <div className="flex justify-center gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleViewClick(branch)}
                                                    aria-label={`View branch ${branch.brchName}`}
                                                >
                                                    <Eye className="h-4 w-4 text-blue-600" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleEditClick(branch)}
                                                    aria-label={`Edit branch ${branch.brchName}`}
                                                >
                                                    <Pencil className="h-4 w-4 text-yellow-600" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleDelete(branch)}
                                                    disabled={isDeleting === branch.brchId}
                                                    aria-label={`Delete branch ${branch.brchName}`}
                                                >
                                                    {isDeleting === branch.brchId ? (
                                                        <div className="h-4 w-4 border-t-2 border-red-600 rounded-full animate-spin" />
                                                    ) : (
                                                        <Trash className="h-4 w-4 text-red-600" />
                                                    )}
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ) : (
                                    <tr key={index} className="h-[53px]">
                                        <td className="border p-2" colSpan="5"></td>
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

export default BranchTable;