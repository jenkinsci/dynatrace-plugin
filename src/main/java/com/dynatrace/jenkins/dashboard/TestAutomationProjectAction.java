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

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.dynatrace.jenkins.dashboard.model.TestCase;
import com.dynatrace.jenkins.dashboard.model.TestCaseStatus;

public class TestAutomationProjectAction implements Action {

	private abstract class GraphImpl extends Graph {
    private final String graphTitle;

    protected GraphImpl(String title) {
      super(-1, 400, 300); // cannot use timestamp, since ranges may change
      this.graphTitle = title;
    }
    
    protected abstract DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet();

    protected JFreeChart createGraph() {
      CategoryDataset dataset = createDataSet().build();

      final JFreeChart chart = ChartFactory.createStackedAreaChart(
      		"", // title 
      		"Build",     // category axis label
      		"# Tests",        // value axis label 
      		dataset,     // data
      		PlotOrientation.VERTICAL, // orientation 
      		true,       // include legend
      		true,        // tooltips
      		false);      // URLs

      chart.setBackgroundPaint(Color.white);
      
      // change the colors
      // 0 = degraded, 1 = failed, 2 = improved, 3 = passed, 4 = volatile
      final StackedAreaRenderer renderer = new StackedAreaRenderer();
      renderer.setSeriesPaint(0, Color.PINK);
      renderer.setSeriesPaint(1, Color.RED);
      renderer.setSeriesPaint(2, Color.BLUE);
      renderer.setSeriesPaint(3, Color.GREEN);
      renderer.setSeriesPaint(4, Color.ORANGE);
      
      CategoryPlot plot = (CategoryPlot) chart.getPlot();
      plot.setRenderer(renderer);
      CategoryAxis categoryaxis = plot.getDomainAxis();
      categoryaxis.setLowerMargin(0.0D);
      categoryaxis.setUpperMargin(0.0D);
      categoryaxis.setCategoryMargin(0.0D);
      
      return chart;
    }
  }
	
  /**
   * Logger.
   */
  private static final Logger logger = Logger.getLogger(TestAutomationProjectAction.class.getName());
  private static final String PLUGIN_NAME = "dynatrace-testautomation-dashboard";
  private static final long serialVersionUID = 1L;
  
	private AbstractProject<?, ?> project;
	private List<TestCase> testCases;
  
	public TestAutomationProjectAction(AbstractProject<?, ?> project) {
		this.project = project;
	}

	public String getDisplayName() {
		return "dynaTrace Test Automation Dashboard";
	}

	public String getIconFileName() {
		return "graph.gif";
	}

	public String getUrlName() {
		return PLUGIN_NAME;
	}
	
	public List<TestCase> getTestCases() {
		return testCases;
	}

  public boolean isChartVisibleOnProjectDashboard() {
    return getExistingReportsList().size() >= 1;
  }
	
  /**
   * Method necessary to get the side-panel included in the Jelly file
   * @return this {@link AbstractProject}
   */
  public AbstractProject<?, ?> getProject() {
    return this.project;
  }
  

  /**
   * Graph of metric points over time.
   */
  public void doSummarizerGraph(final StaplerRequest request,
                                          final StaplerResponse response) throws IOException {
//    final Graph graph = new GraphImpl(mainMetricKey + " Overall Graph") {
//
//      protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet() {
//        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
//            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
//
//        for (ChartUtil.NumberOnlyBuildLabel label : averagesFromReports.keySet()) {
//          dataSetBuilder.add(averagesFromReports.get(label), mainMetricKey, label);
//        }
//
//        return dataSetBuilder;
//      }
//    };

    final Map<ChartUtil.NumberOnlyBuildLabel, Map<TestCaseStatus, Integer>> summaryMetrics= getSummaryMetricsFromReports(getExistingReportsList()); 
  	
    final Graph graph = new GraphImpl("dynaTrace Test Automation Trend") {

      protected DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> createDataSet() {
        DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
            new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
        for (ChartUtil.NumberOnlyBuildLabel label : summaryMetrics.keySet()) {
        	Map<TestCaseStatus, Integer> summary = summaryMetrics.get(label);
        	for (TestCaseStatus s : summary.keySet()) {
						dataSetBuilder.add(summary.get(s), s.toString(), label);
					}
				}
        
        return dataSetBuilder;
      }
    };

    graph.doPng(request, response);
  }
  
  public List<TestAutomationReport> getExistingReportsList() {
    final List<TestAutomationReport> taReportList = new ArrayList<TestAutomationReport>();

    if (null == this.project) {
      return taReportList;
    }

    final List<? extends AbstractBuild<?, ?>> builds = project.getBuilds();
    for (AbstractBuild<?, ?> currentBuild : builds) {
      final TestAutomationBuildAction performanceBuildAction = currentBuild.getAction(TestAutomationBuildAction.class);
      if (performanceBuildAction == null) {
        continue;
      }
      final TestAutomationReport report = performanceBuildAction.getBuildActionResultsDisplay().getCurrentReport();
      if (report == null) {
        continue;
      }

      taReportList.add(report);
    }

    return taReportList;
  }
  
  private Map<ChartUtil.NumberOnlyBuildLabel, Map<TestCaseStatus, Integer>> getSummaryMetricsFromReports(
      final List<TestAutomationReport> reports) {
  	Map<ChartUtil.NumberOnlyBuildLabel, Map<TestCaseStatus, Integer>> result = new TreeMap<ChartUtil.NumberOnlyBuildLabel, Map<TestCaseStatus, Integer>>();
    for (TestAutomationReport report : reports) {
    	if (report.getTestCaseSummary() != null) {
    		Map<TestCaseStatus, Integer> summary = report.getTestCaseSummary();
    		result.put(new ChartUtil.NumberOnlyBuildLabel(report.getBuild()), summary);
    	}
    }

    return result;
  }
  

}
