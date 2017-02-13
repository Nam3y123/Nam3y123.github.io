package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Ship;

public class Laser extends Enemy {
    private int width, duration;
    private boolean vertical;

    public Laser(int x, int y) {
        super(x, y);
        setBounds(x, y, 0, 0);
        vulnerable = false;
        width = 10;
        duration = 52;
        vertical = GENERATOR.nextBoolean();
        //vertical = true;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(duration <= 7) {
            spr.setRegion(134, 0, 12, 12);
            spr.setRotation(0);
            spr.setSize(768, 6 * (duration - 1));
            spr.setRotation(vertical ? 90 : 0);
            spr.setOriginCenter();
            spr.setX(vertical ? getX() - 354: 0);
            spr.setY(vertical ? 384 : getY() - spr.getHeight() / 2);
            spr.draw(batch);
        } else {
            spr.setRotation(vertical ? 90 : 0);
            spr.setRegion(104, 0, 30, 10);
            spr.setSize(90, 30);
            spr.setOriginCenter();
            if(vertical) {
                spr.setX(getX() - 36 - spr.getHeight() / 2 - width);
                spr.setY(getY() - spr.getWidth() / 2);
            } else {
                spr.setX(getX() - spr.getWidth() / 2);
                spr.setY(getY() + 36 - spr.getHeight() / 2 + width);
            }

            spr.draw(batch);

            spr.rotate(180);
            if(vertical)
                spr.translate(72 + width * 2, 0);
            else
                spr.translate(0, -72 - width * 2);
            spr.draw(batch);
        }

        if(width > 0)
            width--;
        duration--;
        if(duration == 0)
            this.remove();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(duration == 7) {
            long id = Ship.sounds[11].play();
            Ship.sounds[11].setVolume(id, Ship.volume / 150f);
        }
        if(duration <= 7) {
            int height = 6 * (duration - 1);
            if(vertical) {
                if(player.getX() + player.getWidth() > getX() - height / 2 + 30 && player.getX() < getX() + height / 2 + 30)
                    player.setHp(player.getHp() - 1);
            } else {
                if(player.getY() + player.getHeight() > getY() - height / 2 && player.getY() < getY() + height / 2)
                    player.setHp(player.getHp() - 1);
            }

        }
    }

    @Override
    public void onHit(Entity offender) {

    }
}
