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
import com.haulmont.cuba.gui.screen.StandardOutcome;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.app.UserSettingsTools;
import com.haulmont.cuba.web.app.main.MainScreen;
import com.haulmont.cuba.web.events.UIRefreshEvent;
import com.haulmont.cuba.web.theme.HaloTheme;
import com.haulmont.editor.helium.web.components.themevariablefield.ThemeVariableField;
import com.haulmont.editor.helium.web.screens.download.DownloadScreen;
import com.haulmont.editor.helium.web.screens.upload.UploadScreen;
import com.haulmont.editor.helium.web.tools.ModifiedThemeVariableDetails;
import com.haulmont.editor.helium.web.tools.Template;
import com.haulmont.editor.helium.web.tools.Templates;
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
    protected static final String COMMON_MODULE_NAME = "Common";
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
    protected LookupField<Template> templateField;
    @Inject
    protected RadioButtonGroup<Template> baseThemeModeField;
    @Inject
    protected CssLayout sideMenuPanel;
    @Inject
    protected AppWorkArea workArea;
    @Inject
    protected HBoxLayout horizontalWrap;

    protected String appWindowTheme;
    protected List<ModifiedThemeVariableDetails> modifiedThemeVariables = new ArrayList<>();
    protected List<ModifiedThemeVariableDetails> modifiedColorTemplateThemeVariables = new ArrayList<>();
    protected Template currentTemplate;
    protected Template customTemplate = new Template(Templates.CUSTOM);

    @Subscribe
    public void onInit(InitEvent event) {
        appWindowTheme = userSettingsTools.loadAppWindowTheme();

        initColorTemplates();
        initThemeVariablesFields();

        updateAdvancedBoxesVisible(false);
        updateMainScreenStyleName();
    }

    @EventListener
    public void onUIRefresh(UIRefreshEvent event) {
        settingsBox.getComponents()
                .forEach(component -> {
                    if (component instanceof ThemeVariableField) {
                        ((ThemeVariableField) component).refreshJavaScriptComponent();
                    }
                });

        updateMainScreenStyleName();

        JavaScript javaScript = JavaScript.getCurrent();
        getModifiedThemeVariables()
                .forEach(modifiedThemeVariableDetails -> javaScript.execute(String.format(
                        "Array.from(document.getElementsByClassName('helium')).forEach(function (element) {element.style.setProperty('%s', '%s')})",
                        modifiedThemeVariableDetails.getName(),
                        modifiedThemeVariableDetails.getValue())));
    }

    @Subscribe("baseThemeModeField")
    public void onBaseThemeModeFieldValueChange(HasValue.ValueChangeEvent<Template> event) {
        if (event.isUserOriginated()) {
            if (customTemplate.equals(templateField.getValue())
                    && event.isUserOriginated()) {
                showConfirmationDialog(baseThemeModeField, event.getValue(), event.getPrevValue());
            } else {
                updateTemplateField(event.getValue());
            }
        }
    }

    @Subscribe("templateField")
    public void onTemplateFieldValueChange(HasValue.ValueChangeEvent<Template> event) {
        if (customTemplate.equals(event.getPrevValue())
                && event.isUserOriginated()) {
            showConfirmationDialog(templateField, event.getValue(), event.getPrevValue());
        } else if (!customTemplate.equals(event.getValue())) {
            updateColorTemplate(event.getValue());
        }
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
        if (customTemplate.equals(templateField.getValue())) {
            dialogs.createOptionDialog(Dialogs.MessageType.WARNING)
                    .withCaption(messages.getMessage(RespMainScreen.class, "warningNotification.caption"))
                    .withContentMode(ContentMode.HTML)
                    .withMessage(messages.getMessage(RespMainScreen.class, "warningNotification.message"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK)
                                    .withHandler(actionPerformedEvent -> {
                                        modifiedThemeVariables = new ArrayList<>();
                                        modifiedColorTemplateThemeVariables = new ArrayList<>();
                                        updateFieldsByColorTemplate(baseThemeModeField.getValue());
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
                                DownloadScreen.BASE_THEME_MODE_PARAM,
                                baseThemeModeField.getValue().getName(),
                                DownloadScreen.TEXT_PARAM,
                                generateDownloadText()
                        )
                ))
                .show();
    }

    @Subscribe("uploadBtn")
    protected void onUploadBtnClick(Button.ClickEvent event) {
        screenBuilders.screen(this)
                .withScreenClass(UploadScreen.class)
                .withOptions(new MapScreenOptions(
                        ImmutableMap.of(
                                UploadScreen.BASE_THEME_MODE_PARAM,
                                baseThemeModeField.getValue(),
                                UploadScreen.BASE_THEME_MODES_PARAM,
                                baseThemeModeField.getOptions()
                        )
                ))
                .withAfterCloseListener(afterScreenCloseEvent -> {
                    if (afterScreenCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                        UploadScreen uploadScreen = afterScreenCloseEvent.getScreen();
                        baseThemeModeField.setValue(uploadScreen.getBaseThemeMode());
                        resetValues();
                        applyUploadedThemeVariables(uploadScreen.getUploadedThemeVariables());
                    }
                })
                .show();
    }

    protected void applyUploadedThemeVariables(List<ModifiedThemeVariableDetails> uploadedThemeVariables) {
        if (uploadedThemeVariables != null
                && !uploadedThemeVariables.isEmpty()) {
            uploadedThemeVariables.forEach(uploadedThemeVariableDetails -> {
                ThemeVariableField themeVariableField =
                        (ThemeVariableField) settingsBox.getComponent(uploadedThemeVariableDetails.getName() + THEME_VARIABLE_FIELD_POSTFIX);
                if (themeVariableField != null) {
                    themeVariableField.setColorValue(uploadedThemeVariableDetails.getValue());
                }
            });
        }
    }

    @Subscribe("advancedModeValue")
    public void onAdvancedModeValueValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        updateAdvancedBoxesVisible(event.getValue() != null ? event.getValue() : false);
    }

    protected void initColorTemplates() {
        List<Template> templates = themeVariablesManager.getTemplates();
        List<Template> baseThemeModes = templates.stream()
                .filter(template -> template.getParent() == null)
                .collect(Collectors.toList());
        baseThemeModeField.setOptionsList(baseThemeModes);

        currentTemplate = baseThemeModes.stream()
                .filter(template -> Templates.LIGHT.equals(template.getName()))
                .findFirst()
                .orElse(null);
        baseThemeModeField.setValue(currentTemplate);

        updateTemplateField(currentTemplate);
    }

    protected void updateTemplateField(Template colorTemplate) {
        List<Template> colorTemplatesValues = themeVariablesManager.getTemplates().stream()
                .filter(template -> Objects.equals(colorTemplate, template.getParent()) || Objects.equals(colorTemplate, template))
                .collect(Collectors.toList());
        templateField.setOptionsList(colorTemplatesValues);
        templateField.setValue(colorTemplate);
    }

    @Install(to = "baseThemeModeField", subject = "optionCaptionProvider")
    protected String baseThemeModeFieldOptionCaptionProvider(Template template) {
        return template.getName();
    }

    @Install(to = "templateField", subject = "optionCaptionProvider")
    protected String templateFieldOptionCaptionProvider(Template template) {
        return baseThemeModeField.getOptions()
                .getOptions()
                .anyMatch(baseThemeMode -> baseThemeMode.equals(template))
                ? template.getName() + " (default)"
                : template.getName();
    }

    protected void initThemeVariablesFields() {
        List<Component> advancedGroupBoxLayouts = new ArrayList<>();
        for (ThemeVariable themeVariable : getDefaultThemeVariables()) {
            String module = themeVariable.getModule();

            Component groupBoxLayout = settingsBox.getComponent(module.toLowerCase() + GROUPBOX_POSTFIX);
            if (groupBoxLayout == null) {
                groupBoxLayout = advancedGroupBoxLayouts.stream()
                        .filter(groupBox -> (module.toLowerCase() + GROUPBOX_POSTFIX).equals(groupBox.getId()))
                        .findFirst()
                        .orElse(null);
            }
            if (groupBoxLayout == null) {
                groupBoxLayout = createGroupBoxLayout(module);

                if (module.equals(BASIC_MODULE_NAME)) {
                    ((GroupBoxLayout) groupBoxLayout).setExpanded(true);
                    settingsBox.add(groupBoxLayout, 1);
                } else if (module.equals(COMMON_MODULE_NAME)) {
                    settingsBox.add(groupBoxLayout);
                } else {
                    advancedGroupBoxLayouts.add(groupBoxLayout);
                }
            }

            ThemeVariableField field = createThemeVariableField(themeVariable);
            ((GroupBoxLayout) groupBoxLayout).add(field);
        }

        advancedGroupBoxLayouts.stream()
                .sorted(Comparator.comparing(Component::getId))
                .forEach(component -> settingsBox.add(component));
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
            boolean isBaseThemeMode = valueChangeEvent.isUserOriginated();
            updateThemeVariable(themeVariable.getName(), valueChangeEvent.getValue(), themeVariable.getModule(), isBaseThemeMode);

            if (themeVariable.isRgbUsed()) {
                updateThemeVariable(themeVariable.getName() + RGB_POSTFIX,
                        ThemeVariableUtils.convertHexToRGB(valueChangeEvent.getValue()),
                        themeVariable.getModule(),
                        isBaseThemeMode);
            }

            Template newTemplate = modifiedThemeVariables.isEmpty()
                    ? currentTemplate
                    : customTemplate;
            templateField.setValue(newTemplate);
        });

        return themeVariableField;
    }

    protected void showConfirmationDialog(OptionsField<Template, Template> optionsField, Template value, Template prevValue) {
        dialogs.createOptionDialog(Dialogs.MessageType.WARNING)
                .withCaption(messages.getMessage(RespMainScreen.class, "warningNotification.caption"))
                .withContentMode(ContentMode.HTML)
                .withMessage(messages.getMessage(RespMainScreen.class, "warningNotification.message"))
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(actionPerformedEvent -> {
                                    updateTemplateField(value);
                                    updateColorTemplate(value);
                                }),
                        new DialogAction(DialogAction.Type.CANCEL)
                                .withHandler(actionPerformedEvent -> optionsField.setValue(prevValue))
                )
                .show();
    }

    protected void updateColorTemplate(Template newTemplate) {
        if (!customTemplate.equals(newTemplate)
                && !currentTemplate.equals(newTemplate)) {
            currentTemplate = newTemplate;

            modifiedThemeVariables = new ArrayList<>();
            modifiedColorTemplateThemeVariables = new ArrayList<>();
        }

        updateMainScreenStyleName();
        updateFieldsByColorTemplate(newTemplate);
    }

    protected void updateMainScreenStyleName() {
        String colorTemplateValue = Objects.requireNonNull(baseThemeModeField.getValue()).getName();

        workArea.setStyleName(appWindowTheme + " " + colorTemplateValue);

        updateMainScreenClassName(MAIN_CLASSNAME, colorTemplateValue);
        updateMainScreenClassName(OVERLAY_CLASSNAME, colorTemplateValue);
    }

    protected void updateMainScreenClassName(String mainClassName, String baseThemeMode) {
        JavaScript.getCurrent()
                .execute(String.format("document.getElementsByClassName('%s')[0].className = '%s %s'",
                        mainClassName, mainClassName, baseThemeMode));
    }

    protected void resetValues() {
        templateField.setValue(baseThemeModeField.getValue());
        modifiedThemeVariables.clear();
        updateMainScreenStyleName();
    }

    protected void updateAdvancedBoxesVisible(boolean value) {
        settingsBox.getOwnComponentsStream()
                .skip(3) // skip Screen defaults and Basic groupboxes
                .forEach(component -> component.setVisible(value));
    }

    protected void updateFieldsByColorTemplate(Template templateValue) {
        settingsBox.getComponents()
                .forEach(component -> {
                    if (component instanceof ThemeVariableField) {
                        ((ThemeVariableField) component).setColorValueByTemplate(templateValue);
                    }
                });
    }

    protected void updateThemeVariable(String themeVariableName, String value, String module, boolean isBaseThemeMode) {
        updateModifiedThemeVariables(themeVariableName, value, module, isBaseThemeMode);
        if (isBaseThemeMode) {
            updateChildThemeVariables(themeVariableName, value);
        }
    }

    protected void updateModifiedThemeVariables(String themeVariableName, String value, String module, boolean isBaseThemeMode) {
        if (value == null) {
            removeModifiedThemeVariableDetails(themeVariableName, modifiedThemeVariables);
            removeModifiedThemeVariableDetails(themeVariableName, modifiedColorTemplateThemeVariables);
        } else {
            if (isBaseThemeMode) {
                addModifiedThemeVariableDetails(themeVariableName, value, module, modifiedThemeVariables);
                removeModifiedThemeVariableDetails(themeVariableName, modifiedColorTemplateThemeVariables);
            } else {
                addModifiedThemeVariableDetails(themeVariableName, value, module, modifiedColorTemplateThemeVariables);
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
            ThemeVariableDetails themeVariableDetails = themeVariable.getThemeVariableDetails(currentTemplate);

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
        boolean isDefaultThemeMode = variantsManager.getDefaultAppThemeMode().equals(baseThemeModeField.getValue().getName());
        if (!isDefaultThemeMode) {
            builder.append(".")
                    .append(baseThemeModeField.getValue().getName())
                    .append(" {\n");
        }

        List<ModifiedThemeVariableDetails> modifiedThemeVariablesList = getModifiedThemeVariables();

        String module = null;
        boolean firstModule = true;
        for (ModifiedThemeVariableDetails details : modifiedThemeVariablesList) {
            if (!details.getModule().equals(module)) {
                if (firstModule) {
                    firstModule = false;
                } else {
                    builder.append("\n");
                }

                module = details.getModule();
                builder.append("  /* ")
                        .append(module)
                        .append(" */")
                        .append("\n");
            }

            if (!isDefaultThemeMode) {
                builder.append("  ");
            }

            builder.append(details.getName())
                    .append(": ")
                    .append(details.getValue())
                    .append(";\n");
        }

        if (!isDefaultThemeMode) {
            builder.append("}");
        }
        return builder.toString();
    }

    protected List<ModifiedThemeVariableDetails> getModifiedThemeVariables() {
        return Stream.of(modifiedThemeVariables, modifiedColorTemplateThemeVariables)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ModifiedThemeVariableDetails::getName))
                .sorted(Comparator.comparing(ModifiedThemeVariableDetails::getModule))
                .collect(Collectors.toList());
    }
}