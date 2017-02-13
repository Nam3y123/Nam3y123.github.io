package com.hyperforce.renegade;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.SnapshotArray;
import com.hyperforce.renegade.EnemyAi.*;
import com.hyperforce.renegade.ProjectileAi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

public class MainClass extends ApplicationAdapter implements InputProcessor, ControllerListener {
	private Stage curStage; // The stage currently being drawn
	private Stage mainGame, shop;
	private Ship player;
	private boolean[] btnDown;
	private int frames;
	private int cooldown;
	private int[] momentum;
	private int[] spawnTimes;
	private Group HUDGroup, mainGroup;
	private short bomb;
	private int[] planetPos;
	private int shopSel, curMenu;
	public static Music[] soundtrack;
	private int superCd;
	private int supernova;
	private int level;
	public static int songPlaying;

	private final Random generator = new Random();

	@Override
	public void create () {
		mainGame = new Stage() {
			@Override
			public void dispose() {
				for(int i = 0; i < getActors().size; i++)
					getActors().get(i).remove();
				super.dispose();
			}
		};
		shop = new Stage() {
			@Override
			public void dispose() {
				for(int i = 0; i < getActors().size; i++)
					getActors().get(i).remove();
				super.dispose();
			}
		};
		btnDown = new boolean[6]; // 0 = Up, 1 = Down, 2 = Left, 3 = Right, 4 = Z, 5 = X
		momentum = new int[2];
		spawnTimes = new int[5];
		frames = 0;
		Ship.score = 0;
		Ship.lives = 0;
		Ship.stars = 0;
		HUDGroup = new Group();
		mainGroup = new Group();
		bomb = -1;
		planetPos = new int[2];
		planetPos[1] = 768;
		shopSel = 0;
		curMenu = 0;
		superCd = 0;
		supernova = 0;
		level = 1;
		Ship.shopping = false;

		soundtrack = new Music[3];
		soundtrack[0] = Gdx.audio.newMusic(Gdx.files.internal("SFX/Music/StageTheme1.mp3"));
		soundtrack[1] = Gdx.audio.newMusic(Gdx.files.internal("SFX/Music/GreenPowerup.mp3"));
		soundtrack[2] = Gdx.audio.newMusic(Gdx.files.internal("SFX/Music/BossTheme1.mp3"));
		soundtrack[1].setOnCompletionListener(music -> {
            soundtrack[0].play();
			soundtrack[0].setLooping(true);
        });
		for(int i = 0; i < soundtrack.length; i++)
			soundtrack[i].setVolume(1f);

		Actor background = new Actor() {
			private final TextureRegion letters = new TextureRegion(new Texture(Gdx.files.internal("Backdrop.png")));
			private final TextureRegion stars = new TextureRegion(new Texture(Gdx.files.internal("Stars.png")));
			private final Sprite bg = new Sprite(new Texture(Gdx.files.internal("Planets.png")));
			private final Texture tex = new Texture(new Pixmap(768, 768, Pixmap.Format.RGB888));
			private final Texture glitchTex = new Texture(new Pixmap(384, 768, Pixmap.Format.RGB888));
			private final Color[] glitchColors = {Color.WHITE, Color.RED, Color.CYAN, Color.GREEN};
			private byte[] glitchX = new byte[35];
			private byte[] glitchY = new byte[35];
			private byte[] glitchNum = new byte[35];
			private byte[] glitchCol = new byte[35];

			@Override
			public void draw(Batch batch, float parentAlpha) {
				for(int i = 0; i < 15; i++) {
					stars.setRegion(0, 15 * generator.nextInt(2), 15, 15);
					batch.draw(stars, generator.nextInt(768 - 15),
							generator.nextInt(696 - 15) + 72);
				}

				bg.setRegion(0, 0, 128, 128);
				bg.setSize(384, 384);
				bg.setPosition(planetPos[0], planetPos[1]);
				bg.draw(batch);
				if(!Ship.shopping) {
					planetPos[1] -= 9;
					if(planetPos[1] < -384) {
						planetPos[0] = generator.nextInt(1024) - 128;
						planetPos[1] = 768;
						bg.setColor(generator.nextFloat(), generator.nextFloat(), generator.nextFloat(), 1f);
					}
				}

				batch.draw(tex, 0, -768);
				if(PowerupSphere.greenPowerDur > 0) {
					if((360 - PowerupSphere.greenPowerDur) % 24 == 0) {
						for(int i = 0; i < 35; i++) {
							glitchX[i] = (byte)generator.nextInt(32);
							glitchY[i] = (byte)generator.nextInt(32);
							glitchNum[i] = (byte)generator.nextInt(16);
							glitchCol[i] = (byte)generator.nextInt(4);
						}

						if(PowerupSphere.greenPowerDur == 360) {
							for(int i = 0; i < soundtrack.length; i++)
								soundtrack[i].stop();
							soundtrack[1].play();
						}
					}

					batch.draw(glitchTex, 384, 0);
					for(int i = 0; i < 35; i++) {
						letters.setRegion(24 * glitchNum[i], 0, 24, 24);
						batch.setColor(glitchColors[glitchCol[i]]);
						batch.draw(letters, 24 * glitchX[i], 24 * glitchY[i] + 24);
					}

					batch.setColor(Color.WHITE);
					PowerupSphere.greenPowerDur--;

					if(PowerupSphere.greenPowerDur % 24 == 0) {
						int x = generator.nextInt(801) - 33;
						mainGroup.addActor(new Star(x, 768, 300 - ((x + 33) / 834f * 60)));
					}
				}
			}
		};
		Actor HUD = new Actor() {
			private final Sprite hud = new Sprite(new Texture(Gdx.files.internal("HUD.png")));

			@Override
			public void draw(Batch batch, float parentAlpha) {
				hud.setSize(768, 72);
				hud.setRegion(0, 0, 256, 24);
				hud.setPosition(0, 0);
				hud.draw(batch);

				for(int i = 0; i < player.getHp(); i++) {
					hud.setSize(72, 48);
					hud.setRegion((24 * i), 24, 24, 16);
					hud.setPosition(129 + (72 * i), 12);
					hud.draw(batch);
				}

				for(int i = 0; i < Ship.lives; i++) {
					hud.setSize(24, 24);
					hud.setRegion(0, 40, 24, 24);
					hud.setPosition(7 + (30 * i), 18);
					hud.draw(batch);
				}
			}
		};
		Image title = new Image(new Texture(Gdx.files.internal("Title.png"))) {

			@Override
			public void act(float delta) {
				if(mainGroup.getChildren().contains(player, true))
					moveBy(0, 18);
				if(getY() > 768)
					remove();
			}
		};
		title.setPosition(199.5f, 468);
		Skin skin = new Skin(Gdx.files.internal("UISkin.json"));
		Label lbl = new Label("CREDIT[S]: 255", skin) {

			@Override
			public void draw(Batch batch, float parentAlpha) {
				if(frames % 180 < 90)
					super.draw(batch, parentAlpha);
			}

			@Override
			public void act(float delta) {
				if(mainGroup.getChildren().contains(player, true))
					remove();
			}
		};
		lbl.setPosition(768 / 2f - lbl.getPrefWidth() / 2f, 144);
		Label highScoreLbl = new Label("", skin) {
			@Override
			public void act(float delta) {
				if(mainGroup.getChildren().contains(player, true))
					remove();
			}
		};

		BufferedReader reader = Gdx.files.internal("HighScores.score").reader(8192);
		java.lang.StringBuilder builder = new java.lang.StringBuilder();
		String[][] scoreStrings = new String[8][2];
		for(int i = 0; i < 8; i++)
			try {
				scoreStrings[i] = reader.readLine().split(" ");
			} catch (IOException e) {
				e.printStackTrace();
			}
		for(int i = 0; i < 8; i++) {
			builder.append(i + 1);
			builder.append(": ");
			builder.append(scoreStrings[i][0]);
			for(int j = 0; j < 9 - scoreStrings[i][1].length(); j++)
				builder.append(" ");
			builder.append(scoreStrings[i][1]);
			builder.append("\n");
		}
		highScoreLbl.setText(builder.toString());
		highScoreLbl.setPosition(768 / 2f - lbl.getPrefWidth() / 2f, 324);

		Label distanceLbl = new Label("0000.00 km", skin) {
			@Override
			public void act(float delta) {
				super.act(delta);
				setColor(Color.WHITE);
				if(PowerupSphere.greenPowerDur > 0) {
					setText("0ERR.OR km");
					setColor(Color.RED);
				}
				else if(!Ship.shopping)
					setText(String.format("%04d.%d km", frames, generator.nextInt(89) + 10));
				else
					setText(String.format("%04d.00 km", frames));
			}
		};
		distanceLbl.setPosition(758 - distanceLbl.getPrefWidth(), 0);
		Label scoreLbl = new Label("0000000", skin) {
			@Override
			public void act(float delta) {
				super.act(delta);
				if(Ship.score < 0)
					Ship.score = 0;
				setText(String.format("%07d", Ship.score));
			}
		};
		scoreLbl.setPosition(780 - distanceLbl.getPrefWidth(), 35);
		Label starLbl = new Label("x 00", skin) {
			@Override
			public void act(float delta) {
				super.act(delta);
				setText(String.format("x %02d", Ship.stars));
			}
		};
		starLbl.setPosition(700 - distanceLbl.getPrefWidth(), 35);
		Label shopStarLbl = new Label("x 00", skin) {
			@Override
			public void act(float delta) {
				super.act(delta);
				setText(String.format("x %02d", Ship.stars));
			}
		};
		shopStarLbl.setPosition(795 - distanceLbl.getPrefWidth(), 728);

		player = new Ship(360, 144);
		Enemy.player = player;
		mainGroup.addActor(background);
		mainGroup.addActor(new Actor(){
			@Override
			public void draw(Batch batch, float parentAlpha) {
				if(supernova > 0)
					Projectile.drawExplosion(player.getX() + 24, player.getY() + 24, supernova, batch, Color.WHITE);
			}
		}); // Supernova explosion
		//mainGroup.addActor(player);
		HUDGroup.addActor(HUD);
		HUDGroup.addActor(lbl);
		HUDGroup.addActor(distanceLbl);
		HUDGroup.addActor(scoreLbl);
		HUDGroup.addActor(starLbl);
		HUDGroup.addActor(highScoreLbl);
		HUDGroup.addActor(title);
		HUDGroup.addActor(new Actor(){
			private final Sprite bossHealth = new Sprite(Enemy.region);

			@Override
			public void draw(Batch batch, float parentAlpha) {
				if(Ship.bossHealth > 0) {
					bossHealth.setRegion(0, 277 , 232, 16);
					bossHealth.setSize(696, 48);
					bossHealth.setPosition(36, 720);
					bossHealth.draw(batch);

					bossHealth.setRegion(0, 265, (int)(212 * (Ship.bossHealth / (float)Ship.bossHealthMax)), 12);
					bossHealth.setSize((int)(636 * (Ship.bossHealth / (float)Ship.bossHealthMax)), 36);
					bossHealth.setPosition(78, 726);
					bossHealth.draw(batch);
				}
			}
		});
		mainGroup.addActor(HUDGroup);
		mainGame.addActor(mainGroup);
		Projectile.group = mainGroup;

		Actor storefront = new Actor() {
			private final Sprite store = new Sprite(new Texture(Gdx.files.internal("Storefront.png")));
			private final Sprite icons = new Sprite(new Texture(Gdx.files.internal("StoreIcons.png")));
			private int age = 0;

			@Override
			public void draw(Batch batch, float parentAlpha) {
				icons.setRegion(48 * shopSel, 48 * curMenu, 48, 48);
				icons.setSize(144, 144);
				icons.setPosition(399, 456);
				icons.draw(batch);

				store.setRegion(0, 24, 256, 256);
				store.setSize(768, 768);
				store.setPosition(0, 0);
				store.draw(batch);

				store.setRegion(0, 14, 10, 10);
				store.setColor((4 - shopSel) / 4f, shopSel / 4f, (4 - shopSel) / 4f, 1f);
				store.setSize(30, 30);
				store.setPosition(12, 534 - (105 * shopSel));
				store.draw(batch);
				store.setColor(Color.WHITE);

				store.setRegion(10, 14, 10, 10);
				store.setSize(30, 30);
				store.setPosition(12, 534 - (105 * shopSel));
				store.draw(batch);

				age++;
			}

			@Override
			public boolean remove() {
				store.getTexture().dispose();
				icons.getTexture().dispose();
				return super.remove();
			}
		};
		Label storeLbl = new Label("~", skin) {
			String[][] details;
			String[][] names;

			@Override
			public void draw(Batch batch, float parentAlpha) {
				if(details == null) {
					details = new String[4][5];
					names = new String[4][5];
					FileHandle handle = Gdx.files.internal("UpgradeDetails");
					String[] detailSegments = handle.readString().split("!!!");
					for(int i = 0; i < 4; i++) {
						String[] sectionSegments = detailSegments[i].split("\n");
						for(int j = 1; j < 10; j+=2) {
							if(i == 0)
								details[i][(j - 1) / 2] = sectionSegments[j].toUpperCase();
							else
								details[i][(j - 1) / 2] = sectionSegments[j + 1].toUpperCase();
						}
						for(int j = 0; j < 10; j+=2) {
							if(i == 0)
								names[i][(j / 2)] = sectionSegments[j].toUpperCase().substring(1, sectionSegments[j].length());
							else
								names[i][(j / 2)] = sectionSegments[j + 1].toUpperCase().substring(1, sectionSegments[j + 1].length());
						}
					}
				}
				this.setWidth(336);
				this.setWrap(true);
				setText(details[curMenu][shopSel]);
				setPosition(417, 417 - (getPrefHeight() / 2f));
				super.draw(batch, parentAlpha);

				this.setWrap(false);
				for(int i = 0; i < 5; i++) {
					setText(names[curMenu][i]);
					setPosition(360 - getPrefWidth(), 538 - (105 * i));
					super.draw(batch, parentAlpha);
				}
			}
		};

		shop.addActor(storefront);
		shop.addActor(storeLbl);
		shop.addActor(shopStarLbl);

		curStage = mainGame;

		InputMultiplexer im = new InputMultiplexer(curStage, this);
		Gdx.input.setInputProcessor(im);
		Controllers.addListener(this);
	}

