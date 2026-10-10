package com.example.community.dto;

import jakarta.validation.constraints.NotBlank;

public class ToolDTO {
    @NotBlank(message = "工具名称不能为空")
    private String name;
    private String description;
    @NotBlank(message = "工具链接不能为空")
    private String url;
    private String category;
    private String coverUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
}
