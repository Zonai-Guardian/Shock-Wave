package com.spiritOfEldervine.graphics;

import java.awt.Point;

import com.spiritOfEldervine.Game;
import com.spiritOfEldervine.Game.GameState;
import com.spiritOfEldervine.engine.EngineCalculator;
import com.spiritOfEldervine.graphics.g_components.GComponent;
import com.spiritOfEldervine.graphics.g_components.GTextField;
import com.spiritOfEldervine.savers.Settings.DebugEnum;

public class TextManager {
    private int cursorPosition = 0;
    private Point selectionPositions = null;
    private Integer targetedTextFieldID = null; // null means nothing is selected
    private boolean selectingInLeftDirection = false;

    public int cursorAnimationFrame = 0;
    public final int cursorAnimationLength = EngineCalculator.secondsToFrames(1.5);

    public TextManager() {}

    // Updaters
    public void updateTimers() {
        cursorAnimationFrame++;
        if (cursorAnimationFrame >= cursorAnimationLength) {cursorAnimationFrame = 0;}
    }
    public void retargetTextField(Integer targetID) {
        targetedTextFieldID = targetID;
        if (isTextFieldTargeted() == false) {
            // Don't do anything else if targetID is not valid.
            targetedTextFieldID = null;
            return;
        }

        cursorPosition = getTextLength(); // Set the cursor to the very right side of the text
        selectionPositions = null; // Nothing should be selected
        cursorAnimationFrame = 0;
    }

    // Getters
    public int getCursorPosition() {return cursorPosition;}
    public Point getSelectionPositions() {return selectionPositions;}
    public Integer getTargetID() {return targetedTextFieldID;}

    // Handlers
    public void handleKey(String text) {
        // The absurd amount of print statements are for debug purposes and should be removed when typing works decently well.

        System.out.println("1 Text: \"" + text + "\"");
        if (Game.gameState == GameState.LOADING || Game.gameState == GameState.STARTUP) {return;} // Don't handle keys if the game is still loading
        System.out.println("2 Text: \"" + text + "\"");
        if (text.isEmpty() || text == null) {return;} // Don't handle key if there is no text
        System.out.println("3 Text: \"" + text + "\"");
        if (EngineCalculator.hasSpecialCodes(text) && text.equals("\\") == false) {return;} // Don't handle key if there are any special codes inside
        
        System.out.println("4 Text: \"" + text + "\"");
        System.out.println("Key Typed! Text: \"" + text + "\""); // Print the text

        if (true) { // isTextFieldTargeted()
            switch (text) {
                case "copy":
                    EngineCalculator.setClipboardText(getTextInSelection());
                    break;
                case "paste":
                    setText(getTextBeforeSelection() + EngineCalculator.getClipboardText(), getTextAfterSelection());
                    break;
                case "backspace":
                    backspaceText(Game.keyboard.heldKeys.contains("ctrl"));
                    break;
                case "left":
                    selectLeft();
                    break;
                case "right":
                    selectRight();
                    break;
                default:
                    setText(getTextBeforeSelection() + text, getTextAfterSelection()); // Automatically deletes the selected text
                    break;
            }
        }
    }

    // Checkers Methods
    //  Checker methods check things like is a text field is targeted, if the text is empty, and if the text is blank
    //  They will all be private and none will be static
    private boolean isTextFieldTargeted() {
        if (targetedTextFieldID == null) {return false;}
        GComponent gcomp = Game.menuManager.getMenu(Game.menuManager.getActiveMenuName()).getGComponentByID(targetedTextFieldID);
        if (gcomp != null && gcomp instanceof GTextField) {
            // The id links to an existing GComponent that is in the active menu and the GComponent is a GTextField 
            return true;
        }
        return false;
    }
    private boolean isTextEmpty() {
        if (isTextFieldTargeted() == false) {return true;}
        return getText().isEmpty();
    }
    private boolean isTextBlank() {
        if (isTextFieldTargeted() == false) {return true;}
        return getText().isBlank();
    }
    public boolean isTextSelected() {
        return selectionPositions != null;
    }
    

    // Helper Methods
    //  None of the helper methods will check that a text field is targeted. They will rely entirely on more primary methods like the Handlers for that)
    //  They will all be private and none will be static
    
