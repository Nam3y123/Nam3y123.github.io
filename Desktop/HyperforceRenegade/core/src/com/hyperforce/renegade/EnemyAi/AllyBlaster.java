package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Entity;
import com.hyperforce.renegade.Projectile;
import com.hyperforce.renegade.Ship;

import java.util.ArrayList;
import java.util.Random;

public class AllyBlaster extends Enemy {
    private int age;

    public AllyBlaster(int x, int y) {
        super(x, y);
        addAction(Actions.sequence(Actions.moveBy(0, 384, 0.25f), Actions.moveTo(getX(), -48, 4f),
                Actions.run(AllyBlaster.this::remove)));
        age = -15;
        spr.setRegion(78, 0, 16, 16);
        spr.setSize(48, 48);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if(getParent() != null && age >= 0 && age % 20 == 0) {
            ArrayList<Enemy> enemies = new ArrayList<>();
            for(int i = 0; i < getParent().getChildren().size; i++) {
                Actor a = getParent().getChildren().get(i);
                if(a instanceof Enemy && !a.equals(this))
                    enemies.add((Enemy)a);
            }

            if(enemies.size() > 0) {
                Enemy target = enemies.get(GENERATOR.nextInt(enemies.size()));
                float dir = (float)Math.atan((getX() - target.getX()) / (-getY() + target.getY())) +
                        (float)(Math.PI / 2f);
                if(-getY() + target.getY() < 0)
                    dir += Math.PI;
                if(-getY() + target.getY() == 0) {
                    if(getX() - target.getX() < 0)
                        dir = 0;
                    else
                        dir = (float)Math.PI;
                }
                getParent().addActor(new Projectile(getX() + 12, getY() + 12, 24, 24, (float)Math.toDegrees(dir), 48,
                        253, 0, 2) {
                    @Override
                    public void checkCollision() {
                        SnapshotArray<Actor> actors = new SnapshotArray<Actor>(group.getChildren());
                        if(age > 2)
                            for(Actor a : actors) {
                                if(a != null && a instanceof Entity && !(a instanceof Ship) && !a.equals(this)) {
                                    Rectangle aRect = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
                                    Rectangle thisRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                                    if(Intersector.overlaps(aRect, thisRect)) {
                                        ((Entity)a).onHit(this);
                                        if(!Ship.upgrades[3][1])
                                            onHit((Entity)a);
                                    }
                                }
                            }
                    }
                });
            }
        }
        age++;
    }

    @Override
    public void onHit(Entity offender) {

    }
}
