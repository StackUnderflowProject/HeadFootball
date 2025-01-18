package si.um.feri.project.map.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;

public class ImageUtils {
    public static Texture fetchTextureFromUrl(String urlString) {
        InputStream input = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(urlString).toURL().openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();

            // Read the input stream into a byte array
            byte[] bytes = StreamUtils.copyStreamToByteArray(input);

            // Create a Texture from the byte array
            return new Texture(new Pixmap(bytes, 0, bytes.length));
        } catch (Exception e) {
            Gdx.app.log("Team Image", "Failed to fetch image: " + e.getMessage());
            return null;
        } finally {
            StreamUtils.closeQuietly(input);
        }
    }
}
