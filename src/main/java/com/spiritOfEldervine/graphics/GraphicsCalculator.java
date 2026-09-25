package com.spiritOfEldervine.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.Game.FitTypes;
import com.spiritOfEldervine.engine.EngineCalculator;

public class GraphicsCalculator {
    // This file is for modifying images, calculating graphics-related calculations, etc.
    public GraphicsCalculator() {}
    
    public static void setDisplayScaleAndOffset(Dimension window, Dimension gameResolution, FitTypes gameFitType) {
        double dx = window.width / gameResolution.getWidth(); //double x
        double dy = window.height / gameResolution.getHeight(); //double y
        double scaleX = 1.0;
        double scaleY = 1.0;
        double scale = 1.0; //for less repeditive code

        switch(gameFitType) {
            case FIT:
                scale = Math.min(dx, dy);
                scaleX = scale;
                scaleY = scale;
                break;
            case FILL:
                scale = Math.max(dx, dy);
                scaleX = scale;
                scaleY = scale;
                break;
            case CENTER:
                //keep scaling at 1.0
                break;
            case STRETCH:
                scaleX = dx;
                scaleY = dy;
        }

        int offsetX = (int)((window.width - gameResolution.width * scaleX) / 2);
        int offsetY = (int)((window.height - gameResolution.height * scaleY) / 2);

        Game.displayOffset = new Point(offsetX, offsetY);
        Game.displayScale = new Point2D.Double(scaleX, scaleY);
    }
    
    //image minipulation:
    public static BufferedImage scaleImageAlpha(BufferedImage originalImage, double alphaMultiplier) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = (Graphics2D) newImage.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alphaMultiplier));
        g.drawImage(originalImage, 0, 0, width, height, null);
        
        return newImage;
    }
    public static BufferedImage lowerImageAlphaTo(BufferedImage image, int alpha) {
        image = getImageCopy(image);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int pixelAlpha = (argb >> 24) & 0xFF;
                Color c = new Color(argb);
                int newAlpha = pixelAlpha < alpha ? pixelAlpha : alpha;
                if (newAlpha != 0) {
                    System.out.println("newAlpha: " + newAlpha + ", alpha: " + alpha + ", pixelAlpha: " + pixelAlpha);
                }
                image.setRGB(x, y, new Color(c.getRed(), c.getGreen(), c.getBlue(), newAlpha).getRGB());
            }
        }
        return image;
    }
    public static BufferedImage capImageAlpha(BufferedImage image, int maxAlpha) {
        image = getImageCopy(image);

        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int arbg = image.getRGB(x, y);
                int alpha = (arbg >>> 24);
                if (alpha > maxAlpha) {
                    arbg = (maxAlpha << 24) | (arbg & 0x00FFFFFF);
                    image.setRGB(x, y, arbg);
                }
            }
        }
        return image;
    }
    public static BufferedImage setImageAlpha(BufferedImage image, int alpha) {
        image = getImageCopy(image);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color c = new Color(image.getRGB(x, y));
                image.setRGB(x, y, new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha).getRGB());
            }
        }
        return image;
    }
    
    public static BufferedImage changeImageColors(Map<Color, Color> colorMap, String imageName) {
        if (colorMap == null || imageName == null || Game.images.contains(imageName) == false) {return null;}

        BufferedImage img = getImageCopy(Game.images.get(imageName));

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (colorMap.containsKey(new Color(img.getRGB(x, y)))) {
                    img.setRGB(x, y, colorMap.get(new Color(img.getRGB(x, y))).getRGB());
                }
            }
        }
        return img;
    }
    public static BufferedImage changeImageColors(Map<Color, Color> colorMap, BufferedImage image) {
        if (colorMap == null || image == null) {return null;}

        BufferedImage img = getImageCopy(image);
        
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (colorMap.containsKey(new Color(img.getRGB(x, y)))) {
                    img.setRGB(x, y, colorMap.get(new Color(img.getRGB(x, y))).getRGB());
                }
            }
        }
        return img;
    }
    public static BufferedImage makeImageWhite(String imageName, int lowestAlpha) {
        if (imageName == null || Game.images.contains(imageName) == false) {return null;}

        BufferedImage img = getImageCopy(Game.images.get(imageName));

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alpha = new Color(img.getRGB(x, y)).getAlpha();
                if (alpha >= lowestAlpha) {
                    img.setRGB(x, y, new Color(255, 255, 255, 255).getRGB());
                }
            }
        }
        return img;
    }
    public static BufferedImage deleteRandomPixelsFromImage(BufferedImage originalImage, int deletePixelsQuantity, int lowestDeleteAlpha) {
        BufferedImage img = getImageCopy(originalImage);

        for (int i = 0; i < deletePixelsQuantity; i++) {
            if (isImageEmpty(img, 0)) {return img;}
            int x = EngineCalculator.randomInt(img.getWidth());
            int y = EngineCalculator.randomInt(img.getHeight());
            if (new Color(img.getRGB(x, y)).getAlpha() < lowestDeleteAlpha) {
                i--;
                continue;
            } else {
                img.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
            }
        }
        return img;
    }
    public static boolean isImageEmpty(BufferedImage img, int lowestAlpha) {
        if (img == null) {System.out.println("img is null in GraphicsCalculator.isImageEmpty()"); return true;}
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int alpha = (img.getRGB(x, y) >> 24) & 0xff;
                if (alpha > lowestAlpha) {return false;}
            }
        }
        return true;
    }
    
    public static BufferedImage getImageCopy(BufferedImage img) {
        if (img == null) {return null;}
        
        BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return copy;
    }

    //old
    public static int getStringWidth(String string, FontMetrics fm) {
        return fm.stringWidth(string);
    }
    public static int getStringHeight(String string, FontMetrics fm, boolean onlyAssent) {
        return fm.getAscent() + (onlyAssent ? 0 : fm.getLeading());
    }

}
