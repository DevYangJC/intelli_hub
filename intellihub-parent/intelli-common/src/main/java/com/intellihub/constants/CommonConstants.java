package com.intellihub.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 通用常量定义
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class CommonConstants {

    private CommonConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 字符集 ====================
    public static final String CHARSET_UTF8_STR = StandardCharsets.UTF_8.displayName();
    public static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    // ==================== 响应消息 ====================
    public static final String SUCCESS = "success";
    public static final String SUCCESS_ZH = "操作成功";
    public static final String ERROR = "error";
    public static final String ERROR_ZH = "操作失败";

    // ==================== 通用错误码 ====================
    public static final Integer SUCCESS_CODE = 200;
    public static final Integer ERROR_CODE = 500;
    public static final Integer UNIVERSAL_ERROR_CODE = 9999;

    // ==================== 常用字符 ====================
    public static final String EMPTY = "";
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String HYPHEN = "-";
    public static final String UNDERSCORE = "_";
    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";
    public static final String COLON = ":";
    public static final String AMPERSAND = "&";
    public static final String EQUALS = "=";
    public static final String QUESTION = "?";
    public static final String AT = "@";
    public static final String HASH = "#";
    public static final String SPACE = " ";
    public static final String NEWLINE = "\n";
    public static final String TAB = "\t";

    // ==================== 文件扩展名 ====================
    public static final String EXT_TXT = "txt";
    public static final String EXT_JSON = "json";
    public static final String EXT_XML = "xml";
    public static final String EXT_CSV = "csv";
    public static final String EXT_XLSX = "xlsx";
    public static final String EXT_XLS = "xls";
    public static final String EXT_PDF = "pdf";
    public static final String EXT_ZIP = "zip";

    // ==================== HTTP 相关 ====================
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String BEARER_PREFIX = "Bearer ";

    // ==================== 布尔值 ====================
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final Integer INT_TRUE = 1;
    public static final Integer INT_FALSE = 0;
}
