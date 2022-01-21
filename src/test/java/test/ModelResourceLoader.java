package test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ModelResourceLoader {
	
	public static URL SIMPLE_CUBE_JSON = getModel("simple_cube_json.gltf");
	public static URL SIMPLE_CUBE_BIN = getModel("simple_cube_json.glb");

	private static URL getModel(String modelName) {
		return ModelResourceLoader.class.getClassLoader().getResource("./test_models/" + modelName);
	}
	
	public static byte[] readModel(URL url) throws IOException {
		try (final InputStream inputStream = url.openStream(); final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();) {
			int read;
			final byte[] buffer = new byte[16384];
			
			while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
				outputStream.write(buffer, 0, read);
			}
			return outputStream.toByteArray();
		}
	}
	
}
