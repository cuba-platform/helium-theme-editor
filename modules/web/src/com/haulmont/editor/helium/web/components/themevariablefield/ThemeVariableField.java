package com.haulmont.editor.helium.web.components.themevariablefield;

import com.haulmont.bali.events.Subscription;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.ColorPicker;
import com.haulmont.cuba.gui.components.Field;
import com.haulmont.cuba.gui.components.Form;
import com.haulmont.cuba.gui.components.HasInputPrompt;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.ValidationException;
import com.haulmont.cuba.gui.components.data.ValueSource;
import com.haulmont.cuba.web.gui.components.CompositeComponent;
import com.haulmont.cuba.web.gui.components.CompositeDescriptor;
import com.haulmont.cuba.web.gui.components.CompositeWithIcon;
import com.haulmont.cuba.web.gui.components.JavaScriptComponent;
import com.haulmont.editor.helium.web.tools.ColorPreset;
import com.haulmont.editor.helium.web.tools.ThemeVariable;
import com.haulmont.editor.helium.web.tools.ThemeVariableDetails;
import com.haulmont.editor.helium.web.tools.ThemeVariableUtils;
import com.vaadin.ui.JavaScript;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

@CompositeDescriptor("theme-variable-field.xml")
public class ThemeVariableField extends CompositeComponent<Form>
        implements Field<ThemeVariable>,
        CompositeWithIcon,
        HasInputPrompt {

    public static final String NAME = "themeVariableField";

    public static final String RGB_POSTFIX = "_rgb";

    protected Label<String> captionField;
    protected TextField<String> valueField;
    protected ColorPicker colorValueField;
    protected JavaScriptComponent jsComponent;
    protected Button resetBtn;

    protected JavaScript javaScript;

    protected ThemeVariable themeVariable;
    protected ColorPreset currentColorPreset = ColorPreset.LIGHT;
    protected String parentValue;

    public ThemeVariableField() {
        addCreateListener(this::onCreate);
    }

    private void onCreate(CreateEvent createEvent) {
        captionField = getInnerComponent("captionField");
        valueField = getInnerComponent("valueField");
        colorValueField = getInnerComponent("colorValueField");
        jsComponent = getInnerComponent("jsComponent");
        resetBtn = getInnerComponent("resetBtn");

        initColorValueField();
        initValueField();
        initJavaScript();
        initResetBtn();
    }

    protected void initColorValueField() {
        colorValueField.addValueChangeListener(valueChangeEvent -> {
            if (valueChangeEvent.isUserOriginated()) {
                valueField.setValue(ThemeVariableUtils.getColorString(valueChangeEvent.getValue()));
            }
        });
    }

    protected void initValueField() {
        valueField.addValueChangeListener(valueChangeEvent -> {
            String value = valueChangeEvent.getValue() != null
                    ? valueChangeEvent.getValue()
                    : parentValue != null
                    ? parentValue
                    : themeVariable.getThemeVariableDetails(currentColorPreset).getValue();
            value = ThemeVariableUtils.getColorString(value);

            if (valueChangeEvent.isUserOriginated()) {
                colorValueField.setValue(value);
            }

            boolean valueIsNull = valueChangeEvent.getValue() == null;
            resetBtn.setEnabled(!valueIsNull);

            if (valueIsNull && parentValue == null) {
                removeThemeVariable();
            } else {
                setThemeVariable(value);
            }
        });
    }

    protected void initJavaScript() {
        javaScript = JavaScript.getCurrent();
        javaScript.execute(jsComponent.getInitFunctionName() + "()");
    }

    protected void initResetBtn() {
        resetBtn.addClickListener(clickEvent -> {
            ThemeVariableDetails details = themeVariable.getThemeVariableDetails(currentColorPreset);
            if (details == null) {
                details = themeVariable.getThemeVariableDetails();
            }

            reset(details);
            fireValueChangeEvent(null);
        });
    }

    protected void setThemeVariable(String value) {
        javaScript.execute(String.format("setThemeVariable('%s', '%s')", themeVariable.getName(), value));

        if (themeVariable.isRgbUsed()) {
            javaScript.execute(String.format("setThemeVariable('%s', '%s')", themeVariable.getName() + RGB_POSTFIX,
                    ThemeVariableUtils.hexStringToRGB(value)));
        }
    }

    protected void removeThemeVariable() {
        javaScript.execute(String.format("removeThemeVariable('%s')", themeVariable.getName()));

        if (themeVariable.isRgbUsed()) {
            javaScript.execute(String.format("removeThemeVariable('%s')", themeVariable.getName() + RGB_POSTFIX));
        }
    }

    @Nullable
    @Override
    public ThemeVariable getValue() {
        return themeVariable;
    }

    @Override
    public void setValue(@Nullable ThemeVariable themeVariable) {
        this.themeVariable = themeVariable;
        setValueByPreset(ColorPreset.LIGHT);
    }

    public void setValueByPreset(ColorPreset colorPreset) {
        if (themeVariable == null) {
            return;
        }

        ThemeVariableDetails details = themeVariable.getThemeVariableDetails(colorPreset);
        if (details == null) {
            details = themeVariable.getThemeVariableDetails();
        }

        if (details == null) {
            setVisible(false);
            return;
        } else if (!isVisible()) {
            setVisible(true);
        }

        if (!Objects.equals(details.getValue(), ThemeVariableUtils.getColorString(colorValueField.getValue()))) {
            currentColorPreset = colorPreset;
            parentValue = null;
            reset(details);
        }
    }

    protected void reset(ThemeVariableDetails details) {
        if (getInputPrompt() != null &&
                !getInputPrompt().equals(details.getPlaceHolder())) {
            removeThemeVariable();
        }

        String name = themeVariable.getName();
        setCaption(name);
        setDescription(name);

        valueField.setValue(null);

        String value = parentValue != null
                ? parentValue
                : ThemeVariableUtils.getColorString(details.getValue());
        colorValueField.setValue(value);

        if (parentValue == null) {
            setInputPrompt(details.getPlaceHolder());
        }
    }

    public void setColorValueByParent(String parentValue) {
        ThemeVariableDetails details = themeVariable.getThemeVariableDetails(currentColorPreset);

        if (parentValue == null) {
            parentValue = details.getValue();
            removeThemeVariable();
        } else {
            String colorModifier = details.getColorModifier();
            if (colorModifier != null) {
                String colorModifierValue = details.getColorModifierValue();
                if (colorModifierValue != null) {
                    int percent = Integer.parseInt(colorModifierValue.substring(0, colorModifierValue.length() - 1));
                    parentValue = colorModifier.equals("d")
                            ? ThemeVariableUtils.darken(parentValue, percent)
                            : ThemeVariableUtils.lighten(parentValue, percent);

                    setThemeVariable(parentValue);
                }
            }
        }

        valueField.setValue(null);
        colorValueField.setValue(parentValue);
        this.parentValue = parentValue;

        String placeHolder = valueField.getInputPrompt();
        if (!placeHolder.startsWith("var(")) {
            valueField.setInputPrompt(parentValue);
        }
    }

    @Override
    public Subscription addValueChangeListener(Consumer<ValueChangeEvent<ThemeVariable>> listener) {
        // do nothing
        return null;
    }

    public Subscription addColorValueChangeListener(Consumer<ValueChangeEvent<String>> listener) {
        colorValueField.addValueChangeListener(this::onChange);
        valueField.addValueChangeListener(this::onChange);
        return getEventHub().subscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    protected void onChange(ValueChangeEvent<String> event) {
        if (event.isUserOriginated()) {
            fireValueChangeEvent(ThemeVariableUtils.getColorString(event.getValue()));
        }
    }

    protected void fireValueChangeEvent(String value) {
        ValueChangeEvent<String> valueChangeEvent = new ValueChangeEvent<>(valueField, value, value);
        publish(ValueChangeEvent.class, valueChangeEvent);
    }

    @Override
    public void removeValueChangeListener(Consumer<ValueChangeEvent<ThemeVariable>> listener) {
        colorValueField.removeValueChangeListener(this::onChange);
        valueField.removeValueChangeListener(this::onChange);
        getEventHub().unsubscribe(ValueChangeEvent.class, (Consumer) listener);
    }

    @Override
    public boolean isValid() {
        return colorValueField.isValid()
                && valueField.isValid();
    }

    @Override
    public void validate() throws ValidationException {
        valueField.validate();
        colorValueField.validate();
    }

    @Override
    public String getInputPrompt() {
        return valueField.getInputPrompt();
    }

    @Override
    public void setInputPrompt(String inputPrompt) {
        valueField.setInputPrompt(inputPrompt);
    }

    @Override
    public boolean isRequired() {
        return valueField.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        valueField.setRequired(required);
    }

    @Override
    public String getRequiredMessage() {
        return valueField.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(String msg) {
        valueField.setRequiredMessage(msg);
    }

    @Override
    public void addValidator(Consumer<? super ThemeVariable> validator) {
        // do nothing
    }

    @Override
    public void removeValidator(Consumer<ThemeVariable> validator) {
        // do nothing
    }

    @Override
    public Collection<Consumer<ThemeVariable>> getValidators() {
        // do nothing
        return null;
    }

    @Override
    public boolean isEditable() {
        return valueField.isEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        valueField.setEditable(editable);
        colorValueField.setEditable(editable);
    }

    @Nullable
    @Override
    public String getCaption() {
        return captionField.getValue();
    }

    @Override
    public void setCaption(@Nullable String caption) {
        captionField.setValue(caption);
    }

    @Nullable
    @Override
    public String getDescription() {
        return valueField.getDescription();
    }

    @Override
    public void setDescription(@Nullable String description) {
        valueField.setDescription(description);
        colorValueField.setDescription(description);
    }

    @Override
    public String getContextHelpText() {
        return valueField.getContextHelpText();
    }

    @Override
    public void setContextHelpText(String contextHelpText) {
        valueField.setContextHelpText(contextHelpText);
    }

    @Override
    public boolean isContextHelpTextHtmlEnabled() {
        return valueField.isContextHelpTextHtmlEnabled();
    }

    @Override
    public void setContextHelpTextHtmlEnabled(boolean enabled) {
        valueField.setContextHelpTextHtmlEnabled(enabled);
    }

    @Override
    public Consumer<ContextHelpIconClickEvent> getContextHelpIconClickHandler() {
        return valueField.getContextHelpIconClickHandler();
    }

    @Override
    public void setContextHelpIconClickHandler(@Nullable Consumer<ContextHelpIconClickEvent> handler) {
        valueField.setContextHelpIconClickHandler(handler);
    }

    @Override
    public boolean isCaptionAsHtml() {
        return captionField.isHtmlEnabled();
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml) {
        captionField.setHtmlEnabled(captionAsHtml);
    }

    @Override
    public boolean isDescriptionAsHtml() {
        return valueField.isDescriptionAsHtml();
    }

    @Override
    public void setDescriptionAsHtml(boolean descriptionAsHtml) {
        valueField.setDescriptionAsHtml(descriptionAsHtml);
    }

    @Override
    public boolean isHtmlSanitizerEnabled() {
        return valueField.isHtmlSanitizerEnabled();
    }

    @Override
    public void setHtmlSanitizerEnabled(boolean htmlSanitizerEnabled) {
        valueField.setHtmlSanitizerEnabled(htmlSanitizerEnabled);
        captionField.setHtmlSanitizerEnabled(htmlSanitizerEnabled);
        colorValueField.setHtmlSanitizerEnabled(htmlSanitizerEnabled);
    }

    @Override
    public void setValueSource(@Nullable ValueSource<ThemeVariable> valueSource) {
        // do nothing
    }

    @Nullable
    @Override
    public ValueSource<ThemeVariable> getValueSource() {
        // do nothing
        return null;
    }
}
