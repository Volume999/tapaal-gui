/*
 * EditNoteAction.java
 */
package pipe.gui.petrinet.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import pipe.gui.petrinet.graphicElements.Note;

public class EditNoteAction extends AbstractAction {

	private final Note selected;

	public EditNoteAction(Note component) {
		selected = component;
	}

	/** Action for editing the text in a Note */
	public void actionPerformed(ActionEvent e) {
		selected.enableEditMode();
	}

}
