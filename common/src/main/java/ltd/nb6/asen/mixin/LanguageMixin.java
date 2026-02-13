package ltd.nb6.asen.mixin;

import ltd.nb6.asen.DualLanguageWrapper;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Language.class)
public abstract class LanguageMixin {
    @Inject(method = "inject", at = @At("HEAD"), cancellable = true)
    private static void asen$onSetInstance(Language language, CallbackInfo ci) {
        if (!(language instanceof DualLanguageWrapper)) {
            Language.inject(new DualLanguageWrapper(language));
            ci.cancel();
        }
    }
}
