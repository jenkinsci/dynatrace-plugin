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
package com.dynatrace.jenkins.dashboard.model_2_0_0;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.collections.CollectionUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.*;

/**
 * Created by krzysztof.necel on 2016-04-04.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestRun {

	@XmlElementWrapper(name="testResults")
	@XmlElement(name="testResult")
	private final List<TestResult> testResults;

	private final Map<TestStatus, Integer> summary;
	private final String id;
	private final TestCategory category;

	public TestRun(List<TestResult> testResults, Map<TestStatus, Integer> summary, String id, TestCategory category) {
		this.testResults = testResults;
		this.summary = summary;
		this.id = id;
		this.category = category;
	}

	// Required by JAXB
	private TestRun() {
		this.testResults = new ArrayList<>();
		this.summary = new EnumMap<>(TestStatus.class);
		this.id = null;
		this.category = null;
	}

	public Map<TestStatus, Integer> getSummary() {
		return summary;
	}

	public String getId() {
		return id;
	}

	public TestCategory getCategory() {
		return category;
	}

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(testResults);
	}

	@Override
	public String toString() {
		return "TestRun{" +
				"id='" + id + '\'' +
				", category=" + category +
				", summary=" + summary +
				'}';
	}

	public TestResult getCorrespondingTestResult(TestResult testResult) {
		for (TestResult result : testResults) {
			if (Objects.equals(result.getTestName(), testResult.getTestName())
					&& Objects.equals(result.getPackageName(), testResult.getPackageName())
					&& Objects.equals(result.getPlatform(), testResult.getPlatform())) {
				return result;
			}
		}
		return null;
	}

	public int getFailedCount() {
		return summary.get(TestStatus.FAILED);
	}

	public int getDegradedCount() {
		return summary.get(TestStatus.DEGRADED);
	}

	public int getVolatileCount() {
		return summary.get(TestStatus.VOLATILE);
	}

	public int getImprovedCount() {
		return summary.get(TestStatus.IMPROVED);
	}

	public int getPassedCount() {
		return summary.get(TestStatus.PASSED);
	}

	public Iterable<TestResult> getFailedTestResults() {
		return getTestResults(TestStatus.FAILED);
	}

	public Iterable<TestResult> getDegradedTestResults() {
		return getTestResults(TestStatus.DEGRADED);
	}

	public Iterable<TestResult> getVolatileTestResults() {
		return getTestResults(TestStatus.VOLATILE);
	}

	public Iterable<TestResult> getImprovedTestResults() {
		return getTestResults(TestStatus.IMPROVED);
	}

	public Iterable<TestResult> getPassedTestResults() {
		return getTestResults(TestStatus.PASSED);
	}

	private Iterable<TestResult> getTestResults(final TestStatus status) {
		return Iterables.filter(testResults, new Predicate<TestResult>() {
			@Override
			public boolean apply(TestResult testResult) {
				return testResult.getStatus() == status;
			}
		});
	}
}
