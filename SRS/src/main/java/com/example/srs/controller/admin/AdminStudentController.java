package com.example.srs.controller.admin;

import com.example.srs.common.Result;
import com.example.srs.domain.User;
import com.example.srs.dto.StudentCreateRequest;
import com.example.srs.dto.StudentUpdateRequest;
import com.example.srs.service.ReservationService;
import com.example.srs.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/admin/student")
@RequiredArgsConstructor
public class AdminStudentController {

    private final UserService userService;
    private final ReservationService reservationService;

    @GetMapping("/list")
    public Result<List<User>> list(@RequestParam(required = false) String keyword) {
        String normalizedKeyword = normalize(keyword);
        List<User> users = userService.lambdaQuery()
                .eq(User::getRole, 0)
                .and(StringUtils.hasText(normalizedKeyword), wrapper -> wrapper
                        .like(User::getStudentId, normalizedKeyword)
                        .or()
                        .like(User::getRealName, normalizedKeyword))
                .orderByDesc(User::getCreateTime)
                .orderByDesc(User::getId)
                .list();
        return Result.success(users);
    }

    @PostMapping
    public Result<User> create(@Valid @RequestBody StudentCreateRequest request) {
        String studentId = request.getStudentId().trim();
        if (studentExists(studentId, null)) {
            return Result.error("\u5b66\u53f7\u5df2\u5b58\u5728");
        }

        User user = new User();
        user.setStudentId(studentId);
        user.setRealName(request.getRealName().trim());
        user.setPassword(request.getPassword().trim());
        user.setRole(0);
        userService.save(user);
        return Result.success("\u5b66\u751f\u8d26\u53f7\u5df2\u521b\u5efa", user);
    }

    @PutMapping("/{userId}")
    public Result<User> update(@PathVariable Long userId, @Valid @RequestBody StudentUpdateRequest request) {
        User user = userService.getById(userId);
        if (user == null || user.getRole() == null || user.getRole() != 0) {
            return Result.error("\u5b66\u751f\u8d26\u53f7\u4e0d\u5b58\u5728");
        }

        boolean hasUpdate = false;
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName().trim());
            hasUpdate = true;
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(request.getPassword().trim());
            hasUpdate = true;
        }
        if (!hasUpdate) {
            return Result.error("\u8bf7\u81f3\u5c11\u63d0\u4ea4\u4e00\u9879\u53ef\u66f4\u65b0\u5185\u5bb9");
        }

        userService.updateById(user);
        return Result.success("\u5b66\u751f\u8d26\u53f7\u5df2\u66f4\u65b0", user);
    }

    @DeleteMapping("/{userId}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Object> delete(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean force) {
        User user = userService.getById(userId);
        if (user == null || user.getRole() == null || user.getRole() != 0) {
            return Result.error("\u5b66\u751f\u8d26\u53f7\u4e0d\u5b58\u5728");
        }

        LocalDateTime now = LocalDateTime.now();
        long futureReservationCount = reservationService.countFutureActiveReservations(user.getStudentId(), now);
        if (futureReservationCount > 0 && !force) {
            return Result.conflict(
                    "\u8be5\u5b66\u751f\u5b58\u5728\u672a\u6765\u9884\u7ea6\uff0c\u786e\u8ba4\u5220\u9664\u540e\u5c06\u81ea\u52a8\u53d6\u6d88\u9884\u7ea6\u5e76\u91ca\u653e\u5ea7\u4f4d\u3002",
                    Map.of("futureReservationCount", futureReservationCount));
        }

        int releasedCount = reservationService.releaseFutureReservations(user.getStudentId(), now);
        userService.removeById(userId);

        if (releasedCount > 0) {
            return Result.success("\u5b66\u751f\u8d26\u53f7\u5df2\u5220\u9664\uff0c\u5e76\u5df2\u53d6\u6d88 " + releasedCount + " \u6761\u672a\u6765\u9884\u7ea6", null);
        }
        return Result.success("\u5b66\u751f\u8d26\u53f7\u5df2\u5220\u9664", null);
    }

    private boolean studentExists(String studentId, Long excludeUserId) {
        return userService.lambdaQuery()
                .eq(User::getStudentId, studentId)
                .eq(User::getRole, 0)
                .ne(excludeUserId != null, User::getId, excludeUserId)
                .count() > 0;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
