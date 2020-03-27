package com.haulmont.editor.helium.web.screens.download;

import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.export.ByteArrayDataProvider;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.export.ExportFormat;
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

    public static final String COLOR_PRESET_PARAM = "colorPreset";
    public static final String TEXT_PARAM = "text";

    protected static final String FILE_NAME = "helium-ext-defaults.scss";

    @WindowParam(name = COLOR_PRESET_PARAM)
    protected String colorPreset;
    @WindowParam(name = TEXT_PARAM)
    protected String text;

    @Inject
    protected TextArea<String> textArea;
    @Inject
    private Label<String> firstStepLabel;

    @Inject
    private Messages messages;
    @Inject
    private Notifications notifications;
    @Inject
    private ExportDisplay exportDisplay;

    @Subscribe
    public void onInit(InitEvent event) {
        firstStepLabel.setValue(messages.formatMessage(DownloadScreen.class, "firstStep", colorPreset));
        textArea.setValue(text);
    }

    @Subscribe("clipboardBtn")
    public void onClipboardBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption(messages.getMessage(DownloadScreen.class, "clipboardNotification"))
                .show();
    }

    @Subscribe("downloadBtn")
    public void onDownloadBtnClick(Button.ClickEvent event) {
        exportDisplay.show(
                new ByteArrayDataProvider(text.getBytes()),
                FILE_NAME,
                ExportFormat.TEXT);
    }

    @Subscribe("closeBtn")
    public void onCloseBtnClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }
}