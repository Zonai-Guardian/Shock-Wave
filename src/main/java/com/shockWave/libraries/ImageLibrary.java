package com.shockWave.libraries;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ImageLibrary {
    private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();

    public ImageLibrary() {}
    public void add(String name, BufferedImage image) {
        images.put(name, image);
    }
    public boolean contains(String name) {return images.containsKey(name);}
    public BufferedImage get(String name) {
        if (contains(name) == false) {System.out.println("Could not get image \"" + name + "\" in ImageLibrary.get()! Returning null...");}
        return images.get(name);
    }
}
