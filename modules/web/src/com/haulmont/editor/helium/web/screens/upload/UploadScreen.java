package com.haulmont.editor.helium.web.screens.upload;

import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.HasValue;
import com.haulmont.cuba.gui.components.RadioButtonGroup;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.components.data.Options;
import com.haulmont.cuba.gui.screen.DialogMode;
import com.haulmont.cuba.gui.screen.Install;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.StandardOutcome;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.editor.helium.web.tools.ModifiedThemeVariableDetails;
import com.haulmont.editor.helium.web.tools.Template;
import com.haulmont.editor.helium.web.tools.ThemeVariablesManager;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@UiController("helium_UploadScreen")
@UiDescriptor("upload-screen.xml")
@DialogMode(forceDialog = true)
public class UploadScreen extends Screen {

    public static final String BASE_THEME_MODE_PARAM = "baseThemeMode";
    public static final String BASE_THEME_MODES_PARAM = "baseThemeModes";

    @WindowParam(name = BASE_THEME_MODE_PARAM)
    protected Template baseThemeMode;

    @WindowParam(name = BASE_THEME_MODES_PARAM)
    protected Options<Template> baseThemeModes;

    @Inject
    protected RadioButtonGroup<Template> baseThemeModeField;
    @Inject
    protected TextArea<String> textArea;

    @Inject
    protected ThemeVariablesManager themeVariablesManager;
    @Inject
    protected Button applyBtn;

    protected List<ModifiedThemeVariableDetails> uploadedThemeVariables = new ArrayList<>();

    public List<ModifiedThemeVariableDetails> getUploadedThemeVariables() {
        return uploadedThemeVariables;
    }

    public Template getBaseThemeMode() {
        return baseThemeModeField.getValue();
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        initBaseThemeModeField();
    }

    protected void initBaseThemeModeField() {
        baseThemeModeField.setOptions(baseThemeModes);
        baseThemeModeField.setValue(baseThemeMode);
    }

    @Install(to = "baseThemeModeField", subject = "optionCaptionProvider")
    protected String baseThemeModeFieldOptionCaptionProvider(Template template) {
        return template.getName();
    }

    @Subscribe("textArea")
    protected void onTextAreaValueChange(HasValue.ValueChangeEvent<String> event) {
        if (event.isUserOriginated()) {
            if (event.getValue() == null) {
                uploadedThemeVariables.clear();
            } else {
                BufferedReader reader = new BufferedReader(new StringReader(event.getValue()));
                uploadedThemeVariables = themeVariablesManager.parseUploadedThemeVariables(reader);
            }
            updateTextArea();
        }
    }

    protected void updateTextArea() {
        if (!uploadedThemeVariables.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (ModifiedThemeVariableDetails details : uploadedThemeVariables) {
                builder.append(details.getName())
                        .append(": ")
                        .append(details.getValue())
                        .append(";")
                        .append("\n");
            }
            textArea.setValue(builder.toString());
        } else {
            textArea.clear();
        }

        updateApplyBtn();
    }

    protected void updateApplyBtn() {
        applyBtn.setEnabled(!uploadedThemeVariables.isEmpty());
    }

    @Subscribe("applyBtn")
    protected void onApplyBtnClick(Button.ClickEvent event) {
        uploadedThemeVariables = themeVariablesManager.updateThemeVariableDetailsByTemplate(uploadedThemeVariables, baseThemeModeField.getValue());
        close(StandardOutcome.COMMIT);
    }

    @Subscribe("cancelBtn")
    protected void onCancelBtnClick(Button.ClickEvent event) {
        close(StandardOutcome.CLOSE);
    }
}