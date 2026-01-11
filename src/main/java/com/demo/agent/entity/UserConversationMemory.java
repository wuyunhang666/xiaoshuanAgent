package com.demo.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户AI会话memoryId记录表
 */
@Data
@TableName(value = "user_conversation_memory")
public class UserConversationMemory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联用户表）
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * AI会话memoryId
     */
    @TableField(value = "memory_id")
    private String memoryId;

    /**
     * 会话标题
     */
    @TableField(value = "conversation_title")
    private String conversationTitle;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 0-未删 1-已删
     */
    @TableField(value = "is_deleted")
    private Integer isDeleted;
}