package net.tapaal.gui.undo;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import pipe.gui.petrinet.PetriNetTab;
import net.tapaal.gui.editor.TemplateExplorer;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import pipe.dataLayer.Template;

public class SortTemplatesCommand extends Command{
	TimedArcPetriNet[] oldOrder;
	final PetriNetTab tabContent;
	final TemplateExplorer templateExplorer;
	final JList<Template> templateList;
	final DefaultListModel<Template> listModel;
	
	public SortTemplatesCommand(PetriNetTab tabContent, TemplateExplorer templateExplorer, JList<Template> templateList, DefaultListModel<Template> listModel) {
		this.templateList = templateList;
		this.tabContent = tabContent;
		this.templateExplorer = templateExplorer;
		this.listModel = listModel;
	}
	
	@Override
	public void undo() {
		Template selectedValue = templateList.getSelectedValue();
		tabContent.undoSort(oldOrder);
		templateExplorer.updateTemplateList();
		templateList.setSelectedValue(selectedValue, true);
	}

	@Override
	public void redo() {
		Template selectedValue = templateList.getSelectedValue();
		oldOrder = tabContent.sortTemplates();
		templateExplorer.updateTemplateList();
		templateList.setSelectedValue(selectedValue, true);
	}
}
