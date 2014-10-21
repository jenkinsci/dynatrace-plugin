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
package com.dynatrace.jenkins.dashboard;

import hudson.model.AbstractBuild;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dynatrace.jenkins.dashboard.model.TestCase;
import com.dynatrace.jenkins.dashboard.model.TestCaseStatus;
import com.dynatrace.jenkins.dashboard.rest.DynaTraceServerRestConnection;
import com.dynatrace.jenkins.dashboard.rest.XmlDashboardParser;

public class TestAutomationDataCollector {
	private PrintStream logger;
	private DynaTraceServerRestConnection connection;
	private AbstractBuild<?, ?> build;
	private Document xmlDashboardReport;
	private XmlDashboardParser parser;
	private String testruninfoId;

	public TestAutomationDataCollector(PrintStream logger,
			DynaTraceServerRestConnection connection, AbstractBuild<?, ?> build) {
		this.logger = logger;
		this.connection = connection;
		this.build = build;
		this.parser = new XmlDashboardParser();
	}

	public TestAutomationReport createReportFromBuild(String testruninfoId) {
		if (xmlDashboardReport == null) {
			logger.println("XML Dashboard Report is NULL!");
			return null;
		}
		TestAutomationReport report = new TestAutomationReport();
		logger.println("parsing XML report");
		List<TestCase> testCases = parser.parse(xmlDashboardReport, testruninfoId);
		logger.println(String.format("got results for %s test cases",
				testCases.size()));
		report.setExecutedTestCases(testCases);
		report.setTestCaseSummary(getTestCaseSummary(testCases));
		return report;
	}

	private Map<TestCaseStatus, Integer> getTestCaseSummary(
			List<TestCase> testCases) {
		Map<TestCaseStatus, Integer> result = new TreeMap<TestCaseStatus, Integer>();
		// prepopulate map
		result.put(TestCaseStatus.DEGRADED, 0);
		result.put(TestCaseStatus.PASSED, 0);
		result.put(TestCaseStatus.FAILED, 0);
		result.put(TestCaseStatus.IMPROVED, 0);
		result.put(TestCaseStatus.VOLATILE, 0);

		// add up the TestCaseStatus
		for (TestCase testCase : testCases) {
			result.put(testCase.getStatus(), result.get(testCase.getStatus()) + 1);
		}

		return result;
	}

	public String getTestRunInfoId(PrintStream logger, Boolean printXmlReportForDebug) {
		// get the XML dashboard report from the dT Server
		getXmlDocument(printXmlReportForDebug);
		// get testruninfoId from XML
		testruninfoId = parser.getTestruninfoIdForBuild(logger, xmlDashboardReport,
				build.getId());
		return testruninfoId;
	}

	private void getXmlDocument(boolean printXmlReportForDebug) {
		logger.println("Fetching XML Report from server");
		String xmlContent = connection.getDashboardReport();

		if (printXmlReportForDebug) {
			logger.println("For debugging: XML report from dynaTrace Server: "
					+ xmlContent);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		xmlDashboardReport = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			xmlDashboardReport = builder.parse(new InputSource(new StringReader(
					xmlContent)));
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
