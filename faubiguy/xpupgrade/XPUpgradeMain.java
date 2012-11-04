package faubiguy.xpupgrade;

import java.util.ArrayList;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.IRecipe;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import dan200.turtle.api.TurtleAPI;

@Mod (modid = "XPUpgrade", name = "XP Turtle Upgrade", version = "1.0")
@NetworkMod (clientSideRequired = true, serverSideRequired = false)
public class XPUpgradeMain {
	public static Item upgradeItem;
	public static int upgradeItemId;

	@Instance("XPUpgrade")
	public static XPUpgradeMain instance;
	
	@SidedProxy (clientSide = "faubiguy.xpupgrade.client.ClientProxy", serverSide = "faubiguy.xpupgrade.CommonProxy")	
	public static CommonProxy proxy;
	public static boolean setLevelEnabled = false;
	
	@PreInit
	public void Pre(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		upgradeItemId = config.getItem("XP Upgrade Item ID", 13540).getInt();
		setLevelEnabled = config.get("General", "setLevel Enabled", false).getBoolean(false);
		config.save();
	}
	@Init
	public void load(FMLInitializationEvent event) {
		upgradeItem = new XPUpgradeItem(upgradeItemId, 64, CreativeTabs.tabRedstone, 0, "XP Upgrade");
		TurtleAPI.registerUpgrade(new XPUpgrade());
		for(Item craftingItem : new Item[] {Item.ingotGold, Item.expBottle}) {
			GameRegistry.addRecipe(new ItemStack(upgradeItem), new Object[] {"SSS", "SXS", "SSS", 'S', Block.stone, 'X', craftingItem});
		}
	}
}
