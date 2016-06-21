package com.dynatrace.jenkins.dashboard.model_2_0_0;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by krzysztof.necel on 2016-06-20.
 */
public class TAReportDetailsTest {

	@Test
	public void isEmpty_False_Test() throws Exception {
		TestResult result1 = new TestResult(null, "testResult", null, "somePlatform1", TestStatus.PASSED, null);

		EnumMap<TestStatus, Integer> summary1 = new EnumMap<TestStatus, Integer>(TestStatus.class);
		EnumMap<TestStatus, Integer> summary2 = new EnumMap<TestStatus, Integer>(TestStatus.class);
		summary1.put(TestStatus.PASSED, 1);

		List<TestRun> testRuns = new ArrayList<TestRun>();
		testRuns.add(new TestRun(Lists.<TestResult>newArrayList(), summary2, "91b7994d-616c-464c-9fa2-a95eca9b9bed", TestCategory.PERFORMANCE));
		testRuns.add(new TestRun(Lists.newArrayList(result1), summary1, "698522f9-c91b-4140-b325-12a51cd3c541", TestCategory.UNIT));

		TAReportDetails reportDetails = new TAReportDetails(testRuns);

		assertThat("Report should not be empty", reportDetails.isEmpty(), is(false));
	}

	@Test
	public void isEmpty_True_Test() throws Exception {
		EnumMap<TestStatus, Integer> summary1 = new EnumMap<TestStatus, Integer>(TestStatus.class);
		EnumMap<TestStatus, Integer> summary2 = new EnumMap<TestStatus, Integer>(TestStatus.class);

		List<TestRun> testRuns = new ArrayList<TestRun>();
		testRuns.add(new TestRun(Lists.<TestResult>newArrayList(), summary2, "91b7994d-616c-464c-9fa2-a95eca9b9bed", TestCategory.PERFORMANCE));
		testRuns.add(new TestRun(Lists.<TestResult>newArrayList(), summary1, "698522f9-c91b-4140-b325-12a51cd3c541", TestCategory.UNIT));

		TAReportDetails reportDetails = new TAReportDetails(testRuns);

		assertThat("Report should be empty", reportDetails.isEmpty(), is(true));
	}

	@Test
	public void getCorrespondingTestResultTest() throws Exception {
		TestResult result1 = new TestResult(new Date(1452269274001L), "testResult", null, "somePlatform1", TestStatus.PASSED, null);
		TestResult result2 = new TestResult(new Date(1452269274002L), "testResult", "pac", "somePlatform1", TestStatus.PASSED, null);
		TestResult result3 = new TestResult(new Date(1452269274003L), "testResult2", null, "somePlatform1", TestStatus.PASSED, null);
		TestResult result4 = new TestResult(new Date(1452269274004L), "testResult2", "pac", "somePlatform1", TestStatus.PASSED, null);
		TestResult result5 = new TestResult(new Date(1452269274005L), "testResult", null, "somePlatform2", TestStatus.PASSED, null);
		TestResult result6 = new TestResult(new Date(1452269274006L), "testResult", "pac", "somePlatform2", TestStatus.PASSED, null);

		TestResult result1Bis = new TestResult(new Date(1452269274011L), "testResult", null, "somePlatform1", TestStatus.DEGRADED, null);
		TestResult result2Bis = new TestResult(new Date(1452269274012L), "testResult", "pac", "somePlatform1", TestStatus.IMPROVED, null);
		TestResult result3Bis = new TestResult(new Date(1452269274013L), "testResult2", null, "somePlatform1", TestStatus.DEGRADED, null);
		TestResult result4Bis = new TestResult(new Date(1452269274014L), "testResult2", "pac", "somePlatform1", TestStatus.IMPROVED, null);
		TestResult result5Bis = new TestResult(new Date(1452269274015L), "testResult", null, "somePlatform2", TestStatus.IMPROVED, null);
		TestResult result6Bis = new TestResult(new Date(1452269274016L), "testResult", "pac", "somePlatform2", TestStatus.FAILED, null);

		List<TestResult> testResults = Lists.newArrayList(result1, result2, result3, result4, result5, result6);
		List<TestResult> testResultsBis = Lists.newArrayList(result1Bis, result2Bis, result3Bis, result4Bis, result5Bis, result6Bis);

		List<TestRun> testRuns = new ArrayList<TestRun>();
		testRuns.add(new TestRun(testResults, null, "91b7994d-616c-464c-9fa2-a95eca9b9bed", TestCategory.UNIT));
		testRuns.add(new TestRun(testResultsBis, null, "698522f9-c91b-4140-b325-12a51cd3c541", TestCategory.PERFORMANCE));

		TAReportDetails reportDetails = new TAReportDetails(testRuns);

		String message = "Incorrect test result has been matched with the passed one";
		assertThat(message, reportDetails.getCorrespondingTestResult(result1Bis, TestCategory.UNIT), is(sameInstance(result1)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result2Bis, TestCategory.UNIT), is(sameInstance(result2)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result3Bis, TestCategory.UNIT), is(sameInstance(result3)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result4Bis, TestCategory.UNIT), is(sameInstance(result4)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result5Bis, TestCategory.UNIT), is(sameInstance(result5)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result6Bis, TestCategory.UNIT), is(sameInstance(result6)));

		assertThat(message, reportDetails.getCorrespondingTestResult(result1, TestCategory.PERFORMANCE), is(sameInstance(result1Bis)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result2, TestCategory.PERFORMANCE), is(sameInstance(result2Bis)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result3, TestCategory.PERFORMANCE), is(sameInstance(result3Bis)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result4, TestCategory.PERFORMANCE), is(sameInstance(result4Bis)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result5, TestCategory.PERFORMANCE), is(sameInstance(result5Bis)));
		assertThat(message, reportDetails.getCorrespondingTestResult(result6, TestCategory.PERFORMANCE), is(sameInstance(result6Bis)));

	}
}