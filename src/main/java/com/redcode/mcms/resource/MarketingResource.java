package com.redcode.mcms.resource;

import com.redcode.mcms.dto.CampaignDto;
import com.redcode.mcms.dto.PromotionDto;
import com.redcode.mcms.dto.SocialPostDto;
import com.redcode.mcms.security.Secured;
import com.redcode.mcms.service.MarketingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Marketing REST API: promotions, campaigns and simulated social-media posts.
 * External social APIs can be connected here later via REST.
 */
@Path("/marketing")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Secured
public class MarketingResource {

    @Inject
    private MarketingService marketingService;

    @GET
    @Path("/promotions")
    public List<PromotionDto> promotions() {
        return marketingService.listPromotions();
    }

    @POST
    @Path("/promotions")
    public Response createPromotion(@Valid PromotionDto dto) {
        return Response.status(Response.Status.CREATED)
                .entity(marketingService.createPromotion(dto)).build();
    }

    @PUT
    @Path("/promotions/{id}")
    public PromotionDto updatePromotion(@PathParam("id") Long id, @Valid PromotionDto dto) {
        return marketingService.updatePromotion(id, dto);
    }

    @DELETE
    @Path("/promotions/{id}")
    public Response deactivatePromotion(@PathParam("id") Long id,
                                        @QueryParam("pause") boolean pause) {
        marketingService.deactivatePromotion(id, pause);
        return Response.ok(Map.of("message", "Promotion updated.")).build();
    }

    @GET
    @Path("/campaigns")
    public List<CampaignDto> campaigns() {
        return marketingService.listCampaigns();
    }

    @POST
    @Path("/campaigns")
    public Response createCampaign(@Valid CampaignDto dto) {
        return Response.status(Response.Status.CREATED)
                .entity(marketingService.createCampaign(dto)).build();
    }

    @PUT
    @Path("/campaigns/{id}")
    public CampaignDto updateCampaign(@PathParam("id") Long id, @Valid CampaignDto dto) {
        return marketingService.updateCampaign(id, dto);
    }

    @GET
    @Path("/social-posts")
    public List<SocialPostDto> socialPosts() {
        return marketingService.listSocialPosts();
    }

    @POST
    @Path("/social-posts")
    public Response createSocialPost(@Valid SocialPostDto dto) {
        return Response.status(Response.Status.CREATED)
                .entity(marketingService.createSocialPost(dto)).build();
    }
}
