package com.fototuvastus;

import com.google.gson.Gson;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opencv.core.Core.inRange;
import static org.opencv.imgproc.Imgproc.moments;


@Service
public class Detection {
    Gson parser = new Gson();
    DetectionResponse response;
    File dir = new File("./bin/images");
    int i = 0;

    //3 different inputs, all return json objects with detection data
    //Default, takes in one image that is specified
    @PostConstruct
    public String run() throws IOException {
        System.out.println("Detection run started.. \n");
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Utils.loadOpenCV();
        return detect("bin/images/ossinovski.jpg");
    }

    String detect(String imagePath) throws IOException {
        Mat image = Imgcodecs.imread(imagePath);

        return parser.toJson(validateImage(image));
    }

    //Takes in image bas64 string
    public String runBase64(String base64String) throws IOException {
        System.out.println("Detection run started.. \n");
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Utils.loadOpenCV();
        Mat image = Imgcodecs.imdecode(new MatOfByte(getBase64ByteArray(base64String)), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        return parser.toJson(validateImage(image));
    }

    //Goes trough all images in /images folder
    public String runMultiple() throws IOException {
        System.out.println("Detection run multiple started.. \n");
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Utils.loadOpenCV();
        List<Mat> images = new ArrayList<>();
        if (dir.isDirectory()) { // make sure it's a directory
            for (File file : dir.listFiles(IMAGE_FILTER)) {
                System.out.println("\n " + file.getName());
                images.add(Imgcodecs.imread(file.toString()));
            }
        }
        DetectionResponse validatedImage;
        List<DetectionResponse> resultJSON = new ArrayList<>();
        for (Mat image : images) {
            validatedImage = validateImage(image);
            resultJSON.add(validatedImage);
        }
        return parser.toJson(resultJSON);
    }

    private byte[] getBase64ByteArray(String data) {
        String base64Image = data.split(",")[1];
        return javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
    }


    DetectionResponse validateImage(Mat image) {
        System.out.println("----------------------------------------------------");
        //response object that holds all values and data regarding to detection
        response = new DetectionResponse();

        Size size_in_cm = this.getImageSizeInCm(image);
        boolean is_image_gray = this.isImageGray(image);
        response.imageSizePx = image.size();
        response.imageSizeCm = size_in_cm;
        response.isGrayScale = is_image_gray;

        System.out.println(String.format("Image height %scm", size_in_cm.height));
        System.out.println(String.format("Image width %scm", size_in_cm.width));
        System.out.println(is_image_gray ? "!!! - Image black and white\n" : "Image not black and white\n");

        // Create detectors for different face features
        CascadeClassifier faceDetector = new CascadeClassifier("bin/cascades/lbpcascade_frontalface.xml");
        CascadeClassifier eyeDetector = new CascadeClassifier(Constants.USE_MCS_CASCADES ? "bin/cascades/haarcascade_mcs_eye.xml" : "bin/cascades/haarcascade_eye.xml");
        CascadeClassifier noseDetector = new CascadeClassifier(Constants.USE_MCS_CASCADES ? "bin/cascades/haarcascade_mcs_nose.xml" : "bin/cascades/haarcascade_nose.xml");
        CascadeClassifier mouthDetector = new CascadeClassifier(Constants.USE_MCS_CASCADES ? "bin/cascades/haarcascade_mcs_mouth.xml" : "bin/cascades/haarcascade_mouth.xml");
        CascadeClassifier smileDetector = new CascadeClassifier("bin/cascades/haarcascade_smile.xml");

        //Check background
        if (objectsInImageBackground(image)) {
            response.hasExtraneousBackgroundObjects = true;
            response.errorCount++;
            System.out.println("!!! - Extraneus objects detected in background \n");
        } else {
            System.out.println("No extraneus objects detected in background \n");
        }

        // Detection material
        MatOfRect faceDetections = new MatOfRect();

        // Detect face
        faceDetector.detectMultiScale(image, faceDetections);
        processDetectionData(image, faceDetections, "face", Constants.COLOR_FACE);

        int haarOptions = Objdetect.CASCADE_DO_ROUGH_SEARCH;
        for (Rect face : faceDetections.toArray()) {
            MatOfRect detections = new MatOfRect();

            //Detections for different face features, detection search areas are limited with values from constants file

            // Detect eyes
            //Search only from upper part of the face
            Mat eyesROI = new Mat(image,
                    new Rect(
                            new Point(face.x + face.width * Constants.SEARCH_AREA_EYES_START_X, face.y + face.height * Constants.SEARCH_AREA_EYES_START_Y),
                            new Point(face.x + face.width * Constants.SEARCH_AREA_EYES_END_X, face.y + face.height * Constants.SEARCH_AREA_EYES_END_Y)));
            eyeDetector.detectMultiScale(eyesROI, detections, 1.05, 4, haarOptions,
                    new Size(face.width * Constants.SIZE_EYES_MIN_WIDTH, face.height * Constants.SIZE_EYES_MIN_HEIGHT), new Size(face.width * Constants.SIZE_EYES_MAX_WIDTH, face.height * Constants.SIZE_EYES_MAX_HEIGHT));
            processDetectionData(eyesROI, detections, "eye", Constants.COLOR_EYES);

            // Detect nose
            Mat noseROI = new Mat(image,
                    new Rect(
                            new Point(face.x + face.width * Constants.SEARCH_AREA_NOSE_START_X, face.y + face.height * Constants.SEARCH_AREA_NOSE_START_Y),
                            new Point(face.x + face.width * Constants.SEARCH_AREA_NOSE_END_X, face.y + face.height * Constants.SEARCH_AREA_NOSE_END_Y)));
            noseDetector.detectMultiScale(noseROI, detections, 1.05, 4, haarOptions,
                    new Size(face.width * Constants.SIZE_NOSE_MIN_WIDTH, face.height * Constants.SIZE_NOSE_MIN_HEIGHT), new Size(face.width * Constants.SIZE_NOSE_MAX_WIDTH, face.height * Constants.SIZE_NOSE_MAX_HEIGHT));
            processDetectionData(noseROI, detections, "nose", Constants.COLOR_NOSE);

            //Search only from lower part of the face
            Mat mouthROI = new Mat(image,
                    new Rect(
                            new Point(face.x + face.width * Constants.SEARCH_AREA_MOUTH_START_X, face.y + face.height * Constants.SEARCH_AREA_MOUTH_START_Y),
                            new Point(face.x + face.width * Constants.SEARCH_AREA_MOUTH_END_X, face.y + face.height * Constants.SEARCH_AREA_MOUTH_END_Y)));
            mouthDetector.detectMultiScale(mouthROI, detections, 1.05, 6, haarOptions
                    , new Size(face.width * Constants.SIZE_MOUTH_MIN_WIDTH, face.height * Constants.SIZE_MOUTH_MIN_HEIGHT), new Size(face.width * Constants.SIZE_MOUTH_MAX_WIDTH, face.height * Constants.SIZE_MOUTH_MAX_HEIGHT));
            processDetectionData(mouthROI, detections, "mouth", Constants.COLOR_MOUTH);


            // Detect smile
            //smileDetector.detectMultiScale(image, detections, 1.1, 7, 0, new Size(30, 30), new Size(50, 50));
            //processDetectionData(image, detections, "smile");
        }

        //Calculate offsets
        List<FaceFeature> eyes = response.features.stream().filter(x -> "eye".equals(x.featureName)).collect(Collectors.toList());
        Optional<FaceFeature> nose = response.features.stream().filter(x -> "nose".equals(x.featureName)).findFirst();
        Optional<FaceFeature> mouth = response.features.stream().filter(x -> "mouth".equals(x.featureName)).findFirst();
        if (eyes.size() == 2 && nose.isPresent() && mouth.isPresent())
            calculateOffsets(eyes.get(0), eyes.get(1), nose.get(), mouth.get());

        //Overall validity
        response.isImageValid = response.errorCount == 0;

        System.out.println(String.format("FINAL RESULT: %s problems detected - picture is %s \n", response.errorCount, (response.isImageValid ? "VALID" : "INVALID")));

        // Save the visualized detection.
        String filename = "bin/faceDetection" + i + ".jpg";
        i++;
        System.out.println(String.format("Writing %s", filename));
        Imgcodecs.imwrite(filename, image);

        return response;
    }

    private void processDetectionData(Mat image, MatOfRect detections, String featureName, double[] rgba) {
        int detectionCount = detections.toArray().length;

        System.out.println(String.format("----------------%s DETECTION START----------------", featureName.toUpperCase()));
        System.out.println(String.format("Detected %s %ss", detectionCount, featureName));

        //Save detection stuff to feature object
        for (Rect rect : detections.toArray()) {
            FaceFeature feature = getFaceFeatureFromDetections(featureName, rect, detectionCount);
            System.out.println(feature.toString());

            //Draw rectangle around feature
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(rgba));

            //Check for errors
            if (detectionCount != (featureName.equals("eye") ? 2 : 1)) {
                response.errorCount++;
                feature.isFeatureValid = false;
                System.out.println("!!! -  Multiple features found ");
            }

            if (featureName.equals("face")) {
                double imageHeight = response.imageSizeCm.height;
                double faceHeight = rect.height * Constants.PIXEL_TO_CM;
                double coveredHeightPercentage = (faceHeight / imageHeight) * 100;

                if (coveredHeightPercentage < Constants.SIZE_FACE_MIN_HEIGHT) {
                    response.faceTooSmall = true;
                    response.errorCount++;
                    System.out.println("!!! - Head too small");
                } else if (coveredHeightPercentage > Constants.SIZE_FACE_MAX_HEIGHT) {
                    response.faceTooLarge = true;
                    response.errorCount++;
                    System.out.println("!!! - Head too large");
                } else {
                    System.out.println("Head is in appropriate proportions");
                }
            }

            if (featureName.equals("eye")) {
                if (detectRedEyes(image, rect)) {
                    System.out.println("!!! - RED eye detected");
                    response.errorCount++;
                    response.hasRedEye = true;
                    feature.isFeatureValid = false;
                } else {
                    System.out.println("No red eye");
                }

                //if (response.isVerticallyMisplaced) {
                Size s = new Size();
                Point p = new Point();
                Mat m = new Mat(image, rect);
                m.locateROI(s, p);

                if (verticallyMisplaced(rect, new Rect(p, s))) {
                    System.out.println("!!! - Photo vertically incorrect");
                    response.errorCount++;
                    response.isVerticallyMisplaced = true;
                    feature.isFeatureValid = false;
                } else {
                    System.out.println("Eye vertically correctly positioned");
                }
                //}
            }

            if (featureName.equals("nose")) {

                Size s = new Size();
                Point p = new Point();
                Mat m = new Mat(image, rect);
                m.locateROI(s, p);

                if (horizontallyMisplaced(rect, new Rect(p, s))) {
                    System.out.println("!!! - Photo horizontally misplaced");
                    response.errorCount++;
                    response.isHorizontallyMisplaced = true;
                    feature.isFeatureValid = false;
                } else {
                    System.out.println("Photo horizontally correctly positioned");
                }
            }

            if (featureName.equals("mouth")) {
                if (detectTeethVisible(image, rect)) {
                    System.out.println("!!! - Teeth found");
                    response.errorCount++;
                    response.hasTeethVisible = true;
                    feature.isFeatureValid = false;
                } else {
                    System.out.println("No teeth found");
                }
            }

            response.features.add(feature);
        }
        System.out.println("----------------------------------------------------\n");
    }

