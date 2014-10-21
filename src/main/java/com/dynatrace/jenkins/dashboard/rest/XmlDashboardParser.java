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
package com.dynatrace.jenkins.dashboard.rest;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.dynatrace.jenkins.dashboard.model.TestCase;
import com.dynatrace.jenkins.dashboard.model.TestMetric;

public class XmlDashboardParser {
	
	public List<TestCase> parse(Document document, String testruninfoId) {
		List<TestCase> testCases = new ArrayList<TestCase>();
		try {
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();
			XPathExpression expr = xpath.compile("//testcase");

			// get information about test cases
			Object result = expr.evaluate(document, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				String testCaseName = n.getAttributes().getNamedItem("testname").getNodeValue();
				TestCase tc = new TestCase(testCaseName);
				tc.setPlatform(n.getAttributes().getNamedItem("platform").getNodeValue());
				tc.setStatus(n.getAttributes().getNamedItem("status").getNodeValue());
				Node prevStatusNode = n.getAttributes().getNamedItem("prevstatus"); 
				if (prevStatusNode != null)
					tc.setPrevStatus(prevStatusNode.getNodeValue());
				else 
					tc.setPrevStatus("NONE");
				// get the testmetrics
				NodeList innerNodes = n.getFirstChild().getNextSibling().getChildNodes();
				for (int j = 0; j < innerNodes.getLength(); j++) {
					Node innerNode = innerNodes.item(j);
					if ("testmetric".equals(innerNode.getNodeName())) {
						NamedNodeMap testMetricAttr = innerNode.getAttributes();
						String measure = testMetricAttr.getNamedItem("measure").getNodeValue();
						String metricgroup = testMetricAttr.getNamedItem("metricgroup").getNodeValue();
						String last = testMetricAttr.getNamedItem("last").getNodeValue();
						Node unitNode = testMetricAttr.getNamedItem("unit");
						String unit = "";
						// WG: unit might be null for browser performance reports
						if (unitNode == null) {
							unit = "num";
						} else {
							unit = unitNode.getNodeValue();
						}
						String timestamp = "";
						String value = "";
						String high ="";
						boolean failed = false;
						Node highNode = testMetricAttr.getNamedItem("high");
						if (highNode != null)
							high = highNode.getNodeValue();
						NodeList testruns = innerNode.getChildNodes();
						for (int k = 0; k < testruns.getLength(); k++){
							if ("testrun".equals(testruns.item(k).getNodeName())) {
								NamedNodeMap testrunAttr = testruns.item(k).getAttributes();
								Node testruninfoNamedItem = testrunAttr.getNamedItem("testruninfo");
								if (testruninfoNamedItem != null && testruninfoNamedItem.getNodeValue().equals(testruninfoId)) {
									timestamp = testrunAttr.getNamedItem("timestamp").getNodeValue();
									value = testrunAttr.getNamedItem("value").getNodeValue();
									failed = Boolean.parseBoolean(testrunAttr.getNamedItem("failed").getNodeValue());
									
									TestMetric tm = new TestMetric();
									tm.setMeasure(measure);
									tm.setMetricgroup(metricgroup);
									tm.setValue(value);
									tm.setUnit(unit);
									tm.setTimestamp(timestamp);
									tm.setLast(last);
									tm.setHigh(high);
									tm.setFailed(failed);
									tc.addTestMetric(tm);
								}
							}
						}
					}
				}
				// only store TestCase instances with associated test metrics
				if (tc.getTestMetrics() != null && tc.getTestMetrics().size() > 0) {
					testCases.add(tc);
				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (DOMException e) {
			throw new RuntimeException(e);
		}
		return testCases;
	}

	public String getTestruninfoIdForBuild(PrintStream logger, Document document, String buildId) {
		String id = null;
		try {
			XPathFactory xFactory = XPathFactory.newInstance();
			XPath xpath = xFactory.newXPath();
			XPathExpression expr = xpath.compile(String.format(
					"//testruninfo[@versionbuild='%s']", buildId));
			
			Node result = (Node) expr.evaluate(document, XPathConstants.NODE);
			if (result == null) {
				logger.println("No testrun found for build id " + buildId);
				return null;
			} 
			id = result.getAttributes().getNamedItem("id").getNodeValue();
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (DOMException e) {
			throw new RuntimeException(e);
		}
		return id;
	}

}
