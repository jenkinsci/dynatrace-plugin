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
package com.dynatrace.jenkins.dashboard;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.dynatrace.jenkins.dashboard.model.TestCaseStatus;
import com.dynatrace.jenkins.dashboard.rest.DynaTraceServerRestConnection;
import com.dynatrace.jenkins.dashboard.util.Messages;

public class TestAutomationRecorder extends Recorder {
	private static final String DEFAULT_USERNAME = "admin";
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PROTOCOL = "http";
	private static final String DEFAULT_PORT = "8020";

	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Override
		public String getDisplayName() {
			return Messages.PUBLISHER_DISPLAYNAME;
		}

		@Override
		public String getHelpFile() {
			return "/plugin/dynatrace-testautomation-dashboard/help.html";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		public String getDefaultUsername() {
			return DEFAULT_USERNAME;
		}
		
		public String getDefaultHost() {
			return DEFAULT_HOST;
		}
		
		public String getDefaultPort() {
			return DEFAULT_PORT;
		}
		

		public ListBoxModel doFillProtocolItems() {
			ListBoxModel model = new ListBoxModel();
			model.add("http");
			model.add("https");
			return model;
		}

		public FormValidation doCheckDynaTraceRestUri(
				@QueryParameter final String dynaTraceRestUri) {
			FormValidation validationResult;

			if (DynaTraceServerRestConnection.validateRestUri(dynaTraceRestUri)) {
				validationResult = FormValidation.ok();
			} else {
				validationResult = FormValidation.error("dynaTrace Server REST URI is not valid");
			}

			return validationResult;
		}

		public FormValidation doCheckUsername(@QueryParameter final String username) {
			FormValidation validationResult;

			if (DynaTraceServerRestConnection.validateUsername(username)) {
				validationResult = FormValidation.ok();
			} else {
				validationResult = FormValidation
						.error("Username for REST interface cannot be empty");
			}

			return validationResult;
		}

		public FormValidation doCheckPassword(@QueryParameter final String password) {
			FormValidation validationResult;

			if (DynaTraceServerRestConnection.validatePassword(password)) {
				validationResult = FormValidation.ok();
			} else {
				validationResult = FormValidation
						.error("Password for REST interface cannot be empty");
			}

			return validationResult;
		}

		public FormValidation doTestDynaTraceConnection(
				@QueryParameter("protocol") final String protocol,
				@QueryParameter("host") final String host,
				@QueryParameter("port") final String port,
				@QueryParameter("username") final String username,
				@QueryParameter("password") final String password,
				@QueryParameter("dashboard") final String dashboard) {
			FormValidation validationResult;
			DynaTraceServerRestConnection connection = new DynaTraceServerRestConnection(
					protocol, host, port, username, password, dashboard);

			if (connection.validateConnection()) {
				validationResult = FormValidation.ok("Connection successful");
			} else {
				validationResult = FormValidation.warning("Connection with dynaTrace RESTful interface could not be established");
			}

			return validationResult;
		}
	}

	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	private DynaTraceServerRestConnection connection;
	/**
	 * Below fields are configured via the <code>config.jelly</code> page.
	 */
	private String protocol = "";
	private String host = "";
	private String port = "";
	private String username = "";
	private String password = "";
	private String dashboard = "";
	private Integer retryCount = 4;
	private Integer delay = 10; // time to wait before trying to get data from the dT server in seconds
	private Boolean printXmlReportForDebug = false;
	private Boolean failBuildWhendTFails = true;

	@DataBoundConstructor
	public TestAutomationRecorder(final String protocol, final String host,
			final String port, final String username, final String password,
			final String dashboard, final Boolean failBuildWhendTFails, final Integer retryCount, final Integer delay, final Boolean printXmlReportForDebug) {
		setProtocol(protocol);
		setHost(host);
		setPort(port);
		setUsername(username);
		setPassword(password);
		setDashboard(dashboard);
		setFailBuildWhendTFails(failBuildWhendTFails);
		setRetryCount(retryCount);
		setDelay(delay);
		setPrintXmlReportForDebug(printXmlReportForDebug);
	}

	public Boolean getPrintXmlReportForDebug() {
		return printXmlReportForDebug;
	}

