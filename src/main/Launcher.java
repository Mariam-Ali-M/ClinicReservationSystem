package main;

import java.io.File;

public class Launcher {

    public static void main(String[] args) throws Exception {
        // المسار للـ JDK الحالي
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        // مسار JAR المشروع (لو عملتي Clean and Build هيكون موجود)
        String jarPath = "dist" + File.separator + "ClinicReservationSystem.jar";

        // مسار JavaFX SDK lib
        String javafxLib = "C:\\Users\\USER\\Downloads\\openjfx-25.0.1_windows-x64_bin-sdk\\lib";

        // بناء الأمر لتشغيل التطبيق مع JavaFX
        ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "--module-path", javafxLib,
                "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics",
                "-cp", jarPath,
                "main.ClinicReservationSystem"
        );

        // ربط الـ output بالـ console عشان نشوف أي errors
        pb.inheritIO();

        // شغل العملية وانتظر النهاية
        Process process = pb.start();
        process.waitFor();
    }
}
