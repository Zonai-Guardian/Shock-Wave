package com.shockWave.graphics;

import com.shockWave.Game.FitTypes;

public class MenuBackground {
    public String imageName;
    public int length; //in frames
    public int fadeOutTime; //in frames
    public FitTypes fitType;

    public MenuBackground(String imageName, int length, int fadeOutTime, FitTypes fitType) {
        this.imageName = imageName;
        this.length = length;
        this.fadeOutTime = fadeOutTime;
        this.fitType = fitType;
    }
}