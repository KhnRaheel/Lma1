package com.lma.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.lma.R;
import com.lma.info.Info;
import com.lma.model.UserPojo;
import com.lma.utils.DialogUtils;
import com.lma.utils.Utils;

import java.util.Objects;


public class SignupActivity extends AppCompatActivity implements Info {

    public static UserPojo userModel;
    public static String strEtPassword;
    public static Activity context;
    boolean isPassVisible = false;
    EditText etUserName;
    EditText etEmail;
    EditText etPassword;
    EditText etConfirmPassword;
    String strEtUserName;
    String strEtEmail;
    String strEtConfirmPassword;
    Dialog dgLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        context = this;
        initViews();
        dgLoading = new Dialog(this);
        DialogUtils.initLoadingDialog(dgLoading);

    }


    public void showPassword(View view) {
        if (!isPassVisible) {
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            isPassVisible = true;
        } else {
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPassVisible = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void castStrings() {
        strEtEmail = etEmail.getText().toString();
        strEtUserName = etUserName.getText().toString();
        strEtPassword = etPassword.getText().toString();
        strEtConfirmPassword = etConfirmPassword.getText().toString();
    }

    private void initViews() {
        etUserName = findViewById(R.id.et_user_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_pass);
        etConfirmPassword = findViewById(R.id.et_confirm_pass);

    }

    public void back(View view) {
        finish();
    }

    public void signUp(View view) {
        castStrings();

        if (!Utils.validEt(etUserName, strEtUserName))
            return;

        if (!Utils.validEt(etEmail, strEtEmail))
            return;


        if (!Utils.validEt(etPassword, strEtPassword))
            return;

        if (!strEtPassword.equals(strEtConfirmPassword)) {
            etConfirmPassword.setError("Password not matched");
            return;
        }

        userModel = new UserPojo(strEtUserName,
                strEtEmail,
                "",
                ""
                );

        dgLoading.show();
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);
        if (firebaseApp == null) {
            Log.i(TAG, "signUp: Firebase App = ");
            return;
        }
        FirebaseAuth.getInstance(firebaseApp).createUserWithEmailAndPassword(strEtEmail, strEtPassword)
                .addOnCompleteListener(task -> {
                    dgLoading.dismiss();
                    if (task.isSuccessful())
                        initData();
                    else {
                        Log.i(TAG, "signUp: " + Objects.requireNonNull(task.getException()).getMessage());
                        task.getException().printStackTrace();
                        Objects.requireNonNull(task.getException()).printStackTrace();
                        try {
                            Toast.makeText(SignupActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } catch (NullPointerException e) {
                            Log.i(TAG, "initSignIn: Null Exception");
                        }
                    }
                });

    }

    private void initData() {
        dgLoading.show();
        Utils.getReference()
                .child(NODE_USERS)
                .child(Utils.getCurrentUserId())
                .setValue(userModel)
                .addOnCompleteListener(task -> {
                    dgLoading.dismiss();
                    if (task.isSuccessful()) {
                        LoginActivity.context.finish();
                        Utils.currentUser = userModel;
                        startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SignupActivity.this, "something wrong", Toast.LENGTH_SHORT).show();
                        if (task.getException() != null) {
                            Log.i(TAG, "signUp: " + task.getException().getMessage());
                            task.getException().printStackTrace();
                            Toast.makeText(SignupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

//    public void loginpage(View view) {
//        startActivity(new Intent(this,LoginActivity.class));
//    }
}