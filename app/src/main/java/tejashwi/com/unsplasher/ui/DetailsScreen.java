/*
 * Copyright (c) 2018, Tejashwi Kalp Taru
 */

package tejashwi.com.unsplasher.ui;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tejashwi.com.unsplasher.R;
import tejashwi.com.unsplasher.rest.APIUtils;
import tejashwi.com.unsplasher.rest.Services;

public class DetailsScreen extends AppCompatActivity implements IDownloadCompleteNotifier {
    private String mUrl = "";
    private String mDownload = "";
    private String mAuthor = "";
    private String mID = "";
    private String TAG = "DetailsScreen";
    private int REQUEST_WRITE = 01;
    private int REQUEST_WALLPAPER = 02;
    private Services mAPIservice = null;
    private Button mDownloadButton = null;
    private Button mSetWallpaper = null;
    private ImageView mImageView;
    private File mPhoto = null;
    private boolean applyWallpaper = false;
    private Bitmap mBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_screen);

        mUrl = getIntent().getExtras().getString("url");
        mDownload = getIntent().getExtras().getString("download");
        mAuthor = getIntent().getExtras().getString("download");
        mID = getIntent().getExtras().getString("id");

        mImageView = findViewById(R.id.imagePreview);
        mDownloadButton = findViewById(R.id.downloadImage);
        mSetWallpaper = findViewById(R.id.setWallpaper);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String appDirectoryName = getString(R.string.app_name);
                File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), appDirectoryName);
                imageRoot.mkdirs();
                mPhoto = new File(imageRoot.getPath(), mID + ".jpg");
                if(mPhoto.exists()){
                    mBitmap = BitmapFactory.decodeFile(mPhoto.getPath());
                    mImageView.setImageBitmap(mBitmap);
                    mDownloadButton.setText("Downloaded");
                    mDownloadButton.setEnabled(false);
                } else {
                    Glide.with(DetailsScreen.this).load(mUrl).into(mImageView);
                }
            }
        });


        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadButton.setEnabled(false);
                if(isStoragePermissionGranted()){
                    if(mBitmap == null){
                        downloadImage();
                    } else {
                        mDownloadButton.setText("Downloaded");
                        mDownloadButton.setEnabled(false);
                    }
                }
            }
        });

        mSetWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSetWallpaper.setEnabled(false);
                applyWallpaper = true;
                if(isStoragePermissionGranted()){
                    mSetWallpaper.setEnabled(false);
                    if(mBitmap == null){
                        downloadImage();
                    } else {
                        setWallpaper();
                    }
                }
            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Storage Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Storage Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Storage Permission is granted");
            return true;
        }
    }

    public boolean isWallpaperChangePermissionGranted(){
        if (Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.SET_WALLPAPER) == PackageManager.PERMISSION_GRANTED){
                Log.v(TAG, "Wallpaper permission is granted");
                return true;
            } else {
                Log.v(TAG, "Wallpaper permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SET_WALLPAPER}, REQUEST_WALLPAPER);
                return false;
            }
        } else {
            //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Wallpaper Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: " + permissions[0] + " was " + grantResults[0]);
            if(requestCode == REQUEST_WRITE){
                if(mBitmap == null) {
                    if(applyWallpaper == true){
                        mSetWallpaper.setEnabled(false);
                    }
                    downloadImage();
                }
            }
            if(requestCode == REQUEST_WALLPAPER){
                changeWallpaper();
            }
        }
    }

    private void downloadImage() {
        if(mAPIservice == null){
            mAPIservice = APIUtils.getClient();
        }

        mDownloadButton.setText("Downloading");
        mDownloadButton.setEnabled(false);

        mAPIservice.downloadImage(mDownload).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    saveToDisk(response.body(), mPhoto.getPath());
                } else {
                    mDownloadButton.setText("Download");
                    mDownloadButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mDownloadButton.setText("Download");
                mDownloadButton.setEnabled(true);
            }
        });
    }

    private void saveToDisk(ResponseBody body, String path){
        try {
            SavePhotoTask task = new SavePhotoTask(path, body.bytes(), this);
            task.execute();
        } catch (IOException ioe){
            Log.e(TAG, ioe.getMessage());
        }
    }

    private void setWallpaper() {
        if(mBitmap != null){
            if(isWallpaperChangePermissionGranted()){
                changeWallpaper();
            }
        } else {
            downloadImage();
        }
    }

    private void changeWallpaper(){
        WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());

        File wallpaper_file = new File(mPhoto.getPath());
        Uri contentURI = getImageContentUri(getApplicationContext(),wallpaper_file);

        ContentResolver cr = this.getContentResolver();
        Log.d("CONTENT TYPE: ", "IS: " + cr.getType(contentURI));

        Intent intent = new Intent(manager.getCropAndSetWallpaperIntent(contentURI));
        startActivity(intent);
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    @Override
    public void onDownloadComplete(boolean result, Bitmap bmp) {
        mBitmap = bmp;
        if(applyWallpaper){
            changeWallpaper();
        }
        mDownloadButton.setText("Downloaded");
        mDownloadButton.setEnabled(false);
    }

    @Override
    public void onDownloadFailure(boolean result, String msg) {
        mDownloadButton.setText("Download");
        mDownloadButton.setEnabled(true);
    }
}

class SavePhotoTask extends AsyncTask<Void, Void, Void> {
    private String path;
    private byte[] bytes;
    private IDownloadCompleteNotifier notifier;
    private Bitmap bmp = null;
    private String msg = "";

    SavePhotoTask(String path, byte[] bytes, IDownloadCompleteNotifier notifier){
        this.path = path;
        this.bytes = bytes;
        this.notifier = notifier;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(this.bmp != null){
            notifier.onDownloadComplete(true, bmp);
        } else {
            notifier.onDownloadFailure(false, this.msg);
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            FileOutputStream fos=new FileOutputStream(this.path);
            fos.write(this.bytes);
            fos.close();
            this.bmp = BitmapFactory.decodeFile(this.path);
        }
        catch (java.io.IOException e) {
            this.msg = e.getMessage();
            this.bmp = null;
        }
        return null;
    }
}

interface IDownloadCompleteNotifier{
    void onDownloadComplete(boolean result, Bitmap bmp);
    void onDownloadFailure(boolean result, String msg);
}
