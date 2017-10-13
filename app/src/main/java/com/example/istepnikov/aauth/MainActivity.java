package com.example.istepnikov.aauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{

    private static final int RC_SIGN_IN = 9001;
    private static final String server_client_id = "684544763684-v0prsbes5o5ttpu887i0je53542m36rn.apps.googleusercontent.com";

    private GoogleApiClient mGoogleApiClient;

    private Menu menu;

    private String displayName = "";

    String scopes = "oauth2:"
            + Scopes.PLUS_LOGIN
            + " "
            + Scopes.PROFILE;

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        this.menu = menu;
        return super.onCreatePanelMenu(featureId, menu);
    }

    private void buildChart(){
        PieChart chart = (PieChart) findViewById(R.id.chart);
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(25, "Yes"));
        entries.add(new PieEntry(75, "No"));
        PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        chart.setData(pieData);
        chart.setCenterText("Results distribution for " + displayName);
        chart.invalidate();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(server_client_id)
                .requestEmail()
//                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
//                .requestScopes(new Scope(scopes))
                .requestScopes(new Scope(Scopes.PLUS_LOGIN), new Scope(Scopes.PROFILE))
                .requestServerAuthCode("684544763684-hv0kj6g6rcoc8u72fisaqipo8jhr2aie.apps.googleusercontent.com", false)
//                .requestIdToken("684544763684-hv0kj6g6rcoc8u72fisaqipo8jhr2aie.apps.googleusercontent.com")
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_in) {
            signIn();
            return true;
        } else if (id == R.id.action_sign_out) {
            signOut();
            return true;
        } else if (id == R.id.action_revoke_access) {
            revokeAccess();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        System.out.println("Hanle Result");
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            System.out.println("Successful");
            ((TextView)findViewById(R.id.text1)).setText("User: "+ acct.getDisplayName() +
                    " (" + acct.getEmail() + ")");
            System.out.println("Ticket:"+acct.getServerAuthCode());

            displayName = acct.getDisplayName();

            menu.findItem(R.id.action_sign_in).setEnabled(false);
            menu.findItem(R.id.action_sign_out).setEnabled(true);
            menu.findItem(R.id.action_revoke_access).setEnabled(true);
            findViewById(R.id.chart).setVisibility(View.VISIBLE);
            buildChart();

            //get OAuth2 token
//            try {
//                String token = GoogleAuthUtil.getToken(getApplicationContext(), acct.getAccount(),scopes);
//                System.out.println("OAuth2 token: "+token);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (GoogleAuthException e) {
//                e.printStackTrace();
//            }
            RetrieveTokenTask task = new RetrieveTokenTask();
            task.setActivity(this);
            task.execute(acct.getAccount());
//            try {
//                String token = task.get();
//                System.out.println("Google OAuth Access Token: "+token);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
        } else {
            // Signed out, show unauthenticated UI. Console.
            System.out.println("Unsuccessful");
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        menu.findItem(R.id.action_sign_in).setEnabled(true);
                        menu.findItem(R.id.action_sign_out).setEnabled(false);
                        menu.findItem(R.id.action_revoke_access).setEnabled(true);
                        ((TextView)findViewById(R.id.text1)).setText(R.string.unauthenticated);
                        findViewById(R.id.chart).setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        menu.findItem(R.id.action_sign_in).setEnabled(true);
                        menu.findItem(R.id.action_sign_out).setEnabled(false);
                        menu.findItem(R.id.action_revoke_access).setEnabled(false);
                        ((TextView)findViewById(R.id.text1)).setText(R.string.unauthenticated);
                        findViewById(R.id.chart).setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void startBilling(){
        InAppBilling billing = new InAppBilling(this);
    }
}
