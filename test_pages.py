import json
import sys
from playwright.sync_api import sync_playwright, Page

results = []
issues = []

def log_console_messages(page: Page, page_name: str):
    messages = []
    def handler(msg):
        if msg.type in ['error', 'warning']:
            messages.append({
                'type': msg.type,
                'text': msg.text,
                'location': str(msg.location) if msg.location else None
            })
    page.on('console', handler)
    return messages, handler

def test_page(page: Page, url: str, page_name: str, expected_elements=None):
    print(f"\n{'='*60}")
    print(f"测试页面: {page_name}")
    print(f"URL: {url}")
    print(f"{'='*60}")
    
    console_errors = []
    console_warnings = []
    
    def console_handler(msg):
        if msg.type == 'error':
            console_errors.append(msg.text)
        elif msg.type == 'warning':
            console_warnings.append(msg.text)
    
    page.on('console', console_handler)
    
    try:
        page.goto(url, wait_until='networkidle', timeout=15000)
        page.wait_for_timeout(2000)
        
        content = page.content()
        has_content = len(content.strip()) > 1000
        
        body_text = page.inner_text('body')
        is_blank = len(body_text.strip()) < 50
        
        screenshot_path = f'd:\\sxxm2\\screenshots\\{page_name.replace("/", "_")}.png'
        page.screenshot(path=screenshot_path, full_page=True)
        
        status = '正常'
        if is_blank:
            status = '空白页面'
            issues.append(f'{page_name}: 页面内容为空')
        
        if console_errors:
            status = '有错误'
            for err in console_errors[:5]:
                issues.append(f'{page_name} - 控制台错误: {err[:200]}')
        
        buttons = page.locator('button').count()
        inputs = page.locator('input').count()
        tables = page.locator('table').count()
        links = page.locator('a').count()
        
        result = {
            'page': page_name,
            'url': url,
            'status': status,
            'has_content': has_content,
            'console_errors': len(console_errors),
            'console_warnings': len(console_warnings),
            'buttons': buttons,
            'inputs': inputs,
            'tables': tables,
            'links': links,
            'screenshot': screenshot_path,
            'error_samples': console_errors[:3]
        }
        
        results.append(result)
        
        print(f"状态: {status}")
        print(f"控制台错误: {len(console_errors)} 个")
        print(f"控制台警告: {len(console_warnings)} 个")
        print(f"交互元素: 按钮={buttons}, 输入框={inputs}, 表格={tables}, 链接={links}")
        
        if console_errors:
            print("错误示例:")
            for err in console_errors[:3]:
                print(f"  - {err[:150]}")
        
        return result
        
    except Exception as e:
        print(f"访问失败: {str(e)}")
        issues.append(f'{page_name}: 访问失败 - {str(e)}')
        result = {
            'page': page_name,
            'url': url,
            'status': '访问失败',
            'error': str(e)
        }
        results.append(result)
        return result
    finally:
        page.remove_listener('console', console_handler)

def main():
    import os
    os.makedirs('d:\\sxxm2\\screenshots', exist_ok=True)
    
    with sync_playwright() as p:
        import os
        browser_paths = [
            (p.chromium, r'C:\Program Files\Google\Chrome\Application\chrome.exe'),
            (p.chromium, r'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe'),
            (p.chromium, os.path.expandvars(r'%LOCALAPPDATA%\Google\Chrome\Application\chrome.exe')),
            (p.chromium, r'C:\Program Files\Microsoft\Edge\Application\msedge.exe'),
            (p.chromium, r'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'),
        ]
        browser = None
        for browser_type, path in browser_paths:
            if os.path.exists(path):
                print(f"使用浏览器: {path}")
                browser = browser_type.launch(headless=True, executable_path=path)
                break
        
        if not browser:
            print("未找到系统浏览器，使用 Playwright 内置浏览器")
            browser = p.chromium.launch(headless=True)
        context = browser.new_context(
            viewport={'width': 1440, 'height': 900}
        )
        page = context.new_page()
        
        print("=" * 60)
        print("第一步: 登录管理后台")
        print("=" * 60)
        
        page.goto('http://localhost:5173/login', wait_until='networkidle')
        page.wait_for_timeout(1000)
        
        try:
            page.fill('input[type="text"]', 'admin')
            page.fill('input[type="password"]', '123456')
            page.click('button[type="submit"]')
            page.wait_for_timeout(3000)
        except:
            pass
        
        context.add_init_script("""
            localStorage.setItem('token', 'mock-token-admin');
            localStorage.setItem('roleType', 'admin');
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
        print("一、管理后台页面测试")
        print("=" * 60)
        
        for url_path, name in admin_pages:
            test_page(page, f'http://localhost:5173{url_path}', name)
        
        print("\n" + "=" * 60)
        print("二、企业端页面测试")
        print("=" * 60)
        
        context.add_init_script("""
            localStorage.setItem('token', 'mock-token-enterprise');
            localStorage.removeItem('roleType');
            localStorage.setItem('roleType', 'enterprise');
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
        
        for url_path, name in enterprise_pages:
            test_page(page, f'http://localhost:5173{url_path}', name)
        
        browser.close()
        
        print("\n\n" + "=" * 60)
        print("测试总结")
        print("=" * 60)
        
        normal_count = sum(1 for r in results if r['status'] == '正常')
        error_count = sum(1 for r in results if r['status'] != '正常')
        
        print(f"总页面数: {len(results)}")
        print(f"正常页面: {normal_count}")
        print(f"有问题页面: {error_count}")
        print(f"问题总数: {len(issues)}")
        
        print("\n问题列表:")
        for issue in issues:
            print(f"  - {issue}")
        
        with open('d:\\sxxm2\\test_report.json', 'w', encoding='utf-8') as f:
            json.dump({
                'results': results,
                'issues': issues,
                'summary': {
                    'total': len(results),
                    'normal': normal_count,
                    'errors': error_count,
                    'issue_count': len(issues)
                }
            }, f, ensure_ascii=False, indent=2)
        
        print(f"\n详细报告已保存到: d:\\sxxm2\\test_report.json")
        print(f"截图已保存到: d:\\sxxm2\\screenshots\\")

if __name__ == '__main__':
    main()
