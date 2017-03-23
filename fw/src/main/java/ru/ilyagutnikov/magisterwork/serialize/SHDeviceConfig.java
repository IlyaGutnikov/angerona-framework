package ru.ilyagutnikov.magisterwork.serialize;

import com.github.angerona.fw.serialize.Resource;

public interface SHDeviceConfig extends Resource {

	SHDeviceType getDeviceType();

	String getDeviceId();

	void setDeviceId(String deviceId);
}