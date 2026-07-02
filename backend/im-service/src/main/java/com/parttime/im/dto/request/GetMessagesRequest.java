package com.parttime.im.dto.request;

import lombok.Data;

@Data
public class GetMessagesRequest {

    private String targetId;

    private Integer page = 1;

    private Integer size = 20;
}