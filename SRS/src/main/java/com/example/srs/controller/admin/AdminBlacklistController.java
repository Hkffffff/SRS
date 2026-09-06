package com.example.srs.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.srs.common.PageResult;
import com.example.srs.common.Result;
import com.example.srs.domain.Blacklist;
import com.example.srs.domain.User;
import com.example.srs.dto.BlacklistCreateRequest;
import com.example.srs.service.BlacklistService;
import com.example.srs.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/admin/blacklist")
@RequiredArgsConstructor
public class AdminBlacklistController {

    private final BlacklistService blacklistService;
    private final UserService userService;

    @GetMapping("/list")
    public Result<PageResult<Blacklist>> list(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) @Min(value = 0, message = "Invalid status") @Max(value = 1, message = "Invalid status") Integer status,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be >= 1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be >= 1") @Max(value = 100, message = "Page size must be <= 100") long pageSize) {
        String normalizedStudentId = studentId == null ? null : studentId.trim();
        Page<Blacklist> records = blacklistService.lambdaQuery()
                .eq(normalizedStudentId != null && !normalizedStudentId.isBlank(), Blacklist::getStudentId, normalizedStudentId)
                .eq(status != null, Blacklist::getStatus, status)
                .orderByDesc(Blacklist::getViolationDate)
                .orderByDesc(Blacklist::getId)
                .page(new Page<>(page, pageSize));
        return Result.success(PageResult.from(records));
    }

    @PostMapping
    public Result<Blacklist> create(@Valid @RequestBody BlacklistCreateRequest request) {
        String studentId = request.getStudentId().trim();
        if (userService.lambdaQuery().eq(User::getStudentId, studentId).count() == 0) {
            return Result.error("User not found");
        }

        long activeCount = blacklistService.lambdaQuery()
                .eq(Blacklist::getStudentId, studentId)
                .eq(Blacklist::getStatus, 1)
                .count();
        if (activeCount > 0) {
            return Result.error("The student is already blacklisted");
        }

        Blacklist blacklist = new Blacklist();
        blacklist.setStudentId(studentId);
        blacklist.setReason(request.getReason().trim());
        blacklist.setViolationDate(LocalDateTime.now());
        blacklist.setStatus(1);
        blacklistService.save(blacklist);
        return Result.success("Blacklist record created", blacklist);
    }

    @PutMapping("/{blacklistId}/release")
    public Result<String> release(@PathVariable Long blacklistId) {
        Blacklist blacklist = blacklistService.getById(blacklistId);
        if (blacklist == null) {
            return Result.error("Blacklist record not found");
        }
        if (blacklist.getStatus() == 0) {
            return Result.error("Blacklist record already released");
        }

        blacklist.setStatus(0);
        blacklistService.updateById(blacklist);
        return Result.success("Blacklist released", null);
    }
}
