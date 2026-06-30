package com.parttime.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parttime.common.annotation.AuditLog;
import com.parttime.common.exception.BusinessException;
import com.parttime.common.util.AesUtil;
import com.parttime.user.dto.request.StudentProfileUpdateRequest;
import com.parttime.user.dto.request.StudentRegisterRequest;
import com.parttime.user.dto.response.ResumeResponse;
import com.parttime.user.dto.response.StudentProfileResponse;
import com.parttime.user.entity.StudentEntity;
import com.parttime.user.mapper.StudentMapper;
import com.parttime.user.service.StudentService;
import com.parttime.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    @Override
    @AuditLog(module = "学生认证", action = "实名认证")
    @Transactional
    public void register(StudentRegisterRequest request, String userId) {
        StudentEntity student = studentMapper.selectById(userId);

        if (student == null) {
            throw new BusinessException("用户不存在");
        }

        if (student.getVerifyStatus() != null && student.getVerifyStatus() == 2) {
            throw new BusinessException(409, "已完成实名认证，无需重复认证");
        }

        int verifyStatus = mockXuexinVerification(request.getStudentNo(), request.getIdCard()) ? 2 : 3;

        student.setRealName(request.getRealName());
        student.setStudentNo(request.getStudentNo());
        student.setSchoolId(Long.parseLong(request.getSchoolId()));
        student.setIdCardEncrypt(AesUtil.encrypt(request.getIdCard()));
        student.setPhoneEncrypt(AesUtil.encrypt(request.getPhone()));
        student.setVerifyStatus(verifyStatus);

        studentMapper.updateById(student);

        log.info("学生实名认证完成: userId={}, verifyStatus={}", userId, verifyStatus);
    }

    @Override
    @AuditLog(module = "学生管理", action = "查看个人信息")
    public StudentProfileResponse getProfile(String userId) {
        StudentEntity student = studentMapper.selectById(userId);

        if (student == null) {
            throw new BusinessException("用户不存在");
        }

        return StudentProfileResponse.builder()
                .userId(student.getId())
                .realName(student.getRealName())
                .studentNo(student.getStudentNo())
                .schoolId(student.getSchoolId())
                .phone(DesensitizeUtil.desensitizePhone(AesUtil.decrypt(student.getPhoneEncrypt())))
                .idCard(DesensitizeUtil.desensitizeIdCard(AesUtil.decrypt(student.getIdCardEncrypt())))
                .avatarUrl(student.getAvatarUrl())
                .verifyStatus(student.getVerifyStatus())
                .creditScore(student.getCreditScore())
                .availableTime(student.getAvailableTime())
                .skillTags(student.getSkillTags())
                .build();
    }

    @Override
    @AuditLog(module = "学生管理", action = "修改个人信息")
    @Transactional
    public void updateProfile(String userId, StudentProfileUpdateRequest request) {
        StudentEntity student = studentMapper.selectById(userId);

        if (student == null) {
            throw new BusinessException("用户不存在");
        }

        if (request.getRealName() != null) {
            student.setRealName(request.getRealName());
        }
        if (request.getAvatarUrl() != null) {
            student.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getAvailableTime() != null) {
            student.setAvailableTime(request.getAvailableTime());
        }
        if (request.getSkillTags() != null) {
            student.setSkillTags(request.getSkillTags());
        }

        studentMapper.updateById(student);

        log.info("学生个人信息更新: userId={}", userId);
    }

    @Override
    @AuditLog(module = "学生管理", action = "查看简历")
    public ResumeResponse getResume(String userId) {
        StudentEntity student = studentMapper.selectById(userId);

        if (student == null) {
            throw new BusinessException("用户不存在");
        }

        return ResumeResponse.builder()
                .userId(student.getId())
                .availableTime(student.getAvailableTime())
                .skillTags(student.getSkillTags())
                .creditScore(student.getCreditScore())
                .build();
    }

    @Override
    @AuditLog(module = "学生管理", action = "创建/更新简历")
    @Transactional
    public void saveResume(String userId, String availableTime, String skillTags) {
        StudentEntity student = studentMapper.selectById(userId);

        if (student == null) {
            throw new BusinessException("用户不存在");
        }

        student.setAvailableTime(availableTime);
        student.setSkillTags(skillTags);

        studentMapper.updateById(student);

        log.info("学生简历更新: userId={}", userId);
    }

    private boolean mockXuexinVerification(String studentNo, String idCard) {
        return true;
    }
}