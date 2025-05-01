import { useEffect, useState } from "react";
import axios from "axios";
import { Button } from "@/components/ui/button";
import { Eye, Trash2, Frown, Plus, Search } from "lucide-react";
import AddNotification from "./AddNotification";
import ViewDetailNotification from "./ViewDetailNotification";
import { getInfo } from "@/context/ApiContext"; 
import { showNotification } from '../NotificationPopup';
import { showConfirm } from "../ConfirmPopup";

const NotificationTable = () => {
  const [notifications, setNotifications] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [viewDetail, setViewDetail] = useState(null);
  const [deleteItem, setDeleteItem] = useState(null);
  const [message, setMessage] = useState("");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [isAdding, setIsAdding] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
useEffect(() => {
  setPage(0)
},[pageSize])
  const fetchNotifications = async () => {
    try {
      const response = await axios.get("http://localhost:6789/api/v1/dashboard/notifications", {
        params: { page, size: pageSize },
        withCredentials: true,
      });

      const data = response.data;

      if (data?.data) {
        setNotifications(data.data);
        setTotalPages(data.totalPages || 1);
      } else {
        setNotifications([]);
        setTotalPages(1);
      }

    } catch (error) {
      console.error("Fetch error:", error);
      setMessage("Failed to load notifications");
    }
  };


  useEffect(() => {
    fetchNotifications(); 

    const interval = setInterval(() => {
        fetchNotifications();
    }, 5000);

    return () => clearInterval(interval); 
}, [page, pageSize]);

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      if (!searchTerm.trim()) {
        setFiltered(notifications);
      } else {
        const lowerTerm = searchTerm.toLowerCase();
        setFiltered(
          notifications.filter(
            (n) =>
              n.title?.toLowerCase().includes(lowerTerm) ||
              n.description?.toLowerCase().includes(lowerTerm)
          )
        );
      }
    }, 500);

    return () => clearTimeout(delayDebounce);
  }, [searchTerm, notifications]);


  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const res = await getInfo();             
        setUserInfo(res?.content?.roleId?.roleId);    
        // console.log("USER ID NÈ",)      
         
      } catch (error) {
        console.error("Failed to get user info:", error);
      }
    };
  
    fetchUserInfo();
  }, []);
  

  const handleAdd = (newNotification) => {
    setNotifications((prev) => {
      const updatedNotifications = [newNotification, ...prev];
      return updatedNotifications.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    });
    setIsAdding(false);
    setPage(0);
    setTimeout(fetchNotifications, 1000);
  };

  const handleDelete = async (id) => {
    // console.log("update ssta")
    
    try {
    await fetch(`http://localhost:6789/api/v1/dashboard/notifications/${id}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: "include",
    });
   
      fetchNotifications();
      setDeleteItem(null)
      showNotification("Notification deleted successfully", 3000, "complete");

 
    } catch (err) {
      console.error("Delete error:", err);
      setMessage("Failed to update satatus notification");
    }
  };

  return (
    <div className="bg-white p-8 shadow-md rounded-lg animate-neonTable">
      <div className="flex justify-between items-center mb-4 gap-4">
        <Button onClick={() => setIsAdding(true)} className="flex items-center gap-2 bg-blue-500 text-white">
        Create <Plus size={18} /> 
        </Button>
        <div className="relative w-1/3 flex justify-end">
          <input
            type="text"
            placeholder="Search title or description..."
            className="border px-3 py-1 rounded-md text-sm w-full"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <Search size={20} className="absolute right-3 top-3 text-gray-500" />
        </div>
      </div>

      {message && <div className="mb-4 text-red-600 font-medium">{message}</div>}

      {isAdding && <AddNotification onAdd={handleAdd} onClose={() => setIsAdding(false)} />}

      {viewDetail && (
        <ViewDetailNotification notification={viewDetail} onClose={() => setViewDetail(null)} />
      )}

      {filtered.length > 0 ? (
        <>
        <div className="overflow-x-auto">
        <table className="w-full border border-gray-300 text-center rounded-md border-collapse">
  <thead className="bg-gradient-to-r from-blue-500 to-indigo-500 text-white">
    <tr>
      <th className="p-3 border ">ID</th>
      <th className="p-3 border ">Created by</th>
      <th className="p-3 border">Title</th>
      <th className="p-3 border ">Description</th>
      <th className="p-3 border ">Date</th>
      <th className="p-3 ">Status</th>
      <th className="p-3 border">Actions</th>
    </tr>
  </thead>
  <tbody className="bg-white">
    {filtered.filter(noti => !noti.userId && noti.status === 1 ).map((noti,index) => (
      <tr key={noti.notiId} className="hover:bg-gray-50">
        <td className="p-3 border">{pageSize * page + index +1}</td>
        <td className="p-3 border text-left">{noti.createdByName}</td>
        <td className="p-3 border text-left">{noti.title}</td>
        <td className="p-3 border text-left">{noti.description}</td>
        <td className="p-3 border">{new Date(noti.createdAt).toLocaleDateString()}</td>
        <td className="p-3 border  font-medium">
          {noti.status === 1 ? (
            <span className="text-green-600 font-bold">ACTIVE</span>
          ) : (
            <span className="text-red-600">Inactive</span>
          )}
        </td>
        <td className="p-3 border">
        <div className="flex justify-center gap-3">
          <Button size="icon" variant="outline" onClick={() => setViewDetail(noti)}>
            <Eye className="h-4 w-4 text-blue-600" />
          </Button>
          {userInfo !== 4 &&(
      <Button
      size="icon"
      variant="outline"
      className="text-red-600"
      onClick={async () => {
        const confirmed = await showConfirm(`Are you sure you want to delete "${noti.title}"?`, 'fail');
        if (!confirmed) return;
    
        handleDelete(noti.notiId);
      }}
    >
      <Trash2 className="h-4 w-4" />
        </Button>
    
          )}
          </div>
        </td>
      </tr>
    ))}
  </tbody>
</table>

          </div>
          <div className="flex gap-2 items-center justify-end pt-4">
            <span>Items per page:</span>
            <select
              value={pageSize}
              onChange={(e) => setPageSize(parseInt(e.target.value))}
              className="border border-gray-300 rounded-md p-2"
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={15}>15</option>
              <option value={20}>20</option>
            </select>
          </div>
          <div className="flex justify-center mt-6 gap-2">
            <Button variant="outline" className="text-sm px-3 py-1" onClick={() => setPage((prev) => Math.max(prev - 1, 0))} disabled={page === 0}>
              Previous
            </Button>
            {Array.from({ length: totalPages }, (_, idx) => (
              <Button
                key={idx}
                onClick={() => setPage(idx)}
                className={idx === page ? "bg-blue-500 text-white" : "bg-white text-black border border-gray-300"}
              >
                {idx + 1}
              </Button>
            ))}
            <Button variant="outline" className="text-sm px-3 py-1" onClick={() => setPage((prev) => Math.min(prev + 1, totalPages - 1))} disabled={page >= totalPages - 1}>
              Next
            </Button>
          </div>
        </>
      ) : (
        <p className="text-center text-gray-500 py-10 flex justify-center gap-3">
          No notifications available <Frown size={24} />
        </p>
      )}
    </div>
  );
};

export default NotificationTable;