	@Override
	public void render () {
		if(supernova == 0)
			curStage.act(Gdx.graphics.getDeltaTime());
		if(curStage == mainGame)
			Gdx.gl.glClearColor(0.125f, 0, 0.125f, 1);
		else
			Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		curStage.draw();

		for(int i = 0; i < soundtrack.length; i++)
			if(soundtrack[i].isPlaying())
				songPlaying = i;

		if(!Ship.shopping && Ship.alive) {
			if(btnDown[0])
				if(momentum[1] < Ship.speed)
					momentum[1] += (Ship.speed / 4);
			if(btnDown[1])
				if(momentum[1] > -Ship.speed)
					momentum[1] -= (Ship.speed / 4);
			if(btnDown[2])
				if(momentum[0] > -Ship.speed)
					momentum[0] -= (Ship.speed / 4);
			if(btnDown[3])
				if(momentum[0] < Ship.speed)
					momentum[0] += (Ship.speed / 4);
			if(btnDown[4] && cooldown == 0) {
				if((!Ship.upgrades[3][2] || superCd > 0) && PowerupSphere.bluePowerDur <= 0)
					mainGroup.addActor(new BasicLaser(player.getX(), player.getY() + 24, 48, 24, 90, 24, 0, 0, 2 +
							BasicLaser.stacks));
				else
					mainGroup.addActor(new BasicLaser(player.getX(), player.getY() + 24, 48, 48, 90, 24, 0, 152, 6 +
							BasicLaser.stacks));
				if(Ship.upgrades[3][3]) {
					mainGroup.addActor(new BasicLaser(player.getX() + 32, player.getY() + 24, 18, 24, 60, 24, 16, 200,
							2 + BasicLaser.stacks));
					mainGroup.addActor(new BasicLaser(player.getX(), player.getY() + 24, 18, 24, 120, 24, 0, 200, 2 +
							BasicLaser.stacks));
				}
				cooldown = Ship.upgrades[3][0] ? 5 : 15;
				if((Ship.upgrades[3][2] && superCd <= 0) || PowerupSphere.bluePowerDur > 0) {
					long id = Ship.sounds[5].play();
					Ship.sounds[5].setVolume(id, Ship.volume / 100f);
				} else {
					long id = Ship.sounds[1 + generator.nextInt(2)].play();
					Ship.sounds[1 + generator.nextInt(2)].setVolume(id, Ship.volume / 100f);
				}
				superCd = 45;
			}

			switch(level % 3) {
				default:
				case 0:
					if(frames == 10 || (frames > 450 && frames < 5000 && spawnTimes[0] == 0)) {
						mainGroup.addActor(new CannonShip(generator.nextInt(768), 512 - generator.nextInt(144)));
						spawnTimes[0] = 175 + generator.nextInt(50);
					}
					if(frames > 300 && frames < 5000 && spawnTimes[1] == 0) {
						int ofs = 512 - generator.nextInt(144);
						mainGroup.addActor(new EyeBlaster(ofs));
						spawnTimes[1] = 75 + generator.nextInt(100);
					}
					if(frames > 2700 && frames < 5000 && spawnTimes[2] == 0) {
						int ofs = 512 - generator.nextInt(144);
						mainGroup.addActor(new MysteryShip(ofs));
						spawnTimes[2] = 400;
					}
					if(frames == 5150) {
						mainGroup.addActor(new EyeBlasterBoss(216, 480));
					}
					break;
				case 1:
					if(frames == 45)
						mainGroup.addActor(new FlyingBoss(1));
					if((frames >= 145 && (frames < 1200 || frames >= 1750) && (frames < 4650 || (frames > 5300 && frames
							% 480 < 420))) && (frames % 60 == 0)) {
						mainGroup.addActor(new Laser((int)player.getX() + 24, (int)player.getY() + 24));
					}
					if(frames > 1250 && frames < 3250 && spawnTimes[0] == 0) {
						mainGroup.addActor(new ShieldShip(generator.nextInt(714), 714 - generator.nextInt(144)));
						spawnTimes[0] = 200 + generator.nextInt(50);
					}
					if(frames > 3250 && frames < 4650 && spawnTimes[0] == 0) {
						mainGroup.addActor(new ShieldLoanShip(generator.nextInt(714), 714 - generator.nextInt(144)));
						spawnTimes[0] = 175 + generator.nextInt(50);
					}
					if(frames > 400 && frames < 1050 && spawnTimes[1] == 0) {
						mainGroup.addActor(new CannonShip(generator.nextInt(768), 714 - generator.nextInt(144)));
						spawnTimes[1] = 175 + generator.nextInt(50);
					}
					if(frames >= 4650 && frames <= 4750 && frames % 50 == 0)
						mainGroup.addActor(new ShieldShip(303 + ((frames - 4650) / 50 * 54), 512 + ((frames - 4450) / 50 * 54)));
					if(frames == 5150) {
						mainGroup.addActor(new LaserBoss(192, 384));
					}
					break;
			}

			if(player.getInvin() <= 25 || PowerupSphere.yellowPowerDur > 0)
				player.moveBy(momentum[0], momentum[1]);
			if(!btnDown[0] && !btnDown[1])
				if(momentum[1] > 0) {
					momentum[1] -= (Ship.speed / 4);
				} else if(momentum[1] < 0) {
					momentum[1] += (Ship.speed / 4);
				}
			if(!btnDown[2] && !btnDown[3])
				if(momentum[0] > 0) {
					momentum[0] -= (Ship.speed / 4);
				} else if(momentum[0] < 0) {
					momentum[0] += (Ship.speed / 4);
				}

			if(!Ship.shopping)
				frames++;
			if(cooldown > 0)
				cooldown--;
			if(superCd > 0)
				superCd--;
			if(supernova > 0) {
				supernova += 24;
				if(supernova == 1680) {
					player.setInvin(0);
					player.setHp(1);
					Projectile dmg = new Projectile(0, 0, 0, 0, 0, 0, 10);
					SnapshotArray<Actor> actors = new SnapshotArray<Actor>(mainGroup.getChildren());
					for(Actor a : actors)
						if(a instanceof Entity)
							((Entity)a).onHit(dmg);
					soundtrack[songPlaying].play();
					supernova = 0;
				}
			}
			if(BasicLaser.stackDur > 0)
				BasicLaser.stackDur--;
			else
				BasicLaser.stacks = 0;
			if(PowerupSphere.bluePowerDur > 0)
				PowerupSphere.bluePowerDur--;
			if(PowerupSphere.yellowPowerDur > 0)
				PowerupSphere.yellowPowerDur--;
			for(int i = 0; i < spawnTimes.length; i++)
				if(spawnTimes[i] > 0)
					spawnTimes[i]--;

			if((frames == 2500 || frames == 5000) && !Ship.shopping) {
				Ship.shopping = true;
				player.addAction(Actions.sequence(Actions.moveBy(0, -128, .5f), Actions.moveBy(0, 1024, 1f), Actions.run(new Runnable() {
					@Override
					public void run() {
						for(Actor a : mainGroup.getChildren())
							if(a instanceof Enemy || a instanceof Projectile)
								a.remove();
						mainGame.addAction(Actions.sequence(Actions.moveBy(0, 768, 0.75f), Actions.run(new Runnable() {
							@Override
							public void run() {
								curStage = shop;
								shopSel = 0;
							}
						})));
					}
				})));
				for(Actor a : mainGroup.getChildren())
					if(a instanceof Enemy)
						a.addAction(Actions.moveBy(0, -768, 1f));
			}
		}

		if(mainGroup.getChildren().get(mainGroup.getChildren().size - 1) != HUDGroup) { // If the HUD isn't the last thing drawn
			mainGroup.removeActor(HUDGroup);
			mainGroup.addActor(HUDGroup); // Remove & re-add the HUD, making it the last thing drawn
		}
	}

