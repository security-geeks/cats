package com.endava.cats.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.utils.ModelUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Wrapper on top of {@link org.openapitools.codegen.utils.ModelUtils} in order to accommodate
 * some particular conditions needed by CATS.
 */
public abstract class CatsModelUtils {
    public static final String X_CATS_FIELD_NAME = "x-cats-field-name";

    private CatsModelUtils() {
        //ntd
    }

    public static boolean isStringSchema(Schema<?> schema) {
        return ModelUtils.isStringSchema(schema);
    }

    public static boolean isBooleanSchema(Schema<?> schema) {
        return ModelUtils.isBooleanSchema(schema);
    }

    public static boolean isArraySchema(Schema<?> schema) {
        return ModelUtils.isArraySchema(schema);
    }

    public static boolean isNumberSchema(Schema<?> schema) {
        return ModelUtils.isNumberSchema(schema);
    }

    public static boolean isFloatSchema(Schema<?> schema) {
        return SchemaTypeUtil.FLOAT_FORMAT.equalsIgnoreCase(schema.getFormat());
    }

    public static boolean isDecimalSchema(Schema<?> schema) {
        return "number".equalsIgnoreCase(schema.getFormat());
    }

    public static boolean isShortIntegerSchema(Schema<?> schema) {
        return SchemaTypeUtil.INTEGER32_FORMAT.equalsIgnoreCase(schema.getFormat());
    }

    public static boolean isIntegerSchema(Schema<?> schema) {
        return ModelUtils.isIntegerSchema(schema);
    }

    public static boolean isDateSchema(Schema<?> schema) {
        return ModelUtils.isDateSchema(schema);
    }

    public static boolean isDateTimeSchema(Schema<?> schema) {
        return ModelUtils.isDateTimeSchema(schema);
    }

    public static boolean isBinarySchema(Schema<?> schema) {
        return ModelUtils.isBinarySchema(schema);
    }

    public static boolean isByteArraySchema(Schema<?> schema) {
        return ModelUtils.isByteArraySchema(schema);
    }


    /**
     * Encapsulating the test for easier maintainability.
     *
     * @param schema the schema being tested
     * @return true if a composed schema, false otherwise
     */
    public static boolean isComposedSchema(Schema<?> schema) {
        return ModelUtils.isComposedSchema(schema);
    }

    /**
     * Composed schemas and Map schemas are not considered ObjectSchemas.
     *
     * @param schema the schema being tested
     * @return true if ObjectSchema, false otherwise
     */
    public static boolean isObjectSchema(Schema<?> schema) {
        return schema instanceof ObjectSchema ||
                (SchemaTypeUtil.OBJECT_TYPE.equals(schema.getType()) && !(schema instanceof MapSchema) && !isComposedSchema(schema));
    }

    /**
     * Gets ref definition name based on full reference.
     *
     * @param ref the full reference
     * @return the simple reference name
     */
    public static String getSimpleRef(String ref) {
        if (ref == null) {
            return null;
        }
        if (!ref.contains("/") || ref.startsWith("#/paths")) {
            return ref;
        }
        return ref.substring(ref.lastIndexOf('/') + 1);
    }

    public static String getSimpleRefUsingOAT(String ref) {
        return ModelUtils.getSimpleRef(ref);
    }

    public static boolean isObjectSchemaUsingOAT(Schema<?> schema) {
        return ModelUtils.isObjectSchema(schema);
    }

    /**
     * Returns all schema which don't seem to be used.
     *
     * @param openAPI the OpenAPI Spec
     * @return a list of unused schemas
     */
    public static List<String> getUnusedSchemas(OpenAPI openAPI) {
        return ModelUtils.getUnusedSchemas(openAPI);
    }

    /**
     * Creates an empty ObjectSchema
     *
     * @return a new ObjectSchema
     */
    public static ObjectSchema newObjectSchema() {
        return new ObjectSchema();
    }

