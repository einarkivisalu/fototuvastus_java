package com.fototuvastus;


import static org.junit.Assert.assertEquals;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;

public class DetectionTests {

    Detection detection = new Detection();

    @BeforeClass
    public static void setup() throws IOException {
        Utils.loadOpenCV();
    }

    @Test
    public void testFace() throws IOException, JSONException {

        HashMap<String, Integer> expected = new HashMap<String, Integer>() {{
            put("bin/images/iva.jpg", 1);
            put("bin/images/ossinovski.jpg", 1);
            put("bin/images/passport.png", 1);
            put("pics/2017-03-13 11.05.51 1469482206027873977_selfie.jpg", 0);
            put("pics/2017-03-13 11.05.53 1469482223920260860_selfie.jpg", 9);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            JSONArray features = json.getJSONArray("features");
            int faceCount = 0;
            for (int i = 0; i < features.length(); i++) {
                if (features.getJSONObject(i).getString("featureName").equals("face")) {
                    faceCount++;
                }
            }
            assertEquals("Number of faces for " + imageName, expected.get(imageName), new Integer(faceCount));
        }
    }

    @Test
    public void testEyes() throws IOException, JSONException {

        HashMap<String, Integer> expected = new HashMap<String, Integer>() {{
            put("bin/images/iva.jpg", 2);
            put("bin/images/ossinovski.jpg", 2);
            put("bin/images/passport.png", 2);
            put("pics/2017-03-13 11.05.51 1469482206027873977_selfie.jpg", 0);
            put("pics/2017-03-13 11.05.53 1469482223920260860_selfie.jpg", 18);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            JSONArray features = json.getJSONArray("features");
            int faceCount = 0;
            for (int i = 0; i < features.length(); i++) {
                if (features.getJSONObject(i).getString("featureName").equals("eye")) {
                    faceCount++;
                }
            }
            assertEquals("Number of eyes for " + imageName, expected.get(imageName), new Integer(faceCount));
        }
    }

    @Test
    public void testNose() throws IOException, JSONException {

        HashMap<String, Integer> expected = new HashMap<String, Integer>() {{
            put("bin/images/tsahkna.jpg", 1);
            put("bin/images/palo.jpg", 1);
            put("bin/images/passport.png", 1);
            put("pics/2017-03-13 11.05.51 1469482206027873977_selfie.jpg", 0);
            put("pics/2017-03-13 11.05.53 1469482223920260860_selfie.jpg", 9);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            JSONArray features = json.getJSONArray("features");
            int faceCount = 0;
            for (int i = 0; i < features.length(); i++) {
                if (features.getJSONObject(i).getString("featureName").equals("nose")) {
                    faceCount++;
                }
            }
            assertEquals("Number of noses for " + imageName, expected.get(imageName), new Integer(faceCount));
        }
    }

    @Test
    public void testMouth() throws IOException, JSONException {

        HashMap<String, Integer> expected = new HashMap<String, Integer>() {{
            put("bin/images/sester.jpg", 1);
            put("bin/images/palo.jpg", 1);
            put("bin/images/passport.png", 1);
            put("pics/2017-03-13 11.05.51 1469482206027873977_selfie.jpg", 0);
            put("pics/2017-03-13 11.05.53 1469482223920260860_selfie.jpg", 9);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            JSONArray features = json.getJSONArray("features");
            int faceCount = 0;
            for (int i = 0; i < features.length(); i++) {
                if (features.getJSONObject(i).getString("featureName").equals("mouth")) {
                    faceCount++;
                }
            }
            assertEquals("Number of mouths for " + imageName, expected.get(imageName), new Integer(faceCount));
        }
    }

    @Test
    public void testRedEyes() throws IOException, JSONException {

        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/iva.jpg", false);
            put("bin/images/red_eye.jpg", true);
            put("bin/images/red_eye2.jpg", true);
            put("bin/images/reps.jpg", false);
            put("bin/images/passport.png", false);
            put("pics/2017-03-13 11.05.51 1469482206027873977_selfie.jpg", false);
            put("pics/2017-03-13 11.05.53 1469482223920260860_selfie.jpg", false);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean hasRedEye = json.getBoolean("hasRedEye");
            assertEquals("Has red eyes for " + imageName, expected.get(imageName), hasRedEye);
        }
    }

    @Test
    public void testTeeth() throws IOException, JSONException {

        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/iva.jpg", false);
            put("bin/images/red_eye.jpg", true);
            put("bin/images/red_eye2.jpg", false);
            put("bin/images/reps.jpg", false);
            put("bin/images/passport.png", true);
            put("pics/2017-03-13 11.05.51 1469482206027873977_selfie.jpg", false);
            put("pics/2017-03-13 11.05.53 1469482223920260860_selfie.jpg", false);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean hasTeethVisible = json.getBoolean("hasTeethVisible");
            assertEquals("Has teeth visible for " + imageName, expected.get(imageName), hasTeethVisible);
        }
    }

    @Test
    public void testEyeOffset() throws IOException, JSONException {

        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/iva.jpg", false);
            put("bin/images/selfie3.jpg", true);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean hasEyeXOffset = json.getBoolean("hasEyeXOffset");
            Boolean hasEyeYOffset = json.getBoolean("hasEyeYOffset");
            assertEquals("Has eye offset for " + imageName, expected.get(imageName), hasEyeXOffset || hasEyeYOffset);
        }
    }

    @Test
    public void testNoseOffset() throws IOException, JSONException {
        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/red_eye.jpg", false);
            put("bin/images/selfie3.jpg", true);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean hasNoseOffset = json.getBoolean("hasNoseOffset");
            assertEquals("Has nose offset for " + imageName, expected.get(imageName), hasNoseOffset);
        }
    }

    @Test
    public void testMouthOffset() throws IOException, JSONException {
        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/red_eye2.jpg", false);
            put("bin/images/selfie3.jpg", true);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean hasMouthOffset = json.getBoolean("hasMouthOffset");
            assertEquals("Has mouth offset for " + imageName, expected.get(imageName), hasMouthOffset);
        }
    }

    @Test
    public void testVertically() throws IOException, JSONException {

        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/passport.png", false);
            put("bin/images/misplaced.jpg", true);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean isVerticallyCorrect = json.getBoolean("isVerticallyMisplaced");
            assertEquals("Is vertically correct for " + imageName, expected.get(imageName), isVerticallyCorrect);
        }
    }

    @Test
    public void testHorizontally() throws IOException, JSONException {

        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/passport.png", false);
            put("bin/images/selfie1.jpg", true);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean isHorizontallyCorrect = json.getBoolean("isHorizontallyMisplaced");
            assertEquals("Is horizontally correct for " + imageName, expected.get(imageName), isHorizontallyCorrect);
        }
    }

    @Test
    public void testGrayScale() throws IOException, JSONException {

        HashMap<String, Boolean> expected = new HashMap<String, Boolean>() {{
            put("bin/images/iva.jpg", false);
            put("bin/images/selfie3.jpg", true);
        }};

        for (String imageName: expected.keySet()) {
            String result = detection.detect(imageName);
            JSONObject json = new JSONObject(result);
            Boolean isGrayScale = json.getBoolean("isGrayScale");
            assertEquals("Is gray scale for " + imageName, expected.get(imageName), isGrayScale);
        }
    }
}
