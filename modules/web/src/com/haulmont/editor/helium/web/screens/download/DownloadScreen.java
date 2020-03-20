package com.haulmont.editor.helium.web.screens.download;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.screen.DialogMode;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

import javax.inject.Inject;

@UiController("helium_DownloadScreen")
@UiDescriptor("download-screen.xml")
@DialogMode(forceDialog = true)
public class DownloadScreen extends Screen {

    public static final String TEXT_PARAM = "text";

    @WindowParam(name = TEXT_PARAM)
    protected String text;
    @Inject
    protected TextArea<String> textArea;

    @Subscribe
    public void onInit(InitEvent event) {
        textArea.setValue(text);
    }
}