package zhe.it_tech613.com.cmpcourier.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import zhe.it_tech613.com.cmpcourier.model.Status;

import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Ip;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Login;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Pass;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpIp;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpLogin;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpPass;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.logFile;

public class UploadEsignTask extends AsyncTask<Void, Void, Status> {
    private ConnectionInterface connectionInterface;
    private FTPClient mFtpClientUT;
    private List<String> barcodeList = new ArrayList<>();

    @Override
    protected zhe.it_tech613.com.cmpcourier.model.Status doInBackground(Void... voids) {
        barcodeList = new ArrayList<>();
        connectionInterface.uploadListener(zhe.it_tech613.com.cmpcourier.model.Status.ConnectingFTP);
        ConnectingWithFTP();
        try {
            LogonToFTP();
            String server_file_Path = PreferenceManager.getFtpESIGN_Path().substring(0, PreferenceManager.getFtpESIGN_Path().lastIndexOf("/")) + "/upload";
            changeWorkingDir(server_file_Path);
            File directory = new File(PreferenceManager.uploadFolder);
            File[] files = directory.listFiles();
            if (files == null || files.length == 0)
                return zhe.it_tech613.com.cmpcourier.model.Status.NoFiles;
            int num_success = 0;
            boolean someFileFailed = false;
            for (File file : files) {
                boolean thisFileFailed = false;

                Log.e("full_path", file.getName());
                PreferenceManager.addToFile("prepare move file to FTP:" + file.getName(), logFile);


                if (!uploadFile(file.getName(), file, file.length(), 0)) {
                    PreferenceManager.addToFile("failed move file to FTP:" + file.getName(), logFile);
                    someFileFailed = true;
                    thisFileFailed = true;
                }
                num_success += 1;
                PreferenceManager.addToFile("success move file to FTP:" + file.getName(), logFile);
                connectionInterface.updateListener(String.format(Constant.czlanguageStrings.getUPLOAD5(), num_success, files.length), (int) (num_success * 100 / files.length));

                if (!thisFileFailed) {
                    barcodeList.add(file.getName().split("\\.")[0]);
                    file.delete();
                }

            }

            if (someFileFailed) {
                return zhe.it_tech613.com.cmpcourier.model.Status.Failed;
            } else if (num_success == num_success + files.length) {
                return zhe.it_tech613.com.cmpcourier.model.Status.Success;
            } else {
                return zhe.it_tech613.com.cmpcourier.model.Status.FileMoved;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return zhe.it_tech613.com.cmpcourier.model.Status.Failed;
        }
    }


    @Override
    protected void onPostExecute(zhe.it_tech613.com.cmpcourier.model.Status status) {
        connectionInterface.uploadListener(status);
        if (status == zhe.it_tech613.com.cmpcourier.model.Status.Success
                || status == zhe.it_tech613.com.cmpcourier.model.Status.FileMoved)
            connectionInterface.getBarcodes(barcodeList);
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            if (status == zhe.it_tech613.com.cmpcourier.model.Status.Success) {mFtpClientUT.logout();}

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onGetResult(ConnectionInterface connectionInterface){
        this.connectionInterface = connectionInterface;
    }

    private void changeWorkingDir(String dir){
        try {
            if (dir.startsWith("/")) dir = dir.substring(1);
            String[] pathSnips = dir.split("/");
            StringBuilder currentDir= new StringBuilder();
            for (String pathSnip : pathSnips) {
                currentDir.append("/").append(pathSnip);
                if (!mFtpClientUT.changeWorkingDirectory(currentDir.toString())) {
                    mFtpClientUT.makeDirectory(currentDir.toString());
                    mFtpClientUT.changeWorkingDirectory(currentDir.toString());
                    Log.e("folder creation", "ok " + currentDir.toString());
                } else Log.e("folder change", "ok " + currentDir.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean uploadFile(String full_path,File file,long originalFileSize, int checkUpload) throws Exception {

        FileInputStream srcFileStream;
        srcFileStream = new FileInputStream(file);
        boolean result = mFtpClientUT.storeFile(full_path, srcFileStream);
        srcFileStream.close();

        long transferredFileSize = 0;
        FTPFile[] targetFiles = mFtpClientUT.listFiles();

        for (FTPFile targetFile : targetFiles){
            if (targetFile.toString().contains(full_path)){
                transferredFileSize = targetFile.getSize();
            }
        }

        if (!result && checkUpload < 4){
            return uploadFile( full_path,file,originalFileSize,checkUpload+1);
        }else if (transferredFileSize != originalFileSize && checkUpload < 4){
            return uploadFile( full_path,file,originalFileSize,checkUpload+1);
        } else if (checkUpload >= 4) { return false;}
        else return true;
    }

    public interface ConnectionInterface {
        void uploadListener(zhe.it_tech613.com.cmpcourier.model.Status result);
        void updateListener(String string, int progress);
        void getBarcodes(List<String> barcodes);
    }

    private void LogonToFTP() {
        String userName=getFtpESIGN_Login();
        String pass=getFtpESIGN_Pass();
        try {
            mFtpClientUT.login(userName, pass);
            if (FTPReply.isPositiveCompletion(mFtpClientUT.getReplyCode())) {
                mFtpClientUT.setFileType(FTP.BINARY_FILE_TYPE);
                mFtpClientUT.sendCommand("OPTS UTF8 ON");
                mFtpClientUT.enterLocalPassiveMode();
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.e("ftp","connection IOException");
        }
    }

    private void ConnectingWithFTP() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String ip=getFtpESIGN_Ip();
        try {
            mFtpClientUT = new FTPClient();
            mFtpClientUT.setControlEncoding("UTF-8");
            mFtpClientUT.setConnectTimeout(300 * 1000);
            mFtpClientUT.connect(InetAddress.getByName(ip),21);
        } catch (SocketException e) {
            e.printStackTrace();
            Log.e("ftp","connection SocketException");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e("ftp","connection UnknownHostException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ftp","connection IOException");
        }
    }
}
