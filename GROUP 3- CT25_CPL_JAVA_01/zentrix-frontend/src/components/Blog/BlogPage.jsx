import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../ui/Header";
import Footer from "../ui/Footer";
import backgroundImage from "../../assets/19366.jpg";

const BlogPage = () => {
  const [blogs, setBlogs] = useState([]);
  const [filteredBlogs, setFilteredBlogs] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  const navigate = useNavigate();

  const fetchBlogs = async () => {
    try {
      const response = await fetch(`http://localhost:6789/api/v1/posts?page=0&size=100`);
      const data = await response.json();
      if (data.success && data.content) {
        const approvedBlogs = data.content.filter(
          (blog) => blog.status === "ACTIVE"&& blog.createdBy?.userId?.status === 1
        );
        setBlogs(approvedBlogs);
        setFilteredBlogs(approvedBlogs); 
      } else {
        showNotification("Can not load your posts", 3000, 'error');
      }
    } catch (error) {
      console.error("Error fetching posts:", error);
      showNotification("An error occurred while fetching posts", 3000, 'error');

    }
  };

  useEffect(() => {
    fetchBlogs();
  }, []);

  const validBlogs = filteredBlogs.filter(
    (blog) => blog.status === "ACTIVE" 
  );

  const totalPages = Math.ceil(validBlogs.length / pageSize);
  const currentPageBlogs = validBlogs.slice(
    currentPage * pageSize,
    currentPage * pageSize + pageSize
  );

  const handleChangePage = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
    }
  };

  const handleSearch = () => {
    if (searchQuery.trim() === "") {
      setFilteredBlogs(blogs);
    } else {
      const keyword = searchQuery.toLowerCase();
      const filtered = blogs.filter(
        (blog) =>
          blog.title.toLowerCase().includes(keyword) ||
          blog.description.toLowerCase().includes(keyword)
      );
      setFilteredBlogs(filtered);
    }
    setCurrentPage(0);
  };

  return (
    <div>
      <div
        className="object-contain pb-100 pt-20 bg-blue-100"
        style={{ backgroundImage: `url(${backgroundImage})` }}
      >
        <Header />
        <nav className="text-gray-700 mb-4 pt-15 flex justify-start ml-45">
          <ol className="list-reset flex">
            <li>
              <a href="/" className="text-blue-600 hover:underline">Homepage</a>
            </li>
            <li><span className="mx-2">/</span></li>
            <li><a className="text-gray-400">Blog</a></li>
          </ol>
        </nav>

        <header className="text-center mb-12 pt-10">
          <h1 className="text-4xl sm:text-5xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-purple-600 animate-gradient mb-4">Latest Technology Blogs</h1>
          <p className="text-gray-500 text-lg sm:text-xl max-w-2xl mx-auto leading-relaxed">Stay updated with the latest trends in tech, gadgets, and software.</p>
        </header>

        <div className="mb-8 flex justify-center pb-10">
          <div className="relative w-1/2">
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search blog..."
              className="w-full px-6 py-2 pl-10 border border-gray-300 rounded-3xl focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <button
            onClick={handleSearch}
            className="ml-4 group relative px-6 text-white text-lg font-medium bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg hover:from-blue-700 hover:to-purple-700 transition-all duration-300 overflow-hidden shadow-md hover:shadow-lg"
          >
            Search
          </button>
        </div>

        <div className="container mx-auto px-10 p-20 rounded-3xl w-full bg-white h-auto shadow-xl">
          {validBlogs.length === 0 ? (
            <p className="text-center text-gray-500 text-lg">
              🚨 No blog posts available.
            </p>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
              {currentPageBlogs.map((blog, index) => (
                <div key={index}>
                  <BlogCard
                    blog={blog}
                    onReadMore={() => navigate(`/blog/${blog.postId}`)}
                  />
                </div>
              ))}
            </div>
          )}

          <div className="mt-10 flex justify-center items-center space-x-4 pt-10">
            <button
              disabled={currentPage === 0}
              onClick={() => handleChangePage(currentPage - 1)}
              className={`px-4 py-2 rounded-lg transition ${
                currentPage === 0
                  ? "bg-gray-400 cursor-not-allowed"
                  : "bg-blue-600 text-white hover:bg-blue-700"
              }`}
            >
              Previous
            </button>
            <span className="text-gray-800 font-semibold">
              Page {currentPage + 1} of {totalPages}
            </span>
            <button
              disabled={currentPage === totalPages - 1}
              onClick={() => handleChangePage(currentPage + 1)}
              className={`px-4 py-2 rounded-lg transition ${
                currentPage === totalPages - 1
                  ? "bg-gray-400 cursor-not-allowed"
                  : "bg-blue-600 text-white hover:bg-blue-700"
              }`}
            >
              Next
            </button>
          </div>
        </div>
      </div>
      <Footer />
    </div>
  );
};

const BlogCard = ({ blog, onReadMore }) => {
  const extractFirstImageFromDescription = (html) => {
    const div = document.createElement("div");
    div.innerHTML = html;
    const imgTag = div.querySelector("img");
    const src = imgTag?.getAttribute("src");
    if (!src) return null;
    return src.startsWith("http") ? src : `http://${src.startsWith("/") ? src.slice(1) : src}`;
  };

  const firstImage =
    extractFirstImageFromDescription(blog.description) ||
    (blog.images?.[0]
      ? blog.images[0].startsWith("http")
        ? blog.images[0]
        : `http://${blog.images[0].startsWith("/") ? blog.images[0].slice(1) : blog.images[0]}`
      : "/fallback.jpg");

  const plainTextDescription = blog.description
    .replace(/<img[^>]*>/g, "")
    .replace(/<[^>]+>/g, "")
    .slice(0, 150);

  return (
    <div className="flex flex-col bg-white shadow-lg rounded-xl overflow-hidden border border-gray-200 transition duration-300 hover:scale-[1.02] hover:shadow-2xl h-full group">
      <img
        src={firstImage}
        alt="Blog Cover"
        className="w-full h-48 object-cover transition-transform duration-500 group-hover:scale-110"
      />
      <div className="p-6 flex flex-col flex-grow bg-gradient-to-t from-gray-50 to-white min-h-[220px]">
        <h3 className="text-xl font-bold text-gray-900 mb-3 tracking-tight line-clamp-2 group-hover:text-indigo-600 transition-colors duration-300">
          {blog.title}
        </h3>
        <p className="text-gray-700 text-sm flex-grow leading-relaxed line-clamp-3">
          {plainTextDescription}
        </p>
        <button
          onClick={onReadMore}
          className="mt-5 flex items-center gap-2 text-indigo-600 font-semibold text-sm hover:text-indigo-800 transition-all duration-300 group/button cursor-pointer"
        >
          <span className="relative">Read More</span>
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="w-5 h-5 transition-transform duration-300 ease-in-out group-hover/button:translate-x-2"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>
    </div>
  );
};

export default BlogPage;