    private FaceFeature getFaceFeatureFromDetections(String featureName, Rect rect, int detectionCount) {
        int xPos = rect.x;
        int yPos = rect.y;
        switch (featureName) {
            case "eye":
                xPos += response.features.get(0).positionXY[0] + (response.features.get(0).size.width * Constants.SEARCH_AREA_EYES_START_X);
                yPos += response.features.get(0).positionXY[1] + (response.features.get(0).size.height * Constants.SEARCH_AREA_EYES_START_Y);
                break;
            case "nose":
                xPos += response.features.get(0).positionXY[0] + (response.features.get(0).size.width * Constants.SEARCH_AREA_NOSE_START_X);
                yPos += response.features.get(0).positionXY[1] + (response.features.get(0).size.height * Constants.SEARCH_AREA_NOSE_START_Y);
                break;
            case "mouth":
                xPos += response.features.get(0).positionXY[0] + (response.features.get(0).size.width * Constants.SEARCH_AREA_MOUTH_START_X);
                yPos += response.features.get(0).positionXY[1] + (response.features.get(0).size.height * Constants.SEARCH_AREA_MOUTH_START_Y);
                break;
        }
        int xPosCenter = xPos + rect.width / 2;
        int yPosCenter = yPos + rect.height / 2;

        return new FaceFeature(
                featureName,
                detectionCount,
                rect.size(),
                new int[]{xPos, yPos},
                new int[]{xPosCenter, yPosCenter},
                new double[]{(double) xPosCenter / response.imageSizePx.width * 100.0f, (float) yPosCenter / response.imageSizePx.height * 100.0f},
                true);
    }

