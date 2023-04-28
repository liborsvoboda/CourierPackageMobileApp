package zhe.it_tech613.com.cmpcourier.utils;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Login;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpESIGN_Pass;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpIp;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getFtpPath;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getLocation;
import static zhe.it_tech613.com.cmpcourier.utils.PreferenceManager.getName;

public class UploadPhotosTask extends AsyncTask<Void, Void, List<Boolean>> {
    private UploadListener uploadListener;
    private FTPClient mFtpClientUP;

    @Override
    protected List<Boolean> doInBackground(Void... voids) {
        connnectingwithFTP();
        try{
            LogonToFTP();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int length = PreferenceManager.getInstance().file_names.size();
        List<Boolean> statuses = new ArrayList<>(length);
        statuses.add(false);
        statuses.add(false);
        statuses.add(false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String date_string = dateFormat.format(new Date());
        String[] date_str_array = dateFormat.format(new Date()).split("/");
        String server_file_Path = "";

        try {
            server_file_Path = getFtpPath() + getLocation() + "/" + date_string + "/" + new String(getName().getBytes(), "ISO-8859-2").replace(" ", "_");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            Log.e("name", getName());
            changeWorkingDir(getFtpPath());
            changeWorkingDir(getFtpPath() + getLocation());
            changeWorkingDir(getFtpPath() + getLocation() + "/" + date_str_array[0]);
            changeWorkingDir(getFtpPath() + getLocation() + "/" + date_str_array[0] + "/" + date_str_array[1]);
            changeWorkingDir(getFtpPath() + getLocation() + "/" + date_str_array[0] + "/" + date_str_array[1] + "/" + date_str_array[2]);
            if (!mFtpClientUP.changeWorkingDirectory(server_file_Path)) {
                mFtpClientUP.makeDirectory(server_file_Path);
                Log.e("folder creation 3", "ok");
                mFtpClientUP.changeWorkingDirectory(server_file_Path);
//                Toast.makeText(this,"folder creation 3",Toast.LENGTH_LONG).show();
            }
            FileInputStream srcFileStream = null;
            for (int i = 0; i < length; i++) {
                String full_path = server_file_Path + "/" + PreferenceManager.getInstance().file_names.get(i) + ".png";
//                    FTPFile[] remoteFiles = mFtpClient.listFiles(server_file_Path );
//                    if (remoteFiles.length > 0)
//                    {
//                        System.out.println("File " + remoteFiles[0].getName() + " exists");
//                        for (FTPFile ftpFile:remoteFiles){
//
//                        }
//                        mFtpClient.deleteFile(full_path);
//                    }
                srcFileStream = new FileInputStream(PreferenceManager.getInstance().files.get(i));
                mFtpClientUP.changeWorkingDirectory(full_path);
                boolean status = mFtpClientUP.storeFile(full_path, srcFileStream);
                statuses.set(i, status);
//                Toast.makeText(this,"upload photo "+i+" "+full_path+status,Toast.LENGTH_LONG).show();
                Log.e("Status", full_path + " " + status);
            }
            srcFileStream.close();
            return statuses;
        } catch (Exception e) {
            e.printStackTrace();
            return statuses;
        }
    }

    @Override
    protected void onPostExecute(List<Boolean> aBoolean) {
        uploadListener.uploadlistener(aBoolean);
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            mFtpClientUP.logout();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onGetResult(UploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    private void changeWorkingDir(String dir) {
        try {
            if (!mFtpClientUP.changeWorkingDirectory(dir)) {
                mFtpClientUP.makeDirectory(dir);
                mFtpClientUP.changeWorkingDirectory(dir);
                Log.e("folder creation", "ok " + dir);
//                Toast.makeText(this,"folder creation 2",Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface UploadListener{
        void uploadlistener(List<Boolean> result);
    }

    private void LogonToFTP() {
        String userName=getFtpESIGN_Login();
        String pass=getFtpESIGN_Pass();
        try {
            mFtpClientUP.login(userName, pass);
            if (FTPReply.isPositiveCompletion(mFtpClientUP.getReplyCode())) {
                mFtpClientUP.setFileType(FTP.BINARY_FILE_TYPE);
                mFtpClientUP.sendCommand("OPTS UTF8 ON");
                mFtpClientUP.enterLocalPassiveMode();
            }
        } catch (IOException e){
            e.printStackTrace();
            Log.e("ftp","connection IOException");
        }
    }

    private void connnectingwithFTP() {
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String ip=getFtpIp();
        try {
            mFtpClientUP = new FTPClient();
            mFtpClientUP.setControlEncoding("UTF-8");
            mFtpClientUP.setConnectTimeout(30 * 1000);
            mFtpClientUP.connect(InetAddress.getByName(ip),21);
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
