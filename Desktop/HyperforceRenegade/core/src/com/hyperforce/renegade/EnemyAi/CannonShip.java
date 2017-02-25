package com.hyperforce.renegade.EnemyAi;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.hyperforce.renegade.Enemy;
import com.hyperforce.renegade.Projectile;

public class CannonShip extends Enemy {
    private int velocity;
    private int travelDist;
    private final int maxVelocity = 12;
    private final float moveTime = 0.001388889f; // When entering / leaving, gives a fixed speed of 12 pixels per frame

    public CannonShip(int x, int y) {
        super(x, 768);
        hp = 2;
        velocity = 12;
        travelDist = 1000; // Placeholderino
        addAction(Actions.moveBy(0, y - 768, moveTime * (768 - y)));
    }

    @Override
    public void act(float delta) {
        if(player.getX() > getX() && velocity < maxVelocity)
            velocity++;
        else if(player.getX() <= getX() && velocity > -maxVelocity)
            velocity--;
        //moveBy(velocity, -0.5f);
        moveBy(velocity, 0);
        if((age % 90 == 89 || age % 90 == 84 || age % 90 == 79) && getParent() != null)
            getParent().addActor(new Projectile(getX(), getY() - 24, 36, 36, 270, 24, 0, 24, 2));
        if(age == 270) {
            addAction(Actions.moveBy(0, 768 - getY(), moveTime * (768 - getY())));
            travelDist = (int)((768 - getY()) / 12f);
        }
        if(age >= 270 + travelDist)
            remove();
        super.act(delta);
    }
}
