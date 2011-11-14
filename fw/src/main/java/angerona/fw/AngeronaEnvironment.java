package angerona.fw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.beenuts.ap.AgentProcess;
import net.sf.beenuts.apr.APR;
import net.sf.tweety.logics.firstorderlogic.syntax.FolSignature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import angerona.fw.error.AgentIdException;
import angerona.fw.error.AgentInstantiationException;
import angerona.fw.logic.ConfidentialKnowledge;
import angerona.fw.logic.base.BaseBeliefbase;
import angerona.fw.serialize.SimulationConfiguration;
import angerona.fw.serialize.SimulationConfiguration.AgentInstance.BeliefbaseInstance;

/**
 * A simulation environment for Angerona. This is actually only used for some functional tests.
 * TODO: Extend the functionality.
 * @author Tim Janus
 */
public class AngeronaEnvironment extends APR {

	private static Logger LOG = LoggerFactory.getLogger(AngeronaEnvironment.class);
	
	/** implementation of the factory used for perceptions */
	private PerceptionFactory perceptionFactory = new DefaultPerceptionFactory();
	
	/**
	 * Adds the agents with the given name to the environment
	 * @param ap		agent process handling low level communication through the environment.
	 * @return	true if everything was fine, false if the same agent process was already registered.
	 * @throws AgentIdException Is thrown if the name of the agent process is not unique (two processes have the same name).
	 */
	public boolean addAgent(AngeronaAgentProcess ap) throws AgentIdException {
		if(agentMap.containsKey(ap.getName())) {
			if(agentMap.get(ap.getName()) == ap)
				return false;
			
			throw new AgentIdException("agent with name: " + ap.getName() + " already registered.");
		}
		
		agentMap.put(ap.getName(), ap);
		agents.add(ap);
		ap.setAPR(this);
		return true;
	}
	
	/**
	 * Gets the high level agent with the given name
	 * @param name		unique name of the agent.
	 * @return			Reference to the agent called 'name', if no agent with the given name exists null is returned.
	 */
	public Agent getAgentByName(String name) {
		AgentProcess ap = agentMap.get(name);
		if(ap == null)
			return null;
		return (Agent)ap.getAgentArchitecture();
	}
	
	/**
	 * runs a simulation until no more perceptions are in the queue. 
	 * During a cycle new perceptions can be generated.
	 */
	public void runTillNoMorePerceptionsLeft() {
		boolean percept = false;
		do {
			percept = false;
			for(AgentProcess ap : agents) {
				AngeronaAgentProcess aap = (AngeronaAgentProcess)ap;
				if(aap.hasPerceptions()) {
					percept = true;
					aap.execCycle();
				}
			}
		} while(percept);
	}
	
	/**
	 * Loads a simulation from the given filename
	 * @param filename	name of the xml file containing the information about the simulation.
	 * @return true if no error occored during loading, false otherwise.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public boolean loadSimulation(String filename) throws FileNotFoundException, IOException {
		boolean reval = true;
		SimulationConfiguration config = null;
		
		try {
			config = SimulationConfiguration.loadXml(filename).get(0);
			File f = new File(filename);
			String parentDir = f.getParent();
			LOG.info("Starting simulation: " + config.getName());
		
			for(SimulationConfiguration.AgentInstance ai : config.getAgents()) {
				Agent highLevelAg = new Agent(ai.getConfig(), ai.getName());
			
				BaseBeliefbase world = PluginInstantiator.createBeliefbase(ai.getWorld().getConfig());
				world.parse(getBeliefbaseFilename(parentDir, ai.getWorld(), world));
				
				ConfidentialKnowledge conf = new ConfidentialKnowledge();
				FolSignature fsig = new FolSignature();
				fsig.fromSignature(world.getSignature());
				conf.setSignature(fsig);
				conf.parse(getBeliefbaseFilename(parentDir, ai.getConfidential(), conf));
				
				Map<String, BaseBeliefbase> views = new HashMap<String, BaseBeliefbase>();
				for(BeliefbaseInstance bi : ai.getViews()) {
					BaseBeliefbase actView = PluginInstantiator.createBeliefbase(bi.getConfig());
					actView.parse(getBeliefbaseFilename(parentDir, bi, actView));
					views.put(bi.getViewAgent(), actView);
				}
				highLevelAg.setBeliefs(world, views, (ConfidentialKnowledge)conf);		
				addAgent(highLevelAg.getAgentProcess());
				
				for(String file : ai.getIntentionFiles()) {
					highLevelAg.loadSkillFromXML(file);
				}
			}
			
			DefaultPerceptionFactory df = new DefaultPerceptionFactory();
			List<Perception> initPercepts = df.generateFromParentElement(config.getFlowElement(), null);
			for(Perception p : initPercepts) {
				this.sendAction(p.getReceiverId(), p);
			}
		} catch (ParserConfigurationException e) {
			reval = false;
			LOG.error("Can't start simulation, something went wrong during xml parsing: " + e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			reval = false;
			LOG.error("Can't start simulation, something went wrong during xml parsing: " + e.getMessage());
			e.printStackTrace();
		} catch (AgentIdException e) {
			reval = false;
			LOG.error("Can't start simulation, something went wrong during agent registration: " + e.getMessage());
			e.printStackTrace();
		} catch (AgentInstantiationException e) {
			reval = false;
			LOG.error("Can't start simulation, something went wrong during agent instatiation: " + e.getMessage());
			e.printStackTrace();
		} catch (InstantiationException e) {
			reval = false;
			LOG.error("Can't start simulation, something went wrong during dynamic instantiation: " + e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			reval = false;
			LOG.error("Can't start simulation, something went wrong during dynamic instantiation: " + e.getMessage());
			e.printStackTrace();
		}
		
		return reval;
	}
	
	/**
	 * Helper method: Generates the filepath to a belief base.
	 * @param parentDir	The parent dir of the simulation
	 * @param bi		data structure containing information about the belief base 
	 * @param bb		An instance of the belief base for determining the file ending.
	 * @return			String representing the path to the correct belief base file.
	 */
	private String getBeliefbaseFilename(String parentDir, SimulationConfiguration.AgentInstance.BeliefbaseInstance bi, BaseBeliefbase bb) {
		return parentDir + "/" + bi.getFileSuffix() + "." + bb.getFileEnding();
	}
	
	@Override
	public void sendAction(String agentName, Object action) {
		// The action send by one agent is the perception of the other one.
		if(Action.ALL.equals(agentName)) {
			for(String name : agentMap.keySet()) {
				agentMap.get(name).perceive(action);
			}
		} else {
			agentMap.get(agentName).perceive(action);
		}
	}

	PerceptionFactory getPerceptionFactory() {
		return perceptionFactory;
	}
}