package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
	Button button;
	
	TextButton retryButton;
	
	LostGameState(GameStateManager manager, AntRun g )
	{
		super( manager );
		game = g;
		
	//	Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
	//	button.setSkin(skin);
	//	button = new Button();
	//	button.add("Retry?");
	}
	
	private Stage stage;
	private Table table;
	// For debug drawing
	private ShapeRenderer shapeRenderer;

	public class MyEventListener implements EventListener
	{
		MyEventListener( MyGdxGame GdxGame )
		{
			
		}
		
		@Override
		public boolean handle(Event event) {
			System.out.println(event.toString());
			return false;
		}
	}
	
	public void create () {
	    stage = new Stage();
	    Gdx.input.setInputProcessor(stage);

	    table = new Table();
	    table.setFillParent(true);
	    stage.addActor(table);

	    stage.addListener( new MyEventListener(null) );
	    
	    shapeRenderer = new ShapeRenderer();

	    Texture buttonUpTex   = new Texture("comic-2.png");
	    Texture buttonDownTex = new Texture("comic-4.png");
	    // Add widgets to the table here.
	    TextureRegion upRegion   = new TextureRegion(buttonUpTex) ;
		TextureRegion downRegion = new TextureRegion(buttonDownTex) ;
		BitmapFont buttonFont    = new BitmapFont();

		TextButtonStyle style = new TextButtonStyle();
		style.up = new TextureRegionDrawable(upRegion);
		style.down = new TextureRegionDrawable(downRegion);
		style.font = buttonFont;
		style.fontColor = Color.BLUE;
		
		LabelStyle labelStyle = new LabelStyle(buttonFont, Color.BLUE );
		Label gameover = new Label("GAME OVER", labelStyle);
		table.add(gameover);
		
		retryButton = new TextButton("RETRY", style);
		table.add(retryButton);
		retryButton.addListener( new MyClickListener(this) );
	}

	public class MyClickListener extends ClickListener
	{
		LostGameState state;
		MyClickListener( LostGameState s)
		{
			state = s;
		}
		
		@Override
		public void clicked(InputEvent event, float x, float y)
		{
			System.out.println("RETRY");
			state.retry();
		}
	}
	 
	@Override
	public void resize (int width, int height) {
	    stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void draw( SpriteBatch batch)
	{
		game.drawMaze(batch);
		
	 
	    stage.draw();
	  //  table.drawDebug(shapeRenderer); // This is optional, but enables debug lines for tables.
	 //   CharSequence str = "Game Over";
	 //   game.font.draw(batch, str, 200, 200);
   
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
	public void update(float dt)
	{
	}
	
	@Override
	public void start()
	{
		create();
	}
}
