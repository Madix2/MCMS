package com.redcode.mcms.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A simulated social-media post linked to a campaign.
 * Real social APIs can be plugged into this model; for the demo the data is
 * pre-populated with realistic engagement statistics.
 */
@Entity
@Table(name = "social_post")
public class SocialPost extends AuditableEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String platform;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "likes")
    private int likes;

    @Column(name = "shares")
    private int shares;

    @Column(name = "comments")
    private int comments;

    @Column(name = "reach")
    private int reach;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Campaign getCampaign() { return campaign; }
    public void setCampaign(Campaign campaign) { this.campaign = campaign; }
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
