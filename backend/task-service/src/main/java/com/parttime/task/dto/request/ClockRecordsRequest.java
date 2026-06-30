package com.parttime.task.dto.request;

import lombok.Data;

@Data
public class ClockRecordsRequest {

    private String jobId;

    private String date;

    private Integer page = 1;

    private Integer size = 20;
}