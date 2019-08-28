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
import com.dynatrace.jenkins.dashboard.model_2_0_0.TestStatus;
import com.dynatrace.jenkins.dashboard.utils.Utils;
import com.dynatrace.jenkins.dashboard.utils.UtilsCompat;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.Job;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by krzysztof.necel on 2016-02-05.
 * @author piotr.lugowski
 */
public class TAReportingProjectAction implements Action {

	private static final String URL_NAME = "dynatrace-test-result-trend";

	private static final int MAX_BUILD_NUMBER_TO_SHOW_ON_CHART = 25;
	private static final int MAX_BUILD_NUMBER_TO_SHOW_IN_TABLE = 250;

	private static final Color COLOR_FAILED = new Color(0xdc172a);
	private static final Color COLOR_PASSED = new Color(0x7dc540);
	private static final Color COLOR_DEGRADED = new Color(0xef651f);
	private static final Color COLOR_IMPROVED = new Color(0x00a6fb);
	private static final Color COLOR_VOLATILE = new Color(0xffe11c);

	private abstract static class GraphImpl extends Graph {

		GraphImpl() {
			super(-1, 500, 300); // cannot use timestamp, since ranges may change
		}

		protected abstract CategoryDataset createDataSet();

		@Override
		protected JFreeChart createGraph() {
			CategoryDataset dataset = createDataSet();

			final JFreeChart chart = ChartFactory.createStackedAreaChart(
					Messages.PROJECT_ACTION_GRAPH_TITLE(),
					Messages.PROJECT_ACTION_GRAPH_COLUMN_AXIS_LABEL(),
					Messages.PROJECT_ACTION_GRAPH_VALUE_AXIS_LABEL(),
					dataset,    // data
					PlotOrientation.VERTICAL, // orientation
					true,       // include legend
					true,       // tooltips
					false);     // URLs

			chart.setBackgroundPaint(Color.white);

			final StackedAreaRenderer renderer = new StackedAreaRenderer();
			renderer.setSeriesPaint(0, COLOR_FAILED);
			renderer.setSeriesPaint(1, COLOR_DEGRADED);
			renderer.setSeriesPaint(2, COLOR_VOLATILE);
			renderer.setSeriesPaint(3, COLOR_IMPROVED);
			renderer.setSeriesPaint(4, COLOR_PASSED);

			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			plot.setRenderer(renderer);
			CategoryAxis categoryaxis = plot.getDomainAxis();
			categoryaxis.setLowerMargin(0.0D);
			categoryaxis.setUpperMargin(0.0D);
			categoryaxis.setCategoryMargin(0.0D);
			categoryaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

			// force the chart axis to have only integer values
			plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

			return chart;
		}
	}

	private final Job<?, ?> project;

	@SuppressWarnings("WeakerAccess")
	public TAReportingProjectAction(Job<?,?> project) {
		this.project = project;
	}

	@Override
	public String getDisplayName() {
		return Messages.PROJECT_ACTION_DISPLAY_NAME();
	}

	@Override
	public String getIconFileName() {
		return Utils.DYNATRACE_ICON_24_X_24_FILEPATH;
	}

	@Override
	public String getUrlName() {
		return URL_NAME;
	}

	public Job<?, ?> getProject() {
		return this.project;
	}

	/**
	 * Generates graph with the number of tests executed in each category for {@link #MAX_BUILD_NUMBER_TO_SHOW_ON_CHART} latest builds.
	 *
	 * @param request request sent to generate the graph
	 * @param response response to modify (if needed)
	 * @throws IOException when PNG file creation was not possible
	 */
	public void doSummarizerGraph(final StaplerRequest request, final StaplerResponse response) throws IOException {
		final Map<ChartUtil.NumberOnlyBuildLabel, Map<TestStatus, Integer>> summaries = new TreeMap<>();

		List<TAReport> reports = getExistingReportsList(MAX_BUILD_NUMBER_TO_SHOW_ON_CHART);
		for (TAReport report : reports) {
			Map<TestStatus, Integer> summary = report.getSummary();
			summaries.put(new ChartUtil.NumberOnlyBuildLabel(report.getBuild()), summary);
		}

		final Graph graph = new GraphImpl() {

			@Override
			protected CategoryDataset createDataSet() {
				DataSetBuilder<TestStatus, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
						new DataSetBuilder<>();

				for (Map.Entry<ChartUtil.NumberOnlyBuildLabel, Map<TestStatus, Integer>> entry : summaries.entrySet()) {
					ChartUtil.NumberOnlyBuildLabel label = entry.getKey();
					Map<TestStatus, Integer> summary = entry.getValue();
					for (Map.Entry<TestStatus, Integer> s : summary.entrySet()) {
						dataSetBuilder.add(s.getValue(), s.getKey(), label);
					}
				}

				return dataSetBuilder.build();
			}
		};

		graph.doPng(request, response);
	}

	/**
	 * @return limited number of the latest test reports (limited by {@link #MAX_BUILD_NUMBER_TO_SHOW_IN_TABLE})
	 */
	public List<TAReport> getExistingReportsList() {
		return getExistingReportsList(MAX_BUILD_NUMBER_TO_SHOW_IN_TABLE);
	}

	private List<TAReport> getExistingReportsList(int limit) {
		final List<TAReport> taReportList = new ArrayList<>();

		if (this.project == null) {
			return taReportList;
		}

		for (Run currentBuild : project.getBuilds()) {
			final TAReportingBuildAction_2_0_0 buildAction = currentBuild.getAction(TAReportingBuildAction_2_0_0.class);
			final TAReport report;

			if (buildAction != null) {
				report = buildAction.getCurrentReport();
			} else {
				// backward compatibility
				report = UtilsCompat.getCompatReport(currentBuild);
			}

			if (report != null) {
				taReportList.add(report);
				if (--limit == 0) {
					break;
				}
			}
		}

		return taReportList;
	}
}
