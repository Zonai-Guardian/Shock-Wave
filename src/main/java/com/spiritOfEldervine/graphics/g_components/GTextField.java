package com.spiritOfEldervine.graphics.g_components;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.graphics.RenderEngine;
import com.spiritOfEldervine.libraries.ConstantLibrary;

public class GTextField extends GComponent {
    public String text = "";
    public String exampleText;
    private ArrayList<String> acceptedCharacters = new ArrayList<>();
    private int maxLength;
    private boolean doSpacesCoundAsChars = false;

    public GTextField(String exampleText, String acceptedCharacters, int maxLength, boolean doSpacesCoundAsChars) {
        this.exampleText = exampleText;
        if (acceptedCharacters.length() > 0) {
            this.acceptedCharacters = new ArrayList<String>(Arrays.asList(acceptedCharacters.split("")));
        }
        this.maxLength = maxLength;
        this.doSpacesCoundAsChars = doSpacesCoundAsChars;
    }

    // Getters
    public String getText() {return text;}
    public boolean isTargeted() {return Game.menuManager.textManager.getTargetID() != null && Game.menuManager.textManager.getTargetID() == id;}
    public boolean isTextSelected() {return Game.menuManager.textManager.isTextSelected();}
    public String getTextBeforeSelection() {return Game.menuManager.textManager.getTextBeforeSelection();}
    public String getTextInSelection() {return Game.menuManager.textManager.getTextInSelection();}
    public String getTextAfterSelection() {return Game.menuManager.textManager.getTextAfterSelection();}

    // Setters
    public boolean setText(String newText) {
        if (isLengthAcceptable(newText) == false) {return false;} // Return if the text is over the character limit
        
        for (int i = 0; i < newText.length(); i++) {
            if (isCharacterAccepted("" + newText.charAt(i)) == false) {
                return false;
            }
        }
        text = newText;
        return true;
    }

    // Others
    
    private boolean isLengthAcceptable(String text) {
        if (maxLength == -1) {
            return true;
        } else {
            String filteredText = text;
            if (doSpacesCoundAsChars == false) {
                filteredText = text.split(" ").toString();
            }
            return filteredText.length() <= maxLength;
        }
    }
    public boolean isCharacterAccepted(String character) {
        if (character.isEmpty()) {return true;} // character is nothing, no more calculation needed.
        if (acceptedCharacters.size() == 0 || acceptedCharacters.contains(character)) {
            return true;
        }
        return false;
    }

