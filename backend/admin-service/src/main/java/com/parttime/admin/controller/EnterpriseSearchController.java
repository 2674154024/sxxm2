package com.parttime.admin.controller;

import com.parttime.admin.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/enterprise")
@RequiredArgsConstructor
public class EnterpriseSearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("keyword") String keyword) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> results = searchService.searchMenus(keyword);
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", results);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "搜索失败");
            response.put("data", null);
        }
        return ResponseEntity.ok(response);
    }
}
