package com.endava.cats.model;

import lombok.Builder;
import lombok.Getter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a report summarizing the results of CATS tests.
 * This report includes information about the number of warnings, errors, and successful tests.
 */
@Getter
@Builder
public class CatsTestReport {
    private final List<CatsTestCaseSummary> testCases;
    private final int totalTests;
    private final int success;
    private final int warnings;
    private final int errors;
    private final long executionTime;
    private final String timestamp;
    private final String catsVersion;

    public List<JunitTestSuite> getTestSuites() {
        DecimalFormat THREE_DECIMAL_FORMAT = new DecimalFormat("#.###");

        Map<String, List<CatsTestCaseSummary>> groupedByFuzzer = testCases.stream()
                .collect(Collectors.groupingBy(CatsTestCaseSummary::getFuzzer));

        List<JunitTestSuite> junitTestSuites = new ArrayList<>();

        for (Map.Entry<String, List<CatsTestCaseSummary>> entry : groupedByFuzzer.entrySet()) {
            String fuzzer = entry.getKey();
            List<CatsTestCaseSummary> testCases = entry.getValue();

            // Compute testsuite-level details
            int totalTests = testCases.size();
            int failures = (int) testCases.parallelStream()
                    .filter(CatsTestCaseSummary::getError)
                    .filter(Predicate.not(CatsTestCaseSummary::is9xxResponse))
                    .count();
            int errors = (int) testCases.parallelStream()
                    .filter(CatsTestCaseSummary::getError)
                    .filter(CatsTestCaseSummary::is9xxResponse)
                    .count();
            int warning = (int) testCases.parallelStream().filter(CatsTestCaseSummary::getWarning).count();
            double totalTime = testCases.parallelStream().mapToDouble(CatsTestCaseSummary::getTimeToExecuteInSec).sum();

            // Create the testsuite object
            JunitTestSuite junitTestSuite = new JunitTestSuite();
            junitTestSuite.fuzzer = fuzzer;
            junitTestSuite.totalTests = totalTests;
            junitTestSuite.failures = failures;
            junitTestSuite.warnings = warning;
            junitTestSuite.errors = errors;
            junitTestSuite.time = THREE_DECIMAL_FORMAT.format(totalTime);
            junitTestSuite.testCases = testCases;

            junitTestSuites.add(junitTestSuite);
        }

        return junitTestSuites;
    }

    /**
     * Retrieves the number of failed tests. Failed tests are those that have an error and are not 9xx responses.
     *
     * @return The number of failed tests.
     */
    public int getFailuresJunit() {
        return (int) testCases.parallelStream()
                .filter(CatsTestCaseSummary::getError)
                .filter(Predicate.not(CatsTestCaseSummary::is9xxResponse))
                .count();
    }

    /**
     * Retrieves the number of errors. Errors are those that have an error and are 9xx responses.
     *
     * @return The number of failed tests.
     */
    public int getErrorsJunit() {
        return (int) testCases.parallelStream()
                .filter(CatsTestCaseSummary::getError)
                .filter(CatsTestCaseSummary::is9xxResponse)
                .count();
    }

    public static class JunitTestSuite {
        String fuzzer;
        int totalTests;
        int failures;
        int errors;
        int warnings;
        String time;
        List<CatsTestCaseSummary> testCases;
    }
}
