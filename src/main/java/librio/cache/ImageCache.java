package librio.cache;

import javafx.scene.image.Image;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;


public class ImageCache {
    private static ImageCache instance;
    private final Map<String, Image> cache;

    private ImageCache() {
        this.cache = new LinkedHashMap<>(100, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                return size() > 100;
            }
        };
    }

    public static synchronized ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }

    public Image getImage(String path,String defaultImagePath) {
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

    public void clearCache() {
        cache.clear();
    }

    public int getCacheSize() {
        return cache.size();
    }
}

