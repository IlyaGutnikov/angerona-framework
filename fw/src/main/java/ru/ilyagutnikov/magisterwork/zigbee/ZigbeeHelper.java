package ru.ilyagutnikov.magisterwork.zigbee;

import java.awt.List;
import java.util.ArrayList;
import java.util.Random;

import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.StringTerm;
import net.sf.tweety.logics.commons.syntax.TermAdapter;
import net.sf.tweety.logics.commons.syntax.Variable;
import net.sf.tweety.logics.fol.syntax.FOLAtom;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.math.term.Term;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public class ZigbeeHelper {

	private final static int NUMBER_OF_CHARS = 10;

	/**
	 *
	 * @return
	 * @author Ilya Gutnikov
	 */
	public static String getRandomHexString(){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < NUMBER_OF_CHARS){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, NUMBER_OF_CHARS);
    }
}
