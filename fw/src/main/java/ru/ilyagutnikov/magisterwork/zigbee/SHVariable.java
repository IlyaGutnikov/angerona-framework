package ru.ilyagutnikov.magisterwork.zigbee;

import net.sf.tweety.logics.commons.syntax.Sort;

import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;

public class SHVariable extends SHDeviceTerm {

	public SHVariable(SHDeviceConfig device){
		this(device,Sort.THING);
	}

	public SHVariable(SHDeviceConfig device, Sort sort){
		super(device, sort);
	}

	public SHVariable(SHVariable other) {
		super(other);
	}

	@Override
	public void set(SHDeviceConfig value) {

		this.value = value;

	}

	@Override
	public SHVariable clone() {
		return new SHVariable(this);
	}
}
