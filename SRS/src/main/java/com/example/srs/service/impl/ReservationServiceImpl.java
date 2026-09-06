package com.example.srs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.srs.domain.Blacklist;
import com.example.srs.domain.Reservation;
import com.example.srs.mapper.ReservationMapper;
import com.example.srs.service.BlacklistService;
import com.example.srs.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private static final ConcurrentMap<String, ReentrantLock> RESERVATION_LOCKS = new ConcurrentHashMap<>();

    @Autowired
    private BlacklistService blacklistService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String reserveSeat(String studentId, Long seatId, LocalDate date, Integer timeSlot) {
        String userLockKey = buildUserLockKey(studentId, date, timeSlot);
        String seatLockKey = buildSeatLockKey(seatId, date, timeSlot);

        lockKeys(userLockKey, seatLockKey);
        try {
            return doReserveSeat(studentId, seatId, date, timeSlot);
        } finally {
            unlockKeys(userLockKey, seatLockKey);
        }
    }

    private String doReserveSeat(String studentId, Long seatId, LocalDate date, Integer timeSlot) {
        if (blacklistService.isUserBlacklisted(studentId)) {
            return "您目前处于违规封禁期，无法预约。";
        }

        TimeSlotRule rule = TimeSlotRule.from(timeSlot);
        if (rule == null) {
            return "预约时间段无效。";
        }

        String reserveTimeValidation = validateReserveTime(date, rule);
        if (reserveTimeValidation != null) {
            return reserveTimeValidation;
        }

        long userSlotCount = this.lambdaQuery()
                .eq(Reservation::getStudentId, studentId)
                .eq(Reservation::getReservationDate, date)
                .eq(Reservation::getTimeSlot, timeSlot)
                .in(Reservation::getStatus, 0, 1)
                .count();
        if (userSlotCount > 0) {
            return "同一天的同一时间段只能保留一条有效预约。";
        }

        long seatOccupiedCount = this.lambdaQuery()
                .eq(Reservation::getSeatId, seatId)
                .eq(Reservation::getReservationDate, date)
                .eq(Reservation::getTimeSlot, timeSlot)
                .in(Reservation::getStatus, 0, 1)
                .count();
        if (seatOccupiedCount > 0) {
            return "该座位在所选时段已被他人预约。";
        }

        Reservation reservation = new Reservation();
        reservation.setStudentId(studentId);
        reservation.setSeatId(seatId);
        reservation.setReservationDate(date);
        reservation.setTimeSlot(timeSlot);
        reservation.setStatus(0);
        this.save(reservation);

        return "success";
    }

    private String validateReserveTime(LocalDate date, TimeSlotRule rule) {
        LocalDate today = LocalDate.now();
        if (date == null) {
            return "预约日期不能为空。";
        }
        if (date.isBefore(today)) {
            return "不能预约今天之前的时间。";
        }
        if (date.isEqual(today) && !LocalTime.now().isBefore(rule.startTime())) {
            return "当前时间已过该时段开始时间，不能再预约当天该时段。";
        }
        return null;
    }

    private String buildUserLockKey(String studentId, LocalDate date, Integer timeSlot) {
        return "user:" + studentId + ":" + date + ":" + timeSlot;
    }

    private String buildSeatLockKey(Long seatId, LocalDate date, Integer timeSlot) {
        return "seat:" + seatId + ":" + date + ":" + timeSlot;
    }

    private void lockKeys(String... keys) {
        List<String> sortedKeys = new ArrayList<>(List.of(keys));
        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
            RESERVATION_LOCKS.computeIfAbsent(key, ignored -> new ReentrantLock()).lock();
        }
    }

    private void unlockKeys(String... keys) {
        List<String> sortedKeys = new ArrayList<>(List.of(keys));
        Collections.sort(sortedKeys, Collections.reverseOrder());
        for (String key : sortedKeys) {
            ReentrantLock lock = RESERVATION_LOCKS.get(key);
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String checkIn(Long reservationId, String studentId) {
        Reservation reservation = this.lambdaQuery()
                .eq(Reservation::getId, reservationId)
                .eq(Reservation::getStudentId, studentId)
                .one();
        if (reservation == null) {
            return "未找到对应预约记录。";
        }
        if (reservation.getStatus() != 0) {
            return "当前预约状态不允许签到。";
        }

        LocalDateTime now = LocalDateTime.now();
        TimeSlotRule rule = TimeSlotRule.from(reservation.getTimeSlot());
        if (rule == null) {
            return "预约时间段无效。";
        }

        if (reservation.getReservationDate().isBefore(now.toLocalDate())) {
            markReservationViolated(reservationId);
            return "预约已过期，系统已判定为违规。";
        }
        if (reservation.getReservationDate().isAfter(now.toLocalDate())) {
            return "未到预约日期，暂不能签到。";
        }

        LocalTime currentTime = now.toLocalTime();
        if (currentTime.isBefore(rule.startTime())) {
            return "未到该时段签到开始时间，暂不能签到。";
        }
        if (currentTime.isAfter(rule.deadlineTime())) {
            markReservationViolated(reservationId);
            return "已超过签到截止时间，系统已判定为违规。";
        }

        reservation.setStatus(1);
        this.updateById(reservation);
        return "success";
    }

    @Override
    public boolean cancelReservation(Long reservationId, String studentId) {
        return this.lambdaUpdate()
                .eq(Reservation::getId, reservationId)
                .eq(Reservation::getStudentId, studentId)
                .eq(Reservation::getStatus, 0)
                .set(Reservation::getStatus, 2)
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markReservationViolated(Long reservationId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null || reservation.getStatus() != 0) {
            return false;
        }

        reservation.setStatus(3);
        this.updateById(reservation);
        createBlacklistIfNeeded(reservation);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processOverdueReservations(LocalDateTime now) {
        List<Reservation> pendingReservations = this.lambdaQuery()
                .eq(Reservation::getStatus, 0)
                .le(Reservation::getReservationDate, now.toLocalDate())
                .list();

        int processedCount = 0;
        for (Reservation reservation : pendingReservations) {
            if (!isOverdue(reservation, now)) {
                continue;
            }
            if (markReservationViolated(reservation.getId())) {
                processedCount++;
            }
        }
        return processedCount;
    }

    @Override
    public long countFutureActiveReservations(String studentId, LocalDateTime now) {
        return findFutureActiveReservations(studentId, now).size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseFutureReservations(String studentId, LocalDateTime now) {
        List<Reservation> futureReservations = findFutureActiveReservations(studentId, now);
        if (futureReservations.isEmpty()) {
            return 0;
        }

        List<Long> reservationIds = futureReservations.stream()
                .map(Reservation::getId)
                .toList();

        this.lambdaUpdate()
                .in(Reservation::getId, reservationIds)
                .in(Reservation::getStatus, 0, 1)
                .set(Reservation::getStatus, 2)
                .update();
        return reservationIds.size();
    }

    private List<Reservation> findFutureActiveReservations(String studentId, LocalDateTime now) {
        if (studentId == null || studentId.isBlank()) {
            return List.of();
        }

        return this.lambdaQuery()
                .eq(Reservation::getStudentId, studentId)
                .in(Reservation::getStatus, 0, 1)
                .list()
                .stream()
                .filter(reservation -> isReservationInFuture(reservation, now))
                .toList();
    }

    private boolean isReservationInFuture(Reservation reservation, LocalDateTime now) {
        LocalDate reservationDate = reservation.getReservationDate();
        if (reservationDate == null) {
            return false;
        }
        if (reservationDate.isAfter(now.toLocalDate())) {
            return true;
        }
        if (!reservationDate.isEqual(now.toLocalDate())) {
            return false;
        }

        TimeSlotRule rule = TimeSlotRule.from(reservation.getTimeSlot());
        return rule != null && now.toLocalTime().isBefore(rule.startTime());
    }

    private boolean isOverdue(Reservation reservation, LocalDateTime now) {
        LocalDate reservationDate = reservation.getReservationDate();
        if (reservationDate.isBefore(now.toLocalDate())) {
            return true;
        }
        if (!reservationDate.isEqual(now.toLocalDate())) {
            return false;
        }

        TimeSlotRule rule = TimeSlotRule.from(reservation.getTimeSlot());
        if (rule == null) {
            return false;
        }
        return now.toLocalTime().isAfter(rule.deadlineTime());
    }

    private void createBlacklistIfNeeded(Reservation reservation) {
        if (blacklistService.isUserBlacklisted(reservation.getStudentId())) {
            return;
        }

        Blacklist blacklist = new Blacklist();
        blacklist.setStudentId(reservation.getStudentId());
        blacklist.setReason(buildViolationReason(reservation));
        blacklist.setViolationDate(LocalDateTime.now());
        blacklist.setStatus(1);
        blacklistService.save(blacklist);
    }

    private String buildViolationReason(Reservation reservation) {
        String slotDesc = switch (reservation.getTimeSlot()) {
            case 1 -> "上午";
            case 2 -> "下午";
            case 3 -> "晚上";
            default -> "未知时段";
        };
        return reservation.getReservationDate() + " " + slotDesc + "预约未签到";
    }

    private record TimeSlotRule(LocalTime startTime, LocalTime endTime, LocalTime deadlineTime) {

        private static TimeSlotRule from(Integer timeSlot) {
            if (timeSlot == null) {
                return null;
            }
            return switch (timeSlot) {
                case 1 -> new TimeSlotRule(LocalTime.of(8, 0), LocalTime.of(12, 0), LocalTime.of(8, 30));
                case 2 -> new TimeSlotRule(LocalTime.of(14, 0), LocalTime.of(17, 0), LocalTime.of(14, 30));
                case 3 -> new TimeSlotRule(LocalTime.of(18, 0), LocalTime.of(22, 0), LocalTime.of(18, 30));
                default -> null;
            };
        }
    }
}
