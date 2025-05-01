import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Edit, Trash2, Plus, Frown, Eye, Search } from "lucide-react";
import AddPost from "./AddPost";
import EditPost from "./EditPost";
// import DeletePost from "./DeletePost";
import ViewDetailPost from "./ViewPostDetail";
import { getInfo } from "@/context/ApiContext";
import { showConfirm } from "../ConfirmPopup";
import { showNotification } from "../NotificationPopup";
import axios from "axios";
const PostTable = () => {
  const [posts, setPosts] = useState([]);
  const [isAddingPost, setIsAddingPost] = useState(false);
  const [editingPost, setEditingPost] = useState(null);
  // const [deletingPost, setDeletingPost] = useState(null);
  const [viewDetailPost, setViewDetailPost] = useState(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [userInfo, setUserInfo] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [filteredPosts, setFilteredPosts] = useState([]);


useEffect(()=> {
  setPage(0);
},[pageSize])

  const fetchPosts = async () => {
    try {
      const query = new URLSearchParams({ page, size: pageSize }).toString();
      const response = await fetch(`http://localhost:6789/api/v1/dashboard/posts?${query}`, {
        credentials: 'include',
      });

      const data = await response.json();

      if (data.success && data.content) {
        // console.log("DATA CỦA POST NÈ", data.content);
        setPosts(data.content);
        setFilteredPosts(data.content);
        setTotalPages(data.pagination?.totalPages || 0);
      } else {
        showNotification("Failed to load posts.", 3000, 'error');

      }
    } catch (error) {
      console.error("Error fetching posts:", error);
      showNotification("An error occurred while fetching posts.", 3000, 'error');

    }
  };

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const info = await getInfo();

        setUserInfo(info);
      } catch (err) {
        console.error("Failed to fetch user info:", err);
      }
    };

    fetchUserInfo();
  }, []);

  const isAdmin = userInfo?.content?.roleId?.roleName === "Admin";

  useEffect(() => {
    fetchPosts();
  }, [page, pageSize]);

  useEffect(() => {
    const delayDebounce = setTimeout(() => {
      if (!searchTerm.trim()) {
        setFilteredPosts(posts);
      } else {
        const lowerTerm = searchTerm.toLowerCase();
        setFilteredPosts(
          posts.filter(
            (post) =>
              post.title?.toLowerCase().includes(lowerTerm) ||
              post.description?.toLowerCase().includes(lowerTerm)
          )
        );
      }
    }, 500);

    return () => clearTimeout(delayDebounce);
  }, [searchTerm, posts]);

  const handleCreate = () => {
    fetchPosts();
    setIsAddingPost(false);
  };

  const handleUpdate = (updatedPost) => {
    setEditingPost(null);
    setPosts((prevPosts) =>
      prevPosts.map((post) =>
        post.postId === updatedPost.postId ? updatedPost : post
      )
    );
  };

  // const handleDelete = (id) => {
  //   setPosts(posts.filter((post) => post.postId !== id));
  //   setDeletingPost(null);
  //   fetchPosts();
  // };

  const handleApprove = async (post) => {
    try {
      const userRes = await axios.get("http://localhost:6789/api/v1/auth/info", {
        withCredentials: true,
      });
      const approvedByUserId = userRes?.data?.content?.userId;
      if (!approvedByUserId) {
        alert("Cannot identify current user to approve post");
        return;
      }

      const response = await axios.patch(
        `http://localhost:6789/api/v1/dashboard/posts/${post.postId}/approve`,
        {},
        {
          params: { approvedById: approvedByUserId },
          withCredentials: true,
        }
      );

      if (response.data.success) {
        const updatedPostRes = await axios.get(
          `http://localhost:6789/api/v1/dashboard/posts/${post.postId}`,
          { withCredentials: true }
        );

        const updatedPost = updatedPostRes.data?.content;
        setPosts((prevPosts) =>
          prevPosts.map((p) => (p.postId === post.postId ? updatedPost : p))
        );
      } else {
        alert("Failed to approve the post");
      }
    } catch (error) {
      console.error("Error approving post:", error);
      alert("An error occurred while approving the post.");
    }
  };
  const extractFirstImageFromDescription = (html) => {
    if (!html) return null;
    const div = document.createElement("div");
    div.innerHTML = html;
    const imgTag = div.querySelector("img");
    const src = imgTag?.getAttribute("src");
    return src
      ? src.startsWith("http")
        ? src
        : `http://${src.startsWith("/") ? src.slice(1) : src}`
      : null;
  };


  return (
    <div className="bg-white p-8 shadow-md rounded-lg relative animate-neonTable">
      {isAddingPost && (
        <AddPost onCreate={handleCreate} onCancel={() => setIsAddingPost(false)} />
      )}

      {posts.length > 0 ? (
        <div>

          <div className="flex justify-between items-center mb-4 gap-4">
            <Button
              className="bg-blue-500 text-white flex items-center px-6 py-2 rounded-lg shadow-md transition delay-150 duration-300 ease-in-out hover:-translate-y-1 hover:scale-110 hover:bg-[#0044cc]"
              onClick={() => setIsAddingPost(true)}
            >
              Create <Plus className="h-5 w-5 mr-2" />
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

          <div className="overflow-x-auto">
            <table className="min-w-full border border-gray-300 rounded-lg text-center">
              <thead>
                <tr className="bg-gradient-to-r from-blue-500 to-indigo-500 text-white">
                  <th className="border p-3 font-bold">ID</th>
                  <th className="border p-3 font-bold">Title</th>
                  <th className="border p-3 font-bold">Description</th>
                  <th className="border p-3 font-bold">Created By</th>
                  <th className="border p-3 font-bold">Status</th>
                  <th className="border p-3 font-bold">Image</th>
                  <th className="border p-3 font-bold">Created At</th>
                  <th className="border p-3 font-bold">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {filteredPosts.map((post) => (
                  <tr key={post.postId} className="hover:bg-gray-100">
                    <td className="border p-3 text-right">{post.postId}</td>
                    <td className="border p-3 text-left max-w-[300px] break-words">
                      {post.title}
                    </td>
                    <td
                      className="line-clamp-2 text-left text-sm text-gray-700 max-w-[250px]"
                      dangerouslySetInnerHTML={{ __html: post.description }}
                    ></td>
                    <td className="border p-3 text-left">
                      {post.createdBy?.userId?.username}
                    </td>
                  
                    <td className="border p-3 text-left ">
                      {post.status === "ACTIVE" ? (
                        <span className="text-green-600 font-bold">ACTIVE</span>
                      ) : (
                        <span className="text-red-500 font-bold">{post.status}</span>
                      )}
                    </td>
                    <td className="border p-3">
                      {(() => {
                        const imageLink =
                          (post.images?.[0] &&
                            (post.images[0].startsWith("http")
                              ? post.images[0]
                              : `http://${post.images[0].startsWith("/") ? post.images[0].slice(1) : post.images[0]}`)) ||
                          extractFirstImageFromDescription(post.description);

                        return imageLink ? (
                          <img
                            src={imageLink}
                            alt="Post"
                            className="w-16 h-16 object-cover rounded border mx-auto"
                          />
                        ) : (
                          <p>No image</p>
                        );
                      })()}
                    </td>


                    <td className="border p-3">
                      {new Date(post.createdAt).toLocaleString('vi-VN', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric'
                      })}
                    </td>

                    <td className="border p-3">
                      <div className="flex justify-center gap-3">
                        <Button
                          variant="outline"
                          size="icon"
                          className="text-blue-600 hover:text-blue-800"
                          onClick={() => setViewDetailPost(post)}
                        >
                          <Eye className="h-4 w-4" />
                        </Button>

                        {viewDetailPost && (
                          <ViewDetailPost post={viewDetailPost} onClose={() => setViewDetailPost(null)} />
                        )}
                        <Button
                          variant="outline"
                          size="icon"
                          className="text-yellow-600 hover:text-yellow-800"
                          onClick={() => setEditingPost(post)}
                        >
                          <Edit className="h-4 w-4" />
                        </Button>

                        {editingPost && (
                          <EditPost
                            post={editingPost}
                            onUpdate={handleUpdate}
                            onCancel={() => setEditingPost(null)}
                          />
                        )}

                        <Button
                          variant="outline"
                          size="icon"
                          className="text-red-600 hover:text-red-800"
                          onClick={async () => {
                            const confirmed = await showConfirm(`Are you sure you want to DELETE post: "${post.title}"?`, 'fail');
                            if (!confirmed) return;

                            try {
                              const response = await fetch(`http://localhost:6789/api/v1/dashboard/posts/remove/${post.postId}`, {
                                method: 'DELETE',
                                headers: { 'Content-Type': 'application/json' },
                                credentials: 'include',
                              });
                              const data = await response.json();
                              if (data.success) {
                                showNotification("Post deleted successfully", 3000, "complete");
                                fetchPosts(); 
                              } else {
                                showNotification("Failed to delete the post", 3000, "fail");
                              }
                            } catch (error) {
                              console.error("Error deleting post:", error);
                              showNotification("An error occurred while deleting the post", 3000, "fail");
                            }
                          }}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>


                        {/* {deletingPost && (
                          <DeletePost
                            post={deletingPost}
                            onDelete={handleDelete}
                            onCancel={() => setDeletingPost(null)}
                          />
                        )} */}

                        {post.status === "VERIFYING" && isAdmin && (
                          <Button
                            variant="outline"
                            size="icon"
                            className="bg-green-400 hover:bg-green-800 text-white w-auto px-2"
                            onClick={() => handleApprove(post)}
                          >
                            Approve
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
            <div>
              <span>Items per page:</span>
              <select
                value={pageSize}
                onChange={(e) => setPageSize(parseInt(e.target.value))}
                className="border border-gray-300 rounded-md p-2 ml-2"
              >
                {[5, 10, 15, 20].map((size) => (
                  <option key={size} value={size}>{size}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex gap-2 justify-center">
            <Button variant="outline" className="text-sm px-3 py-1" onClick={() => setPage(Math.max(page - 1, 0))} disabled={page === 0}>
              Previous
            </Button>
            {Array.from({ length: totalPages }, (_, index) => (
              <Button
                key={index}
                onClick={() => setPage(index)}
                className={index === page ? "bg-blue-500 text-white" : "bg-white text-black"}
              >
                {index + 1}
              </Button>
            ))}
            <Button className="text-sm px-3 py-1" variant="outline" onClick={() => setPage(Math.min(page + 1, totalPages - 1))} disabled={page >= totalPages - 1}>
              Next
            </Button>
          </div>

        </div>
      ) : (
        <div className="flex flex-col items-center justify-center h-64">
          <Frown className="h-16 w-16 text-gray-400 mb-4" />
          <p className="text-gray-600 text-lg">No posts found.</p>
        </div>
      )}
    </div>
  );

};

export default PostTable;