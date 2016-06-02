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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by krzysztof.necel on 2016-03-16.
 */
public class ServerRestURIManager {

	private static final String PATH_SYSTEM_PROFILES = "/rest/management/profiles";
	private static final String PATH_TEST_RUNS_BY_BUILD_NUMBER = "/rest/management/profiles/%s/testruns.xml";
	private static final String PATH_TEST_RUN_BY_ID = "/rest/management/profiles/%s/testruns/%s.xml";
	private static final String PATH_EXPORT_STORED_SESSION = "/rest/management/storedsessions/%s.dts";

	private static final String QUERY_TEST_RUNS_BY_BUILD_NUMBER = "startTime=0&extend=measures&versionBuild=";
	private static final String QUERY_TEST_CONNECTION_WITH_SYSTEM_PROFILE = "startTime=0&extend=testRuns&versionRevision=1024&versionBuild=1024";

	private final String protocol;
	private final String host;
	private final Integer port;

	public ServerRestURIManager(String protocol, String host, Integer port) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
	}

	public URI getSystemProfilesRequestURI() throws URISyntaxException {
		return new URI(protocol, null, host, port, PATH_SYSTEM_PROFILES, null, null);
	}

	public URI getTestConnectionWithSystemProfileRequestURI(final String systemProfileName) throws URISyntaxException {
		return new URI(protocol, null, host, port, String.format(PATH_TEST_RUNS_BY_BUILD_NUMBER, systemProfileName),
				QUERY_TEST_CONNECTION_WITH_SYSTEM_PROFILE, null);
	}

	public URI getTestRunsByBuildNumberRequestURI(final String systemProfileName, final int buildNumber) throws URISyntaxException {
		return new URI(protocol, null, host, port, String.format(PATH_TEST_RUNS_BY_BUILD_NUMBER, systemProfileName),
				QUERY_TEST_RUNS_BY_BUILD_NUMBER + buildNumber, null);
	}

	public URI getTestRunByIdRequestURI(final String systemProfileName, final String testRunId) throws URISyntaxException {
		return new URI(protocol, null, host, port, String.format(PATH_TEST_RUN_BY_ID, systemProfileName, testRunId), null, null);
	}

	public URI getExportStoredSessionRequestURI(final String storedSessionName) throws URISyntaxException {
		return new URI(protocol, null, host, port, String.format(PATH_EXPORT_STORED_SESSION, storedSessionName), null, null);
	}
}
