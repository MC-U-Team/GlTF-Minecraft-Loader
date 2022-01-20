package info.u_team.gltf_minecraft_loader;

import info.u_team.gltf_minecraft_loader.api.GlTFApi;
import net.minecraftforge.fml.common.Mod;

@Mod(GlTFMinecraftLoaderMod.MODID)
public class GlTFMinecraftLoaderMod {
	
	public static final String MODID = "gltfminecraftloader";
	
	public static GlTFApi API;
	
	public GlTFMinecraftLoaderMod() {
		API = new GlTFApiImpl();
	}
	
}
