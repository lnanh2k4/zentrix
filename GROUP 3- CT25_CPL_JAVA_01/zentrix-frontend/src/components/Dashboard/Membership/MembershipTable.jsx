import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Eye, Edit, Trash2, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import AddMembership from "./AddMembership";
import axios from "axios";
import { showNotification } from "../NotificationPopup"; // Import Notification utility
import { showConfirm } from "../ConfirmPopup"; // Import Confirm utility

const API_URL = "http://localhost:6789/api/v1/memberships";

const fetchMemberships = async (page, size) => {
    try {
        const response = await axios.get(`${API_URL}?page=${page}&size=${size}`, { withCredentials: true });
        return {
            content: response.data.content || [],
            totalPages: response.data.pagination?.totalPages || 0,
        };
    } catch (error) {
        console.error("Error fetching memberships:", error);
        throw error;
    }
};

const MembershipTable = () => {
    const [memberships, setMemberships] = useState([]);
    const [filteredMemberships, setFilteredMemberships] = useState([]);
    const [isViewing, setIsViewing] = useState(false);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingMembership, setEditingMembership] = useState(null);
    const [mbsName, setMbsName] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);

    useEffect(() => {
        const loadMemberships = async () => {
            try {
                setLoading(true);
                setError("");
                const { content, totalPages } = await fetchMemberships(currentPage, pageSize);
                const sortedData = content.sort((a, b) => a.mbsId - b.mbsId);
                setMemberships(sortedData);
                setFilteredMemberships(sortedData);
                setTotalPages(totalPages);
            } catch (error) {
                setError("Unable to fetch memberships. Please try again later.");
                setMemberships([]);
                setFilteredMemberships([]);
                setTotalPages(0);
            } finally {
                setLoading(false);
            }
        };
        loadMemberships();
    }, [currentPage, pageSize]);

    useEffect(() => {
        if (!mbsName) {
            setFilteredMemberships(memberships);
            return;
        }
        const filtered = memberships.filter((membership) =>
            membership.mbsName.toLowerCase().includes(mbsName.toLowerCase())
        );
        setFilteredMemberships(filtered);
    }, [mbsName, memberships]);

    const handleCreateClick = () => {
        setEditingMembership(null);
        setIsViewing(false);
        setIsModalOpen(true);
    };

    const handleViewClick = (membership) => {
        setEditingMembership(membership);
        setIsViewing(true);
        setIsModalOpen(true);
    };

    const handleEditClick = (membership) => {
        setEditingMembership(membership);
        setIsViewing(false);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setEditingMembership(null);
        setIsViewing(false);
    };

    const handleDeleteMembership = async (membership) => {
        const confirmed = await showConfirm(`Are you sure you want to delete "${membership.mbsName}"?`, "fail");
        if (!confirmed) return;

        try {
            await axios.delete(`${API_URL}/${membership.mbsId}`, { withCredentials: true });
            setMemberships((prev) => prev.filter((item) => item.mbsId !== membership.mbsId));
            setFilteredMemberships((prev) => prev.filter((item) => item.mbsId !== membership.mbsId));
            if (filteredMemberships.length === 1 && currentPage > 0) setCurrentPage(currentPage - 1);
            showNotification("Membership deleted successfully!", 3000, "complete");
        } catch (error) {
            console.error("Error deleting membership:", error);
            showNotification(
                error.response?.status === 403
                    ? "You do not have permission to delete this membership."
                    : "Unable to delete membership. Please try again.",
                3000,
                "fail"
            );
        }
    };

    const handleSaveMembership = async (membership) => {
        try {
            const isEditing = Boolean(membership.mbsId);
            const method = isEditing ? "put" : "post";
            const url = isEditing ? `${API_URL}/${membership.mbsId}` : API_URL;
            const membershipData = { ...membership };
            if (!isEditing) delete membershipData.mbsId;

            const response = await axios({
                method,
                url,
                data: membershipData,
                withCredentials: true,
            });

            const updatedMembership = response.data.content;

            setMemberships((prev) => {
                const newList = isEditing
                    ? prev.map((item) => (item.mbsId === membership.mbsId ? updatedMembership : item))
                    : [...prev, updatedMembership];
                return newList.sort((a, b) => a.mbsId - b.mbsId);
            });

            setFilteredMemberships((prev) => {
                const newList = isEditing
                    ? prev.map((item) => (item.mbsId === membership.mbsId ? updatedMembership : item))
                    : [...prev, updatedMembership];
                return newList.sort((a, b) => a.mbsId - b.mbsId);
            });

            handleCloseModal();
            showNotification(
                isEditing ? "Membership updated successfully!" : "Membership created successfully!",
                3000,
                "complete"
            );
        } catch (error) {
            console.error("Error saving membership:", error);
            showNotification(
                error.response?.status === 403
                    ? "You do not have permission to create/update this membership."
                    : "Unable to save membership. Please try again.",
                3000,
                "fail"
            );
        }
    };

    const handlePageSizeChange = (event) => {
        const newSize = parseInt(event.target.value);
        setPageSize(newSize);
        setCurrentPage(0);
    };

    const handlePreviousPage = () => {
        if (currentPage > 0) setCurrentPage(currentPage - 1);
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) setCurrentPage(currentPage + 1);
    };

    const renderPagination = () => {
        if (totalPages === 0) return <p className="text-gray-500">No memberships available</p>;

        const maxButtons = 5;
        const halfButtons = Math.floor(maxButtons / 2);
        let startPage = Math.max(0, currentPage - halfButtons);
        let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

        if (endPage - startPage + 1 < maxButtons) {
            startPage = Math.max(0, endPage - maxButtons + 1);
        }

        const pageButtons = [];
        pageButtons.push(
            <Button key="first" variant="outline" onClick={() => setCurrentPage(0)} disabled={currentPage === 0 || loading}>
                First
            </Button>
        );
        pageButtons.push(
            <Button key="prev" variant="outline" onClick={handlePreviousPage} disabled={currentPage === 0 || loading}>
                Previous
            </Button>
        );
        if (startPage > 0) {
            pageButtons.push(<Button key="start-ellipsis" variant="outline" disabled>...</Button>);
        }
        for (let i = startPage; i <= endPage; i++) {
            pageButtons.push(
                <Button
                    key={i}
                    variant={currentPage === i ? "default" : "outline"}
                    onClick={() => setCurrentPage(i)}
                    disabled={loading}
                >
                    {i + 1}
                </Button>
            );
        }
        if (endPage < totalPages - 1) {
            pageButtons.push(<Button key="end-ellipsis" variant="outline" disabled>...</Button>);
        }
        pageButtons.push(
            <Button
                key="next"
                variant="outline"
                onClick={handleNextPage}
                disabled={currentPage >= totalPages - 1 || loading}
            >
                Next
            </Button>
        );
        pageButtons.push(
            <Button
                key="last"
                variant="outline"
                onClick={() => setCurrentPage(totalPages - 1)}
                disabled={currentPage >= totalPages - 1 || loading}
            >
                Last
            </Button>
        );

        return pageButtons;
    };

    return (
        <div className="bg-white p-2 shadow-md rounded-lg relative animate-neonTable">
            <div className="flex justify-between mb-4">
                <Button variant="default" className="bg-blue-500 text-white hover:bg-blue-600" onClick={handleCreateClick}>
                    Create <Plus className="h-4 w-4 ml-2" />
                </Button>
                <Input
                    type="text"
                    placeholder="Search membership..."
                    value={mbsName}
                    onChange={(e) => setMbsName(e.target.value)}
                    className="w-84"
                />
            </div>

            {loading && (
                <div className="text-center p-4">
                    <svg
                        className="animate-spin h-5 w-5 text-blue-500 inline-block"
                        xmlns="http://www.w3.org/2000/svg"
                        fill="none"
                        viewBox="0 0 24 24"
                    >
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                        ></path>
                    </svg>
                </div>
            )}
            {error && <p className="text-red-500 mb-4">{error}</p>}

            {isModalOpen && (
                <AddMembership
                    isOpen={isModalOpen}
                    onClose={handleCloseModal}
                    onCreate={handleSaveMembership}
                    initialData={editingMembership}
                    isViewing={isViewing}
                />
            )}

            <table className="w-full min-w-full border-collapse border border-gray-300">
                <thead>
                    <tr className="bg-blue-500">
                        <th className="border p-2 text-white font-bold">Id</th>
                        <th className="border p-2 text-white font-bold">Membership Name</th>
                        <th className="border p-2 text-white font-bold">Membership Point</th>
                        <th className="border p-2 text-white font-bold">Description</th>
                        <th className="border p-2 text-white font-bold">Action</th>
                    </tr>
                </thead>
                <tbody>
                    {loading ? (
                        <tr>
                            <td colSpan="5" className="text-center p-4">
                                <p className="text-blue-500">Loading...</p>
                            </td>
                        </tr>
                    ) : filteredMemberships.length === 0 ? (
                        <tr>
                            <td colSpan="5" className="text-center p-4">
                                <p className="text-gray-500">No memberships found.</p>
                            </td>
                        </tr>
                    ) : (
                        filteredMemberships.map((membership) => (
                            <tr key={membership.mbsId} className="text-center">
                                <td className="border p-2 text-zinc-950">{membership.mbsId}</td>
                                <td className="border p-2 text-zinc-950">{membership.mbsName}</td>
                                <td className="border p-2 text-zinc-950">{membership.mbsPoint}</td>
                                <td className="border p-2 text-zinc-950 max-w-xs truncate">{membership.mbsDescription}</td>
                                <td className="border p-2">
                                    <div className="flex justify-center gap-2">
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            onClick={() => handleViewClick(membership)}
                                            className="text-blue-600 hover:text-blue-800"
                                        >
                                            <Eye className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            onClick={() => handleEditClick(membership)}
                                            className="text-yellow-600 hover:text-yellow-800"
                                        >
                                            <Edit className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            onClick={() => handleDeleteMembership(membership)}
                                            className="text-red-600 hover:text-red-800"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </table>

            <div className="flex justify-end items-center mt-4">
                <div className="flex items-center gap-2 whitespace-nowrap">
                    <label htmlFor="pageSize" className="text-sm">Items per page:</label>
                    <select
                        id="pageSize"
                        value={pageSize}
                        onChange={handlePageSizeChange}
                        className="border p-1 rounded"
                        disabled={loading}
                    >
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={15}>15</option>
                        <option value={20}>20</option>
                    </select>
                </div>
            </div>
            <div className="flex justify-center items-center gap-2 mt-4">{renderPagination()}</div>
        </div>
    );
};

export default MembershipTable;