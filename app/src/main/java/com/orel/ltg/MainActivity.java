package com.orel.ltg;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FacebookHandler.getInstance().onActivityResult(requestCode, resultCode, data);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FacebookHandler.getInstance().onDestroy();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {

        private Button mFacebookLoginButton;
        private Button mFacebookFriendsButton;
        private RecyclerView mFriendsList;
        private View mProgressBarView;

        private FriendsAdapter mAdapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mFacebookLoginButton = (Button) view.findViewById(R.id.facebook_login_btn);
            mFacebookFriendsButton = (Button) view.findViewById(R.id.facebook_get_friends_btn);
            mFriendsList = (RecyclerView) view.findViewById(R.id.friends_list);
            mProgressBarView = view.findViewById(R.id.progress_bar);

            registerOnClickListeners(mFacebookLoginButton, mFacebookFriendsButton);

            LinearLayoutManager lManager = new LinearLayoutManager(getActivity());
            mFriendsList.setHasFixedSize(true);
            mFriendsList.setLayoutManager(lManager);

            mAdapter = new FriendsAdapter();
            mFriendsList.setAdapter(mAdapter);
            mAdapter.setListener(new FriendsAdapter.OnFriendClickListener() {
                @Override
                public void OnFriendClicked(String friendID) {
                    mProgressBarView.setVisibility(View.VISIBLE);
                    FacebookHandler.getInstance().shareOnWall(getActivity(), friendID, new FacebookHandler.OnFacebookResult() {
                        @Override
                        public void success() {
                            if(isAdded()) {
                                Toast.makeText(getActivity(), "Ata Totach", Toast.LENGTH_SHORT).show();
                                mProgressBarView.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void failed(String error) {
                            if(isAdded()) {
                                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                                mProgressBarView.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            });
//            showHashKey(getActivity());
        }

        private void registerOnClickListeners(View... views) {
            for(View v : views) {
                v.setOnClickListener(this);
            }
        }

        public static void showHashKey(Context context) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(
                        "com.orel.ltg", PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException e) {
            } catch (NoSuchAlgorithmException e) {
            }
        }

        @Override
        public void onClick(View v) {
            if(v.equals(mFacebookLoginButton)) {
                FacebookHandler.getInstance().loginToFacebook(getActivity(), false, new FacebookHandler.OnFacebookResult() {
                    @Override
                    public void success() {
                        if(isAdded()) {
                            Toast.makeText(getActivity(), "Ata Totach", Toast.LENGTH_SHORT).show();
                            mProgressBarView.setVisibility(View.GONE);
                            mFacebookFriendsButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void failed(String error) {
                        if(isAdded()) {
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                            mProgressBarView.setVisibility(View.GONE);
                        }
                    }
                });
            } else if(v.equals(mFacebookFriendsButton)) {
                mProgressBarView.setVisibility(View.VISIBLE);
                FacebookHandler.getInstance().getFriendsList(new FacebookHandler.OnFriendsResult() {
                    @Override
                    public void success(List<FacebookUser> users) {
                        if(isAdded()) {
                            mProgressBarView.setVisibility(View.GONE);
                            mAdapter.setFriendsList(users);
                        }
                    }

                    @Override
                    public void failed(String error) {
                        if(isAdded()) {
                            mProgressBarView.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
