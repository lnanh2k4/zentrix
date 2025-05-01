import { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Eye, Edit, Trash2, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import AddPromotion from "./AddPromotion";
import EditPromotion from "./EditPromotion";
import ViewPromotionDetail from "./ViewPromotionDetail";

const PromotionTable = ({ onView = () => { }, onEdit = () => { }, onDelete = () => { }, onCreate = () => { } }) => {
    const [promotions, setPromotions] = useState([]);
    const [memberships, setMemberships] = useState([]);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedPromotion, setSelectedPromotion] = useState(null);
    const [selectedPromotionForView, setSelectedPromotionForView] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchQuery, setSearchQuery] = useState("");
    const [filterStatus, setFilterStatus] = useState("all");
    const [filterDate, setFilterDate] = useState("");
    const [userId, setUserId] = useState(null);
    const [isAdmin, setIsAdmin] = useState(false);

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success) {
                setUserId(response.data.content.userId);
                const roleName = response.data.content.roleId?.roleName;
                setIsAdmin(roleName === "Admin");
            } else {
                console.error("No user info found");
                setUserId(null);
                setIsAdmin(false);
            }
        } catch (error) {
            console.error("Error fetching user info:", error.response?.data || error.message);
            setUserId(null);
            setIsAdmin(false);
        }
    };

    const fetchMemberships = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/memberships?page=0&size=1000", {
                withCredentials: true,
            });
            const membershipData = response.data.content || [];
            setMemberships(membershipData);
        } catch (error) {
            console.error("Error fetching memberships:", error.response?.data || error.message);
            setMemberships([]);
        }
    };

    useEffect(() => {
        fetchUserInfo();
        fetchMemberships();
    }, []);

    const fetchPromotions = async (page, size, keyword = "", status = "all", date = "") => {
        try {
            setIsLoading(true);
            let url = "http://localhost:6789/api/v1/promotions";
            let response;

            if (keyword) {
                response = await axios.get(`${url}/search`, {
                    params: {
                        keyword: keyword,
                        page: page,
                        size: size,
                        sort: "promId,desc",
                    },
                    withCredentials: true,
                });
            } else if (status !== "all" || date) {
                response = await axios.get(`${url}/filter`, {
                    params: {
                        page: page,
                        size: size,
                        sort: "promId,desc",
                        ...(status !== "all" && { status }),
                        ...(date && { date }),
                    },
                    withCredentials: true,
                });
            } else {
                response = await axios.get(url, {
                    params: {
                        page: page,
                        size: size,
                        sort: "promId,desc",
                    },
                    withCredentials: true,
                });
            }

            const data = response.data;
            const promotionsData = data.content || [];
            const totalPages = data.pagination?.totalPages || (promotionsData.length > 0 ? Math.ceil(promotionsData.length / size) : 1);

            setPromotions(Array.isArray(promotionsData) ? promotionsData : []);
            setTotalPages(totalPages);
        } catch (error) {
            console.error("Error fetching promotions:", error.response?.data || error.message);
            setPromotions([]);
            setTotalPages(0);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchPromotions(currentPage, pageSize, searchQuery, filterStatus, filterDate);
    }, [currentPage, pageSize, searchQuery, filterStatus, filterDate]);

    const isMembershipPromotion = (promName) => {
        return memberships.some((membership) =>
            promName.toLowerCase() === `${membership.mbsName.toLowerCase()} promotion`
        );
    };

    const handlePageSizeChange = (event) => {
        const newSize = parseInt(event.target.value);
        setPageSize(newSize);
        setCurrentPage(0);
    };

    const handlePreviousPage = () => {
        if (currentPage > 0) {
            setCurrentPage(currentPage - 1);
        }
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) {
            setCurrentPage(currentPage + 1);
        }
    };



    const handleSearch = () => {
        setCurrentPage(0);
        setFilterStatus("all");
        setFilterDate("");
    };

    const handleKeyPress = (e) => {
        if (e.key === "Enter") {
            handleSearch();
        }
    };

    const handleCreateClick = () => {
        setIsAddModalOpen(true);
    };

    const handleCloseAddModal = () => {
        setIsAddModalOpen(false);
    };

    const handleEditClick = (promotion) => {
        setSelectedPromotion(promotion);
        setIsEditModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setSelectedPromotion(null);
    };

    const handleViewClick = (promotion) => {
        setSelectedPromotionForView(promotion);
        setIsViewModalOpen(true);
    };

    const handleCloseViewModal = () => {
        setIsViewModalOpen(false);
        setSelectedPromotionForView(null);
    };

    const handleEditSubmit = async (updatedData) => {
        try {
            const payload = {
                ...updatedData,
                approvedBy: isAdmin ? selectedPromotion.approvedBy?.userId?.userId || null : null,
                createdBy: selectedPromotion.createdBy?.userId?.userId || null,
            };
            await axios.put(
                `http://localhost:6789/api/v1/promotions/${selectedPromotion.promId}`,
                payload,
                { withCredentials: true }
            );
            onEdit({ ...selectedPromotion, ...updatedData });
            handleCloseEditModal();
            fetchPromotions(currentPage, pageSize, searchQuery, filterStatus, filterDate);
        } catch (error) {
            console.error("Error updating promotion:", error);
            showNotification("Failed to update promotion: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };

    const handleCreateSubmit = (newPromotion) => {
        onCreate(newPromotion);
        handleCloseAddModal();
        fetchPromotions(currentPage, pageSize, searchQuery, filterStatus, filterDate);
    };

    const handleDelete = async (promotion) => {
        if (!promotion || !promotion.promId) {
            console.error("Invalid promotion data for deletion");
            return;
        }

        const confirmed = await showConfirm(
            `Are you sure you want to delete promotion "${promotion.promName}"?`,
            'fail'
        );
        if (!confirmed) return;
        try {
            await axios.delete(`http://localhost:6789/api/v1/promotions/${promotion.promId}`, {
                withCredentials: true,
            });
            onDelete(promotion);
            if (promotions.length === 1 && currentPage > 0) {
                setCurrentPage(currentPage - 1);
            } else {
                fetchPromotions(currentPage, pageSize, searchQuery, filterStatus, filterDate);
            }
        } catch (error) {
            console.error("Error deleting promotion:", error);
            showNotification("Failed to delete promotion: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };

    const handleApprove = async (promotion) => {
        if (!userId) {

            showNotification("User information not available. Please log in again.", 3000, 'fail');
            return;
        }
        try {
            const payload = {
                promName: promotion.promName,
                promCode: promotion.promCode,
                discount: promotion.discount,
                quantity: promotion.quantity,
                startDate: promotion.startDate,
                endDate: promotion.endDate,
                promStatus: promotion.promStatus,
                approvedBy: userId,
                createdBy: promotion.createdBy?.userId?.userId || null,
            };
            await axios.put(
                `http://localhost:6789/api/v1/promotions/${promotion.promId}`,
                payload,
                { withCredentials: true }
            );
            fetchPromotions(currentPage, pageSize, searchQuery, filterStatus, filterDate);
        } catch (error) {
            console.error("Error approving promotion:", error);
            showNotification("Failed to approve promotion: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };

    const handleDecline = async (promotion) => {
        const confirmed = await showConfirm(
            `Are you sure you want to decline and delete promotion "${promotion.promName}"? This action cannot be undone.`,
            'fail'
        );
        if (!confirmed) return;
        try {
            await axios.delete(
                `http://localhost:6789/api/v1/promotions/${promotion.promId}`,
                { withCredentials: true }
            );
            onDelete(promotion);
            if (promotions.length === 1 && currentPage > 0) {
                setCurrentPage(currentPage - 1);
            } else {
                fetchPromotions(currentPage, pageSize, searchQuery, filterStatus, filterDate);
            }
        } catch (error) {
            console.error("Error declining and deleting promotion:", error);
            showNotification("Failed to decline and delete promotion: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };

    const handleFilterStatus = (value) => {
        setSearchQuery("");
        setFilterDate("");
        setFilterStatus(value);
        setCurrentPage(0);
    };

    const handleFilterDate = (e) => {
        const date = e.target.value;
        setSearchQuery("");
        setFilterStatus("all");
        setFilterDate(date);
        setCurrentPage(0);
    };

    const paddedPromotions = () => {
        const rows = [...promotions];
        while (rows.length < pageSize) {
            rows.push(null);
        }
        return rows;
    };

    const isPromotionActive = (startDate, endDate, promStatus, approvedBy, promName) => {
        // Nếu không phải là promotion của membership và đang Pending (approvedBy === null), trả về false (Inactive)
        if (!isMembershipPromotion(promName) && approvedBy === null) return false;

        // Nếu là promotion của membership hoặc đã được duyệt, kiểm tra ngày và trạng thái
        if (!startDate || !endDate || promStatus === undefined) return false;
        const today = new Date();
        const start = new Date(startDate);
        const end = new Date(endDate);
        const todayDate = new Date(today.setHours(0, 0, 0, 0));
        const startDateOnly = new Date(start.setHours(0, 0, 0, 0));
        const endDateOnly = new Date(end.setHours(0, 0, 0, 0));
        return todayDate >= startDateOnly && todayDate <= endDateOnly && promStatus === 1;
    };

    const isEditable = (startDate) => {
        if (!startDate) return true;
        const today = new Date();
        const start = new Date(startDate);
        const todayDate = new Date(today.setHours(0, 0, 0, 0));
        const startDateOnly = new Date(start.setHours(0, 0, 0, 0));
        return todayDate < startDateOnly;
    };

    const formatDate = (dateString) => {
        if (!dateString) return "";
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, "0");
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    };

    const renderPagination = () => {
        if (totalPages === 0) {
            return <p className="text-gray-500">No promotions available</p>;
        }
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
                className="text-sm px-3 py-1"
            >
                First
            </Button>
        );
        pageButtons.push(
            <Button
                key="prev"
                variant="outline"
                onClick={handlePreviousPage}
                disabled={currentPage === 0 || isLoading}
                className="text-sm px-3 py-1"
            >
                Previous
            </Button>
        );
        if (startPage > 0) {
            pageButtons.push(
                <Button key="start-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>
                    ...
                </Button>
            );
        }
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
        if (endPage < totalPages - 1) {
            pageButtons.push(
                <Button key="end-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>
                    ...
                </Button>
            );
        }
        pageButtons.push(
            <Button
                key="next"
                variant="outline"
                onClick={handleNextPage}
                disabled={currentPage >= totalPages - 1 || isLoading}
                className="text-sm px-3 py-1"
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
                className="text-sm px-3 py-1"
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
                    variant="default"
                    className="bg-blue-500 text-white hover:bg-blue-600"
                    onClick={handleCreateClick}
                >
                    Create <Plus className="h-4 w-4 ml-2" />
                </Button>
                <div className="flex items-center gap-2">
                    <Input
                        type="text"
                        placeholder="Search promotions..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        onKeyPress={handleKeyPress}
                        className="w-84"
                        disabled={filterStatus !== "all" || filterDate}
                    />
                    <Select value={filterStatus} onValueChange={handleFilterStatus} disabled={searchQuery || filterDate}>
                        <SelectTrigger className="w-[180px]">
                            <SelectValue placeholder="Filter by Status" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">All</SelectItem>
                            <SelectItem value="active">Active</SelectItem>
                            <SelectItem value="inactive">Inactive</SelectItem>
                        </SelectContent>
                    </Select>
                    <Input
                        type="date"
                        value={filterDate}
                        onChange={handleFilterDate}
                        className="w-[180px]"
                        disabled={searchQuery || filterStatus !== "all"}
                    />

                </div>
            </div>

            <AddPromotion isOpen={isAddModalOpen} onClose={handleCloseAddModal} onSubmit={handleCreateSubmit} />
            <EditPromotion
                isOpen={isEditModalOpen}
                onClose={handleCloseEditModal}
                onSubmit={handleEditSubmit}
                promotion={selectedPromotion}
            />
            <ViewPromotionDetail
                isOpen={isViewModalOpen}
                onClose={handleCloseViewModal}
                promotion={selectedPromotionForView}
            />

            <div className="overflow-y-auto min-h-[528px]">
                <table className="w-full min-w-full border-collapse border border-gray-300 table-fixed">
                    <thead>
                        <tr className="bg-blue-500">
                            <th className="border p-2 text-white font-bold w-[80px]">ID</th>
                            <th className="border p-2 text-white font-bold w-[200px]">Promotion Name</th>
                            <th className="border p-2 text-white font-bold w-[100px]">Discount</th>
                            <th className="border p-2 text-white font-bold w-[120px]">Start Date</th>
                            <th className="border p-2 text-white font-bold w-[120px]">End Date</th>
                            <th className="border p-2 text-white font-bold w-[100px]">Quantity</th>
                            <th className="border p-2 text-white font-bold w-[100px]">Status</th>
                            <th className="border p-2 text-white font-bold w-[150px]">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            Array(pageSize)
                                .fill(null)
                                .map((_, index) => (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2 w-[80px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[200px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[100px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[120px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[120px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[100px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[100px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[150px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                    </tr>
                                ))
                        ) : (
                            paddedPromotions().map((promotion, index) =>
                                promotion ? (
                                    <tr key={index} className="text-center" style={{ height: "53px" }}>
                                        <td className="border p-2 text-zinc-950">{promotion.promId}</td>
                                        <td className="border p-2 text-zinc-950">{promotion.promName}</td>
                                        <td className="border p-2 text-zinc-950">{promotion.discount}%</td>
                                        <td className="border p-2 text-zinc-950">{formatDate(promotion.startDate)}</td>
                                        <td className="border p-2 text-zinc-950">{formatDate(promotion.endDate)}</td>
                                        <td className="border p-2 text-zinc-950">{promotion.quantity}</td>
                                        <td
                                            className={`border p-2 ${isPromotionActive(promotion.startDate, promotion.endDate, promotion.promStatus, promotion.approvedBy, promotion.promName)
                                                ? "text-green-500"
                                                : "text-red-500"
                                                }`}
                                        >
                                            {isPromotionActive(promotion.startDate, promotion.endDate, promotion.promStatus, promotion.approvedBy, promotion.promName)
                                                ? "Active"
                                                : "Inactive"}
                                        </td>
                                        <td className="border p-2">
                                            <div className="flex justify-center gap-2">
                                                {isMembershipPromotion(promotion.promName) ? (
                                                    <Button
                                                        variant="outline"
                                                        size="icon"
                                                        onClick={() => handleViewClick(promotion)}
                                                        className="text-blue-600 hover:text-blue-800"
                                                    >
                                                        <Eye className="h-4 w-4" />
                                                    </Button>
                                                ) : promotion.approvedBy === null ? (
                                                    <>
                                                        <Button
                                                            variant="outline"
                                                            size="icon"
                                                            onClick={() => handleViewClick(promotion)}
                                                            className="text-blue-600 hover:text-blue-800"
                                                        >
                                                            <Eye className="h-4 w-4" />
                                                        </Button>
                                                        {isAdmin ? (
                                                            <>
                                                                <Button
                                                                    variant="outline"
                                                                    size="sm"
                                                                    onClick={() => handleApprove(promotion)}
                                                                    className="text-green-600 hover:text-green-800"
                                                                >
                                                                    Approve
                                                                </Button>
                                                                <Button
                                                                    variant="outline"
                                                                    size="sm"
                                                                    onClick={() => handleDecline(promotion)}
                                                                    className="text-red-600 hover:text-red-800"
                                                                >
                                                                    Decline
                                                                </Button>
                                                            </>
                                                        ) : (
                                                            <Button
                                                                variant="outline"
                                                                size="sm"
                                                                className="text-green-600 cursor-not-allowed"
                                                            >
                                                                Pending
                                                            </Button>
                                                        )}
                                                    </>
                                                ) : (
                                                    <>
                                                        <Button
                                                            variant="outline"
                                                            size="icon"
                                                            onClick={() => handleViewClick(promotion)}
                                                            className="text-blue-600 hover:text-blue-800"
                                                        >
                                                            <Eye className="h-4 w-4" />
                                                        </Button>
                                                        {isEditable(promotion.startDate) && (
                                                            <>
                                                                <Button
                                                                    variant="outline"
                                                                    size="icon"
                                                                    onClick={() => handleEditClick(promotion)}
                                                                    className="text-yellow-600 hover:text-yellow-800"
                                                                >
                                                                    <Edit className="h-4 w-4" />
                                                                </Button>
                                                                <Button
                                                                    variant="outline"
                                                                    size="icon"
                                                                    onClick={() => handleDelete(promotion)}
                                                                    className="text-red-600 hover:text-red-800"
                                                                >
                                                                    <Trash2 className="h-4 w-4" />
                                                                </Button>
                                                            </>
                                                        )}
                                                    </>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ) : (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2" colSpan="8"></td>
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

export default PromotionTable;