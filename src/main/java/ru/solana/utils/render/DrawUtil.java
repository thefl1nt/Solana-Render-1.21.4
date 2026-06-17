package ru.solana.utils.render;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import org.joml.Matrix4f;
import ru.solana.Rocksolana;
import ru.solana.utils.render.color.ColorRGBA;
import ru.solana.utils.render.msdf.Font;
import ru.solana.utils.render.msdf.MsdfRenderer;
import ru.solana.utils.render.shader.BlurProgram;
import ru.solana.utils.render.shader.CustomRenderTarget;
import ru.solana.utils.render.shader.GlProgram;
import ru.solana.utils.render.tools.BorderRadius;
import ru.solana.utils.render.tools.CustomSprite;
import ru.solana.utils.render.tools.Gradient;

import java.util.function.Supplier;

@UtilityClass
public class DrawUtil {

    private static MinecraftClient mc() {
        return MinecraftClient.getInstance();
    }

    private static Window mw() {
        return mc().getWindow();
    }

    public static final float DEFAULT_SMOOTHNESS = 0.8f;

    public GlProgram rectangleProgram;
    private GlProgram squircleProgram;
    private GlProgram roundedTextureProgram;
    private GlProgram squircleTextureProgram;
    private GlProgram borderProgram;
    private GlProgram figmaBorderProgram;
    private GlProgram loadingProgram;
    private GlProgram gradientRectangleProgram;
    private GlProgram liquidGlassProgram;
    public BlurProgram blurProgram;

    private final CustomRenderTarget buffer = new CustomRenderTarget(false);
    private static final Supplier<CustomRenderTarget> BUFFER = Suppliers.memoize(() -> new CustomRenderTarget(false));

    private static final com.google.common.base.Supplier<SimpleFramebuffer> TEMP_FBO_SUPPLIER = Suppliers
            .memoize(() -> new SimpleFramebuffer(1920, 1024, false));
    private static Framebuffer MAIN_FBO = MinecraftClient.getInstance().getFramebuffer();


    public void initializeShaders() {
        rectangleProgram = new GlProgram(Rocksolana.id("rectangle/data"), VertexFormats.POSITION_COLOR);
        squircleProgram = new GlProgram(Rocksolana.id("squircle/data"), VertexFormats.POSITION_COLOR);
        squircleTextureProgram = new GlProgram(Rocksolana.id("squircle_texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        roundedTextureProgram = new GlProgram(Rocksolana.id("texture/data"), VertexFormats.POSITION_TEXTURE_COLOR);
        borderProgram = new GlProgram(Rocksolana.id("border/data"), VertexFormats.POSITION_COLOR);
        figmaBorderProgram = new GlProgram(Rocksolana.id("corner/data"), VertexFormats.POSITION_COLOR);

        liquidGlassProgram = new GlProgram(Rocksolana.id("liquid_glass/data"), VertexFormats.POSITION_TEXTURE_COLOR);

        loadingProgram = new GlProgram(Rocksolana.id("loading/data"), VertexFormats.POSITION_COLOR);
        gradientRectangleProgram = new GlProgram(Rocksolana.id("gradient_rectangle/data"), VertexFormats.POSITION_COLOR);
        blurProgram = new BlurProgram();
        blurProgram.initShaders();

        if (MAIN_FBO == null) {
            int width = mc().getWindow().getFramebufferWidth();
            int height = mc().getWindow().getFramebufferHeight();
            MAIN_FBO = new SimpleFramebuffer(width, height, false);
        }


        TEMP_FBO_SUPPLIER.get();
    }


    private void drawQuad(float x, float y, float width, float height, boolean flip) {
        final BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        final int color = -1;

        float vTop = flip ? 0f : 1f;
        float vBottom = flip ? 1f : 0f;

        builder.vertex(x, y, 0F).texture(0f, vBottom).color(color);
        builder.vertex(x, y + height, 0F).texture(0f, vTop).color(color);
        builder.vertex(x + width, y + height, 0F).texture(1f, vTop).color(color);
        builder.vertex(x + width, y, 0F).texture(1f, vBottom).color(color);

        BufferRenderer.drawWithGlobalProgram(builder.end());
    }

    public void drawLine(MatrixStack matrices, Vec2f from, Vec2f to, ColorRGBA color) {
        matrices.push();
        try {
            Matrix4f matrix4f = matrices.peek().getPositionMatrix();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            RenderSystem.lineWidth(1);

            drawSetup();

            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            builder.vertex(matrix4f, from.x, from.y, 0).color(color.getRGB());
            builder.vertex(matrix4f, to.x, to.y, 0).color(color.getRGB());
            BufferRenderer.drawWithGlobalProgram(builder.end());

            drawEnd();

        } finally {
            RenderSystem.disableBlend();
            RenderSystem.lineWidth(1.0f);
            matrices.pop();
        }
    }


    public void drawRect(MatrixStack matrices, float x, float y, float width, float height, ColorRGBA color) {
        matrices.push();

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        drawSetup();

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, x, y + height, 0).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y, 0).color(color.getRGB());
        builder.vertex(matrix4f, x, y, 0).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();
        matrices.pop();
    }

