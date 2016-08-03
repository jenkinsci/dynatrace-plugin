/***************************************************
 * Dynatrace Jenkins Plugin

 Copyright (c) 2008-2016, DYNATRACE LLC
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice,
 this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 * Neither the name of the dynaTrace software nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 DAMAGE.
 */
package com.dynatrace.jenkins.dashboard.utils;

import com.dynatrace.jenkins.dashboard.TestAutomationBuildAction;
import com.dynatrace.jenkins.dashboard.TestAutomationReport;
import com.dynatrace.jenkins.dashboard.model.TestCaseStatus;
import com.dynatrace.jenkins.dashboard.model_2_0_0.*;
import com.dynatrace.sdk.server.testautomation.models.TestRuns;
import hudson.model.AbstractBuild;

import java.util.*;

@SuppressWarnings("deprecation")
public final class UtilsCompat {

	private UtilsCompat() {
	}

	public static TAReportDetails convertTestRuns(TestRuns sdkTestRuns) {
		ArrayList<TestRun> testRuns = new ArrayList<>();
		if (sdkTestRuns != null) {
			for (com.dynatrace.sdk.server.testautomation.models.TestRun tr : sdkTestRuns.getTestRuns()) {
				testRuns.add(convertTestRun(tr));
			}
		}
		return new TAReportDetails(testRuns);
	}

	public static TestRun convertTestRun(com.dynatrace.sdk.server.testautomation.models.TestRun sdkTestRun) {
		List<TestResult> testResults = new ArrayList<>();
		for (com.dynatrace.sdk.server.testautomation.models.TestResult sdkResult : sdkTestRun.getTestResults()) {
			testResults.add(convertTestResult(sdkResult));
		}
		Map<TestStatus, Integer> testRunSummary = new EnumMap<>(TestStatus.class);
		testRunSummary.put(TestStatus.FAILED, sdkTestRun.getFailedCount());
		testRunSummary.put(TestStatus.DEGRADED, sdkTestRun.getDegradedCount());
		testRunSummary.put(TestStatus.VOLATILE, sdkTestRun.getVolatileCount());
		testRunSummary.put(TestStatus.IMPROVED, sdkTestRun.getImprovedCount());
		testRunSummary.put(TestStatus.PASSED, sdkTestRun.getPassedCount());
		return new TestRun(testResults, testRunSummary, sdkTestRun.getId(), convertTestCategory(sdkTestRun.getCategory()));
	}

	public static TestResult convertTestResult(com.dynatrace.sdk.server.testautomation.models.TestResult sdkTestResult) {
		Set<TestMeasure> measures = new HashSet<>();
		for (com.dynatrace.sdk.server.testautomation.models.TestMeasure sdkMeasure : sdkTestResult.getMeasures()) {
			measures.add(convertTestMeasure(sdkMeasure));
		}
		return new TestResult(new Date(sdkTestResult.getExecutionTime()), sdkTestResult.getName(), sdkTestResult.getPackageName(), sdkTestResult.getPlatform(), convertTestStatus(sdkTestResult.getStatus()), measures);
	}

	public static TestMeasure convertTestMeasure(com.dynatrace.sdk.server.testautomation.models.TestMeasure sdkTestMeasure) {
		return new TestMeasure(sdkTestMeasure.getName(),
				sdkTestMeasure.getMetricGroup(),
				sdkTestMeasure.getExpectedMin(),
				sdkTestMeasure.getExpectedMax(),
				sdkTestMeasure.getValue(),
				sdkTestMeasure.getUnit(),
				sdkTestMeasure.getViolationPercentage());
	}

	public static TestCategory convertTestCategory(com.dynatrace.sdk.server.testautomation.models.TestCategory sdkTestCategory) {
		switch (sdkTestCategory) {
			case UNIT:
				return TestCategory.UNIT;
			case UI_DRIVEN:
				return TestCategory.UI_DRIVEN;
			case WEB_API:
				return TestCategory.WEB_API;
			case PERFORMANCE:
				return TestCategory.PERFORMANCE;
		}
		throw new IllegalArgumentException("Could not convert TestCategory");
	}

	public static TestStatus convertTestStatus(com.dynatrace.sdk.server.testautomation.models.TestStatus sdkTestStatus) {
		return TestStatus.valueOf(sdkTestStatus.name());
	}

	public static TAReport getCompatReport(AbstractBuild<?, ?> build) {
		final TestAutomationBuildAction oldBuildAction = build.getAction(TestAutomationBuildAction.class);
		if (oldBuildAction == null) {
			return null;
		}

		final TestAutomationReport oldReport = oldBuildAction.getTestAutomationReport();
		if (oldReport == null) {
			return null;
		}

		Map<TestCaseStatus, Integer> oldSummary = oldReport.getTestCaseSummary();
		Map<TestStatus, Integer> newSummary = new EnumMap<>(TestStatus.class);

		for (Map.Entry<TestCaseStatus, Integer> entry : oldSummary.entrySet()) {
			TestStatus newStatus = convertStatus(entry.getKey());
			if (newStatus != null) {
				newSummary.put(newStatus, entry.getValue());
			}
		}

		return new TAReport(newSummary, build);
	}

	private static TestStatus convertStatus(TestCaseStatus oldStatus) {
		switch (oldStatus) {
			case FAILED:
				return TestStatus.FAILED;
			case VOLATILE:
				return TestStatus.VOLATILE;
			case DEGRADED:
				return TestStatus.DEGRADED;
			case IMPROVED:
				return TestStatus.IMPROVED;
			case PASSED:
				return TestStatus.PASSED;
			default:
				return null;
		}
	}
}
