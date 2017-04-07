package ru.ilyagutnikov.magisterwork.operators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.github.angerona.fw.Agent;
import com.github.angerona.fw.am.secrecy.operators.parameter.GenerateOptionsParameter;
import com.github.angerona.fw.comm.Inform;

import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.fol.syntax.FOLAtom;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import ru.ilyagutnikov.magisterwork.components.SmartHomeComponent;
import ru.ilyagutnikov.magisterwork.secrecy.operators.BaseSmartHomeUpdateOperator;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;
import ru.ilyagutnikov.magisterwork.zigbee.SHVariable;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

public class SmartHomeUpdateOperator extends BaseSmartHomeUpdateOperator {

	private Agent agentWithOperator;

	private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight")
			.newMyStem("3.0", Option.<File>empty()).get();

	@Override
	protected Boolean processImpl(GenerateOptionsParameter param) {

		agentWithOperator = param.getAgent();
		Boolean reval = false;

		if (param.getPerception() instanceof Inform) {

			Inform percept = (Inform) param.getPerception();
			Set<FolFormula> perceptFormulas = percept.getContent();
			// Костыль
			FolFormula firstPercept = perceptFormulas.iterator().next();

			if (percept.getSenderId().equals("RealWorld")) {

				reval = executeCommandRealWorld(getCommandFromPercept(firstPercept),
						getDeviceFromPercept(firstPercept));
			}

			if (percept.getSenderId().equals("User")) {

				reval = executeCommandUser(getCommandFromPercept(firstPercept));
			}

		}

		return reval;
	}

	/**
	 * Выполняет команду (запись и прочее в OWL)
	 *
	 * @param command
	 *            команда
	 * @param device
	 *            девайс
	 * @return true - команда выполнена успешна, false - иначе
	 * @author Ilya Gutnikov
	 */
	private boolean executeCommandRealWorld(String command, SHDeviceConfig device) {

		SmartHomeComponent SHComp = agentWithOperator.getComponent(SmartHomeComponent.class);

		if (command.equals("addDevice")) {

			return SHComp.addDeviceToOWL(device);
		}

		return false;
	}

	/**
	 *
	 * @param command
	 * @return
	 * @author Ilya Gutnikov
	 */
	private boolean executeCommandUser(String command) {

		Iterable<Info> result = null;
		ArrayList<JSONObject> jsonObjects = new ArrayList<JSONObject>();

		try {
			result = JavaConversions.asJavaIterable(mystemAnalyzer.analyze(Request.apply(command)).info().toIterable());
		} catch (MyStemApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (result != null) {

			for (final Info info : result) {
				jsonObjects.add(new JSONObject(info.lex()));
			}

			return analyzeJSON(jsonObjects);
		}

		return false;
	}

	private boolean analyzeJSON(ArrayList<JSONObject> objects) {

		return false;
	}

	/**
	 * Получает команду из перцепта
	 *
	 * @param percept
	 *            перцепт
	 * @return Имя предиката-команды
	 * @author Ilya Gutnikov
	 */
	private String getCommandFromPercept(FolFormula percept) {

		FOLAtom firstAtom = percept.getAtoms().iterator().next();

		return firstAtom.getPredicate().getName();
	}

	/**
	 * Получает девайс умного дома из перцепта
	 *
	 * @param percept
	 *            перцепт
	 * @return {@link SHDeviceConfig} девайс
	 * @author Ilya Gutnikov
	 */
	private SHDeviceConfig getDeviceFromPercept(FolFormula percept) {

		FOLAtom firstAtom = percept.getAtoms().iterator().next();
		SHVariable deviceVariable = (SHVariable) firstAtom.getArguments().get(0);

		return deviceVariable.get();
	}
}
