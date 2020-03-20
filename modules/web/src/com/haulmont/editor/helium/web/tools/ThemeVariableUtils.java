package com.haulmont.editor.helium.web.tools;

import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.components.colorpicker.ColorUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThemeVariableUtils {

    // todo GD javadocs regexp
    // todo GD matching he_rgba
    protected static final Pattern RGB_PATTERN = Pattern.compile("([0-9]*), ([0-9]*), ([0-9]*)");
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
        return new Color(rgb).getCSS();
    }

    public static String lighten(String value, int percent) {
        Color color = ColorUtil.stringToColor(value);
        int[] hsl = calculateHslFromColor(color);
        hsl[2] = (int) (hsl[2] + (1 - hsl[2]) * percent * 0.01);
        int rgb = Color.HSLtoRGB(hsl[0], hsl[1], hsl[2]);
        return new Color(rgb).getCSS();
    }

    public static String getColorString(String value) {
        if (value != null) {
            Matcher rgbMatcher = RGB_PATTERN.matcher(value);
            if (rgbMatcher.find()) {
                Color color = new Color(Integer.parseInt(rgbMatcher.group(1)),
                        Integer.parseInt(rgbMatcher.group(2)),
                        Integer.parseInt(rgbMatcher.group(3)));
                return color.getCSS();
            }

            if (!value.startsWith("#")) {
                value = "#" + value;
            }

            Matcher hexMatcher = HEX_PATTERN.matcher(value);
            if (hexMatcher.find()) {
                return value;
            }
        }

        return null;
    }
}
