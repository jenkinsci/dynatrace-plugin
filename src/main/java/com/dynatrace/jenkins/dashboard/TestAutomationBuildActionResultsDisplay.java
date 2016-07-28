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

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.model.TaskListener;

import java.io.IOException;

@Deprecated
public class TestAutomationBuildActionResultsDisplay implements ModelObject {

  private static final String DISPLAY_NAME = "Test Result (legacy)";

  /**
   * The {@link TestAutomationBuildAction} that this report belongs to.
   */
  private transient TestAutomationBuildAction buildAction;
  private static AbstractBuild<?, ?> currentBuild = null;
  private TestAutomationReport currentReport;

  /**
   * Parses the reports and build a {@link TestAutomationBuildActionResultsDisplay}.
   *
   * @throws java.io.IOException If a report fails to parse.
   */
  public TestAutomationBuildActionResultsDisplay(final TestAutomationBuildAction buildAction, TaskListener listener)
      throws IOException {
    this.buildAction = buildAction;

    currentReport = this.buildAction.getTestAutomationReport();
    currentReport.setBuildAction(buildAction);
    addPreviousBuildReportToExistingReport();
  }

	public String getDisplayName() {
		return DISPLAY_NAME;
	}

  public AbstractBuild<?, ?> getBuild() {
    return buildAction.getBuild();
  }
  
  public TestAutomationReport getCurrentReport() {
		return currentReport;
	}


  private void addPreviousBuildReportToExistingReport() {
    if (currentBuild == null) {
      currentBuild = getBuild();
    } else {
      if (currentBuild != getBuild()) {
        currentBuild = null;
        return;
      }
    }

    AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
    if (previousBuild == null) {
      return;
    }
    
    TestAutomationBuildAction prevBuildAction = previousBuild.getAction(TestAutomationBuildAction.class);
    if (prevBuildAction == null) {
    	return;
    }

    TestAutomationBuildActionResultsDisplay previousBuildActionResults = prevBuildAction.getBuildActionResultsDisplay();
    if (previousBuildActionResults == null) {
      return;
    }

    TestAutomationReport lastReport = previousBuildActionResults.getCurrentReport();
    getCurrentReport().setLastBuildReport(lastReport);
  }
}
