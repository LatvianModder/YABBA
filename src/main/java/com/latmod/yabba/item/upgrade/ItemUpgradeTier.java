package com.latmod.yabba.item.upgrade;

import com.latmod.yabba.api.UpgradeData;
import com.latmod.yabba.block.Tier;
import com.latmod.yabba.tile.Barrel;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public class ItemUpgradeTier extends ItemUpgrade
{
	public class TierUpgradeData extends UpgradeData
	{
		public TierUpgradeData(ItemStack is)
		{
			super(is);
		}

		@Override
		public boolean canInsert(Barrel barrel, EntityPlayerMP player)
		{
			return tier == Tier.STAR || barrel.getTier() == tier.getPrevious();
		}

		@Override
		public void onInserted(Barrel barrel, EntityPlayerMP player)
		{
		}

		@Override
		public boolean canRemove(Barrel barrel, EntityPlayerMP player)
		{
			return true;
		}

		@Override
		public void onRemoved(Barrel barrel, EntityPlayerMP player)
		{
		}
	}

	public final Tier tier;

	public ItemUpgradeTier(Tier t)
	{
		tier = t;
	}

	@Override
	public UpgradeData initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		return new TierUpgradeData(stack);
	}
}