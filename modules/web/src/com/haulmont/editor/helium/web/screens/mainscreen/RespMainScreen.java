package com.haulmont.editor.helium.web.screens.mainscreen;

import com.google.common.collect.ImmutableMap;
import com.haulmont.addon.helium.web.theme.HeliumThemeVariantsManager;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.ScreenBuilders;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.screen.Install;
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
import com.haulmont.editor.helium.web.tools.ColorPresets;
import com.haulmont.editor.helium.web.tools.ModifiedThemeVariableDetails;
import com.haulmont.editor.helium.web.tools.ThemeVariable;
import com.haulmont.editor.helium.web.tools.ThemeVariableDetails;
import com.haulmont.editor.helium.web.tools.ThemeVariableUtils;
import com.haulmont.editor.helium.web.tools.ThemeVariablesManager;
import com.vaadin.ui.JavaScript;
import org.springframework.context.event.EventListener;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.haulmont.editor.helium.web.components.themevariablefield.ThemeVariableField.RGB_POSTFIX;

@UiController("respMainScreen")
@UiDescriptor("resp-main-screen.xml")
public class RespMainScreen extends MainScreen {

    protected static final String BASIC_MODULE_NAME = "Basic";
    protected static final String GROUPBOX_PADDING_LESS_STYLENAME = "padding-less";
    protected static final String GROUPBOX_POSTFIX = "-box";
    protected static final String THEME_VARIABLE_FIELD_POSTFIX = "-field";

    protected static final String MAIN_CLASSNAME = "v-app helium appui";
    protected static final String OVERLAY_CLASSNAME = "v-app helium appui v-overlay-container";

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
    protected RadioButtonGroup<ColorPreset> baseThemeField;
    @Inject
    protected CssLayout sideMenuPanel;
    @Inject
    protected AppWorkArea workArea;
    @Inject
    protected HBoxLayout horizontalWrap;

    protected String appWindowTheme;
    protected List<ModifiedThemeVariableDetails> modifiedThemeVariables = new ArrayList<>();
    protected List<ModifiedThemeVariableDetails> modifiedColorPresetThemeVariables = new ArrayList<>();
    protected ColorPreset currentColorPreset;
    protected ColorPreset customColorPreset = new ColorPreset(ColorPresets.CUSTOM);

    @Subscribe
    public void onInit(InitEvent event) {
        appWindowTheme = userSettingsTools.loadAppWindowTheme();

        initColorPresets();
        initSizeField();
        initThemeVariablesFields();

        updateAdvancedBoxesVisible(false);
        updateMainScreenStyleName();
    }

    @EventListener
    public void onUIRefresh(UIRefreshEvent event) {
        baseThemeField.setValue(currentColorPreset);
        modifiedThemeVariables.clear();
    }

    @Subscribe("baseThemeField")
    public void onBaseThemeFieldValueChange(HasValue.ValueChangeEvent<ColorPreset> event) {
        if (event.isUserOriginated()) {
            if (customColorPreset.equals(colorPresetField.getValue())
                    && event.isUserOriginated()) {
                showConfirmationDialog(baseThemeField, event.getValue(), event.getPrevValue());
            } else {
                updateColorPresetField(event.getValue());
            }
        }
    }

