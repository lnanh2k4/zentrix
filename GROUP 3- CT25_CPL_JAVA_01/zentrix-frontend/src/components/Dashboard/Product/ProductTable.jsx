import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Eye, Edit, Trash2, Plus, X, Power, ToggleRight, ToggleLeft, PackageX, ArrowLeft, ArrowRight, Trash } from "lucide-react";
import axios from "axios";
import "../styles.css";
import ImageGallery from "./ImageGallery";
import ProductViewModal from "./ProductViewModal";
import "./ProductTable.css";
import CreateProductModal from "./CreateProductModal";
import ProductEditModal from "./ProductEditModal";
import { showNotification } from '../NotificationPopup';
import { showConfirm } from '../ConfirmPopup';
import { createAttribute, createProduct, createProductType, createProductTypeAttribute, createProductTypeBranch, createProductTypeVariation, createVariation, deleteImage, deleteProduct, deleteProductTypeBranch, getAllAttributes, getAllProducts, getAllProductTypeAttribute, getAllProductTypeAttributeByProdTypeId, getAllProductTypeBranchByProdTypeId, getAllProductTypeVariation, getAllProductTypeVariationByProdTypeId, getAllVariations, getImageProductType, getProduct, getProductTypeBranchs, getProductTypes, searchProducts, unactiveProduct, unactiveProductType, updateProduct, updateProductType, updateProductTypeAttribute, updateProductTypeBranch, updateProductTypeVariation } from "@/context/ApiContext";
const ProductTable = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [categories, setCategories] = useState([]);
    const [suppliers, setSuppliers] = useState([]);
    const [branches, setBranches] = useState([]);
    const [attributes, setAttributes] = useState([]);
    const [variations, setVariations] = useState([]);
    const [products, setProducts] = useState([]);
    const [productTypeBranch, setProductTypeBranch] = useState([]);
    const [productTypeAttribute, setProductTypeAttribute] = useState([]);
    const [productTypeVariation, setProductTypeVariation] = useState([]);
    const [isLoadingCategories, setIsLoadingCategories] = useState(true);
    const [isLoadingSuppliers, setIsLoadingSuppliers] = useState(true);
    const [isLoadingBranches, setIsLoadingBranches] = useState(true);
    const [isLoadingAttributes, setIsLoadingAttributes] = useState(true);
    const [isLoadingVariations, setIsLoadingVariations] = useState(true);
    const [isLoadingProducts, setIsLoadingProducts] = useState(true);
    const [isLoadingSaveProduct, setIsLoadingSaveProduct] = useState(false);
    // Thêm vào phần khai báo state
    const [searchTerm, setSearchTerm] = useState("");
    const [isSearching, setIsSearching] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(0);
    // State for new/existing product and its related entities
    const [selectedProductId, setSelectedProductId] = useState(""); // To store the selected product ID or "" for new product
    const [newProduct, setNewProduct] = useState({
        prodName: "",
        cateId: "", // Store ID internally as a string or number
        suppId: "", // Store ID internally as a string or number
        description: "",
        vat: "",
        status: 1,
    });

    const [newProductTypes, setNewProductTypes] = useState([]); // Array to store temporary ProductTypes and their related entities
    const [newProductType, setNewProductType] = useState({
        prodTypeName: "",
        prodTypePrice: "",
        unit: "",
        unitPrice: "",
        status: 1,
    });

    const [newProductTypeBranches, setNewProductTypeBranches] = useState([]); // Branches for the current ProductType
    const [newBranch, setNewBranch] = useState({
        brchId: "",
        quantity: "",
        status: 1,
    });

    const [newProductTypeAttributes, setNewProductTypeAttributes] = useState([]); // Attributes for the current ProductType
    const [newAttribute, setNewAttribute] = useState({
        atbId: "",
        prodAtbValue: "",
        atbDescription: "",
    });

    const [newProductTypeVariations, setNewProductTypeVariations] = useState([]); // Variations for the current ProductType
    const [newVariation, setNewVariation] = useState({
        variId: "",
        prodTypeValue: "",
    });

    const [newImageProductTypes, setNewImageProductTypes] = useState([]); // Attributes for the current ProductType
    const [newNewImageProductType, setNewImageProductType] = useState({
        imageProdId: "",
        prodTypeId: "",
        imageId: "",
    });

    const [newAttributeName, setNewAttributeName] = useState("");
    const [newVariationName, setNewVariationName] = useState("");
    const [imageFiles, setImageFiles] = useState([]);
    const [imagePreviews, setImagePreviews] = useState([]);

    const [editProduct, setEditProduct] = useState(null);
    const [editProductTypes, setEditProductTypes] = useState([]);
    const [selectedEditProductType, setSelectedEditProductType] = useState(null);

    const [isEditModalOpen, setIsEditModalOpen] = useState(false)
    // Add new state for view modal
    const [isViewModalOpen, setIsViewModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState(null);
    const [selectedProductType, setSelectedProductType] = useState(null);
    const [productTypes, setProductTypes] = useState([]);
    const [isLoadingProductTypes, setIsLoadingProductTypes] = useState(false);

    const renderPagination = () => {
        if (totalPages === 0) return <p className="text-gray-500">No products available</p>;

        const maxButtons = 5; // Số nút phân trang tối đa hiển thị
        const halfButtons = Math.floor(maxButtons / 2);
        let startPage = Math.max(0, currentPage - halfButtons);
        let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

        // Điều chỉnh startPage nếu số nút hiển thị ít hơn maxButtons
        if (endPage - startPage + 1 < maxButtons) {
            startPage = Math.max(0, endPage - maxButtons + 1);
        }

        const pageButtons = [];

        // Nút First
        pageButtons.push(
            <Button
                key="first"
                variant="outline"
                onClick={() => handlePageChange(0)}
                disabled={currentPage === 0 || isLoadingProducts}
                className="text-sm px-3 py-1"
            >
                First
            </Button>
        );

        // Nút Previous
        pageButtons.push(
            <Button
                key="prev"
                variant="outline"
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0 || isLoadingProducts}
                className="text-sm px-3 py-1"
            >
                Previous
            </Button>
        );

        // Dấu "..." nếu không bắt đầu từ trang 0
        if (startPage > 0) {
            pageButtons.push(
                <Button key="start-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>
                    ...
                </Button>
            );
        }

        // Các nút số trang
        for (let i = startPage; i <= endPage; i++) {
            pageButtons.push(
                <Button
                    key={i}
                    variant={currentPage === i ? "default" : "outline"}
                    className={currentPage === i ? "bg-blue-500 text-white" : ""}
                    onClick={() => handlePageChange(i)}
                    disabled={isLoadingProducts}
                >
                    {i + 1}
                </Button>
            );
        }

        // Dấu "..." nếu không kết thúc ở trang cuối
        if (endPage < totalPages - 1) {
            pageButtons.push(
                <Button key="end-ellipsis" variant="outline" className="text-sm px-3 py-1" disabled>
                    ...
                </Button>
            );
        }

        // Nút Next
        pageButtons.push(
            <Button
                key="next"
                variant="outline"
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1 || isLoadingProducts}
                className="text-sm px-3 py-1"
            >
                Next
            </Button>
        );

        // Nút Last
        pageButtons.push(
            <Button
                key="last"
                variant="outline"
                onClick={() => handlePageChange(totalPages - 1)}
                disabled={currentPage >= totalPages - 1 || isLoadingProducts}
                className="text-sm px-3 py-1"
            >
                Last
            </Button>
        );

        return pageButtons;
    };

    const handlePageSizeChange = (event) => {
        const newSize = parseInt(event.target.value);
        console.log("Fetching with pageSize:", newSize); // Debug
        setPageSize(newSize);
        setCurrentPage(0);
        fetchProducts(0, searchTerm, newSize);
    };

    const handleSearchChange = (e) => {
        const newSearchTerm = e.target.value;
        setSearchTerm(newSearchTerm);
        setCurrentPage(0);
        fetchProducts(0, newSearchTerm, pageSize);
    };

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setCurrentPage(newPage);
            fetchProducts(newPage, searchTerm, pageSize);
        }
    };
    /////

    const fetchProducts = async (page = 0, keyword = "", size = pageSize) => {
        setIsLoadingProducts(true);
        try {
            let response;
            if (keyword.trim()) {
                response = await searchProducts(keyword, page, size);
                console.log("Search API response:", response);

                if (response && Array.isArray(response.content)) {
                    response.content.forEach((product, index) => {
                    });
                    response.content = response.content.filter(product => {
                        const isValidStatus = product.status === "1" || product.status === "0" || product.status === 1 || product.status === 0;
                        if (!isValidStatus) {
                            console.log("Filtered out product:", product);
                        }
                        return isValidStatus;
                    });
                }
            } else {
                response = await getAllProducts(page, size);
                // if (response && Array.isArray(response.content)) {
                //     response.content.forEach((product, index) => {
                //     });
                //     response.content = response.content.filter(product => {
                //         const isValidStatus = product.status === "1" || product.status === "0" || product.status === 1 || product.status === 0;
                //         if (!isValidStatus) {
                //         }
                //         return isValidStatus;
                //     });
                // }
            }
            if (!response || !Array.isArray(response.content)) {
                throw new Error("Invalid response structure");
            }
            const newProducts = response.content;
            const newTotalPages = response.pagination?.totalPages || 0;

            setProducts(newProducts);
            setTotalPages(newTotalPages);
        } catch (error) {
            console.error("Error fetching products:", error);
            setProducts([]);
            setTotalPages(0);
        } finally {
            setIsLoadingProducts(false);
        }
    };
    const fetchProductTypes = async (prodId) => {
        try {
            setIsLoadingProductTypes(true);
            const response = await getProductTypes(prodId);
            // Lọc bỏ các mục có status = 3
            const filteredResponse = Array.isArray(response)
                ? response.filter(item => item.status !== 3)
                : [];
            setProductTypes(filteredResponse);
            console.log("Fetched product types (filtered):", filteredResponse); // Debug log
            return filteredResponse; // Trả về dữ liệu đã lọc
        } catch (error) {
            console.error("Error fetching product types:", error);
            setProductTypes([]);
            return []; // Trả về mảng rỗng nếu có lỗi
        } finally {
            setIsLoadingProductTypes(false);
        }
    };
    const fetchProductTypeBranch = async () => {
        try {
            const response = await getProductTypeBranchs();
            setProductTypeBranch(response.content);
            console.log("Selected => Fetched product types Branch:", response.content); // Debug log
            return response.content; // Explicitly return the data
        } catch (error) {
            console.error("Error fetching product types branch:", error);
            setProductTypes([]);
            return []; // Return empty array on error
        }
    };


    const fetchAttributes = async () => {
        try {
            setIsLoadingAttributes(true);
            const attributesResponse = await getAllAttributes();
            console.log(attributesResponse.content)
            setAttributes(Array.isArray(attributesResponse.content) ? attributesResponse.content.reverse() : []);
        } catch (error) {
            console.error("Error fetching attributes:", error);
            setAttributes([]);
        } finally {
            setIsLoadingAttributes(false);
        }
    };

    const fetchVariations = async () => {
        try {
            setIsLoadingVariations(true);
            const variationsResponse = await getAllVariations();
            setVariations(Array.isArray(variationsResponse.content) ? variationsResponse.content.reverse() : []);
        } catch (error) {
            console.error("Error fetching variations:", error);
            setVariations([]);
        } finally {
            setIsLoadingVariations(false);
        }
    };

    const fetchProductTypeAttribute = async () => {
        try {
            const response = await getAllProductTypeAttribute();
            setProductTypeAttribute(response.content);
            console.log("Selected => Fetched product types Attribute:", response.content);
            return response.content;
        } catch (error) {
            console.error("Error fetching product types attribute:", error);
            setProductTypes([]);
            return [];
        }
    };
    const fetchProductTypeVariation = async () => {
        try {
            const response = await getAllProductTypeVariation();
            setProductTypeVariation(response.content);
            console.log("Selected => Fetched product types Variation:", response.content);
            return response.content;
        } catch (error) {
            console.error("Error fetching product types variation:", error);
            setProductTypes([]);
            return [];
        }
    };
    const [productTypeOfProduct, setProductTypeByProductSelect] = useState(false);
    const fetchProductTypeByProdId = async (prodId) => {
        try {
            const response = await getProductTypes(prodId);
            const filteredResponse = Array.isArray(response)
                ? response.filter(item => item.status !== 3)
                : [];
            setProductTypeByProductSelect(filteredResponse);
            console.log("Selected View => Fetched product types (filtered):", filteredResponse);
            return filteredResponse;
        } catch (error) {
            console.error("Error fetching product types variation:", error);
            setProductTypes([]);
            return [];
        }
    };

    const [productTypeBranchOfProduct, setProductTypeBranchByProductSelect] = useState(false);
    const fetchProductTypeBranchByProdTypeId = async (prodTypeId) => {
        try {
            const response = await getAllProductTypeBranchByProdTypeId(prodTypeId);
            setProductTypeBranchByProductSelect(response);
            console.log("Selected View => Fetched product types Branch:", response);
            return response;
        } catch (error) {
            console.error("Error fetching product types variation:", error);
            setProductTypes([]);
            return [];
        }
    };
    const [productTypeAttributeOfProduct, setProductTypeAttributeByProductSelect] = useState(false);
    const fetchProductTypeAttributeByProdTypeId = async (prodTypeId) => {
        try {
            const response = await getAllProductTypeAttributeByProdTypeId(prodTypeId);
            setProductTypeAttributeByProductSelect(response);
            console.log("Selected View => Fetched product types Variation:", response); // Debug log
            return response; // Explicitly return the data
        } catch (error) {
            console.error("Error fetching product types variation:", error);
            setProductTypes([]);
            return []; // Return empty array on error
        }
    };
    const [productTypeVariationOfProduct, setProductTypeVariationByProductSelect] = useState(false);
    const fetchProductTypeVariationByProdTypeId = async (prodTypeId) => {
        try {
            const response = await getAllProductTypeVariationByProdTypeId(prodTypeId);
            setProductTypeVariationByProductSelect(response);
            console.log("Selected View => Fetched product types Variation:", response); // Debug log
            return response; // Explicitly return the data
        } catch (error) {
            console.error("Error fetching product types variation:", error);
            setProductTypes([]);
            return []; // Return empty array on error
        }
    };
    // Handle View action
    const handleView = (product) => {
        setSelectedProduct(product);
        setSelectedProductType(null); // Reset selected product type
        fetchProductTypeByProdId(product.prodId)
        fetchProductTypes(product.prodId); // Fetch product types
        setIsViewModalOpen(true);
    };
    const resetProductTypes = async () => {
        const productTypesData = await fetchProductTypes(editProduct.prodId);
        const newProductTypes = Array.isArray(productTypesData)
            ? productTypesData.filter(item => item.status !== 3)
            : [];

        setEditProductTypes((prev) => {
            const result = prev
                .filter(prevItem => prevItem.status !== 3)
                .map((prevItem) => {
                    const updatedItem = newProductTypes.find(
                        (newItem) => newItem.prodTypeId === prevItem.prodTypeId
                    );
                    return updatedItem ? { ...prevItem, ...updatedItem } : prevItem;
                })
                .filter(item => item.status !== 3);

            console.log('Kết quả cuối cùng:', result);
            return result;
        });
    };
    // Handle Edit action
    const handleEdit = async (product) => {
        localStorage.setItem("prodIdEditting", product.prodId);
        console.log(localStorage.getItem('prodIdEditting'))
        if (isEditModalOpen) {
            handleCloseEditModal();
            return
        }
        if (isModalOpen) {
            handleCloseModal();
        }
        try {
            console.log("handleEdit called with product:", product);
            setEditProduct(product);
            setNewProduct({
                prodName: product.prodName || "",
                cateId: product.cateId || "",
                suppId: product.suppId || "",
                description: product.description || "",
                vat: product.vat?.toString() || "",
                status: product.status || 1,
            });
            const productTypesData = await fetchProductTypes(product.prodId);
            console.log("Fetched editProductTypes:", productTypesData); // Debug log
            setEditProductTypes(Array.isArray(productTypesData) ? productTypesData : []);
            setSelectedEditProductType(null);
            setNewProductTypes([]);
            setIsEditModalOpen(true);
        } catch (error) {
            console.error("Error in handleEdit:", error);
            setEditProductTypes([]);
            showNotification("Failed to load edit modal. Please try again.", 3000, 'fail');
        }
    };
    const handleCloseEditModal = () => {
        setIsEditModalOpen(false);
        setEditProduct(null);
        setEditProductTypes([]);
        setSelectedEditProductType(null);
        setNewProduct({ prodName: "", cateId: "", suppId: "", description: "", vat: "", status: 1 });
        setNewProductTypes([]);
        setNewProductType({ prodTypeName: "", prodTypePrice: "", unit: "", unitPrice: "", status: 1 });
        setNewProductTypeBranches([]);
        setNewBranch({ brchId: "", quantity: "", status: 1 });
        setNewProductTypeAttributes([]);
        setSelectedEditProductType(null);
        setNewAttribute({ atbId: "", prodAtbValue: "", atbDescription: "" });
        setNewProductTypeVariations([]);
        setNewVariation({ variId: "", prodTypeValue: "" });
        setNewAttributeName("");
        setNewVariationName("");
        setImageFiles([]);
        setImagePreviews([]);
        setErrors([]);
    };

    const handleDelete = async (product, e) => {
        e.preventDefault();
        e.stopPropagation();
        const product1 = await getProduct(product.prodId);



        if (product.status == 1) {
            const confirmed = await showConfirm(
                `This action will inactivate all productType inside. Are you sure you want to deactivate ${product.prodName}?`,
                'fail'
            );
            if (confirmed) {
                // Kiểm tra và cập nhật productTypes
                if (product1.productTypes && product1.productTypes.length > 0) {
                    const activeProductTypes = product1.productTypes.filter(item => item.status === 1);
                    if (activeProductTypes.length > 0) {
                        try {
                            for (const item of activeProductTypes) {
                                console.log(item.status)
                                await unactiveProductType(item.prodTypeId, null, 0);
                            }
                            console.log("Updated all active product types to inactive");
                        } catch (error) {
                            console.error("Error updating product types:", error);
                        }
                    }
                }
            }
            else if (!confirmed) return;
        } else {
            const confirmed = await showConfirm(
                `Are you sure you want to activate ${product.prodName}?`
            );
            if (!confirmed) return;
        }

        try {
            await unactiveProduct(product.prodId, null);
            showNotification("Product deactivated successfully!", 3000, 'complete');
            await refreshProducts();
        } catch (error) {
            console.error("Error deactivating product:", error);
            showNotification("Failed to deactivate product.", 3000, 'fail');
        }
    };

    const softDeleteProduct = async (product, e) => {
        e.preventDefault();
        e.stopPropagation();

        const confirmed = await showConfirm(`This action will also delete all productType inside. Are you sure you want to REMOVE ${product.prodName}?`, 'fail');
        if (confirmed) {
            const product1 = await getProduct(product.prodId);
            if (product1.productTypes && product1.productTypes.length > 0) {
                const activeProductTypes = product1.productTypes.filter(item => item.status === 1);
                if (activeProductTypes.length > 0) {
                    try {
                        for (const item of activeProductTypes) {
                            console.log(item.status)
                            await unactiveProductType(item.prodTypeId, null, 3);
                        }
                        console.log("Updated all active productTypes inside product to inactive");
                    } catch (error) {
                        console.error("Error updating product types:", error);
                    }
                }
            }
        }
        else if (!confirmed) return; // If false, exit early

        try {
            await deleteProduct(product.prodId, null);
            setProducts((prev) => prev.filter((item) => item.prodId !== product.prodId));
            await refreshProducts();
            showNotification("Product removed successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error deactivating product:", error);
            showNotification("Failed to remove product.", 3000, 'fail');
            await refreshProducts();
        }
    };

    const handleUnactiveProductType = async (productType, e) => {
        e.preventDefault();
        e.stopPropagation();
        console.log(localStorage.getItem('prodIdEditting'))
        const productEditting = await getProduct(localStorage.getItem('prodIdEditting'));
        if (productEditting.status != 1) {
            showNotification("Cannot reactivate product Type when product is inactive", 3000, 'fail');
            return;
        }
        if (productType.status == 1) {
            const confirmed = await showConfirm(
                `Are you sure you want to deactivate ${productType.prodTypeName}?`,
                'fail'
            );
            if (!confirmed) return;
        } else {
            const confirmed = await showConfirm(
                `Are you sure you want to active ${productType.prodTypeName}?`
            );
            if (!confirmed) return;
        }

        try {

            await unactiveProductType(productType.prodTypeId, null, 1)
            // await refreshProducts(); 
            showNotification("ProductType deactivated successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error deactivating productType:", error);
            showNotification("Failed to deactivate productType.", 3000, 'fail');
        }
        resetProductTypes();
    };
    const handleSoftDeleteProductType = async (productType, e) => {
        e.preventDefault();
        e.stopPropagation();

        const confirmed = await showConfirm(
            `Are you sure you want to delete ${productType.prodTypeName}?`,
            'fail'
        );
        if (!confirmed) return;

        try {
            // Gọi API để set status = 3
            await unactiveProductType(productType.prodTypeId, null, 3);

            // Cập nhật ngay state trước khi reset
            setEditProductTypes((prev) => {
                const updated = prev.map((item) =>
                    item.prodTypeId === productType.prodTypeId
                        ? { ...item, status: 3 } // Cập nhật status thành 3 ngay lập tức
                        : item
                );
                return updated.filter(item => item.status !== 3); // Loại bỏ status = 3
            });

            // Reset lại danh sách để đồng bộ với server
            await resetProductTypes();
            showNotification("ProductType deleted successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error deleting productType:", error);
            showNotification("Failed to delete productType.", 3000, 'fail');
        }
    };
    const handleEditProductTypeSelect = (prodTypeId) => {
        const productType = editProductTypes.find(pt => pt.prodTypeId === parseInt(prodTypeId));
        console.log("Editing productType", editProductTypes)
        fetchProductTypeBranch()
        fetchProductTypeAttribute()
        fetchProductTypeVariation()
        fetchImageProductType(prodTypeId)
        console.log("Selected ProductType:", productType); // Debug log
        setSelectedEditProductType(productType);
        if (productType) {
            setNewProductType({
                prodTypeName: productType.prodTypeName || "",
                prodTypePrice: productType.prodTypePrice?.toString() || "",
                unit: productType.unit || "",
                unitPrice: productType.unitPrice?.toString() || "",
                status: productType.status || 1,
            });
            setNewProductTypeBranches(productType.branches || []);
            setNewProductTypeAttributes(productType.attributes || []);
            setNewProductTypeVariations(productType.variations || []);

        } else {
            setNewProductType({ prodTypeName: "", prodTypePrice: "", unit: "", unitPrice: "", status: 1 });
            setNewProductTypeBranches([]);
            setNewProductTypeAttributes([]);
            setNewProductTypeVariations([]);
            setNewImageProductTypes([]);
            setEditedVariationValues({});
        }
    };

    // State to track edited values (temporary state for inline editing)
    const [editedBranchQuantities, setEditedBranchQuantities] = useState({});
    const [editedAttributeValues, setEditedAttributeValues] = useState({});
    const [editedAttributeDescriptions, setEditedAttributeDescriptions] = useState({});
    const [editedVariationValues, setEditedVariationValues] = useState({});

    // Handlers for inline editing
    const handleEditBranchQuantity = (branchId, value) => {
        console.log("Editing branch quantity:", branchId, value);
        setEditedBranchQuantities(prev => ({ ...prev, [branchId]: value }));
    };

    const handleEditVariationValue = (prodTypeVariId, value) => {
        console.log("Editing variation value:", prodTypeVariId, value);
        setEditedVariationValues(prev => ({ ...prev, [prodTypeVariId]: value }));
    };

    const handleEditAttributeValue = (attrId, value) => {
        console.log("Editing attribute value:", attrId, value);
        setEditedAttributeValues(prev => ({ ...prev, [attrId]: value }));
    };

    const handleEditAttributeDescription = (attrId, value) => {
        console.log("Editing attribute description:", attrId, value);
        setEditedAttributeDescriptions(prev => ({ ...prev, [attrId]: value }));

    };


    const handleUpdateBranch = async (branchId, originalQuantity) => {
        console.log("Updating branch with ID:", branchId);
        console.log("productTypeBranch:", productTypeBranch);
        const updatedQuantity = editedBranchQuantities[branchId] || originalQuantity;
        const parsedQuantity = parseInt(updatedQuantity);

        if (isNaN(parsedQuantity)) {
            showNotification("Invalid quantity value. Please enter a valid number.", 3000, 'fail');
            return;
        }

        const branchToUpdate = productTypeBranch.find(b => b.prodTypeBrchId === branchId);
        if (!branchToUpdate) {
            showNotification("Branch not found in local state.", 3000, 'fail');
            return;
        }

        const requestBody = {
            quantity: parsedQuantity,
            prodTypeId: branchToUpdate.prodTypeId.prodTypeId,
            brchId: branchToUpdate.brchId.brchId
        };

        try {
            console.log("Request body:", requestBody);
            await updateProductTypeBranch(branchId, requestBody)

            setProductTypeBranch(prev =>
                prev.map(branch =>
                    branch.prodTypeBrchId === branchId
                        ? { ...branch, quantity: parsedQuantity }
                        : branch
                )
            );
            setEditedBranchQuantities(prev => {
                const newState = { ...prev };
                delete newState[branchId];
                return newState;
            });

            showNotification('Branch updated successfully!', 3000, 'complete');
        } catch (error) {
            console.error("Error updating branch:", {
                message: error.message,
                response: error.response ? error.response.data : "No response data",
                status: error.response ? error.response.status : "No status",
            });
            showNotification('Failed to update branch.', 3000, 'fail');
        }
    };

    const handleUpdateAttribute = async (attrId, originalValue, originalDescription) => {
        const updatedValue = editedAttributeValues[attrId] || originalValue;
        const updatedDescription = editedAttributeDescriptions[attrId] || originalDescription;
        const requestBody = {
            prodAtbValue: updatedValue,
            atbDescription: updatedDescription,
        };
        try {
            await updateProductTypeAttribute(attrId, requestBody)
            setProductTypeAttribute(prev =>
                prev.map(attr => (attr.prodTypeAtbId === attrId ? { ...attr, prodAtbValue: updatedValue, atbDescription: updatedDescription } : attr))
            );
            showNotification("Attribute updated successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error updating attribute:", error);
            showNotification("Failed to update attribute.", 3000, 'fail');
        }
    };

    // Update Variation
    const handleUpdateVariation = async (prodTypeVariId, originalValue, defaultVari) => {
        if (!prodTypeVariId) {
            console.error("prodTypeVariId is null or undefined");
            showNotification("Cannot update variation: Invalid variation ID", 3000, 'fail');
            return;
        }

        const updatedValue = editedVariationValues[prodTypeVariId] || originalValue;
        if (!updatedValue) {
            showNotification("Variation value cannot be empty", 3000, 'fail');
            return;
        }

        // Find the variation in productTypeVariation to get prodTypeId and variId
        const variation = productTypeVariation.find(v => v.prodTypeVariId === prodTypeVariId);
        if (!variation) {
            console.error("Variation not found for prodTypeVariId:", prodTypeVariId);
            showNotification("Variation data not found", 3000, 'fail');
            return;
        }

        const requestBody = {
            prodTypeValue: updatedValue,
            prodTypeId: variation.prodTypeId?.prodTypeId,
            variId: variation.variId?.variId,
            defaultVari: 1,
        };

        if (!requestBody.prodTypeId || !requestBody.variId) {
            console.error("Missing prodTypeId or variId:", requestBody);
            showNotification("Cannot update: Missing required variation data", 3000, 'fail');
            return;
        }

        console.log("Sending request to update variation:", {
            prodTypeVariId,
            requestBody,
        });

        try {
            await updateProductTypeVariation(prodTypeVariId, requestBody);
            // If setting this variation as default (defaultVari = 1), update others to 0
            if (defaultVari === 1) {
                const otherVariations = productTypeVariation.filter(
                    v => v.prodTypeVariId !== prodTypeVariId && v.prodTypeId.prodTypeId === requestBody.prodTypeId
                );
                for (const otherVari of otherVariations) {
                    await axios.put(
                        `http://localhost:6789/api/v1/products/updateProductTypeVariation/${otherVari.prodTypeVariId}`,
                        { ...otherVari, defaultVari: 0 },
                        {
                            headers: { "Content-Type": "application/json" },
                            withCredentials: "true"
                        }
                    );
                }
            }
            setProductTypeVariation(prev =>
                prev.map(vari =>
                    vari.prodTypeVariId === prodTypeVariId
                        ? { ...vari, prodTypeValue: updatedValue, defaultVari: requestBody.defaultVari }
                        : defaultVari === 1
                            ? { ...vari, defaultVari: 0 } // Reset others to 0 if this one is set as default
                            : vari
                )
            );

            setEditedVariationValues(prev => {
                const newState = { ...prev };
                delete newState[prodTypeVariId];
                return newState;
            });

            showNotification("Variation updated successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error updating variation:", error);
            console.log("Response data:", error.response?.data);
            showNotification("Failed to update variation: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };
    const fetchImageProductType = async (prodTypeId) => {
        try {
            const response = await getImageProductType(prodTypeId);
            console.log("Image Response: ", response);

            if (!response) {
                setExistingImageProductTypes([]);
                setImagePreviews([]);
                return [];
            }

            let images;
            if (Array.isArray(response)) {
                images = response.map(imageProductType => ({
                    imageProdId: imageProductType.imageProdId,
                    url: imageProductType.imageId?.imageLink || imageProductType.imageLink || imageProductType,
                }));
            } else {
                images = [{
                    imageProdId: response.imageProdId,
                    url: response.imageId?.imageLink || response.imageLink || response,
                }];
            }

            setExistingImageProductTypes(images);
            setImagePreviews(images.map(image => image.url));
            console.log("Fetched images:", images);
            return images;

        } catch (error) {
            console.error("Error fetching product images:", error);
            setExistingImageProductTypes([]);
            setImagePreviews([]);
            return [];
        }
    };
    const handleUpdateProductType = async () => {
        if (!newProductType.prodTypeName || !newProductType.prodTypePrice || !newProductType.unit || !newProductType.unitPrice) {
            showNotification("Please fill in all required ProductType fields.", 3000, 'fail');
            return;
        }

        const confirmed = await showConfirm(
            `Are you sure you want to update?`,
            'edit'
        );
        if (!confirmed) return;
        setIsLoadingSaveProduct(true);
        const updatedProductType = {
            ...newProductType,
            prodTypePrice: parseFloat(newProductType.prodTypePrice),
            unitPrice: parseFloat(newProductType.unitPrice),
            prodId: editProduct.prodId,
            branches: newProductTypeBranches,
            attributes: newProductTypeAttributes,
            variations: newProductTypeVariations,
        };

        try {
            if (selectedEditProductType) {

                const response = await updateProductType(selectedEditProductType.prodTypeId, updatedProductType);

                const updated = response;
                console.log("Updated ProductType:", updated);
                setEditProductTypes(prev =>
                    prev.map(pt => (pt.prodTypeId === updated.prodTypeId ? updated : pt))
                );

                for (const branch of newProductTypeBranches) {
                    if (!branch.prodTypeBrchId) {
                        const branchPayload = {
                            brchId: parseInt(branch.brchId),
                            quantity: parseInt(branch.quantity),
                            prodTypeId: selectedEditProductType.prodTypeId,
                            status: branch.status || 1,
                        };
                        const branchResponse = await createProductTypeBranch(branchPayload);
                        console.log("Created ProductTypeBranch:", branchResponse);
                    }
                }

                // New attribute
                for (const attribute of newProductTypeAttributes) {
                    if (!attribute.prodTypeAtbId) {
                        const attributePayload = {
                            atbId: parseInt(attribute.atbId),
                            prodAtbValue: attribute.prodAtbValue || "",
                            atbDescription: attribute.atbDescription || "",
                            prodTypeId: selectedEditProductType.prodTypeId,
                        };
                        const attributeResponse = await createProductTypeAttribute(attributePayload);
                        console.log("Created ProductTypeAttribute:", attributeResponse);
                    }
                }

                // new variation
                for (const variation of newProductTypeVariations) {
                    if (!variation.prodTypeVariId) {
                        const variationPayload = {
                            variId: parseInt(variation.variId),
                            prodTypeValue: variation.prodTypeValue || "",
                            prodTypeId: selectedEditProductType.prodTypeId,
                        };
                        const variationResponse = await createProductTypeVariation(variationPayload)
                        console.log("Created ProductTypeVariation:", variationResponse);
                    }
                }

                // Upload hình ảnh mới (không xóa hình ảnh cũ)
                if (newImageProductTypes.length > 0) {
                    const failedUploads = []; // Lưu danh sách các hình ảnh thất bại (tùy chọn)

                    for (const item of newImageProductTypes) {
                        if (item instanceof File) {
                            const formData = new FormData();
                            formData.append("file", item);
                            formData.append("productTypeId", selectedEditProductType.prodTypeId);
                            console.log("Uploading new image:", item.name);

                            try {
                                const imageResponse = await axios.post(
                                    "http://localhost:6789/api/v1/products/productType/createImage",
                                    formData,
                                    {
                                        headers: { "Content-Type": "multipart/form-data" },
                                        withCredentials: "true"
                                    }
                                );
                                console.log("Image uploaded:", imageResponse.data);
                            } catch (error) {
                                console.error("Error uploading image:", item.name, error.response?.data || error.message);
                                failedUploads.push(item.name); // Ghi lại hình ảnh thất bại (tùy chọn)
                                continue; // Bỏ qua lỗi và tiếp tục với hình ảnh tiếp theo
                            }
                        }
                    }

                    // Thông báo nếu có hình ảnh thất bại (tùy chọn)
                    if (failedUploads.length > 0) {
                        showNotification(`Failed to upload some images: ${failedUploads.join(', ')}`, 10000, 'fail');
                    }

                    // Reset state sau khi xử lý xong
                    setNewImageProductTypes([]);
                    setTempImageProductTypes([]);
                    setImagePreviews([]);
                    await fetchImageProductType(selectedEditProductType.prodTypeId);
                }
                showNotification("ProductType and related entities updated successfully!", 3000, 'complete');
                setIsLoadingSaveProduct(false);
                resetProductTypes();
            } else {
                updatedProductType.images = newImageProductTypes;
                setNewProductTypes(prev => [...prev, updatedProductType]);
                showNotification("New ProductType added temporarily!", 3000, 'complete');
            }
        } catch (error) {
            console.error("Error updating ProductType:", error);
            showNotification("Failed to update ProductType: " + (error.response?.data || error.message), 3000, 'fail');
        }

        // Reset ProductType fields
        // setNewProductType({ prodTypeName: "", prodTypePrice: "", unit: "", unitPrice: "", status: 1 });
        // setNewProductTypeBranches([]);
        // setNewProductTypeAttributes([]);
        // setNewProductTypeVariations([]);
        // setNewImageProductTypes([]);
        // setImagePreviews([]);
        // setTempImageProductTypes([]); // Reset tempImageProductTypes
        // setSelectedEditProductType(null);
    };

    const handleProductTypeSelect = (prodTypeId) => {
        Promise.all([
            // fetchProductTypeBranch(),
            // fetchProductTypeAttribute(),
            // fetchProductTypeVariation(),

            fetchProductTypeAttributeByProdTypeId(prodTypeId),
            fetchProductTypeBranchByProdTypeId(prodTypeId),
            fetchProductTypeVariationByProdTypeId(prodTypeId),

            fetchImageProductType(prodTypeId),
        ]).then(() => {
            const productType = productTypes.find(pt => pt.prodTypeId === prodTypeId);
            console.log(productType)
            setSelectedProductType(productType);
            setSelectedEditProductType(productType);
        }).catch(error => {
            console.error("Error fetching product type data:", error);
        });
    };
    const [tempImageProductTypes, setTempImageProductTypes] = useState([]);
    const [existingImageProductTypes, setExistingImageProductTypes] = useState([]);
    const handleImageChange = (e) => {
        const files = Array.from(e.target.files);
        if (files.length === 0) return;

        const previews = files.map(file => URL.createObjectURL(file));
        setImagePreviews(prev => [...prev, ...previews]);
        setNewImageProductTypes(prev => [...prev, ...files]);

        console.log("Selected Files:", files);
        console.log("Updated Image Previews:", [...imagePreviews, ...previews]);
    };

    const handleRemoveImage = async (index) => {
        if (index < 0 || index >= imagePreviews.length) {
            console.warn("Invalid index for removing image:", index);
            return;
        }

        const updatedPreviews = imagePreviews.filter((_, i) => i !== index);
        setImagePreviews(updatedPreviews);

        if (index < existingImageProductTypes.length) {
            const imageToDelete = existingImageProductTypes[index];
            try {
                await deleteImage(imageToDelete.imageProdId);
                console.log("Deleted image from database:", imageToDelete.imageProdId);
            } catch (error) {
                console.error("Error deleting image:", error);
                showNotification("Failed to delete image: " + (error.response?.data?.message || error.message), 3000, 'fail');
                return;
            }
            const updatedExistingImages = existingImageProductTypes.filter((_, i) => i !== index);
            setExistingImageProductTypes(updatedExistingImages);
        } else {
            const newImageIndex = index - existingImageProductTypes.length;
            const updatedFiles = newImageProductTypes.filter((_, i) => i !== newImageIndex);
            setNewImageProductTypes(updatedFiles);
            URL.revokeObjectURL(imagePreviews[index]);
        }

        console.log("Removed image at index:", index);
        console.log("Updated Previews:", updatedPreviews);
    };

    const handleEditSubmitFinal = async (e) => {
        e.preventDefault();


        const confirmed = await showConfirm(
            `Are you sure you want to save product?`,
            'edit'
        );
        if (!confirmed) return;

        setIsLoadingSaveProduct(true);
        const productPayload = {
            ...newProduct,
            cateId: parseInt(newProduct.cateId),
            suppId: parseInt(newProduct.suppId),
            vat: parseFloat(newProduct.vat) || 0,
            description: newProduct.description || "",
        };
        console.log(productPayload)
        try {

            await updateProduct(editProduct.prodId, productPayload);
            handleCloseEditModal();
            await refreshProducts();
            setIsLoadingSaveProduct(false);
            showNotification("Product and ProductTypes updated successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error updating product:", error);
            showNotification("Failed to update product: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };
    const handleCloseViewModal = () => {
        setIsViewModalOpen(false);
        setSelectedProduct(null);
        setSelectedProductType(null);
        setProductTypes([]);
    };

    const handleRemoveBranch = (index) => {
        setNewProductTypeBranches(prev => prev.filter((_, i) => i !== index));
    };

    const handleRemoveAttribute = (index) => {
        setNewProductTypeAttributes(prev => prev.filter((_, i) => i !== index));
    };

    const handleRemoveVariation = (index) => {
        setNewProductTypeVariations(prev => prev.filter((_, i) => i !== index));
    };
    useEffect(() => {
        const fetchInitialData = async () => {
            try {
                setIsLoadingCategories(true);
                setIsLoadingSuppliers(true);
                setIsLoadingBranches(true);
                setIsLoadingAttributes(true);
                setIsLoadingVariations(true);
                setIsLoadingProducts(true);

                const [categoriesResponse, suppliersResponse, branchesResponse, attributesResponse, variationsResponse, productsResponse] = await Promise.all([
                    axios.get("http://localhost:6789/api/v1/categories?page=0&size=2000", { withCredentials: "true" }),
                    axios.get("http://localhost:6789/api/v1/suppliers", { withCredentials: "true" }),
                    axios.get("http://localhost:6789/api/v1/branches/all", { withCredentials: "true" }),
                    axios.get("http://localhost:6789/api/v1/attributes", { withCredentials: "true" }),
                    axios.get("http://localhost:6789/api/v1/variations", { withCredentials: "true" }),
                    axios.get(`http://localhost:6789/api/v1/products?page=0&size=${pageSize}&status=1,0`, { withCredentials: "true" }),
                ]);

                setCategories(Array.isArray(categoriesResponse.data.content.content) ? categoriesResponse.data.content.content : []);
                setSuppliers(Array.isArray(suppliersResponse.data.content.content) ? suppliersResponse.data.content.content : []);
                setBranches(Array.isArray(branchesResponse.data.content) ? branchesResponse.data.content : []);
                setAttributes(Array.isArray(attributesResponse.data.content) ? attributesResponse.data.content : []);
                setVariations(Array.isArray(variationsResponse.data.content) ? variationsResponse.data.content : []);
                setProducts(Array.isArray(productsResponse.data.content) ? productsResponse.data.content : []);
                setTotalPages(productsResponse.data.pagination.totalPages || 0);
            } catch (error) {
                console.error("Error fetching initial data:", error);
                setCategories([]);
                setSuppliers([]);
                setBranches([]);
                setAttributes([]);
                setVariations([]);
                setProducts([]);
                setTotalPages(0);
            } finally {
                setIsLoadingCategories(false);
                setIsLoadingSuppliers(false);
                setIsLoadingBranches(false);
                setIsLoadingAttributes(false);
                setIsLoadingVariations(false);
                setIsLoadingProducts(false);
            }
        };

        fetchInitialData();
    }, []); // Chỉ chạy một lần khi mount
    const handleSearch = async (page) => {
        setIsLoadingProducts(true);
        try {
            let response;
            if (!searchTerm.trim()) {
                response = await axios.get(
                    `http://localhost:6789/api/v1/products?page=${page}&size=${pageSize}&status=1,0`,
                    { withCredentials: "true" }
                );
            } else {
                response = await axios.get(
                    `http://localhost:6789/api/v1/products/search?keyword=${encodeURIComponent(searchTerm)}&page=${page}&size=${pageSize}`,
                    { withCredentials: "true" }
                );
            }
            setProducts(Array.isArray(response.data?.content) ? response.data.content : []);
            setTotalPages(response.data?.pagination.totalPages || 0);
        } catch (error) {
            console.error("Error searching products:", error);
            setProducts([]);
            setTotalPages(0);
        } finally {
            setIsLoadingProducts(false);
        }
    };
    const handleCreateClick = () => {
        setErrors([]);
        if (isModalOpen) {
            // If modal is open, close it and reset all states
            handleCloseModal();
        } else {
            if (isEditModalOpen) {
                handleCloseEditModal();
            }
            // If modal is closed, open it and reset to new product
            setIsModalOpen(true);
            setSelectedProductId(""); // Reset to new product on modal open
            setNewProduct({ prodName: "", cateId: "", suppId: "", description: "", vat: "", status: 1 });
            setNewProductTypes([]);
        }
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedProductId("");
        setNewProduct({ prodName: "", cateId: "", suppId: "", description: "", vat: "", status: 1 });
        setNewProductTypes([]);
        setNewProductType({ prodTypeName: "", prodTypePrice: "", unit: "", unitPrice: "", status: 1 });
        setNewProductTypeBranches([]);
        setNewBranch({ brchId: "", quantity: "", status: 1 });
        setNewProductTypeAttributes([]);
        setNewAttribute({ atbId: "", prodAtbValue: "", atbDescription: "" });
        setNewProductTypeVariations([]);
        setNewVariation({ variId: "", prodTypeValue: "" });
        setNewAttributeName("");
        setNewVariationName("");
        setImageFiles([]);
        setImagePreviews([]);
        setErrors([]);
    };

    const [errors, setErrors] = useState({});
    const handleInputChange = (e, stateSetter, stateKey) => {
        const { name, value } = e.target;

        if (selectedProductId && stateKey === "newProduct") return;

        let newErrors = { ...errors };

        // Validation cho từng field
        switch (stateKey) {
            case "newProduct":
                if (name === "prodName") {
                    if (!value.trim()) newErrors.prodName = "Product name cannot be empty";
                    else if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]*$/.test(value)) newErrors.prodName = "Product name can only contain letters and numbers";
                    else delete newErrors.prodName;
                } else if (name === "cateId") {
                    if (!value) newErrors.cateId = "Please select a category";
                    else delete newErrors.cateId;
                } else if (name === "suppId") {
                    if (!value) newErrors.suppId = "Please select a supplier";
                    else delete newErrors.suppId;
                } else if (name === "vat") {
                    if (!value.trim()) newErrors.vat = "VAT cannot be empty";
                    else if (isNaN(value) || parseFloat(value) < 0) newErrors.vat = "VAT must be a non-negative number";
                    else delete newErrors.vat;
                }
                // else if (name === "description") {
                //     if (!value.trim()) newErrors.description = "Description cannot be empty";
                //     else delete newErrors.description;
                // }
                break;

            case "newProductType":
                if (name === "prodTypeName") {
                    if (!value.trim()) newErrors.prodTypeName = "Product type name cannot be empty";
                    else if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]*$/.test(value)) newErrors.prodTypeName = "Product type name can only contain letters and numbers";
                    else delete newErrors.prodTypeName;
                } else if (name === "prodTypePrice") {
                    if (!value.trim()) newErrors.prodTypePrice = "Product type price cannot be empty";
                    else if (isNaN(value) || parseFloat(value) < 0) newErrors.prodTypePrice = "Product type price must be a non-negative number";
                    else delete newErrors.prodTypePrice;
                } else if (name === "unit") {
                    if (!value.trim()) newErrors.unit = "Unit cannot be empty";
                    else delete newErrors.unit;
                } else if (name === "unitPrice") {
                    if (!value.trim()) newErrors.unitPrice = "Unit price cannot be empty";
                    else if (isNaN(value) || parseFloat(value) < 0) newErrors.unitPrice = "Unit price must be a non-negative number";
                    else delete newErrors.unitPrice;
                }
                break;

            case "newBranch":
                if (name === "brchId") {
                    if (!value) newErrors.brchId = "Please select a branch";
                    else delete newErrors.brchId;
                } else if (name === "quantity") {
                    if (!value.trim()) newErrors.quantity = "Quantity cannot be empty";
                    else if (isNaN(value) || parseInt(value) < 0) newErrors.quantity = "Quantity must be a non-negative integer";
                    else delete newErrors.quantity;
                }
                break;

            case "newAttribute":
                if (name === "atbId") {
                    if (!value) newErrors.atbId = "Please select an attribute";
                    else delete newErrors.atbId;
                } else if (name === "prodAtbValue") {
                    if (!value.trim()) newErrors.prodAtbValue = "Attribute value cannot be empty";
                    else delete newErrors.prodAtbValue;
                }
                break;

            case "newVariation":
                if (name === "variId") {
                    if (!value) newErrors.variId = "Please select a variation";
                    else delete newErrors.variId;
                } else if (name === "prodTypeValue") {
                    if (!value.trim()) newErrors.prodTypeValue = "Variation value cannot be empty";
                    else delete newErrors.prodTypeValue;
                }
                break;

            default:
                break;
        }

        setErrors(newErrors);
        stateSetter(prev => ({ ...prev, [name]: value }));
    };

    const handleProductSelect = (e) => {
        setErrors([]);
        const prodId = e.target.value;
        setSelectedProductId(prodId);
        if (prodId) {
            const selectedProduct = products.find(p => p.prodId === parseInt(prodId));
            if (selectedProduct) {
                setNewProduct({
                    prodName: selectedProduct.prodName,
                    cateId: selectedProduct.cateId || "",
                    suppId: selectedProduct.suppId || "",
                    description: selectedProduct.description || "",
                    vat: selectedProduct.vat?.toString() || "",
                    status: selectedProduct.status || 1,
                });
                setNewProductTypes([]);
            }
        } else {
            setSelectedProductId(null);
            setNewProduct({ prodName: "", cateId: "", suppId: "", description: "", vat: "", status: 1 });
            setNewProductTypes([]);
            setNewProductType({ prodTypeName: "", prodTypePrice: "", unit: "", unitPrice: "", status: 1 });
            setNewProductTypeBranches([]);
            setNewBranch({ brchId: "", quantity: "", status: 1 });
            setNewProductTypeAttributes([]);
            setNewAttribute({ atbId: "", prodAtbValue: "", atbDescription: "" });
            setNewProductTypeVariations([]);
            setNewVariation({ variId: "", prodTypeValue: "" });
            setNewAttributeName("");
            setNewVariationName("");
            setImageFiles([]);
            setImagePreviews([]);
        }
    };

    const handleAddBranch = () => {
        if (newBranch.brchId && newBranch.quantity) {
            setNewProductTypeBranches((prev) => [
                ...prev,
                { brchId: parseInt(newBranch.brchId), quantity: parseInt(newBranch.quantity), status: newBranch.status },
            ]);
            setNewBranch({ brchId: "", quantity: "", status: 1 });
        } else {
            showNotification("Please select a branch and enter a quantity.", 3000, 'fail');
        }
    };

    const handleAddAttribute = () => {
        if (!newAttribute.atbId) {
            showNotification("Please select an attribute.", 3000, 'fail');
            return;
        }

        if (newAttribute.prodAtbValue == null || !newAttribute.prodAtbValue.trim()) {
            showNotification("Must enter attribute value.", 3000, 'fail');
            return;
        }

        const atbId = parseInt(newAttribute.atbId);

        let attributesToCheck = newProductTypeAttributes;

        if (selectedEditProductType && selectedEditProductType.prodTypeId) {
            const existingAttributes = productTypeAttribute
                .filter((attr) => attr.prodTypeId.prodTypeId === selectedEditProductType.prodTypeId)
                .map((attr) => ({
                    atbId: attr.atbId?.atbId || attr.atbId,
                    prodAtbValue: attr.prodAtbValue,
                    atbDescription: attr.atbDescription,
                }));
            attributesToCheck = [...existingAttributes, ...newProductTypeAttributes];
        }

        const isDuplicateAttribute = attributesToCheck.some((a) => a.atbId === atbId);

        if (isDuplicateAttribute) {
            showNotification("This attribute is already added to this product type.", 3000, 'fail');
            return;
        }

        const newAttributeData = {
            atbId,
            prodAtbValue: newAttribute.prodAtbValue || "",
            atbDescription: newAttribute.atbDescription || "",
        };

        setNewProductTypeAttributes((prev) => [...prev, newAttributeData]);
        setNewAttribute({ atbId: "", prodAtbValue: "", atbDescription: "" });
        showNotification("Attribute added successfully!", 3000, 'complete');
    };

    const handleAddVariation = () => {
        console.log(selectedEditProductType);

        if (!newVariation.variId) {
            showNotification("Please select a variation.", 3000, 'fail');
            return;
        }

        if (!newVariation.prodTypeValue.trim()) {
            showNotification("Please enter a variation value.", 3000, 'fail');
            return;
        }

        const variId = parseInt(newVariation.variId);

        let variationsToCheck = newProductTypeVariations;

        if (selectedEditProductType && selectedEditProductType.prodTypeId) {
            const existingVariations = productTypeVariation
                .filter((vari) => vari.prodTypeId.prodTypeId === selectedEditProductType.prodTypeId)
                .map((v) => ({
                    variId: v.variId?.variId,
                    prodTypeValue: v.prodTypeValue,
                    defaultVari: v.defaultVari,
                }));
            variationsToCheck = [...existingVariations, ...newProductTypeVariations];
        }

        const isDuplicateValue = variationsToCheck.some(
            (v) =>
                v.variId === variId &&
                v.prodTypeValue.toLowerCase() === newVariation.prodTypeValue.toLowerCase()
        );

        if (isDuplicateValue) {
            showNotification(`Variation value "${newVariation.prodTypeValue}" already exists for this variation!`, 3000, 'fail');
            return;
        }

        const hasDefault = variationsToCheck.some(
            (v) => v.variId === variId && v.defaultVari === 1
        );

        const newVariationData = {
            variId,
            prodTypeValue: newVariation.prodTypeValue,
            defaultVari: hasDefault ? 0 : 1,
        };

        setNewProductTypeVariations((prev) => [...prev, newVariationData]);
        setNewVariation({ variId: "", prodTypeValue: "", defaultVari: null });
        showNotification(`Variation "${newVariation.prodTypeValue}" added${hasDefault ? "" : " as default"}!`, 3000, 'complete');
    };

    const handleCreateAttribute = async () => {
        if (!newAttributeName.trim()) {
            showNotification("Attribute name is required.", 3000, 'fail');
            return;
        }

        const isDuplicate = attributes.some(
            (attribute) => attribute.atbName.toLowerCase() === newAttributeName.toLowerCase()
        );

        if (isDuplicate) {
            showNotification("Attribute name already exists!", 3000, 'fail');
            return;
        }

        const payload = {
            atbName: newAttributeName
        };

        try {
            const response = await createAttribute(payload);
            setAttributes((prev) => [...prev, response.result]);
            setNewAttributeName("");
            showNotification("Attribute created successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error creating attribute:", error);
            showNotification("Failed to create attribute.", 3000, 'fail');
        }
        fetchAttributes();
    };

    const handleCreateVariation = async () => {
        if (newVariationName) {
            const isDuplicate = variations.some(
                (variation) => variation.variName.toLowerCase() === newVariationName.toLowerCase()
            );

            if (isDuplicate) {
                showNotification("Variation already exists!", 3000, 'fail');
                return;
            }

            try {
                const payload = {
                    variName: newVariationName
                };
                console.log(newVariationName);
                const response = await createVariation(payload);
                setVariations((prev) => [...prev, response.result]);
                setNewVariationName("");
                showNotification("Variation created successfully!", 3000, 'complete');
            } catch (error) {
                console.error("Error creating variation:", error);
                showNotification("Failed to create variation.", 3000, 'fail');
            }
            fetchVariations(); // Cập nhật lại danh sách variations từ server
        }
    };

    const handleSearchSubmit = (e) => {
        e.preventDefault();
        setCurrentPage(0);
        fetchProducts(0, searchTerm);
    };

    const refreshProducts = async () => {
        setIsLoadingProducts(true);
        try {
            let newProducts;
            if (isSearching) {
                newProducts = await handleSearch(currentPage);
            } else {
                const response = await axios.get(
                    `http://localhost:6789/api/v1/products?page=${currentPage}&size=${pageSize}&status=1,0`, {
                    withCredentials: true
                }
                );
                newProducts = Array.isArray(response.data.content) ? response.data.content : [];
            }

            setProducts((prev) => {
                return prev.map((prevItem) => {
                    const updatedItem = newProducts.find(
                        (newItem) => newItem.prodId === prevItem.prodId
                    );
                    return updatedItem ? { ...prevItem, ...updatedItem } : prevItem;
                });
            });
        } catch (error) {
            console.error("Error refreshing products:", error);
            setProducts([]);
        } finally {
            setIsLoadingProducts(false);
        }
    };

    const handleAddProductType = async () => {
        console.log("Đã nhấn");
        console.log("Test", selectedProductId)
        let newErrors = {};
        if (!newProduct.prodName.trim()) newErrors.prodName = "Product name cannot be empty";
        else if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]*$/.test(newProduct.prodName)) newErrors.prodName = "Product name can only contain letters and numbers";
        if (!newErrors.prodName) {
            if (selectedProductId == null) {
                try {
                    const response = await axios.get(
                        `http://localhost:6789/api/v1/products/search?keyword=${newProduct.prodName}&page=0&size=10`,
                        { withCredentials: true }
                    );
                    console.log("Đã fetch");
                    const data = response.data;
                    if (data.success && data.content.length > 0) {
                        const duplicateProduct = data.content.find(
                            (product) => product.prodName.toLowerCase() === newProduct.prodName.toLowerCase() && product.status !== 3
                        );
                        if (duplicateProduct) {
                            newErrors.prodName = "Product name already exists";
                            showNotification("Product name already exists.", 3000, 'fail');
                        }
                    }
                } catch (error) {
                    console.error("Error checking for duplicate product name:", error);
                    newErrors.prodName = "Error checking product name. Please try again.";
                }
            }
        }
        if (!newProduct.cateId) newErrors.cateId = "Please select a category";
        if (!newProduct.suppId) newErrors.suppId = "Please select a supplier";
        if (!newProduct.vat.trim()) newErrors.vat = "VAT cannot be empty";
        else if (isNaN(newProduct.vat) || parseFloat(newProduct.vat) < 0) newErrors.vat = "VAT must be a non-negative number";
        if (!newProduct.description.trim()) newErrors.description = "Description cannot be empty";

        if (!newProductType.prodTypeName.trim()) {
            newErrors.prodTypeName = "Product type name cannot be empty";
        } else if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]*$/.test(newProductType.prodTypeName)) {
            newErrors.prodTypeName = "Product type name can only contain letters and numbers";
        } else {
            try {
                const response = await axios.get(
                    `http://localhost:6789/api/v1/products/productTypes/search?keyword=${newProductType.prodTypeName}&page=0&size=10`,
                    { withCredentials: true }
                );
                const data = response.data.data;
                if (data.length > 0) {
                    const duplicateProductType = data.find(
                        (productType) => productType.prodTypeName.toLowerCase() === newProductType.prodTypeName.toLowerCase() && productType.status !== 3
                    );
                    if (duplicateProductType) {
                        newErrors.prodTypeName = "Product type name already exists";
                        showNotification("Product type name already exists.", 3000, 'fail');
                    }
                }
            } catch (error) {
                console.error("Error checking for duplicate product type name:", error);
                newErrors.prodTypeName = "Error checking product type name. Please try again.";
            }
        }
        if (!newProductType.prodTypePrice.trim()) newErrors.prodTypePrice = "Product type price cannot be empty";
        else if (isNaN(newProductType.prodTypePrice) || parseFloat(newProductType.prodTypePrice) < 0) newErrors.prodTypePrice = "Product type price must be a non-negative number";
        if (!newProductType.unit.trim()) newErrors.unit = "Unit cannot be empty";
        if (!newProductType.unitPrice.trim()) newErrors.unitPrice = "Unit price cannot be empty";
        else if (isNaN(newProductType.unitPrice) || parseFloat(newProductType.unitPrice) < 0) newErrors.unitPrice = "Unit price must be a non-negative number";

        if (newProductTypeAttributes.length === 0) {
            newErrors.atbId = "Please add at least one attribute for the product type.";
        }
        if (newProductTypeVariations.length === 0) {
            newErrors.variId = "Please add at least one variation for the product type.";
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        // Validate ProductTypeBranches


        for (const branch of newProductTypeBranches) {
            if (!branch.brchId || isNaN(parseInt(branch.brchId))) {
                showNotification("Please ensure all branches have a valid branch ID.", 3000, 'fail');
                return;
            }

        }

        for (const attr of newProductTypeAttributes) {
            if (!attr.atbId || isNaN(parseInt(attr.atbId))) {
                showNotification("Please ensure all attributes have a valid attribute ID.", 3000, 'fail');
                return;
            }
        }

        // Validate ProductTypeVariations (optional, but check if any are added)
        for (const varItem of newProductTypeVariations) {
            if (!varItem.variId || isNaN(parseInt(varItem.variId))) {
                showNotification("Please ensure all variations have a valid variation ID.", 3000, 'fail');
                return;
            }
        }

        // Temporarily store the ProductType and its related entities in newProductTypes
        const tempProductType = {
            ...newProductType,
            prodTypePrice: parseFloat(newProductType.prodTypePrice) || 0,
            unitPrice: parseFloat(newProductType.unitPrice) || 0,
            status: newProductType.status || 1,
            branches: [...newProductTypeBranches],
            attributes: [...newProductTypeAttributes],
            variations: [...newProductTypeVariations],
            images: [...newImageProductTypes]
        };

        setNewProductTypes((prev) => [...prev, tempProductType]);

        // Clear all fields except Product and selected product info
        setNewProductType({ prodTypeName: "", prodTypePrice: "", unit: "", unitPrice: "", status: 1 });
        setNewProductTypeBranches([]);
        setNewBranch({ brchId: "", quantity: "", status: 1 });
        setNewProductTypeAttributes([]);
        setNewAttribute({ atbId: "", prodAtbValue: "", atbDescription: "" });
        setNewProductTypeVariations([]);
        setNewImageProductTypes([]);
        setImagePreviews([]);
        setNewVariation({ variId: "", prodTypeValue: "" });
        showNotification("ProductType and related entities added temporarily!", 3000, 'complete');
        console.log("Vừa lưu một productType tạm thời : ", tempProductType)
    };

    const handleSubmitFinal = async (e) => {
        e.preventDefault();
        console.log("handleSubmitFinal triggered");
        let newErrors = {};

        if (!newProduct.prodName.trim()) newErrors.prodName = "Product name cannot be empty";
        else if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]*$/.test(newProduct.prodName)) newErrors.prodName = "Product name can only contain letters and numbers";

        if (!newProduct.cateId) newErrors.cateId = "Please select a category";
        if (!newProduct.suppId) newErrors.suppId = "Please select a supplier";
        if (!newProduct.vat.trim()) newErrors.vat = "VAT cannot be empty";
        else if (isNaN(newProduct.vat) || parseFloat(newProduct.vat) < 0) newErrors.vat = "VAT must be a non-negative number";

        if (newProductType.prodTypeName && (!newProductType.prodTypePrice || !newProductType.unit || !newProductType.unitPrice)) {
            if (!newProductType.prodTypeName.trim()) newErrors.prodTypeName = "Product type name cannot be empty";
            else if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]*$/.test(newProductType.prodTypeName)) newErrors.prodTypeName = "Product type name can only contain letters and numbers";
            if (!newProductType.prodTypePrice.trim()) newErrors.prodTypePrice = "Product type price cannot be empty";
            else if (isNaN(newProductType.prodTypePrice) || parseFloat(newProductType.prodTypePrice) < 0) newErrors.prodTypePrice = "Product type price must be a non-negative number";
            if (!newProductType.unit.trim()) newErrors.unit = "Unit cannot be empty";
            if (!newProductType.unitPrice.trim()) newErrors.unitPrice = "Unit price cannot be empty";
            else if (isNaN(newProductType.unitPrice) || parseFloat(newProductType.unitPrice) < 0) newErrors.unitPrice = "Unit price must be a non-negative number";
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        if (newProductTypes.length === 0) {
            showNotification("Please add at least one ProductType before submitting.", 3000, 'fail');
            return;
        }

        let prodId = selectedProductId ? parseInt(selectedProductId) : null;
        console.log("Selected Product ID:", prodId);
        const confirmed = await showConfirm(
            `Are you sure you want to save?`,
            'create'
        );
        if (!confirmed) return;
        setIsLoadingSaveProduct(true);
        try {
            if (!prodId) {
                if (!newProduct.prodName || newProduct.prodName.trim() === "") {
                    showNotification("Please enter a product name.", 3000, 'fail');
                    return;
                }
                if (!/^[a-zA-Z0-9\s\u00C0-\u1EF9]*$/.test(newProduct.prodName)) {
                    setErrors(prev => ({ ...prev, prodName: "Product name can only contain letters and numbers" }));
                    return;
                }
                if (!newProduct.cateId || isNaN(parseInt(newProduct.cateId))) {
                    showNotification("Please select a valid category.", 3000, 'fail');
                    return;
                }
                if (!newProduct.suppId || isNaN(parseInt(newProduct.suppId))) {
                    showNotification("Please select a valid supplier.", 3000, 'fail');
                    return;
                }

                const productPayload = {
                    ...newProduct,
                    cateId: parseInt(newProduct.cateId),
                    suppId: parseInt(newProduct.suppId),
                    vat: parseFloat(newProduct.vat) || 0,
                    description: newProduct.description || "",
                };
                console.log("Creating new product with payload:", productPayload); // Debug

                const productResponse = await createProduct(productPayload);
                prodId = productResponse.prodId;
                console.log("New Product created with ID:", prodId); // Debug
                if (!prodId) throw new Error("Product creation failed: No prodId returned.");
            } else {
                console.log("Using existing Product with ID:", prodId); // Debug
            }

            // Process ProductTypes
            console.log("Processing ProductTypes:", newProductTypes); // Debug
            for (const productType of newProductTypes) {
                const productTypePayload = {
                    ...productType,
                    prodId,
                    prodTypePrice: parseFloat(productType.prodTypePrice) || 0,
                    unitPrice: parseFloat(productType.unitPrice) || 0,
                };
                console.log("Sending ProductType payload:", productTypePayload); // Debug

                const productTypeResponse = await createProductType(productTypePayload);
                const prodTypeId = productTypeResponse.prodTypeId;
                console.log("ProductType created with ID:", prodTypeId); // Debug

                // Branches
                for (const branch of productType.branches || []) {
                    const branchPayload = {
                        brchId: parseInt(branch.brchId),
                        quantity: parseInt(branch.quantity),
                        prodTypeId,
                        status: branch.status || 1,
                    };
                    console.log("Creating branch:", branchPayload); // Debug
                    await createProductTypeBranch(branchPayload);
                }

                // Attributes
                for (const attribute of productType.attributes || []) {
                    const attributePayload = {
                        atbId: parseInt(attribute.atbId),
                        prodAtbValue: attribute.prodAtbValue || "",
                        atbDescription: attribute.atbDescription || "",
                        prodTypeId,
                    };
                    console.log("Creating attribute:", attributePayload); // Debug
                    await createProductTypeAttribute(attributePayload);
                }

                // Variations
                for (const variation of productType.variations || []) {
                    const variationPayload = {
                        variId: parseInt(variation.variId),
                        prodTypeValue: variation.prodTypeValue || "",
                        prodTypeId,
                        defaultVari: variation.defaultVari,
                    };
                    console.log("Creating variation:", variationPayload); // Debug
                    await createProductTypeVariation(variationPayload);
                }

                // Images
                if (productType.images && productType.images.length > 0) {
                    const failedUploads = [];

                    for (const imageFile of productType.images) {
                        if (imageFile instanceof File) {
                            const formData = new FormData();
                            formData.append("file", imageFile);
                            formData.append("productTypeId", prodTypeId);
                            console.log("Uploading image for ProductType:", prodTypeId); // Debug

                            try {
                                await axios.post(
                                    "http://localhost:6789/api/v1/products/productType/createImage",
                                    formData,
                                    {
                                        headers: { "Content-Type": "multipart/form-data" },
                                        withCredentials: "true"
                                    }
                                );
                                console.log(`Successfully uploaded image: ${imageFile.name}`);
                            } catch (error) {
                                console.error(`Failed to upload image: ${imageFile.name}`, error.response?.data || error.message);
                                failedUploads.push(imageFile.name);
                            }
                        }
                    }

                    if (failedUploads.length > 0) {
                        showNotification(`Failed to upload images: ${failedUploads.join(", ")}`, 20000, 'fail');
                    }
                }
            }

            console.log("Submission successful"); // Debug
            handleCloseModal();
            setIsLoadingSaveProduct(false);
            showNotification("Product and all ProductTypes created/updated successfully!", 3000, 'complete');
            // await refreshProducts();
            await fetchProducts(0, searchTerm);
        } catch (error) {
            console.error("Error in handleSubmitFinal:", error); // Debug
            showNotification("Failed to submit: " + (error.response?.data?.message || error.message), 3000, 'fail');
        }
    };
    const handleDeleteBranch = async (branchId) => {
        const currentBranches = productTypeBranch.filter(
            b => b.prodTypeId.prodTypeId === selectedEditProductType?.prodTypeId
        );
        const totalBranches = currentBranches.length + newProductTypeBranches.length;

        const confirmed = await showConfirm(
            `Are you sure you want to delete this branch ?`,
            'fail'
        );
        if (!confirmed) return;

        if (totalBranches <= 1) {
            showNotification("Cannot delete the last branch. At least one branch must remain.", 3000, 'fail');
            return;
        }

        try {
            const response = await fetch(
                `http://localhost:6789/api/v1/products/deleteProductTypeBranch/${branchId}`,
                {
                    method: "DELETE",
                    credentials: "include",
                }
            );

            if (response.ok) {
                setProductTypeBranch(prev => prev.filter(branch => branch.prodTypeBrchId !== branchId));
                showNotification("Branch deleted successfully!", 3000, 'complete');
            } else {
                showNotification("Failed to delete branch.", 3000, 'fail');
            }
        } catch (error) {
            console.error("Error deleting branch:", error);
            showNotification("An error occurred while deleting the branch.", 3000, 'fail');
        }
    };
    const handleDeleteAttribute = async (attributeId) => {
        const currentAttributes = productTypeAttribute.filter(
            (a) => a.prodTypeId.prodTypeId === selectedEditProductType?.prodTypeId
        );
        const totalAttributes = currentAttributes.length + newProductTypeAttributes.length;

        if (totalAttributes <= 1) {
            showNotification("Cannot delete the last attribute. At least one attribute must remain.", 3000, 'fail');
            return;
        }

        const confirmed = await showConfirm(
            `Are you sure you want to delete this attribute ?`,
            'fail'
        );
        if (!confirmed) return;

        try {
            const response = await fetch(
                `http://localhost:6789/api/v1/products/deleteProductTypeAttribute/${attributeId}`,
                {
                    method: "DELETE",
                    credentials: "include",
                }
            );

            if (!response.ok) {
                throw new Error("Failed to delete attribute");
            }

            if (selectedEditProductType?.prodTypeId) {
                handleEditProductTypeSelect(selectedEditProductType.prodTypeId);
            }
            showNotification("Attribute deleted successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error deleting attribute:", error);
            showNotification(error.message || "An error occurred while deleting the attribute.", 3000, 'fail');
        }
    };

    const handleDeleteVariation = async (variationId) => {
        const currentVariations = productTypeVariation.filter(
            (v) => v.prodTypeId.prodTypeId === selectedEditProductType?.prodTypeId
        );
        const totalVariations = currentVariations.length + newProductTypeVariations.length;

        if (totalVariations <= 1) {
            showNotification("Cannot delete the last variation. At least one variation must remain.", 3000, 'fail');
            return;
        }

        const confirmed = await showConfirm(
            `Are you sure you want to delete this variation ?`,
            'fail'
        );
        if (!confirmed) return;


        try {
            const response = await fetch(
                `http://localhost:6789/api/v1/products/deleteProductTypeVariation/${variationId}`,
                {
                    method: "DELETE",
                    credentials: "include",
                }
            );

            if (!response.ok) {
                throw new Error("Failed to delete variation");
            }

            // Sau khi xóa thành công, gọi lại handleEditProductTypeSelect để refresh
            if (selectedEditProductType?.prodTypeId) {
                handleEditProductTypeSelect(selectedEditProductType.prodTypeId);
            }
            showNotification("Variation deleted successfully!", 3000, 'complete');
        } catch (error) {
            console.error("Error deleting variation:", error);
            showNotification(error.message || "An error occurred while deleting the variation.", 3000, 'fail');
        }
    };
    return (
        <div className="bg-white p-2 shadow-md rounded-lg animate-neonTable relative ">
            <div className="flex justify-between mb-4">
                <div className="flex items-center gap-2">
                    <Button variant="default" className="bg-blue-500 text-white hover:bg-blue-600" onClick={handleCreateClick}>
                        Create <Plus className="h-4 w-4 mr-2" />
                    </Button>
                </div>
                <form onSubmit={handleSearchSubmit} className="flex items-center gap-2">
                    <input
                        type="text"
                        value={searchTerm}
                        onChange={handleSearchChange}
                        placeholder="Search by product name..."
                        className="border border-gray-300 rounded-md p-2 w-64 "
                    />
                </form>
            </div>
            {isModalOpen && (
                <CreateProductModal
                    handleCloseModal={handleCloseModal}
                    handleAddProductType={handleAddProductType}
                    handleSubmitFinal={handleSubmitFinal}
                    selectedProductId={selectedProductId}
                    handleProductSelect={handleProductSelect}
                    isLoadingProducts={isLoadingProducts}
                    products={products}
                    newProduct={newProduct}
                    handleInputChange={handleInputChange}
                    setNewProduct={setNewProduct}
                    isLoadingCategories={isLoadingCategories}
                    categories={categories}
                    isLoadingSuppliers={isLoadingSuppliers}
                    suppliers={suppliers}
                    setNewProductType={setNewProductType}
                    newBranch={newBranch}
                    setNewBranch={setNewBranch}
                    isLoadingBranches={isLoadingBranches}
                    branches={branches}
                    handleAddBranch={handleAddBranch}
                    newProductTypeBranches={newProductTypeBranches}
                    newAttribute={newAttribute}
                    setNewAttribute={setNewAttribute}
                    isLoadingAttributes={isLoadingAttributes}
                    attributes={attributes}
                    handleAddAttribute={handleAddAttribute}
                    newAttributeName={newAttributeName}
                    setNewAttributeName={setNewAttributeName}
                    handleCreateAttribute={handleCreateAttribute}
                    newProductTypeAttributes={newProductTypeAttributes}
                    newVariation={newVariation}
                    setNewVariation={setNewVariation}
                    isLoadingVariations={isLoadingVariations}
                    variations={variations}
                    handleAddVariation={handleAddVariation}
                    newVariationName={newVariationName}
                    setNewVariationName={setNewVariationName}
                    handleCreateVariation={handleCreateVariation}
                    newProductTypeVariations={newProductTypeVariations}
                    handleImageChange={handleImageChange}
                    imagePreviews={imagePreviews}
                    handleRemoveImage={handleRemoveImage}
                    newProductTypes={newProductTypes}
                    newProductType={newProductType}
                    setNewProductTypeBranches={setNewProductTypeBranches}
                    setNewProductTypeAttributes={setNewProductTypeAttributes}
                    setNewProductTypeVariations={setNewProductTypeVariations}
                    setImagePreviews={setImagePreviews}
                    errors={errors}
                    setAttributes={setAttributes}
                    setVariations={setVariations}
                    setIsLoadingSaveProduct={setIsLoadingSaveProduct}
                    isLoadingSaveProduct={isLoadingSaveProduct}
                />
            )}
            {isEditModalOpen && editProduct && (
                <div>
                    <ProductEditModal
                        editProduct={editProduct}
                        editProductTypes={editProductTypes}
                        productTypeBranch={productTypeBranch}
                        productTypeAttribute={productTypeAttribute}
                        productTypeVariation={productTypeVariation}
                        imagePreviews={imagePreviews}
                        handleCloseEditModal={handleCloseEditModal}
                        handleUpdateProductType={handleUpdateProductType}
                        handleEditSubmitFinal={handleEditSubmitFinal}
                        handleEditProductTypeSelect={handleEditProductTypeSelect}
                        selectedEditProductType={selectedEditProductType}
                        newProduct={newProduct}
                        setNewProduct={setNewProduct}
                        newProductType={newProductType}
                        setNewProductType={setNewProductType}
                        newBranch={newBranch}
                        setNewBranch={setNewBranch}
                        newAttribute={newAttribute}
                        setNewAttribute={setNewAttribute}
                        newVariation={newVariation}
                        setNewVariation={setNewVariation}
                        newProductTypes={newProductTypes}
                        newProductTypeBranches={newProductTypeBranches}
                        newProductTypeAttributes={newProductTypeAttributes}
                        newProductTypeVariations={newProductTypeVariations}
                        handleInputChange={handleInputChange}
                        handleImageChange={handleImageChange}
                        handleRemoveImage={handleRemoveImage}
                        handleAddBranch={handleAddBranch}
                        handleAddAttribute={handleAddAttribute}
                        handleAddVariation={handleAddVariation}
                        handleUpdateBranch={handleUpdateBranch}
                        handleDeleteBranch={handleDeleteBranch}
                        handleUpdateAttribute={handleUpdateAttribute}
                        handleDeleteAttribute={handleDeleteAttribute}
                        handleUpdateVariation={handleUpdateVariation}
                        handleDeleteVariation={handleDeleteVariation}
                        handleEditBranchQuantity={handleEditBranchQuantity}
                        editedBranchQuantities={editedBranchQuantities}
                        handleEditAttributeValue={handleEditAttributeValue}
                        handleEditAttributeDescription={handleEditAttributeDescription}
                        editedAttributeValues={editedAttributeValues}
                        editedAttributeDescriptions={editedAttributeDescriptions}
                        handleEditVariationValue={handleEditVariationValue}
                        editedVariationValues={editedVariationValues}
                        handleUnactiveProductType={handleUnactiveProductType}
                        categories={categories}
                        suppliers={suppliers}
                        branches={branches}
                        attributes={attributes}
                        variations={variations}
                        errors={errors}
                        handleSoftDeleteProductType={handleSoftDeleteProductType}
                        setIsLoadingSaveProduct={setIsLoadingSaveProduct}
                        isLoadingSaveProduct={isLoadingSaveProduct}
                    />
                </div>
            )}
            {/* View Modal */}
            {isViewModalOpen && selectedProduct && (
                <div>
                    <ProductViewModal
                        selectedProduct={selectedProduct}
                        productTypes={productTypeOfProduct}
                        productTypeBranch={productTypeBranchOfProduct}
                        productTypeAttribute={productTypeAttributeOfProduct}
                        productTypeVariation={productTypeVariationOfProduct}
                        imagePreviews={imagePreviews}
                        isLoadingProductTypes={isLoadingProductTypes}
                        handleProductTypeSelect={handleProductTypeSelect}
                        handleCloseViewModal={handleCloseViewModal}
                        selectedProductType={selectedProductType}
                        onClose={handleCloseViewModal}
                    />
                </div>
            )}
            {/* Table rendering remains unchanged */}
            {products && Array.isArray(products) ? (
                <Table className="w-full min-w-full">
                    <TableHeader>
                        <TableRow className="bg-blue-500">
                            <TableHead className="border p-2 text-white font-bold text-center">Product Name</TableHead>
                            <TableHead className="border p-2 text-white font-bold text-center">Category</TableHead>
                            <TableHead className="border p-2 text-white font-bold text-center">Supplier</TableHead>
                            <TableHead className="border p-2 text-white font-bold text-center">Description</TableHead>
                            <TableHead className="border p-2 text-white font-bold text-center">VAT</TableHead>
                            <TableHead className="border p-2 text-white font-bold text-center">Status</TableHead>
                            <TableHead className="border p-2 text-white font-bold text-center">Action</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {products.map((product, index) => (
                            <TableRow key={index} className="text-center ">
                                <TableCell className="border p-2 text-zinc-950">{product.prodName}</TableCell>
                                <TableCell className="border p-2 text-zinc-950">{product.cateId?.cateName || "N/A"}</TableCell>
                                <TableCell className="border p-2 text-zinc-950">{product.suppId?.suppName || "N/A"}</TableCell>
                                <TableCell className="border p-2 text-zinc-950">{product.description}</TableCell>
                                <TableCell className="border p-2 text-zinc-950">{product.vat}</TableCell>
                                <TableCell className={`border p-2 ${product.status === 1 ? "text-green-500 font-bold" :
                                    product.status === 0 ? "text-red-500 font-medium" :
                                        "text-gray-500 font-medium"
                                    }`}>
                                    {product.status === 1 ? "Available" :
                                        product.status === 0 ? "Unavailable" :
                                            "Inactive"}
                                </TableCell>
                                <TableCell className="border p-2">
                                    <div className="flex justify-center gap-2">
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            onClick={() => handleView(product)} // Triggers the view modal
                                            className="text-blue-600 hover:text-blue-800 border-blue-200 hover:border-blue-500"
                                        >
                                            <Eye className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            onClick={() => {
                                                console.log("Editing product:", product);
                                                if (!product || !product.prodId) {
                                                    console.error("Invalid product:", product);
                                                    showNotification("Cannot edit: Product data is invalid.");
                                                    return;
                                                }
                                                handleEdit(product);
                                            }}
                                            className="text-yellow-600 hover:text-yellow-800 border-yellow-200 hover:border-yellow-500"
                                        >
                                            <Edit className="h-4 w-4" />
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            onClick={(e) => handleDelete(product, e)}
                                            className={
                                                product.status === 1
                                                    ? "text-green-600 hover:text-green-800 border-green-200 hover:border-green-500"
                                                    : "text-red-600 hover:text-red-800 border-red-200 hover:border-red-500"
                                            }
                                        >
                                            {product.status === 1 ? (
                                                <ToggleRight className="h-4 w-4" />
                                            ) : (
                                                <ToggleLeft className="h-4 w-4" />
                                            )}
                                        </Button>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            onClick={(e) => softDeleteProduct(product, e)}
                                            className={"text-red-600 hover:text-red-800 border-red-200 hover:border-red-500"}
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </div>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            ) : (
                <p className="text-lg text-gray-700 p-4 rounded-lg bg-amber-100">No products available or error loading data.</p>
            )}
            <div className="flex justify-end items-center mt-4">
                <div className="flex items-center gap-2 whitespace-nowrap">
                    <label htmlFor="pageSize" className="text-sm">
                        Items per page:
                    </label>
                    <select
                        id="pageSize"
                        value={pageSize}
                        onChange={handlePageSizeChange}
                        className="border p-1 rounded"
                    >
                        <option value={5}>5</option>
                        <option value={10}>10</option>
                        <option value={15}>15</option>
                        <option value={20}>20</option>
                    </select>
                </div>
            </div>
            <div className="flex justify-center items-center gap-2 mt-4">
                {renderPagination()}
            </div>

        </div>
    );
};

export default ProductTable;