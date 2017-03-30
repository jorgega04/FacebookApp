package com.jaap.facebookapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker  profileTracker;
    private ProfilePictureView profilePictureView;
    private TextView greeting;

    private static final String PERMISSION = "publish_actions";
    private Button bPostUpdate;
    private ShareDialog shareDialog;

    private FacebookCallback<Sharer.Result> sharedCallBack = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onSuccess(Sharer.Result result) {
            if(result.getPostId()!=null){
                showMessage("Publicacion enviada");
            }
        }

        @Override
        public void onCancel() {
            showMessage("Se cancelo la publicacion");
        }

        @Override
        public void onError(FacebookException error) {
            showMessage("Error al crear la publicacion");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (LoginButton)findViewById(R.id.login_button);
        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        greeting = (TextView) findViewById(R.id.greeting);

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        updateUI();
                        showMessage("fue exitoso el llamado");
                    }

                    @Override
                    public void onCancel() {
                        showMessage("Se activo cancelar");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        showMessage("Ocurrio un error");
                    }
                });

        //loginButton.setReadPermissions(Arrays.asList("user_status"));
        LoginManager.getInstance().logInWithPublishPermissions(this,Arrays.asList("publish_actions"));

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
                updateUI();
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
                updateUI();
            }
        };
        // If the access token is available already assign it.
        //accessToken = AccessToken.getCurrentAccessToken();
        //LoginManager.getInstance().logInWithReadPermissions(this,Arrays.asList("email"));

        //Compartir publicacion
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager,sharedCallBack);

        bPostUpdate = (Button) findViewById(R.id.bPost);
        bPostUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if(accessToken !=null){
                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://developers.facebook.com"))
                            .build();

                    if(ShareDialog.canShow(ShareLinkContent.class)){
                        shareDialog.show(linkContent);
                    }
                }
            }
        });




    }

    private void showMessage(String mensajeMostrar){
        Toast.makeText(this, mensajeMostrar, Toast.LENGTH_SHORT).show();

    }

    private void updateUI(){

        AccessToken accessToken= AccessToken.getCurrentAccessToken();
        Profile profile= Profile.getCurrentProfile();

        if(accessToken != null && profile != null){
            Log.d("JGA::","Profile_id" + profile.getId());
            Log.d("JGA::","Profile_fn" + profile.getFirstName());
            Log.d("JGA::","Profile_ln" + profile.getLastName());
            Log.d("JGA::","Profile_Uri" + profile.getLinkUri());
            profilePictureView.setProfileId(profile.getId());
            greeting.setText("Hola" + profile.getFirstName());
        }else{
            Log.d("JGA::","token"+accessToken);
            Log.d("JGA::","profile"+profile);
            profilePictureView.setProfileId(null);
            greeting.setText(null);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

}
