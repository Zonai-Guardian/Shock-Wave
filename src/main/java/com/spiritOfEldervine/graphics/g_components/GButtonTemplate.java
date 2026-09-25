package com.spiritOfEldervine.graphics.g_components;

import com.spiritOfEldervine.graphics.g_components.GComponent.ActivationType;

public class GButtonTemplate {
    public String name;
    public String tooltip;
    public ActivationType activationType;
    public String actionValue = null;
    public Runnable actionMethod = null;
    public Runnable updateMethod = null;

    public GButtonTemplate(String name, String tooltip, ActivationType activationType) {
        this.name = name;
        this.tooltip = tooltip;
        this.activationType = activationType;
    }
    public GButtonTemplate(String name, String tooltip, ActivationType activationType, String actionValue) {
        this.name = name;
        this.tooltip = tooltip;
        this.activationType = activationType;
        this.actionValue = actionValue;
    }
    public GButtonTemplate(String name, String tooltip, ActivationType activationType, Runnable actionMethod, Runnable updateMethod) {
        this.name = name;
        this.tooltip = tooltip;
        this.activationType = activationType;
        this.actionMethod = actionMethod;
        this.updateMethod = updateMethod;
    }
    public GButtonTemplate(String name, String tooltip, ActivationType activationType, String actionValue, Runnable actionMethod, Runnable updateMethod) {
        this.name = name;
        this.tooltip = tooltip;
        this.activationType = activationType;
        this.actionValue = actionValue;
        this.actionMethod = actionMethod;
        this.updateMethod = updateMethod;
    }
}
