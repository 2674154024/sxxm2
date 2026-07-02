package com.parttime.admin.service.impl;

import com.parttime.admin.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    private static final List<Map<String, Object>> ADMIN_MENUS = new ArrayList<>();
    private static final List<Map<String, Object>> ENTERPRISE_MENUS = new ArrayList<>();

    static {
        ADMIN_MENUS.add(createMenu("/admin/audit/enterprise", "企业审核", "Briefcase"));
        ADMIN_MENUS.add(createMenu("/admin/audit/job", "岗位审核", "Briefcase"));
        ADMIN_MENUS.add(createMenu("/admin/risk/complaint", "投诉工单", "CircleCheck"));
        ADMIN_MENUS.add(createMenu("/admin/risk/dashboard", "风控看板", "CircleCheck"));
        ADMIN_MENUS.add(createMenu("/admin/operation/report", "数据报表", "TrendCharts"));
        ADMIN_MENUS.add(createMenu("/admin/operation/notification", "推送管理", "TrendCharts"));
        ADMIN_MENUS.add(createMenu("/admin/finance/settlement", "薪资发放", "Wallet"));
        ADMIN_MENUS.add(createMenu("/admin/finance/report", "财务报表", "Wallet"));
        ADMIN_MENUS.add(createMenu("/admin/system/config", "系统配置", "Setting"));
        ADMIN_MENUS.add(createMenu("/admin/system/roles", "角色权限", "Setting"));
        ADMIN_MENUS.add(createMenu("/admin/system/audit-log", "审计日志", "Setting"));

        ENTERPRISE_MENUS.add(createMenu("/enterprise/dashboard", "数据看板", "TrendCharts"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/job/list", "岗位列表", "Briefcase"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/job/publish", "发布岗位", "Briefcase"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/job/batch-import", "批量导入", "Briefcase"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/talent/apply", "投递管理", "UserFilled"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/talent/library", "人才库", "UserFilled"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/schedule", "排班管理", "Calendar"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/salary/calculate", "薪资核算", "Wallet"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/salary/records", "发放记录", "Wallet"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/finance/statement", "对账单", "Files"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/finance/invoice", "发票管理", "Files"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/agreement", "协议管理", "Files"));
        ENTERPRISE_MENUS.add(createMenu("/enterprise/settings", "设置", "Setting"));
    }

    private static Map<String, Object> createMenu(String path, String name, String icon) {
        Map<String, Object> menu = new HashMap<>();
        menu.put("path", path);
        menu.put("name", name);
        menu.put("icon", icon);
        return menu;
    }

    @Override
    public List<Map<String, Object>> searchMenus(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String lowerKeyword = keyword.toLowerCase().trim();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> menu : ADMIN_MENUS) {
            String name = (String) menu.get("name");
            if (name.toLowerCase().contains(lowerKeyword)) {
                results.add(menu);
            }
        }

        for (Map<String, Object> menu : ENTERPRISE_MENUS) {
            String name = (String) menu.get("name");
            if (name.toLowerCase().contains(lowerKeyword)) {
                results.add(menu);
            }
        }

        return results;
    }
}
