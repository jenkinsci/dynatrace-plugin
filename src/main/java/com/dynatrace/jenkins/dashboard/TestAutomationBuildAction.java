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

 * @date: 28.8.2013
 * @author: cwat-wgottesh
 */
package com.dynatrace.jenkins.dashboard;

import com.dynatrace.jenkins.dashboard.utils.Utils;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.StreamTaskListener;
import org.kohsuke.stapler.StaplerProxy;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class TestAutomationBuildAction implements Action, StaplerProxy {

	private static final String DISPLAY_NAME = "Test Result (legacy)";
	private static final String URL_NAME = "dynatrace-test-result";

	private final AbstractBuild<?, ?> build;
	private final TestAutomationReport report;
	private transient WeakReference<TestAutomationBuildActionResultsDisplay> buildActionResultsDisplay;

	private transient static final Logger logger = Logger
			.getLogger(TestAutomationBuildAction.class.getName());

	public TestAutomationBuildAction(AbstractBuild<?, ?> build,
			TestAutomationReport report) {
		this.build = build;
		this.report = report;
	}

	public String getIconFileName() {
		return Utils.DYNATRACE_ICON_24_X_24_FILEPATH;
	}

	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	public String getUrlName() {
		return URL_NAME;
	}

	public TestAutomationBuildActionResultsDisplay getTarget() {
		return getBuildActionResultsDisplay();
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public TestAutomationReport getTestAutomationReport() {
		return report;
	}

	public TestAutomationBuildActionResultsDisplay getBuildActionResultsDisplay() {
		TestAutomationBuildActionResultsDisplay buildDisplay = null;
		WeakReference<TestAutomationBuildActionResultsDisplay> wr = this.buildActionResultsDisplay;
		if (wr != null) {
			buildDisplay = wr.get();
			if (buildDisplay != null)
				return buildDisplay;
		}

		try {
			buildDisplay = new TestAutomationBuildActionResultsDisplay(this,StreamTaskListener.fromStdout());
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"Error creating new BuildActionResultsDisplay()", e);
		}
		this.buildActionResultsDisplay = new WeakReference<TestAutomationBuildActionResultsDisplay>(buildDisplay);
		return buildDisplay;
	}

	public void setBuildActionResultsDisplay(
			WeakReference<TestAutomationBuildActionResultsDisplay> buildActionResultsDisplay) {
		this.buildActionResultsDisplay = buildActionResultsDisplay;
	}
}
