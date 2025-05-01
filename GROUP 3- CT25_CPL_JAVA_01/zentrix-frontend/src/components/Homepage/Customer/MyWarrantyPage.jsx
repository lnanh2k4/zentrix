import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "@/components/ui/Sidebar";
import Header from "@/components/ui/Header";
import Footer from "@/components/ui/Footer";
import { Shield } from "lucide-react";
import axios from "axios";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";

const MyWarrantyPage = () => {
    const [warranties, setWarranties] = useState([]);
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedWarranty, setSelectedWarranty] = useState(null);
    const navigate = useNavigate();

    const isExpired = (endDate) => {
        if (!endDate) return false;
        const today = new Date();
        const expDate = new Date(endDate);
        return expDate < today;
    };

    const fetchUserInfo = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
                withCredentials: true,
            });
            if (response.data.success) {
                setUser(response.data.content);
                return response.data.content.phone;
            }
            console.warn("User info fetched successfully but returned an unsuccessful response:", response.data);
            return null;
        } catch (error) {
            console.error("Error fetching user info:", {
                message: error.message,
                response: error.response?.data,
                status: error.response?.status,
                url: error.config?.url,
            });
            setUser(null);
            return null;
        }
    };

    const fetchWarrantiesByPhone = async (phone) => {
        if (!phone) return;

        try {
            const response = await axios.get("http://localhost:6789/api/v1/warranties/phone", {
                params: {
                    phone,
                    page: 0,
                    size: 1000,
                    sortBy: "warnId",
                    sortDir: "desc",
                },
                withCredentials: true,
            });
            if (response.data.success) {
                setWarranties(response.data.content.content);
            } else {
                setWarranties([]);
            }
        } catch (error) {
            console.error("Error fetching warranties:", error.response?.data || error.message);
            setWarranties([]);
        }
    };

    useEffect(() => {
        const loadWarranties = async () => {
            setLoading(true);
            const phone = await fetchUserInfo();

            if (phone) {
                await fetchWarrantiesByPhone(phone);
            }

            setLoading(false);
        };

        loadWarranties();
    }, []);

    const formatDate = (date) => {
        if (!date) return "N/A";
        const parsedDate = new Date(date);
        return isNaN(parsedDate.getTime()) ? "N/A" : parsedDate.toLocaleDateString();
    };

    const handleViewClick = (warranty) => {
        setSelectedWarranty(warranty);
        setIsViewModalOpen(true);
    };

    const handleCloseViewModal = () => {
        setIsViewModalOpen(false);
        setSelectedWarranty(null);
    };

    const getStatusText = (status) => {
        return status === 1 ? "In Progress" : status === 2 ? "Has Done" : "Unknown";
    };

    return (
        <div className="bg-[#f8f8fc] text-gray-900 min-h-screen flex flex-col" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
            <header className="h-20 bg-red-600 text-white flex items-center px-4 shadow-md w-full">
                <Header />
            </header>
            <div className="flex flex-1 container mx-auto p-6 space-x-6">
                <Sidebar />
                <main className="flex-1 flex flex-col space-y-8">
                    <div className="bg-white rounded-lg shadow-md p-6">
                        <div className="flex items-center space-x-3">
                            <Shield className="w-8 h-8 text-[#8c1d6b]" />
                            <h2 className="text-2xl font-semibold text-[#8c1d6b]">My Warranties</h2>
                        </div>
                    </div>
                    <div className="relative w-full max-w-[60rem] max-h-[90vh] bg-white rounded-lg shadow-[0_4px_20px_rgba(0,0,0,0.1)] overflow-y-auto p-6 mx-0">
                        <h3 className="text-lg font-semibold text-[#8c1d6b] mb-4">
                            Available Warranties ({warranties.length})
                        </h3>
                        {loading ? (
                            <p className="text-gray-500 text-center py-4">Loading...</p>
                        ) : warranties.length === 0 ? (
                            <p className="text-gray-500 text-center py-4">
                                No warranties available at the moment
                            </p>
                        ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                {warranties.map((warranty, index) => {
                                    const expired = isExpired(warranty.warnEndDate);
                                    return (
                                        <div
                                            key={index}
                                            className="border border-gray-200 rounded-lg p-4 hover:shadow-lg transition-shadow cursor-pointer"
                                            onClick={() => handleViewClick(warranty)}
                                        >
                                            <h4 className="text-md font-medium text-gray-800 mb-2">
                                                Warranty #{warranty.warnId}
                                            </h4>
                                            <div className="space-y-1">
                                                <p className="text-sm text-gray-600">
                                                    Start Date: {formatDate(warranty.warnStartDate)}
                                                </p>
                                                <p className="text-sm text-gray-600">
                                                    End Date: {formatDate(warranty.warnEndDate)}
                                                    {expired && (
                                                        <span className="ml-2 text-red-500 font-medium">
                                                            (Expired)
                                                        </span>
                                                    )}
                                                </p>
                                                <p className="text-sm text-gray-600">
                                                    Status: {warranty.status === 1 ? "In progress" : "Has done"}
                                                </p>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        )}
                    </div>
                </main>
            </div>

            {/* Modal hiển thị chi tiết warranty */}
            {selectedWarranty && (
                <Dialog open={isViewModalOpen} onOpenChange={handleCloseViewModal}>
                    <DialogContent className="sm:max-w-[550px]">
                        <DialogHeader>
                            <DialogTitle>Warranty Details</DialogTitle>
                        </DialogHeader>
                        <div className="grid gap-4 py-4">
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="warnId" className="text-right">ID</Label>
                                <Input
                                    id="warnId"
                                    value={selectedWarranty.warnId || ""}
                                    className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                                <Label htmlFor="status" className="text-right">Status</Label>
                                <Input
                                    id="status"
                                    value={getStatusText(selectedWarranty.status)}
                                    className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="fullName" className="text-right">Full Name</Label>
                                <Input
                                    id="fullName"
                                    value={`${selectedWarranty.userId?.firstName || ""} ${selectedWarranty.userId?.lastName || ""}`.trim() || "N/A"}
                                    className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="email" className="text-right">Email</Label>
                                <Input
                                    id="email"
                                    value={selectedWarranty.userId?.email || "N/A"}
                                    className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="phone" className="text-right">Phone</Label>
                                <Input
                                    id="phone"
                                    value={selectedWarranty.userId?.phone || "N/A"}
                                    className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="createdBy" className="text-right">Created By</Label>
                                <Input
                                    id="createdBy"
                                    value={
                                        selectedWarranty.createdBy?.userId
                                            ? `${selectedWarranty.createdBy.userId.firstName || ""} ${selectedWarranty.createdBy.userId.lastName || ""}`.trim() || "N/A"
                                            : "N/A"
                                    }
                                    className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="warnStartDate" className="text-right">Start Date</Label>
                                <Input
                                    id="warnStartDate"
                                    value={selectedWarranty.warnStartDate ? new Date(selectedWarranty.warnStartDate).toLocaleDateString() : "N/A"}
                                    className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                                <Label htmlFor="warnEndDate" className="text-right">End Date</Label>
                                <Input
                                    id="warnEndDate"
                                    value={selectedWarranty.warnEndDate ? new Date(selectedWarranty.warnEndDate).toLocaleDateString() : "N/A"}
                                    className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="description" className="text-right">Description</Label>
                                <Textarea
                                    id="description"
                                    value={selectedWarranty.description || "N/A"}
                                    className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    rows={3}
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                            <div className="grid grid-cols-4 items-center gap-4">
                                <Label htmlFor="receive" className="text-right">Received Items</Label>
                                <Textarea
                                    id="receive"
                                    value={selectedWarranty.receive || "N/A"}
                                    className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                                    readOnly
                                    rows={3}
                                    onFocus={(e) => e.target.blur()}
                                />
                            </div>
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={handleCloseViewModal}>Close</Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            )}

            <Footer />
        </div>
    );
};

export default MyWarrantyPage;