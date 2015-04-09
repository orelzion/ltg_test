package com.orel.ltg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yirmy on 08/04/2015.
 */
public class FacebookHandler {

    private final static String TAG = FacebookHandler.class.getName();
    private final static Object locker = new Object();

    private CallbackManager callbackManager;
    private AccessTokenTracker mAccessTokenTracker;
    private AccessToken mAccessToken;

    private static FacebookHandler ourInstance;

    public static FacebookHandler getInstance() {
        if(ourInstance == null) {
            synchronized (locker) {
                if(ourInstance == null) {
                    ourInstance = new FacebookHandler();
                }
            }
        }
        return ourInstance;
    }

    private FacebookHandler() {
        if(!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(LtgApplication.getApplication());
        }
        if(callbackManager == null) {
            callbackManager = CallbackManager.Factory.create();
        }
        if(mAccessTokenTracker == null) {
            mAccessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    mAccessToken = currentAccessToken;
                }
            };
            mAccessTokenTracker.startTracking();
        }
    }

    public void loginToFacebook(final Activity activity, final OnFacebookResult onFacebookLogin) {

        if(mAccessToken != null) {
            if(onFacebookLogin != null) {
                onFacebookLogin.success();
            }
            return;
        }

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(onFacebookLogin != null) {
                    onFacebookLogin.success();
                }
            }

            @Override
            public void onCancel() {
                if(onFacebookLogin != null) {
                    onFacebookLogin.failed("");
                }
            }

            @Override
            public void onError(FacebookException e) {
                if(onFacebookLogin != null) {
                    onFacebookLogin.failed(e.toString());
                }
                Log.e(TAG, e.toString());
            }
        });

        List<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("user_friends");

        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
    }

    public void getFriendsList(final OnFriendsResult onFriendsResult) {
        GraphRequest request = GraphRequest.newMyFriendsRequest(mAccessToken, new GraphRequest.GraphJSONArrayCallback() {
            @Override
            public void onCompleted(JSONArray jsonArray, GraphResponse graphResponse) {
                if(graphResponse.getError() != null) {
                    if(onFriendsResult != null) {
                        onFriendsResult.failed(graphResponse.getError().toString());
                    }
                } else {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<FacebookUser>>(){}.getType();
                    List<FacebookUser> friends = gson.fromJson(jsonArray.toString(), type);
                    if(onFriendsResult != null) {
                        onFriendsResult.success(friends);
                    }
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void shareOnWall(Activity activity, final OnFacebookResult onFacebookResult) {
        
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onDestroy() {
        if(mAccessTokenTracker != null) {
            mAccessTokenTracker.stopTracking();
        }
    }

    public interface OnFacebookResult {
        public void success();
        public void failed(String error);
    }

    public interface OnFriendsResult {
        public void success(List<FacebookUser> users);
        public void failed(String error);
    }
}
