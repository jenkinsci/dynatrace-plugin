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
import com.dynatrace.jenkins.dashboard.model_2_0_0.TAReportDetails;
import com.dynatrace.jenkins.dashboard.model_2_0_0.TestRun;
import com.dynatrace.jenkins.dashboard.model_2_0_0.TestStatus;
import hudson.model.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by krzysztof.necel on 2016-01-25.
 */
public final class Utils {

	public static final String DYNATRACE_ICON_24_X_24_FILEPATH = "/plugin/dynatrace-dashboard/images/dynatrace_icon_24x24.png";
	public static final String DYNATRACE_ICON_48_X_48_FILEPATH = "/plugin/dynatrace-dashboard/images/dynatrace_icon_48x48.png";

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
	private static final String FORMAT_DOUBLE_NULL_VALUE = "N/A";

	private Utils() {
	}

	public static Document stringToXmlDocument(String xmlContent) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document xmlDocument = builder.parse(new InputSource(new StringReader(xmlContent)));
			return xmlDocument;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Map<TestStatus, Integer> createReportAggregatedSummary(TAReportDetails reportDetails) {
		// just sum all the reports for test runs
		final Map<TestStatus, Integer> summary = new EnumMap<TestStatus, Integer>(TestStatus.class);
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

	public static boolean isValidBuild(AbstractBuild build, PrintStream logger, String message) {
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

	public static void updateBuildVariables(AbstractBuild<?,?> build, List<ParameterValue> parameters) {
		ParametersAction existingAction = build.getAction(ParametersAction.class);
		final ParametersAction newAction;
		if (existingAction == null) {
			newAction = new ParametersAction(parameters);
		} else {
			newAction = existingAction.createUpdated(parameters);
			build.getActions().remove(existingAction);
		}
		build.addAction(newAction);
	}

	public static void updateBuildVariable(AbstractBuild<?,?> build, String key, String value) {
		updateBuildVariables(build, Collections.<ParameterValue>singletonList(new StringParameterValue(key, value)));
	}
}
