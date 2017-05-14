package com.fototuvastus;

public interface Constants {

    boolean USE_MCS_CASCADES = true;

    //Pixel to centimeter ratio
    double PIXEL_TO_CM = 0.0264583;

    //Feature colors
    double[] COLOR_FACE = new double[]{255, 255, 255, 255};
    double[] COLOR_EYES = new double[]{206, 154, 0, 255};
    double[] COLOR_NOSE = new double[]{0, 0, 255, 0};
    double[] COLOR_MOUTH = new double[]{246, 246, 12, 0};

    //Feature search areas(% of face area)
    //Eyes
    float SEARCH_AREA_EYES_START_X = .0f;
    float SEARCH_AREA_EYES_END_X = 1.0f;
    float SEARCH_AREA_EYES_START_Y = .0f;
    float SEARCH_AREA_EYES_END_Y = .6f;

    //Nose
    float SEARCH_AREA_NOSE_START_X = .2f;
    float SEARCH_AREA_NOSE_END_X = .8f;
    float SEARCH_AREA_NOSE_START_Y = .4f;
    float SEARCH_AREA_NOSE_END_Y = .75f;

    //Mouth
    float SEARCH_AREA_MOUTH_START_X = .2f;
    float SEARCH_AREA_MOUTH_END_X = .8f;
    float SEARCH_AREA_MOUTH_START_Y = .7f;
    float SEARCH_AREA_MOUTH_END_Y = 1.0f;


    //Feature sizes(% of face size)
    //Face
    double SIZE_FACE_MIN_HEIGHT = 40;
    double SIZE_FACE_MAX_HEIGHT = 55;

    //Eyes
    float SIZE_EYES_MIN_WIDTH = .15f;
    float SIZE_EYES_MIN_HEIGHT = .1f;
    float SIZE_EYES_MAX_WIDTH = .3f;
    float SIZE_EYES_MAX_HEIGHT = .3f;

    //Nose
    float SIZE_NOSE_MIN_WIDTH = .1f;
    float SIZE_NOSE_MIN_HEIGHT = .1f;
    float SIZE_NOSE_MAX_WIDTH = .4f;
    float SIZE_NOSE_MAX_HEIGHT = .4f;

    //Mouth
    float SIZE_MOUTH_MIN_WIDTH = .3f;
    float SIZE_MOUTH_MIN_HEIGHT = .1f;
    float SIZE_MOUTH_MAX_WIDTH = .5f;
    float SIZE_MOUTH_MAX_HEIGHT = .3f;

    //Offsets
    float OFFSET_EYES_YAXIS_MAX = 15.0f;
    float OFFSET_EYES_XAXIS_MAX = 15.0f;
    float OFFSET_MOUTH_XAXIS_MAX = 10.0f;
    float OFFSET_NOSE_XAXIS_MAX = 10.0f;

}
