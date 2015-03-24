package com.tw10g12.Validation;

import javax.swing.JComponent;

public class FormValidatorGroup extends AbstractValidationGroup
{
	@Override
	public void addDependentComponent(JComponent dependentComponent)
	{
		super.addDependentComponent(dependentComponent);
	}
	
	@Override
	public void validateFailed(JComponent component)
	{
		super.validateFailed(component);
		updateComponentStates();
	}
	
	@Override
	public void validatePassed(JComponent component)
	{
		super.validatePassed(component);
		updateComponentStates();
	}
	
	public void initialCheck()
	{
		updateComponentStates();
	}
	
	private void updateComponentStates()
	{
		boolean enable = true;
		
		for(JComponent validator : validators)
		{
			System.out.println(validator.isVisible());
			System.out.println(validator.getParent().isVisible());
			System.out.println();
			if(validator.isVisible() && validator.getParent().isVisible())
			{
				if(!validComponents.containsKey(validator))
				{
					boolean valid = ((AbstractValidator)validator.getInputVerifier()).validationCriteria(validator);
					validComponents.put(validator, valid);
					if(!valid)
					{
						enable = false;
						break;
					}
				}
				else
				{
					if(!validComponents.get(validator))
					{
						enable = false;
						break;
					}
				}
			}
		}
		
		for(JComponent component : dependentComponents)
		{
			component.setEnabled(enable);
		}
	}
}
