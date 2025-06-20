package hk.ust.cse.comp107x.alpha;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.content.Context;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Random;


public class ClassicActivity extends Activity {
    //for high score
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String dataName = "heha";
    String intData = "huha";
    int defaultInt = 0;
    int hiScore;

    double ballSpeed = 0;
    //declaring canvas
    Canvas canvas;
    SquashCourtView squashCourtView;
    private SoundPool soundPool;
    int sample1 = -1;
    int sample2 = -1;
    MediaPlayer backgroundMusic;

    Display display;
    Point size;
    int screenWidth;
    int screenHeight;
    int racketWidth;
    int racketHeight;
    Point racketPosition;

    Point ballPosition;
    int ballWidth;
    boolean left;
    boolean right;
    boolean up;
    boolean down;
    boolean r_left;
    boolean r_right;

    long lastFrameTime;
    int fps;
    int score;
    int lives;

    // New fields for second character sprite animation
    Bitmap character1Sprite1;
    Bitmap character1Sprite2;
    Bitmap character2Sprite1;
    Bitmap character2Sprite2;
    boolean character1SpriteToggle = true;
    boolean character2SpriteToggle = true;
    Point character1Position;
    Point character2Position;
    int characterWidth;
    int characterHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        squashCourtView = new SquashCourtView(this);
        setContentView(squashCourtView);
        prefs = getSharedPreferences(dataName,MODE_PRIVATE);
        editor = prefs.edit();
        hiScore = prefs.getInt("myInt", defaultInt);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        sample1 = soundPool.load(this, R.raw.squash1, 1);
        sample2 = soundPool.load(this, R.raw.squash2, 1);

        // Initialize and start background music
        backgroundMusic = MediaPlayer.create(this, R.raw.background_music);
        backgroundMusic.setLooping(true);
        backgroundMusic.start();

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        //racket position
        racketHeight = 10;
        racketPosition = new Point();
        racketPosition.x = screenWidth / 2;
        racketPosition.y = screenHeight - 20;
        racketWidth = screenWidth / 9;
        //ball characterstics
        ballWidth = screenWidth / 35;
        ballPosition = new Point();
        ballPosition.x = screenWidth / 2;
        ballPosition.y = 1 + ballWidth;
        lives = 3;

        // Load character sprites
        character1Sprite1 = BitmapFactory.decodeResource(getResources(), R.drawable.character1);
        character1Sprite2 = BitmapFactory.decodeResource(getResources(), R.drawable.character1);
        character2Sprite1 = BitmapFactory.decodeResource(getResources(), R.drawable.character2);
        character2Sprite2 = BitmapFactory.decodeResource(getResources(), R.drawable.character2);

