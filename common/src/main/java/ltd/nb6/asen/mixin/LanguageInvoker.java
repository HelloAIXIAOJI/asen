package ltd.nb6.asen.mixin;

import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Language.class)
public interface LanguageInvoker {
    @Invoker("loadDefault")
    static Language asen$invokeLoadDefault() {
        throw new AssertionError();
    }
}
