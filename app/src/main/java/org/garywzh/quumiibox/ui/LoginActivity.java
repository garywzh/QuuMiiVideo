package org.garywzh.quumiibox.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.common.exception.ConnectionException;
import org.garywzh.quumiibox.common.exception.FatalException;
import org.garywzh.quumiibox.common.exception.RemoteException;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.Member;
import org.garywzh.quumiibox.network.RequestHelper;
import org.garywzh.quumiibox.util.LogUtils;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mAccountView;
    private EditText mPwdView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the login form.
        mAccountView = (EditText) findViewById(R.id.account);

        mPwdView = (EditText) findViewById(R.id.password);
        mPwdView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            private int mActionIdSignIn = getResources().getInteger(R.integer.id_action_sign);

            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == mActionIdSignIn || id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        findViewById(R.id.sign_up).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://www.quumii.com/do.php?ac=5ceffa4024c2df235070de3f6cbaea3b"));
                startActivity(i);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mAccountView.setError(null);
        mPwdView.setError(null);

        // Store values at the time of the login attempt.
        String email = mAccountView.getText().toString();
        String password = mPwdView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPwdView.setError(getString(R.string.error_field_required));
            focusView = mPwdView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mAccountView.setError(getString(R.string.error_field_required));
            focusView = mAccountView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void onLoginSuccess(String username) {
        Toast.makeText(this, getString(R.string.toast_login_success, username),
                Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private final String TAG = UserLoginTask.class.getSimpleName();

        private final String mUsername;
        private final String mPassword;
        private Exception mException;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                final LoginResult result = RequestHelper.login(mUsername, mPassword);
                if (result != null) {
                    UserState.getInstance().login(result);
                    return true;
                }
            } catch (ConnectionException | RemoteException e) {
                mException = e;
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                onLoginSuccess(mUsername);
                return;
            }

            if (mException == null) {
                LoginActivity.this.mPwdView.setError(getString(R.string.error_incorrect_password));
                LoginActivity.this.mPwdView.requestFocus();
                return;
            }

            LogUtils.w(TAG, "login failed", mException);

            int resId;
            if (mException instanceof ConnectionException) {
                resId = R.string.toast_connection_exception;
            } else if (mException instanceof RemoteException) {
                resId = R.string.toast_remote_exception;
            } else {
                throw new FatalException(mException);
            }

            Toast.makeText(LoginActivity.this, resId, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}