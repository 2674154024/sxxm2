import json
import re
import os
from playwright.sync_api import sync_playwright, Page, ConsoleMessage

def is_api_error(text: str) -> bool:
    api_error_patterns = [
        r'Failed to load resource',
        r'Internal Server Error',
        r'系统繁忙',
        r'加载.*失败',
        r'网络错误',
        r'request failed',
        r'500 \(',
        r'404 \(',
        r'Network Error',
        r'timeout',
    ]
    for pattern in api_error_patterns:
        if re.search(pattern, text, re.IGNORECASE):
            return True
    return False

def test_page_detailed(page: Page, url: str, page_name: str):
    print(f"\n{'='*60}")
    print(f"测试页面: {page_name}")
    print(f"URL: {url}")
    print(f"{'='*60}")
    
    all_console = []
    real_errors = []
    real_warnings = []
    api_errors = []
    
    def console_handler(msg: ConsoleMessage):
        entry = {
            'type': msg.type,
            'text': msg.text,
            'location': str(msg.location) if msg.location else None
        }
        all_console.append(entry)
        
        if msg.type == 'error':
            if is_api_error(msg.text):
                api_errors.append(msg.text)
            else:
                real_errors.append(msg.text)
        elif msg.type == 'warning':
            if not is_api_error(msg.text):
                real_warnings.append(msg.text)
    
    page.on('console', console_handler)
    
    page_errors = []
    def page_error_handler(error):
        page_errors.append(str(error))
    
    page.on('pageerror', page_error_handler)
    
    try:
        page.goto(url, wait_until='networkidle', timeout=15000)
        page.wait_for_timeout(2000)
        
        content = page.content()
        body_text = page.inner_text('body')
        
        has_layout = page.locator('.admin-layout, .enterprise-layout, .login-container').count() > 0
        has_sidebar = page.locator('.sidebar, aside').count() > 0
        has_router_view = page.locator('.content-wrapper, main').count() > 0
        
        is_blank = len(body_text.strip()) < 50
        
        page_title = page.title()
        
        buttons = page.locator('button').count()
        inputs = page.locator('input, textarea, select').count()
        tables = page.locator('table').count()
        cards = page.locator('.el-card, .card').count()
        
        has_loading = page.locator('.el-loading, .loading').count() > 0
        has_empty_state = page.locator('.el-empty, .empty').count() > 0
        
        screenshot_path = f'd:\\sxxm2\\screenshots\\detail_{page_name.replace("/", "_")}.png'
        page.screenshot(path=screenshot_path, full_page=True)
        
        status = '正常'
        issues = []
        
        if is_blank:
            status = '空白页面'
            issues.append('页面内容为空')
        
        if page_errors:
            status = '有JS错误'
            for err in page_errors[:3]:
                issues.append(f'JS运行时错误: {err[:200]}')
        
        if real_errors:
            if status == '正常':
                status = '有控制台错误'
            for err in real_errors[:3]:
                issues.append(f'控制台错误: {err[:200]}')
        
        if not has_layout and not is_blank:
            issues.append('未检测到布局结构')
        
        result = {
            'page': page_name,
            'url': url,
            'status': status,
            'page_title': page_title,
            'is_blank': is_blank,
            'has_layout': has_layout,
            'has_sidebar': has_sidebar,
            'real_js_errors': len(page_errors),
            'real_console_errors': len(real_errors),
            'real_console_warnings': len(real_warnings),
            'api_errors': len(api_errors),
            'total_console': len(all_console),
            'interactive_elements': {
                'buttons': buttons,
                'inputs': inputs,
                'tables': tables,
                'cards': cards,
            },
            'has_loading': has_loading,
            'has_empty_state': has_empty_state,
            'issues': issues,
            'real_error_samples': real_errors[:5],
            'page_error_samples': page_errors[:5],
            'warning_samples': real_warnings[:5],
            'screenshot': screenshot_path
        }
        
        print(f"状态: {status}")
        print(f"页面标题: {page_title}")
        print(f"布局检测: {'是' if has_layout else '否'}")
        print(f"空白页面: {'是' if is_blank else '否'}")
        print(f"---")
        print(f"真实JS运行时错误: {len(page_errors)}")
        print(f"真实控制台错误: {len(real_errors)}")
        print(f"控制台警告(非API): {len(real_warnings)}")
        print(f"API请求错误(正常): {len(api_errors)}")
        print(f"---")
        print(f"交互元素:")
        print(f"  按钮: {buttons}")
        print(f"  输入框: {inputs}")
        print(f"  表格: {tables}")
        print(f"  卡片: {cards}")
        
        if issues:
            print("\n发现的问题:")
            for issue in issues:
                print(f"  - {issue}")
        
        if real_errors:
            print("\n真实控制台错误示例:")
            for err in real_errors[:3]:
                print(f"  - {err[:150]}")
        
        if page_errors:
            print("\nJS运行时错误示例:")
            for err in page_errors[:3]:
                print(f"  - {err[:150]}")
        
        if real_warnings:
            print("\n警告示例:")
            for warn in real_warnings[:3]:
                print(f"  - {warn[:150]}")
        
        return result
        
    except Exception as e:
        print(f"访问失败: {str(e)}")
        return {
            'page': page_name,
            'url': url,
            'status': '访问失败',
            'error': str(e),
            'issues': [f'访问失败: {str(e)}']
        }
    finally:
        page.remove_listener('console', console_handler)
        page.remove_listener('pageerror', page_error_handler)

