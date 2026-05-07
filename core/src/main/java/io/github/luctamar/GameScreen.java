package io.github.luctamar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;

public class GameScreen implements Screen {

    private final Main game;

    // ══════════════════════════════════════════
    // CONSTANTS
    // ══════════════════════════════════════════

    private static final int W = 640;
    private static final int H = 480;
    private static final float GRAVITY = -600f;
    private static final float JUMP_VELOCITY = 350f;
    private static final float MOVE_SPEED = 160f;
    private static final int START_LIVES = 3;
    private static final float SPAWN_X = 50f;
    private static final float SPAWN_Y = 80f;

    // ══════════════════════════════════════════
    // RENDERING
    // ══════════════════════════════════════════

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private BitmapFont hudFont;
    private Texture playerSheet, enemySheet, coinSheet;
    private Texture pixel;

    // ══════════════════════════════════════════
    // PLAYER ANIMATION
    // ══════════════════════════════════════════

    enum PlayerState { IDLE, RUNNING, JUMPING }

    private Animation<TextureRegion> idleAnim, runAnim, jumpAnim;
    private float stateTime = 0f;
    private PlayerState playerState = PlayerState.IDLE;
    private PlayerState prevState = PlayerState.IDLE;
    private boolean facingRight = true;

    // ══════════════════════════════════════════
    // PLAYER PHYSICS
    // ══════════════════════════════════════════

    private float playerX, playerY;
    private float velocityY = 0f;
    private boolean onGround = false;
    private Rectangle playerBounds;

    // ══════════════════════════════════════════
    // GAME STATE
    // ══════════════════════════════════════════

    private int score = 0;
    private int lives = START_LIVES;
    private boolean switchToGameOver = false;
    private boolean playerWon = false;

    // ══════════════════════════════════════════
    // ENEMIES, COINS, PLATFORMS
    // ══════════════════════════════════════════

    private Animation<TextureRegion> slimeAnim, coinAnim;
    private ArrayList<float[]> enemies;
    private ArrayList<Rectangle> coins;
    private ArrayList<Rectangle> platforms;


    public GameScreen(Main game) {
        this.game = game;
    }

