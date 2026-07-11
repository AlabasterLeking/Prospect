package alabaster.prospect.common.prospecting;

import alabaster.prospect.Prospect;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class ProspectingGems {
    private ProspectingGems() {}

    private static final Map<ResourceLocation, ProspectingGemEffect> REGISTRY = new LinkedHashMap<>();

    public static void register(ResourceLocation id, ProspectingGemEffect effect) {
        if (REGISTRY.putIfAbsent(id, effect) != null) {
            throw new IllegalStateException("Duplicate prospecting gem id: " + id);
        }
    }

    public static Optional<ProspectingGemEffect> get(ResourceLocation id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    public static Map<ResourceLocation, ProspectingGemEffect> all() {
        return REGISTRY;
    }

    public static final ResourceLocation QUARTZ = id("quartz");
    public static final ResourceLocation DIAMOND = id("diamond");
    public static final ResourceLocation EMERALD = id("emerald");
    public static final ResourceLocation LAPIS = id("lapis");
    public static final ResourceLocation AMETHYST = id("amethyst");
    public static final ResourceLocation TOPAZ = id("topaz");
    public static final ResourceLocation RUBY = id("ruby");
    public static final ResourceLocation SAPPHIRE = id("sapphire");

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Prospect.MODID, path);
    }

    public static void bootstrap() {
        register(QUARTZ, new QuartzGemEffect());
        register(DIAMOND, new DiamondGemEffect());
        register(EMERALD, new EmeraldGemEffect());
        register(LAPIS, new LapisGemEffect());
        register(AMETHYST, new AmethystGemEffect());
        register(TOPAZ, new TopazGemEffect());
        register(RUBY, new RubyGemEffect());
        register(SAPPHIRE, new SapphireGemEffect());
    }
}