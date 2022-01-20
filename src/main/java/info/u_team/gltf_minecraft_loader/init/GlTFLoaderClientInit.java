package info.u_team.gltf_minecraft_loader.init;

import info.u_team.gltf_minecraft_loader.GlTFLoaderMod;
import info.u_team.gltf_minecraft_loader.loader.GlTFLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class GlTFLoaderClientInit {
	
	private static void onModelRegistry(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(GlTFLoader.TYPE, GlTFLoader.INSTANCE);
	}
	
	public static void init() {
		final IEventBus bus = Bus.MOD.bus().get();
		
		bus.addListener(GlTFLoaderClientInit::onModelRegistry);
		
		// TODO remove, just tests
		ForgeModelBakery.addSpecialModel(new ResourceLocation(GlTFLoaderMod.MODID, "test"));
	}
	
}
