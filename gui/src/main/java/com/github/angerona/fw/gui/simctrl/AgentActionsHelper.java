package com.github.angerona.fw.gui.simctrl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.github.angerona.fw.AngeronaEnvironment;

public class AgentActionsHelper {

	/**
	 *
	 * @return
	 * @author Ilya Gutnikov
	 */
	public List<JMenu> createMenuForAgents() {

		List<JMenu> menus = new ArrayList<JMenu>();

		for (String agentName : AngeronaEnvironment.getInstance().getAgentNames()) {

			JMenu menu = new JMenu(agentName);
			menu.add(addDesireAction(agentName));
			menu.add(addIntentionAction(agentName));
			menus.add(menu);
		}

		return menus;
	}

	/**
	 *
	 * @param agentName
	 * @return
	 * @author Ilya Gutnikov
	 */
	private JMenuItem addDesireAction(String agentName) {

		JMenuItem addDesire = new JMenuItem(agentName + ": add simple desire");
		addDesire.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String agentName = e.getActionCommand().substring(0, e.getActionCommand().indexOf(":"));
				AngeronaEnvironment.getInstance().addDesireToAgent(agentName);

			}
		});

		return addDesire;
	}

	/**
	 *
	 * @param agentName
	 * @return
	 * @author Ilya Gutnikov
	 */
	private JMenuItem addIntentionAction(String agentName) {

		JMenuItem addIntetion = new JMenuItem(agentName + ": add simple intention (action)");
		addIntetion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String agentName = e.getActionCommand().substring(0, e.getActionCommand().indexOf(":"));
				AngeronaEnvironment.getInstance().addIntetionToAgent(agentName);

			}
		});

		return addIntetion;
	}

}
