package angerona.fw.dummy;

import java.util.LinkedList;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import angerona.fw.OperatorPlugin;
import angerona.fw.operators.BaseIntentionUpdateOperator;
import angerona.fw.operators.BaseGenerateOptionsOperator;
import angerona.fw.operators.BasePolicyControlOperator;
import angerona.fw.operators.BaseChangeOperator;
import angerona.fw.operators.BaseViolatesOperator;
import angerona.fw.operators.BaseSubgoalGenerationOperator;
import angerona.fw.operators.dummy.DummyIntentionUpdateOperator;
import angerona.fw.operators.dummy.DummyGenerateOptionsOperator;
import angerona.fw.operators.dummy.DummySubgoalGenerationOperator;
import angerona.fw.operators.dummy.DummyPolicyControlOperator;
import angerona.fw.operators.dummy.DummyChangeOperator;
import angerona.fw.operators.dummy.DummyViolatesOperator;

/**
 * The dummy operator plugin is an example/test plugin for the Angerona framework.
 * The classes provided by this plugin only implement some dummy functionality to test
 * the program flow of the Angerona framework.
 * @author Tim Janus
 */
@PluginImplementation
public class DummyOperatorPlugin implements OperatorPlugin {

	@Override
	public List<Class<? extends BaseChangeOperator>> getSupportedChangeOperators() {
		List<Class<? extends BaseChangeOperator>> reval = new LinkedList<Class<? extends BaseChangeOperator>>();
		reval.add(DummyChangeOperator.class);
		return reval;
	}

	@Override
	public List<Class<? extends BaseIntentionUpdateOperator>> getSupportedFilterOperators() {
		List<Class<? extends BaseIntentionUpdateOperator>> reval = new LinkedList<Class<? extends BaseIntentionUpdateOperator>>();
		reval.add(DummyIntentionUpdateOperator.class);
		return reval;
	}

	@Override
	public List<Class<? extends BaseGenerateOptionsOperator>> getSupportedGenerateOptionsOperators() {
		List<Class<? extends BaseGenerateOptionsOperator>> reval = new LinkedList<Class<? extends BaseGenerateOptionsOperator>>();
		reval.add(DummyGenerateOptionsOperator.class);
		return reval;
	}

	@Override
	public List<Class<? extends BasePolicyControlOperator>> getSupportedPolicyControlOperators() {
		List<Class<? extends BasePolicyControlOperator>> reval = new LinkedList<Class<? extends BasePolicyControlOperator>>();
		reval.add(DummyPolicyControlOperator.class);
		return reval;
	}

	@Override
	public List<Class<? extends BaseViolatesOperator>> getSupportedViolatesOperators() {
		List<Class<? extends BaseViolatesOperator>> reval = new LinkedList<Class<? extends BaseViolatesOperator>>();
		reval.add(DummyViolatesOperator.class);
		return reval;
	}

	@Override
	public List<Class<? extends BaseSubgoalGenerationOperator>> getSupportedPlaners() {
		List<Class<? extends BaseSubgoalGenerationOperator>> reval = new LinkedList<Class<? extends BaseSubgoalGenerationOperator>>();
		reval.add(DummySubgoalGenerationOperator.class);
		return reval;
	}

}