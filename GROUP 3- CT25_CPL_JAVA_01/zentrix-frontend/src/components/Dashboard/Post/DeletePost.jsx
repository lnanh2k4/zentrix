import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { Trash2 } from "lucide-react";
import { showNotification } from '../NotificationPopup';


const DeletePost = ({ post, onDelete, onCancel }) => {

    const handleDelete = async () => {
        try {
            const response = await fetch(`http://localhost:6789/api/v1/dashboard/posts/remove/${post.postId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: "include",
            });

            const data = await response.json();
            if (data.success) {
                onDelete(post.id);
                showNotification("Post deleted successfully", 3000, "complete");
            } else {
            showNotification("Failed to delete the post", 3000, "fail");

            }
        } catch (error) {
            // console.error('Error deleting post:', error);
          showNotification("An error occurred while deleting the post", 3000, "fail");

        }
    };

    return (
      <div className="fixed inset-0 flex items-center justify-center bg-black/10 z-50">
    <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        exit={{ opacity: 0, scale: 0.9 }}
        transition={{ duration: 0.3 }}
        className="bg-white p-6 max-w-md mx-auto rounded-lg shadow-lg text-center"
    >
      
      <div className="flex items-center mb-4 gap-3">
          <Trash2 className="text-red-500 w-6 h-6" />
          <h2 className="text-xl font-semibold text-gray-800">Delete Post</h2>
        </div>
        <p>Are you sure you want to delete <strong>{post.title}</strong>?</p>
        <div className="flex justify-center mt-4 gap-2">
            <Button variant="outline" onClick={onCancel}>Cancel</Button>
            <Button className="bg-red-500 text-white hover:bg-red-600" onClick={handleDelete}>Delete</Button>
        </div>
    </motion.div>
</div>

    );
};

export default DeletePost;
