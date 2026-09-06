package com.example.srs.controller.admin;

import com.example.srs.common.Result;
import com.example.srs.domain.Room;
import com.example.srs.domain.Seat;
import com.example.srs.dto.SeatSaveRequest;
import com.example.srs.service.ReservationService;
import com.example.srs.service.RoomService;
import com.example.srs.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/seat")
@RequiredArgsConstructor
public class AdminSeatController {

    private final SeatService seatService;
    private final RoomService roomService;
    private final ReservationService reservationService;

    @GetMapping("/room/{roomId}/list")
    public Result<List<Seat>> listByRoom(@PathVariable Long roomId) {
        if (roomService.getById(roomId) == null) {
            return Result.error("自习室不存在");
        }

        List<Seat> seats = seatService.lambdaQuery()
                .eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getSeatNumber)
                .list();
        return Result.success(seats);
    }

    @PostMapping
    public Result<Seat> create(@Valid @RequestBody SeatSaveRequest request) {
        Room room = roomService.getById(request.getRoomId());
        if (room == null) {
            return Result.error("所属自习室不存在");
        }

        validateSeatNumber(request.getRoomId(), request.getSeatNumber(), null);

        Seat seat = new Seat();
        seat.setRoomId(request.getRoomId());
        seat.setSeatNumber(request.getSeatNumber().trim());
        seat.setHasWindow(request.getHasWindow());
        seat.setHasPower(request.getHasPower());
        seat.setStatus(request.getStatus());
        seatService.save(seat);
        syncRoomTotalSeats(request.getRoomId());
        return Result.success("座位创建成功", seat);
    }

    @PutMapping("/{seatId}")
    public Result<Seat> update(@PathVariable Long seatId, @Valid @RequestBody SeatSaveRequest request) {
        Seat seat = seatService.getById(seatId);
        if (seat == null) {
            return Result.error("座位不存在");
        }
        if (roomService.getById(request.getRoomId()) == null) {
            return Result.error("所属自习室不存在");
        }

        long reservationCount = reservationService.lambdaQuery().eq(com.example.srs.domain.Reservation::getSeatId, seatId).count();
        if (!seat.getRoomId().equals(request.getRoomId()) && reservationCount > 0) {
            return Result.error("该座位已有预约记录，不允许变更所属自习室");
        }

        validateSeatNumber(request.getRoomId(), request.getSeatNumber(), seatId);

        Long oldRoomId = seat.getRoomId();
        seat.setRoomId(request.getRoomId());
        seat.setSeatNumber(request.getSeatNumber().trim());
        seat.setHasWindow(request.getHasWindow());
        seat.setHasPower(request.getHasPower());
        seat.setStatus(request.getStatus());
        seatService.updateById(seat);

        syncRoomTotalSeats(oldRoomId);
        if (!oldRoomId.equals(request.getRoomId())) {
            syncRoomTotalSeats(request.getRoomId());
        }
        return Result.success("座位更新成功", seat);
    }

    @DeleteMapping("/{seatId}")
    public Result<String> delete(@PathVariable Long seatId) {
        Seat seat = seatService.getById(seatId);
        if (seat == null) {
            return Result.error("座位不存在");
        }

        long reservationCount = reservationService.lambdaQuery().eq(com.example.srs.domain.Reservation::getSeatId, seatId).count();
        if (reservationCount > 0) {
            return Result.error("该座位已有预约记录，不允许删除");
        }

        Long roomId = seat.getRoomId();
        seatService.removeById(seatId);
        syncRoomTotalSeats(roomId);
        return Result.success("座位删除成功", null);
    }

    private void validateSeatNumber(Long roomId, String seatNumber, Long seatId) {
        long duplicateCount = seatService.lambdaQuery()
                .eq(Seat::getRoomId, roomId)
                .eq(Seat::getSeatNumber, seatNumber.trim())
                .ne(seatId != null, Seat::getId, seatId)
                .count();
        if (duplicateCount > 0) {
            throw new IllegalArgumentException("同一自习室下座位编号不能重复");
        }
    }

    private void syncRoomTotalSeats(Long roomId) {
        Room room = roomService.getById(roomId);
        if (room == null) {
            return;
        }

        int totalSeats = Math.toIntExact(seatService.lambdaQuery().eq(Seat::getRoomId, roomId).count());
        room.setTotalSeats(totalSeats);
        roomService.updateById(room);
    }
}
