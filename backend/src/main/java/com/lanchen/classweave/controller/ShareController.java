package com.lanchen.classweave.controller;

import com.lanchen.classweave.common.ApiResponse;
import com.lanchen.classweave.common.ScheduleIdResponse;
import com.lanchen.classweave.dto.share.CreateShareLinkRequest;
import com.lanchen.classweave.dto.share.ShareLinkResponse;
import com.lanchen.classweave.dto.share.SharePreviewResponse;
import com.lanchen.classweave.security.UserPrincipal;
import com.lanchen.classweave.service.ShareService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/schedules/{scheduleId}/share-links")
    public ApiResponse<ShareLinkResponse> createShareLink(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId,
            @RequestBody(required = false) @Valid CreateShareLinkRequest request
    ) {
        CreateShareLinkRequest actualRequest = request == null ? new CreateShareLinkRequest(null) : request;
        return ApiResponse.success(shareService.createShareLink(principal.getId(), scheduleId, actualRequest));
    }

    @GetMapping("/schedules/{scheduleId}/share-links")
    public ApiResponse<List<ShareLinkResponse>> listShareLinks(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.success(shareService.listShareLinks(principal.getId(), scheduleId));
    }

    @DeleteMapping("/share-links/{shareLinkId}")
    public ApiResponse<Boolean> deactivate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long shareLinkId
    ) {
        return ApiResponse.success(shareService.deactivateShareLink(principal.getId(), shareLinkId));
    }

    @GetMapping("/shares/{shareToken}")
    public ApiResponse<SharePreviewResponse> preview(@PathVariable String shareToken) {
        return ApiResponse.success(shareService.preview(shareToken));
    }

    @PostMapping("/shares/{shareToken}/accept")
    public ApiResponse<ScheduleIdResponse> accept(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String shareToken
    ) {
        return ApiResponse.success(shareService.accept(principal.getId(), shareToken));
    }
}
