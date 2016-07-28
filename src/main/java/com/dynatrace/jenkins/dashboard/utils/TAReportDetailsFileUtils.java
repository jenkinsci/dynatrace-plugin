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
package com.dynatrace.jenkins.dashboard.utils;

import com.dynatrace.jenkins.dashboard.model_2_0_0.TAReportDetails;
import hudson.model.AbstractBuild;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by krzysztof.necel on 2016-05-05.
 */
public class TAReportDetailsFileUtils {

	private static final String TA_REPORT_DETAILS_FILENAME = "dtTestResult.xml";

	public static void persistReportDetails(AbstractBuild<?,?> build, TAReportDetails reportDetails) throws JAXBException {
		final File file = new File(build.getRootDir(), TA_REPORT_DETAILS_FILENAME);
		final JAXBContext jaxbContext = JAXBContext.newInstance(TAReportDetails.class);
		final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.marshal(reportDetails, file);
	}

	public static TAReportDetails loadReportDetails(AbstractBuild<?,?> build) throws JAXBException {
		final File file = new File(build.getRootDir(), TA_REPORT_DETAILS_FILENAME);
		final JAXBContext jaxbContext = JAXBContext.newInstance(TAReportDetails.class);
		final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		return (TAReportDetails) jaxbUnmarshaller.unmarshal(file);
	}
}
