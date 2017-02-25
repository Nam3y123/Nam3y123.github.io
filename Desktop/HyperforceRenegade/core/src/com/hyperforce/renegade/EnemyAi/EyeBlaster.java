package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Projectile;

public class EyeBlaster extends Enemy {
    public EyeBlaster(int y) {
        super(0, y);
        spr.setRegion(16, 0, 16, 16);
        spr.setOrigin(0, 0);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        spr.setRegion(16, 0, 16, 16);
        spr.setSize(48, 48);
        spr.setRotation(0);
        spr.setPosition(getX(), getY());
        super.draw(batch, parentAlpha);
        int finalDir = findFinalDir();
        if(finalDir < 0)
            finalDir += 360;
        if(finalDir % 90 == 0) {
            spr.setRegion(32, 0, 8, 8);
            spr.setSize(24, 24);
            spr.setRotation(finalDir);
        }
        else {
            spr.setRegion(32, 8, 9, 9);
            spr.setSize(27, 27);
            spr.setRotation(finalDir + 45);
        }
        switch(finalDir) {
            case 0:
                spr.setPosition(getX() + 30, getY() + 12);
                break;
            case 45:
                spr.setPosition(getX() + 48, getY() + 21);
                break;
            case 90:
                spr.setPosition(getX() + 36, getY() + 30);
                break;
            case 135:
                spr.setPosition(getX() + 27, getY() + 48);
                break;
            case 180:
                spr.setPosition(getX() + 18, getY() + 36);
                break;
            case 225:
                spr.setPosition(getX(), getY() + 27);
                break;
            case 270:
                spr.setPosition(getX() + 12, getY() + 18);
                break;
            case 315:
                spr.setPosition(getX() + 21, getY());
                break;
        }
        spr.draw(batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        moveBy(6, 0);
        int finalDir = findFinalDir();
        if(age == 64) {
            float cannonX = getX() + 12 + (float)(Math.cos(Math.toRadians(finalDir)) * 24);
            float cannonY = getY() + 12 + (float)(Math.sin(Math.toRadians(finalDir)) * 24);
            getParent().addActor(new Projectile(cannonX, cannonY, 24, 24, finalDir, 16, 0, 96, 1));
            getParent().addActor(new Projectile(cannonX, cannonY, 24, 24, finalDir - 30, 16, 0, 96, 1));
            getParent().addActor(new Projectile(cannonX, cannonY, 24, 24, finalDir + 30, 16, 0, 96, 1));
        }
        if(age == 128)
            remove();
    }

    public int findFinalDir() {
        float dir = (float)Math.atan((getY() - player.getY()) / (getX() - player.getX()));
        if(getX() >= player.getX())
            dir += Math.PI;
        int finalDir = (int)Math.floor(((Math.toDegrees(dir) + 22.5) % 360)  / 45d) * 45;
        if(finalDir < 0)
            finalDir += 360;
        if(finalDir < 90)
            finalDir = 0;
        else if(finalDir < 180)
            finalDir = 180;
        return finalDir;
    }
}
