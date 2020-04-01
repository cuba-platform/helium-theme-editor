package com.haulmont.editor.helium.web.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Theme variable
 */
public class ThemeVariable {

    protected String module;
    protected String name;
    protected boolean rgbUsed;
    protected Map<String, ThemeVariableDetails> detailsMap = new HashMap<>();

    public ThemeVariable() {
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRgbUsed() {
        return rgbUsed;
    }

    public void setRgbUsed(boolean rgbUsed) {
        this.rgbUsed = rgbUsed;
    }

    public Map<String, ThemeVariableDetails> getDetailsMap() {
        return detailsMap;
    }

    public void setDetailsMap(Map<String, ThemeVariableDetails> detailsMap) {
        this.detailsMap = detailsMap;
    }

    public void setThemeVariableDetails(String colorPreset, ThemeVariableDetails details) {
        if (colorPreset != null) {
            if (detailsMap.containsKey(colorPreset)) {
                detailsMap.replace(colorPreset, details);
            } else {
                detailsMap.put(colorPreset, details);
            }
        }
    }

    public ThemeVariableDetails getThemeVariableDetails() {
        return detailsMap.get(ColorPresets.LIGHT);
    }

    public ThemeVariableDetails getThemeVariableDetails(String colorPreset) {
        return detailsMap.get(colorPreset);
    }
}
