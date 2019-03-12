package com.buckylabs.appbackup1;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Apk> apks;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{Manifest.permission.WRITE_SETTINGS}, 1);
            requestPermissions(new String[]{Manifest.permission.MANAGE_DOCUMENTS}, 1);
            //   requestPermissions(new String[]{android.Manifest.permission.F}, 1);

        }

        startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:com.buckylabs.appbackup1")));

      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);

            }
        }*/

        recyclerView=findViewById(R.id.recyclerView);

        PackageManager pm = getPackageManager();
        apks=new ArrayList<>();
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MyAdapter adapter=new MyAdapter(context,apks);

        recyclerView.setAdapter(adapter);

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent .addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities( intent , 0);
        Collections.sort(apps,new ResolveInfo.DisplayNameComparator(pm));
        for(ResolveInfo app:apps){
            Log.d("--","-------------------");
            Log.d("AppName"," "+app.activityInfo.loadLabel(pm));

            String Appname= (String) app.activityInfo.loadLabel(pm);
            Drawable AppIcon= app.activityInfo.loadIcon(pm);
            Apk apk=new Apk(Appname,AppIcon);
            apks.add(apk);

        }







      //  String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/App_Backup/";
        File file = new File(Environment.getExternalStorageDirectory(),
                "Calculator.apk");
        //Uri path = Uri.fromFile(file);
        Uri path = FileProvider.getUriForFile(context,context.getPackageName()+ ".provider",file);
        Intent pdfOpenintent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
       pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pdfOpenintent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        pdfOpenintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pdfOpenintent.setDataAndType(path, "application/vnd.android.package-archive");
        try {
            Toast.makeText(context, "opening", Toast.LENGTH_LONG).show();
           // startActivityForResult(pdfOpenintent, Activity.RESULT_OK);
              startActivity(pdfOpenintent);

        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(context, "-------------------------", Toast.LENGTH_LONG).show();
e.printStackTrace();
        }



        /*for (ResolveInfo info : apps) {

            try {
                File f1 =new File( info.activityInfo.applicationInfo.publicSourceDir);
                String rootPath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/App_Backup/";
                String file_name = info.loadLabel(getPackageManager()).toString();
                //File f2 = new File(Environment.getExternalStorageDirectory().toString()+"/Folder");
                File f2 = new File(rootPath);
                if (!f2.exists()) {
                    f2.mkdirs();
                }
                f2 = new File(f2.getPath()+"/"+file_name+".apk");
                f2.createNewFile();
                InputStream in = new FileInputStream(f1);
                FileOutputStream out = new FileOutputStream(f2);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0){
                    out.write(buf, 0, len);
                }
                out.flush();
                out.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
*/


    }

public void ApkBackupGenerator(String s){

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

}
