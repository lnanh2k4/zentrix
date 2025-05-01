import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import jsPDF from "jspdf";

const ViewWarrantyDetail = ({ isOpen, onClose, warranty }) => {
    if (!warranty) return null;

    const userInfo = warranty.userId || {};
    const createdByInfo = warranty.createdBy || {};

    const getStatusText = (status) => {
        return status === 1 ? "In Progress" : status === 2 ? "Has Done" : "Unknown";
    };

    const handleExportWarrantyPDF = (warranty) => {
        const doc = new jsPDF();
        doc.setFont("times", "normal");

        doc.setFontSize(18);
        doc.text("Warranty Receipt", doc.internal.pageSize.width / 2, 20, { align: "center" });

        doc.setFontSize(11);
        doc.text("ZENTRIX INFORMATION TECHNOLOGY SOLUTIONS TRADING SERVICES COMPANY LIMITED", 14, 30);
        doc.text("Address: 600, Nguyen Van Cu Street, An Binh Ward, Ninh Kieu District, Cantho City, Vietnam", 14, 35);
        doc.text("Hotline: 0393.510.720", 14, 40);
        doc.setLineWidth(0.5);
        doc.line(14, 45, 196, 45);

        doc.setFontSize(14);
        doc.text("REPAIR RECEIPT - WARRANTY", 14, 55);

        doc.setFontSize(11);
        const fullName = `${userInfo.firstName || ""} ${userInfo.lastName || ""}`.trim() || "N/A";
        doc.text(`Customer: ${fullName}`, 14, 65);
        doc.text(`Phone: ${userInfo.phone || "N/A"}`, 14, 70);
        doc.text(`Product: ${warranty.prodTypeId?.prodTypeName || "N/A"}`, 14, 75);
        doc.text(doc.splitTextToSize(`Description: ${warranty.description || "N/A"}`, 180), 14, 80);
        doc.text(doc.splitTextToSize(`Received Items: ${warranty.receive || "N/A"}`, 180), 14, 90);
        doc.text(`Start date: ${warranty.warnStartDate ? new Date(warranty.warnStartDate).toLocaleDateString("vi-VN") : "N/A"}`, 14, 100);
        doc.text(`End date: ${warranty.warnEndDate ? new Date(warranty.warnEndDate).toLocaleDateString("vi-VN") : "N/A"}`, 14, 105);
        const weeksDuration = warranty.warnEndDate && warranty.warnStartDate
            ? Math.ceil((new Date(warranty.warnEndDate) - new Date(warranty.warnStartDate)) / (1000 * 60 * 60 * 24 * 7))
            : "N/A";
        doc.text(`Warranty period: ${weeksDuration} weeks`, 14, 110);

        doc.setFontSize(10);
        doc.text("NOTE:", 14, 130);
        doc.text("NOT RECEIVED CUSTOMER INFORMATION", 14, 135);
        doc.text("Bring the repair receipt when picking up the device.", 14, 140);
        doc.text("Warranty period from 10-15 days excluding holidays", 14, 145);
        doc.setLineWidth(0.5);
        doc.line(14, 150, 196, 150);

        doc.setFontSize(10);
        doc.text("CUSTOMER SIGN FOR EQUIPMENT", 14, 165);
        doc.text("Technician", 150, 165);

        doc.setFontSize(9);
        doc.setTextColor(255, 0, 0);
        doc.text("ZENSTIX.STORE", 150, 175);
        const createdByFullName = createdByInfo.userId
            ? `${createdByInfo.userId.firstName || ""} ${createdByInfo.userId.lastName || ""}`.trim() || "N/A"
            : "N/A";
        doc.text(`Created by: ${createdByFullName}`, 130, 180);
        doc.text("RECEIVED", 150, 185);

        doc.save(`warranty_${userInfo.userId || "unknown"}_${warranty.prodTypeId?.prodTypeId || "unknown"}.pdf`);
    };

    return (
        <Dialog open={isOpen} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[550px]">
                <DialogHeader className="relative">
                    <DialogTitle>Warranty Detail</DialogTitle>
                    <Button
                        onClick={() => handleExportWarrantyPDF(warranty)} // Truyền warranty vào hàm
                        className="absolute top-0 right-0 bg-blue-500 text-white hover:bg-blue-600"
                    >
                        Print PDF
                    </Button>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="warnId" className="text-right">ID</Label>
                        <Input
                            id="warnId"
                            value={warranty.warnId || ""}
                            className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                        <Label htmlFor="status" className="text-right">Status</Label>
                        <Input
                            id="status"
                            value={getStatusText(warranty.status)}
                            className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="fullName" className="text-right">Full Name</Label>
                        <Input
                            id="fullName"
                            value={`${userInfo.firstName || ""} ${userInfo.lastName || ""}`.trim() || "N/A"}
                            className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="email" className="text-right">Email</Label>
                        <Input
                            id="email"
                            value={userInfo.email || "N/A"}
                            className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="phone" className="text-right">Phone</Label>
                        <Input
                            id="phone"
                            value={userInfo.phone || "N/A"}
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
                                createdByInfo.userId
                                    ? `${createdByInfo.userId.firstName || ""} ${createdByInfo.userId.lastName || ""}`.trim() || "N/A"
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
                            value={warranty.warnStartDate ? new Date(warranty.warnStartDate).toLocaleDateString() : "N/A"}
                            className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                        <Label htmlFor="warnEndDate" className="text-right">End Date</Label>
                        <Input
                            id="warnEndDate"
                            value={warranty.warnEndDate ? new Date(warranty.warnEndDate).toLocaleDateString() : "N/A"}
                            className="col-span-1 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            onFocus={(e) => e.target.blur()}
                        />
                    </div>
                    <div className="grid grid-cols-4 items-center gap-4">
                        <Label htmlFor="description" className="text-right">Description</Label>
                        <Textarea
                            id="description"
                            value={warranty.description || "N/A"}
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
                            value={warranty.receive || "N/A"}
                            className="col-span-3 border border-gray-300 rounded-md p-2 bg-gray-100 text-gray-500 select-none"
                            readOnly
                            rows={3}
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

export default ViewWarrantyDetail;