package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectBiomes;
import alabaster.prospect.common.tag.ProspectModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ProspectBiomeTags extends BiomeTagsProvider {

    private static final String BOP = "biomesoplenty";
    private static final String TERRALITH = "terralith";

    public static final TagKey<Biome> HAS_PROSPECTOR_CAMP = tag("has_structure/prospector_camp");
    public static final TagKey<Biome> HAS_PROSPECTOR_CAMP_BADLANDS = tag("has_structure/prospector_camp_badlands");
    public static final TagKey<Biome> HAS_PROSPECTOR_CAMP_SAVANNA = tag("has_structure/prospector_camp_savanna");
    public static final TagKey<Biome> HAS_PROSPECTOR_CAMP_TAIGA = tag("has_structure/prospector_camp_taiga");
    public static final TagKey<Biome> HAS_PROSPECTOR_CAMP_SNOWY = tag("has_structure/prospector_camp_snowy");
    public static final TagKey<Biome> HAS_ABANDONED_MINE = tag("has_structure/abandoned_mine");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_BADLANDS = tag("has_structure/abandoned_mine_badlands");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_JUNGLE = tag("has_structure/abandoned_mine_jungle");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_SNOWY = tag("has_structure/abandoned_mine_snowy");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_DESERT = tag("has_structure/abandoned_mine_desert");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_FUNGAL = tag("has_structure/abandoned_mine_fungal");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_SAVANNA = tag("has_structure/abandoned_mine_savanna");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_QUARRY = tag("has_structure/abandoned_mine_quarry");
    public static final TagKey<Biome> HAS_ABANDONED_MINE_MANGROVE = tag("has_structure/abandoned_mine_mangrove");

    public ProspectBiomeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper helper) {
        super(output, lookupProvider, Prospect.MODID, helper);
    }

    private static void optional(TagAppender<Biome> appender, String namespace, String... paths) {
        for (String path : paths) {
            appender.addOptional(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }

    private static TagKey<Biome> tag(String path) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Prospect.MODID, path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(HAS_PROSPECTOR_CAMP)
                .addTag(BiomeTags.IS_HILL)
                .add(Biomes.MEADOW, Biomes.PLAINS);
        tag(HAS_PROSPECTOR_CAMP_BADLANDS).addTag(BiomeTags.IS_BADLANDS);
        tag(HAS_PROSPECTOR_CAMP_SAVANNA).addTag(BiomeTags.IS_SAVANNA);
        tag(HAS_PROSPECTOR_CAMP_TAIGA).add(Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        tag(HAS_PROSPECTOR_CAMP_SNOWY).add(Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA);

        tag(HAS_ABANDONED_MINE)
                .add(Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.MEADOW, Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST,
                        Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.CHERRY_GROVE);
        tag(HAS_ABANDONED_MINE_BADLANDS).addTag(BiomeTags.IS_BADLANDS);
        tag(HAS_ABANDONED_MINE_JUNGLE).addTag(BiomeTags.IS_JUNGLE);
        tag(HAS_ABANDONED_MINE_SNOWY).add(Biomes.SNOWY_SLOPES, Biomes.FROZEN_PEAKS, Biomes.JAGGED_PEAKS, Biomes.GROVE);
        tag(HAS_ABANDONED_MINE_DESERT).add(Biomes.DESERT).addTag(Tags.Biomes.IS_DESERT);
        tag(HAS_ABANDONED_MINE_FUNGAL).add(Biomes.DARK_FOREST);
        tag(HAS_ABANDONED_MINE_SAVANNA).addTag(BiomeTags.IS_SAVANNA);
        tag(HAS_ABANDONED_MINE_QUARRY).add(Biomes.STONY_PEAKS, Biomes.WINDSWEPT_GRAVELLY_HILLS);
        tag(HAS_ABANDONED_MINE_MANGROVE).add(Biomes.MANGROVE_SWAMP, Biomes.SWAMP).addTag(Tags.Biomes.IS_SWAMP);


        optional(tag(HAS_PROSPECTOR_CAMP), BOP, "grassland", "pasture", "prairie", "field", "shrubland", "lavender_field", "overgrown_greens");
        optional(tag(HAS_PROSPECTOR_CAMP), TERRALITH, "blooming_plateau", "blooming_valley", "brushland", "lush_valley", "shield", "steppe", "valley_clearing", "lavender_valley");
        optional(tag(HAS_PROSPECTOR_CAMP_TAIGA), BOP, "coniferous_forest", "fir_clearing");
        optional(tag(HAS_PROSPECTOR_CAMP_TAIGA), TERRALITH, "siberian_taiga", "birch_taiga", "alpine_grove");
        optional(tag(HAS_PROSPECTOR_CAMP_SNOWY), BOP, "snowy_coniferous_forest", "snowy_fir_clearing", "snowy_maple_woods");
        optional(tag(HAS_PROSPECTOR_CAMP_SNOWY), TERRALITH, "wintry_forest", "snowy_maple_forest");
        optional(tag(HAS_ABANDONED_MINE), BOP, "maple_woods", "seasonal_forest", "woodland", "old_growth_woodland", "redwood_forest", "aspen_glade", "jacaranda_glade", "mediterranean_forest", "coniferous_forest", "fir_clearing", "dead_forest", "old_growth_dead_forest");
        optional(tag(HAS_ABANDONED_MINE), TERRALITH, "alpine_grove", "birch_taiga", "cloud_forest", "lavender_forest", "sakura_grove", "siberian_grove", "siberian_taiga", "forested_highlands", "alpine_highlands", "haze_mountain", "rocky_mountains");
        optional(tag(HAS_ABANDONED_MINE_BADLANDS), TERRALITH, "bryce_canyon", "white_mesa");
        optional(tag(HAS_ABANDONED_MINE_SNOWY), BOP, "snowy_coniferous_forest", "snowy_fir_clearing", "snowy_maple_woods");
        optional(tag(HAS_ABANDONED_MINE_SNOWY), TERRALITH, "frozen_cliffs", "wintry_forest", "snowy_maple_forest", "snowy_cherry_grove", "windswept_spires", "emerald_peaks");
        optional(tag(HAS_ABANDONED_MINE_DESERT), TERRALITH, "desert_canyon", "desert_oasis", "desert_spires", "ancient_sands", "sandstone_valley", "lush_desert");
        optional(tag(HAS_ABANDONED_MINE_FUNGAL), BOP, "ominous_woods");
        optional(tag(HAS_ABANDONED_MINE_FUNGAL), TERRALITH, "moonlight_grove");
        optional(tag(HAS_ABANDONED_MINE_QUARRY), BOP, "crag", "jade_cliffs");
        optional(tag(HAS_ABANDONED_MINE_QUARRY), TERRALITH, "stony_spires", "granite_cliffs", "yosemite_cliffs", "painted_mountains", "white_cliffs", "gravel_desert");

        optional(tag(ProspectModTags.VOLCANIC_BIOMES), BOP, "volcano", "volcanic_plains", "hot_springs");
        optional(tag(ProspectModTags.VOLCANIC_BIOMES), TERRALITH, "volcanic_peaks", "volcanic_crater", "caldera", "basalt_cliffs", "yellowstone");

        ResourceLocation grove = ProspectBiomes.GLOWSHROOM_GROVE.location();
        tag(BiomeTags.IS_OVERWORLD).addOptional(grove);
        tag(BiomeTags.HAS_MINESHAFT).addOptional(grove);
        tag(Tags.Biomes.IS_UNDERGROUND).addOptional(grove);
    }
}