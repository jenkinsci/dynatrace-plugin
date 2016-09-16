package com.dynatrace.jenkins.dashboard.utils;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;

import javax.annotation.Nonnull;
import java.util.*;

public class DynatraceVariablesAction extends ParametersAction {

	private List<ParameterValue> parameters = new ArrayList<>();

	public DynatraceVariablesAction(Collection<? extends ParameterValue> parameters) {
		this.parameters.addAll(parameters);
	}

	@Override
	public List<ParameterValue> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	@Override
	public ParameterValue getParameter(String name) {
		for (ParameterValue p : parameters) {
			if (p == null) continue;
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	@Nonnull
	@Override
	public DynatraceVariablesAction createUpdated(Collection<? extends ParameterValue> overrides) {
		List<ParameterValue> newParams = new ArrayList<>(overrides);

		outer:
		for (ParameterValue value : this.parameters) {
			for (ParameterValue newParam : newParams) {
				if (newParam.getName().equals(value.getName())) {
					continue outer;
				}
			}
			newParams.add(value);
		}

		return new DynatraceVariablesAction(newParams);
	}

	@Extension
	public static final class DynatraceBuildVariablesContributor extends BuildVariableContributor {

		@Override
		public void buildVariablesFor(AbstractBuild r, Map<String, String> variables) {
			DynatraceVariablesAction a = r.getAction(DynatraceVariablesAction.class);
			if (a == null) {
				return;
			}
			for (ParameterValue spv : a.getParameters()) {
				variables.put(spv.getName(), String.valueOf(spv.getValue()));
			}
		}
	}

	@Extension
	public static final class DynatraceVariablesEnvironmentContributor extends EnvironmentContributor {
		@Override
		public void buildEnvironmentFor(Run r, EnvVars vars, TaskListener listener) {
			DynatraceVariablesAction a = r.getAction(DynatraceVariablesAction.class);
			if (a == null) {
				return;
			}
			for (ParameterValue spv : a.getParameters()) {
				vars.put(spv.getName(), String.valueOf(spv.getValue()));
			}
		}
	}
}
