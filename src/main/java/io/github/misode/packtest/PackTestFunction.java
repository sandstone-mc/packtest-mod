package io.github.misode.packtest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record PackTestFunction(List<Step> steps, PackTestDirectives directives, @Nullable String description) {
    public void run(GameTestHelper helper, Identifier id) {
        PackTestExecutor executor = new PackTestExecutor(helper, this.directives.maxTicks(), id, this.description);
        executor.run(this);
    }

    public static PackTestFunction fromLines(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandSourceStack context,
            List<String> lines) throws IllegalArgumentException {
        PackTestDirectives directives = new PackTestDirectives();
        List<Step> steps = new ArrayList<>();
        String description = null;
        int i = 0;

        while (i < lines.size()) {
            final int line = i + 1;
            StringBuilder builder = new StringBuilder(lines.get(i++).trim());

            while (!builder.isEmpty() && builder.charAt(builder.length() - 1) == '\\') {
                if (i >= lines.size()) {
                    throw new IllegalArgumentException("Line continuation at end of file");
                }

                builder.deleteCharAt(builder.length() - 1);
                builder.append(lines.get(i++).trim());
                CommandFunction.checkCommandLineLength(builder);
            }

            String command = builder.toString();
            if (command.isEmpty()) continue;

            CommandFunction.checkCommandLineLength(command);
            StringReader reader = new StringReader(command);
            if (!reader.canRead()) continue;

            if (reader.peek() == '#') {
                if (command.length() > 1 && command.charAt(1) == '>') {
                    description = command.substring(2).trim();
                } else {
                    parseDirective(reader, directives);
                }
                continue;
            }

            try {
                steps.add(parseCommand(dispatcher, context, command, line));
            } catch (CommandSyntaxException e) {
                throw new IllegalArgumentException("Whilst parsing command on line " + line + ": " + e.getMessage());
            }
        }

        return new PackTestFunction(steps, directives, description);
    }

    private static void parseDirective(
            StringReader reader,
            PackTestDirectives directives) throws IllegalArgumentException {
        reader.skip();
        reader.skipWhitespace();

        if (reader.canRead() && reader.peek() == '@') {
            reader.skip();
            String name = reader.readUnquotedString();
            reader.skipWhitespace();
            String value = reader.canRead() ? reader.getRemaining() : null;
            directives.add(name, value);
        }
    }

    private static Step parseCommand(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandSourceStack context,
            String command,
            int line) throws CommandSyntaxException {
        ParseResults<CommandSourceStack> parseResults = dispatcher.parse(command, context);
        Commands.validateParseResults(parseResults);
        ContextChain<CommandSourceStack> chain = ContextChain.tryFlatten(parseResults.getContext().build(command))
                .orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS
                        .dispatcherUnknownCommand()
                        .createWithContext(parseResults.getReader()));
        String commandName = chain.getTopContext().getLastChild().getNodes().stream()
                .map(n -> n.getNode().getName())
                .filter(n -> !n.isEmpty())
                .findFirst()
                .orElse("");
        return new Step(command, chain, line, commandName);
    }

    public record Step(String command, ContextChain<CommandSourceStack> chain, int line, String commandName) {}
}
