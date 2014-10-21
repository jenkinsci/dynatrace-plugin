/***************************************************
 * dynaTrace Jenkins Plugin
 
 Copyright (c) 2008-2014, COMPUWARE CORPORATION
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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class TestMetric implements Comparable<TestMetric> {
	private transient static final DateTimeFormatter DATETIME_FORMAT = ISODateTimeFormat
			.dateTime();
	private transient static final DateFormat FORMATTER = SimpleDateFormat
			.getDateTimeInstance();

	private String measure = "";
	private String metricgroup = "";
	private String high = "";
	private String last = "";
	private String unit = "";
	private String value = "";
	private boolean failed;
	private Date timestamp;

	// private List<TestRun> testRuns;

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public String getMetricgroup() {
		return metricgroup;
	}

	public void setMetricgroup(String metricgroup) {
		this.metricgroup = metricgroup;
	}

	public String getHigh() {
		return high;
	}

	public void setHigh(String high) {
		this.high = createStringWith2Dec(high);
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = createStringWith2Dec(last);
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = DATETIME_FORMAT.parseDateTime(timestamp).toDate();
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getFormattedTimestamp() {
		return FORMATTER.format(timestamp);
	}

	public void setTestRun(String timestamp, String value, String failed) {
		setTimestamp(timestamp);
		setValue(value);
		this.failed = Boolean.parseBoolean(failed);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = createStringWith2Dec(value);
	}
	
	private String createStringWith2Dec(String v) {
		if (v.indexOf(".") > -1) {
			Double d = Double.parseDouble(v);
			DecimalFormat df = new DecimalFormat("#.##");
			return df.format(d);
		} else
			return value;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	public String getIcon() {
		if (failed)
			return "red.gif";
		else
			return "green.gif";
	}

	@Override
	public String toString() {
		return "TestMetric [measure=" + measure + ", metricgroup=" + metricgroup
				+ ", high=" + high + ", last=" + last + ", unit=" + unit + ", value="
				+ value + ", failed=" + failed + ", timestamp=" + timestamp + "]";
	}

	public int compareTo(TestMetric o) {
		String thisMetricGroupMeasure = getMetricgroup() + " - " + getMeasure();
		String otherMetricGroupMeasure = o.getMetricgroup() + " - " + o.getMeasure();
		return thisMetricGroupMeasure.compareTo(otherMetricGroupMeasure);
	}
}
