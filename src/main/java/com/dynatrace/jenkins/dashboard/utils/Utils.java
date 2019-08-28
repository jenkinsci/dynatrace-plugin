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

import com.dynatrace.jenkins.dashboard.TABuildSetupStatusAction;
import com.dynatrace.jenkins.dashboard.TAGlobalConfiguration;
import com.dynatrace.jenkins.dashboard.model_2_0_0.*;
import com.dynatrace.sdk.server.BasicServerConfiguration;
import com.dynatrace.sdk.server.DynatraceClient;
import com.dynatrace.sdk.server.testautomation.models.TestRuns;
import com.google.common.collect.ImmutableList;

import hudson.model.Run;
import hudson.model.ParameterValue;
import hudson.model.Result;
import hudson.model.StringParameterValue;
import jenkins.model.GlobalConfiguration;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by krzysztof.necel on 2016-01-25.
 */
public final class Utils {
	private static final String TEST_MEASURE_UNIT_DEFAULT = "num";
	public static final String DYNATRACE_ICON_24_X_24_FILEPATH = "/plugin/dynatrace-dashboard/images/dynatrace_icon_24x24.png";
	public static final String DYNATRACE_ICON_48_X_48_FILEPATH = "/plugin/dynatrace-dashboard/images/dynatrace_icon_48x48.png";

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
	private static final String FORMAT_DOUBLE_NULL_VALUE = "N/A";

	private Utils() {
	}

	public static DynatraceClient createClient() {
		final TAGlobalConfiguration globalConfig = GlobalConfiguration.all().get(TAGlobalConfiguration.class);
		if (globalConfig != null) {
			BasicServerConfiguration config = new BasicServerConfiguration(globalConfig.username,
                    globalConfig.password.getPlainText(),
                    globalConfig.protocol.startsWith("https"),
                    globalConfig.host,
                    globalConfig.port,
                    globalConfig.validateCerts,
                    //connection timeout, 0 stands for infinite
                    0);
			return new DynatraceClient(config);
		}
		return null;
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

	private static TestResult convertTestResult(com.dynatrace.sdk.server.testautomation.models.TestResult sdkTestResult) {
		Set<TestMeasure> measures = new HashSet<>();
		for (com.dynatrace.sdk.server.testautomation.models.TestMeasure sdkMeasure : sdkTestResult.getMeasures()) {
			measures.add(convertTestMeasure(sdkMeasure));
		}
		return new TestResult(sdkTestResult.getExecutionTime(), sdkTestResult.getName(), sdkTestResult.getPackageName(), sdkTestResult.getPlatform(), convertTestStatus(sdkTestResult.getStatus()), measures);
	}

	private static TestMeasure convertTestMeasure(com.dynatrace.sdk.server.testautomation.models.TestMeasure sdkTestMeasure) {
		String unit = sdkTestMeasure.getUnit() != null ? sdkTestMeasure.getUnit() : TEST_MEASURE_UNIT_DEFAULT;
		return new TestMeasure(sdkTestMeasure.getName(),
				sdkTestMeasure.getMetricGroup(),
				sdkTestMeasure.getExpectedMin(),
				sdkTestMeasure.getExpectedMax(),
				sdkTestMeasure.getValue(),
				unit,
				sdkTestMeasure.getViolationPercentage());
	}

	private static TestCategory convertTestCategory(com.dynatrace.sdk.server.testautomation.models.TestCategory sdkTestCategory) {
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

	private static TestStatus convertTestStatus(com.dynatrace.sdk.server.testautomation.models.TestStatus sdkTestStatus) {
		return TestStatus.valueOf(sdkTestStatus.name());
	}


	public static Map<TestStatus, Integer> createReportAggregatedSummary(TAReportDetails reportDetails) {
		// just sum all the reports for test runs
		final Map<TestStatus, Integer> summary = new EnumMap<>(TestStatus.class);
		for (TestRun testRun : reportDetails.getTestRuns()) {
			Map<TestStatus, Integer> testRunSummary = testRun.getSummary();
			for (Map.Entry<TestStatus, Integer> entry : testRunSummary.entrySet()) {
				Integer value = summary.get(entry.getKey());
				summary.put(entry.getKey(), value == null ? entry.getValue() : entry.getValue() + value);
			}
		}
		return summary;
	}

	public static String formatDouble(Double d) {
		return d == null ? FORMAT_DOUBLE_NULL_VALUE : DECIMAL_FORMAT.format(d);
	}

	public static String formatDoublePercentage(Double d) {
		return d == null ? FORMAT_DOUBLE_NULL_VALUE : DECIMAL_FORMAT.format(d * 100);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean isValidBuild(Run build, PrintStream logger, String message) {
		if (build.getResult() == Result.ABORTED) {
			logger.println("Build has been aborted - " + message);
			return false;
		}
		TABuildSetupStatusAction setupStatusAction = build.getAction(TABuildSetupStatusAction.class);
		if (setupStatusAction != null && setupStatusAction.isSetupFailed()) {
			logger.println("Failed to set up environment for Dynatrace AppMon Plugin - " + message);
			return false;
		}
		return true;
	}

	public static void updateBuildVariables(Run<?, ?> build, List<ParameterValue> parameters) {
		DynatraceVariablesAction existingAction = build.getAction(DynatraceVariablesAction.class);
		if (existingAction == null) {
			build.addAction(new DynatraceVariablesAction(parameters));
		} else {
			build.replaceAction(existingAction.createUpdated(parameters));
		}
	}

	public static void updateBuildVariable(Run<?, ?> build, String key, String value) {
		updateBuildVariables(build, ImmutableList.of(new StringParameterValue(key, value)));
	}
}
