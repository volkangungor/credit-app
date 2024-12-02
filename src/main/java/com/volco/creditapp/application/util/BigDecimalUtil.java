package com.volco.creditapp.application.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {

    public static final int PRECISION = 30;
    public static final int SCALE = 4;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static BigDecimal setScale(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal divide(BigDecimal a, int b) {
        return setScale(a.divide(BigDecimal.valueOf(b), SCALE, ROUNDING_MODE));
    }
}