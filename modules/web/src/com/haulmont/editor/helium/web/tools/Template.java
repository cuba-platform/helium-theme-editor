package com.haulmont.editor.helium.web.tools;

/**
 * Color template
 */
public class Template {

    protected String name;
    protected Template parent;

    public Template() {
    }

    public Template(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Template getParent() {
        return parent;
    }

    public void setParent(Template parent) {
        this.parent = parent;
    }
}
