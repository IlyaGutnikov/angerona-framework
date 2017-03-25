package ru.ilyagutnikov.magisterwork.zigbee;

import net.sf.tweety.logics.commons.syntax.StringTerm;
import net.sf.tweety.logics.commons.syntax.TermAdapter;

public class SHVariable extends StringTerm {

	public SHVariable(String value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public SHVariable(SHVariable shVariable) {
		super(shVariable);
	}

	@Override
	public void set(String value) {
		this.value = value;

	}

	@Override
	public SHVariable clone() {
		// TODO Auto-generated method stub
		return new SHVariable(this);
	}

}
