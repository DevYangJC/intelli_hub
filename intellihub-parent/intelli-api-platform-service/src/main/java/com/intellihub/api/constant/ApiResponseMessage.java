package com.intellihub.api.constant;

/**
 * API响应消息常量类
 * 统一管理所有API返回的提示消息，避免硬编码
 *
 * @author intellihub
 * @since 1.0.0
 */
public final class ApiResponseMessage {

    private ApiResponseMessage() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static final String SUCCESS = "操作成功";
    public static final String CREATE_SUCCESS = "创建成功";
    public static final String UPDATE_SUCCESS = "更新成功";
    public static final String DELETE_SUCCESS = "删除成功";
    public static final String SAVE_SUCCESS = "保存成功";
    public static final String ADD_SUCCESS = "添加成功";
    public static final String PUBLISH_SUCCESS = "发布成功";
    public static final String OFFLINE_SUCCESS = "下线成功";
    public static final String DEPRECATE_SUCCESS = "废弃成功";
    public static final String COPY_SUCCESS = "复制成功";
    public static final String APPLY_SUCCESS = "应用成功";
    public static final String REMOVE_SUCCESS = "移除成功";
    public static final String ROLLBACK_SUCCESS = "回滚成功";
    public static final String VERSION_CREATE_SUCCESS = "版本创建成功";
    public static final String VERSION_DELETE_SUCCESS = "版本删除成功";
    public static final String POLICY_CREATE_SUCCESS = "限流策略创建成功";
    public static final String POLICY_UPDATE_SUCCESS = "限流策略更新成功";
    public static final String POLICY_DELETE_SUCCESS = "限流策略删除成功";
    public static final String POLICY_APPLY_SUCCESS = "策略应用成功";
    public static final String POLICY_REMOVE_SUCCESS = "策略移除成功";
    public static final String CONNECTION_SUCCESS = "连接成功";
    public static final String CONNECTION_FAILED = "连接失败";
    public static final String PUBLISH_FAILED = "发布失败";
    public static final String UNPUBLISH_FAILED = "下线失败";
    public static final String DELETE_FAILED = "删除失败";
    public static final String NOT_FOUND = "资源不存在";
    public static final String ANNOUNCEMENT_NOT_FOUND = "公告不存在";
}
