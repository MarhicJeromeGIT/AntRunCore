package com.mygdx.game;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

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
	
	int antWidth  = 35;
	int antHeight = 35;
	int antStartSpeed = 40;
	boolean antReachedMiddle;
	
	Gauge gauge;
	
	Texture TxtFastButtonReleased;
	Texture TxtFastButtonPressed;
	boolean fastButtonPressed;
	Rectangle fastButtonZone;
	float fastMultiplier = 2.0f;
	
	Music bgMusic;
	
	int points;
	
	public class PawData
	{
		public PawData(int _x, int _y, AntDirection dir, boolean left)
		{
			x = _x;
			y = _y;
			final float[] pawrot = { 180, 90,0,270};
			angle = pawrot[ dir.ordinal() ];
			angle += left ? -30 : +30;
		}
		public int x;
		public int y;
		public float angle;
	}
	Vector<PawData> footprints;
	boolean leftPaw;
	float nextFootprintTime;
	Texture pawTex;
	
	public enum AntDirection
	{
		SOUTH, // 0
		WEST,  // 90
		NORTH, // 180
		EAST,  // 270
	}
	
	public enum TileType
	{
		STRAIGHT_HORI,
		STRAIGHT_VERT,
		CROSS,
		TURN_NE,
		TURN_NW,
		TURN_SE,
		TURN_SW,
		// 
		START,
		STRAIGHT_HORI_BANNED,
		STRAIGHT_VERT_BANNED,
		CROSS_BANNED,
		TURN_NE_BANNED,
		TURN_NW_BANNED,
		TURN_SE_BANNED,
		TURN_SW_BANNED,
		
	}
	Texture[] tex;
	
	Texture[] texBanned;
	
    public BitmapFont font;

	public AntRun()
	{
		maze = new TileType[SX][SY];
		visited = new boolean[SX][SY];
				
		tex = new Texture[TileType.values().length];
		tex[TileType.STRAIGHT_HORI.ordinal()]   = new Texture("square_hori_2.png");
		tex[TileType.STRAIGHT_VERT.ordinal()]   = new Texture("square_vert_2.png");
		tex[TileType.CROSS.ordinal()]   = new Texture("square_cross_2.png");
		tex[TileType.TURN_NE.ordinal()] = new Texture("square_turn_NE_2.png");
		tex[TileType.TURN_NW.ordinal()] = new Texture("square_turn_NW_2.png");
		tex[TileType.TURN_SE.ordinal()] = new Texture("square_turn_SE_2.png");
		tex[TileType.TURN_SW.ordinal()] = new Texture("square_turn_SW_2.png");
		tex[TileType.STRAIGHT_HORI_BANNED.ordinal()]   = new Texture("square_hori_banned.png");
		tex[TileType.STRAIGHT_VERT_BANNED.ordinal()]   = new Texture("square_vert_banned.png");
		tex[TileType.CROSS_BANNED.ordinal()]   = new Texture("square_cross_banned.png");
		tex[TileType.TURN_NE_BANNED.ordinal()] = new Texture("square_NE_banned.png");
		tex[TileType.TURN_NW_BANNED.ordinal()] = new Texture("square_NW_banned.png");
		tex[TileType.TURN_SE_BANNED.ordinal()] = new Texture("square_SE_banned.png");
		tex[TileType.TURN_SW_BANNED.ordinal()] = new Texture("square_SW_banned.png");
		tex[TileType.START.ordinal()]   = new Texture("square_start_2.png");
		
		gauge = new Gauge();
		
		antTexture = new Texture[3];
		antTexture[0] = new Texture("newcat_1.png");
		antTexture[1] = new Texture("newcat_1.png");
		antTexture[2] = new Texture("newcat_2.png");		
		antTextureIndex = 0;
		antAnimTime = 0.400f;
		antDirection = AntDirection.SOUTH;
		
		footprints = new Vector<PawData>();
		pawTex = new Texture("paw.png");
		
		TxtFastButtonReleased = new Texture("fastReleased.png");
		TxtFastButtonPressed = new Texture("fastPressed.png");
		fastButtonPressed = false;
		fastButtonZone = new Rectangle(635,400,130,71);

		bgMusic = Gdx.audio.newMusic(Gdx.files.internal("bg1.mp3"));
		// start the playback of the background music immediately
		bgMusic.setLooping(true);
		bgMusic.play();
		
        font = new BitmapFont();
        font.getData().setScale(2.0f,2.0f);
       
        
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
		nextFootprintTime = 0.0f;
		leftPaw = true;
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
			if( antDirection != AntDirection.NORTH && !isPathBlocked( AntDirection.SOUTH, tileX, (SY+tileY-1)%SY ))
			{
				possibleDir.add(AntDirection.SOUTH);
			}			
			if( antDirection != AntDirection.EAST && !isPathBlocked( AntDirection.WEST, (SX+tileX-1)%SX, tileY ))
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
		if( nextDirection != antDirection )
		{
			antDirection = nextDirection;
			// recenter the cat in the middle of the path :
			if( antDirection == AntDirection.NORTH || antDirection == AntDirection.SOUTH )
			{
				antX = tileX * tileSize + tileSize / 2;
			}
			else if( antDirection == AntDirection.WEST || antDirection == AntDirection.EAST )
			{
				antY = tileY * tileSize + tileSize / 2;
			}
		}
	}
	boolean isPathBlocked( AntDirection direction, int tileX, int tileY )
	{
		TileType currentTile = maze[tileX][tileY];
		if( currentTile.ordinal() >= TileType.START.ordinal() )
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
		footprints.clear();
		for( int i=0; i< SX; i++ )
		{
			for( int j=0; j< SY; j++ )
			{
				int pick = new Random().nextInt( TileType.START.ordinal() );// avoid picking the "start","banned" or "banned" tiles
				maze[i][j] = TileType.values()[pick ]; 
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
				footprints.clear();
				
				for( int i=0; i< SX; i++ )
				{
					for( int j=0; j< SY; j++ )
					{
						visited[i][j] = false;
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
				while( (pickX == newTileX && pickY == newTileY ) || maze[pickX][pickY].ordinal() > TileType.START.ordinal() )
				{
					pickX = new Random().nextInt(SX);
					pickY = new Random().nextInt(SY);
				}
				maze[pickX][pickY] = TileType.values()[ maze[pickX][pickY].ordinal() + 8 ];
				System.out.println( "banned " + pickX + "," + pickY );
			}
			
			// check that we didn't run into a wall :
			if( isPathBlocked( antDirection, newTileX, newTileY ) )
			{
				died = true;
				return;
			}

		}
		
		antX = newAntX;
		antY = newAntY;
		
		nextFootprintTime -= dt;
		if( nextFootprintTime <= 0 )
		{
			leftPaw = !leftPaw;
			nextFootprintTime = fastButtonPressed ? 0.2f : 0.4f;
			
			if( antDirection == AntDirection.NORTH || antDirection == AntDirection.SOUTH )
			{
				int x = (int) (leftPaw ? antX - antWidth * 0.25 : antX + antWidth * 0.25);
				footprints.addElement( new PawData( x, (int) antY, antDirection, leftPaw));
			}
			else if( antDirection == AntDirection.EAST || antDirection == AntDirection.WEST )
			{
				int y = (int) (leftPaw ? antY -antHeight * 0.25 : antY + antHeight * 0.25);
				footprints.addElement( new PawData( (int) antX, y, antDirection, leftPaw));
			}

		}
		
		// When we reach the middle of a tile, the ant must take a decision concerning the next turn :
		if( !antReachedMiddle )
		{
			int posXonTile = (int) (antX % tileSize);
			int posYonTile = (int) (antY % tileSize);
			int middle = tileSize / 2;
			
			if( antDirection == AntDirection.NORTH )
			{
				if( posYonTile+antHeight/2 >= middle )
				{
					antReachedMiddle = true;
				}
			}
			else if( antDirection == AntDirection.SOUTH )
			{
				if( posYonTile+antHeight/2 <= middle )
				{
					antReachedMiddle = true;
				}
			}
			else if( antDirection == AntDirection.WEST )
			{
				if( posXonTile+antWidth/2 <= middle )
				{
					antReachedMiddle = true;
				}
			}
			else if( antDirection == AntDirection.EAST )
			{
				if( posXonTile+antWidth/2 >= middle )
				{
					antReachedMiddle = true;
				}
			}
			
			if( antReachedMiddle )
			{
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
	public void drawPoints(SpriteBatch batch)
	{
        CharSequence str = String.valueOf(points) + " Pts";
        font.draw(batch, str, 670, 350 );
 
	}
	public void draw(SpriteBatch batch)
	{		
		drawMaze(batch);
		
		
		for( int i=0; i< footprints.size(); i++ )
		{
			int s =10;
			batch.draw (pawTex, footprints.get(i).x, footprints.get(i).y, s/2, s/2, s, s, 1.0f,
					1.0f, footprints.get(i).angle, 0, 0, pawTex.getWidth(), pawTex.getHeight(), false, false);

		}

		//batch.draw( antTexture[antTextureIndex], antX, antY, antWidth, antHeight);
		final float[] antrot = { 180, 90, 0, 270 };
		float antRotation = antrot[ antDirection.ordinal() ];
		batch.draw (antTexture[antTextureIndex], antX - antWidth/2, antY - antHeight/2,antWidth/2, antHeight/2, antWidth, antHeight, 1.0f,
				1.0f, antRotation, 0, 0, antTexture[antTextureIndex].getWidth(), antTexture[antTextureIndex].getHeight(), false, false);

		
		gauge.draw(batch);
		
		batch.draw( fastButtonPressed ? TxtFastButtonPressed : TxtFastButtonReleased, fastButtonZone.x, fastButtonZone.y, fastButtonZone.width, fastButtonZone.height );

		
		drawPoints(batch);

	}
}
