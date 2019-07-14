package com.calculator.egor.m_game_galaga;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class game_over extends AppCompatActivity {

    // TODO: Переменные
    Button _btnTryAgain, _btnMainMenu, _btnQuit;
    public static TextView _lblYourScore, _lblHighScore, _lblNewHighScore;
    TextView _lblGameOver;
    Intent _mainMenu, _game;
    int _score;

    /*Переменная для музыки на заднем фоне*/
    HomeWatcher mHomeWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // TODO: Убираем статус бар
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); /*Статичный статус экрана (Портретный)*/

        // TODO: Описываем объекты
        _btnTryAgain = (Button) findViewById(R.id.btnTryAgain);
        _btnMainMenu = (Button) findViewById(R.id.btnMainMenu);
        _btnQuit = (Button) findViewById(R.id.btnQuit);
        _lblYourScore = (TextView) findViewById(R.id.lblYourScore);
        _lblGameOver = (TextView) findViewById(R.id.lblGameOver);
        _lblHighScore = (TextView) findViewById(R.id.lblHighScore);
        _lblNewHighScore = (TextView) findViewById(R.id.lblNewHighScore);

        _lblNewHighScore.setVisibility(View.INVISIBLE);

        // TODO: Событие для демнонстрации наилучшего результата ("High Score")
        _score = getIntent().getIntExtra("score", 0);
        _lblYourScore.setText("Your score: " + String.valueOf(_score));

        SharedPreferences prefs = getSharedPreferences("game_data",Context.MODE_PRIVATE);
        int _highScore = prefs.getInt("high_score", 0);

        if (_score > _highScore){
            _lblHighScore.setText("Your best score: " + String.valueOf(_score));
            _lblNewHighScore.startAnimation(AnimationUtils.loadAnimation(game_over.this, R.anim.text_title_anim)); /*Анимация для нового рекорда*/
            _lblNewHighScore.setVisibility(View.VISIBLE);

            /*Обновляем лучший результат*/
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("high_score", _score);
            editor.commit();
        }
        else {
            _lblHighScore.setText("Your best score: " + String.valueOf(_highScore));
            _lblNewHighScore.setVisibility(View.INVISIBLE);
        }

        _lblGameOver.startAnimation(AnimationUtils.loadAnimation(game_over.this,R.anim.text_title_anim)); /*Анимация для заглавного текста*/

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

        // TODO: Событие для btnTryAgain
        _btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameClass.gameRestart(); /*Метод перезагрузки уровня*/
                _game = new Intent(getApplicationContext(), game.class);
                startActivity(_game);
                finish();
            }
        });

        // TODO: Событие для btnMainMenu
        _btnMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameClass.gameRestart(); /*Метод перезагрузки уровня*/
                _mainMenu = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(_mainMenu);
                finish();
            }
        });

        // TODO: Событие для btnQuit
        _btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
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
