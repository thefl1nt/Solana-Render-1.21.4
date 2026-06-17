package ru.solana.utils.render.tools;

import lombok.Getter;
import net.minecraft.util.Identifier;
import ru.solana.Rocksolana;

@Getter
public class CustomSprite {

    private final Identifier texture;

    public CustomSprite(String path) {
        if (path.contains(":")) {
            this.texture = Identifier.of(path);
        } else if (path.contains("/")) {
            this.texture = Rocksolana.id(path);
        } else {
            this.texture = Rocksolana.id("icons/category/" + path);
        }
    }
}