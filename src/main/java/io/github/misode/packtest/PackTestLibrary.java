package io.github.misode.packtest;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;

public class PackTestLibrary {
    private static final FileToIdConverter LISTER = new FileToIdConverter("test", ".mcfunction");

    private static final Map<Identifier, PackTestFunction> LOADED = new HashMap<>();

    private final PackTestRegistries registries;

    public PackTestLibrary(HolderLookup.Provider registries) {
        this.registries = new PackTestRegistries(registries);
    }

    public static @Nullable PackTestFunction getLoaded(Identifier id) {
        return LOADED.get(id);
    }

    public void load(MinecraftServer server) {
        ResourceManager manager = server.getResourceManager();
        RegistryAccess.Frozen registryLookup = this.registries
                .load(manager, ForkJoinPool.commonPool())
                .join();
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack source = server.createCommandSourceStack();

        Map<Identifier, Resource> resources = LISTER.listMatchingResources(manager);
        Map<Identifier, PackTestFunction> loaded = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = LISTER.fileToId(entry.getKey());
            try {
                List<String> lines = readLines(entry.getValue());
                PackTestFunction fn = PackTestFunction.fromLines(dispatcher, source, lines);
                loaded.put(id, fn);
            } catch (Exception e) {
                PackTest.LOGGER.error("Failed to load test {}", id, e);
            }
        }

        LOADED.clear();
        LOADED.putAll(loaded);
        this.registries.register(registryLookup, loaded);
        PackTest.LOGGER.info("Loaded {} test functions", loaded.size());
    }

    private static List<String> readLines(Resource resource) {
        try (BufferedReader lvt1 = resource.openAsReader()) {
            return lvt1.lines().toList();
        } catch (IOException var6) {
            throw new CompletionException(var6);
        }
    }
}
