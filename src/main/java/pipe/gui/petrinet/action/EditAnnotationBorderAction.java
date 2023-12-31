/*
 * Created on 07-Mar-2004
 * Author is Michael Camacho
 */
package pipe.gui.petrinet.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.AnnotationNote;

public class EditAnnotationBorderAction extends AbstractAction {

	private final AnnotationNote selected;

	public EditAnnotationBorderAction(AnnotationNote component) {
		selected = component;
	}

	/** Action for editing the text in an AnnotationNote */
	public void actionPerformed(ActionEvent e) {
		TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(
				selected.showBorder(!selected.isShowingBorder()));
	}

}
