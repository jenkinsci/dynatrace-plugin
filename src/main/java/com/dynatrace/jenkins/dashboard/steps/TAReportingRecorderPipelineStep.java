package com.dynatrace.jenkins.dashboard.steps;

import com.dynatrace.jenkins.dashboard.Messages;
import com.dynatrace.jenkins.dashboard.TAReportingRecorder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * @author piotr.lugowski
 */
/*Extracted to new step, because we need custom step description*/
public class TAReportingRecorderPipelineStep extends AbstractStepImpl {

    public String statusNameIfDegraded;
    public String statusNameIfVolatile;
    public Boolean printXmlReportForDebug;

    @DataBoundConstructor
    public TAReportingRecorderPipelineStep() {
        this.statusNameIfDegraded = null;
        this.statusNameIfVolatile = null;
        this.printXmlReportForDebug = false;
    }

    @DataBoundSetter
    public void setStatusNameIfDegraded(String statusNameIfDegraded) {
        this.statusNameIfDegraded = statusNameIfDegraded;
    }

    @DataBoundSetter
    public void setStatusNameIfVolatile(String statusNameIfVolatile) {
        this.statusNameIfVolatile = statusNameIfVolatile;
    }

    @DataBoundSetter
    public void setPrintXmlReportForDebug(Boolean printXmlReportForDebug) {
        this.printXmlReportForDebug = printXmlReportForDebug;
    }

    public static final class Execution extends AbstractSynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;
        @Inject
        private transient TAReportingRecorderPipelineStep step;
        @StepContextParameter
        private transient Launcher launcher;
        @StepContextParameter
        private transient TaskListener listener;
        @StepContextParameter
        private transient FilePath workspace;
        @StepContextParameter
        private transient Run<?, ?> build;

        @Inject
        public Execution(StepContext context) {
            super(context);
        }

        @Override
        protected Void run() throws Exception {
            Boolean modifyStatusIfDegraded = StringUtils.isNotBlank(step.statusNameIfDegraded);
            Boolean modifyStatusIfVolatile = StringUtils.isNotBlank(step.statusNameIfVolatile);
            TAReportingRecorder taReportingRecorder = new TAReportingRecorder(modifyStatusIfDegraded, modifyStatusIfVolatile, step.statusNameIfVolatile,
                    step.statusNameIfVolatile, step.printXmlReportForDebug);
            taReportingRecorder.perform(build, workspace, launcher, listener);
            return null;
        }
    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        private static final boolean DEFAULT_PRINT_XML_REPORT_FOR_DEBUG = false;

        private static final String MODIFY_BUILD_STATUS_UNSTABLE = "UNSTABLE";

        private static final String MODIFY_BUILD_STATUS_FAILURE = "FAILURE";

        private static final String FUNCTION_NAME = "appMonPublishTestResults";

        public DescriptorImpl() {
            super(Execution.class);
        }

        public static boolean getDefaultPrintXmlReportForDebug() {
            return DEFAULT_PRINT_XML_REPORT_FOR_DEBUG;
        }

        public static String getModifyBuildStatusUnstable() {
            return MODIFY_BUILD_STATUS_UNSTABLE;
        }

        public static String getModifyBuildStatusFailure() {
            return MODIFY_BUILD_STATUS_FAILURE;
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.RECORDER_DISPLAY_NAME();
        }

        @Override
        public String getFunctionName() {
            return FUNCTION_NAME;
        }
    }
}
