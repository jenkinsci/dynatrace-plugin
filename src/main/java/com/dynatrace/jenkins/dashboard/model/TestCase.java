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
package com.dynatrace.jenkins.dashboard.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@Deprecated
public class TestCase {
	private Set<TestMetric> testMetrics;
	private String testCaseName;
	private String platform;
	private TestCaseStatus status;
	private TestCaseStatus prevStatus;

	public TestCase(String testCaseName) {
		this.testCaseName = testCaseName;
	}

	public String getIcon() {
		switch (status) {
		case PASSED:
			return "green.gif";
		case VOLATILE:
			return "yellow.gif";	
		default:
			return "red.gif";
		}
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatform() {
		return platform;
	}

	public void setStatus(TestCaseStatus status) {
		this.status = status;
	}

	public void setStatus(String status) {
		this.status = TestCaseStatus.fromString(status);
	}

	public TestCaseStatus getStatus() {
		return status;
	}

	public TestCaseStatus getPrevStatus() {
		return prevStatus;
	}

	public void setPrevStatus(TestCaseStatus prevStatus) {
		this.prevStatus = prevStatus;
	}
	
	public void setPrevStatus(String prevStatus) {
		this.prevStatus = TestCaseStatus.fromString(prevStatus);
	}

	public Set<TestMetric> getTestMetrics() {
		if (testMetrics != null)
			return Collections.unmodifiableSet(testMetrics);
		else
			return null;
	}

	public void addTestMetric(TestMetric tm) {
		if (testMetrics == null) {
			testMetrics = new TreeSet<TestMetric>();
		}
		testMetrics.add(tm);
	}
	
	public String getFormattedTimestamp() {
		if (testMetrics.size() > 0)
			return testMetrics.iterator().next().getFormattedTimestamp();
		else
			return "";
	}

	@Override
	public String toString() {
		return "TestCase '"+testCaseName+"' [testMetrics=" + testMetrics + "]";
	}
}
