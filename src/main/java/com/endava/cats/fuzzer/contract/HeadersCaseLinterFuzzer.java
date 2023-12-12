package com.endava.cats.fuzzer.contract;

import com.endava.cats.annotations.LinterFuzzer;
import com.endava.cats.args.NamingArguments;
import com.endava.cats.model.CatsHeader;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.report.TestCaseListener;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@Singleton
@LinterFuzzer
public class HeadersCaseLinterFuzzer extends BaseLinterFuzzer {
    private final PrettyLogger log = PrettyLoggerFactory.getLogger(this.getClass());
    private final NamingArguments namingArguments;

    public HeadersCaseLinterFuzzer(TestCaseListener tcl, NamingArguments nameArgs) {
        super(tcl);
        this.namingArguments = nameArgs;
    }

    @Override
    public void process(FuzzingData data) {
        String expectedResult = "Headers must follow %s naming".formatted(namingArguments.getHeadersNaming().getDescription());
        testCaseListener.addScenario(log, "Check if headers follow {} naming", namingArguments.getHeadersNaming().getDescription());
        testCaseListener.addExpectedResult(log, expectedResult);

        String errors = this.checkHeaders(data);

        if (this.hasErrors(errors)) {
            testCaseListener.reportResultError(log, data, "Headers not following recommended naming",
                    "Headers not following {} naming: {}", namingArguments.getHeadersNaming().getDescription(), StringUtils.stripEnd(errors.trim(), ","));
        } else {
            testCaseListener.reportResultInfo(log, data, "Headers follow naming conventions.");
        }
    }

    private String checkHeaders(FuzzingData data) {
        Set<CatsHeader> headers = data.getHeaders();
        return this.check(headers.stream().map(CatsHeader::getName).toArray(String[]::new), header ->
                !namingArguments.getHeadersNaming().getPattern().matcher(header).matches());
    }

    @Override
    protected String runKey(FuzzingData data) {
        return data.getPath() + data.getMethod();
    }

    @Override
    public String description() {
        return "verifies that HTTP headers follow naming conventions";
    }
}