def main():
    os.makedirs('d:\\sxxm2\\screenshots', exist_ok=True)
    
    with sync_playwright() as p:
        edge_path = r'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
        browser = p.chromium.launch(headless=True, executable_path=edge_path)
        context = browser.new_context(
            viewport={'width': 1440, 'height': 900}
        )
        page = context.new_page()
        
        print("=" * 60)
        print("设置登录状态 (管理后台 - 超级管理员)")
        print("=" * 60)
        
        context.add_init_script("""
            localStorage.setItem('token', 'mock-token-admin');
            localStorage.setItem('admin', JSON.stringify({
                admin_id: '1',
                username: 'admin',
                real_name: '超级管理员',
                phone: '13800138000',
                role_type: 5,
                avatar: ''
            }));
            localStorage.setItem('roleType', '5');
        """)
        
        admin_pages = [
            ('/admin/audit/enterprise', '管理后台-企业审核'),
            ('/admin/audit/job', '管理后台-岗位审核'),
            ('/admin/risk/complaint', '管理后台-投诉工单'),
            ('/admin/risk/dashboard', '管理后台-风控看板'),
            ('/admin/finance/settlement', '管理后台-结算管理'),
            ('/admin/finance/report', '管理后台-财务报表'),
            ('/admin/operation/report', '管理后台-运营报表'),
            ('/admin/operation/notification', '管理后台-通知管理'),
            ('/admin/system/config', '管理后台-系统配置'),
            ('/admin/system/roles', '管理后台-角色权限'),
            ('/admin/system/audit-log', '管理后台-审计日志'),
        ]
        
        print("\n" + "=" * 60)
        print("第一部分: 管理后台页面测试")
        print("=" * 60)
        
        admin_results = []
        for url_path, name in admin_pages:
            result = test_page_detailed(page, f'http://localhost:5173{url_path}', name)
            admin_results.append(result)
        
        print("\n" + "=" * 60)
        print("第二部分: 企业端页面测试")
        print("=" * 60)
        
        context.clear_cookies()
        context.add_init_script("""
            localStorage.clear();
            localStorage.setItem('token', 'mock-token-enterprise');
            localStorage.setItem('enterprise', JSON.stringify({
                enterprise_id: '1',
                enterprise_name: '茶颜悦色餐饮有限公司',
                credit_code: '91430100MA4L123456',
                legal_person: '张三',
                phone: '13800138000',
                verify_status: 2,
                avatar: ''
            }));
        """)
        
        enterprise_pages = [
            ('/enterprise/dashboard', '企业端-数据看板'),
            ('/enterprise/job/list', '企业端-岗位列表'),
            ('/enterprise/job/publish', '企业端-发布岗位'),
            ('/enterprise/job/batch-import', '企业端-批量导入'),
            ('/enterprise/talent/apply', '企业端-投递管理'),
            ('/enterprise/talent/library', '企业端-人才库'),
            ('/enterprise/schedule', '企业端-排班管理'),
            ('/enterprise/salary/calculate', '企业端-薪资核算'),
            ('/enterprise/salary/records', '企业端-发放记录'),
            ('/enterprise/finance/statement', '企业端-对账单'),
            ('/enterprise/finance/invoice', '企业端-发票管理'),
            ('/enterprise/agreement', '企业端-协议管理'),
            ('/enterprise/settings', '企业端-设置'),
        ]
        
        enterprise_results = []
        for url_path, name in enterprise_pages:
            result = test_page_detailed(page, f'http://localhost:5173{url_path}', name)
            enterprise_results.append(result)
        
        browser.close()
        
        all_results = admin_results + enterprise_results
        
        normal_count = sum(1 for r in all_results if r.get('status') == '正常')
        error_count = sum(1 for r in all_results if r.get('status') not in ['正常', '访问失败'])
        fail_count = sum(1 for r in all_results if r.get('status') == '访问失败')
        blank_count = sum(1 for r in all_results if r.get('is_blank'))
        
        all_issues = []
        for r in all_results:
            for issue in r.get('issues', []):
                all_issues.append(f"{r['page']}: {issue}")
        
        print("\n\n" + "=" * 60)
        print("测试总结")
        print("=" * 60)
        print(f"总页面数: {len(all_results)}")
        print(f"正常页面: {normal_count}")
        print(f"有问题页面: {error_count}")
        print(f"访问失败: {fail_count}")
        print(f"空白页面: {blank_count}")
        print(f"问题总数: {len(all_issues)}")
        
        if all_issues:
            print("\n所有问题列表:")
            for issue in all_issues:
                print(f"  - {issue}")
        
        report = {
            'summary': {
                'total': len(all_results),
                'normal': normal_count,
                'errors': error_count,
                'failed': fail_count,
                'blank': blank_count,
                'issue_count': len(all_issues),
            },
            'admin_pages': admin_results,
            'enterprise_pages': enterprise_results,
            'all_issues': all_issues
        }
        
        with open('d:\\sxxm2\\test_report_detailed.json', 'w', encoding='utf-8') as f:
            json.dump(report, f, ensure_ascii=False, indent=2)
        
        print(f"\n详细报告已保存到: d:\\sxxm2\\test_report_detailed.json")

if __name__ == '__main__':
    main()
