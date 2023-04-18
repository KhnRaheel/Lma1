package com.lma.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lma.R;


public class TypeRecyclerViewHolder extends RecyclerView.ViewHolder {

    TextView tvName;
    TextView tvIMEI;
    TextView tvModelNumber;
    TextView tvPhone;

    AppCompatButton btnRing;
    AppCompatButton btnMap;
    AppCompatButton btnReqCall;
    AppCompatButton btnRemove;
    CardView cvClick;

    public TypeRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);

        tvName = itemView.findViewById(R.id.tv_device_name);
        tvIMEI = itemView.findViewById(R.id.tv_imei);
        tvModelNumber = itemView.findViewById(R.id.tv_model_number);
        tvPhone = itemView.findViewById(R.id.tv_phone);
        cvClick = itemView.findViewById(R.id.cv_click);

        btnRing = itemView.findViewById(R.id.btn_ring);
        btnMap = itemView.findViewById(R.id.btn_map);
        btnReqCall = itemView.findViewById(R.id.btn_request_call);
        btnRemove = itemView.findViewById(R.id.btn_remove);

    }

}


