package com.azhar.hitungpengeluaran.view.fragment.pengeluaran.add;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.azhar.hitungpengeluaran.R;
import com.azhar.hitungpengeluaran.model.ModelDatabase;
import com.azhar.hitungpengeluaran.view.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import androidx.lifecycle.ViewModel;

import com.azhar.hitungpengeluaran.model.ModelDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddPengeluaranActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri photoUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private static String KEY_IS_EDIT = "key_is_edit";
    private static String KEY_DATA = "key_data";

    public static void startActivity(Context context, boolean isEdit, ModelDatabase pengeluaran) {
        Intent intent = new Intent(context, AddPengeluaranActivity.class);
        intent.putExtra(KEY_IS_EDIT, isEdit);
        intent.putExtra(KEY_DATA, pengeluaran);
        context.startActivity(intent);
    }

    private AddPengeluaranViewModel addPengeluaranViewModel;

    private boolean mIsEdit = false;
    private int strId = 0;

    Toolbar toolbar;
    TextInputEditText etPelanggan, etKeterangan, etTanggal, etJmlUang;
    Button btnSimpan, btnUploadPhoto;
    ImageView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_data);

        toolbar = findViewById(R.id.toolbar);
        etPelanggan = findViewById(R.id.etPelanggan);
        etKeterangan = findViewById(R.id.etKeterangan);
        etTanggal = findViewById(R.id.etTanggal);
        etJmlUang = findViewById(R.id.etJmlUang);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        ivPhoto = findViewById(R.id.ivPhoto);

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        addPengeluaranViewModel = ViewModelProviders.of(this).get(AddPengeluaranViewModel.class);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        loadData();
        initAction();
    }

    private void loadData() {
        mIsEdit = getIntent().getBooleanExtra(KEY_IS_EDIT, false);
        if (mIsEdit) {
            ModelDatabase pengeluaran = getIntent().getParcelableExtra(KEY_DATA);
            if (pengeluaran != null) {
                strId = pengeluaran.uid;
                String pelanggan = pengeluaran.pelanggan;
                String keterangan = pengeluaran.keterangan;
                String tanggal = pengeluaran.tanggal;
                int uang = pengeluaran.jmlUang;

                etPelanggan.setText(pelanggan);
                etKeterangan.setText(keterangan);
                etTanggal.setText(tanggal);
                etJmlUang.setText(String.valueOf(uang));
                // Load image into ImageView using Glide or Picasso if necessary
            }
        }
    }

    private void initAction() {
        etTanggal.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog.OnDateSetListener date = (view1, year, monthOfYear, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String strFormatDefault = "d MMMM yyyy";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strFormatDefault, Locale.getDefault());
                etTanggal.setText(simpleDateFormat.format(calendar.getTime()));
            };

            new DatePickerDialog(AddPengeluaranActivity.this, date,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnUploadPhoto.setOnClickListener(view -> chooseImage());

        btnSimpan.setOnClickListener(v -> {
            String strTipe = "pengeluaran";
            String strPelanggan = etPelanggan.getText().toString();
            String strKeterangan = etKeterangan.getText().toString();
            String strTanggal = etTanggal.getText().toString();
            String strJmlUang = etJmlUang.getText().toString();

            if (strPelanggan.isEmpty() || strKeterangan.isEmpty() || strTanggal.isEmpty() || strJmlUang.isEmpty()) {
                Toast.makeText(AddPengeluaranActivity.this, "Ups, form tidak boleh kosong!",
                        Toast.LENGTH_SHORT).show();
            } else {
                if (photoUri != null) {
                    uploadImageAndSaveData(strTipe, strPelanggan, strKeterangan, strTanggal, Integer.parseInt(strJmlUang));
                } else {
                    saveData(strTipe, strPelanggan, strKeterangan, strTanggal, Integer.parseInt(strJmlUang), null);
                }
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            photoUri = data.getData();
            ivPhoto.setImageURI(photoUri);
        }
    }

    private void uploadImageAndSaveData(String strTipe, String strPelanggan, String strKeterangan, String strTanggal, int jmlUang) {
        StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

        ref.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveData(strTipe, strPelanggan, strKeterangan, strTanggal, jmlUang, uri.toString());
                }))
                .addOnFailureListener(e -> Toast.makeText(AddPengeluaranActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void saveData(String strTipe, String strPelanggan, String strKeterangan, String strTanggal, int jmlUang, String photoUrl) {
        if (mIsEdit) {
            addPengeluaranViewModel.updatePengeluaran(strId, strPelanggan, strKeterangan, strTanggal, jmlUang, photoUrl);
        } else {
            addPengeluaranViewModel.addPengeluaran(strTipe, strPelanggan, strKeterangan, strTanggal, jmlUang, photoUrl);
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(AddPengeluaranActivity.this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
