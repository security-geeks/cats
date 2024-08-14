package com.endava.cats.fuzzer.fields.within;

import com.endava.cats.args.FilesArguments;
import com.endava.cats.http.ResponseCodeFamilyPredefined;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.report.TestCaseListener;
import com.endava.cats.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

@QuarkusTest
class WithinSingleCodePointEmojisInStringFieldsTrimValidateFuzzerTest {
    private ServiceCaller serviceCaller;
    private TestCaseListener testCaseListener;
    private FilesArguments filesArguments;
    private WithinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer;

    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        testCaseListener = Mockito.mock(TestCaseListener.class);
        filesArguments = Mockito.mock(FilesArguments.class);
        withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer = new WithinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer(serviceCaller, testCaseListener, filesArguments);
        Mockito.when(testCaseListener.isFieldNotADiscriminator(Mockito.anyString())).thenReturn(true);
        Mockito.when(testCaseListener.isFieldNotADiscriminator("pet#type")).thenReturn(false);
    }

    @Test
    void shouldProperlyOverrideSuperClassMethods() {
        FuzzingData data = Mockito.mock(FuzzingData.class);
        Map<String, Schema> reqTypes = new HashMap<>();
        reqTypes.put("field", new StringSchema());
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(reqTypes);
        FuzzingStrategy fuzzingStrategy = withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer.getFieldFuzzingStrategy(data, "field").get(1);
        Assertions.assertThat(fuzzingStrategy.name()).isEqualTo(FuzzingStrategy.replace().name());

        Assertions.assertThat(fuzzingStrategy.getData().toString()).contains("\uD83D\uDC80");
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer.getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern()).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer.description()).isNotNull();
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer.concreteFuzzStrategy().name()).isEqualTo(FuzzingStrategy.replace().name());

        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer.typeOfDataSentToTheService()).isNotNull();
    }

    @Test
    void shouldNotFuzzIfDiscriminatorField() {
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer.isFuzzerWillingToFuzz(null, "pet#type")).isFalse();
    }

    @Test
    void shouldFuzzIfNotDiscriminatorField() {
        Assertions.assertThat(withinSingleCodePointEmojisInStringFieldsTrimValidateFuzzer.isFuzzerWillingToFuzz(null, "pet#number")).isTrue();
    }
}
