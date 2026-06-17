package ru.solana.utils.render.shader.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;

import ru.solana.utils.render.shader.GlProgram;

public class KawaseBlurProgram extends GlProgram {
    MinecraftClient mc = MinecraftClient.getInstance();
    Window mw = mc.getWindow();
    private GlUniform resolutionUniform;
    private GlUniform offsetUniform;
    private GlUniform saturationUniform;
    private GlUniform tintIntensityUniform;
    private GlUniform tintColorUniform;

    public KawaseBlurProgram(Identifier identifier) {
        super(identifier, VertexFormats.POSITION_TEXTURE_COLOR);
    }

    public void updateUniforms(float offset) {
        offsetUniform.set(offset);
        resolutionUniform.set(1f / mw.getWidth(), 1f / mw.getHeight());
        saturationUniform.set(1.0F);
        tintIntensityUniform.set(0.0F);
        tintColorUniform.set(1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void setup() {
        this.resolutionUniform = findUniform("Resolution");
        this.offsetUniform = findUniform("Offset");
        this.saturationUniform = findUniform("Saturation");
        this.tintIntensityUniform = findUniform("TintIntensity");
        this.tintColorUniform = findUniform("TintColor");
        super.setup();
    }
}