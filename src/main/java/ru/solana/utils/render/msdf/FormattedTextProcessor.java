package ru.solana.utils.render.msdf;

import lombok.Generated;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FormattedTextProcessor {
    public static List<TextSegment> processText(Text text, int defaultColor) {
        ArrayList<TextSegment> segments = new ArrayList<TextSegment>();
        text.visit((style, string) -> {
            if (!string.isEmpty()) {
                int color = FormattedTextProcessor.extractColor(style, defaultColor);
                boolean bold = style.isBold();
                boolean italic = style.isItalic();
                boolean underlined = style.isUnderlined();
                boolean strikethrough = style.isStrikethrough();
                segments.add(new TextSegment(string, color, bold, italic, underlined, strikethrough));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return segments;
    }

    private static int extractColor(Style style, int defaultColor) {
        TextColor textColor = style.getColor();
        if (textColor != null) {
            return textColor.getRgb() | 0xFF000000;
        }
        return defaultColor;
    }

    public static class TextSegment {
        public final String text;
        public final int color;
        public final boolean bold;
        public final boolean italic;
        public final boolean underlined;
        public final boolean strikethrough;

        @Generated
        public TextSegment(String text, int color, boolean bold, boolean italic, boolean underlined, boolean strikethrough) {
            this.text = text;
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.underlined = underlined;
            this.strikethrough = strikethrough;
        }

        @Generated
        public String getText() {
            return this.text;
        }

        @Generated
        public int getColor() {
            return this.color;
        }

        @Generated
        public boolean isBold() {
            return this.bold;
        }

        @Generated
        public boolean isItalic() {
            return this.italic;
        }

        @Generated
        public boolean isUnderlined() {
            return this.underlined;
        }

        @Generated
        public boolean isStrikethrough() {
            return this.strikethrough;
        }

        @Generated
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof TextSegment)) {
                return false;
            }
            TextSegment other = (TextSegment)o;
            if (!other.canEqual(this)) {
                return false;
            }
            if (this.getColor() != other.getColor()) {
                return false;
            }
            if (this.isBold() != other.isBold()) {
                return false;
            }
            if (this.isItalic() != other.isItalic()) {
                return false;
            }
            if (this.isUnderlined() != other.isUnderlined()) {
                return false;
            }
            if (this.isStrikethrough() != other.isStrikethrough()) {
                return false;
            }
            String this$text = this.getText();
            String other$text = other.getText();
            return !(this$text == null ? other$text != null : !this$text.equals(other$text));
        }

        @Generated
        protected boolean canEqual(Object other) {
            return other instanceof TextSegment;
        }

        @Generated
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            result = result * 59 + this.getColor();
            result = result * 59 + (this.isBold() ? 79 : 97);
            result = result * 59 + (this.isItalic() ? 79 : 97);
            result = result * 59 + (this.isUnderlined() ? 79 : 97);
            result = result * 59 + (this.isStrikethrough() ? 79 : 97);
            String $text = this.getText();
            result = result * 59 + ($text == null ? 43 : $text.hashCode());
            return result;
        }

        @Generated
        public String toString() {
            return "FormattedTextProcessor.TextSegment(text=" + this.getText() + ", color=" + this.getColor() + ", bold=" + this.isBold() + ", italic=" + this.isItalic() + ", underlined=" + this.isUnderlined() + ", strikethrough=" + this.isStrikethrough() + ")";
        }
    }
}

