package com.example.tgarden.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.AndroidViewModel;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64DataException;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.tgarden.Databases.BaseDataBindingActivity;
import com.example.tgarden.LocationOwner.RetailerDashboard;
import com.example.tgarden.R;
import com.example.tgarden.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends BaseDataBindingActivity<ActivityRegisterBinding, AndroidViewModel> {

    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected int getContentViewLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void initListeners() {
        mBinding.setOnClickBack(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBinding.setOnClickRegister(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(mBinding.email.getText()).toString().trim();
                String password = Objects.requireNonNull(mBinding.password.getText()).toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mBinding.email.setError(getResources().getString(R.string.invalid_email));
                    mBinding.email.setFocusable(true);
                } else if (password.length() < 6) {
                    mBinding.password.setError(getResources().getString(R.string.lenght_pass));
                    mBinding.password.setFocusable(true);
                } else {
                    registerUser(email, password);
                }
            }
        });

        mBinding.setOnClickHaveAccount(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void initData() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.create_account));
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void subscribeToViewModel() {

    }

    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();

                            String email = Objects.requireNonNull(user).getEmail();
                            String uid = user.getUid();
                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", "");
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", "");
                            hashMap.put("image", "");
                            hashMap.put("cover", "");
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");
                            reference.child(uid).setValue(hashMap);

                            Toast.makeText(RegisterActivity.this, "Register...\n" + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            startActivity(
                                    new Intent(RegisterActivity.this, RetailerDashboard.class));
                            finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this,
                                    getResources().getString(R.string.authentication_failed),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}