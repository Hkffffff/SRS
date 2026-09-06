package com.example.srs.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomSaveRequest {

    @NotBlank(message = "自习室名称不能为空")
    private String roomName;

    @NotNull(message = "楼层不能为空")
    @Min(value = 1, message = "楼层必须大于等于 1")
    private Integer floor;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能是 0 或 1")
    @Max(value = 1, message = "状态只能是 0 或 1")
    private Integer status;

    private Boolean autoGenerateSeats = false;

    @Min(value = 1, message = "排数必须大于等于 1")
    private Integer rowCount;

    @Min(value = 1, message = "每排座位数必须大于等于 1")
    private Integer seatsPerRow;
}
