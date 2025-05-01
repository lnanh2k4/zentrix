import React from 'react';
import { Button } from "@/components/ui/button";
import { Download } from "lucide-react";

import jsPDF from 'jspdf';

import * as XLSX from 'xlsx';
const InvoiceModal = ({ isOpen, onClose, selectedOrder }) => {
    if (!isOpen || !selectedOrder) return null;

    const totalOrderPrice = selectedOrder.orderDetails
        ? selectedOrder.orderDetails.reduce(
            (sum, detail) => sum + (detail.unitPrice * detail.quantity || 0),
            0
        )
        : 0;

    const handleExportPDF = () => {
        const doc = new jsPDF();

        doc.setFontSize(18);
        doc.text("ZENTRIX INFORMATION TECHNOLOGY", 14, 20);
        doc.setFontSize(10);
        doc.text("Address: 600, Nguyen Van Cu Street, An Binh Ward, Ninh Kieu District, Cantho City, Vietnam", 14, 28);
        doc.text("Hotline: 0393.510.720", 14, 34);
        doc.setLineWidth(0.5);
        doc.line(14, 38, 196, 38);

        doc.setFontSize(16);
        doc.text(`Invoice - Order #${selectedOrder.orderId}`, 14, 48);

        doc.setFontSize(11);
        doc.text(`Branch: ${selectedOrder.brchId?.brchName || "N/A"}`, 14, 58);
        doc.text(`Customer: ${(selectedOrder.userId?.firstName || "") + " " + (selectedOrder.userId?.lastName || "") || "N/A"}`, 14, 66); // "Khách hàng" → "Customer"
        doc.text(`Date: ${selectedOrder.createdAt || "N/A"}`, 14, 74);
        doc.text(`Payment Method: ${selectedOrder.paymentMethod || "N/A"}`, 14, 82);

        const tableColumn = ["Product Type", "Quantity", "Unit Price", "Total"];
        const tableRows = [];

        selectedOrder.orderDetails.forEach((detail) => {
            const productName = detail.prodTypeBranchId?.prodTypeId?.prodId?.prodName || "N/A";
            const productTypeName = detail.prodTypeBranchId?.prodTypeId?.prodTypeName || "N/A";
            const detailData = [
                `${productName} - ${productTypeName}`,
                detail.quantity?.toString() || "N/A",
                `${detail.unitPrice?.toLocaleString("vi-VN") || "N/A"} VND`, // Định dạng kiểu VN
                `${(detail.unitPrice * detail.quantity)?.toLocaleString("vi-VN") || "N/A"} VND`,
            ];
            tableRows.push(detailData);
        });

        const totalOrderPrice = selectedOrder.orderDetails
            ? selectedOrder.orderDetails.reduce((sum, detail) => sum + (detail.unitPrice * detail.quantity || 0), 0)
            : 0;
        tableRows.push(["", "", "     TOTAL:", `${totalOrderPrice.toLocaleString("vi-VN")} VND`]);

        let startY = 92;
        const pageWidth = 182;
        const colWidths = [90, 20, 36, 36];
        const rowHeight = 8;

        doc.setFontSize(10);
        doc.setFillColor(22, 160, 133);
        doc.rect(14, startY, pageWidth, rowHeight, "F");
        doc.setTextColor(255, 255, 255);

        let currentX = 14;
        tableColumn.forEach((header, index) => {
            const textWidth = doc.getTextWidth(header);
            const offset = (colWidths[index] - textWidth) / 2;
            doc.text(header, currentX + offset, startY + 6);
            currentX += colWidths[index];
        });

        doc.setDrawColor(0);
        doc.rect(14, startY, pageWidth, rowHeight);

        doc.setTextColor(0, 0, 0);
        startY += rowHeight;

        tableRows.forEach((row, rowIndex) => {
            currentX = 14;

            if (rowIndex % 2 === 0) {
                doc.setFillColor(240, 240, 240);
                doc.rect(14, startY, pageWidth, rowHeight, "F");
            }

            row.forEach((cell, colIndex) => {
                if (rowIndex === tableRows.length - 1 && colIndex === 2) {
                    doc.text(cell, currentX + 2, startY + 6);
                } else if (rowIndex === tableRows.length - 1 && colIndex === 3) {
                    doc.text(cell, currentX + 2, startY + 6);
                } else if (colIndex === 1 || colIndex === 2 || colIndex === 3) {
                    const textWidth = doc.getTextWidth(cell);
                    const offset = (colWidths[colIndex] - textWidth) / 2;
                    doc.text(cell, currentX + offset, startY + 6);
                } else {
                    doc.text(cell, currentX + 2, startY + 6);
                }
                currentX += colWidths[colIndex];
            });
            doc.rect(14, startY, pageWidth, rowHeight);
            startY += rowHeight;
        });

        currentX = 14;
        for (let i = 0; i <= tableColumn.length; i++) {
            doc.line(currentX, 92, currentX, startY);
            if (i < tableColumn.length) currentX += colWidths[i];
        }

        doc.save(`invoice_${selectedOrder.orderId}.pdf`);
    };

    const handleExportExcel = () => {
        const sheetData = [];

        sheetData.push(["Invoice - Order #" + selectedOrder.orderId]);
        sheetData.push([]);
        sheetData.push(["Branch", selectedOrder.brchId?.brchName || "N/A"]);
        sheetData.push(["Customer", (selectedOrder.userId?.firstName || "") + " " + (selectedOrder.userId?.lastName || "") || "N/A"]);
        sheetData.push(["Date", selectedOrder.createdAt || "N/A"]);
        sheetData.push(["Payment Method", selectedOrder.paymentMethod || "N/A"]);
        sheetData.push([]);

        const tableHeader = ["Product Type", "Quantity", "Unit Price", "Total"];
        sheetData.push(tableHeader);

        selectedOrder.orderDetails.forEach((detail) => {
            const productName = detail.prodTypeBranchId?.prodTypeId?.prodId?.prodName || "N/A";
            const productTypeName = detail.prodTypeBranchId?.prodTypeId?.prodTypeName || "N/A";
            sheetData.push([
                `${productName} - ${productTypeName}`,
                detail.quantity || "N/A",
                `${detail.unitPrice?.toLocaleString("vi-VN") || "N/A"} VND`, // Định dạng kiểu VN
                `${(detail.unitPrice * detail.quantity)?.toLocaleString("vi-VN") || "N/A"} VND`,
            ]);
        });

        const totalOrderPrice = selectedOrder.orderDetails
            ? selectedOrder.orderDetails.reduce((sum, detail) => sum + (detail.unitPrice * detail.quantity || 0), 0)
            : 0;
        sheetData.push([]);
        sheetData.push(["", "", "TOTAL:", `${totalOrderPrice.toLocaleString("vi-VN")} VND`]);

        const workbook = XLSX.utils.book_new();
        const sheet = XLSX.utils.aoa_to_sheet(sheetData);

        sheet["!cols"] = [
            { wch: 40 },
            { wch: 15 },
            { wch: 20 },
            { wch: 20 },
        ];

        sheet["!rows"] = [];
        for (let i = 0; i < sheetData.length; i++) {
            sheet["!rows"][i] = { hpt: 20 };
        }

        const range = XLSX.utils.decode_range(sheet["!ref"]);
        for (let R = range.s.r; R <= range.e.r; R++) {
            for (let C = range.s.c; C <= range.e.c; C++) {
                const cellAddress = XLSX.utils.encode_cell({ r: R, c: C });
                if (!sheet[cellAddress]) continue;

                sheet[cellAddress].s = {
                    font: { name: "Arial", sz: 12 },
                    alignment: { vertical: "center", horizontal: C === 0 ? "left" : "center" }, // Căn trái cho cột đầu, căn giữa cho các cột khác
                    border: {
                        top: { style: "thin", color: { rgb: "000000" } },
                        bottom: { style: "thin", color: { rgb: "000000" } },
                        left: { style: "thin", color: { rgb: "000000" } },
                        right: { style: "thin", color: { rgb: "000000" } },
                    },
                };

                if (R === 0) {
                    sheet[cellAddress].s.font = { name: "Arial", sz: 16, bold: true };
                    sheet[cellAddress].s.fill = { fgColor: { rgb: "FFFF00" } }; // Màu vàng
                    sheet[cellAddress].s.alignment = { horizontal: "center" };
                }
                else if (R === 7) {
                    sheet[cellAddress].s.fill = { fgColor: { rgb: "16A085" } }; // Màu xanh ngọc
                    sheet[cellAddress].s.font = { name: "Arial", sz: 12, bold: true, color: { rgb: "FFFFFF" } }; // Chữ trắng
                }
                else if (R >= 8 && R < sheetData.length - 2) {
                    if ((R - 8) % 2 === 0) {
                        sheet[cellAddress].s.fill = { fgColor: { rgb: "F0F0F0" } }; // Màu xám nhạt cho hàng chẵn
                    }
                }
                else if (R === sheetData.length - 1) {
                    sheet[cellAddress].s.font = { name: "Arial", sz: 12, bold: true };
                    sheet[cellAddress].s.fill = { fgColor: { rgb: "E0E0E0" } };
                    if (C === 2) {
                        sheet[cellAddress].s.alignment = { horizontal: "right" };
                    } else if (C === 3) {
                        sheet[cellAddress].s.alignment = { horizontal: "center" };
                    }
                }
            }
        }

        // Gắn sheet vào workbook và xuất file
        XLSX.utils.book_append_sheet(workbook, sheet, "Invoice");
        XLSX.writeFile(workbook, `invoice_${selectedOrder.orderId}.xlsx`);
    };

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <div className="absolute inset-0 bg-black opacity-50" onClick={onClose}></div>
            <div className="fixed bg-white p-6 rounded-lg shadow-lg w-full">
                <h2 className="text-2xl font-bold mb-4">Invoice - Order #{selectedOrder.orderId}</h2>

                {/* Thông tin order */}
                <div className="mb-6">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <p><strong>Branch:</strong> {selectedOrder.brchId?.brchName || "N/A"}</p>
                            <p><strong>Created By:</strong> {(selectedOrder.userId?.firstName || "") + " " + (selectedOrder.userId?.lastName || "") || "N/A"}</p>
                        </div>
                        <div>
                            <p><strong>Date:</strong> {selectedOrder.createdAt || "N/A"}</p>
                            <p><strong>Payment Method:</strong> {selectedOrder.paymentMethod || "N/A"}</p>
                        </div>
                    </div>
                </div>

                {/* Chi tiết order */}
                <table className="w-full border-collapse border border-gray-300 mb-6">
                    <thead>
                        <tr className="bg-gray-200">
                            <th className="border p-2">Product</th>
                            <th className="border p-2">Product Type</th>
                            <th className="border p-2">Quantity</th>
                            <th className="border p-2">Unit Price</th>
                            <th className="border p-2">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        {selectedOrder.orderDetails?.map((detail) => (
                            <tr key={detail.orderDetailId} className="text-center">
                                <td className="border p-2">{detail.prodTypeBranchId?.prodTypeId?.prodId?.prodName || "N/A"}</td>
                                <td className="border p-2">{detail.prodTypeBranchId?.prodTypeId?.prodTypeName || "N/A"}</td>
                                <td className="border p-2">{detail.quantity || "N/A"}</td>
                                <td className="border p-2">{detail.unitPrice?.toLocaleString() || "N/A"} VND</td>
                                <td className="border p-2">{(detail.unitPrice * detail.quantity)?.toLocaleString() || "N/A"} VND</td>
                            </tr>
                        ))}
                        <tr className="font-bold">
                            <td colSpan="4" className="border p-2 text-right">Total:</td>
                            <td className="border p-2">{totalOrderPrice.toLocaleString()} VND</td>
                        </tr>
                    </tbody>
                </table>

                {/* Nút hành động */}
                <div className="flex justify-end gap-4">
                    <Button
                        variant="outline"
                        onClick={onClose}
                        className="text-gray-600 hover:text-gray-800"
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={handleExportPDF}
                        className="bg-red-500 hover:bg-red-600 text-white"
                    >
                        <Download className="h-4 w-4 mr-2" />
                        Export to PDF
                    </Button>
                    <Button
                        onClick={handleExportExcel}
                        className="bg-green-500 hover:bg-green-600 text-white"
                    >
                        <Download className="h-4 w-4 mr-2" />
                        Export to Excel
                    </Button>
                </div>


            </div>
        </div>
    );
};

export default InvoiceModal;