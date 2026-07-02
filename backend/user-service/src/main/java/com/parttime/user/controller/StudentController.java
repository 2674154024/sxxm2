package com.parttime.user.controller;

import com.parttime.common.response.R;
import com.parttime.user.dto.request.ResumeRequest;
import com.parttime.user.dto.request.StudentProfileUpdateRequest;
import com.parttime.user.dto.request.StudentRegisterRequest;
import com.parttime.user.dto.response.ResumeResponse;
import com.parttime.user.dto.response.StudentProfileResponse;
import com.parttime.user.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/auth/register")
    public R<String> register(@Validated @RequestBody StudentRegisterRequest request,
                            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        studentService.register(request, userId);
        return R.ok("实名认证提交成功");
    }

    @GetMapping("/profile")
    public R<StudentProfileResponse> getProfile(@RequestAttribute("X-User-Id") String userId) {
        return R.ok(studentService.getProfile(userId));
    }

    @PutMapping("/profile")
    public R<String> updateProfile(@RequestAttribute("X-User-Id") String userId,
                                 @Validated @RequestBody StudentProfileUpdateRequest request) {
        studentService.updateProfile(userId, request);
        return R.ok("个人信息更新成功");
    }

    @GetMapping("/resume")
    public R<ResumeResponse> getResume(@RequestAttribute("X-User-Id") String userId) {
        return R.ok(studentService.getResume(userId));
    }

    @PostMapping("/resume")
    public R<String> saveResume(@RequestAttribute("X-User-Id") String userId,
                              @Validated @RequestBody ResumeRequest request) {
        studentService.saveResume(userId, request);
        return R.ok("简历保存成功");
    }

    @PutMapping("/resume")
    public R<String> updateResume(@RequestAttribute("X-User-Id") String userId,
                               @Validated @RequestBody ResumeRequest request) {
        studentService.saveResume(userId, request);
        return R.ok("简历更新成功");
    }
}