    // ══════════════════════════════════════════
    // SHOW
    // ══════════════════════════════════════════

    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);

        pixel = new Texture("white.png");

        hudFont = new BitmapFont();
        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.5f);

        // Player animations
        playerSheet = new Texture("player.png");
        TextureRegion[][] pGrid = TextureRegion.split(playerSheet, 64, 64);
        idleAnim = new Animation<>(0.2f, pGrid[0]);
        runAnim  = new Animation<>(0.1f, pGrid[1]);
        jumpAnim = new Animation<>(0.15f, pGrid[2]);
        idleAnim.setPlayMode(Animation.PlayMode.LOOP);
        runAnim.setPlayMode(Animation.PlayMode.LOOP);
        jumpAnim.setPlayMode(Animation.PlayMode.NORMAL);

        // Enemy animations
        enemySheet = new Texture("enemy-slime.png");
        TextureRegion[][] eGrid = TextureRegion.split(enemySheet, 64, 64);
        slimeAnim = new Animation<>(0.15f, eGrid[0]);
        slimeAnim.setPlayMode(Animation.PlayMode.LOOP);

        // Coin animations
        coinSheet = new Texture("coin.png");
        TextureRegion[][] cGrid = TextureRegion.split(coinSheet, 32, 32);
        coinAnim = new Animation<>(0.08f, cGrid[0]);
        coinAnim.setPlayMode(Animation.PlayMode.LOOP);

        // Player bounds (tighter than sprite for fair collisions)
        playerBounds = new Rectangle(0, 0, 28, 48);

        buildLevel();

        playerX = SPAWN_X;
        playerY = SPAWN_Y;
    }

    // ══════════════════════════════════════════
    // LEVEL DESIGN
    // ══════════════════════════════════════════

    private void buildLevel() {
        platforms = new ArrayList<>();
        platforms.add(new Rectangle(0, 30, W, 20));           // ground
        platforms.add(new Rectangle(100, 130, 120, 16));      // lower
        platforms.add(new Rectangle(300, 130, 120, 16));
        platforms.add(new Rectangle(500, 130, 120, 16));
        platforms.add(new Rectangle(50, 230, 140, 16));       // mid
        platforms.add(new Rectangle(250, 260, 160, 16));
        platforms.add(new Rectangle(470, 230, 130, 16));
        platforms.add(new Rectangle(150, 360, 130, 16));      // high
        platforms.add(new Rectangle(380, 390, 140, 16));

        enemies = new ArrayList<>();
        enemies.add(new float[]{200, 50, 70, 100, 350});     // ground patrol
        enemies.add(new float[]{310, 146, 50, 300, 400});    // platform patrols
        enemies.add(new float[]{260, 276, -45, 250, 390});

        coins = new ArrayList<>();
        coins.add(new Rectangle(60, 70, 32, 32));            // ground level
        coins.add(new Rectangle(440, 70, 32, 32));
        coins.add(new Rectangle(140, 160, 32, 32));          // lower platforms
        coins.add(new Rectangle(540, 160, 32, 32));
        coins.add(new Rectangle(80, 260, 32, 32));           // mid platforms
        coins.add(new Rectangle(310, 290, 32, 32));
        coins.add(new Rectangle(510, 260, 32, 32));
        coins.add(new Rectangle(190, 390, 32, 32));          // high platforms
        coins.add(new Rectangle(430, 420, 32, 32));
    }

    // ══════════════════════════════════════════
    // INPUT
    // ══════════════════════════════════════════

    private void handleInput(float delta) {
        prevState = playerState;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= MOVE_SPEED * delta;
            facingRight = false;
            if (onGround) playerState = PlayerState.RUNNING;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += MOVE_SPEED * delta;
            facingRight = true;
            if (onGround) playerState = PlayerState.RUNNING;
        } else if (onGround) {
            playerState = PlayerState.IDLE;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && onGround) {
            velocityY = JUMP_VELOCITY;
            onGround = false;
        }

        if (!onGround) {
            playerState = PlayerState.JUMPING;
        }

        if (playerState != prevState) {
            stateTime = 0f;
        }

        if (playerX < 0) playerX = 0;
        if (playerX > W - 64) playerX = W - 64;
    }

    // ══════════════════════════════════════════
    // PHYSICS
    // ══════════════════════════════════════════

    private void updatePhysics(float delta) {
        velocityY += GRAVITY * delta;
        playerY += velocityY * delta;

        onGround = false;

        for (Rectangle plat : platforms) {
            if (velocityY <= 0) {
                float playerBottom = playerY;
                float platTop = plat.y + plat.height;
                boolean horizontalOverlap =
                    (playerX + 64 > plat.x) && (playerX < plat.x + plat.width);

                if (horizontalOverlap
                    && playerBottom <= platTop
                    && playerBottom >= platTop - 15) {
                    playerY = platTop;
                    velocityY = 0;
                    onGround = true;
                }
            }
        }

        if (playerY < -100) {
            loseLife();
        }
    }

    // ══════════════════════════════════════════
    // ENEMIES
    // ══════════════════════════════════════════

    private void updateEnemies(float delta) {
        for (float[] enemy : enemies) {
            enemy[0] += enemy[2] * delta;
            if (enemy[0] <= enemy[3]) {
                enemy[0] = enemy[3];
                enemy[2] = -enemy[2];
            }
            if (enemy[0] >= enemy[4]) {
                enemy[0] = enemy[4];
                enemy[2] = -enemy[2];
            }
        }
    }

    // ══════════════════════════════════════════
    // COLLISIONS
    // ══════════════════════════════════════════

    private void checkCollisions() {
        playerBounds.setPosition(playerX + 18, playerY + 6);

        for (float[] enemy : enemies) {
            Rectangle enemyRect = new Rectangle(enemy[0] + 12, enemy[1] + 10, 40, 36);
            if (playerBounds.overlaps(enemyRect)) {
                loseLife();
                return;
            }
        }

        Iterator<Rectangle> it = coins.iterator();
        while (it.hasNext()) {
            Rectangle coin = it.next();
            if (playerBounds.overlaps(coin)) {
                it.remove();
                score += 100;
            }
        }

        if (coins.isEmpty()) {
            switchToGameOver = true;
            playerWon = true;
        }
    }

    // ══════════════════════════════════════════
    // LIVES
    // ══════════════════════════════════════════

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            switchToGameOver = true;
            playerWon = false;
        } else {
            playerX = SPAWN_X;
            playerY = SPAWN_Y;
            velocityY = 0;
            onGround = false;
        }
    }

    // ══════════════════════════════════════════
    // RENDER
    // ══════════════════════════════════════════

    @Override
    public void render(float delta) {
        // ── Deferred screen transition (must happen BEFORE any drawing) ──
        if (switchToGameOver) {
            game.setScreen(new GameOverScreen(game, score, playerWon));
            dispose();
            return;
        }

        stateTime += delta;

        // ── Update ──
        handleInput(delta);
        updatePhysics(delta);
        updateEnemies(delta);
        checkCollisions();

        // ── Pick player animation ──
        Animation<TextureRegion> currentAnim;
        switch (playerState) {
            case RUNNING: currentAnim = runAnim;  break;
            case JUMPING: currentAnim = jumpAnim; break;
            default:      currentAnim = idleAnim;
        }
        boolean looping = playerState != PlayerState.JUMPING;
        TextureRegion frame = currentAnim.getKeyFrame(stateTime, looping);

        // Flip
        if (!facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        }

        // ── Clear ──
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // ── Platforms ──
        batch.setColor(0.3f, 0.3f, 0.5f, 1f);
        for (Rectangle plat : platforms) {
            batch.draw(pixel, plat.x, plat.y, plat.width, plat.height);
        }
        batch.setColor(Color.WHITE);

        // ── Coins ──
        TextureRegion coinFrame = coinAnim.getKeyFrame(stateTime, true);
        for (Rectangle coin : coins) {
            batch.draw(coinFrame, coin.x, coin.y);
        }

        // ── Enemies ──
        TextureRegion slimeFrame = slimeAnim.getKeyFrame(stateTime, true);
        for (float[] enemy : enemies) {
            batch.draw(slimeFrame, enemy[0], enemy[1]);
        }

        // ── Player ──
        batch.draw(frame, playerX, playerY);

        // ── HUD ──
        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "Score: " + score, 10, H - 10);
        hudFont.draw(batch, "Lives: " + lives, 10, H - 35);
        hudFont.draw(batch, "Coins: " + coins.size() + " left", W - 180, H - 10);

        batch.end();
    }

    @Override
    public void resize(int w, int h) { camera.setToOrtho(false, W, H); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        playerSheet.dispose();
        enemySheet.dispose();
        coinSheet.dispose();
        hudFont.dispose();
        pixel.dispose();
    }
}
