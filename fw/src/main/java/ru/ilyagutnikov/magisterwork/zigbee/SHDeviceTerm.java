package ru.ilyagutnikov.magisterwork.zigbee;

import net.sf.tweety.logics.commons.syntax.Sort;
import net.sf.tweety.logics.commons.syntax.TermAdapter;

import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public abstract class SHDeviceTerm extends TermAdapter<SHDeviceConfig> {

	protected SHDeviceConfig value;

	public SHDeviceTerm(SHDeviceConfig value) {
		super(value);
	}

	public SHDeviceTerm(SHDeviceConfig value, Sort sort) {
		super(value, sort);
	}

	public SHDeviceTerm(SHDeviceTerm other) {
		super(other.value, other.getSort());
	}

	public abstract void set(SHDeviceConfig value);

	@Override
	public SHDeviceConfig get() {
		return value;
	}

	@Override
	public String toString(){
		return this.value.toString();
	}

}
