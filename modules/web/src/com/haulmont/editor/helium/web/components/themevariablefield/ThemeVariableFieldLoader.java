package com.haulmont.editor.helium.web.components.themevariablefield;

import com.haulmont.cuba.gui.xml.layout.loaders.AbstractFieldLoader;

public class ThemeVariableFieldLoader extends AbstractFieldLoader<ThemeVariableField> {

    @Override
    public void createComponent() {
        resultComponent = factory.create(ThemeVariableField.NAME);
        loadId(resultComponent, element);
    }
}
