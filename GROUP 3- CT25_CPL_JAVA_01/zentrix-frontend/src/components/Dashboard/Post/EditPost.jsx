import { useState, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { X } from "lucide-react";
import { motion } from "framer-motion";
import { showNotification } from '../NotificationPopup';
import { showConfirm } from '../ConfirmPopup';



import "tinymce/tinymce";
import "tinymce/themes/silver";
import "tinymce/icons/default";
import "tinymce/plugins/lists";
import "tinymce/plugins/link";
import "tinymce/plugins/image";
import "tinymce/plugins/media";
import "tinymce/plugins/table";

const EditPost = ({ post, onUpdate, onCancel }) => {
  const [updatedPost, setUpdatedPost] = useState(post);
  const [images, setImages] = useState([]);
  const [isDirty, setIsDirty] = useState(false);
  const [existingImages, setExistingImages] = useState(Array.isArray(post.images) ? post.images : []);
  const editorRef = useRef(null);
  const editorId = useRef(`editor-${Date.now()}`);

  useEffect(() => {
    if (!window.tinymce.get(editorId.current)) {
      window.tinymce.init({
        selector: `#${editorId.current}`,
        height: 300,
        menubar: true,
        plugins: "lists link image media table",
        toolbar:
          "undo redo | bold italic underline | alignleft aligncenter alignright | bullist numlist outdent indent | link image media",
        setup: (editor) => {
          editorRef.current = editor;
          editor.on("init", () => {
            editor.setContent(post.description || "");
          });
          editor.on("Change", () => {
            const content = editor.getContent();
            setUpdatedPost((prev) => ({ ...prev, description: content }));
            setIsDirty(true);
          });
        },
      });
    }

    return () => {
      const instance = window.tinymce.get(editorId.current);
      if (instance) instance.destroy();
    };
  }, [post]);

  const handleUpdate = async () => {
    const latestDescription = editorRef.current?.getContent() || "";
    if (!updatedPost.title.trim() || !latestDescription.trim()) {
      showNotification("Please fill in all fields", 3000, 'error');
      return;
    }
    const confirmed = await showConfirm(
      `Are you sure you want to save Post?`,
      'edit'
  );
  if (!confirmed) return;
    const formData = new FormData();
    formData.append("postId", updatedPost.postId);
    formData.append("title", updatedPost.title);
    formData.append("description", latestDescription);
    formData.append("createdAt", updatedPost.createdAt);
    images.forEach((img) => formData.append("imageFiles", img));
    existingImages.forEach((url) => formData.append("existingImageLinks", url));

    try {
      const res = await fetch(`http://localhost:6789/api/v1/dashboard/posts/update/${updatedPost.postId}`, {
        method: "PUT",
        body: formData,
        credentials: "include",
      });
      const data = await res.json();
      if (data.success) {
        showNotification("Post edit successfully ", 3000, "complete");
        onUpdate(data.content);
        onCancel();
      } else {
        alert("Failed to update: " + data.message);
      }
    } catch (err) {
      console.error("Update error:", err);
     showNotification("An error occurred while updating the post", 3000, 'fail');

    }
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    setImages((prev) => [...prev, ...files]);
    setIsDirty(true);
  };

  const handleCancel = async () => {
    if (isDirty) return;
    onCancel();
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/10 z-50">
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.9 }}
        transition={{ duration: 0.3 }}
        className="bg-white p-6 max-w-4xl mx-auto rounded-xl shadow-lg"
      >
        <div className="flex justify-between items-center border-b pb-4 mb-4">
          <h2 className="text-2xl font-semibold text-gray-800">Edit Post</h2>
          <button onClick={handleCancel} className="text-gray-500 hover:text-red-500">
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="space-y-4">
          <input
            type="text"
            placeholder="Title"
            value={updatedPost.title}
            onChange={(e) => {
              setUpdatedPost({ ...updatedPost, title: e.target.value });
              setIsDirty(true);
            }}
            className="w-full p-3 border rounded-md focus:outline-none"
          />

          <textarea id={editorId.current} ref={editorRef} className="w-full rounded border min-h-[300px]"></textarea>

          <div className="flex flex-wrap gap-3">
            {existingImages.map((imgUrl, idx) => (
              <div key={idx} className="relative w-24 h-24 border rounded overflow-hidden">
                <img
                  src={imgUrl.startsWith("http") ? imgUrl : `http://${imgUrl}`}
                  alt={`existing-${idx}`}
                  className="object-cover w-full h-full"
                />
                <button
                  onClick={() => {
                    setExistingImages((prev) => prev.filter((_, i) => i !== idx));
                    setIsDirty(true);
                  }}
                  className="absolute top-0 right-0 bg-gray-600 text-white text-xs px-1 rounded-bl hover:bg-red-600"
                >
                  <X size={14} />
                </button>
              </div>
            ))}

            {images.map((img, idx) => (
              <div key={idx} className="relative w-24 h-24 border rounded overflow-hidden">
                <img
                  src={URL.createObjectURL(img)}
                  alt={`new-${idx}`}
                  className="object-cover w-full h-full"
                />
                <button
                  onClick={() => setImages((prev) => prev.filter((_, i) => i !== idx))}
                  className="absolute top-0 right-0 bg-gray-600 text-white text-xs px-1 rounded-bl hover:bg-red-600"
                >
                  <X size={14} />
                </button>
              </div>
            ))}
          </div>
        </div>

        <div className="flex justify-end gap-3 mt-6">
          <Button variant="outline" onClick={handleCancel}>Cancel</Button>
          <Button className="bg-blue-500 text-white hover:bg-blue-600" onClick={handleUpdate}>Update</Button>
        </div>
      </motion.div>
    </div>
  );
};

export default EditPost;