    private boolean horizontallyMisplaced(Rect orig, Rect rect) {
        double imageWidth = response.imageSizePx.width;
        double noseCenterLevel = rect.x + (orig.width / 2);

        double imageMinNosePosition = imageWidth * 0.44;
        double imageMaxNosePosition = imageWidth * 0.56;

//        System.out.println(String.format("Nose loc %d -> %d to %d ", noseCenterLevel, imageMinNosePosition, imageMaxNosePosition));

        return noseCenterLevel < imageMinNosePosition || noseCenterLevel > imageMaxNosePosition;
    }

    private boolean verticallyMisplaced(Rect orig, Rect rect) {
        double imageHeight = response.imageSizePx.height;
        double eyeCenterLevel = rect.y + (orig.height / 2);

        double imageMinEyeLevel = imageHeight * 0.35;
        double imageMaxEyeLevel = imageHeight * 0.5;

//        System.out.println(String.format("Eyes loc %d -> %d to %d ", eyeCenterLevel, imageMinEyeLevel, imageMaxEyeLevel));
        return eyeCenterLevel < imageMinEyeLevel || eyeCenterLevel > imageMaxEyeLevel;
    }

    //Check if any features are too far from image y/x center axises(changeable from constants)
    private void calculateOffsets(FaceFeature eye1, FaceFeature eye2, FaceFeature nose, FaceFeature mouth) {
        if (Math.abs(eye1.baseAxisOffsetXY[0] - 50.0f + eye2.baseAxisOffsetXY[0] - 50.0f) > Constants.OFFSET_EYES_XAXIS_MAX) {
            System.out.println(Math.abs(eye1.baseAxisOffsetXY[0] - 50.0f + eye2.baseAxisOffsetXY[0] - 50.0f) + "% eyes X offset");
            response.hasEyeXOffset = true;
            response.errorCount++;
        }
        if (Math.abs(eye1.baseAxisOffsetXY[1] - eye2.baseAxisOffsetXY[1]) > Constants.OFFSET_EYES_YAXIS_MAX) {
            System.out.println(Math.abs(eye1.baseAxisOffsetXY[1] - eye2.baseAxisOffsetXY[1]) + "% eyes Y offset");
            response.hasEyeYOffset = true;
            response.errorCount++;
        }
        if (Math.abs(nose.baseAxisOffsetXY[0] - 50.0f) > Constants.OFFSET_NOSE_XAXIS_MAX) {
            System.out.println(Math.abs(nose.baseAxisOffsetXY[0] - 50.0f) + "% nose X offset");
            response.hasNoseOffset = true;
            response.errorCount++;
        }
        if (Math.abs(mouth.baseAxisOffsetXY[0] - 50.0f) > Constants.OFFSET_MOUTH_XAXIS_MAX) {
            System.out.println(Math.abs(mouth.baseAxisOffsetXY[0] - 50.0f) + "% mouth X offset");
            response.hasMouthOffset = true;
            response.errorCount++;
        }
    }