    private String getText() {
        if (Game.settings.isDebugActive(DebugEnum.TYPING_TEST)) {
            return Game.word;
        } else {
            GComponent gcomp = Game.menuManager.getMenu(Game.menuManager.getActiveMenuName()).getGComponentByID(targetedTextFieldID);
            if (gcomp instanceof GTextField textField) {
                return textField.getText();
            } else {
                System.out.println("Could not find GComponent with class GTextField in the active menu in TextManager.getText()! ID: " + targetedTextFieldID + ", Class: " + gcomp.getClass());
                EngineCalculator.printStackTrace();
                return null;
            }
        }
    }
    private void setText(String beforeText, String afterText) {
        boolean shouldUpdateCursorPosition = true;
        if (Game.settings.isDebugActive(DebugEnum.TYPING_TEST)) {
            Game.word = beforeText + afterText;
        } else {
            GComponent gcomp = Game.menuManager.getMenu(Game.menuManager.getActiveMenuName()).getGComponentByID(targetedTextFieldID);
            if (gcomp instanceof GTextField textField) {
                shouldUpdateCursorPosition = textField.setText(beforeText + afterText);
            } else {
                System.out.println("Could not find GComponent with class GTextField in the active menu in TextManager.getText()! ID: " + targetedTextFieldID + ", Class: " + gcomp.getClass());
                EngineCalculator.printStackTrace();
            }
        }
        if (shouldUpdateCursorPosition) {
            cursorPosition = Math.min(beforeText.length(), getTextLength());
            selectionPositions = null;
            cursorAnimationFrame = 0;
        }
    }
    /*
    private String getText() {
    }
    private void setText(String newText) {
    }
        */
    public String getTextBeforeSelection() {
        if (isTextSelected()) {
            // Text is selected
            return getText().substring(0, selectionPositions.x);
        } else {
            // Text is not selected
            return getText().substring(0, cursorPosition);
        }
    }
    public String getTextInSelection() {
        if (isTextSelected()) {
            // Text is selected
            return getText().substring(selectionPositions.x, selectionPositions.y);
        } else {
            // Text is not selected
            return "";
        }
    }
    private int getTextLength() {return getText().length();}
    public String getTextAfterSelection() {
        String fieldText = getText();
        int endIndex = fieldText.length();
        
        if (isTextSelected()) {
            // Text is selected
            return fieldText.substring(selectionPositions.y, endIndex);
        } else {
            // Text is not selected
            return fieldText.substring(cursorPosition, endIndex);
        }
    }
    private String getCharacter(int index) {
        return getText().substring(index, index + 1);
    }
    private void deleteSelectedText() {
        setText(getTextBeforeSelection(), getTextAfterSelection());
    }

    private void selectLeft() {
        if (Game.keyboard.heldKeys.contains("shift")) {
            if (isTextSelected()) {
                if (selectingInLeftDirection) {
                    selectionPositions.x = Math.max(0, selectionPositions.x - 1);
                } else {
                    selectionPositions.y = Math.max(0, selectionPositions.y - 1);
                }
            } else {
                selectionPositions = new Point(Math.max(0, cursorPosition - 1), cursorPosition);
                selectingInLeftDirection = true;
            }
        } else {
            if (isTextSelected()) {
                cursorPosition = selectionPositions.x;
                selectionPositions = null;
            } else {
                cursorPosition = Math.max(0, cursorPosition - 1);
            }
        }
        if (selectionPositions != null) {
            if (selectionPositions.x == selectionPositions.y) {
                cursorPosition = selectionPositions.x;
                selectionPositions = null;
            } else if (selectionPositions.y < selectionPositions.x) {
                selectionPositions = new Point(selectionPositions.y, selectionPositions.x);
            }
        }
    }
    private void selectRight() {
        if (Game.keyboard.heldKeys.contains("shift")) {
            if (isTextSelected()) {
                if (selectingInLeftDirection) {
                    selectionPositions.x = Math.min(getTextLength(), selectionPositions.x + 1);
                } else {
                    selectionPositions.y = Math.min(getTextLength(), selectionPositions.y + 1);
                }
            } else {
                selectionPositions = new Point(Math.max(0, cursorPosition + 1), cursorPosition);
                selectingInLeftDirection = false;
            }
        } else {
            if (isTextSelected()) {
                cursorPosition = selectionPositions.y;
                selectionPositions = null;
            } else {
                cursorPosition = Math.min(getTextLength(), cursorPosition + 1);
            }
        }
        if (selectionPositions != null) {
            if (selectionPositions.x == selectionPositions.y) {
                cursorPosition = selectionPositions.x;
                selectionPositions = null;
            } else if (selectionPositions.y < selectionPositions.x) {
                selectionPositions = new Point(selectionPositions.y, selectionPositions.x);
            }
        }
    }

