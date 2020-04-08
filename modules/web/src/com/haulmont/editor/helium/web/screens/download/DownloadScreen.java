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

    public static final String BASE_THEME_PARAM = "baseTheme";
    public static final String TEXT_PARAM = "text";

    protected static final String FILE_NAME = "helium-ext-defaults.scss";

    @WindowParam(name = BASE_THEME_PARAM)
    protected String baseTheme;
    @WindowParam(name = TEXT_PARAM)
    protected String text;

    @Inject
    protected TextArea<String> textArea;
    @Inject
    protected Label<String> firstStepLabel;

    @Inject
    protected Messages messages;
    @Inject
    protected Notifications notifications;
    @Inject
    protected ExportDisplay exportDisplay;

    @Subscribe
    public void onInit(InitEvent event) {
        firstStepLabel.setValue(messages.formatMessage(DownloadScreen.class, "firstStep", baseTheme));
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