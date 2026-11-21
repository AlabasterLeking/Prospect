package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.worldgen.CaveInProcessor;
import alabaster.prospect.common.worldgen.ConfigPlacement;
import alabaster.prospect.common.worldgen.EntityChanceProcessor;
import alabaster.prospect.common.worldgen.HillsidePiece;
import alabaster.prospect.common.worldgen.QuarryStructure;
import alabaster.prospect.common.worldgen.ScatterProcessor;
import alabaster.prospect.common.worldgen.HillsideStructure;
import alabaster.prospect.common.worldgen.ProspectorCampStructure;
import alabaster.prospect.common.worldgen.UniformOreProcessor;
import alabaster.prospect.common.worldgen.ZiplineProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ProspectStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, Prospect.MODID);

    public static final Supplier<StructureType<ProspectorCampStructure>> PROSPECTOR_CAMP = STRUCTURE_TYPES.register("prospector_camp", () -> () -> ProspectorCampStructure.CODEC);

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Prospect.MODID);

    public static final Supplier<StructureProcessorType<UniformOreProcessor>> UNIFORM_ORE = PROCESSORS.register("uniform_ore", () -> () -> UniformOreProcessor.CODEC);
    public static final Supplier<StructureProcessorType<CaveInProcessor>> CAVE_IN = PROCESSORS.register("cave_in", () -> () -> CaveInProcessor.CODEC);
    public static final Supplier<StructureProcessorType<ScatterProcessor>> SCATTER = PROCESSORS.register("scatter", () -> () -> ScatterProcessor.CODEC);
    public static final Supplier<StructureProcessorType<EntityChanceProcessor>> ENTITY_CHANCE = PROCESSORS.register("entity_chance", () -> () -> EntityChanceProcessor.CODEC);
    public static final Supplier<StructureProcessorType<ZiplineProcessor>> ZIPLINE = PROCESSORS.register("zipline", () -> () -> ZiplineProcessor.CODEC);

    public static final Supplier<StructureType<HillsideStructure>> HILLSIDE = STRUCTURE_TYPES.register("hillside", () -> () -> HillsideStructure.CODEC);
    public static final Supplier<StructureType<QuarryStructure>> QUARRY = STRUCTURE_TYPES.register("quarry", () -> () -> QuarryStructure.CODEC);

    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, Prospect.MODID);

    public static final Supplier<StructurePieceType> HILLSIDE_PIECE = PIECES.register("hillside", () -> HillsidePiece::new);

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENTS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Prospect.MODID);

    public static final Supplier<PlacementModifierType<ConfigPlacement>> CONFIG_PLACEMENT = PLACEMENTS.register("config", () -> () -> ConfigPlacement.CODEC);
}