	public void setPrintXmlReportForDebug(Boolean printXmlReportForDebug) {
		this.printXmlReportForDebug = printXmlReportForDebug;
	}

	@Override
	public BuildStepDescriptor<Publisher> getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return new TestAutomationProjectAction(project);
	}

	public BuildStepMonitor getRequiredMonitorService() {
		// No synchronization necessary between builds
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		PrintStream logger = listener.getLogger();
		
		// BEFORE we actually try do fetch something from the server - wait for the configured delay
		if (delay != 0) {
			logger.println("Sleeping for the configured delay of " + delay + "sec");
			Thread.sleep(delay*1000);
		}
		
		DynaTraceServerRestConnection connection = new DynaTraceServerRestConnection(
				protocol, host, port, username, password, dashboard);
		logger.println("Verify connection to dynaTrace Server REST interface ...");
		if (!connection.validateConnection()) {
			logger.println("Connection to dynaTrace Server REST interface unsuccessful, cannot proceed with this build step");
			if (build.getResult().isBetterOrEqualTo(Result.UNSTABLE))
				build.setResult(Result.FAILURE);
			return true;
		}
		
		logger.println("Connection successful, getting testruninfo for this build ("+ build.getId() + ")");
		TestAutomationDataCollector dataCollector = new TestAutomationDataCollector(logger, connection, build);
		String testruninfoId = dataCollector.getTestRunInfoId(logger, printXmlReportForDebug);

		// processing might take a while - if we don't get the testruninfo right away, let's wait for n*10s and retry
		int retryCount = 0;
		while (testruninfoId == null && retryCount < getRetryCount()) {
			logger.println("Waiting for 10 more seconds to get test run info ID from dynaTrace... "  + retryCount + " try out of " + getRetryCount());
			Thread.sleep(10000);
			testruninfoId = dataCollector.getTestRunInfoId(logger, printXmlReportForDebug);
			retryCount++;
		}
		
		if (testruninfoId == null) {
			// no test run recorded by the dynaTrace server
			logger
					.println("No test run recorded by the configured dynaTrace server - skipping data collection");
			return true;
		} else {
			logger.println("Got testruninfoid " + testruninfoId
					+ ", continue with fetching measurements from dynaTrace Server...");
			
			TestAutomationReport report = dataCollector.createReportFromBuild(testruninfoId);
			report.setProtocol(getProtocol());
			report.setHost(getHost());
			report.setPort(getPort());
			report.setDashboardName(getDashboard());
			
			logger.println("Report built!");
			logger.println("TestCase Summary: " + report.getTestCaseSummary() + ", fail build on results: " + failBuildWhendTFails);
			TestAutomationBuildAction buildAction = new TestAutomationBuildAction(build, report);
			build.addAction(buildAction);

			if (failBuildWhendTFails) {
				// mark the build as unstable or failure depending on what dynaTrace
				// says -> if # of failing tests > 0 build fails
				if (report.getTestCaseSummary().get(TestCaseStatus.FAILED) > 0) {
					build.setResult(Result.FAILURE);
				} else if (report.getTestCaseSummary().get(TestCaseStatus.VOLATILE) > 0) {
					// we don't have any failed tests, but volatile ones -> unstable
					build.setResult(Result.UNSTABLE);
				}
			}

			logger.println("Build status is: " + build.getResult());
			return true;
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public DynaTraceServerRestConnection getConnection() {
		return connection;
	}

	public void setConnection(DynaTraceServerRestConnection connection) {
		this.connection = connection;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getDashboard() {
		return dashboard;
	}

	public void setDashboard(String dashboard) {
		this.dashboard = dashboard;
	}

	public void setFailBuildWhendTFails(Boolean failBuildWhendTFails) {
		this.failBuildWhendTFails = failBuildWhendTFails;
	}

	public Boolean getFailBuildWhendTFails() {
		return failBuildWhendTFails;
	}
	
	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}
	
	public Integer getRetryCount() {
		return retryCount;
	}
	
	public Integer getDelay() {
		return delay;
	}
	
	public void setDelay(Integer delay) {
		this.delay = delay;
	}

}
