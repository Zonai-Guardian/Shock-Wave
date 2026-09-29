package com.shockWave;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.shockWave.graphics.GraphicsCalculator;
import com.shockWave.savers.KeyBindings;
import com.shockWave.savers.Settings;

public class FileModifier {
    private static Path projectPath = null;
    //file paths
    public static final String IMAGE_PATH = "/images/";
    public static final String IMAGE_PATH_GUI = "/images/gui/";
    public static final String IMAGE_PATH_ICONS = "/images/gui/icons/";
    public static final String IMAGE_PATH_CHARACTERS = "/images/characters/";
    public static final String IMAGE_PATH_PROPS = "/images/props/";
    public static final String IMAGE_PATH_SPRITE = "/images/sprites/";
    public static final String IMAGE_PATH_BACKGROUNDS = "/images/backgrounds/";
    public static final String IMAGE_PATH_OTHER = "/images/other/";

    public static final String AUDIO_PATH_GUI = "/audio/gui/";
    public static final String AUDIO_PATH_BATTLE = "/audio/battle/";
    public static final String AUDIO_PATH_ENVIRONMENT = "/audio/environment/";

    public FileModifier(){};
    public void init() {
        projectPath = getProjectPath();
        System.out.println("ProjectPath: " + projectPath);
    }
    
    public BufferedImage loadImage(String name, String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(FileModifier.class.getResource(path + name));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Image failed to load \"" + name + "\" with path \"" + path + "\" in FileModifier.loadImage");
            e.printStackTrace(); // If image loading fails
        }
        if (GraphicsCalculator.isImageEmpty(image, 0)) {System.out.println("Image " + name + " is empty immediately after being loaded");}
        return image;
    }
    public void loadImages(Map<String, String> fileAndName, String path) {
        for (Entry<String, String> e : fileAndName.entrySet()) {
            Game.images.add(e.getValue(), loadImage(e.getKey(), path));
        }
    }
    public void loadKeyBindings() {
        //load a new KeyBindings from a file using Jackson, set it to Game.keyBindings
        //or if it fails: create a new KeyBindings with default keybinds and set it to Game.keyBindings

        try {
            loadDataFile();
            //loading data file fails...
        } catch (IOException e) {
            Game.keyBindings = new KeyBindings();
            Game.keyBindings.loadDefaultKeyBinds();
        }
    }
    public void loadSettings() {
        // Replace this with loading settings from a data file soon  #InDev
        Settings settings = new Settings();
        settings.init();

        Game.settings = settings;
    }
    
    public void confirmDataFolderStructure() {

    }
    public void confirmFolder(Path path) {

    }
    private void loadDataFile() throws IOException {
        //load file from game_data folder using the project path

        //Game.keyBindings.init(); // Use Later

        throw new IOException("TEST Fialed to load data file from [path]"); // #InDev
    }
    private Path getProjectPath() {
        try {
            Path path = Paths.get(
                FileModifier.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
            );
            if (path.toString().endsWith(File.separator + "targer" + File.separator + "classes") ||
                path.toString().endsWith(File.separator + "targer" + File.separator + "classes")) {
                return path.getParent().getParent();
            }
            if (path.toString().endsWith(".jar")) {
                return path.getParent();
            }
            return path.toAbsolutePath().getParent();
        } catch(URISyntaxException | NullPointerException e) {
            return Paths.get("").toAbsolutePath();
        }
    }
    /*
    public Animation loadAnimation(int tileWidth, int tileHeight, String spriteName, String animationName, int frameLength) {
        Animation anim = new Animation(animationName);
        BufferedImage sprite = loadImage(spriteName);
        if (sprite == null) {
            System.out.println("sprite '" + spriteName + ".png' is null!");
        } else if (sprite.getWidth() % tileWidth != 0 || sprite.getHeight() % tileHeight != 0) {
            System.out.println("sprite '" + spriteName + ".png' (Size X: " + sprite.getWidth() + " Y: " + sprite.getHeight() + ") is not in sections of " + tileWidth + " x " + tileHeight + "!");
        } else {
            for (int yi = 0; yi <= sprite.getHeight() - tileHeight; yi += tileWidth) {
                for (int xi = 0; xi <= sprite.getWidth() - tileWidth; xi += tileWidth) {
                    anim.frames.add(new AnimationFrame(sprite.getSubimage(xi, yi, tileWidth, tileHeight), frameLength));
                }
            }
        }
        return anim;
    }
    public Animation loadFlatAnimation(String spriteName, String animationName, int frameLength, boolean reverseFrames, boolean mirrorFramesX, boolean mirrorFramesY, boolean loops) {
        Animation anim = new Animation(animationName.toLowerCase(), loops);
        BufferedImage sprite = null;
        try {
            sprite = ImageIO.read(getClass().getResource("resources/images/" + spriteName + ".png"));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace(); // If image loading fails
            System.out.println("Image failed to load: " + spriteName + ".png");
        }
        if (sprite == null) {
            System.out.println("sprite '" + spriteName + ".png' is null!");
        } else if (sprite.getWidth() % sprite.getHeight() != 0) {
            System.out.println("sprite '" + spriteName + ".png' (Size X: " + sprite.getWidth() + " Y: " + sprite.getHeight() + ") is not in sections of " + sprite.getHeight() + " x " + sprite.getHeight() + "!");
        } else {
            for (int xi = 0; xi <= sprite.getWidth() - sprite.getHeight(); xi += sprite.getHeight()) {
                BufferedImage pendingImage = sprite.getSubimage(xi, 0, sprite.getHeight(), sprite.getHeight());
                if (mirrorFramesX || mirrorFramesY) {
                    anim.frames.add(new AnimationFrame(PointCalculator.mirrorImage(pendingImage, mirrorFramesX, mirrorFramesY), frameLength));
                } else {
                    anim.frames.add(new AnimationFrame(pendingImage, frameLength));
                }
            }
            if (reverseFrames) {
                anim.reverseFrames();
            }
        }
        return anim;
    }
    public BufferedImage loadScaledImage(int x, int y, String name) {
        BufferedImage usableImage = null;
        try {
            BufferedImage tempImage = ImageIO.read(getClass().getResource("resources/images/" + name + ".png"));
            usableImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d1 = usableImage.createGraphics();
            g2d1.drawImage(tempImage, 0, 0, x, y, null);
            g2d1.dispose();
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace(); // If image loading fails
            System.out.println("Image failed to load: " + name + ".png");
        }
        return usableImage;
    }
        
    public World loadWorld(String name) {
        //don't use Gson! Use Jackson instead!
        //Gson gson = new Gson();
        return null;
    }
        */
    public Clip loadClip(String fileName) {
        String fileType = ".wav";
        Clip clip = null;
        try {
            AudioInputStream getAudioStream = AudioSystem.getAudioInputStream(new File("resources/audio/" + fileName + fileType));
            clip = AudioSystem.getClip();
            clip.open(getAudioStream);
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Couldn't load audio file: " + fileName + fileType);
            e.printStackTrace();
        }
        if (clip == null) {
            System.out.println("Loading audio file: " + fileName + fileType + " returned null!");
        }
        return clip;
    }
}
