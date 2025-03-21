package com.mygdx.puigbros;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.puigbros.jsonloaders.CollectableJson;
import com.mygdx.puigbros.jsonloaders.EnemyJson;
import com.mygdx.puigbros.jsonloaders.LevelJson;

import java.util.ArrayList;

public class GameScreen implements Screen {

    PuigBros game;
    ButtonLayout joypad, pauseMenu;

    Stage stage;
    TileMap tileMap;

    Player player;
    ArrayList<Actor> enemies;
    ArrayList<Actor> collectables;
    boolean paused;


    public GameScreen(PuigBros game)
    {
        this.game = game;

        // Pause menu
        pauseMenu = new ButtonLayout(game.camera, game.manager, game.mediumFont);
        pauseMenu.loadFromJson("pausemenu.json");

        // Create joypad
        joypad = new ButtonLayout(game.camera, game.manager, null);
        joypad.loadFromJson("joypad.json");

        // Create tile map
        tileMap = new TileMap(game.manager, game.batch);

        // Init game entities
        stage = new Stage();
        player = new Player(game.manager);
        enemies = new ArrayList<>();
        collectables = new ArrayList<>();
        player.setMap(tileMap);
        player.setJoypad(joypad);
        stage.addActor(player);

        Viewport viewport = new Viewport() {
        };
        viewport.setCamera(game.camera);
        stage.setViewport(viewport);

        // Load level from json file
        Json json = new Json();

        FileHandle file = Gdx.files.internal("Level.json");
        String scores = file.readString();
        LevelJson l = json.fromJson(LevelJson.class, scores);
        tileMap.loadFromLevel(l);

        // Init enemies from json level file
        for(int i = 0; i < l.getEnemies().size(); i++)
        {
            EnemyJson e = l.getEnemies().get(i);
            if(e.getType().equals("Dino"))
            {
                Dino d = new Dino(e.getX() * tileMap.TILE_SIZE, e.getY() * tileMap.TILE_SIZE, game.manager, player);
                d.setMap(tileMap);
                enemies.add(d);
                stage.addActor(d);
            }
        }

        // Init collectibles from json level file
        for(int i = 0; i < l.getCollectables().size(); i++)
        {
            CollectableJson c = l.getCollectables().get(i);
            if(c.getType().equals("PowerUp"))
            {
                PowerUp p = new PowerUp(c.getX() * tileMap.TILE_SIZE, c.getY() * tileMap.TILE_SIZE, game.manager);
                p.setMap(tileMap);
                collectables.add(p);
                stage.addActor(p);
            }
        }

        paused = false;

        game.manager.get("sound/music.mp3", Music.class).play();
        game.manager.get("sound/music.mp3", Music.class).setLooping(true);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        // Render step =============================================
        game.camera.update();
        game.batch.setProjectionMatrix(game.camera.combined);
        game.shapeRenderer.setProjectionMatrix(game.camera.combined);
        ScreenUtils.clear(Color.SKY);

        // Draw tile map and background
        tileMap.render();

        // Bounding box draw =======================================
        /*player.drawDebug(game.shapeRenderer);
        for (int i = 0; i < enemies.size(); i++)
            enemies.get(i).drawDebug(game.shapeRenderer);*/
        // =========================================================

        // Draw stage: player, enemies and collectibles
        stage.draw();

        if(paused)
        {
            // Draw pause menu
            pauseMenu.render(game.batch, game.textBatch);
        }
        else
        {
            // Draw GUI
            joypad.render(game.batch, game.textBatch);
            game.textBatch.begin();
            game.mediumFont.draw(game.textBatch, "Vides: " + game.lives, 40,460);
            // Debug touch pointers
            /*for(Integer i : joypad.pointers.keySet())
            {
                String action = joypad.pointers.get(i);
                game.mediumFont.draw(game.textBatch, "Pointer "+i+": " + action+" ("+(int)joypad.buttons.get(action).debug_x+","+(int)joypad.buttons.get(action).debug_y+")", 40, 400 - i*40);
            }*/
            game.textBatch.end();
        }


        // Update step =============================================
        if(paused)
        {
            // Game paused
            if(pauseMenu.consumeRelease("Resume"))
            {
                joypad.setAsActiveInputProcessor();
                paused = false;
            }
            if(pauseMenu.consumeRelease("Quit"))
            {
                this.dispose();
                game.setScreen(new MainMenuScreen(game));
            }
        }
        else
        {
            // Game running
            updateGameLogic(delta);

            // Pause game
            if(joypad.consumePush("Pause"))
            {
                paused = true;
                pauseMenu.setAsActiveInputProcessor();
            }
        }
    }

    void updateGameLogic(float delta)
    {
        stage.act(delta);

        // Scroll update
        tileMap.scrollX = (int) (player.getX() - 400);
        if (tileMap.scrollX < 0)
            tileMap.scrollX = 0;
        if (tileMap.scrollX >= tileMap.width * tileMap.TILE_SIZE - 800)
            tileMap.scrollX = tileMap.width * tileMap.TILE_SIZE - 800 - 1;

        // Collision player - enemies
        Rectangle rect_player = new Rectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight());

        for (int i = 0; i < enemies.size(); i++)
        {
            Actor enemy = enemies.get(i);
            Rectangle rect_enemy = new Rectangle(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight());
            WalkingCharacter wc = (WalkingCharacter) enemy;

            if (!player.isDead() && !wc.isDead()) {
                if (rect_enemy.overlaps(rect_player)) {
                    if(player.hasInvulnerability()) {
                      // Kill enemies if invulnerable
                      wc.kill();
                    } else if (player.getY() + player.getHeight()*0.5f < wc.getY() - wc.getHeight()*0.25f &&
                        player.isFalling() && player.getSpeed().y > 0.f) {
                        // Kill enemies by jumping over them
                        player.jump(0.5f);
                        wc.kill();
                    } else {
                        // Lose a life
                        game.manager.get("sound/music.mp3", Music.class).stop();
                        game.manager.get("sound/loselife.wav", Sound.class).play();
                        player.kill();
                    }
                }
            }
        }

        // Pick up collectables
        for (int i = 0; i < collectables.size(); i++)
        {
            Actor collectable = collectables.get(i);
            Rectangle rect_coll = new Rectangle(collectable.getX(), collectable.getY(), collectable.getWidth(), collectable.getHeight());

            if (rect_coll.overlaps(rect_player))
            {
                // Give invulnerability
                player.getInvulnerability();
                collectable.remove();
                collectables.remove(collectable);
                game.manager.get("sound/powerup.wav", Sound.class).play();
            }
        }

        // Lose life
        if (player.isDead() && player.getAnimationFrame() >= 25.f) {
            game.lives--;
            if (game.lives <= 0) {
                this.dispose();
                game.setScreen(new MainMenuScreen(game));
            } else {
                this.dispose();
                game.setScreen(new GameScreen(game));
            }
        }

        // Complete level
        if(player.getX() >= tileMap.width * tileMap.TILE_SIZE)
        {
            this.dispose();
            game.setScreen(new LevelCompleteScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        game.manager.get("sound/music.mp3", Music.class).stop();
    }
}
