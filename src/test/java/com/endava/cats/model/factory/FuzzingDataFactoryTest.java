package com.endava.cats.model.factory;

import com.endava.cats.args.FilesArguments;
import com.endava.cats.command.CatsCommand;
import com.endava.cats.http.HttpMethod;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.util.CatsUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@QuarkusTest
class FuzzingDataFactoryTest {

    @InjectMock
    private CatsUtil catsUtil;
    @InjectMock
    private FilesArguments filesArguments;

    private FuzzingDataFactory fuzzingDataFactory;

    @BeforeEach
    void setup() {
        fuzzingDataFactory = new FuzzingDataFactory(catsUtil, filesArguments);
    }

    @Test
    void givenAContract_whenParsingThePathItemDetailsForPost_thenCorrectFuzzingDataAreBeingReturned() throws Exception {
        List<FuzzingData> data = setupFuzzingData("/pets", "src/test/resources/petstore.yml");

        Assertions.assertThat(data).hasSize(3);
        Assertions.assertThat(data.get(0).getMethod()).isEqualByComparingTo(HttpMethod.POST);
        Assertions.assertThat(data.get(1).getMethod()).isEqualByComparingTo(HttpMethod.POST);
        Assertions.assertThat(data.get(2).getMethod()).isEqualByComparingTo(HttpMethod.GET);
    }

    private List<FuzzingData> setupFuzzingData(String path, String contract) throws IOException {
        OpenAPIParser openAPIV3Parser = new OpenAPIParser();
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setFlatten(true);
        OpenAPI openAPI = openAPIV3Parser.readContents(new String(Files.readAllBytes(Paths.get(contract))), null, options).getOpenAPI();
        Map<String, Schema> schemas = CatsCommand.getSchemas(openAPI);
        PathItem pathItem = openAPI.getPaths().get(path);
        return fuzzingDataFactory.fromPathItem(path, pathItem, schemas, openAPI);
    }

    @Test
    void shouldCorrectlyParseRefOneOf() throws Exception {
        List<FuzzingData> dataList = setupFuzzingData("/pet-types", "src/test/resources/petstore.yml");
        Assertions.assertThat(dataList).hasSize(2);
        Assertions.assertThat(dataList.get(0).getPayload()).contains("\"petType\":{\"breedType\"");
        Assertions.assertThat(dataList.get(1).getPayload()).contains("\"petType\":{\"breedType\"");
    }

    @Test
    void shouldThrowExceptionWhenSchemeDoesNotExist() {
        Assertions.assertThatThrownBy(() -> setupFuzzingData("/pet-types", "src/test/resources/petstore-no-schema.yml")).isInstanceOf(IllegalArgumentException.class);
    }
}
