package ru.ilyagutnikov.magisterwork.operators;

import java.util.Set;

import com.github.angerona.fw.am.secrecy.operators.parameter.GenerateOptionsParameter;
import com.github.angerona.fw.comm.Inform;

import net.sf.tweety.logics.fol.syntax.FolFormula;
import ru.ilyagutnikov.magisterwork.secrecy.operators.BaseSmartHomeUpdateOperator;

public class SmartHomeUpdateOperator extends BaseSmartHomeUpdateOperator {

	@Override
	protected Boolean processImpl(GenerateOptionsParameter param) {

		if (param.getPerception() instanceof Inform) {

			Inform percept = (Inform) param.getPerception();

			if (percept.getSenderId().equals("RealWorld")) {

				Set<FolFormula> tetst = percept.getContent();

				System.out.println("It's a mutha-fickin success");
				System.out.println(tetst);
			}

			if (percept.getSenderId().equals("User")) {


			}

		}

		return false;
	}

}
