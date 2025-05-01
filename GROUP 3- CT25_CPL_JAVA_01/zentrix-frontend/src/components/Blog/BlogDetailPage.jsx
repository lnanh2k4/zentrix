import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Header from "../ui/Header";
import Footer from "../ui/Footer";
import backgroundImage from "../../assets/19366.jpg";


const BlogDetail = () => {
    const { postId } = useParams();
    const [blog, setBlog] = useState(null);
    const navigate = useNavigate();

    const fetchBlogDetail = async () => {
        try {
            const response = await fetch(`http://localhost:6789/api/v1/posts/${postId}`);
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            const data = await response.json();
            if (data.success && data.content) {
                // console.log("Blog Data:", data.content);
                setBlog(data.content);
            } else {
                console.warn("API response missing content:", data);
                showNotification("Can not load blog ", 3000, 'error');

            }
        } catch (error) {
            showNotification("Error when load Blog", 3000, 'error');

        }
    };

    useEffect(() => {
        // console.log("Post ID:", postId);
        fetchBlogDetail();
    }, [postId]);
    const extractFirstImageFromDescription = (html) => {
        const div = document.createElement("div");
        div.innerHTML = html;
        const imgTag = div.querySelector("img");
        if (!imgTag) return null;

        const src = imgTag.getAttribute("src");
        if (!src) return null;

        return src.startsWith("http")
            ? src
            : `http://${src.startsWith("/") ? src.slice(1) : src}`;
    };

    if (!blog) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-100">
                <p className="text-xl text-gray-600">Loading...</p>
            </div>
        );
    }
    const isValidImage = blog.images && blog.images.length > 0 ;

    const firstImage =
        (blog.images && blog.images.length > 0 && blog.images[0]);

    return (
        <div className="flex flex-col min-h-400 py-5" style={{ backgroundImage: `url(${backgroundImage})` }}>
            <div>
                <Header />
            </div>

            <div className="flex-grow max-w-7xl mx-auto px-4 py-8 bg-white rounded-lg shadow-lg mt-[-50px] z-10 min-h-200 min-w-300">
                <nav className="text-gray-700 mb-6 flex justify-start max-w-7xl mx-auto pt-30">
                    <ol className="list-reset flex">
                        <li>
                            <a href="/" className="text-blue-600 hover:underline">
                                Homepage
                            </a>
                        </li>
                        <li>
                            <span className="mx-2">/</span>
                        </li>
                        <li>
                            <a href="/blog" className="text-blue-600 hover:underline">
                                Blog
                            </a>
                        </li>
                        <li>
                            <span className="mx-2">/</span>
                        </li>
                        <li>
                            <span className="text-gray-400">{blog.title}</span>
                        </li>
                    </ol>
                </nav>
                <h1 className="text-4xl md:text-5xl font-extrabold text-gray-900 mb-6 leading-tight uppercase tracking-wide border-b-4 border-blue-500 py-2">
                    {blog.title}
                </h1>

                <div className="flex items-center text-gray-600 text-sm mb-6">
                    <span>Created At: {new Date(blog.createdAt).toLocaleDateString()}</span>
                    <span className="mx-2">•</span>
                    <span>Author: {blog.createdBy?.userId?.username || "N/A"}</span>
                </div>

                {isValidImage && (
                    <div className="mb-8">
                        <img
                            src={`http://${firstImage}`}
                            alt={blog.title}
                            className="w-full h-96 object-cover rounded-lg shadow-md"
                            onError={(e) => {
                                e.target.style.display = "none";
                            }}
                        />
                        <p className="text-center text-gray-500 italic mt-2">
                            {blog.title}
                        </p>
                    </div>
                )}

                <div
                    className="prose prose-lg max-w-none text-gray-800 leading-relaxed"
                    dangerouslySetInnerHTML={{ __html: blog.description }}
                />
            </div>

            <Footer />
        </div>
    );
};

export default BlogDetail;