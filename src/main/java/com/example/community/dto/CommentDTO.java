package com.example.community.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentDTO {
    @NotBlank(message = "评论不能为空")
    private String content;
    private Long parentId;
    private Long replyToUserId;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Long replyToUserId) { this.replyToUserId = replyToUserId; }
}
