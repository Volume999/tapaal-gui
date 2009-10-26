package pipe.gui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.SpinnerNumberModel;

import pipe.dataLayer.Constant;
import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.undo.UndoableEdit;

/*
 * LeftConstantsPane.java
 *
 * Created on 08-10-2009, 13:51:42
 */

/**
 *
 * @author Morten Jacobsen
 */
public class ConstantsDialogPanel extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6734583459331431789L;
	private JRootPane rootPane;
	private DataLayer model;
	
	private String name;

	/** Creates new form LeftConstantsPane */
    public ConstantsDialogPanel() {
        initComponents();
    }
    
    public ConstantsDialogPanel(JRootPane pane, DataLayer model, Constant constant) {
        initComponents();
        
        rootPane = pane;
        this.model = model;
 
        this.name = constant.getName();
        
        // Set up initial values
       SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
    		   constant.getValue(), constant.getLowerBound(),	constant.getUpperBound(), 1);
       valueSpinner.setModel(spinnerModel);
       nameTextField.setText(name);
       nameTextField.addKeyListener(new KeyAdapter(){
    	   private static final int MAX_CHARS = 15;
    	   public void keyTyped(KeyEvent e){
    		   char c = e.getKeyChar();
    		   if(!Character.isLetter(c) && !Character.isISOControl(c))
    			   e.consume();
    		   
    		   if(Character.isLetter(c) && nameTextField.getText().length() == MAX_CHARS)
    			   e.consume();
    	   }
       });

       // wire up buttons
       okButton.addActionListener(new ActionListener(){

    	   public void actionPerformed(ActionEvent e){
    		   onOK();
    	   }
       });

       cancelButton.addActionListener(new ActionListener(){
    	   public void actionPerformed(ActionEvent e){
    		   exit();
    	   }
       });
       
       rootPane.setDefaultButton(okButton);
    }
    
    public void onOK()
    {
    	String newName = nameTextField.getText();
    	
    	if(newName.trim().isEmpty())
    	{
    		JOptionPane.showMessageDialog(CreateGui.getApp(), 
    				"You must specify a name.", 
    				"Missing name", 
    				JOptionPane.ERROR_MESSAGE);
    	}
    	else
    	{
    		int val = (Integer)valueSpinner.getValue();
       		
    		if(name != ""){
    			UndoableEdit edit = model.updateConstant(name, new Constant(newName, val));
    			if(edit == null){
    				JOptionPane.showMessageDialog(CreateGui.getApp(),
    						"The specified value is invalid for the current net.\n" +
    						"Updating the constant to the specified value invalidates the guard\n" + 
    						"on one or more ars.",
    						"Constant value invalid for current net",
    						JOptionPane.ERROR_MESSAGE);
    			}else{
    				CreateGui.getView().getUndoManager().addNewEdit(edit);
    			}
    		}
    		else{
    			UndoableEdit edit = model.addConstant(newName, val);
    			if(edit == null){
    				JOptionPane.showMessageDialog(CreateGui.getApp(),
    						"A constant with the specified name already exists.",
    						"Constant exists",
    						JOptionPane.ERROR_MESSAGE);
    			}
    			else
    				CreateGui.getView().getUndoManager().addNewEdit(edit);
    		}
    		
    		model.buildConstraints();
    	}    	
    	
   	   	exit();
    }
    
    public void exit()
    {
    	rootPane.getParent().setVisible(false);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameTextField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        valueSpinner = new javax.swing.JSpinner();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        nameLabel.setText("Name:");

        valueLabel.setText("Value:");

        okButton.setText("OK");

        cancelButton.setText("Cancel");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(nameLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(valueLabel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(valueSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valueLabel)
                    .addComponent(valueSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JSpinner valueSpinner;
    // End of variables declaration//GEN-END:variables

}
