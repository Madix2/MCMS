package com.redcode.mcms.service;

import com.redcode.mcms.dto.CampaignDto;
import com.redcode.mcms.dto.PromotionDto;
import com.redcode.mcms.dto.SocialPostDto;
import com.redcode.mcms.entity.*;
import com.redcode.mcms.exception.BusinessException;
import com.redcode.mcms.exception.NotFoundException;
import com.redcode.mcms.repository.*;
import com.redcode.mcms.security.AuthContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Marketing module: promotions, campaigns and simulated social-media posts.
 * Status of promotions is derived from their start/end dates and discount.
 */
@Stateless
public class MarketingService {

    @Inject
    private PromotionRepository promotionRepository;

    @Inject
    private CampaignRepository campaignRepository;

    @Inject
    private SocialPostRepository socialPostRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private NotificationService notificationService;

    @Inject
    private AuditService auditService;

    @Inject
    private AuthContext authContext;

    // ---------- Promotions ----------

    public List<PromotionDto> listPromotions() {
        return promotionRepository.findAllOrderedDesc().stream()
                .map(this::toPromotionDto).collect(Collectors.toList());
    }

    public PromotionDto createPromotion(PromotionDto dto) {
        authContext.requireRole(AuthContext.RolePermission.MARKETING, AuthContext.RolePermission.ADMIN);
        validatePromotion(dto);
        Promotion p = new Promotion();
        applyPromotion(dto, p);
        refreshPromotionStatus(p);
        Promotion saved = promotionRepository.save(p);
        if (saved.getStatus() == PromotionStatus.ACTIVE || saved.getStatus() == PromotionStatus.SCHEDULED) {
            notificationService.notify("PROMOTION", "PROMOTION",
                    "Promotion \"" + saved.getName() + "\" created.", null);
        }
        auditService.log("CREATE", "Promotion", "Promotion \"" + saved.getName() + "\" created");
        return toPromotionDto(saved);
    }

    public PromotionDto updatePromotion(Long id, PromotionDto dto) {
        authContext.requireRole(AuthContext.RolePermission.MARKETING, AuthContext.RolePermission.ADMIN);
        validatePromotion(dto);
        Promotion p = getPromotion(id);
        applyPromotion(dto, p);
        refreshPromotionStatus(p);
        promotionRepository.update(p);
        auditService.log("UPDATE", "Promotion", "Promotion \"" + p.getName() + "\" updated");
        return toPromotionDto(p);
    }

    public void deactivatePromotion(Long id, boolean pause) {
        authContext.requireRole(AuthContext.RolePermission.MARKETING, AuthContext.RolePermission.ADMIN);
        Promotion p = getPromotion(id);
        p.setStatus(pause ? PromotionStatus.PAUSED : PromotionStatus.EXPIRED);
        promotionRepository.update(p);
        auditService.log("DEACTIVATE", "Promotion", "Promotion \"" + p.getName() + "\" deactivated");
    }

