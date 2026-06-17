package ru.solana.utils.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import ru.solana.mixin.accessor.ShaderProgramAccessor;

import java.util.ArrayList;
import java.util.List;

public class GlProgram {

    MinecraftClient mc = MinecraftClient.getInstance();

    private static final List<Runnable> REGISTERED_PROGRAMS = new ArrayList<>();

    protected ShaderProgram backingProgram;

    protected ShaderProgramKey programKey;

    public GlProgram(Identifier id, VertexFormat vertexFormat) {
        this.programKey = new ShaderProgramKey(id.withPrefixedPath("core/"), vertexFormat, Defines.EMPTY);

        REGISTERED_PROGRAMS.add(
                () -> {
                    try {
                        this.backingProgram = mc.getShaderLoader().getProgramToLoad(programKey);
                        this.setup();
                    } catch (ShaderLoader.LoadException e) {
                      //  throw new RuntimeException("Failed to initialize shader program", e);
                    }
                }
        );
    }

    public RenderPhase renderPhaseProgram() {
        return new RenderPhase.ShaderProgram(programKey);
    }

    public ShaderProgram use() {
        return RenderSystem.setShader(programKey);
    }

    protected void setup() {}

    public GlUniform findUniform(String name) {
        return ((ShaderProgramAccessor) this.backingProgram).getUniformsByName().get(name);
    }

    @ApiStatus.Internal
    public static void loadAndSetupPrograms() {
        REGISTERED_PROGRAMS.forEach(Runnable::run);
    }
}