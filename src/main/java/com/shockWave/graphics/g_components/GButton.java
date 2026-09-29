package com.shockWave.graphics.g_components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import com.shockWave.graphics.RenderEngine;
import com.shockWave.libraries.ConstantLibrary;

public class GButton extends GComponent {

    // Constructors
    public GButton() {}
    

    @Override
    public void render(Point offset, Graphics2D g) {
        Color center = null;
        Color border = null;
        Color text = null;
        int borderSize = ConstantLibrary.GUI.BORDER_SIZE;
        int roundedSize = ConstantLibrary.GUI.BORDER_ROUNDED_SIZE;

        // Get the correct colors for the button
        if (selected) {
            switch(componentStyle) {
                case SIMPLE:
                    center = ConstantLibrary.GUI.MenuComponent.SELECTED_CENTER;
                    border = ConstantLibrary.GUI.MenuComponent.SELECTED_BORDER;
                    text = ConstantLibrary.GUI.MenuComponent.SELECTED_TEXT;
                    break;
                case SETTINGS:
                    center = ConstantLibrary.GUI.SettingsComponent.SELECTED_CENTER;
                    border = ConstantLibrary.GUI.SettingsComponent.SELECTED_BORDER;
                    text = ConstantLibrary.GUI.SettingsComponent.SELECTED_TEXT;
                    break;
            }
        } else { // Not Selected
            switch(componentStyle) {
                case SIMPLE:
                    center = ConstantLibrary.GUI.MenuComponent.STANDARD_CENTER;
                    border = ConstantLibrary.GUI.MenuComponent.STANDARD_BORDER;
                    text = ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    break;
                case SETTINGS:
                    center = ConstantLibrary.GUI.SettingsComponent.STANDARD_CENTER;
                    border = ConstantLibrary.GUI.SettingsComponent.STANDARD_BORDER;
                    text = ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    break;
            }
        }
        
        // Actually start rendering
        // Border
        g.setColor(border);
        RenderEngine.setGraphicsStrokeWidth(borderSize, g); // Set Stroke Width
        g.drawRoundRect(rect.x + offset.x + borderSize / 2, rect.y + offset.y + borderSize / 2, rect.width - borderSize, rect.height - borderSize, roundedSize, roundedSize);
        RenderEngine.setGraphicsStrokeWidth(1, g); // Reset Stroke Width to defaut (1)
        // Center
        g.setColor(center);
        g.fillRoundRect(rect.x + borderSize + offset.x, rect.y + borderSize + offset.y, rect.width - borderSize * 2, rect.height - borderSize * 2, roundedSize, roundedSize);
        // Text in Center
        g.setColor(text);
        RenderEngine.drawTextInRect(new Rectangle(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height), new Point(), displayText, displayTextDirection, false, g);
    }
    @Override
    public void renderDebug(Point offset, Graphics2D g) {
        Color center = null;
        Color border = null;
        Color text = null;
        int borderSize = ConstantLibrary.GUI.BORDER_SIZE;
        int roundedSize = ConstantLibrary.GUI.BORDER_ROUNDED_SIZE;

        // Get the correct colors for the button
        if (selected) {
            switch(componentStyle) {
                case SIMPLE:
                    center = ConstantLibrary.GUI.MenuComponent.SELECTED_CENTER;
                    border = ConstantLibrary.GUI.MenuComponent.SELECTED_BORDER;
                    text = ConstantLibrary.GUI.MenuComponent.SELECTED_TEXT;
                    break;
                case SETTINGS:
                    center = ConstantLibrary.GUI.SettingsComponent.SELECTED_CENTER;
                    border = ConstantLibrary.GUI.SettingsComponent.SELECTED_BORDER;
                    text = ConstantLibrary.GUI.SettingsComponent.SELECTED_TEXT;
                    break;
            }
        } else { // Not Selected
            switch(componentStyle) {
                case SIMPLE:
                    center = ConstantLibrary.GUI.MenuComponent.STANDARD_CENTER;
                    border = ConstantLibrary.GUI.MenuComponent.STANDARD_BORDER;
                    text = ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    break;
                case SETTINGS:
                    center = ConstantLibrary.GUI.SettingsComponent.STANDARD_CENTER;
                    border = ConstantLibrary.GUI.SettingsComponent.STANDARD_BORDER;
                    text = ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    break;
            }
        }
        
        // Actually start rendering
        // Text in Center
        g.setColor(text);
        RenderEngine.drawTextInRect(new Rectangle(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height), new Point(), displayText, displayTextDirection, true, g);
    }
    
    /*
    @Override
    public GComponent getCopy() {
        // Create GButton
        GButton b = new GButton();

        // GComponent Vars
        b.rect = rect;
        b.displayText = displayText;
        b.tooltip = tooltip;
        b.displayTextDirection = displayTextDirection;
        b.componentStyle = componentStyle;
        b.activationType = activationType;
        b.animationDirection = animationDirection;
        b.updateMethod = updateMethod;
        b.actionMethod = actionMethod;
        b.actionValue = actionValue;
        b.dataArray = dataArray;
        b.selected = selected;
        b.scrolls = scrolls;

        // GButton Vars
        
        return b;
    }
    */
}
