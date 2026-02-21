package alabaster.prospect.common.entity.sparklenode;

import alabaster.prospect.common.registry.ProspectModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = "prospect")
public class SparkleNodeSpawner {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.random.nextInt(200) != 0) return;
        if (level.players().isEmpty()) return;

        ServerPlayer player = level.players().get(level.random.nextInt(level.players().size()));
        BlockPos base = player.blockPosition();

        BlockPos pos = base.offset(
                level.random.nextInt(64) - 32,
                0,
                level.random.nextInt(64) - 32
        );

        pos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);

        if (!isValidWater(level, pos)) return;

        SparkleNodeEntity entity = new SparkleNodeEntity(ProspectModEntities.SPARKLE_NODE.get(), level);
        entity.setPos(pos.getX() + 0.5, pos.getY() + 1.02, pos.getZ() + 0.5);

        level.addFreshEntity(entity);
    }

    private static boolean isValidWater(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getFluidState().is(Fluids.WATER) && level.isEmptyBlock(pos.above());
    }
}
