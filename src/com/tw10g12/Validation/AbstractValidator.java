package com.tw10g12.Validation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
 
/**
 * This class handles most of the details of validating a component, including
 * all display elements such as popup help boxes and color changes.
 * 
 * @see WantsValidationStatus
 */
 
public abstract class AbstractValidator extends InputVerifier implements KeyListener, FocusListener {
    private JDialog popup;
    private Object parent;
    private JLabel messageLabel;
    private JLabel image;
    private Point point;
    private Dimension cDim;
    private Color color;
	private List<WantsValidationStatus> validationListeners = new ArrayList<WantsValidationStatus>();
	private boolean wasValid = true;
	
    private AbstractValidator() {
        color = new Color(243, 255, 159);
    }
	
    private AbstractValidator(JComponent c, String message) {
        this();
        c.addKeyListener(this);
        c.addFocusListener(this);
        messageLabel = new JLabel(message + " ");
        image = new JLabel(new ImageIcon("exception_16x16.png"));
    }
	
    /**
     * @param parent A JDialog that implements the ValidationCapable interface.
     * @param c The JComponent to be validated.
     * @param message A message to be displayed in the popup help tip if validation fails.
     */
	
    public AbstractValidator (JDialog parent, JComponent c, String message) {		
        this(c, message);
        this.parent = parent;
        popup = new JDialog(parent);
        initComponents();
    }
	
    /**
     * @param parent A JFrame that implements the ValidationCapable interface.
     * @param c The JComponent to be validated.
     * @param message A message to be displayed in the popup help tip if validation fails.
     */
	
    public AbstractValidator (JFrame parent, JComponent c, String message) {
        this(c, message);
        this.parent = parent;
        popup = new JDialog(parent);
        initComponents();
    }
    
    /**
     * 
     * @param parent JPanel that implements the the ValidationCapable interface.
     * @param c The JComponent to be validated.
     * @param message A message to be displayed in the popup help tip if validation fails.
     */
    public AbstractValidator (JPanel parent, JComponent c, String message) {
        this(c, message);
        this.parent = parent;        
        popup = new JDialog();
       // popup.setContentPane(parent);
        initComponents();
    }
    /**
     * Implement the actual validation logic in this method. The method should
     * return false if data is invalid and true if it is valid. It is also possible
     * to set the popup message text with setMessage() before returning, and thus
     * customize the message text for different types of validation problems.
     * 
     * @param c The JComponent to be validated.
     * @return false if data is invalid. true if it is valid.
     */
	
    protected abstract boolean validationCriteria(JComponent c);
	
    /**
     * This method is called by Java when a component needs to be validated.
     * It should not be called directly. Do not override this method unless
     * you really want to change validation behavior. Implement
     * validationCriteria() instead.
     */
	
    public boolean verify(JComponent c, boolean showPopup)
    {
    	if (!validationCriteria(c)) {
			
            onValidationFailed(c);
            
            c.setBackground(Color.PINK);
            if(showPopup) showPopup(c);
            return false;
        }
        
        c.setBackground(Color.WHITE);
		
        onValidationPassed(c);
        
        return true;
    }
    
    public boolean verify(JComponent c) 
    {		
        return verify(c, false);
    }
    
    private void showPopup(JComponent c)
    {
    	popup.setSize(0, 0);
        popup.setLocationRelativeTo(c);
        point = popup.getLocation();
        cDim = c.getSize();
        popup.setLocation(point.x-(int)cDim.getWidth()/2,
            point.y+(int)cDim.getHeight()/2);
        popup.pack();
        popup.setVisible(true);
    }
	
    private void onValidationFailed(JComponent c)
    {
    	if(parent instanceof WantsValidationStatus)
            ((WantsValidationStatus)parent).validateFailed(c);
    	
		for(WantsValidationStatus listener : validationListeners)
		{
			listener.validateFailed(c);
		}
		wasValid = false;
    }
    
    private void onValidationPassed(JComponent c)
    {
    	if(parent instanceof WantsValidationStatus)
            ((WantsValidationStatus)parent).validatePassed(c);
		
    	for(WantsValidationStatus listener : validationListeners)
    	{
    		listener.validatePassed(c);
    	}
    	wasValid = true;
    }
    
    /**
     * Changes the message that appears in the popup help tip when a component's
     * data is invalid. Subclasses can use this to provide context sensitive help
     * depending on what the user did wrong.
     * 
     * @param message
     */
	
    protected void setMessage(String message) {
       messageLabel.setText(message);
    }
	
     /**
      * @see KeyListener
      */
	
    public void keyPressed(KeyEvent e) {
        popup.setVisible(false);
        
    }
	
    /**
     * @see KeyListener
     */
	
    public void keyTyped(KeyEvent e) {}
	
    /**
     * @see KeyListener
     */
	
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public boolean shouldYieldFocus(JComponent input)
    {
    	// TODO Auto-generated method stub
    	super.shouldYieldFocus(input);
    	return true;
    }
	
    @Override
	public void focusGained(FocusEvent e) 
    {
		// TODO Auto-generated method stub
    	if(!wasValid) verify((JComponent)e.getComponent(), true);
	}

	@Override
	public void focusLost(FocusEvent e) {
		 popup.setVisible(false);
		 popup.dispose();
		 if(parent instanceof JComponent)
	            ((JComponent)parent).revalidate();
		
	}

	private void initComponents() {
       // popup.getContentPane().setLayout(new FlowLayout());
        popup.setUndecorated(true);
        popup.getContentPane().setBackground(color);
        popup.getContentPane().add(image);
        popup.getContentPane().add(messageLabel);
        popup.setFocusableWindowState(false);
        popup.setResizable(false);
    }

	public void addValidationListener(WantsValidationStatus listener)
	{
		validationListeners.add(listener);
	}
	
	public boolean removeValidationListener(WantsValidationStatus listener)
	{
		return validationListeners.remove(listener);
	}
}