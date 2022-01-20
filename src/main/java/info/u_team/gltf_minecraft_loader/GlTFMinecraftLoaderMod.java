package info.u_team.gltf_minecraft_loader;

import info.u_team.gltf_minecraft_loader.api.GlTFApi;
import info.u_team.gltf_minecraft_loader.loader.GlTFLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod(GlTFMinecraftLoaderMod.MODID)
public class GlTFMinecraftLoaderMod {
	
	public static final String MODID = "gltfminecraftloader";
	
	public static GlTFApi API;
	
	public GlTFMinecraftLoaderMod() {
		API = new GlTFApiImpl();
		
		// TODO remove
		ForgeModelBakery.addSpecialModel(new ResourceLocation(MODID, "test"));
		
		Bus.MOD.bus().get().addListener(EventPriority.NORMAL, true, ModelRegistryEvent.class, event -> {
			ModelLoaderRegistry.registerLoader(GlTFLoader.TYPE, GlTFLoader.INSTANCE);
		});
	}
	
}
