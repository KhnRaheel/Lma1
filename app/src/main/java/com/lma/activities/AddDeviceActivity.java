package com.lma.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lma.R;
import com.lma.info.Info;
import com.lma.model.Device;
import com.lma.utils.DialogUtils;
import com.lma.utils.Utils;

import java.util.Objects;
import java.util.UUID;

public class AddDeviceActivity extends AppCompatActivity implements Info {
    Dialog dgLoading;
    EditText etName;
    EditText etModelNumber;
    EditText etIMEI;
    EditText etPhone;
    String strEtPhone;
    String strEtName;
    String strEtModelNumber;
    String strEtIMEI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        initViews();
        dgLoading = new Dialog(this);
        DialogUtils.initLoadingDialog(dgLoading);
    }

    public void back(View view) {
        finish();
    }

    private void initViews() {
        etName = findViewById(R.id.et_user_name);
        etModelNumber = findViewById(R.id.et_email);
        etIMEI = findViewById(R.id.et_pass);
        etPhone = findViewById(R.id.et_phone);
    }


    private void castStrings() {
        strEtIMEI = etIMEI.getText().toString();
        strEtName = etName.getText().toString();
        strEtModelNumber = etModelNumber.getText().toString();
        strEtPhone = etPhone.getText().toString();
    }

    public void add(View view) {
        castStrings();
        if (!Utils.validEt(etName, strEtName))
            return;
        if (!Utils.validEt(etModelNumber, strEtModelNumber))
            return;
        if (!Utils.validEt(etIMEI, strEtIMEI))
            return;
        if (!Utils.validEt(etPhone, strEtPhone))
            return;

        Log.d(TAG, "add: RETUN RETUTN T");
        initData();
    }

    private void initData() {
        String id = UUID.randomUUID().toString();
        Device device = new Device(id, strEtName, strEtModelNumber, strEtIMEI, strEtPhone);
        Utils.getReference().child(NODE_DEVICES)
                .child(Utils.getCurrentUserId())
                .child(strEtIMEI)
                .setValue(device)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddDeviceActivity.this, "Device added successfully", Toast.LENGTH_SHORT).show();
                        AddDeviceActivity.this.finish();
                    } else {
                        Toast.makeText(AddDeviceActivity.this, "Device not added at the moment", Toast.LENGTH_SHORT).show();
                        Objects.requireNonNull(task.getException()).printStackTrace();
                    }
                });

    }


}