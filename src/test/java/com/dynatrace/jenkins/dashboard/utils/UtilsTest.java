package com.dynatrace.jenkins.dashboard.utils;

import com.dynatrace.jenkins.dashboard.TestUtils;
import com.dynatrace.jenkins.dashboard.model_2_0_0.*;
import com.google.common.collect.Lists;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by krzysztof.necel on 2016-06-15.
 */
public class UtilsTest {

	@Test
	public void stringToXmlDocumentTest() throws Exception {
		XPath xpath = XPathFactory.newInstance().newXPath();
		Document document = Utils.stringToXmlDocument(TestUtils.getSampleXmlString());

		assertThat("Document must not be null", document, is(notNullValue()));

		Node testRunsNode = document.getFirstChild();
		assertThat("Incorrect name of the XML element", testRunsNode.getNodeName(), is("testRuns"));

		NodeList testRunsList = (NodeList) xpath.compile("/*/testRun").evaluate(document, XPathConstants.NODESET);
		assertThat("Incorrect number of testRun nodes", testRunsList.getLength(), is(2));

		Node firstTestRun = testRunsList.item(0);
		assertThat("Incorrect name of the XML element", firstTestRun.getNodeName(), is("testRun"));
	}

	@Test
	public void formatDoubleTest() throws ParseException {
		NumberFormat numberFormat = NumberFormat.getInstance();

		String s = Utils.formatDouble(2.5234609);
		assertThat("Incorrect name of the XML element", numberFormat.parse(s).doubleValue(), is(closeTo(2.5234609, 0.01)));
		s = Utils.formatDouble(0.00001);
		assertThat("Incorrect name of the XML element", numberFormat.parse(s).doubleValue(), is(closeTo(0.00001, 0.01)));
		s = Utils.formatDouble(-7.2);
		assertThat("Incorrect name of the XML element", numberFormat.parse(s).doubleValue(), is(closeTo(-7.2, 0.01)));
	}

	@Test
	public void formatDoublePercentageTest() throws ParseException {
		NumberFormat numberFormat = NumberFormat.getInstance();

		String s = Utils.formatDoublePercentage(2.5234609);
		assertThat("Incorrect name of the XML element", numberFormat.parse(s).doubleValue(), is(closeTo(252.34609, 0.01)));
		s = Utils.formatDoublePercentage(0.00001);
		assertThat("Incorrect name of the XML element", numberFormat.parse(s).doubleValue(), is(closeTo(0.001, 0.01)));
		s = Utils.formatDoublePercentage(-7.2);
		assertThat("Incorrect name of the XML element", numberFormat.parse(s).doubleValue(), is(closeTo(-720.0, 0.01)));
	}

	@Test
	public void updateBuildVariables_NoVariableBefore_Test() {
		AbstractBuild build = mock(AbstractBuild.class);
		when(build.getAction(DynatraceVariablesAction.class)).thenReturn(null);

		List<ParameterValue> parameters = new ArrayList<ParameterValue>();
		parameters.add(new StringParameterValue("key1S", "value1"));
		parameters.add(new StringParameterValue("key2S", "value2"));
		parameters.add(new StringParameterValue("key3S", "value3"));

		Utils.updateBuildVariables(build, parameters);

		ArgumentCaptor<DynatraceVariablesAction> argumentCaptor = ArgumentCaptor.forClass(DynatraceVariablesAction.class);
		verify(build).addAction(argumentCaptor.capture());
		DynatraceVariablesAction updatedAction = argumentCaptor.getValue();
		assertUpdateBuildVariables(updatedAction);
	}

	@Test
	public void updateBuildVariables_OneVariableBefore_Test() {
		List<ParameterValue> oldParameters = new ArrayList<ParameterValue>();
		oldParameters.add(new StringParameterValue("key1S", "value1"));

		AbstractBuild build = mock(AbstractBuild.class);
		when(build.getAction(DynatraceVariablesAction.class)).thenReturn(new DynatraceVariablesAction(oldParameters));

		List<ParameterValue> newParameters = new ArrayList<ParameterValue>();
		newParameters.add(new StringParameterValue("key2S", "value2"));
		newParameters.add(new StringParameterValue("key3S", "value3"));

		Utils.updateBuildVariables(build, newParameters);

		ArgumentCaptor<DynatraceVariablesAction> argumentCaptor = ArgumentCaptor.forClass(DynatraceVariablesAction.class);
		verify(build).replaceAction(argumentCaptor.capture());
		DynatraceVariablesAction updatedAction = argumentCaptor.getValue();
		assertUpdateBuildVariables(updatedAction);
	}

	@Test
	public void updateBuildVariables_TwoVariablesBefore_Test() {
		List<ParameterValue> oldParameters = new ArrayList<ParameterValue>();
		oldParameters.add(new StringParameterValue("key1S", "value1"));
		oldParameters.add(new StringParameterValue("key2S", "value2"));

		AbstractBuild build = mock(AbstractBuild.class);
		when(build.getAction(DynatraceVariablesAction.class)).thenReturn(new DynatraceVariablesAction(oldParameters));

		List<ParameterValue> newParameters = new ArrayList<ParameterValue>();
		newParameters.add(new StringParameterValue("key3S", "value3"));

		Utils.updateBuildVariables(build, newParameters);

		ArgumentCaptor<DynatraceVariablesAction> argumentCaptor = ArgumentCaptor.forClass(DynatraceVariablesAction.class);
		verify(build).replaceAction(argumentCaptor.capture());
		DynatraceVariablesAction updatedAction = argumentCaptor.getValue();
		assertUpdateBuildVariables(updatedAction);
	}

