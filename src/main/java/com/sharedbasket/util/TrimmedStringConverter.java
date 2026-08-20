package com.sharedbasket.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/** Maps a blank submitted value to null so required="true" treats whitespace as a missing value. */
@FacesConverter("trimmedString")
public class TrimmedStringConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value == null ? "" : value.toString();
    }
}