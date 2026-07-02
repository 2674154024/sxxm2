package com.parttime.admin.service;

import java.util.List;
import java.util.Map;

public interface SearchService {
    List<Map<String, Object>> searchMenus(String keyword);
}
