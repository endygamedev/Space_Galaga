package com.calculator.egor.m_game_galaga;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class splashscreen extends AppCompatActivity {

    // TODO: Переменные
    ImageView _imgTitle;
    AnimationDrawable _animTitle;
    TextView _lblTitle, _lblCreator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        // TODO: Убираем статус бар
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); /*Статичный статус экрана (Портретный)*/

        // TODO: Описываем объекты
        _imgTitle = (ImageView) findViewById(R.id.imgTitle);
        _lblTitle = (TextView) findViewById(R.id.lblTitle);
        _lblCreator = (TextView) findViewById(R.id.lblCreator);

        // TODO: Анимация титульной картинки
        _imgTitle.setImageResource(R.drawable.anim_logo);
        _animTitle = (AnimationDrawable) _imgTitle.getDrawable();
        _animTitle.setOneShot(false);
        _animTitle.stop();
        _animTitle.start();

        // TODO: Анимация заглавного текста
        _lblTitle.startAnimation(AnimationUtils.loadAnimation(splashscreen.this, R.anim.text_splash_screen)); /*Анимация для заглавного текста*/
        _lblCreator.startAnimation(AnimationUtils.loadAnimation(splashscreen.this, R.anim.text_splash_screen)); /*Анимация для текста создателя*/
        _imgTitle.startAnimation(AnimationUtils.loadAnimation(splashscreen.this, R.anim.text_splash_screen)); /*Анимация для картинки*/

        // TODO: Переход в Главное меню
        Thread _thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3100);
                    Intent _screen = new Intent(splashscreen.this, MainActivity.class);
                    startActivity(_screen);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        _thread.start();
    }
}
