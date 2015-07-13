package usbong.android.builder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import usbong.android.builder.controllers.Controller;

/**
 * Created by talusan on 7/13/2015.
 */
public class UploadUtree extends AsyncTask<String, Integer, String> {
    private static String TAG = "usbong.community.IterateDownload";
    private int responseCode = 0;
    private String responseString = "";
    private HttpURLConnection conn = null;
    private DataOutputStream dOut = null;
    private String lineEnd = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "*****";
    private Context c;
    private ProgressDialog d;
    private PowerManager.WakeLock mWakeLock;

    public UploadUtree(Context c, ProgressDialog d) {
        this.c = c;
        this.d = d;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        if (d != null)
            d.show();
    }

    @SuppressWarnings("resource")
    protected String doInBackground(String... sUrl) {

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBuffersize = 1024 * 1024;

        String path = sUrl[0];
        String name = sUrl[1];
//	    String path1 = sUrl[1];
        File file = new File(path);
//	    File file1 = new File(path1);

        try {
            FileInputStream fileIn = new FileInputStream(file);
//	        FileInputStream fileIn1 = new FileInputStream(file1);
            URL url = new URL("http://192.168.0.12/usbong/build-upload.php");

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("fileupload", path);

            //Added file1
//	        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//	        conn.setRequestProperty("fileupload1", path1);
            //End

            dOut = new DataOutputStream(conn.getOutputStream());
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"fileupload\";filename=\"" + path + "\"" + lineEnd);
            dOut.writeBytes(lineEnd);

            bytesAvailable = fileIn.available();
            bufferSize = Math.min(bytesAvailable, maxBuffersize);
            buffer = new byte[bufferSize];
            bytesRead = fileIn.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dOut.write(buffer, 0, bufferSize);
                bytesAvailable = fileIn.available();
                bufferSize = Math.min(bytesAvailable, maxBuffersize);
                bytesRead = fileIn.read(buffer, 0, bufferSize);
            }

            dOut.writeBytes(lineEnd);

//	        //FILE UPLOAD 2 TEST START
//	        dOut.writeBytes(twoHyphens + boundary + lineEnd);
//	        dOut.writeBytes("Content-Disposition: form-data; name=\"fileupload1\";filename=\"" + path1 + "\"" + lineEnd);
//	        dOut.writeBytes(lineEnd);
//
//	        bytesAvailable = fileIn1.available();
//	        bufferSize = Math.min(bytesAvailable, maxBuffersize);
//	        buffer = new byte[bufferSize];
//	        bytesRead = fileIn1.read(buffer, 0, bufferSize);
//
//	        while(bytesRead > 0)
//	        {
//	            dOut.write(buffer, 0, bufferSize);
//	            bytesAvailable = fileIn1.available();
//	            bufferSize = Math.min(bytesAvailable, maxBuffersize);
//	            bytesRead = fileIn1.read(buffer, 0, bufferSize);
//	        }
//
//	        dOut.writeBytes(lineEnd);
//	        //FILE UPLOAD 2 TEST END

            //PARAMETER FIELD NAME
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"JP\"" + lineEnd);
            dOut.writeBytes(lineEnd);
            dOut.writeBytes(name); // mobile_no is String variable
            dOut.writeBytes(lineEnd);
            //PARAMETER END

            //PARAMETER FIELD NAME
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"description\"" + lineEnd);
            dOut.writeBytes(lineEnd);
            dOut.writeBytes("This is the description"); // mobile_no is String variable
            dOut.writeBytes(lineEnd);
            //PARAMETER END

            dOut.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                fileIn.close();
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            } else {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    responseString += line;
                }
            }

            Log.i("UPLOAD", "HTTP Response is: " + conn.getResponseCode() + ": " + conn.getResponseMessage());

            if (responseCode == 200) {
                fileIn.close();
                return null;
            }

            fileIn.close();
            dOut.flush();
            dOut.close();
        } catch (MalformedURLException e) {
            return e.toString();
        } catch (Exception e) {
            return e.toString();
        }

        return responseString + "";
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, "Response:" + result);
        if (d != null)
            d.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        if (d != null) {
            d.setIndeterminate(false);
            d.setMax(100);
            d.setProgress(progress[0]);
        }
    }
}
