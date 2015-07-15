package usbong.android.builder.fragments.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import usbong.android.builder.utils.FileUtils;
import usbong.android.builder.utils.PackageUtils;
import usbong.android.builder.UploadUtree;

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
    private EditText uploaderET;
    private EditText descriptionET;
    /**
     * The fragment's ListView/GridView.
     */
    @InjectView(android.R.id.list)
    AbsListView listView;
    @InjectView(android.R.id.empty)
    TextView emptyView;
    @InjectView(R.id.search)
    EditText search;

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
        treeNameTV.setText(treeName);

        getFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("file/*");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadUtree();
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
                    Uri uri = data.getData();
                    filePath = uri.getPath();
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
            if (!file.exists())
                file.mkdir();

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
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        String folderLocation = "/storage/emulated/legacy/usbong/usbong_trees/temp/";
        UploadUtree u = new UploadUtree(getActivity(), dialog);
        uploader = uploaderET.getText().toString();
        description = descriptionET.getText().toString();
        u.execute(folderLocation + treeName + ".utree", uploader, description);
    }
}
