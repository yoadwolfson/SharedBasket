package com.sharedbasket.util;

import com.sharedbasket.domain.ItemUnit;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.math.BigDecimal;

@FacesValidator("wholeNumberForPiece")
public class WholeNumberForPieceValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if (value == null) {
            return;
        }
        UIComponent itemUnitComponent = component.getParent() == null ? null : component.getParent().findComponent("itemUnit");
        if (itemUnitComponent == null) {
            return;
        }
        Object unitValue = itemUnitComponent.getAttributes().get("value");
        if (!(unitValue instanceof ItemUnit) || unitValue != ItemUnit.PIECE) {
            return;
        }
        BigDecimal quantity = value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
        if (quantity.stripTrailingZeros().scale() > 0) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Quantity must be a whole number for pieces", null));
        }
    }
}