    private void validatePromotion(PromotionDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new BusinessException("Promotion name is required.");
        }
        if (dto.getDiscount() == null || dto.getDiscount().signum() < 0
                || dto.getDiscount().compareTo(new java.math.BigDecimal("100")) > 0) {
            throw new BusinessException("Discount must be a percentage between 0 and 100.");
        }
    }

    private void applyPromotion(PromotionDto dto, Promotion p) {
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        p.setDiscount(dto.getDiscount());
        if (dto.getProductId() != null) {
            p.setProduct(productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found.")));
        }
        if (dto.getCategoryId() != null) {
            p.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found.")));
        }
    }

    /** Derives status from today's date against the schedule. */
    private void refreshPromotionStatus(Promotion p) {
        LocalDate today = LocalDate.now();
        LocalDate end = p.getEndDate() != null ? p.getEndDate() : today.plusDays(7);
        LocalDate start = p.getStartDate() != null ? p.getStartDate() : today;
        if (p.getStatus() == PromotionStatus.PAUSED) {
            return;
        }
        if (today.isBefore(start)) {
            p.setStatus(PromotionStatus.SCHEDULED);
        } else if (today.isAfter(end)) {
            p.setStatus(PromotionStatus.EXPIRED);
        } else {
            p.setStatus(PromotionStatus.ACTIVE);
        }
    }

    private Promotion getPromotion(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promotion not found."));
    }

    private PromotionDto toPromotionDto(Promotion p) {
        PromotionDto dto = new PromotionDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setDiscount(p.getDiscount());
        dto.setStatus(p.getStatus().name());
        if (p.getProduct() != null) {
            dto.setProductId(p.getProduct().getId());
            dto.setProductName(p.getProduct().getName());
        }
        if (p.getCategory() != null) {
            dto.setCategoryId(p.getCategory().getId());
            dto.setCategoryName(p.getCategory().getName());
        }
        return dto;
    }

    // ---------- Campaigns ----------

    public List<CampaignDto> listCampaigns() {
        return campaignRepository.findAllOrderedDesc().stream()
                .map(this::toCampaignDto).collect(Collectors.toList());
    }

    public CampaignDto createCampaign(CampaignDto dto) {
        authContext.requireRole(AuthContext.RolePermission.MARKETING, AuthContext.RolePermission.ADMIN);
        Campaign c = new Campaign();
        applyCampaign(dto, c);
        Campaign saved = campaignRepository.save(c);
        auditService.log("CREATE", "Campaign", "Campaign \"" + saved.getName() + "\" created");
        return toCampaignDto(saved);
    }

    public CampaignDto updateCampaign(Long id, CampaignDto dto) {
        authContext.requireRole(AuthContext.RolePermission.MARKETING, AuthContext.RolePermission.ADMIN);
        Campaign c = campaignRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Campaign not found."));
        applyCampaign(dto, c);
        campaignRepository.update(c);
        return toCampaignDto(c);
    }

    private void applyCampaign(CampaignDto dto, Campaign c) {
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        c.setStartDate(dto.getStartDate());
        c.setEndDate(dto.getEndDate());
        c.setBudget(dto.getBudget());
        if (dto.getStatus() != null) {
            c.setStatus(PromotionStatus.valueOf(dto.getStatus()));
        }
    }

    private CampaignDto toCampaignDto(Campaign c) {
        CampaignDto dto = new CampaignDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setBudget(c.getBudget());
        dto.setStatus(c.getStatus().name());
        return dto;
    }

    // ---------- Social media (simulated integration) ----------

    public List<SocialPostDto> listSocialPosts() {
        return socialPostRepository.findAllOrderedDesc().stream()
                .map(this::toSocialPostDto).collect(Collectors.toList());
    }

    public SocialPostDto createSocialPost(SocialPostDto dto) {
        authContext.requireRole(AuthContext.RolePermission.MARKETING, AuthContext.RolePermission.ADMIN);
        SocialPost post = new SocialPost();
        applySocialPost(dto, post);
        SocialPost saved = socialPostRepository.save(post);
        auditService.log("CREATE", "SocialPost", "Social post scheduled on " + saved.getPlatform());
        return toSocialPostDto(saved);
    }

    private void applySocialPost(SocialPostDto dto, SocialPost post) {
        post.setPlatform(dto.getPlatform());
        post.setContent(dto.getContent());
        post.setStatus(dto.getStatus() == null ? "SCHEDULED" : dto.getStatus());
        post.setScheduledAt(dto.getScheduledAt());
        if (dto.getCampaignId() != null) {
            post.setCampaign(campaignRepository.findById(dto.getCampaignId())
                    .orElseThrow(() -> new NotFoundException("Campaign not found.")));
        }
    }

    private SocialPostDto toSocialPostDto(SocialPost post) {
        SocialPostDto dto = new SocialPostDto();
        dto.setId(post.getId());
        dto.setPlatform(post.getPlatform());
        dto.setContent(post.getContent());
        dto.setStatus(post.getStatus());
        dto.setScheduledAt(post.getScheduledAt());
        dto.setLikes(post.getLikes());
        dto.setShares(post.getShares());
        dto.setComments(post.getComments());
        dto.setReach(post.getReach());
        if (post.getCampaign() != null) {
            dto.setCampaignId(post.getCampaign().getId());
            dto.setCampaignName(post.getCampaign().getName());
        }
        return dto;
    }
}
