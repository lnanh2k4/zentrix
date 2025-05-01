import { useEffect } from "react";
import { X } from "lucide-react";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import timezone from "dayjs/plugin/timezone";

dayjs.extend(utc);
dayjs.extend(timezone);

const ViewDetailNotification = ({ notification, onClose }) => {
  useEffect(() => {
    if (!notification) onClose();
  }, [notification, onClose]);

  if (!notification) return null;

  const formattedTime = dayjs(notification.createdAt)
    .tz("Asia/Ho_Chi_Minh")
    .format("DD/MM/YYYY HH:mm:ss");

  return (
    <div className="fixed inset-0 z-50 bg-black/60 flex items-center justify-center px-4">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto relative animate-in fade-in duration-300 p-6">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-500 hover:text-red-500 transition"
        >
          <X size={24} />
        </button>

        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">📢 Notification Detail</h2>
        </div>

        <div className="space-y-4 text-sm">
          <div>
            <label className="text-gray-600 font-medium block mb-1">Title</label>
            <div className="border rounded-md px-3 py-2 text-gray-800 bg-gray-50">
              {notification.title}
            </div>
          </div>

          <div>
            <label className="text-gray-600 font-medium block mb-1">Description</label>
            <div className="border rounded-md px-3 py-2 text-gray-800 bg-gray-50 whitespace-pre-line">
              {notification.description}
            </div>
          </div>

          <div>
            <label className="text-gray-600 font-medium block mb-1">Status</label>
            <span
              className={`inline-block px-3 py-1 rounded-full text-sm font-semibold ${
                notification.status === 1
                  ? "bg-green-100 text-green-700"
                  : "bg-gray-200 text-gray-600"
              }`}
            >
              {notification.status === 1 ? "Active" : "Inactive"}
            </span>
          </div>

          <div>
            <label className="text-gray-600 font-medium block mb-1">Created At</label>
            <div className="border rounded-md px-3 py-2 text-gray-800 bg-gray-50">
              {formattedTime}
            </div>
          </div>
        </div>

        <div className="flex justify-end mt-6">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default ViewDetailNotification;
