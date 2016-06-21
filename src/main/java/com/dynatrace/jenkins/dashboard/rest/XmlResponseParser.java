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
package com.dynatrace.jenkins.dashboard.rest;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.dynatrace.jenkins.dashboard.model_2_0_0.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by krzysztof.necel on 2016-02-08.
 */
public class XmlResponseParser {

	private static final String TEST_MEASURE_UNIT_DEFAULT = "num";
	private static final String TEST_MEASURE_DOUBLE_POSITIVE_INF = "INF";
	private static final String TEST_MEASURE_DOUBLE_NEGATIVE_INF = "-INF";

	public static TAReportDetails parseTestRunsDocument(Document document, PrintStream logger) {
		logger.println("Parsing XML response...");
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();

			NodeList testRuns = (NodeList) xpath.compile("/*/testRun").evaluate(document, XPathConstants.NODESET);

			final int testRunsCount = testRuns.getLength();
			final List<TestRun> testRunsList = new ArrayList<TestRun>(testRunsCount);
			for (int i = 0; i < testRunsCount; ++i) {
				Node testRunNode = testRuns.item(i);
				testRunsList.add(parseTestRun(testRunNode));
			}

			return new TAReportDetails(testRunsList);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public static TestRun parseTestRunDocument(Document document, PrintStream logger) {
		logger.println("Parsing XML response...");
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();

			Node testRun = (Node) xpath.compile("/testRun").evaluate(document, XPathConstants.NODE);

			return parseTestRun(testRun);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	private static TestRun parseTestRun(Node testRun) {
		final NamedNodeMap attributes = testRun.getAttributes();
		// mandatory attributes
		final Map<TestStatus, Integer> testRunSummary = parseTestRunSummary(testRun);
		final String testRunID = attributes.getNamedItem("id").getNodeValue();
		final String category = attributes.getNamedItem("category").getNodeValue();
		// optional attributes
		final Node platformNode = attributes.getNamedItem("platform");

		// KN: It is null in DT 6.1 and probably in 6.2 - should not happen in 6.3+
		// We use this value when there is no "platform" attribute in TestResult node
		final String platform = platformNode == null ? null : platformNode.getNodeValue();

		final NodeList testResults = testRun.getChildNodes();
		final List<TestResult> testResultsList = new ArrayList<TestResult>();
		for (int i = 0; i < testResults.getLength(); i++) {
            Node testResult = testResults.item(i);
            if ("testResult".equals(testResult.getNodeName())) {
                TestResult testCase = parseTestResult(testResult, platform);
                testResultsList.add(testCase);
            }
        }
		return new TestRun(testResultsList, testRunSummary, testRunID, TestCategory.fromString(category));
	}

	private static Map<TestStatus, Integer> parseTestRunSummary(Node testRun) {
		NamedNodeMap attributes = testRun.getAttributes();
		Map<TestStatus, Integer> testRunSummary = new EnumMap<TestStatus, Integer>(TestStatus.class);
		testRunSummary.put(TestStatus.FAILED, Integer.parseInt(attributes.getNamedItem("numFailed").getNodeValue()));
		testRunSummary.put(TestStatus.DEGRADED, Integer.parseInt(attributes.getNamedItem("numDegraded").getNodeValue()));
		testRunSummary.put(TestStatus.VOLATILE, Integer.parseInt(attributes.getNamedItem("numVolatile").getNodeValue()));
		testRunSummary.put(TestStatus.IMPROVED, Integer.parseInt(attributes.getNamedItem("numImproved").getNodeValue()));
		testRunSummary.put(TestStatus.PASSED, Integer.parseInt(attributes.getNamedItem("numPassed").getNodeValue()));
		return testRunSummary;
	}

	private static TestResult parseTestResult(Node testResult, String platform) {
		NamedNodeMap attributes = testResult.getAttributes();
		// mandatory attributes
		String timestamp = attributes.getNamedItem("exectime").getNodeValue();
		String testName = attributes.getNamedItem("name").getNodeValue();
		TestStatus status = TestStatus.fromString(attributes.getNamedItem("status").getNodeValue());
		// optional attributes
		Node packageNameNode = attributes.getNamedItem("package");
		Node platformNode = attributes.getNamedItem("platform");

		// KN: It is null in older DT versions
		String packageName = packageNameNode == null ? null : packageNameNode.getNodeValue();
		if (platformNode != null) {
			platform = platformNode.getNodeValue();
		}

		// testMeasures
		NodeList measures = testResult.getChildNodes();
		TreeSet<TestMeasure> measuresSet = new TreeSet<TestMeasure>();
		for (int j = 0; j < measures.getLength(); j++) {
            Node measure = measures.item(j);
            if ("measure".equals(measure.getNodeName())) {
                TestMeasure tm = parseTestMeasure(measure);
                measuresSet.add(tm);
            }
        }

		return new TestResult(new Date(Long.parseLong(timestamp)), testName, packageName, platform, status, measuresSet);
	}

	private static TestMeasure parseTestMeasure(Node testMeasure) {
		NamedNodeMap attributes = testMeasure.getAttributes();
		// mandatory attributes
		String name = attributes.getNamedItem("name").getNodeValue();
		String metricGroup = attributes.getNamedItem("metricGroup").getNodeValue();
		String value = attributes.getNamedItem("value").getNodeValue();
		// optional attributes
		Node violationPercentageNode = attributes.getNamedItem("violationPercentage");
		Node expectedMinNode = attributes.getNamedItem("expectedMin");
		Node expectedMaxNode = attributes.getNamedItem("expectedMax");
		Node unitNode = attributes.getNamedItem("unit");

		String violationPercentage = violationPercentageNode == null ? null : violationPercentageNode.getNodeValue();
		// KN: Safe in case that corridor is not calculated yet
		String expectedMin = expectedMinNode == null ? null : expectedMinNode.getNodeValue();
		String expectedMax = expectedMaxNode == null ? null : expectedMaxNode.getNodeValue();
		// WG: unit might be null for browser performance reports
		// Update KN: In DT 6.5+ shouldn't happen
		String unit = unitNode == null ? TEST_MEASURE_UNIT_DEFAULT : unitNode.getNodeValue();

		return new TestMeasure(name,
				metricGroup,
				doubleValueOf(expectedMin),
				doubleValueOf(expectedMax),
				doubleValueOf(value),
				unit,
				doubleValueOf(violationPercentage));
	}

	/**
	 * Returns a {@code Double} object holding the {@code double} value represented by the argument string {@code s}.
	 * Supports also the special symbols {@link #TEST_MEASURE_DOUBLE_POSITIVE_INF} and {@link #TEST_MEASURE_DOUBLE_NEGATIVE_INF}.
	 *
	 * <p>If {@code s} is {@code null} or does not contain a parsable number, the return value is {@code null}.
	 * <p>If {@code s} is equals {@link #TEST_MEASURE_DOUBLE_POSITIVE_INF}, the return value is {@link Double#POSITIVE_INFINITY}.
	 * <p>If {@code s} is equals {@link #TEST_MEASURE_DOUBLE_NEGATIVE_INF}, the return value is {@link Double#NEGATIVE_INFINITY}.
	 *
	 * @param      s   the string to be parsed.
	 * @return     a {@code Double} object holding the value represented by the {@code String} argument or {@code null}.
	 */
	private static Double doubleValueOf(String s) {
		if (s == null) {
			return null;
		} else if (TEST_MEASURE_DOUBLE_POSITIVE_INF.equals(s)) {
			return Double.POSITIVE_INFINITY;
		} else if (TEST_MEASURE_DOUBLE_NEGATIVE_INF.equals(s)) {
			return Double.NEGATIVE_INFINITY;
		}
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException e) {
			Logger.getGlobal().log(Level.WARNING, "XmlResponseParser.doubleValueOf(): " + e.toString());
			return null;
		}
	}
}
