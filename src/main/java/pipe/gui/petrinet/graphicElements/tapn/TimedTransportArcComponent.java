package pipe.gui.petrinet.graphicElements.tapn;

import java.awt.Polygon;
import java.util.Hashtable;
import java.util.List;

import net.tapaal.gui.petrinet.TAPNLens;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.Expressions.ArcExpression;
import pipe.gui.TAPAALGUI;
import pipe.gui.Constants;
import pipe.gui.petrinet.graphicElements.PlaceTransitionObject;
import pipe.gui.petrinet.undo.ArcTimeIntervalEditCommand;
import net.tapaal.gui.petrinet.undo.Command;
import dk.aau.cs.model.tapn.ConstantBound;
import dk.aau.cs.model.tapn.ConstantWeight;
import dk.aau.cs.model.tapn.TimeInterval;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.model.tapn.Weight;

public class TimedTransportArcComponent extends TimedInputArcComponent {

	private int group;
	private final boolean isInPreSet;
	private TimedTransportArcComponent connectedTo;
	private TransportArc underlyingTransportArc;

	public TimedTransportArcComponent(PlaceTransitionObject newSource, int groupNr, boolean isInPreSet) {
		super(new TimedOutputArcComponent(newSource));
		this.isInPreSet = isInPreSet;
		setGroup(groupNr);
		// hack to reprint the label of the arc
		updateLabel(true);
		isPrototype = true;
    }

	public TimedTransportArcComponent(TimedInputArcComponent timedArc, int group, boolean isInPreSet) {
		super(timedArc);
		this.isInPreSet = isInPreSet;
		this.setGroup(group);
		// hack to reprint the label of the arc
		updateLabel(true);
	}

	public TimedTransportArcComponent(TimedPlaceComponent p, TimedTransitionComponent t, TransportArc model, int group, TAPNLens lens){
	    super(p);
	    setTarget(t);
	    this.isInPreSet = true;
	    this.setGroup(group);
	    setUnderlyingArc(model);
        this.lens = lens;
	    updateLabel(true);
	    sealArc();
    }

    public TimedTransportArcComponent(TimedTransitionComponent t, TimedPlaceComponent p, TransportArc model, int group, TAPNLens lens){
        super(t);
        setTarget(p);
        this.isInPreSet = false;
        this.setGroup(group);
        this.lens = lens;
        setUnderlyingArc(model);

        updateLabel(true);
        sealArc();
    }

	public void setUnderlyingArc(TransportArc arc) {
		underlyingTransportArc = arc; // must explicitly set underlying arc
											// on connected to
		updateLabel(true);
	}

	public TransportArc underlyingTransportArc() {
		return underlyingTransportArc;
	}

	@Override
	protected void setHead() {
		setHead(new Polygon(new int[] { 0, 5, 0, -5 }, new int[] { 0, -11, -18, -11 }, 4), true);
    }

	public void setGroupNr(int groupNr) {
		setGroup(groupNr);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		repaint();

	}

	public void setInputExpression(ArcExpression expression) {
	    underlyingTransportArc().setInputExpression(expression);
    }

    public void setOutputExpression(ArcExpression expression) {
	    underlyingTransportArc().setOutputExpression(expression);
    }

	public int getGroupNr() {
		return getGroup();
	}

