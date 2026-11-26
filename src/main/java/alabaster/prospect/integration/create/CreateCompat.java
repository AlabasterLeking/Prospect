package alabaster.prospect.integration.create;

import alabaster.prospect.Prospect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

public final class CreateCompat {

    public static final String MODID = "create";
    private static final String CONTRAPTION_ENTITY = "com.simibubi.create.content.contraptions.AbstractContraptionEntity";
    private static final String POTATO_PROJECTILE = "com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity";

    private static boolean resolved;
    @Nullable
    private static Class<?> contraptionEntity;
    private static Method toGlobal;
    private static Method toLocal;
    private static Method getContraption;
    private static Method getBlocks;
    private static boolean potatoResolved;
    @Nullable
    private static Class<?> potatoProjectile;
    private static Method getItem;

    private CreateCompat() {
    }

    public record Hit(Entity contraption, Vec3 local, Vec3 world, Direction face, double distanceSqr) {
    }

    private static boolean available() {
        if (!resolved) {
            resolved = true;
            if (ModList.get().isLoaded(MODID)) {
                try {
                    Class<?> entity = Class.forName(CONTRAPTION_ENTITY);
                    toGlobal = entity.getMethod("toGlobalVector", Vec3.class, float.class);
                    toLocal = entity.getMethod("toLocalVector", Vec3.class, float.class);
                    getContraption = entity.getMethod("getContraption");
                    getBlocks = getContraption.getReturnType().getMethod("getBlocks");
                    contraptionEntity = entity;
                } catch (ReflectiveOperationException | LinkageError e) {
                    Prospect.LOGGER.warn("Couldn't hook into Create contraptions for grappling hooks", e);
                }
            }
        }
        return contraptionEntity != null;
    }

    private static boolean potatoAvailable() {
        if (!potatoResolved) {
            potatoResolved = true;
            if (ModList.get().isLoaded(MODID)) {
                try {
                    Class<?> projectile = Class.forName(POTATO_PROJECTILE);
                    getItem = projectile.getMethod("getItem");
                    potatoProjectile = projectile;
                } catch (ReflectiveOperationException | LinkageError e) {
                    Prospect.LOGGER.warn("Couldn't hook into Create's potato cannon for dynamite", e);
                }
            }
        }
        return potatoProjectile != null;
    }

    public static ItemStack potatoItem(Entity entity) {
        if (!potatoAvailable() || !potatoProjectile.isInstance(entity)) return ItemStack.EMPTY;
        try {
            return (ItemStack) getItem.invoke(entity);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return ItemStack.EMPTY;
        }
    }

    public static boolean isContraption(@Nullable Entity entity) {
        return entity != null && available() && contraptionEntity.isInstance(entity);
    }

    @Nullable
    public static Vec3 toGlobal(Entity entity, Vec3 local) {
        if (!isContraption(entity)) return null;
        try {
            return (Vec3) toGlobal.invoke(entity, local, 1.0F);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Nullable
    public static Hit clip(Level level, Vec3 from, Vec3 to) {
        if (!available()) return null;
        Hit best = null;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(from, to).inflate(0.5), CreateCompat::isContraption)) {
            Hit hit = clip(entity, from, to);
            if (hit != null && (best == null || hit.distanceSqr() < best.distanceSqr())) {
                best = hit;
            }
        }
        return best;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Hit clip(Entity entity, Vec3 from, Vec3 to) {
        try {
            Object contraption = getContraption.invoke(entity);
            if (contraption == null) return null;
            Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = (Map<BlockPos, StructureTemplate.StructureBlockInfo>) getBlocks.invoke(contraption);
            Vec3 localFrom = (Vec3) toLocal.invoke(entity, from, 1.0F);
            Vec3 localTo = (Vec3) toLocal.invoke(entity, to, 1.0F);
            BlockPos min = BlockPos.containing(Math.min(localFrom.x, localTo.x) - 1.0, Math.min(localFrom.y, localTo.y) - 1.0, Math.min(localFrom.z, localTo.z) - 1.0);
            BlockPos max = BlockPos.containing(Math.max(localFrom.x, localTo.x) + 1.0, Math.max(localFrom.y, localTo.y) + 1.0, Math.max(localFrom.z, localTo.z) + 1.0);
            BlockHitResult nearest = null;
            double nearestSqr = Double.MAX_VALUE;
            for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
                StructureTemplate.StructureBlockInfo info = blocks.get(pos);
                if (info == null) continue;
                BlockState state = info.state();
                VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                if (shape.isEmpty()) continue;
                BlockHitResult hit = shape.clip(localFrom, localTo, pos.immutable());
                if (hit == null) continue;
                double distance = hit.getLocation().distanceToSqr(localFrom);
                if (distance < nearestSqr) {
                    nearestSqr = distance;
                    nearest = hit;
                }
            }
            if (nearest == null) return null;
            Vec3 world = (Vec3) toGlobal.invoke(entity, nearest.getLocation(), 1.0F);
            Vec3 normal = Vec3.atLowerCornerOf(nearest.getDirection().getNormal());
            Vec3 worldNormal = ((Vec3) toGlobal.invoke(entity, nearest.getLocation().add(normal), 1.0F)).subtract(world);
            Direction face = Direction.getNearest(worldNormal.x, worldNormal.y, worldNormal.z);
            return new Hit(entity, nearest.getLocation(), world, face, nearestSqr);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }
}