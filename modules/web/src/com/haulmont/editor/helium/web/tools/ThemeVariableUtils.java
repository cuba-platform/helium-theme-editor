package com.haulmont.editor.helium.web.tools;

import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.components.colorpicker.ColorUtil;

import java.util.regex.Pattern;

public class ThemeVariableUtils {

    // todo GD javadocs regexp
    protected static final Pattern HEX_PATTERN = Pattern.compile("(#?([A-Fa-f0-9]){3}([A-Fa-f0-9]){3})");

    public static int[] calculateHslFromColor(Color color) {
        int[] hsl = new int[3];

        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;

        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float d = max - min;

        float h = 0f, s = 0f, l = 0f;

        if (max == min) {
            h = 0;
        }
        if (max == r) {
            h = 60 * (g - b) / d;
        } else if (max == g) {
            h = 60 * (b - r) / d + 120;
        } else if (max == b) {
            h = 60 * (r - g) / d + 240;
        }

        l = (max + min) / 2f;

        if (max == min) {
            s = 0;
        } else if (l < 0.5) {
            s = d / (2 * l);
        } else {
            s = d / (2 - 2 * l);
        }

        hsl[0] = (int) ((h + 360) % 360);
        hsl[1] = (int) (s * 100);
        hsl[2] = (int) (l * 100);
        // If saturation is 0, the hue is not well defined. Use hue 0 in this
        // case.
        if (hsl[1] == 0) {
            hsl[0] = 0;
        }

        return hsl;
    }

    public static String darken(String value, int percent) {
        Color color = ColorUtil.stringToColor(value);
        int[] hsl = calculateHslFromColor(color);
        hsl[2] = (int) (hsl[2] * (1 - percent * 0.01));
        int rgb = Color.HSLtoRGB(hsl[0], hsl[1], hsl[2]);
        return new Color(rgb).getCSS().toUpperCase();
    }

    public static String lighten(String value, int percent) {
        Color color = ColorUtil.stringToColor(value);
        int[] hsl = calculateHslFromColor(color);
        hsl[2] = (int) (hsl[2] + (1 - hsl[2]) * percent * 0.01);
        int rgb = Color.HSLtoRGB(hsl[0], hsl[1], hsl[2]);
        return new Color(rgb).getCSS().toUpperCase();
    }

    public static String getColorString(String value) {
        if (value != null) {
            if (!value.startsWith("#")) {
                value = "#" + value;
            }

            if (HEX_PATTERN.matcher(value).find()) {
                return value.toUpperCase();
            }

            throw new NumberFormatException("Supports only hex format");
        } else {
            return null;
        }
    }

    public static String hexStringToRGB(String hexValue) {
        if (hexValue == null) {
            return null;
        }

        if (HEX_PATTERN.matcher(hexValue).find()) {
            Color color = ColorUtil.stringToColor(hexValue);
            return color.getRed()
                    + ", "
                    + color.getGreen()
                    + ", "
                    + color.getBlue();
        } else {
            return null;
        }
    }
}