    private void backspaceText(boolean backspaceEntireWord) {
        if (getTextLength() == 0) {return;} // Do not backspace it there is no text
        if (cursorPosition == 0 && isTextSelected() == false) {return;} // Do not backspace it the cursor is before all characters (also prevents a negative cursor position)
        
        if (isTextSelected()) {
            setText(getTextBeforeSelection(), getTextAfterSelection());
        } else {
            if (backspaceEntireWord == false) {
                // Only backspace one character
                String before = getTextBeforeSelection();
                setText(before.substring(0, before.length() - 1), getTextAfterSelection());
            } else {
                if (cursorPosition <= 1) {
                    backspaceText(false);
                    return;
                }
                // Backspace the whole word
                String allChars = getTextBeforeSelection();
                // These are not acrually the first and second chars, but the second-last and first-last.
                String firstChar = allChars.substring(allChars.length() - 1).toLowerCase();
                if (firstChar.toUpperCase().equals(firstChar)) {
                    firstChar = allChars.substring(allChars.length() - 2, allChars.length() - 1).toLowerCase();
                }
                
                boolean isCharSpace = firstChar.equals(" ");
                boolean isCharLetter = firstChar.toUpperCase().equals(firstChar) == false;
                boolean isCharDiget = !firstChar.isEmpty() && Character.isDigit(firstChar.charAt(0));
                
                System.out.println("1: cursor: " + cursorPosition + ", selection: " + selectionPositions);
                selectionPositions = new Point(cursorPosition - 2, cursorPosition);
                System.out.println("2: cursor: " + cursorPosition + ", selection: " + selectionPositions);

                while (true) {
                    if (selectionPositions.x <= 0) {
                        selectionPositions.x = 0;
                        System.out.println("Loop Broken Because selectionPosition was 0 or less!!!!!");
                        break;
                    }
                    int startIndex = selectionPositions.x;
                    int endIndex = selectionPositions.x + 1;
                    System.out.println("Indexes: startIndex: " + startIndex + ", endIndex: " + endIndex);
                    String nextChar = allChars.substring(startIndex, endIndex).toLowerCase();
                    boolean isNextCharSpace = nextChar.equals(" ");
                    boolean isNextCharLetter = nextChar.toUpperCase().equals(nextChar) == false;
                    boolean isNextCharDiget = !nextChar.isEmpty() && Character.isDigit(nextChar.charAt(0));

                    System.out.println("Booleans: isNextCharDiget: " + isNextCharDiget + ", isNextCharLetter: " + isNextCharLetter + ", isCharLetter: " + isCharLetter + ", isNextCharSpace: " + isNextCharSpace + ", isCharSpace: " + isCharSpace + ", firstChar: " + firstChar + ", nextChar: " + nextChar);
                    if (isNextCharLetter == isCharLetter && isNextCharDiget == isCharDiget && isNextCharSpace == isCharSpace) {
                        selectionPositions.x--;
                    } else {
                        System.out.println("Last!!!: isNextCharLetter: " + isNextCharLetter + ", isCharLetter: " + isCharLetter + ", isNextCharSpace: " + isNextCharSpace + ", isCharSpace: " + isCharSpace + ", firstChar: " + firstChar + ", nextChar: " + nextChar);
                        selectionPositions.x++;
                        break;
                    }
                }
                System.out.println("3: cursor: " + cursorPosition + ", selection: " + selectionPositions);
                deleteSelectedText();
                System.out.println("4: cursor: " + cursorPosition + ", selection: " + selectionPositions);
            }
        }
    }
    private void deleteText(boolean backspaceEntireWord) {
        if (getTextLength() == 0) {return;} // Do not backspace it there is no text
        if (cursorPosition == 0) {return;} // Do not backspace it the cursor is before all characters (also prevents a negative cursor position)
        
        if (isTextSelected()) {
            setText(getTextBeforeSelection(), getTextAfterSelection());
        } else {
            if (backspaceEntireWord == false) {
                // Only backspace one character
                String before = getTextBeforeSelection();
                setText(before.substring(0, before.length() - 1), getTextAfterSelection());
            } else {
                if (cursorPosition <= 1) {
                    backspaceText(false);
                    return;
                }
                // Backspace the whole word
                String allChars = getTextBeforeSelection();
                // These are not acrually the first and second chars, but the second-last and first-last.
                String firstChar = allChars.substring(allChars.length() - 1).toLowerCase();
                if (firstChar.toUpperCase().equals(firstChar)) {
                    firstChar = allChars.substring(allChars.length() - 2, allChars.length() - 1).toLowerCase();
                }
                
                boolean isCharSpace = firstChar.equals(" ");
                boolean isCharLetter = firstChar.toUpperCase().equals(firstChar) == false;
                boolean isCharDiget = !firstChar.isEmpty() && Character.isDigit(firstChar.charAt(0));
                
                System.out.println("1: cursor: " + cursorPosition + ", selection: " + selectionPositions);
                selectionPositions = new Point(cursorPosition - 2, cursorPosition);
                System.out.println("2: cursor: " + cursorPosition + ", selection: " + selectionPositions);

                while (true) {
                    if (selectionPositions.x <= 0) {
                        selectionPositions.x = 0;
                        System.out.println("Loop Broken Because selectionPosition was 0 or less!!!!!");
                        break;
                    }
                    int startIndex = selectionPositions.x;
                    int endIndex = selectionPositions.x + 1;
                    System.out.println("Indexes: startIndex: " + startIndex + ", endIndex: " + endIndex);
                    String nextChar = allChars.substring(startIndex, endIndex).toLowerCase();
                    boolean isNextCharSpace = nextChar.equals(" ");
                    boolean isNextCharLetter = nextChar.toUpperCase().equals(nextChar) == false;
                    boolean isNextCharDiget = !nextChar.isEmpty() && Character.isDigit(nextChar.charAt(0));

                    System.out.println("Booleans: isNextCharDiget: " + isNextCharDiget + ", isNextCharLetter: " + isNextCharLetter + ", isCharLetter: " + isCharLetter + ", isNextCharSpace: " + isNextCharSpace + ", isCharSpace: " + isCharSpace + ", firstChar: " + firstChar + ", nextChar: " + nextChar);
                    if (isNextCharLetter == isCharLetter && isNextCharDiget == isCharDiget && isNextCharSpace == isCharSpace) {
                        selectionPositions.x--;
                    } else {
                        System.out.println("Last!!!: isNextCharLetter: " + isNextCharLetter + ", isCharLetter: " + isCharLetter + ", isNextCharSpace: " + isNextCharSpace + ", isCharSpace: " + isCharSpace + ", firstChar: " + firstChar + ", nextChar: " + nextChar);
                        selectionPositions.x++;
                        break;
                    }
                }
                System.out.println("3: cursor: " + cursorPosition + ", selection: " + selectionPositions);
                deleteSelectedText();
                System.out.println("4: cursor: " + cursorPosition + ", selection: " + selectionPositions);
            }
        }
    }
    /*
    private void backspaceText(boolean backspaceEntireWord) {
        if (getTextLength() == 0) {return;} // Do not backspace it there is no text
        if (cursorPosition == 0) {return;} // Do not backspace it the cursor is before all characters (also prevents a negative cursor position)
        
        if (isTextSelected()) {
            setText(getTextBeforeSelection(), getTextAfterSelection());
        } else {
            if (backspaceEntireWord == false) {
                // Only backspace one character
                System.out.println("Before: " + getTextBeforeSelection() + ", " + getTextInSelection() + ", " + getTextAfterSelection() + ", Position: " + cursorPosition);
                String before = getTextBeforeSelection();
                setText(before.substring(0, before.length() - 1), getTextAfterSelection());
                System.out.println("After: " + getTextBeforeSelection() + ", " + getTextInSelection() + ", " + getTextAfterSelection() + ", Position: " + cursorPosition);
            } else {
                // Backspace the whole word
                String firstCharacter = getTextBeforeSelection().substring(getTextBeforeSelection().length() - 1).toLowerCase();
                System.out.println("First: " + firstCharacter); // "First: 5"
                boolean isFirstCharacterLetter = firstCharacter.equals(firstCharacter.toUpperCase()) == false; // If character is letter firstCharacter and firstCharacter.toUpperCase() will be different.
                boolean isFirstCharacterSpace = firstCharacter.equals(" ");
                boolean hasDeletedCharacters = false;
                while (true) {
                    if (cursorPosition == 0) {break;}
                    String nextCharacter = getTextBeforeSelection().substring(getTextBeforeSelection().length() - 1).toLowerCase();
                    boolean isNextCharacterLetter = nextCharacter.equals(nextCharacter.toUpperCase()) == false;
                    if (cursorPosition == 0 || isFirstCharacterLetter != isNextCharacterLetter || (nextCharacter.equals(" ") && !isFirstCharacterSpace && hasDeletedCharacters)) {break;}
                    backspaceText(false); // delete letter
                    if (hasDeletedCharacters == false) {
                        hasDeletedCharacters = true;
                        backspaceText(false);
                    }
                }
            }
        }
    }
    */
}
