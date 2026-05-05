package io.github.luctamar;

// ============================================================
// 🎮 GAME SCREEN — Full Project Starter
// ============================================================
//
// Day 1 (Apr 27): Movement, jumping, gravity
// Day 2 (Apr 29): Sprite sheet animations and flipping
// Day 3 (May 1):  Enemies, coins, and collision
// Day 4 (May 5):  Platforms and level design
// Day 5 (May 7):  Lives, win/lose, screen transitions
// Day 6 (May 11): HUD and hitbox polish
// Day 7 (May 13): Final polish and submit
//
// Work through the TODOs in order by day.
// If you didn't finish a previous day, do those FIRST.
//
// ============================================================

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

    // ── Constants ──
    private static final int W = 640;
    private static final int H = 480;
    private static final float GRAVITY = -600f;
    private static final float JUMP_VELOCITY = 350f;
    private static final float MOVE_SPEED = 160f;
    private static final float GROUND_Y = 50f;

    // DAY 5 TODO 1: Add these constants:
       private static final int START_LIVES = 3;
       private static final float SPAWN_X = 50f;
       private static final float SPAWN_Y = 80f;


    // ── Rendering ──
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Texture playerSheet;

    private Texture pixel;

    private BitmapFont hudFont;

    // ── Textures & animations ──

    private Animation<TextureRegion> idleAnim, runAnim, jumpAnim;
    private float stateTime = 0f;
    private boolean facingRight = true;

    Texture enemySheet, coinSheet;
    Animation<TextureRegion> slimeAnim, coinAnim;
    ArrayList<Rectangle> platforms;


    int lives = START_LIVES;
    boolean switchToGameOver = false;
    boolean playerWon = false;

    // ── Player ──
    private float playerX = 100f;
    private float playerY = GROUND_Y;
    private float velocityY = 0f;
    private boolean onGround = true;

    // ── Enemies ──
    private ArrayList<float[]> enemies;

    // ── Coins ──
    private ArrayList<Rectangle> coins;
    private int score = 0;

    // ── Player bounding box ──
    private Rectangle playerBounds;


    public GameScreen(Main game) {
        this.game = game;
    }


    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 640, 480);

        playerSheet = new Texture("player.png");
        pixel = new Texture("white.png");

        hudFont = new BitmapFont();
        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.5f);

        TextureRegion[][] grid = TextureRegion.split(playerSheet, 64, 64);

        idleAnim = new Animation<>(0.2f, grid[0]);
        runAnim  = new Animation<>(0.1f, grid[1]);
        jumpAnim = new Animation<>(0.15f, grid[2]);

        idleAnim.setPlayMode(Animation.PlayMode.LOOP);
        runAnim.setPlayMode(Animation.PlayMode.LOOP);
        jumpAnim.setPlayMode(Animation.PlayMode.NORMAL);

        // ── Load enemy and coin sheets (done for you) ──
        Texture enemySheet = new Texture("enemy-slime.png");
        TextureRegion[][] eGrid = TextureRegion.split(enemySheet, 64, 64);
        Animation<TextureRegion> slimeAnim = new Animation<>(0.15f, eGrid[0]);
        slimeAnim.setPlayMode(Animation.PlayMode.LOOP);

        Texture coinSheet = new Texture("coin.png");
        TextureRegion[][] cGrid = TextureRegion.split(coinSheet, 32, 32);
        Animation<TextureRegion> coinAnim = new Animation<>(0.08f, cGrid[0]);
        coinAnim.setPlayMode(Animation.PlayMode.LOOP);

        // ── Player bounding box (done for you) ──
        //   DAY 6 TODO 3: Later, change this to tighter bounds:
        playerBounds = new Rectangle(0, 0, 28, 48);

        // DAY 4 TODO 4: Create platforms list and add platforms:
        platforms = new ArrayList<>();
        platforms.add(new Rectangle(0, 30, W, 20));                 // ground
        platforms.add(new Rectangle(100, 130, 120, 16));      // lower
        platforms.add(new Rectangle(300, 130, 120, 16));
        platforms.add(new Rectangle(500, 130, 120, 16));
        platforms.add(new Rectangle(50, 230, 140, 16));       // mid
        platforms.add(new Rectangle(250, 260, 160, 16));
        platforms.add(new Rectangle(470, 230, 130, 16));
        platforms.add(new Rectangle(150, 360, 130, 16));      // high
        platforms.add(new Rectangle(380, 390, 140, 16));

        enemies = new ArrayList<>();
        enemies.add(new float[]{250, GROUND_Y, 80, 200, 350});
        enemies.add(new float[]{450, GROUND_Y, 60, 400, 550});

        //   DAY 4 TODO 5: Later, update enemies to patrol on platforms:
        enemies = new ArrayList<>();
        enemies.add(new float[]{200, 50, 70, 100, 350});      // ground
        enemies.add(new float[]{310, 146, 50, 300, 400});     // lower platform
        enemies.add(new float[]{260, 276, -45, 250, 390});    // mid platform

        for (int i = 0; i < 5; i++) {
            coins.add(new Rectangle(150 + i * 70, 200, 32, 32));
        }

        //   DAY 4 TODO 6: Later, update coins to sit on platforms:
        coins = new ArrayList<>();
        coins.add(new Rectangle(60, 70, 32, 32));         // ground
        coins.add(new Rectangle(440, 70, 32, 32));
        coins.add(new Rectangle(140, 160, 32, 32));       // lower
        coins.add(new Rectangle(540, 160, 32, 32));
        coins.add(new Rectangle(80, 260, 32, 32));        // mid
        coins.add(new Rectangle(310, 290, 32, 32));
        coins.add(new Rectangle(510, 260, 32, 32));
        coins.add(new Rectangle(190, 390, 32, 32));       // high
        coins.add(new Rectangle(430, 420, 32, 32));

        // DAY 5 TODO 3: Set spawn position:
        playerX = SPAWN_X;
        playerY = SPAWN_Y;

    }

    // ══════════════════════════════════════════
    // UPDATE METHODS
    // ══════════════════════════════════════════

    // DAY 3 TODO 8: Create updateEnemies method:

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

    // DAY 3 TODO 9: Create checkCollisions method:

    private void checkCollisions() {

        // DAY 6 TODO 4: Later, use tighter offset:
        playerBounds.setPosition(playerX + 18, playerY + 6);

        for (float[] enemy : enemies) {
            // DAY 6 TODO 5: Later, tighten enemy hitbox:
            Rectangle enemyRect = new Rectangle(enemy[0] + 12, enemy[1] + 10, 40, 36);

            if (playerBounds.overlaps(enemyRect)) {
                playerX = 100;
                playerY = GROUND_Y;
                velocityY = 0;
                System.out.println("Hit! Resetting player.");

                // DAY 5 TODO 4: Later, replace the above reset with:
                loseLife();
                return;
            }
        }

        Iterator<Rectangle> it = coins.iterator();
        while (it.hasNext()) {
            Rectangle coin = it.next();
            if (playerBounds.overlaps(coin)) {
                it.remove();
                score++;
                System.out.println("Coin! Score: " + score);
            }
        }

        // DAY 5 TODO 5: Add win condition:
        if (coins.isEmpty()) {
            switchToGameOver = true;
            playerWon = true;
        }
    }

    @Override
    public void render(float delta) {

        // DAY 5 TODO 7: Deferred screen transition — add at VERY TOP of render:
        if (switchToGameOver) {
            game.setScreen(new GameOverScreen(game, score, playerWon));
            dispose();
            return;
        }

        // ── INPUT ──

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= MOVE_SPEED * delta;
            facingRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
            playerX += MOVE_SPEED * delta;
            facingRight = true;


        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && onGround){
            velocityY = JUMP_VELOCITY;
            onGround = false;
        }

        // DAY 5 TODO 8: Keep player in screen bounds:
        if (playerX < 0) playerX = 0;
        if (playerX > W - 64) playerX = W - 64;

        // ── PHYSICS ──

        velocityY += GRAVITY * delta;
        playerY += velocityY * delta;

        // DAY 4 TODO 7: Later, REPLACE the simple GROUND_Y check above with platform collision:
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

        // DAY 5 TODO 9: Add fall-off-screen detection after physics:
        if (playerY < -100) {
            loseLife();
        }

        // ── UPDATES ──

        // DAY 3 TODO 10: Call update methods:
        updateEnemies(delta);
        checkCollisions();

        // ── ANIMATION ──

        // DAY 2 TODO 4: Add delta to stateTime
        stateTime += delta;

        // DAY 2 TODO 5: Pick the right animation:
        //   Animation<TextureRegion> currentAnim;
        //   if (!onGround)                                        → currentAnim = jumpAnim
        //   else if (LEFT or RIGHT pressed)                       → currentAnim = runAnim
        //   else
        Animation<TextureRegion> currentAnim;
        if (!onGround){
            currentAnim = jumpAnim;
        } else if (LEFT || RIGHT) {
            currentAnim = runAnim;
        }else currentAnim = idleAnim;

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

        // TODO 3: Loop through every float[] in the enemies list.
        //   For each enemy:
        //     1. Update x position: enemy[0] += enemy[2] * delta
        //     2. If enemy[0] <= enemy[3] (hit left boundary):
        //          - Set enemy[0] = enemy[3]  (snap to boundary)
        //          - Negate enemy[2]           (reverse direction: speed = -speed)
        //     3. If enemy[0] >= enemy[4] (hit right boundary):
        //          - Set enemy[0] = enemy[4]
        //          - Negate enemy[2]
        //
        //   Use a for-each loop: for (float[] enemy : enemies) { ... }

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
