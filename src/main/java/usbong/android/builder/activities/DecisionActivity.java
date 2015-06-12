package usbong.android.builder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;
import usbong.android.builder.R;
import usbong.android.builder.fragments.ScreenFragment;
import usbong.android.builder.fragments.dialogs.DecisionListDialogFragment;
import usbong.android.builder.utils.StringUtils;

import java.util.ArrayList;

public class DecisionActivity extends ActionBarActivity {

    private static final String TAG = DecisionActivity.class.getSimpleName();
    public static final String EXTRA_SCREEN_PARENT_ID = "EXTRA_SCREEN_PARENT_ID";
    public static final String EXTRA_TREE_ID = "EXTRA_TREE_ID";
    public static final String EXTRA_POSSIBLE_DECISIONS = "EXTRA_POSSIBLE_DECISIONS";
    public static final String EXTRA_CONDITION_PREFIX = "EXTRA_CONDITION_PREFIX";
    public static final String DEFAULT_CONDITION_PREFIX = "DECISION";
    @InjectView(R.id.decision)
    FloatLabeledEditText decision;
    @InjectView(android.R.id.button1)
    Button createScreen;

    private ArrayList<String> decisions = new ArrayList<String>();
    private long screenParentId = -1;
    private long treeId = -1;
    private String conditionPrefix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_decision);

        ButterKnife.inject(this);

        if (getIntent() != null) {
            treeId = getIntent().getLongExtra(EXTRA_TREE_ID, -1);
            screenParentId = getIntent().getLongExtra(EXTRA_SCREEN_PARENT_ID, -1);
            conditionPrefix = getIntent().getStringExtra(EXTRA_CONDITION_PREFIX);
            if(conditionPrefix == null) {
                conditionPrefix = DEFAULT_CONDITION_PREFIX;
            }
            decisions = getIntent().getStringArrayListExtra(EXTRA_POSSIBLE_DECISIONS);
        }
        if (screenParentId == -1) {
            throw new IllegalArgumentException("screen id is required");
        }
        if (treeId == -1) {
            throw new IllegalArgumentException("tree id is required");
        }
    }

    @OnClick(android.R.id.button1)
    public void onCreateScreen() {
        if (StringUtils.isEmpty(decision.getText().toString().trim())) {
            Log.w(TAG, "Empty decision text");
            Toast.makeText(this, "Please input a decision", Toast.LENGTH_SHORT).show();
            return;
        }
        if(decisions != null && !decisions.isEmpty()) {
            boolean isValidDecisionText = false;
            for(String decisionText : decisions) {
                if(decision.getTextString().trim().equals(decisionText)) {
                    isValidDecisionText = true;
                    break;
                }
            }
            if(!isValidDecisionText) {
                Log.w(TAG, "Invalid decision text");
                Toast.makeText(this, "Please choose a decision text by selecting one from the list", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent(this, ScreenActivity.class);
        intent.putExtra(ScreenFragment.EXTRA_TREE_ID, treeId);
        intent.putExtra(ScreenFragment.EXTRA_PARENT_ID, screenParentId);
        intent.putExtra(ScreenFragment.EXTRA_RELATION_CONDITION, conditionPrefix
                + "~" + decision.getText().toString().trim());
        startActivity(intent);
        finish();
    }

    @OnClick(android.R.id.button2)
    public void onOpenDialog() {
        DecisionListDialogFragment dialog = DecisionListDialogFragment.newInstance(decisions);
        dialog.setCallback(new DecisionListDialogFragment.Callback() {
            @Override
            public void onSelect(String text) {
                decision.setText(text);
            }
        });
        dialog.show(getSupportFragmentManager(), "DIALOG");
    }
}