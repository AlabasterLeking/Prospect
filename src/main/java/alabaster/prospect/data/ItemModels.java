package alabaster.prospect.data;

import alabaster.prospect.common.item.SlimeBucketItem;
import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModItems;
import com.google.common.collect.Sets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemModels extends ItemModelProvider
{
    public static final String GENERATED = "item/generated";
    public static final String HANDHELD = "item/handheld";
    private static final ResourceLocation LIT = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "lit");
    private static final ResourceLocation DANCE = ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "dance");

    public ItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Prospect.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        Set<Item> items = BuiltInRegistries.ITEM.stream().filter(i -> Prospect.MODID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
                .collect(Collectors.toSet());

        // Custom Models
        items.remove(ProspectModItems.COPPER_PAN.get());
        items.remove(ProspectModItems.IRON_PAN.get());
        items.remove(ProspectModItems.GOLDEN_PAN.get());
        items.remove(ProspectModItems.NETHERITE_PAN.get());
        items.remove(ProspectModItems.MINING_HELMET.get());
        items.remove(ProspectModItems.DYNAMITE.get());
        items.remove(ProspectModItems.LIT_DYNAMITE.get());
        items.remove(ProspectModItems.STICKY_DYNAMITE.get());
        items.remove(ProspectModItems.LIT_STICKY_DYNAMITE.get());
        items.remove(ProspectModItems.DYNAMITE_BUNDLE.get());
        items.remove(ProspectModItems.LIT_DYNAMITE_BUNDLE.get());

        // Blocks with special item sprites
        Set<Item> spriteBlockItems = Sets.newHashSet(
                ProspectModItems.EXTINGUISHED_LANTERN.get(),
                ProspectModItems.GLOWING_MUSHROOM_BLUE.get(),
                ProspectModItems.GLOWING_MUSHROOM_GREEN.get(),
                ProspectModItems.GLOWING_MUSHROOM_PURPLE.get()
        );
        takeAll(items, spriteBlockItems.toArray(new Item[0])).forEach(item -> withExistingParent(itemName(item), GENERATED).texture("layer0", resourceItem(itemName(item))));

        // Blocks with flat block textures for their items
        Set<Item> flatBlockItems = Sets.newHashSet(
                ProspectModItems.EXTINGUISHED_TORCH.get(),
                ProspectModItems.WOODEN_RAIL.get(),
                ProspectModItems.LAUNCH_RAIL.get(),
                ProspectModItems.PAYLOAD_RAIL.get(),
                ProspectModItems.GLOWTHREAD.get(),
                ProspectModItems.SMALL_QUARTZ_BUD.get(),
                ProspectModItems.MEDIUM_QUARTZ_BUD.get(),
                ProspectModItems.LARGE_QUARTZ_BUD.get(),
                ProspectModItems.QUARTZ_CLUSTER.get()
        );

        takeAll(items, flatBlockItems.toArray(new Item[0])).forEach(item -> itemGeneratedModel(item, resourceBlock(itemName(item))));
        // Blocks with dedicated inventory models
        takeAll(items, ProspectModItems.GLOWING_MUSHROOM_BLOCK_BLUE.get(), ProspectModItems.GLOWING_MUSHROOM_BLOCK_GREEN.get(), ProspectModItems.GLOWING_MUSHROOM_BLOCK_PURPLE.get())
                .forEach(item -> blockBasedModel(item, "_inventory"));

        takeAll(items, ProspectModItems.TINTED_GLASS_PANE.get()).forEach(item -> itemGeneratedModel(item, mcLoc("block/tinted_glass")).renderType("translucent"));
        takeAll(items, ProspectModItems.SAPPHIRE_GLASS_PANE.get()).forEach(item -> itemGeneratedModel(item, resourceBlock("sapphire_glass")).renderType("translucent"));

        items.remove(ProspectModItems.ALTIMETER.get());

        // Items with property overrides
        takeAll(items, ProspectModItems.PORTABLE_SMELTER.get()).forEach(this::portableSmelterModel);
        takeAll(items, ProspectModItems.SLIME_BUCKET.get()).forEach(this::slimeBucketModel);

        // Blocks whose items look alike
        takeAll(items, i -> i instanceof BlockItem).forEach(item -> blockBasedModel(item, ""));

        // Handheld items
        Set<Item> handheldItems = Sets.newHashSet(
                ProspectModItems.PROSPECTING_PICKAXE.get()
        );
        takeAll(items, handheldItems.toArray(new Item[0])).forEach(item -> itemHandheldModel(item, resourceItem(itemName(item))));

        // Spawn eggs
        takeAll(items, i -> i instanceof SpawnEggItem).forEach(item -> withExistingParent(itemName(item), mcLoc("item/template_spawn_egg")));

        // Generated items
        items.forEach(item -> itemGeneratedModel(item, resourceItem(itemName(item))));
    }

    private void slimeBucketModel(Item item) {
        String name = itemName(item);
        ItemModelBuilder bucket = withExistingParent(name, GENERATED).texture("layer0", resourceItem(name + "_0"));
        for (int frame = 1; frame <= SlimeBucketItem.DANCE_FRAMES; frame++) {
            ItemModelBuilder dance = withExistingParent(name + "_" + frame, GENERATED).texture("layer0", resourceItem(name + "_" + frame));
            bucket.override().predicate(DANCE, frame / 10.0F - 0.05F).model(dance).end();
        }
    }

    private void portableSmelterModel(Item item) {
        String name = itemName(item);
        ItemModelBuilder lit = withExistingParent(name + "_lit", GENERATED).texture("layer0", resourceItem(name + "_lit"));
        withExistingParent(name, GENERATED).texture("layer0", resourceItem(name)).override().predicate(LIT, 1).model(lit).end();
    }

    public void blockBasedModel(Item item, String suffix) {
        withExistingParent(itemName(item), resourceBlock(itemName(item) + suffix));
    }

    public void itemHandheldModel(Item item, ResourceLocation texture) {
        withExistingParent(itemName(item), HANDHELD).texture("layer0", texture);
    }

    public ItemModelBuilder itemGeneratedModel(Item item, ResourceLocation texture) {
        return withExistingParent(itemName(item), GENERATED).texture("layer0", texture);
    }

    private String itemName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    public ResourceLocation resourceBlock(String path) {
        return ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "block/" + path);
    }

    public ResourceLocation resourceItem(String path) {
        return ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "item/" + path);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Collection<T> takeAll(Set<? extends T> src, T... items) {
        List<T> ret = Arrays.asList(items);
        for (T item : items) {
            if (!src.contains(item)) {
                Prospect.LOGGER.warn("Item {} not found in set", item);
            }
        }
        if (!src.removeAll(ret)) {
            Prospect.LOGGER.warn("takeAll array didn't yield anything ({})", Arrays.toString(items));
        }
        return ret;
    }

    public static <T> Collection<T> takeAll(Set<T> src, Predicate<T> pred) {
        List<T> ret = new ArrayList<>();

        Iterator<T> iter = src.iterator();
        while (iter.hasNext()) {
            T item = iter.next();
            if (pred.test(item)) {
                iter.remove();
                ret.add(item);
            }
        }

        if (ret.isEmpty()) {
            Prospect.LOGGER.warn("takeAll predicate yielded nothing", new Throwable());
        }
        return ret;
    }
}