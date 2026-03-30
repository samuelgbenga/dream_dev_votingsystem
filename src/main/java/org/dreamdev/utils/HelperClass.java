package org.dreamdev.utils;

import org.dreamdev.models.Permission;

import java.util.List;

public class HelperClass {
    public static boolean hasPermission(List<Permission> permissions, Permission existingPermission) {
        for(Permission permission: permissions){
            if(permission.equals(existingPermission)) return true;
        }
        return false;
    }
}
