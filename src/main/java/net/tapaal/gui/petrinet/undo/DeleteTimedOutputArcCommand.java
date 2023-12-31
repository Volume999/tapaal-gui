package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedOutputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class DeleteTimedOutputArcCommand extends TAPNElementCommand {
	private final TimedOutputArcComponent arc;

	public DeleteTimedOutputArcCommand(TimedOutputArcComponent arc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.arc = arc;
	}

	@Override
	public void redo() {
		arc.underlyingArc().delete();

		guiModel.removePetriNetObject(arc);
	}

	@Override
	public void undo() {
	    arc.deselect();
		guiModel.addPetriNetObject(arc);
		tapn.add(arc.underlyingArc());
	}

}
