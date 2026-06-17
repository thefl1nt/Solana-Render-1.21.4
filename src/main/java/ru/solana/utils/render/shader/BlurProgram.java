package ru.solana.utils.render.shader;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import ru.solana.Rocksolana;
import ru.solana.utils.math.Timer;
import ru.solana.utils.render.shader.impl.KawaseBlurProgram;

public class BlurProgram {
    MinecraftClient mc = MinecraftClient.getInstance();
    Window mw = mc.getWindow();
    public static final Supplier<CustomRenderTarget> CACHE = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());
    public static final Supplier<CustomRenderTarget> BUFFER = Suppliers.memoize(() -> new CustomRenderTarget(false).setLinear());

    private static KawaseBlurProgram kawaseDownProgram;
    private static KawaseBlurProgram kawaseUpProgram;

    private final Timer timer = new Timer();

    public void initShaders() {
        kawaseDownProgram = new KawaseBlurProgram(Rocksolana.id("kawase_down/data"));
        kawaseUpProgram = new KawaseBlurProgram(Rocksolana.id("kawase_up/data"));
    }

    public void draw() {
        if (!timer.finished(25)) return;

        CustomRenderTarget cache = CACHE.get();
        CustomRenderTarget buffer = BUFFER.get();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        kawaseDownProgram.use();

        cache.setup();
        mc.getFramebuffer().beginRead();
        RenderSystem.setShaderTexture(0, mc.getFramebuffer().getColorAttachment());
        drawQuad(0, 0, mw.getScaledWidth(), mw.getScaledHeight());
        cache.stop();

        CustomRenderTarget[] buffers = {cache, buffer};

        final int steps = 3;
        for (int i = 1; i < steps; ++i) {
            int step = i % 2;
            buffers[step].setup();
            buffers[(step + 1) % 2].beginRead();
            RenderSystem.setShaderTexture(0, buffers[(step + 1) % 2].getColorAttachment());
            drawQuad(0, 0, mw.getScaledWidth(), mw.getScaledHeight());
            buffers[(step + 1) % 2].endRead();
            buffers[step].stop();
        }

        kawaseUpProgram.use();
        for (int i = 0; i < steps; ++i) {
            int step = i % 2;
            buffers[(step + 1) % 2].setup();
            buffers[step].beginRead();
            RenderSystem.setShaderTexture(0, buffers[step].getColorAttachment());
            drawQuad(0, 0, mw.getScaledWidth(), mw.getScaledHeight());
            buffers[step].endRead();
            buffers[step].stop();
        }

        mc.getFramebuffer().endRead();

        mc.getFramebuffer().beginWrite(false);
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();

        timer.reset();
    }

    private void drawQuad(float x, float y, float width, float height) {
        int color = -1;
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(x, y, 0F).texture(0, 1).color(color);
        builder.vertex(x, y + height, 0F).texture(0, 0).color(color);
        builder.vertex(x + width, y + height, 0F).texture(1, 0).color(color);
        builder.vertex(x + width, y, 0F).texture(1, 1).color(color);

        BufferRenderer.drawWithGlobalProgram(builder.end());
    }

    public static int getTexture() {
        return BUFFER.get().getColorAttachment();
    }

    public void setBlurRadius(float blurRadius) {
        kawaseDownProgram.updateUniforms(blurRadius);
        kawaseUpProgram.updateUniforms(blurRadius);
    }

}
