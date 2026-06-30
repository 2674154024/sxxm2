package com.parttime.complaint.service;

import com.parttime.complaint.dto.response.ComplaintResponse;
import com.parttime.complaint.dto.response.FeedbackResponse;

import java.util.List;

public interface ComplaintService {

    ComplaintResponse createComplaint(String studentId, String defendantId, String defendantType,
                                       String jobId, String complaintType, String complaintContent, String evidenceUrls);

    List<ComplaintResponse> getStudentComplaints(String studentId, Integer page, Integer size);

    List<ComplaintResponse> getAllComplaints(Integer status, Integer page, Integer size);

    ComplaintResponse handleComplaint(String complaintId, String action, java.math.BigDecimal amount,
                                       String handleResult, String handledBy);

    FeedbackResponse studentFeedback(String studentId, String toId, String jobId, Integer score, String comment);

    FeedbackResponse enterpriseFeedback(String enterpriseId, String toId, String jobId, Integer score, String comment);
}