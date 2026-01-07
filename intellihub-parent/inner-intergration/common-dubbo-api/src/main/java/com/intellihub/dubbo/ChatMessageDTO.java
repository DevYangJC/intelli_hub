package com.intellihub.dubbo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 聊天消息DTO
 *
 * @author intellihub
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色（user/assistant/system）
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * Token数
     */
    private Integer tokens;

    /**
     * 创建时间
     */
    private String createdAt;
}
