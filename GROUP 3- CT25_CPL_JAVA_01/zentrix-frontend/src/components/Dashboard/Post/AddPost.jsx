import { useState, useRef, useEffect } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { X } from "lucide-react";
import "tinymce/tinymce";
import "tinymce/themes/silver";
import "tinymce/icons/default";
import "tinymce/plugins/lists";
import "tinymce/plugins/link";
import "tinymce/plugins/image";
import "tinymce/plugins/media";
import "tinymce/plugins/table";
import { showNotification } from '../NotificationPopup';
import { showConfirm } from '../ConfirmPopup';



const AddPost = ({ onCreate, onCancel }) => {
  const [newPost, setNewPost] = useState({
    title: "",
    description: "",
    createdAt: new Date().toISOString().split("T")[0],
    rating: 5,
  });

  const editorRef = useRef(null);
  const [errors, setErrors] = useState({
    title: "",
    description: "",
  });
  useEffect(() => {
    if (!window.tinymce.get("editor")) {
      window.tinymce.init({
        selector: "#editor",
        height: 400,
        menubar: true,
        plugins: ["lists", "link", "image", "media", "table"],
        toolbar:
          "undo redo | formatselect | bold italic underline | alignleft aligncenter alignright | bullist numlist | image media link",

        images_upload_handler: async (blobInfo, success, failure) => {
          try {
            const formData = new FormData();
            formData.append("imageFile", blobInfo.blob());

            const res = await fetch("http://localhost:6789/api/v1/upload/image", {
              method: "POST",
              body: formData,
              credentials: "include",
            });

            const result = await res.json();

            if (res.ok && result.success && result.content) {
              const imageUrl = result.content;
              success(imageUrl.startsWith("http") ? imageUrl : `http://${imageUrl}`);
            } else {
              failure("Failed to upload image");
            }
          } catch (err) {
            console.error("Image upload error:", err);
            failure("Error uploading image");
          }
        },

        setup: (editor) => {
          editorRef.current = editor;
          editor.on("Change", () => {
            setNewPost((prev) => ({ ...prev, description: editor.getContent() }));
          });
        },
      });
    }

    return () => {
      if (window.tinymce.get("editor")) {
        window.tinymce.get("editor").destroy();
      }
    };
  }, []);



  const validateForm = () => {
    let isValid = true;
    const newErrors = { title: "", description: "" };

    if (!newPost.title.trim()) {
      newErrors.title = "Title is required";
      isValid = false;
    } else if (newPost.title.length < 3) {
      newErrors.title = "Title must be at least 3 characters long";
      isValid = false;
    } else if (newPost.title.length > 100) {
      newErrors.title = "Title cannot exceed 100 characters";
      isValid = false;
    }

    const plainText = newPost.description.replace(/<[^>]*>/g, "").trim();
    if (!plainText) {
      newErrors.description = "Description is required";
      isValid = false;
    } else if (plainText.length < 10) {
      newErrors.description = "Description must be at least 10 characters long";
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  const fetchUserInfo = async () => {
    try {
      const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
        withCredentials: true,
      });
      if (response.data.success) {
        return response.data.content; 
      } else {
        return null;
      }
    } catch (error) {
      console.error("Error fetching user info:", error.response?.data || error.message);
      return null;
    }
  };
  

  const handleCreate = async () => {
    const confirmed = await showConfirm(
      `Are you sure you want to save?`,
      'create'
  );
  if (!confirmed) {
    setLoading(false);
    return; 
}
    if (!validateForm()) return;
  
    const userInfo = await fetchUserInfo();
 
    if (!userInfo) {
      alert("Cannot load user info!");
      return;
    }
  
    const formData = new FormData();
    formData.append("title", newPost.title);
    formData.append("description", newPost.description);
    formData.append("createdAt", newPost.createdAt);
    formData.append("rating", newPost.rating);
    formData.append("createdBy", userInfo.userId);
  
    if (userInfo.roleId?.roleId === 3 || userInfo.roleId?.roleName === "Admin") {
      formData.append("status", "ACTIVE");
      formData.append("approvedBy", userInfo.userId);
    }
  
    try {
      const response = await fetch("http://localhost:6789/api/v1/dashboard/posts/add", {
        method: "POST",
        body: formData,
        credentials: "include",
      });
  
      const result = await response.json();
  
      if (response.ok && result.success) {
        showNotification("Post created successfully", 3000, "complete");
        
        onCreate();
      } else {
        showNotification("Failed to create post: " + result.message, 3000, "fail");
      }
    } catch (error) {
      console.error("Create post error:", error);
      showNotification("An error occurred while creating the post.");
    }
  };
  
  
  


  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50 z-50">
      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.95 }}
        className="bg-white w-full max-w-4xl p-6 rounded-xl shadow-xl"
      >
        <div className="flex justify-between items-center mb-4 border-b pb-3">
          <h2 className="text-2xl font-bold text-gray-800">Create New Post</h2>
          <button onClick={onCancel} className="text-gray-500 hover:text-red-500">
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="space-y-4">
          <input
            type="text"
            placeholder="Post title"
            className="w-full p-2 border rounded"
            value={newPost.title}
            onChange={(e) => setNewPost({ ...newPost, title: e.target.value })}
          />{errors.title && (
            <p className="text-red-500 text-sm mt-1">{errors.title}</p>
          )}
          <textarea id="editor" className="w-full rounded border min-h-[300px]"></textarea>
          {errors.description && (
            <p className="text-red-500 text-sm mt-1">{errors.description}</p>
          )}
        </div>


        <div className="flex justify-end gap-3 mt-6">
          <Button variant="outline" onClick={onCancel}>Cancel</Button>
          <Button onClick={handleCreate} className="bg-blue-600 text-white hover:bg-blue-700">
            Submit
          </Button>
        </div>
      </motion.div>
    </div>
  );
};

export default AddPost;
