package com.haulmont.editor.helium.web.tools;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;

/**
 * Color presets
 */
public enum ColorPreset implements EnumClass<String> {
    LIGHT("light"),
    DARK("dark"),
    BLUE("blue"),
    CUSTOM("custom");

    private String id;

    ColorPreset(String id) {
        this.id = id;
    }

    @Nullable
    public static ColorPreset fromId(String name) {
        for (ColorPreset val : ColorPreset.values()) {
            if (name.equals(val.getId())) {
                return val;
            }
        }
        return null;
    }


    @Override
    public String getId() {
        return id;
    }
}
