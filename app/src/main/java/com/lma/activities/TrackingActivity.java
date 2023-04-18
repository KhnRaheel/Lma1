package com.lma.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lma.R;
import com.lma.adapter.TypeRecyclerViewAdapter;
import com.lma.info.Info;
import com.lma.model.Device;
import com.lma.model.Super;
import com.lma.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends AppCompatActivity implements Info {

    RecyclerView rvDevices;
    TextView tvDevices;
    List<Super> superList;
    TypeRecyclerViewAdapter typeRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        rvDevices = findViewById(R.id.rv_devices);
        tvDevices = findViewById(R.id.tv_number_devices);

        initRv();
        initDevices();

    }

    private void initRv() {
        superList = new ArrayList<>();
        typeRecyclerViewAdapter
                = new TypeRecyclerViewAdapter(this, superList, RV_TYPE_DEVICES);
        rvDevices.setAdapter(typeRecyclerViewAdapter);
    }

    private void initDevices() {
        Utils.getReference()
                .child(NODE_DEVICES)
                .child(Utils.getCurrentUserId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        superList.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Device device = child.getValue(Device.class);
                            if (device != null)
                                superList.add(device);
                        }
                        tvDevices.setText(String.valueOf(superList.size()));
                        typeRecyclerViewAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void back(View view) {
        finish();
    }
}