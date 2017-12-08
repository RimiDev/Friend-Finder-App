package cs.dawson.dawsonelectriccurrents;

import android.app.Fragment;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class AcademicCalendarActivity extends AppCompatActivity {

    private static final String TAG = AcademicCalendarActivity.class.getName();
    private ImageButton load;
    private EditText yearInput;
    private RadioGroup rg;

    private static final String YEAR = "year";
    private static final String SEMESTER = "semester";
    private static final String RADIO = "radio";
    private static final String FALL = "fall";
    private static final String WINTER = "winter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_calendar);
        if (savedInstanceState != null) {
            loadInitialCalendar();
        } else {
            loadInitialCalendar();
        }
        yearInput = (EditText) findViewById(R.id.yearInput);
        rg = (RadioGroup) findViewById(R.id.radioGroup);
        rg.check(R.id.fallRadioBtn);
        load = (ImageButton) findViewById(R.id.loadBtn);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateAllInput()) {
                    sendDataToFragment();
                } else {
                    errorMessage();
                }
            }
        });
    }

    /**
     * Loads the initial webview with the current semester
     */
    private void loadInitialCalendar() {
        CalendarWVFragment fragment = new CalendarWVFragment();
        android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.academicCalendarFragment, fragment, fragment.getTag()).commit();
    }

    /**
     * Sends the data to the fragment
     */
    private void sendDataToFragment() {
        String semester = getSemesterValue();
        if (validateAllInput()) {
            Bundle bundle = new Bundle();
            bundle.putString(YEAR, yearInput.getText().toString());
            bundle.putString(SEMESTER, semester);
            CalendarWVFragment fragment = new CalendarWVFragment();
            fragment.setArguments(bundle);
            android.support.v4.app.FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.academicCalendarFragment, fragment, fragment.getTag()).commit();
        }
    }

    /**
     * Checks if all input was input
     *
     * @return
     */
    private boolean validateAllInput() {
        String year = ((EditText) findViewById(R.id.yearInput)).getText().toString();
        Log.i(TAG, YEAR + ": " + year);

        if (rg.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.noInput,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (!validateYear(year)) {
            return false;
        }

        return true;
    }

    /**
     * Validates the year input from the user
     *
     * @param year
     * @return
     */
    private boolean validateYear(String year) {
        try {
            int yearInt = Integer.parseInt(year);
            Log.i(TAG, "Year input: " + yearInt);
            if (yearInt < 1995 || yearInt > 2025)
                return false;
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalidYear,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    /**
     * Returns the semester
     *
     * @return
     */
    private String getSemesterValue() {
        String semester = "";
        int id = rg.getCheckedRadioButtonId();
        Log.i(TAG, "RadioGroup ID: " + id);
        Log.i(TAG, "Fall ID: " + R.id.fallRadioBtn);
        Log.i(TAG, "Winter ID: " + R.id.winterRadioBtn);

        if (id == R.id.fallRadioBtn)
            semester = FALL;
        else if (id == R.id.winterRadioBtn)
            semester = WINTER;

        return semester;
    }

    /**
     * Pops a toast message to validate the year
     */
    private void errorMessage() {
        Toast.makeText(this, R.string.invalidYear,
                Toast.LENGTH_LONG).show();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstaneState) {
        super.onSaveInstanceState(savedInstaneState);

        savedInstaneState.putString(YEAR, ((EditText) findViewById(R.id.yearInput)).getText().toString());
        savedInstaneState.putInt(RADIO, rg.getCheckedRadioButtonId());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String year = savedInstanceState.getString(YEAR);
        int radioBtnSelection = savedInstanceState.getInt(RADIO);

        ((TextView) findViewById(R.id.yearInput)).setText(year);
        if (radioBtnSelection == R.id.fallRadioBtn){
            rg.check(R.id.fallRadioBtn);
        } else if (radioBtnSelection == R.id.winterRadioBtn) {
            rg.check(R.id.winterRadioBtn);
        }


    }
}
