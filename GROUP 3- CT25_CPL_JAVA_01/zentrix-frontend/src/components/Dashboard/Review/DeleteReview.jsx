import { X } from "lucide-react";

const DeleteReview = ({ review, onDelete, onCancel }) => {
    if (!review) return null;

    return (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg shadow-lg p-6 max-w-md w-full relative animate-fadeIn">
                <button onClick={onCancel} className="absolute top-2 right-2 text-gray-500 hover:text-gray-800">
                    <X />
                </button>
                <h2 className="text-xl font-semibold text-red-600 mb-4">Delete Review</h2>
                <p className="text-gray-700 mb-6">
                    Are you sure you want to delete the review: <br />
                    <span className="italic text-gray-900">"{review.comment}"</span>?
                </p>
                <div className="flex justify-end gap-3">
                    <button
                        onClick={onCancel}
                        className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
                    >
                        Cancel
                    </button>
                    <button
                        onClick={onDelete}
                        className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
};

export default DeleteReview;
