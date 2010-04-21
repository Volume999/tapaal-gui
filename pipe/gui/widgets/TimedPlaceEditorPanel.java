package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TimedPlace;
import pipe.dataLayer.colors.ColorSet;
import pipe.dataLayer.colors.ColoredTimeInvariant;
import pipe.dataLayer.colors.ColoredTimedPlace;
import pipe.dataLayer.colors.ColoredToken;
import pipe.dataLayer.colors.IntOrConstant;
import pipe.dataLayer.colors.IntOrConstantRange;
import pipe.dataLayer.colors.IntervalBound;
import pipe.gui.CreateGui;
import pipe.gui.GuiView;
import pipe.gui.undo.UndoableEdit;

/**
 *
 * @author  pere
 */
public class TimedPlaceEditorPanel 
extends javax.swing.JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4163767112591119036L;
	TimedPlace place;
	Boolean attributesVisible;
	Integer marking;
	String name;
	MarkingParameter mParameter;
	DataLayer pnmlData;
	GuiView view;
	JRootPane rootPane;



	/**
	 * Creates new form PlaceEditor
	 */
	public TimedPlaceEditorPanel(JRootPane _rootPane, TimedPlace _place, 
			DataLayer _pnmlData, GuiView _view) {
		place = _place;
		pnmlData = _pnmlData;
		view = _view;
		attributesVisible = place.getAttributesVisible();
		marking = place.getCurrentMarking();
		name = place.getName();
		mParameter = place.getMarkingParameter();
		rootPane = _rootPane;

		initComponents();
		rootPane.setDefaultButton(okButton);

		MarkingParameter[] markings = pnmlData.getMarkingParameters();
		if (markings.length > 0) {
			markingComboBox.addItem("");
			for (int i = 0; i < markings.length; i++) {
				markingComboBox.addItem(markings[i]);
			}
		} else {
			markingComboBox.setEnabled(false);
		}  

		if (mParameter != null){
			for (int i = 1; i < markingComboBox.getItemCount(); i++) {
				if (mParameter == (MarkingParameter)markingComboBox.getItemAt(i)){
					markingComboBox.setSelectedIndex(i);
				}
			}
		}
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		placeEditorPanel = new javax.swing.JPanel();
		markingComboBox = new javax.swing.JComboBox();
		//      tokenPanel = new JPanel(new FlowLayout());

		setLayout(new java.awt.GridBagLayout());

		placeEditorPanel.setLayout(new java.awt.GridBagLayout());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 8, 5, 8);
		add(placeEditorPanel, gridBagConstraints);

		initBasicPropertiesPanel();
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		//gridBagConstraints.insets = new java.awt.Insets(3,3,3,3);
		placeEditorPanel.add(basicPropertiesPanel, gridBagConstraints);

		if(pnmlData.isUsingColors()){
			initColoredTimeInvariantPanel();
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			//gridBagConstraints.insets = new java.awt.Insets(3,3,3,3);
			placeEditorPanel.add(coloredTimeInvariantPanel, gridBagConstraints);

			initColorInvariantPanel();
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 2;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			//gridBagConstraints.insets = new java.awt.Insets(3,3,3,3);
			placeEditorPanel.add(colorInvariantPanel, gridBagConstraints);

			initTokenPanel();
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 2;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.gridheight = 3;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			//gridBagConstraints.insets = new Insets(0,0,0,5);
			placeEditorPanel.add(tokenPanel, gridBagConstraints);
		}else{
			initTimeInvariantPanel();
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			//gridBagConstraints.insets = new java.awt.Insets(3,3,3,3);
			placeEditorPanel.add(timeInvariantPanel, gridBagConstraints);
		}

		initButtonPanel();
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		add(buttonPanel, gridBagConstraints);
	}// </editor-fold>//GEN-END:initComponents

	private void initColoredTimeInvariantPanel() {		
		coloredTimeInvariantPanel = new JPanel(new GridBagLayout());
		coloredTimeInvariantPanel.setBorder(BorderFactory.createTitledBorder("Time Invariant"));

		JLabel label = new JLabel("Time invariant:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.insets = new Insets(3,3,3,3);
		coloredTimeInvariantPanel.add(label, gbc);

		invRelationNormal = new JComboBox(new String[]{"<=","<"});
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		coloredTimeInvariantPanel.add(invRelationNormal, gbc);

		Dimension txtBoxDims = new Dimension(50,25);
		invScaleTextbox = new JTextField();
		invScaleTextbox.setMinimumSize(txtBoxDims);
		invScaleTextbox.setMaximumSize(txtBoxDims);
		invScaleTextbox.setPreferredSize(txtBoxDims);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		coloredTimeInvariantPanel.add(invScaleTextbox, gbc);

		String mathExprString = "* val +";
		JLabel mathExprLabel = new JLabel(mathExprString);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(0,0,0,0);
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		coloredTimeInvariantPanel.add(mathExprLabel, gbc);

		invOffsetTextbox = new JTextField();
		invOffsetTextbox.setMinimumSize(txtBoxDims);
		invOffsetTextbox.setMaximumSize(txtBoxDims);
		invOffsetTextbox.setPreferredSize(txtBoxDims);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		coloredTimeInvariantPanel.add(invOffsetTextbox, gbc);

		invariantInf = new JCheckBox("Inf");
		invariantInf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				if(invariantInf.isSelected()){
					invScaleTextbox.setEnabled(false);
					invOffsetTextbox.setEnabled(false);
					invRelationNormal.setEnabled(false);
					invRelationNormal.setSelectedItem("<");
				}else{
					invScaleTextbox.setEnabled(true);
					invOffsetTextbox.setEnabled(true);
					invRelationNormal.setEnabled(true);
					invRelationNormal.setSelectedItem("<=");
				}
			}
		});
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(3,3,3,3);
		gbc.gridx = 5;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		coloredTimeInvariantPanel.add(invariantInf, gbc);

		setInitialTimeInvariantState();
	}

	private void setInitialTimeInvariantState() {
		ColoredTimeInvariant inv = ((ColoredTimedPlace)place).getTimeInvariant();

		if(inv.goesToInfinity()){
			invScaleTextbox.setText("0");
			invScaleTextbox.setEnabled(false);
			invOffsetTextbox.setText("0");
			invOffsetTextbox.setEnabled(false);
			invRelationNormal.setSelectedItem("<");
			invRelationNormal.setEnabled(false);

			invariantInf.setSelected(true);
		}else{
			invScaleTextbox.setText(inv.getUpper().getScale().toString());
			invScaleTextbox.setEnabled(true);
			invOffsetTextbox.setText(inv.getUpper().getOffset().toString());
			invOffsetTextbox.setEnabled(true);
			invRelationNormal.setSelectedItem(inv.getOperator());
			invRelationNormal.setEnabled(true);

			invariantInf.setSelected(false);
		}
	}

	private void initTokenPanel() {
		tokenPanel = new JPanel(new GridBagLayout());
		tokenPanel.setBorder(BorderFactory.createTitledBorder("Tokens"));

		tokenHelpLabel = new JLabel("Double-click a value to change it.");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3,3,0,3);
		tokenPanel.add(tokenHelpLabel, gbc);

		tokenHelpLabel2 = new JLabel("You can use integers and constants.");
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0,3,3,3);
		tokenPanel.add(tokenHelpLabel2, gbc);

		tokenTableModel = new TokenTableModel((ColoredTimedPlace)place);
		tokenTable = new JTable(tokenTableModel);
		Dimension dims = new Dimension(150,133);
		tokenTable.setPreferredScrollableViewportSize(dims);

		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
		render.setHorizontalAlignment(JTextField.RIGHT);
		tokenTable.getColumn("Value").setCellRenderer(render);
		tokenTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tokenTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting() || tokenTable.getSelectedRow() == -1){
					removeTokenButton.setEnabled(false);
				}else{
					removeTokenButton.setEnabled(true);
				}			
			}
		});

		JScrollPane pane = new JScrollPane(tokenTable);
		pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.BOTH;
		//gbc.insets = new Insets(3,3,3,3);
		tokenPanel.add(pane, gbc);

		JPanel btnPanel = new JPanel(new GridBagLayout());
		addTokenButton = new JButton("Add");
		addTokenButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				tokenTableModel.addColoredToken(new ColoredToken());

			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(3,3,3,3);
		btnPanel.add(addTokenButton, gbc);

		removeTokenButton = new JButton("Remove");
		removeTokenButton.setEnabled(false);
		removeTokenButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				tokenTableModel.removeColoredToken(tokenTable.getSelectedRow());
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(3,3,3,3);
		btnPanel.add(removeTokenButton, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(3,3,3,3);
		tokenPanel.add(btnPanel, gbc);
	}

	private void initButtonPanel() {
		java.awt.GridBagConstraints gridBagConstraints;
		buttonPanel = new javax.swing.JPanel();
		buttonPanel.setLayout(new java.awt.GridBagLayout());

		okButton = new javax.swing.JButton();
		okButton.setText("OK");
		okButton.setMaximumSize(new java.awt.Dimension(75, 25));
		okButton.setMinimumSize(new java.awt.Dimension(75, 25));
		okButton.setPreferredSize(new java.awt.Dimension(75, 25));
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonHandler(evt);
			}
		});
		okButton.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				okButtonKeyPressed(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 9);
		buttonPanel.add(okButton, gridBagConstraints);

		cancelButton = new javax.swing.JButton();
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonHandler(evt);
			}
		});

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 10);
		buttonPanel.add(cancelButton, gridBagConstraints);
	}

	private void initBasicPropertiesPanel() {
		basicPropertiesPanel = new JPanel();
		basicPropertiesPanel.setLayout(new java.awt.GridBagLayout());
		basicPropertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Place"));

		nameLabel = new javax.swing.JLabel();
		nameLabel.setText("Name:");
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(nameLabel, gridBagConstraints);

		nameTextField = new javax.swing.JTextField();
		nameTextField.setText(place.getName());
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(nameTextField, gridBagConstraints);

		if(!pnmlData.isUsingColors()){
			markingLabel = new javax.swing.JLabel();
			markingLabel.setText("Marking:");
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
			basicPropertiesPanel.add(markingLabel, gridBagConstraints);

			markingSpinner = new javax.swing.JSpinner();
			markingSpinner.setModel(new SpinnerNumberModel(place.getCurrentMarking(),0,Integer.MAX_VALUE,1));
			markingSpinner.setMinimumSize(new java.awt.Dimension(50, 20));
			markingSpinner.setPreferredSize(new java.awt.Dimension(50, 20));
			//      markingSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
			//         public void stateChanged(javax.swing.event.ChangeEvent evt) {
			////            markingSpinnerStateChanged(evt);
			//        	 if (tokenPanel.getComponents().length > (Integer) markingSpinner.getValue() ){
			//        		 tokenPanel.remove(tokenPanel.getComponents().length-1);
			//        	 }else{
			//        		 JSpinner newAgeSpinner = new JSpinner( new SpinnerNumberModel( 0f, 0f, Float.MAX_VALUE, 1f ) ); 
			//        		 newAgeSpinner.setMaximumSize(new Dimension(50,30));
			//        		 newAgeSpinner.setMinimumSize(new Dimension(50,30));
			//        		 newAgeSpinner.setPreferredSize(new Dimension(50,30));
			//        		 tokenPanel.add(newAgeSpinner);
			//        	 }
			//        	 ((EscapableDialog)rootPane.getParent()).pack();
			//         }
			//      });

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
			basicPropertiesPanel.add(markingSpinner, gridBagConstraints);
		}

		attributesCheckBox = new javax.swing.JCheckBox();
		attributesCheckBox.setSelected(place.getAttributesVisible());
		attributesCheckBox.setText("Show place attributes");
		attributesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
		attributesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		basicPropertiesPanel.add(attributesCheckBox, gridBagConstraints);
	}

	private void initColorInvariantPanel() {
		colorInvariantPanel = new JPanel();
		colorInvariantPanel.setLayout(new java.awt.GridBagLayout());
		colorInvariantPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Color Invariant"));

		colorInvariantLabel = new JLabel("Color Invariant:");
		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		colorInvariantPanel.add(colorInvariantLabel, gridBagConstraints);

		String colorInvariant = ((ColoredTimedPlace)place).getColorInvariantStringWithoutSetNotation(); 
		colorInvariantTextBox = new JTextField(colorInvariant);
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		colorInvariantPanel.add(colorInvariantTextBox, gridBagConstraints);

		colorInvariantExampleLabel = new JLabel("Example: 1, 3, 4-6, 8, 12-");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		colorInvariantPanel.add(colorInvariantExampleLabel, gridBagConstraints);
	}

	private void initTimeInvariantPanel() {
		timeInvariantPanel = new JPanel();
		timeInvariantPanel.setLayout(new java.awt.GridBagLayout());
		timeInvariantPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Time Invariant"));		

		invariantGroup = new JPanel(new GridBagLayout());

		invRelationNormal = new JComboBox(new String[]{"<=","<"});
		invRelationConstant = new JComboBox(new String[]{"<=","<"});
		invariantSpinner = new JSpinner(new SpinnerNumberModel(0,0,Integer.MAX_VALUE,1));
		invariantSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if ((Integer)invariantSpinner.getValue() < 1){
					invRelationNormal.setModel( new DefaultComboBoxModel(new String[]{"<="}) );
					invRelationNormal.setSelectedItem("<=");
				}else if (invRelationNormal.getModel().getSize()==1){
					invRelationNormal.setModel( new DefaultComboBoxModel(new String[]{"<=","<"}) );
				}

			}

		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.insets = new Insets(3,3,3,3);
		invariantGroup.add(invRelationNormal, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.insets = new Insets(3,3,3,3);
		invariantGroup.add(invRelationConstant, gbc);

		invariantSpinner.setMaximumSize(new Dimension(50,30));
		invariantSpinner.setMinimumSize(new Dimension(50,30));
		invariantSpinner.setPreferredSize(new Dimension(50,30));

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.insets = new Insets(3,3,3,3);
		invariantGroup.add(invariantSpinner, gbc);

		invariantInf = new JCheckBox("inf");
		invariantInf.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				if ( ! invariantInf.isSelected()){
					invariantSpinner.setEnabled(true);
					invRelationNormal.setSelectedItem("<=");
					if ((Integer)invariantSpinner.getValue() < 1){
						invRelationNormal.setModel(new DefaultComboBoxModel(new String[]{"<="}));
					}
					else{
						invRelationNormal.setModel(new DefaultComboBoxModel(new String[]{"<=", "<"}));
					}
				}else {
					invariantSpinner.setEnabled(false);
					invRelationNormal.setSelectedItem("<");
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[]{"<"}));
				}

			}

		});
		gbc = new GridBagConstraints();
		gbc.gridx = 3;
		gbc.gridy = 0;
		invariantGroup.add(invariantInf, gbc);

		Set<String> constants = CreateGui.getModel().getConstantNames();
		invConstantsComboBox = new JComboBox(constants.toArray());
		invConstantsComboBox.setMinimumSize(new Dimension(80,30));
		invConstantsComboBox.setPreferredSize(new Dimension(80,30));
		invConstantsComboBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e){
				if(e.getStateChange() == ItemEvent.SELECTED){
					setRelationModelForConstants();
				}
			}
		});

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		invariantGroup.add(invConstantsComboBox, gbc);

		normalInvRadioButton = new JRadioButton("Normal");
		normalInvRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				disableInvariantComponents();
				enableNormalInvariantComponents();
			}
		});


		constantInvRadioButton = new JRadioButton("Constant");
		constantInvRadioButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				disableInvariantComponents();
				enableConstantInvariantComponents();
			}
		});
		if(constants.isEmpty())
			constantInvRadioButton.setEnabled(false);


		ButtonGroup btnGroup = new ButtonGroup();
		btnGroup.add(normalInvRadioButton);
		btnGroup.add(constantInvRadioButton);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		invariantGroup.add(normalInvRadioButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		invariantGroup.add(constantInvRadioButton,gbc);

		String invariantToSet = place.getInvariant();
		int invariantValue = 0;
		int substringStart = 0;

		String constantName ="";

		if (invariantToSet.contains("<=")){
			invRelationNormal.setSelectedItem("<=");
			substringStart = 2;
		}else {
			invRelationNormal.setSelectedItem("<");
			substringStart = 1;
		}

		boolean isInf = false;
		if (invariantToSet.substring(substringStart).equals("inf")){
			isInf = true;
			invariantSpinner.setEnabled(false);
			invRelationNormal.setModel(new DefaultComboBoxModel(new String[]{"<"}));
			invariantInf.setSelected(true);
			invRelationNormal.setSelectedItem("<");
		}

		if(!isInf)
		{
			boolean isNumber = true;
			try{
				invariantValue = Integer.parseInt(invariantToSet.substring(substringStart));
			}catch(NumberFormatException e){
				isNumber = false;
			}

			if(!isNumber){
				constantName = invariantToSet.substring(substringStart);
			}
		}

		disableInvariantComponents();
		if(!constantName.isEmpty()){
			enableConstantInvariantComponents();
			constantInvRadioButton.setSelected(true);
			invConstantsComboBox.setSelectedItem(constantName);
			//setRelationModelForConstants();
		}
		else{
			enableNormalInvariantComponents();
			normalInvRadioButton.setSelected(true);
			if(!isInf){ 
				if ((Integer)invariantSpinner.getValue() < 1){
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[]{"<="}));
				}
				else{
					invRelationNormal.setModel(new DefaultComboBoxModel(new String[]{"<=", "<"}));
				}
				invariantSpinner.setValue(invariantValue);
				invariantSpinner.setEnabled(true);
				invRelationNormal.setSelectedItem(invariantToSet.substring(0, substringStart));

				invariantInf.setSelected(false);
			}
		}

		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		//gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
		timeInvariantPanel.add(invariantGroup, gridBagConstraints);
	}

	private void setRelationModelForConstants() {
		int value = CreateGui.getModel().getConstantValue(
				invConstantsComboBox.getSelectedItem().toString());

		String selected = invRelationConstant.getSelectedItem().toString();
		if(value == 0){
			invRelationConstant.setModel(new DefaultComboBoxModel(new String[]{"<="}));
		}else{
			invRelationConstant.setModel(new DefaultComboBoxModel(new String[]{"<=", "<"}));
		}
		invRelationConstant.setSelectedItem(selected);
	}

	protected void enableConstantInvariantComponents() {
		invRelationConstant.setEnabled(true);
		invConstantsComboBox.setEnabled(true);
		setRelationModelForConstants();
	}

	protected void enableNormalInvariantComponents() {
		invRelationNormal.setEnabled(true);
		invariantInf.setEnabled(true);
		invariantSpinner.setValue(0);
		invariantInf.setSelected(true);
		invRelationNormal.setModel(new DefaultComboBoxModel(new String[]{"<"}));
	}

	protected void disableInvariantComponents() {
		invRelationNormal.setEnabled(false);
		invRelationConstant.setEnabled(false);
		invariantSpinner.setEnabled(false);
		invConstantsComboBox.setEnabled(false);
		invariantInf.setEnabled(false);
	}

	/*   private void markingSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_markingSpinnerStateChanged
      if ((markingComboBox.getSelectedIndex() > 0) &&
         (((MarkingParameter)markingComboBox.getSelectedItem()).getValue() 
                 != markingSpinner.getValue())){
         markingComboBox.setSelectedIndex(0);
      }

   }//GEN-LAST:event_markingSpinnerStateChanged
	 */   
	ChangeListener changeListener = new javax.swing.event.ChangeListener() {
		public void stateChanged(javax.swing.event.ChangeEvent evt) {
			JSpinner spinner = (JSpinner)evt.getSource();
			JSpinner.NumberEditor numberEditor =
				((JSpinner.NumberEditor)spinner.getEditor());
			numberEditor.getTextField().setBackground(new Color(255,255,255));
			spinner.removeChangeListener(this);
		}
	};   


	private void okButtonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_okButtonKeyPressed
		if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
			doOK();
		}
	}//GEN-LAST:event_okButtonKeyPressed

	private void doOK(){
		Integer newMarking = marking;
		if(!pnmlData.isUsingColors()){
			try {
				newMarking = (Integer)markingSpinner.getValue();
			} catch (Exception e){
				JSpinner.NumberEditor numberEditor =
					((JSpinner.NumberEditor)markingSpinner.getEditor());
				numberEditor.getTextField().setBackground(new Color(255,0,0));
				markingSpinner.addChangeListener(changeListener);
				markingSpinner.requestFocusInWindow();
				return;
			}      
		}

		view.getUndoManager().newEdit(); // new "transaction""

		String newName = nameTextField.getText();

		if (!newName.equals(name)){
			if (! Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)){
				System.err.println("Acceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z]*");
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"Acceptable names for places are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
						"Error",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			} else if ( (pnmlData.getPlaceByName(newName) != null) || (pnmlData.getTransitionByName(newName) != null) ){
				System.err.println("Places cannot be called the same as an other Place or Transition.");
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"Places cannot be called the same as an other Place or Transition.",
						"Error",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			} else {

				view.getUndoManager().addEdit(place.setPNObjectName(newName));

				ArrayList<TAPNQuery> queries = CreateGui.getModel().getQueries();

				for (TAPNQuery q : queries) {
					q.query = q.query.replaceAll(name + "[^\\_a-zA-Z0-9]", newName); 
				}
				//CreateGui.createLeftPane();
			}
			//    	  if (!(newName.charAt(0)=='#')){
			//    		  if(newName.contains("*") || newName.contains("+")){
			//        		  System.err.println("Places can't have names with *'s or +'s");
			//        		  JOptionPane.showMessageDialog(CreateGui.getApp(),
			//          				"Places can't have names with *'s or +'s\n",
			//          				"Error",
			//          				JOptionPane.INFORMATION_MESSAGE);
			//        		  return;
			//        	  }else {
			//        		  view.getUndoManager().addEdit(place.setPNObjectName(newName));
			//        	  }
			//    	  } else {
			//    		  System.err.println("Places can't have names starting with #");
			//    		  JOptionPane.showMessageDialog(CreateGui.getApp(),
			//      				"Places can't have names starting with #\n",
			//      				"Error",
			//      				JOptionPane.INFORMATION_MESSAGE);
			//    		  return;
			//    	  }
		}



		if(pnmlData.isUsingColors()){
			ColoredTimedPlace coloredTimedPlace = (ColoredTimedPlace)place;

			ColoredTimeInvariant timeInvariant = null;
			try{
				timeInvariant = createTimeInvariant();
			}catch(IllegalArgumentException e){
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"There was an error in parsing the time invariant.\n\nPossible causes:\n\t- Use of negative values\n\t- Use of undefined constant",
						"Error",
						JOptionPane.INFORMATION_MESSAGE);
				view.getUndoManager().undo();

				return;
			}
			
			view.getUndoManager().addEdit(coloredTimedPlace.setTimeInvariant(timeInvariant));


			if(!colorInvariantTextBox.getText().isEmpty()){

				ColorSet colorInvariant = new ColorSet();
				String[] ranges = colorInvariantTextBox.getText().split(",");
				for(String range : ranges){
					try{
						IntOrConstantRange ir = IntOrConstantRange.parse(range.trim());
						colorInvariant.add(ir);
					}catch(IllegalArgumentException e){
						JOptionPane.showMessageDialog(CreateGui.getApp(),
								String.format("There was an error in parsing the color invariant. The problem concerns \"%1s\".",range),
								"Error",
								JOptionPane.INFORMATION_MESSAGE);
						view.getUndoManager().undo();

						return;
					}
				}
				UndoableEdit action = coloredTimedPlace.setColorInvariant(colorInvariant); 
				view.getUndoManager().addEdit(action);
			}

			List<ColoredToken> tokens = tokenTableModel.getTokens();
			for(ColoredToken token : tokens){	
				if(token.getColor() == null){
					JOptionPane.showMessageDialog(CreateGui.getApp(),
							"One or more tokens are missing a color value.",
							"Error",
							JOptionPane.INFORMATION_MESSAGE);
					view.getUndoManager().undo();
					return;
				}

				if(token.getColor().getValue() < 0){
					JOptionPane.showMessageDialog(CreateGui.getApp(),
							"One or more tokens have a negative color value.",
							"Error",
							JOptionPane.INFORMATION_MESSAGE);
					view.getUndoManager().undo();
					return;
				}

				if(!coloredTimedPlace.satisfiesInvariant(token)){
					String msg = String.format("A token value violates the color invariant of the place. The problem concerns the token value \"%1$s\".", token.getColor());
					if(token.getColor().isUsingConstant()){
						msg += String.format("\n \"%1$s\" is a constant with value %2$d.",
								token.getColor(),
								CreateGui.getModel().getConstantValue(token.getColor().getConstantName()));
					}

					JOptionPane.showMessageDialog(CreateGui.getApp(),
							msg,
							"Error",
							JOptionPane.INFORMATION_MESSAGE);
					view.getUndoManager().undo();

					return;
				}
			}

			UndoableEdit edit = coloredTimedPlace.setColoredTokens(tokens);
			view.getUndoManager().addEdit(edit);
		}else{
			boolean isNormalInvariant = normalInvRadioButton.isSelected();
			String newInvariant = "";

			if(isNormalInvariant)
			{
				newInvariant = (String)invRelationNormal.getSelectedItem();
				if ( ! invariantInf.isSelected()){
					newInvariant = newInvariant + invariantSpinner.getValue();
				} else {
					newInvariant = newInvariant + "inf";
				}
			}
			else{
				String constantName = (String)invConstantsComboBox.getSelectedItem();
				newInvariant = (String)invRelationConstant.getSelectedItem() + constantName;
			}

			//if ()  -  TODO do some check if it has canged and if value is ok
			view.getUndoManager().addEdit(place.setInvariant(newInvariant));

			if (markingComboBox.getSelectedIndex() >0) {
				// There's a marking parameter selected
				MarkingParameter parameter = 
					(MarkingParameter)markingComboBox.getSelectedItem() ;
				if (parameter != mParameter){

					if (mParameter != null) {
						// The marking parameter has been changed
						view.getUndoManager().addEdit(place.changeMarkingParameter(
								(MarkingParameter)markingComboBox.getSelectedItem()));
					} else {
						//The marking parameter has been changed
						view.getUndoManager().addEdit(place.setMarkingParameter(
								(MarkingParameter)markingComboBox.getSelectedItem()));
					}
				}
			} else {
				// There is no marking parameter selected
				if (mParameter != null) {
					// The rate parameter has been changed
					view.getUndoManager().addEdit(place.clearMarkingParameter());
				}
				if (newMarking != marking) {
					view.getUndoManager().addEdit(place.setCurrentMarking(newMarking));            
				}
			}
		}

		//      ArrayList<Float> ageOfTokensToSet = new ArrayList<Float>();
		//      for (Component ageOfTokenSpinner : tokenPanel.getComponents()){
		//    	  if (ageOfTokenSpinner instanceof JSpinner){
		//    		  ageOfTokensToSet.add(Float.parseFloat(""+((JSpinner)ageOfTokenSpinner).getValue()));
		//    	  }
		//      }
		//      view.getUndoManager().addEdit(place.setAgeOfTokens(ageOfTokensToSet));


		if (attributesVisible != attributesCheckBox.isSelected()){
			place.toggleAttributesVisible();
		}    
		place.repaint();

		CreateGui.getModel().buildConstraints();
		exit();
	}


	private ColoredTimeInvariant createTimeInvariant() {
		if(!invariantInf.isSelected()){
			IntOrConstant scale = new IntOrConstant(invScaleTextbox.getText());
			IntOrConstant offset = new IntOrConstant(invOffsetTextbox.getText());
			
			return new ColoredTimeInvariant((String)invRelationNormal.getSelectedItem(), 
					new IntervalBound(scale, offset));
		}else{
			return new ColoredTimeInvariant();
		}
	}

	private void okButtonHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonHandler
		doOK();
	}//GEN-LAST:event_okButtonHandler


	private void exit() {
		rootPane.getParent().setVisible(false);
	}


	private void cancelButtonHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonHandler
		//view.getUndoManager().undo();
		exit();
	}//GEN-LAST:event_cancelButtonHandler



	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JCheckBox attributesCheckBox;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JButton cancelButton;
	private javax.swing.JComboBox markingComboBox;
	private javax.swing.JLabel markingLabel;
	private javax.swing.JSpinner markingSpinner;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JPanel placeEditorPanel;
	private javax.swing.JPanel basicPropertiesPanel;
	private javax.swing.JPanel timeInvariantPanel;
	private javax.swing.JPanel colorInvariantPanel;
	private JLabel colorInvariantLabel;
	private JLabel colorInvariantExampleLabel;
	private JTextField colorInvariantTextBox;
	private JPanel invariantGroup;
	private JComboBox invRelationNormal;
	private JComboBox invRelationConstant;
	private JSpinner invariantSpinner;
	private JCheckBox invariantInf;
	private JComboBox invConstantsComboBox;
	private JRadioButton normalInvRadioButton;
	private JRadioButton constantInvRadioButton;

	private JPanel tokenPanel;
	private JLabel tokenHelpLabel;
	private JLabel tokenHelpLabel2;
	private JTable tokenTable;
	private JButton addTokenButton;
	private JButton removeTokenButton;
	private TokenTableModel tokenTableModel;

	private JPanel coloredTimeInvariantPanel;
	private JTextField invScaleTextbox;
	private JTextField invOffsetTextbox;
}
