package com.calculator.egor.m_game_galaga;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class game extends AppCompatActivity {

    // TODO: Переменные
    public static int seconds = 0, hearts = 3;
    int[] newWaveDifficulties = new int[] {30, 60, 90, 120};
    float imgDX, imgDY;
    float y, dY = 10;
    Random rnd = new Random();

    TextView _lblScore;
    public  static ImageView _imgHeartUI1, _imgHeartUI2, _imgHeartUI3;
    ImageView _imgShip, _imgAsteroid, _imgMushroom;
    ImageButton _imgBtnPause;
    ConstraintLayout _mainConstaint;
    ConstraintLayout _holst;
    ConstraintLayout.LayoutParams layoutParams;
    AnimationDrawable _animShip, _animExplosive;
    public static Timer _timerGenerateAsteroid, _timerFall, _timerGenerateMushroom, _timerCheck;
    Display display;
    LayoutInflater _inf;
    Intent _gameOver, _mainMenu;

    /*Задачи для таймеров*/
     TaskGenerateAsteroid _tGenAsteroid;
    TaskFall _tF;
    TaskGenerateMushroom _tMushroom;
    TaskCheckCollision _tCheckCollision;

    /*Переменные для гироскопа*/
    SensorManager sensorManager;
    Sensor gyroscopeSensor;
    SensorEventListener gyroscopeEventListener;

    /*Переменная для вибрации*/
    public static Vibrator _vibrator;

    /*Переменная для музыки на заднем фоне*/
    HomeWatcher mHomeWatcher;

    // TODO: Переменная Collision для опеределения столкноваения корабля с объектами
    public boolean Collision(ImageView _imgShip, View _imgAsteroid) {
        Rect shipRect = new Rect();
        _imgShip.getHitRect(shipRect);
        Rect asteroidRect = new Rect();
        _imgAsteroid.getHitRect(asteroidRect);
        return shipRect.intersect(asteroidRect);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // TODO: Убираем статус бар
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); /*Статичный статус экрана (Портретный)*/

        // TODO: Описываем объекты
        _lblScore = (TextView) findViewById(R.id.lblScore);
        _imgShip = (ImageView) findViewById(R.id.imgShip);
        _mainConstaint = (ConstraintLayout) findViewById(R.id.ConstraintMainLayout);
        _holst = (ConstraintLayout) findViewById(R.id.ConstraintLayout);
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        /*Описываем объекты для сенсора*/
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        _vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE); // Описываем вибратор

        _imgHeartUI1 = (ImageView) findViewById(R.id.imgHeartUI1);
        _imgHeartUI2 = (ImageView) findViewById(R.id.imgHeartUI2);
        _imgHeartUI3 = (ImageView) findViewById(R.id.imgHeartUI3);

        _imgBtnPause = (ImageButton) findViewById(R.id.imgBtnPause);

        // TODO: Анимация корабля
        _imgShip.setImageResource(R.drawable.anim_ship);
        _animShip = (AnimationDrawable) _imgShip.getDrawable();
        _animShip.setOneShot(false);
        _animShip.stop();
        _animShip.start();

        /*Таймер для генерации астероидов*/
        _timerGenerateAsteroid = new Timer();
        _tGenAsteroid = new TaskGenerateAsteroid();
        _timerGenerateAsteroid.schedule(_tGenAsteroid, 0, 1100);

        /*Таймер для падения астероидов*/
        _timerFall = new Timer();
        _tF = new TaskFall();
        _timerFall.schedule(_tF, 200, 70);

        /*Таймер для проверки столкновений*/
        _timerCheck = new Timer();
        _tCheckCollision = new TaskCheckCollision();
        _timerCheck.schedule(_tCheckCollision, 0, 500);

        /*Таймер для генерации грибов*/
        _timerGenerateMushroom = new Timer();
        _tMushroom = new TaskGenerateMushroom();
        _timerGenerateMushroom.schedule(_tMushroom, 30000, 30000);

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

        // TODO: Событие перетягивания корабля без ACTION_UP
        _imgShip.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imgDX = _imgShip.getX() - event.getRawX();
                        imgDY = _imgShip.getY() - event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        _imgShip.animate()
                                .x(event.getRawX() + imgDX)
                                .y(event.getRawY() + imgDY)
                                .setDuration(0)
                                .start();
                        break;

                    default:
                        return false;
                }
                return true;
            }
        });


        // TODO: Событие для гироскопа
        if (gyroscopeSensor == null) {
            Toast.makeText(getApplicationContext(), "Gyroscope sensor not available.", Toast.LENGTH_SHORT).show();
            finish();
        }

        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[2] > 1f)
                    _mainConstaint.setBackgroundResource(R.drawable.space_background_red);
                else if (event.values[2] < -1f)
                    _mainConstaint.setBackgroundResource(R.drawable.space_background_blue);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        // TODO: Событие для imgBtnPause
        _imgBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                _imgShip.setEnabled(false);
                gameClass.allTimersOff(); /*Метод остановки всех таймеров*/

                AlertDialog.Builder ad = new AlertDialog.Builder(game.this);
                ad.setTitle("Pause Menu")
                        .setMessage("Game is Paused!"+ "\nYou can resume the game or quit to main menu.")
                        .setCancelable(false);

                ad.setNegativeButton("Resume", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        _imgShip.setEnabled(true);
                        gameClass.allTimersOn(); /*Метод запуска всех таймеров*/

                        pauseGenerateAsteroid(); /*Метод для перезагруки астероидов в режиме паузы*/

                        _tF = new TaskFall();
                        _timerFall.schedule(_tF, 200, 70);

                        _tCheckCollision = new TaskCheckCollision();
                        _timerCheck.schedule(_tCheckCollision, 0, 500);

                        _tMushroom = new TaskGenerateMushroom();
                        _timerGenerateMushroom.schedule(_tMushroom,30000,30000);
                    }
                });

                ad.setPositiveButton("Main Menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gameClass.gameRestart(); /*Метод перезагрузки уровеня*/
                        _mainMenu = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(_mainMenu);
                        finish();
                    }
                });

                ad.show();
            }
        });
    }


    // TODO: Класс для генерации астероидов
    class TaskGenerateAsteroid extends TimerTask {
        @Override
        public void run() {
            seconds++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _lblScore.setText("Score: " + String.valueOf(seconds));
                    _inf = LayoutInflater.from(game.this);
                    _imgAsteroid = (ImageView) _inf.inflate(R.layout.asteroid, null);
                    _holst.addView(_imgAsteroid);

                    _imgAsteroid.setId(seconds);
                    _imgAsteroid.setTag("asteroid");
                    _imgAsteroid.getLayoutParams().width = rnd.nextInt(270-170+1)+170; /*Задаём разную ширину*/
                    _imgAsteroid.getLayoutParams().height =  rnd.nextInt(270-170+1)+170; /*Задаём разную высоту*/

                    layoutParams = (ConstraintLayout.LayoutParams) _imgAsteroid.getLayoutParams();

                    _imgAsteroid.setX(rnd.nextInt(_holst.getWidth() - 50));
                    _imgAsteroid.setY(_holst.getHeight() - 1950);
                }
            });
        }
    }

    // TODO: Класс для генерации грибов
    class TaskGenerateMushroom extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _inf = LayoutInflater.from(game.this);
                    _imgMushroom = (ImageView) _inf.inflate(R.layout.mushroom, null);
                    _holst.addView(_imgMushroom);

                    _imgMushroom.setId(seconds);
                    _imgMushroom.setTag("mushroom");

                    layoutParams = (ConstraintLayout.LayoutParams) _imgMushroom.getLayoutParams();

                    _imgMushroom.setX(rnd.nextInt(_holst.getWidth() - 30));
                    _imgMushroom.setY(_holst.getHeight() - 1950);
                }
            });
        }
    }


    // TODO: Класс для падения астероидов
    class TaskFall extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < _holst.getChildCount(); i++) {
                        y = _holst.getChildAt(i).getY();
                        y += dY;
                        _holst.getChildAt(i).setY(y);
                        if (y > _holst.getHeight() - 50)
                            _holst.removeViewAt(i);
                    }
                }
            });
        }
    }

    // TODO: Класс для проверки столкновений
    class  TaskCheckCollision extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    waveDifficulties(); /*Проверка новой волны сложности*/

                    // TODO: Проверка на количество жизней
                    if (hearts == 0) {
                        _vibrator.vibrate(500);
                        _imgHeartUI1.setVisibility(View.INVISIBLE);
                        gameClass.allTimersOff(); /*Метод остановки всех таймеров*/

                        _imgShip.setImageResource(R.drawable.anim_explosive);
                        _animExplosive = (AnimationDrawable) _imgShip.getDrawable();
                        _animExplosive.setOneShot(false);
                        _animExplosive.stop();
                        _animExplosive.start();

                        _imgShip.setEnabled(false);

                        /*Процесс для звершения игры*/
                        Thread _threadGameOver = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    sleep(3000);
                                    _gameOver = new Intent(game.this, game_over.class);
                                    _gameOver.putExtra("score", seconds);
                                    startActivity(_gameOver);
                                    finish();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        _threadGameOver.start();
                    }

                    /*Проверка на вылет нашего корабля за пределы экрана*/
                    if (_imgShip.getY() > (_holst.getHeight() - 50)) {
                        hearts = 0;
                        _imgHeartUI1.setVisibility(View.INVISIBLE);
                        _imgHeartUI2.setVisibility(View.INVISIBLE);
                        _imgHeartUI3.setVisibility(View.INVISIBLE);
                    }

                    // TODO: Столкновения корабля и объектов
                    for (int i = 0; i < _holst.getChildCount(); i++) {

                        /*Проверка столкновения между кораблем и астероидом*/
                        if (String.valueOf(_holst.getChildAt(i).getTag()) == "asteroid") {
                            if (Collision(_imgShip, _holst.getChildAt(i)))
                                gameClass.collisionAsteroid();
                        }

                        /*Проверка столкновения между кораблем и грибом*/
                        if (String.valueOf(_holst.getChildAt(i).getTag()) == "mushroom") {
                            if (Collision(_imgShip,_holst.getChildAt(i))) {
                                gameClass.collisionMushroom(i, _holst);
                            }
                        }
                    }
                }
            });
        }
    }

    // TODO: Метод для усложенения уровня
    public void waveDifficulties() {
        /*Волна 1 - 30 секунд */
        if (seconds == newWaveDifficulties[0]) {
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 1000);
        }

        /*Волна 2 - 60 секунд*/
        if (seconds == newWaveDifficulties[1]) {
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 900);
        }

        /*Волна 3 - 90 секунд */
        if (seconds == newWaveDifficulties[2]) {
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 800);
        }

        /*Волна 4 - 120 секунд*/
        if (seconds == newWaveDifficulties[3]) {
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 700);
        }
    }

    // TODO: Метод для перезагруки астероидов в режиме паузы
    public void pauseGenerateAsteroid(){
        /*Волна 0 - < 30 секунд*/
        if (seconds < newWaveDifficulties[0]){
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 1100);
        }

        /*Волна 1 - >= 30 секунд && < 60 секунд */
        if (seconds >= newWaveDifficulties[0] && seconds < newWaveDifficulties[1]){
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 1000);
        }

        /*Волна 2 - >= 60 секунд && < 90 секунд*/
        if (seconds >= newWaveDifficulties[1] && seconds < newWaveDifficulties[2]){
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 900);
        }

        /*Волна 3 - >= 90 секунд && < 120 секунд*/
        if (seconds >= newWaveDifficulties[2] && seconds < newWaveDifficulties[3]){
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 800);
        }

        /*Волна 4 - >= 120 секунд*/
        if (seconds >= newWaveDifficulties[3]){
            if (_timerGenerateAsteroid != null)
                _timerGenerateAsteroid.cancel();
            _timerGenerateAsteroid = new Timer();
            _tGenAsteroid = new TaskGenerateAsteroid();
            _timerGenerateAsteroid.schedule(_tGenAsteroid, 100, 700);
        }
    }

    // TODO: Методы для гироскопа и музыки на заднем фоне
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroscopeEventListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_FASTEST);

        /*MusicService*/
        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    // TODO: Методы для гироскопа и музыки на заднем фоне
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroscopeEventListener);


        /*MusicService*/
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
    protected void onDestroy() {
        super.onDestroy();

        /*Отключаем MusicService*/
        doUnbindService();
        Intent music = new Intent();
        music.setClass(this,MusicService.class);
        stopService(music);
    }
}
