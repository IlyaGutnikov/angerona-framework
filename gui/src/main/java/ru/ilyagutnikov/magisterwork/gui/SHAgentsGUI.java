package ru.ilyagutnikov.magisterwork.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angerona.fw.AngeronaEnvironment;
import com.github.angerona.fw.comm.SpeechAct.SpeechActType;
import com.github.angerona.fw.gui.AngeronaWindow;
import com.github.angerona.fw.reflection.FolFormulaVariable;
import com.github.angerona.fw.serialize.SimulationConfiguration;

import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.Variable;
import net.sf.tweety.logics.fol.syntax.FOLAtom;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfig;
import ru.ilyagutnikov.magisterwork.serialize.SHDeviceConfigReal;
import ru.ilyagutnikov.magisterwork.zigbee.ZigbeeHelper;

/**
 *
 * @author ilyagutnikov
 *
 */
public class SHAgentsGUI {

	private static Logger LOG = LoggerFactory.getLogger(SHAgentsGUI.class);

	private SHAgentsGUI() {
	}

	private static SHAgentsGUI instance;

	/**
	 * Реализация синглтона
	 *
	 * @return instance
	 * @author Ilya Gutnikov
	 */
	public static synchronized SHAgentsGUI getInstance() {

		if (instance == null) {
			instance = new SHAgentsGUI();
		}
		return instance;
	}

	protected JMenu SHAgentsMenu = new JMenu("SHAgents actions");
	protected JMenu userMenu = new JMenu("User");
	protected JMenu smartHomeMenu = new JMenu("SmartHome");
	protected JMenu realWorldMenu = new JMenu("RealWorld");

	protected JMenu shDevicesMenu = new JMenu("Devices of real world");

	public JMenu createSubMenu() {

		/// RealWorld
		JMenuItem addDeviceFromRW = new JMenuItem("Add device");
		addDeviceFromRW.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO open an XML file
				onLoadDeviceXML();

			}
		});
		realWorldMenu.add(addDeviceFromRW);
		realWorldMenu.addSeparator();
		realWorldMenu.add(shDevicesMenu);

		/// SmartHome

		/// User
		// TODO add text input

		SHAgentsMenu.add(realWorldMenu);
		SHAgentsMenu.add(smartHomeMenu);
		SHAgentsMenu.add(userMenu);

		return SHAgentsMenu;
	}

	/**
	 * Загружает файл устройства
	 *
	 * @author Ilya Gutnikov
	 */
	private void onLoadDeviceXML() {

		FileFilter filter = new FileNameExtensionFilter("XML File","xml");

		JFileChooser fileDialog = new JFileChooser();
		fileDialog.setCurrentDirectory(new File("."));
		fileDialog.setFileFilter(filter);

		int reval = fileDialog.showOpenDialog(null);
		if (reval == JFileChooser.APPROVE_OPTION) {

			File file = fileDialog.getSelectedFile();
			SHDeviceConfig device = SHDeviceConfig.loadXml(file);

			if (device.getDeviceId().isEmpty()) {

				device.setDeviceId(ZigbeeHelper.getRandomHexString());
			}

			addDeviceToRealWorldDevicesList(device);

			Constant deviceConst = new Constant("device");
			deviceConst.set(device.toString());
			//TODO сделать свой предикат
			FOLAtom addDeviceAtom = new FOLAtom(new Predicate("addDevice", 1), deviceConst);

			AngeronaEnvironment.getInstance().sendPerception("RealWorld", "SmartHome",
					addDeviceAtom, SpeechActType.SAT_INFORMATIVE);
		}
	}

	/**
	 * Добавляет устройство в список устройств реального мира
	 * @param device
	 * @author Ilya Gutnikov
	 */
	private void addDeviceToRealWorldDevicesList(SHDeviceConfig device) {

		shDevicesMenu.add(new JMenuItem(device.getName() + "@" + device.getDeviceId()));
	}

}
