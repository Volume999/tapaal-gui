package pipe.gui.ColoredComponents;

import dk.aau.cs.debug.Logger;
import dk.aau.cs.gui.Context;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ColoredTimeInterval;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.Expressions.*;
import dk.aau.cs.model.CPN.ProductType;
import dk.aau.cs.model.tapn.TimedInputArc;
import dk.aau.cs.model.tapn.TimedOutputArc;
import dk.aau.cs.model.tapn.TransportArc;
import net.tapaal.swinghelpers.GridBagHelper;
import pipe.gui.CreateGui;
import pipe.gui.graphicElements.Arc;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.graphicElements.Place;
import pipe.gui.graphicElements.tapn.*;
import pipe.gui.widgets.EscapableDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ColoredArcGuardPanel extends JPanel {
    PetriNetObject objectToBeEdited;
    boolean isTransportArc = false;
    boolean isInputArc = false;
    Context context;
    private Integer transportWeight;
    private ColorExpression transportInputExpr;
    private ColorExpression transportOutputExpr;

    public ColoredArcGuardPanel(PetriNetObject objectToBeEdited, Context context){
        this.objectToBeEdited = objectToBeEdited;
        if(objectToBeEdited instanceof TimedTransportArcComponent){
            isTransportArc = true;
            setTransportExpression();
        }
        if(((Arc)objectToBeEdited).getSource() instanceof Place){
            isInputArc = true;
            this.colorType = ((TimedPlaceComponent) ((Arc)objectToBeEdited).getSource()).underlyingPlace().getColorType();
        }
        this.context = context;
        this.setLayout(new GridBagLayout());
        initPanels();
        initExpr();
        setTimeConstraints();
        hideIrrelevantInformation();
    }

    public void hideIrrelevantInformation(){
        if(!objectToBeEdited.isTimed()){
            arcColorInvariantPanel.setVisible(false);
        }
        if(isTransportArc){
            regularArcExprPanel.setVisible(false);
        } else{
            transportWeightPanel.setVisible(false);
        }
    }

    private void setTransportExpression() {
        ArcExpression arcExprInput = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getInputExpression();
        ArcExpression arcExprOutput = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getOutputExpression();

        if (arcExprInput instanceof NumberOfExpression) {
            transportWeight = arcExprInput.weight();
            Vector<ColorExpression> vecColorExpr = ((NumberOfExpression) arcExprInput).getColor();
            transportInputExpr = new TupleExpression(vecColorExpr);
        }

        if (arcExprOutput instanceof  NumberOfExpression) {
            Vector<ColorExpression> vecColorExpr = ((NumberOfExpression) arcExprOutput).getColor();
            transportOutputExpr = new TupleExpression(vecColorExpr);
        }
    }

    private void initPanels() {
        initRegularArcExpressionPanel();
        initColoredTimedGuard();
        initWeightPanel();
        if(isTransportArc){
            initTransportArcExpressionPanel();
        }

    }

    private void initWeightPanel(){
        transportWeightPanel = new JPanel(new GridBagLayout());
        //int current = transportWeight;
        int min = 1;
        int max = 9999;
        int step = 1;
        numberModel = new SpinnerNumberModel(1, min, max, step);
        colorExpressionWeightSpinner = new JSpinner(numberModel);
        JLabel weightLabel = new JLabel("Weight:");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10 ,5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        transportWeightPanel.add(weightLabel, gbc);

        gbc.gridx = 1;
        transportWeightPanel.add(colorExpressionWeightSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(transportWeightPanel, gbc);
    }

    private void initColoredTimedGuard() {
        ColorType ct;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(initColorConstraintPanel(), gbc);
    }

    private JPanel initColorConstraintPanel() {
        arcColorInvariantPanel = new JPanel(new GridBagLayout());
        ColorComboboxPanel colorComboboxPanel = new ColorComboboxPanel(colorType, "colors");

        addTimeConstraintButton = new JButton("Add");
        removeTimeConstraintButton = new JButton("Remove");
        editTimeConstraintButton = new JButton("Edit");

        Dimension buttonSize = new Dimension(80, 27);

        addTimeConstraintButton.setPreferredSize(buttonSize);
        removeTimeConstraintButton.setPreferredSize(buttonSize);
        editTimeConstraintButton.setPreferredSize(buttonSize);

        timeConstraintListModel = new DefaultListModel();
        timeConstraintList = new JList(timeConstraintListModel);
        JScrollPane timeConstraintScrollPane = new JScrollPane(timeConstraintList);
        timeConstraintScrollPane.setViewportView(timeConstraintList);
        timeConstraintScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);


        timeConstraintScrollPane.setBorder(BorderFactory.createTitledBorder("Time interval for colors"));
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Color");
        tableModel.addColumn("Time interval");

        JTable table = new JTable(tableModel);
        table.setPreferredSize(new Dimension(300, 150));
        timeConstraintScrollPane.setPreferredSize(new Dimension(300, 150));
        timeConstraintList.addMouseListener(createDoubleClickMouseAdapter());



        addTimeConstraintButton.addActionListener(actionEvent -> {
            JComboBox[] comboBoxes = colorComboboxPanel.getColorTypeComboBoxesArray();
            ColoredTimeInterval timeInterval;

            if (!(colorType instanceof ProductType)) {
                timeInterval = ColoredTimeInterval.ZERO_INF_DYN_COLOR((dk.aau.cs.model.CPN.Color) comboBoxes[0].getItemAt(comboBoxes[0].getSelectedIndex()));
            } else {
                Vector<dk.aau.cs.model.CPN.Color> colors = new Vector<dk.aau.cs.model.CPN.Color>();
                for (int i = 0; i < comboBoxes.length; i++) {
                    colors.add((dk.aau.cs.model.CPN.Color) comboBoxes[i].getItemAt(comboBoxes[i].getSelectedIndex()));
                }
                dk.aau.cs.model.CPN.Color color = new dk.aau.cs.model.CPN.Color(colorType, 0, colors);

                timeInterval = ColoredTimeInterval.ZERO_INF_DYN_COLOR(color);

            }
            boolean alreadyExists = false;
            for (int i = 0; i < timeConstraintListModel.size(); i++) {
                if (timeInterval.equalsOnlyColor(timeConstraintListModel.get(i)))
                    alreadyExists = true;
            }
            if (alreadyExists) {
                JOptionPane.showMessageDialog(null, "A time constraint for this color is already active and can be found in the list.");
            } else
                timeConstraintListModel.addElement(timeInterval);

        });

        removeTimeConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int index = timeConstraintList.getSelectedIndex();
                Object star = timeConstraintListModel.elementAt(index);
                if(star instanceof ColoredTimeInterval) {
                    if(((ColoredTimeInterval) star).getColor().equals(dk.aau.cs.model.CPN.Color.STAR_COLOR)) {
                        JOptionPane.showMessageDialog(null, "Star interval cannot be removed");
                    }else {
                        timeConstraintListModel.removeElementAt(timeConstraintList.getSelectedIndex());
                    }
                }
            }
        });

        editTimeConstraintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Time Interval", true);
                Container contentPane = guiDialog.getContentPane();

                // 1 Set layout
                contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
                ColoredTimeIntervalDialogPanel ctiPanel = new ColoredTimeIntervalDialogPanel(guiDialog.getRootPane(), context,(ColoredTimeInterval) timeConstraintListModel.getElementAt(timeConstraintList.getSelectedIndex()));
                contentPane.add(ctiPanel);

                guiDialog.setResizable(false);

                // Make window fit contents' preferred size
                guiDialog.pack();

                // Move window to the middle of the screen
                guiDialog.setLocationRelativeTo(null);
                guiDialog.setVisible(true);

                if (ctiPanel.isEditConfirmed()){
                    timeConstraintListModel.set(timeConstraintList.getSelectedIndex(), ctiPanel.getNewTimeInterval());
                }
            }
        });


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridwidth = 3;
        arcColorInvariantPanel.add(colorComboboxPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3,3);
        buttonPanel.add(addTimeConstraintButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(removeTimeConstraintButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(3, 3, 3, 3);
        buttonPanel.add(editTimeConstraintButton, gbc);

        Dimension dim;
        dim = new Dimension(375, 200);
        timeConstraintScrollPane.setPreferredSize(dim);
        timeConstraintScrollPane.setMaximumSize(dim);
        timeConstraintScrollPane.setMinimumSize(dim);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        arcColorInvariantPanel.add(buttonPanel,gbc);


        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridheight = 2;
        arcColorInvariantPanel.add(timeConstraintScrollPane, gbc);

        return arcColorInvariantPanel;
    }

    private void initTransportArcExpressionPanel(){
        transportExprTabbedPane = new JTabbedPane();
        inputPanel = new ColorExpressionDialogPanel(context, transportInputExpr, true);
        outputPanel = new ColorExpressionDialogPanel(context, transportOutputExpr, true);

        transportExprTabbedPane.add(inputPanel, "input");
        transportExprTabbedPane.add(outputPanel, "output");

        GridBagConstraints gbc = GridBagHelper.as(0, 4, GridBagHelper.Anchor.WEST, new Insets(5, 10, 5, 10));
        gbc.fill = GridBagConstraints.BOTH;
        add(transportExprTabbedPane, gbc);

    }

    private void initRegularArcExpressionPanel(){
        regularArcExprPanel = new JPanel(new GridBagLayout());
        regularArcExprPanel.setBorder(BorderFactory.createTitledBorder("Arc Expressions"));
        initExprField();
        initNumberExpressionsPanel();
        initArithmeticPanel();
        initEditPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        add(regularArcExprPanel, gbc);
    }

    private void initEditPanel() {
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("Editing"));
        editPanel.setPreferredSize(new Dimension(260, 190));

        ButtonGroup editButtonsGroup = new ButtonGroup();
        deleteExprSelectionButton = new JButton("Delete Selection");
        resetExprButton = new JButton("Reset Expression");
        undoButton = new JButton("Undo");
        redoButton = new JButton("Redo");
        editExprButton = new JButton("Edit Expression");
        editExprButton.setEnabled(true);

        //TODO: add tooltips to buttons

        editButtonsGroup.add(deleteExprSelectionButton);
        editButtonsGroup.add(resetExprButton);
        editButtonsGroup.add(undoButton);
        editButtonsGroup.add(redoButton);
        editButtonsGroup.add(editExprButton);

        deleteExprSelectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                deleteSelection();
            }
        });

        resetExprButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PlaceHolderArcExpression pHExpr = new PlaceHolderArcExpression();
                arcExpression = arcExpression.replace(arcExpression, pHExpr);
                updateSelection(pHExpr);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        editPanel.add(undoButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0 , 0);
        editPanel.add(redoButton, gbc);

        gbc.insets = new Insets(0, 0, 5 , 0);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(deleteExprSelectionButton, gbc);

        gbc.gridy = 2;
        editPanel.add(resetExprButton, gbc);

        gbc.gridy = 3;
        editPanel.add(editExprButton, gbc);

        //TODO: Actionlisteners

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;

        regularArcExprPanel.add(editPanel, gbc);
    }

    private void initArithmeticPanel() {
        JPanel arithmeticPanel = new JPanel(new GridBagLayout());
        arithmeticPanel.setBorder(BorderFactory.createTitledBorder("Arithmetic Expressions"));

        additionButton = new JButton("Addition");
        addAdditionPlaceHolderButton = new JButton("Add Placeholder");
        subtractionButton = new JButton("Subtraction");
        scalarButton = new JButton("Scalar");

        final Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelScalar = new SpinnerNumberModel(current, min, max, step);

        JSpinner scalarJSpinner = new JSpinner(numberModelScalar);

        scalarJSpinner.setPreferredSize(new Dimension(50, 27));
        scalarJSpinner.setPreferredSize(new Dimension(50, 27));
        scalarJSpinner.setPreferredSize(new Dimension(50, 27));

        additionButton.setPreferredSize(new Dimension(110, 30));
        additionButton.setMinimumSize(new Dimension(110, 30));
        additionButton.setMaximumSize(new Dimension(110, 30));

        subtractionButton.setPreferredSize(new Dimension(110, 30));
        subtractionButton.setMinimumSize(new Dimension(110, 30));
        subtractionButton.setMaximumSize(new Dimension(110, 30));

        scalarButton.setPreferredSize(new Dimension(110, 30));
        scalarButton.setMinimumSize(new Dimension(110, 30));
        scalarButton.setMaximumSize(new Dimension(110, 30));

        addAdditionPlaceHolderButton.setPreferredSize(new Dimension(150, 30));

        additionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AddExpression addExpr;
                if (currentSelection.getObject() instanceof ArcExpression) {
                    Vector<ArcExpression> vExpr = new Vector();
                    vExpr.add((ArcExpression) currentSelection.getObject());
                    vExpr.add(new PlaceHolderArcExpression());
                    addExpr = new AddExpression(vExpr);
                    arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
                    updateSelection(addExpr);
                }
            }
        });

        subtractionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SubtractExpression subExpr = null;
                if (currentSelection.getObject() instanceof PlaceHolderArcExpression) {
                    subExpr = new SubtractExpression((PlaceHolderArcExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                }
                else if (currentSelection.getObject() instanceof SubtractExpression) {
                    subExpr = new SubtractExpression((SubtractExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                }
                else if (currentSelection.getObject() instanceof ScalarProductExpression) {
                    subExpr = new SubtractExpression((ScalarProductExpression)currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                } else if (currentSelection.getObject() instanceof NumberOfExpression || currentSelection.getObject() instanceof AddExpression) {
                    subExpr = new SubtractExpression((ArcExpression) currentSelection.getObject(), new PlaceHolderArcExpression());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
                    updateSelection(subExpr);
                }
            }
        });

        scalarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ScalarProductExpression scalarExpr = null;
                Integer value = (Integer)scalarJSpinner.getValue();
                if (currentSelection.getObject() instanceof ArcExpression) {
                    scalarExpr = new ScalarProductExpression(value, (ArcExpression) currentSelection.getObject());
                    arcExpression = arcExpression.replace(currentSelection.getObject(), scalarExpr);
                    updateSelection(scalarExpr);
                }
            }
        });

        addAdditionPlaceHolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (currentSelection.getObject() instanceof AddExpression) {
                    AddExpression addExpr = (AddExpression) currentSelection.getObject();
                    Vector<ArcExpression> vecExpr =  addExpr.getAddExpression();
                    vecExpr.add(new PlaceHolderArcExpression());
                    addExpr = new AddExpression(vecExpr);
                    arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
                    updateSelection(addExpr);
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0,0, 0);
        gbc.anchor = GridBagConstraints.WEST;
        arithmeticPanel.add(additionButton, gbc);

        gbc.gridy = 1;
        arithmeticPanel.add(addAdditionPlaceHolderButton, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        arithmeticPanel.add(separator,gbc);


        gbc.gridy = 3;
        arithmeticPanel.add(subtractionButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        arithmeticPanel.add(scalarJSpinner, gbc);

        gbc.gridx = 1;
        arithmeticPanel.add(scalarButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;

        regularArcExprPanel.add(arithmeticPanel,gbc);

    }

    private void initNumberExpressionsPanel() {
        JPanel numberExprPanel = new JPanel(new GridBagLayout());
        numberExprPanel.setBorder(BorderFactory.createTitledBorder("Numerical Expressions"));

        Integer current = 1;
        Integer min = 1;
        Integer max = 999;
        Integer step = 1;
        SpinnerNumberModel numberModelNumber = new SpinnerNumberModel(current, min, max, step);
        SpinnerNumberModel numberModelAll = new SpinnerNumberModel(current, min, max, step);

        numberExpressionJSpinner = new JSpinner(numberModelNumber);
        numberExpressionButton = new JButton("Number Expr");



        allExpressionComboBox = new JComboBox();
        allExpressionJSpinner = new JSpinner(numberModelAll);
        allExpressionButton = new JButton("All Expression");

        addColorExpressionButton = new JButton("Edit Color Expr");

        for (ColorType element : context.activeModel().parentNetwork().colorTypes()) {
            allExpressionComboBox.addItem(element);
        }

        numberExpressionJSpinner.setPreferredSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMinimumSize(new Dimension(50, 27));
        numberExpressionJSpinner.setMaximumSize(new Dimension(50, 27));

        allExpressionJSpinner.setPreferredSize(new Dimension(50, 27));
        allExpressionJSpinner.setMinimumSize(new Dimension(50, 27));
        allExpressionJSpinner.setMaximumSize(new Dimension(50, 27));

        allExpressionComboBox.setPreferredSize(new Dimension(150, 27));
        allExpressionComboBox.setMinimumSize(new Dimension(150, 27));
        allExpressionComboBox.setMaximumSize(new Dimension(150, 27));

        numberExpressionButton.setPreferredSize(new Dimension(125, 27));
        numberExpressionButton.setMinimumSize(new Dimension(125, 27));
        numberExpressionButton.setMaximumSize(new Dimension(125, 27));

        allExpressionButton.setPreferredSize(new Dimension(125, 27));
        allExpressionButton.setMinimumSize(new Dimension(125, 27));
        allExpressionButton.setMaximumSize(new Dimension(125, 27));

        addColorExpressionButton.setMaximumSize(new Dimension(125, 27));
        addColorExpressionButton.setMinimumSize(new Dimension(125, 27));
        addColorExpressionButton.setPreferredSize(new Dimension(125, 27));


       allExpressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addAllExpression();
            }
        });

        numberExpressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                addNumberExpression();
            }
        });

        addColorExpressionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (currentSelection.getObject() instanceof ColorExpression) {
                    ColorExpression colorExpr = (ColorExpression)currentSelection.getObject();

                    EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Color Expression", true);
                    Container contentPane = guiDialog.getContentPane();

                    ColorExpressionDialogPanel cep = new ColorExpressionDialogPanel(guiDialog.getRootPane(), context, colorExpr, false);
                    contentPane.add(cep);

                    guiDialog.setResizable(true);
                    guiDialog.pack();
                    guiDialog.setLocationRelativeTo(null);
                    guiDialog.setVisible(true);

                    if (cep.clickedOK == true) {
                        ColorExpression expr = cep.getColorExpression();
                        arcExpression = arcExpression.replace(currentSelection.getObject(), expr);
                        updateSelection(expr);
                    }
                }
                else {
                    JOptionPane.showMessageDialog(CreateGui.getApp(), "You have to select a colored placeholder location <+> or a color expression already added in the expression to add a color expression.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0,5 ,5 );
        numberExprPanel.add(allExpressionJSpinner, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.WEST;
        numberExprPanel.add(allExpressionComboBox, gbc);

        gbc.gridx = 2;
        numberExprPanel.add(allExpressionButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0,5 ,5 );
        numberExprPanel.add(numberExpressionJSpinner, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(numberExpressionButton, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setEnabled(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(separator,gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        numberExprPanel.add(addColorExpressionButton, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.VERTICAL;

        regularArcExprPanel.add(numberExprPanel, gbc);
    }

    private void initExprField () {
        exprField = new JTextPane();

        StyledDocument doc = exprField.getStyledDocument();

        //Set alignment to be centered for all paragraphs
        MutableAttributeSet standard = new SimpleAttributeSet();
        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
        StyleConstants.setFontSize(standard, 14);
        doc.setParagraphAttributes(0, 0, standard, true);

        exprField.setBackground(java.awt.Color.white);

        exprField.setEditable(false);
        exprField.setToolTipText("Tooltip missing");

        JScrollPane exprScrollPane = new JScrollPane(exprField);
        exprScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        Dimension d = new Dimension(880, 80);
        exprScrollPane.setPreferredSize(d);
        exprScrollPane.setMinimumSize(d);

        exprField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!exprField.isEditable()) {
                    updateSelection();
                }
            }
        });

        exprField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                //TODO: setSaveButtonsEnabled()
            }
        });

        exprField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!exprField.isEditable()) {
                    //TODO: see line 1232 in CTLQueryDialog for impl example.
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 4;
        regularArcExprPanel.add(exprScrollPane, gbc);
    }

    private void addAllExpression() {
        AllExpression allExpr = new AllExpression((ColorType) allExpressionComboBox.getItemAt(allExpressionComboBox.getSelectedIndex()));
        Integer value = (Integer)allExpressionJSpinner.getValue();
        NumberOfExpression numbExpr = new NumberOfExpression(value, allExpr);
        if (currentSelection.getObject() instanceof ArcExpression) {
            arcExpression = arcExpression.replace(currentSelection.getObject(), numbExpr);
            updateSelection(numbExpr);
        }
    }

    private void addNumberExpression() {
        Vector<ColorExpression> colorExprVec = new Vector();
        colorExprVec.add(new PlaceHolderColorExpression());
        Integer value = (Integer)numberExpressionJSpinner.getValue();
        NumberOfExpression numbExpr;
        if (currentSelection.getObject() instanceof NumberOfExpression) {
            numbExpr = new NumberOfExpression(value, ((NumberOfExpression)currentSelection.getObject()).getColor());
        } else {
            numbExpr = new NumberOfExpression(value, colorExprVec);
        }
        arcExpression = arcExpression.replace(currentSelection.getObject(), numbExpr);
        updateSelection(numbExpr);
    }

    private void parseExpression(ArcExpression expressionToParse) {
        if (expressionToParse instanceof AddExpression) {
            AddExpression addExpr = new AddExpression(((AddExpression) expressionToParse).getAddExpression());
            arcExpression = arcExpression.replace(currentSelection.getObject(), addExpr);
            updateSelection(addExpr);
        }
        else if(expressionToParse instanceof SubtractExpression) {
            SubtractExpression subExpr = new SubtractExpression(((SubtractExpression) expressionToParse).getLeftExpression(), ((SubtractExpression) expressionToParse).getRightExpression());
            arcExpression = arcExpression.replace(currentSelection.getObject(), subExpr);
            updateSelection(subExpr);
        }
        else if (expressionToParse instanceof NumberOfExpression) {
            NumberOfExpression expessionToParse = (NumberOfExpression) expressionToParse;
            NumberOfExpression numbExpr = null;
            if (expessionToParse.getAll() != null) {
                numbExpr = new NumberOfExpression(((NumberOfExpression) expressionToParse).getNumber(), expessionToParse.getAll());
            } else {
                numbExpr = new NumberOfExpression(expessionToParse.getNumber(), expessionToParse.getColor());
            }
            arcExpression = arcExpression.replace(currentSelection.getObject(), numbExpr);
            updateSelection(numbExpr);
        }
    }
    private void deleteSelection() {
        if (currentSelection != null) {
            Expression replacement = null;
            if (currentSelection.getObject() instanceof ArcExpression) {
                replacement = getSpecificChildOfProperty(1, currentSelection.getObject());
            }
            else if (currentSelection.getObject() instanceof ArcExpression) {
                replacement = new PlaceHolderColorExpression();
            }
            if (replacement != null) {
                arcExpression = arcExpression.replace(currentSelection.getObject(), replacement);
                updateSelection(replacement);
            }
        }
    }

    private ArcExpression getSpecificChildOfProperty(int number, Expression property) {
        ExprStringPosition[] children = property.getChildren();
        int count = 0;
        for (int i = 0; i < children.length; i++) {
            Expression child = children[i].getObject();
            if (child instanceof ArcExpression) {
                count++;
            }
            if (count == number) {
                return (ArcExpression) child;
            }
        }

        return new PlaceHolderArcExpression();
    }

    private void initExpr() {
        if (isInputArc != true) {
            if ((((TimedOutputArcComponent) objectToBeEdited).underlyingArc()).getExpression() != null) {
                PlaceHolderArcExpression placeholderArc = new PlaceHolderArcExpression();
                arcExpression = placeholderArc;
                updateSelection(arcExpression);
                parseExpression((((TimedOutputArcComponent) objectToBeEdited).underlyingArc()).getExpression());
            } else {
                arcExpression = new PlaceHolderArcExpression();
                exprField.setText(arcExpression.toString());
            }
        }
        else {
            if (isTransportArc != true) {
                TimedInputArc inputArc = ((TimedInputArcComponent) objectToBeEdited).underlyingTimedInputArc();
                if (inputArc.getArcExpression() != null) {
                    PlaceHolderArcExpression placeholderArc = new PlaceHolderArcExpression();
                    arcExpression = placeholderArc;
                    updateSelection(arcExpression);
                    parseExpression(inputArc.getArcExpression());
                } else {
                    arcExpression = new PlaceHolderArcExpression();
                    exprField.setText(arcExpression.toString());
                }
            } else {
                TransportArc transportArc = ((TimedTransportArcComponent) objectToBeEdited).underlyingTransportArc();
                if (isInputArc) {
                    if (transportArc.getInputExpression() != null) {
                        PlaceHolderArcExpression placeHolderArcExpression = new PlaceHolderArcExpression();
                        arcExpression = placeHolderArcExpression;
                        updateSelection(arcExpression);
                        parseExpression(transportArc.getInputExpression());
                    } else {
                        arcExpression = new PlaceHolderArcExpression();
                        exprField.setText(arcExpression.toString());
                    }
                } else {
                    if (transportArc.getOutputExpression() != null) {
                        PlaceHolderArcExpression placeHolderArcExpression = new PlaceHolderArcExpression();
                        arcExpression = placeHolderArcExpression;
                        updateSelection(arcExpression);
                        parseExpression(transportArc.getOutputExpression());
                    } else {
                        arcExpression = new PlaceHolderArcExpression();
                        exprField.setText(arcExpression.toString());
                    }
                }
            }

        }
    }
    private void updateSelection() {
        int index = exprField.getCaretPosition();
        ExprStringPosition position = arcExpression.objectAt(index);
        Logger.log(position.getObject().toString());
        if (position == null) {
            return;
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        if (currentSelection != null) {
            toggleEnabledButtons();
        } {
            //TODO: disable all expr buttons
        }

        //TODO: updateexprButtonsAccordingToSelection; line 573
    }

    private void updateSelection(Expression newSelection) {
        exprField.setText(arcExpression.toString());

        ExprStringPosition position;
        if (arcExpression.containsPlaceHolder()) {
            Expression ae = arcExpression.findFirstPlaceHolder();
            position = arcExpression.indexOf(ae);
        }
        else {
            position = arcExpression.indexOf(newSelection);
        }

        exprField.select(position.getStart(), position.getEnd());
        currentSelection = position;

        if (currentSelection != null) {
            toggleEnabledButtons();
        }
        else {
            //TODO::
        }
    }
    private void toggleEnabledButtons() {
        if (currentSelection.getObject() instanceof ColorExpression) {
            addAdditionPlaceHolderButton.setEnabled(false);
            addColorExpressionButton.setVisible(true);
        }
        else if (currentSelection.getObject() instanceof AddExpression) {
            allExpressionButton.setEnabled(false);
            numberExpressionButton.setEnabled(false);
            additionButton.setEnabled(false);
            subtractionButton.setEnabled(false);
            addAdditionPlaceHolderButton.setEnabled(false);
            scalarButton.setEnabled(false);
            addColorExpressionButton.setVisible(false);
            addAdditionPlaceHolderButton.setEnabled(true);
        }
        else if (currentSelection.getObject() instanceof ArcExpression) {
            allExpressionButton.setEnabled(true);
            numberExpressionButton.setEnabled(true);
            additionButton.setEnabled(true);
            subtractionButton.setEnabled(true);
            addAdditionPlaceHolderButton.setEnabled(true);
            scalarButton.setEnabled(true);
            addColorExpressionButton.setVisible(false);
            addAdditionPlaceHolderButton.setEnabled(false);
        }

    }
    public void onOkColored() {
        if (isInputArc) {
            if (isTransportArc) {
                int weight =  Integer.parseInt(colorExpressionWeightSpinner.getValue().toString());
                TransportArc transportArc = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc();
                ArcExpression inputExpression = getTransportExpression(inputPanel.getColorExpression(), weight);
                ArcExpression outputExpression = getTransportExpression(outputPanel.getColorExpression(), weight);
                transportArc.setInputExpression(inputExpression);
                transportArc.setOutputExpression(outputExpression);
                /*objectToBeEditedTransport.setUnderlyingArc(transportArc);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setInputExpression(inputExpression);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setOutputExpression(outputExpression);
                ((ColoredTransportArc)((ColoredTransportArcComponent)petriNetObject).underlyingTransportArc()).setCtiList(getctiList());*/
                transportArc.setColorTimeIntervals(getctiList());
                ((TimedTransportArcComponent) objectToBeEdited).updateLabel(false);
            } else {
                TimedInputArc inputArc = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc();
                ArcExpression arcExpression = this.arcExpression;
                inputArc.setExpression(arcExpression);
                /*objectToBeEditedInput.setUnderlyingArc(inputArc);
                ((ColoredInputArc)((ColoredInputArcComponent)petriNetObject).underlyingTimedInputArc()).setExpression(arcExpression);
                ((ColoredInputArc)((ColoredInputArcComponent)petriNetObject).underlyingTimedInputArc()).setColorTimeIntervals(getctiList());*/
                inputArc.setColorTimeIntervals(getctiList());
                ((TimedInputArcComponent) objectToBeEdited).updateLabel(false);
            }
        } else {
            ArcExpression arcExpression = this.arcExpression;
            TimedOutputArc outputArc = ((TimedOutputArcComponent)objectToBeEdited).underlyingArc();
            ((TimedOutputArcComponent) objectToBeEdited).setUnderlyingArc(outputArc);
            outputArc.setExpression(arcExpression);
        }

    }

    private ArcExpression getTransportExpression(ColorExpression colorExpr, int weight) {
        ArcExpression expr;
        Vector<ColorExpression> vecColorExpr = new Vector<ColorExpression>();
        if (colorExpr instanceof TupleExpression) { // we have to use TupleExpression if we want the colorExpressionPanel inside arcPanel when it is transport. IF the tuple only have one element we extract it to remove an unnecessary expression and parentheses
            if (((TupleExpression) colorExpr).getColors().size() == 1) {
                colorExpr = ((TupleExpression) colorExpr).getColors().firstElement();
            }
        }
        vecColorExpr.add(colorExpr);
        expr = new NumberOfExpression(weight, vecColorExpr);
        return expr;
    }

    private void setTimeConstraints() {
        List<ColoredTimeInterval> timeIntervals;
        if(isInputArc){
            if (isTransportArc){
                timeIntervals = ((TimedTransportArcComponent)objectToBeEdited).underlyingTransportArc().getColorTimeIntervals();
            }
            else{
                timeIntervals = ((TimedInputArcComponent)objectToBeEdited).underlyingTimedInputArc().getColorTimeIntervals();
            }
            for (ColoredTimeInterval timeInterval : timeIntervals) {
                timeConstraintListModel.addElement(timeInterval);
            }
        }
    }

    private MouseListener createDoubleClickMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                if (!timeConstraintList.isSelectionEmpty()) {
                    if (arg0.getButton() == MouseEvent.BUTTON1 && arg0.getClickCount() == 2) {
                        EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "Edit Time Interval", true);
                        Container contentPane = guiDialog.getContentPane();

                        // 1 Set layout
                        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
                        ColoredTimeIntervalDialogPanel ctiPanel = new ColoredTimeIntervalDialogPanel(guiDialog.getRootPane(), context, (ColoredTimeInterval) timeConstraintListModel.getElementAt(timeConstraintList.getSelectedIndex()));
                        contentPane.add(ctiPanel);

                        guiDialog.setResizable(false);

                        // Make window fit contents' preferred size
                        guiDialog.pack();

                        // Move window to the middle of the screen
                        guiDialog.setLocationRelativeTo(null);
                        guiDialog.setVisible(true);

                        if (ctiPanel.isEditConfirmed())
                            timeConstraintListModel.set(timeConstraintList.getSelectedIndex(), ctiPanel.getNewTimeInterval());

                    }
                }
            }
        };
    }

    public DefaultListModel getTimeConstraintModel() {return timeConstraintListModel;}
    private List<ColoredTimeInterval> getctiList() {
        List<ColoredTimeInterval> ctiList = new ArrayList<ColoredTimeInterval>();
        for (int i = 0; i < getTimeConstraintModel().size(); i++) {
            ctiList.add((ColoredTimeInterval) getTimeConstraintModel().get(i));
        }
        return ctiList;
    }

    private ColorType colorType;
    private JPanel regularArcExprPanel;
    JPanel arcColorInvariantPanel;
    JPanel transportWeightPanel;
    JTabbedPane transportExprTabbedPane;
    DefaultListModel timeConstraintListModel;
    JList timeConstraintList;
    private ExprStringPosition currentSelection = null;
    JSpinner numberExpressionJSpinner;
    JComboBox allExpressionComboBox;
    JSpinner allExpressionJSpinner;
    private ArcExpression arcExpression;
    private JTextPane exprField;
    JButton allExpressionButton;
    JButton addColorExpressionButton;
    JButton addTimeConstraintButton;
    JButton removeTimeConstraintButton;
    JButton editTimeConstraintButton;
    JButton deleteExprSelectionButton;
    JButton resetExprButton;
    JButton undoButton;
    JButton redoButton;
    JButton editExprButton;
    JButton additionButton;
    JButton addAdditionPlaceHolderButton;
    JButton subtractionButton;
    JButton scalarButton;
    JButton numberExpressionButton;
    SpinnerNumberModel numberModel;
    ColorExpressionDialogPanel inputPanel;
    ColorExpressionDialogPanel outputPanel;
    JSpinner colorExpressionWeightSpinner;

}
