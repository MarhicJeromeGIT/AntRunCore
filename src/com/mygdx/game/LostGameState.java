package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class LostGameState extends GameState
{
	AntRun game;
	
	Texture GameOverTex;
	float fadeInRatio;
	Texture RetryTex;
	Rectangle retryZone;
	
	LostGameState(GameStateManager manager, AntRun g )
	{
		super( manager );
		game = g;
		
		GameOverTex = new Texture("gameover.png");
		fadeInRatio = 0.0f;
		RetryTex = new Texture("retry.png");
		
		retryZone = new Rectangle( (78*8 - 232) /2, 150, 232, 100 );
	}
	
	@Override
	public void draw( SpriteBatch batch)
	{
		game.drawMaze(batch);
		game.drawPoints(batch);
		
		// Try to keep that centered while growing :
		int x = (int) ((78*8 - 499 * fadeInRatio ) /2);
		int y = (int) ((600 - 80  * fadeInRatio ) /2);
		batch.draw( GameOverTex,  x, y, 499 * fadeInRatio, 80 * fadeInRatio );
		
		if( fadeInRatio >= 1.0f )
		{
			batch.draw( RetryTex,  retryZone.x, retryZone.y, retryZone.width, retryZone.height );	
		}
		
		
	}
	
	// Called when the retry button is pressed
	public void retry()
	{
		if( stateManager.getCurrentState() == this)
		{
			stateManager.popState();
		}
	}
	
	@Override
	public void touchEvent( float x, float y, boolean pressed )
	{
		if( !pressed && fadeInRatio >= 1.0f ) // released
		{
			if( retryZone.contains(x,y) )
			{
				retry();
			}
		}
	}
	
	@Override
	public void update(float dt)
	{
		fadeInRatio = Math.min( 1.0f, fadeInRatio + dt );
	}
	
	@Override
	public void start()
	{
		fadeInRatio = 0.0f;
	}
}
