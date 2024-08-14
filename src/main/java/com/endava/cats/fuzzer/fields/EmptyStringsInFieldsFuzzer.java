package com.endava.cats.fuzzer.fields;

import com.endava.cats.annotations.FieldFuzzer;
import com.endava.cats.args.FilesArguments;
import com.endava.cats.args.FilterArguments;
import com.endava.cats.fuzzer.fields.base.Expect4XXForRequiredBaseFieldsFuzzer;
import com.endava.cats.http.HttpMethod;
import com.endava.cats.http.ResponseCodeFamily;
import com.endava.cats.http.ResponseCodeFamilyPredefined;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.report.TestCaseListener;
import com.endava.cats.strategy.FuzzingStrategy;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;

/**
 * Fuzzer that sends empty values in string fields.
 */
@Singleton
@FieldFuzzer
public class EmptyStringsInFieldsFuzzer extends Expect4XXForRequiredBaseFieldsFuzzer {
    private final FilterArguments filterArguments;

    /**
     * Creates a new EmptyStringsInFieldsFuzzer instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     * @param fa filter arguments
     */
    public EmptyStringsInFieldsFuzzer(ServiceCaller sc, TestCaseListener lr, FilesArguments cp, FilterArguments fa) {
        super(sc, lr, cp);
        this.filterArguments = fa;
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "empty strings";
    }

    @Override
    protected List<FuzzingStrategy> getFieldFuzzingStrategy(FuzzingData data, String fuzzedField) {
        return Collections.singletonList(FuzzingStrategy.replace().withData(""));
    }

    @Override
    protected ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamilyPredefined.FOURXX;
    }

    @Override
    public boolean isFuzzerWillingToFuzz(FuzzingData data, String fuzzedField) {
        return HttpMethod.requiresBody(data.getMethod()) || data.isQueryParam(fuzzedField);
    }

    @Override
    public List<String> skipForFields() {
        return filterArguments.getSkipFields();
    }

    @Override
    public String description() {
        return "iterate through each field and send empty String values";
    }
}
