package in.ac.iitm.placement.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    ProgressDialog progress;
    LinearLayout signin, ldaplogin;
    EditText username, password;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    private boolean mSignInClicked ;
    private boolean mIntentInProgress;
    //RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //getSupportActionBar().hide();
        signin = (LinearLayout) findViewById(R.id.gbutton);
        ldaplogin = (LinearLayout) findViewById(R.id.ldaplogin);
        username = (EditText) this.findViewById(R.id.rollno);
        password = (EditText) this.findViewById(R.id.password);
        password.setTypeface(Typeface.DEFAULT_BOLD);
        username.setTypeface(Typeface.DEFAULT_BOLD);
        // container =(RelativeLayout) findViewById(R.id.container);
        // Button revoke =(Button) findViewById(R.id.revoke);
        ldaplogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.getText().toString().trim().length() > 0&&password.getText().toString().trim().length()>0){
                    if(Utils.isNetworkAvailable(getBaseContext())){
                        progress = new ProgressDialog(LoginActivity.this);
                        progress.setCancelable(false);
                        progress.setMessage("Logging In...");
                        progress.show();
                        PlacementLdaplogin(getBaseContext());
                    }else {
                        MakeSnSnackbar("No internet connection");
                    }

                }else{
                    MakeSnSnackbar("Enter your username and password");
                }

            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    progress = new ProgressDialog(LoginActivity.this);
                    progress.setCancelable(false);
                    progress.setMessage("Logging In...");
                    progress.show();
                    signin.setEnabled(false);
                    mGoogleApiClient.connect();
                    signin.setEnabled(false);

                }

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {

        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                    Log.d("hete", "on failed");
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.

        signin.setEnabled(true);
        mSignInClicked = false;

        String personName = "", personId = "", personEmail = "", personRollno = "";

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi
                    .getCurrentPerson(mGoogleApiClient);
            personName = currentPerson.getDisplayName();
            personId = currentPerson.getId();
            personEmail = Plus.AccountApi.getAccountName(mGoogleApiClient);
        }
        if (personEmail.endsWith("smail.iitm.ac.in")) {
            Utils.saveprefString("rollno", personEmail.substring(0, 8).toLowerCase(), getBaseContext());
            Utils.saveprefString("personname", personName, getBaseContext());
            Utils.saveprefString("personemail", personEmail.toLowerCase(), getBaseContext());
            Utils.saveprefString("department", personEmail.substring(0, 2).toLowerCase(), getBaseContext());
            Log.d("email", personEmail);
            Log.d("rollno", personEmail.substring(0, 8).toLowerCase());
            Log.d("rollno", personEmail.substring(0, 2).toLowerCase());
            PlacementLogin();
        } else {
            MakeSnSnackbar("Account you login with is not smail try again using smail account :)");
            signOutFromGplus();
            signin.setEnabled(true);

        }
        //Toast.makeText(this, "User" + personName + " is connected!" + Plus.AccountApi.getAccountName(mGoogleApiClient), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        Log.d("hete", "on suspend");

    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {

            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
                Log.d("hete", "on result");
                progress.dismiss();
                signin.setEnabled(true);
                signOutFromGplus();

            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }

    /**
     * Sign-out from google
     */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            Log.d("logout", "logout");
        }
        if (progress != null) {
            progress.dismiss();

        }
    }

    private void revokeGplusAccess() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e("", "User access revoked!");
                            mGoogleApiClient.connect();
                        }

                    });
        }
    }

    private void PlacementLdaplogin(final Context context) {
        RequestQueue queue = Volley.newRequestQueue(this);

        username = (EditText) this.findViewById(R.id.rollno);
        password = (EditText) this.findViewById(R.id.password);
        String url = getString(R.string.dominename) + "mobldaplogin.php";
        Utils.saveprefString("rollno", username.getText().toString().toLowerCase(), getBaseContext());
        Utils.saveprefString("department", username.getText().toString().substring(0, 2).toLowerCase(), getBaseContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String responseBody = response.toString();
                        responseBody = responseBody.replaceAll("\\s", "");
                        if (responseBody.equals("1")) {
                            Log.d("valid login", responseBody + "ok");
                            Intent downloadIntent;
                            downloadIntent = new Intent(getBaseContext(), MainActivity.class);
                            startActivity(downloadIntent);
                            Utils.saveprefBool("logedin", true, context);
                            finish();
                        } else if (responseBody.equals("2")) {
                            MakeSnSnackbar("You have not registered for placement yet");
                            Log.d("invalid login", responseBody + "Error connecting to server !!");
                            Utils.clearpref(context);
                        } else if (responseBody.equals("3")) {
                            MakeSnSnackbar("Invalid Credentials");
                            Log.d("invalid login", responseBody + "Error connecting to server !!");
                            Utils.clearpref(context);
                        } else {
                            MakeSnSnackbar("Error connecting to server !!");
                          Log.d("invalid login", responseBody + "Error connecting to server !!");
                            Utils.clearpref(context);
                        }
                        progress.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MakeSnSnackbar("Error connecting to server !!");
                Log.d("invalid login", error.toString() + "Error connecting to server !!");
                Utils.clearpref(context);
                progress.dismiss();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("rollno", username.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }

        };
        queue.add(stringRequest);
    }

    public  void PlacementLogin(){
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = getString(R.string.dominename) + "/moblogin.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        if (response == null) {
                            MakeSnSnackbar("Error connecting to server !!");
                            Log.d("invalid login", response + "ok");
                            signOutFromGplus();
                            Utils.clearpref(getBaseContext());
                            signin.setEnabled(true);
                        } else {
                            response = response.replaceAll("\\s", "");
                            // Toast.makeText(getBaseContext(), "lk" + responseBody + "kh", Toast.LENGTH_SHORT).show();
                            if (response.equals("1")) {
                                Log.d("valid login", response + "ok");
                                Intent downloadIntent;
                                downloadIntent = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(downloadIntent);
                                Utils.saveprefBool("logedin", true, getBaseContext());
                                signOutFromGplus();

                                //Toast.makeText(getBaseContext(), "lk" + responseBody + "kh", Toast.LENGTH_SHORT).show();
                                finish();

                            } else if (response.equals("2")) {
                                MakeSnSnackbar("You have not registered for placement yet");
                                Log.d("invalid login", response + "ok");
                                signOutFromGplus();
                                Utils.clearpref(getBaseContext());
                                signin.setEnabled(true);

                            } else {
                                MakeSnSnackbar("Error connecting to server !!");
                                Log.d("invalid login", response + "ok");
                                signOutFromGplus();
                                Utils.clearpref(getBaseContext());
                                signin.setEnabled(true);
                            }
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        // Log.d("Error.Response", response);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("rollno", Utils.getprefString("rollno", getBaseContext()));

                return params;
            }
        };
        queue.add(postRequest);

    }
    public void MakeSnSnackbar(String text){
        hideKeyboard();
        Snackbar snack=Snackbar.make((CoordinatorLayout) findViewById(R.id.container), text, Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) snack.getView();
        group.setBackgroundColor(Color.WHITE);
        for (int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);
            if (v instanceof TextView) {
                TextView t = (TextView) v;
                t.setTextColor(Color.RED);
                t.setTextSize(17);
            }
        }
        snack.show();
    }
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
