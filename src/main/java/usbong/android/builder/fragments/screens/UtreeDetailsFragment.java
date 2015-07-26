package usbong.android.builder.fragments.screens;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.activeandroid.query.Select;
import org.w3c.dom.Text;
import usbong.android.builder.R;
import usbong.android.builder.activities.UtreeDetailsActivity;
import usbong.android.builder.controllers.ScreenListController;
import usbong.android.builder.controllers.UtreeListController;
import usbong.android.builder.converters.UtreeConverter;
import usbong.android.builder.exceptions.NoStartingScreenException;
import usbong.android.builder.models.Screen;
import usbong.android.builder.models.Utree;
import android.net.Uri;
import usbong.android.builder.models.UtreeDetails;
import usbong.android.builder.utils.FileUtils;
import usbong.android.builder.utils.PackageUtils;
import usbong.android.builder.UploadUtree;
import android.provider.MediaStore;
import android.os.Environment;
import android.os.Build;
import android.provider.DocumentsContract;
import android.content.ContentUris;
import java.io.File;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * interface.
 */
public class UtreeDetailsFragment extends Fragment {

    private static final String TAG = UtreeDetailsFragment.class.getSimpleName();
    public static final String EXTRA_TREE_NAME = "EXTRA_TREE_NAME";
    public static final String EXTRA_UTREE = "EXTRA_UTREE";
    public static final String EXTRA_TREE_ID = "EXTRA_TREE_ID";
    private String treeName = "";
    private View view;
    private Button getFileButton, upload;
    private static final int ACTIVITY_CHOOSE_FILE = 1;
    private String filePath = "";
    private TextView iconPath, treeNameTV;
    private ProgressDialog dialog;
    private UtreeListController controller;
    private Utree utree;
    private long treeId;
    private String uploader = "";
    private String description = "";
    private String youtubeLink = "";
    private EditText uploaderET;
    private EditText descriptionET;
    private EditText youtubeET;
    private Uri uri;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args
     * @return A new instance of fragment UtreeFragment.
     */
    public static Fragment newInstance(Bundle args) {
        UtreeDetailsFragment fragment = new UtreeDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UtreeDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            treeName = getArguments().getString(EXTRA_TREE_NAME);

            treeId = getArguments().getLong(EXTRA_TREE_ID);
            if (treeId == -1) {
                throw new IllegalArgumentException("tree is required");
            }
            utree = new Select().from(Utree.class)
                    .where(Utree._ID + " = ?", treeId)
                    .executeSingle();
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_utree_details, container, false);
        upload = (Button) view.findViewById(R.id.upload);
        getFileButton = (Button) view.findViewById(R.id.getFile);
        iconPath = (TextView) view.findViewById(R.id.iconPath);
        treeNameTV = (TextView) view.findViewById(R.id.selectedUtreeName);
        uploaderET = (EditText) view.findViewById(R.id.uploaderName);
        descriptionET = (EditText) view.findViewById(R.id.description);
        youtubeET = (EditText) view.findViewById(R.id.youtubeLink1);

        treeNameTV.setText(treeName);

        getFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("image/*");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uploaderET.getText().toString().matches("") || descriptionET.getText().toString().matches("")
                        || youtubeET.getText().toString().matches("") || treeNameTV.getText().toString().matches("")) {
                    Toast.makeText(getActivity(), "Please complete all required fields", Toast.LENGTH_SHORT).show();
                } else {
                    uploadUtree();
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == getActivity().RESULT_OK) {
                    uri = data.getData();
                    filePath = uri.toString();
                    iconPath.setText(filePath);
                }
            }
        }
    }

    private void uploadUtree() {
        //TODO: Async task with dialogue to upload the utree to server
        //Check if Usbong andriod app is installed
        if (PackageUtils.isPackageInstalled("usbong.android", getActivity())) {
            //Uploads the zipped .utree to Usbong/Usbong_trees directory
            String treeFolderLocation = getActivity().getFilesDir() + File.separator + "trees" + File.separator + treeName + File.separator;
            Toast.makeText(getActivity(), treeFolderLocation, Toast.LENGTH_SHORT).show();
            String tempFolderLocation = getActivity().getFilesDir() + File.separator + "temp" + File.separator;
            String folderLocation = "/storage/emulated/legacy/usbong/usbong_trees/temp/";
            File file = new File(folderLocation);
            if (!file.exists()) {
                file.mkdir();
            } else {
                FileUtils.delete(folderLocation);
            }

            FileUtils.mkdir(treeFolderLocation);
            String xmlFileLocation = treeFolderLocation + treeName + ".xml";
            String zipFilePath = folderLocation + File.separator + treeName + ".utree";
            UtreeConverter converter = new UtreeConverter();
            converter.convert(utree, xmlFileLocation);
            FileUtils.delete(tempFolderLocation);
            FileUtils.copyAll(treeFolderLocation, tempFolderLocation + treeName + ".utree" + File.separator);
            FileUtils.zip(zipFilePath, tempFolderLocation);
            FileUtils.delete(tempFolderLocation);
        } else {
            try {
                Intent viewIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("https://play.google.com/store/apps/details?id=usbong.android"));
                startActivity(viewIntent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Unable to Connect Try Again...",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        // instantiate it within the onCreate method
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Uploading: " + treeName);
        dialog.setTitle("Saving trees...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        String folderLocation = "/storage/emulated/legacy/usbong/usbong_trees/temp/";
        UploadUtree u = new UploadUtree(getActivity(), dialog);
        uploader = uploaderET.getText().toString();
        description = descriptionET.getText().toString();
        youtubeLink = youtubeET.getText().toString();

        File screenshotFile = new File(filePath);
        String screenshot = getPath(getActivity(), uri);


        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        UtreeDetails utreeDetails = new UtreeDetails(uploader, folderLocation + treeName + ".utree",
                treeName, description, youtubeLink, "", screenshot, "", "", ts);
        Log.d(TAG, utreeDetails.toString());
        u.execute(utreeDetails);
//        u.execute(folderLocation + treeName + ".utree", uploader, description);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