    /**
     * Eliminates the last part of the string if it matches the previous one.
     *
     * @param input The input string separated by ".".
     * @return The modified string with the last duplicate part removed.
     */
    public static String eliminateDuplicatePart(String input) {
        return Stream.of(input.split("\\."))
                .reduce((prev, curr) -> {
                    if (prev.endsWith(curr)) {
                        return prev;
                    } else {
                        return prev + "." + curr;
                    }
                })
                .orElse("");
    }

    /**
     * Checks if the field name contains a complex regex. For now, this is used to determine if the field is an email or a URI.
     * This is of course not 100% accurate, but it's a good start.
     *
     * @param schema the schema to be checked
     * @return true if the field name contains a complex regex, false otherwise
     */
    public static boolean isComplexRegex(Schema<?> schema) {
        if (StringUtils.isBlank(schema.getPattern())) {
            return false;
        }

        String lowerField = Optional.ofNullable(schema.getExtensions()).orElse(Collections.emptyMap()).getOrDefault(X_CATS_FIELD_NAME, "").toString().toLowerCase(Locale.ROOT);
        String pattern = schema.getPattern();
        return isEmail(pattern, lowerField) || isUri(pattern, lowerField) || isPassword(pattern, lowerField);
    }

    /**
     * Checks if the given combination or pattern and field name is a URI.
     *
     * @param pattern    the pattern of the field
     * @param lowerField the name of the field
     * @return true if the field name contains a complex regex, false otherwise
     */
    public static boolean isUri(String pattern, String lowerField) {
        return (lowerField.endsWith("url") || lowerField.endsWith("uri") || lowerField.endsWith("link")) && ("http://www.test.com".matches(pattern) || "https://www.test.com".matches(pattern));
    }

    /**
     * Checks if the given combination or pattern and field name is an email.
     *
     * @param pattern    the pattern of the field
     * @param lowerField the name of the field
     * @return true if the field name contains a complex regex, false otherwise
     */
    public static boolean isEmail(String pattern, String lowerField) {
        return lowerField.endsWith("email") && "test@test.com".matches(pattern);
    }

    /**
     * Checks if the given combination or pattern and field name is a password.
     *
     * @param pattern    the pattern of the field
     * @param lowerField the name of the field
     * @return true if the field name contains a complex regex, false otherwise
     */
    public static boolean isPassword(String pattern, String lowerField) {
        return lowerField.endsWith("password") && "catsISc00l?!useIt#".matches(pattern);
    }

    public static boolean isNotEmptySchema(Schema schema) {
        return schema != null &&
                (StringUtils.isNotBlank(schema.get$ref()) ||
                        StringUtils.isNotBlank(schema.getType()) ||
                        !CollectionUtils.isEmpty(schema.getTypes()) ||
                        !CollectionUtils.isEmpty(schema.getProperties()) ||
                        !CollectionUtils.isEmpty(schema.getAllOf()) ||
                        !CollectionUtils.isEmpty(schema.getAnyOf()) ||
                        !CollectionUtils.isEmpty(schema.getOneOf()) ||
                        schema.getItems() != null ||
                        !CollectionUtils.isEmpty(schema.getRequired()));
    }

    public static boolean isAnyOf(Schema schema) {
        return ModelUtils.isAnyOf(schema);
    }

    public static boolean isOneOf(Schema schema) {
        return ModelUtils.isOneOf(schema);
    }

    public static boolean isAllOf(Schema schema) {
        return ModelUtils.isAllOf(schema);
    }

    public static boolean isAllOfWithProperties(Schema schema) {
        return ModelUtils.isAllOfWithProperties(schema);
    }

    public static List<Schema> getInterfaces(Schema schema) {
        return ModelUtils.getInterfaces(schema);
    }

    public static Schema<?> getSchemaItems(Schema<?> schema) {
        return ModelUtils.getSchemaItems(schema);
    }

    public static boolean isMapSchema(Schema<?> schema) {
        return ModelUtils.isMapSchema(schema);
    }

    public static Schema getAdditionalProperties(Schema schema) {
        return ModelUtils.getAdditionalProperties(schema);
    }
}
