package com.example.srs.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeatSaveRequest {

    @NotNull(message = "自习室ID不能为空")
    private Long roomId;

    @NotBlank(message = "座位编号不能为空")
    private String seatNumber;

    @NotNull(message = "是否靠窗不能为空")
    @Min(value = 0, message = "是否靠窗只能是 0 或 1")
    @Max(value = 1, message = "是否靠窗只能是 0 或 1")
    private Integer hasWindow;

    @NotNull(message = "是否有插座不能为空")
    @Min(value = 0, message = "是否有插座只能是 0 或 1")
    @Max(value = 1, message = "是否有插座只能是 0 或 1")
    private Integer hasPower;

    @NotNull(message = "座位状态不能为空")
    @Min(value = 0, message = "座位状态只能是 0 或 1")
    @Max(value = 1, message = "座位状态只能是 0 或 1")
    private Integer status;
}
