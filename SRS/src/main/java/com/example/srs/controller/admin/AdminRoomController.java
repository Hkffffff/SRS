package com.example.srs.controller.admin;

import com.example.srs.common.Result;
import com.example.srs.domain.Room;
import com.example.srs.domain.Seat;
import com.example.srs.dto.RoomSaveRequest;
import com.example.srs.service.RoomService;
import com.example.srs.service.SeatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/room")
@RequiredArgsConstructor
public class AdminRoomController {

    private final RoomService roomService;
    private final SeatService seatService;

    @GetMapping("/list")
    public Result<List<Room>> list(@RequestParam(required = false) Integer status) {
        List<Room> rooms = roomService.lambdaQuery()
                .eq(status != null, Room::getStatus, status)
                .orderByAsc(Room::getFloor)
                .orderByAsc(Room::getId)
                .list();
        return Result.success(rooms);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping
    public Result<Room> create(@Valid @RequestBody RoomSaveRequest request) {
        validateRoomName(request.getRoomName(), null);
        validateSeatGeneration(request);

        Room room = new Room();
        room.setRoomName(request.getRoomName().trim());
        room.setFloor(request.getFloor());
        room.setStatus(request.getStatus());
        room.setTotalSeats(0);
        roomService.save(room);

        if (Boolean.TRUE.equals(request.getAutoGenerateSeats())) {
            List<Seat> seats = buildGeneratedSeats(room.getId(), request.getRowCount(), request.getSeatsPerRow());
            seatService.saveBatch(seats);
            room.setTotalSeats(seats.size());
            roomService.updateById(room);
        }

        return Result.success("自习室创建成功", room);
    }

    @PutMapping("/{roomId}")
    public Result<Room> update(@PathVariable Long roomId, @Valid @RequestBody RoomSaveRequest request) {
        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error("自习室不存在");
        }

        validateRoomName(request.getRoomName(), roomId);

        room.setRoomName(request.getRoomName().trim());
        room.setFloor(request.getFloor());
        room.setStatus(request.getStatus());
        roomService.updateById(room);
        return Result.success("自习室更新成功", room);
    }

    @PutMapping("/{roomId}/status")
    public Result<String> updateStatus(
            @PathVariable Long roomId,
            @RequestParam @Min(value = 0, message = "状态只能是 0 或 1") @Max(value = 1, message = "状态只能是 0 或 1") Integer status) {
        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error("自习室不存在");
        }

        room.setStatus(status);
        roomService.updateById(room);
        return Result.success("自习室状态更新成功", null);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/{roomId}")
    public Result<String> delete(@PathVariable Long roomId) {
        Room room = roomService.getById(roomId);
        if (room == null) {
            return Result.error("自习室不存在");
        }

        long seatCount = seatService.lambdaQuery()
                .eq(Seat::getRoomId, roomId)
                .count();
        if (seatCount > 0) {
            seatService.lambdaUpdate()
                    .eq(Seat::getRoomId, roomId)
                    .remove();
        }

        roomService.removeById(roomId);
        return Result.success(seatCount > 0 ? "自习室及其下全部座位已删除" : "自习室删除成功", null);
    }

    private void validateRoomName(String roomName, Long roomId) {
        long duplicateCount = roomService.lambdaQuery()
                .eq(Room::getRoomName, roomName.trim())
                .ne(roomId != null, Room::getId, roomId)
                .count();
        if (duplicateCount > 0) {
            throw new IllegalArgumentException("自习室名称已存在");
        }
    }

    private void validateSeatGeneration(RoomSaveRequest request) {
        if (!Boolean.TRUE.equals(request.getAutoGenerateSeats())) {
            return;
        }

        if (request.getRowCount() == null || request.getSeatsPerRow() == null) {
            throw new IllegalArgumentException("启用批量生成座位时，排数和每排座位数不能为空");
        }
    }

    private List<Seat> buildGeneratedSeats(Long roomId, Integer rowCount, Integer seatsPerRow) {
        List<Seat> seats = new ArrayList<>();
        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            String rowLabel = toExcelColumnLabel(rowIndex);
            for (int colIndex = 1; colIndex <= seatsPerRow; colIndex++) {
                Seat seat = new Seat();
                seat.setRoomId(roomId);
                seat.setSeatNumber(rowLabel + "-" + String.format("%02d", colIndex));
                seat.setHasWindow(colIndex == 1 || colIndex == seatsPerRow ? 1 : 0);
                seat.setHasPower(1);
                seat.setStatus(1);
                seats.add(seat);
            }
        }
        return seats;
    }

    private String toExcelColumnLabel(int index) {
        StringBuilder builder = new StringBuilder();
        int current = index;
        while (current > 0) {
            current--;
            builder.insert(0, (char) ('A' + (current % 26)));
            current /= 26;
        }
        return builder.toString();
    }
}
