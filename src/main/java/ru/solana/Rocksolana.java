package ru.solana;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import ru.solana.utils.render.color.ColorRGBA;
import ru.solana.utils.render.DrawUtil;
import ru.solana.utils.render.msdf.Font;
import ru.solana.utils.render.msdf.Fonts;
import ru.solana.utils.render.shader.GlProgram;
import ru.solana.utils.render.tools.BorderRadius;

public class Rocksolana implements ModInitializer {

    public static final String NAME = "solana"; public static final String MOD_ID = NAME.toLowerCase();

    @Override
    public void onInitialize() {

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Rocksolana.id("after_shader_load");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        GlProgram.loadAndSetupPrograms();
                    }
                });

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            DrawUtil.initializeShaders();
        });

        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext context, RenderTickCounter tickCounter) {
        Font font1 = Fonts.REGULAR.getFont(10.0f);
        Font font2 = Fonts.MEDIUM.getFont(20.0f);
        Font font3 = Fonts.SEMIBOLD.getFont(15.0f);
        Font font4 = Fonts.ROUND_BOLD.getFont(25.0f);
        Font font5 = Fonts.BOLD.getFont(25.0f);

        DrawUtil.drawLiquidRect(
                context.getMatrices(),
                10, 120, 190, 120,
                new BorderRadius(12, 12, 12, 12),
                new ColorRGBA(255, 255, 255, 255),
                3.0f, 35.0f, 0.5f, 1.0f,
                true, 0f, 0.5f
        );

        DrawUtil.drawBlur(
                context.getMatrices(),
                120, 10, 190, 100, 5,
                new BorderRadius(12, 12, 12, 12),
                new ColorRGBA(255, 255, 255 , 255)
        );

        DrawUtil.drawRoundedRect(
                context.getMatrices(),
                320, 10, 190, 100,
                new BorderRadius(12, 12, 12, 12),
                new ColorRGBA(255, 255, 255 , 255)
        );

        DrawUtil.drawRoundedRect(
                context.getMatrices(),
                210, 120, 190, 120,
                new BorderRadius(12, 12, 12, 12),
                new ColorRGBA(255, 0, 0 , 255),
                new ColorRGBA(0, 255, 0 , 255),
                new ColorRGBA(0, 0, 255 , 255),
                new ColorRGBA(155, 15, 111, 255)
        );

        DrawUtil.drawRoundedBorder(
                context.getMatrices(),
                410, 120, 190, 120, 2,
                new BorderRadius(12, 12, 12, 12),
                new ColorRGBA(0, 0, 0 , 255)
        );

        DrawUtil.drawShadow(
                context.getMatrices(),
                10, 260, 190, 100, 30,
                BorderRadius.all(15),
                new ColorRGBA(255, 255, 255 , 255)
        );

        DrawUtil.drawText(font1, "Aa Bb Cc 123456", 10, 400, ColorRGBA.WHITE, context);
        DrawUtil.drawText(font2, "Aa Bb Cc 123456", 10, 410, ColorRGBA.BLACK, context);
        DrawUtil.drawText(font3, "Aa Bb Cc 123456", 10, 430, ColorRGBA.RED, context);
        DrawUtil.drawText(font4, "Aa Bb Cc 123456", 10, 450, ColorRGBA.YELLOW, context);
        DrawUtil.drawText(font5, "Aa Bb Cc 123456", 10, 470, ColorRGBA.GREEN, context);

        DrawUtil.drawTexture(
                context.getMatrices(),
                Identifier.of("solana", "textures/pic.png"),
                10, 10,
                100, 100,
                ColorRGBA.WHITE
        );
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

}
