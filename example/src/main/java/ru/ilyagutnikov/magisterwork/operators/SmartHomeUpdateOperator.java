package ru.ilyagutnikov.magisterwork.operators;

import java.util.Set;

import com.github.angerona.fw.Agent;
import com.github.angerona.fw.am.secrecy.operators.parameter.GenerateOptionsParameter;
import com.github.angerona.fw.comm.Inform;

import net.sf.tweety.logics.fol.syntax.FolFormula;
import ru.ilyagutnikov.magisterwork.components.SmartHomeComponent;
import ru.ilyagutnikov.magisterwork.secrecy.operators.BaseSmartHomeUpdateOperator;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public class SmartHomeUpdateOperator extends BaseSmartHomeUpdateOperator {

	private Agent agentWithOperator;

	@Override
	protected Boolean processImpl(GenerateOptionsParameter param) {

		agentWithOperator = param.getAgent();

		if (param.getPerception() instanceof Inform) {

			Inform percept = (Inform) param.getPerception();
			Set<FolFormula> perceptFormulas = percept.getContent();
			//Костыль
			FolFormula firstPercept = perceptFormulas.iterator().next();



			if (percept.getSenderId().equals("RealWorld")) {

				System.out.println("It's a mutha-fickin success");
				System.out.println(firstPercept);
			}

			if (percept.getSenderId().equals("User")) {


			}

		}

		return false;
	}


	private boolean executeCommand(String command, SHDeviceConfig device) {

		SmartHomeComponent SHComp = agentWithOperator.getComponent(SmartHomeComponent.class);

		if (command.equals("addDevice")) {

			return SHComp.addDeviceToOWL(device);
		}

		return false;
	}

	private String getCommandFromPercept(FolFormula percept) {

		return "";
	}
}
