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

    public boolean isRgbUsed() {
        return rgbUsed;
    }

    public void setRgbUsed(boolean rgbUsed) {
        this.rgbUsed = rgbUsed;
    }

    public Map<ColorPreset, ThemeVariableDetails> getDetailsMap() {
        return detailsMap;
    }

    public void setDetailsMap(Map<ColorPreset, ThemeVariableDetails> detailsMap) {
        this.detailsMap = detailsMap;
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

    public ThemeVariableDetails getThemeVariableDetails(ColorPreset colorPreset) {
        if (colorPreset == null) {
            return null;
        }

        ThemeVariableDetails details = detailsMap.get(colorPreset);
        if (details == null && colorPreset.getParent() != null) {
            details = detailsMap.get(colorPreset.getParent());
        }

        if (details == null) {
            ColorPreset lightColorPreset = getDefaultColorPreset();
            if (lightColorPreset != null) {
                details = detailsMap.get(lightColorPreset);
            }
        }

        return details;
    }

    public ColorPreset getDefaultColorPreset() {
        return detailsMap.keySet().stream()
                .filter(preset -> ColorPresets.LIGHT.equals(preset.getName()))
                .findFirst()
                .orElse(null);
    }

    public boolean hasColorPreset(ColorPreset colorPreset) {
        return detailsMap.keySet().stream()
                .anyMatch(preset -> preset.equals(colorPreset));
    }
}
