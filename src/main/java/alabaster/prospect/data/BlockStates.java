package alabaster.prospect.data;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.block.PlacedDynamiteBlock;
import alabaster.prospect.common.block.PlacedDynamiteBundleBlock;
import alabaster.prospect.common.registry.ProspectModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BlockStates extends BlockStateProvider
{
    private static final int DEFAULT_ANGLE_OFFSET = 180;

    public BlockStates(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Prospect.MODID, existingFileHelper);
    }

    private String blockName(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    public ResourceLocation resourceBlock(String path) {
        return ResourceLocation.fromNamespaceAndPath(Prospect.MODID, "block/" + path);
    }

    public ModelFile existingModel(String path) {
        return new ModelFile.ExistingModelFile(resourceBlock(path), models().existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        this.simpleBlock(ProspectModBlocks.RUBY_ORE.get());
        this.simpleBlock(ProspectModBlocks.SAPPHIRE_ORE.get());
        this.simpleBlock(ProspectModBlocks.TOPAZ_ORE.get());
        this.simpleBlock(ProspectModBlocks.DEEPSLATE_RUBY_ORE.get());
        this.simpleBlock(ProspectModBlocks.DEEPSLATE_SAPPHIRE_ORE.get());
        this.simpleBlock(ProspectModBlocks.DEEPSLATE_TOPAZ_ORE.get());
        this.simpleBlock(ProspectModBlocks.RUBY_BLOCK.get());
        this.simpleBlock(ProspectModBlocks.SAPPHIRE_BLOCK.get());
        this.simpleBlock(ProspectModBlocks.TOPAZ_BLOCK.get());
        this.simpleBlock(ProspectModBlocks.CRYSTALLINE_QUARTZ.get());
        this.simpleBlock(ProspectModBlocks.BUDDING_QUARTZ.get());
        crystalBud(ProspectModBlocks.SMALL_QUARTZ_BUD.get());
        crystalBud(ProspectModBlocks.MEDIUM_QUARTZ_BUD.get());
        crystalBud(ProspectModBlocks.LARGE_QUARTZ_BUD.get());
        crystalBud(ProspectModBlocks.QUARTZ_CLUSTER.get());

        glowingMushroom(ProspectModBlocks.GLOWING_MUSHROOM_BLUE.get());
        glowingMushroom(ProspectModBlocks.GLOWING_MUSHROOM_GREEN.get());
        glowingMushroom(ProspectModBlocks.GLOWING_MUSHROOM_PURPLE.get());
        glowingCap(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_BLUE.get());
        glowingCap(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_GREEN.get());
        glowingCap(ProspectModBlocks.GLOWING_MUSHROOM_BLOCK_PURPLE.get());
        glowthread(ProspectModBlocks.GLOWTHREAD.get(), ProspectModBlocks.GLOWTHREAD_PLANT.get());
        rail(ProspectModBlocks.WOODEN_RAIL.get());
        extinguishedTorch(ProspectModBlocks.EXTINGUISHED_TORCH.get(), ProspectModBlocks.EXTINGUISHED_WALL_TORCH.get());
        extinguishedLantern(ProspectModBlocks.EXTINGUISHED_LANTERN.get());
        placedDynamite(ProspectModBlocks.PLACED_DYNAMITE.get(), PlacedDynamiteBlock.STICKS);
        placedDynamite(ProspectModBlocks.PLACED_DYNAMITE_BUNDLE.get(), PlacedDynamiteBundleBlock.BUNDLES);
    }

    private void crystalBud(Block block) {
        String name = blockName(block);
        directionalBlock(block, models().cross(name, resourceBlock(name)).renderType("cutout"));
    }

    private void extinguishedTorch(Block torch, Block wallTorch) {
        String name = blockName(torch);
        simpleBlock(torch, models().torch(name, resourceBlock(name)).renderType("cutout"));
        horizontalBlock(wallTorch, models().torchWall(blockName(wallTorch), resourceBlock(name)).renderType("cutout"), 90);
    }

    private void extinguishedLantern(Block block) {
        String name = blockName(block);
        ModelFile standing = models().withExistingParent(name, mcLoc("block/template_lantern")).texture("lantern", resourceBlock(name)).renderType("cutout");
        ModelFile hanging = models().withExistingParent(name + "_hanging", mcLoc("block/template_hanging_lantern")).texture("lantern", resourceBlock(name)).renderType("cutout");
        getVariantBuilder(block).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LanternBlock.HANGING) ? hanging : standing)
                .build(), LanternBlock.WATERLOGGED);
    }

    private void placedDynamite(Block block, IntegerProperty count) {
        String name = blockName(block);
        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(models().getExistingFile(resourceBlock(name + (state.hasProperty(PlacedDynamiteBlock.LYING) && state.getValue(PlacedDynamiteBlock.LYING) ? "_lying_" : "_") + state.getValue(count))))
                .rotationY(((int) state.getValue(HorizontalDirectionalBlock.FACING).toYRot() + 180) % 360)
                .build());
    }

    private void glowingMushroom(Block block) {
        String name = blockName(block);
        simpleBlock(block, models().withExistingParent(name, resourceBlock("template_split_glowing_cross"))
                .texture("base", resourceBlock(name + "_stem"))
                .texture("glow", resourceBlock(name + "_cap"))
                .texture("particle", resourceBlock(name)));
    }

    private void glowingCap(Block block) {
        String name = blockName(block);
        String insideName = name.replace("glowing_mushroom_block_", "glowing_mushroom_block_inside_");
        ModelFile outside = models().withExistingParent(name, resourceBlock("template_glowing_single_face")).texture("texture", resourceBlock(name));
        ModelFile inside = models().withExistingParent(insideName, resourceBlock("template_glowing_single_face")).texture("texture", resourceBlock(insideName));
        models().withExistingParent(name + "_inventory", resourceBlock("template_glowing_cube_all")).texture("all", resourceBlock(name));
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
        for (Direction direction : Direction.values()) {
            int x = direction == Direction.UP ? 270 : direction == Direction.DOWN ? 90 : 0;
            int y = direction.getAxis().isHorizontal() ? ((int) direction.toYRot() + 180) % 360 : 0;
            builder.part().modelFile(outside).rotationX(x).rotationY(y).uvLock(direction != Direction.NORTH).addModel()
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), true).end();
            builder.part().modelFile(inside).rotationX(x).rotationY(y).addModel()
                    .condition(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), false).end();
        }
    }

    private void glowthread(Block head, Block plant) {
        String name = blockName(head);
        simpleBlock(head, models().withExistingParent(name, resourceBlock("template_split_glowing_cross"))
                .texture("base", resourceBlock(name + "_strands"))
                .texture("glow", resourceBlock(name + "_tip"))
                .texture("particle", resourceBlock(name)));
        String plantName = blockName(plant);
        simpleBlock(plant, models().cross(plantName, resourceBlock(plantName)).renderType("cutout"));
    }

    private void rail(Block block) {
        String name = blockName(block);
        ModelFile flat = models().withExistingParent(name, mcLoc("block/rail_flat")).texture("rail", resourceBlock(name)).renderType("cutout");
        ModelFile corner = models().withExistingParent(name + "_corner", mcLoc("block/rail_curved")).texture("rail", resourceBlock(name + "_corner")).renderType("cutout");
        ModelFile raisedNe = models().withExistingParent(name + "_raised_ne", mcLoc("block/template_rail_raised_ne")).texture("rail", resourceBlock(name)).renderType("cutout");
        ModelFile raisedSw = models().withExistingParent(name + "_raised_sw", mcLoc("block/template_rail_raised_sw")).texture("rail", resourceBlock(name)).renderType("cutout");
        getVariantBuilder(block).forAllStatesExcept(state -> {
            RailShape shape = state.getValue(RailBlock.SHAPE);
            return switch (shape) {
                case NORTH_SOUTH -> rotated(flat, 0);
                case EAST_WEST -> rotated(flat, 90);
                case ASCENDING_NORTH -> rotated(raisedNe, 0);
                case ASCENDING_EAST -> rotated(raisedNe, 90);
                case ASCENDING_SOUTH -> rotated(raisedSw, 0);
                case ASCENDING_WEST -> rotated(raisedSw, 90);
                case SOUTH_EAST -> rotated(corner, 0);
                case SOUTH_WEST -> rotated(corner, 90);
                case NORTH_WEST -> rotated(corner, 180);
                case NORTH_EAST -> rotated(corner, 270);
            };
        }, BaseRailBlock.WATERLOGGED);
    }

    private ConfiguredModel[] rotated(ModelFile model, int y) {
        return ConfiguredModel.builder().modelFile(model).rotationY(y).build();
    }
}