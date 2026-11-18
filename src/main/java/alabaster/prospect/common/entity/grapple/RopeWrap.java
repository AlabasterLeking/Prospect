package alabaster.prospect.common.entity.grapple;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RopeWrap {

    private static final int MAX_POINTS = 16;
    private static final int STEPS_PER_TICK = 4;
    private static final int SWEEP_STEPS = 12;
    private static final int LINE_STEPS = 40;
    private static final double OFFSET = 0.04;
    private static final double MIN_GAP = 0.1;
    private static final double UNWRAP_BEND = -0.03;

    private final List<Vec3> points = new ArrayList<>();
    private final List<Vec3> bends = new ArrayList<>();
    @Nullable
    private Vec3 previousTarget;
    private int tick = -1;
    private double delta;

    public List<Vec3> points() {
        return Collections.unmodifiableList(points);
    }

    public Vec3 pivot(Vec3 anchor) {
        return points.isEmpty() ? anchor : points.get(points.size() - 1);
    }

    public double fixedLength(Vec3 anchor) {
        double total = 0.0;
        Vec3 from = anchor;
        for (Vec3 point : points) {
            total += from.distanceTo(point);
            from = point;
        }
        return total;
    }

    public double takeDelta() {
        double taken = delta;
        delta = 0.0;
        return taken;
    }

    public void update(Level level, Entity owner, Vec3 anchor, Vec3 anchorStart, Vec3 target, int tick) {
        if (this.tick == tick) return;
        this.tick = tick;
        for (int i = 0; i < STEPS_PER_TICK && unwrap(level, owner, anchor, anchorStart, target); i++) {}
        if (!insideBlock(level, target)) {
            for (int i = 0; i < STEPS_PER_TICK && points.size() < MAX_POINTS && wrap(level, owner, anchor, anchorStart, target); i++) {}
        }
        previousTarget = target;
    }

    private boolean wrap(Level level, Entity owner, Vec3 anchor, Vec3 anchorStart, Vec3 target) {
        Vec3 from = points.isEmpty() ? anchorStart : points.get(points.size() - 1);
        BlockHitResult hit = clip(level, owner, from, target);
        if (hit.getType() == HitResult.Type.MISS) return false;
        Vec3 point = null;
        if (previousTarget != null && clip(level, owner, from, previousTarget).getType() == HitResult.Type.MISS) {
            double lo = 0.0;
            double hi = 1.0;
            for (int i = 0; i < SWEEP_STEPS; i++) {
                double mid = (lo + hi) * 0.5;
                BlockHitResult probe = clip(level, owner, from, previousTarget.lerp(target, mid));
                if (probe.getType() == HitResult.Type.MISS) {
                    lo = mid;
                } else {
                    hi = mid;
                    hit = probe;
                }
            }
            point = hug(level, hit.getBlockPos(), from, previousTarget.lerp(target, lo));
        }
        if (point == null || clip(level, owner, from, point).getType() != HitResult.Type.MISS) {
            point = detour(level, hit.getBlockPos(), from, target);
        }
        Vec3 previous = pivot(anchor);
        if (point.distanceTo(previous) < MIN_GAP) return false;
        delta += previous.distanceTo(point) + point.distanceTo(target) - previous.distanceTo(target);
        points.add(point);
        bends.add(point.subtract(previous).cross(target.subtract(point)).normalize());
        return true;
    }

    private boolean unwrap(Level level, Entity owner, Vec3 anchor, Vec3 anchorStart, Vec3 target) {
        if (points.isEmpty()) return false;
        int last = points.size() - 1;
        Vec3 bend = points.get(last);
        Vec3 before = last == 0 ? anchor : points.get(last - 1);
        Vec3 in = bend.subtract(before);
        Vec3 out = target.subtract(bend);
        double scale = in.length() * out.length();
        Vec3 stored = bends.get(last);
        if (scale > 1.0E-6 && stored.lengthSqr() > 0.5 && in.cross(out).dot(stored) / scale > UNWRAP_BEND) return false;
        Vec3 from = last == 0 ? anchorStart : before;
        if (clip(level, owner, from, target).getType() != HitResult.Type.MISS) return false;
        delta -= before.distanceTo(bend) + bend.distanceTo(target) - before.distanceTo(target);
        points.remove(last);
        bends.remove(last);
        return true;
    }

    @Nullable
    private static Vec3 hug(Level level, BlockPos pos, Vec3 a, Vec3 b) {
        Vec3 bestNear = null;
        double best = Double.MAX_VALUE;
        AABB bestBox = null;
        for (AABB box : boxes(level, pos)) {
            double lo = 0.0;
            double hi = 1.0;
            for (int i = 0; i < LINE_STEPS; i++) {
                double m1 = lo + (hi - lo) / 3.0;
                double m2 = hi - (hi - lo) / 3.0;
                if (distanceSqr(box, a.lerp(b, m1)) < distanceSqr(box, a.lerp(b, m2))) {
                    hi = m2;
                } else {
                    lo = m1;
                }
            }
            Vec3 line = a.lerp(b, (lo + hi) * 0.5);
            double distance = distanceSqr(box, line);
            if (distance < best) {
                best = distance;
                bestNear = clamp(box, line);
                bestBox = box;
            }
        }
        return bestNear == null ? null : pushOut(bestBox, bestNear);
    }

    private static Vec3 detour(Level level, BlockPos pos, Vec3 a, Vec3 b) {
        Vec3 bestPoint = null;
        double best = Double.MAX_VALUE;
        AABB bestBox = null;
        for (AABB box : boxes(level, pos)) {
            for (Vec3[] edge : edges(box)) {
                double lo = 0.0;
                double hi = 1.0;
                for (int i = 0; i < LINE_STEPS; i++) {
                    double m1 = lo + (hi - lo) / 3.0;
                    double m2 = hi - (hi - lo) / 3.0;
                    if (path(a, edge[0].lerp(edge[1], m1), b) < path(a, edge[0].lerp(edge[1], m2), b)) {
                        hi = m2;
                    } else {
                        lo = m1;
                    }
                }
                Vec3 point = edge[0].lerp(edge[1], (lo + hi) * 0.5);
                double length = path(a, point, b);
                if (length < best) {
                    best = length;
                    bestPoint = point;
                    bestBox = box;
                }
            }
        }
        return pushOut(bestBox, bestPoint);
    }

    private static Vec3 pushOut(AABB box, Vec3 point) {
        double x = side(point.x, box.minX, box.maxX);
        double y = side(point.y, box.minY, box.maxY);
        double z = side(point.z, box.minZ, box.maxZ);
        Vec3 out = new Vec3(x, y, z);
        if (out.lengthSqr() < 1.0E-9) {
            out = point.subtract(box.getCenter());
        }
        return point.add(out.normalize().scale(OFFSET));
    }

    private static double side(double value, double min, double max) {
        if (Math.abs(value - min) < 1.0E-4) return -1.0;
        if (Math.abs(value - max) < 1.0E-4) return 1.0;
        return 0.0;
    }

    private static double path(Vec3 a, Vec3 point, Vec3 b) {
        return a.distanceTo(point) + point.distanceTo(b);
    }

    private static Vec3 clamp(AABB box, Vec3 point) {
        return new Vec3(Math.max(box.minX, Math.min(box.maxX, point.x)), Math.max(box.minY, Math.min(box.maxY, point.y)), Math.max(box.minZ, Math.min(box.maxZ, point.z)));
    }

    private static double distanceSqr(AABB box, Vec3 point) {
        return clamp(box, point).distanceToSqr(point);
    }

    private static List<AABB> boxes(Level level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        if (shape.isEmpty()) return List.of(new AABB(pos));
        List<AABB> boxes = new ArrayList<>();
        for (AABB box : shape.toAabbs()) {
            boxes.add(box.move(pos));
        }
        return boxes;
    }

    private static List<Vec3[]> edges(AABB box) {
        List<Vec3[]> edges = new ArrayList<>(12);
        double[] xs = {box.minX, box.maxX};
        double[] ys = {box.minY, box.maxY};
        double[] zs = {box.minZ, box.maxZ};
        for (double y : ys) {
            for (double z : zs) {
                edges.add(new Vec3[]{new Vec3(box.minX, y, z), new Vec3(box.maxX, y, z)});
            }
        }
        for (double x : xs) {
            for (double z : zs) {
                edges.add(new Vec3[]{new Vec3(x, box.minY, z), new Vec3(x, box.maxY, z)});
            }
        }
        for (double x : xs) {
            for (double y : ys) {
                edges.add(new Vec3[]{new Vec3(x, y, box.minZ), new Vec3(x, y, box.maxZ)});
            }
        }
        return edges;
    }

    private static boolean insideBlock(Level level, Vec3 point) {
        BlockPos pos = BlockPos.containing(point);
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        if (shape.isEmpty()) return false;
        for (AABB box : shape.toAabbs()) {
            if (box.move(pos).contains(point)) return true;
        }
        return false;
    }

    private static BlockHitResult clip(Level level, Entity owner, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
    }
}