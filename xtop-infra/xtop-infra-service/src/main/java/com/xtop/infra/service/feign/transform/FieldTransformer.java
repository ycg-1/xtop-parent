package com.xtop.infra.service.feign.transform;

import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class FieldTransformer {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Object transform(Object value, String rule) {
        if (value == null) return null;
        switch (rule) {
            case "UPPER": return String.valueOf(value).toUpperCase();
            case "TIMESTAMP_TO_DATE":
                if (value instanceof Long) return SDF.format(new Date((Long) value));
            default: return value;
        }
    }
}