package com.example.mymusicapp.activity;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mymusicapp.R;
import com.example.mymusicapp.repository.DBAccountHelper;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "";
    final int ACTIVITY3 = 3;
    EditText etNameLogin, etPass;
    int RC_SIGN_IN = 432;
    AccessTokenTracker tokenTracker;
    private LoginButton loginButton;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tokenTracker  = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Toast.makeText(ActivityLogin.this, "Đã đăng xuất tài khoản Facebook của bạn!", Toast.LENGTH_SHORT).show();
                } else {
                    loaduserProfile(currentAccessToken);
                }
            }
        };

        etPass = findViewById(R.id.etPass);
        etNameLogin = findViewById(R.id.etNameLogin);
        loginButton = findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.sign_in_button).setOnClickListener((View.OnClickListener) this);
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        checkLoginStatus();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }
    public void skipLogin(View v)
    {
        Intent intent = new Intent(ActivityLogin.this,
                com.example.mymusicapp.activity.ActivityMain.class); // paste first line
        intent.putExtra("name", "");
        intent.putExtra("check","1");
        startActivity(intent);
    }
    public void btnLogin(View v) {
        String name = etNameLogin.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if (name.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ", Toast.LENGTH_SHORT).show();
        } else {
            try {
                DBAccountHelper db = new DBAccountHelper(this);
                db.open();
                Boolean login = db.Login(name, pass);
                db.close();
                if (login) {
                    Intent intent = new Intent(ActivityLogin.this,
                            com.example.mymusicapp.activity.ActivityMain.class); // paste first line
                    intent.putExtra("name", name);
                    intent.putExtra("check","0");
                    startActivity(intent);
                    etNameLogin.setText("");
                    etPass.setText("");
                } else {
                    Toast.makeText(this, "Sai Tài Khoản" , Toast.LENGTH_SHORT).show();
                }
            } catch (SQLException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void btnSignUp(View v) {
        startActivity(new Intent(this, ActivitySignUp.class));
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.

            if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "Không đăng nhập được với Google", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                String name = task.getResult().getEmail();
                Intent intent = new Intent(ActivityLogin.this,
                        com.example.mymusicapp.activity.ActivityMain.class); // paste first line
                intent.putExtra("name", name);
                intent.putExtra("check","API");
                startActivityForResult(intent, ACTIVITY3);
            }



        } else if (requestCode == ACTIVITY3) {
            if (resultCode == RESULT_OK) {
                signOut();
                LoginManager.getInstance().logOut();
                Toast.makeText(this, "Đăng xuất thành công " + data.getStringExtra("tvn"), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void loaduserProfile(AccessToken newAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");

                    Intent intent = new Intent(ActivityLogin.this,
                            com.example.mymusicapp.activity.ActivityMain.class); // paste first line
                    intent.putExtra("name", email);
                    intent.putExtra("check","API");
                    startActivityForResult(intent, ACTIVITY3);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.

//            startActivity(new Intent(this,ActivityApp.class));
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private void checkLoginStatus() {
        if (AccessToken.getCurrentAccessToken() != null) {
            loaduserProfile(AccessToken.getCurrentAccessToken());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }
}