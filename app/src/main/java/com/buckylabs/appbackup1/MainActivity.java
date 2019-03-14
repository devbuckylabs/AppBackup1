package com.buckylabs.appbackup1;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.widget.GridLayout.HORIZONTAL;
import static android.widget.GridLayout.VERTICAL;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Apk> apks;
    List<ApplicationInfo> filteredApks;
    Context context;
    boolean isChecked;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS}, 1);
            requestPermissions(new String[]{Manifest.permission.MANAGE_DOCUMENTS}, 1);
            //   requestPermissions(new String[]{android.Manifest.permission.F}, 1);

        }


        String rootPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/App_Backup/";
        File f2 = new File(rootPath);
        if (!f2.exists()) {
            f2.mkdirs();
        }


        handler=new Handler(getMainLooper());

        recyclerView = findViewById(R.id.recyclerView);

        PackageManager pm = getPackageManager();
        apks = new ArrayList<>();
        filteredApks = new ArrayList<>();
        recyclerView.hasFixedSize();
        DividerItemDecoration itemDecor = new DividerItemDecoration(context, HORIZONTAL);
        itemDecor.setOrientation(VERTICAL);
        recyclerView.addItemDecoration(itemDecor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final MyAdapter adapter = new MyAdapter(context, apks);

        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

                Log.e("Before","****");
                Log.e("------",filteredApks.toString());
                BackgroundThread backgroundThread=new BackgroundThread(context,filteredApks);
                backgroundThread.execute();
                Log.e("After","****");
                Log.e("------",filteredApks.toString());

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(context, "background running", Toast.LENGTH_SHORT).show();

                    }
                });
                //filteredApks.clear();
                //uncheckAll();


              /*  new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("------","****");
                        Log.e("------",filteredApks.toString());

                        getCheckedApks(filteredApks);
                        Log.e("------","Backuppppppp");
                        filteredApks.clear();
                        uncheckAll();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }
                }).start();*/



            }
        }));


        getAllInstalledApps(pm);



    }

    public void ApkBackupGenerator(String s) {

        try {
            String rootPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/App_Backup/";
            File root = new File(rootPath);
            if (!root.exists()) {
                root.mkdirs();
            }
            File f = new File(rootPath + s);
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();

            FileOutputStream out = new FileOutputStream(f);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getAllInstalledApps(PackageManager pm) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        Collections.sort(apps, new ApplicationInfo.DisplayNameComparator(pm));
        for (ApplicationInfo app : apps) {
            Log.d("--", "-------------------");
            Log.d("AppName", " " + app.name);

            String Appname = (String) app.loadLabel(pm);
           Drawable AppIcon = app.loadIcon(pm);
            Apk apk = new Apk(Appname, AppIcon, app, isChecked);
            apks.add(apk);

        }

    }

    public void restoreApp() {

        //  String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/App_Backup/";
        File file = new File(Environment.getExternalStorageDirectory(),
                "Calculator.apk");
        //Uri path = Uri.fromFile(file);
        Uri path = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent pdfOpenintent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pdfOpenintent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        pdfOpenintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pdfOpenintent.setDataAndType(path, "application/vnd.android.package-archive");
        try {
            Toast.makeText(context, "opening", Toast.LENGTH_LONG).show();
            // startActivityForResult(pdfOpenintent, Activity.RESULT_OK);
            startActivity(pdfOpenintent);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "-------------------------", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


    }


    public void getBackupOfAllApks(List<ApplicationInfo> apps) {


        for (ApplicationInfo info : apps) {
            Log.e("Size------  ",apps.size()-1+"  ");
            try {
                File f1 = new File(info.sourceDir);
                String rootPath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/App_Backup/";
                String file_name = info.loadLabel(getPackageManager()).toString();
                //File f2 = new File(Environment.getExternalStorageDirectory().toString()+"/Folder");
                File f2 = new File(rootPath);
                if (!f2.exists()) {
                    f2.mkdirs();
                }
                Log.e("Backing up ",file_name);
                f2 = new File(f2.getPath() + "/" + file_name + ".apk");
                f2.createNewFile();
                InputStream in = new FileInputStream(f1);
                FileOutputStream out = new FileOutputStream(f2);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                Log.e("BackUp Complete ",file_name);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void getCheckedApks(List<ApplicationInfo> filteredApks) {

       if(filteredApks==null){
         Log.e("No-----","No items checked");
       }else{
        for (Apk apk : apks) {

            if (apk.isChecked()) {

                filteredApks.add(apk.getAppInfo());
                Log.e("Backing up *** ",apk.getAppName());
            }

            // Log.e("Checked",""+filteredApks.toString());

        }

           getBackupOfAllApks(filteredApks);
        }

    }


    public void uncheckAll(){

        for(Apk apk:apks){

            apk.setChecked(false);

        }


    }



























    public static interface ClickListener {
        public void onClick(View view, int position);

        public void onLongClick(View view, int position);

    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                clicklistener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


    class BackgroundThread extends AsyncTask<Void, Void, Void> {
int i=0;
        Context context;
        List<ApplicationInfo> checkedApks;

        public BackgroundThread(Context context, List<ApplicationInfo> checkedApks) {
            this.context = context;
            this.checkedApks = checkedApks;
        }



        @Override
        protected Void doInBackground(Void... voids) {
 handler.post(new Runnable() {
     @Override
     public void run() {
         i++;
         Log.e("in DoInBackground "+i,"*****************************");

     }
 });
            getCheckedApks(checkedApks);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        handler.post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(context, "Backed Up..........", Toast.LENGTH_SHORT).show();
                Log.e("Done","BAckedUp");

            }
        });
        }

        public void getCheckedApks(List<ApplicationInfo> filteredApks) {

            for (Apk apk : apks) {

                if (apk.isChecked()) {

                    filteredApks.add(apk.getAppInfo());
                }

                // Log.e("Checked",""+filteredApks.toString());


            }
            getBackupOfAllApks(filteredApks);
        }
    }


}

