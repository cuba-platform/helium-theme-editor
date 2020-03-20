package com.haulmont.editor.helium.web.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Theme variable
 */
public class ThemeVariable {

    protected String module;
    protected String name;
    protected Map<ColorPreset, ThemeVariableDetails> detailsMap = new HashMap<>();

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

    public Map<ColorPreset, ThemeVariableDetails> getDetailsMap() {
        return detailsMap;
    }

    public void setDetailsMap(Map<ColorPreset, ThemeVariableDetails> detailsMap) {
        this.detailsMap = detailsMap;
    }

    public void setThemeVariableDetails(String preset, ThemeVariableDetails details) {
        ColorPreset colorPreset = ColorPreset.fromId(preset);
        setThemeVariableDetails(colorPreset, details);
    }

    public void setThemeVariableDetails(ColorPreset colorPreset, ThemeVariableDetails details) {
        if (colorPreset != null) {
            if (detailsMap.containsKey(colorPreset)) {
                detailsMap.replace(colorPreset, details);
            } else {
                detailsMap.put(colorPreset, details);
            }
        }
    }

    public ThemeVariableDetails getThemeVariableDetails() {
        return detailsMap.get(ColorPreset.LIGHT);
    }

    public ThemeVariableDetails getThemeVariableDetails(String preset) {
        ColorPreset colorPreset = ColorPreset.fromId(preset);
        return getThemeVariableDetails(colorPreset);
    }

    public ThemeVariableDetails getThemeVariableDetails(ColorPreset colorPreset) {
        return detailsMap.get(colorPreset);
    }
}
