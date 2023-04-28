package zhe.it_tech613.com.cmpcourier.utils;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import java.security.cert.Certificate;

import zhe.it_tech613.com.cmpcourier.activity.ChangeStatusActivity;

/**
 * Created by ZheZui on 23/07/2018.
 */
public class Constant {
    private static final String TAG = "Constant";
    public static String separator = "===";
    public static String sp = "/";
    public static LanguageStrings languageStrings = new LanguageStrings();
    public static LanguageStrings czlanguageStrings = new LanguageStrings();
    public static int x = 0;
    public static int y = 0;

    private static String ORIGIN_PDF = "_origin.pdf";
    private static String PDF = ".pdf";

    public static final int MUTIPLEREQUESTCODE = 12345;

    public static final int ZMENA_ID_0 = 23451, ZMENA_ID_1 = 54321;

    public enum LoginStatus {
        UserNotSignedIn("userNotSignedIn", 1),
        UserToSignIn("userToSignIn", 2),
        UserHasCancelledLogin("userHasCancelledLogin", 3),
        UserLoggedIn("userLoggedIn", 4),
        UserSignedUp("userSignedUp", 5);

        private String stringValue;
        private int intValue;

        private LoginStatus(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

        @Override
        public String toString() {
            return stringValue;
        }

        public int getIntValue() {
            return intValue;
        }
    }

    private static List<String> unZip(String path) throws IOException {
        InputStream is;
        ZipInputStream zis;
        String filename = "";
        List<String> lastPdfFileName = new ArrayList<>();
        File file = new File(path);
        is = new FileInputStream(path);
        zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;
        byte[] buffer = new byte[BUFFER];
        int count;

        while ((ze = zis.getNextEntry()) != null) {
            filename = ze.getName();
            String dirPath = file.getParent() + File.separator + file.getName().split("\\.")[0];
            // Need to create directories if not exists, or
            // it will generate an Exception...
            File newFile = new File(dirPath);
            if (!newFile.exists()) newFile.mkdir();
            if (ze.isDirectory()) {
                File fmd = new File(dirPath + File.separator + filename);
                fmd.mkdirs();
                continue;
            }

            newFile = new File(dirPath + File.separator + filename);
            newFile.createNewFile();
            FileOutputStream fout = new FileOutputStream(dirPath + File.separator + filename);
            if (filename.endsWith(PDF)) lastPdfFileName.add(newFile.getPath());

            while ((count = zis.read(buffer)) != -1) {
                fout.write(buffer, 0, count);
            }

            fout.close();
            zis.closeEntry();
        }

        zis.close();
        return lastPdfFileName;
    }

//    public static List<String> unZipGeneral(Context context, String path, String password) {
//        try {
//            List<String> lastName;
//            if ((lastName = unZip(path)) != null) {
//                File file = new File(path);
//                String dirPath = file.getParent() + File.separator + file.getName().split("\\.")[0];
//                File directory = new File(dirPath);
//                File[] files = directory.listFiles();
//                for (File f : files) {
//                    if (isArchive(f)) lastName = unZipGeneral(context, f.getPath(), password);
//                }
//            }
//            return lastName;
//        } catch (IOException e1) {
//            Toast.makeText(context, e1.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//            try {
//                return unZipProtected(context, path, password);
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                return new ArrayList<>();
//            }
//        }
//    }

