package com.nonsense.planttracker.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.AndroidConstants;
import com.nonsense.planttracker.tracker.impl.GenericRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class CollectPlantData extends AppCompatActivity {

    private GenericRecord record;
    private boolean applyToGroup;
    private long selectedGroup;

    private boolean showNotes;
    private TreeMap<String, Long> availableGroups;

    private ArrayList<String> mImages = new ArrayList<>();


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_plant_data);

        Intent intent = getIntent();

        record = (GenericRecord)intent.getSerializableExtra(
                AndroidConstants.INTENTKEY_GENERIC_RECORD);
        availableGroups = new TreeMap<>((Map<String, Long>)(intent.getSerializableExtra(
                AndroidConstants.INTENTKEY_AVAILABLE_GROUPS)));
        showNotes = (boolean)intent.getBooleanExtra(AndroidConstants.INTENTKEY_SHOW_NOTES,
                false);

        bindUi();
    }

    private void bindUi()   {
        bindTabs();

        bindGroupListSpinner();

        bindDataPoints();

        if (showNotes)  {
            bindNotes();
        }

        bindSharedControls();
    }

    private void bindTabs() {
        TabHost tabs = (TabHost) findViewById(R.id.tabHost);
        tabs.setup();
        tabs.setCurrentTab(0);

        TabHost.TabSpec dialogTab = tabs.newTabSpec("Tab1");
        dialogTab.setIndicator("Info");
        dialogTab.setContent(R.id.tab1);
        tabs.addTab(dialogTab);

        TabHost.TabSpec changeDateTab = tabs.newTabSpec("Tab2");
        changeDateTab.setIndicator("Set Date");
        changeDateTab.setContent(R.id.tab2);
        tabs.addTab(changeDateTab);

        TabHost.TabSpec changeTimeTab = tabs.newTabSpec("Tab3");
        changeTimeTab.setIndicator("Set Time");
        changeTimeTab.setContent(R.id.tab3);
        tabs.addTab(changeTimeTab);

        TabHost.TabSpec attachImagesTab = tabs.newTabSpec("Tab4");
        attachImagesTab.setIndicator("Attach Images");
        attachImagesTab.setContent(R.id.tab4);
        tabs.addTab(attachImagesTab);

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // Hide the keyboard if we're on any of the static tabs
                if (!tabId.equals("Tab1")) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm =
                                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });
    }

    private void bindGroupListSpinner() {
        Spinner groupListSpinner = (Spinner)findViewById(R.id.groupListSpinner);

        final ArrayList<String> groupNames = new ArrayList<>(availableGroups.keySet());

        if (groupNames.size() == 0)  {
            groupListSpinner.setEnabled(false);
            groupNames.add("No Groups");

            CheckBox applyToGroupCheckBox =
                    (CheckBox)findViewById(R.id.applyToGroupCheckbox);

            applyToGroupCheckBox.setEnabled(false);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, groupNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupListSpinner.setAdapter(adapter);

        groupListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = groupNames.get(position);
                if (name != null && availableGroups != null && availableGroups.containsKey(name))  {
                    selectedGroup = availableGroups.get(name);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGroup = 0;
            }
        });
    }

    private void bindDataPoints()   {
        LinearLayout tab1 = (LinearLayout)findViewById(R.id.tab1);

        LinearLayout heading = new LinearLayout(this);
        TextView headingTextView = new TextView(this);
        headingTextView.setTextSize(34);
        headingTextView.setTypeface(Typeface.DEFAULT_BOLD);

        headingTextView.setText(record.displayName);
        heading.addView(headingTextView);
        tab1.addView(heading);

        for(String dataPoint : record.dataPoints.keySet())  {
            Object dpo = record.getDataPoint(dataPoint);
            if (dpo instanceof String)   {
                tab1.addView(bindStringInput(dataPoint, (String)dpo));
            }
            else if (dpo instanceof Integer)    {
                tab1.addView(bindIntegerInput(dataPoint, (Integer)dpo));
            }
            else if (dpo instanceof  Double)    {
                tab1.addView(bindDoubleInput(dataPoint, (Double)dpo));
            }
        }
    }

    private void bindNotes()    {
        final LinearLayout tab1 = (LinearLayout)findViewById(R.id.tab1);

        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        final TextView notesTextView = createNewLabel();
        notesTextView.setText("Notes");
        container.addView(notesTextView);

        final EditText editText = new EditText(this);
        container.addView(editText);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        editText.setSingleLine(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                record.notes = editText.getText().toString();
            }
        });

        tab1.addView(container);
    }

    private void bindSharedControls()   {
        record.time = Calendar.getInstance();

        final DatePicker datePicker = (DatePicker)findViewById(R.id.eventDatePicker);
        datePicker.updateDate(record.time.get(Calendar.YEAR), record.time.get(Calendar.MONTH),
                record.time.get(Calendar.DAY_OF_MONTH));

        final TimePicker timePicker = (TimePicker)findViewById(R.id.eventTimePicker);
        timePicker.setHour(record.time.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(record.time.get(Calendar.MINUTE));

        final CheckBox applyToGroupCheckBox = (CheckBox)findViewById(R.id.applyToGroupCheckbox);
        applyToGroupCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                applyToGroup = isChecked;
            }
        });

        final Button cameraButton = (Button)findViewById(R.id.openCameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(CollectPlantData.this,
                        PlantCam.class);

                startActivityForResult(cameraIntent, AndroidConstants.ACTIVITY_PLANT_CAM);
            }
        });

        final Button galleryButton = (Button)findViewById(R.id.attachImagesButton);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

                startActivityForResult(i, 5);
            }
        });

        final Button okButton = (Button)findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endActivity();
            }
        });

        final Button cancelButton = (Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelActivity();
            }
        });
    }

    private LinearLayout bindStringInput(final String key, String value)   {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView label = createNewLabel();
        label.setText(key);
        layout.addView(label);

        final EditText editText = createNewInput();
        layout.addView(editText);
        editText.setText(value);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                record.setDataPoint(key, editText.getText().toString());
            }
        });

        return layout;
    }

    private LinearLayout bindIntegerInput(final String key, Integer value)   {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView label = createNewLabel();
        label.setText(key);
        layout.addView(label);

        final EditText editText = createNewInput();
        layout.addView(editText);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setText(String.valueOf(value));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (!editText.getText().toString().equals(""))  {
                        Integer val = Integer.parseInt(editText.getText().toString());
                        record.setDataPoint(key, val);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //TODO add up and down button, whole number adjustment

        return layout;
    }

    private LinearLayout bindDoubleInput(final String key, Double value) {
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        final TextView label = createNewLabel();
        label.setText(key);
        layout.addView(label);

        final EditText editText = createNewInput();
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setText(String.valueOf(value));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    if (!editText.getText().toString().equals("")) {
                        Double val = Double.parseDouble(editText.getText().toString());
                        record.setDataPoint(key, val);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        layout.addView(editText);

        //TODO add up and down button, .1 adjustment

        return layout;
    }

    private TextView createNewLabel()   {
        TextView label = new TextView(this);

        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT);
        label.setLayoutParams(params);
        label.setGravity(Gravity.CENTER);

        return label;
    }

    private EditText createNewInput()   {
        EditText editText = new EditText(this);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(params);

        return editText;
    }

    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) {
        super.onActivityResult(requestCode, resultCode, returnedIntent);
        switch (requestCode) {

            case AndroidConstants.ACTIVITY_PLANT_CAM:
                if (resultCode == Activity.RESULT_OK) {
                    ArrayList<String> selectedFiles = (ArrayList<String>)returnedIntent.
                            getSerializableExtra(AndroidConstants.INTENTKEY_SELECTED_FILES);

                    mImages.addAll(selectedFiles);
                }
                break;
        }
    }

    private void cancelActivity()   {
        if (record.images != null)  {
            for(String s : mImages)   {
                File f = new File(s);
                f.delete();
            }
        }

        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void endActivity()  {
        DatePicker datePicker = (DatePicker)findViewById(R.id.eventDatePicker);
        TimePicker timePicker = (TimePicker)findViewById(R.id.eventTimePicker);

        Calendar cal = Calendar.getInstance();
        cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                timePicker.getHour(), timePicker.getMinute());

        record.time = cal;

        Intent retInt = new Intent();

        retInt.putExtra(AndroidConstants.INTENTKEY_GENERIC_RECORD, record);
        retInt.putExtra(AndroidConstants.INTENTKEY_APPLY_TO_GROUP, applyToGroup);
        retInt.putExtra(AndroidConstants.INTENTKEY_SELECTED_GROUP, selectedGroup);
        retInt.putExtra(AndroidConstants.INTENTKEY_SELECTED_FILES, mImages);

        setResult(Activity.RESULT_OK, retInt);
        finish();
    }
}