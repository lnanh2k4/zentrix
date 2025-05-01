import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Eye, Edit, Trash2, Plus, Lock } from "lucide-react";
import { Input } from "@/components/ui/input";
import AddStaff from "./AddStaff";
import ViewStaffDetail from "./ViewStaffDetail";
import EditStaff from "./EditStaff";
import { deleteStaff, getAllStaffs, lockStaff } from "@/context/ApiContext";
import { showNotification } from "../NotificationPopup";
import { useUserInfo } from "@/services/InfoService";
import { showConfirm } from "../ConfirmPopup";

const StaffTable = ({
    onEdit = () => { },
    onDelete = () => { },
    onCreate = () => { },
    onLock = () => { }
}) => {
    const [staffs, setStaffs] = useState([]);
    const [isAddStaffModelOpen, setIsAddStaffModalOpen] = useState(false);
    const [isEditStaffModelOpen, setIsEditStaffModalOpen] = useState(false);
    const [isViewDetailStaffModelOpen, setIsViewDetailStaffModalOpen] = useState(false);
    const [selectedStaff, setSelectedStaff] = useState(null); // Sửa từ mảng thành null
    const [isLoading, setIsLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchQuery, setSearchQuery] = useState("");
    const { user, loading } = useUserInfo()
    const SEX_MALE = 1;
    const STATUS_ACTIVE = 1;
    const STATUS_VERIFYING = 2;

    // Fetch danh sách nhân viên
    const fetchStaffs = async (page, keyword = "") => {
        try {
            setIsLoading(true);
            const response = await getAllStaffs(page, pageSize, keyword);

            if (response.success) {
                setStaffs(response.content || []);
                setTotalPages(response.pagination?.totalPages || 0);
            } else {
                setStaffs([]);
                setTotalPages(0);
            }
        } catch (error) {
            console.error("Error fetching staffs:", error.response?.content || error.message);
            setStaffs([]);
            setTotalPages(0);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchStaffs(currentPage, searchQuery);
    }, [currentPage, pageSize, searchQuery, isAddStaffModelOpen, user, loading]);

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

    const handleCreateClick = () => {
        setIsAddStaffModalOpen(true);
    };

    const handleCloseAddModal = () => {
        setIsAddStaffModalOpen(false);
    };

    const handleEditClick = (staff) => {
        setSelectedStaff(staff);
        setIsEditStaffModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditStaffModalOpen(false);
    };

    const handleViewDetailStaffClick = (staff) => {
        setSelectedStaff(staff);
        setIsViewDetailStaffModalOpen(true);
    };

    const handleCloseViewDetailStaffModal = () => {
        setIsViewDetailStaffModalOpen(false);
        setSelectedStaff(null);
    };

    const handleCreateSubmit = (newStaff) => {
        onCreate(newStaff);
        handleCloseAddModal();
        fetchStaffs(currentPage, searchQuery);
    };

    const handleEditSubmit = (updatedData) => {
        onEdit({ ...selectedStaff, ...updatedData });
        handleCloseEditModal();
        fetchStaffs(currentPage, searchQuery);
    };

    const handleDelete = async (staff) => {
        if (!staff?.staffId) {
            console.error("Invalid staff data for deletion!");
            return;
        }
        const confirm = await showConfirm(`Are you sure to delete ${staff.userId.firstName} ${staff.userId.lastName}?`, 'fail')
        if (!confirm) return
        try {
            await deleteStaff(staff.staffId);
            onDelete(staff);
            showNotification("Delete staff successfully", 3000, 'complete')
            if (staffs.length === 1 && currentPage > 0) setCurrentPage(currentPage - 1);
            fetchStaffs(currentPage, searchQuery);
        } catch (error) {
            console.error("Error deleting staff:", error.response?.data || error.message);
            showNotification(`Error deleting staff: ${error.response?.data || error.message}`, 3000, 'fail')
        }
    };

    const handleLock = async (staff) => {
        if (!staff?.staffId) {
            console.error("Invalid staff data for lock!");
            return;
        }
        const confirm = await showConfirm(`Are you sure to ${staff.userId.status === STATUS_ACTIVE || staff.userId.status === STATUS_VERIFYING ? "lock" : "unlock"} ${staff.userId.firstName} ${staff.userId.lastName}?`, 'fail')
        if (!confirm) return
        try {
            await lockStaff(staff.staffId);
            onLock(staff);
            showNotification(`${staff.userId.status === STATUS_ACTIVE || staff.userId.status === STATUS_VERIFYING ? "Lock" : "Unlock"} ${staff.userId.firstName} ${staff.userId.lastName} staff successfully`, 3000, 'complete')
            fetchStaffs(currentPage, searchQuery);
        } catch (error) {
            console.error(`Error ${staff.userId.status === STATUS_ACTIVE || staff.userId.status === STATUS_VERIFYING ? "lock" : "unlock"} staff:`, error.response?.data || error.message);
            showNotification(`Error ${staff.userId.status === STATUS_ACTIVE || staff.userId.status === STATUS_VERIFYING ? "lock" : "unlock"} staff: ${error.response?.data || error.message}`, 3000, 'fail')
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === "Enter") fetchStaffs(currentPage, searchQuery);
    };

    const paddedStaffs = () => {
        const rows = [...staffs];
        while (rows.length < pageSize) rows.push(null);
        return rows;
    };

    // Hàm render phân trang
    const renderPagination = () => {
        if (totalPages === 0) return <p className="text-gray-500">No staff available</p>;

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
                <Input
                    type="text"
                    placeholder="Search staffs..."
                    value={searchQuery}
                    onKeyPress={handleKeyPress}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-84"
                />
            </div>

            {isAddStaffModelOpen && (
                <AddStaff
                    isOpen={isAddStaffModelOpen}
                    onClose={handleCloseAddModal}
                    onSubmit={handleCreateSubmit}
                />
            )}
            {isEditStaffModelOpen && (
                <EditStaff
                    isOpen={isEditStaffModelOpen}
                    onClose={handleCloseEditModal}
                    onSubmit={handleEditSubmit}
                    staff={selectedStaff}
                />
            )}
            {isViewDetailStaffModelOpen && (
                <ViewStaffDetail
                    isOpen={isViewDetailStaffModelOpen}
                    onClose={handleCloseViewDetailStaffModal}
                    staff={selectedStaff}
                />
            )}

            <div className="overflow-y-auto min-h-[528px]">
                <table className="w-full min-w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-blue-500">
                            <th className="border p-2 text-white font-bold w-15">Staff Id</th>
                            <th className="border p-2 text-white font-bold w-50">Full Name</th>
                            <th className="border p-2 text-white font-bold w-20">Sex</th>
                            <th className="border p-2 text-white font-bold w-40">Email</th>
                            <th className="border p-2 text-white font-bold w-30">Phone</th>
                            <th className="border p-2 text-white font-bold w-30">Branch</th>
                            <th className="border p-2 text-white font-bold w-30">Role</th>
                            <th className="border p-2 text-white font-bold w-20">Status</th>
                            <th className="border p-2 text-white font-bold w-30">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            Array(pageSize)
                                .fill(null)
                                .map((_, index) => (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2 w-15">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-50">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-20">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-40">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-30">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-30">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-30">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-20">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-30">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                    </tr>
                                ))
                        ) : (
                            paddedStaffs().map((staff, index) =>
                                staff ? (
                                    <tr key={index} className="text-center" style={{ height: "53px" }}>
                                        <td className="border p-2 text-zinc-950">{staff.staffId}</td>
                                        <td className="border p-2 text-zinc-950">
                                            {staff.userId.firstName} {staff.userId.lastName}
                                        </td>
                                        <td className="border p-2 text-zinc-950">
                                            {staff.userId.sex === SEX_MALE ? "Male" : "Female"}
                                        </td>
                                        <td className="border p-2 text-zinc-950">{staff.userId.email}</td>
                                        <td className="border p-2 text-zinc-950">{staff.userId.phone}</td>
                                        <td className="border p-2 text-zinc-950">{staff.brchId.brchName}</td>
                                        <td className="border p-2 text-zinc-950">{staff.userId.roleId.roleName}</td>
                                        <td
                                            className={`font-bold border p-2 ${staff.userId.status === STATUS_ACTIVE
                                                ? "text-green-500"
                                                : staff.userId.status === STATUS_VERIFYING
                                                    ? "text-orange-400"
                                                    : "text-red-500"
                                                }`}
                                        >
                                            {staff.userId.status === STATUS_ACTIVE
                                                ? "ACTIVE"
                                                : staff.userId.status === STATUS_VERIFYING
                                                    ? "VERIFYING"
                                                    : "LOCKED"}
                                        </td>
                                        <td className="border p-2">
                                            <div className="flex justify-center gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleViewDetailStaffClick(staff)}
                                                    className="text-blue-600 hover:text-blue-800"
                                                >
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleEditClick(staff)}
                                                    className="text-yellow-600 hover:text-yellow-800"
                                                >
                                                    <Edit className="h-4 w-4" />
                                                </Button>
                                                {(user?.username != staff.userId.username) && (
                                                    <>
                                                        <Button
                                                            variant="outline"
                                                            size="icon"
                                                            onClick={() => handleLock(staff)}
                                                            className="text-red-600 hover:text-red-800"
                                                        >
                                                            <Lock className="h-4 w-4" />
                                                        </Button>
                                                        <Button
                                                            variant="outline"
                                                            size="icon"
                                                            onClick={() => handleDelete(staff)}
                                                            className="text-red-600 hover:text-red-800"
                                                        >
                                                            <Trash2 className="h-4 w-4" />
                                                        </Button>
                                                    </>
                                                )}

                                            </div>
                                        </td>
                                    </tr>
                                ) : (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2" colSpan="9"></td>
                                    </tr>
                                )
                            )
                        )}
                    </tbody>
                </table>
            </div>
            <div className="flex justify-end items-center mt-4">
                <div className="flex items-center gap-2 whitespace-nowrap">
                    <label htmlFor="pageSize" className="text-sm">Items per page:</label>
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

export default StaffTable;