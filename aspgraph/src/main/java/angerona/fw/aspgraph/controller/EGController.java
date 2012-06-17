package angerona.fw.aspgraph.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import angerona.fw.aspgraph.graphs.EDGEdge;
import angerona.fw.aspgraph.graphs.EDGVertex;
import angerona.fw.aspgraph.graphs.EDGVertex.Color;
import angerona.fw.aspgraph.graphs.EGAssumptionVertex;
import angerona.fw.aspgraph.graphs.EGBotVertex;
import angerona.fw.aspgraph.graphs.EGEdge;
import angerona.fw.aspgraph.graphs.EGEdge.EdgeType;
import angerona.fw.aspgraph.graphs.EGLiteralVertex;
import angerona.fw.aspgraph.graphs.EGLiteralVertex.Annotation;
import angerona.fw.aspgraph.graphs.EGTopVertex;
import angerona.fw.aspgraph.graphs.EGVertex;
import angerona.fw.aspgraph.graphs.ExplanationGraph;
import angerona.fw.aspgraph.graphs.ExtendedDependencyGraph;
import angerona.fw.aspgraph.util.AnswerSetTwoValued;
import angerona.fw.aspgraph.util.EGList;
import angerona.fw.aspgraph.util.LinkedCycle;
import angerona.fw.aspgraph.util.Path;
import angerona.fw.aspgraph.util.SubEG;
import angerona.fw.aspgraph.util.Tarjan;

import net.sf.tweety.logicprogramming.asplibrary.syntax.Program;
import net.sf.tweety.logicprogramming.asplibrary.util.AnswerSet;

public class EGController {
	
	private HashMap<AnswerSet, EGList> egLists;
	private static EGController instance;
	private Set<String>[] nneArray;
	private Path[] allAPArray;
	private Set<Set<String>> lces;
	private HashMap<AnswerSet, Set<Set<String>>> assumptions; 
	private HashMap<String,Set<SubEG>> incompleteEGs;

	
	private EGController(){
		assumptions = new HashMap<AnswerSet,Set<Set<String>>>();
	}
	
	public static EGController instance(){
		if (instance == null) instance = new EGController();
		return instance;
	}
	
	public void createEGs(Map<AnswerSet,ExtendedDependencyGraph> edgs){
		egLists = new HashMap<AnswerSet,EGList>();
		
		/* Create EGs according to answer set */
		for (AnswerSet as : edgs.keySet()){
			if (as != null){
				ExtendedDependencyGraph edg = edgs.get(as);
				AnswerSetTwoValued answerSet = edg.getAnswerSet();
				calculateAssumptions(answerSet, edg, as);
				Set<String> literals = edg.getNodeMap().keySet();
			
				/* Calculate basic nodes: facts, constraints, unfounded */
				Set<EDGVertex> factNodeSet = new HashSet<EDGVertex>();
				Set<EDGVertex> unfoundedNodeSet = new HashSet<EDGVertex>();
				Set<EDGVertex> constraintNodeSet = new HashSet<EDGVertex>();
				for (EDGVertex v : edg.getVertices()){
					if (edg.getInEdges(v).size() == 0){
						if (v.getRuleNo() == 0) unfoundedNodeSet.add(v); // unfounded literal
						else factNodeSet.add(v); // fact
					} else if (v.getLiteral().equals("-")) constraintNodeSet.add(v); // constraint
				}
				
				/* Initialize egLists */
				egLists.put(as, new EGList());
				
				/* Create EGs according to assumption */
				for (Set<String> assumption : assumptions.get(as)){
					incompleteEGs = new HashMap<String,Set<SubEG>>();
					
					/* Create EGs according to literal */
					for (String literal : literals){
						Set<String> visitedLits = new HashSet<String>(); // visited literals on the way of calculating EG to literal 
						createEGsToLiteral(literal, assumption,factNodeSet,unfoundedNodeSet,constraintNodeSet,edg,as,visitedLits);
					}
				}
			}
		}
	}

