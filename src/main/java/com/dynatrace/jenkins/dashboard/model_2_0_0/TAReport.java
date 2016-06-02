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

import com.dynatrace.jenkins.dashboard.utils.TAReportDetailsFileUtils;
import hudson.model.AbstractBuild;

import javax.xml.bind.JAXBException;
import java.lang.ref.SoftReference;
import java.util.Map;

/**
 * Created by krzysztof.necel on 2016-02-05.
 */
public class TAReport {

	private final AbstractBuild<?, ?> build;
	private final Map<TestStatus, Integer> summary;
	private transient SoftReference<TAReportDetails> reportDetailsRef;

	public TAReport(Map<TestStatus, Integer> summary, AbstractBuild<?, ?> build) {
		this.summary = summary;
		this.build = build;
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public Map<TestStatus, Integer> getSummary() {
		return summary;
	}

	@Override
	public String toString() {
		return "TAReport{" +
				"buildNumber=" + build.getNumber() +
				", summary=" + summary +
				'}';
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

	public synchronized TAReportDetails loadReportDetails() {
		TAReportDetails reportDetails = reportDetailsRef == null ? null: reportDetailsRef.get();
		if (reportDetails == null) {
			try {
				reportDetails = TAReportDetailsFileUtils.loadReportDetails(build);
				reportDetailsRef = new SoftReference<TAReportDetails>(reportDetails);
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
		return reportDetails;
	}
}
