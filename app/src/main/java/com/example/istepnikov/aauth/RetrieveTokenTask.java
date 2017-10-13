package com.example.istepnikov.aauth;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by istepnikov on 13.10.2017.
 */
public class RetrieveTokenTask extends AsyncTask<Account,Void,String> {

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    Activity activity;

    String scopes = "oauth2:"
            + Scopes.PLUS_LOGIN
            + " "
            + Scopes.PROFILE;

    @Override
    protected String doInBackground(Account... params) {
        Account account = params[0];
//        String scopes = "oauth2:profile email";
        String token = null;
        try {
            token = GoogleAuthUtil.getToken(getActivity().getApplicationContext(), account, scopes);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (UserRecoverableAuthException e) {
            //startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
        } catch (GoogleAuthException e) {
            Log.e(TAG, e.getMessage());
        }
        return token;
    }

    @Override
    protected void onPostExecute(String token) {
        super.onPostExecute(token);
        Log.i("Token Value: ", token);
    }
}
