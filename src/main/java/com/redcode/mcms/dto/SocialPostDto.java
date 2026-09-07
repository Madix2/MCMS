package com.redcode.mcms.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Carrier for a simulated social-media post.
 */
public class SocialPostDto {

    private Long id;

    @NotBlank(message = "Platform is required.")
    private String platform;

    private Long campaignId;
    private String campaignName;

    @NotBlank(message = "Post content is required.")
    private String content;

    private String status;
    private LocalDateTime scheduledAt;
    private int likes;
    private int shares;
    private int comments;
    private int reach;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Long getCampaignId() { return campaignId; }
    public void setCampaignId(Long campaignId) { this.campaignId = campaignId; }
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getShares() { return shares; }
    public void setShares(int shares) { this.shares = shares; }
    public int getComments() { return comments; }
    public void setComments(int comments) { this.comments = comments; }
    public int getReach() { return reach; }
    public void setReach(int reach) { this.reach = reach; }
}
