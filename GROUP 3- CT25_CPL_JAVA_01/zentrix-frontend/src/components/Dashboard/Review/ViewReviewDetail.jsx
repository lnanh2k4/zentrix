import React from "react";
import { X } from "lucide-react";
import { FaStar } from "react-icons/fa";


const ViewDetailReview = ({ review, onClose }) => {
  if (!review) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/10 flex items-center justify-center px-4">
      <div className="bg-white rounded-2xl max-w-2xl w-full shadow-2xl relative p-8 space-y-6 max-h-[90vh] overflow-y-auto transform transition-all duration-300 hover:shadow-[0_0_20px_rgba(255,255,255,0.4)]">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-600 hover:text-red-500 hover:scale-110 transition-all duration-200 ease-in-out"
        >
          <X size={24} />
        </button>
  
        <h2 className="text-2xl font-extrabold text-gray-900 bg-gradient-to-r transition-transform duration-300 hover:scale-105">
          Review Detail
        </h2>
  
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm text-gray-700 bg-gray-50 p-4 rounded-lg shadow-inner">
          <div className="space-y-2">
            <div>
              <strong className="text-blue-600">Name:</strong>{" "}
              <span className="text-gray-800">{review.user.username || "Anonymous"}</span>
            </div>
            <div>
              <strong className="text-blue-600">Email:</strong>{" "}
              <span className="text-gray-800">{review.user.email || "N/A"}</span>
            </div>
          </div>
          <div className="space-y-2">
            <div>
              <strong className="text-blue-600">Date:</strong>{" "}
              <span className="text-gray-800">{review.createdAt}</span>
            </div>
            <div>
              <strong className="text-blue-600">Rating:</strong>{" "}
              <span className="text-yellow-500 font-semibold">{review.rating} <FaStar className="inline h-5 w-5 text-yellow-500" /></span>
            </div>
          </div>
        </div>
  
        <div className="pt-4 border-t-2 border-blue-100">
          <h3 className="font-semibold text-gray-800 text-lg mb-3">Comment:</h3>
          <p className="text-gray-800 whitespace-pre-wrap leading-relaxed bg-gray-50 p-4 rounded-lg shadow-sm hover:bg-gray-100 transition-colors duration-200">
            {review.comment || "No comment."}
          </p>
        </div>
  
        {review.image && (
  <div className="pt-4 border-t-2 border-blue-100">
    <div className="flex justify-center">
      <img
        src={review.image.startsWith("http") ? review.image : `http://${review.image}`}
        alt="Review Attachment"
        className="w-48 h-48 object-cover rounded-xl border-2  shadow-md hover:scale-105 transition-transform duration-300"
      />
    </div>
  </div>
)}

      </div>
    </div>
  );
};

export default ViewDetailReview;
