package usbong.android.builder.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.dd.processbutton.iml.ActionProcessButton;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;

import java.io.File;

import rx.Observer;
import usbong.android.builder.R;
import usbong.android.builder.controllers.UtreeController;
import usbong.android.builder.models.Utree;
import usbong.android.builder.utils.FileUtils;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link UtreeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UtreeFragment extends Fragment {

    public static final String TAG = UtreeFragment.class.getSimpleName();
    public static final String EXTRA_ID = "EXTRA_ID";
    private static final int NEW_TREE = -1;
    protected Utree currentUtree;

    private long id = NEW_TREE;
    private UtreeController controller;

    @InjectView(R.id.name)
    FloatLabeledEditText name;
    @InjectView(android.R.id.button1)
    ActionProcessButton saveButton;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args
     * @return A new instance of fragment UtreeFragment.
     */
    public static UtreeFragment newInstance(Bundle args) {
        UtreeFragment fragment = new UtreeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public UtreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();

        if (arguments != null) {
            id = arguments.getLong(EXTRA_ID, NEW_TREE);
        }
        int titleResId = R.string.new_tree;
        if (id != NEW_TREE) {
            titleResId = R.string.edit_tree;
        }
        getActivity().setTitle(getString(titleResId));
        controller = new UtreeController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_utree, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);
        saveButton.setMode(ActionProcessButton.Mode.PROGRESS);
        if (id != NEW_TREE) {
            controller.fetchUtree(id, new Observer<Utree>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, e.getMessage(), e);
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNext(Utree utree) {
                    SpannableString text = new SpannableString(Html.fromHtml(utree.name));
                    name.setText(text);
                }
            });
        }
    }

    public void renameUtreeFolder(String path, String oldName, String newName){
        String sourceXml = path + File.separator + oldName + File.separator + oldName + ".xml";
        String destinationXml = path + File.separator + oldName + File.separator + newName + ".xml";
        FileUtils.rename(sourceXml, destinationXml);
        String sourceFolder =  path + File.separator + oldName;
        String destinationFolder = path + File.separator + newName;
        FileUtils.rename(sourceFolder, destinationFolder);
    }

    @OnClick(android.R.id.button1)
    public void onSave() {
        //TODO: improve code
        saveButton.setProgress(1);
        controller.fetchUtree(id, new Observer<Utree>() {
            @Override
            public void onCompleted() {
                saveButton.setProgress(50);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                saveButton.setProgress(-1);
            }

            @Override
            public void onNext(final Utree utree) {
                final String oldUtreeName = utree.name;
                utree.name = name.getText().toString().trim();
                controller.save(utree, new Observer<Utree>() {
                    @Override
                    public void onCompleted() {
                        if(id != NEW_TREE){
                            String outputFolderLocation = getActivity().getFilesDir() + File.separator + "trees";
                            renameUtreeFolder(outputFolderLocation, oldUtreeName, utree.name);
                        }
                        //TODO: implement better transition?
                        saveButton.setProgress(100);
                        Toast.makeText(getActivity(), getString(R.string.utree_saved), Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }

                    @Override
                    public void onError(Throwable e){
                        Log.e(TAG, e.getMessage(), e);
                        saveButton.setProgress(-1);
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }

                    @Override
                    public void onNext(Utree o) {
                    }
                });
            }
        });
    }

}
