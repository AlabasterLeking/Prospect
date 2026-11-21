package alabaster.prospect.client.event;

import alabaster.prospect.Prospect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Prospect.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BuiltinPackEvents {

    private static final ResourceLocation ANIMATED_FURNACE = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "resourcepacks/animated_furnace");
    private static final Path ENABLED_FILE = FMLPaths.CONFIGDIR.get().resolve("prospect-builtin-packs.txt");

    private static boolean saveOptions;

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        event.addPackFinders(ANIMATED_FURNACE, PackType.CLIENT_RESOURCES, Component.translatable("pack.prospect.animated_furnace"), PackSource.BUILT_IN, false, Pack.Position.TOP);
        enableOnce("mod/" + ANIMATED_FURNACE);
    }

    private static void enableOnce(String id) {
        Set<String> enabled = readEnabled();
        if (!enabled.add(id)) return;
        Options options = Minecraft.getInstance().options;
        List<String> packs = options.resourcePacks;
        if (!packs.contains(id)) {
            int index = packs.size();
            for (int i = 0; i < packs.size(); i++) {
                if (packs.get(i).startsWith("file/")) {
                    index = i;
                    break;
                }
            }
            packs.add(index, id);
            if (!saveOptions) {
                saveOptions = true;
                NeoForge.EVENT_BUS.addListener(BuiltinPackEvents::onClientTick);
            }
        }
        writeEnabled(enabled);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (!saveOptions) return;
        saveOptions = false;
        Minecraft.getInstance().options.save();
    }

    private static Set<String> readEnabled() {
        Set<String> enabled = new LinkedHashSet<>();
        if (Files.exists(ENABLED_FILE)) {
            try {
                for (String line : Files.readAllLines(ENABLED_FILE)) {
                    if (!line.isBlank()) enabled.add(line.trim());
                }
            } catch (IOException e) {
                Prospect.LOGGER.warn("Could not read {}", ENABLED_FILE, e);
            }
        }
        return enabled;
    }

    private static void writeEnabled(Set<String> enabled) {
        try {
            Files.write(ENABLED_FILE, enabled);
        } catch (IOException e) {
            Prospect.LOGGER.warn("Could not write {}", ENABLED_FILE, e);
        }
    }
}
