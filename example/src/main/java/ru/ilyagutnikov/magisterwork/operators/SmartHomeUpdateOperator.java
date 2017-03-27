package ru.ilyagutnikov.magisterwork.operators;

import java.util.List;
import java.util.Set;

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

public class SmartHomeUpdateOperator extends BaseSmartHomeUpdateOperator {

	private Agent agentWithOperator;

	@Override
	protected Boolean processImpl(GenerateOptionsParameter param) {

		agentWithOperator = param.getAgent();
		Boolean reval = false;

		if (param.getPerception() instanceof Inform) {

			Inform percept = (Inform) param.getPerception();
			Set<FolFormula> perceptFormulas = percept.getContent();
			//Костыль
			FolFormula firstPercept = perceptFormulas.iterator().next();

			if (percept.getSenderId().equals("RealWorld")) {

				reval = executeCommand(getCommandFromPercept(firstPercept), getDeviceFromPercept(firstPercept));
			}

			if (percept.getSenderId().equals("User")) {


			}

		}

		return reval;
	}

	/**
	 * Выполняет команду (запись и прочее в OWL)
	 * @param command команда
	 * @param device девайс
	 * @return true - команда выполнена успешна, false - иначе
	 * @author Ilya Gutnikov
	 */
	private boolean executeCommand(String command, SHDeviceConfig device) {

		SmartHomeComponent SHComp = agentWithOperator.getComponent(SmartHomeComponent.class);

		if (command.equals("addDevice")) {

			return SHComp.addDeviceToOWL(device);
		}

		return false;
	}

	/**
	 *
	 * @param command
	 * @param subCommands
	 * @return
	 * @author Ilya Gutnikov
	 */
	private boolean executeCommand(String command, List<String> subCommands) {

		return false;
	}

	/**
	 *
	 * @param command
	 * @return
	 * @author Ilya Gutnikov
	 */
	private boolean executeCommand(String command) {

		return false;
	}

	/**
	 * Получает команду из перцепта
	 * @param percept перцепт
	 * @return Имя предиката-команды
	 * @author Ilya Gutnikov
	 */
	private String getCommandFromPercept(FolFormula percept) {

		FOLAtom firstAtom = percept.getAtoms().iterator().next();

		return firstAtom.getPredicate().getName();
	}

	/**
	 * Получает девайс умного дома из перцепта
	 * @param percept перцепт
	 * @return {@link SHDeviceConfig} девайс
	 * @author Ilya Gutnikov
	 */
	private SHDeviceConfig getDeviceFromPercept(FolFormula percept) {

		FOLAtom firstAtom = percept.getAtoms().iterator().next();
		SHVariable deviceVariable = (SHVariable) firstAtom.getArguments().get(0);

		return deviceVariable.get();
	}
}
