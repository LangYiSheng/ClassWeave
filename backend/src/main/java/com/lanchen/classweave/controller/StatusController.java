package com.lanchen.classweave.controller;

import com.lanchen.classweave.common.ApiResponse;
import com.lanchen.classweave.common.ErrorCode;
import com.lanchen.classweave.dto.status.StatusPanelResponse;
import com.lanchen.classweave.exception.BusinessException;
import com.lanchen.classweave.security.UserPrincipal;
import com.lanchen.classweave.service.StatusService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/panel")
    public ApiResponse<StatusPanelResponse> getPanel(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @NotBlank String scheduleIds,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(defaultValue = "60") @Min(value = 1, message = "windowMinutes 最小为 1") Integer windowMinutes
    ) {
        List<Long> ids = Arrays.stream(scheduleIds.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .map(this::parseId)
                .toList();
        return ApiResponse.success(statusService.getStatusPanel(principal.getId(), ids, date, time, windowMinutes));
    }

    private Long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "scheduleIds 格式错误");
        }
    }
}