    private boolean detectRedEyes(Mat image, Rect rect) {
        Mat eye = new Mat(image, new Rect(new Point(rect.x + rect.width * 0.4, rect.y + rect.height * 0.4), new Point(rect.x + rect.width * 0.4 + rect.width / 5, rect.y + rect.height * 0.4 + rect.height / 5)));
        Mat hsv = new Mat();
        Imgproc.cvtColor(eye, hsv, Imgproc.COLOR_BGR2HSV);

        Mat upperRedHueRange = new Mat();
        inRange(hsv, new Scalar(160, 100, 100), new Scalar(179, 255, 255), upperRedHueRange);

        Moments oMoments = moments(upperRedHueRange);

        Imgproc.rectangle(image, new Point(rect.x + rect.width * 0.4, rect.y + rect.height * 0.4), new Point(rect.x + rect.width * 0.4 + rect.width / 5, rect.y + rect.height * 0.4 + rect.height / 5),
                new Scalar(new double[]{255, 0, 0, 0}));
        return oMoments.m00 > 0;
    }

    private boolean detectTeethVisible(Mat image, Rect rect) {
        Mat mouth = new Mat(image, new Rect(rect.x, rect.y, rect.width, rect.height));
        Mat hsv = new Mat();
        Imgproc.cvtColor(mouth, hsv, Imgproc.COLOR_BGR2HSV);

        Mat upperWhiteHueRange = new Mat();

        int sensitivity = 30;
        Scalar lower_white = new Scalar(0, 0, 255 - sensitivity);
        Scalar upper_white = new Scalar(255, sensitivity, 255);

        inRange(hsv, lower_white, upper_white, upperWhiteHueRange);

        Moments oMoments = moments(upperWhiteHueRange);

        return oMoments.m00 > 0;
    }

