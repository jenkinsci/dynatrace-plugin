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
package com.dynatrace.jenkins.dashboard.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class DynaTraceServerRestConnection {

	private static final Logger logger = Logger
			.getLogger(DynaTraceServerRestConnection.class.getName());
	public static final String DASHBOARD_PATH = "/rest/management/dashboard/";
	public static final String OPEN_DASHBOARD_PATH = "/rest/integration/opendashboard";
	
	private WebResource restResource;

	public DynaTraceServerRestConnection(final String protocol, final String host,
			final String port, final String username, final String password,
			final String dashboardName) {
		URI uri = null;
		try {
			uri = new URI(protocol, null, host, Integer.parseInt(port), DASHBOARD_PATH + dashboardName, null, null);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		logger.info("Connecting to " + uri + " using username \"" + username+"\"");

		DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		config.getState().setCredentials(null, null, -1, username, password);
		Client restClient = ApacheHttpClient.create(config);
		restClient.setFollowRedirects(true);

		restResource = restClient.resource(uri);
	}

	public String getDashboardReport() {
		String s = restResource.get(ClientResponse.class).getEntity(String.class);
		return s;
	}

	/*
	 * Validate methods - used by TestAutomationRecorder to verify input from
	 * configuration page is correct
	 */
	public static boolean validateRestUri(String dynaTraceRestUri) {
		return false;
	}

	public static boolean validateUsername(final String username) {
		return !isEmpty(username);
	}

	public static boolean validatePassword(final String password) {
		return !isEmpty(password);
	}

	public boolean validateConnection() {
		boolean validationResult = false;

		try {
			ClientResponse response = restResource.get(ClientResponse.class);

			if (response.getStatus() == 200) {
				String output = response.getEntity(String.class);
				logger.info(String.format(
						"Response from dynaTrace: code: %s, output: %s",
						response.getStatus(), output));
				validationResult = true;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"Some problem connecting to the dynaTrace REST interface, see stack-trace for "
							+ "more information", e);
		}

		return validationResult;
	}

	/* Helper methods */

	/**
	 * Checks if a string is empty (null, empty, or only spaces)
	 **/
	private static boolean isEmpty(final String field) {
		if (field == null || field.isEmpty()) {
			return true;
		}

		final String trimmedField = field.trim();
		if (trimmedField.length() == 0) {
			return true;
		}

		return false;
	}
}
