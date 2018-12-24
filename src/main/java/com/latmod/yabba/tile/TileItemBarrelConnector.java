package com.latmod.yabba.tile;

import com.feed_the_beast.ftblib.lib.tile.EnumSaveType;
import com.feed_the_beast.ftblib.lib.tile.TileBase;
import com.latmod.yabba.YabbaConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author LatvianModder
 */
public class TileItemBarrelConnector extends TileBase implements IItemHandler
{
	private static final HashSet<TileItemBarrelConnector> ALL_CONNECTORS = new HashSet<>();

	private static BlockPos currentPos = null;
	private static final Comparator<ItemBarrel> BARREL_COMPARATOR = (o1, o2) -> {
		int i = Boolean.compare(o1.type.isEmpty(), o2.type.isEmpty());
		i = i == 0 ? Boolean.compare(o2.barrel.isLocked(), o1.barrel.isLocked()) : i;
		return i == 0 ? Double.compare(currentPos.distanceSq(((TileEntity) o1.barrel.block).getPos()), currentPos.distanceSq(((TileEntity) o2.barrel.block).getPos())) : i;
	};

	public static void markAllDirty(BlockPos pos, int dimension)
	{
		Iterator<TileItemBarrelConnector> iterator = ALL_CONNECTORS.iterator();

		while (iterator.hasNext())
		{
			TileItemBarrelConnector connector = iterator.next();

			if (connector.isInvalid())
			{
				iterator.remove();
			}
			else if (connector.world != null && dimension == connector.world.provider.getDimension() && connector.pos.distanceSq(pos) <= 65536D)
			{
				connector.lastUpdate = 0L;
			}
		}
	}

	public final List<ItemBarrel> linkedBarrels = new ArrayList<>();
	private long lastUpdate = 0L;

	@Override
	protected void writeData(NBTTagCompound nbt, EnumSaveType type)
	{
	}

	@Override
	protected void readData(NBTTagCompound nbt, EnumSaveType type)
	{
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
	{
		return (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) ? (T) this : super.getCapability(capability, facing);
	}

	@Override
	public void validate()
	{
		super.validate();
		ALL_CONNECTORS.add(this);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		ALL_CONNECTORS.remove(this);
	}

	@Override
	public void markDirty()
	{
		sendDirtyUpdate();
	}

	@Override
	public boolean notifyBlock()
	{
		return false;
	}

	private void addToList(HashSet<ItemBarrel> scanned, BlockPos pos, EnumFacing from)
	{
		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, from))
		{
			IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, from);

			if (itemHandler instanceof ItemBarrel && scanned.add((ItemBarrel) itemHandler))
			{
				for (EnumFacing facing1 : EnumFacing.VALUES)
				{
					if (facing1 != from)
					{
						addToList(scanned, pos.offset(facing1), facing1.getOpposite());
					}
				}
			}
		}
	}

	@Nullable
	private ItemBarrel getAt(int slot)
	{
		if (world == null || world.isRemote)
		{
			return null;
		}
		else if (world.getTotalWorldTime() - lastUpdate >= YabbaConfig.general.connector_update_ticks)
		{
			lastUpdate = world.getTotalWorldTime();
			linkedBarrels.clear();

			if (world != null && !world.isRemote)
			{
				HashSet<ItemBarrel> scanned = new HashSet<>();

				for (EnumFacing facing : EnumFacing.VALUES)
				{
					addToList(scanned, pos.offset(facing), facing.getOpposite());
				}

				linkedBarrels.addAll(scanned);
				currentPos = pos;
				linkedBarrels.sort(BARREL_COMPARATOR);
			}
		}

		if (slot <= 0 || slot > linkedBarrels.size())
		{
			return null;
		}

		ItemBarrel barrel = linkedBarrels.get(slot - 1);
		return barrel == null || barrel.barrel.block.isBarrelInvalid() ? null : barrel;
	}

	@Override
	public int getSlots()
	{
		getAt(0);
		return 1 + linkedBarrels.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		ItemBarrel barrel = getAt(slot);
		return barrel == null ? ItemStack.EMPTY : barrel.getStackInSlot(1);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		if (slot == 0)
		{
			return false;
		}

		getAt(0);

		for (ItemBarrel barrel : linkedBarrels)
		{
			if (barrel.isItemValid(0, stack))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}
		else if (slot != 0)
		{
			return stack;
		}

		getAt(0);

		for (ItemBarrel barrel : linkedBarrels)
		{
			if (!barrel.isEmpty())
			{
				stack = barrel.insertItem(0, stack, simulate);

				if (stack.isEmpty())
				{
					return ItemStack.EMPTY;
				}
			}
		}

		for (ItemBarrel barrel : linkedBarrels)
		{
			if (barrel.isEmpty())
			{
				stack = barrel.insertItem(0, stack, simulate);

				if (stack.isEmpty())
				{
					return ItemStack.EMPTY;
				}
			}
		}

		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (slot == 0)
		{
			return ItemStack.EMPTY;
		}

		ItemBarrel barrel = getAt(slot);
		return barrel == null ? ItemStack.EMPTY : barrel.extractItem(1, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return Integer.MAX_VALUE;
	}
}