package com.dynatrace.jenkins.dashboard;

import com.dynatrace.jenkins.dashboard.model_2_0_0.TestStatus;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matcher;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by krzysztof.necel on 2016-06-15.
 */
public class TestUtils {

	public static String getSampleXmlString() throws IOException {
		return IOUtils.toString(TestUtils.class.getResourceAsStream("sampleXML.xml"), "UTF-8");
	}

	public static void assertSummary(Map<TestStatus, Integer> actual, int nFailed, int nDegraded, int nVolatile,
							   int nImproved, int nPassed, int nInvalidated) {
		Matcher<Integer> matcher;

		matcher = nFailed != 0 ? is(nFailed) : anyOf(is(nFailed), is(nullValue()));
		assertThat("Incorrect number of FAILED test results", actual.get(TestStatus.FAILED), matcher);

		matcher = nDegraded != 0 ? is(nDegraded) : anyOf(is(nDegraded), is(nullValue()));
		assertThat("Incorrect number of DEGRADED test results", actual.get(TestStatus.DEGRADED), matcher);

		matcher = nVolatile != 0 ? is(nVolatile) : anyOf(is(nVolatile), is(nullValue()));
		assertThat("Incorrect number of VOLATILE test results", actual.get(TestStatus.VOLATILE), matcher);

		matcher = nImproved != 0 ? is(nImproved) : anyOf(is(nImproved), is(nullValue()));
		assertThat("Incorrect number of IMPROVED test results", actual.get(TestStatus.IMPROVED), matcher);

		matcher = nPassed != 0 ? is(nPassed) : anyOf(is(nPassed), is(nullValue()));
		assertThat("Incorrect number of PASSED test results", actual.get(TestStatus.PASSED), matcher);

		matcher = nInvalidated != 0 ? is(nInvalidated) : anyOf(is(nInvalidated), is(nullValue()));
		assertThat("Incorrect number of INVALIDATED test results", actual.get(TestStatus.INVALIDATED), matcher);
	}
}
