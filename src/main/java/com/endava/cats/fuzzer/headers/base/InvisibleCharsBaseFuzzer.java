package com.endava.cats.fuzzer.headers.base;

import com.endava.cats.io.ServiceCaller;
import com.endava.cats.model.FuzzingStrategy;
import com.endava.cats.report.TestCaseListener;

import java.util.List;

/**
 * Base class for fuzzers sending Control Chars or Unicode Separators in headers.
 */
public abstract class InvisibleCharsBaseFuzzer extends ExpectOnly4XXBaseHeadersFuzzer {

    protected InvisibleCharsBaseFuzzer(ServiceCaller sc, TestCaseListener lr) {
        super(sc, lr);
    }

    @Override
    public List<FuzzingStrategy> fuzzStrategy() {
        return this.getInvisibleChars()
                .stream().map(value -> concreteFuzzStrategy().withData(value)).toList();
    }

    @Override
    public String description() {
        return "iterate through each header and " + typeOfDataSentToTheService();
    }

    public abstract List<String> getInvisibleChars();

    public abstract FuzzingStrategy concreteFuzzStrategy();
}