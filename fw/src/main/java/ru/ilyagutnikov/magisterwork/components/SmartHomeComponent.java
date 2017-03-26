package ru.ilyagutnikov.magisterwork.components;

import java.io.File;

import com.github.angerona.fw.BaseAgentComponent;

public class SmartHomeComponent extends BaseAgentComponent {

	private File OWLFile;

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

	public void setOWLFile(File owlFile) {

		OWLFile = owlFile;
	}

}
