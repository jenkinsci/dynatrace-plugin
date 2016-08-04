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
import com.dynatrace.jenkins.dashboard.model_2_0_0.TestRun;
import com.dynatrace.jenkins.dashboard.utils.Utils;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TAReportRetrieverByTestRunId extends TAReportRetriever {

	private final Collection<String> testRunIds;

	public TAReportRetrieverByTestRunId(String systemProfile, PrintStream logger, boolean printXmlReportForDebug, Collection<String> testRunIds) {
		super(systemProfile, logger, printXmlReportForDebug);
		this.testRunIds = testRunIds;
	}

	public TAReportDetails fetchReport() throws InterruptedException, ServerConnectionException, ServerResponseException {
		final List<TestRun> testRunsList = new ArrayList<>();
		waitForDelay(); // BEFORE we actually try to fetch the data from the server - wait for the configured delay

		for (String testRunId : testRunIds) {
			while (true) {
				TestRun testRun = fetchSingleTestRun(testRunId);
				if (!testRun.isEmpty() || currentTry >= retryCount) {
					testRunsList.add(testRun);
					break;
				}
				currentTry++;
				waitForDelay();
			}
		}

		return new TAReportDetails(testRunsList);
	}

	private TestRun fetchSingleTestRun(String testRunId) throws ServerConnectionException, ServerResponseException {
		logger.println(String.format("Connecting to Dynatrace Server REST interface... (ID=%s)", testRunId));
		return Utils.convertTestRun(connection.fetchTestRun(systemProfile, testRunId));
	}
}
