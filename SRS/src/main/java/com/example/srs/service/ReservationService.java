package com.example.srs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.srs.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ReservationService extends IService<Reservation> {

    /**
     * 发起预约。
     *
     * @param studentId 学号
     * @param seatId    座位ID
     * @param date      预约日期
     * @param timeSlot  时间段（1-上午，2-下午，3-晚上）
     * @return 预约结果提示
     */
    String reserveSeat(String studentId, Long seatId, LocalDate date, Integer timeSlot);

    /**
     * 学生签到。
     *
     * @param reservationId 预约记录ID
     * @param studentId     学号
     * @return 签到结果提示，成功时返回 success
     */
    String checkIn(Long reservationId, String studentId);

    /**
     * 学生取消预约。
     *
     * @param reservationId 预约记录ID
     * @param studentId     学号
     * @return 是否取消成功
     */
    boolean cancelReservation(Long reservationId, String studentId);

    /**
     * 标记预约为违约，并按规则自动拉黑。
     *
     * @param reservationId 预约记录ID
     * @return 是否处理成功
     */
    boolean markReservationViolated(Long reservationId);

    /**
     * 扫描并处理已过签到时限的预约。
     *
     * @param now 当前时间
     * @return 本次自动处理的违约数量
     */
    int processOverdueReservations(LocalDateTime now);

    long countFutureActiveReservations(String studentId, LocalDateTime now);

    int releaseFutureReservations(String studentId, LocalDateTime now);
}
