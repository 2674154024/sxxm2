package com.parttime.task.util;

import java.math.BigDecimal;

public class GeoUtil {

    private static final int EARTH_RADIUS = 6371;

    public static BigDecimal calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(EARTH_RADIUS * c * 1000);
    }

    public static BigDecimal roundToHalf(BigDecimal value) {
        BigDecimal half = BigDecimal.valueOf(0.5);
        return value.divide(half, 0, java.math.RoundingMode.HALF_UP).multiply(half);
    }
}