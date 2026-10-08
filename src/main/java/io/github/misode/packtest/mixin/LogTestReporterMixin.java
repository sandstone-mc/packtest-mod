package io.github.misode.packtest.mixin;

import io.github.misode.packtest.PackTestException;
import io.github.misode.packtest.PackTestExecutor;
import io.github.misode.packtest.PackTest;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
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
        PackTestExecutor executor = PackTestExecutor.currentOrNull();
        if (executor != null) {
            String testName = executor.nameAndDescription();
            String lineNumber = testInfo.getError() instanceof PackTestException err
                    ? err.getLine()
                    : "";
            String message = Util.describeError(testInfo.getError());
            if (testInfo.isRequired()) {
                if (PackTest.isAnnotationsEnabled()) {
                    LOGGER.error(PackTest.wrapError("{} failed{}!") + "\n::error title=Test {} failed{}!::{}", testName, lineNumber, testName, lineNumber, message);
                } else {
                    LOGGER.error(PackTest.wrapError("{} failed{}! {}"), testName, lineNumber, message);
                }
            } else {
                LOGGER.warn(PackTest.wrapWarning("(optional) {} failed{}! {}"), testName, lineNumber, message);
            }
            PackTestExecutor.clear();
            ci.cancel();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "onTestSuccess", at = @At(value = "HEAD"), cancellable = true)
    private void onTestSuccess(GameTestInfo testInfo, CallbackInfo ci) {
        PackTestExecutor executor = PackTestExecutor.currentOrNull();
        if (executor != null) {
            String testName = executor.nameAndDescription();
            BlockPos blockPos = testInfo.getTestBlockPos();
            if (blockPos == null) {
                LOGGER.info("{} passed on tick {}!" , testName, testInfo.getTick());
            } else {
                LOGGER.info("{} passed at {} on tick {}!" , testName, blockPos.toShortString(), testInfo.getTick());
            }
            PackTestExecutor.clear();
        }
    }
}
