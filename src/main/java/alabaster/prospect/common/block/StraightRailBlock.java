package alabaster.prospect.common.block;

import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;

public abstract class StraightRailBlock extends BaseRailBlock {

    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    protected StraightRailBlock(Properties properties) {
        super(true, properties);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        RailShape shape = state.getValue(SHAPE);
        RailShape rotated = switch (rotation) {
            case CLOCKWISE_180 -> switch (shape) {
                case ASCENDING_EAST -> RailShape.ASCENDING_WEST;
                case ASCENDING_WEST -> RailShape.ASCENDING_EAST;
                case ASCENDING_NORTH -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_NORTH;
                default -> shape;
            };
            case COUNTERCLOCKWISE_90 -> switch (shape) {
                case NORTH_SOUTH -> RailShape.EAST_WEST;
                case EAST_WEST -> RailShape.NORTH_SOUTH;
                case ASCENDING_EAST -> RailShape.ASCENDING_NORTH;
                case ASCENDING_WEST -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_NORTH -> RailShape.ASCENDING_WEST;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_EAST;
                default -> shape;
            };
            case CLOCKWISE_90 -> switch (shape) {
                case NORTH_SOUTH -> RailShape.EAST_WEST;
                case EAST_WEST -> RailShape.NORTH_SOUTH;
                case ASCENDING_EAST -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_WEST -> RailShape.ASCENDING_NORTH;
                case ASCENDING_NORTH -> RailShape.ASCENDING_EAST;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_WEST;
                default -> shape;
            };
            default -> shape;
        };
        return state.setValue(SHAPE, rotated);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        RailShape shape = state.getValue(SHAPE);
        RailShape mirrored = switch (mirror) {
            case LEFT_RIGHT -> switch (shape) {
                case ASCENDING_NORTH -> RailShape.ASCENDING_SOUTH;
                case ASCENDING_SOUTH -> RailShape.ASCENDING_NORTH;
                default -> shape;
            };
            case FRONT_BACK -> switch (shape) {
                case ASCENDING_EAST -> RailShape.ASCENDING_WEST;
                case ASCENDING_WEST -> RailShape.ASCENDING_EAST;
                default -> shape;
            };
            default -> shape;
        };
        return state.setValue(SHAPE, mirrored);
    }
}