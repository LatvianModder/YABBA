package com.latmod.yabba;

import com.feed_the_beast.ftbl.api.FTBLibAPI;
import com.feed_the_beast.ftbl.api.FTBLibPlugin;
import com.feed_the_beast.ftbl.api.IFTBLibClientRegistry;
import com.feed_the_beast.ftbl.api.IFTBLibPlugin;
import com.feed_the_beast.ftbl.api.IFTBLibRegistry;
import com.feed_the_beast.ftbl.api.IRecipes;
import com.latmod.yabba.models.ModelBarrel;
import com.latmod.yabba.util.Barrel;
import com.latmod.yabba.util.EnumUpgrade;
import com.latmod.yabba.util.Tier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Created by LatvianModder on 19.01.2017.
 */
public enum FTBLibIntegration implements IFTBLibPlugin
{
    @FTBLibPlugin
    INSTANCE;

    public static FTBLibAPI API;

    @Override
    public void init(FTBLibAPI api)
    {
        API = api;
    }

    @Override
    public void registerCommon(IFTBLibRegistry reg)
    {
        YabbaConfig.init(reg);
    }

    @Override
    public void registerClient(IFTBLibClientRegistry reg)
    {
        YabbaConfig.initClient(reg);
    }

    @Override
    public void configLoaded(LoaderState.ModState state)
    {
        Barrel.clearCache();
    }

    @Override
    public void registerRecipes(IRecipes recipes)
    {
        ItemStack blankUpgrade = EnumUpgrade.BLANK.item();

        recipes.addRecipe(ItemHandlerHelper.copyStackWithSize(blankUpgrade, YabbaConfig.CRAFTING_UPGRADE_STACK_SIZE.getInt()),
                "SSS", "ICI", "SSS",
                'I', Blocks.IRON_BARS,
                'C', "chestWood",
                'S', "slabWood");

        if(YabbaConfig.CRAFTING_BARREL_EASY_RECIPE.getBoolean())
        {
            recipes.addRecipe(YabbaItems.BARREL.createStack(ModelBarrel.INSTANCE, YabbaRegistry.DEFAULT_SKIN, Tier.WOOD),
                    "U", "C",
                    'U', blankUpgrade,
                    'C', "chestWood");
        }
        else
        {
            recipes.addRecipe(YabbaItems.BARREL.createStack(ModelBarrel.INSTANCE, YabbaRegistry.DEFAULT_SKIN, Tier.WOOD),
                    " U ", "SCS", " P ",
                    'U', blankUpgrade,
                    'C', "chestWood",
                    'S', "slabWood",
                    'P', "plankWood");
        }

        recipes.addRecipe(new ItemStack(YabbaItems.PAINTER),
                "WWU", " I ", " I ",
                'U', blankUpgrade,
                'I', "ingotIron",
                'W', Blocks.WOOL);

        recipes.addRecipe(new ItemStack(YabbaItems.HAMMER),
                "WUW", " I ", " I ",
                'U', blankUpgrade,
                'I', "ingotIron",
                'W', Blocks.WOOL);

        recipes.addRecipe(EnumUpgrade.IRON_UPGRADE.item(),
                "III", "IUI", "III",
                'U', blankUpgrade,
                'I', "ingotIron");

        recipes.addRecipe(EnumUpgrade.GOLD_UPGRADE.item(),
                "III", "IUI", "III",
                'U', blankUpgrade,
                'I', "ingotGold");

        recipes.addRecipe(EnumUpgrade.DIAMOND_UPGRADE.item(),
                "IUI",
                'U', blankUpgrade,
                'I', "gemDiamond");

        recipes.addShapelessRecipe(EnumUpgrade.VOID.item(), blankUpgrade, "dyeBlack");
        recipes.addShapelessRecipe(EnumUpgrade.NETHER_STAR_UPGRADE.item(), blankUpgrade, Items.NETHER_STAR);

        recipes.addRecipe(new ItemStack(YabbaItems.UPGRADE, 1, EnumUpgrade.OBSIDIAN_SHELL.metadata),
                " I ", "IUI", " I ",
                'U', blankUpgrade,
                'I', "obsidian");

        recipes.addShapelessRecipe(new ItemStack(YabbaItems.UPGRADE, 1, EnumUpgrade.REDSTONE_OUT.metadata), blankUpgrade, Items.COMPARATOR);
        recipes.addShapelessRecipe(new ItemStack(YabbaItems.UPGRADE, 1, EnumUpgrade.HOPPER.metadata), blankUpgrade, Blocks.HOPPER);

        recipes.addRecipe(new ItemStack(YabbaItems.ANTIBARREL),
                "NQN", "NON", "NCN",
                'N', "ingotBrickNether",
                'Q', "blockQuartz",
                'O', "obsidian",
                'C', "chestWood");
    }
}