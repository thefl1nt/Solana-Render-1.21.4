package ru.solana.utils.render.msdf;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import ru.solana.Rocksolana;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public final class ResourceProvider {
    private static final ResourceManager RESOURCE_MANAGER = MinecraftClient.getInstance().getResourceManager();
    private static final Gson GSON = new Gson();

    public static Identifier getShaderIdentifier(String name) {
        return Rocksolana.id("core/" + name);
    }

    public static <T> T fromJsonToInstance(Identifier identifier, Class<T> clazz) {
        return (T)GSON.fromJson(ResourceProvider.toString(identifier), clazz);
    }

    public static String toString(Identifier identifier) {
        return ResourceProvider.toString(identifier, "\n");
    }

    public static String toString(Identifier identifier, String delimiter) {
        try (InputStream inputStream = RESOURCE_MANAGER.open(identifier);){
            String string;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));){
                string = reader.lines().collect(Collectors.joining(delimiter));
            }
            return string;
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
