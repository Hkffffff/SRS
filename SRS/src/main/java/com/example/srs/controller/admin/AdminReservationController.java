package com.example.srs.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.srs.common.PageResult;
import com.example.srs.common.Result;
import com.example.srs.domain.Reservation;
import com.example.srs.domain.Room;
import com.example.srs.domain.Seat;
import com.example.srs.service.ReservationService;
import com.example.srs.service.RoomService;
import com.example.srs.service.SeatService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/admin/reservation")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService reservationService;
    private final SeatService seatService;
    private final RoomService roomService;

    @GetMapping("/list")
    public Result<PageResult<Reservation>> list(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Long seatId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) @Min(value = 0, message = "Invalid status") @Max(value = 3, message = "Invalid status") Integer status,
            @RequestParam(required = false) @Min(value = 1, message = "Invalid time slot") @Max(value = 3, message = "Invalid time slot") Integer timeSlot,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be >= 1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be >= 1") @Max(value = 100, message = "Page size must be <= 100") long pageSize) {

        java.util.List<Long> seatIdsByRoom = null;
        if (roomId != null) {
            seatIdsByRoom = seatService.lambdaQuery()
                    .eq(Seat::getRoomId, roomId)
                    .list()
                    .stream()
                    .map(Seat::getId)
                    .toList();
            if (seatIdsByRoom.isEmpty()) {
                return Result.success(PageResult.from(new Page<>(page, pageSize)));
            }
        }

        String normalizedStudentId = studentId == null ? null : studentId.trim();
        Page<Reservation> reservationPage = reservationService.lambdaQuery()
                .eq(normalizedStudentId != null && !normalizedStudentId.isBlank(), Reservation::getStudentId, normalizedStudentId)
                .eq(seatId != null, Reservation::getSeatId, seatId)
                .in(seatIdsByRoom != null, Reservation::getSeatId, seatIdsByRoom)
                .eq(date != null, Reservation::getReservationDate, date)
                .eq(status != null, Reservation::getStatus, status)
                .eq(timeSlot != null, Reservation::getTimeSlot, timeSlot)
                .orderByDesc(Reservation::getCreateTime)
                .orderByDesc(Reservation::getId)
                .page(new Page<>(page, pageSize));
        enrichReservationDisplay(reservationPage.getRecords());
        return Result.success(PageResult.from(reservationPage));
    }

    @PutMapping("/{reservationId}/cancel")
    public Result<String> cancel(@PathVariable Long reservationId) {
        Reservation reservation = reservationService.getById(reservationId);
        if (reservation == null) {
            return Result.error("Reservation record not found");
        }
        if (reservation.getStatus() != 0) {
            return Result.error("Current status does not allow cancellation");
        }

        reservation.setStatus(2);
        reservationService.updateById(reservation);
        return Result.success("Reservation cancelled", null);
    }

    @PutMapping("/{reservationId}/violate")
    public Result<String> markViolated(@PathVariable Long reservationId) {
        boolean success = reservationService.markReservationViolated(reservationId);
        if (!success) {
            return Result.error("Only pending reservations can be marked violated");
        }
        return Result.success("Reservation marked violated and added to blacklist", null);
    }

    @PutMapping("/process-overdue")
    public Result<Integer> processOverdueReservations() {
        int processedCount = reservationService.processOverdueReservations(LocalDateTime.now());
        return Result.success("Overdue reservations processed", processedCount);
    }

    private void enrichReservationDisplay(java.util.List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return;
        }

        Set<Long> seatIds = reservations.stream()
                .map(Reservation::getSeatId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Seat> seatMap = seatService.listByIds(seatIds).stream()
                .collect(Collectors.toMap(Seat::getId, Function.identity()));

        Set<Long> roomIds = seatMap.values().stream()
                .map(Seat::getRoomId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Room> roomMap = roomService.listByIds(roomIds).stream()
                .collect(Collectors.toMap(Room::getId, Function.identity()));

        for (Reservation reservation : reservations) {
            Seat seat = seatMap.get(reservation.getSeatId());
            if (seat == null) {
                continue;
            }

            reservation.setSeatNumber(seat.getSeatNumber());
            Room room = roomMap.get(seat.getRoomId());
            if (room != null) {
                reservation.setRoomName(room.getRoomName());
            }
        }
    }
}
