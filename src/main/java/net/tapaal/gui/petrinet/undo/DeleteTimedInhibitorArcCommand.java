package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedInhibitorArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class DeleteTimedInhibitorArcCommand extends TAPNElementCommand {
	private final TimedInhibitorArcComponent inhibitorArc;

	public DeleteTimedInhibitorArcCommand(TimedInhibitorArcComponent inhibitorArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.inhibitorArc = inhibitorArc;
	}

	@Override
	public void redo() {
		inhibitorArc.underlyingTimedInhibitorArc().delete();

		guiModel.removePetriNetObject(inhibitorArc);
	}

	@Override
	public void undo() {
	    inhibitorArc.deselect();
		guiModel.addPetriNetObject(inhibitorArc);
		tapn.add(inhibitorArc.underlyingTimedInhibitorArc());
	}
}
