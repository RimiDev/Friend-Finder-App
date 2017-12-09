package cs.dawson.dawsonelectriccurrents;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import cs.dawson.dawsonelectriccurrents.database.FriendFinderDBHelper;
import cs.dawson.dawsonelectriccurrents.utilities.UserLoader;

import static cs.dawson.dawsonelectriccurrents.utilities.Options.*;

public class SettingsActivity extends MenuActivity {

    private static final String TAG = SettingsActivity.class.getName();
    private FriendFinderDBHelper database;
    private final String USERS_PREFS = "user";
    private final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private TextView password;

    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "pw";
    private static final String LASTUPDATED = "lastUpdated";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new FriendFinderDBHelper(this);
        database.getWritableDatabase();
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(USERS_PREFS, MODE_PRIVATE);
        if (prefs != null) {
            // Edit the textviews for the current shared preferences
            ((TextView) findViewById(R.id.editFirstName)).setText(prefs.getString(FIRSTNAME, ""));
            ((TextView) findViewById(R.id.editLastName)).setText(prefs.getString(LASTNAME, ""));
            ((TextView) findViewById(R.id.editEmail)).setText(prefs.getString(EMAIL, ""));
            ((TextView) findViewById(R.id.editPassword)).setText(prefs.getString(PASSWORD, ""));
            ((TextView) findViewById(R.id.lastUpdatedSp)).setText(prefs.getString(LASTUPDATED, ""));
        }
    }

    /**
     * Sets the database
     * @param database
     */
    public void setDatabase(FriendFinderDBHelper database) { this.database = database; }

    /**
     * Saves the information from the form into the database
     * All fields must be entered in order to save the information
     * @param view
     */
    public void saveSettings(View view) {
        // Get the information
        String firstName = ((EditText) findViewById(R.id.editFirstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.editLastName)).getText().toString();
        String email = ((EditText) findViewById(R.id.editEmail)).getText().toString();
        String password = ((EditText) findViewById(R.id.editPassword)).getText().toString();

        if (validateInformation(firstName, lastName, email, password)) {
            if (getSharedPreferences(USERS_PREFS, MODE_PRIVATE).getString(EMAIL, "") != "") {
                new UserLoader(MODIFY_USER, this, database, new String[] { firstName, lastName, email, password }).execute();
            } else {
                new UserLoader(ADD_USER, this, database, new String[] { firstName, lastName, email, password}).execute();
            }
            finish();
        } else {
            showInvalidInput();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.exitSettings);
        builder.setMessage(R.string.exitDialog);
        builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}});
        builder.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }});
        builder.show();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    /**
     * Validates if all the information in the form is correct to our standards
     * @param firstName
     * @param lastName
     * @param email
     * @param password
     * @return
     */
    private boolean validateInformation(String firstName, String lastName, String email, String password) {
        if (firstName == "" || lastName == "" || email == "" || password == "")
            return false;

        if (!email.matches(EMAIL_REGEX))
            return false;

        return true;
    }

    /**
     * Validates user input
     */
    private void showInvalidInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.error);
        builder.setMessage(R.string.invalid);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}}).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstaneState) {
        super.onSaveInstanceState(savedInstaneState);

        savedInstaneState.putString(FIRSTNAME, ((EditText) findViewById(R.id.editFirstName)).getText().toString());
        savedInstaneState.putString(LASTNAME, ((EditText) findViewById(R.id.editLastName)).getText().toString());
        savedInstaneState.putString(EMAIL, ((EditText) findViewById(R.id.editEmail)).getText().toString());
        savedInstaneState.putString(PASSWORD, ((EditText) findViewById(R.id.editPassword)).getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String fn = savedInstanceState.getString(FIRSTNAME);
        String ln = savedInstanceState.getString(LASTNAME);
        String email = savedInstanceState.getString(EMAIL);
        String pw = savedInstanceState.getString(PASSWORD);

        ((TextView) findViewById(R.id.editFirstName)).setText(fn);
        ((TextView) findViewById(R.id.editLastName)).setText(ln);
        ((TextView) findViewById(R.id.editEmail)).setText(email);
        ((TextView) findViewById(R.id.editPassword)).setText(pw);

    }


}
