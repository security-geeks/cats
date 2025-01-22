package com.endava.cats.generator.format.impl;

import com.endava.cats.generator.format.api.DataFormat;
import com.endava.cats.generator.format.api.OpenAPIFormat;
import com.endava.cats.generator.format.api.PropertySanitizer;
import com.endava.cats.generator.format.api.ValidDataFormatGenerator;
import com.endava.cats.util.CatsUtil;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Locale;

@Singleton
public class LanguageGenerator implements ValidDataFormatGenerator, OpenAPIFormat {
    private static final List<String> ALL_LANGUAGES = List.of(Locale.getISOCountries());

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "language".equalsIgnoreCase(PropertySanitizer.sanitize(format)) ||
                PropertySanitizer.sanitize(propertyName).endsWith("language");
    }

    @Override
    public List<String> matchingFormats() {
        return List.of("language");
    }

    @Override
    public Object generate(Schema<?> schema) {
        String generated = CatsUtil.selectRandom(ALL_LANGUAGES);

        return DataFormat.matchesPatternOrNull(schema, generated);
    }
}