    public static List<String> unZipGeneral(Context context, String path, String password, String password_1) {
        try {
            List<String> lastPdfFileName = new ArrayList<>();
            ZipFile firstFile = new ZipFile(path);
            File file = new File(path);
            String dirPath = file.getParent() + File.separator + file.getName().split("\\.")[0];
            if (firstFile.isEncrypted()) {
                firstFile.setPassword(password);
            }
            firstFile.extractAll(file.getParent() + File.separator + file.getName().split("\\.")[0]);
            Log.e("extractPath", file.getParent() + File.separator + file.getName().split("\\.")[0]);
            file = new File(dirPath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (isArchive(f)) {
                        ZipFile zipF = new ZipFile(f.getPath());
                        if (zipF.isEncrypted()) zipF.setPassword(password_1);
                        zipF.extractAll(f.getParent() + File.separator + f.getName().split("\\.")[0]);
                        new Handler().postDelayed(()->{
                            try {
                                ZipFile secondZip = new ZipFile(f.getPath());
                                if (secondZip.isEncrypted()) {
                                    secondZip.setPassword(password_1);
                                }
                                secondZip.extractAll(PreferenceManager.signedFolder);
                                File directory = new File(PreferenceManager.signedFolder);
                                File[] signedfiles = directory.listFiles();
                                for (File file1: signedfiles) {
                                    File newFile = new File(PreferenceManager.signedFolder, file1.getName().replace(PDF,ORIGIN_PDF));
                                    file1.renameTo(newFile);
                                }
                            } catch (ZipException e) {
                                e.printStackTrace();
                            }
                        },5000);
                        file = new File(f.getParent() + File.separator + f.getName().split("\\.")[0]);
                        if (file.isDirectory()) {
                            files = file.listFiles();
                            assert files != null;
                            for (File pdf_file : files) {
                                if (pdf_file.getName().endsWith(".pdf"))
                                    lastPdfFileName.add(pdf_file.getPath());
                            }
                        }
                    }
                }
            }
            return lastPdfFileName;
        } catch (ZipException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

//    private static List<String> unZipProtected(Context context, String path, String password) throws IOException {
//        // password-protected zip file I need to read
//        Log.e("unzipStart", password + " " + path);
//        FileInputStream fis = null;
//        fis = new FileInputStream(path);
//        File file = new File(path);
//        // wrap it in the decrypt stream
//        ZipDecryptInputStream zdis = new ZipDecryptInputStream(fis, password);
//        // wrap the decrypt stream by the ZIP input stream
//        ZipInputStream zis = new ZipInputStream(zdis);
//
//        // read all the zip entries and save them as files
//        ZipEntry ze;
//        FileOutputStream fos;
//        List<String> lastPdfFileName = new ArrayList<>();
//        String filename = "";
//        while ((ze = zis.getNextEntry()) != null) {
//            filename = ze.getName();
//            String dirPath = file.getParent() + File.separator + file.getName().split("\\.")[0];
//            File newFile = new File(dirPath);
//            if (!newFile.exists()) newFile.mkdir();
//            if (ze.isDirectory()) {
//                File fmd = new File(dirPath + File.separator + filename);
//                fmd.mkdirs();
//                continue;
//            }
//            newFile = new File(dirPath + File.separator + filename);
//            newFile.createNewFile();
//            fos = new FileOutputStream(newFile);
//            int b;
//            while ((b = zis.read()) != -1) {
//                fos.write(b);
//            }
//            fos.close();
//            if (filename.endsWith(".pdf")) lastPdfFileName.add(newFile.getPath());
//
//
//            Toast.makeText(context, Constant.czlanguageStrings.getSUCCESS_DECRYPT(), Toast.LENGTH_LONG).show();
//        }
//        zis.closeEntry();
//        zis.close();
//        return lastPdfFileName;
//    }

    private static boolean isArchive(File f) {
        int fileSignature = 0;
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            fileSignature = raf.readInt();
        } catch (IOException e) {
            // handle if you like
            e.printStackTrace();
        }
        return fileSignature == 0x504B0304 || fileSignature == 0x504B0506 || fileSignature == 0x504B0708;
    }

