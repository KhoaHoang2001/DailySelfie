package com.example.dailyselfie;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final long INTERVAL_TWO_MINUTES = 2 * 30 * 1000L;

    private SelfieRecordAdapter mAdapter;
    private String mCurrentSelfieName;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView selfieList = (ListView) findViewById(R.id.selfie_list);

        mAdapter = new SelfieRecordAdapter(getApplicationContext());
        selfieList.setAdapter(mAdapter);
        selfieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelfieRecord selfieRecord = (SelfieRecord) mAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, selfieRecord.getPath());
                startActivity(intent);
            }
        });
        createSelfieAlarm();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_camera) {
            dispatchTakePictureIntent();
            return true;
        }
        if (id == R.id.action_delete_selected) {
            deleteSelectedSelfies();
            return true;
        }
        if (id == R.id.action_delete_all) {
            deleteAllSelfies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        mCurrentSelfieName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile = File.createTempFile(
                mCurrentSelfieName,
                ".jpg",
                getExternalFilesDir(null));

        mCurrentPhotoPath = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                try {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", createImageFile()));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File photoFile = new File(mCurrentPhotoPath);
            File selfieFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), mCurrentSelfieName + ".jpg");
            photoFile.renameTo(selfieFile);

            SelfieRecord selfieRecord = new SelfieRecord(Uri.fromFile(selfieFile).getPath(), mCurrentSelfieName);
            mAdapter.add(selfieRecord);
        } else {
            File photoFile = new File(mCurrentPhotoPath);
            photoFile.delete();
        }
    }

    private void deleteSelectedSelfies() {
        ArrayList<SelfieRecord> selectedSelfies = mAdapter.getSelectedRecords();
        for (SelfieRecord selfieRecord : selectedSelfies) {
            File selfieFile = new File(selfieRecord.getPath());
            selfieFile.delete();
        }
        mAdapter.clearSelected();
    }

    private void deleteAllSelfies() {
        for (SelfieRecord selfieRecord : mAdapter.getAllRecords()) {
            File selfieFile = new File(selfieRecord.getPath());
            selfieFile.delete();
        }
        mAdapter.clearAll();
    }

    private void createSelfieAlarm() {
        Intent intent = new Intent(this, SelfieNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 10,
                INTERVAL_TWO_MINUTES,
                pendingIntent);
    }
}