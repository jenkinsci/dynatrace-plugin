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

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.model.TaskListener;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.dynatrace.jenkins.dashboard.util.Messages;

import java.awt.*;
import java.io.IOException;

/**
 * Root object of a dynaTrace Test Automation Build Report.
 */
public class TestAutomationResults implements ModelObject {

	/**
	 * The {@link TestAutomationBuildAction} that this report belongs to.
	 */
	private transient TestAutomationBuildAction buildAction;
	private static AbstractBuild<?, ?> currentBuild = null;
	private TestAutomationReport currentReport;

	/**
	 * Parses the reports and build a {@link TestAutomationResults}.
	 * 
	 * @throws java.io.IOException
	 *           If a report fails to parse.
	 */
	TestAutomationResults(
			final TestAutomationBuildAction buildAction,
			TaskListener listener) throws IOException {
		this.buildAction = buildAction;

		currentReport = this.buildAction.getTestAutomationReport();
		currentReport.setBuildAction(buildAction);
		addPreviousBuildReportToExistingReport();
	}

	public String getDisplayName() {
		return Messages.REPORT_DISPLAYNAME;
	}

	public AbstractBuild<?, ?> getBuild() {
		return buildAction.getBuild();
	}

	public TestAutomationReport getDynaTraceTestAutomationReport() {
		return currentReport;
	}

	private void addPreviousBuildReportToExistingReport() {
		// Avoid parsing all builds.
		if (TestAutomationResults.currentBuild == null) {
			TestAutomationResults.currentBuild = getBuild();
		} else {
			if (TestAutomationResults.currentBuild != getBuild()) {
				TestAutomationResults.currentBuild = null;
				return;
			}
		}

		AbstractBuild<?, ?> previousBuild = getBuild().getPreviousBuild();
		if (previousBuild == null) {
			return;
		}

		TestAutomationBuildAction previousPerformanceAction = previousBuild
				.getAction(TestAutomationBuildAction.class);
		if (previousPerformanceAction == null) {
			return;
		}

		TestAutomationBuildActionResultsDisplay previousBuildActionResults = previousPerformanceAction
				.getBuildActionResultsDisplay();
		if (previousBuildActionResults == null) {
			return;
		}

		TestAutomationReport lastReport = previousBuildActionResults.getCurrentReport();
		getDynaTraceTestAutomationReport().setLastBuildReport(lastReport);
	}

	/**
	 * Graph of metric points over time.
	 */
	public void doSummarizerGraph(final StaplerRequest request,
			final StaplerResponse response) throws IOException {
		final String metricKey = request.getParameter("metricDataKey");
		// TODO final MetricData metricData =
		// this.currentReport.getMetricByKey(metricKey);

		// final Graph graph = new GraphImpl(metricKey, metricData.getFrequency()) {
		//
		// protected DataSetBuilder<String, Integer> createDataSet() {
		// DataSetBuilder<String, Integer> dataSetBuilder = new
		// DataSetBuilder<String, Integer>();

		// TODO int i = 1;
		// for (MetricValues value : metricData.getMetricValues()) {
		// dataSetBuilder.add(value.getValue(), metricKey, i++);
		// }

		// return dataSetBuilder;
		// }
		// };

		// graph.doPng(request, response);
	}

	private abstract class GraphImpl extends Graph {
		private final String graphTitle;
		private final String xLabel;

		protected GraphImpl(final String metricKey, final String frequency) {
			super(-1, 400, 300); // cannot use timestamp, since ranges may change
			this.graphTitle = stripTitle(metricKey);
			this.xLabel = "Time in " + frequency;
		}

		private String stripTitle(final String metricKey) {
			return metricKey.substring(metricKey.lastIndexOf("|") + 1);
		}

		protected abstract DataSetBuilder<String, Integer> createDataSet();

		protected JFreeChart createGraph() {
			final CategoryDataset dataset = createDataSet().build();

			final JFreeChart chart = ChartFactory.createLineChart(graphTitle, // title
					xLabel, // category axis label
					null, // value axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					false, // include legend
					true, // tooltips
					false // urls
					);

			chart.setBackgroundPaint(Color.white);

			return chart;
		}
	}
}
