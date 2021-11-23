package ar.edu.ips.aus.seminario2.sampleproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private GameAnimationThread thread;
    private Context context;
    private PlayerSprite playerSprites;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public void reset(){
        thread.reset();
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GameView(Context context){
        super(context);
        init();
    }

    private void init() {
        Game.getInstance(this.getContext());
        getHolder().addCallback(this);

        Game.getInstance().initPlayers();

        playerSprites = new PlayerSprite(getResources());

        thread = new GameAnimationThread(getHolder(), this);
        setFocusable(true);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry){
            try {
                thread.setRunning(false);
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }


    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        if (!thread.getFinish() && !thread.getLose()){
            if (canvas != null){
                MazeBoard board = Game.getInstance().getMazeBoard();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                int count = 0;
                for (Player p: Game.getInstance().getPlayers()) {
                    Rect srcRect = playerSprites.getSourceRectangle(this, board, p, count);
                    Rect dstRect = playerSprites.getDestinationRectangle(this, board, p);
                    Log.d("MAZE: ", String.format("src rect: %s - dst rect: %s", srcRect.toShortString(), dstRect.toShortString()));
                    canvas.drawBitmap(playerSprites.getSprites(), srcRect, dstRect, null);
                    count++;
                }
            }
        } else {
            thread.setRunning(false);
//            Context context = Game.getInstance().getContext();
            Intent intent;

            if (thread.getLose()) {
                intent = new Intent(this.context, LoseActivity.class);
            } else {
                intent = new Intent(this.context, FinishActivity.class);
            }
            this.context.startActivity(intent);
        }

    }
}
