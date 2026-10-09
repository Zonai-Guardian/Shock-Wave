package com.shockWave.graphics.g_components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import com.shockWave.Game;
import com.shockWave.Game.Direction8;
import com.shockWave.engine.EngineCalculator;
import com.shockWave.graphics.RenderEngine;
import com.shockWave.libraries.ConstantLibrary;

import aurelienribon.tweenengine.TweenEquations;

public class GToggle extends GComponent {
    public boolean isEnabled = false;
    public Point position;

    private boolean isAnimationActive = false;
    private int maxAnimationFrame = 30;
    private int animationFrame = 0;
    
    //constructors
    public GToggle() {}

    @Override
    public final void updateComponent() {
        if (isAnimationActive) {
            animationFrame++;
            if (animationFrame > maxAnimationFrame) {
                isAnimationActive = false;
            }
        }
    }
    private void resetAnimation() {
        animationFrame = 0;
        isAnimationActive = true;
    }

    @Override
    public void activateComponent() {
        isEnabled = !isEnabled;
        resetAnimation();
        toggleComponent(isEnabled);
    }
    public void toggleComponent(boolean isNowEnabled) {
        // For Overriding
    }


    @Override
    public void updateTick() {
        // Update Animations
    }

    @Override
    public void render(Point offset, Graphics2D g) {
        // Offset is applied right here on toggle rect:
        Rectangle toggleRect = new Rectangle(rect.x + rect.width - rect.height * 2 + offset.x, rect.y + offset.y, rect.height * 2, rect.height);

        // Calculate Toggle Center Offset:
        double animationMultiplier = EngineCalculator.tweenValue(Math.min(1.0, Math.max(0.0, ((double)animationFrame / (double)maxAnimationFrame))), TweenEquations.easeInOutQuad);
        Point startEndForActiveAnimation = isEnabled ? new Point(0, toggleRect.height) : new Point(toggleRect.height, 0);
        int offsetForInactiveAnimation = isEnabled ? toggleRect.height : 0;
        int toggleCenterOffset = (int)(!isAnimationActive ? offsetForInactiveAnimation : (startEndForActiveAnimation.x + (startEndForActiveAnimation.y - startEndForActiveAnimation.x) * animationMultiplier));

        Rectangle toggleCenterRect = new Rectangle(
            (int)(toggleRect.x + toggleCenterOffset),
            toggleRect.y,
            toggleRect.height, // This is SUPPOSED to be the height even though it is being given for the width of the new rect.
            toggleRect.height
        );
        Color centerColor = null;
        Color borderColor = null;
        Color textColor = null;
        int red = (int)(isAnimationActive ? 
            (isEnabled ? (1 - animationMultiplier) * 255 : animationMultiplier * 255) :
            (isEnabled ? 0 : 255)
        );
        int green = (int)(isAnimationActive ?
            (!isEnabled ? (1 - animationMultiplier) * 255 : animationMultiplier * 255) :
            (isEnabled ? 255 : 0)
        );
        Color enabledColor = new Color(red, green, 0);

        int borderSize = ConstantLibrary.GUI.BORDER_SIZE;
        int roundedSize = rect.height;

        // Get the correct colors for the button
        if (selected) {
            switch(componentStyle) {
                case SIMPLE:
                    centerColor = ConstantLibrary.GUI.MenuComponent.SELECTED_CENTER;
                    borderColor = ConstantLibrary.GUI.MenuComponent.SELECTED_BORDER;
                    textColor = ConstantLibrary.GUI.MenuComponent.SELECTED_TEXT;
                    break;
                case SETTINGS:
                    centerColor = ConstantLibrary.GUI.SettingsComponent.SELECTED_CENTER;
                    borderColor = ConstantLibrary.GUI.SettingsComponent.SELECTED_BORDER;
                    textColor = ConstantLibrary.GUI.SettingsComponent.SELECTED_TEXT;
                    break;
            }
        } else { // Not Selected
            switch(componentStyle) {
                case SIMPLE:
                    centerColor = ConstantLibrary.GUI.MenuComponent.STANDARD_CENTER;
                    borderColor = ConstantLibrary.GUI.MenuComponent.STANDARD_BORDER;
                    textColor = ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    break;
                case SETTINGS:
                    centerColor = ConstantLibrary.GUI.SettingsComponent.STANDARD_CENTER;
                    borderColor = ConstantLibrary.GUI.SettingsComponent.STANDARD_BORDER;
                    textColor = ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    break;
            }
        }
        
        // Actually start rendering

        // Render Text on left side
        g.setColor(textColor);
        g.setFont(ConstantLibrary.GUI.GBUTTON_FONT);
        RenderEngine.drawTextInRect(new Rectangle(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height), new Point(), displayText, Direction8.LEFT_CENTER, false, g);

        // Toggle on right side

        // Toggle Outside-Center
        g.setColor(centerColor);
        g.fillRoundRect(toggleRect.x + borderSize / 2, toggleRect.y + borderSize / 2, toggleRect.width - borderSize, toggleRect.height - borderSize, roundedSize, roundedSize);
        
        // Toggle Outside-Border
        g.setColor(borderColor);
        RenderEngine.setGraphicsStrokeWidth(borderSize, g); // Set Stroke Width
        g.drawRoundRect(toggleRect.x + borderSize / 2, toggleRect.y + borderSize / 2, toggleRect.width - borderSize, toggleRect.height - borderSize, roundedSize, roundedSize);
        RenderEngine.setGraphicsStrokeWidth(1, g); // Reset Stroke Width to defaut (1)
        // Rendering the border by drawing the rect with a wider stroke instead of filling the rect lets the border be opaque but the center be transparrent

        // Toggle Center

        // Toggle Center-Center
        g.setColor(enabledColor);
        g.fillRoundRect(toggleCenterRect.x + borderSize / 2, toggleCenterRect.y + borderSize / 2, toggleCenterRect.width - borderSize, toggleCenterRect.height - borderSize, toggleCenterRect.height, toggleCenterRect.height);
        
        // Toggle Center-Border
        g.setColor(borderColor);
        RenderEngine.setGraphicsStrokeWidth(borderSize, g); // Set Stroke Width
        g.drawRoundRect(toggleCenterRect.x + borderSize / 2, toggleCenterRect.y + borderSize / 2, toggleCenterRect.width - borderSize, toggleCenterRect.height - borderSize, toggleCenterRect.height, toggleCenterRect.height);
        RenderEngine.setGraphicsStrokeWidth(1, g); // Reset Stroke Width to defaut (1)
        // Rendering the border by drawing the rect with a wider stroke instead of filling the rect lets the border be opaque but the center be transparrent

    }
}
