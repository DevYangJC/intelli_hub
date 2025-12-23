package com.intellihub.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("iam_login_log")
public class IamLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 登录类型：password/sms/oauth
     */
    private String loginType;

    /**
     * 结果：success/fail
     */
    private String loginResult;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 登录地点
     */
    private String location;

    /**
     * 浏览器UA
     */
    private String userAgent;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
