package com.tw10g12.Validation;

import javax.swing.*;

/**
 * Created by Tom on 03/03/2015.
 */
public class DecimalValidator extends AbstractTextFieldValidator
{

    private int decimcalPlaces;
    private float min;
    private float max;

    public DecimalValidator(JPanel parent, JTextField textField, String message, int decimalPlaces, float min, float max)
    {
        super(parent, textField, message);
        this.decimcalPlaces = decimalPlaces;
        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean validationCriteria(JComponent c)
    {
        if(!(c instanceof JTextField)) return false;
        JTextField field = (JTextField)c;
        try
        {
            float val = Float.parseFloat(field.getText());
            if(val < min) return false;
            if(val > max) return false;
        }
        catch (NumberFormatException ex)
        {
            return false;
        }
        return true;
    }
}
