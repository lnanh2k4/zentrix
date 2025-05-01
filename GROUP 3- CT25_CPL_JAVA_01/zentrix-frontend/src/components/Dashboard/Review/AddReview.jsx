import { getInfo } from "@/context/ApiContext";
import React, { useState, useEffect } from "react";
import { showNotification } from "../NotificationPopup";


const AddReview = ({ onCreate, onCancel, productId, fetchReviews }) => {
    const [review, setReview] = useState({ name: "Current User", rating: 5, comment: "", image: null });
    const [imagePreview, setImagePreview] = useState(null);
    const [error, setError] = useState(null);
    // console.log("Product ID in AddReview:", productId);
    useEffect(() => {
        setReview((prevReview) => ({ ...prevReview, rating: 5 }));
    }, []);

    const handleImageUpload = (e) => {
        const file = e.target.files[0];
        if (file) {
            setReview({ ...review, image: file });
            const reader = new FileReader();
            reader.onloadend = () => setImagePreview(reader.result);
            reader.readAsDataURL(file);
        }
    };

    const removeImage = () => {
        setReview({ ...review, image: null });
        setImagePreview(null);
    };

    const submitReview = async () => {
        setError(null);
        const userInfo = await getInfo();
        if (!userInfo) {
            setError("Please login to submit a review.");
            return;
        }

        if (!review.comment.trim()) {
            setError("Comment cannot be empty.");
            return;
        }
        
        if (!review.comment.trim().length > 100) {
            setError("Only 100 character .");
            return;
        }
        
        const formData = new FormData();
        formData.append("productId", productId); 
        formData.append("userId", userInfo.content.userId);
        formData.append("rating", review.rating);
        formData.append("comment", review.comment.trim());
        if (review.image) {
            formData.append("imageFile", review.image);
        }

        try {
            const response = await fetch("http://localhost:6789/api/v1/reviews/add", {
                method: "POST",
                headers: {
                    Accept: "application/json",
                },
                body: formData,
                credentials: "include",
            });
            if (response.ok) {
                const result = await response.json();
               showNotification("Review submitted successfully!", 3000, "complete");

                if (fetchReviews) fetchReviews();
                if (result.content) onCreate(result.content);
                onCancel();
            } else {
                const errorData = await response.json();
                if(errorData.content == 'Review Exception: Review not found'){
                    showNotification("Please buy product to review!", 3000, "error");

                }
                setError(errorData.message || "Failed to submit review.");
            }
        } catch (error) {
            console.error("Error submitting review:", error);
            setError("An error occurred while submitting the review.");
        }
    };

    return (
        <div className="mt-4 bg-gray-100 p-4 rounded-lg shadow-md w-full">
            <h2 className="text-lg font-semibold mb-3 text-gray-700">Add a Review</h2>
            <div className="flex space-x-1 mb-3">
                {[1, 2, 3, 4, 5].map((star) => (
                    <div key={star}>
                        <input
                            type="radio"
                            id={`star${star}`}
                            name="rating"
                            value={star}
                            className="hidden"
                            onChange={() => setReview({ ...review, rating: star })}
                            checked={review.rating === star}
                        />
                        <label
                            htmlFor={`star${star}`}
                            className={`text-2xl cursor-pointer transition ${
                                review.rating >= star ? "text-yellow-500" : "text-gray-400"
                            } hover:text-yellow-500`}
                        >
                            ★
                        </label>
                    </div>
                ))}
            </div>
            <textarea
                placeholder="Share your thoughts..."
                value={review.comment}
                onChange={(e) =>{
                    if(e.target.value.trim().length <= 100 ){
                        setReview({ ...review, comment: e.target.value })

                    }
                }}
                className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-400"
                rows={3}
            />  
            <div className="space-y-4 pt-4">
                <div className="flex  flex-col items-start gap-2">
                <div style={{ textAlign: 'right', color: review.comment.length === 100 && 'red'}}><text>{review.comment.trim().length}/100</text></div>
                    <label className="bg-gray-400 text-white px-4 py-2 rounded-lg cursor-pointer hover:bg-blue-700">
                        Select file
                        <input
                            type="file"
                            accept="image/jpeg, image/png, image/bmp, image/webp"
                            className="hidden"
                            onChange={handleImageUpload}
                        />
                    </label>
                    {imagePreview && (
                        <div className="relative">
                            <img src={imagePreview} alt="Preview" className="w-20 h-20 object-cover mt-2 mr-2" />
                            <button
                                onClick={removeImage}
                                className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center"
                            >
                                ×
                            </button>
                        </div>
                    )}
                </div>
            </div>
            <div className="flex justify-end mt-3 gap-3">
                <button
                    onClick={onCancel}
                    className="px-4 py-2 text-gray-600 border border-gray-400 rounded-lg hover:bg-gray-200"
                >
                    Cancel
                </button>
                <button
                    onClick={submitReview}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition"
                >
                    Submit
                </button>
            </div>
        </div>
    );
};

export default AddReview;