package io.github.misode.packtest.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.misode.packtest.PackTestFunction;
import io.github.misode.packtest.PackTestLibrary;
import io.github.misode.packtest.TestMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.ReportGameListener;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ReportGameListener.class)
public class ReportGameListenerMixin {
    @WrapOperation(method = "visualizePassedTest", at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/ReportGameListener;say(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/ChatFormatting;Ljava/lang/String;)V", ordinal = 0))
    private static void injectDescriptionPassed(ServerLevel level, ChatFormatting color, String text, Operation<Void> original, GameTestInfo testInfo) {
        PackTestFunction fn = PackTestLibrary.getLoaded(testInfo.id());
        if (fn == null) {
            original.call(level, color, text);
            return;
        }
        original.call(level, ChatFormatting.GREEN, TestMessages.pass(testInfo, fn));
    }
}