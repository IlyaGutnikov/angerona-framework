package ru.ilyagutnikov.magisterwork.secrecy.operators;

import com.github.angerona.fw.Agent;
import com.github.angerona.fw.am.secrecy.operators.parameter.GenerateOptionsParameter;
import com.github.angerona.fw.operators.Operator;
import com.github.angerona.fw.util.Pair;

public abstract class BaseSmartHomeUpdateOperator extends
Operator<Agent, GenerateOptionsParameter, Boolean> {

	public static final String OPERATION_NAME = "SmartHomeUpdate";

	@Override
	public Pair<String, Class<?>> getOperationType() {

		return new Pair<String, Class<?>>(OPERATION_NAME,
				BaseSmartHomeUpdateOperator.class);
	}

	@Override
	protected GenerateOptionsParameter getEmptyParameter() {

		return new GenerateOptionsParameter();
	}

	@Override
	protected Boolean defaultReturnValue() {

		return false;
	}

}