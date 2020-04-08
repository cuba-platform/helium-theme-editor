package com.haulmont.editor.helium.web.tools;

/**
 * Color preset
 */
public class ColorPreset {

    protected String name;
    protected ColorPreset parent;

    public ColorPreset() {
    }

    public ColorPreset(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ColorPreset getParent() {
        return parent;
    }

    public void setParent(ColorPreset parent) {
        this.parent = parent;
    }
}
