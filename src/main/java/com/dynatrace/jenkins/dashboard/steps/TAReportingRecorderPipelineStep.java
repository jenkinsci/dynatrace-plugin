package com.dynatrace.jenkins.dashboard.steps;

import com.dynatrace.jenkins.dashboard.Messages;
import com.dynatrace.jenkins.dashboard.TAReportingRecorder;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**Extracted to new step, because we need custom step description*/
public class TAReportingRecorderPipelineStep extends AbstractStepImpl {

    public Boolean modifyStatusIfDegraded;
    public Boolean modifyStatusIfVolatile;
    public String statusNameIfDegraded;
    public String statusNameIfVolatile;
    public Boolean printXmlReportForDebug;

    @DataBoundConstructor
    public TAReportingRecorderPipelineStep() {
        this.modifyStatusIfDegraded = false;
        this.modifyStatusIfVolatile = false;
        this.statusNameIfDegraded = "";
        this.statusNameIfVolatile = "";
        this.printXmlReportForDebug = false;
    }

    @DataBoundSetter
    public void setModifyStatusIfDegraded(Boolean modifyStatusIfDegraded) {
        this.modifyStatusIfDegraded = modifyStatusIfDegraded;
    }

    @DataBoundSetter
    public void setModifyStatusIfVolatile(Boolean modifyStatusIfVolatile) {
        this.modifyStatusIfVolatile = modifyStatusIfVolatile;
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
            TAReportingRecorder taReportingRecorder = new TAReportingRecorder(step.modifyStatusIfDegraded, step.modifyStatusIfVolatile,
                    step.statusNameIfVolatile, step.statusNameIfVolatile, step.printXmlReportForDebug);
            taReportingRecorder.perform(build, workspace, launcher, listener);
            return null;
        }
    }

    @Extension
    @Symbol("appMonPublishTestResults")/*is a function name in pipeline script*/
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl{

        private static final boolean DEFAULT_PRINT_XML_REPORT_FOR_DEBUG = false;

        private static final String MODIFY_BUILD_STATUS_UNSTABLE = "UNSTABLE";
        private static final String MODIFY_BUILD_STATUS_FAILURE = "FAILURE";

        public static boolean getDefaultPrintXmlReportForDebug() {
            return DEFAULT_PRINT_XML_REPORT_FOR_DEBUG;
        }

        public static String getModifyBuildStatusUnstable() {
            return MODIFY_BUILD_STATUS_UNSTABLE;
        }

        public static String getModifyBuildStatusFailure() {
            return MODIFY_BUILD_STATUS_FAILURE;
        }

        public DescriptorImpl() {
            super(Execution.class);
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.RECORDER_DISPLAY_NAME();
        }

        @Override
        public String getFunctionName() {
            return "appMonPublishTestResults";
        }
    }
}
