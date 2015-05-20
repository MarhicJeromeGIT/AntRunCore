package com.mygdx.game;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class AntRun 
{
	final int SX = 8;
	final int SY = 6;
	TileType[][] maze;
	boolean[][] visited;
	
	int tileSize = 75;
	
	int startX;
	int startY;
	float startAngle;
	
	// ant data :
	float antX; // en unites (max 800)
	float antY; // max 480
	float antSpeed; // unites/second
	AntDirection antDirection;
	Texture[] antTexture;
	int antTextureIndex;
	float antAnimTime;
	boolean died;
	
	int antWidth  = 10;
	int antHeight = 15;
	int antStartSpeed = 30;
	boolean antReachedMiddle;
	
	Gauge gauge;
	
	Texture TxtFastButtonReleased;
	Texture TxtFastButtonPressed;
	boolean fastButtonPressed;
	Rectangle fastButtonZone;
	float fastMultiplier = 2.0f;
	
	Music bgMusic;
	
	int points;
	
	public enum AntDirection
	{
		SOUTH, // 0
		WEST,  // 90
		NORTH, // 180
		EAST,  // 270
	}
	
	public enum TileType
	{
		FOREVER_BANNED,
		VISITED,
		START,
		STRAIGHT_HORI,
		STRAIGHT_VERT,
		CROSS,
		TURN_NE,
		TURN_NW,
		TURN_SE,
		TURN_SW,
	}
	Texture[] tex;
	
    public BitmapFont font;

	public AntRun()
	{
		maze = new TileType[SX][SY];
		visited = new boolean[SX][SY];
				
		tex = new Texture[TileType.values().length];
		tex[TileType.FOREVER_BANNED.ordinal()] = new Texture("visited.png");
		tex[TileType.VISITED.ordinal()] = new Texture("visited.png");
		tex[TileType.START.ordinal()]   = new Texture("square_start.png");
		tex[TileType.STRAIGHT_HORI.ordinal()]   = new Texture("square_hori.png");
		tex[TileType.STRAIGHT_VERT.ordinal()]   = new Texture("square_vert.png");
		tex[TileType.CROSS.ordinal()]   = new Texture("square_cross.png");
		tex[TileType.TURN_NE.ordinal()] = new Texture("square_turn_NE.png");
		tex[TileType.TURN_NW.ordinal()] = new Texture("square_turn_NW.png");
		tex[TileType.TURN_SE.ordinal()] = new Texture("square_turn_SE.png");
		tex[TileType.TURN_SW.ordinal()] = new Texture("square_turn_SW.png");

		gauge = new Gauge();
		
		antTexture = new Texture[3];
		antTexture[0] = new Texture("ant.png");
		antTexture[1] = new Texture("ant_walk1.png");
		antTexture[2] = new Texture("ant_walk2.png");
		antTextureIndex = 0;
		antAnimTime = 0.400f;
		antDirection = AntDirection.SOUTH;
		
		TxtFastButtonReleased = new Texture("fastReleased.png");
		TxtFastButtonPressed = new Texture("fastPressed.png");
		fastButtonPressed = false;
		fastButtonZone = new Rectangle(650,400,70,50);

		bgMusic = Gdx.audio.newMusic(Gdx.files.internal("bg1.mp3"));
		// start the playback of the background music immediately
		bgMusic.setLooping(true);
		bgMusic.play();
		
        font = new BitmapFont();


		restartGame();
	}
	
	// Called when the Ant starts walking in the maze
	public void startAnt()
	{
		antX = (int) (((float)startX+0.5) * tileSize);
		antY = (int) (((float)startY+0.5) * tileSize);
		int dir = (int)startAngle/90;
		antDirection = AntDirection.values()[dir];		
		antReachedMiddle = true;
		gauge.addStep(1);
		antTextureIndex = 1;
		died = false;
	}
	private void decideNewAntDirection()
	{		
		// decide a new direction for the ant to take.
		AntDirection nextDirection = antDirection;
		int tileX = (int) (antX/tileSize);
		int tileY = (int) (antY/tileSize);
		TileType type = maze[tileX][tileY];
		switch( type )
		{
		case STRAIGHT_HORI:
		case STRAIGHT_VERT:
			nextDirection = antDirection;
			break;
		case CROSS:
			// we have three potential directions. Choose a direction whose path is not blocked, otherwise keep the same path.
			// Also, no U-turn.
			ArrayList<AntDirection> possibleDir = new ArrayList<AntDirection>(3);
			if( antDirection != AntDirection.SOUTH && !isPathBlocked( AntDirection.NORTH, tileX, (tileY+1)%SY ))
			{
				possibleDir.add(AntDirection.NORTH);
			}
			if( antDirection != AntDirection.NORTH && !isPathBlocked( AntDirection.SOUTH, tileX, Math.abs((tileY-1)%SY) ))
			{
				possibleDir.add(AntDirection.SOUTH);
			}			
			if( antDirection != AntDirection.EAST && !isPathBlocked( AntDirection.WEST, Math.abs((tileX-1)%SX), tileY ))
			{
				possibleDir.add(AntDirection.WEST);
			}			
			if( antDirection != AntDirection.WEST && !isPathBlocked( AntDirection.EAST, (tileX+1)%SX, tileY ))
			{
				possibleDir.add(AntDirection.EAST);
			}
			if( !possibleDir.isEmpty() )
			{
				int randomIndex = new Random().nextInt( possibleDir.size() );
				nextDirection = possibleDir.get(randomIndex);
			}
			break;
		case TURN_NE:
			if( antDirection == AntDirection.SOUTH )
				nextDirection = AntDirection.EAST;
			else // going west
				nextDirection = AntDirection.NORTH;
			break;
		case TURN_NW:
			if( antDirection == AntDirection.SOUTH )
				nextDirection = AntDirection.WEST;
			else // going east
				nextDirection = AntDirection.NORTH;
			break;
		case TURN_SE:
			if( antDirection == AntDirection.NORTH )
				nextDirection = AntDirection.EAST;
			else // going west
				nextDirection = AntDirection.SOUTH;
			break;
		case TURN_SW:
			if( antDirection == AntDirection.NORTH )
				nextDirection = AntDirection.WEST;
			else // going east
				nextDirection = AntDirection.SOUTH;
			break;
		default:
			break;
		}
		
		antDirection = nextDirection;
	}
	boolean isPathBlocked( AntDirection direction, int tileX, int tileY )
	{
		TileType currentTile = maze[tileX][tileY];
		if( currentTile == TileType.START )
		{
			return true;
		}
		if( visited[tileX][tileY] )
		{
			return true;
		}
		switch( direction )
		{
		case SOUTH:
			if( currentTile == TileType.STRAIGHT_HORI || currentTile == TileType.TURN_SE || currentTile == TileType.TURN_SW )
			{
				return true;
			}
			break;
		case NORTH:
			if( currentTile == TileType.STRAIGHT_HORI || currentTile == TileType.TURN_NE || currentTile == TileType.TURN_NW )
			{
				return true;
			}
			break;
		case WEST:
			if( currentTile == TileType.STRAIGHT_VERT || currentTile == TileType.TURN_SW || currentTile == TileType.TURN_NW )
			{
				return true;
			}
			break;
		case EAST:
			if( currentTile == TileType.STRAIGHT_VERT || currentTile == TileType.TURN_SE || currentTile == TileType.TURN_NE )
			{
				return true;
			}
			break;
		}
		return false;
	}
	// Reinitializes the game.
	public void restartGame()
	{
		antX = 0;
		antY = 0;
		antSpeed = antStartSpeed;
		antReachedMiddle = false;
		fastButtonPressed = false;
		died = false;
		gauge.reset();
		
		for( int i=0; i< SX; i++ )
		{
			for( int j=0; j< SY; j++ )
			{
				int pick = new Random().nextInt( TileType.values().length -3 );
				maze[i][j] = TileType.values()[pick + 3 ]; // avoid picking the "start","banned" or "visited" tiles
				visited[i][j] = false;
			}
		}
		
		startX = new Random().nextInt(SX);
		startY = new Random().nextInt(SY);
		startAngle = 0.0f;
		maze[startX][startY] = TileType.START;

		points = 0;
	}
	
	private void updateAnt( float dt )
	{
		// Update the Ant Animation		
		antAnimTime -= dt;
		if( antAnimTime <= 0.0f )
		{
			antAnimTime = 0.400f;
			antTextureIndex = 1 + (antTextureIndex)%2;
		}	
		
		// Update the Ant deplacement
		int tileX = (int) (antX/tileSize);
		int tileY = (int) (antY/tileSize);
		
		float newAntX = antX;
		float newAntY = antY;
		
		boolean hasCrossedLimit = false; // true if we went outside the limit (loop on the other side)
		float currentSpeed = antSpeed;
		if(fastButtonPressed)
			currentSpeed *= fastMultiplier;
		
		switch(antDirection)
		{
		case SOUTH:
			newAntY -= currentSpeed * dt;
			if( newAntY < 0 )
			{
				newAntY = SY * tileSize -1;
				hasCrossedLimit = true;
			}
			break;
		case NORTH:
			newAntY += currentSpeed * dt;
			if( newAntY >= SY * tileSize )
			{
				newAntY = 0;
				hasCrossedLimit = true;
			}
			break;
		case WEST:
			newAntX -= currentSpeed * dt;
			if( newAntX < 0 )
			{
				newAntX = SX * tileSize -1;
				hasCrossedLimit = true;
			}
			break;
		case EAST:
			newAntX += currentSpeed * dt;
			if( newAntX >= SX * tileSize )
			{
				newAntX = 0;
				hasCrossedLimit = true;
			}
			break;
		default:
			break;
		}
		
		int newTileX = (int) (newAntX/tileSize);
		int newTileY = (int) (newAntY/tileSize);
		if( newTileX != tileX || newTileY != tileY ) // we started walking on a new tile.
		{
			points +=2;
			gauge.addStep(fastButtonPressed? 2 : 1);
			System.out.println(gauge.nbSteps);
			antReachedMiddle = false;
			// check that we didn't run into a wall :
			if( isPathBlocked( antDirection, newTileX, newTileY ) )
			{
				died = true;
				return;
			}
			else
			{
				// mark the old tile as visited
				visited[tileX][tileY] = true;
				
				if( hasCrossedLimit )
				{
					fastButtonPressed = false;
					points += 3;
				}
				
				// BONUS! reset all the visited tiles, and transform the "start" tile in a go through tile.
				if( hasCrossedLimit && gauge.isFull() )
				{
					gauge.reset();
					for( int i=0; i< SX; i++ )
					{
						for( int j=0; j< SY; j++ )
						{
							if(maze[i][j] != TileType.FOREVER_BANNED )
							{
								visited[i][j] = false;
							}
						}
					}
					if( startX > 0 )
					{
						maze[startX][startY] = TileType.STRAIGHT_HORI;
						startX = -1;
						startY = -1;
					}
					
					// pick a tile at random and ban it
					int pickX = newTileX;
					int pickY = newTileY;
					while( (pickX == newTileX && pickY == newTileY ) || maze[pickX][pickY] == TileType.FOREVER_BANNED )
					{
						pickX = new Random().nextInt(SX);
						pickY = new Random().nextInt(SY);
					}
					maze[pickX][pickY] = TileType.FOREVER_BANNED;
					visited[pickX][pickY] = true;
				}
			}
		}
		
		antX = newAntX;
		antY = newAntY;
		
		// When we reach the middle of a tile, the ant must take a decision concerning the next turn :
		if( !antReachedMiddle )
		{
			int posXonTile = (int) (antX % tileSize);
			int posYonTile = (int) (antY % tileSize);
			int offset = 5;
			int middle = tileSize / 2;
			if( posXonTile > middle-offset && posXonTile < middle+offset && posYonTile > middle-offset && posYonTile < middle+offset )
			{
				antReachedMiddle = true;
				points += 2;
				decideNewAntDirection();
			}
		}
	}
	
	public void update(float dt)
	{
		updateAnt(dt);
	}
	
	public void touchEvent( float x, float y, boolean pressed )
	{
		// are we touching the press button ?
		if( pressed && fastButtonZone.contains(new Vector2(x,y)) )
		{
			fastButtonPressed = true;
			return;
		}
		
		if( !pressed ) // released
		{
			// which tile are we touching :
			int tx = (int) (x / tileSize);
			int ty = (int) (y / tileSize);
			if( tx >= 0 && tx < SX && ty >= 0 && ty < SY )
			{
				int antTileX = (int) (antX/tileSize);
				int antTileY = (int) (antY/tileSize);
				// cannote rotate already visited or currently visiting tile
				if( visited[tx][ty] || (tx == antTileX && ty == antTileY ) )
				{
					return;
				}
				
				//rotate this tile:
				TileType type = maze[tx][ty];
				switch( type )
				{
				case TURN_NE:
					maze[tx][ty] = TileType.TURN_SE;
					break;
				case TURN_SE:
					maze[tx][ty] = TileType.TURN_SW;
					break;
				case TURN_SW:
					maze[tx][ty] = TileType.TURN_NW;
					break;
				case TURN_NW:
					maze[tx][ty] = TileType.TURN_NE;
					break;
				case STRAIGHT_HORI:
					maze[tx][ty] = TileType.STRAIGHT_VERT;
					break;
				case STRAIGHT_VERT:
					maze[tx][ty] = TileType.STRAIGHT_HORI;
					break;
				case START:
					startAngle += 90.0f;
					startAngle  = (int)startAngle % 360;
					break;
				default:
					break;
				}
			}
		}
	}
	public void drawMaze(SpriteBatch batch)
	{
		for( int i=0; i< SX; i++ )
		{
			for( int j=0; j < SY; j++ )
			{
				if( visited[i][j] )
				{
					batch.draw(tex[TileType.VISITED.ordinal()], i * tileSize, j * tileSize, tileSize, tileSize );
				}
				else
				{
					TileType type = maze[i][j];
					if( type == TileType.START )
					{
				//		public void draw (Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX,
				//				float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
	
						batch.draw (tex[type.ordinal()], (float)i * tileSize, (float)j * tileSize, tileSize/2,tileSize/2, (float)tileSize,(float)tileSize, 1.0f,
								1.0f, -startAngle, 0,0,124,124, false, false);
	
					}
					else
					{
						batch.draw(tex[type.ordinal()], i * tileSize, j * tileSize, tileSize, tileSize );
					}
				}
			}
		}
	}
	
	public void draw(SpriteBatch batch)
	{		
		drawMaze(batch);
		
		//batch.draw( antTexture[antTextureIndex], antX, antY, antWidth, antHeight);
		final float[] antrot = { 180, 90, 0, 270 };
		float antRotation = antrot[ antDirection.ordinal() ];
		batch.draw (antTexture[antTextureIndex], antX, antY, 0,0, antWidth, antHeight, 1.0f,
				1.0f, antRotation, 0, 0, 64, 75, false, false);
		
		gauge.draw(batch);
		
		batch.draw( fastButtonPressed ? TxtFastButtonPressed : TxtFastButtonReleased, fastButtonZone.x, fastButtonZone.y, fastButtonZone.width, fastButtonZone.height );

		
        CharSequence str = String.valueOf(points);
        font.draw(batch, str, 650, 200);
        

	}
}
