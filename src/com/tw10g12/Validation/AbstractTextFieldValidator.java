package com.tw10g12.Validation;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class AbstractTextFieldValidator extends AbstractValidator implements DocumentListener
{
	JTextField textField;
	
	private AbstractTextFieldValidator(JTextField textField, String message) 
	{
		this((JDialog)null, textField, message);
	}
	
	public AbstractTextFieldValidator(JDialog parent, JTextField textField, String message)
	{
		super(parent, textField, message);
		this.textField = textField;
		textField.getDocument().addDocumentListener(this);
	}
	
	public AbstractTextFieldValidator(JFrame parent, JTextField textField, String message)
	{
		super(parent, textField, message);
		this.textField = textField;
		textField.getDocument().addDocumentListener(this);
	}
	
	public AbstractTextFieldValidator(JPanel parent, JTextField textField, String message)
	{
		super(parent, textField, message);
		this.textField = textField;
		textField.getDocument().addDocumentListener(this);
	}
	
	@Override
	public void changedUpdate(DocumentEvent e)
	{
		this.verify(textField);
	}
	@Override
	public void insertUpdate(DocumentEvent e)
	{
		this.verify(textField);
	}
	@Override
	public void removeUpdate(DocumentEvent e)
	{
		this.verify(textField);
	}
}
