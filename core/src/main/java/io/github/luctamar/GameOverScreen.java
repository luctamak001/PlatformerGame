package io.github.luctamar;

// ============================================================
// 🎮 GAME OVER SCREEN — DAY 5
// ============================================================
//
// Create this screen on Day 5 (May 7).
// It shows "YOU WIN!" or "GAME OVER" based on whether the
// player collected all coins or lost all lives.
//
// Pressing R starts a new game. Pressing M returns to menu.
//
// ============================================================

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameOverScreen implements Screen {

    private final Main game;
    private final int finalScore;
    private final boolean won;

    private static final int W = 640;
    private static final int H = 480;

    // DAY 5 TODO G1: Declare these fields:
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private BitmapFont titleFont;
    private BitmapFont bodyFont;
    private GlyphLayout layout;
    private float stateTime = 0f;

    public GameOverScreen(Main game, int finalScore, boolean won) {
        this.game = game;
        this.finalScore = finalScore;
        this.won = won;
    }

    @Override
    public void show() {
        // DAY 5 TODO G2: Initialize rendering:
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);

        // DAY 5 TODO G3: Create title font (big):
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);

        // DAY 5 TODO G4: Create body font (medium, white):
        bodyFont = new BitmapFont();
        bodyFont.setColor(Color.WHITE);
        bodyFont.getData().setScale(1.5f);

        // DAY 5 TODO G5: Create GlyphLayout:
        layout = new GlyphLayout();

    }

    @Override
    public void render(float delta) {
        // DAY 5 TODO G6: Add delta to stateTime
        stateTime += delta;


        // DAY 5 TODO G7: Check for R key (restart) and M key (menu):
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            game.setScreen(new GameScreen(game));
            dispose();
            return;
        }
         if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
            dispose();
            return;
        }


        // DAY 5 TODO G8: Clear screen:
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        // DAY 5 TODO G9: Draw the game over screen:
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Title — yellow for win, red for lose
        String title = won ? "YOU WIN!" : "GAME OVER";
        titleFont.setColor(won ? Color.YELLOW : Color.RED);
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, (W - layout.width) / 2f, H / 2f + 80);

        // Final score
        String scoreText = "Final Score: " + finalScore;
        layout.setText(bodyFont, scoreText);
        bodyFont.draw(batch, scoreText, (W - layout.width) / 2f, H / 2f);

        // Blinking restart prompt
        if ((int)(stateTime * 2) % 2 == 0) {
            String restart = "Press R to Play Again";
            layout.setText(bodyFont, restart);
            bodyFont.draw(batch, restart, (W - layout.width) / 2f, H / 2f - 60);
        }

        // Menu option
        bodyFont.getData().setScale(1.2f);
        String menu = "Press M for Menu";
        layout.setText(bodyFont, menu);
        bodyFont.draw(batch, menu, (W - layout.width) / 2f, H / 2f - 110);
        bodyFont.getData().setScale(1.5f);

        batch.end();

    }

    @Override
    public void resize(int w, int h) { camera.setToOrtho(false, W, H); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // DAY 5 TODO G10: Dispose batch, titleFont, bodyFont
        batch.dispose();
        titleFont.dispose();
        bodyFont.dispose();

    }
}
