package com.parttime.common.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class AmountUtilTest {

    @Test
    public void testAdd() {
        BigDecimal a = new BigDecimal("10.50");
        BigDecimal b = new BigDecimal("20.30");
        assertEquals(new BigDecimal("30.80"), AmountUtil.add(a, b));
    }

    @Test
    public void testSubtract() {
        BigDecimal a = new BigDecimal("30.80");
        BigDecimal b = new BigDecimal("10.50");
        assertEquals(new BigDecimal("20.30"), AmountUtil.subtract(a, b));
    }

    @Test
    public void testMultiply() {
        BigDecimal a = new BigDecimal("17.00");
        BigDecimal b = new BigDecimal("8.5");
        assertEquals(new BigDecimal("144.50"), AmountUtil.multiply(a, b));
    }

    @Test
    public void testDivide() {
        BigDecimal a = new BigDecimal("100.00");
        BigDecimal b = new BigDecimal("4");
        assertEquals(new BigDecimal("25.00"), AmountUtil.divide(a, b));
    }

    @Test
    public void testDivideByZero() {
        assertThrows(IllegalArgumentException.class, () -> AmountUtil.divide(new BigDecimal("100"), BigDecimal.ZERO));
    }

    @Test
    public void testCalcTax() {
        assertEquals(0, AmountUtil.calcTax(new BigDecimal("50000")).compareTo(new BigDecimal("0.00")));
        assertEquals(0, AmountUtil.calcTax(new BigDecimal("72000")).compareTo(new BigDecimal("360.00")));
        assertEquals(0, AmountUtil.calcTax(new BigDecimal("160000")).compareTo(new BigDecimal("7480.00")));
    }

    @Test
    public void testIsValidHourlyWage() {
        assertTrue(AmountUtil.isValidHourlyWage(new BigDecimal("17")));
        assertTrue(AmountUtil.isValidHourlyWage(new BigDecimal("20")));
        assertFalse(AmountUtil.isValidHourlyWage(new BigDecimal("16")));
        assertFalse(AmountUtil.isValidHourlyWage(null));
    }

    @Test
    public void testRoundWorkHours() {
        assertEquals(0, AmountUtil.roundWorkHours(new BigDecimal("8.46")).compareTo(new BigDecimal("8.5")));
        assertEquals(0, AmountUtil.roundWorkHours(new BigDecimal("8.2")).compareTo(new BigDecimal("8.2")));
        assertEquals(0, AmountUtil.roundWorkHours(null).compareTo(new BigDecimal("0.0")));
    }
}