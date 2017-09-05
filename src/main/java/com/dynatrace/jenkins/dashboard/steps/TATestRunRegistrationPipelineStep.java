package com.dynatrace.jenkins.dashboard.steps;

import com.dynatrace.jenkins.dashboard.Messages;
import com.dynatrace.jenkins.dashboard.TATestRunRegistrationBuildStep;
import com.dynatrace.sdk.server.testautomation.models.TestCategory;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**Extracted to new step, because we need to return a value (testRunId) from step*/
public class TATestRunRegistrationPipelineStep extends AbstractStepImpl{

    private final String category;
    private String platform;

    @DataBoundConstructor
    public TATestRunRegistrationPipelineStep(String category) {
        this.category = category;
        this.platform = "";
    }

    public String getCategory() {
        return category;
    }

    public String getPlatform() {
        return platform;
    }

    @DataBoundSetter
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public static final class Execution extends AbstractSynchronousStepExecution<String>{
        private static final long serialVersionUID = 1L;

        @Inject
        public Execution(StepContext context) {
            super(context);
        }

        @Inject
        private transient TATestRunRegistrationPipelineStep step;

        @StepContextParameter
        private transient Launcher launcher;

        @StepContextParameter
        private transient TaskListener listener;

        @StepContextParameter
        private transient FilePath workspace;

        @StepContextParameter
        private transient Run<?, ?> build;

        @Override
        protected String run() throws Exception {
            TATestRunRegistrationBuildStep buildStep = new TATestRunRegistrationBuildStep(step.category, step.platform);
            buildStep.perform(build, workspace, launcher, listener);
            if (buildStep.getTestRunId() != null) {
                return buildStep.getTestRunId();
            }
            return "";
        }

    }

    @Extension
    public static final class DescriptorImpl extends AbstractStepDescriptorImpl {

        private static final String DEFAULT_CATEGORY = TestCategory.UNIT.getInternal();

        public DescriptorImpl() {
            super(Execution.class);
        }

        public static String getDefaultCategory() {
            return DEFAULT_CATEGORY;
        }

        @Override
        public String getFunctionName() {
            return "appMonRegisterTestRun";
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return Messages.BUILD_STEP_DISPLAY_NAME();
        }

        public ListBoxModel doFillCategoryItems() {
            ListBoxModel model = new ListBoxModel();
            for (TestCategory category : TestCategory.values()) {
                model.add(category.getInternal());
            }
            return model;
        }
    }
}