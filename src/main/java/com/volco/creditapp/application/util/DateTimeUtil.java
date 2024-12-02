package com.volco.creditapp.application.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateTimeUtil {
    public static Instant toInstant(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
}
