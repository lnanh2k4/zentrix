import React from "react";
import { X, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";

const DeleteNotification = ({ notification, onCancel, onConfirm }) => {
  if (!notification) return null;

  
  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center px-4">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6 relative animate-in fade-in duration-300">
        <button
          onClick={onCancel}
          className="absolute top-4 right-4 text-gray-500 hover:text-red-500 transition"
        >
          <X size={22} />
        </button>

        <div className="flex items-center mb-4 gap-3">
          <Trash2 className="text-red-500 w-6 h-6" />
          <h2 className="text-xl font-semibold text-gray-800">Delete Notification</h2>
        </div>

        <p className="text-gray-600 mb-6">
          Are you sure you want to delete the notification{" "}
          <span className="font-medium text-red-600">"{notification.title}"</span>? This action cannot be undone.
        </p>

        <div className="flex justify-end gap-3">
          <Button variant="outline" onClick={onCancel}>
            Cancel
          </Button>
          <Button className="bg-red-600 text-white hover:bg-red-700" onClick={() => onConfirm(notification.notiId)}>
            Delete
          </Button>
        </div>
      </div>
    </div>
  );
};

export default DeleteNotification;
