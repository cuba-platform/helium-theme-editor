package com.haulmont.editor.helium.web.screens.sandbox;

import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.gui.Dialogs;
import com.haulmont.cuba.gui.Notifications;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.app.core.inputdialog.DialogActions;
import com.haulmont.cuba.gui.app.core.inputdialog.InputParameter;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.data.datagrid.ContainerDataGridItems;
import com.haulmont.cuba.gui.components.data.datagrid.ContainerTreeDataGridItems;
import com.haulmont.cuba.gui.components.data.table.ContainerGroupTableItems;
import com.haulmont.cuba.gui.components.data.table.ContainerTableItems;
import com.haulmont.cuba.gui.components.data.table.ContainerTreeTableItems;
import com.haulmont.cuba.gui.components.data.tree.ContainerTreeItems;
import com.haulmont.cuba.gui.components.mainwindow.SideMenu;
import com.haulmont.cuba.gui.components.validation.NotEmptyValidator;
import com.haulmont.cuba.gui.icons.CubaIcon;
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

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UiController("helium_ComponentsSandbox")
@UiDescriptor("components-sandbox.xml")
public class ComponentsSandbox extends ScreenFragment {

    protected static final int SAMPLE_DATA_SIZE = 10;

    // Basic functionality

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
    protected SideMenu sideMenuSample;

    @Inject
    protected Metadata metadata;
    @Inject
    protected DataComponents dataComponents;
    @Inject
    protected UserSettingsTools userSettingsTools;

    protected String appWindowTheme;

    protected CollectionContainer<User> usersDc;
    protected CollectionContainer<Group> groupsDc;

    @Subscribe
    public void onInit(InitEvent event) {
        appWindowTheme = userSettingsTools.loadAppWindowTheme();
        initDataContainers();
        initContainerSamples();
        initOptions();
        initSideMenuSample();
    }

    protected void initDataContainers() {
        usersDc = dataComponents.createCollectionContainer(User.class);
        usersDc.setItems(generateUsersSampleData());

        groupsDc = dataComponents.createCollectionContainer(Group.class);
        groupsDc.setItems(generateGroupsSampleData());
    }

    protected void initContainerSamples() {
        basicTable.setItems(new ContainerTableItems<>(usersDc));
        basicTokenList.setValue(usersDc.getItems());
    }

    protected List<User> generateUsersSampleData() {
        List<User> users = new ArrayList<>(SAMPLE_DATA_SIZE);
        for (int i = 0; i < SAMPLE_DATA_SIZE; i++) {
            users.add(createUser(i));
        }
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

    protected User createUser(int index) {
        User user = metadata.create(User.class);
        user.setLogin("user" + index);
        user.setName("User " + index);
        user.setActive(index % 2 == 0);

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

    protected void initSideMenuSample() {
        sideMenuSample.addMenuItem(createMenuItem("menu-item-1", "Customers", CubaIcon.USER.source()));
        sideMenuSample.addMenuItem(createMenuItem("menu-item-2", "Cars", CubaIcon.CAR.source()));
        sideMenuSample.addMenuItem(createMenuItem("menu-item-3", "Products", CubaIcon.SHOPPING_CART.source()));
        sideMenuSample.addMenuItem(createMenuItem("menu-item-4", "Orders", CubaIcon.DRIVERS_LICENSE.source()));
        sideMenuSample.addMenuItem(createMenuItem("menu-item-5", "Books", CubaIcon.BOOK.source()));
    }

    protected SideMenu.MenuItem createMenuItem(String id, String caption, String icon) {
        SideMenu.MenuItem item = sideMenuSample.createMenuItem(id, caption);
        item.setIcon(icon);
        return item;
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
    protected TokenList<User> tokenListR1;
    @Inject
    protected TokenList<User> tokenListR2;
    @Inject
    protected TokenList<User> tokenListR3;
    @Inject
    protected TokenList<User> tokenListSmall;
    @Inject
    protected TokenList<User> tokenListSimpleRO;
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
    protected TokenList<User> tokenListDisabledLookup;
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

        tokenListRO.setValue(usersDc.getItems());
        tokenListR1.setValue(usersDc.getItems());
        tokenListR2.setValue(usersDc.getItems());
        tokenListR3.setValue(usersDc.getItems());
        tokenListSmall.setValue(usersDc.getItems());
        tokenListSimpleRO.setValue(usersDc.getItems());
        tokenListSimple.setValue(usersDc.getItems());
        tokenListSample.setValue(usersDc.getItems());
        tokenListMiddle.setValue(usersDc.getItems());
        tokenListLarge.setValue(usersDc.getItems());
        tokenListInline.setValue(usersDc.getItems());
        tokenListDisabledSimple.setValue(usersDc.getItems());
        tokenListDisabledLookup.setValue(usersDc.getItems());
        tokenListDisabled.setValue(usersDc.getItems());

        tokenListR1.addValidator(getBeanLocator().get(NotEmptyValidator.NAME));
        tokenListR2.addValidator(getBeanLocator().get(NotEmptyValidator.NAME));
        tokenListR3.addValidator(getBeanLocator().get(NotEmptyValidator.NAME));
        try {
            tokenListR1.validate();
        } catch (Exception ignored) {
        }
        try {
            tokenListR3.validate();
        } catch (Exception ignored) {
        }
        try {
            tokenListR2.validate();
        } catch (Exception ignored) {
        }

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
        dataGridSample.setItems(new ContainerDataGridItems<>(usersDc));

        treeDataGridSample.setItems(new ContainerTreeDataGridItems<>(groupsDc, "parent"));
        treeTableSample.setItems(new ContainerTreeTableItems<>(groupsDc, "parent"));
        tree.setItems(new ContainerTreeItems<>(groupsDc, "parent"));
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
//                .withHideDelayMs(-1)
                .show();
    }

    @Subscribe("showHumanizedBtn")
    public void onShowHumanizedBtnClick(Button.ClickEvent event) {
        notifications.create()
                .withCaption("Humanized notification")
                .withDescription("Hi there! I’m a CUBA’s humanized message")
                .withType(Notifications.NotificationType.HUMANIZED)
//                .withHideDelayMs(-1)
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