	/**
	 * Creates EGs according to a literal resp. an assumption and an answer set
	 * @param literal Literal, to which an EGs are created
	 * @param assumption Assumption resp. which the EGs are created
	 * @param factNodeSet Set of EDG-nodes which represent facts
	 * @param unfoundedNodeSet Set of EGG-nodes which represent unfounded literals
	 * @param constraintNodeSet Set of EDG-nodes which represent constraints
	 * @param edg EDG which is used for transformation between EDG and EGs
	 * @param as Answer set resp. which the EGs are created
	 * @param visitedLits Visited Literals on the way of creating an EG to another literal
	 */
	private void createEGsToLiteral(String literal, Set<String> assumption, Set<EDGVertex> factNodeSet, Set<EDGVertex> unfoundedNodeSet, Set<EDGVertex> constraintNodeSet, ExtendedDependencyGraph edg, AnswerSet as, Set<String> visitedLits){
		
		/* Literal is an assumption */
		if (assumption.contains(literal)){
			ExplanationGraph eg = createAssumptionEG(literal);
			egLists.get(as).addEGtoLiteral(literal,assumption, eg);
		}
		else{
			List<EDGVertex> specificNodes = edg.getNodeMap().get(literal); // all EDG-nodes which represent the literal
			for (EDGVertex v : specificNodes){			
				/* fact */
				if (factNodeSet.contains(v)){
					ExplanationGraph eg = createFactEG(literal);
					egLists.get(as).addEGtoLiteral(literal,assumption, eg);
					
				/* unfounded node */
				} else if (unfoundedNodeSet.contains(v)){
					ExplanationGraph eg = createUnfoundedEG(literal);
					egLists.get(as).addEGtoLiteral(literal,assumption, eg);
					
				/* dependent of other nodes */
				} else if (!constraintNodeSet.contains(v)){
					ExplanationGraph eg = new ExplanationGraph();
					EGLiteralVertex litVertex = null;
					Set<Set<SubEG>> subEGs = new HashSet<Set<SubEG>>();
					
					/* node is green */
					if (v.getColor().equals(Color.GREEN)){
						createPositiveEGs(edg, v, as, assumption, visitedLits, factNodeSet, unfoundedNodeSet, constraintNodeSet);
					
					/* node is red and represents a literal which is not in the answer set*/	
					} else if (edg.getAnswerSet().getUnfounded().contains(v.getLiteral())){
						Set<ExplanationGraph> list = egLists.get(as).getEGList(literal, assumption);
						if (list != null){
							if (list.isEmpty()) createNegativeEGs(edg, v, as, assumption, visitedLits, factNodeSet, unfoundedNodeSet, constraintNodeSet);
						}
						else createNegativeEGs(edg, v, as, assumption, visitedLits, factNodeSet, unfoundedNodeSet, constraintNodeSet);
					}
				}	
			}	
		}
		
	}
	
	private ExplanationGraph createUnfoundedEG(String literal) {
		ExplanationGraph eg = new ExplanationGraph();
		EGVertex litVertex = new EGLiteralVertex(Annotation.NEG, literal);
		EGVertex botVertex = new EGBotVertex(literal);
		EGEdge e = new EGEdge(litVertex,botVertex,EdgeType.NEG);
		eg.addVertex(litVertex);
		eg.addVertex(botVertex);
		eg.addEdge(litVertex, botVertex, e);
		return eg;
	}

	/**
	 * Creates an EG with an fact sink
	 * @param literal Literal which is a fact
	 * @return EG which represents that a literal is a fact
	 */
	private ExplanationGraph createFactEG(String literal) {
		ExplanationGraph eg = new ExplanationGraph();
		EGVertex litVertex = new EGLiteralVertex(Annotation.POS, literal);
		EGVertex topVertex = new EGTopVertex(literal);
		EGEdge e = new EGEdge(litVertex,topVertex,EdgeType.POS);
		eg.addVertex(litVertex);
		eg.addVertex(topVertex);
		eg.addEdge(litVertex, topVertex, e);
		return eg;
	}

