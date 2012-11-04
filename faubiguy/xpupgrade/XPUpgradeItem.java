package faubiguy.xpupgrade;

import faubiguy.xpupgrade.CommonProxy;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class XPUpgradeItem extends Item {

	public XPUpgradeItem(int id) {
		this(id, 64, CreativeTabs.tabMisc, 0, "Unnamed");
	}
	
	public XPUpgradeItem(int id, int maxStackSize, CreativeTabs tab, int texture, String name) {
		super(id);
		setMaxStackSize(maxStackSize);
		setCreativeTab(tab);
		setIconIndex(texture);
		setItemName(name);
		LanguageRegistry.addName(this, name);
	}
	
	@Override
	public String getTextureFile() {
		return CommonProxy.TEXTURE_LOCATION;
	}


}
