package com.sharedbasket.util;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;

@FacesValidator("currencyScale")
public class CurrencyScaleValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        BigDecimal amount = value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Estimated price can have at most 2 decimal places", null));
        }
    }
}
