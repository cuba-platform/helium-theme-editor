package com.haulmont.editor.helium.web.screens.mainscreen;

import com.google.common.collect.ImmutableMap;
import com.haulmont.addon.helium.web.theme.HeliumThemeVariantsManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.screen.MapScreenOptions;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.app.UserSettingsTools;
import com.haulmont.cuba.web.app.main.MainScreen;
import com.haulmont.cuba.web.events.UIRefreshEvent;
import com.haulmont.cuba.web.theme.HaloTheme;
import com.haulmont.editor.helium.web.components.themevariablefield.ThemeVariableField;
import com.haulmont.editor.helium.web.screens.download.DownloadScreen;
import com.haulmont.editor.helium.web.tools.ColorPreset;
import com.haulmont.editor.helium.web.tools.ThemeVariable;
import com.haulmont.editor.helium.web.tools.ThemeVariableDetails;
import com.haulmont.editor.helium.web.tools.ThemeVariableUtils;
import com.haulmont.editor.helium.web.tools.ThemeVariablesManager;
import org.springframework.context.event.EventListener;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.haulmont.editor.helium.web.components.themevariablefield.ThemeVariableField.RGB_POSTFIX;

@UiController("respMainScreen")
@UiDescriptor("resp-main-screen.xml")
public class RespMainScreen extends MainScreen {

    protected static final String BASIC_MODULE_NAME = "Basic";
    protected static final String GROUPBOX_PADDING_LESS_STYLENAME = "padding-less";
    protected static final String GROUPBOX_POSTFIX = "-box";
    protected static final String THEME_VARIABLE_FIELD_POSTFIX = "-field";

    @Inject
    protected HeliumThemeVariantsManager variantsManager;
    @Inject
    protected UserSettingsTools userSettingsTools;
    @Inject
    protected ThemeVariablesManager themeVariablesManager;
    @Inject
    protected UiComponents uiComponents;
    @Inject
    protected ScreenBuilders screenBuilders;
    @Inject
    protected Dialogs dialogs;
    @Inject
    protected Messages messages;

    @Inject
    protected ScrollBoxLayout settingsBox;
    @Inject
    protected LookupField<ColorPreset> colorPresetField;
    @Inject
    protected LookupField<String> sizeField;
    @Inject
    protected CssLayout sideMenuPanel;
    @Inject
    protected AppWorkArea workArea;
    @Inject
    protected HBoxLayout horizontalWrap;

    protected String appWindowTheme;
    protected Map<String, String> modifiedThemeVariables = new HashMap<>();
    protected ColorPreset colorPreset = ColorPreset.LIGHT;

    @Subscribe
    public void onInit(InitEvent event) {
        appWindowTheme = userSettingsTools.loadAppWindowTheme();

        initColorPresetField();
        initSizeField();
        initThemeVariablesFields();

        updateAdvancedBoxesVisible(false);
        updateMainScreenStyleName();
    }

    @EventListener
    public void onUIRefresh(UIRefreshEvent event) {
        colorPresetField.setValue(colorPreset);
        modifiedThemeVariables.clear();
    }

    @Subscribe("colorPresetField")
    public void onColorPresetFieldValueChange(HasValue.ValueChangeEvent<ColorPreset> event) {
        if (ColorPreset.CUSTOM.equals(event.getPrevValue())
                && event.isUserOriginated()) {
            dialogs.createOptionDialog(Dialogs.MessageType.WARNING)
                    .withCaption(messages.getMessage(RespMainScreen.class, "warningNotification.caption"))
                    .withContentMode(ContentMode.HTML)
                    .withMessage(messages.getMessage(RespMainScreen.class, "warningNotification.message"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK)
                                    .withHandler(actionPerformedEvent -> updateColorPreset(event.getValue())),
                            new DialogAction(DialogAction.Type.CANCEL)
                                    .withHandler(actionPerformedEvent -> colorPresetField.setValue(event.getPrevValue()))
                    )
                    .show();
        } else if (!ColorPreset.CUSTOM.equals(event.getValue())) {
            updateColorPreset(event.getValue());
        }
    }

