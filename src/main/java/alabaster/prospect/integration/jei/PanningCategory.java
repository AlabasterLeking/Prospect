package alabaster.prospect.integration.jei;

import alabaster.prospect.Prospect;
import alabaster.prospect.common.item.PanItem;
import alabaster.prospect.common.prospecting.PanningTables;
import alabaster.prospect.common.registry.ProspectModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PanningCategory implements IRecipeCategory<PanningTables.Table> {

    public static final RecipeType<PanningTables.Table> TYPE = RecipeType.create(Prospect.MODID, "panning", PanningTables.Table.class);
    public static final int COLUMNS = 9;
    private static final int CELL_WIDTH = 18;
    private static final int CELL_HEIGHT = 28;
    private static final int HEADER = 12;
    private static final int MAX_ROWS = 4;
    private static final int MAX_LISTED_BIOMES = 20;
    private static final float CHANCE_SCALE = 0.75F;
    private static final int TEXT_COLOR = 0x404040;
    private static final int LINK_COLOR = 0x3F76E4;
    private static final int LAVA_COLOR = 0xC0501E;

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final int rows;

    public PanningCategory(IGuiHelper guiHelper, int rows) {
        this.rows = Mth.clamp(rows, 1, MAX_ROWS);
        this.background = guiHelper.createBlankDrawable(COLUMNS * CELL_WIDTH, HEADER + this.rows * CELL_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ProspectModItems.COPPER_PAN.get()));
        this.slot = guiHelper.getSlotDrawable();
    }

    public static List<PanItem> pans(PanningTables.Table table) {
        if (table.lava()) return List.of((PanItem) ProspectModItems.NETHERITE_PAN.get());
        return Arrays.asList((PanItem) ProspectModItems.COPPER_PAN.get(), (PanItem) ProspectModItems.IRON_PAN.get(), (PanItem) ProspectModItems.NETHERITE_PAN.get(), (PanItem) ProspectModItems.GOLDEN_PAN.get());
    }

    @Override
    public RecipeType<PanningTables.Table> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.prospect.panning");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    @SuppressWarnings("removal")
    public void setRecipe(IRecipeLayoutBuilder builder, PanningTables.Table table, IFocusGroup focuses) {
        List<PanItem> pans = pans(table);
        int shown = Math.min(table.entries().size(), COLUMNS * rows);
        for (int i = 0; i < shown; i++) {
            int index = i;
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(table.entries().get(i).item()));
            builder.addSlot(RecipeIngredientRole.OUTPUT, (i % COLUMNS) * CELL_WIDTH + 1, HEADER + (i / COLUMNS) * CELL_HEIGHT + 1)
                    .setBackground(slot, -1, -1)
                    .addItemStack(stack)
                    .addTooltipCallback((view, tooltip) -> {
                        tooltip.add(Component.translatable("jei.prospect.panning.chances").withStyle(ChatFormatting.GRAY));
                        for (PanItem pan : pans) {
                            tooltip.add(Component.literal(" ").append(pan.getDescription()).append(": " + format(table.chance(index, pan.luck()))).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    });
        }
    }

    @Override
    public void draw(PanningTables.Table table, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, tableName(table), 0, 1, TEXT_COLOR, false);
        Component label = biomeLabel(table);
        guiGraphics.drawString(font, label, background.getWidth() - font.width(label), 1, table.lava() ? LAVA_COLOR : LINK_COLOR, false);

        float luck = pans(table).getFirst().luck();
        int shown = Math.min(table.entries().size(), COLUMNS * rows);
        for (int i = 0; i < shown; i++) {
            String text = format(table.chance(i, luck));
            float x = (i % COLUMNS) * CELL_WIDTH + (CELL_WIDTH - font.width(text) * CHANCE_SCALE) / 2.0F;
            float y = HEADER + (i / COLUMNS) * CELL_HEIGHT + 19;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x, y, 0.0F);
            guiGraphics.pose().scale(CHANCE_SCALE, CHANCE_SCALE, 1.0F);
            guiGraphics.drawString(font, text, 0, 0, TEXT_COLOR, false);
            guiGraphics.pose().popPose();
        }
    }

    @Override
    @SuppressWarnings("removal")
    public List<Component> getTooltipStrings(PanningTables.Table table, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        Component label = biomeLabel(table);
        int left = background.getWidth() - font.width(label);
        if (mouseX < left || mouseY < 0 || mouseY > font.lineHeight + 1) return List.of();
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("jei.prospect.panning.found_in"));
        List<Component> names = table.biomes().stream().map(PanningCategory::biomeName).sorted(Comparator.comparing(Component::getString)).toList();
        names.stream().limit(MAX_LISTED_BIOMES).forEach(name -> tooltip.add(name.copy().withStyle(ChatFormatting.GRAY)));
        if (names.size() > MAX_LISTED_BIOMES) {
            tooltip.add(Component.translatable("jei.prospect.panning.more", names.size() - MAX_LISTED_BIOMES).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (table.lava()) {
            tooltip.add(Component.translatable("jei.prospect.panning.lava").withStyle(ChatFormatting.GOLD));
        }
        return tooltip;
    }

    private static Component tableName(PanningTables.Table table) {
        String[] words = table.name().split("_");
        StringBuilder fallback = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!fallback.isEmpty()) fallback.append(' ');
            fallback.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return Component.translatableWithFallback("jei.prospect.panning.table." + table.name(), fallback.toString());
    }

    private static Component biomeLabel(PanningTables.Table table) {
        int count = table.biomes().size();
        if (count == 0) return Component.translatable("jei.prospect.panning.no_biomes");
        return Component.translatable(count == 1 ? "jei.prospect.panning.biome" : "jei.prospect.panning.biomes", count);
    }

    private static Component biomeName(ResourceLocation biome) {
        return Component.translatableWithFallback("biome." + biome.getNamespace() + "." + biome.getPath(), biome.toString());
    }

    private static String format(float chance) {
        float percent = chance * 100.0F;
        if (percent <= 0.0F) return "0%";
        if (percent < 1.0F) return "<1%";
        if (percent < 10.0F) return String.format(Locale.ROOT, "%.1f%%", percent);
        return String.format(Locale.ROOT, "%.0f%%", percent);
    }
}