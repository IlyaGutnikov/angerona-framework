package ru.ilyagutnikov.magisterwork.components;

import com.github.angerona.fw.BaseAgentComponent;

public class SmartHomeComponent extends BaseAgentComponent {

	public SmartHomeComponent() {
		// TODO Auto-generated constructor stub
	}

	public SmartHomeComponent(SmartHomeComponent other) {
		super(other);
	}

	@Override
	public SmartHomeComponent clone() {
		return new SmartHomeComponent(this);
	}

}
