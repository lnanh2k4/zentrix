import { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Eye, Edit, Trash2, Plus } from "lucide-react";
import { Input } from "@/components/ui/input";
import AddWarranty from "./AddWarranty";
import EditWarranty from "./EditWarranty";
import ViewWarrantyDetail from "./ViewWarrantyDetail";

const WarrantyTable = ({ onView = () => { }, onEdit = () => { }, onDelete = () => { }, onCreate = () => { } }) => {
    const [warranties, setWarranties] = useState([]);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedWarranty, setSelectedWarranty] = useState(null);
    const [selectedWarrantyForView, setSelectedWarrantyForView] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchType, setSearchType] = useState("id");
    const [searchValue, setSearchValue] = useState("");

    const fetchWarranties = async (page, size, search = false) => {
        try {
            setIsLoading(true);
            let response;

            if (search && searchValue) {
                if (searchType === "id") {
                    response = await axios.get(`http://localhost:6789/api/v1/warranties/${searchValue}`, {
                        withCredentials: true,
                    });
                    const warranty = response.data.content;
                    setWarranties(warranty ? [warranty] : []);
                    setTotalPages(1);
                } else if (searchType === "phone") {
                    response = await axios.get("http://localhost:6789/api/v1/warranties/phone", {
                        params: { phone: searchValue, page: page, size: size, sortBy: "warnId", sortDir: "desc" },
                        withCredentials: true,
                    });
                    const warrantiesData = response.data.content?.content || [];
                    const paginationInfo = response.data.content;
                    setWarranties(warrantiesData);
                    setTotalPages(paginationInfo?.totalPages || 0);
                }
            } else {
                response = await axios.get("http://localhost:6789/api/v1/warranties", {
                    params: { page: page, size: size, sortBy: "warnId", sortDir: "desc" },
                    withCredentials: true,
                });
                const warrantiesData = response.data.content?.content || [];
                const paginationInfo = response.data.content;
                setWarranties(warrantiesData);
                setTotalPages(paginationInfo?.totalPages || 0);
            }
            console.log("API Response:", response.data);
        } catch (error) {
            console.error("Error fetching warranties:", error.response?.data || error.message);
            setWarranties([]);
            setTotalPages(0);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchWarranties(currentPage, pageSize, searchValue !== "");
    }, [currentPage, pageSize, searchType, searchValue]);

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

    const handleCreateClick = () => setIsAddModalOpen(true);
    const handleCloseAddModal = () => setIsAddModalOpen(false);
    const handleEditClick = (warranty) => {
        setSelectedWarranty(warranty);
        setIsEditModalOpen(true);
    };
    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setSelectedWarranty(null);
    };
    const handleViewClick = (warranty) => {
        setSelectedWarrantyForView(warranty);
        setIsViewModalOpen(true);
    };
    const handleCloseViewModal = () => {
        setIsViewModalOpen(false);
        setSelectedWarrantyForView(null);
    };

    const handleEditSubmit = (updatedData) => {
        // Cập nhật state warranties ngay lập tức với dữ liệu từ EditWarranty
        setWarranties((prev) =>
            prev.map((w) => (w.warnId === selectedWarranty.warnId ? { ...w, ...updatedData } : w))
        );
        onEdit({ ...selectedWarranty, ...updatedData }); // Gọi callback cha nếu cần
        handleCloseEditModal();
        // Gọi fetchWarranties để đồng bộ dữ liệu từ server (không đợi thông báo)
        fetchWarranties(currentPage, pageSize, searchValue !== "");
    };

    const handleCreateSubmit = (newWarranty) => {
        onCreate(newWarranty);
        handleCloseAddModal();
        fetchWarranties(currentPage, pageSize, searchValue !== "");
    };

    const handleDelete = async (warranty) => {
        if (!warranty || !warranty.warnId) return;
        if (warranty.status === 1) {

            showNotification("Cannot delete warranty that is currently 'In Progress'.", 3000, 'fail');
            return;
        }
        if (!window.confirm(`Are you sure you want to delete warranty with ID "${warranty.warnId}"?`)) return;
        try {
            await axios.delete(`http://localhost:6789/api/v1/warranties/${warranty.warnId}`, {
                withCredentials: true,
            });
            onDelete(warranty);
            if (warranties.length === 1 && currentPage > 0) setCurrentPage(currentPage - 1);
            fetchWarranties(currentPage, pageSize, searchValue !== "");
        } catch (error) {
            console.error("Error deleting warranty:", error);

            showNotification("Failed to delete warranty: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };

    const paddedWarranties = () => {
        const rows = [...warranties];
        while (rows.length < pageSize) rows.push(null);
        return rows;
    };

    const getUserFullName = (user) => {
        if (!user || typeof user === "number") return "N/A";
        return `${user.firstName || ""} ${user.lastName || ""}`.trim() || "N/A";
    };

    const getUserPhone = (user) => {
        if (!user || typeof user === "number") return "N/A";
        return user.phone || "N/A";
    };

    const getStatusText = (status) => {
        return status === 1 ? "In Progress" : status === 2 ? "Has Done" : "Unknown";
    };

    const renderPagination = () => {
        if (totalPages === 0) return <p className="text-gray-500">No warranties available</p>;

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
        if (startPage > 0)
            pageButtons.push(<Button key="start-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>...</Button>);
        for (let i = startPage; i <= endPage; i++) {
            pageButtons.push(
                <Button
                    key={i}
                    variant={currentPage === i ? "default" : "outline"}
                    className={currentPage === i ? "bg-blue-500 text-white" : ""}
                    onClick={() => setCurrentPage(i)}
                >
                    {i + 1}
                </Button>
            );
        }
        if (endPage < totalPages - 1)
            pageButtons.push(<Button key="end-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>...</Button>);
        pageButtons.push(
            <Button
                key="next"
                variant="outline"
                onClick={handleNextPage}
                disabled={currentPage >= totalPages - 1 || isLoading || (searchType === "id" && searchValue !== "")}
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
                disabled={currentPage >= totalPages - 1 || isLoading || (searchType === "id" && searchValue !== "")}
                className="text-sm px-3 py-1"
            >
                Last
            </Button>
        );

        return pageButtons;
    };

    return (
        <div className="bg-white w-full p-4 shadow-md rounded-lg overflow-x-auto animate-neonTable">
            <div className="flex justify-between mb-4 gap-4">
                <Button variant="default" className="bg-blue-500 text-white hover:bg-blue-600" onClick={handleCreateClick}>
                    Create <Plus className="h-4 w-4 ml-2" />
                </Button>
                <div className="flex items-center gap-2">
                    <Input
                        placeholder={`Search by ${searchType === "id" ? "ID" : "Phone"}`}
                        value={searchValue}
                        onChange={(e) => setSearchValue(e.target.value)}
                        className="w-[200px]"
                    />
                    <span>By</span>
                    <select value={searchType} onChange={(e) => setSearchType(e.target.value)} className="select-search-by border p-1 rounded">
                        <option value="id">ID</option>
                        <option value="phone">Phone Number</option>
                    </select>
                </div>
            </div>

            <AddWarranty isOpen={isAddModalOpen} onClose={handleCloseAddModal} onSubmit={handleCreateSubmit} />
            <EditWarranty isOpen={isEditModalOpen} onClose={handleCloseEditModal} onSubmit={handleEditSubmit} warranty={selectedWarranty} />
            <ViewWarrantyDetail isOpen={isViewModalOpen} onClose={handleCloseViewModal} warranty={selectedWarrantyForView} />

            <div className="overflow-y-auto min-h-[528px]">
                <table className="w-full min-w-full border-collapse border border-gray-300 table-fixed">
                    <thead>
                        <tr className="bg-blue-500">
                            <th className="border p-2 text-white font-bold w-[80px]">ID</th>
                            <th className="border p-2 text-white font-bold w-[150px]">Full Name</th>
                            <th className="border p-2 text-white font-bold w-[120px]">Phone Number</th>
                            <th className="border p-2 text-white font-bold w-[150px]">Product Name</th>
                            <th className="border p-2 text-white font-bold w-[120px]">End Date</th>
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
                                        <td className="border p-2 w-[150px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[120px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[150px]">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-[120px]">
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
                            paddedWarranties().map((warranty, index) =>
                                warranty ? (
                                    <tr key={index} className="text-center" style={{ height: "53px" }}>
                                        <td className="border p-2 text-zinc-950">{warranty.warnId}</td>
                                        <td className="border p-2 text-zinc-950">{getUserFullName(warranty.userId)}</td>
                                        <td className="border p-2 text-zinc-950">{getUserPhone(warranty.userId)}</td>
                                        <td className="border p-2 text-zinc-950">{warranty.prodTypeId?.prodTypeName || "N/A"}</td>
                                        <td className="border p-2 text-zinc-950">
                                            {new Date(warranty.warnEndDate).toLocaleDateString("en-GB", {
                                                day: "2-digit",
                                                month: "2-digit",
                                                year: "numeric",
                                            })}
                                        </td>
                                        <td className={`border p-2 ${warranty.status === 1 ? "text-blue-500" : "text-green-500"}`}>
                                            {getStatusText(warranty.status)}
                                        </td>
                                        <td className="border p-2">
                                            <div className="flex justify-center gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleViewClick(warranty)}
                                                    className="text-blue-600 hover:text-blue-800"
                                                >
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                                {warranty.status === 1 && (
                                                    <Button
                                                        variant="outline"
                                                        size="icon"
                                                        onClick={() => handleEditClick(warranty)}
                                                        className="text-yellow-600 hover:text-yellow-800"
                                                    >
                                                        <Edit className="h-4 w-4" />
                                                    </Button>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ) : (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2" colSpan="7"></td>
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
                    <select id="pageSize" value={pageSize} onChange={handlePageSizeChange} className="border p-1 rounded">
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

export default WarrantyTable;