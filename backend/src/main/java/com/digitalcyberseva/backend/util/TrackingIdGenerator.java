package com.digitalcyberseva.backend.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class TrackingIdGenerator {

    public String generate() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "DCS-" + date + "-" + randomSuffix;
    }
}
