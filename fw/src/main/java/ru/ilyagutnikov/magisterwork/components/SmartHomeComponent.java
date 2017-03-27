package ru.ilyagutnikov.magisterwork.components;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angerona.fw.Agent;
import com.github.angerona.fw.BaseAgentComponent;

import ru.ilyagutnikov.magisterwork.AdditionalData;
import ru.ilyagutnikov.magisterwork.dogont.DogOnt.*;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public class SmartHomeComponent extends BaseAgentComponent {

	private static Logger LOG = LoggerFactory.getLogger(SmartHomeComponent.class);

	private File OWLFile;
	private OWLOntologyManager manager;
	private OWLOntology ontology;

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
			ontology = manager.loadOntologyFromOntologyDocument(owlFile);

			LOG.info(AdditionalData.DEBUG_MARKER, "Онтология была успешно загружена");

		} catch (OWLOntologyCreationException e) {

			LOG.info(AdditionalData.DEBUG_MARKER, "Онтология не была загружена " + e);
			e.printStackTrace();
		}


	}

	/**
	 * Создание индвидуала девайса, на основе конфигурации
	 * @param device конфигурация девайса
	 * @return true - если индивидуал был создан и false иначе
	 * @author Ilya Gutnikov
	 */
	public boolean addDeviceToOWL(SHDeviceConfig device) {



		return false;
	}

	private boolean createIndvidualByOWLClass(String individualName, Entities enitity) {

		OWLDataFactory factory = manager.getOWLDataFactory();

		return false;
	}

}
