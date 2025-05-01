import { useState, useEffect } from "react";
import { MessageCircle, Star, StarOff } from "lucide-react";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import AddReview from "@/components/Dashboard/Review/AddReview";
import EditReview from "@/components/Dashboard/Review/EditReview";
import { getInfo } from "@/context/ApiContext";
import { FaStar } from "react-icons/fa";
import { showNotification } from "@/components/Dashboard/NotificationPopup";
import { showConfirm } from "@/components/Dashboard/ConfirmPopup";

const ReviewProduct = ({ averageRating, productId, userId, prodTypeId, isReviewNow }) => {

  const [isAddingReview, setIsAddingReview] = useState(false);
  const [reviews, setReviews] = useState([]);
  const [filteredReviews, setFilteredReviews] = useState([]);
  const [filterRating, setFilterRating] = useState("All");
  const [visibleReviews, setVisibleReviews] = useState(5);
  const [isExpanded, setIsExpanded] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [editingReviewId, setEditingReviewId] = useState(null);
  const [previewImage, setPreviewImage] = useState(null);



  const fetchReviews = async () => {
    try {
      const response = await fetch(`http://localhost:6789/api/v1/reviews/public/${productId}`, {
        method: "GET",
        credentials: "include",
      });
      const data = await response.json();

      if (data.success && data.content) {

        let allReviews = data.content;

        setReviews(allReviews);
      } else {
        showNotification("Failed to load reviews", 3000, "error");
      }
    } catch (error) {
      console.error("Error fetching reviews:", error);
      showNotification("An error occurred while fetching reviews.", 3000, "error");

    }
  };


  useEffect(() => {
    const fetchUser = async () => {
      const info = await getInfo();
      // console.log("User info:", info);
      if (info?.success) {
        setUserInfo(info);
      }
    };
    fetchUser();
  }, []);

  useEffect(() => {
    fetchReviews();
  }, []);

  useEffect(() => {
    if (filterRating === "All") {
      setFilteredReviews([...reviews].reverse());
    } else {
      setFilteredReviews(reviews.filter((review) => review.rating === parseInt(filterRating)));
    }
  }, [filterRating, reviews]);

  useEffect(() => {
    // console.log("Product ID in ReviewProduct:", productId);
  }, [productId]);



  const handleOpenReviewForm = () => setIsAddingReview(true);
  const handleCancelReview = () => setIsAddingReview(false);

  const handleCreateReview = async () => {
    showNotification("Created review successfully", 3000, "complete");

    setIsAddingReview(false);
    await fetchReviews();
  };



  const handleFilterRating = (rating) => setFilterRating(rating);

  const handleToggleReviews = () => {
    setVisibleReviews(isExpanded ? 5 : filteredReviews.length);
    setIsExpanded(!isExpanded);
  };

  const handleDeleteReview = async (reviewId) => {
    try {
      const response = await fetch(`http://localhost:6789/api/v1/reviews/remove/${reviewId}`, {
        method: "DELETE",
        credentials: "include",
      });
      const data = await response.json();
      if (data.success) {
        setReviews(reviews.filter((review) => review.reviewId !== reviewId));
        showNotification("Remove review is successfully", 3000, "complete");
      } else {
        showNotification("Failed to delete review!", 3000, "error");

      }
    } catch (error) {
      showNotification("Error deleting review.", 3000, "error");
    }
  };

  const handleEditReview = (reviewId) => {
    setEditingReviewId(reviewId);
  };
  const handleEditSuccess = async () => {
    showNotification("Edited review successfully", 3000, "complete");
    setEditingReviewId(null);
    await fetchReviews();
  };
  const formatAverageRating = (rating) => {
    if (!rating || isNaN(rating) || rating <= 0) return "No rating yet";
    return `${Number(rating).toFixed(1)}/5`;
  };
  const calculateAverageRating = () => {
    if (reviews.length === 0) return 0;
    const total = reviews.reduce((sum, r) => sum + r.rating, 0);
    return total / reviews.length;
  };

  const average = calculateAverageRating();

  const checkCondition = async () => {


    try {
      const query = new URLSearchParams({
        productId: productId,
        userId: userInfo.content.userId,
      }).toString();

      const response = await fetch(`http://localhost:6789/api/v1/reviews/check-condition?${query}`, {
        method: "POST",
        headers: {
          Accept: "application/json",
        },
        credentials: "include",
      });
      if (!response.ok) {
        showNotification("Please buy product to review!", 3000, "error");

        return;
      } else {
        handleOpenReviewForm();
      }
    } catch (error) {

    }
  };

  return (
    <>
      <h2 className="text-xl sm:text-2xl font-semibold flex items-center mb-5">
        <MessageCircle className="mr-2 text-blue-500 w-5 h-5 sm:w-6 sm:h-6" /> Product Review
      </h2>

      <div className="mt-2 bg-yellow-50 p-4 rounded-2xl">
        <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 sm:gap-6">
          <div className="flex flex-col items-center mt-4 gap-1 min-w-[150px] sm:min-w-[200px]">
            <div className="text-yellow-500 text-2xl sm:text-3xl font-bold">
              {formatAverageRating(average)}
            </div>
            <div className="flex">
              {[...Array(5)].map((_, i) => (
                <FaStar key={i} className="w-4 h-4 sm:w-5 sm:h-5 text-yellow-400 fill-yellow-400" />
              ))}
            </div>
            <span className="text-blue-600 text-xs sm:text-sm mt-1 underline cursor-pointer">
              {reviews.length} Ratings
            </span>
          </div>

          <div className="flex flex-col gap-2 flex-1 min-w-0">
            {[5, 4, 3, 2, 1].map((star) => {
              const count = reviews.filter((r) => r.rating === star).length;
              const percent = reviews.length ? (count / reviews.length) * 100 : 0;
              return (
                <div key={star} className="flex items-center gap-2">
                  <div className="w-[30px] text-sm text-right">{star} ★</div>
                  <div className="flex-1 h-3 bg-gray-200 rounded relative">
                    <div className="absolute top-0 left-0 h-3 bg-blue-400 rounded" style={{ width: `${percent}%` }}></div>
                  </div>
                  <div className="w-[60px] text-sm text-right">{count} Ratings</div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
      {userInfo && userInfo?.content?.roleId?.roleId !== 6 && (
        !isAddingReview ? (
          <div className="text-center py-5">
            <span className="inline-block w-full h-[1px] bg-gray-300 my-4"></span>
            <h2 className="text-[1.5rem] text-gray-600 py-5">How do you rate this product?</h2>
            <button
              onClick={checkCondition}
              className={`px-8 py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white font-medium rounded-xl shadow-lg hover:bg-gradient-to-r hover:from-blue-600 hover:to-indigo-700 focus:outline-none focus:ring-4 focus:ring-green-300/50 active:scale-95 transition-all duration-300 ease-in-out ${isReviewNow
                ? 'transform scale-110 border-2 border-blue-400 shadow-xl hover:scale-115'
                : ''
                }`}
            >
              Rating Now
            </button>
            <span className="inline-block w-full h-[1px] bg-gray-300 mt-8"></span>
          </div>
        ) : (
          <AddReview
            onCreate={handleCreateReview}
            onCancel={handleCancelReview}
            productId={productId}
            userId={userInfo?.userId}
            fetchReviews={fetchReviews}
          />
        )
      )}



      <h2 className="text-lg sm:text-[1.375rem] font-semibold flex items-center mt-4 text-gray-600">
        Filter
      </h2>
      <div className="flex gap-2 mt-4 ">
        {["All", 5, 4, 3, 2, 1].map((label) => (
          <button
            key={label}
            className={`px-3 py-1 rounded-2xl border border-gray-300 hover:border-gray-400 flex items-center gap-1 ${filterRating === label.toString() ? "bg-blue-100 border-blue-400" : ""
              }`}
            onClick={() => handleFilterRating(label.toString())}
          >
            {label === "All" ? (
              "All"
            ) : (
              <>
                {label}
                <FaStar className="text-yellow-400" />
              </>
            )}
          </button>
        ))}
      </div>
      <div className="mt-6 space-y-5">
        {[...filteredReviews].slice(0, visibleReviews).map((review) => (
          <div
            key={review.reviewId}
            className="flex gap-4 items-start p-5 bg-white rounded-2xl shadow-md border hover:shadow-lg transition duration-300"
          >
            <Avatar>
              <AvatarFallback className="uppercase bg-blue-100 text-blue-700 font-semibold">
                {review.user?.username?.[0]}
              </AvatarFallback>
            </Avatar>

            <div className="flex-1 space-y-3 overflow-hidden">
              {editingReviewId !== review.reviewId && (
                <>
                  <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
                    <span className="font-semibold text-base text-gray-800">
                      {review.user?.username || "Unknown User"}
                    </span>
                    <div className="flex mt-1 sm:mt-0">
                      {[...Array(5)].map((_, i) => (
                        <FaStar
                          key={i}
                          className={`w-4 h-4 ${i < review.rating ? 'text-yellow-400' : 'text-gray-300'}`}
                        />
                      ))}
                    </div>

                  </div>

                  <p className="text-sm text-gray-700 leading-relaxed w-[100%] break-words whitespace-normal">{review.comment}</p>

                  {review.image && (
                    <img
                      src={review.image.startsWith("http") ? review.image : `http://${review.image}`}
                      alt="Review"
                      className="w-32 h-32 object-cover rounded-xl border shadow-sm mt-2"
                      onClick={() => setPreviewImage(review.image.startsWith("http") ? review.image : `http://${review.image}`)}
                    />
                  )}

                  {previewImage && (
                    <div
                      className="fixed inset-0 z-[999] bg-black/40 backdrop-blur-sm flex items-center justify-center transition-opacity duration-300 ease-in-out"
                      onClick={() => setPreviewImage(null)}
                    >
                      <img
                        src={previewImage}
                        alt="Preview"
                        className="max-w-[90vw] max-h-[90vh] rounded-xl shadow-2xl border-4 border-white/90 object-contain transform transition-transform duration-300 hover:scale-105"
                        onClick={(e) => e.stopPropagation()}
                      />
                    </div>
                  )}

                  <p className="text-right text-xs text-gray-500">     
                        {new Date(review.createdAt).toLocaleString('vi-VN', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric'
                      })}</p>

                  {review.user?.userId === userInfo?.content?.userId && (
                    <div className="flex justify-end gap-3">
                      <button onClick={() => handleEditReview(review.reviewId)} className="text-blue-600 font-medium">
                        Edit
                      </button>
                      <button 
                        onClick={async () => {
                          const shortComment = review.comment.length > 20 
                          ? review.comment.slice(0, 20) + "..." 
                          : review.comment;
                        
                        const confirmed = await showConfirm(
                          `Are you sure you want to DELETE post: "${shortComment}"?`,
                          'fail'
                        );
                        
                          if (!confirmed) return;

                          try {
                            const response = await fetch(`http://localhost:6789/api/v1/reviews/remove/${review.reviewId}`, {
                              method: 'DELETE',
                              headers: { 'Content-Type': 'application/json' },
                              credentials: 'include',
                            });
                            const data = await response.json();
                            if (data.success) {
                              showNotification("Review deleted successfully", 3000, "complete");
                              fetchReviews();
                            } else {
                              showNotification("Failed to delete the review", 3000, "fail");
                            }
                          } catch (error) {
                            console.error("Error deleting review:", error);
                            showNotification("An error occurred while deleting the review", 3000, "fail");
                          }
                        }}
                       className="text-red-500 font-medium">
                        Delete
                      </button>
                    </div>
                  )}
                </>
              )}

              {editingReviewId === review.reviewId && (
                <EditReview
                  review={review}
                  onCancel={() => setEditingReviewId(null)}
                  onSave={handleEditSuccess}
                />
              )}
            </div>

          </div>
        ))}

        {filteredReviews.length > 5 && (
          <div className="text-center pt-4">
            <button
              className="text-blue-600 font-medium hover:underline hover:text-blue-700 transition"
              onClick={handleToggleReviews}
            >
              {isExpanded ? "Show Less" : "Show More"}
            </button>
          </div>
        )}
      </div>

    </>
  );
};

export default ReviewProduct;
