package com.ignis.colfinancial;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URL;
import java.security.MessageDigest;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    CallbackManager callbackManager;
    ProgressDialog mDialog;


    private LinearLayout profile_section;
    private Button sign_out;
    private SignInButton sign_in;
    private TextView name, email;
    private ImageView prof_pic;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    int a = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profile_section = (LinearLayout) findViewById(R.id.pro_sec);
        sign_out = (Button) findViewById(R.id.logout);
        sign_in = (SignInButton) findViewById(R.id.sign_in_button);
        prof_pic = (ImageView) findViewById(R.id.prof_pic);
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);
        profile_section.setVisibility(View.INVISIBLE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API
                , signInOptions).build();


        sign_out.setOnClickListener(this);
        sign_in.setOnClickListener(this);


        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setMessage("Retrieving data...");
                mDialog.show();

                String accesstoken = loginResult.getAccessToken().getToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();
                        Log.d("response", response.toString());
                      getData(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields","id,name,email");
                request.setParameters(parameters);
                request.executeAsync();

                profile_section.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        //if ready
        if(AccessToken.getCurrentAccessToken() != null){
            email.setText(AccessToken.getCurrentAccessToken().getUserId());
        }


       // printKeyHash();
    }


    /*private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.ignis.colfinancial", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {

                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch(Exception e) {
        }
    }*/

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button:
                setSign_in();
                break;
            case R.id.logout:
                setSign_out();
                break;

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void setSign_out() {
        Toast.makeText(this, "signout", Toast.LENGTH_SHORT).show();
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                update_ui(false);
            }
        });
    }

    private void setSign_in() {
        Toast.makeText(this, "signin", Toast.LENGTH_SHORT).show();
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, REQ_CODE);
        a = 1;
    }

    private void handle_result(GoogleSignInResult result) {

        Toast.makeText(this, "handle_result", Toast.LENGTH_SHORT).show();

        if (result.isSuccess()) {

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();


                name.setText(personName);
                email.setText(personEmail);
                Glide.with(this).load(personPhoto).into(prof_pic);
                update_ui(true);


            } else {
                update_ui(false);
            }

        }
    }

    private void update_ui(boolean islogin) {

        if (islogin) {
            profile_section.setVisibility(View.VISIBLE);
            sign_in.setVisibility(View.INVISIBLE);

        } else {
            profile_section.setVisibility(View.INVISIBLE);
            sign_in.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(a == 1) {
            Toast.makeText(this, "" + resultCode, Toast.LENGTH_SHORT).show();
            if (resultCode == -1) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handle_result(result);

                a = 0;
            }
        }


        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void getData(JSONObject object) {

        try {

            URL profile_picture = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=90&height=90");

            Picasso.with(this).load(profile_picture.toString()).into(prof_pic);

            email.setText(object.getString("email"));
          //  birthday.setText(object.getString("birthday"));
           // friends.setText("Friends: "+object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
            name.setText(object.getString("name"));

           // Glide.with(this).load(profile_picture).into(prof_pic);

        } catch(Exception e) {
        }


    }
}
