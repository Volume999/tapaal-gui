package dk.aau.cs.translations.tapn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.aau.cs.TA.Edge;
import dk.aau.cs.TA.Location;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.StandardUPPAALQuery;
import dk.aau.cs.TA.TimedAutomaton;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.TCTL.visitors.BroadcastTranslationQueryVisitor;
import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.Degree2Converter;
import dk.aau.cs.petrinet.PetriNetUtil;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TAPNArc;
import dk.aau.cs.petrinet.TAPNInhibitorArc;
import dk.aau.cs.petrinet.TAPNPlace;
import dk.aau.cs.petrinet.TAPNQuery;
import dk.aau.cs.petrinet.TAPNTransition;
import dk.aau.cs.petrinet.TAPNTransportArc;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.Token;
import dk.aau.cs.petrinet.degree2converters.InhibDegree2Converter;
import dk.aau.cs.translations.ModelTranslator;
import dk.aau.cs.translations.Pairing;
import dk.aau.cs.translations.QueryTranslator;
import dk.aau.cs.translations.Pairing.ArcType;

public class Degree2BroadcastTranslation implements
ModelTranslator<TimedArcPetriNet, NTA>,
QueryTranslator<TAPNQuery, UPPAALQuery>{

	protected static final String QUERY_PATTERN = "([a-zA-Z][a-zA-Z0-9_]*) (==|<|<=|>=|>) ([0-9])*";
	private static final String PLOCK = "P_lock";
	private static final String P_CAPACITY = "P_capacity";
	private static final String TOKEN_TEMPLATE_NAME = "Token";
	private static final String CONTROL_TEMPLATE_NAME = "Control";
	private static final String ID_TYPE = "id_t";
	private static final String ID_PARAMETER_NAME = "id";
	protected static final String CLOCK_NAME = "x";
	private static final String COUNTER_NAME = "count%1$d";
	private static final String COUNTER_UPDATE = "%1$s%2$s";
	private static final String TEST_CHANNEL = "%1$s_test%2$s";
	private static final String INIT_CHANNEL = "c%1$d%2$s";
	protected static final String T_I_IN_FORMAT = "%1$s_%2$d_in";
	protected static final String T_MAX_FORMAT = "%1$s_%2$d";
	protected static final String P_T_IN_FORMAT = "P_" + T_I_IN_FORMAT;
	protected static final String LOCK_BOOL = "lock";

	private Hashtable<String, Location> namesToLocations = new Hashtable<String, Location>();
	private Hashtable<Arc, String> arcsToCounters = new Hashtable<Arc, String>();
	private Hashtable<String, Hashtable<String, String>> arcGuards = new Hashtable<String, Hashtable<String,String>>();
	private int numberOfInitChannels = 0;
	protected int extraTokens = 0;
	private int largestPresetSize = 0;
	private boolean useSymmetry;

	public Degree2BroadcastTranslation(int extraTokens, boolean useSymmetry) {
		this.extraTokens = extraTokens;
		this.useSymmetry = useSymmetry;
	}

	public NTA transformModel(TimedArcPetriNet model) throws Exception {
		arcsToCounters.clear();
		arcGuards.clear();
		clearLocationMappings();
		numberOfInitChannels = 0;
		largestPresetSize = 0;

		TimedArcPetriNet degree2Net = getDegree2Converter().transform((TAPN)model);

		NTA nta = new NTA();
		if(useSymmetry){
			nta.addTimedAutomaton(createTokenAutomaton(degree2Net, model));
		}else{
			int j = 0;
			for(Token token : degree2Net.getTokens()){
				if(!token.place().getName().equals(PLOCK)){
					clearLocationMappings();
					arcsToCounters.clear();
					TimedAutomaton ta = createTokenAutomaton(degree2Net, model);
					ta.setName(TOKEN_TEMPLATE_NAME + j);
					ta.setInitLocation(getLocationByName(token.place().getName()));
					nta.addTimedAutomaton(ta);
					j++;
				}
			}

			for(int i = 0; i < extraTokens; i++){
				clearLocationMappings();
				arcsToCounters.clear();
				TimedAutomaton tokenTemplate = createTokenAutomaton(degree2Net, model);
				tokenTemplate.setInitLocation(getLocationByName(P_CAPACITY));
				nta.addTimedAutomaton(tokenTemplate);
				tokenTemplate.setName(TOKEN_TEMPLATE_NAME + String.valueOf(degree2Net.getNumberOfTokens()-1+i));
			}
		}

		nta.addTimedAutomaton(createControlAutomaton(degree2Net, model));
		nta.setSystemDeclarations(createSystemDeclaration(degree2Net.getNumberOfTokens()));
		nta.setGlobalDeclarations(createGlobalDeclarations(degree2Net, model));

		return nta;
	}

	protected Degree2Converter getDegree2Converter() {
		return new InhibDegree2Converter();
	}

	private String createSystemDeclaration(int tokensInModel) {
		if(useSymmetry){
			return "system " + CONTROL_TEMPLATE_NAME + "," + TOKEN_TEMPLATE_NAME + ";";
		}else{
			StringBuilder builder = new StringBuilder("system ");
			builder.append(CONTROL_TEMPLATE_NAME);

			for(int i = 0; i < extraTokens + tokensInModel - 1; i++)
			{
				builder.append(", ");
				builder.append(TOKEN_TEMPLATE_NAME);
				builder.append(i);
			}
			builder.append(";");

			return builder.toString();
		}
	}

	protected String createGlobalDeclarations(TimedArcPetriNet degree2Net, TimedArcPetriNet originalModel) {
		StringBuilder builder = new StringBuilder();

		if(useSymmetry){
			builder.append("const int N = ");
			builder.append(degree2Net.getTokens().size() + extraTokens - 1);
			builder.append(";\n");
			builder.append("typedef ");
			builder.append("scalar[N] ");
			builder.append(ID_TYPE);
			builder.append(";\n");

			for(int i = 0; i < numberOfInitChannels; i++){
				builder.append("chan ");
				builder.append(String.format(INIT_CHANNEL, i,""));
				builder.append(";\n");
			}
		}


		for(TAPNTransition t : degree2Net.getTransitions()){
			if(t.getPreset().size() > 1){
				builder.append("chan ");
				builder.append(t.getName());
				builder.append(";\n");
			}
		}

		for(TAPNTransition t : originalModel.getTransitions()){
			if(!t.isDegree2() || t.hasInhibitorArcs()){
				builder.append("broadcast chan ");
				builder.append(String.format(TEST_CHANNEL, t.getName(),""));
				builder.append(";\n");
			}
		}		

		for(int i = 0; i < largestPresetSize; i++){
			builder.append("bool ");
			builder.append(String.format(COUNTER_NAME, i));
			builder.append(";\n");
		}

		builder.append("bool ");
		builder.append(LOCK_BOOL);
		builder.append("= false;\n");

		return builder.toString();
	}

	protected String convertInvariant(TAPNPlace place) {
		String inv = "";
		String invariant = place.getInvariant();
		if(!invariant.equals("<inf")){
			inv = CLOCK_NAME + " " + invariant;
		}

		return inv;
	}

	protected Location getLocationByName(String name){
		return namesToLocations.get(name);
	}

	protected void addLocationMapping(String name, Location location){
		namesToLocations.put(name, location);
	}

	protected void clearLocationMappings(){
		namesToLocations.clear();
	}

	private TimedAutomaton createControlAutomaton(TimedArcPetriNet degree2Net,
			TimedArcPetriNet model) {
		TimedAutomaton control = new TimedAutomaton();
		createInitialLocationsForControlAutomaton(degree2Net, control);
		createEdgesForControlAutomaton(degree2Net, model, control);
		control.setName(CONTROL_TEMPLATE_NAME);
		if(useSymmetry){
			Location initial = createInitializationTransitionsForControlAutomaton(degree2Net,control);
			control.setInitLocation(initial);
		}else{
			control.setInitLocation(getLocationByName(PLOCK));
		}

		return control;
	}

	private Location createInitializationTransitionsForControlAutomaton(
			TimedArcPetriNet degree2Net, TimedAutomaton control) {
		Location first = new Location("","");
		first.setCommitted(true);
		control.addLocation(first);
		Location prev = first;

		for(int i = 0; i < degree2Net.getNumberOfTokens()-2; i++){
			Location l = new Location("","");
			l.setCommitted(true);
			control.addLocation(l);

			Edge e = new Edge(prev,
					l,
					"",
					String.format(INIT_CHANNEL, i, "!"),
			"");
			control.addTransition(e);
			prev = l;
		}

		Edge e = new Edge(prev,
				getLocationByName(PLOCK),
				"",
				String.format(INIT_CHANNEL, degree2Net.getNumberOfTokens()-2,"!"),
		"");
		control.addTransition(e);
		return first;
	}

	private void createEdgesForControlAutomaton(TimedArcPetriNet degree2Net,
			TimedArcPetriNet originalModel,
			TimedAutomaton control) {
		for(TAPNTransition transition : degree2Net.getTransitions()){
			if(!transition.isFromOriginalNet()){
				Pairing pairing = createPairing(transition, false);

				if(!pairing.getInput().getName().equals(PLOCK)){
					Edge e = new Edge(getLocationByName(pairing.getInput().getName()),
							getLocationByName(pairing.getOutput().getName()),
							"",
							transition.getName() + "!",
					"");
					control.addTransition(e);
				}
			}
		}	

		for(TAPNTransition transition : originalModel.getTransitions()){
			if(!transition.isDegree2() || transition.hasInhibitorArcs()){
				Location ptest = new Location("",createInvariantForControl(transition));
				ptest.setCommitted(true);
				control.addLocation(ptest);

				Edge first = new Edge(getLocationByName(PLOCK),
						ptest,
						"",
						String.format(TEST_CHANNEL, transition.getName(), "!"),
				"");
				control.addTransition(first);

				if(transition.getPreset().size() != 1){
					Edge second = new Edge(ptest,
							getLocationByName(String.format(P_T_IN_FORMAT, transition.getName(), 1)),
							"",
							String.format(T_I_IN_FORMAT+"%3$s", transition.getName(), 1, "!"),
							createResetExpressionForControl(transition));
					control.addTransition(second);
				}else{
					Edge second = new Edge(ptest,
							getLocationByName(PLOCK),
							"",
							String.format(T_MAX_FORMAT+"%3$s", transition.getName(), 1, "!"),
							createResetExpressionForControl(transition));
					control.addTransition(second);
				}
			}
		}
	}

	private void createInitialLocationsForControlAutomaton(
			TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for(TAPNPlace place: degree2Net.getPlaces()){			
			if(isPartOfLockTemplate(place.getName())){
				Location l = new Location(place.getName(),"");

				if(!place.getName().equals(PLOCK)){
					l.setCommitted(true);
				}

				ta.addLocation(l);
				addLocationMapping(place.getName(), l);
			}
		}
	}

	private TimedAutomaton createTokenAutomaton(TimedArcPetriNet degree2Net,
			TimedArcPetriNet originalModel) throws Exception {
		TimedAutomaton token = new TimedAutomaton();
		createInitialLocationsForTokenAutomata(degree2Net, token);
		createEdgesForTokenAutomata(degree2Net, token);
		createTestingEdgesForTokenAutomata(originalModel, token);

		if(useSymmetry){
			createInitializationTransitionsForTokenAutomata(degree2Net, token);
			token.setName(TOKEN_TEMPLATE_NAME);
			token.setInitLocation(getLocationByName(P_CAPACITY));
			token.setParameters("const " + ID_TYPE + " " + ID_PARAMETER_NAME);
		}

		token.setDeclarations(createLocalDeclarations(originalModel));

		return token;
	}

	protected String createLocalDeclarations(TimedArcPetriNet model) {
		return "clock " + CLOCK_NAME + ";";
	}

	private void createInitializationTransitionsForTokenAutomata(TimedArcPetriNet degree2Net,
			TimedAutomaton ta) {
		int i = 0;
		for(Token token : degree2Net.getTokens()){
			if(!token.place().getName().equals(PLOCK) && !token.place().getName().equals(P_CAPACITY)){
				Edge e = new Edge(getLocationByName(P_CAPACITY),
						getLocationByName(token.place().getName()),
						"",
						String.format(INIT_CHANNEL, i, "?"),
						createUpdateExpressionForTokenInitialization(token));
				ta.addTransition(e);
				i++;
				numberOfInitChannels++;
			}
		}
	}

	private void createEdgesForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton token) {
		for(TAPNTransition transition : degree2Net.getTransitions()){
			if(transition.isFromOriginalNet()){
				if(transition.getPreset().size() == 1){
					Pairing pairing = createPairing(transition, true);
					String guard = createTransitionGuardWithLock(pairing.getInputArc(), pairing.getOutput(), pairing.getArcType().equals(ArcType.TARC));
					Edge e = new Edge(getLocationByName(pairing.getInput().getName()),
							getLocationByName(pairing.getOutput().getName()),
							guard,
							"",
							CreateResetExpressionIfNormalArc(pairing.getOutputArc()));
					token.addTransition(e);
					saveGuard(transition.getName(),pairing.getInput().getName(), guard);
				}else{
					List<Pairing> pairing = CreatePairing(transition);

					Pairing pair1 = pairing.get(0);
					String guard1 = createTransitionGuardWithLock(pair1.getInputArc(), pair1.getOutput(), pair1.getArcType().equals(ArcType.TARC));
					Edge e1 = new Edge(getLocationByName(pair1.getInput().getName()),
							getLocationByName(pair1.getOutput().getName()),
							guard1,
							transition.getName() + "?",
							CreateResetExpressionIfNormalArc(pair1.getOutputArc()));
					token.addTransition(e1);
					saveGuard(transition.getName(), pair1.getInput().getName(), guard1);

					Pairing pair2 = pairing.get(1);
					String guard2 = createTransitionGuardWithLock(pair2.getInputArc(), pair2.getOutput(), pair2.getArcType().equals(ArcType.TARC));
					Edge e2 = new Edge(getLocationByName(pair2.getInput().getName()),
							getLocationByName(pair2.getOutput().getName()),
							guard2,
							transition.getName() + "!",
							CreateResetExpressionIfNormalArc(pair2.getOutputArc()));
					token.addTransition(e2);
					saveGuard(transition.getName(), pair2.getInput().getName(), guard2);
				}
			}else{
				Pairing pairing = createPairing(transition, true);
				String guard = createTransitionGuard(pairing.getInputArc(), pairing.getOutput(), pairing.getArcType().equals(ArcType.TARC));
				Edge e = new Edge(getLocationByName(pairing.getInput().getName()),
						getLocationByName(pairing.getOutput().getName()),
						guard,
						transition.getName() + "?",
						CreateResetExpressionIfNormalArc(pairing.getOutputArc()));
				token.addTransition(e);
				saveGuard(transition.getName(), pairing.getInput().getName(), guard);
			}
		}		
	}

	private void saveGuard(String transitionName, String inputPlaceName,
			String guard) {
		String originalTransitionName = getOriginalTransitionName(transitionName);
		if(originalTransitionName != null && !originalTransitionName.isEmpty()){
			if(!arcGuards.containsKey(originalTransitionName)){
				arcGuards.put(originalTransitionName, new Hashtable<String, String>());
			}

			arcGuards.get(originalTransitionName).put(inputPlaceName, guard);
		}
	}

	private String getOriginalTransitionName(String name) {
		Pattern pattern = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*)_([0-9]*(?:_in)?)$");

		Matcher matcher = pattern.matcher(name);
		if(!matcher.find()){
			return null;
		}else{
			return matcher.group(1);
		}
	}

	private List<Pairing> CreatePairing(TAPNTransition t) {
		List<Pairing> pairing = new ArrayList<Pairing>();
		HashSet<Arc> usedPostSetArcs = new HashSet<Arc>();

		for(Arc inputArc : t.getPreset()){
			for(Arc outputArc : t.getPostset()){
				if(!usedPostSetArcs.contains(outputArc)){
					if(inputArc instanceof TAPNTransportArc && outputArc instanceof TAPNTransportArc && inputArc == outputArc){
						Pairing p = new Pairing((TAPNArc)inputArc,
								((TAPNArc)inputArc).getGuard(),
								outputArc,
								ArcType.TARC);
						pairing.add(p);

						usedPostSetArcs.add(outputArc);
						break;
					}else if(!(inputArc instanceof TAPNTransportArc) && !(outputArc instanceof TAPNTransportArc)){
						Pairing p = new Pairing((TAPNArc)inputArc,
								((TAPNArc)inputArc).getGuard(),
								outputArc,
								ArcType.NORMAL);
						pairing.add(p);

						usedPostSetArcs.add(outputArc);
						break;
					}
				}
			}
		}

		return pairing;
	}

	private Pairing createPairing(TAPNTransition transition, boolean isForTokenAutomaton) {
		Arc source = null;
		Arc dest = null;

		for(Arc presetArc : transition.getPreset()){
			if(isForTokenAutomaton && !isPartOfLockTemplate(presetArc.getSource().getName()))
			{
				source = presetArc;
				break;
			}else if(!isForTokenAutomaton && isPartOfLockTemplate(presetArc.getSource().getName())){
				source = presetArc;
				break;
			}
		}

		for(Arc postsetArc : transition.getPostset()){
			if(isForTokenAutomaton && !isPartOfLockTemplate(postsetArc.getTarget().getName()))
			{
				dest = postsetArc;
				break;
			}else if(!isForTokenAutomaton && isPartOfLockTemplate(postsetArc.getTarget().getName())){
				dest = postsetArc;
				break;
			}
		}

		boolean isTransportArc = source instanceof TAPNTransportArc;
		ArcType type = isTransportArc ? ArcType.TARC : ArcType.NORMAL;

		return new Pairing((TAPNArc)source,((TAPNArc)source).getGuard(), dest, type);
	}

	private void createTestingEdgesForTokenAutomata(TimedArcPetriNet originalModel, TimedAutomaton ta) throws Exception {

		for(TAPNTransition transition : originalModel.getTransitions()){
			int size = transition.getPreset().size() + transition.getInhibitorArcs().size();
			if(size > largestPresetSize) largestPresetSize = size;

			if(!transition.isDegree2() || transition.hasInhibitorArcs()){
				int i = 0;
				for(Arc arc : transition.getPreset()){
					String source = arc.getSource().getName();
					String counter = String.format(COUNTER_NAME, i);
					arcsToCounters.put(arc, counter);

					String guard = arcGuards.get(transition.getName()).get(source);
					if(guard == null && source.equals(P_CAPACITY)){
						guard = "";
					}else if(guard == null){
						if(arc instanceof TAPNInhibitorArc){
							guard = createTransitionGuard((TAPNInhibitorArc)arc, null, false);
						}
						throw new Exception("guard was not precomputed");
					}
					
					//String guard = createGuardForTestingEdge(transition, arc);
					Edge e = new Edge(getLocationByName(source),
							getLocationByName(source),
							guard,
							String.format(TEST_CHANNEL, transition.getName(), "?"),
							String.format(COUNTER_UPDATE, counter, "= true"));
					ta.addTransition(e);		
					i++;
				}

				for(TAPNInhibitorArc arc : transition.getInhibitorArcs()){
					String source = arc.getSource().getName();
					String counter = String.format(COUNTER_NAME, i);
					arcsToCounters.put(arc, counter);

					Edge e = new Edge(getLocationByName(source),
							getLocationByName(source),
							createTransitionGuard(arc, null, false),
							String.format(TEST_CHANNEL, arc.getTarget().getName(), "?"),
							String.format(COUNTER_UPDATE, counter, "=true"));
					ta.addTransition(e);
					i++;
				}
			}
		}
	}

	private void createInitialLocationsForTokenAutomata(TimedArcPetriNet degree2Net, TimedAutomaton ta) {
		for(TAPNPlace place: degree2Net.getPlaces()){			
			if(!isPartOfLockTemplate(place.getName())){
				Location l = new Location(place.getName(), convertInvariant(place));
				ta.addLocation(l);
				addLocationMapping(place.getName(), l);
			}
		}
	}

	private String createInvariantForControl(TAPNTransition transition) {
		return createBooleanExpressionForControl(transition, "==", "==");
	}

	private String createBooleanExpressionForControl(TAPNTransition transition, String comparison, String inhibComparison)
	{
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for(Arc presetArc : transition.getPreset()){
			if(!first){
				builder.append(" && ");
			}

			String counter = arcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(comparison);
			builder.append("true");
			first = false;
		}

		for(TAPNInhibitorArc inhib : transition.getInhibitorArcs()){
			if(!first){
				builder.append(" && ");
			}

			String counter = arcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(inhibComparison);
			builder.append("false");
		}

		return builder.toString();
	}

	private String createResetExpressionForControl(TAPNTransition transition) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for(Arc presetArc : transition.getPreset()){
			if(!first){
				builder.append(", ");
			}

			String counter = arcsToCounters.get(presetArc);
			builder.append(counter);
			builder.append(":=false");
			first = false;
		}

		for(TAPNInhibitorArc inhib : transition.getInhibitorArcs()){
			if(!first){
				builder.append(", ");
			}

			String counter = arcsToCounters.get(inhib);
			builder.append(counter);
			builder.append(":=false");
		}

		return builder.toString();
	}

	protected String createTransitionGuard(TAPNArc arc, TAPNPlace target, boolean isTransportArc) {
		String newGuard = PetriNetUtil.createGuard(arc.getGuard(), target, isTransportArc);
		return createTransitionGuard(newGuard);
	}

	protected String createTransitionGuardWithLock(TAPNArc arc, TAPNPlace target, boolean isTransportArc){
		String guard = createTransitionGuard(arc, target, isTransportArc);

		if(guard == null || guard.isEmpty()){
			guard = LOCK_BOOL + " == 0";
		}else{
			guard += " && " + LOCK_BOOL + " == 0";
		}

		return guard;
	}

	protected String createTransitionGuard(String guard) {
		if(guard.equals("false")) return guard;
		if(guard.equals("[0,inf)")) return "";

		String[] splitGuard = guard.substring(1, guard.length()-1).split(",");
		char firstDelim = guard.charAt(0);
		char secondDelim = guard.charAt(guard.length()-1);

		StringBuilder builder = new StringBuilder();
		builder.append(CLOCK_NAME);
		builder.append(" ");

		if(firstDelim == '('){
			builder.append(">");
		} else {
			builder.append(">=");
		}

		builder.append(splitGuard[0]);

		if(!splitGuard[1].equals("inf")){
			builder.append(" && ");
			builder.append(CLOCK_NAME);
			builder.append(" ");

			if(secondDelim == ')'){
				builder.append("<");
			}else {
				builder.append("<=");
			}
			builder.append(splitGuard[1]);
		}

		return builder.toString();
	}	

	protected String CreateResetExpressionIfNormalArc(Arc arc) {
		if(!(arc instanceof TAPNTransportArc)){
			return String.format("%1s := 0", CLOCK_NAME);
		}else{
			return "";
		}
	}

	private boolean isPartOfLockTemplate(String name){
		Pattern pattern = Pattern.compile("^(P_(?:[a-zA-Z][a-zA-Z0-9_]*)_(?:(?:[0-9]*_(?:in|out)|check))|P_lock|P_deadlock)$");

		Matcher matcher = pattern.matcher(name);
		return matcher.find();
	}

	protected String createUpdateExpressionForTokenInitialization(Token token) {
		return "";
	}

	public UPPAALQuery transformQuery(TAPNQuery tapnQuery) throws Exception {
		BroadcastTranslationQueryVisitor visitor = new BroadcastTranslationQueryVisitor(useSymmetry,tapnQuery.getTotalTokens());
		
		return new StandardUPPAALQuery(visitor.getUppaalQueryFor(tapnQuery));
	}
}