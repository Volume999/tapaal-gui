package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.CaretListener;

import pipe.dataLayer.TimedTransitionComponent;
import dk.aau.cs.gui.Context;
import dk.aau.cs.gui.undo.MakeTransitionSharedCommand;
import dk.aau.cs.gui.undo.RenameTimedTransitionCommand;
import dk.aau.cs.gui.undo.UnshareTransitionCommand;
import dk.aau.cs.model.tapn.SharedTransition;
import dk.aau.cs.model.tapn.TimedInhibitorArc;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TimedTransition;
import dk.aau.cs.model.tapn.TransportArc;
import dk.aau.cs.util.RequireException;

public class TAPNTransitionEditor extends javax.swing.JPanel {
	private static final long serialVersionUID = 1744651413834659994L;
	private TimedTransitionComponent transition;
	private JRootPane rootPane;
	private Context context;

	public TAPNTransitionEditor(JRootPane _rootPane, TimedTransitionComponent _transition, Context context) {
		rootPane = _rootPane;
		transition = _transition;
		this.context = context;
		initComponents();

		rootPane.setDefaultButton(okButton);
	}

	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		transitionEditorPanel = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel();
		nameTextField = new javax.swing.JTextField();
		nameTextField.setPreferredSize(new Dimension(120,27));
		rotationLabel = new javax.swing.JLabel();
		rotationComboBox = new javax.swing.JComboBox();
		buttonPanel = new javax.swing.JPanel();
		cancelButton = new javax.swing.JButton();
		okButton = new javax.swing.JButton();
		sharedCheckBox = new JCheckBox("Shared");
		sharedTransitionsComboBox = new JComboBox(context.network().sharedTransitions().toArray());
		sharedTransitionsComboBox.setPreferredSize(new Dimension(120,27));
		setLayout(new java.awt.GridBagLayout());

		transitionEditorPanel.setLayout(new java.awt.GridBagLayout());
		transitionEditorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Transition Editor"));

