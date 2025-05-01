import React from "react";
import { X } from "lucide-react";

const ViewDetailPost = ({ post, onClose }) => {
  if (!post) return null;

  const parser = new DOMParser();
  const doc = parser.parseFromString(post.description || "", "text/html");
  
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/10 z-50">
  <div className="bg-white rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-y-auto shadow-2xl relative transform transition-all duration-300 hover:shadow-[0_0_25px_rgba(255,255,255,0.5)]">
    <button
      onClick={onClose}
      className="absolute top-4 right-4 text-gray-600 hover:text-red-600 hover:scale-110 transition-all duration-200 ease-in-out"
    >
      <X size={26} />
    </button>

    <div className="p-8 space-y-6">
      {post.postId < 6 && (
        <img
          src={
            post.images?.[0]?.startsWith("http")
              ? post.images?.[0]
              : `http://${post.images?.[0]}`
          }
          className="w-full h-64 object-cover rounded-lg border-2 border-indigo-200 shadow-md transform hover:scale-105 transition-transform duration-300"
          alt="Post image"
        />
      )}

      <h2 className="text-3xl font-extrabold text-gray-900 text-left pb-4 bg-gradient-to-r transition-all duration-300 hover:scale-105">
        {post.title}
      </h2>

      <div
        className="prose max-w-none text-gray-800 leading-relaxed tracking-wide"
        dangerouslySetInnerHTML={{ __html: post.description }}
      />
      <div className="pt-6 text-sm text-gray-600 flex justify-between items-center border-t-2 border-indigo-100">
        <span className="flex items-center gap-2 transition-colors duration-200 hover:text-indigo-600">
          👤 {post.createdBy?.userId?.username || "Unknown"}
        </span>
        <span className="flex items-center gap-2 transition-colors duration-200 hover:text-indigo-600">
          📅 {post.createdAt}
        </span>
      </div>
    </div>
  </div>
</div>

  );
};

export default ViewDetailPost;
