package com.example.apicalling2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.apicalling2.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
        binding.upLoad.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,1);

        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && requestCode == RESULT_OK && null != data){
            Uri selectedImage = data.getData();
            String[] filepath = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,filepath,null
            ,null,null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filepath[0]);
            String pitcherPath = cursor.getString(columnIndex);
            cursor.close();

            binding.image.setImageBitmap(BitmapFactory.decodeFile(pitcherPath));
            String filename = pitcherPath.substring(pitcherPath.lastIndexOf("/")+1);
            binding.text.setText(filename);
        }
    }
}