package com.calculator.egor.m_game_galaga;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;

import java.util.Timer;

public class gameClass {

    // TODO: Метод перезагрузки уровня
    public static void gameRestart() {
        game.seconds = 0;
        game.hearts = 3;
        game._imgHeartUI1.setVisibility(View.VISIBLE);
        game._imgHeartUI2.setVisibility(View.VISIBLE);
        game._imgHeartUI3.setVisibility(View.VISIBLE);
    }

    // TODO: Метод столкновения корабля и астероида
    public static void collisionAsteroid() {
        switch (game.hearts){
            case 3:
                game._imgHeartUI3.setVisibility(View.INVISIBLE);
                game.hearts -= 1;
                game._vibrator.vibrate(300);
                break;

            case 2:
                game._imgHeartUI2.setVisibility(View.INVISIBLE);
                game.hearts -= 1;
                game._vibrator.vibrate(300);
                break;

            case 1:
                game.hearts = 0;
                break;
        }
    }

    // TODO: Метод столкновения корабля и гриба
    public static void collisionMushroom(int i, ConstraintLayout _holst) {
        switch (game.hearts){
            case 3:
                _holst.removeView(_holst.getChildAt(i));
                break;

            case 2:
                game._imgHeartUI3.setVisibility(View.VISIBLE);
                game.hearts += 1;
                _holst.removeView(_holst.getChildAt(i));
                break;

            case 1:
                game._imgHeartUI2.setVisibility(View.VISIBLE);
                game.hearts += 1;
                _holst.removeView(_holst.getChildAt(i));
                break;
        }
    }

    // TODO: Метод для остановки всех таймеров
    public static void allTimersOff(){
        game._timerCheck.cancel();
        game._timerCheck = null;
        game._timerGenerateAsteroid.cancel();
        game._timerGenerateAsteroid = null;
        game._timerFall.cancel();
        game._timerFall = null;
        game._timerGenerateMushroom.cancel();
        game._timerGenerateMushroom = null;
    }

    // TODO: Метод для запуска всех таймеров
    public static void allTimersOn(){
        if (game._timerCheck != null)
            game._timerCheck.cancel();
        game._timerCheck = new Timer();

        if (game._timerFall != null)
            game._timerFall.cancel();
        game._timerFall = new Timer();

        if (game._timerGenerateMushroom != null)
            game._timerGenerateMushroom.cancel();
        game._timerGenerateMushroom = new Timer();
    }
}