		sharedCheckBox.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				JCheckBox box = (JCheckBox)arg0.getSource();
				if(box.isSelected()){
					switchToNameDropDown();
				}else{
					switchToNameTextField();
				}
			}		
		});
		sharedCheckBox.setEnabled(context.network().numberOfSharedTransitions() > 0 && !hasArcsToSharedPlaces(transition.underlyingTransition()));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(sharedCheckBox, gridBagConstraints);

		nameLabel.setText("Name:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(nameLabel, gridBagConstraints);

		nameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(java.awt.event.FocusEvent evt) {
				nameTextFieldFocusGained(evt);
			}

			@Override
			public void focusLost(java.awt.event.FocusEvent evt) {
				nameTextFieldFocusLost(evt);
			}
		});

		rotationLabel.setText("Rotate:");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(rotationLabel, gridBagConstraints);

		rotationComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {
				"0\u00B0", "+45\u00B0", "+90\u00B0", "-45\u00B0" }));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(rotationComboBox, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		add(transitionEditorPanel, gridBagConstraints);

		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonHandler(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		buttonPanel.add(okButton, gridBagConstraints);

		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonHandler(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		buttonPanel.add(cancelButton, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 8, 3);
		add(buttonPanel, gridBagConstraints);

		nameTextField.setText(transition.getName());
		if(transition.underlyingTransition().isShared()){
			switchToNameDropDown();
			sharedCheckBox.setSelected(true);
			sharedTransitionsComboBox.setSelectedItem(transition.underlyingTransition().sharedTransition());
		}else{
			switchToNameTextField();
		}
	}
	
	private boolean hasArcsToSharedPlaces(TimedTransition underlyingTransition) {
		for(TimedInputArc arc : context.activeModel().inputArcs()){
			if(arc.destination().equals(underlyingTransition) && arc.source().isShared()) return true;
		}
		
		for(TimedOutputArc arc : context.activeModel().outputArcs()){
			if(arc.source().equals(underlyingTransition) && arc.destination().isShared()) return true;
		}
		
		for(TransportArc arc : context.activeModel().transportArcs()){
			if(arc.transition().equals(underlyingTransition) && arc.source().isShared()) return true;
			if(arc.transition().equals(underlyingTransition) && arc.destination().isShared()) return true;
		}
		
		for(TimedInhibitorArc arc : context.activeModel().inhibitorArcs()){
			if(arc.destination().equals(underlyingTransition) && arc.source().isShared()) return true;
		}
		
		return false;
	}

	protected void switchToNameTextField() {
		transitionEditorPanel.remove(sharedTransitionsComboBox);
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(nameTextField, gbc);	
		transitionEditorPanel.validate();
		transitionEditorPanel.repaint();
	}

	protected void switchToNameDropDown() {
		transitionEditorPanel.remove(nameTextField);
		GridBagConstraints gbc = new java.awt.GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gbc.insets = new java.awt.Insets(3, 3, 3, 3);
		transitionEditorPanel.add(sharedTransitionsComboBox, gbc);		
		transitionEditorPanel.validate();
		transitionEditorPanel.repaint();
	}

	private void nameTextFieldFocusLost(java.awt.event.FocusEvent evt) {
		focusLost(nameTextField);
	}
	
	private void nameTextFieldFocusGained(java.awt.event.FocusEvent evt) {
		focusGained(nameTextField);
	}

	private void focusGained(javax.swing.JTextField textField) {
		textField.setCaretPosition(0);
		textField.moveCaretPosition(textField.getText().length());
	}

	private void focusLost(javax.swing.JTextField textField) {
		textField.setCaretPosition(0);
	}

	CaretListener caretListener = new javax.swing.event.CaretListener() {
		public void caretUpdate(javax.swing.event.CaretEvent evt) {
			JTextField textField = (JTextField) evt.getSource();
			textField.setBackground(new Color(255, 255, 255));
			// textField.removeChangeListener(this);
		}
	};

	private void okButtonHandler(java.awt.event.ActionEvent evt) {
		String newName = nameTextField.getText();
			
		context.undoManager().newEdit(); // new "transaction""
		boolean wasShared = transition.underlyingTransition().isShared() && !sharedCheckBox.isSelected();
		if(transition.underlyingTransition().isShared()){
			context.undoManager().addEdit(new UnshareTransitionCommand(transition.underlyingTransition().sharedTransition(), transition.underlyingTransition()));
			transition.underlyingTransition().unshare();
		}
		
		if(sharedCheckBox.isSelected()){	
			SharedTransition selectedTransition = (SharedTransition)sharedTransitionsComboBox.getSelectedItem();
			context.undoManager().addEdit(new MakeTransitionSharedCommand(selectedTransition, transition.underlyingTransition()));
			try{
				selectedTransition.makeShared(transition.underlyingTransition());
			}catch(RequireException e){
				context.undoManager().undo();
				JOptionPane.showMessageDialog(this,"Another transition in the same component is already shared under that name", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}else{			
			if(transition.underlyingTransition().model().isNameUsed(newName) && (wasShared || !transition.underlyingTransition().name().equals(newName))){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this,
						"The specified name is already used by another place or transition.",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			String oldName = transition.underlyingTransition().name();
			try{ // set name
				transition.underlyingTransition().setName(newName);
				context.undoManager().addEdit(new RenameTimedTransitionCommand(transition.underlyingTransition(), oldName, newName));
			}catch(RequireException e){
				context.undoManager().undo(); 
				JOptionPane.showMessageDialog(this,
						"Acceptable names for transitions are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			context.nameGenerator().updateIndices(transition.underlyingTransition().model(), newName);
		}

		Integer rotationIndex = rotationComboBox.getSelectedIndex();
		if (rotationIndex > 0) {
			int angle = 0;
			switch (rotationIndex) {
			case 1:
				angle = 45;
				break;
			case 2:
				angle = 90;
				break;
			case 3:
				angle = 135; // -45
				break;
			default:
				break;
			}
			if (angle != 0) {
				context.undoManager().addEdit(transition.rotate(angle));
			}
		}
		exit();
	}

	private void exit() {
		rootPane.getParent().setVisible(false);
	}

	private void cancelButtonHandler(java.awt.event.ActionEvent evt) {
		exit();
	}

	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JComboBox rotationComboBox;
	private javax.swing.JLabel rotationLabel;
	private javax.swing.JPanel transitionEditorPanel;
	private javax.swing.JCheckBox sharedCheckBox;
	private javax.swing.JComboBox sharedTransitionsComboBox;

}