package com.fototuvastus;

import java.io.*;

public class Utils {
    public static void loadOpenCV() throws IOException {

        String prefix;
        String suffix;

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            suffix = "dll";
            if (System.getenv("ProgramFiles(x86)") != null) {
                prefix = "opencv_java320";
            } else {
                prefix = "opencv_java320_x86";
            }

        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            prefix = "libopencv_java320";
            suffix = "dylib";
        } else {
            prefix = "libopencv_java320";
            suffix = "so";
        }

        File temp = createTempFile(prefix, suffix);
        System.load(temp.getAbsolutePath());
    }

    public static File createTempFile(String prefix, String suffix) throws IOException {

        File temp = new File(new File(System.getProperty("java.io.tmpdir")), prefix + "." + suffix);

        if (temp.exists()) {
            return temp;
        }

        temp.createNewFile();
        //File temp = File.createTempFile(prefix, suffix);
        temp.deleteOnExit();

        if (!temp.exists()) {
            throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
        }

        // Prepare buffer for data copying
        byte[] buffer = new byte[1024];
        int readBytes;

        // Open and check input stream
        InputStream is = new FileInputStream(String.format("bin/opencv/%s.%s", prefix, suffix));
        if (is == null) {
            throw new FileNotFoundException("OpenCV library was not found inside JAR.");
        }

        // Open output stream and copy data between source file in JAR and the temporary file
        OutputStream os = new FileOutputStream(temp);
        try {
            while ((readBytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        } finally {
            os.close();
            is.close();
        }

        return temp;
    }

}
