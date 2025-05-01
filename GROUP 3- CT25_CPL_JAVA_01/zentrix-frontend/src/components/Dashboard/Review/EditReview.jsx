import { useState, useEffect } from "react";
import { showNotification } from "../NotificationPopup";


const EditReview = ({ review, onSave, onCancel }) => {
  const [updatedReview, setUpdatedReview] = useState({ ...review });
  const [imagePreview, setImagePreview] = useState(null);

  useEffect(() => {
    if (review.image) {
      setImagePreview(review.image.startsWith("http") ? review.image : `http://${review.image}`);
    }
  }, [review.image]);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setUpdatedReview({ ...updatedReview, imageFile: file });
      const reader = new FileReader();
      reader.onloadend = () => setImagePreview(reader.result);
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async () => {
    const formData = new FormData();
    formData.append("reviewId", updatedReview.reviewId);
    formData.append("rating", updatedReview.rating);
    formData.append("comment", updatedReview.comment);
    if (updatedReview.imageFile) {
      formData.append("imageFile", updatedReview.imageFile);
    }

    try {
      const res = await fetch(`http://localhost:6789/api/v1/dashboard/reviews/update/${updatedReview.reviewId}`, {
        method: "PUT",
        body: formData,
        credentials: "include",
      });

      const result = await res.json();
      if (res.ok && result.success) {
        showNotification("Review update successful", 3000, 'complete');
        onSave();
      } else {
        showNotification("Failed to update review", 3000, 'fail');

      }
    } catch (err) {
      showNotification("Error updating review.", 3000, 'error');

    }
  };

  return (
    <div className="">

      <div className="flex gap-1 mb-3">
        {[1, 2, 3, 4, 5].map((star) => (
          <label key={star}>
            <input
              type="radio"
              className="hidden"
              checked={updatedReview.rating === star}
              onChange={() => setUpdatedReview({ ...updatedReview, rating: star })}
            />
            <span className={`text-2xl cursor-pointer ${updatedReview.rating >= star ? 'text-yellow-400' : 'text-gray-300'}`}>
              ★
            </span>
          </label>
        ))}
      </div>

      <textarea
        rows={3}
        className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-400"
        value={updatedReview.comment}
        onChange={(e) => setUpdatedReview({ ...updatedReview, comment: e.target.value })}
        placeholder="Update your thoughts..."
      />

      <div className="flex flex-col items-start gap-3 mt-4">
        <label className=" self-start bg-blue-500 text-white px-4 py-2 rounded-lg cursor-pointer hover:bg-blue-600 transition">
          Change Image
          <input type="file" accept="image/*" onChange={handleImageChange} className="hidden" />
        </label>
        {imagePreview && (
          <img src={imagePreview} alt="Preview" className="w-24 h-24 object-cover rounded-lg shadow self-start" />
        )}
   
      </div>

      <div className="flex justify-end gap-3 mt-4">
        <button
          onClick={onCancel}
          className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-100 text-gray-700"
        >
          Cancel
        </button>
        <button
          onClick={handleSubmit}
          className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition"
        >
          Save
        </button>
      </div>
    </div>
  );
};

export default EditReview;
