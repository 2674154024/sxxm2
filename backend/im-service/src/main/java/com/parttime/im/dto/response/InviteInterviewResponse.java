package com.parttime.im.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteInterviewResponse {

    private String applyId;

    private String trtcRoomId;

    private Long interviewTime;

    private String studentId;
}