package com.tw10g12.Validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

public abstract class AbstractValidationGroup implements WantsValidationStatus
{
	protected Set<JComponent> validators = new HashSet<JComponent>();
	protected List<JComponent> dependentComponents = new ArrayList<JComponent>();
	protected Map<JComponent, Boolean> validComponents = new HashMap<JComponent, Boolean>();
	
	public void addValidator(JComponent validatedComponent)
	{
		if(validatedComponent.getInputVerifier() instanceof AbstractValidator)
		{
			validators.add(validatedComponent);
			AbstractValidator validator = (AbstractValidator)validatedComponent.getInputVerifier();
			validator.addValidationListener(this);
		}
		else throw new RuntimeException("Input verifier must be an instance of AbstractValidator");
	}
	
	public void addDependentComponent(JComponent dependentComponent)
	{
		dependentComponents.add(dependentComponent);
	}

	@Override
	public void validateFailed(JComponent component)
	{
		if(validators.contains(component))
		{
			validComponents.put(component, false);
		}
	}

	@Override
	public void validatePassed(JComponent component)
	{
		if(validators.contains(component))
		{
			validComponents.put(component, true);
		}
	}
}
