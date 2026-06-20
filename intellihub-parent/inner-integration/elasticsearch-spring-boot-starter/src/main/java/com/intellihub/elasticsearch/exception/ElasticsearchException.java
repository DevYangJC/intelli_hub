package com.intellihub.elasticsearch.exception;

/**
 * Elasticsearch 统一异常
 *
 * @author IntelliHub
 */
public class ElasticsearchException extends RuntimeException {

    private final ErrorCode errorCode;

    public ElasticsearchException(String message) {
        super(message);
        this.errorCode = ErrorCode.UNKNOWN;
    }

    public ElasticsearchException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.UNKNOWN;
    }

    public ElasticsearchException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ElasticsearchException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * ES 错误码枚举
     */
    public enum ErrorCode {
        /** 未知错误 */
        UNKNOWN("ES_UNKNOWN", "未知错误"),
        /** 连接失败 */
        CONNECTION_FAILED("ES_CONN_FAILED", "ES连接失败"),
        /** 索引不存在 */
        INDEX_NOT_FOUND("ES_INDEX_NOT_FOUND", "索引不存在"),
        /** 索引已存在 */
        INDEX_ALREADY_EXISTS("ES_INDEX_EXISTS", "索引已存在"),
        /** 文档不存在 */
        DOCUMENT_NOT_FOUND("ES_DOC_NOT_FOUND", "文档不存在"),
        /** 查询语法错误 */
        QUERY_SYNTAX_ERROR("ES_QUERY_SYNTAX", "查询语法错误"),
        /** 映射错误 */
        MAPPING_ERROR("ES_MAPPING_ERROR", "映射错误"),
        /** 超时 */
        TIMEOUT("ES_TIMEOUT", "操作超时"),
        /** 批量操作部分失败 */
        BULK_PARTIAL_FAILURE("ES_BULK_PARTIAL", "批量操作部分失败");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
