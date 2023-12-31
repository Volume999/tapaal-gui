package pipe.gui.swingcomponents;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;

public class WidthAdjustingComboBox<T> extends JComboBox<T>{

	private static final long serialVersionUID = 1L;
	
	public WidthAdjustingComboBox(int maxNumberOfPlacesToShowAtOnce) {
		this.addPopupMenuListener(new PopupMenuListener() {

			public void popupMenuCanceled(PopupMenuEvent arg0) {}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
			
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				adjustWidthOfPopUpMenu();
			}
		});
		this.setMaximumRowCount(maxNumberOfPlacesToShowAtOnce);
	}
	
	private void adjustWidthOfPopUpMenu() {
		if (this.getItemCount() == 0) {
			return;
		}
		Object comp = this.getUI().getAccessibleChild(this, 0);
		if (!(comp instanceof JPopupMenu)) {
			return;
		}
		FontMetrics metrics = this.getFontMetrics(this.getFont()); 
		int maxWidth=0;
		for(int i=0;i<this.getItemCount();i++){
			if(this.getItemAt(i)==null)
				continue;
			int currentWidth=metrics.stringWidth(this.getItemAt(i).toString());
			if(maxWidth<currentWidth)
				maxWidth=currentWidth;
		}
		JPopupMenu popup = (JPopupMenu) comp;
		//the following is a hack to fix a compatibility issue with the PopUpMenu-class
		Component[] listOfComponents = popup.getComponents();
		JScrollPane scrollPane;
		for (Component element : listOfComponents) {
			if (element instanceof JScrollPane) {
				scrollPane = (JScrollPane) element;
				Dimension size = scrollPane.getPreferredSize();
				if (size.width < maxWidth+70) {
					size.width = maxWidth+70;
				}
				scrollPane.setPreferredSize(size);
				scrollPane.setMaximumSize(size);
				break;
			}
		}		
	}
}
