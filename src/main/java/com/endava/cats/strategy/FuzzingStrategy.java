package com.endava.cats.strategy;

import com.endava.cats.generator.simple.StringGenerator;
import com.endava.cats.util.JsonUtils;
import com.endava.cats.util.CatsUtil;
import com.endava.cats.util.FuzzingResult;
import com.endava.cats.util.WordUtils;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.swagger.v3.oas.models.media.Schema;
import lombok.Getter;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsulates various fuzzing strategies:
 * <ul>
 * <li>REPLACE - when the fuzzed value replaces the one generated by the OpenAPIModelGenerator</li>
 * <li>TRAIL - trails the current value with the given string</li>
 * <li>PREFIX - prefixes the current value with the given string</li>
 * <li>SKIP - doesn't do anything to the current value</li>
 * <li>NOOP - returns the given string</li>
 * </ul>
 */
@Getter
public abstract sealed class FuzzingStrategy permits InsertFuzzingStrategy, NoopFuzzingStrategy,
        PrefixFuzzingStrategy, ReplaceFuzzingStrategy, SkipFuzzingStrategy, TrailFuzzingStrategy {
    private static final Pattern ALL = Pattern.compile("^[\\p{C}\\p{Z}\\p{So}\\p{Sk}\\p{M}]+[\\p{C}\\p{Z}\\p{So}\\p{Sk}\\p{M}]*$");
    private static final Pattern WITHIN = Pattern.compile("([\\p{C}\\p{Z}\\p{So}\\p{Sk}\\p{M}]+|జ్ఞ\u200Cా|স্র\u200Cু)");

    /**
     * The actual data to be fuzzed.
     */
    protected Object data;

    /**
     * Creates and returns a {@link PrefixFuzzingStrategy} for prefix-based fuzzing.
     *
     * @return a FuzzingStrategy representing the prefix-based fuzzing strategy
     */
    public static FuzzingStrategy prefix() {
        return new PrefixFuzzingStrategy();
    }

    /**
     * Creates and returns a {@link NoopFuzzingStrategy} for noop-based fuzzing.
     *
     * @return a FuzzingStrategy representing the noop-based fuzzing strategy
     */
    public static FuzzingStrategy noop() {
        return new NoopFuzzingStrategy();
    }

    /**
     * Creates and returns a {@link ReplaceFuzzingStrategy} for replace-based fuzzing.
     *
     * @return a FuzzingStrategy representing the replace-based fuzzing strategy
     */
    public static FuzzingStrategy replace() {
        return new ReplaceFuzzingStrategy();
    }

    /**
     * Creates and returns a {@link SkipFuzzingStrategy} when skipping fuzzing.
     *
     * @return a FuzzingStrategy representing ignoring fuzzing
     */
    public static FuzzingStrategy skip() {
        return new SkipFuzzingStrategy();
    }

    /**
     * Creates and returns a {@link TrailFuzzingStrategy} for trail-based fuzzing.
     *
     * @return a FuzzingStrategy representing the trail-based fuzzing strategy
     */
    public static FuzzingStrategy trail() {
        return new TrailFuzzingStrategy();
    }

    /**
     * Creates and returns a {@link InsertFuzzingStrategy} for insert-based fuzzing.
     *
     * @return a FuzzingStrategy representing the insert-based fuzzing strategy
     */
    public static FuzzingStrategy insert() {
        return new InsertFuzzingStrategy();
    }

    /**
     * Merges a fuzzed value with a supplied value using the appropriate FuzzingStrategy.
     *
     * @param fuzzedValue   the fuzzed value obtained from a previous fuzzing operation
     * @param suppliedValue the value supplied to merge with the fuzzed value
     * @return the result of merging the fuzzed value with the supplied value using the appropriate FuzzingStrategy
     */
    public static Object mergeFuzzing(Object fuzzedValue, Object suppliedValue) {
        FuzzingStrategy currentStrategy = fromValue(fuzzedValue);

        return currentStrategy.process(suppliedValue);
    }

    /**
     * Determines and returns the appropriate FuzzingStrategy based on the provided value object.
     *
     * @param valueObject the value object for which to determine the FuzzingStrategy
     * @return the FuzzingStrategy determined based on the provided value object
     */
    public static FuzzingStrategy fromValue(Object valueObject) {
        String valueAsString = String.valueOf(valueObject);
        if (StringUtils.isBlank(valueAsString) || ALL.matcher(valueAsString).matches()) {
            return replace().withData(valueObject);
        }
        if (isUnicodeControlChar(valueAsString.charAt(0)) || isUnicodeWhitespace(valueAsString.charAt(0)) || isUnicodeOtherSymbol(valueAsString.charAt(0))) {
            return prefix().withData(replaceSpecialCharsWithEmpty(valueAsString));
        }
        if (isUnicodeControlChar(valueAsString.charAt(valueAsString.length() - 1)) || isUnicodeWhitespace(valueAsString.charAt(valueAsString.length() - 1)) || isUnicodeOtherSymbol(valueAsString.charAt(valueAsString.length() - 1))) {
            return trail().withData(replaceSpecialCharsWithEmpty(valueAsString));
        }
        if (isLargeString(valueAsString)) {
            return replace().withData(valueObject);
        }
        Matcher withinMatcher = WITHIN.matcher(valueAsString);
        if (withinMatcher.find()) {
            return insert().withData(withinMatcher.group());
        }

        return replace().withData(valueObject);
    }

    private static String replaceSpecialCharsWithEmpty(String value) {
        return value.replaceAll("[^\\p{Z}\\p{C}\\p{So}\\p{Sk}\\p{M}]+", "");
    }

    /**
     * Formats a given string by escaping Unicode whitespace, control characters, and other symbols.
     * If the input string is null, the method returns null.
     *
     * @param data the string to be formatted
     * @return the formatted string with escaped Unicode characters, or null if the input is null
     */
    public static String formatValue(String data) {
        if (data == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (isUnicodeWhitespace(c) || isUnicodeControlChar(c) || isUnicodeOtherSymbol(c)) {
                builder.append(String.format("\\u%04x", (int) c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static boolean isUnicodeControlChar(char c) {
        return Character.getType(c) == Character.CONTROL || Character.getType(c) == Character.FORMAT
                || Character.getType(c) == Character.PRIVATE_USE || Character.getType(c) == Character.SURROGATE;
    }

    private static boolean isUnicodeWhitespace(char c) {
        return Character.getType(c) == Character.LINE_SEPARATOR ||
                Character.getType(c) == Character.PARAGRAPH_SEPARATOR || Character.getType(c) == Character.SPACE_SEPARATOR;
    }

    private static boolean isUnicodeOtherSymbol(char c) {
        return Character.getType(c) == Character.OTHER_SYMBOL || Character.getType(c) == Character.MODIFIER_SYMBOL;
    }

    static boolean isLargeString(String data) {
        return data.startsWith("ca") && data.endsWith("ts");
    }

    /**
     * Sets the data for the FuzzingStrategy and returns the modified strategy.
     *
     * @param inner the data to be set for the FuzzingStrategy
     * @return the modified FuzzingStrategy with the specified data
     */
    public FuzzingStrategy withData(Object inner) {
        this.data = inner;
        return this;
    }

    /**
     * Checks if the current FuzzingStrategy is a SkipFuzzingStrategy.
     *
     * @return true if the current strategy is a SkipFuzzingStrategy, false otherwise
     */
    public boolean isSkip() {
        return this.getClass().isAssignableFrom(SkipFuzzingStrategy.class);
    }

    @Override
    public String toString() {
        return this.truncatedValue();
    }

    /**
     * Gets a truncated representation of the data associated with the FuzzingStrategy.
     * If the data is null, returns the name of the strategy.
     *
     * @return a truncated representation of the data or the name of the strategy if the data is null
     */
    public String truncatedValue() {
        if (data != null) {
            String toPrint = String.valueOf(data);
            if (toPrint.length() > 30) {
                toPrint = toPrint.substring(0, 30) + "...";
            }
            return this.name() + " with " + formatValue(toPrint);
        }
        return this.name();
    }

    /**
     * Gets a FuzzingStrategy with a repeated character to replace a valid value based on the provided schema.
     * If the schema is null or the minLength is not specified, the provided character is used as is.
     *
     * @param schema            the schema to determine the minimum length for repeating characters
     * @param characterToRepeat the character to be repeated for the replacement
     * @return a FuzzingStrategy with the specified repeated character for replacement
     */
    public static FuzzingStrategy getFuzzStrategyWithRepeatedCharacterReplacingValidValue(Schema<?> schema, String characterToRepeat) {
        String spaceValue = characterToRepeat;
        if (schema != null && schema.getMinLength() != null) {
            spaceValue = StringUtils.repeat(spaceValue, (schema.getMinLength() / spaceValue.length()) + 1);
        }
        return FuzzingStrategy.replace().withData(spaceValue);
    }

    /**
     * Gets a list of FuzzingStrategy instances for large values, based on the specified size.
     * The strategy involves replacing the original value with a marked large string.
     *
     * @param largeStringsSize the desired size for the large strings
     * @return a list containing a single FuzzingStrategy for large values
     */
    public static List<FuzzingStrategy> getLargeValuesStrategy(int largeStringsSize) {
        String generatedValue = StringGenerator.generateRandomUnicode();
        int payloadSize = largeStringsSize / generatedValue.length();
        if (payloadSize == 0) {
            return Collections.singletonList(FuzzingStrategy.replace().withData(markLargeString(generatedValue.substring(0, largeStringsSize))));
        }
        return Collections.singletonList(FuzzingStrategy.replace().withData(markLargeString(StringUtils.repeat(generatedValue, payloadSize + 1))));
    }

    /**
     * Marks a given input string as a large string by adding a prefix and suffix.
     *
     * @param input the input string to be marked as a large string
     * @return the marked large string with added prefix and suffix
     */
    public static String markLargeString(String input) {
        return "ca" + input + "ts";
    }

    /**
     * Processes a given value according to the implemented logic in the specific fuzzing strategy.
     *
     * @param value the value to be processed
     * @return the result of processing the input value
     */
    public abstract Object process(Object value);

    /**
     * Returns the name of the fuzzing strategy.
     *
     * @return the name of the fuzzing strategy
     */
    public abstract String name();

    /**
     * Retrieves a list of fuzzing strategies based on the characteristics of the provided field schema.
     *
     * @param fuzzedFieldSchema The schema representing the characteristics of the field to be fuzzed.
     * @param invisibleChars    A list of invisible characters to be used in fuzzing strategies.
     * @param maintainSize      If true, maintains the size of the field during fuzzing; if false, allows size modification.
     * @return A list of {@link FuzzingStrategy} objects representing different fuzzing strategies for the field.
     * It returns {@code FuzzingStrategy.skip()} If the field schema type is not "string" or has a binary/byte format.
     * @throws NullPointerException If 'fuzzedFieldSchema' or 'invisibleChars' is null.
     */
    public static List<FuzzingStrategy> getFuzzingStrategies(Schema<?> fuzzedFieldSchema, List<String> invisibleChars, boolean maintainSize) {
        boolean isNotString = !"string".equalsIgnoreCase(fuzzedFieldSchema.getType());
        boolean isBinaryFormat = "binary".equalsIgnoreCase(fuzzedFieldSchema.getFormat());
        boolean isByteFormat = "byte".equalsIgnoreCase(fuzzedFieldSchema.getFormat());

        if (isNotString || isBinaryFormat || isByteFormat) {
            return Collections.singletonList(FuzzingStrategy.skip().withData("Field does not match String schema or has binary/byte format"));
        }
        String initialValue = StringGenerator.generateValueBasedOnMinMax(fuzzedFieldSchema);

        /* independent of the supplied strategy, we still maintain sizes for enums */
        final boolean insertWithoutReplace = !maintainSize || !CollectionUtils.isEmpty(fuzzedFieldSchema.getEnum());

        return invisibleChars.stream()
                .map(value -> FuzzingStrategy.replace()
                        .withData(CatsUtil.insertInTheMiddle(initialValue, value, insertWithoutReplace)))
                .toList();
    }


    /**
     * Replaces a specific field in the given payload using the provided fuzzing strategy.
     *
     * @param payload                    the original payload containing the field to be replaced
     * @param jsonPropertyForReplacement the JSON property representing the field to be replaced
     * @param fuzzingStrategyToApply     the fuzzing strategy to apply for replacement
     * @return a FuzzingResult containing the modified payload and information about the replacement
     */
    public static FuzzingResult replaceField(String payload, String jsonPropertyForReplacement, FuzzingStrategy fuzzingStrategyToApply) {
        return replaceField(payload, jsonPropertyForReplacement, fuzzingStrategyToApply, false);
    }

    /**
     * Replaces a specific field in the given payload using the provided fuzzing strategy.
     *
     * @param payload                    the original payload containing the field to be replaced
     * @param jsonPropertyForReplacement the JSON property representing the field to be replaced
     * @param fuzzingStrategyToApply     the fuzzing strategy to apply for replacement
     * @param mergeFuzzing               weather to merge the fuzzed value with the valid value
     * @return a FuzzingResult containing the modified payload and information about the replacement
     */
    public static FuzzingResult replaceField(String payload, String jsonPropertyForReplacement, FuzzingStrategy fuzzingStrategyToApply, boolean mergeFuzzing) {
        if (StringUtils.isNotBlank(payload)) {
            String jsonPropToGetValue = jsonPropertyForReplacement;
            if (JsonUtils.isJsonArray(payload)) {
                jsonPropToGetValue = JsonUtils.FIRST_ELEMENT_FROM_ROOT_ARRAY + jsonPropertyForReplacement;
                jsonPropertyForReplacement = JsonUtils.ALL_ELEMENTS_ROOT_ARRAY + jsonPropertyForReplacement;
            }
            DocumentContext jsonDocument = JsonPath.parse(payload);
            Object oldValue = jsonDocument.read(JsonUtils.sanitizeToJsonPath(jsonPropToGetValue));
            if (oldValue instanceof JSONArray && !jsonPropToGetValue.contains("[*]")) {
                oldValue = jsonDocument.read("$." + jsonPropToGetValue + "[0]");
                jsonPropertyForReplacement = "$." + jsonPropertyForReplacement + "[*]";
            }
            Object valueToSet = fuzzingStrategyToApply.process(oldValue);
            if (mergeFuzzing) {
                valueToSet = FuzzingStrategy.mergeFuzzing(WordUtils.nullOrValueOf(oldValue), fuzzingStrategyToApply.getData());
            }
            CatsUtil.replaceOldValueWithNewOne(jsonPropertyForReplacement, jsonDocument, valueToSet);

            return new FuzzingResult(jsonDocument.jsonString(), valueToSet);
        }
        return FuzzingResult.empty();
    }
}
