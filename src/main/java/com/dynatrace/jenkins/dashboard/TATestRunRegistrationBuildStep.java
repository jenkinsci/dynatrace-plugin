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
import com.dynatrace.sdk.server.testautomation.TestAutomation;
import com.dynatrace.sdk.server.testautomation.models.CreateTestRunRequest;
import com.dynatrace.sdk.server.testautomation.models.TestCategory;
import com.dynatrace.sdk.server.testautomation.models.TestMetaData;
import com.dynatrace.sdk.server.testautomation.models.TestRun;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cwpl-dglugla
 */
public class TATestRunRegistrationBuildStep extends Builder {

	private final String category;
	private final String platform;

	@DataBoundConstructor
	public TATestRunRegistrationBuildStep(String category, String platform) {
		this.category = category;
		this.platform = platform;
	}

	public String getCategory() {
		return category;
	}

	public String getPlatform() {
		return platform;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		final PrintStream logger = listener.getLogger();
		if (!Utils.isValidBuild(build, logger, "test run won't be registered")) {
			return true;
		}

		logger.println("Registering test run via Dynatrace Server REST interface...");
		try {
			Map<String, String> variables = build.getBuildVariables();
			String systemProfile = variables.get(BuildVarKeys.BUILD_VAR_KEY_SYSTEM_PROFILE);
			String versionMajor = variables.get(BuildVarKeys.BUILD_VAR_KEY_VERSION_MAJOR);
			String versionMinor = variables.get(BuildVarKeys.BUILD_VAR_KEY_VERSION_MINOR);
			String versionRevision = variables.get(BuildVarKeys.BUILD_VAR_KEY_VERSION_REVISION);
			String versionBuild = Integer.toString(build.getNumber());
			String versionMilestone = variables.get(BuildVarKeys.BUILD_VAR_KEY_VERSION_MILESTONE);
			String marker = variables.get(BuildVarKeys.BUILD_VAR_KEY_MARKER);

			Map<String, String> additionalInformation = new HashMap<>();
			additionalInformation.put("JENKINS_JOB", build.getUrl());

			final TestAutomation restEndpoint = new TestAutomation(Utils.createClient());
			CreateTestRunRequest request = new CreateTestRunRequest(systemProfile, versionBuild);
			request.setVersionMajor(versionMajor);
			request.setVersionMinor(versionMinor);
			request.setVersionRevision(versionRevision);
			request.setVersionBuild(versionBuild);
			request.setVersionMilestone(versionMilestone);
			request.setCategory(TestCategory.fromInternal(category));
			request.setPlatform(platform);
			request.setMarker(marker);
			request.setAdditionalMetaData(new TestMetaData(additionalInformation));
			TestRun testRun = restEndpoint.createTestRun(request);

			Utils.updateBuildVariable(build, BuildVarKeys.BUILD_VAR_KEY_TEST_RUN_ID, testRun.getId());
			updateTestRunIdsAction(build, testRun.getId());

			logger.println("Registered test run with ID=" + testRun.getId());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.println("ERROR: Dynatrace AppMon Plugin - build step execution failed (see the stacktrace to get more information):\n" + e.toString());
			return false;
		}
	}

	private void updateTestRunIdsAction(AbstractBuild build, String newTestRunId) {
		TATestRunIdsAction testRunIdsAction = build.getAction(TATestRunIdsAction.class);
		if (testRunIdsAction == null) {
			testRunIdsAction = new TATestRunIdsAction();
			build.addAction(testRunIdsAction);
		}
		testRunIdsAction.getTestRunIds().add(newTestRunId);
	}

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		private static final String DEFAULT_CATEGORY = TestCategory.UNIT.getInternal();

		public static String getDefaultCategory() {
			return DEFAULT_CATEGORY;
		}

		@Override
		public boolean isApplicable(Class aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return Messages.BUILD_STEP_DISPLAY_NAME();
		}

		public ListBoxModel doFillCategoryItems() {
			ListBoxModel model = new ListBoxModel();
			for (TestCategory category : TestCategory.values()) {
				model.add(category.getInternal());
			}
			return model;
		}
	}
}
