package com.endava.cats.generator.format.impl;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@QuarkusTest
class InvalidDateFormatGeneratorTest {

    @Test
    void givenADateFormatGeneratorStrategy_whenGettingTheAlmostValidValue_thenTheValueIsReturnedAsExpected() {
        InvalidDateFormatGenerator strategy = new InvalidDateFormatGenerator();
        Assertions.assertThat(strategy.getAlmostValidValue()).isEqualTo(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now(ZoneId.systemDefault())));
    }


    @Test
    void givenADateFormatGeneratorStrategy_whenGettingTheTotallyWrongValue_thenTheValueIsReturnedAsExpected() {
        InvalidDateFormatGenerator strategy = new InvalidDateFormatGenerator();
        Assertions.assertThat(strategy.getTotallyWrongValue()).isEqualTo("1000-07-21");
    }
}