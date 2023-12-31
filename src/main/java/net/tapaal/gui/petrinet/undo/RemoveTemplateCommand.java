package net.tapaal.gui.petrinet.undo;

import java.util.Collection;

import net.tapaal.gui.petrinet.verification.TAPNQuery;
import net.tapaal.gui.petrinet.Template;
import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.petrinet.editor.TemplateExplorer;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedPlace;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.util.Tuple;

public class RemoveTemplateCommand extends AddTemplateCommand {
	private final Collection<TAPNQuery> queriesToDelete;
	private final PetriNetTab tabContent;
	private final Collection<Tuple<TimedTransition, SharedTransition>> transitionsToUnshare;

	public RemoveTemplateCommand(
			PetriNetTab tabContent,
			TemplateExplorer templateExplorer, 
			Template template, 
			int listIndex, 
			Collection<TAPNQuery> queriesToDelete, 
			Collection<Tuple<TimedTransition, SharedTransition>> transitionsToUnshare
	) {
		super(templateExplorer, template, listIndex);
		this.tabContent = tabContent;
		this.queriesToDelete = queriesToDelete;
		this.transitionsToUnshare = transitionsToUnshare;
	}

	@Override
	public void redo() {
		super.undo(); // Just the opposite of adding a template
		for(TAPNQuery query : queriesToDelete) { tabContent.removeQuery(query);	}
		for(Tuple<TimedTransition, SharedTransition> tuple : transitionsToUnshare){ tuple.value1().unshare(); }
		
		// remove the places from the list of inclusion places
		for (TimedPlace p : template.model().places()) {
			if(p.isShared()) continue;
			for (TAPNQuery q : tabContent.queries()) {
				q.inclusionPlaces().removePlace(p);
			}
		}
	}

	@Override
	public void undo() {
		super.redo(); // Just the opposite of adding a template
		for(TAPNQuery query : queriesToDelete){ tabContent.addQuery(query); }
		for(Tuple<TimedTransition, SharedTransition> tuple : transitionsToUnshare){ tuple.value2().makeShared(tuple.value1()); }
	}
}