    public void drawLiquidRect(MatrixStack matrices,
                               float x, float y, float width, float height,
                               BorderRadius borderRadius, ColorRGBA color,
                               float cornerSmoothness,
                               float fresnelPower, float fresnelAlpha,
                               float baseAlpha, boolean fresnelInvert,
                               float fresnelMix, float distortStrength) {

        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        Framebuffer screenFBO = mc().getFramebuffer();
        int screenTexture = screenFBO.getColorAttachment();

        liquidGlassProgram.use();
        liquidGlassProgram.findUniform("ModelViewMat").set(matrix4f);
        liquidGlassProgram.findUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
        liquidGlassProgram.findUniform("Size").set(width, height);
        liquidGlassProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        liquidGlassProgram.findUniform("Smoothness").set(1.0f);
        liquidGlassProgram.findUniform("CornerSmoothness").set(cornerSmoothness);
        liquidGlassProgram.findUniform("GlobalAlpha").set(color.getAlpha() / 255f);
        liquidGlassProgram.findUniform("FresnelPower").set(fresnelPower);
        liquidGlassProgram.findUniform("FresnelColor").set(1f, 1f, 1f);
        liquidGlassProgram.findUniform("FresnelAlpha").set(fresnelAlpha);
        liquidGlassProgram.findUniform("BaseAlpha").set(baseAlpha);
        liquidGlassProgram.findUniform("FresnelInvert").set(fresnelInvert ? 1 : 0);
        liquidGlassProgram.findUniform("FresnelMix").set(fresnelMix);
        liquidGlassProgram.findUniform("DistortStrength").set(distortStrength);

        RenderSystem.setShaderTexture(0, screenTexture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        drawSetup();

        float scaleX = (float) screenFBO.textureWidth / mc().getWindow().getScaledWidth();
        float scaleY = (float) screenFBO.textureHeight / mc().getWindow().getScaledHeight();

        float fx = x * scaleX;
        float fy = y * scaleY;
        float fwidth = width * scaleX;
        float fheight = height * scaleY;

        fy = screenFBO.textureHeight - fy - fheight;

        float u0 = fx / screenFBO.textureWidth;
        float v0 = fy / screenFBO.textureHeight;
        float u1 = (fx + fwidth) / screenFBO.textureWidth;
        float v1 = (fy + fheight) / screenFBO.textureHeight;

        BufferBuilder builder = RenderSystem.renderThreadTesselator()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        builder.vertex(matrix4f, x, y, 0f).texture(u0, v1).color(r, g, b, a);
        builder.vertex(matrix4f, x, y + height, 0f).texture(u0, v0).color(r, g, b, a);
        builder.vertex(matrix4f, x + width, y + height, 0f).texture(u1, v0).color(r, g, b, a);
        builder.vertex(matrix4f, x + width, y, 0f).texture(u1, v1).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    public void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = DEFAULT_SMOOTHNESS;

        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        rectangleProgram.findUniform("Smoothness").set(smoothness);

        drawSetup();

        float horizontalPadding = -smoothness / 2.0F + smoothness * 2.0F;
        float verticalPadding = smoothness / 2.0F + smoothness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();
        matrices.pop();
    }

    public void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color1, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = DEFAULT_SMOOTHNESS;

        gradientRectangleProgram.use();
        gradientRectangleProgram.findUniform("Size").set(width, height);
        gradientRectangleProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        gradientRectangleProgram.findUniform("Smoothness").set(smoothness);

        gradientRectangleProgram.findUniform("TopLeftColor").set(
                color1.getRed() / 255.0f,
                color1.getGreen() / 255.0f,
                color1.getBlue() / 255.0f,
                color1.getAlpha() / 255.0f
        );
        gradientRectangleProgram.findUniform("BottomLeftColor").set(
                color2.getRed() / 255.0f,
                color2.getGreen() / 255.0f,
                color2.getBlue() / 255.0f,
                color2.getAlpha() / 255.0f
        );
        gradientRectangleProgram.findUniform("BottomRightColor").set(
                color3.getRed() / 255.0f,
                color3.getGreen() / 255.0f,
                color3.getBlue() / 255.0f,
                color3.getAlpha() / 255.0f
        );
        gradientRectangleProgram.findUniform("TopRightColor").set(
                color4.getRed() / 255.0f,
                color4.getGreen() / 255.0f,
                color4.getBlue() / 255.0f,
                color4.getAlpha() / 255.0f
        );

        drawSetup();

        float horizontalPadding = -smoothness / 2.0F + smoothness * 2.0F;
        float verticalPadding = smoothness / 2.0F + smoothness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).color(color1.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).color(color2.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).color(color3.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).color(color4.getRGB());

        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();
        matrices.pop();
    }

    public void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, BorderRadius borderRadius, Gradient gradient) {
        drawRoundedRect(matrices, x, y, width, height, borderRadius,
                gradient.getTopLeftColor(),
                gradient.getBottomLeftColor(),
                gradient.getBottomRightColor(),
                gradient.getTopRightColor());
    }

    public void drawRoundedBorder(MatrixStack matrices, float x, float y, float width, float height, float borderThickness, BorderRadius borderRadius, ColorRGBA borderColor) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float internalSmoothness = DEFAULT_SMOOTHNESS, externalSmoothness = 1.0F;

        borderProgram.use();
        borderProgram.findUniform("Size").set(width, height);
        borderProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        borderProgram.findUniform("Smoothness").set(internalSmoothness, externalSmoothness);
        borderProgram.findUniform("Thickness").set(borderThickness);

        drawSetup();

        float horizontalPadding = -externalSmoothness / 2.0F + externalSmoothness * 2.0F;
        float verticalPadding = externalSmoothness / 2.0F + externalSmoothness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).color(borderColor.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();
        matrices.pop();
    }


    public void drawRoundedCornerOnly(MatrixStack matrices, float x, float y, float width, float height, float borderThickness, BorderRadius borderRadius, ColorRGBA borderColor, float cornerIdex) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float internalSmoothness = DEFAULT_SMOOTHNESS, externalSmoothness = 1.0F;

        figmaBorderProgram.use();
        figmaBorderProgram.findUniform("Size").set(width, height);
        figmaBorderProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        figmaBorderProgram.findUniform("Smoothness").set(internalSmoothness, externalSmoothness);
        figmaBorderProgram.findUniform("Thickness").set(borderThickness);
        figmaBorderProgram.findUniform("CornerIndex").set(cornerIdex);

        drawSetup();

        float horizontalPadding = -externalSmoothness / 2.0F + externalSmoothness * 2.0F;
        float verticalPadding = externalSmoothness / 2.0F + externalSmoothness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).color(borderColor.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).color(borderColor.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();
        matrices.pop();
    }

    public void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, ColorRGBA textureColor) {
        matrices.push();

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, identifier);

        drawSetup();

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0F).texture(0.0F, 0.0F).color(textureColor.getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0F).texture(0.0F, 1.0F).color(textureColor.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0F).texture(1.0F, 1.0F).color(textureColor.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0F).texture(1.0F, 0.0F).color(textureColor.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();

        RenderSystem.setShaderTexture(0, 0);
        matrices.pop();
    }
    public void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, Gradient textureColor) {
        matrices.push();

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, identifier);

        drawSetup();

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0F).texture(0.0F, 0.0F).color(textureColor.getTopLeftColor().getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0F).texture(0.0F, 1.0F).color(textureColor.getBottomLeftColor().getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0F).texture(1.0F, 1.0F).color(textureColor.getBottomRightColor().getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0F).texture(1.0F, 0.0F).color(textureColor.getTopRightColor().getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();

        RenderSystem.setShaderTexture(0, 0);
        matrices.pop();
    }

    public void drawTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, float u1, float u2, float v1, float v2, ColorRGBA clor) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        matrices.push();
        int color = clor.getRGB();

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        float x2 = x + width;
        float y2 = y + height;

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, identifier);

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0F).texture(u1, v1).color(color);
        builder.vertex(matrix4f, x, y2, 0.0F).texture(u1, v2).color(color);
        builder.vertex(matrix4f, x2, y2, 0.0F).texture(u2, v2).color(color);
        builder.vertex(matrix4f, x2, y, 0.0F).texture(u2, v1).color(color);
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();

        RenderSystem.setShaderTexture(0, 0);
        matrices.pop();
        RenderSystem.disableBlend();
    }

    public void drawSprite(MatrixStack matrices, CustomSprite sprite, float x, float y, float width, float height, ColorRGBA color) {
        drawTexture(matrices, sprite.getTexture(), x, y, width, height, 0, 1, 0, 1, color);
    }

    public void drawRoundedTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius) {
        drawRoundedTexture(matrices, identifier, x, y, width, height, borderRadius, ColorRGBA.WHITE);
    }

    public void drawRoundedTexture(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = DEFAULT_SMOOTHNESS;

        roundedTextureProgram.use();
        RenderSystem.setShaderTexture(0, identifier);

        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);

        drawSetup();

        float horizontalPadding = -smoothness / 2.0F + smoothness * 2.0F;
        float verticalPadding = smoothness / 2.0F + smoothness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).texture(0.0F, 0.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).texture(0.0F, 1.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).texture(1.0F, 1.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).texture(1.0F, 0.0F).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());
        drawEnd();

        RenderSystem.setShaderTexture(0, 0);
        matrices.pop();
    }

    public void drawShadow(MatrixStack matrices, float x, float y, float width, float height, float softness, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        rectangleProgram.use();
        rectangleProgram.findUniform("Size").set(width, height);
        rectangleProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius() * 3,
                borderRadius.bottomLeftRadius() * 3,
                borderRadius.topRightRadius() * 3,
                borderRadius.bottomRightRadius() * 3
        );
        rectangleProgram.findUniform("Smoothness").set(softness);

        drawSetup();

        float horizontalPadding = -softness / 2.0F + softness * 2.0F;
        float verticalPadding = softness / 2.0F + softness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();
        matrices.pop();
    }

    public void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, float squirt, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = 0.03f;

        blurRadius /= 22.5f;

        if (blurRadius <= 0) return;

        blurProgram.setBlurRadius(2);
        squircleTextureProgram.use();
        RenderSystem.setShaderTexture(0, BlurProgram.getTexture());
        squircleTextureProgram.findUniform("Size").set(width, height);
        squircleTextureProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius() * squirt / 2F,
                borderRadius.bottomLeftRadius() * squirt / 2F,
                borderRadius.topRightRadius() * squirt / 2F,
                borderRadius.bottomRightRadius() * squirt / 2F
        );
        squircleTextureProgram.findUniform("Smoothness").set(0.1f);
        squircleTextureProgram.findUniform("CornerSmoothness").set(squirt);

        drawSetup();

        float horizontalPadding = -smoothness / 2.0F + smoothness * 2.0F;
        float verticalPadding = smoothness / 2.0F + smoothness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        int screenWidth = mc().getWindow().getScaledWidth();
        int screenHeight = mc().getWindow().getScaledHeight();

        float u = adjustedX / screenWidth;
        float v = (screenHeight - adjustedY - adjustedHeight) / screenHeight;
        float texWidth = adjustedWidth / screenWidth;
        float texHeight = adjustedHeight / screenHeight;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).texture(u, v + texHeight).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).texture(u, v).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).texture(u + texWidth, v).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).texture(u + texWidth, v + texHeight).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());

        drawEnd();

        RenderSystem.setShaderTexture(0, 0);
        matrices.pop();
    }

    public void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float blurRadius, BorderRadius borderRadius, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        blurRadius /= 22.5f;

        if (blurRadius <= 0) return;

        blurProgram.setBlurRadius(2);
        roundedTextureProgram.use();
        RenderSystem.setShaderTexture(0, BlurProgram.getTexture());

        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        roundedTextureProgram.findUniform("Smoothness").set(0.01f);

        drawSetup();

        int screenWidth = mc().getWindow().getScaledWidth();
        int screenHeight = mc().getWindow().getScaledHeight();

        float u = x / screenWidth;
        float v = (screenHeight - y - height) / screenHeight;
        float texWidth = width / screenWidth;
        float texHeight = height / screenHeight;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix4f, x, y, 0.0F).texture(u, v + texHeight).color(color.getRGB());
        builder.vertex(matrix4f, x, y + height, 0.0F).texture(u, v).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y + height, 0.0F).texture(u + texWidth, v).color(color.getRGB());
        builder.vertex(matrix4f, x + width, y, 0.0F).texture(u + texWidth, v + texHeight).color(color.getRGB());
        BufferRenderer.drawWithGlobalProgram(builder.end());
        drawEnd();


        RenderSystem.setShaderTexture(0, 0);
        matrices.pop();

    }

    public void drawText(Font font, String text, float x, float y, ColorRGBA color, DrawContext context) {
        MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), context.getMatrices().peek().getPositionMatrix(), x, y, 0.0f);
    }

    public void drawRoundedTextureWithUV(MatrixStack matrices, Identifier identifier, float x, float y, float width, float height, BorderRadius borderRadius, ColorRGBA color, float u1, float v1, float u2, float v2) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float smoothness = DEFAULT_SMOOTHNESS;

        roundedTextureProgram.use();
        RenderSystem.setShaderTexture(0, identifier);

        roundedTextureProgram.findUniform("Size").set(width, height);
        roundedTextureProgram.findUniform("Radius").set(
                borderRadius.topLeftRadius(),
                borderRadius.bottomLeftRadius(),
                borderRadius.topRightRadius(),
                borderRadius.bottomRightRadius()
        );
        roundedTextureProgram.findUniform("Smoothness").set(smoothness);

        drawSetup();

        float horizontalPadding = -smoothness / 2.0F + smoothness * 2.0F;
        float verticalPadding = smoothness / 2.0F + smoothness;
        float adjustedX = x - horizontalPadding / 2.0F;
        float adjustedY = y - verticalPadding / 2.0F;
        float adjustedWidth = width + horizontalPadding;
        float adjustedHeight = height + verticalPadding;

        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        builder.vertex(matrix4f, adjustedX, adjustedY, 0.0F).texture(u1, v1).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX, adjustedY + adjustedHeight, 0.0F).texture(u1, v2).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY + adjustedHeight, 0.0F).texture(u2, v2).color(color.getRGB());
        builder.vertex(matrix4f, adjustedX + adjustedWidth, adjustedY, 0.0F).texture(u2, v1).color(color.getRGB());

        BufferRenderer.drawWithGlobalProgram(builder.end());
        drawEnd();

        RenderSystem.setShaderTexture(0, 0);
        matrices.pop();
    }


    public void drawSetup() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public void drawEnd() {
        RenderSystem.disableBlend();
    }

}