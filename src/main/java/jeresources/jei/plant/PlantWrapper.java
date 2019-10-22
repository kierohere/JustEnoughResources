package jeresources.jei.plant;

import jeresources.api.drop.PlantDrop;
import jeresources.compatibility.CompatBase;
import jeresources.entry.PlantEntry;
import jeresources.util.RenderHelper;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class PlantWrapper implements IRecipeCategoryExtension, ITooltipCallback<ItemStack> {
    private final PlantEntry plantEntry;

    public PlantWrapper(PlantEntry entry) {
        plantEntry = entry;
    }

    @Override
    public void setIngredients(@Nonnull IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, plantEntry.getPlantItemStack());
        ingredients.setOutputs(VanillaTypes.ITEM, plantEntry.getLootDropStacks());
    }

    @Override
    public void drawInfo(int recipeWidth, int recipeHeight, double mouseX, double mouseY) {
        RenderHelper.renderBlock(getFarmland(), 26, 50, -10, 20F, 0.4F);
        RenderHelper.renderBlock(getBlockState(), 26, 32, 10, 20F, 0.4F);
    }

    @Override
    public void onTooltip(int slotIndex, boolean input, ItemStack ingredient, List<String> tooltip) {
        if (!input)
            tooltip.add(getChanceString(ingredient));
    }

    public float getChance(ItemStack itemStack) {
        PlantDrop drop = this.plantEntry.getDrop(itemStack);
        switch (drop.getDropKind()) {
            case chance:
                return drop.getChance();
            case weight:
                return (float) drop.getWeight() / this.plantEntry.getTotalWeight();
            case minMax:
                return Float.NaN;
            default:
                return 0;
        }
    }

    public int[] getMinMax(ItemStack itemStack) {
        PlantDrop drop = this.plantEntry.getDrop(itemStack);
        return new int[]{drop.getMinDrop(), drop.getMaxDrop()};
    }

    private String getChanceString(ItemStack itemStack) {
        float chance = getChance(itemStack);
        String toPrint;
        if (Float.isNaN(chance)) {
            int[] minMax = this.getMinMax(itemStack);
            toPrint = minMax[0] + (minMax[0] == minMax[1] ? "" : " - " + minMax[1]);
        } else {
            toPrint = String.format("%2.2f", chance * 100).replace(",", ".") + "%";
        }
        return toPrint;
    }

    private BlockState state;
    private long timer = -1;
    private static final int TICKS = 500; // .5s

    private BlockState getBlockState() {
        if (this.plantEntry.getPlant() != null) {
            if (timer == -1) timer = System.currentTimeMillis() + TICKS;
            if (this.state == null) {
                this.state = this.plantEntry.getPlant().getPlant(CompatBase.getWorld(), BlockPos.ZERO);
            }
            if (System.currentTimeMillis() > timer) {
                this.state = this.state.cycle(CropsBlock.AGE);
                this.timer = System.currentTimeMillis() + TICKS;
            }
            return this.state;
        } else {
            // return Block.getBlockFromItem(this.plantEntry.getPlantItemStack().getItem()).getStateFromMeta(this.plantEntry.getPlantItemStack().getItemDamage());
            return Block.getBlockFromItem(this.plantEntry.getPlantItemStack().getItem()).getDefaultState();
        }
    }

    private BlockState getFarmland() {
        if (plantEntry.getSoil() != null) {
            return plantEntry.getSoil();
        }

        return Blocks.FARMLAND.getDefaultState();
    }
}
