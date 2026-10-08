package io.github.misode.packtest.mixin;

import io.github.misode.packtest.PackTestExecutor;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.ReportGameListener;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ReportGameListener.class)
public class ReportGameListenerMixin {
    /**
     * @author packtest
     * @reason include description in chat message
     */
    @Overwrite
    protected static void visualizeFailedTest(GameTestInfo testInfo, Throwable error) {
        PackTestExecutor executor = PackTestExecutor.currentOrNull();
        String name = executor == null ? testInfo.id().toString() : executor.nameAndDescription();
        String errorMessage = error.getMessage() + (error.getCause() == null ? "" : " cause: " + Util.describeError(error.getCause()));
        String failureMessage = (testInfo.isRequired() ? "" : "(optional) ") + name + " failed! " + errorMessage;
        say(testInfo.getLevel(), testInfo.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, failureMessage);
        Throwable rootCause = com.google.common.base.MoreObjects.firstNonNull(org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(error), error);
        if (rootCause instanceof net.minecraft.gametest.framework.GameTestAssertPosException assertError) {
            testInfo.getTestInstanceBlockEntity().markError(assertError.getAbsolutePos(), assertError.getMessageToShowAtBlock());
        }
        net.minecraft.gametest.framework.GlobalTestReporter.onTestFailed(testInfo);
    }

    /**
     * @author packtest
     * @reason include description in chat message
     */
    @Overwrite
    private static void visualizePassedTest(GameTestInfo testInfo, String text) {
        PackTestExecutor executor = PackTestExecutor.currentOrNull();
        String name = executor == null ? testInfo.id().toString() : executor.nameAndDescription();
        say(testInfo.getLevel(), ChatFormatting.GREEN, name + " " + text);
        net.minecraft.gametest.framework.GlobalTestReporter.onTestSuccess(testInfo);
    }

    @Shadow
    protected static void say(net.minecraft.server.level.ServerLevel level, ChatFormatting format, String text) {}
}