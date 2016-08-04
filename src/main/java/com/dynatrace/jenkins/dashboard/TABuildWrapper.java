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

import com.dynatrace.jenkins.dashboard.utils.BuildVarKeys;
import com.dynatrace.jenkins.dashboard.utils.Utils;
import com.dynatrace.sdk.server.exceptions.ServerConnectionException;
import com.dynatrace.sdk.server.exceptions.ServerResponseException;
import com.dynatrace.sdk.server.sessions.Sessions;
import com.dynatrace.sdk.server.sessions.models.StartRecordingRequest;
import com.dynatrace.sdk.server.testautomation.TestAutomation;
import com.dynatrace.sdk.server.testautomation.models.FetchTestRunsRequest;
import com.sun.jersey.api.client.ClientHandlerException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.*;

/**
 * Created by krzysztof.necel on 2016-02-09.
 */
public class TABuildWrapper extends BuildWrapper {

	/**
	 * The 1st arg is system profile name, the 2nd is build number
	 */
	private static final String RECORD_SESSION_NAME = "%s_Jenkins_build_%s";

	public final String systemProfile;
	// Test run attributes - no versionBuild attribute because it's taken from the build object
	public final String versionMajor;
	public final String versionMinor;
	public final String versionRevision;
	public final String versionMilestone;
	public final String marker;
	public final Boolean recordSession;

	@DataBoundConstructor
	public TABuildWrapper(final String systemProfile, final String versionMajor, final String versionMinor,
						  final String versionRevision, final String versionMilestone, final String marker,
						  final Boolean recordSession) {
		this.systemProfile = systemProfile;
		this.versionMajor = versionMajor;
		this.versionMinor = versionMinor;
		this.versionRevision = versionRevision;
		this.versionMilestone = versionMilestone;
		this.marker = marker;
		this.recordSession = recordSession;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		final Sessions sessions = new Sessions(Utils.createClient());
		final TAGlobalConfiguration globalConfig = GlobalConfiguration.all().get(TAGlobalConfiguration.class);
		final PrintStream logger = listener.getLogger();
		try {
			String serverUrl = new URI(globalConfig.protocol, null, globalConfig.host, globalConfig.port, null, null, null).toString();

			if (recordSession) {
				logger.println("Starting session recording via Dynatrace Server REST interface...");


				StartRecordingRequest request = new StartRecordingRequest(systemProfile);
				request.setPresentableName(String.format(RECORD_SESSION_NAME, systemProfile, build.getNumber()));

				final String sessionNameOut = sessions.startRecording(request);
				logger.println("Dynatrace session " + sessionNameOut + " has been started");
			}

			setupBuildVariables(build, serverUrl);
		} catch (Exception e) {
			e.printStackTrace();
			build.addAction(new TABuildSetupStatusAction(true));
			logger.println("ERROR: Dynatrace AppMon Plugin - build set up failed (see the stacktrace to get more information):\n" + e.toString());
		}

		return new Environment() {

			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				final PrintStream logger = listener.getLogger();
				logger.println("Dynatrace AppMon Plugin - build tear down...");
				try {
					if (recordSession) {
						final String storedSessionName = storeSession(logger);
						Utils.updateBuildVariable(build, BuildVarKeys.BUILD_VAR_KEY_STORED_SESSION_NAME, storedSessionName);
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.println("ERROR: Dynatrace AppMon Plugin - build tear down failed (see the stacktrace to get more information):\n" + e.toString());
				}
				return true;
			}

			/**
			 * @return stored session name
			 */
			private String storeSession(final PrintStream logger) {
				logger.println("Storing session via Dynatrace Server REST interface...");
				String sessionNameOut = null;
				try {
					sessionNameOut = sessions.stopRecording(systemProfile);
					logger.println("Dynatrace session " + sessionNameOut + " has been stored");
				} catch (ServerResponseException | ServerConnectionException e) {
					e.printStackTrace();
					logger.println("ERROR: Dynatrace AppMon Plugin - store session failed (see the stacktrace to get more information):\n" + e.toString());
				}
				return sessionNameOut;
			}
		};
	}

	private void setupBuildVariables(AbstractBuild build, String serverUrl) {
		final TAGlobalConfiguration globalConfig = GlobalConfiguration.all().get(TAGlobalConfiguration.class);

		List<ParameterValue> parameters = new ArrayList<>(10);
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_SYSTEM_PROFILE, systemProfile));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_VERSION_MAJOR, versionMajor));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_VERSION_MINOR, versionMinor));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_VERSION_REVISION, versionRevision));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_VERSION_BUILD, Integer.toString(build.getNumber())));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_VERSION_MILESTONE, versionMilestone));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_MARKER, marker));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_GLOBAL_SERVER_URL, serverUrl));
		parameters.add(new StringParameterValue(BuildVarKeys.BUILD_VAR_KEY_GLOBAL_USERNAME, globalConfig.username));
		parameters.add(new PasswordParameterValue(BuildVarKeys.BUILD_VAR_KEY_GLOBAL_PASSWORD, globalConfig.password));

		Utils.updateBuildVariables(build, parameters);
	}

	@Extension
	public static class DescriptorImpl extends BuildWrapperDescriptor {

		private static final boolean DEFAULT_RECORD_SESSION = false;

		public static boolean getDefaultRecordSession() {
			return DEFAULT_RECORD_SESSION;
		}

		@Override
		public String getDisplayName() {
			return Messages.BUILD_WRAPPER_DISPLAY_NAME();
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> abstractProject) {
			return true;
		}

		public FormValidation doCheckSystemProfile(@QueryParameter final String systemProfile) {
			if (StringUtils.isNotBlank(systemProfile)) {
				return FormValidation.ok();
			} else {
				return FormValidation.error(Messages.RECORDER_VALIDATION_BLANK_SYSTEM_PROFILE());
			}
		}

		public FormValidation doTestDynatraceConnection(@QueryParameter final String systemProfile) {
			try {
				final TestAutomation connection = new TestAutomation(Utils.createClient());
				FetchTestRunsRequest request = new FetchTestRunsRequest(systemProfile);
				//We set many constraints to ENSURE no or few testruns are returned as this is testing the connection only
				request.setVersionBuildFilter("1024");
				request.setVersionMajorFilter("1024");
				request.setMaxBuilds(1);
				try {
					connection.fetchTestRuns(request);
				} catch (ServerResponseException e) {
					switch (e.getStatusCode()) {
						case HTTP_UNAUTHORIZED:
							return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_UNAUTHORIZED());
						case HTTP_FORBIDDEN:
							return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_FORBIDDEN());
						case HTTP_NOT_FOUND:
							return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_NOT_FOUND());
						default:
							return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_OTHER_CODE(e.getStatusCode()));
					}
				}
				return FormValidation.ok(Messages.RECORDER_VALIDATION_CONNECTION_OK());
			} catch (Exception e) {
				e.printStackTrace();
				if (e.getCause() instanceof ClientHandlerException && e.getCause().getCause() instanceof SSLHandshakeException) {
					return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_CERT_EXCEPTION(e.toString()));
				}
				return FormValidation.warning(Messages.RECORDER_VALIDATION_CONNECTION_UNKNOWN(e.toString()));
			}
		}
	}
}
