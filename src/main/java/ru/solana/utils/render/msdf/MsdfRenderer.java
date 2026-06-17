package ru.solana.utils.render.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.joml.Matrix4f;
import ru.solana.utils.render.Batching;

import java.util.List;

public final class MsdfRenderer {
    public static final ShaderProgramKey MSDF_FONT_SHADER_KEY = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("msdf_font/data"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);

    public static void renderText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z) {
        MsdfRenderer.renderText(font, text, size, color, matrix, x, y, z, false, 0.0f, 1.0f, 0.0f);
    }

    public static void renderText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd, float maxWidth) {
        text = text.replace("\u0456", "i").replace("\u0406", "I");
        float thickness = 0.05f;
        float smoothness = 0.5f;
        float spacing = 0.0f;
        if (Batching.getActive() != null) {
            font.applyGlyphs(matrix, (VertexConsumer) Batching.getActive().getBuilder(), text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture((int)0, (int)font.getTextureId());
        ShaderProgram shader = RenderSystem.setShader((ShaderProgramKey)MSDF_FONT_SHADER_KEY);
        shader.getUniform("Range").set(font.getAtlas().range());
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(smoothness);
        shader.getUniform("EnableFadeout").set(enableFadeout ? 1 : 0);
        shader.getUniform("FadeoutStart").set(fadeoutStart);
        shader.getUniform("FadeoutEnd").set(fadeoutEnd);
        shader.getUniform("MaxWidth").set(maxWidth);
        shader.getUniform("TextPosX").set(x);
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        font.applyGlyphs(matrix, (VertexConsumer)builder, text, size, thickness * 0.5f * size, spacing, x - 0.75f, y + size * 0.7f, z, color);
        BuiltBuffer builtBuffer = builder.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void renderText(MsdfFont font, String text, float size, int color, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd) {
        float maxWidth = font.getWidth(text, size) * 2.0f;
        MsdfRenderer.renderText(font, text, size, color, matrix, x, y, z, enableFadeout, fadeoutStart, fadeoutEnd, maxWidth);
    }

    public static void renderText(MsdfFont font, Text text, float size, Matrix4f matrix, float x, float y, float z) {
        MsdfRenderer.renderText(font, text, size, matrix, x, y, z, false, 0.0f, 1.0f, 0.0f);
    }

    public static void renderText(MsdfFont font, Text text, float size, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd, float maxWidth) {
        float thickness = 0.05f;
        float smoothness = 0.5f;
        float spacing = 0.0f;
        List<FormattedTextProcessor.TextSegment> segments = FormattedTextProcessor.processText(text, Colors.WHITE);
        float currentX = x;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture((int)0, (int)font.getTextureId());
        ShaderProgram shader = RenderSystem.setShader((ShaderProgramKey)MSDF_FONT_SHADER_KEY);
        shader.getUniform("Range").set(font.getAtlas().range());
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(smoothness);
        shader.getUniform("EnableFadeout").set(enableFadeout ? 1 : 0);
        shader.getUniform("FadeoutStart").set(fadeoutStart);
        shader.getUniform("FadeoutEnd").set(fadeoutEnd);
        shader.getUniform("MaxWidth").set(maxWidth);
        shader.getUniform("TextPosX").set(x);
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (FormattedTextProcessor.TextSegment segment : segments) {
            font.applyGlyphs(matrix, (VertexConsumer)builder, segment.text, size, thickness * 0.5f * size, spacing - 0.3f, currentX - 0.75f, y + size * 0.7f, z, segment.color);
            currentX += font.getWidth(segment.text, size);
        }
        BuiltBuffer builtBuffer = builder.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builtBuffer);
        }
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void renderText(MsdfFont font, Text text, float size, Matrix4f matrix, float x, float y, float z, boolean enableFadeout, float fadeoutStart, float fadeoutEnd) {
        float maxWidth = font.getTextWidth(text, size) * 2.0f;
        MsdfRenderer.renderText(font, text, size, matrix, x, y, z, enableFadeout, fadeoutStart, fadeoutEnd, maxWidth);
    }

    @Generated
    private MsdfRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

