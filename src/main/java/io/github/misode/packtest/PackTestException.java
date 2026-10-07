package io.github.misode.packtest;

import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PackTestException extends GameTestAssertException {
    final int line;
    final String command;
    final @Nullable String description;

    public PackTestException(Component message, int tick, int line, String command, @Nullable String description) {
        super(message, tick);
        this.line = line;
        this.command = command;
        this.description = description;
    }

    public int getLine() {
        return this.line;
    }

    @Override
    public @NonNull Component getDescription() {
        String prefix = this.command.isEmpty()
                ? "On line " + this.line + ": "
                : "\"" + this.command + "\" on line " + this.line + ": ";
        return Component.literal(prefix).append(super.getDescription());
    }
}
