package com.volco.creditapp.application.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.volco.creditapp.application.util.BigDecimalUtil.SCALE;

public class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        BigDecimal value = new BigDecimal(p.getText());
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}