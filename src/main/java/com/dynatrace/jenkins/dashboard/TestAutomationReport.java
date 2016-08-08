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

 * @date: 28.8.2013
 * @author: cwat-wgottesh
 */
package com.dynatrace.jenkins.dashboard;

import com.dynatrace.jenkins.dashboard.model.*;
import hudson.model.AbstractBuild;
import org.joda.time.DateTime;

import java.util.*;

@Deprecated
public class TestAutomationReport {
	private String protocol;
	private String host;
	private String port;
	private String dashboardName;
	
	private List<TestCase> executedTestCases;
	private DateTime timestamp;
	private TestAutomationBuildAction buildAction;
	private TestAutomationReport lastBuildReport;
	private Map<TestCaseStatus, Integer> testCaseSummary;

	public void setBuildAction(TestAutomationBuildAction buildAction) {
		this.buildAction = buildAction;
	}

	public void setLastBuildReport(TestAutomationReport lastReport) {
		this.lastBuildReport = lastReport;
	}

	public DateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	public TestAutomationBuildAction getBuildAction() {
		return buildAction;
	}

	public TestAutomationReport getLastBuildReport() {
		return lastBuildReport;
	}

	public List<TestCase> getExecutedTestCases() {
		Collections.sort(executedTestCases, new TestCaseComparator());
		return executedTestCases;
	}

	public void setExecutedTestCases(List<TestCase> executedTestCases) {
		this.executedTestCases = executedTestCases;
	}
	
	public AbstractBuild<?, ?> getBuild() {
    return buildAction.getBuild();
  }

	@Override
	public String toString() {
		return "TestAutomationReport [executedTestCases=" + executedTestCases
				+ ", timestamp=" + timestamp + ", buildAction=" + buildAction + "]";
	}
	
	public void setTestCaseSummary(Map<TestCaseStatus, Integer> testCaseSummary) {
		this.testCaseSummary = testCaseSummary;
	}
	
	public Map<TestCaseStatus, Integer> getTestCaseSummary() {
		return testCaseSummary;
	}
	
	public TestMetric getMetric(String metricgroup, String measure) {
		for (TestCase testCase : executedTestCases) {
			Set<TestMetric> testMetrics = testCase.getTestMetrics();
			for (TestMetric testMetric : testMetrics) {
				if (testMetric.getMetricgroup().equals(metricgroup) && testMetric.getMeasure().equals(measure)) {
					return testMetric;
				}
			}
		}
		return null;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDashboardName() {
		return dashboardName;
	}

	public void setDashboardName(String dashboardName) {
		this.dashboardName = dashboardName;
	}

	public int getFailedCount() {
		return getTestCaseSummary().get(TestCaseStatus.FAILED);
	}

	public int getPassedCount() {
		return getTestCaseSummary().get(TestCaseStatus.PASSED);
	}

	public int getDegradedCount() {
		return getTestCaseSummary().get(TestCaseStatus.DEGRADED);
	}

	public int getVolatileCount() {
		return getTestCaseSummary().get(TestCaseStatus.VOLATILE);
	}

	public int getImprovedCount() {
		return getTestCaseSummary().get(TestCaseStatus.IMPROVED);
	}
	
	public List<TestCase> getFailedTestCases() {
		return getTestCases(TestCaseStatus.FAILED);
	}
	
	public List<TestCase> getPassedTestCases() {
		return getTestCases(TestCaseStatus.PASSED);
	}

	public List<TestCase> getDegradedTestCases() {
		return getTestCases(TestCaseStatus.DEGRADED);
	}
	
	public List<TestCase> getVolatileTestCases() {
		return getTestCases(TestCaseStatus.VOLATILE);
	}
	
	public List<TestCase> getImprovedTestCases() {
		return getTestCases(TestCaseStatus.IMPROVED);
	}
	
	public List<TestCase> getTestCases(final TestCaseStatus status) {
		List<TestCase> result = new ArrayList<>();
		for (TestCase tc : executedTestCases) {
			if (tc.getStatus() == status) {
				result.add(tc);
			}
		}
		return result;
	}
	
}
