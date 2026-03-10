package com.lanchen.classweave.dto.schedule;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateDisplaySettingsRequest(
        @Size(max = 20, message = "displayColor 长度不能超过 20")
        String displayColor,
        @NotNull(message = "displayOpacity 不能为空")
        @DecimalMin(value = "0.00", inclusive = false, message = "displayOpacity 必须大于 0")
        @DecimalMax(value = "1.00", inclusive = true, message = "displayOpacity 最大为 1")
        BigDecimal displayOpacity,
        @NotNull(message = "isVisibleDefault 不能为空")
        Boolean isVisibleDefault
) {
}
