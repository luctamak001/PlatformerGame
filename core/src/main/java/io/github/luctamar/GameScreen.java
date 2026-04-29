package io.github.armstrong;

// ============================================================
// 🎮 GAME SCREEN — Day 2 Starter
// ============================================================
//
// Day 1 (Apr 27): Get the player moving, jumping, and landing
// Day 2 (Apr 29): Add sprite sheet animations and flipping
//
// If you didn't finish Day 1 TODOs, do those FIRST.
// Then move on to the Day 2 TODOs.
//
// ============================================================

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameScreen implements Screen {

    private final Main game;

    // ── Constants ──
    private static final float GRAVITY = -500f;
    private static final float JUMP_VELOCITY = 300f;
    private static final float MOVE_SPEED = 150f;
    private static final float GROUND_Y = 50f;

    // ── Rendering ──
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture playerSheet;

    private Animation<TextureRegion> idleAnim, runAnim. jumbAnim;
    private float stateTime = 0f;
    private boolean facingRight = true


    // ── Player ──
    private float playerX = 100f;
    private float playerY = GROUND_Y;
    private float velocityY = 0f;
    private boolean onGround = true;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 480);

        playerSheet = new Texture("player.png");

        TextureRegion[][] grid = TextureRegion.split(playerSheet, 64, 64);

           idleAnim = new Animation<>(0.2f, grid[0]);
           runAnim  = new Animation<>(0.1f, grid[1]);
           jumpAnim = new Animation<>(0.15f, grid[2]);

        idleAnim.setPlayMode(Animation.PlayMode.LOOP);
        runAnim.setPlayMode(Animation.PlayMode.LOOP);
        jumpAnim.setPlayMode(Animation.PlayMode.NORMAL);


    }

    @Override
    public void render(float delta) {

        // ── INPUT ──

        // DAY 1 TODO: LEFT arrow → subtract MOVE_SPEED * delta from playerX
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= MOVE_SPEED * delta;
            facingRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            playerX += MOVE_SPEED * delta;
            facingRight = true;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && onGround){
            velocityY = JUMP_VELOCITY;
            onGround = false;
        }


        // ── PHYSICS ──

        velocityY += GRAVITY * delta;
        playerY += velocityY * delta;

        if (playerY <= GROUND_Y) {
            playerY = GROUND_Y;
            velocityY = 0f;
            onGround = true;
        }


        // ── ANIMATION ──

        stateTime += delta;

        Animation<TextureRegion> currentAnim;
        if (!onGround){
            currentAnim = jumpAnim;
        } else if (LEFT || RIGHT){
            currentAnim = runAnim;
        }else {
            currentAnim = idleAnim;
        }

        boolean looping = onGround;
        TextureRegion frame = currentAnim.getKeyFrame(stateTime, looping);

        if (!facingRight && !frame.isFlipX()) frame.flip(true, false);
        else if (facingRight && frame.isFlipX()) frame.flip(true, false);



        // ── DRAW ──
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // DAY 2 TODO 8: Replace this line with: batch.draw(frame, playerX, playerY);
        batch.draw(playerSheet, playerX, playerY, 64, 64);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        playerSheet.dispose();
    }
}
