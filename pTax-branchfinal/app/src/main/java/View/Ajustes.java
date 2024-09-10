package View;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.auditarrm.taxscan.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Ajustes extends AppCompatActivity {

    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    public StorageReference storageRef;
    Button logout;
    ImageView photo;
    EditText nombre;
    Button home;
    Button emp;
    private Button buttonSaveName, buttonChangePhoto;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ajustes);

        // Inicia las variables necesarias para usar Firebase
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Inicia los elementos de xml
        home = findViewById(R.id.Button_home);
        logout = findViewById(R.id.logout_button);
        photo = findViewById(R.id.profile_image);
        emp = findViewById(R.id.btnEntEmpresas);
        nombre = findViewById(R.id.editText_username);
        buttonSaveName = findViewById(R.id.button_save_name);
        buttonChangePhoto = findViewById(R.id.button_change_photo);


        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            Uri photoUrl = currentUser.getPhotoUrl();

            if (name != null) {
                nombre.setText(name);
            }

            if (photoUrl != null) {
                Glide.with(this).load(photoUrl).into(photo);  // Usa Glide o Picasso para cargar la imagen desde la URI
            }
        }

        // Botón para cambiar el nombre
        buttonSaveName.setOnClickListener(view -> {
            String newName = nombre.getText().toString();
            if (!newName.isEmpty()) {
                actualizarNombreUsuario(newName);
            }
        });

        // Botón para cambiar la foto
        buttonChangePhoto.setOnClickListener(view -> {
            seleccionarFoto();
        });

        // Botón para ir a la home
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Ajustes.this, Home.class);
                startActivity(intent);
            }
        });
        emp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Ajustes.this, EmpresasRegistradas.class);
                startActivity(intent);
            }
        });

        // Mostrar nombre y foto del usuario actual
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            Uri photoUrl = currentUser.getPhotoUrl();

            if (name != null) {
                nombre.setText(name);
            }

            if (photoUrl != null) {
                photo.setImageURI(photoUrl);
            }
        }

        // Botón para cerrar sesión
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Ajustes.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Ajustar insets para Edge to Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void actualizarNombreUsuario(String newName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Ajustes.this, "Nombre actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Ajustes.this, "Error al actualizar el nombre", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Método para seleccionar una nueva foto
    private void seleccionarFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);  // 1000 es el request code
    }

    // Resultado de la selección de la foto
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            photoUri = data.getData();
            if (photoUri != null) {
                photo.setImageURI(photoUri);  // Mostrar la nueva foto seleccionada
                subirFotoAStorage(photoUri);
            }
        }
    }

    // Subir la nueva foto a Firebase Storage
    private void subirFotoAStorage(Uri photoUri) {
        StorageReference profileRef = storageRef.child("users/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.putFile(photoUri).addOnSuccessListener(taskSnapshot -> {
            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                actualizarFotoUsuario(uri);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(Ajustes.this, "Error al subir la foto", Toast.LENGTH_SHORT).show();
        });
    }

    // Actualizar la foto del usuario en Firebase
    private void actualizarFotoUsuario(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Ajustes.this, "Foto actualizada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Ajustes.this, "Error al actualizar la foto", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}