    public static boolean zipFiles(Context context, String directoryPath, String zipFileName, String barcode, String pin, String pin1) throws ZipException {
        Log.e(TAG, "zipFiles Path: " + directoryPath);
        File directory = new File(directoryPath);
        File[] signedfiles = directory.listFiles();
        if (signedfiles == null || signedfiles.length == 0) {
            Toast.makeText(context, String.format(Constant.czlanguageStrings.getCHECK_SIGNOSIGN(), directoryPath), Toast.LENGTH_LONG).show();
            return false;
        }
        for (File file : signedfiles) {
            if (file.getName().endsWith(ORIGIN_PDF)) continue;
            String[] splits = file.getName().split("_");
            StringBuilder builder = new StringBuilder();
            if (splits.length > 2) {
                for (int i = 0; i < splits.length - 2; i++) {
                    String s = splits[i];
                    builder.append(s);
                    if (i<splits.length - 3) builder.append("_");
                }
                builder.append(PDF);
            }
            String newName = builder.toString();
            File newFile = new File(directoryPath, newName);
            if (newFile.exists()) newFile.delete();
            file.renameTo(newFile);
        }
        signedfiles = directory.listFiles();
        for (File file :signedfiles){
            if (file.getName().endsWith(ORIGIN_PDF)){
                File newFile = new File(directoryPath, file.getName().replace(ORIGIN_PDF, PDF));
                if (newFile.exists()) {
                    file.delete();
                    continue;
                }
                file.renameTo(newFile);
            }
        }
        signedfiles = directory.listFiles();
        Log.e(TAG, "Size: " + signedfiles.length);
//        String[] signedfileNames = new String[signedfiles.length];
//        for (int i = 0; i < signedfiles.length; i++) {
//            Log.e(TAG, "FileName: " + signedfiles[i].getName());
//            signedfileNames[i] = signedfiles[i].getPath();
//        }
        String barcodeFolderName = PreferenceManager.downLoadFolder + File.separator + barcode;
        File barcodeFolder = new File(barcodeFolderName);
        if (barcodeFolder.exists()) {
            File[] files = barcodeFolder.listFiles();
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFiles(f.getPath());
                    f.delete();
                }
            }
            files = barcodeFolder.listFiles();
            for (File f : files) {
                if (!f.isDirectory()) {
                    if (isArchive(f)) {
                        String subZipFileName = f.getPath();//get sub zip file name for creating new sub zip file
                        f.delete();
                        if (zip(signedfiles, subZipFileName, pin1)) {
                            for (File file : signedfiles) {
                                if (file.exists()) file.delete();
                                Log.e(TAG, "deleted FileName: " + file.getPath());
                            }
                        }
                        files = barcodeFolder.listFiles();
                        if (files == null || files.length == 0) {
                            Toast.makeText(context, String.format(Constant.czlanguageStrings.getCHECK_SIGNOSIGN(), barcodeFolder.getPath()), Toast.LENGTH_LONG).show();
                            return false;
                        }
//                        String[] fileNames = new String[files.length];
//                        for (int i = 0; i < files.length; i++) {
//                            Log.e(TAG, "FileName: " + files[i].getName());
//                            fileNames[i] = files[i].getPath();
//                        }
                        if (zip(files, zipFileName, pin)) {
                            for (File file : files) {
                                if (file.exists()) file.delete();
                                Log.e(TAG, "deleted FileName: " + file.getPath());
                            }
                        }
                    }
                }
            }
        } else {
            if (zip(signedfiles, zipFileName, pin1)) {
                for (File file : signedfiles) {
                    if (file.exists()) file.delete();
                    Log.e(TAG, "deleted FileName: " + file.getPath());
                }
            }
        }

        return true;
    }

    public static void deleteFiles(String path) {
        Log.e(TAG, "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFiles(file.getPath());
                file.delete();
            } else file.delete();
            Log.e(TAG, "FileName:" + file.getName());
        }
    }

    private static final int BUFFER = 4096;

    private static boolean zip(File[] files, String destPath, String pass) throws ZipException {
        ZipFile zipFile = new ZipFile(destPath);
        // Setting parameters
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
//        zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        // Setting password
        zipParameters.setPassword(pass);
        for (File f : files) {
            zipFile.addFile(f, zipParameters);
        }
        return true;
    }

    private static boolean zip(String[] _files, String zipFileName, String pass) throws IOException {
        BufferedInputStream origin = null;
        File zipFile = new File(zipFileName);
        zipFile.createNewFile();
        FileOutputStream dest = new FileOutputStream(zipFileName);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        byte[] data = new byte[BUFFER];

        for (String file : _files) {
            Log.e("Compress", "Adding: " + file);
//            if (!file.endsWith(".pdf")) continue;
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, BUFFER);

            ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        out.close();
        return true;
    }
}

