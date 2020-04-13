package com.haulmont.editor.helium.web.screens.sandbox;

import com.haulmont.addon.helium.web.theme.HeliumThemeVariantsManager;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.app.core.inputdialog.DialogActions;
import com.haulmont.cuba.gui.app.core.inputdialog.InputParameter;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.calendar.ListCalendarEventProvider;
import com.haulmont.cuba.gui.components.calendar.SimpleCalendarEvent;
import com.haulmont.cuba.gui.components.data.datagrid.ContainerDataGridItems;
import com.haulmont.cuba.gui.components.data.datagrid.ContainerTreeDataGridItems;
import com.haulmont.cuba.gui.components.data.table.ContainerGroupTableItems;
import com.haulmont.cuba.gui.components.data.table.ContainerTableItems;
import com.haulmont.cuba.gui.components.data.table.ContainerTreeTableItems;
import com.haulmont.cuba.gui.components.data.tree.ContainerTreeItems;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.model.DataComponents;
import com.haulmont.cuba.gui.screen.Install;
import com.haulmont.cuba.gui.screen.ScreenFragment;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.RoleType;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.app.UserSettingsTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UiController("helium_ComponentsSandbox")
@UiDescriptor("components-sandbox.xml")
public class ComponentsSandbox extends ScreenFragment {

    protected static final int SAMPLE_DATA_SIZE = 10;

    // Basic functionality

    @Inject
    protected LookupField<String> sizeField;
    @Inject
    protected TabSheet previewTabSheet;
    @Inject
    protected ScrollBoxLayout innerPreviewBox;
    @Inject
    protected Table<User> basicTable;
    @Inject
    protected TokenList<User> basicTokenList;
    @Inject
    protected LookupField<String> basicRequiredLookupField;
    @Inject
    protected LookupField<String> basicLookupField;
    @Inject
    protected RadioButtonGroup<String> basicRadioButtonGroup;
    @Inject
    protected CheckBoxGroup<String> basicCheckBoxGroup;

    @Inject
    protected Metadata metadata;
    @Inject
    protected DataComponents dataComponents;
    @Inject
    protected UserSettingsTools userSettingsTools;
    @Inject
    protected HeliumThemeVariantsManager variantsManager;

    protected String appWindowTheme;

    protected CollectionContainer<User> usersDc;
    protected CollectionContainer<Group> groupsDc;

    @Subscribe
    public void onInit(InitEvent event) {
        appWindowTheme = userSettingsTools.loadAppWindowTheme();
        initSizeField();
        initDataContainers();
        initContainerSamples();
        initOptions();
    }

    @Subscribe("sizeField")
    public void onSizeFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        String size = event.getValue() == null
                ? ""
                : event.getValue();

