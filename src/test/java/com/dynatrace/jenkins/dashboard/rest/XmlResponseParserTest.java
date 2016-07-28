package com.dynatrace.jenkins.dashboard.rest;

import com.dynatrace.jenkins.dashboard.TestUtils;
import com.dynatrace.jenkins.dashboard.model_2_0_0.*;
import com.dynatrace.jenkins.dashboard.utils.Utils;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.PrintStream;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

/**
 * Created by krzysztof.necel on 2016-06-17.
 */
public class XmlResponseParserTest {

	@Test
	public void parseTestRunsDocumentTest() throws Exception {
		Document document = Utils.stringToXmlDocument(TestUtils.getSampleXmlString());
		TAReportDetails reportDetails = XmlResponseParser.parseTestRunsDocument(document, mock(PrintStream.class));

		assertThat("Report should not be null", reportDetails, is(notNullValue()));
		assertThat("Report should not be empty", reportDetails.isEmpty(), is(false));

		List<TestRun> testRuns = reportDetails.getTestRuns();
		assertThat("Test runs list should not be null", testRuns, is(notNullValue()));
		assertThat("Test runs list size doesn't match", testRuns, hasSize(2));

		TestRun testRun1 = testRuns.get(0);
		TestRun testRun2 = testRuns.get(1);

		assertThat("Incorrect test run ID", testRun1.getId(), is(equalTo("698522f9-c91b-4140-b325-12a51cd3c541")));
		assertThat("Incorrect test run ID", testRun2.getId(), is(equalTo("91b7994d-616c-464c-9fa2-a95eca9b9bed")));

		assertThat("Incorrect test run category", testRun1.getCategory(), is(TestCategory.UNIT));
		assertThat("Incorrect test run category", testRun2.getCategory(), is(TestCategory.PERFORMANCE));

		Map<TestStatus, Integer> summary1 = testRun1.getSummary();
		Map<TestStatus, Integer> summary2 = testRun2.getSummary();

		TestUtils.assertSummary(summary1, 0, 0, 1, 0, 2, 0);
		TestUtils.assertSummary(summary2, 0, 0, 0, 0, 1, 0);

		Iterable<TestResult> degradedTestResults1 = testRun1.getDegradedTestResults();
		Iterable<TestResult> improvedTestResults2 = testRun2.getImprovedTestResults();

		assertThat("Incorrect test results count", degradedTestResults1.iterator().hasNext(), is(false));
		assertThat("Incorrect test results count", improvedTestResults2.iterator().hasNext(), is(false));

		Map<String, TestResult> testResults1Passed = new HashMap<String, TestResult>();
		Map<String, TestResult> testResults1Volatile = new HashMap<String, TestResult>();
		Map<String, TestResult> testResults2Passed = new HashMap<String, TestResult>();
		for (TestResult result : testRun1.getPassedTestResults()) {
			testResults1Passed.put(result.getTestName(), result);
		}
		for (TestResult result : testRun1.getVolatileTestResults()) {
			testResults1Volatile.put(result.getTestName(), result);
		}
		for (TestResult result : testRun2.getPassedTestResults()) {
			testResults2Passed.put(result.getTestName(), result);
		}

		assertThat("Incorrect test results count", testResults1Passed.size(), is(equalTo(2)));
		assertThat("Incorrect test results count", testResults1Volatile.size(), is(equalTo(1)));
		assertThat("Incorrect test results count", testResults2Passed.size(), is(equalTo(1)));

		TestResult result1 = testResults1Passed.get("Tunnel3Test.suspendLinearIncr2Test");
		TestResult result2 = testResults1Volatile.get("Tunnel3Test.suspendSquareDecr2Test");
		TestResult result3 = testResults1Passed.get("Tunnel3Test.suspendInverselyPropotional2Test");
		TestResult result4 = testResults2Passed.get("Tunnel3Test.suspendLinearIncr2Test");

		assertTestResult(result1, 1452269274462L, "tunnel.calculations1", "Win8.1-X64", TestStatus.PASSED);
		assertTestResult(result2, 1452269274459L, null, "Win8.1-X64", TestStatus.VOLATILE);
		assertTestResult(result3, 1452269274464L, "tunnel.calculations2", "Win8.1-X64", TestStatus.PASSED);
		assertTestResult(result4, 1452269255995L, "tunnel.calculations3", "Win7", TestStatus.PASSED);

		Set<TestMeasure> measures1 = result1.getTestMeasures();
		Set<TestMeasure> measures2 = result2.getTestMeasures();
		Set<TestMeasure> measures3 = result3.getTestMeasures();
		Set<TestMeasure> measures4 = result4.getTestMeasures();

		assertThat("Incorrect test measures count", measures1, hasSize(1));
		assertThat("Incorrect test measures count", measures2, hasSize(1));
		assertThat("Incorrect test measures count", measures3, hasSize(1));
		assertThat("Incorrect test measures count", measures4, hasSize(1));

		TestMeasure measure1 = measures1.iterator().next();
		TestMeasure measure2 = measures2.iterator().next();
		TestMeasure measure3 = measures3.iterator().next();
		TestMeasure measure4 = measures4.iterator().next();

		assertTestMeasure(measure1, "Failed Transaction Count1", "Error Detection1", null, null, 0d, "num", Double.NEGATIVE_INFINITY);
		assertTestMeasure(measure2, "Failed Transaction Count2", "Error Detection2", 0d, 2d, 1d, "num", 0d);
		assertTestMeasure(measure3, "Failed Transaction Count3", "Error Detection3", 0d, 0d, 0d, "num", Double.POSITIVE_INFINITY);
		assertTestMeasure(measure4, "Failed Transaction Count4", "Error Detection4", 0d, 0d, 0d, "num", 0d);
	}

	private void assertTestResult(TestResult actual, long timestamp, String packageName, String platform, TestStatus status) {
		assertThat("Test result cannot be null", actual, is(notNullValue()));
		assertThat("Incorrect test result timestamp", actual.getTimestamp(), is(new Date(timestamp)));
		assertThat("Incorrect test result package name", actual.getPackageName(), is(equalTo(packageName)));
		assertThat("Incorrect test result platform", actual.getPlatform(), is(equalTo(platform)));
		assertThat("Incorrect test result status", actual.getStatus(), is(status));
	}

	private void assertTestMeasure(TestMeasure actual, String name, String metricGroup, Double expectedMin,
								   Double expectedMax, Double value, String unit, Double violationPercentage) {
		assertThat("Incorrect test measure's name", actual.getName(), is(equalTo(name)));
		assertThat("Incorrect test measure's metricGroup", actual.getMetricGroup(), is(equalTo(metricGroup)));
		assertThat("Incorrect test measure's expectedMin", actual.getExpectedMin(), is(equalTo(expectedMin)));
		assertThat("Incorrect test measure's expectedMax", actual.getExpectedMax(), is(equalTo(expectedMax)));
		assertThat("Incorrect test measure's value", actual.getValue(), is(equalTo(value)));
		assertThat("Incorrect test measure's unit", actual.getUnit(), is(equalTo(unit)));
		assertThat("Incorrect test measure's violationPercentage", actual.getViolationPercentage(), is(equalTo(violationPercentage)));
	}
}