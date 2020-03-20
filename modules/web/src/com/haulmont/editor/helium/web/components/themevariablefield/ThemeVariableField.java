package com.haulmont.editor.helium.web.components.themevariablefield;

import com.haulmont.bali.events.Subscription;
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

@CompositeDescriptor("theme-variable-field.xml")
public class ThemeVariableField extends CompositeComponent<Form>
        implements Field<ThemeVariable>,
        CompositeWithIcon,
        HasInputPrompt {

    public static final String NAME = "themeVariableField";

    protected Label<String> captionField;
    protected TextField<String> valueField;
    protected ColorPicker colorValueField;
    protected JavaScriptComponent jsComponent;

    protected ThemeVariable themeVariable;
    protected ColorPreset currentColorPreset = ColorPreset.LIGHT;

    public ThemeVariableField() {
        addCreateListener(this::onCreate);
    }

    private void onCreate(CreateEvent createEvent) {
        captionField = getInnerComponent("captionField");
        valueField = getInnerComponent("valueField");
        colorValueField = getInnerComponent("colorValueField");
        jsComponent = getInnerComponent("jsComponent");

        initColorValueField();
        initValueField();
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
            String value = valueChangeEvent.getValue();
            if (value == null) {
                removeThemeVariable();
            } else {
                setThemeVariable(value);
            }

            if (valueChangeEvent.isUserOriginated()) {
                if (value == null) {
                    value = themeVariable.getThemeVariableDetails(currentColorPreset).getValue();
                }
                colorValueField.setValue(ThemeVariableUtils.getColorString(value));
            }
        });
    }

    protected void setThemeVariable(String value) {
        jsComponent.callFunction("setThemeVariable", themeVariable.getName(), value);
    }

    protected void removeThemeVariable() {
        jsComponent.callFunction("removeThemeVariable", themeVariable.getName());
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
            return;
        }

        currentColorPreset = colorPreset;

        String name = themeVariable.getName();
        setCaption(name);
        setDescription(name);

        setInputPrompt(details.getPlaceHolder());
        valueField.setValue(null);
        colorValueField.setValue(ThemeVariableUtils.getColorString(details.getValue()));
    }

    public void setColorValueByParent(String parentValue) {
        String value = parentValue;
        ThemeVariableDetails details = themeVariable.getThemeVariableDetails(currentColorPreset);
        String colorModifier = details.getColorModifier();
        if (colorModifier != null) {
            String colorModifierValue = details.getColorModifierValue();
            if (colorModifierValue != null) {
                int percent = Integer.parseInt(colorModifierValue.substring(0, colorModifierValue.length() - 1));
                value = colorModifier.equals("d")
                        ? ThemeVariableUtils.darken(parentValue, percent)
                        : ThemeVariableUtils.lighten(parentValue, percent);
            }
        }

        if (value == null) {
            value = details.getValue();
        }

        valueField.setValue(null);
        colorValueField.setValue(ThemeVariableUtils.getColorString(value));
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
            ValueChangeEvent<String> valueChangeEvent = new ValueChangeEvent<>(event.getComponent(),
                    event.getPrevValue(), ThemeVariableUtils.getColorString(event.getValue()), true);
            publish(ValueChangeEvent.class, valueChangeEvent);
        }
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
