package com.flipkart.foxtrot.core.exception;

/**
 * Created by rishabh.goyal on 19/12/15.
 */
public enum ErrorCode {

    TABLE_INITIALIZATION_ERROR,
    TABLE_METADATA_FETCH_FAILURE,
    TABLE_NOT_FOUND,
    TABLE_ALREADY_EXISTS,

    INVALID_REQUEST,
    DOCUMENT_NOT_FOUND,
    MALFORMED_QUERY,
    CARDINALITY_OVERFLOW,

    ACTION_RESOLUTION_FAILURE,
    UNRESOLVABLE_OPERATION,
    ACTION_EXECUTION_ERROR,

    STORE_CONNECTION_ERROR,
    STORE_EXECUTION_ERROR,
    DATA_CLEANUP_ERROR,

    EXECUTION_EXCEPTION,
    CONSOLE_SAVE_EXCEPTION,
    CONSOLE_FETCH_EXCEPTION
}
