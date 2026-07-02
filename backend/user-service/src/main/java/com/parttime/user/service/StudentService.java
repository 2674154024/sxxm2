package com.parttime.user.service;

import com.parttime.user.dto.request.ResumeRequest;
import com.parttime.user.dto.request.StudentProfileUpdateRequest;
import com.parttime.user.dto.request.StudentRegisterRequest;
import com.parttime.user.dto.response.ResumeResponse;
import com.parttime.user.dto.response.StudentProfileResponse;

public interface StudentService {

    void register(StudentRegisterRequest request, String userId);

    StudentProfileResponse getProfile(String userId);

    void updateProfile(String userId, StudentProfileUpdateRequest request);

    ResumeResponse getResume(String userId);

    void saveResume(String userId, ResumeRequest request);
}