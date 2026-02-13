package ltd.nb6.asen;

import ltd.nb6.asen.mixin.LanguageInvoker;
import net.minecraft.locale.Language;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DualLanguageWrapper extends Language {
    private final Language original;
    private static Language english;
    private static final ThreadLocal<Boolean> processing = ThreadLocal.withInitial(() -> false);
    
    // Regex for Java Formatter placeholders: %[argument_index$][flags][width][.precision]conversion
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    public DualLanguageWrapper(Language original) {
        this.original = original;
    }

    @Override
    public @NotNull String getOrDefault(@NotNull String key) {
        return getOrDefault(key, key);
    }

    @Override
    public @NotNull String getOrDefault(@NotNull String key, @NotNull String defaultValue) {
        String localized = original.getOrDefault(key, defaultValue);

        if (processing.get()) {
            return localized;
        }

        if (english == null) {
            processing.set(true);
            try {
                english = LanguageInvoker.asen$invokeLoadDefault();
            } finally {
                processing.set(false);
            }
        }

        if (original == english || english == null) {
            return localized;
        }

        processing.set(true);
        try {
            String en = english.getOrDefault(key, key);
            if (!localized.equals(en) && !key.equals(en)) {
                return mergeWithPlaceholders(localized, en);
            }
        } finally {
            processing.set(false);
        }

        return localized;
    }

    /**
     * Merges two format strings, ensuring placeholders refer to the same arguments.
     * Converts relative placeholders (like %s) to indexed ones (like %1$s).
     */
    private String mergeWithPlaceholders(String localized, String english) {
        boolean hasPlaceholders = localized.indexOf('%') != -1 || english.indexOf('%') != -1;
        if (!hasPlaceholders) {
            return localized + " | " + english;
        }

        String fixedLocalized = convertToIndexed(localized);
        String fixedEnglish = convertToIndexed(english);

        return fixedLocalized + " | " + fixedEnglish;
    }

    private String convertToIndexed(String input) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        int lastPos = 0;
        int argIndex = 1;

        while (matcher.find()) {
            sb.append(input, lastPos, matcher.start());
            String indexGroup = matcher.group(1);
            String conversion = matcher.group(6);

            if (conversion.equals("%")) {
                // Literal %
                sb.append("%%");
            } else if (indexGroup != null) {
                // Already indexed
                sb.append(matcher.group());
            } else {
                String flags = matcher.group(2);
                boolean isRelative = flags != null && flags.indexOf('<') != -1;

                if (isRelative) {
                    // Relative index <, use previous index (don't increment argIndex)
                    sb.append(matcher.group());
                } else {
                    // Not indexed, convert to %n$
                    sb.append("%").append(argIndex).append("$");
                    // Append remaining groups (flags, width, precision, etc.)
                    for (int i = 2; i <= 6; i++) {
                        String group = matcher.group(i);
                        if (group != null) {
                            sb.append(group);
                        }
                    }
                    argIndex++;
                }
            }
            lastPos = matcher.end();
        }
        sb.append(input.substring(lastPos));
        return sb.toString();
    }

    @Override
    public boolean has(@NotNull String key) {
        return original.has(key);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return original.isDefaultRightToLeft();
    }

    @Override
    public @NotNull FormattedCharSequence getVisualOrder(@NotNull FormattedText text) {
        return original.getVisualOrder(text);
    }

    public Language getOriginal() {
        return original;
    }
}
