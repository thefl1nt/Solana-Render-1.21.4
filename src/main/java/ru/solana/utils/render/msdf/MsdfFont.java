package ru.solana.utils.render.msdf;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import ru.solana.Rocksolana;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class MsdfFont {
    private final String name;
    private final AbstractTexture texture;
    private final FontData.AtlasData atlas;
    private final FontData.MetricsData metrics;
    private final Map<Integer, MsdfGlyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;
    private final ConcurrentHashMap<Long, Float> widthCache = new ConcurrentHashMap();

    private MsdfFont(String name, AbstractTexture texture, FontData.AtlasData atlas, FontData.MetricsData metrics, Map<Integer, MsdfGlyph> glyphs, Map<Integer, Map<Integer, Float>> kernings) {
        this.name = name;
        this.texture = texture;
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
        this.kernings = kernings;
    }

    public int getTextureId() {
        return this.texture.getGlId();
    }

    public void applyGlyphs(Matrix4f matrix, VertexConsumer consumer, String text, float size, float thickness, float spacing, float x, float y, float z, int color) {
        int prevChar = -1;
        boolean skipNext = false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == '\u00a7') {
                skipNext = true;
                continue;
            }
            MsdfGlyph glyph = this.glyphs.get((int)c);
            if (glyph == null) continue;
            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                x += kerning.getOrDefault((int)c, Float.valueOf(0.0f)).floatValue() * size;
            }
            x += glyph.apply(matrix, consumer, size, x, y, z, color) + thickness + spacing;
            prevChar = c;
        }
    }

    public float getWidthOld(String text, float size) {
        text = text.replace("\u0456", "i").replace("\u0406", "I");
        int prevChar = -1;
        float width = 0.0f;
        boolean skipNext = false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (skipNext) {
                skipNext = false;
                continue;
            }
            if (c == '\u00a7') {
                skipNext = true;
                continue;
            }
            MsdfGlyph glyph = this.glyphs.get((int)c);
            if (glyph == null) continue;
            Map<Integer, Float> kerning = this.kernings.get(prevChar);
            if (kerning != null) {
                width += kerning.getOrDefault((int)c, Float.valueOf(0.0f)).floatValue() * size;
            }
            width += glyph.getWidth(size) + 0.25f;
            prevChar = c;
        }
        return width;
    }

    private static long widthKey(String s, float size) {
        int h = s.hashCode();
        return (long)h & 0xFFFFFFFFL ^ (long)Float.floatToIntBits(size) << 32;
    }

    public float getWidth(String text, float size) {
        long key;
        Float cached;
        text = text.replace("\u0456", "i").replace("\u0406", "I");
        if ((cached = this.widthCache.get(key = MsdfFont.widthKey(text, size))) != null) {
            return cached.floatValue();
        }
        float w = this.getWidthOld(text, size);
        this.widthCache.put(key, Float.valueOf(w));
        return w;
    }

    public void clearWidthCache() {
        this.widthCache.clear();
    }

    public float getTextWidth(Text text, float size) {
        return this.getWidth(text.getString(), size);
    }

    public Font getFont(float size) {
        return new Font(this, size);
    }

    public String getName() {
        return this.name;
    }

    public FontData.AtlasData getAtlas() {
        return this.atlas;
    }

    public FontData.MetricsData getMetrics() {
        return this.metrics;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name = "?";
        private Identifier dataIdentifer;
        private Identifier atlasIdentifier;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder data(String dataFileName) {
            this.dataIdentifer = Identifier.of((String) Rocksolana.MOD_ID, (String)("fonts/msdf/" + dataFileName + ".json"));
            return this;
        }

        public Builder atlas(String atlasFileName) {
            this.atlasIdentifier = Identifier.of((String) Rocksolana.MOD_ID, (String)("fonts/msdf/" + atlasFileName + ".png"));
            return this;
        }

        public MsdfFont build() {
            FontData data = ResourceProvider.fromJsonToInstance(this.dataIdentifer, FontData.class);
            AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(this.atlasIdentifier);
            if (data == null) {
                throw new RuntimeException("Failed to read font data file: " + this.dataIdentifer.toString() + "; Are you sure this is json file? Try to check the correctness of its syntax.");
            }
            RenderSystem.recordRenderCall(() -> texture.setFilter(true, false));
            float aWidth = data.atlas().width();
            float aHeight = data.atlas().height();
            Map<Integer, MsdfGlyph> glyphs = data.glyphs().stream().collect(Collectors.toMap(glyphData -> glyphData.unicode(), glyphData -> new MsdfGlyph((FontData.GlyphData)glyphData, aWidth, aHeight)));
            HashMap<Integer, Map<Integer, Float>> kernings = new HashMap<Integer, Map<Integer, Float>>();
            data.kernings().forEach(kerning -> {
                HashMap<Integer, Float> map = (HashMap<Integer, Float>)kernings.get(kerning.leftChar());
                if (map == null) {
                    map = new HashMap<Integer, Float>();
                    kernings.put(kerning.leftChar(), map);
                }
                map.put(kerning.rightChar(), Float.valueOf(kerning.advance()));
            });
            return new MsdfFont(this.name, texture, data.atlas(), data.metrics(), glyphs, kernings);
        }
    }
}
