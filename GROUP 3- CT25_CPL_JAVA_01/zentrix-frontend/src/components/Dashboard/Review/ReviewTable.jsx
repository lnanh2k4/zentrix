import axios from "axios";
import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Search, Frown, Trash2, Eye } from "lucide-react";
import DeleteReview from "./DeleteReview";
import ViewDetailReview from "./ViewReviewDetail";
import { FaStar } from "react-icons/fa";
import { showNotification } from '../NotificationPopup';
import { showConfirm } from "../ConfirmPopup";



const ReviewTable = () => {
    const [reviews, setReviews] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    const [selectedReviewToDelete, setSelectedReviewToDelete] = useState(null);
    const [selectedReviewToView, setSelectReviewToView] = useState(null);


    useEffect(()=> {
        setPage(0)
    },[pageSize])

    const fetchReviews = async () => {
        try {
            const response = await axios.get("http://localhost:6789/api/v1/dashboard/reviews", {
                params: {
                    page,
                    size: pageSize,
                },
                withCredentials: true,
            });

            if (response.data.success && response.data.content) {
                setReviews(response.data.content);
                setTotalPages(response.data.pagination.totalPages || 0);

            } else {
                console.error("API returned unsuccessful response:", response.data.message);
                showNotification("Failed to load reviews.", 3000, 'fail');

            }
        } catch (error) {
            console.error("Error fetching reviews:", error);
            showNotification("An error occurred while fetching reviews.", 3000, 'error');

        }
    };

    useEffect(() => {
        fetchReviews();
    }, [page, pageSize]);

    const filteredReviews = reviews.filter(
        (review) =>
            (review.comment || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
            (review.product?.prodTypeName || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
            (review.user?.username || "").toLowerCase().includes(searchTerm.toLowerCase())
    );

    const handleDeleteConfirm = async (reviewId) => {


        try {
            const response = await axios.delete(`http://localhost:6789/api/v1/reviews/remove/${reviewId}`, {
                withCredentials: true,
            });

            if (response.data.success) {
                showNotification("Review delete successfully", 3000, "complete");
                setSelectedReviewToDelete(null);
                fetchReviews();
            } else {
                showNotification("Failed to delete review.", 3000, 'fail');

            }
        } catch (error) {
            console.error("Delete error:", error);
            showNotification("Error deleting review.", 3000, 'error');

        }
    };

    return (
        <div className="bg-white p-8 shadow-md rounded-lg relative animate-neonTable ">
            <div className="flex justify-end mb-6">
                <div className="relative w-1/3">
                    <input
                        type="text"
                        placeholder="Search reviews..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full p-2 border rounded-md pr-10"
                    />
                    <Search className="absolute right-3 top-2 text-gray-500" />
                </div>
            </div>
            <div className="overflow-x-auto">
                <table className="w-full border border-gray-300 rounded-lg text-center">
                    <thead>
                        <tr className="bg-gradient-to-r from-blue-500 to-indigo-500 text-white">
                            <th className="border p-3 font-bold">ID</th>
                            <th className="border p-3 font-bold">productType Name</th>
                            <th className="border p-3 font-bold">User</th>
                            <th className="border p-3 font-bold">Comment</th>
                            <th className="border p-3 font-bold">Rating</th>
                            <th className="border p-3 font-bold">Image</th>
                            <th className="border p-3 font-bold">Created At</th>
                            <th className="border p-3 font-bold">Action</th>

                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">

                        {filteredReviews.length === 0 ? (
                            <tr>
                                <td colSpan="7" className="text-center py-4 text-gray-500">
                                    <div className="flex items-center justify-center gap-2 ">
                                        No results found
                                        <Frown size={25} className="text-gray-500 " />
                                    </div>
                                </td>
                            </tr>

                        ) : (
                            filteredReviews.map((review) => (
                                <tr key={review.reviewId} className="hover:bg-gray-100">
                                    <td className="border p-3 text-right">{review.reviewId}</td>
                                    <td className="border p-3 text-left">{review.product.prodTypeName}</td>
                                    <td className="border p-3 text-left">{review.user.username}</td>
                                    <td className="border p-3 truncate max-w-xs text-left" title={review.comment}>{review.comment}</td>
                                    <td className="border p-3">
                                        {[...Array(review.rating)].map((_, index) => (
                                            <FaStar key={index} className="inline h-5 w-5 text-yellow-500" />
                                        ))}
                                    </td>


                                    <td className="border p-3">
                                        {review.image ? (
                                            <img className="w-24 h-24 object-contain mx-auto" src={review.image.startsWith("http") ? review.image : `http://${review.image}`} alt="Review" />
                                        ) : (
                                            "No Image"
                                        )}
                                    </td>
                                    <td className="border p-3">
                                        {new Date(review.createdAt).toLocaleString('vi-VN', {
                                            day: '2-digit',
                                            month: '2-digit',
                                            year: 'numeric',
                                        })}
                                    </td>
                                    <td className="border p-3">
                                        <div className="flex justify-center gap-3">
                                            <Button
                                                variant="outline"
                                                size="icon"
                                                onClick={() => setSelectReviewToView(review)}
                                                className="text-blue-600 hover:text-blue-800">
                                                <Eye className="h-4 w-4" />
                                            </Button>
                                            {selectedReviewToView && (
                                                <ViewDetailReview
                                                    review={selectedReviewToView}
                                                    onClose={() => setSelectReviewToView(null)}
                                                />
                                            )}
                                            <Button
                                                variant="outline"
                                                onClick={async () => {
                                                    const shortComment = review.comment.length > 20 
                                                    ? review.comment.slice(0, 20) + "..." 
                                                    : review.comment;
                                                    const confirmed = await showConfirm(`Are you sure you want to REMOVE the review: "${shortComment}"?`, 'fail');
                                                    if (!confirmed) return;

                                                    handleDeleteConfirm(review.reviewId);
                                                }}
                                                className="text-red-600 hover:text-red-800"
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </Button>

                                        </div>
                                    </td>

                                </tr>
                            ))
                        )}
                    </tbody>

                </table>
            </div>
            <div className="flex gap-2 items-center justify-end pt-4">
                <span>Items per page:</span>
                <select
                    value={pageSize}
                    onChange={(e) => setPageSize(parseInt(e.target.value))}
                    className="border border-gray-300 rounded-md p-2"
                >
                    <option value={5}>5</option>
                    <option value={10}>10</option>
                    <option value={15}>15</option>
                    <option value={20}>20</option>
                </select>
            </div>
            <div className="flex justify-center mt-4 gap-2">
                <Button variant="outline" className="text-sm px-3 py-1"
                    onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                    disabled={page === 0}
                >
                    Previous
                </Button>

                {Array.from({ length: totalPages }, (_, index) => (
                    <Button
                        key={index}
                        onClick={() => setPage(index)}
                        className={`${index === page ? 'bg-blue-500 text-white' : 'bg-white text-black'
                            } border rounded px-3 py-1`}
                    >
                        {index + 1}
                    </Button>
                ))}

                <Button variant="outline" className="text-sm px-3 py-1"
                    onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))}
                    disabled={page >= totalPages - 1}
                >
                    Next
                </Button>
            </div>
            {selectedReviewToDelete && (
                <DeleteReview
                    review={selectedReviewToDelete}
                    onCancel={() => setSelectedReviewToDelete(null)}
                    onDelete={() => handleDeleteConfirm(selectedReviewToDelete.reviewId)}
                />
            )}

        </div>

    );
};

export default ReviewTable;
