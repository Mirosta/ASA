package com.tw10g12.Validation;

import javax.swing.JComponent;

public interface WantsValidationStatus {
	
	    void validateFailed(JComponent component);  // Called when a component has failed validation.
	    void validatePassed(JComponent component);  // Called when a component has passed validation.
	
}
