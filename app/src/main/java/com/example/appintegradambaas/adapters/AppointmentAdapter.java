package com.example.appintegradambaas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appintegradambaas.R;
import com.example.appintegradambaas.data.entities.Appointment;
import java.util.List;
import java.util.function.Consumer;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private final List<Appointment> list;
    private final Consumer<Appointment> onEdit;
    private final Consumer<Appointment> onDelete;

    public AppointmentAdapter(List<Appointment> list, Consumer<Appointment> onEdit, Consumer<Appointment> onDelete) {
        this.list = list;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment a = list.get(position);
        holder.tvPatient.setText(a.patientName);
        holder.tvDateTime.setText(a.date + " " + a.time);
        holder.tvDescription.setText(a.description);

        holder.btnEdit.setOnClickListener(v -> onEdit.accept(a));
        holder.btnDelete.setOnClickListener(v -> onDelete.accept(a));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatient, tvDateTime, tvDescription;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatient = itemView.findViewById(R.id.tvPatient);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
