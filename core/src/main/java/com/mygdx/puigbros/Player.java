package com.mygdx.puigbros;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Player extends WalkingCharacter {

    static final float JUMP_IMPULSE = -400f;
    static final float RUN_SPEED = 240f;
    static final float RUN_ACCELERATION = 200f;

    Joypad joypad;

    Texture idleTextures[];
    Texture runTextures[];
    Texture jumpTextures[];
    Texture deadTextures[];
    Texture currentFrame;

    float animationFrame = 0;
    boolean lookLeft = false;

    public Player()
    {
        setBounds(400,40,48, 112);
        loadTextures();
    }

    public void loadTextures()
    {
        idleTextures = new Texture[10];

        for (int i = 0; i < 10; i++)
        {
            idleTextures[i] = new Texture("player/Idle (" +(i+1)+").png");
        }

        runTextures = new Texture[8];

        for (int i = 0; i < 8; i++)
        {
            runTextures[i] = new Texture("player/Run (" +(i+1)+").png");
        }

        jumpTextures = new Texture[12];

        for (int i = 0; i < 12; i++)
        {
            jumpTextures[i] = new Texture("player/Jump (" +(i+1)+").png");
        }

        deadTextures = new Texture[10];

        for (int i = 0; i < 10; i++)
        {
            deadTextures[i] = new Texture("player/Dead (" +(i+1)+").png");
        }

        currentFrame = idleTextures[0];
    }

    public void setJoypad(Joypad joypad) {
        this.joypad = joypad;
    }
    @Override
    public void act(float delta) {
        super.act(delta);

        if(getX() < getWidth() / 2)
        {
            setX(getWidth() / 2);
        }

        if(dead)
        {
            animationFrame += 10.f*delta;
            if(animationFrame >= 10.f)
            {
                animationFrame = 9.f;
            }
            currentFrame = deadTextures[(int)animationFrame];
        }
        else
        {
            if(falling)
            {
                if(speed.y < 0)
                {
                    float base_impulse = -JUMP_IMPULSE;
                    float current_impulse = -speed.y;
                    animationFrame = 0 + ((base_impulse - current_impulse) / 32);
                    if (animationFrame > 8) animationFrame = 8;
                }
                else
                {
                    animationFrame = 9 + (speed.y / 64);
                    if (animationFrame > 11) animationFrame = 11;
                }
                currentFrame = jumpTextures[(int)animationFrame];

            }
            else if((speed.x < 0.1f && speed.x > -0.1f))
            {
                // Idle
                animationFrame += 10 * delta;
                if (animationFrame >= 10.f) animationFrame -= 10.f;
                currentFrame = idleTextures[(int)animationFrame];
            }
            else
            {
                // Walk
                animationFrame += 10 * delta;
                if (animationFrame >= 8.f) animationFrame -= 8.f;
                currentFrame = runTextures[(int)animationFrame];
            }

            if(!falling && joypad.consumePush("Jump"))
            {
                jump();
            }

            if(!falling) {
                if (joypad.isPressed("Right")) {
                    lookLeft = false;
                    speed.x += RUN_ACCELERATION * delta;
                    if (speed.x > RUN_SPEED) {
                        speed.x = RUN_SPEED;
                    }
                } else if (joypad.isPressed("Left")) {
                    lookLeft = true;
                    speed.x -= RUN_ACCELERATION * delta;
                    if (speed.x < -RUN_SPEED) {
                        speed.x = -RUN_SPEED;
                    }
                } else {
                    speed.x *= 1 - (0.99f * delta);
                    if (speed.x < 5f && speed.x >= -5f) {
                        speed.x = 0;
                    }
                }
            }
        }
    }

    public void jump()
    {
        speed.y = JUMP_IMPULSE;
    }

    @Override
    public void kill()
    {
        if(!dead) {
            super.kill();
            animationFrame = 0;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);


        batch.draw(currentFrame, getX() - getWidth()*0.5f - map.scrollX - (lookLeft ? 28 : 50), getY() - getHeight()*0.5f, 128, 128, 0, 0, 669, 569, lookLeft, true);
    }

    public void drawDebug(ShapeRenderer shapes) {
        //super.drawDebug(shapes);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.NAVY);
        shapes.rect(getX() - getWidth()*0.5f - map.scrollX, getY() - getHeight()*0.5f, getWidth(), getHeight());
        shapes.end();
    }
}
