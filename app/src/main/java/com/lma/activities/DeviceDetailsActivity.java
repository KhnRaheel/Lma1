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
import com.lma.singletons.DeviceSingleton;
import com.lma.utils.DialogUtils;
import com.lma.utils.Utils;

import java.util.Objects;

public class DeviceDetailsActivity extends AppCompatActivity implements Info {
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
        setContentView(R.layout.activity_device_details);

        if (DeviceSingleton.getInstance() == null)
            finish();

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

        etName.setText(DeviceSingleton.getInstance().getName());
        etModelNumber.setText(DeviceSingleton.getInstance().getModelNumber());
        etIMEI.setText(DeviceSingleton.getInstance().getIMEI());
        etPhone.setText(DeviceSingleton.getInstance().getPhone());

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

        initData();
    }

    private void initData() {
        Device device = new Device(DeviceSingleton.getInstance().getId(),
                strEtName, strEtModelNumber, strEtIMEI, strEtPhone);

        Log.i(TAG, "sdasd : " + strEtIMEI);
        Log.i(TAG, "sdasd : " + strEtModelNumber);
        Log.i(TAG, "sdasd : " + strEtPhone);

        Utils.getReference().child(NODE_DEVICES)
                .child(Utils.getCurrentUserId())
                .child(strEtIMEI)
                .setValue(device)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(DeviceDetailsActivity.this, "Device added successfully", Toast.LENGTH_SHORT).show();
                        DeviceDetailsActivity.this.finish();
                    } else {
                        Toast.makeText(DeviceDetailsActivity.this, "Device not added at the moment", Toast.LENGTH_SHORT).show();
                        Objects.requireNonNull(task.getException()).printStackTrace();
                    }
                });

    }


}