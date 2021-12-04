package com.endava.cats.fuzzer.fields.only;

import com.endava.cats.args.FilesArguments;
import com.endava.cats.args.IgnoreArguments;
import com.endava.cats.fuzzer.FieldFuzzer;
import com.endava.cats.fuzzer.ValidateAndTrim;
import com.endava.cats.fuzzer.WhitespaceFuzzer;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.report.TestCaseListener;
import com.endava.cats.util.CatsUtil;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@FieldFuzzer
@WhitespaceFuzzer
@ValidateAndTrim
public class OnlyWhitespacesInFieldsValidateTrimFuzzer extends InvisibleCharsOnlyValidateTrimFuzzer {

    public OnlyWhitespacesInFieldsValidateTrimFuzzer(ServiceCaller sc, TestCaseListener lr, CatsUtil cu, FilesArguments cp, IgnoreArguments fa) {
        super(sc, lr, cu, cp, fa);
    }

    @Override
    protected String typeOfDataSentToTheService() {
        return "values with unicode separators only";
    }

    @Override
    List<String> getInvisibleChars() {
        return catsUtil.getSeparatorsFields();
    }

}