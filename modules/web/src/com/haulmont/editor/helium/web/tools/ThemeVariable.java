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
    protected Map<Template, ThemeVariableDetails> detailsMap = new HashMap<>();

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

    public Map<Template, ThemeVariableDetails> getDetailsMap() {
        return detailsMap;
    }

    public void setDetailsMap(Map<Template, ThemeVariableDetails> detailsMap) {
        this.detailsMap = detailsMap;
    }

    public void setThemeVariableDetails(Template template, ThemeVariableDetails details) {
        if (template != null) {
            if (detailsMap.containsKey(template)) {
                detailsMap.replace(template, details);
            } else {
                detailsMap.put(template, details);
            }
        }
    }

    public ThemeVariableDetails getThemeVariableDetails(Template template) {
        if (template == null) {
            return null;
        }

        ThemeVariableDetails details = detailsMap.get(template);
        if (details == null && template.getParent() != null) {
            details = detailsMap.get(template.getParent());
        }

        if (details == null) {
            Template lightTemplate = getDefaultColorTemplate();
            if (lightTemplate != null) {
                details = detailsMap.get(lightTemplate);
            }
        }

        return details;
    }

    public Template getDefaultColorTemplate() {
        return detailsMap.keySet().stream()
                .filter(template -> Templates.LIGHT.equals(template.getName()))
                .findFirst()
                .orElse(null);
    }

    public boolean hasColorTemplate(Template template) {
        return detailsMap.keySet().stream()
                .anyMatch(colorTemplate -> colorTemplate.equals(template));
    }
}
