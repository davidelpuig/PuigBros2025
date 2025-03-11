package com.mygdx.puigbros;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.mygdx.puigbros.jsonloaders.ButtonJson;
import com.mygdx.puigbros.jsonloaders.ButtonLayoutJson;

import java.util.HashMap;
import java.util.Map;

public class ButtonLayout implements InputProcessor {

    class Button {

        Rectangle rect;
        String action;
        String imageOn, imageOff;
        boolean pressed;
        int pushes;


        Button(int x, int y, int sx, int sy, String action, String on, String off)
        {
            rect = new Rectangle(x, y, sx, sy);
            this.action = action;
            this.imageOn = on;
            this.imageOff = off;
            pressed = false;
            pushes = 0;
        }
    }

    Map<String,Button> buttons;
    Map<Integer,Button> pointers;
    final OrthographicCamera camera;
    AssetManager manager;

    public ButtonLayout(OrthographicCamera camera, AssetManager manager)
    {
        this.camera = camera;
        this.manager = manager;
        buttons = new HashMap<>();
        pointers = new HashMap<>();

        Gdx.input.setInputProcessor(this);

    }

    public void loadFromJson(String fileName)
    {
        Json json = new Json();
        FileHandle file = Gdx.files.internal(fileName);
        String fileText = file.readString();
        ButtonLayoutJson l = json.fromJson(ButtonLayoutJson.class, fileText);

        for(ButtonJson b : l.buttons)
        {
            addButton(b.x, b.y, b.width, b.height, b.action, b.image_on, b.image_off);
        }

    }

    public void addButton(int x, int y, int sx, int sy, String action, String imageOn, String imageOff)
    {
        Button b = new Button(x, y, sx, sy, action, imageOn, imageOff);
        buttons.put(action, b);
    }

    boolean isPressed(String action)
    {
        if(buttons.get(action) != null)
            return buttons.get(action).pressed;
        else
            return false;
    }

    boolean consumePush(String action)
    {
        if(buttons.get(action) != null)
        {
            if(buttons.get(action).pushes > 0)
            {
                buttons.get(action).pushes = 0;
                return true;
            }
        }
        return false;

    }

    public void render(ShapeRenderer shapeRenderer)
    {
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for(String i:buttons.keySet())
        {
            Button b = buttons.get(i);
            shapeRenderer.setColor(b.pressed ? Color.YELLOW : Color.BLACK);
            shapeRenderer.ellipse(b.rect.x, b.rect.y, b.rect.width, b.rect.height, 2);
            shapeRenderer.rect(b.rect.x, b.rect.y, b.rect.width, b.rect.height);
        }
        shapeRenderer.end();
    }

    public void render(SpriteBatch batch)
    {
        batch.begin();

        for(String i:buttons.keySet())
        {
            Button b = buttons.get(i);
            Texture t = manager.get(b.pressed ? b.imageOn : b.imageOff, Texture.class);
            batch.draw(t, b.rect.x, b.rect.y, b.rect.width, b.rect.height, 0, 0, t.getWidth(), t.getHeight(), false, true);
        }
        batch.end();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown (int x, int y, int pointer, int button) {

        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        for(String i:buttons.keySet())
        //for(int i = 0; i < buttons.size(); i++)
        {
            if(buttons.get(i).rect.contains(touchPos.x,touchPos.y))
            {
                pointers.put(pointer,buttons.get(i));
                buttons.get(i).pressed = true;
                buttons.get(i).pushes ++;
            }
        }

        return true; // return true to indicate the event was handled
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        // your touch up code here
        if(pointers.get(pointer) != null)
        {
            pointers.get(pointer).pressed = false;
            pointers.remove(pointer);
        }
        return true; // return true to indicate the event was handled
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        Vector3 touchPos = new Vector3();
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        /*if(pointers.get(pointer) != null)
        {
            if(!pointers.get(pointer).rect.contains(touchPos.x, touchPos.y))
            {
                pointers.get(pointer).pressed = false;
            }
        }*/

        for(String i:buttons.keySet())
        //for(int i = 0; i < buttons.size(); i++)
        {
            if(buttons.get(i).rect.contains(touchPos.x,touchPos.y))
            {
                if(pointers.get(pointer) != null)
                {
                    pointers.get(pointer).pressed = false;
                }
                pointers.put(pointer,buttons.get(i));
                buttons.get(i).pressed = true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
