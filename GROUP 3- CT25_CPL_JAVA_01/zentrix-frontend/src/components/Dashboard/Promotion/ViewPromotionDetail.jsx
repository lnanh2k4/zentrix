import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";

const ViewPromotionDetail = ({ isOpen, onClose, promotion }) => {
    if (!promotion) return null;

    const createdByInfo = promotion.createdBy || {};
    const approvedByInfo = promotion.approvedBy || {};

    const getStatusText = (promStatus) => {
        return promStatus === 1 ? "Active" : promStatus === 0 ? "Inactive" : "Unknown";
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[550px]">
                <DialogHeader>
                    <DialogTitle>Promotion Detail</DialogTitle>
                </DialogHeader>
                <div className="space-y-2 py-4">
                    {/* ID */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="promId" className="block text-sm font-medium text-gray-700">ID</Label>
                        <Input
                            id="promId"
                            value={promotion.promId || ""}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                        <Label htmlFor="promStatus" className="block text-sm font-medium text-gray-700">Status</Label>
                        <Input
                            id="promStatus"
                            value={getStatusText(promotion.promStatus)}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>

                    {/* Promotion Name */}
                    <div>
                        <Label htmlFor="promName" className="block text-sm font-medium text-gray-700">Promotion Name</Label>
                        <Input
                            id="promName"
                            value={promotion.promName || "N/A"}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>

                    {/* Promotion Code */}
                    <div>
                        <Label htmlFor="promCode" className="block text-sm font-medium text-gray-700">Promotion Code</Label>
                        <Input
                            id="promCode"
                            value={promotion.promCode || "N/A"}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>

                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="discount" className="block text-sm font-medium text-gray-700">Discount (%)</Label>
                        <Input
                            id="discount"
                            value={promotion.discount ? `${(promotion.discount * 100).toFixed(2)}%` : "N/A"}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                        <Label htmlFor="quantity" className="block text-sm font-medium text-gray-700">Quantity</Label>
                        <Input
                            id="quantity"
                            value={promotion.quantity || "N/A"}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>

                    {/* Start Date */}
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="startDate" className="block text-sm font-medium text-gray-700">Start Date</Label>
                        <Input
                            id="startDate"
                            value={promotion.startDate ? new Date(promotion.startDate).toLocaleDateString() : "N/A"}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                        <Label htmlFor="endDate" className="block text-sm font-medium text-gray-700">End Date</Label>
                        <Input
                            id="endDate"
                            value={promotion.endDate ? new Date(promotion.endDate).toLocaleDateString() : "N/A"}
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>

                    <div>
                        <Label htmlFor="createdBy" className="block text-sm font-medium text-gray-700">Created By</Label>
                        <Input
                            id="createdBy"
                            value={
                                createdByInfo.userId
                                    ? `${createdByInfo.userId.firstName || ""} ${createdByInfo.userId.lastName || ""}`.trim() || "N/A"
                                    : "N/A"
                            }
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>

                    <div>
                        <Label htmlFor="approvedBy" className="block text-sm font-medium text-gray-700">Approved By</Label>
                        <Input
                            id="approvedBy"
                            value={
                                approvedByInfo.userId
                                    ? `${approvedByInfo.userId.firstName || ""} ${approvedByInfo.userId.lastName || ""}`.trim() || "N/A"
                                    : "N/A"
                            }
                            className="mt-1 block w-full border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={onClose}>Close</Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default ViewPromotionDetail;