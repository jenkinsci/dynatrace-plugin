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

/**
 * Created by krzysztof.necel on 2016-02-11.
 */
public final class BuildVarKeys {

	private BuildVarKeys() {
	}

	public static final String BUILD_VAR_KEY_SYSTEM_PROFILE = "dtProfile";
	public static final String BUILD_VAR_KEY_VERSION_MAJOR = "dtVersionMajor";
	public static final String BUILD_VAR_KEY_VERSION_MINOR = "dtVersionMinor";
	public static final String BUILD_VAR_KEY_VERSION_REVISION = "dtVersionRevision";
	public static final String BUILD_VAR_KEY_VERSION_BUILD = "dtVersionBuild";
	public static final String BUILD_VAR_KEY_VERSION_MILESTONE = "dtVersionMilestone";
	public static final String BUILD_VAR_KEY_MARKER = "dtMarker";
	public static final String BUILD_VAR_KEY_TEST_RUN_ID = "dtTestrunID";
	public static final String BUILD_VAR_KEY_STORED_SESSION_NAME = "dtStoredSessionName";
	public static final String BUILD_VAR_KEY_GLOBAL_SERVER_URL = "dtServerUrl";
	public static final String BUILD_VAR_KEY_GLOBAL_USERNAME = "dtUsername";
	public static final String BUILD_VAR_KEY_GLOBAL_PASSWORD = "dtPassword";
}
