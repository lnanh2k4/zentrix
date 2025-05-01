import { useState, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Eye, Edit, Trash2, Plus, Lock } from "lucide-react";
import { Input } from "@/components/ui/input";
import AddCustomer from "./AddCustomer";
import ViewCustomerDetail from "./ViewCustomerDetail";
import EditCustomer from "./EditCustomer";
import { useUserInfo, checkUserRole } from "@/services/InfoService"; // Import từ InfoService
import { deleteCustomer, getAllCustomer, lockCustomer } from "@/context/ApiContext"; // Dùng hàm từ ApiContext
import { showNotification } from "../NotificationPopup";
import { showConfirm } from "../ConfirmPopup";

const CustomerTable = ({
    onEdit = () => { },
    onDelete = () => { },
    onCreate = () => { },
    onLock = () => { },
}) => {
    const [customers, setCustomers] = useState([]);
    const [isAddCustomerModelOpen, setIsAddCustomerModalOpen] = useState(false);
    const [isEditCustomerModelOpen, setIsEditCustomerModalOpen] = useState(false);
    const [isViewDetailCustomerModelOpen, setIsViewDetailCustomerModalOpen] = useState(false);
    const [selectedCustomer, setSelectedCustomer] = useState(null); // Sửa từ mảng thành null
    const [isLoading, setIsLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [searchQuery, setSearchQuery] = useState("");
    const SEX_MALE = 1;
    const STATUS_ACTIVE = 1;
    const STATUS_VERIFYING = 2;

    // Kiểm tra quyền truy cập
    const { user, loading: userLoading, error: userError } = useUserInfo();
    const role = checkUserRole(user);

    // Fetch danh sách khách hàng
    const fetchCustomers = async (page, keyword = "") => {
        try {
            setIsLoading(true);
            const response = await getAllCustomer(page, pageSize, keyword);
            console.log("API Response:", response);
            if (response.success) {
                setCustomers(response.content || []);
                setTotalPages(response.pagination?.totalPages || 0);
            } else {
                console.error("API returned unsuccessful response:", response.message);
                setCustomers([]);
                setTotalPages(0);
            }
        } catch (error) {
            console.error("Error fetching customers:", error.response?.content || error.message);
            setCustomers([]);
            setTotalPages(0);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (!userLoading && user) {
            fetchCustomers(currentPage, searchQuery);
        }
    }, [currentPage, pageSize, searchQuery, isAddCustomerModelOpen, userLoading, user]);

    // Xử lý quyền truy cập
    if (userLoading) return <div>Loading user info...</div>;
    if (userError || !user || (!role.isAdmin() && !role.isSellerStaff() && !role.isWarehouseStaff() && !role.isShipperStaff())) {
        return <div>You do not have permission to view this page.</div>;
    }

    const handlePageSizeChange = (event) => {
        const newSize = parseInt(event.target.value);
        setPageSize(newSize);
        setCurrentPage(0);
        fetchProducts(0, searchTerm);
    };

    const handlePreviousPage = () => {
        if (currentPage > 0) setCurrentPage(currentPage - 1);
    };

    const handleNextPage = () => {
        if (currentPage < totalPages - 1) setCurrentPage(currentPage + 1);
    };

    const handleCreateClick = () => {
        setIsAddCustomerModalOpen(true);
    };

    const handleCloseAddModal = () => {
        setIsAddCustomerModalOpen(false);
    };

    const handleEditClick = (customer) => {
        setSelectedCustomer(customer);
        setIsEditCustomerModalOpen(true);
    };

    const handleCloseEditModal = () => {
        setIsEditCustomerModalOpen(false);
    };

    const handleViewDetailCustomerClick = (customer) => {
        setSelectedCustomer(customer);
        setIsViewDetailCustomerModalOpen(true);
    };

    const handleCloseViewDetailCustomerModal = () => {
        setIsViewDetailCustomerModalOpen(false);
        setSelectedCustomer(null);
    };

    const handleCreateSubmit = (newCustomer) => {
        onCreate(newCustomer);
        handleCloseAddModal();
        fetchCustomers(currentPage, searchQuery);
    };

    const handleEditSubmit = (updatedData) => {
        onEdit({ ...selectedCustomer, ...updatedData });
        handleCloseEditModal();
        fetchCustomers(currentPage, searchQuery);
    };

    const handleDelete = async (customer) => {
        if (!customer?.userId) {
            console.error("Invalid customer data for deletion!");
            return;
        }
        const confirm = await showConfirm(`Are you sure to delete ${customer.firstName} ${customer.lastName}?`, 'fail')
        if (confirm) {
            try {
                await deleteCustomer(customer.userId)
                onDelete(customer);
                showNotification("Delete customer successfully", 3000, 'complete')
                if (customers.length === 1 && currentPage > 0) setCurrentPage(currentPage - 1);
                fetchCustomers(currentPage, searchQuery);
            } catch (error) {
                console.error("Error deleting customer:", error.response?.data || error.message);
                showNotification(`Error deleting customer: ${error.response?.data || error.message}`, 3000, 'fail')
            }
        }

    };

    const handleLock = async (customer) => {
        if (!customer?.userId) {
            console.error("Invalid customer data for lock!");
            return;
        }

        const confirm = await showConfirm(`Are you sure to ${customer.status === STATUS_ACTIVE || customer.status === STATUS_VERIFYING ? "lock" : "unlock"} ${customer.firstName} ${customer.lastName}?`, 'fail')
        if (confirm) {
            try {
                await lockCustomer(customer.userId)
                onLock(customer);
                showNotification(`${customer.status === STATUS_ACTIVE || customer.status === STATUS_VERIFYING ? "Lock" : "Unlock"} ${customer.firstName} ${customer.lastName} customer successfully`, 3000, 'complete')
                fetchCustomers(currentPage, searchQuery);
            } catch (error) {
                console.error(`Error ${customer.status === STATUS_ACTIVE || customer.status === STATUS_VERIFYING ? "Lock" : "Unlock"} customer:`, error.response?.data || error.message);
                showNotification(`Error ${customer.status === STATUS_ACTIVE || customer.status === STATUS_VERIFYING ? "lock" : "unlock"} customer: ${error.response?.data || error.message}`, 3000, 'fail')
            }
        }

    };

    const handleKeyPress = (e) => {
        if (e.key === "Enter") fetchCustomers(currentPage, searchQuery);
    };

    const paddedCustomers = () => {
        const rows = [...customers];
        while (rows.length < pageSize) rows.push(null);
        return rows;
    };

    // Hàm render phân trang
    const renderPagination = () => {
        if (totalPages === 0) return <p className="text-gray-500">No customers available</p>;

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
                    placeholder="Search customers..."
                    value={searchQuery}
                    onKeyPress={handleKeyPress}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-84"
                />
            </div>
            {isAddCustomerModelOpen && (
                <AddCustomer
                    isOpen={isAddCustomerModelOpen}
                    onClose={handleCloseAddModal}
                    onSubmit={handleCreateSubmit}
                />
            )}
            {isEditCustomerModelOpen && (
                <EditCustomer
                    isOpen={isEditCustomerModelOpen}
                    onClose={handleCloseEditModal}
                    onSubmit={handleEditSubmit}
                    customer={selectedCustomer}
                />
            )}
            {isViewDetailCustomerModelOpen && (
                <ViewCustomerDetail
                    isOpen={isViewDetailCustomerModelOpen}
                    onClose={handleCloseViewDetailCustomerModal}
                    customer={selectedCustomer}
                />
            )}

            <div className="overflow-y-auto min-h-[528px]">
                <table className="w-full min-w-full border-collapse border border-gray-300">
                    <thead>
                        <tr className="bg-blue-500">
                            <th className="border p-2 text-white font-bold w-20">User Id</th>
                            <th className="border p-2 text-white font-bold w-50">Full Name</th>
                            <th className="border p-2 text-white font-bold w-20">Sex</th>
                            <th className="border p-2 text-white font-bold w-50">Email</th>
                            <th className="border p-2 text-white font-bold w-25">Status</th>
                            <th className="border p-2 text-white font-bold w-50">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        {isLoading ? (
                            Array(pageSize)
                                .fill(null)
                                .map((_, index) => (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2 w-20">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-50">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-20">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-30">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-25">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                        <td className="border p-2 w-50">
                                            <div className="h-4 bg-gray-200 rounded animate-pulse" />
                                        </td>
                                    </tr>
                                ))
                        ) : (
                            paddedCustomers().map((customer, index) =>
                                customer ? (
                                    <tr key={index} className="text-center" style={{ height: "53px" }}>
                                        <td className="border p-2 text-zinc-950">{customer.userId}</td>
                                        <td className="border p-2 text-zinc-950">
                                            {customer.firstName} {customer.lastName}
                                        </td>
                                        <td className="border p-2 text-zinc-950">
                                            {customer.sex === SEX_MALE ? "Male" : "Female"}
                                        </td>
                                        <td className="border p-2 text-zinc-950">{customer.email}</td>
                                        <td
                                            className={`font-bold border p-2 ${customer.status === STATUS_ACTIVE
                                                ? "text-green-500"
                                                : customer.status === STATUS_VERIFYING
                                                    ? "text-orange-400"
                                                    : "text-red-500"
                                                }`}
                                        >
                                            {customer.status === STATUS_ACTIVE
                                                ? "ACTIVE"
                                                : customer.status === STATUS_VERIFYING
                                                    ? "VERIFYING"
                                                    : "LOCKED"}
                                        </td>
                                        <td className="border p-2">
                                            <div className="flex justify-center gap-2">
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleViewDetailCustomerClick(customer)}
                                                    className="text-blue-600 hover:text-blue-800"
                                                >
                                                    <Eye className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleEditClick(customer)}
                                                    className="text-yellow-600 hover:text-yellow-800"
                                                >
                                                    <Edit className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleLock(customer)}
                                                    className="text-red-600 hover:text-red-800"
                                                >
                                                    <Lock className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="outline"
                                                    size="icon"
                                                    onClick={() => handleDelete(customer)}
                                                    className="text-red-600 hover:text-red-800"
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ) : (
                                    <tr key={index} style={{ height: "53px" }}>
                                        <td className="border p-2" colSpan="6"></td>
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

export default CustomerTable;