    @Subscribe("colorPresetField")
    public void onColorPresetFieldValueChange(HasValue.ValueChangeEvent<ColorPreset> event) {
        if (customColorPreset.equals(event.getPrevValue())
                && event.isUserOriginated()) {
            showConfirmationDialog(colorPresetField, event.getValue(), event.getPrevValue());
        } else if (!customColorPreset.equals(event.getValue())) {
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
        if (customColorPreset.equals(colorPresetField.getValue())) {
            dialogs.createOptionDialog(Dialogs.MessageType.WARNING)
                    .withCaption(messages.getMessage(RespMainScreen.class, "warningNotification.caption"))
                    .withContentMode(ContentMode.HTML)
                    .withMessage(messages.getMessage(RespMainScreen.class, "warningNotification.message"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK)
                                    .withHandler(actionPerformedEvent -> {
                                        modifiedThemeVariables = new ArrayList<>();
                                        modifiedColorPresetThemeVariables = new ArrayList<>();
                                        updateFieldsByColorPreset(baseThemeField.getValue());
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
                                DownloadScreen.BASE_THEME_PARAM,
                                baseThemeField.getValue().getName(),
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

    protected void initColorPresets() {
        List<ColorPreset> colorPresets = themeVariablesManager.getColorPresets();
        List<ColorPreset> baseThemes = colorPresets.stream()
                .filter(preset -> preset.getParent() == null)
                .collect(Collectors.toList());
        baseThemeField.setOptionsList(baseThemes);

        currentColorPreset = baseThemes.stream()
                .filter(preset -> ColorPresets.LIGHT.equals(preset.getName()))
                .findFirst()
                .orElse(null);
        baseThemeField.setValue(currentColorPreset);

        updateColorPresetField(currentColorPreset);
    }

    protected void updateColorPresetField(ColorPreset colorPreset) {
        List<ColorPreset> colorPresetsValues = themeVariablesManager.getColorPresets().stream()
                .filter(preset -> Objects.equals(colorPreset, preset.getParent()) || Objects.equals(colorPreset, preset))
                .collect(Collectors.toList());
        colorPresetField.setOptionsList(colorPresetsValues);
        colorPresetField.setValue(colorPreset);
    }

    @Install(to = "baseThemeField", subject = "optionCaptionProvider")
    protected String baseThemeFieldOptionCaptionProvider(ColorPreset colorPreset) {
        return colorPreset.getName();
    }

    @Install(to = "colorPresetField", subject = "optionCaptionProvider")
    protected String colorPresetFieldOptionCaptionProvider(ColorPreset colorPreset) {
        return colorPreset.getName();
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
            boolean isBaseTheme = valueChangeEvent.isUserOriginated();
            updateThemeVariable(themeVariable.getName(), valueChangeEvent.getValue(), themeVariable.getModule(), isBaseTheme);

            if (themeVariable.isRgbUsed()) {
                updateThemeVariable(themeVariable.getName() + RGB_POSTFIX,
                        ThemeVariableUtils.convertHexToRGB(valueChangeEvent.getValue()),
                        themeVariable.getModule(),
                        isBaseTheme);
            }

            ColorPreset newColorPreset = modifiedThemeVariables.isEmpty()
                    ? currentColorPreset
                    : customColorPreset;
            colorPresetField.setValue(newColorPreset);
        });

        return themeVariableField;
    }

    protected void showConfirmationDialog(OptionsField<ColorPreset, ColorPreset> optionsField, ColorPreset value, ColorPreset prevValue) {
        dialogs.createOptionDialog(Dialogs.MessageType.WARNING)
                .withCaption(messages.getMessage(RespMainScreen.class, "warningNotification.caption"))
                .withContentMode(ContentMode.HTML)
                .withMessage(messages.getMessage(RespMainScreen.class, "warningNotification.message"))
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(actionPerformedEvent -> updateColorPreset(value)),
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withHandler(actionPerformedEvent -> optionsField.setValue(prevValue))
                )
                .show();
    }

    protected void updateColorPreset(ColorPreset newColorPreset) {
        if (!customColorPreset.equals(newColorPreset)
                && !currentColorPreset.equals(newColorPreset)) {
            currentColorPreset = newColorPreset;

            modifiedThemeVariables = new ArrayList<>();
            modifiedColorPresetThemeVariables = new ArrayList<>();
        }

        updateMainScreenStyleName();
        updateFieldsByColorPreset(newColorPreset);
    }

    protected void updateMainScreenStyleName() {
        String colorPresetValue = Objects.requireNonNull(baseThemeField.getValue()).getName();

        workArea.setStyleName(appWindowTheme + " " + colorPresetValue + " " + sizeField.getValue());

        updateMainScreenClassName(MAIN_CLASSNAME, colorPresetValue);
        updateMainScreenClassName(OVERLAY_CLASSNAME, colorPresetValue);
    }

    protected void updateMainScreenClassName(String mainClassName, String baseTheme) {
        JavaScript.getCurrent()
                .execute(String.format("document.getElementsByClassName('%s')[0].className = '%s %s'",
                        mainClassName, mainClassName, baseTheme));
    }

    protected void resetValues() {
        colorPresetField.setValue(baseThemeField.getValue());
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

    protected void updateThemeVariable(String themeVariableName, String value, String module, boolean isBaseTheme) {
        updateModifiedThemeVariables(themeVariableName, value, module, isBaseTheme);
        updateChildThemeVariables(themeVariableName, value);
    }

    protected void updateModifiedThemeVariables(String themeVariableName, String value, String module, boolean isBaseTheme) {
        if (value == null) {
            removeModifiedThemeVariableDetails(themeVariableName, modifiedThemeVariables);
            removeModifiedThemeVariableDetails(themeVariableName, modifiedColorPresetThemeVariables);
        } else {
            if (isBaseTheme) {
                addModifiedThemeVariableDetails(themeVariableName, value, module, modifiedThemeVariables);
                removeModifiedThemeVariableDetails(themeVariableName, modifiedColorPresetThemeVariables);
            } else {
                addModifiedThemeVariableDetails(themeVariableName, value, module, modifiedColorPresetThemeVariables);
                removeModifiedThemeVariableDetails(themeVariableName, modifiedThemeVariables);
            }
        }
    }

    protected void removeModifiedThemeVariableDetails(String name, List<ModifiedThemeVariableDetails> modifiedThemeVariableDetails) {
        if (modifiedThemeVariableDetails != null) {
            modifiedThemeVariableDetails.stream()
                    .filter(details -> details.getName().equals(name))
                    .findFirst()
                    .ifPresent(modifiedThemeVariableDetails::remove);
        }
    }

    protected void addModifiedThemeVariableDetails(String name, String value, String module, List<ModifiedThemeVariableDetails> modifiedThemeVariableDetails) {
        if (modifiedThemeVariableDetails != null) {
            ModifiedThemeVariableDetails existingDetails = modifiedThemeVariableDetails.stream()
                    .filter(details -> details.getName().equals(name))
                    .findFirst()
                    .orElse(null);

            if (existingDetails != null) {
                int index = modifiedThemeVariableDetails.indexOf(existingDetails);
                existingDetails.setValue(value);
                modifiedThemeVariableDetails.set(index, existingDetails);
            } else {
                existingDetails = new ModifiedThemeVariableDetails(name, module, value);
                modifiedThemeVariableDetails.add(existingDetails);
            }
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
            ThemeVariableDetails themeVariableDetails = themeVariable.getThemeVariableDetails(currentColorPreset);

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
                .append(baseThemeField.getValue().getName())
                .append(" {\n");

        List<ModifiedThemeVariableDetails> modifiedThemeVariablesList =
                Stream.of(modifiedThemeVariables, modifiedColorPresetThemeVariables)
                        .flatMap(Collection::stream)
                        .sorted(Comparator.comparing(ModifiedThemeVariableDetails::getName))
                        .sorted(Comparator.comparing(ModifiedThemeVariableDetails::getModule))
                        .collect(Collectors.toList());

        String module = null;
        for (ModifiedThemeVariableDetails details : modifiedThemeVariablesList) {
            if (!details.getModule().equals(module)) {
                module = details.getModule();
                builder.append("\n")
                        .append("  /* ")
                        .append(module)
                        .append(" */")
                        .append("\n");
            }

            builder.append("  ")
                    .append(details.getName())
                    .append(": ")
                    .append(details.getValue())
                    .append(";\n");
        }

        builder.append("}");
        return builder.toString();
    }
}