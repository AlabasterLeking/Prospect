package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.registry.ProspectModEntities;
import alabaster.prospect.common.registry.ProspectModItems;
import alabaster.prospect.common.tag.ProspectModTags;
import alabaster.prospect.common.utilities.ProspectAdvancements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Advancements extends AdvancementProvider {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/block/tuff.png");

    public Advancements(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        super(output, registries, helper, List.of(Advancements::generate));
    }

    private static void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ProspectModItems.IRON_PAN.get(), title(ProspectAdvancements.ROOT), description(ProspectAdvancements.ROOT), BACKGROUND, AdvancementType.TASK, false, false, false)
                .addCriterion("has_pan", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ProspectModItems.COPPER_PAN.get(), ProspectModItems.IRON_PAN.get(), ProspectModItems.GOLDEN_PAN.get(), ProspectModItems.NETHERITE_PAN.get())))
                .save(saver, id(ProspectAdvancements.ROOT));

        HolderSet<Structure> mines = externalTag(registries.lookupOrThrow(Registries.STRUCTURE), ProspectModTags.ABANDONED_MINES);
        child(root, ProspectAdvancements.DEAD_END, ProspectModItems.WOODEN_RAIL.get(), AdvancementType.TASK, false)
                .addCriterion("found_mine", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.location().setStructures(mines)))
                .save(saver, id(ProspectAdvancements.DEAD_END));

        AdvancementHolder payDirt = triggered(root, ProspectAdvancements.PAY_DIRT, ProspectModItems.COPPER_PAN.get(), AdvancementType.TASK, false, saver);
        child(payDirt, ProspectAdvancements.TRIAL_BY_FIRE, ProspectModItems.NETHERITE_PAN.get(), AdvancementType.GOAL, false)
                .addCriterion("has_netherite_pan", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.NETHERITE_PAN.get()))
                .save(saver, id(ProspectAdvancements.TRIAL_BY_FIRE));

        child(root, ProspectAdvancements.BRIGHT_IDEA, ProspectModItems.MINING_HELMET.get(), AdvancementType.TASK, false)
                .addCriterion("has_mining_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.MINING_HELMET.get()))
                .save(saver, id(ProspectAdvancements.BRIGHT_IDEA));

        AdvancementHolder rockhound = child(root, ProspectAdvancements.ROCKHOUND, ProspectModItems.PROSPECTING_PICKAXE.get(), AdvancementType.TASK, false)
                .addCriterion("has_prospecting_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.PROSPECTING_PICKAXE.get()))
                .save(saver, id(ProspectAdvancements.ROCKHOUND));
        child(rockhound, ProspectAdvancements.GEM_COLLECTOR, ProspectModItems.RUBY.get(), AdvancementType.GOAL, false)
                .addCriterion("ruby", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.RUBY.get()))
                .addCriterion("sapphire", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.SAPPHIRE.get()))
                .addCriterion("topaz", InventoryChangeTrigger.TriggerInstance.hasItems(ProspectModItems.TOPAZ.get()))
                .save(saver, id(ProspectAdvancements.GEM_COLLECTOR));

        AdvancementHolder struckADeal = child(root, ProspectAdvancements.STRUCK_A_DEAL, ProspectModItems.WANDERING_PROSPECTOR_SPAWN_EGG.get(), AdvancementType.TASK, false)
                .addCriterion("traded", CriteriaTriggers.TRADE.createCriterion(new TradeTrigger.TriggerInstance(Optional.empty(), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(ProspectModEntities.WANDERING_PROSPECTOR.get()))), Optional.empty())))
                .save(saver, id(ProspectAdvancements.STRUCK_A_DEAL));
        triggered(struckADeal, ProspectAdvancements.BACK_FROM_THE_DEAD, Items.GOLDEN_APPLE, AdvancementType.GOAL, true, saver);

        AdvancementHolder catchingAir = triggered(root, ProspectAdvancements.CATCHING_AIR, Items.MINECART, AdvancementType.TASK, false, saver);
        triggered(catchingAir, ProspectAdvancements.LIFTOFF, ProspectModItems.LAUNCH_RAIL.get(), AdvancementType.TASK, false, saver);

        AdvancementHolder hooked = triggered(root, ProspectAdvancements.HOOKED_ON_A_FEELING, ProspectModItems.GRAPPLING_HOOK.get(), AdvancementType.TASK, false, saver);
        triggered(hooked, ProspectAdvancements.ZIPPEE, Items.LEAD, AdvancementType.TASK, false, saver);
        triggered(hooked, ProspectAdvancements.GET_OVER_HERE, Items.ZOMBIE_HEAD, AdvancementType.TASK, false, saver);

        AdvancementHolder fireInTheHole = triggered(root, ProspectAdvancements.FIRE_IN_THE_HOLE, ProspectModItems.DYNAMITE.get(), AdvancementType.TASK, false, saver);
        triggered(fireInTheHole, ProspectAdvancements.HOT_POTATO, ProspectModItems.LIT_DYNAMITE.get(), AdvancementType.TASK, true, saver);
    }

    private static <T> HolderSet<T> externalTag(HolderLookup.RegistryLookup<T> lookup, TagKey<T> tag) {
        Optional<HolderSet.Named<T>> existing = lookup.get(tag);
        if (existing.isPresent()) return existing.get();
        Holder.Reference<T> sibling = lookup.listElements().findFirst().orElseThrow();
        HolderOwner<T> owner = new HolderOwner<>() {
            @Override
            public boolean canSerializeIn(HolderOwner<T> other) {
                return sibling.canSerializeIn(other);
            }
        };
        return HolderSet.emptyNamed(owner, tag);
    }

    private static Advancement.Builder child(AdvancementHolder parent, String name, ItemLike icon, AdvancementType type, boolean hidden) {
        return Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(name), description(name), null, type, true, true, hidden);
    }

    private static AdvancementHolder triggered(AdvancementHolder parent, String name, ItemLike icon, AdvancementType type, boolean hidden, Consumer<AdvancementHolder> saver) {
        return child(parent, name, icon, type, hidden)
                .addCriterion(ProspectAdvancements.TRIGGERED, impossible())
                .save(saver, id(name));
    }

    private static Criterion<ImpossibleTrigger.TriggerInstance> impossible() {
        return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
    }

    private static String id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Prospect.MODID, name).toString();
    }

    private static Component title(String name) {
        return Component.translatable("advancements." + Prospect.MODID + "." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements." + Prospect.MODID + "." + name + ".description");
    }
}
