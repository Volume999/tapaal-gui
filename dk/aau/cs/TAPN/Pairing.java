package dk.aau.cs.TAPN;

import dk.aau.cs.petrinet.Arc;
import dk.aau.cs.petrinet.TAPNPlace;

public class Pairing {
	public enum ArcType { NORMAL, TARC }

	private Arc input;
	private Arc output;
	private String interval;
	private ArcType arcType;
	
	public Pairing(Arc input, String interval, Arc output, ArcType arcType){
		this.input = input;
		this.interval = interval;
		this.output = output;
		this.arcType = arcType;
	}

	public TAPNPlace getInput() {
		return (TAPNPlace)input.getSource();
	}
	
	public TAPNPlace getOutput() {
		return (TAPNPlace)output.getTarget();
	}
	
	public ArcType getArcType(){
		return arcType;
	}
	
	public String getInterval(){
		return interval;
	}
	
	public Arc getInputArc(){
		return input;		
	}
}