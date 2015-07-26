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
import usbong.android.builder.models.UtreeDetails;

/**
 * Created by talusan on 7/13/2015.
 */
public class UploadUtree extends AsyncTask<UtreeDetails, Integer, String> {
    private static String TAG = "android.builder.UploadUtree";
    private String responseString = "";

    private Context mContext;
    private ProgressDialog mDialog;
    private int progress = 0;
    private UtreeDetails utree;

    public UploadUtree(Context context, ProgressDialog dialog) {
        this.mContext = context;
        this.mDialog = dialog;
    }

    @Override
    protected void onPreExecute() {
        PowerManager.WakeLock mWakeLock;
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        if (mDialog != null)
            mDialog.show();
    }

    @SuppressWarnings("resource")
    protected String doInBackground(UtreeDetails... utrees) {
        utree = utrees[0];
        HttpURLConnection conn = null;
        DataOutputStream dOut = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int responseCode = 0;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBuffersize = 512;

        String utreeFilePath = utree.getFILEPATH();
        String uploader = utree.getUPLOADER();
        String description = utree.getDESCRIPTION();
        String screenshotFilePath = utree.getSCREENSHOT();
        String youtubeLink = utree.getYOUTUBELINK();
        String timestamp = utree.getDATEUPLOADED();

        File utreeFile = new File(utreeFilePath);
        File screenshotFile = new File(screenshotFilePath);

        try {
            FileInputStream utreeFileIn = new FileInputStream(utreeFile);
            FileInputStream screebshotFileIn = new FileInputStream(screenshotFile);
            URL url = new URL("http://192.168.0.12/usbong/build-upload.php");
//            URL url = new URL("http://shrimptalusan.hostei.com/usbong/build-upload.php");

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("utreeFile", utreeFilePath);

            //Added file1
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("screenshotFile", screenshotFilePath);
            //End

            dOut = new DataOutputStream(conn.getOutputStream());
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"utreeFile\";filename=\"" + utreeFilePath + "\"" + lineEnd);
            dOut.writeBytes(lineEnd);

            bytesAvailable = utreeFileIn.available();
            bufferSize = Math.min(bytesAvailable, maxBuffersize);
            buffer = new byte[bufferSize];
            bytesRead = utreeFileIn.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                progress += bytesRead;
                dOut.write(buffer, 0, bufferSize);
                bytesAvailable = utreeFileIn.available();
                publishProgress((int) ((progress * 100) / (utreeFile.length())));
                bufferSize = Math.min(bytesAvailable, maxBuffersize);
                buffer = new byte[bufferSize]; //TEST
                bytesRead = utreeFileIn.read(buffer, 0, bufferSize);
            }

            dOut.writeBytes(lineEnd);

//	        //FILE UPLOAD 2 TEST START
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"screenshotFile\";filename=\"" + screenshotFilePath + "\"" + lineEnd);
            dOut.writeBytes(lineEnd);

            bytesAvailable = screebshotFileIn.available();
            bufferSize = Math.min(bytesAvailable, maxBuffersize);
            buffer = new byte[bufferSize];
            bytesRead = screebshotFileIn.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dOut.write(buffer, 0, bufferSize);
                bytesAvailable = screebshotFileIn.available();
                bufferSize = Math.min(bytesAvailable, maxBuffersize);
                bytesRead = screebshotFileIn.read(buffer, 0, bufferSize);
            }

            dOut.writeBytes(lineEnd);
//	        //FILE UPLOAD 2 TEST END

            //PARAMETER FIELD NAME
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"uploader\"" + lineEnd);
            dOut.writeBytes(lineEnd);
            dOut.writeBytes(uploader); // mobile_no is String variable
            dOut.writeBytes(lineEnd);
            //PARAMETER END

            //PARAMETER FIELD NAME
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"description\"" + lineEnd);
            dOut.writeBytes(lineEnd);
            dOut.writeBytes(description); // mobile_no is String variable
            dOut.writeBytes(lineEnd);
            //PARAMETER END

            //PARAMETER FIELD NAME
            dOut.writeBytes(twoHyphens + boundary + lineEnd);
            dOut.writeBytes("Content-Disposition: form-data; name=\"youtubelink\"" + lineEnd);
            dOut.writeBytes(lineEnd);
            dOut.writeBytes(youtubeLink); // mobile_no is String variable
            dOut.writeBytes(lineEnd);
            //PARAMETER END

            dOut.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                utreeFileIn.close();
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
                utreeFileIn.close();
                return null;
            }

            utreeFileIn.close();
            publishProgress(100);
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
        if (mDialog != null)
            mDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        if (mDialog != null) {
            mDialog.setIndeterminate(false);
            mDialog.setMax(100);
            mDialog.setProgress(progress[0]);
        }
    }
}
