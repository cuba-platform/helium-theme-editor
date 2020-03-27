package com.haulmont.editor.helium.web.screens.mainscreen;

import com.google.common.collect.ImmutableMap;
import com.haulmont.addon.helium.web.theme.HeliumThemeVariantsManager;
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
import com.haulmont.cuba.web.theme.HaloTheme;
import com.haulmont.editor.helium.web.components.themevariablefield.ThemeVariableField;
import com.haulmont.editor.helium.web.screens.download.DownloadScreen;
import com.haulmont.editor.helium.web.tools.ColorPreset;
import com.haulmont.editor.helium.web.tools.ThemeVariable;
import com.haulmont.editor.helium.web.tools.ThemeVariableDetails;
import com.haulmont.editor.helium.web.tools.ThemeVariableUtils;
import com.haulmont.editor.helium.web.tools.ThemeVariablesManager;

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

            Component groupBoxLayout = settingsBox.getComponent(module.toLowerCase() + "-box");
            if (groupBoxLayout == null) {
                groupBoxLayout = createGroupBoxLayout(module);

                if (module.equals("Basic")) {
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
        groupBoxLayout.setId(id.toLowerCase() + "-box");
        groupBoxLayout.setCaption(id);
        groupBoxLayout.setCollapsable(true);
        groupBoxLayout.setExpanded(false);
        groupBoxLayout.setWidth("100%");
        groupBoxLayout.setStyleName("padding-less");
        return groupBoxLayout;
    }

    protected ThemeVariableField createThemeVariableField(ThemeVariable themeVariable) {
        ThemeVariableField themeVariableField = uiComponents.create(ThemeVariableField.NAME);
        themeVariableField.setValue(themeVariable);
        themeVariableField.setId(themeVariable.getName() + "-field");

        themeVariableField.addColorValueChangeListener(valueChangeEvent -> {
            updateThemeVariable(themeVariable.getName(), valueChangeEvent.getValue());

            if (themeVariable.isRgbUsed()) {
                updateThemeVariable(themeVariable.getName() + RGB_POSTFIX,
                        ThemeVariableUtils.hexStringToRGB(valueChangeEvent.getValue()));
            }

            ColorPreset newColorPreset = modifiedThemeVariables.isEmpty()
                    ? colorPreset
                    : ColorPreset.CUSTOM;
            colorPresetField.setValue(newColorPreset);
        });

        return themeVariableField;
    }

    @Subscribe("colorPresetField")
    public void onColorPresetFieldValueChange(HasValue.ValueChangeEvent<ColorPreset> event) {
        if (ColorPreset.CUSTOM.equals(event.getPrevValue())
                && event.isUserOriginated()) {
            dialogs.createOptionDialog(Dialogs.MessageType.WARNING)
                    .withCaption("Warning")
                    .withContentMode(ContentMode.HTML)
                    .withMessage("Pay attention your changes will be discarded.<br/>Click <b>OK</b> to continue")
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

    protected void updateColorPreset(ColorPreset newColorPreset) {
        if (newColorPreset != ColorPreset.CUSTOM
                && newColorPreset != colorPreset) {
            colorPreset = newColorPreset;
        }

        updateMainScreenStyleName();
        updateFieldsByColorPreset(newColorPreset);
    }

    @Subscribe("sizeField")
    public void onSizeFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        updateMainScreenStyleName();
    }

    protected void updateMainScreenStyleName() {
        String colorPresetValue = colorPreset == null
                ? ""
                : colorPreset.getId();

        workArea.setStyleName(appWindowTheme + " " + colorPresetValue + " " + sizeField.getValue());
        horizontalWrap.setStyleName(appWindowTheme + " " + colorPresetValue);
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
                    .withCaption("Warning")
                    .withContentMode(ContentMode.HTML)
                    .withMessage("Pay attention your changes will be discarded.<br/>Click <b>OK</b> to continue")
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

    protected void resetValues() {
        colorPresetField.setValue(ColorPreset.LIGHT);
        sizeField.setValue(variantsManager.loadUserAppThemeSizeSetting());
        updateMainScreenStyleName();
    }

    @Subscribe("downloadBtn")
    public void onDownloadBtnClick(Button.ClickEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(DownloadScreen.class)
                .withOptions(new MapScreenOptions(
                        ImmutableMap.of(
                                DownloadScreen.TEXT_PARAM,
                                generateDownloadText()
                        )
                ))
                .show();
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

    @Subscribe("advancedModeValue")
    public void onAdvancedModeValueValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        updateAdvancedBoxesVisible(event.getValue() != null ? event.getValue() : false);
    }

    protected void updateAdvancedBoxesVisible(boolean value) {
        settingsBox.getOwnComponentsStream()
                .skip(3)
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
            ThemeVariableField themeVariableField = (ThemeVariableField) settingsBox.getComponent(themeVariable.getName() + "-field");
            themeVariableField.setColorValueByParent(value);
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
}