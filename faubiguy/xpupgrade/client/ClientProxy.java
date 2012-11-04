package faubiguy.xpupgrade.client;

import net.minecraftforge.client.MinecraftForgeClient;
import faubiguy.xpupgrade.CommonProxy;

public class ClientProxy extends CommonProxy{
	@Override
	public void registerRenderers() {
		MinecraftForgeClient.preloadTexture(TEXTURE_LOCATION);
	}
}
