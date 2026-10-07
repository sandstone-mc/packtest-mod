package io.github.misode.packtest;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;

public final class TestMessages {
    private TestMessages() {}

    public static String pass(GameTestInfo testInfo, PackTestFunction fn) {
        return header(testInfo, fn) + " passed at " + testInfo.getTestBlockPos().toShortString() + " on tick " + testInfo.getTick() + "!";
    }

    public static String fail(GameTestInfo testInfo, PackTestFunction fn, Throwable error) {
        return header(testInfo, fn) + " failed! " + Util.describeError(error);
    }

    public static String failAnnotated(GameTestInfo testInfo, PackTestFunction fn, Throwable error, boolean annotations) {
        String errorMessage = Util.describeError(error);
        String lineSuffix = (error instanceof PackTestException pe ? " on line " + pe.getLine() : "") + "! ";
        String text = header(testInfo, fn) + " failed" + lineSuffix + errorMessage;
        if (testInfo.isRequired()) {
            if (annotations) {
                return "::error title=Test " + text + "::" + errorMessage;
            }
            return colorize(text, "\u001b[0;31m");
        }
        return colorize(text, "\u001b[0;33m");
    }

    private static String header(GameTestInfo testInfo, PackTestFunction fn) {
        String prefix = testInfo.isRequired() ? "" : "(optional) ";
        String description = fn.description();
        String wrappedDescription = description.isEmpty() ? "" : " (" + description + ")";
        return prefix + testInfo.id() + wrappedDescription;
    }

    private static String colorize(String text, @NonNull String code) {
        return code + text + "\u001b[0m";
    }
}