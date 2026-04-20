package com.example.businessidea.Module;

public class Category {
    private String name;
    private boolean isSelected;

    public Category(String name) {
        this.name = name;
        this.isSelected = false; // Default: not selected
    }

    public String getName() { return name; }
    public boolean isSelected() { return isSelected; }

    public void setSelected(boolean selected) { this.isSelected = selected; }
}
