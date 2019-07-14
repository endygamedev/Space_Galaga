package com.calculator.egor.m_game_galaga;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class tutotial extends AppCompatActivity {

    WebView _webTutorial, _webRotate;
    Button _btnMainMenu;
    TextView _lblTitle;
    Intent _mainMenu;

    /*Переменная для музыки на заднем фоне*/
    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutotial);

        // TODO: Убираем статус бар
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); /*Статичный статус экрана (Портретный)*/

        _webTutorial = (WebView) findViewById(R.id.webTutorial);
        _webTutorial.loadUrl("file:///android_asset/tutorial_drive.html"); /*Подключаем наше Gif-изображение из HTML файла*/

        _webRotate = (WebView) findViewById(R.id.webRotate);
        _webRotate.loadUrl("file:///android_asset/tutorial_rotate.html"); /*Подключаем наше Gif-изображение из HTML файла*/

        _btnMainMenu = (Button) findViewById(R.id.btnMainMenu);
        _lblTitle = (TextView) findViewById(R.id.lblTitle);

        _lblTitle.startAnimation(AnimationUtils.loadAnimation(tutotial.this, R.anim.text_title_anim)); /*Анимация для заглавного текста*/

        /*Загружаем MusicService*/
        doBindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        startService(music);

        /*Начинаем работу HomeWatchr'а*/
        mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
            @Override
            public void onHomeLongPressed() {
                if (mServ != null) {
                    mServ.pauseMusic();
                }
            }
        });
        mHomeWatcher.startWatch();

        // TODO: Событие для btnMainMenu
        _btnMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mainMenu = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(_mainMenu);
                finish();
            }
        });
    }


    // TODO: Настройки музыки для заднего фона. Код был взят с GitHub'а (https://github.com/moisoni97/services.backgroud_music)
    private boolean mIsBound = false;
    private MusicService mServ;
    private ServiceConnection Scon =new ServiceConnection(){
        public void onServiceConnected(ComponentName name, IBinder
                binder) {
            mServ = ((MusicService.ServiceBinder)binder).getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            mServ = null;
        }
    };

    void doBindService(){
        bindService(new Intent(this,MusicService.class),
                Scon, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if(mIsBound)
        {
            unbindService(Scon);
            mIsBound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        PowerManager pm = (PowerManager)
                getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = false;
        if (pm != null) {
            isScreenOn = pm.isScreenOn();
        }

        if (!isScreenOn) {
            if (mServ != null) {
                mServ.pauseMusic();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*Отключаем MusicService*/
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);

    }
}
