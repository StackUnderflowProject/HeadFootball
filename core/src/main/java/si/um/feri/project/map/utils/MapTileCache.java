package si.um.feri.project.map.utils;

import com.badlogic.gdx.graphics.Texture;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapTileCache {
    private static final int MAX_CACHE_SIZE = 100; // Adjust based on memory constraints

    private static final Map<String, Texture> cache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Texture> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                eldest.getValue().dispose(); // Clean up the texture to prevent memory leaks
            }
            return shouldRemove;
        }
    };

    public static Texture getCachedTile(int zoom, int x, int y) throws IOException {
        String key = zoom + "/" + x + "/" + y;
        Texture cachedTexture = cache.get(key);

        if (cachedTexture != null && cachedTexture.isManaged()) {
            return cachedTexture;
        }

        // If not in cache or disposed, fetch new tile
        Texture newTexture = MapRasterTiles.getRasterTile(zoom, x, y);
        cache.put(key, newTexture);
        return newTexture;
    }

    public static Texture getCachedTile(ZoomXY zoomXY) throws IOException {
        return getCachedTile(zoomXY.zoom, zoomXY.x, zoomXY.y);
    }

    public static void clearCache() {
        for (Texture texture : cache.values()) {
            texture.dispose();
        }
        cache.clear();
    }
}