    public Size getImageSizeInCm(Mat image) {
        int height = image.rows();
        double height_in_cm = (height * Constants.PIXEL_TO_CM);

        int width = image.cols();
        double width_in_cm = (width * Constants.PIXEL_TO_CM);

        Size ret_size = new Size();
        ret_size.height = height_in_cm;
        ret_size.width = width_in_cm;

        return ret_size;
    }

    public boolean isImageGray(Mat image) {
        Mat diff = Mat.zeros(image.rows(), image.cols(), image.type());
        List<Mat> splits = new ArrayList<>();

        Core.split(image, splits);
        Core.absdiff(splits.get(0), splits.get(1), diff);

        Scalar diff_scalar = Core.mean(diff);
        if (diff_scalar.val[0] > 3.0)
            return false;

        Core.absdiff(splits.get(0), splits.get(2), diff);

        diff_scalar = Core.mean(diff);
        return diff_scalar.val[0] <= 3.0;
    }

    private boolean objectsInImageBackground(Mat image) {
        Instant start = Instant.now();

        int chan = image.channels();
        Scalar mu = Core.mean(image);
//        System.out.println(String.format("Image colors mean values  :"+ mu.toString()));
        double bright = 0;
        for (int i = 0; i < chan; i++)
            bright += mu.val[i];
//        System.out.println(String.format("Brightness summary %f", bright));

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat thr = new Mat(image.rows(), image.cols(), CvType.CV_8U);
//        Mat dst = new Mat(image.rows(), image.cols(), CvType.CV_8U, Scalar.all(0));
//        String filename = "bin/gauss.jpg";

//        System.out.println("cvtColor");
        Imgproc.cvtColor(image, thr, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(thr, thr, new Size(5, 5), 1);
        if (bright > 450)
            Imgproc.threshold(thr, thr, 180, 255, Imgproc.THRESH_TOZERO);
        else
            Imgproc.threshold(thr, thr, 160, 255, Imgproc.THRESH_TOZERO);

//        System.out.println("imwrite");
//        filename = "bin/threshold.jpg";
//        Imgcodecs.imwrite(filename, thr);
/*        
        int iCannyLowerThreshold = 80, iCannyUpperThreshold = 100;      
        Imgproc.Canny(thr, thr, iCannyLowerThreshold, iCannyUpperThreshold);
        filename = "bin/canny.jpg";
        Imgcodecs.imwrite(filename, thr);
  */
//        System.out.println("findContours");
        Imgproc.findContours(thr, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE, new Point(0, 0));
        Scalar color = new Scalar(255, 255, 255);

//        System.out.println(String.format("Contour count %d", contours.size()));

        int id = 0;
        double maxar = 0;
        List<MatOfPoint> mcontours = new ArrayList<MatOfPoint>();
        for (int idx = 0; idx != contours.size(); ++idx) {
            double contourarea = Imgproc.arcLength(new MatOfPoint2f(contours.get(idx).toArray()), true);

            if (contourarea > 1000) {
                mcontours.add(contours.get(idx));
            }
/*
            if (contourarea > maxar)
            {
            	maxar = contourarea;
            	id = idx;
            }
  */
        }
        //    mcontours.add(contours.get(id));

        int hstep = image.cols() / 10;

//		System.out.println(String.format("Laius %d  k�rgus %d", image.cols(), image.rows() ));
//		System.out.println(String.format("Hstep %d ", hstep));
        // vertical check - from up-to-contour 
        for (int ii = 1; ii < image.cols(); ii += hstep) {
            // get the refernce point for column and summarize the color values
            double[] basepoint = image.get(1, ii);
            double baseclr = basepoint[0] + basepoint[1] + basepoint[2];

            // only color changes that are more than N pixels are counted (currently pixel count 5)
            int chng_len = 0;
            for (int jj = 1; jj < image.rows(); jj++) {
                double[] tmp = image.get(jj, ii);
                double tmpclr = tmp[0] + tmp[1] + tmp[2];

                boolean chng = false;
                boolean isborder = false;
                // check if color has chenged between the iterations (current base jump value 10)
                if (Math.abs(tmpclr - baseclr) > 10) {
//					System.out.println(String.format("v2rvi vahetus %d %d", ii, jj));
//					System.out.println(String.format("tmp %f basecl %f", tmp[kk], basecl[kk]));
                    for (int temp = 0; temp < mcontours.size(); temp++) {
                        if (Math.abs(Imgproc.pointPolygonTest(new MatOfPoint2f(mcontours.get(temp).toArray()), new Point(ii, jj), true)) < 5) {
//							System.out.println(String.format("contour found at %d %d", ii, jj));
                            isborder = true;
                            break;
                        }
                    }
                    if (!isborder) {
                        chng = true;
                    } else {
                        break;
                    }
                }
                if (chng) {
                    chng_len += 1;
                    if (chng_len >= 10) {
                        System.out.println(String.format("Color vertical change in background around %d %d", ii, jj));
//    	                Imgproc.drawMarker(dst, new Point(ii,jj), color);
                        return true;
                    }
                } else {
                    chng_len = 0;
                    baseclr = tmpclr;
                }
            }
        }

        // find the "shoulder corner locations" on the image - i.e. contour location on columns 1 and image.width-1
        int maxrows = 0;
        for (int temp = 0; temp < mcontours.size(); temp++) {
            Rect r = Imgproc.boundingRect(mcontours.get(temp));
            if (maxrows < r.height)
                maxrows = r.height;
        }
//        System.out.println(String.format("max contour rows %d ", maxrows));
        // hack one shoulder might be higher that the other use only 75% from the lowest point   
        maxrows = maxrows * 3 / 4;
        int vstep = maxrows / 10;

//		System.out.println(String.format("Vcontrol to %d vstep %d ", maxrows, vstep));

//         vstep = 5;
        for (int ii = 1; ii < maxrows; ii += vstep) {
            // horisontal check - from left to right 
            // get the refernce point for column and summarize the color values
            double[] basepoint = image.get(ii, 1);
            double baseclr = basepoint[0] + basepoint[1] + basepoint[2];

            // only color changes that are more than N pixels are counted (currently pixel count 5)
            int chng_len = 0;
            for (int jj = 2; jj < image.cols(); jj++) {
                double[] tmp = image.get(ii, jj);
                double tmpclr = tmp[0] + tmp[1] + tmp[2];

                boolean chng = false;
                boolean isborder = false;
                // check if color has chenged between the iterations (current base jump value 10)
                if (Math.abs(tmpclr - baseclr) > 10) {
//					System.out.println(String.format("v2rvi vahetus %d %d", ii, jj));
//					System.out.println(String.format("tmp %f basecl %f", tmp[kk], basecl[kk]));
                    for (int temp = 0; temp < mcontours.size(); temp++) {
                        if (Imgproc.pointPolygonTest(new MatOfPoint2f(mcontours.get(temp).toArray()), new Point(jj, ii), true) < 10) {
//							System.out.println(String.format("contour found at %d %d", ii, jj));
                            isborder = true;
                            break;
                        }
                    }
                    if (!isborder) {
                        chng = true;
                    } else {
                        break;
                    }
                }
                if (chng) {
                    chng_len += 1;
                    if (chng_len >= 10) {
                        System.out.println(String.format("Color horisontal change in background left around %d %d", jj, ii));
//    	                Imgproc.drawMarker(dst, new Point(jj,ii), color);
                        return true;
                    }
                } else {
                    chng_len = 0;
                    baseclr = tmpclr;
                }
            }

            // horizontal check - from right to left
            // get the reference point for column and summarize the color values
            basepoint = image.get(ii, image.cols() - 1);
            baseclr = basepoint[0] + basepoint[1] + basepoint[2];

            // only color changes that are more than N pixels are counted (currently pixel count 5)
            chng_len = 0;
            for (int jj = image.cols() - 2; jj > 0; jj--) {
                double[] tmp = image.get(ii, jj);
                double tmpclr = tmp[0] + tmp[1] + tmp[2];

                boolean chng = false;
                boolean isborder = false;
                // check if color has chenged between the iterations (current base jump value 10)
                if (Math.abs(tmpclr - baseclr) > 10) {
//					System.out.println(String.format("v2rvi vahetus %d %d", ii, jj));
//					System.out.println(String.format("tmp %f basecl %f", tmp[kk], basecl[kk]));
                    for (int temp = 0; temp < mcontours.size(); temp++) {
                        if (Imgproc.pointPolygonTest(new MatOfPoint2f(mcontours.get(temp).toArray()), new Point(jj, ii), true) < 10) {
//							System.out.println(String.format("contour found at %d %d", ii, jj));
                            isborder = true;
                            break;
                        }
                    }
                    if (!isborder) {
                        chng = true;
                    } else {
                        break;
                    }
                }
                if (chng) {
                    chng_len += 1;
                    if (chng_len >= 10) {
                        System.out.println(String.format("Color horisontal change in background right around %d %d", jj, ii));
//    	                Imgproc.drawMarker(dst, new Point(jj,ii), color);
                        return true;
                    }
                } else {
                    chng_len = 0;
                    baseclr = tmpclr;
                }
            }

        }

//        System.out.println(String.format("contour array size %d ", mcontours.get(0).toArray().length));
//        Imgproc.drawContours(dst, mcontours, -1, color, 2);    

//        filename = "bin/contour.jpg";
//        Imgcodecs.imwrite(filename, dst);

        //your code
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");

        return false;
    }

    private String[] EXTENSIONS = new String[]{
            "jpg", "png"
    };

    private FilenameFilter IMAGE_FILTER = (dir, name) -> {
        for (final String ext : EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return (true);
            }
        }
        return (false);
    };
}