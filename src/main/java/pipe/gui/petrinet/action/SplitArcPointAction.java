/*
 * SplitArcPointAction.java
 *
 * Created on 21-Jun-2005
 */
package pipe.gui.petrinet.action;

import java.awt.event.ActionEvent;

import pipe.gui.TAPAALGUI;
import pipe.gui.petrinet.graphicElements.ArcPathPoint;

/**
 * @author Nadeem
 * 
 *         This class is used to split a point on an arc into two to allow the
 *         arc to be manipulated further.
 */
public class SplitArcPointAction extends javax.swing.AbstractAction {

	private final ArcPathPoint arcPathPoint;

	public SplitArcPointAction(ArcPathPoint _arcPathPoint) {
		arcPathPoint = _arcPathPoint;
	}

	public void actionPerformed(ActionEvent e) {
		TAPAALGUI.getCurrentTab().getUndoManager().addNewEdit(arcPathPoint.splitPoint());
	}

}