        innerPreviewBox.setStyleName(size);
    }

    protected void initSizeField() {
        sizeField.setOptionsList(variantsManager.getAppThemeSizeList());
        sizeField.setValue(variantsManager.loadUserAppThemeSizeSetting());
    }

    protected void initDataContainers() {
        usersDc = dataComponents.createCollectionContainer(User.class);
        usersDc.setItems(generateUsersSampleData());

        groupsDc = dataComponents.createCollectionContainer(Group.class);
        groupsDc.setItems(generateGroupsSampleData());
    }

    protected void initContainerSamples() {
        basicTable.setItems(new ContainerTableItems<>(usersDc));

        basicTokenList.setOptionsList(usersDc.getItems());
        basicTokenList.setValue(basicTokenList.getOptions()
                .getOptions()
                .skip(2)
                .collect(Collectors.toList()));
    }

    protected List<User> generateUsersSampleData() {
        List<User> users = new ArrayList<>(SAMPLE_DATA_SIZE);
        users.add(createUser("Romeo Montague", "romeo", true));
        users.add(createUser("Juliet Capulet", "juliet", true));
        users.add(createUser("Dave Paris", "daveParis", false));
        users.add(createUser("Ted Montague", "tedMontague", false));
        users.add(createUser("Caroline Montague", "carolinMontague", true));
        users.add(createUser("Fulgencio Capulet", "fulgencio", true));
        users.add(createUser("Gloria Capulet", "gloriaCapulet", false));
        return users;
    }

    protected List<Group> generateGroupsSampleData() {
        List<Group> groups = new ArrayList<>(SAMPLE_DATA_SIZE);
        for (int i = 0; i < SAMPLE_DATA_SIZE; i++) {
            Group parent = null;
            if (i > 0) {
                parent = groups.get(i - 1);
            }
            groups.add(createGroup(i, parent));
        }
        return groups;
    }

    protected User createUser(String name, String login, boolean active) {
        User user = metadata.create(User.class);
        user.setName(name);
        user.setLogin(login);
        user.setActive(active);
        return user;
    }

    protected Group createGroup(int index, Group parent) {
        Group group = metadata.create(Group.class);
        group.setName("group" + index);
        if (parent != null) {
            group.setParent(parent);
        }
        return group;
    }

    protected void initOptions() {
        List<String> options = generateSampleOptions();
        basicCheckBoxGroup.setOptionsList(options);
        basicRadioButtonGroup.setOptionsList(options);
        basicLookupField.setOptionsList(options);
        basicRequiredLookupField.setOptionsList(options);
    }

    protected List<String> generateSampleOptions() {
        return Arrays.asList("Option 1", "Options 2", "Option 3");
    }

    // All components

    @Inject
    protected TextField<String> textFieldRO;
    @Inject
    protected TextField<String> textFieldD;

    @Inject
    protected LookupField<RoleType> lookupFieldRO;
    @Inject
    protected LookupField<RoleType> lookupFieldD;

    @Inject
    protected PickerField<User> pickerFieldRO;
    @Inject
    protected PickerField<User> pickerFieldD;
    @Inject
    protected PickerField<User> pickerFieldSmall;
    @Inject
    protected PickerField<User> pickerFieldMiddle;
    @Inject
    protected PickerField<User> pickerFieldLarge;
    @Inject
    protected PickerField<User> pickerField;

    @Inject
    protected LookupPickerField<User> lookupPickerFieldR;
    @Inject
    protected LookupPickerField<User> lookupPickerField;
    @Inject
    protected LookupPickerField<User> lookupPickerFieldRO;
    @Inject
    protected LookupPickerField<User> lookupPickerFieldD;
    @Inject
    protected LookupPickerField<User> lookupPickerFieldMiddle;
    @Inject
    protected LookupPickerField<User> lookupPickerFieldSmall;
    @Inject
    protected LookupPickerField<User> lookupPickerFieldLarge;

    @Inject
    protected CheckBox checkBoxRO2;
    @Inject
    protected CheckBox checkBoxD2;

    @Inject
    protected RadioButtonGroup<RoleType> radioButtonGroupD;
    @Inject
    protected RadioButtonGroup<RoleType> radioButtonGroupRO;

    @Inject
    protected TokenList<User> tokenListRO;
    @Inject
    protected TokenList<User> tokenListSmall;
    @Inject
    protected TokenList<User> tokenListSimple;
    @Inject
    protected TokenList<User> tokenListSample;
    @Inject
    protected TokenList<User> tokenListMiddle;
    @Inject
    protected TokenList<User> tokenListLarge;
    @Inject
    protected TokenList<User> tokenListInline;
    @Inject
    protected TokenList<User> tokenListDisabledSimple;
    @Inject
    protected TokenList<User> tokenListDisabled;

    @Inject
    protected Table<User> tableSample;
    @Inject
    protected GroupTable<User> groupTableSample;
    @Inject
    protected Table<User> largeTableSample;
    @Inject
    protected Table<User> middleTableSample;
    @Inject
    protected Table<User> smallTableSample;
    @Inject
    protected Table<User> tablePopupView;
    @Inject
    protected TreeTable<Group> treeTableSample;

    @Inject
    protected DataGrid<User> dataGridSample;
    @Inject
    protected TreeDataGrid<Group> treeDataGridSample;

    @Inject
    protected TabSheet tabSheet;
    @Inject
    protected FlowBoxLayout tabSheetStylesBox;

    @Inject
    protected Tree<Group> tree;

    @Inject
    protected SourceCodeEditor codeEditor;
    @Inject
    protected SourceCodeEditor codeEditorD;
    @Inject
    protected SourceCodeEditor codeEditorRO;

    @Inject
    protected CheckBox highlightActiveLineCheck;
    @Inject
    protected CheckBox printMarginCheck;
    @Inject
    protected CheckBox showGutterCheck;

    @Inject
    protected TwinColumn<Group> twinColumnLarge;
    @Inject
    protected TwinColumn<Group> twinColumnMiddle;
    @Inject
    protected TwinColumn<Group> twinColumnSmall;
    @Inject
    protected TwinColumn<Group> twinColumnRequired;
    @Inject
    protected TwinColumn<Group> twinColumnSample;

    @Inject
    protected ProgressBar progressBarP;
    @Inject
    protected ProgressBar progressBar;

    @Inject
    protected SuggestionField<User> suggestionFieldLarge;
    @Inject
    protected SuggestionField<User> suggestionFieldMiddle;
    @Inject
    protected SuggestionField<User> suggestionFieldSmall;
    @Inject
    protected SuggestionField<User> suggestionFieldDisabled;
    @Inject
    protected SuggestionField<User> suggestionFieldReadOnly;
    @Inject
    protected SuggestionField<User> suggestionFieldRequired;
    @Inject
    protected SuggestionField<User> suggestionFieldSample;

    @Inject
    protected SuggestionPickerField<User> suggestionPickerFieldLarge;
    @Inject
    protected SuggestionPickerField<User> suggestionPickerFieldMiddle;
    @Inject
    protected SuggestionPickerField<User> suggestionPickerFieldSmall;
    @Inject
    protected SuggestionPickerField<User> suggestionPickerFieldDisabled;
    @Inject
    protected SuggestionPickerField<User> suggestionPickerFieldReadonly;
    @Inject
    protected SuggestionPickerField<User> suggestionPickerFieldSample;

    @Inject
    protected Calendar<Date> monthCalendar;
    @Inject
    protected Calendar<Date> weekCalendar;
    @Inject
    protected Calendar<Date> dayCalendar;

    @Inject
    protected Dialogs dialogs;
    @Inject
    protected Notifications notifications;
    @Inject
    protected UiComponents uiComponents;

    @Subscribe
    public void onAfterInit(AfterInitEvent event) {
        textFieldRO.setValue("Value");
        textFieldD.setValue("Value");

        lookupFieldRO.setValue(RoleType.DENYING);
        lookupFieldD.setValue(RoleType.DENYING);

        checkBoxRO2.setValue(true);
        checkBoxD2.setValue(true);

        pickerField.setValue(usersDc.getItems().get(0));
        pickerFieldRO.setValue(usersDc.getItems().get(0));
        pickerFieldD.setValue(usersDc.getItems().get(0));
        pickerFieldLarge.setValue(usersDc.getItems().get(0));
        pickerFieldMiddle.setValue(usersDc.getItems().get(0));
        pickerFieldSmall.setValue(usersDc.getItems().get(0));

        lookupPickerField.setOptionsList(usersDc.getItems());
        lookupPickerFieldR.setOptionsList(usersDc.getItems());
        lookupPickerFieldRO.setOptionsList(usersDc.getItems());
        lookupPickerFieldD.setOptionsList(usersDc.getItems());
        lookupPickerFieldLarge.setOptionsList(usersDc.getItems());
        lookupPickerFieldMiddle.setOptionsList(usersDc.getItems());
        lookupPickerFieldSmall.setOptionsList(usersDc.getItems());

        lookupPickerField.setValue(usersDc.getItems().get(0));
        lookupPickerFieldR.setValue(usersDc.getItems().get(0));
        lookupPickerFieldRO.setValue(usersDc.getItems().get(0));
        lookupPickerFieldD.setValue(usersDc.getItems().get(0));
        lookupPickerFieldLarge.setValue(usersDc.getItems().get(0));
        lookupPickerFieldMiddle.setValue(usersDc.getItems().get(0));
        lookupPickerFieldSmall.setValue(usersDc.getItems().get(0));

        radioButtonGroupRO.setValue(RoleType.DENYING);
        radioButtonGroupD.setValue(RoleType.DENYING);

        tokenListRO.setOptionsList(usersDc.getItems());
        tokenListSmall.setOptionsList(usersDc.getItems());
        tokenListSimple.setOptionsList(usersDc.getItems());
        tokenListSample.setOptionsList(usersDc.getItems());
        tokenListMiddle.setOptionsList(usersDc.getItems());
        tokenListLarge.setOptionsList(usersDc.getItems());
        tokenListInline.setOptionsList(usersDc.getItems());
        tokenListDisabledSimple.setOptionsList(usersDc.getItems());
        tokenListDisabled.setOptionsList(usersDc.getItems());

        tokenListRO.setValue(tokenListRO.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListSmall.setValue(tokenListSmall.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListSimple.setValue(tokenListSimple.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListSample.setValue(tokenListSample.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListMiddle.setValue(tokenListMiddle.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListLarge.setValue(tokenListLarge.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListInline.setValue(tokenListInline.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListDisabledSimple.setValue(tokenListDisabledSimple.getOptions().getOptions().skip(2).collect(Collectors.toList()));
        tokenListDisabled.setValue(tokenListDisabled.getOptions().getOptions().skip(2).collect(Collectors.toList()));

        tabSheetStylesBox.getComponents().stream()
                .filter(component -> component instanceof CheckBox)
                .map(component -> ((CheckBox) component))
                .forEach(checkBox -> checkBox.addValueChangeListener(this::changeTableStyle));

        tree.expandTree();

        highlightActiveLineCheck.setValue(codeEditor.isHighlightActiveLine());
        printMarginCheck.setValue(codeEditor.isShowPrintMargin());
        showGutterCheck.setValue(codeEditor.isShowGutter());

        codeEditorRO.setValue("highlightActiveLineCheck.setValue(codeEditor.isHighlightActiveLine());");
        codeEditorD.setValue("highlightActiveLineCheck.setValue(codeEditor.isHighlightActiveLine());");

        tableSample.setItems(new ContainerTableItems<>(usersDc));
        groupTableSample.setItems(new ContainerGroupTableItems<>(usersDc));
        smallTableSample.setItems(new ContainerTableItems<>(usersDc));
        middleTableSample.setItems(new ContainerTableItems<>(usersDc));
        largeTableSample.setItems(new ContainerTableItems<>(usersDc));
        tablePopupView.setItems(new ContainerTableItems<>(usersDc));
        dataGridSample.setItems(new ContainerDataGridItems<>(usersDc));

        treeDataGridSample.setItems(new ContainerTreeDataGridItems<>(groupsDc, "parent"));
        treeTableSample.setItems(new ContainerTreeTableItems<>(groupsDc, "parent"));
        tree.setItems(new ContainerTreeItems<>(groupsDc, "parent"));

        twinColumnSample.setOptionsList(groupsDc.getItems());
        twinColumnRequired.setOptionsList(groupsDc.getItems());
        twinColumnSmall.setOptionsList(groupsDc.getItems());
        twinColumnMiddle.setOptionsList(groupsDc.getItems());
        twinColumnLarge.setOptionsList(groupsDc.getItems());

        progressBar.setValue(0.5);
        progressBarP.setValue(0.5);

        suggestionFieldSample.setSearchExecutor(this::userSearchExecutor);
        suggestionFieldReadOnly.setSearchExecutor(this::userSearchExecutor);
        suggestionFieldDisabled.setSearchExecutor(this::userSearchExecutor);
        suggestionFieldRequired.setSearchExecutor(this::userSearchExecutor);
        suggestionFieldLarge.setSearchExecutor(this::userSearchExecutor);
        suggestionFieldMiddle.setSearchExecutor(this::userSearchExecutor);
        suggestionFieldSmall.setSearchExecutor(this::userSearchExecutor);

        suggestionPickerFieldSample.setSearchExecutor(this::userSearchExecutor);
        suggestionPickerFieldReadonly.setSearchExecutor(this::userSearchExecutor);
        suggestionPickerFieldDisabled.setSearchExecutor(this::userSearchExecutor);
        suggestionPickerFieldLarge.setSearchExecutor(this::userSearchExecutor);
        suggestionPickerFieldMiddle.setSearchExecutor(this::userSearchExecutor);
        suggestionPickerFieldSmall.setSearchExecutor(this::userSearchExecutor);

        ListCalendarEventProvider eventProvider = new ListCalendarEventProvider();

        SimpleCalendarEvent<Date> calendarEvent1 = new SimpleCalendarEvent<>();
        calendarEvent1.setCaption("Event 1");
        calendarEvent1.setDescription("Description 1");
        calendarEvent1.setStart(new Date(2020 - 1900, 2, 23));
        calendarEvent1.setEnd(DateUtils.addHours(calendarEvent1.getStart(), 4));
        eventProvider.addEvent(calendarEvent1);

        SimpleCalendarEvent<Date> calendarEvent2 = new SimpleCalendarEvent<>();
        calendarEvent2.setCaption("Event 2");
        calendarEvent2.setDescription("Description 2");
        calendarEvent2.setStart(new Date(2020 - 1900, 2, 25));
        calendarEvent2.setEnd(DateUtils.addHours(calendarEvent2.getStart(), 6));
        eventProvider.addEvent(calendarEvent2);

        SimpleCalendarEvent<Date> calendarEvent3 = new SimpleCalendarEvent<>();
        calendarEvent3.setCaption("Event 3");
        calendarEvent3.setDescription("Description 3");
        calendarEvent3.setStart(new Date(2020 - 1900, 2, 26));
        calendarEvent3.setEnd(DateUtils.addHours(calendarEvent3.getStart(), 2));
        calendarEvent3.setAllDay(true);
        eventProvider.addEvent(calendarEvent3);

        monthCalendar.setEventProvider(eventProvider);
        weekCalendar.setEventProvider(eventProvider);
        dayCalendar.setEventProvider(eventProvider);

        monthCalendar.addRangeSelectListener(dateCalendarRangeSelectEvent -> {
        });
        weekCalendar.addRangeSelectListener(dateCalendarRangeSelectEvent -> {
        });
        dayCalendar.addRangeSelectListener(dateCalendarRangeSelectEvent -> {
        });


        dayCalendar.setStartDate(new Date());
        dayCalendar.setEndDate(new Date());
    }

    protected List<User> userSearchExecutor(String searchString, Map<String, Object> searchParams) {
        return usersDc.getItems().stream()
                .filter(user -> StringUtils.containsIgnoreCase(user.getName(), searchString))
                .collect(Collectors.toList());
    }

    protected void changeTableStyle(HasValue.ValueChangeEvent<Boolean> e) {
        String id = e.getComponent().getId();
        Boolean checked = e.getValue();
        if (checked != null) {
            if (checked) {
                tabSheet.addStyleName(prepareStyleName(id));
            } else {
                tabSheet.removeStyleName(prepareStyleName(id));
            }
        }
    }

    protected String prepareStyleName(@Nullable String stylename) {
        if (stylename == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stylename.length(); i++) {
            char c = stylename.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("-").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Install(to = "dataGridSample", subject = "detailsGenerator")
    protected Component dataGridDetailsGenerator(User user) {
        VBoxLayout mainLayout = uiComponents.create(VBoxLayout.class);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("200px");

        return mainLayout;
    }

    @Subscribe("showDetailsBtn")
    public void onShowDetailsBtnClick(Button.ClickEvent event) {
        User singleSelected = dataGridSample.getSingleSelected();
        if (singleSelected != null) {
            dataGridSample.setDetailsVisible(singleSelected, true);
        }
    }

    @Subscribe("closeDetailsBtn")
    public void onCloseDetailsBtnClick(Button.ClickEvent event) {
        User singleSelected = dataGridSample.getSingleSelected();
        if (singleSelected != null) {
            dataGridSample.setDetailsVisible(singleSelected, false);
        }
    }

    @Subscribe("showMessageDialogBtn")
    public void onShowMessageDialogBtnClick(Button.ClickEvent event) {
        dialogs.createMessageDialog()
                .withCaption("Confirmation")
                .withMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Et sollicitudin quam massa id enim et. Purus parturient pretium arcu quis vitae feugiat sit quis. Sem dictum vel nisi, cursus purus nibh fermentum tortor. Ultrices scelerisque orci, ullamcorper imperdiet orci bibendum a, aliquet. Purus mauris vitae odio fermentum semper diam commodo quis. Pulvinar nulla duis adipiscing nunc eu laoreet laoreet. Ornare sodales donec malesuada id eu arcu lectus ipsum scelerisque.")
                .withType(Dialogs.MessageType.CONFIRMATION)
                .show();
    }

    @Subscribe("showOptionDialogBtn")
    public void onShowOptionDialogBtnClick(Button.ClickEvent event) {
        dialogs.createOptionDialog()
                .withCaption("Title")
                .withMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Et sollicitudin quam massa id enim et. Purus parturient pretium arcu quis vitae feugiat sit quis. Sem dictum vel nisi, cursus purus nibh fermentum tortor. Ultrices scelerisque orci, ullamcorper imperdiet orci bibendum a, aliquet. Purus mauris vitae odio fermentum semper diam commodo quis. Pulvinar nulla duis adipiscing nunc eu laoreet laoreet. Ornare sodales donec malesuada id eu arcu lectus ipsum scelerisque.")
                .withType(Dialogs.MessageType.CONFIRMATION)
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e ->
                                        notifications.create()
                                                .withCaption("OK pressed")
                                                .show()
                                ),

                        new DialogAction(DialogAction.Type.CANCEL))
                .show();
    }

    @Subscribe("showInputDialogBtn")
    public void onShowInputDialogBtnClick(Button.ClickEvent event) {
        dialogs.createInputDialog(this)
                .withCaption("Enter values")
                .withParameters(
                        InputParameter.stringParameter("name")
                                .withCaption("Name").withRequired(true),
                        InputParameter.doubleParameter("quantity")
                                .withCaption("Quantity").withDefaultValue(1.0),
                        InputParameter.enumParameter("roleType", RoleType.class)
                                .withCaption("Role Type")
                )
                .withActions(DialogActions.OK_CANCEL)
                .show();
    }

    @Subscribe("showTrayBtn")
    public void onShowTrayBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Tray notification")
                .withDescription("Hi there! I’m a CUBA’s tray message")
                .withType(Notifications.NotificationType.TRAY)
                .show();
    }

    @Subscribe("showHumanizedBtn")
    public void onShowHumanizedBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Humanized notification")
                .withDescription("Hi there! I’m a CUBA’s humanized message")
                .withType(Notifications.NotificationType.HUMANIZED)
                .show();
    }

    @Subscribe("showWarningBtn")
    public void onShowWarningBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Warning notification")
                .withDescription("Hi there! I’m a CUBA’s warning message")
                .withType(Notifications.NotificationType.WARNING)
                .withContentMode(ContentMode.HTML)
                .show();
    }

    @Subscribe("showErrorBtn")
    public void onShowErrorBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Error notification")
                .withDescription("Hi there! I’m a CUBA’s error message")
                .withType(Notifications.NotificationType.ERROR)
                .withContentMode(ContentMode.HTML)
                .show();
    }

    @Subscribe("showSystemBtn")
    public void onShowSystemBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("System notification")
                .withDescription("Hi there! I’m a CUBA’s system message")
                .withType(Notifications.NotificationType.SYSTEM)
                .withContentMode(ContentMode.HTML)
                .show();
    }

    @Subscribe("highlightActiveLineCheck")
    protected void onHighlightActiveLineCheckValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.getValue() != null) {
            codeEditor.setHighlightActiveLine(event.getValue());
        }
    }

    @Subscribe("printMarginCheck")
    protected void onPrintMarginCheckValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.getValue() != null) {
            codeEditor.setShowPrintMargin(event.getValue());
        }
    }

    @Subscribe("showGutterCheck")
    protected void onShowGutterCheckValueChange(HasValue.ValueChangeEvent<Boolean> event) {
        if (event.getValue() != null) {
            codeEditor.setShowGutter(event.getValue());
        }
    }
}
