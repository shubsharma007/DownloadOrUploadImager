package com.example.apicalling2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.apicalling2.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Retrofit retrofit;
    ApiInterface apiInterface;
    Call<Dog> fact;
    Bitmap bitmap;
    BitmapDrawable bitmapDrawable;
    private static final int REQUEST_IMAGE_CAPTURE = 10;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        retrofit = new Retrofit.Builder().baseUrl("https://dog.ceo/api/breeds/image/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(ApiInterface.class);

        binding.go.setOnClickListener(view -> {
            fact = apiInterface.dogGo();

            fact.enqueue(new Callback<Dog>() {
                @Override
                public void onResponse(Call<Dog> call, Response<Dog> response) {
                    if (response.isSuccessful()) {

                        Dog mydog = response.body();
                        Glide.with(MainActivity.this).load(mydog.getMessage()).into(binding.image);
                        //  binding.image.setImageResource(mydog.getMessage());
                        binding.text.setText(mydog.getStatus());


                    } else {
                        Toast.makeText(MainActivity.this, response.code(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onFailure(Call<Dog> call, Throwable t) {
                    Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        });

        binding.download.setOnClickListener(view -> {

            bitmapDrawable = (BitmapDrawable) binding.image.getDrawable();
            bitmap = bitmapDrawable.getBitmap();

            FileOutputStream fileOutputStream;
            File sdCard = Environment.getExternalStorageDirectory();
            File Directory = new File(sdCard.getAbsolutePath() + "/Download/Shubham");
            Directory.mkdir();
            String fileName = String.format("%d.jpg", System.currentTimeMillis());
            File outFile = new File(Directory, fileName);

            Toast.makeText(this, "Image Saved...", Toast.LENGTH_SHORT).show();
            try {
                fileOutputStream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();

                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(outFile));
                sendBroadcast(intent);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        binding.openCamera.setOnClickListener(v -> {
            openCamera();
        });
        binding.upLoad.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);

        });

    }
    // Method to open the camera
    private void openCamera() {
        // Create an intent to capture an image
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Create a file to store the captured image
            File imageFile = createImageFile();

            // If the file was created successfully
            if (imageFile != null) {
                // Get the URI of the image file
                imageUri = FileProvider.getUriForFile(this, "com.your.package.name.fileprovider", imageFile);

                // Pass the URI to the camera intent
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                // Start the camera activity
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // Create a file to store the captured image
    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp;

        // Get the directory to store the image file
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            // Create the image file
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // The image was captured successfully
            // You can use the imageUri to access the captured image
            // For example, you can display the image in an ImageView
            ImageView imageView = findViewById(R.id.image);
            imageView.setImageURI(imageUri);
        }
        if (requestCode == 1 && requestCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filepath = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filepath, null
                    , null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filepath[0]);
            String pitcherPath = cursor.getString(columnIndex);
            cursor.close();

            binding.image.setImageBitmap(BitmapFactory.decodeFile(pitcherPath));
            String filename = pitcherPath.substring(pitcherPath.lastIndexOf("/") + 1);
            binding.text.setText(filename);
        }
    }
}