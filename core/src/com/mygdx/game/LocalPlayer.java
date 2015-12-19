package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class LocalPlayer extends Player {
	boolean isMovingLeft;
	boolean isMovingRight;
	boolean isMovingUp;
	boolean isMovingDown;

	private float moveSpeed;
	int sightX = 400;
	int sightY = 240;

	private boolean canMove;

	public LocalPlayer(Shape shape, boolean passable) {
		super(shape, passable);
		sprite = "Costume1.png";
		changeAppearance();
		moveSpeed = 200;
		canMove = true;

		// TODO Auto-generated constructor stub
	}

	public void setCurrentMap(GameMap currentMap) {
		this.currentMap = currentMap;
	}@Override
	public void update(float stateTime) {
		if (canMove()) {
			try {
				//Each direction has preset limits for the character pos to help prevent outofbounds errors and to smoothen movement along the edges. Once collision is perfected, these should'nt be necessary
				if (isMovingLeft && !isMovingRight) {
					if (pos.getX() > 0 - this.left && currentMap.moveLeft()) {
						currentFrame = moveLeft.getKeyFrame(stateTime, true);
						direction = DirectionOfTravel.LEFT;
					}
				} else if (isMovingRight && !isMovingLeft) {
					if (pos.getX() < currentMap.mapWidth - this.right && currentMap.moveRight()) {
						currentFrame = moveRight.getKeyFrame(stateTime, true);
						direction = DirectionOfTravel.RIGHT;
					}
				} else if (isMovingUp && !isMovingDown) {
					if (pos.getY() < currentMap.mapHeight - this.up && currentMap.moveUp()) {
						currentFrame = moveUp.getKeyFrame(stateTime, true);
						direction = DirectionOfTravel.UP;

					}
				} else if (isMovingDown && !isMovingUp) {
					if (pos.getY() > this.down && currentMap.moveDown()) {
						currentFrame = moveDown.getKeyFrame(stateTime, true);
						direction = DirectionOfTravel.DOWN;
					}
				} else {
					direction = DirectionOfTravel.IDLE;
					// no direction, just stopped
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
	public void setFOV(int x, int y) {
		sightX = x;
		sightY = y;
	}
	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}
	public boolean canMove() {
		return canMove;
	}
	public float getMoveDist() {
		return moveSpeed * Gdx.graphics.getDeltaTime();
	}
	
	protected void handleKeyDown(int keycode) {
		if ((Input.Keys.LEFT == keycode) || (Input.Keys.A == keycode)) {
			isMovingLeft = true;
		} else if ((Input.Keys.RIGHT == keycode) || (Input.Keys.D == keycode)) {
			isMovingRight = true;
		} else if ((Input.Keys.UP == keycode) || (Input.Keys.W == keycode)) {
			isMovingUp = true;
		} else if ((Input.Keys.DOWN == keycode) || (Input.Keys.S == keycode)) {
			isMovingDown = true;
		}
	}

	public void handleKeyUp(int keycode) {
		if ((Input.Keys.LEFT == keycode) || (Input.Keys.A == keycode)) {
			isMovingLeft = false;
		} else if ((Input.Keys.RIGHT == keycode) || (Input.Keys.D == keycode)) {
			isMovingRight = false;
		} else if ((Input.Keys.UP == keycode) || (Input.Keys.W == keycode)) {
			isMovingUp = false;
		} else if ((Input.Keys.DOWN == keycode) || (Input.Keys.S == keycode)) {
			isMovingDown = false;
		}
	}
}