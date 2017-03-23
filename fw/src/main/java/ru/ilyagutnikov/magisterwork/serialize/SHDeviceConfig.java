package ru.ilyagutnikov.magisterwork.serialize;

import java.io.File;

import com.github.angerona.fw.serialize.Resource;
import com.github.angerona.fw.serialize.SerializeHelper;

public interface SHDeviceConfig extends Resource {

	SHDeviceType getDeviceType();

	String getDeviceId();

	void setDeviceId(String deviceId);

	static SHDeviceConfigReal loadXml(File source) {
		return SerializeHelper.get().loadXmlTry(SHDeviceConfigReal.class, source);
	}
}