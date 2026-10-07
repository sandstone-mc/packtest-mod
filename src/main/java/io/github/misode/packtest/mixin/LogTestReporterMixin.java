package io.github.misode.packtest.mixin;

import io.github.misode.packtest.PackTest;
import io.github.misode.packtest.PackTestFunction;
import io.github.misode.packtest.PackTestLibrary;
import io.github.misode.packtest.TestMessages;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.LogTestReporter;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remove coordinates and add command numbers from failing test logs when auto is enabled.
 * Apply ascii color codes to failure messages.
 */
@Mixin(LogTestReporter.class)
public class LogTestReporterMixin {
    @Shadow
    @Final
    @Mutable
    private static Logger LOGGER;

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "onTestFailed", at = @At(value = "HEAD"), cancellable = true)
    private void onTestFailed(GameTestInfo testInfo, CallbackInfo ci) {
        if (PackTest.isAutoEnabled()) {
            String text = TestMessages.failAnnotated(
                testInfo,
                PackTestLibrary.getLoaded(testInfo.id()),
                testInfo.getError(),
                PackTest.isAnnotationsEnabled()
            );
            if (testInfo.isRequired()) {
                LOGGER.error("{}", text);
            } else {
                LOGGER.warn("{}", text);
            }
            ci.cancel();
        }
    }

    @Inject(method = "onTestSuccess", at = @At(value = "HEAD"))
    private void onTestSuccess(GameTestInfo testInfo, CallbackInfo ci) {
        PackTestFunction fn = PackTestLibrary.getLoaded(testInfo.id());
        if (fn == null) return;
        LOGGER.info("{}", TestMessages.pass(testInfo, fn));
    }
}