package com.example.srs.task;

import com.example.srs.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationOverdueTask {

    private final ReservationService reservationService;

    @Scheduled(fixedDelayString = "${srs.reservation.overdue-scan-delay-ms:60000}")
    public void processOverdueReservations() {
        int processedCount = reservationService.processOverdueReservations(LocalDateTime.now());
        if (processedCount > 0) {
            log.info("Processed {} overdue reservations automatically.", processedCount);
        }
    }
}
