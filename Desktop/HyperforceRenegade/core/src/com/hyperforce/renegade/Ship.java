package com.hyperforce.renegade;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Timer;

public class Ship extends Actor implements Entity{
    private TextureRegion spr;
    private int flameAnim;
    private float[] oldPos;
    private boolean moving, vulnerable;
    private int hp, stoHp, invin;
    private int shield;
    private int rot;

    public static int score;
    public static int lives;
    public static int stars;
    public static boolean shopping;
    public static int speed;
    public static Sound[] sounds;
    public static int volume;
    public static boolean[][] upgrades;
    public static int vampirism;
    public static int bossHealth, bossHealthMax;
    public static boolean alive;
    public static boolean bossDead;
    public static boolean scoreDouble;
    private static int stoScore;

    public Ship(float x, float y) {
        super();
        spr = new TextureRegion(new Texture("ShipSprite.png"));
        flameAnim = 0;
        oldPos = new float[2];
        oldPos[0] = x;
        oldPos[1] = y;
        setBounds(x, y, 48, 48);
        setOrigin(24, 24);

        hp = 3;
        stoHp = hp;
        invin = 0;
        shield = 0;
        lives = 2;
        speed = 12;
        volume = 33;
        vampirism = 0;
        bossHealth = 0;
        upgrades = new boolean[4][5];
        //upgrades[3][0] = true;
        //upgrades[3][1] = true;
        //upgrades[3][2] = true;
        //upgrades[3][3] = true;
        //upgrades[3][4] = true;
        sounds = new Sound[13];
        vulnerable = false;
        alive = true;
        rot = 0;

        sounds[0] = Gdx.audio.newSound(Gdx.files.internal("SFX/Damaged.wav"));
        sounds[1] = Gdx.audio.newSound(Gdx.files.internal("SFX/Laser_Shoot.wav"));
        sounds[2] = Gdx.audio.newSound(Gdx.files.internal("SFX/Laser_Shoot2.wav"));
        sounds[3] = Gdx.audio.newSound(Gdx.files.internal("SFX/Bomb.wav"));
        sounds[4] = Gdx.audio.newSound(Gdx.files.internal("SFX/DeathExplosion.wav"));
        sounds[5] = Gdx.audio.newSound(Gdx.files.internal("SFX/Laser_Shoot3.wav"));
        sounds[6] = Gdx.audio.newSound(Gdx.files.internal("SFX/Supernova.wav"));
        sounds[7] = Gdx.audio.newSound(Gdx.files.internal("SFX/Pickup_Star.wav"));
        sounds[8] = Gdx.audio.newSound(Gdx.files.internal("SFX/Pickup_Powerup.wav"));
        sounds[9] = Gdx.audio.newSound(Gdx.files.internal("SFX/Buy.wav"));
        sounds[10] = Gdx.audio.newSound(Gdx.files.internal("SFX/DeathExplosion.wav"));
        sounds[11] = Gdx.audio.newSound(Gdx.files.internal("SFX/Enemy_Laser.wav"));
        sounds[12] = Gdx.audio.newSound(Gdx.files.internal("SFX/ShieldBlock.wav"));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(invin % 2 == 0) {
            if(alive) {
                spr.setRegion(0, 0, 48, 48);
                batch.draw(spr, getX(), getY());
                spr.setRegion(0, 48 + 9 * (flameAnim / 3), 48, 9);
                batch.draw(spr, getX(), getY() - 9);
            } else {
                spr.setRegion(72, 48 * ((rot / 2) % 4), 48, 48);
                batch.draw(spr, getX(), getY());
            }

            if(upgrades[1][3] && shield == 0) {
                spr.setRegion(0, 75, 72, 72);
                batch.draw(spr, getX() - 12, getY() - 12);
            }
        }
        if(stoHp != hp)
            if(invin == 0) {
                long id = sounds[0].play();
                sounds[0].setVolume(id, volume);
                invin = 45;
            } else {
                hp = stoHp;
            }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        moving = !(oldPos[0] == getX() && oldPos[1] == getY());
        // Causes player to be blown back when not moving. Uncomment if you are a horrible person and want Stage 2 to be unbearable.
        /*if(!moving && !shopping && vulnerable)
            moveBy(0, -3);*/
        if(!shopping && vulnerable) {
            if(getY() < 72)
                setPosition(getX(), 72);
            if(getY() > 768 - 48)
                setPosition(getX(), 768 - 48);
            if(getX() < 0 && !Ship.upgrades[1][1])
                setPosition(0, getY());
            else if(getX() > 768 - getWidth() && !Ship.upgrades[1][1])
                setPosition(768 - getWidth(), getY());

            if(getX() < -24 && Ship.upgrades[1][1])
                moveBy(768, 0);
            else if(getX() > 768 - getWidth() / 2 && Ship.upgrades[1][1])
                moveBy(-768, 0);
        }
        if(Ship.upgrades[1][0])
            speed = 16;
        flameAnim = (flameAnim + 1) % 9;
        if(invin > 0)
            invin--;
        oldPos[0] = getX();
        oldPos[1] = getY();
        if(!vulnerable)
            hp = 3;
        stoHp = hp;
        if(!shopping)
            checkCollision();
        if(score != stoScore && scoreDouble)
            score += score - stoScore;
        stoScore = score;
        if(hp <= 0) {
            long id = sounds[4].play();
            sounds[4].setVolume(id, Ship.volume / 50f);
            lives--;
            if(lives > 0) {
                hp = 3;
                stoHp = 3;
            }
            alive = false;
            vulnerable = false;
            invin = (int)(0.75f / Gdx.graphics.getDeltaTime());
            rot = 0;
            addAction(Actions.parallel(Actions.moveBy(0, -384, 0.75f), Actions.repeat((int)(0.75f /
                    Gdx.graphics.getDeltaTime()), Actions.run(() -> rot++))));
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if(lives <= -1)
                        Gdx.app.exit();
                    else {
                        invin = 25;
                        stoHp = 3;
                        alive = true;
                        vulnerable = true;
                        setPosition(360, 144);
                    }
                }
            }, 0.75f);
        }
        if(shield > 0)
            shield--;
    }

    public void checkCollision() {
        for(Actor a : getParent().getChildren()) {
            if(a instanceof Enemy && !a.equals(this)) {
                Rectangle aRect = new Rectangle(a.getX(), a.getY(), a.getWidth(), a.getHeight());
                Rectangle thisRect = new Rectangle(getX(), getY(), getWidth(), getHeight());
                if(Intersector.overlaps(aRect, thisRect)) {
                    ((Enemy)a).onHit(this);
                }
            }
        }
    }

    @Override
    public boolean remove() {
        for(int i = 0; i < sounds.length; i++)
            sounds[i].dispose();
        spr.getTexture().dispose();
        return super.remove();
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getHp() {
        return hp;
    }

    public void setInvin(int invin) {
        this.invin = invin;
    }

    public int getInvin() { return invin; }

    @Override
    public void onHit(Entity offender) {
        if(offender instanceof Projectile && ((Projectile)offender).getDmg() > 0 && invin == 0) {
            if(shield == 0 && upgrades[1][3])
                invin = 25;
            else
                hp--;
            shield = 240;
        }
        if(invin > 0 && hp != stoHp)
            hp = stoHp;
    }

    public boolean isVulnerable() {
        return vulnerable;
    }

    public void setVulnerable(boolean vulnerable) {
        this.vulnerable = vulnerable;
    }
}
