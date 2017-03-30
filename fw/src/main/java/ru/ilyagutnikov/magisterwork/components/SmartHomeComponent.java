package ru.ilyagutnikov.magisterwork.components;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angerona.fw.Agent;
import com.github.angerona.fw.BaseAgentComponent;

import ru.ilyagutnikov.magisterwork.AdditionalData;
import ru.ilyagutnikov.magisterwork.ont.DogOnt;
import ru.ilyagutnikov.magisterwork.ont.DogOnt.*;
import ru.ilyagutnikov.magisterwork.serialize.SHAgentConfig;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceType;

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

		if (device.getDeviceType().equals(SHDeviceType.SH_Light)) {

			return createSimpleLamp(device);
		}

		return false;
	}

	/**
	 * ============================================
	 * High-level funcs
	 * ============================================
	 */

	private boolean createSimpleLamp(SHDeviceConfig device) {

		boolean result = false;

		OWLNamedIndividual simpleLamp = createIndvidualByOWLClass(device.getName(), Entities.SimpleLamp);
		OWLNamedIndividual onOffFunc = createIndvidualByOWLClass(Entities.OnOffFunctionality.toString() + device.getName(), Entities.OnOffFunctionality);
		OWLNamedIndividual stateValue = createIndvidualByOWLClass(Entities.OnOffState.toString() + device.getName(), Entities.OnOffState);
		OWLNamedIndividual offStateVal = createIndvidualByOWLClass(Entities.OffStateValue.toString() + device.getName(), Entities.OffStateValue);

		result = addObjectPropertyToIndvidual(simpleLamp, onOffFunc, ObjectProperties.hasFunctionality);
		result = addObjectPropertyToIndvidual(simpleLamp, stateValue, ObjectProperties.hasStateValue);
		result = addObjectPropertyToIndvidual(simpleLamp, offStateVal, ObjectProperties.hasState);

		result = addDataPropertyToIndividual(simpleLamp, DataProperties.nodeId, device.getDeviceId());

		return result;
	}

	/**
	 * ============================================
	 * Low-level funcs
	 * ============================================
	 */

	/**
	 * Создает индивидуал класса
	 * @param individualName Имя индивидуала
	 * @param entity Имя класса
	 * @return {@link OWLNamedIndividual} созданный индивидуал
	 * @author Ilya Gutnikov
	 */
	private OWLNamedIndividual createIndvidualByOWLClass(String individualName, Entities entity) {

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLNamedIndividual individual = factory.getOWLNamedIndividual(individualName, DogOnt.pm);

		OWLClass individualClass = factory.getOWLClass(entity.toString(), DogOnt.pm);

		OWLClassAssertionAxiom assertionAxiom = factory.getOWLClassAssertionAxiom(individualClass, individual);

		manager.addAxiom(ontology, assertionAxiom);

		try {
			manager.saveOntology(ontology);
			System.out.println("Онтология была успешно сохранена");
			return individual;
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			System.out.println("Ошибка при сохранении онтологии " + e);
			return null;
		}
	}

	/**
	 * Удаляет индвидуал
	 * @param indvidual Индивидуал, который необходимо удалить
	 * @author Ilya Gutnikov
	 */
	private void deleteIndividual(OWLNamedIndividual indvidual) {

        OWLEntityRemover remover = new OWLEntityRemover(ontology);

        indvidual.accept(remover);
        manager.applyChanges(remover.getChanges());

        remover.reset();
	}

	/**
	 * Получает список индивидуалов для выбранного класса
	 * @param owlClass Выбранный класс
	 * @return Список индивидуалов
	 * @author Ilya Gutnikov
	 */
	private ArrayList<OWLNamedIndividual> getIndividualsByOWLClass(OWLClass owlClass) {

		ArrayList<OWLNamedIndividual> output = new ArrayList<OWLNamedIndividual>();

		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	    OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
	    for (OWLClass c : ontology.getClassesInSignature()) {

	        if (c.equals(owlClass)){

	            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(c, false);
	            for (OWLNamedIndividual i : instances.getFlattened()) {

	            	output.add(i);
	            }
	        }
	    }

		return output;
	}

	/**
	 * Получает список индивидуалов для выбранного класса
	 * @param entity Сущность класса
	 * @return Список индивидуалов
	 * @author Ilya Gutnikov
	 */
	private ArrayList<OWLNamedIndividual> getIndividualsByOWLClass(Entities entity) {

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass entClass = factory.getOWLClass(entity.toString(), DogOnt.pm);

		return getIndividualsByOWLClass(entClass);
	}

	/**
	 * Получает индвидуала по его имени и классу
	 * @param individualName Имя индивидуала
	 * @param entity Сущность
	 * @return {@link OWLNamedIndividual} индивидуал
	 * @author Ilya Gutnikov
	 */
	private OWLNamedIndividual getIndividualByNameAndClass(String individualName, Entities entity) {

		ArrayList<OWLNamedIndividual> allIndviduals = getIndividualsByOWLClass(entity);
		for (OWLNamedIndividual owlNamedIndividual : allIndviduals) {

			if (owlNamedIndividual.getIRI().getShortForm().equals(individualName)) {
				return owlNamedIndividual;
			}
		}

		return null;
	}

	/**
	 * Добавляет свойства объекта для индивидуала
	 * @param individual Индивидуал
	 * @param propertyIndividual Индивидула-свойство
	 * @param property Сущность свойства
	 * @return true - если операция прошла успешно и false иначе
	 * @author Ilya Gutnikov
	 */
	private boolean addObjectPropertyToIndvidual(OWLNamedIndividual individual, OWLNamedIndividual propertyIndividual, ObjectProperties property) {

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLObjectProperty owlProp = factory.getOWLObjectProperty(property.toString(), DogOnt.pm);

		OWLObjectPropertyAssertionAxiom propertyAxiom = factory.getOWLObjectPropertyAssertionAxiom(owlProp, individual, propertyIndividual);

		manager.addAxiom(ontology, propertyAxiom);

		try {
			manager.saveOntology(ontology);
			System.out.println("Онтология была успешно сохранена");
			return true;
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			System.out.println("Ошибка при сохранении онтологии " + e);
			return false;
		}
	}

	/**
	 * Получает аксиомы объектов для выбранного индивидуала
	 * @param individual Индивидуал
	 * @return Список аксиом
	 * @author Ilya Gutnikov
	 */
	private ArrayList<OWLObjectPropertyAssertionAxiom> getObjectPropertiesAxiomsFromIndividual(OWLNamedIndividual individual) {

		ArrayList<OWLObjectPropertyAssertionAxiom> props = new ArrayList<OWLObjectPropertyAssertionAxiom>();
		Set<OWLObjectPropertyAssertionAxiom> properties = ontology.getObjectPropertyAssertionAxioms(individual);
		props.addAll(properties);

		return props;
	}

	/**
	 *
	 * @param individual
	 * @param property
	 * @return
	 * @author Ilya Gutnikov
	 */
	private OWLObjectPropertyAssertionAxiom getObjectPropertyAxiomFromIndividual(OWLNamedIndividual individual, ObjectProperties property) {

		ArrayList<OWLObjectPropertyAssertionAxiom> props = getObjectPropertiesAxiomsFromIndividual(individual);

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLObjectProperty owlProp = factory.getOWLObjectProperty(property.toString(), DogOnt.pm);

		for (OWLObjectPropertyAssertionAxiom obProperty : props) {
			if (obProperty.getObjectPropertiesInSignature().contains(owlProp)) {

				return obProperty;
			}
		}

		return null;
	}

	/**
	 *
	 * @param axiom
	 * @author Ilya Gutnikov
	 */
	private void deleteObjectPropertyAxiom(OWLObjectPropertyAssertionAxiom axiom) {

		ontology.removeAxiom(axiom);

		try {
			manager.saveOntology(ontology);
			System.out.println("Онтология была успешно сохранена");
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			System.out.println("Ошибка при сохранении онтологии " + e);
		}

	}

	/**
	 * Добавляет свойство данных для индивидула
	 * @param individual Индивидуал
	 * @param property свойство
	 * @param propertyValue Значение свойства
	 * @return true - если операция прошла успешно и false иначе
	 * @author Ilya Gutnikov
	 */
	private boolean addDataPropertyToIndividual(OWLNamedIndividual individual, DataProperties property, String propertyValue) {

		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLDataProperty owlProperty = factory.getOWLDataProperty(property.toString(), DogOnt.pm);

		OWLDataPropertyAssertionAxiom propertyAxiom = factory.getOWLDataPropertyAssertionAxiom(owlProperty, individual, propertyValue);

		manager.addAxiom(ontology, propertyAxiom);

		try {
			manager.saveOntology(ontology);
			System.out.println("Онтология была успешно сохранена");
			return true;
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			System.out.println("Ошибка при сохранении онтологии " + e);
			return false;
		}
	}

}
