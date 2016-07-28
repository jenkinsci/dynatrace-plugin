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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by krzysztof.necel on 2016-02-04.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestResult {

	private static final DateFormat FORMATTER = SimpleDateFormat.getDateTimeInstance();

	private final Date timestamp;
	private final String testName;
	private final String packageName;
	private final String platform;
	private final TestStatus status;

	@XmlElementWrapper(name="testMeasures")
	@XmlElement(name="testMeasure")
	private final Set<TestMeasure> testMeasures;

	public TestResult(Date timestamp, String testName, String packageName, String platform, TestStatus status, Set<TestMeasure> testMeasures) {
		this.timestamp = timestamp;
		this.testName = testName;
		this.packageName = packageName;
		this.platform = platform;
		this.status = status;
		this.testMeasures = testMeasures;
	}

	// Required by JAXB
	private TestResult() {
		this.timestamp = null;
		this.testName = null;
		this.packageName = null;
		this.platform = null;
		this.status = null;
		this.testMeasures = new TreeSet<TestMeasure>();
	}

	public String getTestName() {
		return testName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getPlatform() {
		return platform;
	}

	public TestStatus getStatus() {
		return status;
	}

	public Set<TestMeasure> getTestMeasures() {
		return testMeasures;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getFormattedTimestamp() {
		return FORMATTER.format(timestamp);
	}

	public boolean getFailed() {
		return status == TestStatus.FAILED;
	}

	public TestMeasure getMeasureByName(String metricGroup, String measureName) {
		for (TestMeasure testMeasure : testMeasures) {
			if (Objects.equals(testMeasure.getMetricGroup(), metricGroup)
					&& Objects.equals(testMeasure.getName(), measureName)) {
				return testMeasure;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "TestResult{" +
				"packageName='" + packageName + '\'' +
				", testName='" + testName + '\'' +
				", platform='" + platform + '\'' +
				", timestamp=" + timestamp +
				", status=" + status +
				", testMeasures=" + testMeasures +
				'}';
	}
}
