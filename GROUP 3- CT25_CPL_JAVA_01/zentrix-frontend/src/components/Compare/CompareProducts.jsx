import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { Button } from "@/components/ui/button";
import Header from "../ui/Header";
import Footer from "../ui/Footer";
import { IoMdAdd } from "react-icons/io";
import { showNotification } from "../Dashboard/NotificationPopup";




const CompareProducts = () => {
  const [compareList, setCompareList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [allProducts, setAllProducts] = useState([]);
  const [productSearchQuery, setProductSearchQuery] = useState("");
  const [productSearchResults, setProductSearchResults] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const stored = localStorage.getItem("compareList");
    if (stored) {
      const list = JSON.parse(stored);
      const fetchData = async () => {
        const updatedList = await Promise.all(
          list.map(async (product) => {
            try {
              const [ratingRes, attrRes, productRes] = await Promise.all([
                axios.get(`http://localhost:6789/api/v1/reviews?prodType=${product.prodTypeId}`),
                axios.get(`http://localhost:6789/api/v1/products/productTypeAttribute/${product.prodTypeId}`),
                axios.get(`http://localhost:6789/api/v1/products/productTypes/${product.prodTypeId}`),
              ]);

              const productType = productRes.data;
              if (productType.status === 0) return null;

              const reviews = ratingRes.data?.content || [];
              const totalRating = reviews.reduce((sum, r) => sum + r.rating, 0);
              const averageRating = reviews.length ? totalRating / reviews.length : 0;

              return {
                ...product,
                rating: averageRating,
                attributes: attrRes.data || [],
                description: productType.description || "No description",
                status: productType.status,
              };
            } catch (err) {
              console.error("Error loading product data", err);
              return null;
            }
          })
        );

        const filteredList = updatedList.filter((item) => item && item.status !== 0);
        localStorage.setItem("compareList", JSON.stringify(filteredList));
        setCompareList(filteredList);
        setLoading(false);
      };
      fetchData();
    } else {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    fetchAllProductTypes();
  }, []);

  useEffect(() => {
    const query = productSearchQuery.toLowerCase().trim();
    if (!query) {
      setProductSearchResults([]);
      return;
    }
    const filtered = allProducts.filter((p) =>
      (p.prodTypeName || "").toLowerCase().includes(query)
    );
    setProductSearchResults(filtered.slice(0, 10));
  }, [productSearchQuery, allProducts]);

  const fetchAllProductTypes = async () => {
    try {
      const res = await axios.get("http://localhost:6789/api/v1/products/homepage", {
        params: { page: 0, size: 1000 },
        withCredentials: true,
      });
      const productTypes = res.data.content?.content || res.data.content || [];
      const enriched = await Promise.all(
        productTypes.map(async (p) => ({
          ...p,
          image: await fetchImageProductType(p.prodTypeId),
        }))
      );
      setAllProducts(enriched);
    } catch (err) {
      console.error("Failed to fetch product types", err);
    }
  };
  function formatVNDCustom(amount) {
    let value = parseFloat(amount);
     if (isNaN(value)) return "0 VNĐ";
   
     value = Math.round(value);
   
     const str = value.toString();
     let result = "";
   
     for (let i = 0; i < str.length; i++) {
    const indexFromRight = str.length - i;
      result = str.charAt(indexFromRight - 1) + result;
   
      if (i % 3 === 2 && i !== str.length - 1) {
         result = "." + result;
       }
    }
  
     return result + " VNĐ";
   }
  const fetchImageProductType = async (prodTypeId) => {
    try {
      const res = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, {
        withCredentials: true,
      });

      let url = Array.isArray(res.data) && res.data.length > 0
        ? res.data[0].imageId?.imageLink || res.data[0].imageLink
        : res.data.imageId?.imageLink || res.data.imageLink;

      if (url && !url.startsWith("http")) {
        url = `http://localhost:6789${url.startsWith("/") ? "" : "/"}${url}`;
      }

      return url || "/images/placeholder.jpg";
    } catch (err) {
      return "/images/placeholder.jpg";
    }
  };


  const handleSelectProductType = async (product) => {
    const alreadyInList = compareList.some(p => p.prodTypeId === product.prodTypeId);

    if (alreadyInList) return;

    if (compareList.length >= 5) {
      showNotification("Only compare up to 5 products", 3000, 'fail');
      return;
    }

    try {
      const [ratingRes, attrRes, productRes] = await Promise.all([
        axios.get(`http://localhost:6789/api/v1/reviews?prodType=${product.prodTypeId}`),
        axios.get(`http://localhost:6789/api/v1/products/productTypeAttribute/${product.prodTypeId}`),
        axios.get(`http://localhost:6789/api/v1/products/productTypes/${product.prodTypeId}`),
      ]);

      const productType = productRes.data;
      if (productType.status === 0) return;

      const reviews = ratingRes.data?.content || [];
      const totalRating = reviews.reduce((sum, r) => sum + r.rating, 0);
      const averageRating = reviews.length ? totalRating / reviews.length : 0;

      const newItem = {
        prodId: product.prodTypeId,
        prodTypeId: product.prodTypeId,
        name: product.prodTypeName,
        price: product.unitPrice || product.prodTypePrice,
        image: product.image,
        rating: averageRating,
        attributes: attrRes.data || [],
        description: productType.description || "No description",
        status: productType.status,
      };

      const updated = [...compareList, newItem];
      localStorage.setItem("compareList", JSON.stringify(updated));
      setCompareList(updated);
      setProductSearchQuery("");
      setProductSearchResults([]);
    } catch (err) {
      console.error("Error selecting product for compare:", err);
    }
  };



  const removeFromCompare = (prodId) => {
    const updated = compareList.filter((p) => p.prodId !== prodId);
    localStorage.setItem("compareList", JSON.stringify(updated));
    setCompareList(updated);
  };

  const handleClearAll = () => {
    localStorage.removeItem("compareList");
    setCompareList([]);
  };

  const getLowestPrice = () => {
    return Math.min(...compareList.map((item) => item.price || 999999999));
  };


  if (loading) return <div className="text-center py-16 text-gray-600 text-lg">Loading...</div>;

  return (
    <div className="min-h-screen bg-gray-50" style={{ backgroundImage: `url('${localStorage.getItem('urlWallpaper')}')` }}>
      <header className="h-20 bg-blue-700 text-white flex items-center px-6 shadow-lg">
        <Header />
      </header>
      <div className="container mx-auto px-6 py-12">
      <div className="flex justify-between items-center mb-8 bg-white p-4 rounded-xl shadow">
          <h2 className="text-3xl font-bold text-gray-800 flex items-center gap-2">
            📊 Compare Products
          </h2>
          {compareList.length > 0 && (
            <Button
              variant="outline"
              className="border-red-500 text-red-500 hover:bg-red-50 hover:text-red-600 transition-colors"
              onClick={handleClearAll}
            >
              Clear All
            </Button>
          )}
        </div>

        {compareList.length === 0 ? (
          <div className="flex justify-center items-center min-h-[500px] px-4">
            <div className="w-full max-w-sm rounded-xl overflow-hidden">
              {!productSearchQuery ? (
                <button
                  onClick={() => setProductSearchQuery(" ")}
                  className="w-full h-full min-h-[500px] bg-gradient-to-r from-blue-500 via-blue-600 to-blue-600 hover:from-blue-600 hover:via-blue-700 hover:to-blue-700 text-white flex items-center justify-center transition-all duration-300 rounded-xl shadow-2xl hover:shadow-[0_0_20px_5px_rgba(59,130,246,0.5)] active:scale-95 active:shadow-inner transform hover:scale-105 animate-pulse"
                >

                  <IoMdAdd size={72} />
                </button>
              ) : (
                <div className="p-6 space-y-4 bg-white">
                  <div className="relative">
                    <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                      <svg
                        className="w-5 h-5 text-gray-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth="2"
                          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                        ></path>
                      </svg>
                    </div>
                    <input
                      value={productSearchQuery}
                      onChange={(e) => setProductSearchQuery(e.target.value)}
                      placeholder="Search product type..."
                      className="w-full py-3 pl-10 pr-4 border border-gray-200 rounded-lg 
                        focus:outline-none focus:ring-2 focus:ring-blue-400 
                        transition duration-200 shadow-sm text-gray-700 placeholder-gray-400"
                    />
                  </div>
                  <div className="bg-white border rounded max-h-64 overflow-y-auto">
                    {productSearchResults.length > 0 && (
                      productSearchResults.map((p) => {
                        const isSelected = compareList.some(item => item.prodTypeId === p.prodTypeId);
                        return (
                          <div
                            key={p.prodTypeId}
                            onClick={() => !isSelected && handleSelectProductType(p)}
                            className={`flex items-center gap-3 p-3 cursor-pointer
                                     ${isSelected
                                ? "opacity-50 cursor-not-allowed"
                                : "hover:bg-gray-100 transition-colors duration-200"}`}
                          >
                            <img
                              src={p.image || "/images/placeholder.jpg"}
                              alt={p.prodTypeName}
                              className="w-10 h-10 object-cover rounded"
                            />
                            <div className="flex-1">
                              <p className="text-sm font-medium text-gray-800">{p.prodTypeName}</p>
                              <p className="text-sm text-red-500">
                               
                { formatVNDCustom(p.unitPrice || p.prodTypePrice)}
                              </p>
                              {isSelected && (
                                <p className="text-xs text-gray-500 italic">Added to list</p>
                              )}
                            </div>
                          </div>
                        );
                      })
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="overflow-x-auto bg-white rounded-xl shadow-md p-6">
            <table className="w-full text-sm border-separate border-spacing-0">
              <thead className="bg-gray-100">
                <tr>
                  {compareList.map((p) => {
                    const isBestPrice = p.price === getLowestPrice();
                    return (
                      <th
                        key={p.prodId}
                        className={`p-4 align-top border-b w-full sm:w-72 md:w-80 lg:w-96 xl:w-[300px] ${isBestPrice ? "bg-green-50 border-2 border-green-300" : ""}`}
                      >
                        <div className="space-y-3 min-h-[550px] flex flex-col justify-between">
                          <div>
                            <div className="relative">
                              <img
                                src={p.image}
                                alt={p.name}
                                className="h-32 w-full object-contain rounded-lg"
                              />
                              {isBestPrice && (
                                <span className="absolute top-0 right-0 bg-green-500 text-white text-xs font-bold px-2 py-1 rounded-bl-lg rounded-tr-lg">
                                  Best Price
                                </span>
                              )}
                            </div>
                            <div className="text-lg font-semibold text-gray-800">{p.name}</div>
                            <div
                              className={`text-lg ${isBestPrice ? "text-green-600 font-bold bg-green-100 px-2 py-1 rounded inline-block" : "text-gray-700"}`}
                            >
                              { formatVNDCustom(p.price)}
                            </div>
                            <div className="text-yellow-500 flex items-center justify-center gap-1">
                              ⭐ <span className="text-gray-700">{p.rating?.toFixed(1)}</span>
                            </div>
                            <div className="bg-gray-50 p-4 rounded-lg mt-4 text-left">
                              <h4 className="text-sm font-medium text-gray-700">Specifications</h4>
                              <table className="w-full text-sm mt-2">
                                <tbody>
                                  {p.attributes?.map((attr, index) => (
                                    <tr key={index} className="border-t border-gray-200">
                                      <td className="font-medium text-gray-600">{attr.atbId?.atbName || "Attribute"}</td>
                                      <td className="text-gray-600">{attr.prodAtbValue || "N/A"}</td>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                            </div>
                          </div>
                          <div className="flex flex-col gap-2">
                            <Button
                              onClick={() => navigate(`/product/${p.prodTypeId}`)}
                              className={`${isBestPrice ? "bg-green-600 hover:bg-green-700" : "bg-blue-600 hover:bg-blue-700"} text-white py-1 rounded-lg transition-colors`}
                            >
                              Detail
                            </Button>
                            <Button
                              variant="ghost"
                              className="text-red-500 hover:text-red-600 py-1 rounded-lg transition-colors"
                              onClick={() => removeFromCompare(p.prodId)}
                            >
                              Remove
                            </Button>
                          </div>
                        </div>
                      </th>
                    );
                  })}
                  {compareList.length < 5 && (
                    <th className="p-4 align-top border-b w-full sm:w-72 md:w-80 lg:w-96 xl:w-[300px]">
                      <div className="w-full rounded-xl border-2 border-gray-300 shadow-md min-h-[550px] flex flex-col justify-between">
                        {!productSearchQuery ? (
                          <button
                            onClick={() => setProductSearchQuery(" ")}
                            className="w-full h-full min-h-[550px] bg-gradient-to-r from-blue-500 via-blue-600 to-blue-600 hover:from-blue-600 hover:via-blue-700 hover:to-blue-700 text-white flex items-center justify-center transition-all duration-300 rounded-xl shadow-2xl hover:shadow-[0_0_20px_5px_rgba(59,130,246,0.5)] active:scale-95 active:shadow-inner transform hover:scale-105"
                          >

                            <IoMdAdd size={72} />
                          </button>
                        ) : (
                          <div className="p-4 flex flex-col justify-start space-y-3">
                            <input
                              autoFocus
                              value={productSearchQuery}
                              onChange={(e) => setProductSearchQuery(e.target.value)}
                              placeholder="Search product type..."
                              className="w-full px-4 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-400"
                            />
                            <div className="bg-white border rounded shadow max-h-64 overflow-y-auto">
                              {productSearchResults.length > 0 &&
                                productSearchResults.map((p) => {
                                  const isSelected = compareList.some((item) => item.prodTypeId === p.prodTypeId);
                                  return (
                                    <div
                                      key={p.prodTypeId}
                                      onClick={() => !isSelected && handleSelectProductType(p)}
                                      className={`flex items-center gap-3 p-3 cursor-pointer 
                                                ${isSelected ? "opacity-50 cursor-not-allowed" : "hover:bg-gray-100"}`}
                                    >
                                      <img
                                        src={p.image || "/images/placeholder.jpg"}
                                        alt={p.prodTypeName}
                                        className="w-10 h-10 rounded object-cover"
                                      />
                                      <div>
                                        <p className="text-sm font-medium text-gray-800">{p.prodTypeName}</p>
                                        <p className="text-sm text-red-500">
                                          
{ formatVNDCustom(p.unitPrice || p.prodTypePrice)}
                                        </p>
                                        {isSelected && (
                                          <p className="text-xs text-gray-500 italic">Added to list</p>
                                        )}
                                      </div>
                                    </div>
                                  );
                                })}
                            </div>
                          </div>
                        )}
                      </div>
                    </th>
                  )}
                </tr>
              </thead>
            </table>
          </div>
        )}
      </div>

      <Footer />
    </div>
  );
};

export default CompareProducts;