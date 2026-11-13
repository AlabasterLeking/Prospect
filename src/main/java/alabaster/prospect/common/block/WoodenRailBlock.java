package alabaster.prospect.common.block;

import alabaster.prospect.Prospect;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WoodenRailBlock extends RailBlock {

    public static final MapCodec<WoodenRailBlock> CODEC = simpleCodec(WoodenRailBlock::new);
    public static final float MAX_SPEED = 0.3F;
    public static final double DRAG = 0.993;
    public static final SoundEvent RATTLE = SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "block.wooden_rail.rattle"));
    private static final double RATTLE_MIN_SPEED = 0.05;
    private static final float RATTLE_CHANCE = 0.35F;
    private static final int SHAKE_TIME = 6;
    private static final float SHAKE_DAMAGE = 5.0F;

    public WoodenRailBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<RailBlock> codec() {
        return (MapCodec<RailBlock>) (MapCodec<?>) CODEC;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return MAX_SPEED;
    }

    @Override
    public void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        Vec3 motion = cart.getDeltaMovement();
        double scale = DRAG;
        double speed = motion.horizontalDistance();
        if (speed * scale > MAX_SPEED) {
            scale = MAX_SPEED / speed;
        }
        cart.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
        if (!level.isClientSide() && cart.isVehicle() && speed > RATTLE_MIN_SPEED && level.random.nextFloat() < RATTLE_CHANCE * Math.min(1.0, speed / MAX_SPEED)) {
            level.playSound(null, cart.getX(), cart.getY(), cart.getZ(), RATTLE, SoundSource.NEUTRAL,
                    0.4F + (float) Math.min(1.0, speed / MAX_SPEED) * 0.4F, 0.8F + level.random.nextFloat() * 0.4F);
            if (cart.getHurtTime() <= 0) {
                cart.setHurtTime(SHAKE_TIME);
                cart.setHurtDir(level.random.nextBoolean() ? 1 : -1);
                cart.setDamage(Math.max(cart.getDamage(), SHAKE_DAMAGE));
            }
        }
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }
}