package ru.ilyagutnikov.magisterwork.serialize;

import java.io.File;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.github.angerona.fw.serialize.Resource;
import com.github.angerona.fw.serialize.SerializeHelper;
import com.github.angerona.fw.serialize.SimulationConfiguration;

/**
 * Класс, который хранит в себе описание дейвайса Умного Дома
 * @author ilyagutnikov
 *
 */
@Root(name="shdevice-configuration")
public class SHDeviceConfigReal implements SHDeviceConfig {

	public static final String RESOURCE_TYPE = "SmartHomeDeviceTemplate";

	@Element(name="name")
	protected String name;

	@Element(name="device-type")
	protected String deviceType;

	@Element(name="category", required=false)
	protected String category = "";

	@Element(name="description", required=false)
	protected String description = "";

	@Override
	public String getName() {

		return name;
	}

	@Override
	public String getDescription() {

		return description;
	}

	@Override
	public String getResourceType() {

		return RESOURCE_TYPE;
	}

	@Override
	public String getCategory() {

		return category;
	}

	@Override
	public SHDeviceType getDeviceType() {

		return SHDeviceType.valueOf(deviceType);
	}

	public static SHDeviceConfigReal loadXml(File source) {
		return SerializeHelper.get().loadXmlTry(SHDeviceConfigReal.class, source);
	}

}
