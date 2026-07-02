package com.parttime.user.controller;

import com.parttime.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1/safety")
@RequiredArgsConstructor
public class SafetyController {

    @GetMapping("/cases")
    public R<List<CaseDTO>> getCaseList() {
        List<CaseDTO> cases = Arrays.asList(
            new CaseDTO(1, "刷单诈骗", "常见骗局", "⚠️", "danger", "以\"足不出户，日赚300\"为诱饵，先返小利，后要求大额垫付，最终失联。"),
            new CaseDTO(2, "押金诈骗", "入职陷阱", "💸", "warning", "以\"服装费\"\"培训费\"等名义要求先交钱，交完后拉黑消失。"),
            new CaseDTO(3, "传销陷阱", "违法活动", "🔗", "danger", "以\"创业\"\"加盟\"为幌子，要求发展下线获取佣金，本质是传销。"),
            new CaseDTO(4, "信息泄露", "隐私风险", "🔐", "warning", "要求提供身份证、银行卡密码等敏感信息，用于盗刷或其他违法用途。"),
            new CaseDTO(5, "网络钓鱼", "账号安全", "🎣", "danger", "通过虚假网站或短信骗取账号密码，导致账号被盗、资金损失。"),
            new CaseDTO(6, "高薪诱惑", "求职陷阱", "💰", "warning", "以\"高薪日结\"为诱饵，实际工作内容与承诺不符或涉嫌违法。")
        );
        return R.ok(cases);
    }

