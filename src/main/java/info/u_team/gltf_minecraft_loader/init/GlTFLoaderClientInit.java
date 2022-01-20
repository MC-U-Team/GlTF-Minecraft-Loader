package info.u_team.gltf_minecraft_loader.init;

import info.u_team.gltf_minecraft_loader.GlTFLoaderMod;
import info.u_team.gltf_minecraft_loader.loader.GlTFLoader;
import info.u_team.gltf_minecraft_loader.resource_pack.GlTFResourcesResourcePack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class GlTFLoaderClientInit {
	
	private static void onModelRegistry(ModelRegistryEvent event) {
		ModelLoaderRegistry.registerLoader(GlTFLoader.TYPE, GlTFLoader.INSTANCE);
	}
	
	private static void onAddPackFinders(AddPackFindersEvent event) {
		event.addRepositorySource((consumer, factory) -> {
			consumer.accept(Pack.create("GlTF Resources", true, GlTFResourcesResourcePack::new, factory, Position.TOP, PackSource.BUILT_IN));
		});
	}
	
	public static void init() {
		final IEventBus bus = Bus.MOD.bus().get();
		
		bus.addListener(GlTFLoaderClientInit::onModelRegistry);
		bus.addListener(GlTFLoaderClientInit::onAddPackFinders);
		
		// TODO remove, just tests
		ForgeModelBakery.addSpecialModel(new ResourceLocation(GlTFLoaderMod.MODID, "test"));
	}
	
}
