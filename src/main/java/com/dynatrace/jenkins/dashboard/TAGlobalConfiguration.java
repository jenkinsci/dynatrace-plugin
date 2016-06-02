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
package com.dynatrace.jenkins.dashboard;

import com.dynatrace.jenkins.dashboard.rest.ServerRestConnection;
import com.sun.jersey.api.client.ClientHandlerException;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.net.ssl.SSLHandshakeException;

import static java.net.HttpURLConnection.*;

/**
 * Created by krzysztof.necel on 2016-02-10.
 */
@Extension
public class TAGlobalConfiguration extends GlobalConfiguration {

	private static final String DEFAULT_PROTOCOL = "https";
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 8021;
	private static final String DEFAULT_USERNAME = "admin";
	private static final int DEFAULT_DELAY = 10; // seconds
	private static final int DEFAULT_RETRY_COUNT = 3;

	public String protocol;
	public String host;
	public Integer port;
	public String username;
	public String password;
	public Integer delay;		// time to wait before trying to get data from the DT server in seconds
	public Integer retryCount;

	public TAGlobalConfiguration() {
		load(); // KN: load config from XML file on startup
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
		req.bindJSON(this, json.getJSONObject("dynatrace-test-automation")); // KN: section name from config.jelly
		save();
		return true;
	}

	public static String getDefaultProtocol() {
		return DEFAULT_PROTOCOL;
	}

	public static String getDefaultHost() {
		return DEFAULT_HOST;
	}

	public static int getDefaultPort() {
		return DEFAULT_PORT;
	}

	public static String getDefaultUsername() {
		return DEFAULT_USERNAME;
	}

	public static int getDefaultDelay() {
		return DEFAULT_DELAY;
	}

	public static int getDefaultRetryCount() {
		return DEFAULT_RETRY_COUNT;
	}

	public ListBoxModel doFillProtocolItems() {
		ListBoxModel model = new ListBoxModel();
		model.add("http");
		model.add("https");
		return model;
	}

	public FormValidation doCheckPort(@QueryParameter final String port) {
		try {
			final int i = Integer.parseInt(port);
			if (i > 0 && i < 65536) {
				return FormValidation.ok();
			}
			return FormValidation.error(Messages.RECORDER_VALIDATION_PORT_OUT_OF_RANGE());
		} catch (NumberFormatException e) {
			return FormValidation.error(Messages.RECORDER_VALIDATION_PORT_NAN());
		}
	}

	public FormValidation doCheckUsername(@QueryParameter final String username) {
		if (StringUtils.isNotBlank(username)) {
			return FormValidation.ok();
		} else {
			return FormValidation.error(Messages.RECORDER_VALIDATION_BLANK_USERNAME());
		}
	}

	public FormValidation doCheckPassword(@QueryParameter final String password) {
		if (StringUtils.isNotBlank(password)) {
			return FormValidation.ok();
		} else {
			return FormValidation.error(Messages.RECORDER_VALIDATION_BLANK_PASSWORD());
		}
	}

	public FormValidation doCheckDelay(@QueryParameter final String delay) {
		try {
			final int i = Integer.parseInt(delay);
			if (i >= 0) {
				return FormValidation.ok();
			}
		} catch (NumberFormatException e) { }
		return FormValidation.error(Messages.RECORDER_VALIDATION_DELAY_NAN());
	}

	public FormValidation doCheckRetryCount(@QueryParameter final String retryCount) {
		try {
			final int i = Integer.parseInt(retryCount);
			if (i >= 0) {
				return FormValidation.ok();
			}
		} catch (NumberFormatException e) { }
		return FormValidation.error(Messages.RECORDER_VALIDATION_RETRY_COUNT_NAN());
	}

	public FormValidation doTestDynatraceConnection(
			@QueryParameter("protocol") final String protocol,
			@QueryParameter("host") final String host,
			@QueryParameter("port") final String port,
			@QueryParameter("username") final String username,
			@QueryParameter("password") final String password) {

		try {
			final ServerRestConnection connection = new ServerRestConnection(protocol, host,
					Integer.parseInt(port), username, password);
			int status = connection.doGetSystemProfilesRequest().getStatus();
			switch (status) {
				case HTTP_OK:
					return FormValidation.ok(Messages.RECORDER_VALIDATION_CONNECTION_OK());
				case HTTP_UNAUTHORIZED:
					return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_UNAUTHORIZED());
				default:
					return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_OTHER_CODE(status));
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ClientHandlerException && e.getCause() instanceof SSLHandshakeException) {
				return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_CERT_EXCEPTION(e.toString()));
			}
			return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_UNKNOWN(e.toString()));
		}
	}
}
