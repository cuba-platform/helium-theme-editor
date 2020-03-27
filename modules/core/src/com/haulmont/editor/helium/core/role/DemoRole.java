package com.haulmont.editor.helium.core.role;

import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition;
import com.haulmont.cuba.security.app.role.annotation.Role;
import com.haulmont.cuba.security.app.role.annotation.ScreenAccess;
import com.haulmont.cuba.security.role.ScreenPermissionsContainer;

@Role(name = "demo", description = "Demo role")
public class DemoRole extends AnnotatedRoleDefinition {
    @ScreenAccess(screenIds = {"respMainScreen", "helium_Sandbox"})
    @Override
    public ScreenPermissionsContainer screenPermissions() {
        return super.screenPermissions();
    }
}
