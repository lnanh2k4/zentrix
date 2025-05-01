import apiService from "@/services/ApiService"
//Endpoint chung
const AUTH_ENDPOINT = `/auth`
//Endpoint riêng
const USER_ENDPOINT = `/users`
const STAFF_ENDPOINT = `/staffs`
const ROLE_ENDPOINT = `${USER_ENDPOINT}/roles`
const BRANCH_ENDPOINT = `/branches`
const REGISTER_ENDPOINT = `${AUTH_ENDPOINT}/sign-up`
const LOGIN_ENDPOINT = `${AUTH_ENDPOINT}/sign-in`
const LOGOUT_ENDPOINT = `${AUTH_ENDPOINT}/sign-out`
const CHANGE_PASSWORD_ENDPOINT = `${AUTH_ENDPOINT}/password/change`
const INFO_ENDPOINT = `${AUTH_ENDPOINT}/info`
const PRODUCT_ENDPOINT = `/products`
const ATTRIBUTE_ENDPOINT = `/attributes`
const VARIATION_ENDPOINT = `/variations`
// CUSTOMER ENDPOINT
export const getAllCustomer = async (page = 0, size = 10, keyword = "") => {
    const params = { page, size };
    if (keyword) {
        params.keyword = keyword;
        return apiService.get(`${USER_ENDPOINT}/search`, { params });
    }
    return apiService.get(USER_ENDPOINT, { params });
};

export const createCustomer = async (data) => {
    return apiService.post(USER_ENDPOINT, data)
}

export const editCustomer = async (data) => {
    return apiService.put(USER_ENDPOINT, data)
}
export const lockCustomer = async (userId) => {
    return apiService.patch(`${USER_ENDPOINT}/${userId}`, { status: "LOCKED" })
}
export const deleteCustomer = async (userId) => {
    return apiService.delete(`${USER_ENDPOINT}/${userId}`)
}

// STAFF ENDPOINT
export const getAllStaffs = async (page = 0, size = 10, keyword = "") => {
    const params = { page, size };
    if (keyword) {
        params.keyword = keyword;
        console.log("Param", params)
        return apiService.get(`${STAFF_ENDPOINT}/search`, { params });
    }
    return apiService.get(STAFF_ENDPOINT, { params });
};

export const getStaffByUsername = async (username) => {
    return apiService.get(`${STAFF_ENDPOINT}/username/${username}`)
}

export const createStaff = async (data) => {
    return apiService.post(`${STAFF_ENDPOINT}/`, data)
}
export const editStaff = async (data) => {
    return apiService.put(STAFF_ENDPOINT, data)
}
export const lockStaff = async (staffId) => {
    return apiService.patch(`${STAFF_ENDPOINT}/${staffId}`, { status: "LOCKED" })
}
export const deleteStaff = async (staffId) => {
    return apiService.delete(`${STAFF_ENDPOINT}/${staffId}`)
}
// AUTHORIZATION ENDPOINT
export const register = async (data) => {
    return apiService.post(REGISTER_ENDPOINT, data, {
        headers: { "Content-Type": "application/json" },
    })
}
export const login = async (data) => {
    return apiService.post(LOGIN_ENDPOINT, data)
}

export const logout = async () => {
    return apiService.post(LOGOUT_ENDPOINT)
}

export const changePassword = async (userId, currentPassword, newPassword) => {
    return apiService.patch(`${CHANGE_PASSWORD_ENDPOINT}/${userId}`, {
        currentPassword: currentPassword,
        newPassword: newPassword
    });
}

export const editProfile = async (data, config) => {
    return apiService.put(USER_ENDPOINT, data, config)
}

// INFORMATION ENDPOINT
export const getInfo = async () => {
    return apiService.get(INFO_ENDPOINT)
}

