package com.example.srs.controller;

import com.example.srs.auth.AuthHelper;
import com.example.srs.auth.LoginUser;
import com.example.srs.common.Result;
import com.example.srs.domain.Reservation;
import com.example.srs.domain.Room;
import com.example.srs.domain.Seat;
import com.example.srs.service.ReservationService;
import com.example.srs.service.RoomService;
import com.example.srs.service.SeatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final SeatService seatService;
    private final RoomService roomService;

    public ReservationController(
            ReservationService reservationService,
            SeatService seatService,
            RoomService roomService) {
        this.reservationService = reservationService;
        this.seatService = seatService;
        this.roomService = roomService;
    }

    @PostMapping("/reserve")
    public Result<String> reserve(
            @RequestParam Long seatId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Integer timeSlot,
            HttpSession session) {

        LoginUser loginUser = AuthHelper.requireLoginUser(session);
        String message = reservationService.reserveSeat(loginUser.getStudentId(), seatId, date, timeSlot);
        if ("success".equals(message)) {
            return Result.success("预约成功，请在规定签到时间内完成签到。", null);
        }
        return Result.error(message);
    }

    @PostMapping("/checkIn")
    public Result<String> checkIn(@RequestParam Long reservationId, HttpSession session) {
        LoginUser loginUser = AuthHelper.requireLoginUser(session);
        String message = reservationService.checkIn(reservationId, loginUser.getStudentId());
        if ("success".equals(message)) {
            return Result.success("签到成功，祝你学习顺利。", null);
        }
        return Result.error(message);
    }

    @PostMapping("/cancel")
    public Result<String> cancel(@RequestParam Long reservationId, HttpSession session) {
        LoginUser loginUser = AuthHelper.requireLoginUser(session);
        boolean success = reservationService.cancelReservation(reservationId, loginUser.getStudentId());
        if (success) {
            return Result.success("已成功取消预约，座位已释放。", null);
        }
        return Result.error("取消失败");
    }

    @GetMapping("/my")
    public Result<List<Reservation>> myReservations(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) Integer status,
            HttpSession session) {
        LoginUser loginUser = AuthHelper.requireLoginUser(session);
        List<Reservation> reservations = reservationService.lambdaQuery()
                .eq(Reservation::getStudentId, loginUser.getStudentId())
                .eq(date != null, Reservation::getReservationDate, date)
                .eq(status != null, Reservation::getStatus, status)
                .orderByDesc(Reservation::getReservationDate)
                .orderByDesc(Reservation::getCreateTime)
                .list();
        enrichReservationDisplay(reservations);
        return Result.success(reservations);
    }

    private void enrichReservationDisplay(List<Reservation> reservations) {
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
