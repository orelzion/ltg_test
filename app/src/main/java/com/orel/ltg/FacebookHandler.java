package com.orel.ltg;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yirmy on 08/04/2015.
 */
public class FacebookHandler {

    private final static String TAG = FacebookHandler.class.getName();
    private final static Object locker = new Object();

    private final static String USER_FRIENDS_PERMISSION = "user_friends";
    private final static String PUBLIC_PROFILE_PERMISSION = "public_profile";
    private final static String PUBLISH_PERMISSION = "publish_actions";

    private CallbackManager callbackManager;
    private ProfileTracker mProfileTracker;
    private Profile mProfile;

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
        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                mProfile = newProfile;
            }
        };
        mProfileTracker.startTracking();
    }

    public boolean isLoggedIn() {
        return mProfile != null;
    }

    public void loginToFacebook(final Activity activity, final boolean requestPublish, final OnFacebookResult onFacebookLogin) {

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

        if(!requestPublish) {
            List<String> permissions = new ArrayList<>();
            permissions.add(PUBLIC_PROFILE_PERMISSION);
            permissions.add(USER_FRIENDS_PERMISSION);

            LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
        } else {
            LoginManager.getInstance().logInWithPublishPermissions(activity, Arrays.asList(PUBLISH_PERMISSION));
        }
    }

    public void getFriendsList(final OnFriendsResult onFriendsResult) {

        AccessToken token = AccessToken.getCurrentAccessToken();

        if(token == null) {
            if(onFriendsResult != null) {
                onFriendsResult.failed("Log in is required");
            }
            return;
        }

        GraphRequest request = GraphRequest.newMyFriendsRequest(token, new GraphRequest.GraphJSONArrayCallback() {
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

    public void shareOnWall(final Activity activity, final String friendID, final OnFacebookResult onFacebookResult) {
        ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                .setContentTitle(activity.getString(R.string.share_title))
                .setContentDescription(activity.getString(R.string.share_description))
                .setContentUrl(Uri.parse(activity.getString(R.string.share_link)))
                .setPeopleIds(Arrays.asList(friendID))
                .build();

        if(hasPublishPermission()) {
            ShareApi.share(shareLinkContent, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    if(onFacebookResult != null) {
                        onFacebookResult.success();
                    }
                }

                @Override
                public void onCancel() {
                    if(onFacebookResult != null) {
                        onFacebookResult.failed("");
                    }
                }

                @Override
                public void onError(FacebookException e) {
                    if(onFacebookResult != null) {
                        onFacebookResult.failed(e.toString());
                    }
                }
            });
        } else {
            loginToFacebook(activity, true, new OnFacebookResult() {
                @Override
                public void success() {
                    shareOnWall(activity, friendID, onFacebookResult);
                }

                @Override
                public void failed(String error) {
                    if(onFacebookResult != null) {
                        onFacebookResult.failed(error);
                    }
                }
            });
        }
    }

    public boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void onDestroy() {
        if(mProfileTracker != null && mProfileTracker.isTracking()) {
            mProfileTracker.stopTracking();
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
