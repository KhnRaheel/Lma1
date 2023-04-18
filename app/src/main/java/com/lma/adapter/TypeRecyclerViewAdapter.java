package com.lma.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;
import com.lma.R;
import com.lma.activities.CommandActivity;
import com.lma.activities.TrackingDevice;
import com.lma.info.Info;
import com.lma.model.Device;
import com.lma.model.Super;
import com.lma.singletons.DeviceSingleton;
import com.lma.utils.Utils;

import java.util.List;


public class TypeRecyclerViewAdapter extends RecyclerView.Adapter<TypeRecyclerViewHolder> implements Info {
    Context context;
    List<Super> listInstances;
    int type;

    public TypeRecyclerViewAdapter(Context context, List<Super> listInstances, int type) {
        this.context = context;
        this.listInstances = listInstances;
        this.type = type;
    }

    @NonNull
    @Override
    public TypeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TypeRecyclerViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.rv_devices, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final TypeRecyclerViewHolder holder, int position) {
        initNotices(holder, position);

    }

    private void initNotices(TypeRecyclerViewHolder holder, int position) {
        Device device = (Device) listInstances.get(position);
        holder.tvName.setText(device.getName());
        holder.tvIMEI.setVisibility(View.GONE);
        holder.tvPhone.setText(device.getPhone());
        holder.tvModelNumber.setVisibility(View.GONE);
        holder.cvClick.setOnClickListener(view -> {
            DeviceSingleton.setInstance(device);
            Intent intent = new Intent(context, CommandActivity.class);
            intent.putExtra("getPhone", device.getPhone());
            intent.putExtra("getIMEI", device.getIMEI());
            context.startActivity(intent);
        });


        holder.btnMap.setVisibility(View.GONE);
        holder.btnRing.setVisibility(View.GONE);
        holder.btnReqCall.setVisibility(View.GONE);
        holder.btnRemove.setVisibility((View.GONE));
    }

    private void btnRemove(Device device) {
        FirebaseDatabase.getInstance().getReference()
                .child(NODE_DEVICES)
                .child(Utils.getCurrentUserId())
                .child(device.getIMEI())
                .removeValue();
    }

    private void btnReqCall(Device device) {
//         REQUEST A CALL FROM END DEVICE
        sendMessage(device.getPhone(), device.getIMEI(), COMMAND_CALL);

    }

    private void sendMessage(String phoneNo, String imei, String command) {
        if (!checkSMSPermission())
            return;

        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(phoneNo, null, imei + " " + command, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(context, "SMS sent.",
                Toast.LENGTH_LONG).show();
    }

    private boolean checkSMSPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("asd", "Check self permission");
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    Manifest.permission.SEND_SMS)) {
                Log.i("asd", "should show permission dialog");
            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.SEND_SMS},
                        11);
            }
            return false;
        } else {
            Log.i("long", "///////Permission granted");
            return true;
        }
    }

    private void btnRing(Device device) {
        sendMessage(device.getPhone(), device.getIMEI(), COMMAND_RING);
    }

    private void btnMap(Device device) {
        sendMessage(device.getPhone(), device.getIMEI(), COMMAND_MAP);

    }

    @Override
    public int getItemCount() {
        return listInstances.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }
}
