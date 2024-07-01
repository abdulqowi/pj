package com.fp.ProductService.util;

public class AppConstant {
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILURE = "failure";

    public static final String STATUS_ERROR = "error";
    public static final String STATUS_ALREADY_EXISTS = "Already exists";
    public static final String STATUS_NOT_FOUND = "Not Found";

    public static final String CODE_OK = "200";
    public static final String CODE_CREATED = "201";
    public static final String CODE_NO_CONTENT = "204";
    public static final int STATUS_CODE_BAD_REQUEST = 400;
    public static final int STATUS_CODE_NOT_FOUND = 404;
    public static final String MESSAGE_PRODUCTS_RETRIEVED = "Products retrieved successfully";
    public static final String MESSAGE_PRODUCT_CREATED = "Product created successfully";
    public static final String MESSAGE_PRODUCT_DELETED = "Product deleted successfully";

    // Default values for pagination and sorting
    public static final String DEFAULT_SORT_BY = "updatedAt";
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "10";

    // Error Messages
    public static final String ERROR_VALIDATION = "Validation Error";
    public static final String ERROR_ALREADY_EXISTS = "Already exists";
    public static final String ERROR_NOT_FOUND = "Not Found";
    public static final String ERROR_UNAUTHORIZED = "Unauthorized";
    public static final String ERROR_BAD_CREDENTIALS = "Bad Credentials";
}