    @GetMapping("/cases/{id}")
    public R<CaseDetailDTO> getCaseDetail(@PathVariable Integer id) {
        CaseDetailDTO detail = switch (id) {
            case 1 -> new CaseDetailDTO(
                1, "刷单诈骗", "常见骗局", "⚠️", "danger",
                "以\"足不出户，日赚300\"为诱饵，先返小利，后要求大额垫付，最终失联。",
                "李某是一名大学生，在网上看到\"刷单兼职，日赚300+\"的广告。对方要求她先垫付资金购买商品，承诺完成任务后返还本金并支付佣金。李某起初只投了50元，很快就收到了55元的返现。尝到甜头后，她陆续投入了8000元，然而这次对方以\"任务未完成\"为由拒绝返现，并要求继续充值。李某这才意识到被骗，报警后警方介入调查，但资金已被转移。",
                Arrays.asList("刷单本身就是违法行为", "任何要求先垫资的兼职都是诈骗", "低投入高回报的承诺不可信"),
                Arrays.asList("正规兼职不会要求你先交钱", "如果对方要求下载不知名APP，立即停止", "发现被骗后立即报警并保留证据")
            );
            case 2 -> new CaseDetailDTO(
                2, "押金诈骗", "入职陷阱", "💸", "warning",
                "以\"服装费\"\"培训费\"等名义要求先交钱，交完后拉黑消失。",
                "张某通过某平台找到一份\"商场导购\"兼职，对方称入职需要缴纳300元服装费和200元培训费，承诺工作满一个月后全额退还。张某交了钱后，对方又以\"办理上岗证\"为由要求再交100元。张某产生怀疑，要求退款，却发现已被对方拉黑。后经调查，该\"商场导购\"岗位根本不存在。",
                Arrays.asList("《劳动合同法》明确禁止用人单位收取押金", "服装费、培训费等名义收费都是违法的", "要求转账到个人账户的都是骗子"),
                Arrays.asList("入职前要求收费的一律拒绝", "通过正规平台寻找兼职，签订电子合同", "保留聊天记录和转账凭证")
            );
            case 3 -> new CaseDetailDTO(
                3, "传销陷阱", "违法活动", "🔗", "danger",
                "以\"创业\"\"加盟\"为幌子，要求发展下线获取佣金，本质是传销。",
                "王某的高中同学邀请她参加一个\"互联网创业项目\"，称只要投资3000元成为会员，就能获得推广佣金。王某加入后发现，所谓的\"创业项目\"根本没有实际产品，赚钱的方式只有发展下线。她意识到这是传销，但已经投入了5000元。后来该组织被警方取缔，王某的资金也无法追回。",
                Arrays.asList("要求发展下线获取佣金的可能是传销", "没有实际产品的\"创业项目\"要警惕", "高门槛入会、承诺快速致富的多是骗局"),
                Arrays.asList("了解项目是否有实际产品和盈利模式", "拒绝拉人头式的推广方式", "发现传销活动及时报警")
            );
            case 4 -> new CaseDetailDTO(
                4, "信息泄露", "隐私风险", "🔐", "warning",
                "要求提供身份证、银行卡密码等敏感信息，用于盗刷或其他违法用途。",
                "陈某在某兼职平台注册时，被要求上传身份证正反面照片和手持身份证照片。几天后，她收到银行短信通知，银行卡被消费了2万元。经调查，她的身份信息被不法分子用于注册网贷并盗刷。原来，该兼职平台存在信息泄露漏洞，她的个人信息被泄露并出售。",
                Arrays.asList("不要轻易上传身份证照片", "银行卡密码绝不透露给任何人", "警惕要求提供验证码的请求"),
                Arrays.asList("只在正规平台注册并完善隐私设置", "开启银行卡短信提醒和交易限额", "定期检查账户交易记录")
            );
            case 5 -> new CaseDetailDTO(
                5, "网络钓鱼", "账号安全", "🎣", "danger",
                "通过虚假网站或短信骗取账号密码，导致账号被盗、资金损失。",
                "赵某收到一条短信，声称她的兼职账户存在异常，需要点击链接验证身份。赵某点击链接后进入一个看似正规的登录页面，输入了账号和密码。随后她发现自己的账户被登录，钱包里的余额被转走。经调查，该链接是钓鱼网站，用于窃取用户信息。",
                Arrays.asList("不要点击陌生短信中的链接", "登录网站前确认网址是否正确", "不要在非官方渠道输入账号密码"),
                Arrays.asList("开启账户登录提醒", "使用手机验证码登录", "定期更换密码")
            );
            case 6 -> new CaseDetailDTO(
                6, "高薪诱惑", "求职陷阱", "💰", "warning",
                "以\"高薪日结\"为诱饵，实际工作内容与承诺不符或涉嫌违法。",
                "孙某看到一则\"日结500元\"的兼职广告，内容是\"数据录入\"。面试时对方要求先交200元保证金，承诺工作一周后退还。孙某工作三天后发现，所谓的\"数据录入\"其实是帮助诈骗分子发送垃圾短信。当她要求停止工作并退还保证金时，对方威胁要曝光她的个人信息。",
                Arrays.asList("薪资明显高于市场水平的要警惕", "工作内容模糊不清的可能是陷阱", "要求交保证金的都是骗子"),
                Arrays.asList("了解清楚工作内容再入职", "通过正规平台寻找兼职", "发现违法活动及时报警")
            );
            default -> new CaseDetailDTO(
                1, "刷单诈骗", "常见骗局", "⚠️", "danger",
                "以\"足不出户，日赚300\"为诱饵，先返小利，后要求大额垫付，最终失联。",
                "李某是一名大学生，在网上看到\"刷单兼职，日赚300+\"的广告。对方要求她先垫付资金购买商品，承诺完成任务后返还本金并支付佣金。李某起初只投了50元，很快就收到了55元的返现。尝到甜头后，她陆续投入了8000元，然而这次对方以\"任务未完成\"为由拒绝返现，并要求继续充值。李某这才意识到被骗，报警后警方介入调查，但资金已被转移。",
                Arrays.asList("刷单本身就是违法行为", "任何要求先垫资的兼职都是诈骗", "低投入高回报的承诺不可信"),
                Arrays.asList("正规兼职不会要求你先交钱", "如果对方要求下载不知名APP，立即停止", "发现被骗后立即报警并保留证据")
            );
        };
        return R.ok(detail);
    }

    public record CaseDTO(
        Integer id,
        String title,
        String type,
        String icon,
        String color,
        String desc
    ) {}

    public record CaseDetailDTO(
        Integer id,
        String title,
        String type,
        String icon,
        String color,
        String desc,
        String content,
        List<String> warning,
        List<String> tips
    ) {}
}