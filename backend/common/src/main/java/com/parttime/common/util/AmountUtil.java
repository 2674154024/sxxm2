package com.parttime.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额计算工具类
 * <p>
 * 所有金额计算使用BigDecimal，禁止float/double，确保精度。
 * 默认精度为2位小数，采用四舍五入模式。
 * </p>
 * <p>
 * 适用场景：薪资计算、个税计算、工时计算、金额校验。
 * </p>
 */
public class AmountUtil {

    /**
     * 长沙最低时薪标准（元/小时）
     */
    public static final BigDecimal MIN_HOURLY_WAGE = new BigDecimal("17");
    private static final int DEFAULT_SCALE = 2;

    /**
     * 金额加法
     *
     * @param value1 被加数
     * @param value2 加数
     * @return 和（保留2位小数）
     */
    public static BigDecimal add(BigDecimal value1, BigDecimal value2) {
        return value1.add(value2).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 金额减法
     *
     * @param value1 被减数
     * @param value2 减数
     * @return 差（保留2位小数）
     */
    public static BigDecimal subtract(BigDecimal value1, BigDecimal value2) {
        return value1.subtract(value2).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 金额乘法
     *
     * @param value1 被乘数
     * @param value2 乘数
     * @return 积（保留2位小数）
     */
    public static BigDecimal multiply(BigDecimal value1, BigDecimal value2) {
        return value1.multiply(value2).setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 金额除法
     *
     * @param dividend 被除数
     * @param divisor  除数
     * @return 商（保留2位小数）
     * @throws IllegalArgumentException 除数为0时抛出
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        return dividend.divide(divisor, DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 计算个税（累计预扣法）
     * <p>
     * 税率表（简化版）：
     * - 累计收入 ≤ 60000: 税率0%
     * - 累计收入 ≤ 96000: 税率3%，速算扣除数0
     * - 累计收入 ≤ 204000: 税率10%，速算扣除数2520
     * - 累计收入 > 204000: 税率20%，速算扣除数16920
     * </p>
     *
     * @param cumulativeSalary 累计收入
     * @return 应缴税额（保留2位小数）
     */
    public static BigDecimal calcTax(BigDecimal cumulativeSalary) {
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal threshold = new BigDecimal("60000");

        if (cumulativeSalary.compareTo(threshold) <= 0) {
            tax = BigDecimal.ZERO;
        } else if (cumulativeSalary.compareTo(new BigDecimal("96000")) <= 0) {
            tax = cumulativeSalary.subtract(threshold).multiply(new BigDecimal("0.03"));
        } else if (cumulativeSalary.compareTo(new BigDecimal("204000")) <= 0) {
            tax = cumulativeSalary.subtract(threshold).multiply(new BigDecimal("0.10")).subtract(new BigDecimal("2520"));
        } else {
            tax = cumulativeSalary.subtract(threshold).multiply(new BigDecimal("0.20")).subtract(new BigDecimal("16920"));
        }

        if (tax.compareTo(BigDecimal.ZERO) < 0) {
            tax = BigDecimal.ZERO;
        }

        return tax.setScale(DEFAULT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 校验时薪是否符合长沙最低标准
     *
     * @param hourlyWage 时薪
     * @return true表示符合标准（≥17元/小时），false表示不符合
     */
    public static boolean isValidHourlyWage(BigDecimal hourlyWage) {
        return hourlyWage != null && hourlyWage.compareTo(MIN_HOURLY_WAGE) >= 0;
    }

    /**
     * 工时四舍五入（精确到0.5小时）
     *
     * @param hours 工时
     * @return 四舍五入后的工时（保留1位小数）
     */
    public static BigDecimal roundWorkHours(BigDecimal hours) {
        if (hours == null) {
            return BigDecimal.ZERO;
        }
        return hours.setScale(1, RoundingMode.HALF_UP);
    }
}