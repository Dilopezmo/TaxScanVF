package View;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.auditarrm.taxscan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import Model.Image;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    Context context;
    ArrayList<Image> pdfList;
    OnItemClickListener onItemClickListener;

    public ImageAdapter(Context context, ArrayList<Image> pdfList) {
        this.context = context;
        this.pdfList = pdfList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Image pdf = pdfList.get(position);
        holder.title.setText(pdf.getName());

        holder.imageView.setImageResource(R.drawable.baseline_insert_drive_file_24);
        holder.deleteButton.setImageResource(R.drawable.ic_delete);
        // Abrir pdf cuando se le de click
        holder.itemView.setOnClickListener(view -> {

            Intent intent = new Intent(context, PDFViewer.class);
            intent.putExtra("pdfUri", pdf.getUrl()); // Pasar la URI del PDF al Intent
            DatabaseReference facturasRef = FirebaseDatabase.getInstance().getReference("facturas").child(uid).child(pdf.getName().substring(0,pdf.getName().length()-4));

            facturasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<String> scannedData = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String data = snapshot.getValue(String.class);
                        scannedData.add(data);
                    }
                    intent.putStringArrayListExtra("scannedData", scannedData);
                    context.startActivity(intent);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error al descargar los datos de la factura: ", databaseError.toException());
                }
            });
        });

        holder.deleteButton.setOnClickListener(v -> {
            deletePdfFromFirebase(uid, pdf.getName(), position);
        });
    }

    private void deletePdfFromFirebase(String uid, String pdfName, int position) {
        // Elimina el PDF de Firebase
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("facturas/")
                .child(uid)
                .child(pdfName);

        // Primero eliminamos el archivo del Storage
        storageRef.delete().addOnSuccessListener(aVoid -> {
            // Después de eliminar el archivo, eliminamos los datos de Realtime Database
            DatabaseReference facturasRef = FirebaseDatabase.getInstance()
                    .getReference("facturas")
                    .child(uid)
                    .child(pdfName.substring(0, pdfName.length() - 4));

            // Eliminar de la lista local y notificar el cambio
            pdfList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Factura eliminada", Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error al eliminar el archivo de Storage", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    //Inicia los elementos para el recyclerview
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;
        ImageView deleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.list_item_title);
            imageView = itemView.findViewById(R.id.list_item_image);
            deleteButton = itemView.findViewById(R.id.delete_item);
        }
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onClick(Image pdf);
    }
}