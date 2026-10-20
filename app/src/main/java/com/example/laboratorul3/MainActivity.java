
package com.example.laboratorul3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ImageView firstImage;
    private ImageView secondImage;

    private ImageView selectedImage;
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        firstImage = findViewById(R.id.imageView);
        secondImage = findViewById(R.id.imageView2);

        findViewById(R.id.previousButton).setOnClickListener(v -> {
            firstImage.setImageResource(R.drawable.img);
            secondImage.setImageResource(R.drawable.img2);
        });

        findViewById(R.id.nextButton).setOnClickListener(v -> {
            firstImage.setImageResource(R.drawable.img2);
            secondImage.setImageResource(R.drawable.img);
        });

        registerForContextMenu(firstImage);
        registerForContextMenu(secondImage);
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {

        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

        selectedImage = (ImageView) v;

        if (v.getId() == R.id.imageView) {
            data = "Prima imagine";
        } else if (v.getId() == R.id.imageView2) {
            data = "A doua imagine";
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        int selectedItem = item.getItemId();

        if (selectedItem == R.id.view_info) {

            Intent intent = new Intent(
                    MainActivity.this,
                    MainActivity2.class
            );

            intent.putExtra("info", data);
            startActivity(intent);

            return true;

        } else if (selectedItem == R.id.edit) {

            Toast.makeText(
                    this,
                    "Ai selectat: " + data,
                    Toast.LENGTH_SHORT
            ).show();

            return true;

        }
        else if (selectedItem == R.id.share) {
            shareSelectedImage();
            return true;
        }
        else if (selectedItem == R.id.set_wallpaper) {
            setSelectedImageAsWallpaper();
            return true;
        }
        else if (selectedItem == R.id.delete) {

            selectedImage.setVisibility(View.GONE);

            Toast.makeText(
                    this,
                    "Imagine ștearsa din galerie",
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        }

        return super.onContextItemSelected(item);
    }

    private void setSelectedImageAsWallpaper() {
        if (selectedImage == null || selectedImage.getDrawable() == null) {
            Toast.makeText(
                    this,
                    "Nu exista o imagine selectata.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        android.graphics.drawable.Drawable drawable =
                selectedImage.getDrawable();

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        if (width <= 0 || height <= 0) {
            Toast.makeText(
                    this,
                    "Dimensiunea imaginii nu este valida.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
        );

        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        String savedImage = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bitmap,
                "GalerieFoto",
                "Imagine pentru fundal"
        );

        if (savedImage == null) {
            Toast.makeText(
                    this,
                    "Nu s-a putut salva imaginea.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Uri imageUri = Uri.parse(savedImage);

        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(imageUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(
                    intent,
                    "Seteaza imaginea ca fundal"
            ));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    "Nu exista o aplicatie compatibila pentru aceasta actiune.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void shareSelectedImage() {
        if (selectedImage == null || selectedImage.getDrawable() == null) {
            Toast.makeText(
                    this,
                    "Nu exista o imagine de partajat.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        android.graphics.drawable.Drawable drawable =
                selectedImage.getDrawable();

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        if (width <= 0 || height <= 0) {
            Toast.makeText(
                    this,
                    "Dimensiunea imaginii nu este valida.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
        );

        android.graphics.Canvas canvas =
                new android.graphics.Canvas(bitmap);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        File directory = new File(getCacheDir(), "shared_images");

        if (!directory.exists() && !directory.mkdirs()) {
            Toast.makeText(
                    this,
                    "Nu s-a putut pregati imaginea.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        File imageFile = new File(directory, "shared_image.png");

        try (FileOutputStream outputStream =
                     new FileOutputStream(imageFile)) {

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    imageFile
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(
                    intent,
                    "Partajeaza fotografia"
            ));

        } catch (IOException e) {
            Toast.makeText(
                    this,
                    "A aparut o eroare la pregatirea fotografiei.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int selectedItem = item.getItemId();

        if (selectedItem == R.id.sort) {

            Toast.makeText(
                    this,
                    "Ai selectat Sort",
                    Toast.LENGTH_SHORT
            ).show();

            return true;

        } else if (selectedItem == R.id.select) {

            Toast.makeText(
                    this,
                    "Ai selectat Select",
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
