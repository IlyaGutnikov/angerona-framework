package ru.ilyagutnikov.magisterwork.zigbee;

import net.sf.tweety.logics.commons.syntax.TermAdapter;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public abstract class SHDeviceTerm extends TermAdapter<SHDeviceConfig>{

	protected SHDeviceConfig value;

	public SHDeviceTerm(SHDeviceConfig value) {
		super(value);
		this.value = value;
	}

	@Override
	public abstract void set(SHDeviceConfig value);

	@Override
	public SHDeviceConfig get() {

		return value;
	}

	@Override
	public String toString(){
		return "name: " + value.getName() + "_category: " + value.getCategory();
	}
}
