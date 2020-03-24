package com.haulmont.editor.helium.web.tools;

import com.haulmont.cuba.core.sys.AppContext;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haulmont.editor.helium.web.components.themevariablefield.ThemeVariableField.RGB_POSTFIX;

/**
 * Theme variables manager
 */
@Component(ThemeVariablesManager.NAME)
public class ThemeVariablesManager {

    public static final String NAME = "helium_ThemeVariablesManager";

    // todo GD javadocs and tests for regexp
    protected static final Pattern THEME_VARIABLE_GROUP_BEGIN_PATTERN = Pattern.compile("(?<=\\/\\/\\sbegin\\h)(\\w*)(\\.(\\w*)|$)");
    protected static final Pattern THEME_VARIABLE_GROUP_END_PATTERN = Pattern.compile("(?<=\\/\\/\\send\\h)(\\w*)(\\.(\\w*)|$)");
    protected static final int MODULE_GROUP = 1;
    protected static final int PRESET_GROUP = 3;

    protected static final Pattern THEME_VARIABLE_PATTERN =
            Pattern.compile("^\\h+(-(-\\w+)*(-color|-color_rgb)):\\h+([^;!]+)?;(\\h+\\/\\/\\h+\\((.*(?=\\)\\h\\())(\\)\\h\\((?i)(d|l)([0-9]+?%))|)");
    protected static final int NAME_GROUP = 1;
    protected static final int VALUE_GROUP = 4;
    protected static final int PARENT_VARIABLE_GROUP = 6;
    protected static final int COLOR_MODIFIER_GROUP = 8;
    protected static final int COLOR_MODIFIER_VALUE_GROUP = 9;

    protected static final Pattern RGB_PATTERN = Pattern.compile("([0-9]*), ([0-9]*), ([0-9]*)");
    protected static final Pattern HEX_PATTERN = Pattern.compile("(#?([A-Fa-f0-9]){3}([A-Fa-f0-9]){3})");

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ThemeVariablesManager.class);

    protected List<ThemeVariable> themeVariables = new ArrayList<>();

    public ThemeVariablesManager() {
        initThemeVariables();
    }

    public List<ThemeVariable> getThemeVariables() {
        return themeVariables;
    }

    protected void initThemeVariables() {
        try {
            File themeFile = new File(AppContext.getProperty("helium.theme-variables-path"));
            BufferedReader reader = new BufferedReader(new FileReader(themeFile));
            String line;
            String module = null;
            String preset = null;
            Matcher matcher;

            while ((line = reader.readLine()) != null) {
                if (module == null) {
                    matcher = THEME_VARIABLE_GROUP_BEGIN_PATTERN.matcher(line);
                    if (matcher.find()) {
                        module = matcher.group(MODULE_GROUP);
                        if (matcher.groupCount() >= PRESET_GROUP) {
                            preset = matcher.group(PRESET_GROUP);
                        }
                    }
                } else {
                    matcher = THEME_VARIABLE_GROUP_END_PATTERN.matcher(line);
                    if (matcher.find()) {
                        module = null;
                    } else {
                        matcher = THEME_VARIABLE_PATTERN.matcher(line);
                        if (matcher.find()) {
                            String name = matcher.group(NAME_GROUP);
                            ColorPreset colorPreset = preset != null
                                    ? ColorPreset.fromId(preset.toLowerCase())
                                    : ColorPreset.LIGHT;

                            String value = matcher.group(VALUE_GROUP);
                            int groupCount = matcher.groupCount();
                            ThemeVariable parentThemeVariable = loadParentThemeVariable(matcher.group(VALUE_GROUP));
                            if (parentThemeVariable != null) {
                                value = parentThemeVariable.getThemeVariableDetails(colorPreset).getValue();
                            } else if (groupCount >= PARENT_VARIABLE_GROUP) {
                                String parentVariableName = matcher.group(PARENT_VARIABLE_GROUP);
                                if (parentVariableName != null) {
                                    parentThemeVariable = getThemeVariableByName(parentVariableName);
                                }
                            }

                            ThemeVariable themeVariable;
                            if (RGB_PATTERN.matcher(value).find()
                                    && name != null
                                    && name.endsWith(RGB_POSTFIX)) {
                                String mainThemeVariableName = name.substring(0, name.lastIndexOf(RGB_POSTFIX));
                                themeVariable = getThemeVariableByName(mainThemeVariableName);
                                if (themeVariable != null) {
                                    themeVariable.setRgbUsed(true);
                                }
                            } else if (HEX_PATTERN.matcher(value).find()) {
                                ThemeVariableDetails details = new ThemeVariableDetails();
                                details.setPlaceHolder(matcher.group(VALUE_GROUP));
                                details.setValue(value);
                                details.setParentThemeVariable(parentThemeVariable);

                                if (groupCount >= COLOR_MODIFIER_GROUP) {
                                    String colorModifier = matcher.group(COLOR_MODIFIER_GROUP);
                                    if (colorModifier != null) {
                                        details.setColorModifier(colorModifier);
                                    }
                                }

                                if (groupCount >= COLOR_MODIFIER_VALUE_GROUP) {
                                    String colorModifierValue = matcher.group(COLOR_MODIFIER_VALUE_GROUP);
                                    if (colorModifierValue != null) {
                                        details.setColorModifierValue(colorModifierValue);
                                    }
                                }

                                themeVariable = getThemeVariableByName(name);
                                if (themeVariable != null) {
                                    themeVariable.setThemeVariableDetails(colorPreset, details);
                                } else {
                                    themeVariable = new ThemeVariable();
                                    themeVariable.setModule(module);
                                    themeVariable.setName(name);
                                    themeVariable.setThemeVariableDetails(colorPreset, details);
                                    themeVariables.add(themeVariable);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error ", e);
        }
    }

    protected ThemeVariable loadParentThemeVariable(String value) {
        if (value.contains("var")) {
            String dependentThemeVariableName = value.substring(value.indexOf("(") + 1, value.indexOf(")"));
            return getThemeVariableByName(dependentThemeVariableName);
        }

        return null;
    }

    protected ThemeVariable getThemeVariableByName(String variableName) {
        return themeVariables.stream()
                .filter(themeVariable -> variableName.equals(themeVariable.getName()))
                .findFirst()
                .orElse(null);
    }
}
