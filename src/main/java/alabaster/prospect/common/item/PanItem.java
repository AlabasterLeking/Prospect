package alabaster.prospect.common.item;

import alabaster.prospect.common.entity.sparklenode.SparkleNodeEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.AABB;

public class PanItem extends Item {

    public PanItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide) return InteractionResult.SUCCESS;

        ServerLevel level = (ServerLevel) ctx.getLevel();

        AABB box = new AABB(ctx.getClickedPos()).inflate(1.0);

        var list = level.getEntitiesOfClass(SparkleNodeEntity.class, box);

        if (list.isEmpty()) return InteractionResult.PASS;

        SparkleNodeEntity node = list.get(0);

        // TODO: Loot roll based on biome/elevation
        node.discard();

        return InteractionResult.SUCCESS;
    }
}