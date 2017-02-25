package com.hyperforce.renegade;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.hyperforce.renegade.ProjectileAi.*;

import java.util.Random;

import static com.hyperforce.renegade.Projectile.group;

public class Enemy extends Actor implements Entity {
    protected int hp;
    protected Sprite spr;
    protected int invinDur;
    protected int age;
    protected boolean vulnerable;

    public final static Texture region = new Texture(Gdx.files.internal("Enemies.png"));
    public static Ship player;
    protected static final Random GENERATOR = new Random();

    public Enemy(int x, int y) {
        super();
        setBounds(x, y, 48, 48);
        hp = 3;
        invinDur = 0;
        age = 0;
        vulnerable = true;
        spr = new Sprite(region);
        spr.setBounds(x, y, 48, 48);
        spr.setRegion(0, 0, 16, 16);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(invinDur % 2 == 0)
            spr.draw(batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        spr.setPosition(getX(), getY());
        if(invinDur > 0)
            invinDur--;
        age++;
    }

    public void setInvinDur(int invinDur) {
        this.invinDur = invinDur;
    }

    @Override
    public void onHit(Entity offender) {
        if(offender instanceof Ship && player.getInvin() <= 0)
            ((Ship)offender).setHp(((Ship)offender).getHp() - 1);
        else if(offender instanceof Projectile && ((Projectile)offender).getDmg() > 0 && invinDur == 0) {
            Projectile proj = (Projectile)offender;
            hp -= proj.getDmg();
            invinDur = 15;
            long id = Ship.sounds[0].play();
            Ship.sounds[0].setVolume(id, Ship.volume / 150f);
            if(hp <= 0) {
                if(Ship.upgrades[3][4]) {
                    if(BasicLaser.stacks < 3)
                        BasicLaser.stacks++;
                    BasicLaser.stackDur = 180;
                }
                if(Ship.upgrades[1][2]) {
                    Ship.vampirism++;
                    if(Ship.vampirism == 5) {
                        Ship.vampirism = 0;
                        if(player.getHp() < 3)
                            player.setHp(player.getHp() + 1);
                    }
                }
                if(Ship.upgrades[1][4]) {
                    group.addActor(new Explosion(getX() + getWidth() / 2, getY() + getHeight() / 2));
                }
                Ship.score += 500;
                getParent().addActor(new Star(getX(), getY()));
                Enemy.this.remove();
            }
        }
    }

    protected void largeStarOnHit(Entity offender) {
        if(offender instanceof Ship && player.getInvin() <= 0)
            ((Ship)offender).setHp(((Ship)offender).getHp() - 1);
        else if(offender instanceof Projectile && ((Projectile)offender).getDmg() > 0 && invinDur == 0) {
            Projectile proj = (Projectile)offender;
            hp -= proj.getDmg();
            invinDur = 15;
            long id = Ship.sounds[0].play();
            Ship.sounds[0].setVolume(id, Ship.volume / 150f);
            if(hp <= 0) {
                if(Ship.upgrades[3][4]) {
                    if(BasicLaser.stacks < 3)
                        BasicLaser.stacks++;
                    BasicLaser.stackDur = 180;
                }
                if(Ship.upgrades[1][2]) {
                    Ship.vampirism++;
                    if(Ship.vampirism == 5) {
                        Ship.vampirism = 0;
                        if(player.getHp() < 3)
                            player.setHp(player.getHp() + 1);
                    }
                }
                if(Ship.upgrades[1][4]) {
                    group.addActor(new Explosion(getX() + getWidth() / 2, getY() + getHeight() / 2));
                }
                Ship.score += 500;
                getParent().addActor(new LargeStar(getX(), getY()));
                Enemy.this.remove();
            }
        }
    }

    public boolean getVulnerable() {
        return vulnerable;
    }
}
