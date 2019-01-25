package com.marksmile.icp.tools.authcode;

import java.awt.Color;

public class ColorUtil {

    public static Color getColor(int rgb) {
        return new Color((rgb & 0xff0000) >> 16, (rgb & 0xff00) >> 8, (rgb & 0xff));
    }

    public static double getColorDistance(Color c1, Color c2) {
        return Math.sqrt(Math.pow(c1.getRed() - c2.getRed(),2) + Math.pow(c1.getGreen() - c2.getGreen(),2) + Math.pow(c1.getBlue() - c2.getBlue(),2));
    }

    public static Color getColorAvg(Color[] cs) {
        int r = 0, g = 0, b = 0;
        for (Color color : cs) {
            r = r + color.getRed();
            g = g + color.getGreen();
            b = b + color.getBlue();
        }
        int l = cs.length;
        return new Color(r / l, g / l, b / l);
    }
}
