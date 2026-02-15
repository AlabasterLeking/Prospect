package alabaster.prospect.common.registry;

import alabaster.prospect.Prospect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ProspectModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Prospect.MODID);

    // Ores and Minerals
    public static final Supplier<Block> RUBY_ORE = BLOCKS.register("ruby_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));
    public static final Supplier<Block> SAPPHIRE_ORE = BLOCKS.register("sapphire_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));
    public static final Supplier<Block> TOPAZ_ORE = BLOCKS.register("topaz_ore",
            () -> new Block(Block.Properties.ofFullCopy(Blocks.DIAMOND_ORE)));
}