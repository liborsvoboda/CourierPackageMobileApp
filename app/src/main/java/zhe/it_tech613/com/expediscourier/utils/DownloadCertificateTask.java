package zhe.it_tech613.com.cmpcourier.utils;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import zhe.it_tech613.com.cmpcourier.model.Status;

import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Ip;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Login;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Pass;

public class DownloadCertificateTask extends AsyncTask<Void, Void, Status> {
    private ConnectionInterface connectionInterface;
    private FTPClient mFtpClientCT;
    @Override
    protected zhe.it_tech613.com.cmpcourier.model.Status doInBackground(Void... voids) {
        connectionInterface.downloadListener(zhe.it_tech613.com.cmpcourier.model.Status.ConnectingFTP);
        ConnectingWithFTP();
        connectionInterface.downloadListener(zhe.it_tech613.com.cmpcourier.model.Status.ConnectedFTP);
        try {
            LogonToFTP();
            String server_file_Path = PreferenceManager.getFtpCertificatePath();
            connectionInterface.downloadListener(zhe.it_tech613.com.cmpcourier.model.Status.AccessDirectory);
            changeWorkingDir(server_file_Path);
            int num_success = 0;
            connectionInterface.downloadListener(zhe.it_tech613.com.cmpcourier.model.Status.GetFiles);
            if (mFtpClientCT.listNames().length==0) return zhe.it_tech613.com.cmpcourier.model.Status.NoFiles;
            for (String remoteFile:mFtpClientCT.listNames()) {
                if (remoteFile.equals(".") || remoteFile.equals("..")) continue;
                if (!remoteFile.contains("000"+PreferenceManager.getID()+".pfx")) continue;
                Log.e("files", remoteFile);
                File downloadFile = new File(PreferenceManager.downLoadFolder + File.separator + "MyCertificate.pfx");
                downloadFile.createNewFile();
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
                boolean success = mFtpClientCT.retrieveFile(remoteFile, outputStream);
                if (success) {
                    num_success+=1;
                    connectionInterface.updateListener(String.format(Constant.czlanguageStrings.getDOWNLOAD5(),num_success,0), 100);
                }
                outputStream.close();
            }
            return zhe.it_tech613.com.cmpcourier.model.Status.Success;
        } catch (Exception e) {
            e.printStackTrace();
            return zhe.it_tech613.com.cmpcourier.model.Status.Failed;
        }
    }

    @Override
    protected void onPostExecute(zhe.it_tech613.com.cmpcourier.model.Status aBoolean) {
        connectionInterface.downloadListener(aBoolean);
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            mFtpClientCT.logout();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onGetResult(ConnectionInterface connectionInterface){
        this.connectionInterface = connectionInterface;
    }

    private boolean changeWorkingDir(String dir){
        try {
            if (dir.startsWith("/")) dir = dir.substring(1);
            String[] pathSnips = dir.split("/");
            StringBuilder currentDir= new StringBuilder();
            for (String pathSnip : pathSnips) {
                currentDir.append("/").append(pathSnip);
                if (!mFtpClientCT.changeWorkingDirectory(currentDir.toString())) {
                    mFtpClientCT.makeDirectory(currentDir.toString());
                    mFtpClientCT.changeWorkingDirectory(currentDir.toString());
                    Log.e("folder creation", "ok " + currentDir.toString());
                } else Log.e("folder change", "ok " + currentDir.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }

    public interface ConnectionInterface {
        void downloadListener(zhe.it_tech613.com.cmpcourier.model.Status result);
        void updateListener(String string, int progress);
    }

    private void LogonToFTP(){
        String userName=getFtpESIGN_Login();
        String pass=getFtpESIGN_Pass();
        try {
            mFtpClientCT.login(userName, pass);
            if (FTPReply.isPositiveCompletion(mFtpClientCT.getReplyCode())) {
                mFtpClientCT.setFileType(FTP.BINARY_FILE_TYPE);
                mFtpClientCT.sendCommand("OPTS UTF8 ON");
                mFtpClientCT.enterLocalPassiveMode();
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
        boolean status;
        try {
            mFtpClientCT = new FTPClient();
            mFtpClientCT.setControlEncoding("UTF-8");
            mFtpClientCT.setConnectTimeout(30 * 1000);
            mFtpClientCT.connect(InetAddress.getByName(ip),21);
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
