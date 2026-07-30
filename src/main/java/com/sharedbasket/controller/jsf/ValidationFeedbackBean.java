package com.sharedbasket.controller.jsf;

import com.sharedbasket.util.ValidationUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "validationFeedbackBean")
@RequestScoped
public class ValidationFeedbackBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean hasFieldError(String fieldName) {
        return ValidationUtil.hasFieldError(fieldName, FacesContext.getCurrentInstance());
    }

    public String getFieldError(String fieldName) {
        return ValidationUtil.getFieldError(fieldName, FacesContext.getCurrentInstance());
    }
}
