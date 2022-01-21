package test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import info.u_team.gltf_minecraft_loader.GlTFModel;
import info.u_team.gltf_parser.GlTFParser;
import info.u_team.gltf_parser.generated.gltf.GlTF;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;

public class GLTFModelParserTest {

	@Test
	public void testLoadingOfGLTF() throws Exception {
		try (final GlTFParser parser = GlTFParser
				.fromJson(ModelResourceLoader.readModel(ModelResourceLoader.SIMPLE_CUBE_JSON))) {
			assertDoesNotThrow(() -> {
				final GlTF gltf = parser.parse();
				final GlTFModel model = new GlTFModel(parser, gltf, null);
				final BakedModel bakedmodel = model.bake(null, null, null, null, null, null);
				final List<BakedQuad> quads = bakedmodel.getQuads(null, null, null, null);
				assertEquals(6, quads.size()); // Test model has 6 quads (36 indices)
				quads.forEach(quad -> {
					quad.getVertices();
				});
			});
		}
	}

}