    private void render(Point offset, boolean shouldRenderDebug, Graphics2D g) {
        boolean isTargeted = isTargeted();
        g.setFont(ConstantLibrary.GUI.FONT);

        // Assign Colors
        Color centerColor = null;
        Color borderColor = null;
        Color textColor = null;
        Color selectionColor = null;
        Color exampleTextColor = null;

        if (isTargeted == false) {
            switch (componentStyle) {
                case SIMPLE:
                    centerColor =  ConstantLibrary.GUI.MenuComponent.STANDARD_CENTER;
                    textColor =  ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    borderColor = ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    exampleTextColor = ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    break;
                case SETTINGS:
                    centerColor =  ConstantLibrary.GUI.SettingsComponent.STANDARD_CENTER;
                    textColor =  ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    borderColor = ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    exampleTextColor = ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    break;
            }
        } else {
            switch (componentStyle) {
                case SIMPLE:
                    centerColor =  ConstantLibrary.GUI.MenuComponent.SELECTED_CENTER;
                    textColor =  ConstantLibrary.GUI.MenuComponent.SELECTED_TEXT;
                    borderColor = ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    exampleTextColor = ConstantLibrary.GUI.MenuComponent.STANDARD_TEXT;
                    break;
                case SETTINGS:
                    centerColor =  ConstantLibrary.GUI.SettingsComponent.SELECTED_CENTER;
                    textColor =  ConstantLibrary.GUI.SettingsComponent.SELECTED_TEXT;
                    borderColor = ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    exampleTextColor = ConstantLibrary.GUI.SettingsComponent.STANDARD_TEXT;
                    break;
            }
        }
        selectionColor = new Color(255 - textColor.getRed(), 255 - textColor.getGreen(), 255 - textColor.getBlue());
        
        // Render Center
        g.setColor(centerColor); // Use the text color to make sure the border is visable
        g.drawRoundRect(rect.x + offset.x, rect.y + offset.y, rect.width, rect.height, ConstantLibrary.GUI.BORDER_ROUNDED_SIZE, ConstantLibrary.GUI.BORDER_ROUNDED_SIZE);
        
        // Render Border
        g.setColor(borderColor); // Use the text color to make sure the border is visable
        int borderSize = ConstantLibrary.GUI.BORDER_SIZE;
        RenderEngine.setGraphicsStrokeWidth(borderSize, g);
        g.drawRoundRect(rect.x + offset.x + borderSize, rect.y + offset.y + borderSize, rect.width - borderSize * 2, rect.height - borderSize * 2, ConstantLibrary.GUI.BORDER_ROUNDED_SIZE, ConstantLibrary.GUI.BORDER_ROUNDED_SIZE);
        RenderEngine.setGraphicsStrokeWidth(1, g);

        // Calculate Positions
        Rectangle textRect = RenderEngine.getRectAroundText(rect, new Point(20, 5), text.isEmpty() ? exampleText : text, displayTextDirection, shouldRenderDebug, g);
        FontMetrics fm = g.getFontMetrics();
        Point origin = new Point(textRect.x, textRect.y + (textRect.height + fm.getAscent()) / 2);
        int xOffset = 0;

        // Render Text
        if (isTargeted()) {
            //Rectangle selection = 
            String textBefore = Game.menuManager.textManager.getTextBeforeSelection();
            String textIn = Game.menuManager.textManager.getTextInSelection();
            String textAfter = Game.menuManager.textManager.getTextAfterSelection();

            if (isTextSelected() == false && Game.menuManager.textManager.cursorAnimationFrame < Game.menuManager.textManager.cursorAnimationLength / 2) {
                int beforeOffset = (textBefore != null && textBefore.isEmpty() == false) ? fm.stringWidth(textBefore) : 0;
                
                g.setColor(Color.WHITE);
                g.fillRect(origin.x + xOffset + offset.x + beforeOffset - 2, origin.y + offset.y - fm.getAscent() + fm.getLeading() / 2, 4, fm.getHeight());
            }
            if (text.isEmpty()) {
                g.setColor(exampleTextColor);
                g.drawString(exampleText, origin.x + offset.x, origin.y + offset.y);
                return;
            }

            if (textBefore != null && textBefore.isEmpty() == false) {
                g.setColor(textColor);
                g.drawString(textBefore, origin.x + xOffset + offset.x, origin.y + offset.y);
                xOffset += fm.stringWidth(textBefore);
            }
            if (isTextSelected()) {
                g.setColor(textColor);
                g.fillRect(origin.x + xOffset + offset.x, origin.y + offset.y - fm.getAscent() + fm.getLeading() / 2, fm.stringWidth(textIn), fm.getHeight());
                g.setColor(selectionColor);
                g.drawString(textIn, origin.x + xOffset + offset.x, origin.y + offset.y);
                xOffset += fm.stringWidth(textIn);
            } else {
            }
            if (textAfter != null && textAfter.isBlank() == false) {
                g.setColor(textColor);
                g.drawString(textAfter, origin.x + xOffset + offset.x, origin.y + offset.y);
            }
        } else {
            if (text.isEmpty()) {
                g.setColor(exampleTextColor);
                g.drawString(exampleText, origin.x + offset.x, origin.y + offset.y);
                return;
            }
            g.setColor(textColor);
            g.drawString(text, origin.x + offset.x, origin.y + offset.y);
        }
    }
    
    @Override
    public void render(Point offset, Graphics2D g) {
        render(offset, false, g);
    }
    @Override
    public void renderDebug(Point offset, Graphics2D g) {
        render(offset, true, g);
    }
}
