import { useState, useEffect } from "react";
import axios from "axios";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { ChevronDown, ChevronUp, RefreshCw } from "lucide-react";

const ViewBranchDetail = ({ isOpen, onClose, branch }) => {
    const [productTypeBranches, setProductTypeBranches] = useState([]);
    const [staffs, setStaffs] = useState([]);
    const [filteredStaffs, setFilteredStaffs] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [isProductTypesExpanded, setIsProductTypesExpanded] = useState(false);
    const [isStaffsExpanded, setIsStaffsExpanded] = useState(false);
    const [selectedRole, setSelectedRole] = useState("");
    const [roles, setRoles] = useState([]);
    const [searchProductType, setSearchProductType] = useState("");
    const [filteredProductTypes, setFilteredProductTypes] = useState([]);
    const [searchStaff, setSearchStaff] = useState("");

    // Fetch related data (product types, staffs) for the branch
    const fetchRelatedData = async () => {
        if (!branch || !branch.brchId) return;

        setIsLoading(true);
        setError(null); // Reset error to null

        try {
            // Fetch product types
            try {
                const productTypeResponse = await axios.get(
                    `http://localhost:6789/api/v1/branches/${branch.brchId}/product-types`,
                    { withCredentials: true }
                );
                if (productTypeResponse.data.success) {
                    const productTypes = productTypeResponse.data.content || [];
                    setProductTypeBranches(productTypes);
                } else {
                    setProductTypeBranches([]); // No data, set empty array
                }
            } catch (err) {
                // Treat 404 or similar "not found" errors as "no data" rather than an error
                if (err.response?.status === 404) {
                    setProductTypeBranches([]);
                } else {
                    throw err; // Rethrow other errors to be caught by the outer catch
                }
            }

            // Fetch staffs
            try {
                const staffResponse = await axios.get(`http://localhost:6789/api/v1/staffs`, {
                    params: { page: 0, size: 1000 },
                    withCredentials: true,
                });
                if (staffResponse.data.success) {
                    const filteredStaffs = (staffResponse.data.content || []).filter(
                        (staff) => staff.brchId?.brchId === branch.brchId && staff.userId?.status === 1
                    );
                    setStaffs(filteredStaffs);
                    setFilteredStaffs(filteredStaffs);
                    const uniqueRoles = [...new Set(filteredStaffs.map((staff) => staff.userId?.roleId?.roleName).filter(Boolean))];
                    setRoles(uniqueRoles);
                } else {
                    setStaffs([]);
                    setFilteredStaffs([]);
                }
            } catch (err) {
                // Treat 404 or similar "not found" errors as "no data" rather than an error
                if (err.response?.status === 404) {
                    setStaffs([]);
                    setFilteredStaffs([]);
                } else {
                    throw err; // Rethrow other errors to be caught by the outer catch
                }
            }
        } catch (err) {
            console.error("Error fetching related data:", err);
            setError("An error occurred while fetching data: " + (err.response?.data?.message || err.message));
        } finally {
            setIsLoading(false);
        }
    };

    // Handle role filter for staffs
    const handleRoleFilter = (role) => {
        setSelectedRole(role);
        if (role === "") {
            setFilteredStaffs(staffs);
        } else {
            const filtered = staffs.filter((staff) => staff.userId?.roleId?.roleName === role);
            setFilteredStaffs(filtered);
        }
    };

    // Handle product type search by name or ID
    const handleProductTypeSearch = (searchTerm) => {
        setSearchProductType(searchTerm);
        if (searchTerm.trim() === "") {
            setFilteredProductTypes(productTypeBranches);
        } else {
            const filtered = productTypeBranches.filter((ptb) => {
                const nameMatch = ptb.prodTypeId?.prodTypeName?.toLowerCase().includes(searchTerm.toLowerCase());
                const idMatch = ptb.prodTypeBrchId.toString().includes(searchTerm);
                return nameMatch || idMatch;
            });
            setFilteredProductTypes(filtered);
        }
    };

    // Handle staff search by name or role name
    const handleStaffSearch = (searchTerm) => {
        setSearchStaff(searchTerm);
        if (searchTerm.trim() === "") {
            setFilteredStaffs(staffs);
        } else {
            const filtered = staffs.filter((staff) => {
                const fullName = staff.userId ? `${staff.userId.firstName} ${staff.userId.lastName}` : "";
                const nameMatch = fullName.toLowerCase().includes(searchTerm.toLowerCase());
                const roleMatch = staff.userId?.roleId?.roleName?.toLowerCase().includes(searchTerm.toLowerCase());
                return nameMatch || roleMatch;
            });
            setFilteredStaffs(filtered);
        }
    };

    // Fetch data when the dialog opens
    useEffect(() => {
        if (isOpen && branch) {
            fetchRelatedData();
        }
    }, [isOpen, branch]);

    // Update filtered product types when productTypeBranches changes
    useEffect(() => {
        setFilteredProductTypes(productTypeBranches);
    }, [productTypeBranches]);

    // Update filtered staffs when staffs changes
    useEffect(() => {
        setFilteredStaffs(staffs);
    }, [staffs]);

    if (!branch) return null;

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="w-[90vw] max-w-[90%] sm:max-w-[80%] lg:max-w-[70%] max-h-[80vh] overflow-y-auto p-4 sm:p-6 lg:p-8">
                <DialogHeader>
                    <DialogTitle className="text-lg sm:text-xl lg:text-2xl font-bold text-blue-600">
                        Branch Details - {branch.brchName}
                    </DialogTitle>
                </DialogHeader>
                <div className="space-y-6 sm:space-y-8">
                    {/* Branch Information */}
                    <div className="bg-gray-50 p-4 sm:p-6 rounded-lg shadow-sm">
                        <h3 className="text-base sm:text-lg lg:text-xl font-semibold text-gray-800 mb-3 sm:mb-4">
                            Branch Information
                        </h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
                            <div className="space-y-2 sm:space-y-3">
                                <p className="text-sm sm:text-base lg:text-lg">
                                    <strong className="text-gray-700">ID:</strong> {branch.brchId}
                                </p>
                                <p className="text-sm sm:text-base lg:text-lg">
                                    <strong className="text-gray-700">Name:</strong> {branch.brchName}
                                </p>
                                <p className="text-sm sm:text-base lg:text-lg">
                                    <strong className="text-gray-700">Address:</strong> {branch.address || "None"}
                                </p>
                            </div>
                            <div className="space-y-2 sm:space-y-3">
                                <p className="text-sm sm:text-base lg:text-lg">
                                    <strong className="text-gray-700">Phone:</strong> {branch.phone || "None"}
                                </p>
                                <p className="text-sm sm:text-base lg:text-lg">
                                    <strong className="text-gray-700">Status:</strong>{" "}
                                    <span
                                        className={
                                            branch.status === 1 ? "text-green-600" : "text-red-600"
                                        }
                                    >
                                        {branch.status === 1 ? "Active" : "Inactive"}
                                    </span>
                                </p>
                            </div>
                        </div>
                    </div>

                    {/* Product Types in Branch */}
                    <div className="bg-gray-50 p-4 sm:p-6 rounded-lg shadow-sm">
                        <div className="flex justify-between items-center mb-3 sm:mb-4">
                            <h3 className="text-base sm:text-lg lg:text-xl font-semibold text-gray-800">
                                Product Types in Branch
                            </h3>
                            <div className="flex gap-2">
                                <Button
                                    onClick={fetchRelatedData}
                                    className="bg-gray-500 hover:bg-gray-600 text-white text-sm px-3 py-1"
                                    disabled={isLoading}
                                >
                                    <RefreshCw className={`h-5 w-5 ${isLoading ? "animate-spin" : ""}`} />
                                </Button>
                                <Button
                                    onClick={() => setIsProductTypesExpanded(!isProductTypesExpanded)}
                                    className="bg-blue-500 hover:bg-blue-600 text-white text-sm px-3 py-1"
                                >
                                    {isProductTypesExpanded ? <ChevronUp className="h-5 w-5" /> : <ChevronDown className="h-5 w-5" />}
                                </Button>
                            </div>
                        </div>
                        {isProductTypesExpanded && (
                            <>
                                <div className="mb-4">
                                    <input
                                        type="text"
                                        placeholder="Search by product type name or ID..."
                                        value={searchProductType}
                                        onChange={(e) => handleProductTypeSearch(e.target.value)}
                                        className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                                    />
                                </div>

                                {isLoading ? (
                                    <p className="text-gray-600 text-sm sm:text-base">Loading product types...</p>
                                ) : error ? (
                                    <p className="text-red-500 text-sm sm:text-base">{error}</p>
                                ) : filteredProductTypes.length === 0 ? (
                                    <p className="text-gray-600 text-sm sm:text-base">
                                        {searchProductType ? "No matching product types found." : "No products found in branch"}
                                    </p>
                                ) : (
                                    <div className="overflow-x-auto">
                                        <table className="w-full border-collapse border border-gray-300">
                                            <thead>
                                                <tr className="bg-blue-500 text-white">
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">ID</th>
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Product Type</th>
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Quantity</th>
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Status</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {filteredProductTypes.map((ptb) => {
                                                    const quantity = ptb.quantity || 0;
                                                    let statusText, statusColor;

                                                    if (quantity === 0) {
                                                        statusText = "Out of Stock";
                                                        statusColor = "text-red-600";
                                                    } else if (quantity < 50) {
                                                        statusText = "Low Stock";
                                                        statusColor = "text-yellow-600";
                                                    } else {
                                                        statusText = "In Stock";
                                                        statusColor = "text-green-600";
                                                    }

                                                    return (
                                                        <tr key={ptb.prodTypeBrchId} className="hover:bg-gray-100">
                                                            <td className="border p-2 sm:p-3 text-sm sm:text-base">{ptb.prodTypeBrchId}</td>
                                                            <td className="border p-2 sm:p-3 text-sm sm:text-base">{ptb.prodTypeId?.prodTypeName || "N/A"}</td>
                                                            <td className="border p-2 sm:p-3 text-sm sm:text-base">{quantity}</td>
                                                            <td className={`border p-2 sm:p-3 text-sm sm:text-base ${statusColor}`}>
                                                                {statusText}
                                                            </td>
                                                        </tr>
                                                    );
                                                })}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </>
                        )}
                    </div>

                    {/* Staffs */}
                    <div className="bg-gray-50 p-4 sm:p-6 rounded-lg shadow-sm">
                        <div className="flex justify-between items-center mb-3 sm:mb-4">
                            <h3 className="text-base sm:text-lg lg:text-xl font-semibold text-gray-800">
                                Staffs
                            </h3>
                            <Button
                                onClick={() => setIsStaffsExpanded(!isStaffsExpanded)}
                                className="bg-blue-500 hover:bg-blue-600 text-white text-sm px-3 py-1"
                            >
                                {isStaffsExpanded ? <ChevronUp className="h-5 w-5" /> : <ChevronDown className="h-5 w-5" />}
                            </Button>
                        </div>
                        {isStaffsExpanded && (
                            <>
                                <div className="mb-4 flex flex-col sm:flex-row gap-4">
                                    <div>
                                        <input
                                            type="text"
                                            placeholder="Search by staff name or role..."
                                            value={searchStaff}
                                            onChange={(e) => handleStaffSearch(e.target.value)}
                                            className="w-full p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500"
                                        />
                                    </div>
                                    <div>
                                        <label htmlFor="roleFilter" className="text-sm sm:text-base font-medium text-gray-700 mr-2">
                                            Filter by Role:
                                        </label>
                                        <select
                                            id="roleFilter"
                                            value={selectedRole}
                                            onChange={(e) => handleRoleFilter(e.target.value)}
                                            className="border border-gray-300 rounded-md p-2 focus:ring-2 focus:ring-blue-500"
                                        >
                                            <option value="">All Roles</option>
                                            {roles.map((role) => (
                                                <option key={role} value={role}>
                                                    {role}
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                </div>

                                {isLoading ? (
                                    <p className="text-gray-600 text-sm sm:text-base">Loading staffs...</p>
                                ) : error ? (
                                    <p className="text-red-500 text-sm sm:text-base">{error}</p>
                                ) : filteredStaffs.length === 0 ? (
                                    <p className="text-gray-600 text-sm sm:text-base">
                                        {searchStaff ? "No matching staffs found." : "No staff found in branch"}
                                    </p>
                                ) : (
                                    <div className="overflow-x-auto">
                                        <table className="w-full border-collapse border border-gray-300">
                                            <thead>
                                                <tr className="bg-blue-500 text-white">
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Staff ID</th>
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Name</th>
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Email</th>
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Phone</th>
                                                    <th className="border p-2 sm:p-3 text-left text-sm sm:text-base">Role</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {filteredStaffs.map((staff) => (
                                                    <tr key={staff.staffId} className="hover:bg-gray-100">
                                                        <td className="border p-2 sm:p-3 text-sm sm:text-base">{staff.staffId}</td>
                                                        <td className="border p-2 sm:p-3 text-sm sm:text-base">
                                                            {staff.userId
                                                                ? `${staff.userId.firstName} ${staff.userId.lastName}`
                                                                : "N/A"}
                                                        </td>
                                                        <td className="border p-2 sm:p-3 text-sm sm:text-base">
                                                            {staff.userId?.email || "N/A"}
                                                        </td>
                                                        <td className="border p-2 sm:p-3 text-sm sm:text-base">
                                                            {staff.userId?.phone || "N/A"}
                                                        </td>
                                                        <td className="border p-2 sm:p-3 text-sm sm:text-base">
                                                            {staff.userId?.roleId?.roleName || "N/A"}
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                </div>
                <DialogFooter className="mt-6 sm:mt-8">
                    <Button onClick={onClose} className="bg-blue-500 hover:bg-blue-600 text-white">
                        Close
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default ViewBranchDetail;