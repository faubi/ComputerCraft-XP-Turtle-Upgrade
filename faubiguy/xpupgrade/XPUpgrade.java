package faubiguy.xpupgrade;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;
import dan200.turtle.api.ITurtleAccess;
import dan200.turtle.api.ITurtlePeripheral;
import dan200.turtle.api.ITurtleUpgrade;
import dan200.turtle.api.TurtleSide;
import dan200.turtle.api.TurtleUpgradeType;
import dan200.turtle.api.TurtleVerb;

public class XPUpgrade implements ITurtleUpgrade {
	
	public static ItemStack craftingItem;

	@Override
	public int getUpgradeID() {
		return 239;
	}

	@Override
	public String getAdjective() {
		return "XP";
	}

	@Override
	public ItemStack getCraftingItem() {
		// TODO Auto-generated method stub
		return new ItemStack(XPUpgradeMain.upgradeItem);
	}

	@Override
	public boolean isSecret() {
		return false;
	}

	@Override
	public String getIconTexture(ITurtleAccess turtle, TurtleSide side) {
		return CommonProxy.TEXTURE_LOCATION;
	}

	@Override
	public int getIconIndex(ITurtleAccess turtle, TurtleSide side) {
		return 0;
	}

	@Override
	public ITurtlePeripheral createPeripheral(ITurtleAccess turtle,
			TurtleSide side) {
		return new XPUpgradePeripheral(turtle);
	}

	@Override
	public boolean useTool(ITurtleAccess turtle, TurtleSide side,
			TurtleVerb verb, int direction) {
		return false;
	}

	@Override
	public TurtleUpgradeType getType() {
		return TurtleUpgradeType.Peripheral;
	}

}
