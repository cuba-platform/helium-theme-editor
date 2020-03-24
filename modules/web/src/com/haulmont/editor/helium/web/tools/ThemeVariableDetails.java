package com.haulmont.editor.helium.web.tools;

/**
 * Theme variable details
 */
public class ThemeVariableDetails {

    protected String value;
    protected String colorModifier;
    protected String colorModifierValue;
    protected String placeHolder;
    protected ThemeVariable parentThemeVariable;

    public ThemeVariableDetails() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColorModifier() {
        return colorModifier;
    }

    public void setColorModifier(String colorModifier) {
        this.colorModifier = colorModifier;
    }

    public String getColorModifierValue() {
        return colorModifierValue;
    }

    public void setColorModifierValue(String colorModifierValue) {
        this.colorModifierValue = colorModifierValue;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public ThemeVariable getParentThemeVariable() {
        return parentThemeVariable;
    }

    public void setParentThemeVariable(ThemeVariable parentThemeVariable) {
        this.parentThemeVariable = parentThemeVariable;
    }
}
