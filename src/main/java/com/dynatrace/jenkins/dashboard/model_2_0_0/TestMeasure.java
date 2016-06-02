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

import com.dynatrace.jenkins.dashboard.utils.Utils;
import org.apache.commons.lang.builder.CompareToBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Created by krzysztof.necel on 2016-02-04.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestMeasure implements Comparable<TestMeasure> {

	private final String name;
	private final String metricGroup;
	private final Double expectedMin;
	private final Double expectedMax;
	private final Double value;
	private final String unit;
	private final Double violationPercentage;

	public TestMeasure(String name, String metricGroup, String expectedMin, String expectedMax,
					   String value, String unit, String violationPercentage) {
		this.name = name;
		this.metricGroup = metricGroup;
		this.expectedMin = expectedMin == null ? null : Double.valueOf(expectedMin);
		this.expectedMax = expectedMax == null ? null : Double.valueOf(expectedMax);
		this.value = value == null ? null : Double.valueOf(value);
		this.unit = unit;
		this.violationPercentage = violationPercentage == null ? null : Double.valueOf(violationPercentage);
	}

	// Required by JAXB
	private TestMeasure() {
		this.name = null;
		this.metricGroup = null;
		this.expectedMin = null;
		this.expectedMax = null;
		this.value = null;
		this.unit = null;
		this.violationPercentage = null;
	}

	public String getName() {
		return name;
	}

	public String getMetricGroup() {
		return metricGroup;
	}

	public String getExpectedMin() {
		return Utils.formatDouble(expectedMin);
	}

	public String getExpectedMax() {
		return Utils.formatDouble(expectedMax);
	}

	public String getValue() {
		return Utils.formatDouble(value);
	}

	public String getUnit() {
		return unit;
	}

	public String getViolationPercentage() {
		return Utils.formatDoublePercentage(violationPercentage);
	}

	@Override
	public String toString() {
		return "TestMeasure{" +
				"name='" + name + '\'' +
				", metricGroup='" + metricGroup + '\'' +
				", expectedMin='" + expectedMin + '\'' +
				", expectedMax='" + expectedMax + '\'' +
				", value='" + value + '\'' +
				", unit='" + unit + '\'' +
				", violationPercentage='" + violationPercentage + '\'' +
				'}';
	}

	@Override
	public int compareTo(TestMeasure o) {
		return new CompareToBuilder()
				.append(metricGroup, o.metricGroup)
				.append(name, o.name)
				.toComparison();
	}
}
