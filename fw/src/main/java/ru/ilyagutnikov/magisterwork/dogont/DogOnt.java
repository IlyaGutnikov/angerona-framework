package ru.ilyagutnikov.magisterwork.dogont;

import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class DogOnt {

	public static String base = "http://elite.polito.it/ontologies/dogont.owl";

	private static String wildCard = "#";

	public static PrefixManager pm = new DefaultPrefixManager(base + wildCard);

	public enum Entities {

		SimpleLamp,
		OnOffFunctionality,
		OnOffState,
		OnStateValue,
		OffStateValue

	}

	public enum ObjectProperties {

		hasFunctionality
	}


	public enum DataProperties {

		nodeId

	}
}