	@Test
	public void updateBuildVariables_ConflictingVariablesBefore_Test() {
		List<ParameterValue> oldParameters = new ArrayList<ParameterValue>();
		oldParameters.add(new StringParameterValue("key1S", "value1"));
		oldParameters.add(new StringParameterValue("key3S", "wrongValue3"));

		AbstractBuild build = mock(AbstractBuild.class);
		when(build.getAction(DynatraceVariablesAction.class)).thenReturn(new DynatraceVariablesAction(oldParameters));

		List<ParameterValue> newParameters = new ArrayList<ParameterValue>();
		newParameters.add(new StringParameterValue("key2S", "value2"));
		newParameters.add(new StringParameterValue("key3S", "value3"));

		Utils.updateBuildVariables(build, newParameters);

		ArgumentCaptor<DynatraceVariablesAction> argumentCaptor = ArgumentCaptor.forClass(DynatraceVariablesAction.class);
		verify(build).replaceAction(argumentCaptor.capture());
		DynatraceVariablesAction updatedAction = argumentCaptor.getValue();
		assertUpdateBuildVariables(updatedAction);
	}

	@Test
	public void updateBuildVariableTest() {
		List<ParameterValue> oldParameters = new ArrayList<ParameterValue>();
		oldParameters.add(new StringParameterValue("key1S", "value1"));
		oldParameters.add(new StringParameterValue("key3S", "value3"));

		AbstractBuild build = mock(AbstractBuild.class);
		when(build.getAction(DynatraceVariablesAction.class)).thenReturn(new DynatraceVariablesAction(oldParameters));

		Utils.updateBuildVariable(build, "key2S", "value2");

		ArgumentCaptor<DynatraceVariablesAction> argumentCaptor = ArgumentCaptor.forClass(DynatraceVariablesAction.class);
		verify(build).replaceAction(argumentCaptor.capture());
		DynatraceVariablesAction updatedAction = argumentCaptor.getValue();
		assertUpdateBuildVariables(updatedAction);
	}

	private void assertUpdateBuildVariables(DynatraceVariablesAction updatedAction) {
		assertThat("DynatraceVariablesAction cannot be null (was not modified?)", updatedAction, is(notNullValue()));

		List<ParameterValue> resultParameters = updatedAction.getParameters();
		assertThat("Incorrect number of parameters", resultParameters, hasSize(3));

		StringParameterValue key1S = (StringParameterValue) updatedAction.getParameter("key1S");
		StringParameterValue key2S = (StringParameterValue) updatedAction.getParameter("key2S");
		StringParameterValue key3S = (StringParameterValue) updatedAction.getParameter("key3S");

		assertThat("Parameter is missing", key1S, is(notNullValue()));
		assertThat("Parameter is missing", key2S, is(notNullValue()));
		assertThat("Parameter is missing", key3S, is(notNullValue()));

		assertThat("Parameter has incorrect value", key1S.value, is(equalTo("value1")));
		assertThat("Parameter has incorrect value", key2S.value, is(equalTo("value2")));
		assertThat("Parameter has incorrect value", key3S.value, is(equalTo("value3")));
	}

	@Test
	public void createReportAggregatedSummaryTest() {
		ArrayList<TestRun> testRuns = new ArrayList<TestRun>();
		testRuns.add(new TestRun(Lists.<TestResult>newArrayList(), createSummary(3, 3, 3, 3, 1, 3), "", TestCategory.UNIT));
		testRuns.add(new TestRun(Lists.<TestResult>newArrayList(), createSummary(2, 2, 2, 2, 1, 2), "", TestCategory.UNIT));
		testRuns.add(new TestRun(Lists.<TestResult>newArrayList(), createSummary(1, 1, 1, 1, 1, 1), "", TestCategory.PERFORMANCE));
		testRuns.add(new TestRun(Lists.<TestResult>newArrayList(), createSummary(1, 0, 0, 0, 1, 0), "", TestCategory.PERFORMANCE));
		TAReportDetails reportDetails = new TAReportDetails(testRuns);
		Map<TestStatus, Integer> result = Utils.createReportAggregatedSummary(reportDetails);
		TestUtils.assertSummary(result, 7, 6, 6, 6, 4, 6);
	}

	private EnumMap<TestStatus, Integer> createSummary(int nFailed, int nDegraded, int nVolatile,
													   int nImproved, int nPassed, int nInvalidated) {
		EnumMap<TestStatus, Integer> summary = new EnumMap<TestStatus, Integer>(TestStatus.class);

		summary.put(TestStatus.FAILED, nFailed);
		summary.put(TestStatus.DEGRADED, nDegraded);
		summary.put(TestStatus.VOLATILE, nVolatile);
		summary.put(TestStatus.IMPROVED, nImproved);
		summary.put(TestStatus.PASSED, nPassed);
		summary.put(TestStatus.INVALIDATED, nInvalidated);
		return summary;
	}
}