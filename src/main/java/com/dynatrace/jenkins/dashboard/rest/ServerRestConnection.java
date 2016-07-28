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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by krzysztof.necel on 2016-02-09.
 */
public class ServerRestConnection {

	private static final Logger LOGGER = Logger.getLogger(ServerRestConnection.class.getName());

	private final ServerRestURIManager uriManager;
	private final Client restClient;

	public ServerRestConnection(String protocol, String host, Integer port, String username, String password) {
		this.uriManager = new ServerRestURIManager(protocol, host, port);
		this.restClient = Client.create();
		this.restClient.addFilter(new HTTPBasicAuthFilter(username, password));
		this.restClient.setFollowRedirects(true);
	}

	public ClientResponse doGetSystemProfilesRequest() throws URISyntaxException {
		return doGet(uriManager.getSystemProfilesRequestURI());
	}

	public ClientResponse doGetTestConnectionWithSystemProfileRequest(final String systemProfileName)
			throws URISyntaxException {
		return doGet(uriManager.getTestConnectionWithSystemProfileRequestURI(systemProfileName));
	}

	public ClientResponse doGetTestRunsByBuildNumberRequest(final String systemProfileName, final int buildNumber)
			throws URISyntaxException {
		return doGet(uriManager.getTestRunsByBuildNumberRequestURI(systemProfileName, buildNumber));
	}

	public ClientResponse doGetTestRunByIdRequest(final String systemProfileName, final String testRunId)
			throws URISyntaxException {
		return doGet(uriManager.getTestRunByIdRequestURI(systemProfileName, testRunId));
	}

	private ClientResponse doGet(final URI uri) {
		LOGGER.log(Level.INFO, "Connecting to Dynatrace Server REST interface...: " + uri);
		WebResource restResource = restClient.resource(uri);
		ClientResponse response = restResource.get(ClientResponse.class);
		LOGGER.log(Level.INFO, "HTTP status code from DT Server: " + response.getStatus());
		return response;
	}
}
