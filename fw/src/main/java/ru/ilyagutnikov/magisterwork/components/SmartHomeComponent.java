package ru.ilyagutnikov.magisterwork.components;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angerona.fw.Agent;
import com.github.angerona.fw.BaseAgentComponent;

import ru.ilyagutnikov.magisterwork.AdditionalData;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public class SmartHomeComponent extends BaseAgentComponent {

	private static Logger LOG = LoggerFactory.getLogger(SmartHomeComponent.class);

	private File OWLFile;
	private OWLOntologyManager manager;

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

		manager = OWLManager.createOWLOntologyManager();
		try {
			manager.loadOntologyFromOntologyDocument(owlFile);
			LOG.info(AdditionalData.DEBUG_MARKER, "Онтология была загружена успешно");

		} catch (OWLOntologyCreationException e) {

			LOG.info(AdditionalData.DEBUG_MARKER, "Онтология не была загружена " + e);
			e.printStackTrace();
		}


	}

	public boolean addDeviceToOWL(SHDeviceConfig device) {

		return false;
	}

}
