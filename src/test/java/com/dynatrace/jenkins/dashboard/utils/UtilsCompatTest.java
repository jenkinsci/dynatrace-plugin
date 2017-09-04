package com.dynatrace.jenkins.dashboard.utils;

import com.dynatrace.jenkins.dashboard.TestAutomationBuildAction;
import com.dynatrace.jenkins.dashboard.TestAutomationReport;
import com.dynatrace.jenkins.dashboard.TestUtils;
import com.dynatrace.jenkins.dashboard.model.TestCaseStatus;
import com.dynatrace.jenkins.dashboard.model_2_0_0.TAReport;
import com.dynatrace.jenkins.dashboard.model_2_0_0.TestStatus;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by krzysztof.necel on 2016-06-15.
 */
public class UtilsCompatTest {

	@Test
	public void getCompatReportTest() {
		Map<TestCaseStatus, Integer> oldSummary = new TreeMap<>();

		oldSummary.put(TestCaseStatus.FAILED, 1);
		oldSummary.put(TestCaseStatus.DEGRADED, 2);
		oldSummary.put(TestCaseStatus.VOLATILE, 3);
		oldSummary.put(TestCaseStatus.IMPROVED, 4);

		TestAutomationReport oldReport = mock(TestAutomationReport.class);
		when(oldReport.getTestCaseSummary()).thenReturn(oldSummary);

		TestAutomationBuildAction buildAction = mock(TestAutomationBuildAction.class);
		when(buildAction.getTestAutomationReport()).thenReturn(oldReport);

		Run build = mock(Run.class);
		when(build.getAction(TestAutomationBuildAction.class)).thenReturn(buildAction);

		TAReport result = UtilsCompat.getCompatReport(build);
		assertThat("Compat report must not be null", result, is(notNullValue()));

		Map<TestStatus, Integer> resultSummary = result.getSummary();
		assertThat("Result summary must not be null", result, is(notNullValue()));
		TestUtils.assertSummary(resultSummary, 1, 2, 3, 4, 0, 0);

		Run<?, ?> resultBuild = result.getBuild();
		assertThat("Compat report must reference build that was passed", resultBuild, is(theInstance(build)));
	}

	@Test
	public void getCompatReport_NullReport_Test() {
		TestAutomationBuildAction buildAction = mock(TestAutomationBuildAction.class);
		when(buildAction.getTestAutomationReport()).thenReturn(null);

		AbstractBuild build = mock(AbstractBuild.class);
		when(build.getAction(TestAutomationBuildAction.class)).thenReturn(buildAction);

		TAReport result = UtilsCompat.getCompatReport(build);
		assertThat("Compat report must be null", result, is(nullValue()));
	}

	@Test
	public void getCompatReport_NullAction_Test() {
		AbstractBuild build = mock(AbstractBuild.class);
		when(build.getAction(TestAutomationBuildAction.class)).thenReturn(null);

		TAReport result = UtilsCompat.getCompatReport(build);
		assertThat("Compat report must be null", result, is(nullValue()));
	}
}