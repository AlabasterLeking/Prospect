package alabaster.prospect.common.prospecting;

import alabaster.prospect.Prospect;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PanningTables {

    public static final String FOLDER = "gameplay/panning/";
    public static final String DEFAULT = "default";
    public static final String NETHER_DEFAULT = "nether_default";

    private static final ResourceLocation ITEM_ENTRY = ResourceLocation.withDefaultNamespace("item");
    private static final FileToIdConverter FILES = FileToIdConverter.json("loot_table/" + FOLDER.substring(0, FOLDER.length() - 1));

    private static final List<Map.Entry<TagKey<Biome>, String>> GROUPS = List.of(
            Map.entry(Tags.Biomes.IS_SNOWY, "snowy"),
            Map.entry(BiomeTags.IS_BADLANDS, "badlands"),
            Map.entry(BiomeTags.IS_RIVER, "river"),
            Map.entry(BiomeTags.IS_OCEAN, "ocean"),
            Map.entry(BiomeTags.IS_JUNGLE, "jungle"),
            Map.entry(Tags.Biomes.IS_SWAMP, "swamp"),
            Map.entry(Tags.Biomes.IS_DESERT, "desert"),
            Map.entry(BiomeTags.IS_TAIGA, "taiga"),
            Map.entry(Tags.Biomes.IS_WINDSWEPT, "windswept_hills"));

    private static volatile List<Table> clientTables = List.of();

    private PanningTables() {
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Prospect.MODID, FOLDER + name);
    }

    @Nullable
    public static String resolve(MinecraftServer server, Holder<Biome> biome) {
        String path = biome.unwrapKey().map(key -> key.location().getPath()).orElse("plains");
        if (exists(server, path)) return path;
        for (Map.Entry<TagKey<Biome>, String> group : GROUPS) {
            if (biome.is(group.getKey())) return group.getValue();
        }
        return null;
    }

    private static boolean exists(MinecraftServer server, String name) {
        return server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id(name))) != LootTable.EMPTY;
    }

    private static String displayTable(MinecraftServer server, Holder<Biome> biome) {
        String table = resolve(server, biome);
        if (table != null) return table;
        return biome.is(BiomeTags.IS_NETHER) ? NETHER_DEFAULT : DEFAULT;
    }

    public static List<Table> collect(MinecraftServer server) {
        Map<String, List<Holder.Reference<Biome>>> biomes = new HashMap<>();
        Registry<Biome> registry = server.registryAccess().registryOrThrow(Registries.BIOME);
        registry.holders().forEach(holder -> biomes.computeIfAbsent(displayTable(server, holder), name -> new ArrayList<>()).add(holder));

        List<Table> tables = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Resource> file : FILES.listMatchingResources(server.getResourceManager()).entrySet()) {
            ResourceLocation fileId = FILES.fileToId(file.getKey());
            if (!fileId.getNamespace().equals(Prospect.MODID)) continue;
            String name = fileId.getPath();
            List<Entry> entries = readEntries(file.getKey(), file.getValue());
            if (entries.isEmpty()) continue;
            List<Holder.Reference<Biome>> holders = biomes.getOrDefault(name, List.of());
            boolean lava = name.equals(NETHER_DEFAULT) || (!holders.isEmpty() && holders.stream().allMatch(holder -> holder.is(BiomeTags.IS_NETHER)));
            List<ResourceLocation> biomeIds = holders.stream().map(holder -> holder.key().location()).sorted().toList();
            tables.add(new Table(name, entries, biomeIds, lava));
        }
        tables.sort(Comparator.comparing(Table::lava).thenComparing(table -> !table.name().equals(DEFAULT) && !table.name().equals(NETHER_DEFAULT)).thenComparing(Table::name));
        return tables;
    }

    private static List<Entry> readEntries(ResourceLocation file, Resource resource) {
        List<Entry> entries = new ArrayList<>();
        try (Reader reader = resource.openAsReader()) {
            JsonObject json = GsonHelper.parse(reader);
            for (JsonElement pool : GsonHelper.getAsJsonArray(json, "pools", new JsonArray())) {
                for (JsonElement element : GsonHelper.getAsJsonArray(pool.getAsJsonObject(), "entries", new JsonArray())) {
                    JsonObject entry = element.getAsJsonObject();
                    if (!ITEM_ENTRY.equals(ResourceLocation.tryParse(GsonHelper.getAsString(entry, "type", "")))) continue;
                    ResourceLocation item = ResourceLocation.tryParse(GsonHelper.getAsString(entry, "name", ""));
                    if (item == null || !BuiltInRegistries.ITEM.containsKey(item)) continue;
                    entries.add(new Entry(item, GsonHelper.getAsInt(entry, "weight", 1), GsonHelper.getAsInt(entry, "quality", 0)));
                }
            }
        } catch (Exception e) {
            Prospect.LOGGER.warn("Couldn't read panning table {} for JEI", file, e);
        }
        return entries;
    }

    public static List<Table> clientTables() {
        return clientTables;
    }

    public static void setClientTables(List<Table> tables) {
        clientTables = List.copyOf(tables);
    }

    public record Entry(ResourceLocation item, int weight, int quality) {

        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Entry::item,
                ByteBufCodecs.VAR_INT, Entry::weight,
                ByteBufCodecs.VAR_INT, Entry::quality,
                Entry::new
        );

        public int weight(float luck) {
            return Math.max((int) Math.floor(weight + quality * luck), 0);
        }
    }

    public record Table(String name, List<Entry> entries, List<ResourceLocation> biomes, boolean lava) {

        public static final StreamCodec<ByteBuf, Table> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, Table::name,
                Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), Table::entries,
                ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), Table::biomes,
                ByteBufCodecs.BOOL, Table::lava,
                Table::new
        );

        public float chance(int index, float luck) {
            int total = 0;
            for (Entry entry : entries) total += entry.weight(luck);
            return total <= 0 ? 0.0F : (float) entries.get(index).weight(luck) / total;
        }
    }
}