	@Override
	public void updateLabel(boolean displayConstantNames) {
        if (isInPreSet && underlyingTransportArc != null) {
            if (TAPAALGUI.getApp() != null && TAPAALGUI.getAppGui().showZeroToInfinityIntervals()) {
                getNameLabel().setText(underlyingTransportArc.interval().toString(displayConstantNames) + " : " + getGroup());

                if (getNameLabel().getText().contains("inf")) {
                    String intervalStringWithInfSymbol = getNameLabel().getText().replace("inf", Character.toString('\u221e'));
                    getNameLabel().setText(intervalStringWithInfSymbol);
                }
            } else {
                if (underlyingTransportArc.interval().toString(displayConstantNames).equals("[0,inf)")) {
                    getNameLabel().setText(" : " + getGroup());
                } else {
                    getNameLabel().setText(underlyingTransportArc.interval().toString(displayConstantNames) + " : " + getGroup());
                }
            }

            // Handle constant highlighting
            boolean focusedConstant = false;
            boolean isvisible = true;
            if (underlyingTransportArc.interval().lowerBound() instanceof ConstantBound) {
                if (((ConstantBound) underlyingTransportArc.interval().lowerBound()).constant().hasFocus()) {
                    focusedConstant = true;
                }
                if (!((ConstantBound) underlyingTransportArc.interval().lowerBound()).constant().getVisible()) {
                    focusedConstant = false;
                }
            }
            if (underlyingTransportArc.interval().upperBound() instanceof ConstantBound) {
                if (((ConstantBound) underlyingTransportArc.interval().upperBound()).constant().getVisible()) {
                    focusedConstant = true;
                }
                if (!((ConstantBound) underlyingTransportArc.interval().upperBound()).constant().getVisible()) {
                    isvisible = false;
                }
            }
            if (getWeight() instanceof ConstantWeight) {
                if (((ConstantWeight) getWeight()).constant().hasFocus()) {
                    focusedConstant = true;
                }
                if (!((ConstantWeight) getWeight()).constant().hasFocus()) {
                    isvisible = false;
                }
            }
            if (focusedConstant) {
                getNameLabel().setForeground(Constants.SELECTION_TEXT_COLOUR);
            } else {
                getNameLabel().setForeground(Constants.ELEMENT_TEXT_COLOUR);
            }
            pnName.setVisible(isvisible);
        } else if (!isInPreSet) {
            getNameLabel().setText(" : " + getGroup());
        } else {
            getNameLabel().setText("");
        }
        if (underlyingTransportArc != null) {
            getNameLabel().setText(getWeight().toString(displayConstantNames) + " " + getNameLabel().getText());
        }
        if (isColored()) {
            String labelText = getNameLabel().getText() + "\n";
            if (isInPreSet() && underlyingTransportArc() != null) {
                if (underlyingTransportArc().getInputExpression() != null)
                    labelText += underlyingTransportArc().getInputExpression().toString() + "\n";

                List<ColoredTimeInterval> ctiList = underlyingTransportArc().getColorTimeIntervals();
                for (ColoredTimeInterval timeInterval : ctiList) {
                    if (timeInterval != null)
                        labelText += timeInterval + "\n";
                }
                getNameLabel().setText(labelText);
            }
            else if (!isInPreSet() && underlyingTransportArc() != null) {
                if (underlyingTransportArc().getOutputExpression() != null)
                    labelText += underlyingTransportArc().getOutputExpression().toString() + "\n";
                getNameLabel().setText(labelText);
            }
        }
		
		this.setLabelPosition();
	}

	public boolean isInPreSet() {
		return isInPreSet;
	}

	public TimedTransportArcComponent getConnectedTo() {
		return connectedTo;
	}

	public void setConnectedTo(TimedTransportArcComponent connectedTo) {
		this.connectedTo = connectedTo;
	}


	public void setGroup(int group) {
		this.group = group;
	}

	public int getGroup() {
		return group;
	}

	@Override
	public TimeInterval getGuard() {
		return underlyingTransportArc.interval();
	}
	
	@Override
	public boolean isUrgentTransition(){
		return underlyingTransportArc.transition().isUrgent();
	}

	@Override
	public Command setGuardAndWeight(TimeInterval guard, Weight weight) {

		TimeInterval oldTimeInterval = underlyingTransportArc.interval();
		underlyingTransportArc.setTimeInterval(guard);
		Weight oldWeight = getWeight();
		setWeight(weight);
		connectedTo.setWeight(weight);

		// hacks - I use the weight to display the TimeInterval
		updateLabel(true);
		connectedTo.updateLabel(true);
		
		repaint();

		return new ArcTimeIntervalEditCommand(this, oldTimeInterval,
				underlyingTransportArc.interval(), oldWeight, weight);
	}
	
	public TimedTransportArcComponent copy(TimedArcPetriNet tapn, Hashtable<PlaceTransitionObject, PlaceTransitionObject> oldToNewMapping) {
		TimedTransportArcComponent arc = new TimedTransportArcComponent(this, group, isInPreSet);
		arc.setSource(oldToNewMapping.get(this.getSource()));
		arc.setTarget(oldToNewMapping.get(this.getTarget()));
		
		arc.setUnderlyingArc(tapn.getTransportArcFromPlaceTransitionAndPlace(tapn.getPlaceByName(underlyingTransportArc.source().name()), 
																			 tapn.getTransitionByName(underlyingTransportArc.transition().name()), 
																			 tapn.getPlaceByName(underlyingTransportArc.destination().name())));

		
		return arc;
	}
	
	@Override
	public void setWeight(Weight weight){
		underlyingTransportArc.setWeight(weight);
		connectedTo.underlyingTransportArc.setWeight(weight);
	}
	
	@Override
	public Weight getWeight(){
		return underlyingTransportArc.getWeight();
	}
    @Override
    public List<ColoredTimeInterval> getCtiList(){
        return underlyingTransportArc.getColorTimeIntervals();
    }
    @Override
    public void setCtiList(List<ColoredTimeInterval> ctiList){
        underlyingTransportArc.setColorTimeIntervals(ctiList);
    }


}
