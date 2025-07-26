package mdsol.torilhosaddon.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class HealthBarConfig {
    boolean enabled = true;
    boolean hideWhenFull = false;
    boolean showNumber = true;
    float textScale = 0.02f;
    boolean drawTextShadow = false;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    int textColor = 0xFFFFFFFF;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    int borderColor = 0xFFFFFFFF;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    int bgColor = 0xFF000000;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    int defaultColor = 0xFF40CC40;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    int altColor1 = 0xFFFFCC40;

    @ConfigEntry.ColorPicker(allowAlpha = true)
    int altColor2 = 0xFFCC3030;

    @ConfigEntry.BoundedDiscrete(max = 100)
    int altColor1Threshold = 75;

    @ConfigEntry.BoundedDiscrete(max = 100)
    int altColor2Threshold = 40;

    float width = 1.2f;
    float height = 0.22f;
    float borderWidth = 0.02f;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    PositionAnchor positionAnchor = PositionAnchor.PLAYER_FEET;

    float positionOffsetX = 0;
    float positionOffsetY = -0.4f;
    float positionOffsetZ = 0;

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    Orientation orientation = Orientation.HORIZONTAL;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean hideWhenFull() {
        return hideWhenFull;
    }

    public boolean showNumber() {
        return showNumber;
    }

    public float getTextScale() {
        return textScale;
    }

    public boolean drawTextShadow() {
        return drawTextShadow;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public int getAltColor1() {
        return altColor1;
    }

    public int getAltColor2() {
        return altColor2;
    }

    public int getAltColor1Threshold() {
        return altColor1Threshold;
    }

    public int getAltColor2Threshold() {
        return altColor2Threshold;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public int getBgColor() {
        return bgColor;
    }

    public PositionAnchor getPositionAnchor() {
        return positionAnchor;
    }

    public float getPositionOffsetX() {
        return positionOffsetX;
    }

    public float getPositionOffsetY() {
        return positionOffsetY;
    }

    public float getPositionOffsetZ() {
        return positionOffsetZ;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public enum PositionAnchor {
        PLAYER_FEET,
        PLAYER_CENTER,
        PLAYER_HEAD
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }
}
