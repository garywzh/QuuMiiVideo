package org.garywzh.quumiibox.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
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

import org.garywzh.quumiibox.AppContext;
import org.garywzh.quumiibox.R;
import org.garywzh.quumiibox.common.UserState;
import org.garywzh.quumiibox.model.LoginResult;
import org.garywzh.quumiibox.model.OperatInfo;
import org.garywzh.quumiibox.network.NetworkHelper;
import org.garywzh.quumiibox.util.LogUtils;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements OnClickListener {
    public static String TAG = LoginActivity.class.getSimpleName();
    public static String URL_REGISTER = "http://www.huoji.tv/do.php?ac=943c400772ea74e9ed9335e02dc786a3";

    private EditText mAccountView;
    private EditText mPwdView;
    private View mProgressView;
    private View mLoginFormView;
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_activity_login);

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
        if (mLogin != null) {
            mLogin.setOnClickListener(this);
        }
        View signUp = findViewById(R.id.sign_up);
        if (signUp != null) {
            signUp.setOnClickListener(this);
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                attemptLogin();
                break;
            case R.id.sign_up:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(URL_REGISTER));
                startActivity(i);
                break;
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        mAccountView.setError(null);
        mPwdView.setError(null);

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
            showProgress(true);

            mSubscription = NetworkHelper.getApiService()
                    .login(email, password)
                    .subscribeOn(Schedulers.io())
                    .map(new Func1<LoginResult, Boolean>() {
                        @Override
                        public Boolean call(LoginResult loginResult) {
                            if (loginResult != null && loginResult.operatinfo.status == OperatInfo.STATUS_LOGIN_SUCCESS) {
                                UserState.getInstance().login(loginResult);
                                return true;
                            }
                            return false;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            showProgress(false);
                            e.printStackTrace();
                            Toast.makeText(AppContext.getInstance(), R.string.toast_network_error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(Boolean success) {
                            showProgress(false);
                            if (success) {
                                onLoginSuccess(UserState.getInstance().getUsername());
                            } else {
                                LoginActivity.this.mPwdView.setError(getString(R.string.error_incorrect_password));
                                LoginActivity.this.mPwdView.requestFocus();
                                LogUtils.w(TAG, "login failed");
                            }
                        }
                    });
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
        Toast.makeText(this, getString(R.string.toast_login_success) + " " + username,
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

    @Override
    public void onStop() {
        super.onStop();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
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
}