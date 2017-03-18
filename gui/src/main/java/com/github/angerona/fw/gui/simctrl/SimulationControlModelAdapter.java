package com.github.angerona.fw.gui.simctrl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.angerona.fw.Angerona;
import com.github.angerona.fw.AngeronaEnvironment;
import com.github.angerona.fw.gui.AngeronaWindow;
import com.github.angerona.fw.serialize.SimulationConfiguration;
import com.github.angerona.fw.util.ModelAdapter;

import ru.ilyagutnikov.magisterwork.AdditionalData;

/**
 * Implements the SimulatonControlModel
 * @author Tim Janus
 */
public class SimulationControlModelAdapter extends ModelAdapter implements SimulationControlModel {

	private static Logger LOG = LoggerFactory.getLogger(SimulationControlModelAdapter.class);

	/** the SimulationConfiguration of the data model */
	private SimulationConfiguration simulationConfig;

	private int simulationTick;

	/** the SimulationState of the data model */
	private SimulationState simulationState = SimulationState.SS_UNDEFINED;

	/** the AngeroneEnvironment representing the dynamic simulation */
	private AngeronaEnvironment environment = AngeronaEnvironment.getInstance();
	private AgentActionsHelper helper = new AgentActionsHelper();

	/** generate a thread pool using one thread (the worker thread for the simulation) */
	private final ExecutorService pool = Executors.newFixedThreadPool(1);

	/**
	 * Helper method: sets the SimulationState and fires the PropertyChangeEvent
	 * @param newState	The new SimulationState
	 */
	private synchronized void setSimulationState(SimulationState newState) {
		simulationState = changeProperty("simulationState", simulationState, newState);
	}

	private synchronized void setSimulationTick(int tick) {
		this.simulationTick = changeProperty("simulationTick", this.simulationTick, tick);
	}

	@Override
	public void setSimulation(SimulationConfiguration config) {

		LOG.info(AdditionalData.DEBUG_MARKER, "Установка конфигурации симуляции. Конфигруация: " + config.getName());

		if(simulationConfig != null) {
			if(	simulationState == SimulationState.SS_INITALIZED ||
				simulationState == SimulationState.SS_FINISHED) {
				pool.execute(new Runnable() {
					@Override
					public void run() {
						synchronized(environment) {
							environment.cleanupEnvironment();
						}
					}
				});
			}
		}
		simulationConfig = changeProperty("simulationConfig", simulationConfig, config);
		if(simulationConfig != null) {
			setSimulationState(SimulationState.SS_LOADED);
		} else {
			setSimulationState(SimulationState.SS_UNDEFINED);
		}
	}

	@Override
	public SimulationState initSimulation() {

		LOG.info(AdditionalData.DEBUG_MARKER, "Инициализация симуляции");

		if(simulationState == SimulationState.SS_LOADED) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					synchronized(environment) {
						if(environment.initSimulation(simulationConfig)) {
							setSimulationState(SimulationState.SS_INITALIZED);
							setSimulationTick(simulationTick);
							AngeronaWindow.get().setAgentActionsActive(true);

							AngeronaWindow.get().getAgentsActionMenu().removeAll();
							for (JMenu menu : helper.createMenuForAgents()) {

								AngeronaWindow.get().getAgentsActionMenu().add(menu);
							}
						}
					}
				}
			});
		}
		return simulationState;
	}

	@Override
	public SimulationState runSimulation() {
		LOG.info(AdditionalData.DEBUG_MARKER, "Запуск симуляции");
		if(simulationState == SimulationState.SS_INITALIZED) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					synchronized(environment) {
						if(!environment.runOneTick()) {
							setSimulationState(SimulationState.SS_FINISHED);
							AngeronaWindow.get().setAgentActionsActive(false);
						} else {
							setSimulationTick(simulationTick);
						}
					}
				}
			});

		}
		return simulationState;
	}

	@Override
	public SimulationState getSimulationState() {
		return simulationState;
	}

	@Override
	public SimulationConfiguration getSimulation() {
		return simulationConfig;
	}
}
