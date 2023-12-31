package net.tapaal.gui.petrinet.undo;

import pipe.gui.petrinet.dataLayer.DataLayer;
import pipe.gui.petrinet.graphicElements.tapn.TimedInputArcComponent;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.gui.petrinet.undo.TAPNElementCommand;

public class AddTimedInputArcCommand extends TAPNElementCommand {
	private final TimedInputArcComponent timedArc;

	public AddTimedInputArcCommand(TimedInputArcComponent timedArc, TimedArcPetriNet tapn, DataLayer guiModel) {
		super(tapn, guiModel);
		this.timedArc = timedArc;
	}

	@Override
	public void undo() {
		timedArc.underlyingTimedInputArc().delete();

		guiModel.removePetriNetObject(timedArc);
	}

	@Override
	public void redo() {
		guiModel.addPetriNetObject(timedArc);

		tapn.add(timedArc.underlyingTimedInputArc());
	}
}
