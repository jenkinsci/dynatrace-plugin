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
package com.dynatrace.jenkins.dashboard.rest;

import com.dynatrace.jenkins.dashboard.TAGlobalConfiguration;
import com.dynatrace.jenkins.dashboard.model_2_0_0.TAReportDetails;
import com.dynatrace.jenkins.dashboard.utils.Utils;
import com.sun.jersey.api.client.ClientResponse;
import jenkins.model.GlobalConfiguration;
import org.w3c.dom.Document;

import java.io.PrintStream;
import java.net.URISyntaxException;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by krzysztof.necel on 2016-04-18.
 */
public abstract class TAReportRetriever {

	protected final String systemProfile;
	protected final PrintStream logger;
	private final boolean printXmlReportForDebug;

	private final int delay;
	protected final int retryCount;
	protected final ServerRestConnection connection;

	protected int currentTry = 0;

	protected TAReportRetriever(String systemProfile, PrintStream logger, boolean printXmlReportForDebug) {
		final TAGlobalConfiguration globalConfig = GlobalConfiguration.all().get(TAGlobalConfiguration.class);

		this.systemProfile = systemProfile;
		this.logger = logger;
		this.printXmlReportForDebug = printXmlReportForDebug;

		this.delay = globalConfig.delay;
		this.retryCount = globalConfig.retryCount;
		this.connection = new ServerRestConnection(globalConfig.protocol, globalConfig.host,
				globalConfig.port, globalConfig.username, globalConfig.password);
	}

	public abstract TAReportDetails fetchReport() throws InterruptedException, URISyntaxException;

	protected void waitForDelay() throws InterruptedException {
		if (delay > 0) {
			String message = "Sleeping for the configured delay of " + delay + " seconds before retrieving test run data from Dynatrace Server...";
			if (currentTry > 0) {
				message += " re-try " + currentTry + " out of " + retryCount;
			}
			logger.println(message);
			Thread.sleep(delay * 1000);
		}
	}

	protected Document extractXmlDocument(ClientResponse response) {
		checkResponseStatus(response);
		final String xmlContent = response.getEntity(String.class);
		if (printXmlReportForDebug) {
			logger.println("DEBUG: XML response from Dynatrace Server REST interface:\n" + xmlContent);
		}

		return Utils.stringToXmlDocument(xmlContent);
	}

	protected void checkResponseStatus(ClientResponse response) {
		int status = response.getStatus();
		logger.println("HTTP status code from Dynatrace Server: " + status);
		if (status != HTTP_OK) {
			throw new IllegalStateException("HTTP status code from Dynatrace Server was " + status + ". Expected " + HTTP_OK +".");
		}
	}
}
