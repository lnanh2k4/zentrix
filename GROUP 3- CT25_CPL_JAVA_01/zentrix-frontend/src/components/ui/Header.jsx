import { ShoppingCart, User, Search, ChevronDown, Bell } from "lucide-react";
import { useState, useEffect, useRef } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { FaImage } from "react-icons/fa";
import { useNavigate } from "react-router-dom";
import {
  NavigationMenu,
  NavigationMenuContent,
  NavigationMenuItem,
  NavigationMenuList,
  NavigationMenuTrigger,
} from "@/components/ui/navigation-menu";
import { Button } from "@/components/ui/button";
import axios from "axios";

const Header = () => {
  const [notificationList, setNotificationList] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const [isShowCarts, setIsShowCarts] = useState(false);
  const [activeTab, setActiveTab] = useState("collect");
  const [promotions, setPromotions] = useState([]);
  const [userPromotions, setUserPromotions] = useState([]);
  const [allProducts, setAllProducts] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [productSearchResults, setProductSearchResults] = useState([]);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();
  const [isShowBranches, setIsShowBranches] = useState(false);
  const [branches, setBranches] = useState([]);
  const [cartItems, setCartItems] = useState([]);
  const [cartId, setCartId] = useState(null);
  const [branchSearchQuery, setBranchSearchQuery] = useState("");
  const [isLoadingBranches, setIsLoadingBranches] = useState(false);
  const [isShowPromotions, setIsShowPromotions] = useState(false);
  const [showLoginModal, setShowLoginModal] = useState(false);
  const promotionsRef = useRef(null);
  const API_URL = "http://localhost:6789/api/v1/cart";
  const [selectedBranch, setSelectedBranch] = useState(null);

  const [urlWallpaper, setUrlWallpaper] = useState(() => localStorage.getItem("urlWallpaper") || "");
  const [isShowSettings, setIsShowSettings] = useState(false);
  const isOrderPage = window.location.pathname === "/orderPage";
  const availableWallpapers = [
    "/wallpaper_background.jpg",
    "/wallpaper_background_1.jpg",
    "/wallpaper_background_2.jpg",
    "/wallpaper_background_3.jpg",
  ];

  const handleWallpaperChange = async (e) => {
    localStorage.setItem("urlWallpaper", e.target.value);
    setUrlWallpaper(e.target.value);
    window.location.reload();
  };

  const fetchImageProductType = async (prodTypeId) => {
    try {
      const response = await axios.get(`http://localhost:6789/api/v1/products/ImageProduct/${prodTypeId}`, {
        withCredentials: true,
      });
      if (Array.isArray(response.data) && response.data.length > 0) {
        const firstImage = response.data[0];
        let url = firstImage.imageId?.imageLink || firstImage.imageLink || firstImage || "/images/placeholder.jpg";
        if (url && !url.startsWith("http")) {
          url = `http://localhost:6789${url.startsWith("/") ? "" : "/"}${url}`;
        }
        return url;
      } else if (!Array.isArray(response.data) && response.data) {
        let url = response.data.imageId?.imageLink || response.data.imageLink || response.data || "/images/placeholder.jpg";
        if (url && !url.startsWith("http")) {
          url = `http://localhost:6789${url.startsWith("/") ? "" : "/"}${url}`;
        }
        return url;
      }
      return "/images/placeholder.jpg";
    } catch (error) {
      console.error("Error fetching product images:", error);
      return "/images/placeholder.jpg";
    }
  };

  const fetchBranchQuantity = async (prodTypeId, brchId) => {
    try {
      const response = await axios.get(`http://localhost:6789/api/v1/products/productTypeBranch/${prodTypeId}`, {
        withCredentials: true,
      });
      const branchData = response.data.find((item) => item.brchId?.brchId === Number(brchId));
      return branchData ? branchData.quantity : 0; // Mặc định là 0 nếu không có dữ liệu
    } catch (error) {
      console.error(`Error fetching branch quantity for prodTypeId ${prodTypeId}:`, error);
      return 0; // Mặc định là 0 thay vì null
    }
  };

  const fetchAllProductTypes = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await axios.get("http://localhost:6789/api/v1/products/homepage", {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        withCredentials: true,
      });
      const productTypes = Array.isArray(response.data.content) ? response.data.content : [];
      console.log("Fetched product types in Header:", productTypes);

      const activeProductTypes = productTypes.filter((productType) => productType.status === 1);

      const enrichedProductTypes = await Promise.all(
        activeProductTypes.map(async (productType) => {
          const image = await fetchImageProductType(productType.prodTypeId);
          const quantity = await fetchBranchQuantity(productType.prodTypeId, selectedBranch?.brchId || 5);
          const categoryId = productType.prodId?.cateId?.cateId || null; // Đảm bảo categoryId luôn có giá trị
          return {
            prodId: productType.prodTypeId || "unknown-id",
            prodName: productType.prodTypeName || "Unknown Product",
            prodTypePrice: productType.prodTypePrice || 0,
            unitPrice: productType.unitPrice || productType.prodTypePrice || 0,
            image: image,
            categoryId: categoryId, // Sửa ở đây
            brand: (productType.prodTypeName || "Unknown").split(" ")[0] || "Unknown",
            quantity: quantity, // Không lọc null ở đây
          };
        })
      );

      // Không lọc sản phẩm dựa trên quantity ở đây, để tất cả sản phẩm đều được hiển thị trong tìm kiếm
      setAllProducts(enrichedProductTypes);
    } catch (error) {
      console.error("Error fetching product types in Header:", error.response?.data || error);
      setAllProducts([]);
      if (error.response?.status === 401) {
        console.warn("User not authenticated, but search will still work with available data.");
      }
    }
  };

  const handleSearchProductType = (query) => {
    setSearchQuery(query);
    if (!query.trim()) {
      setProductSearchResults([]);
      return;
    }
    const filteredProducts = allProducts.filter((product) =>
      product.prodName.toLowerCase().includes(query.toLowerCase())
    );
    setProductSearchResults(filteredProducts.slice(0, 10));
  };

  const handleSelectProduct = (product) => {
    localStorage.setItem("selectedProdTypeId", product.prodId);
    navigate(`/product/${encodeURIComponent(product.prodId)}`);
    setSearchQuery("");
    setProductSearchResults([]);
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      currencyDisplay: "code",
    }).format(price);
  };

  const fetchCartItems = async (userId, page = 0, size = 10) => {
    try {
      const response = await axios.get(API_URL, {
        params: { userId, page, size },
        withCredentials: true,
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching cart items:", error);
      throw error;
    }
  };

  const fetchCartItemsDetails = async (cartId, userId) => {
    try {
      const response = await axios.get(`${API_URL}/items`, {
        params: { userId, cartId },
        withCredentials: true,
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching cart items details:", error);
      throw error;
    }
  };

  const fetchUserInfo = async () => {
    try {
      const response = await axios.get("http://localhost:6789/api/v1/auth/info", {
        withCredentials: true,
      });
      if (response.data.success) {
        setUser(response.data.content);
        return response.data.content.userId;
      } else {
        setUser(null);
        return null;
      }
    } catch (error) {
      console.error("Error fetching user info:", error.response?.data || error.message);
      setUser(null);
      return null;
    }
  };

  const loadCart = async () => {
    try {
      const fetchedUserId = await fetchUserInfo();
      if (!fetchedUserId) {
        setCartItems([]);
        setCartId(null);
        return;
      }

      const cartResponse = await fetchCartItems(fetchedUserId);
      if (!cartResponse.success || !cartResponse.content || cartResponse.content.length === 0) {
        setCartItems([]);
        setCartId(null);
      } else {
        const firstCart = cartResponse.content[0];
        const cartId = firstCart.cartId;
        setCartId(cartId);

        const itemsResponse = await fetchCartItemsDetails(cartId, fetchedUserId);
        console.log("debug for add to cart items: ", itemsResponse);
        if (!itemsResponse.success || !itemsResponse.content) {
          setCartItems([]);
        } else {
          const items = await Promise.all(
            itemsResponse.content.map(async (item) => {
              const imageUrl = item.prodTypeVariId?.prodTypeId?.prodTypeId
                ? await fetchImageProductType(item.prodTypeVariId.prodTypeId.prodTypeId)
                : "";
              return {
                id: item.cartProductTypeVariationId,
                cartId: item.cartId.cartId,
                name: item.prodTypeVariId?.prodTypeId?.prodTypeName || "Unnamed Product",
                originalPrice: item.prodTypeVariId?.prodTypeId?.prodTypePrice || 4390000,
                salePrice:
                  item.prodTypeVariId?.prodTypeId?.unitPrice ||
                  item.prodTypeVariId?.prodTypeId?.prodTypePrice ||
                  3290000,
                quantity: item.quantity,
                variation: item.prodTypeVariId || null,
                vat: item.prodTypeVariId?.prodTypeId?.prodId?.vat || 0,
                prodTypeId: item.prodTypeVariId?.prodTypeId?.prodTypeId,
                prodTypeVariId: item.prodTypeVariId?.prodTypeVariId,
                urlImage: imageUrl,
                createdAt: item.createdAt || new Date().toISOString(),
                variCode: item.variCode, // Thêm variCode vào object
              };
            })
          );

          const groupedItems = {};

          // Nhóm các sản phẩm theo variCode
          items.forEach((item) => {
            const variCode = item.variCode;

            if (!groupedItems[variCode]) {
              // Nếu chưa có nhóm cho variCode này, tạo mới
              groupedItems[variCode] = {
                key: variCode,
                name: item.name,
                originalPrice: item.originalPrice,
                salePrice: item.salePrice,
                urlImage: item.urlImage,
                vat: item.vat,
                quantity: item.quantity, // Lấy quantity từ bản ghi đầu tiên
                prodTypeId: item.prodTypeId,
                cartProductTypeVariationIds: [item.id],
                variations: [
                  {
                    id: item.id,
                    variationName: item.variation?.variId?.variName || "Unknown",
                    variationValue: item.variation?.prodTypeValue || "Unknown",
                    quantity: item.quantity,
                    cartProductTypeVariationId: item.id,
                    prodTypeVariId: item.prodTypeVariId,
                  },
                ],
                createdAt: item.createdAt,
              };
            } else {
              // Nếu đã có nhóm, thêm variation vào danh sách
              groupedItems[variCode].cartProductTypeVariationIds.push(item.id);
              groupedItems[variCode].variations.push({
                id: item.id,
                variationName: item.variation?.variId?.variName || "Unknown",
                variationValue: item.variation?.prodTypeValue || "Unknown",
                quantity: item.quantity,
                cartProductTypeVariationId: item.id,
                prodTypeVariId: item.prodTypeVariId,
              });
            }
          });

          // Sắp xếp variations theo variationName nếu cần
          Object.values(groupedItems).forEach((group) => {
            group.variations.sort((a, b) => a.variationName.localeCompare(b.variationName));
          });

          const uniqueItems = Object.values(groupedItems);
          setCartItems(uniqueItems);
        }
      }
    } catch (error) {
      console.error("Failed to load cart:", error);
      setCartItems([]);
    }
  };

  const fetchUserPromotions = async (userId) => {
    if (!userId) {
      setUserPromotions([]);
      return;
    }
    try {
      const response = await axios.get("http://localhost:6789/api/v1/promotions/my-promotions", {
        params: { userId },
        withCredentials: true,
      });
      if (response.data.success) {
        const userPromoData = response.data.content.map((userPromo) => ({
          userPromId: userPromo.userPromId,
          promId: userPromo.promId.promId,
          status: userPromo.status,
        }));
        await fetchPromotionsByIds(userPromoData);
      } else {
        setUserPromotions([]);
        console.log("No promotions found for user:", userId);
      }
    } catch (error) {
      if (error.response?.status === 400 && error.response?.data?.message.includes("Promotion not found")) {
        setUserPromotions([]);
        console.log("No promotions available for user:", userId);
      } else {
        console.error("Error fetching user promotions:", error.response?.data || error.message);
        setUserPromotions([]);
      }
    }
  };

  const fetchPromotionsByIds = async (userPromoData) => {
    if (!userPromoData.length) {
      setUserPromotions([]);
      return;
    }
    try {
      const promoPromises = userPromoData.map((userPromo) =>
        axios.get(`http://localhost:6789/api/v1/promotions/${userPromo.promId}`, {
          withCredentials: true,
        })
      );
      const responses = await Promise.all(promoPromises);
      const promotionData = responses
        .filter((response) => response.data.success)
        .map((response, index) => ({
          userPromId: userPromoData[index].userPromId,
          promId: response.data.content.promId,
          promotionName: response.data.content.promName,
          discount: response.data.content.discount,
          expiryDate: response.data.content.endDate,
          quantity: response.data.content.quantity,
          promCode: response.data.content.promCode,
          promStatus: response.data.content.promStatus,
          status: userPromoData[index].status,
        }));
      setUserPromotions(promotionData);
    } catch (error) {
      console.error("Error fetching promotions by IDs:", error.response?.data || error.message);
      setUserPromotions([]);
    }
  };

  const fetchBranches = async () => {
    setIsLoadingBranches(true);
    try {
      const response = await axios.get("http://localhost:6789/api/v1/branches", {
        params: { page: 0, size: 100 },
        withCredentials: true,
      });
      if (response.data.success) {
        setBranches(response.data.content || []);
      } else {
        setBranches([]);
      }
    } catch (error) {
      console.error("Error fetching branches:", error);
      setBranches([]);
    } finally {
      setIsLoadingBranches(false);
    }
  };

  const searchBranches = async (name) => {
    if (!name.trim()) {
      fetchBranches();
      return;
    }
    setIsLoadingBranches(true);
    try {
      const response = await axios.get(`http://localhost:6789/api/v1/branches/search/${encodeURIComponent(name)}`, {
        withCredentials: true,
      });
      if (response.data.success) {
        const branchesData = Array.isArray(response.data.content) ? response.data.content : [response.data.content];
        setBranches(branchesData || []);
      } else {
        setBranches([]);
      }
    } catch (error) {
      console.error("Error searching branches:", error);
      setBranches([]);
    } finally {
      setIsLoadingBranches(false);
    }
  };

  const fetchAllActivePromotions = async () => {
    try {
      const pageSize = 10;
      let allPromotions = [];
      let currentPage = 0;
      let totalPages = 1;

      while (currentPage < totalPages) {
        const response = await axios.get("http://localhost:6789/api/v1/promotions/filter", {
          params: { status: "active", page: currentPage, size: pageSize, sort: "promId,asc" },
          withCredentials: true,
        });

        if (response.data.success) {
          const filteredPromotions = (response.data.content || []).filter(
            (promo) => promo.approvedBy !== null
          );
          allPromotions = [...allPromotions, ...filteredPromotions];
          if (currentPage === 0) totalPages = response.data.pagination?.totalPages || 1;
        }
        currentPage += 1;
      }
      setPromotions(allPromotions);
    } catch (error) {
      console.error("Error fetching active promotions:", error);
      setPromotions([]);
    }
  };

  useEffect(() => {
    const initializeData = async () => {
      await fetchAllProductTypes();

      const userId = await fetchUserInfo();
      if (userId) {
        await fetchUserPromotions(userId);
        await loadCart();
      }
      await fetchAllActivePromotions();

      try {
        const response = await axios.get("http://localhost:6789/api/v1/branches", {
          params: { page: 0, size: 100 },
          withCredentials: true,
        });
        if (response.data.success) {
          const allBranches = response.data.content || [];
          const activeBranches = allBranches
            .filter((branch) => branch.status === 1)
            .sort((a, b) => a.brchId - b.brchId);

          setBranches(activeBranches);

          const savedBranchId = localStorage.getItem("selectedBranchId");
          const savedBranch = activeBranches.find(
            (branch) => branch.brchId === Number(savedBranchId)
          );

          if (savedBranch) {
            setSelectedBranch({
              brchId: savedBranch.brchId,
              brchName: savedBranch.brchName,
              address: savedBranch.address || "Unknown Address",
            });
          } else if (activeBranches.length > 0) {
            const defaultBranch = activeBranches[0];
            setSelectedBranch({
              brchId: defaultBranch.brchId,
              brchName: defaultBranch.brchName,
              address: defaultBranch.address || "Unknown Address",
            });
            localStorage.setItem("selectedBranchId", defaultBranch.brchId);
            localStorage.setItem("selectedBranchName", defaultBranch.brchName);
            localStorage.setItem("selectedBranchAddress", defaultBranch.address || "Unknown Address");
          }
        }
      } catch (error) {
        console.error("Error fetching branches:", error);
      }

      const currentPath = window.location.pathname;
      const savedCateId = localStorage.getItem("selectedCateId");

      if (currentPath === "/" && !savedCateId && selectedBranch) {
        navigate(`/products/branch/${selectedBranch.brchId}`);
      }
    };
    initializeData();
    const handleCartUpdate = () => {
      loadCart();
    };
    window.addEventListener("cartUpdated", handleCartUpdate);

    // Cleanup event listener on component unmount
    return () => {
      window.removeEventListener("cartUpdated", handleCartUpdate);
    };
  }, []);

  const handleClaim = async (promotion) => {
    if (!user) {
      alert("Please sign in to claim a promotion!");
      navigate("/signin");
      return;
    }
    if (promotion.quantity <= 0) {
      alert("This promotion is out of stock!");
      return;
    }
    if (isPromotionClaimed(promotion.promId)) {
      alert("You have already claimed this promotion!");
      return;
    }
    try {
      const response = await axios.post(
        `http://localhost:6789/api/v1/promotions/claim?promId=${promotion.promId}&userId=${user.userId}`,
        null,
        { withCredentials: true }
      );
      if (response.data.success) {
        await fetchAllActivePromotions();
        await fetchUserPromotions(user.userId);
      }
    } catch (error) {
      console.error("Error claiming promotion:", error);
      alert("Failed to claim promotion: " + (error.response?.data?.message || error.message));
    }
  };

  const isPromotionClaimed = (promId) => {
    return userPromotions.some((promo) => promo.promId === promId);
  };

  const handleTabChange = async (tab) => {
    setActiveTab(tab);
    if (tab === "collect") {
      await fetchAllActivePromotions();
    } else if (tab === "my" && user) {
      await fetchUserPromotions(user.userId);
    }
  };

  const goToHomePage = () => navigate("/");

  const handleUserClick = () => {
    if (!user) {
      navigate("/login");
    } else {
      navigate("/home");
    }
  };

  const getFullName = () => {
    return user ? `${user.firstName}`.trim() : "Login";
  };

  const handlePromotionsClick = () => {
    if (!user) {
      setShowLoginModal(true);
    } else {
      setIsShowPromotions(!isShowPromotions);
    }
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (promotionsRef.current && !promotionsRef.current.contains(event.target)) {
        setIsShowPromotions(false);
      }
    };

    if (isShowPromotions) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isShowPromotions]);

  const fetchNotifications = async () => {
    try {
      const userId = await fetchUserInfo();
      if (userId) {
        const response = await axios.get(`http://localhost:6789/api/v1/notifications/get-all/${userId}`, {
          withCredentials: true,
        });
        if (response.data.data) {
          setNotificationList(response.data.data);
        }
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
    }
  };

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(() => {
      fetchNotifications();
    }, 5000);

    return () => clearInterval(interval);
  }, []);

  const handleNotificationClick = () => {
    setShowNotifications(!showNotifications);
  };

  const handleSelectBranch = (branch) => {
    setSelectedBranch({
      brchId: branch.brchId,
      brchName: branch.brchName,
      address: branch.address || "Unknown Address",
    });
    setIsShowBranches(false);
    localStorage.setItem("selectedBranchId", branch.brchId);
    localStorage.setItem("selectedBranchName", branch.brchName);
    localStorage.setItem("selectedBranchAddress", branch.address || "Unknown Address");
    window.location.reload();
  };

  return (
    <header className="shadow-lg fixed top-0 left-0 right-0 z-50 bg-[#0044cc] text-white py-4 backdrop-blur-lg bg-opacity-90">
      <div className="container mx-auto flex items-center justify-between px-8 space-x-6">
        <img
          src="/logo_zentrix.png"
          alt="Zentrix Logo"
          className="h-12 w-auto object-contain drop-shadow-[0_8px_12px_rgba(0,0,0,0.5)] cursor-pointer"
          style={{ filter: "drop-shadow(0px 10px 10px rgba(0,0,0,0.8))" }}
          onClick={goToHomePage}
        />
        <p
          className="text-white text-xl font-semibold tracking-wide drop-shadow-md cursor-pointer"
          onClick={goToHomePage}
        >
          Zentrix Store
        </p>

        <div className="flex-1 mx-6 max-w-lg relative">
          <motion.input
            whileFocus={{ scale: 1.05 }}
            className="bg-white w-full px-5 py-3 rounded-full text-gray-800 placeholder-gray-500 outline-none shadow-lg focus:ring-2 focus:ring-[#A0BFE0] transition"
            placeholder="Search something"
            value={searchQuery}
            onChange={(e) => handleSearchProductType(e.target.value)}
          />
          <Search className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400" size={24} />

          <AnimatePresence>
            {searchQuery && productSearchResults.length > 0 && (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 10 }}
                className="absolute left-0 mt-2 w-full bg-white shadow-lg rounded-lg p-3 z-50 text-black max-h-96 overflow-y-auto"
              >
                {productSearchResults.map((product) => (
                  <div
                    key={product.prodId}
                    className="flex items-start p-2 hover:bg-gray-100 rounded cursor-pointer"
                    onClick={() => handleSelectProduct(product)}
                  >
                    <img
                      src={product.image}
                      alt={product.prodName}
                      className="w-12 h-12 object-cover rounded mr-3"
                      onError={(e) => (e.target.src = "/images/placeholder.jpg")}
                    />
                    <div className="flex flex-col">
                      <p className="text-sm font-medium">{product.prodName}</p>
                      <div className="flex items-center mt-1">
                        <p className="text-sm font-medium text-red-500">
                          {product.unitPrice ? formatPrice(product.unitPrice) : "N/A"}
                        </p>
                        {product.unitPrice !== product.prodTypePrice && (
                          <p className="text-xs text-gray-400 line-through ml-2">
                            {product.prodTypePrice ? formatPrice(product.prodTypePrice) : "N/A"}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        <nav className="flex items-center space-x-8 text-lg font-semibold">
          <div className="relative" ref={promotionsRef}>
            <div
              className="text-white hover:text-blue-500 bg-transparent text-xl cursor-pointer"
              onClick={handlePromotionsClick}
            >
              Promotions
            </div>
            <AnimatePresence>
              {isShowPromotions && user && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 10 }}
                  className="absolute left-0 mt-2 w-[400px] bg-white shadow-lg rounded-md z-50 text-black"
                >
                  <div className="p-4">
                    <div className="flex space-x-4 border-b mb-4">
                      <button
                        className={`py-2 px-4 ${activeTab === "collect" ? "border-b-2 border-blue-600 text-blue-600 font-semibold" : "text-gray-600 hover:text-blue-600"}`}
                        onClick={() => handleTabChange("collect")}
                      >
                        Collect Promotions
                      </button>
                      <button
                        className={`py-2 px-4 ${activeTab === "my" ? "border-b-2 border-blue-600 text-blue-600 font-semibold" : "text-gray-600 hover:text-blue-600"}`}
                        onClick={() => handleTabChange("my")}
                      >
                        My Promotions
                      </button>
                    </div>
                    <div className="space-y-4 max-h-[300px] overflow-y-auto" key={activeTab}>
                      {activeTab === "collect" && (
                        (() => {
                          const availablePromotions = promotions.filter(
                            (promotion) => !isPromotionClaimed(promotion.promId) && promotion.approvedBy !== null
                          );
                          return availablePromotions.length > 0 ? (
                            availablePromotions.map((promotion) => {
                              const isClaimed = isPromotionClaimed(promotion.promId);
                              return (
                                <div
                                  key={promotion.promId}
                                  className="flex justify-between items-center p-3 border-b hover:bg-gray-100 rounded"
                                >
                                  <div>
                                    <span className="font-medium">{promotion.promName}</span>
                                    <p className="text-sm text-gray-500">Code: {promotion.promCode}</p>
                                    <p className="text-sm text-gray-500">Discount: {promotion.discount}%</p>
                                    <p className="text-sm text-gray-500">Quantity: {promotion.quantity}</p>
                                  </div>
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    className={`${isClaimed || promotion.promStatus !== 1 || promotion.quantity <= 0 ? "opacity-50 cursor-not-allowed border-gray-400 text-gray-400" : "text-blue-600 border-blue-600 hover:bg-blue-100"}`}
                                    disabled={isClaimed || promotion.promStatus !== 1 || promotion.quantity <= 0}
                                    onClick={() => handleClaim(promotion)}
                                  >
                                    {isClaimed ? "Claimed" : "Claim"}
                                  </Button>
                                </div>
                              );
                            })
                          ) : (
                            <p className="text-gray-500">New promotions are coming soon – don’t miss out!.</p>
                          );
                        })()
                      )}
                      {activeTab === "my" && (
                        (() => {
                          const activePromotions = userPromotions.filter((promotion) => promotion.status === 1);
                          return activePromotions.length > 0 ? (
                            activePromotions.map((promotion) => (
                              <div
                                key={promotion.promId}
                                className="flex justify-between items-center p-3 border-b bg-gray-50 rounded"
                              >
                                <div>
                                  <span className="font-medium">{promotion.promotionName}</span>
                                  <p className="text-sm text-gray-500">Discount: {promotion.discount}%</p>
                                  <p className="text-sm text-gray-500">
                                    Expires: {new Date(promotion.expiryDate).toLocaleDateString()}
                                  </p>
                                </div>
                              </div>
                            ))
                          ) : (
                            <p className="text-gray-500">You haven’t claimed any promotions yet.</p>
                          );
                        })()
                      )}
                    </div>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div className="relative">
            <div
              className={`flex items-center gap-2 ${isOrderPage ? "opacity-50 cursor-not-allowed" : "cursor-pointer hover:text-blue-500"} transition-colors duration-200 text-xl`}
              onClick={() => {
                if (isOrderPage) return;
                setIsShowBranches(!isShowBranches);
                if (!isShowBranches) fetchBranches();
              }}
            >
              {selectedBranch ? selectedBranch.brchName : "Loading..."} <ChevronDown size={24} />
            </div>
            <AnimatePresence>
              {isShowBranches && !isOrderPage && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 10 }}
                  className="absolute left-0 mt-2 w-64 bg-white shadow-lg rounded-md z-50 text-black"
                >
                  <div className="p-2">
                    <div className="relative">
                      <input
                        type="text"
                        placeholder="Search for a branch..."
                        value={branchSearchQuery}
                        onChange={(e) => {
                          setBranchSearchQuery(e.target.value);
                          searchBranches(e.target.value);
                        }}
                        className="w-full px-3 py-1 border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 text-gray-700 placeholder-gray-400 text-sm"
                      />
                      <Search className="absolute right-2 top-1/2 transform -translate-y-1/2 text-gray-400" size={16} />
                    </div>
                  </div>
                  <ul className="p-2 space-y-1 max-h-64 overflow-y-auto">
                    {isLoadingBranches ? (
                      <li className="text-center text-gray-500 text-sm py-2">Loading branches...</li>
                    ) : branches.length > 0 ? (
                      branches.map((branch) => (
                        <li
                          key={branch.brchId}
                          className={`p-2 rounded-md cursor-pointer transition-all duration-200 text-sm ${selectedBranch && selectedBranch.brchId === branch.brchId ? "bg-blue-100 text-blue-700 font-semibold" : "hover:bg-gray-100 text-gray-700"}`}
                          onClick={() => handleSelectBranch(branch)}
                        >
                          <div className="flex justify-between items-center">
                            <span>{branch.brchName || "Unnamed Branch"}</span>
                            {selectedBranch && selectedBranch.brchId === branch.brchId && (
                              <span className="text-blue-600">✔</span>
                            )}
                          </div>
                          <p className="text-xs text-gray-500">{branch.address || "No address"}</p>
                        </li>
                      ))
                    ) : (
                      <li className="text-center text-gray-500 text-sm py-2">No branches found</li>
                    )}
                  </ul>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div
            className="text-white hover:text-blue-500 bg-transparent cursor-pointer text-xl"
            onClick={() => navigate("/about")}
          >
            About Us
          </div>

          <div
            className="relative text-1xl"
            onMouseEnter={() => setIsShowCarts(true)}
            onMouseLeave={() => setIsShowCarts(false)}
          >
            <ShoppingCart size={35} className="cursor-pointer text-white hover:text-blue-500 transition-colors duration-200" />
            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-medium w-5 h-5 flex items-center justify-center rounded-full">
              {cartItems.length}
            </span>
            <AnimatePresence>
              {isShowCarts && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 10 }}
                  className="absolute right-0 mt-2 w-72 bg-white shadow-lg rounded-lg p-3 z-50 text-black"
                >
                  <h3 className="font-bold border-b pb-2 opacity-50">Shopping Cart</h3>
                  {cartItems.length > 0 ? (
                    <ul className="mt-2 space-y-2 max-h-64 overflow-y-auto">
                      {cartItems.map((item) => (
                        <li key={item.key} className="flex items-center gap-3 p-2 border-b">
                          <img
                            src={item.urlImage}
                            alt={item.name}
                            className="w-12 h-12 object-cover rounded"
                            onError={(e) => (e.target.src = "/images/placeholder.jpg")}
                          />
                          <div className="flex-1">
                            <p className="text-sm font-semibold">{item.name}</p>
                            <p className="text-xs text-gray-500">
                              {formatPrice(item.salePrice)} x {item.quantity}
                            </p>
                            {item.variations && item.variations.length > 0 && (
                              <p className="text-xs text-gray-400">
                                {item.variations.map((v) => `${v.variationName}: ${v.variationValue}`).join(", ")}
                              </p>
                            )}
                          </div>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="text-sm text-gray-500 mt-2">
                      {user ? "Your cart is empty." : "Please login to view cart."}
                    </p>
                  )}
                  {user && (
                    <button
                      onClick={() => navigate("/cart")}
                      className="block text-center w-full mt-3 bg-blue-500 text-white py-2 rounded hover:bg-blue-600 transition"
                    >
                      View Cart
                    </button>
                  )}
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div
            className="relative group"
            onMouseEnter={() => setShowNotifications(true)}
            onMouseLeave={() => setShowNotifications(false)}
          >
            <Bell
              size={35}
              className="cursor-pointer text-white hover:text-blue-500 transition-colors duration-200"
              onClick={handleNotificationClick}
            />
            <span
              className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-medium w-5 h-5 flex items-center justify-center rounded-full"
            >
              {notificationList.filter((item) => item.status !== 0).length}
            </span>
            <AnimatePresence>
              {showNotifications && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 10 }}
                  className="absolute right-0 mt-3 w-80 bg-white shadow-xl rounded-xl p-4 z-50 text-black border border-gray-100 overflow-hidden backdrop-blur-sm bg-opacity-90 transform-gpu will-change-transform"
                >
                  <h3 className="font-semibold text-gray-800 border-b border-gray-200 pb-2 text-[1rem] tracking-wide">
                    Notifications
                  </h3>
                  <ul className="mt-2 max-h-96 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100">
                    {notificationList.length > 0 ? (
                      notificationList
                        .filter((item) => item.status !== 0)
                        .map((note, index) => (
                          <li
                            key={index}
                            className="py-2 px-3 rounded-lg cursor-pointer transition-all duration-150 ease-in-out border-b border-gray-100 last:border-b-0 group/item"
                          >
                            <span className="text-gray-700 text-sm">{note.title}</span>
                          </li>
                        ))
                    ) : (
                      <p className="text-center text-gray-400 py-4 text-sm font-medium">No notifications</p>
                    )}
                  </ul>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div
            className="relative group transition duration-300 hover:text-blue-500 cursor-pointer flex items-center gap-2"
            onClick={handleUserClick}
          >
            <User size={35} />
            <span className="text-sm font-semibold">{getFullName()}</span>
          </div>
          <div className="relative flex items-center">
            <FaImage className="mr-2 text-white-500" />
            <ChevronDown
              size={24}
              className="cursor-pointer hover:text-blue-500 transition-colors"
              onClick={() => setIsShowSettings(!isShowSettings)}
            />
            <AnimatePresence>
              {isShowSettings && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 10 }}
                  className="absolute right-0 top-full mt-2 w-64 bg-white shadow-lg rounded-md p-4 z-50 text-black"
                >
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Wallpaper</label>
                    <select
                      value={urlWallpaper}
                      onChange={handleWallpaperChange}
                      className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                    >
                      <option value="">No Background</option>
                      {availableWallpapers.map((wallpaper, index) => (
                        <option key={index} value={wallpaper}>
                          Wallpaper {index + 1}
                        </option>
                      ))}
                    </select>
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>
        </nav>
      </div>

      <AnimatePresence>
        {showLoginModal && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center mt-100 justify-center z-50"
          >
            <motion.div
              initial={{ scale: 0.9 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.9 }}
              className="bg-white p-6 rounded-lg shadow-lg text-center"
            >
              <h3 className="text-lg font-semibold text-gray-800 mb-4">You need to login</h3>
              <p className="text-gray-600 mb-6">Please login to view promotions.</p>
              <div className="flex justify-center gap-4">
                <Button
                  onClick={() => {
                    setShowLoginModal(false);
                    navigate("/login");
                  }}
                  className="bg-blue-500 text-white hover:bg-blue-600"
                >
                  Login
                </Button>
                <Button
                  onClick={() => setShowLoginModal(false)}
                  className="bg-gray-300 text-gray-800 hover:bg-gray-400"
                >
                  Cancel
                </Button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </header>
  );
};

export default Header;