package model.com.app.lyudony;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.lyudony.clockview.widgets.TimeDiskView;

import java.util.List;

/**
 * 主入口 启动服务
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG ="MainActivity" ;

    //实例化SharedPreferences对象
    private SharedPreferences mySharedPreferences;

    private IDialog mTimeDialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置去title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //检查权限
//       WindowPermissionCheck.checkPermission(MainActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                //若未授权则请求权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
            //启动服务
            Intent intentService = new Intent(MainActivity.this, AutomaticTaskService.class);
            startService(intentService);
        }


        //启动时间
        TimeDiskView timeDiskView = findViewById(R.id.custom_view_time);
        timeDiskView.start();

        mTimeDialog = new IDialog(MainActivity.this, R.style.DialogTheme);

        final Button btnStarTime = findViewById(R.id.btn_star_time);//设置开始时间
        final Button btnEndTime = findViewById(R.id.btn_end_time);//设置结束时间

        final Button btnOnOff = findViewById(R.id.btn_onOff);

        //实例化SharedPreferences对象（第一步）
        mySharedPreferences = getSharedPreferences("shared", Activity.MODE_PRIVATE);
        //实例化SharedPreferences.Editor对象（第二步）
        final SharedPreferences.Editor editor = mySharedPreferences.edit();

        String starTime = mySharedPreferences.getString("startTime", "");
        String endTime = mySharedPreferences.getString("endTime", "");

        if (starTime.length() > 0) {
            btnStarTime.setText("begin " + starTime);
        } else {
            btnStarTime.setText("设置上班打卡时间");
        }
        if (endTime.length() > 0) {
            btnEndTime.setText("end " + endTime);
        } else {
            btnEndTime.setText("设置下班打卡时间");
        }

        btnStarTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimeDialog.show();
                mTimeDialog.setOnTimePickerClickListener(new IDialog.OnTimePickerClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void setTime(String hour, String minute) {
                        btnStarTime.setText("begin " + hour + minute);
                        //用putString的方法保存数据
                        editor.putString("startTime", hour + minute);
                        //提交当前数据
                        editor.apply();
                    }
                });
            }
        });

        btnEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimeDialog.show();
                mTimeDialog.setOnTimePickerClickListener(new IDialog.OnTimePickerClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void setTime(String hour, String minute) {
                        btnEndTime.setText("end " + hour + minute);
                        //用putString的方法保存数据
                        editor.putString("endTime", hour + minute);
                        //提交当前数据
                        editor.apply();
                    }
                });
            }
        });

        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if ("ON".equals(btnOnOff.getText().toString())) {
                    btnOnOff.setText(R.string.off);
                    btnOnOff.setTextColor(getResources().getColor(R.color.color_off));
                    Toast.makeText(getApplicationContext(), "已关闭", Toast.LENGTH_SHORT).show();
                } else {
                    btnOnOff.setText(R.string.on);
                    btnOnOff.setTextColor(getResources().getColor(R.color.color_on));
                    Toast.makeText(getApplicationContext(), "已打开", Toast.LENGTH_SHORT).show();
                }
                try {
                    PackageManager packageManager = getPackageManager();
                    PackageInfo packageInfo = null;

                    packageInfo = packageManager.getPackageInfo("com.tencent.wework", 0);

                    //设置意图
                    Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
                    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    if (packageInfo != null) {
                        resolveIntent.setPackage(packageInfo.packageName);
                    }
                    List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
                    ResolveInfo resolveInfo = apps.iterator().next();
                    if (resolveInfo != null && packageInfo != null) {
                        String className = resolveInfo.activityInfo.name;
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Log.e(TAG, "packageName ------------" + packageInfo.packageName);
                        ComponentName cn = new ComponentName(packageInfo.packageName, className);
                        intent.setComponent(cn);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                            Log.e(TAG, "PendingIntent error --------" + e.getMessage());
                        }

                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });

    }


    @Override
    //重写上下文菜单的创建方法
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_context, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        switch (item.getItemId()) {
            case R.id.we_chat:
                editor.putString("model", "wechat");
                editor.apply();
                Toast.makeText(this, "已设置企业微信打卡模式", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"已设置企业微信打卡模式");
                break;
            case R.id.ding_ding:
                editor.putString("model", "dingding");
                editor.apply();
                Toast.makeText(this, "已设置钉钉打卡模式", Toast.LENGTH_SHORT).show();
                Log.e(TAG,"已设置钉钉打卡模式");
                break;
        }
        return true;
    }
}
