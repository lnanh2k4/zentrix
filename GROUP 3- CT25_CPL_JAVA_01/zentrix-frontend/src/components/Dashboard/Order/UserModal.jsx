import { Button } from "@/components/ui/button";

const UserModal = ({ isOpen, onClose, selectedUser }) => {
    if (!isOpen || !selectedUser) return null;

    return (
        <div className="fixed inset-0 flex items-center justify-center z-50">
            <div className="absolute inset-0 bg-black opacity-50" onClick={onClose}></div>

            <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-md z-10 animate-neonTable">         <div className="bg-white p-6 rounded-lg shadow-lg max-w-md w-full">
                <h2 className="text-xl font-bold mb-4">User Details</h2>
                <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <p><strong>User ID:</strong> {selectedUser.userId || "N/A"}</p>
                        <p><strong>Full Name:</strong> {(selectedUser.firstName || "") + " " + (selectedUser.lastName || "") || "N/A"}</p>
                        <p><strong>Email:</strong> {selectedUser.email || "N/A"}</p>
                        <p><strong>Phone:</strong> {selectedUser.phone || "N/A"}</p>
                        <p><strong>Address:</strong> {selectedUser.address || "N/A"}</p>
                    </div>
                    <div className="space-y-2">
                        <p><strong>Company Name:</strong> {selectedUser.companyName || "N/A"}</p>
                        <p><strong>Tax Code:</strong> {selectedUser.taxCode || "N/A"}</p>
                        <p><strong>Role:</strong> {selectedUser.roleId?.roleName || "N/A"}</p>
                        <p><strong>Sex:</strong> {selectedUser.sex === 1 ? "Male" : selectedUser.sex === 0 ? "Female" : "N/A"}</p>
                        <p>
                            <strong>Status:</strong>{" "}
                            <span
                                className={
                                    selectedUser.status === 1
                                        ? "text-green-600 bg-green-100 px-2 py-1 rounded"
                                        : selectedUser.status === 0
                                            ? "text-red-600 bg-red-100 px-2 py-1 rounded"
                                            : "text-gray-600"
                                }
                            >
                                {selectedUser.status === 1 ? "Active" : selectedUser.status === 0 ? "Inactive" : "N/A"}
                            </span>
                        </p>
                    </div>
                </div>
                <div className="mt-4 flex justify-end">
                    <Button
                        onClick={onClose}
                        className="bg-gray-500 hover:bg-gray-700 text-white"
                    >
                        Close
                    </Button>
                </div>
            </div>
            </div>
        </div>
    );
};

export default UserModal;