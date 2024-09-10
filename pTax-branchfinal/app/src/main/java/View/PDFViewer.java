package View;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.auditarrm.taxscan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import Controller.ArchivosControlador;

public class PDFViewer extends AppCompatActivity {

    private ImageView pdfImageView;
    private File pdfFile;
    private Button showMenuButton, saveButton;
    private LinearLayout scannedDataLayout;
    private boolean isMenuVisible = false;
    private ArchivosControlador archivosControlador1;
    private ArrayList<EditText> editTextList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);
        saveButton = findViewById(R.id.saveButtonn);
        saveButton.setText("Guardar cambios");// Inicialmente oculto
        editTextList = new ArrayList<>();
        archivosControlador1 = new ArchivosControlador();
        pdfImageView = findViewById(R.id.pdfImageView);
        showMenuButton = findViewById(R.id.showMenuButton);
        scannedDataLayout = findViewById(R.id.scannedDataLayout);

        // Obtener la URI del PDF desde el Intent
        String pdfUrl = getIntent().getStringExtra("pdfUri");
        if (pdfUrl == null) {
            Log.e("depuracion", "No PDF URL provided");
            return;
        }
        ArrayList<String> scannedData = getIntent().getStringArrayListExtra("scannedData");

        Uri PDFsubir;
        if (pdfUrl != null) {
            PDFsubir = Uri.parse(pdfUrl);
            archivosControlador1.subirFactura(PDFsubir, scannedData);
        } else {
            PDFsubir = null;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Descargar el archivo PDF desde la URL y guardarlo localmente
        pdfFile = new File(getCacheDir(), "example.pdf");
        downloadPdf(pdfUrl, pdfFile);

        // Mostrar la primera página del PDF
        showPdfPage(0);

        showMenuButton.setOnClickListener(view -> {
            if (isMenuVisible) {

                scannedData.clear();
                for (EditText editText : editTextList) {
                    scannedData.add(editText.getText().toString());
                }

                scannedDataLayout.setVisibility(View.GONE);
                showMenuButton.setText("Mostrar Datos Escaneados");
            } else {
                showMenuButton.setText("Ocultar Datos Escaneados");
                populateScannedData(scannedData);
                scannedDataLayout.setVisibility(View.VISIBLE);
            }
            isMenuVisible = !isMenuVisible;
        });

        saveButton.setOnClickListener(v -> {
            ArrayList<String> updatedData = new ArrayList<>();
            for (EditText editText : editTextList) {
                updatedData.add(editText.getText().toString());
            }
            guardarDatosEditadosEnFirebase(uid, PDFsubir.getLastPathSegment().substring(0, PDFsubir.getLastPathSegment().length() - 4), updatedData);
        });


    }

    private void populateScannedData(ArrayList<String> scannedData) {
        scannedDataLayout.removeAllViews();  // Limpiar el layout
        editTextList.clear();  // Limpiar lista de EditTexts

        if (scannedData != null) {
            for (String data : scannedData) {
                EditText editText = new EditText(this);
                editText.setText(data);
                editText.setPadding(16, 8, 16, 8);
                scannedDataLayout.addView(editText);
                editTextList.add(editText);  // Agregar a la lista para manejar los datos posteriormente
            }
        }
    }

    private void guardarDatosEditadosEnFirebase(String uid, String nombreArchivo, ArrayList<String> updatedData) {
        DatabaseReference facturaRef = FirebaseDatabase.getInstance().getReference("facturas").child(uid).child(nombreArchivo);
        facturaRef.setValue(updatedData)
                .addOnSuccessListener(aVoid -> Toast.makeText(PDFViewer.this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(PDFViewer.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show());
    }

    //Descarga el pdf para mostrarlo cuando se le de click
    private void downloadPdf(String pdfUrl, File outputFile) {
        Log.d("depuracion", "Attempting to download PDF from: " + pdfUrl);

        new Thread(() -> {
            try (InputStream inputStream = new URL(pdfUrl).openStream();
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                runOnUiThread(() -> {
                    Log.d("depuracion", "PDF successfully downloaded to: " + outputFile.getAbsolutePath());
                    showPdfPage(0); // Show the PDF after download
                });
            } catch (IOException e) {
                Log.e("depuracion", "Error downloading PDF", e);
            }
        }).start();
    }

    // Muestra el pdf
    private void showPdfPage(int pageIndex) {
        if (pdfFile.exists()) {
            Log.d("depuracion", "Displaying PDF from: " + pdfFile.getAbsolutePath());
            try (ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

                if (pageIndex >= 0 && pageIndex < pdfRenderer.getPageCount()) {
                    PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);

                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    pdfImageView.setImageBitmap(bitmap);

                    page.close();
                } else {
                    Log.e("depuracion", "Invalid page index: " + pageIndex);
                }
                pdfRenderer.close();
            } catch (IOException e) {
                Log.e("depuracion", "Error rendering PDF", e);
            }
        } else {
            Log.e("depuracion", "PDF file does not exist: " + pdfFile.getAbsolutePath());
        }
    }

    private void cargarDatosFacturaDesdeFirebase(String uid, String nombreArchivo) {

    }

}