// ROLE ENDPOINT
export const getAllRoles = async () => {
    return apiService.get(ROLE_ENDPOINT);
};
// BRANCH ENDPOINT
export const getAllBranches = async () => {
    return apiService.get(`${BRANCH_ENDPOINT}/all`);
};
// PRODUCT ENDPOINT
//----get
export const getAllProducts = async (page = 0, size = 10) => {
    const params = { page, size };
    params.status = "1,0";
    return apiService.get(PRODUCT_ENDPOINT, { params });
};
export const searchProducts = async (keyword, page = 0, size = 10) => {
    const params = { keyword, page, size };
    return apiService.get(`${PRODUCT_ENDPOINT}/search`, { params });
};
export const getProductTypes = async (id) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/${id}/productTypes`);
};
export const getProduct = async (id) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/${id}`);
};
export const getProductByProductTypeId = async (id) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/product/searchBy/${id}`);
};
export const getProductTypeBranchs = async (id) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/productTypeBranchs`);
};
export const getAllProductTypeAttribute = async () => {
    return apiService.get(`${PRODUCT_ENDPOINT}/productTypeAttributes`);
};
export const getAllProductTypeVariation = async () => {
    return apiService.get(`${PRODUCT_ENDPOINT}/productTypeVariations`);
};
export const getAllProductTypeBranchByProdTypeId = async (prodTypeId) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/productTypeBranch/${prodTypeId}`);
};
export const getAllProductTypeAttributeByProdTypeId = async (prodTypeId) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/productTypeAttribute/${prodTypeId}`);
};
export const getAllProductTypeVariationByProdTypeId = async (prodTypeId) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/productTypeVariation/${prodTypeId}`);
};
//----update
export const updateProduct = async (prodId, data) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/update/${prodId}`, data);
};
//----create
export const createProduct = async (data) => {
    return apiService.post(`${PRODUCT_ENDPOINT}/create`, data);
};
//----delete
export const unactiveProduct = async (prodId, data) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/unactive/${prodId}`, data);
};
export const deleteProduct = async (prodId, data) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/softDeleteProduct/${prodId}`, data);
};
export const deleteImage = async (id) => {
    return apiService.delete(`${PRODUCT_ENDPOINT}/productType/image/${id}`);
};
export const deleteProductTypeBranch = async (id) => {
    return apiService.delete(`${PRODUCT_ENDPOINT}/deleteProductTypeBranch/${id}`);
};
//PRODUCT TYPE ENDPOINT
//----get
export const getImageProductType = async (prodTypeId) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/ImageProduct/${prodTypeId}`);
};
export const getProductTypeById = async (prodTypeId) => {
    return apiService.get(`${PRODUCT_ENDPOINT}/productTypes/${prodTypeId}`);
};
//----update
export const updateProductType = async (prodTypeId, data) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/updateProductType/${prodTypeId}`, data);
};
export const updateProductTypeBranch = async (brchId, data) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/updateProductTypeBranch/${brchId}`, data);
};
export const updateProductTypeAttribute = async (attrId, data) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/updateProductTypeAttribute/${attrId}`, data);
};
export const updateProductTypeVariation = async (prodTypeVariId, data) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/updateProductTypeVariation/${prodTypeVariId}`, data);
};
//----create
export const createProductType = async (data) => {
    return apiService.post(`${PRODUCT_ENDPOINT}/createProductType`, data);
};
export const createProductTypeBranch = async (data) => {
    return apiService.post(`${PRODUCT_ENDPOINT}/createProductTypeBranch`, data);
};
export const createProductTypeAttribute = async (data) => {
    return apiService.post(`${PRODUCT_ENDPOINT}/createProductTypeAttribute`, data);
};
export const createProductTypeVariation = async (data) => {
    return apiService.post(`${PRODUCT_ENDPOINT}/createProductTypeVariation`, data);
};
//----delete
export const unactiveProductType = async (prodTypeId, data, id) => {
    return apiService.put(`${PRODUCT_ENDPOINT}/unactiveProductType/${prodTypeId}?status=${id}`, data);
};


//ATTRIBUTE ENDPOINT
//----get
export const getAllAttributes = async () => {
    return apiService.get(`${ATTRIBUTE_ENDPOINT}`);
};
//----create
export const createAttribute = async (data) => {
    return apiService.post(`${ATTRIBUTE_ENDPOINT}/createAttribute`, data);
};
//VARIATION ENDPOINT
//----get
export const getAllVariations = async () => {
    return apiService.get(`${VARIATION_ENDPOINT}`);
};
//----create
export const createVariation = async (data) => {
    return apiService.post(`${VARIATION_ENDPOINT}/createVariation`, data);
};
// MEMBERSHIP ENDPOINT
