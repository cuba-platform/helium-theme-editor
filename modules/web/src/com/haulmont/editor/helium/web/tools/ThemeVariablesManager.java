package com.haulmont.editor.helium.web.tools;

import com.haulmont.cuba.core.sys.AppContext;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static final String TRANSPARENT_COLOR_VALUE = "transparent";

    protected static final String TEMPLATES_FILE_NAME = "helium-templates.scss";

    /**
     * Theme variable module regexp. Intended to match the theme variable module.
     * <p>
     * Regexp explanation:
     * <ul>
     *     <li>{@code (?<=/\*\s)} - matches the beginning of theme variable module</li>
     *     <li>{@code .*} - matches a module name</li>
     *     <li>{@code (?=\s\*\/)} - matches the ending of theme variable module</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     *      /* Common *\/
     * }</pre>
     * <ul>
     *     <li>{@code Common} - a module name</li>
     * </ul>
     */
    protected static final Pattern MODULE_PATTERN = Pattern.compile("(?<=/\\*\\s).*(?=\\s\\*/)");

    /**
     * Base theme mode regexp. Intended to match the base theme mode value.
     * <p>
     * Regexp explanation:
     * <ul>
     *     <li>{@code (?<=&\.)} - matches the beginning of base theme mode value</li>
     *     <li>{@code \w*} - matches a color template</li>
     *     <li>{@code (?=\s\{)} - matches the ending of base theme mode value</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     *      &.dark {
     * }</pre>
     * <ul>
     *     <li>{@code dark} - a base theme mode</li>
     * </ul>
     */
    protected static final Pattern BASE_THEME_MODE_PATTERN = Pattern.compile("(?<=&\\.)\\w*(?=\\s\\{)");

    /**
     * Color template regexp. Intended to match the color template.
     * <p>
     * Regexp explanation:
     * <ul>
     *     <li>{@code (?<=\\.helium\\.)(\\w*)} - matches the base theme mode value</li>
     *     <li>{@code \\.} - matches a dot</li>
     *     <li>{@code (\\w*)(?=\\s\\{)} - matches the color template value</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     *      .helium.cobalt.light {
     * }</pre>
     * <ul>
     *     <li>{@code light} - a base theme mode</li>
     *     <li>{@code cobalt} - a color template</li>
     * </ul>
     */
    protected static final Pattern COLOR_TEMPLATE_PATTERN = Pattern.compile("(?<=\\.helium\\.)(\\w*)\\.(\\w*)(?=\\s\\{)");

    /**
     * The index of a base theme mode in {@code COLOR_TEMPLATE_PATTERN} pattern.
     */
    protected static final int BASE_THEME_MODE_GROUP = 1;

    /**
     * The index of a color template in {@code COLOR_TEMPLATE_PATTERN} pattern.
     */
    protected static final int COLOR_TEMPLATE_GROUP = 2;

    /**
     * Theme variable regexp. Intended to match the theme variable.
     * <p>
     * Regexp explanation:
     * <ul>
     *     <li>{@code ^\\h+} - matches any horizontal whitespace character</li>
     *     <li>{@code (-(-\w+)*(-color|-color_rgb)*)} - matches a theme variable name containing "-color" or "-color_rgb"</li>
     *     <li>{@code :} - matches a separator between name and value</li>
     *     <li>{@code \\h+} - matches any horizontal whitespace character</li>
     *     <li>{@code ([^;!]+)?;} - matches a theme variable value ending in ";"</li>
     *     <li>{@code \\h+\\/\\/\\h+\\(} - matches a parent variable start</li>
     *     <li>{@code ((-(-\w+)*(?=\)))} - matches a parent variable</li>
     *     <li>{@code (\\)} - matches a parent variable end</li>
     *     <li>{@code \\h\(} - matches a color modifier start</li>
     *     <li>{@code (?i)(d|l)} - matches a color modifier (darken | lighten)</li>
     *     <li>{@code ([0-9]+?%)} - matches a color modifier value</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     *      --primary-hover-color: #5440AC;      // (--primary-color) (d10%)
     * }</pre>
     * <ul>
     *     <li>{@code --primary-hover-color} - a theme variable name</li>
     *     <li>{@code #5440AC} - a theme variable value</li>
     *     <li>{@code --primary-color} - a parent theme variable</li>
     *     <li>{@code d} - a color modifier (darken)</li>
     *     <li>{@code 10%} - a color modifier value</li>
     * </ul>
     */
    protected static final Pattern THEME_VARIABLE_PATTERN =
            Pattern.compile("^^\\h+(-(-\\w+)*(-color|-color_rgb)*):\\h+([^;!]+)?;(\\h+//\\h+\\((-(-\\w+)*(?=\\)))\\)(?=\\h\\(|)(\\h\\((?i)(d|l)([0-9]+?%)|)|)");

    /**
     * The index of a theme variable name in {@code THEME_VARIABLE_PATTERN} pattern.
     */
    protected static final int NAME_GROUP = 1;

    /**
     * The index of a theme variable value in {@code THEME_VARIABLE_PATTERN} pattern.
     */
    protected static final int VALUE_GROUP = 4;

    /**
     * The index of a parent theme variable in {@code THEME_VARIABLE_PATTERN} pattern.
     */
    protected static final int PARENT_VARIABLE_GROUP = 6;

    /**
     * The index of a color modifier in {@code THEME_VARIABLE_PATTERN} pattern.
     */
    protected static final int COLOR_MODIFIER_GROUP = 9;

    /**
     * The index of a color modifier value in {@code THEME_VARIABLE_PATTERN} pattern.
     */
    protected static final int COLOR_MODIFIER_VALUE_GROUP = 10;

    /**
     * RGB color regexp. Intended to match the RGB color value.
     * <p>
     * Regexp explanation:
     * <ul>
     *     <li>{@code ([0-9]*)} - matches a color component value</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     *      99, 116, 151
     * }</pre>
     * <ul>
     *     <li>{@code 99} - a red color component</li>
     *     <li>{@code 116} - a red color component</li>
     *     <li>{@code 151} - a red color component</li>
     * </ul>
     */
    protected static final Pattern RGB_PATTERN = Pattern.compile("([0-9]*), ([0-9]*), ([0-9]*)");

    /**
     * HEX color regexp. Intended to match the HEX color value.
     * <p>
     * Regexp explanation:
     * <ul>
     *     <li>{@code (#?([A-Fa-f0-9]){3}([A-Fa-f0-9]){3})} - matches a hex color value</li>
     * </ul>
     * <p>
     * Example:
     * <pre>{@code
     *      #2A8463
     * }</pre>
     */
    protected static final Pattern HEX_PATTERN = Pattern.compile("(#?([A-Fa-f0-9]){3}([A-Fa-f0-9]){3})");

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ThemeVariablesManager.class);

    /**
     * The list of theme variables.
     */
    protected List<ThemeVariable> themeVariables = new ArrayList<>();

    /**
     * The list of color templates.
     */
    protected List<Template> templates = new ArrayList<>();

    /**
     * The default color template - light.
     */
    protected Template lightTemplate = new Template(Templates.LIGHT);

    public ThemeVariablesManager() {
        initColorTemplates();
        initThemeVariables();
    }

    /**
     * @return the list of theme variables
     */
    public List<ThemeVariable> getThemeVariables() {
        return themeVariables;
    }

    /**
     * @return the list of color templates
     */
    public List<Template> getTemplates() {
        return templates;
    }

    /**
     * Init color templates list.
     */
    protected void initColorTemplates() {
        templates.add(lightTemplate);
    }

    /**
     * Theme variables file parsing.
     */
    protected void initThemeVariables() {
        try {
            String themeVariablesFilePath = AppContext.getProperty("helium.editor.themeVariablesFilePath");
            if (themeVariablesFilePath == null) {
                return;
            }

            File file = new File(themeVariablesFilePath);
            parseThemeVariables(new BufferedReader(new FileReader(file)));
        } catch (FileNotFoundException e) {
            log.error("File with theme variables not found", e);
        }

        InputStream fileStream = getClass().getClassLoader().getResourceAsStream(TEMPLATES_FILE_NAME);
        parseThemeVariables(new BufferedReader(new InputStreamReader(fileStream)));
    }

    /**
     * Parse theme variables from reader.
     *
     * @param reader reader
     */
    protected void parseThemeVariables(BufferedReader reader) {
        try {
            String line;
            String module = null;
            Template template = lightTemplate;
            Matcher matcher;

            while ((line = reader.readLine()) != null) {
                matcher = BASE_THEME_MODE_PATTERN.matcher(line);
                if (matcher.find()) {
                    Template newTemplate = new Template(matcher.group());
                    templates.add(newTemplate);
                    template = newTemplate;
                }

                matcher = COLOR_TEMPLATE_PATTERN.matcher(line);
                if (matcher.find()) {
                    String baseThemeMode = matcher.group(BASE_THEME_MODE_GROUP);
                    String colorTemplateValue = matcher.group(COLOR_TEMPLATE_GROUP);

                    Template newTemplate = new Template(colorTemplateValue);
                    newTemplate.setParent(getColorTemplateByName(baseThemeMode));

                    templates.add(newTemplate);
                    template = newTemplate;
                }

                matcher = MODULE_PATTERN.matcher(line);
                if (matcher.find()) {
                    module = matcher.group();
                }

                if (module != null) {
                    matcher = THEME_VARIABLE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String name = matcher.group(NAME_GROUP);
                        String value = matcher.group(VALUE_GROUP);
                        boolean commentDependence = false;

                        int groupCount = matcher.groupCount();
                        ThemeVariable parentThemeVariable = loadParentThemeVariable(matcher.group(VALUE_GROUP));
                        if (parentThemeVariable != null) {
                            value = parentThemeVariable.getThemeVariableDetails(template).getValue();
                        } else if (groupCount >= PARENT_VARIABLE_GROUP) {
                            String parentVariableName = matcher.group(PARENT_VARIABLE_GROUP);
                            if (parentVariableName != null) {
                                parentThemeVariable = getThemeVariableByName(parentVariableName);
                                commentDependence = true;
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
                        } else if (HEX_PATTERN.matcher(value).find() || TRANSPARENT_COLOR_VALUE.equals(value)) {
                            ThemeVariableDetails details = new ThemeVariableDetails();
                            details.setPlaceHolder(matcher.group(VALUE_GROUP));
                            details.setValue(value);
                            details.setParentThemeVariable(parentThemeVariable);
                            details.setCommentDependence(commentDependence);

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
                            if (template != null) {
                                Template parentTemplate = template.getParent();
                                if (parentTemplate != null) {
                                    ThemeVariableDetails parentThemeDetails = themeVariable.getThemeVariableDetails(parentTemplate);
                                    if (parentThemeDetails != null
                                            && parentThemeDetails.isCommentDependence()
                                            && details.getParentThemeVariable() == null) {
                                        details.setCommentDependence(true);
                                        details.setParentThemeVariable(parentThemeDetails.getParentThemeVariable());

                                        if (details.getColorModifier() == null) {
                                            details.setColorModifier(parentThemeDetails.getColorModifier());
                                        }

                                        if (details.getColorModifierValue() == null) {
                                            details.setColorModifierValue(parentThemeDetails.getColorModifierValue());
                                        }
                                    }
                                }

                                if (themeVariable != null) {
                                    themeVariable.setThemeVariableDetails(template, details);
                                } else {
                                    themeVariable = new ThemeVariable();
                                    themeVariable.setModule(module);
                                    themeVariable.setName(name);
                                    themeVariable.setThemeVariableDetails(template, details);
                                    themeVariables.add(themeVariable);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error parsing file with theme variables", e);
        }
    }

    /**
     * Returns the parent theme variable if the value is in follow format:
     * <pre>
     *     var(parentThemeVariableName)
     * </pre>
     *
     * @param value a string containing parent theme variable name
     * @return a parent theme variable
     */
    protected ThemeVariable loadParentThemeVariable(String value) {
        if (value.contains("var")) {
            String dependentThemeVariableName = value.substring(value.indexOf("(") + 1, value.indexOf(")"));
            return getThemeVariableByName(dependentThemeVariableName);
        }

        return null;
    }

    /**
     * Returns the theme variable by given name.
     *
     * @param variableName a theme variable name
     * @return a theme variable
     */
    protected ThemeVariable getThemeVariableByName(String variableName) {
        return themeVariables.stream()
                .filter(themeVariable -> variableName.equals(themeVariable.getName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the color template by given name.
     *
     * @param name a color template name
     * @return a color template
     */
    protected Template getColorTemplateByName(String name) {
        return templates != null
                ? templates.stream()
                .filter(colorTemplate -> colorTemplate.getName().equals(name))
                .findFirst()
                .orElse(null)
                : null;
    }
}
