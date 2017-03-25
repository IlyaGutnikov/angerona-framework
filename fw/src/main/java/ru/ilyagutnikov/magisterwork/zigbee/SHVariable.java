package ru.ilyagutnikov.magisterwork.zigbee;

import net.sf.tweety.logics.commons.syntax.TermAdapter;
import net.sf.tweety.logics.commons.syntax.Variable;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public class SHVariable extends SHDeviceTerm {

	public SHVariable(SHDeviceConfig value) {
		super(value);
	}

	@Override
	public void set(SHDeviceConfig value) {

		if (!(value.getName().isEmpty() && value.getCategory().isEmpty() && value.getDeviceId().isEmpty())) {

			this.value = value;
		}

	}

	@Override
	public SHVariable clone() {
		// TODO Auto-generated method stub
		return new SHVariable(value);
	}


}
