package com.example.srs.controller;

import com.example.srs.common.Result;
import com.example.srs.domain.Reservation;
import com.example.srs.domain.Room;
import com.example.srs.domain.Seat;
import com.example.srs.service.ReservationService;
import com.example.srs.service.RoomService;
import com.example.srs.service.SeatService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomService roomService;
    private final SeatService seatService;
    private final ReservationService reservationService;

    public RoomController(
            RoomService roomService,
            SeatService seatService,
            ReservationService reservationService) {
        this.roomService = roomService;
        this.seatService = seatService;
        this.reservationService = reservationService;
    }

    @GetMapping("/list")
    public Result<List<Room>> getRoomList() {
        List<Room> rooms = roomService.lambdaQuery()
                .eq(Room::getStatus, 1)
                .orderByAsc(Room::getFloor)
                .orderByAsc(Room::getId)
                .list();
        return Result.success(rooms);
    }

    @GetMapping("/{roomId}/seats")
    public Result<List<Seat>> getSeatsByRoomId(
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Integer timeSlot) {

        List<Seat> seats = seatService.getSeatsByRoomId(roomId);
        if (date == null || timeSlot == null || seats.isEmpty()) {
            return Result.success(seats);
        }

        List<Long> seatIds = seats.stream().map(Seat::getId).toList();
        List<Reservation> reservations = reservationService.lambdaQuery()
                .in(Reservation::getSeatId, seatIds)
                .eq(Reservation::getReservationDate, date)
                .eq(Reservation::getTimeSlot, timeSlot)
                .in(Reservation::getStatus, 0, 1)
                .list();
        Set<Long> occupiedSeatIds = new HashSet<>(reservations.stream().map(Reservation::getSeatId).toList());

        for (Seat seat : seats) {
            if (occupiedSeatIds.contains(seat.getId())) {
                seat.setStatus(0);
            }
        }
        return Result.success(seats);
    }
}
