package com.lanchen.classweave.service;

import com.lanchen.classweave.common.ErrorCode;
import com.lanchen.classweave.common.ScheduleIdResponse;
import com.lanchen.classweave.config.AppProperties;
import com.lanchen.classweave.domain.entity.ScheduleAccessEntity;
import com.lanchen.classweave.domain.entity.ScheduleEntity;
import com.lanchen.classweave.domain.entity.ScheduleShareLinkEntity;
import com.lanchen.classweave.domain.entity.UserEntity;
import com.lanchen.classweave.domain.enums.AccessRole;
import com.lanchen.classweave.domain.enums.AccessSource;
import com.lanchen.classweave.domain.enums.PermissionType;
import com.lanchen.classweave.domain.repository.ScheduleAccessRepository;
import com.lanchen.classweave.domain.repository.ScheduleShareLinkRepository;
import com.lanchen.classweave.dto.share.CreateShareLinkRequest;
import com.lanchen.classweave.dto.share.ShareLinkResponse;
import com.lanchen.classweave.dto.share.SharePreviewResponse;
import com.lanchen.classweave.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ShareService {

    private static final BigDecimal DEFAULT_OPACITY = new BigDecimal("0.85");

    private final ScheduleService scheduleService;
    private final ScheduleShareLinkRepository shareLinkRepository;
    private final ScheduleAccessRepository scheduleAccessRepository;
    private final UserService userService;
    private final AppProperties appProperties;

    public ShareService(
            ScheduleService scheduleService,
            ScheduleShareLinkRepository shareLinkRepository,
            ScheduleAccessRepository scheduleAccessRepository,
            UserService userService,
            AppProperties appProperties
    ) {
        this.scheduleService = scheduleService;
        this.shareLinkRepository = shareLinkRepository;
        this.scheduleAccessRepository = scheduleAccessRepository;
        this.userService = userService;
        this.appProperties = appProperties;
    }

    @Transactional
    public ShareLinkResponse createShareLink(Long userId, Long scheduleId, CreateShareLinkRequest request) {
        ScheduleAccessEntity ownerAccess = scheduleService.getOwnerAccess(userId, scheduleId);
        ScheduleShareLinkEntity shareLink = new ScheduleShareLinkEntity();
        shareLink.setSchedule(ownerAccess.getSchedule());
        shareLink.setCreatedByUser(ownerAccess.getUser());
        shareLink.setShareToken(generateShareToken());
        shareLink.setPermissionType(PermissionType.VIEW);
        shareLink.setActive(true);
        shareLink.setExpiresAt(request.expiresAt());
        shareLink = shareLinkRepository.save(shareLink);
        return toShareLinkResponse(shareLink);
    }

    @Transactional(readOnly = true)
    public List<ShareLinkResponse> listShareLinks(Long userId, Long scheduleId) {
        scheduleService.getOwnerAccess(userId, scheduleId);
        return shareLinkRepository.findBySchedule_IdOrderByCreatedAtDesc(scheduleId).stream()
                .map(this::toShareLinkResponse)
                .toList();
    }

    @Transactional
    public boolean deactivateShareLink(Long userId, Long shareLinkId) {
        ScheduleShareLinkEntity shareLink = shareLinkRepository.findById(shareLinkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "分享链接不存在"));
        scheduleService.getOwnerAccess(userId, shareLink.getSchedule().getId());
        shareLink.setActive(false);
        return true;
    }

    @Transactional(readOnly = true)
    public SharePreviewResponse preview(String shareToken) {
        ScheduleShareLinkEntity shareLink = shareLinkRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "分享链接不存在"));
        ScheduleEntity schedule = shareLink.getSchedule();
        UserEntity owner = schedule.getOwnerUser();
        return new SharePreviewResponse(
                schedule.getId(),
                schedule.getName(),
                owner.getDisplayName() == null || owner.getDisplayName().isBlank() ? owner.getUsername() : owner.getDisplayName(),
                schedule.getTermLabel(),
                schedule.getDescription(),
                isExpired(shareLink),
                true
        );
    }

    @Transactional
    public ScheduleIdResponse accept(Long userId, String shareToken) {
        ScheduleShareLinkEntity shareLink = shareLinkRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "分享链接不存在"));
        Long scheduleId = shareLink.getSchedule().getId();
        if (scheduleAccessRepository.existsByUser_IdAndSchedule_Id(userId, scheduleId)) {
            return new ScheduleIdResponse(scheduleId);
        }
        if (isExpired(shareLink)) {
            throw new BusinessException(ErrorCode.SHARE_LINK_EXPIRED);
        }

        UserEntity user = userService.findById(userId);
        ScheduleAccessEntity access = new ScheduleAccessEntity();
        access.setUser(user);
        access.setSchedule(shareLink.getSchedule());
        access.setAccessRole(AccessRole.VIEWER);
        access.setAccessSource(AccessSource.SHARE_LINK);
        access.setShareLink(shareLink);
        access.setDisplayColor(shareLink.getSchedule().getDefaultColor());
        access.setDisplayOpacity(DEFAULT_OPACITY);
        access.setVisibleDefault(true);
        scheduleAccessRepository.save(access);
        return new ScheduleIdResponse(scheduleId);
    }

    private boolean isExpired(ScheduleShareLinkEntity shareLink) {
        return !Boolean.TRUE.equals(shareLink.getActive())
                || (shareLink.getExpiresAt() != null && shareLink.getExpiresAt().isBefore(LocalDateTime.now()));
    }

    private ShareLinkResponse toShareLinkResponse(ScheduleShareLinkEntity entity) {
        return new ShareLinkResponse(
                entity.getId(),
                entity.getSchedule().getId(),
                entity.getShareToken(),
                entity.getPermissionType().name(),
                entity.getActive(),
                entity.getExpiresAt(),
                buildShareUrl(entity.getShareToken())
        );
    }

    private String buildShareUrl(String token) {
        return appProperties.share().baseUrl().replaceAll("/+$", "") + "/" + token;
    }

    private String generateShareToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
