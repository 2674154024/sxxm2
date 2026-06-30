package com.parttime.im.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import com.parttime.im.dto.request.InviteInterviewRequest;
import com.parttime.im.dto.response.InviteInterviewResponse;
import com.parttime.im.service.ImService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/enterprise")
public class EnterpriseImController {

    @Autowired
    private ImService imService;

    @PostMapping("/invite-interview")
    @AuditLog(module = "即时通讯", action = "邀请面试")
    public R<InviteInterviewResponse> inviteInterview(@Valid @RequestBody InviteInterviewRequest request) {
        InviteInterviewResponse response = imService.inviteInterview(request.getApplyId(), request.getInterviewTime());
        return R.ok(response);
    }
}