	@Override
	public void dispose() {
		mainGame.dispose();
		Projectile.tex.dispose();
		for(int i = 0; i < soundtrack.length; i++)
			soundtrack[i].dispose();
		if(Projectile.pixmap != null)
			Projectile.pixmap.dispose();
		if(EyeBlasterBoss.screenFill.getTexture() != null)
			EyeBlasterBoss.screenFill.getTexture().dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.UP:
				btnDown[0] = true;
				if(shopSel > 0)
					shopSel--;
				return true;
			case Input.Keys.DOWN:
				btnDown[1] = true;
				if(shopSel < 4)
					shopSel++;
				return true;
			case Input.Keys.LEFT:
				btnDown[2] = true;
				return true;
			case Input.Keys.RIGHT:
				btnDown[3] = true;
				return true;
			case Input.Keys.Z:
				btnDown[4] = true;
				if(shopSel > 1 && curStage == shop && curMenu == 0) {
					curMenu = shopSel - 1;
					shopSel = 0;
				} else if (curStage == shop) {
					if(Ship.stars >= (shopSel + 1) * 10 || Ship.upgrades[curMenu][shopSel]) {
						if(!Ship.upgrades[curMenu][shopSel]) {
							Ship.stars -= (shopSel + 1) * 10;
							long id = Ship.sounds[9].play();
							Ship.sounds[9].setVolume(id, Ship.volume / 100f);
						}
						Ship.upgrades[curMenu][shopSel] = true;
						if(curMenu == 2)
							bomb = (short)shopSel;
					}
				}
				return true;
			case Input.Keys.X:
				if(curStage == mainGame && !Ship.shopping)
					switch (bomb) {
						case 0:
							if(Ship.stars >= 2) {
								long id = Ship.sounds[3].play();
								Ship.sounds[3].setVolume(id, Ship.volume / 100f);
								mainGroup.addActor(new GigaBomb(player.getX(), player.getY() - 24, 90));
								Ship.stars -= 2;
							}
							break;
						case 1:
							if(Ship.stars >= 1) {
								mainGroup.addActor(new BlitzBomb(player.getX() - 24, player.getY() - 24));
								Ship.stars--;
							}
							break;
						case 2:
							if(Ship.stars >= 5) {
								long id = Ship.sounds[3].play();
								Ship.sounds[3].setVolume(id, Ship.volume / 100f);
								mainGroup.addActor(new Obliterator(player));
								Ship.stars -= 5;
							}
							break;
						case 3:
							if(Ship.stars >= 3) {
								for(int i = 0; i < soundtrack.length; i++)
									soundtrack[i].pause();
								long id = Ship.sounds[5].play();
								Ship.sounds[6].setVolume(id, Ship.volume / 100f);
								supernova = 24;
								Ship.stars -= 3;
							}
							break;
						case 4:
							if(Ship.stars >= 5) {
								long id = Ship.sounds[5].play();
								Ship.sounds[6].setVolume(id, Ship.volume / 100f);
								mainGroup.addActor(new AllyBlaster((int)player.getX(), (int)player.getY()));
								Ship.stars -= 5;
							}
							break;
					}
				if(curMenu != 0) {
					shopSel = curMenu + 1;
					curMenu = 0;
				} else if(curStage == shop) {
					curStage = mainGame;
					frames++;
					player.setPosition(360, 144);
					player.setHp(3);
					mainGame.getRoot().addAction(Actions.sequence(Actions.moveBy(0, -768, 0.75f), Actions.run(() ->
							Ship.shopping = false)));
				}
				return true;
			case Input.Keys.SHIFT_RIGHT:
				btnDown[5] = true;
				return true;
			case Input.Keys.A:
				if(btnDown[5])
					Ship.stars += 10;
				return true;
			case Input.Keys.S:
				if(btnDown[5])
					frames = ((frames / 2500) + 1) * 2500 - 1;
				return true;
			case Input.Keys.ENTER:
				if(!mainGroup.getChildren().contains(player, false)) {
					for(Actor a : mainGroup.getChildren())
						if(a instanceof Enemy || a instanceof Projectile)
							a.addAction(Actions.moveBy(0, -768, 1f));
					player.setY(-720);
					player.setInvin(84);
					player.addAction(Actions.sequence(Actions.moveBy(0, 864, 1.4f), Actions.run(() -> {
                        for(Actor a : mainGroup.getChildren())
                            if(a instanceof Enemy || a instanceof Projectile)
                                a.remove();
                        player.setVulnerable(true);
                        soundtrack[0].play();
                        soundtrack[0].setLooping(true);
                    })));
					mainGroup.addActor(player);
					frames = -84;
					return true;
				} else
					return false;
			case Input.Keys.M:
				if(Ship.volume != 0)
					Ship.volume = 0;
				else
					Ship.volume = 33;
				for(int i = 0; i < soundtrack.length; i++)
					soundtrack[i].setVolume(Ship.volume / 100f);
				soundtrack[1].setVolume(Ship.volume / 50f);
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.UP:
				btnDown[0] = false;
				return true;
			case Input.Keys.DOWN:
				btnDown[1] = false;
				return true;
			case Input.Keys.LEFT:
				btnDown[2] = false;
				return true;
			case Input.Keys.RIGHT:
				btnDown[3] = false;
				return true;
			case Input.Keys.Z:
				btnDown[4] = false;
				return true;
			case Input.Keys.SHIFT_RIGHT:
				btnDown[5] = false;
				return true;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public void connected(Controller controller) {

	}

	@Override
	public void disconnected(Controller controller) {

	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		switch (buttonCode) {
			case 0: // A button
				keyDown(Input.Keys.Z);
				break;
			case 1: // B button
				keyDown(Input.Keys.X);
				break;
			case 7: // Start button
				keyDown(Input.Keys.ENTER);
				break;
		}
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		switch (buttonCode) {
			case 0: // A button
				keyUp(Input.Keys.Z);
				break;
			case 1: // B button
				keyUp(Input.Keys.X);
				break;
			case 7: // Start button
				keyUp(Input.Keys.ENTER);
				break;
		}
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		if(Math.abs(value) > 0.25)
			if(axisCode == 1) {
				if(value > 0)
					keyDown(Input.Keys.DOWN);
				else
					keyDown(Input.Keys.UP);
			} else {
				if(value > 0)
					keyDown(Input.Keys.RIGHT);
				else
					keyDown(Input.Keys.LEFT);
			}
		else
			if(axisCode == 1) {
				btnDown[0] = false;
				btnDown[1] = false;
			} else {
				btnDown[2] = false;
				btnDown[3] = false;
			}
		return true;
	}

	@Override
	public boolean povMoved(Controller controller, int povCode, PovDirection value) {
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
		return false;
	}
}
