package ru.solana.utils.render.color;

import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import ru.solana.utils.math.MathUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Objects;

@Getter
public class ColorRGBA {
    public static final ColorRGBA WHITE = new ColorRGBA(255, 255, 255);
    public static final ColorRGBA BLACK = new ColorRGBA(0, 0, 0);
    public static final ColorRGBA GREEN = new ColorRGBA(0, 255, 0);
    public static final ColorRGBA RED = new ColorRGBA(255, 0, 0);
    public static final ColorRGBA BLUE = new ColorRGBA(0, 0, 255);
    public static final ColorRGBA YELLOW = new ColorRGBA(255, 255, 0);
    public static final ColorRGBA GRAY = new ColorRGBA(88, 87, 93);
    public static final ColorRGBA TRANSPARENT = new ColorRGBA(0, 0, 0,0);

    private transient float[] hsbValues;

    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    public ColorRGBA(int color) {
        this(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), ColorUtil.alpha(color));
    }

    public ColorRGBA(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public ColorRGBA(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public ColorRGBA(int red, int green, int blue, int alpha) {
        red = MathHelper.clamp(red, 0, 255);
        green = MathHelper.clamp(green, 0, 255);
        blue = MathHelper.clamp(blue, 0, 255);
        alpha = MathHelper.clamp(alpha, 0, 255);

        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public int getRGB() {
        int a = Math.round(clamp(alpha));
        int r = Math.round(clamp(red));
        int g = Math.round(clamp(green));
        int b = Math.round(clamp(blue));
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private int clamp(float value) {
        return (int) Math.max(0, Math.min(255, value));
    }

    public ColorRGBA withAlpha(int newAlpha) {
        return new ColorRGBA(this.red, this.green, this.blue, newAlpha);
    }

    public ColorRGBA mulAlpha(float percent) {
        return withAlpha((int) (alpha * percent));
    }

    public ColorRGBA mix(ColorRGBA color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new ColorRGBA((int) MathUtil.interpolate(this.getRed(), color2.getRed(), amount),
                (int) MathUtil.interpolate(this.getGreen(), color2.getGreen(), amount),
                (int) MathUtil.interpolate(this.getBlue(), color2.getBlue(), amount),
                (int) MathUtil.interpolate(this.getAlpha(), color2.getAlpha(), amount));
    }


    public float getSaturation() {
        return getHSBValues()[2];
    }

    public float getBrightness() {
        return getHSBValues()[1];
    }

    private float[] getHSBValues() {
        if (this.hsbValues == null) {
            this.hsbValues = calculateHSB();
        }
        return this.hsbValues;
    }

    private float[] calculateHSB() {
        float r = this.red / 255.0f;
        float g = this.green / 255.0f;
        float b = this.blue / 255.0f;

        float maxC = Math.max(r, Math.max(g, b));
        float minC = Math.min(r, Math.min(g, b));
        float delta = maxC - minC;

        float hue = 0f;
        if (delta != 0) {
            if (maxC == r) {
                hue = ((g - b) / delta);
            } else if (maxC == g) {
                hue = ((b - r) / delta) + 2f;
            } else { // maxC == b
                hue = ((r - g) / delta) + 4f;
            }
            hue /= 6f;
            if (hue < 0) {
                hue += 1f;
            }
        }

        float saturation = (maxC == 0) ? 0f : (delta / maxC);
        float brightness = maxC;

        return new float[]{hue, saturation, brightness};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorRGBA colorRGBA = (ColorRGBA) o;
        return Float.compare(red, colorRGBA.red) == 0 && Float.compare(green, colorRGBA.green) == 0 && Float.compare(blue, colorRGBA.blue) == 0 && Float.compare(alpha, colorRGBA.alpha) == 0;
    }


    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue, alpha);
    }

}