package com.fototuvastus;

import org.opencv.core.Size;

import java.util.ArrayList;

public class DetectionResponse {
    public int errorCount;
    public Boolean isImageValid;
    public Boolean hasExtraneousBackgroundObjects;
    public Boolean isVerticallyMisplaced;
    public Boolean isHorizontallyMisplaced;
    public Boolean faceTooSmall;
    public Boolean faceTooLarge;
    public Boolean hasRedEye;
    public Boolean hasTeethVisible;
    public Boolean hasEyeXOffset;
    public Boolean hasEyeYOffset;
    public Boolean hasNoseOffset;
    public Boolean hasMouthOffset;
    public Boolean isGrayScale;
    public Size imageSizeCm;
    public Size imageSizePx;
    public ArrayList<FaceFeature> features;

    public DetectionResponse() {
        features = new ArrayList<>();
        errorCount = 0;
        isImageValid = true;
        hasExtraneousBackgroundObjects = false;
        isVerticallyMisplaced = false;
        isHorizontallyMisplaced = false;
        faceTooSmall = false;
        faceTooLarge = false;
        hasRedEye = false;
        hasTeethVisible = false;
        hasEyeXOffset = false;
        hasEyeYOffset = false;
        hasNoseOffset = false;
        hasMouthOffset = false;
        imageSizeCm = null;
        imageSizePx = null;
        isGrayScale = false;
    }
}