    @Subscribe("sizeField")
    public void onSizeFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        updateMainScreenStyleName();
    }


    @Subscribe("mobileMenuButton")
    public void onMobileMenuButtonClick(Button.ClickEvent event) {
        String stylename = sideMenuPanel.getStyleName();
        if (stylename != null
                && !stylename.isEmpty()
                && stylename.contains(HaloTheme.SIDEMENU_PANEL_OPEN)) {
            sideMenuPanel.removeStyleName(HaloTheme.SIDEMENU_PANEL_OPEN);
        } else {
            sideMenuPanel.addStyleName(HaloTheme.SIDEMENU_PANEL_OPEN);
        }
    }

    @Subscribe("resetBtn")
    public void onResetBtnClick(Button.ClickEvent event) {
        if (ColorPreset.CUSTOM.equals(colorPresetField.getValue())) {
            dialogs.createOptionDialog(Dialogs.MessageType.WARNING)
                    .withCaption(messages.getMessage(RespMainScreen.class, "warningNotification.caption"))
                    .withContentMode(ContentMode.HTML)
                    .withMessage(messages.getMessage(RespMainScreen.class, "warningNotification.message"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK)
                                    .withHandler(actionPerformedEvent -> {
                                        updateFieldsByColorPreset(ColorPreset.LIGHT);
                                        modifiedThemeVariables = new HashMap<>();
                                        resetValues();
                                    }),
                            new DialogAction(DialogAction.Type.CANCEL)
                    )
                    .show();
        } else {
            resetValues();
        }
    }

    @Subscribe("downloadBtn")
    public void onDownloadBtnClick(Button.ClickEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(DownloadScreen.class)
                .withOptions(new MapScreenOptions(
                        ImmutableMap.of(
                                DownloadScreen.COLOR_PRESET_PARAM,
                                colorPreset.getId(),
                                DownloadScreen.TEXT_PARAM,
                                generateDownloadText()
                        )
                ))
                .show();
    }

    @Subscribe("advancedModeValue")
    public void onAdvancedModeValueValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        updateAdvancedBoxesVisible(event.getValue() != null ? event.getValue() : false);
    }

    protected void initColorPresetField() {
        List<ColorPreset> colorPresetList = Arrays.asList(ColorPreset.LIGHT, ColorPreset.DARK, ColorPreset.BLUE);
        colorPresetField.setOptionsList(colorPresetList);
        colorPresetField.setValue(colorPreset);
    }

    protected void initSizeField() {
        sizeField.setOptionsList(variantsManager.getAppThemeSizeList());
        sizeField.setValue(variantsManager.loadUserAppThemeSizeSetting());
    }

    protected void initThemeVariablesFields() {
        for (ThemeVariable themeVariable : getDefaultThemeVariables()) {
            String module = themeVariable.getModule();

            Component groupBoxLayout = settingsBox.getComponent(module.toLowerCase() + GROUPBOX_POSTFIX);
            if (groupBoxLayout == null) {
                groupBoxLayout = createGroupBoxLayout(module);

                if (module.equals(BASIC_MODULE_NAME)) {
                    ((GroupBoxLayout) groupBoxLayout).setExpanded(true);
                    settingsBox.add(groupBoxLayout, 1);
                } else {
                    settingsBox.add(groupBoxLayout);
                }
            }

            ThemeVariableField field = createThemeVariableField(themeVariable);
            ((GroupBoxLayout) groupBoxLayout).add(field);
        }
    }

    protected List<ThemeVariable> getDefaultThemeVariables() {
        return themeVariablesManager.getThemeVariables();
    }

    protected GroupBoxLayout createGroupBoxLayout(String id) {
        GroupBoxLayout groupBoxLayout = uiComponents.create(GroupBoxLayout.class);
        groupBoxLayout.setSpacing(true);
        groupBoxLayout.setId(id.toLowerCase() + GROUPBOX_POSTFIX);
        groupBoxLayout.setCaption(id);
        groupBoxLayout.setCollapsable(true);
        groupBoxLayout.setExpanded(false);
        groupBoxLayout.setWidth("100%");
        groupBoxLayout.setStyleName(GROUPBOX_PADDING_LESS_STYLENAME);
        return groupBoxLayout;
    }

    protected ThemeVariableField createThemeVariableField(ThemeVariable themeVariable) {
        ThemeVariableField themeVariableField = uiComponents.create(ThemeVariableField.NAME);
        themeVariableField.setValue(themeVariable);
        themeVariableField.setId(themeVariable.getName() + THEME_VARIABLE_FIELD_POSTFIX);

        themeVariableField.addColorValueChangeListener(valueChangeEvent -> {
            updateThemeVariable(themeVariable.getName(), valueChangeEvent.getValue());

            if (themeVariable.isRgbUsed()) {
                updateThemeVariable(themeVariable.getName() + RGB_POSTFIX,
                        ThemeVariableUtils.convertHexToRGB(valueChangeEvent.getValue()));
            }

            ColorPreset newColorPreset = modifiedThemeVariables.isEmpty()
                    ? colorPreset
                    : ColorPreset.CUSTOM;
            colorPresetField.setValue(newColorPreset);
        });

        return themeVariableField;
    }

    protected void updateColorPreset(ColorPreset newColorPreset) {
        if (newColorPreset != ColorPreset.CUSTOM
                && newColorPreset != colorPreset) {
            colorPreset = newColorPreset;
        }

        updateMainScreenStyleName();
        updateFieldsByColorPreset(newColorPreset);
    }

    protected void updateMainScreenStyleName() {
        String colorPresetValue = colorPreset == null
                ? ""
                : colorPreset.getId();

        workArea.setStyleName(appWindowTheme + " " + colorPresetValue + " " + sizeField.getValue());
        horizontalWrap.setStyleName(appWindowTheme + " " + colorPresetValue);
    }

    protected void resetValues() {
        colorPresetField.setValue(ColorPreset.LIGHT);
        modifiedThemeVariables.clear();
        sizeField.setValue(variantsManager.loadUserAppThemeSizeSetting());
        updateMainScreenStyleName();
    }

    protected void updateAdvancedBoxesVisible(boolean value) {
        settingsBox.getOwnComponentsStream()
                .skip(3) // skip Screen defaults and Basic groupboxes
                .forEach(component -> component.setVisible(value));
    }

    protected void updateFieldsByColorPreset(ColorPreset colorPresetValue) {
        settingsBox.getComponents()
                .forEach(component -> {
                    if (component instanceof ThemeVariableField) {
                        ((ThemeVariableField) component).setValueByPreset(colorPresetValue);
                    }
                });
    }

    protected void updateThemeVariable(String themeVariableName, String value) {
        updateModifiedThemeVariables(themeVariableName, value);
        updateChildThemeVariables(themeVariableName, value);
    }

    protected void updateModifiedThemeVariables(String themeVariableName, String value) {
        if (value == null) {
            modifiedThemeVariables.remove(themeVariableName);
        } else if (modifiedThemeVariables.containsKey(themeVariableName)) {
            modifiedThemeVariables.replace(themeVariableName, value);
        } else {
            modifiedThemeVariables.put(themeVariableName, value);
        }
    }

    protected void updateChildThemeVariables(String variableName, String value) {
        List<ThemeVariable> childrenThemeVariables = getChildrenThemeVariables(variableName);

        childrenThemeVariables.forEach(themeVariable -> {
            ThemeVariableField themeVariableField =
                    (ThemeVariableField) settingsBox.getComponent(themeVariable.getName() + THEME_VARIABLE_FIELD_POSTFIX);
            if (themeVariableField != null) {
                themeVariableField.setColorValueByParent(value);
            }
        });
    }

    protected List<ThemeVariable> getChildrenThemeVariables(String variableName) {
        List<ThemeVariable> childrenThemeVariables = new ArrayList<>();
        for (ThemeVariable themeVariable : getDefaultThemeVariables()) {
            ThemeVariableDetails themeVariableDetails = themeVariable.getThemeVariableDetails(colorPreset);
            if (themeVariableDetails == null) {
                themeVariableDetails = themeVariable.getThemeVariableDetails();
            }

            if (themeVariableDetails != null) {
                ThemeVariable parentThemeVariable = themeVariableDetails.getParentThemeVariable();
                if (parentThemeVariable != null
                        && variableName.equals(parentThemeVariable.getName())) {
                    childrenThemeVariables.add(themeVariable);
                    childrenThemeVariables.addAll(getChildrenThemeVariables(themeVariable.getName()));
                }
            }
        }
        return childrenThemeVariables;
    }

    protected String generateDownloadText() {
        StringBuilder builder = new StringBuilder();
        builder.append(".helium.")
                .append(colorPreset.getId())
                .append(" {\n");

        for (Map.Entry<String, String> entry : modifiedThemeVariables.entrySet()) {
            builder.append("  ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append(";\n");
        }

        builder.append("}");
        return builder.toString();
    }
}