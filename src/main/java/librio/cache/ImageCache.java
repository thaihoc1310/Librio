package librio.cache;

import javafx.scene.image.Image;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * The ImageCache class is a singleton that provides a caching mechanism for images.
 * It uses a least-recently-used (LRU) strategy to manage its entries, ensuring efficient memory usage.
 */
public class ImageCache {
    private static ImageCache instance;
    private final Map<String, Image> cache;

    /**
     * Private constructor to initialize the image cache as a least-recently-used (LRU) cache.
     * The cache has an initial capacity of 1000 entries, a load factor of 0.75, and is access-ordered.
     * When the cache size exceeds 1000, the eldest entry is removed automatically.
     */
    private ImageCache() {
        this.cache = new LinkedHashMap<>(1000, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                return size() > 1000;
            }
        };
    }

    /**
     * Provides access to the singleton instance of the ImageCache class.
     *
     * @return the singleton instance of the ImageCache class
     */
    public static synchronized ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    /**
     * Retrieves an image from the specified path. If the image is not found in the
     * cache or on the file system, a default image is returned.
     *
     * @param path the path to the desired image
     * @param defaultImagePath the path to the default image to be used if the desired image is unavailable
     * @return the image object corresponding to the specified path or the default image if the specified path is not available
     */
    public Image getImage(String path, String defaultImagePath) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        File file = new File(path);
        if (file.exists()) {
            Image image = new Image(file.toURI().toString());
            cache.put(path, image);
            return image;
        }

        Image defaultImage = new Image(new File(defaultImagePath).toURI().toString());
        cache.put(path, defaultImage);
        return defaultImage;
    }

    /**
     * Clears all entries in the image cache.
     * <p>
     * This method empties the cache, effectively removing all stored images and freeing up memory.
     * It can be useful in scenarios where the cache needs to be reset, such as when logging out
     * a user or when significant changes to the cache content are required.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Returns the current size of the image cache.
     *
     * @return the number of entries currently stored in the image cache
     */
    public int getCacheSize() {
        return cache.size();
    }
}

