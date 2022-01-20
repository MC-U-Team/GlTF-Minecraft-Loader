package info.u_team.gltf_minecraft_loader;

import java.util.List;
import java.util.Random;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class GlTFBakedModel implements BakedModel {

	private List<BakedQuad> bakedQuads;
	
	public GlTFBakedModel(List<BakedQuad> bakedQuads) {
		this.bakedQuads = bakedQuads;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState pState, Direction pSide, Random pRand) {
		return this.bakedQuads;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return null;
	}

	@Override
	public ItemOverrides getOverrides() {
		return null;
	}

}
