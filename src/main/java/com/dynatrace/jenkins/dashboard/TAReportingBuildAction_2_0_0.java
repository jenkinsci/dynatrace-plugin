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

import com.dynatrace.jenkins.dashboard.model_2_0_0.TAReport;
import com.dynatrace.jenkins.dashboard.rest.ServerRestURIManager;
import com.dynatrace.jenkins.dashboard.utils.Utils;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.GlobalConfiguration;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerProxy;

import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by krzysztof.necel on 2016-02-05.
 */
public class TAReportingBuildAction_2_0_0 implements Action, StaplerProxy, SimpleBuildStep.LastBuildAction, RunAction2{

	private static final String URL_NAME = "dynatrace-test-result";

	private Run<?, ?> build;
	private final String storedSessionName;
	private final TAReport currentReport;
	private transient TAReport previousReport;

	public TAReportingBuildAction_2_0_0(Run<?, ?> build, String storedSessionName, TAReport report) {
		this.build = build;
		this.storedSessionName = storedSessionName;
		this.currentReport = report;
	}

	@Override
	public String getDisplayName() {
		return Messages.BUILD_ACTION_DISPLAY_NAME();
	}

	@Override
	public String getIconFileName() {
		return Utils.DYNATRACE_ICON_24_X_24_FILEPATH;
	}

	@Override
	public String getUrlName() {
		return URL_NAME;
	}

	@Override
	public TAReportingBuildAction_2_0_0 getTarget() {
		addPreviousBuildReport();
		return this;
	}

	public Run<?, ?> getBuild() {
		return build;
	}

	public String getStoredSessionURL() {
		if (storedSessionName != null) {
			try {
				final TAGlobalConfiguration globalConfig = GlobalConfiguration.all().get(TAGlobalConfiguration.class);
				final ServerRestURIManager uriManager;
				if (globalConfig != null) {
					uriManager = new ServerRestURIManager(globalConfig.protocol, globalConfig.host, globalConfig.port);
					return uriManager.getExportStoredSessionRequestURI(storedSessionName).toString();
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public TAReport getCurrentReport() {
		return currentReport;
	}

	public TAReport getPreviousReport() {
		return previousReport;
	}

	public void setBuild(Run<?, ?> build) {
		this.build = build;
	}

	private void addPreviousBuildReport() {
		Run<?, ?> previousBuild = build.getPreviousBuild();
		if (previousBuild == null) {
			previousReport = null;
			return;
		}

		TAReportingBuildAction_2_0_0 prevBuildAction = previousBuild.getAction(TAReportingBuildAction_2_0_0.class);
		if (prevBuildAction == null) {
			previousReport = null;
			return;
		}

		previousReport = prevBuildAction.getCurrentReport();
	}

	/**
	 * Custom message format method. Used in jelly script to be able to resolve externalized strings only once per request.
	 * It significantly improves performance in old Jenkins versions but code is a bit less clear.
	 */
	public static String formatMessage(String pattern, Object argument) {
		return MessageFormat.format(pattern, argument);
	}

	@Override
	public void onAttached(Run<?, ?> r) {
		setBuild(r);
	}

	@Override
	public void onLoad(Run<?, ?> r) {
		setBuild(r);
	}

	@Override
	public Collection<? extends Action> getProjectActions() {
		return Collections.singletonList(new TAReportingProjectAction(build.getParent()));
	}

}
