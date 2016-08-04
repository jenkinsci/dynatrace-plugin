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

import com.dynatrace.jenkins.dashboard.model_2_0_0.TAReportDetails;
import com.dynatrace.jenkins.dashboard.utils.Utils;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.testautomation.models.FetchTestRunsRequest;
import com.dynatrace.sdk.server.testautomation.models.TestRuns;

import java.io.PrintStream;
import java.util.NoSuchElementException;

public class TAReportRetrieverByBuildNumber extends TAReportRetriever {

	private final int buildNumber;

	public TAReportRetrieverByBuildNumber(String systemProfile, PrintStream logger, boolean printXmlReportForDebug, int buildNumber) {
		super(systemProfile, logger, printXmlReportForDebug);
		this.buildNumber = buildNumber;
	}

	public TAReportDetails fetchReport() throws ServerConnectionException, ServerResponseException, InterruptedException, NoSuchElementException {
		for (currentTry = 0; currentTry <= retryCount; ++currentTry) {
			waitForDelay(); // BEFORE we actually try to fetch the data from the server - wait for the configured delay

			TAReportDetails currentReport = fetchReportOnce();

			if (!currentReport.isEmpty()) {
				return currentReport;
			}

			// retry if there is no test executions
			logger.println("The test runs don't contain any test execution!");
		}

		throw new NoSuchElementException("No matching test run with test executions has been recorded by the configured Dynatrace Server");
	}

	private TAReportDetails fetchReportOnce() throws ServerConnectionException, ServerResponseException {
		logger.println("Connecting to Dynatrace Server REST interface...");
		FetchTestRunsRequest request = new FetchTestRunsRequest();
		request.setVersionBuildFilter(String.valueOf(buildNumber));
		TestRuns tr = connection.fetchTestRuns(request);
		return Utils.convertTestRuns(tr);
	}
}
