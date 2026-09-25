package com.spiritOfEldervine.graphics;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.Game.FitTypes;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MenuBackgroundManager {
    public ArrayList<MenuBackground> backgrounds = new ArrayList<>(); //([name of background image])
    public int activeBackgroundIndex = 0;
    public Integer previousBackgroundIndex = 0;
    public int backgroundFrame = 0;

    //manual creation:
    public MenuBackgroundManager() {}
    public void add(MenuBackground background) {
        backgrounds.add(background);
    }

    //automatic creation:
    public MenuBackgroundManager(String[] imageNames, int length, int fadeOutTime, FitTypes fitType) {
        for (String name : imageNames) {
            backgrounds.add(new MenuBackground(name, length, fadeOutTime, fitType));
        }
    }

    public void update() {
        backgroundFrame++;
        //check if the background should change
        if (backgroundFrame > backgrounds.get(activeBackgroundIndex).length) {
            //go to next background
            activeBackgroundIndex = activeBackgroundIndex + 1 >= backgrounds.size() ? 0 : activeBackgroundIndex + 1;
            previousBackgroundIndex = activeBackgroundIndex == 0 ? backgrounds.size() - 1 : activeBackgroundIndex - 1;
            backgroundFrame = 0;
        }
    }
    public void render(Dimension windowSize, Graphics g) {
        BufferedImage image = Game.images.get(backgrounds.get(activeBackgroundIndex).imageName);
        BufferedImage fadingImage = null;
        boolean renderFadingBackground = backgroundFrame <= backgrounds.get(previousBackgroundIndex).fadeOutTime;

        //for later
        Double alphaMultiplier = null;

        if (renderFadingBackground) {
            fadingImage = Game.images.get(backgrounds.get(previousBackgroundIndex).imageName);
            if (fadingImage == null) {
                System.out.println("fadingImage is null in MenuBackgroundManager.render()");
                Game.exitGame(1);
            }

            alphaMultiplier = 1.0 - ((double) backgroundFrame / backgrounds.get(previousBackgroundIndex).fadeOutTime);
        }

        renderBackground(windowSize, image, backgrounds.get(activeBackgroundIndex).fitType, g);
        if (renderFadingBackground) {
            Graphics2D g2d = (Graphics2D) g;
            RenderEngine.setGraphicsAlpha(alphaMultiplier, g);
            renderBackground(windowSize, fadingImage, backgrounds.get(previousBackgroundIndex).fitType, g2d);
            RenderEngine.setGraphicsAlpha(1.0, g);
        }
        //draw values on screen:
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
        g.setColor(Color.WHITE);
        g.drawString("BackgroundFrame: " + backgroundFrame, 200, 50);
    }
    private void renderBackground(Dimension windowSize, BufferedImage image, FitTypes fit, Graphics g) {
        //render image differently depending on what fit type it uses

        //calculate scaling
        double x = (double) windowSize.width / image.getWidth();
        double y = (double) windowSize.height / image.getHeight();
        double sX = 1.0; //scale X
        double sY = 1.0; //scale Y
        double scale;
        Rectangle drawSize;

        switch(fit) {
            case FIT:
                scale = Math.min(x, y);
                sX = scale;
                sY = scale;
                break;
            case FILL:
                scale = Math.max(x, y);
                sX = scale;
                sY = scale;
                break;
            case STRETCH:
                sX = x;
                sY = y;
                break;
            case CENTER:
                //Keep scaling at 1.0
                break;
        }
        double width = image.getWidth() * sX;
        double height = image.getHeight() * sY;


        drawSize = new Rectangle(0, 0, (int)(width), (int)(height));
        drawSize.x = (int)((windowSize.width - width) / 2);
        drawSize.y = (int)((windowSize.height - height) / 2);
        
        //actually render the image
        g.drawImage(image, drawSize.x, drawSize.y, drawSize.width, drawSize.height, null);
    }
}
