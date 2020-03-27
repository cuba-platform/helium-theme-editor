package com.haulmont.editor.helium.core.role;

import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition;
import com.haulmont.cuba.security.app.role.annotation.EntityAccess;
import com.haulmont.cuba.security.app.role.annotation.EntityAttributeAccess;
import com.haulmont.cuba.security.app.role.annotation.Role;
import com.haulmont.cuba.security.app.role.annotation.ScreenAccess;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.role.EntityAttributePermissionsContainer;
import com.haulmont.cuba.security.role.EntityPermissionsContainer;
import com.haulmont.cuba.security.role.ScreenPermissionsContainer;

@Role(name = "demo", description = "Demo role")
public class DemoRole extends AnnotatedRoleDefinition {
    @ScreenAccess(screenIds = {"respMainScreen", "helium_ComponentsSandbox", "helium_DownloadScreen"})
    @Override
    public ScreenPermissionsContainer screenPermissions() {
        return super.screenPermissions();
    }


    @EntityAccess(entityClass = Group.class, operations = EntityOp.READ)
    @EntityAccess(entityClass = User.class, operations = EntityOp.READ)
    @Override
    public EntityPermissionsContainer entityPermissions() {
        return super.entityPermissions();
    }

    @EntityAttributeAccess(entityClass = Group.class, view = {"name", "parent"})
    @EntityAttributeAccess(entityClass = User.class, view = {"login", "name", "active", "group"})
    @Override
    public EntityAttributePermissionsContainer entityAttributePermissions() {
        return super.entityAttributePermissions();
    }
}