        // Initialize character positions and sizes
        characterWidth = screenWidth / 15;
        characterHeight = screenHeight / 10;
        character1Position = new Point(screenWidth / 4, screenHeight / 2);
        character2Position = new Point(3 * screenWidth / 4, screenHeight / 2);
    }

    class SquashCourtView extends SurfaceView implements Runnable {
        Thread ourThread = null;
        SurfaceHolder ourHolder;
        volatile boolean playing;
        Paint paint;

        public SquashCourtView(Context context) {
            super(context);
            ourHolder = getHolder();
            paint = new Paint();
            down = true;

            Random random = new Random();
            int ballDirection = random.nextInt(3);
            switch (ballDirection) {
                case 0:
                    left = true;
                    right = false;
                    break;
                case 1:
                    right = true;
                    left = false;
                    break;
                case 2:
                    right = false;
                    left = false;
                    break;
            }
        }

        @Override
        public void run() {
            while (playing) {
                updateCourt();
                drawCourt();
                controlFPS ();
            }
        }

        public void updateCourt() {
            if (r_right) {
                if (racketPosition.x + racketWidth/2 <= screenWidth ) {
                    racketPosition.x += screenWidth/28;
                }
            }
            if (r_left) {
                if (racketPosition.x - racketWidth/2 >= 0) {
                    racketPosition.x -= screenWidth/28;
                }
            }
            if (ballPosition.x + ballWidth > screenWidth) {
                left = true;
                right = false;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }
            if (ballPosition.x < 0) {
                left = false;
                right = true;
                soundPool.play(sample1, 1, 1, 0, 0, 1);
            }
            if (ballPosition.y > screenHeight - ballWidth) {
                lives = lives - 1;
                if (lives == 0) {
                    if(score>hiScore){
                        hiScore = score;
                        editor.putInt("myInt",hiScore);
                        editor.commit();
                    }
                    lives = 3;
                    score = 0;
                    ballSpeed = 0;
                    soundPool.play(sample2, 1, 1, 0, 0, 1);
                }
                ballPosition.y = 1 + ballWidth;
                Random randnum = new Random();
                int startX = randnum.nextInt(screenWidth) + 1;
                ballPosition.x = startX;

                int direction = randnum.nextInt(3) + 1;
                switch (direction) {
                    case 1:
                        right = true;
                        left = true;
                        break;
                    case 2:
                        right = true;
                        left = false;
                        break;
                    case 3:
                        right = false;
                        left = false;
                        break;
                }
            }
            //we hit top of the screen
            if(ballPosition.y <= 0)
            {
                down = true;
                up = false;
                ballPosition.y = 1;
                soundPool.play(sample1,1,1,0,0,1);
            }
            if(up){
                ballPosition.y -= ballSpeed + 10;
            }
            if(down){
                ballPosition.y +=8 + ballSpeed;
            }
            if(left){
                ballPosition.x -= 12 + ballSpeed;
            }
            if(right){
                ballPosition.x += 12 + ballSpeed;
            }
            //to check weather ball has hit racquet
            if(ballPosition.y + ballWidth >= racketPosition.y - racketHeight/2  ){
                int halfRacket = racketWidth/2;
                if((ballPosition.x + ballWidth > racketPosition.x - halfRacket)&&(ballPosition.x - ballWidth < racketPosition.x + halfRacket)) {
                    soundPool.play(sample2, 1, 1, 0, 0, 1);
                    score++;
                    up = true;
                    down = false;
                    //rebound the ball horizontally
                    if (ballPosition.x > racketPosition.x) {
                        right = true;
                        left = false;

                    } else {
                        right = false;
                        left = true;
                    }
                    if (ballSpeed != 10) {
                        ballSpeed = ballSpeed + 0.05;
                    }
                }
            }

            // Toggle sprite animation for characters
            character1SpriteToggle = !character1SpriteToggle;
            character2SpriteToggle = !character2SpriteToggle;
        }
        public void drawCourt(){
            if(ourHolder.getSurface().isValid()){
                canvas = ourHolder.lockCanvas();
                // Draw tile map background
                canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.back), 0, 0, paint);

                // Draw characters with sprite swapping animation
                Bitmap character1Sprite = character1SpriteToggle ? character1Sprite1 : character1Sprite2;
                Bitmap character2Sprite = character2SpriteToggle ? character2Sprite1 : character2Sprite2;
                canvas.drawBitmap(Bitmap.createScaledBitmap(character1Sprite, characterWidth, characterHeight, false),
                        character1Position.x, character1Position.y, paint);
                canvas.drawBitmap(Bitmap.createScaledBitmap(character2Sprite, characterWidth, characterHeight, false),
                        character2Position.x, character2Position.y, paint);

                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(30);
                canvas.drawText("Score:"+score+" Lives:"+lives,20,40,paint);
                canvas.drawRect(racketPosition.x - (racketWidth / 2),
                        racketPosition.y - (racketHeight / 2), racketPosition.x + (racketWidth / 2),
                        racketPosition.y + racketHeight, paint);///drawing bat
                canvas.drawCircle(ballPosition.x, ballPosition.y, ballWidth, paint);//drawing ball

                ourHolder.unlockCanvasAndPost(canvas);

            }
        }

        public void controlFPS(){
            long timeThisFrame = (System.currentTimeMillis()-lastFrameTime);
            long timeToSleep = 15-timeThisFrame;
            if(timeThisFrame > 0){
                fps = (int)(1000/timeThisFrame);
            }
            if(timeToSleep>0){
                try{
                    ourThread.sleep(timeToSleep);
                }catch(InterruptedException e){
                }
            }
            lastFrameTime = System.currentTimeMillis();
        }
        public void pause(){
            playing = false;
            try{
                ourThread.join();
            }catch(InterruptedException e){
            }
        }
        public void resume(){
            playing = true;
            ourThread = new Thread(this);
            ourThread.start();
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if ((racketPosition.x <= screenWidth - racketWidth / 2) || (racketPosition.x >= racketWidth / 2)) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (motionEvent.getX() >= screenWidth / 2) {
                        r_left = false;
                        r_right = true;
                    }
                    else{
                        r_left = true;
                        r_right = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    r_right = false;
                    r_left = false;
                    break;
            }
        }
        return true;
    }
    @Override
    protected void onStop(){
        super.onStop();
        while(true){
            squashCourtView.pause();
            break;
        }
        finish();
    }
    @Override
    protected void onPause(){
        super.onPause();
        squashCourtView.pause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        squashCourtView.resume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }
    public boolean onKeyDown(int Keycode,KeyEvent event){
        if(Keycode == KeyEvent.KEYCODE_BACK){
            squashCourtView.pause();
            if (backgroundMusic != null) {
                backgroundMusic.stop();
                backgroundMusic.release();
                backgroundMusic = null;
            }
            finish();
            return true;
        }
        return false;
    }
}