	private void constructEG(int i, String literal, Set<String> assumption, AnswerSet as, ExplanationGraph eg, Set<SubEG> subEGArray[], EGVertex litVertex, boolean isIncompleteEG, String cycleTarget){
		if (i == -1){
			if (isIncompleteEG){
				Set<SubEG> egSet;
				if (incompleteEGs.containsKey(literal)) egSet = incompleteEGs.get(literal);
				else egSet = new HashSet<SubEG>();
				egSet.add(new SubEG(eg, cycleTarget));
				incompleteEGs.put(literal, egSet);
			}
			else egLists.get(as).addEGtoLiteral(literal, assumption, eg);

		}
		else{
			for (SubEG e : subEGArray[i]){
				try {
					ExplanationGraph eg2 = eg.deepCopy();
					eg2.addEdge(litVertex, e.getEG(), e.getRootLiteral(),e.getConnectingEdgeType());
					constructEG(i-1,literal,assumption,as,eg2,subEGArray, litVertex, e.isIncomplete(literal), e.getCycleTarget());
				} catch (ClassNotFoundException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}
	}
	
	private void constructLCE(int i, Set<String> lce){
		if (i == -1) lces.add(lce);
		else{
			for (String lit : nneArray[i]){
				Set<String> nextLce = new HashSet<String>();
				nextLce.addAll(lce);
				nextLce.add(lit);
				constructLCE(i-1,nextLce);
			}
		}
	}
	
	private void calculateAssumptions(AnswerSetTwoValued as, ExtendedDependencyGraph edg, AnswerSet asn){
		Set<Path> allAssumptionPaths = new HashSet<Path>();
		Tarjan t = new Tarjan();
		List<LinkedCycle> linkedCycles = t.executeTarjan(edg);
		for (LinkedCycle linkedCycle : linkedCycles){
			Set<Path> assumptionPath = new HashSet<Path>(linkedCycle.getAssumptionPaths());
			/* Remove green nodes and nodes without no outgoing negative edges */
			
			for (Path path : assumptionPath){
				Path path2 = new Path();
				path2.addAll(path);
				path2.setStart(path.getStart());
				path2.setEnd(path.getEnd());
				for (EDGVertex v : path2.getVertices()){
					if (v.getColor().equals(Color.GREEN)) path.remove(v);
					else {
						boolean allPositive = true;
						for (EDGEdge e : edg.getOutEdges(v)){
							if (e.getLabel().equals(EDGEdge.EdgeType.NEG)) allPositive = false;
						}
						if (allPositive) path.remove(v);
					}
				}
			}
			if (!assumptionPath.isEmpty()){
				allAssumptionPaths.addAll(assumptionPath);
			}
		}
		/* calculate Assumptions */
		HashSet<Set<String>> assumptionsAs = new HashSet<Set<String>>();
		assumptions.put(asn, assumptionsAs);
		allAPArray = new Path[allAssumptionPaths.size()];
		allAPArray = allAssumptionPaths.toArray(allAPArray);
		constructAssumption(allAPArray.length-1,new HashSet<String>(), asn);
		
		/* Remove subsets to get minimal assumptions */
		Set<Set<String>> assumptionSet = assumptions.get(asn);
		Set<Set<String>> ass = new HashSet<Set<String>>(assumptionSet);
		for (Set<String> a : ass){
			for (Set<String> a2 : ass){
				if (!a.equals(a2)){
					if (a.containsAll(a2)) assumptionSet.remove(a);
				}
			}
		}
	}
	
	private void constructAssumption(int i, Set<String> assumption, AnswerSet as){
		if (i == -1){
			assumptions.get(as).add(assumption);
		}
		else{
			for (EDGVertex v : allAPArray[i].getVertices()){
				Set<String> nextAssumption = new HashSet<String>();
				nextAssumption.addAll(assumption);
				nextAssumption.add(v.getLiteral());
				constructAssumption(i-1,nextAssumption,as);
			}
		}
	}
	
	public Set<Set<String>> getAssumptions(AnswerSet as){
		return assumptions.get(as);
	}
	
	public Set<ExplanationGraph> getEGs(AnswerSet as, Set<String> assumption, String literal){
		EGList list = egLists.get(as);
		return list.getEGList(literal, assumption);
	}
	
	/**
	 * Creates an EG with an assumption sink
	 * @param literal Literal which is an assumption
	 * @return EG which represents that a literal is an assumption
	 */
	private ExplanationGraph createAssumptionEG(String literal){
		ExplanationGraph eg = new ExplanationGraph();
		EGVertex litVertex = new EGLiteralVertex(Annotation.NEG, literal);
		EGVertex assumeVertex = new EGAssumptionVertex(literal);
		EGEdge e = new EGEdge(litVertex,assumeVertex,EdgeType.NEG);
		eg.addVertex(litVertex);
		eg.addVertex(assumeVertex);
		eg.addEdge(litVertex, assumeVertex, e);
		return eg;
	}
	
	private void createPositiveEGs(ExtendedDependencyGraph edg, EDGVertex v, AnswerSet as, Set<String> assumption, Set<String> visitedLits, Set<EDGVertex> factNodeSet, Set<EDGVertex> unfoundedNodeSet, Set<EDGVertex> constraintNodeSet){
		ExplanationGraph eg = new ExplanationGraph();
		Set<Set<SubEG>> subEGsSet = new HashSet<Set<SubEG>>();
		String literal = v.getLiteral();
		EGLiteralVertex litVertex = new EGLiteralVertex(Annotation.POS,literal);
		
		for (EDGEdge e : edg.getInEdges(v)){
			Set<SubEG> subEGs = new HashSet<SubEG>();
			EDGVertex source = e.getSource();
			EGEdge.EdgeType type = null;
			eg.addVertex(litVertex);
			
			switch(e.getLabel()){
				case POS : type = EGEdge.EdgeType.POS; break;
				case NEG : type = EGEdge.EdgeType.NEG;
			}
			
			Set<ExplanationGraph> list = egLists.get(as).getEGList(source.getLiteral(),assumption);
			
			/* EGs for sourcenode not build yet*/
			if (list == null){
				visitedLits.add(literal);
				
				/* there is a cycle */
				if (visitedLits.contains(source.getLiteral())){
					try {
						ExplanationGraph eg2 = eg.deepCopy();
						Annotation targetAnnotation;
						if (edg.getAnswerSet().getUnfounded().contains(source.getLiteral())) targetAnnotation = Annotation.NEG;
						else targetAnnotation = Annotation.POS;
						EGLiteralVertex targetVertex = new EGLiteralVertex(targetAnnotation, source.getLiteral());
						eg2.addEdge(litVertex, targetVertex , new EGEdge(litVertex, targetVertex, type));
						subEGs.add(new SubEG(eg2, type, source.getLiteral(), source.getLiteral()));	
					} catch (ClassNotFoundException	| IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				/* there is no cycle */
				else createEGsToLiteral(source.getLiteral(), assumption, factNodeSet, unfoundedNodeSet, constraintNodeSet, edg, as,visitedLits);
					
				list = egLists.get(as).getEGList(source.getLiteral(),assumption);
				if (list != null){
					for (ExplanationGraph subgraph : list){
					subEGs.add(new SubEG(subgraph,type,source.getLiteral(),null));
					}
				}
					
				Set<SubEG> list2 = incompleteEGs.get(source.getLiteral());
				if (list2 != null){
					for (SubEG subgraph : list2){
						subEGs.add(new SubEG(subgraph.getEG(),type,source.getLiteral(),subgraph.getCycleTarget()));
					}
				}
				
			/* EGs for sourcenode already build */	
			} else{
				for (ExplanationGraph subgraph : list){
					subEGs.add(new SubEG(subgraph,type,source.getLiteral(),null));
				}
				
				Set<SubEG> list2 = incompleteEGs.get(source.getLiteral());
				if (list2 != null){
					for (SubEG subgraph : list2){
						subEGs.add(new SubEG(subgraph.getEG(),type,source.getLiteral(),subgraph.getCycleTarget()));
					}
				}
			}
			
			subEGsSet.add(subEGs);
		}
		Set<SubEG> egSetArray[] = new HashSet[subEGsSet.size()];
		egSetArray = subEGsSet.toArray(egSetArray);
		constructEG(egSetArray.length-1,literal,assumption,as,eg,egSetArray,litVertex, false,null);
	}
	
	private void createNegativeEGs(ExtendedDependencyGraph edg, EDGVertex v, AnswerSet as, Set<String> assumption, Set<String> visitedLits, Set<EDGVertex> factNodeSet, Set<EDGVertex> unfoundedNodeSet, Set<EDGVertex> constraintNodeSet){
		ExplanationGraph eg = new ExplanationGraph();
		String literal = v.getLiteral();
		EGLiteralVertex litVertex = new EGLiteralVertex(Annotation.NEG,literal);
		eg.addVertex(litVertex);

		/* Calculate LCEs */
		Set<Set<String>> nnes = calculateNNEs(edg,literal);
		Set<Set<String>> lces = calculateLCEs(nnes);
		
		for (Set<String> lce : lces){
			Set<Set<SubEG>> subEGsSet = new HashSet<Set<SubEG>>();

			for (String lit : lce){
				Set<SubEG> subEGs = new HashSet<SubEG>();
				EGEdge.EdgeType type;
				if (edg.getAnswerSet().getFounded().contains(lit)) type = EGEdge.EdgeType.NEG;
				else type = EGEdge.EdgeType.POS;
			
				Set<ExplanationGraph> list = egLists.get(as).getEGList(lit,assumption);
			
				/* EGs for sourcenode not build yet*/
				if (list == null){
					visitedLits.add(literal);
				
					/* there is a cycle */
					if (visitedLits.contains(lit)){
						try {
							ExplanationGraph eg2 = eg.deepCopy();
							Annotation targetAnnotation;
							if (edg.getAnswerSet().getUnfounded().contains(lit)) targetAnnotation = Annotation.NEG;
							else targetAnnotation = Annotation.POS;
							EGLiteralVertex targetVertex = new EGLiteralVertex(targetAnnotation, lit);
							eg2.addEdge(litVertex, targetVertex , new EGEdge(litVertex, targetVertex, type));
							subEGs.add(new SubEG(eg2, type, lit,lit));	
						} catch (ClassNotFoundException	| IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					/* there is no cycle */
					else createEGsToLiteral(lit, assumption, factNodeSet, unfoundedNodeSet, constraintNodeSet, edg, as,visitedLits);
					
					list = egLists.get(as).getEGList(lit,assumption);
					if (list != null){
						for (ExplanationGraph subgraph : list){
							subEGs.add(new SubEG(subgraph,type,lit,null));
						}
					}
					
					Set<SubEG> list2 = incompleteEGs.get(lit);
					if (list2 != null){
						for (SubEG subgraph : list2){
							subEGs.add(new SubEG(subgraph.getEG(),type,lit,subgraph.getCycleTarget()));
						}
					}
				
					/* EGs for sourcenode already build */	
				} else{
					for (ExplanationGraph subgraph : list){
						subEGs.add(new SubEG(subgraph,type,lit,null));
					}
				
					Set<SubEG> list2 = incompleteEGs.get(lit);
					if (list2 != null){
						for (SubEG subgraph : list2){
							subEGs.add(new SubEG(subgraph.getEG(),type,lit,subgraph.getCycleTarget()));
						}
					}
				}
			
				subEGsSet.add(subEGs);
			}
			Set<SubEG> egSetArray[] = new HashSet[subEGsSet.size()];
			egSetArray = subEGsSet.toArray(egSetArray);
			constructEG(egSetArray.length-1,literal,assumption,as,eg,egSetArray,litVertex, false, null);
		}	
	}
	
	private Set<Set<String>> calculateNNEs(ExtendedDependencyGraph edg, String literal){
		Set<Set<String>> nnes = new HashSet<Set<String>>();
		for (EDGVertex node : edg.getNodeMap().get(literal)){
			Set<String> nne = new HashSet<String>();
			for (EDGEdge e : edg.getInEdges(node)){
				EDGVertex source = e.getSource();
				EDGEdge.EdgeType label = e.getLabel();
				if (source.getColor().equals(Color.GREEN) && label.equals(EDGEdge.EdgeType.NEG)){
					nne.add(source.getLiteral());
				}
				if (source.getColor().equals(Color.RED) && label.equals(EDGEdge.EdgeType.POS)){
					boolean allRed = true;
					for (EDGVertex v2 : edg.getNodeMap().get(source.getLiteral())){
						if (v2.getColor().equals(Color.GREEN)) allRed = false;
					}
					if (allRed) nne.add(source.getLiteral());
				}
			}
			nnes.add(nne);
		}
		return nnes;
	}
	
	private Set<Set<String>> calculateLCEs(Set<Set<String>> nnes){
		lces = new HashSet<Set<String>>(); 
		nneArray = new Set[nnes.size()];
		nneArray = nnes.toArray(nneArray);
		constructLCE(nneArray.length-1,new HashSet<String>());
		/* Remove subsets */
		HashSet<Set<String>> lcesCopy = new HashSet<Set<String>>(lces);
		for (Set<String> lce1 : lcesCopy){
			for (Set<String> lce2 : lcesCopy){
				if (!lce1.equals(lce2) && lce1.containsAll(lce2)) lces.remove(lce1);
			}
		}
		return lces;
	}

	
	
	
		
}
