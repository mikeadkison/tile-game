package com.mygdx.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox.CheckBoxStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class TheGame extends ApplicationAdapter 
{
	SpriteBatch batch;
	
	GameMap currentMap;
	GuiManager mapGuiManager; //holds all gui elements which are displayed when map is visible
	GuiManager mainMenuGuiManager;
	//GuiManager mainMenuGuiManager
	//etc
	private boolean itemListExists;
	private PrintWriter out;
    private BufferedReader in;
    private long time;
    private final int SEND_SPACING = 50;
    private DirectionOfTravel playerOldDirection;
    private Skin skin;
    private Stage stage;
    private Socket socket;
    private Table mainMenuTable;
    private TextField errorTextField;
    
    //private List<Player> playersDrawnInLobby;
    private LabelStyle labelStyle;
    private Table lobbyTable;
    private Shape playerShape;
    private Point oldPos;
    private Map<Player, CheckBox> playerToCheckBoxMap;
    private VerticalGroup chatMessagesVGroup;
    private static final int CHAT_BOX_HEIGHT = 150;
    private int numChatLines; //for in-lobby chat
    private TextField messageTextField;
    private InputListener inLobbyMessageTextFieldListener;
    private InputListener inGameMessageTextFieldListener;
    
    protected LocalPlayer localPlayer;
    private Preferences preferences;
    private static final Color GREEN = new Color(.168f, .431f, .039f, 1);
    
    private static enum GameState {
        MAIN_MENU,
        CONNECTED_TO_SERVER,
        IN_LOBBY,
        GAME_STARTED,
    }
    private GameState gameState;
    private InputMultiplexer inputMultiplexer; //will delegate events tos the game inputprocessor and the gui inputprocessor (the stage)
    private InputProcessor gameInputProcessor;
    
	@Override
	public void create() {
		numChatLines = 0;
		oldPos = new Point(0, 0);
		gameState = GameState.MAIN_MENU;
		batch = new SpriteBatch();
		
		//set up input processors (stage and gameInputProcessor) and add them to the multiplexer
		// stage should get events first and then possibly gameInputProcessor
		stage = new Stage(); //the gui is laid out here
		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(stage);
		
		Gdx.input.setInputProcessor(inputMultiplexer); //the stage which contains the gui/hud gets to handle inputs first, and then pass the ones it doesn't handle down to the game
		
		playerShape = new Shape(Arrays.asList(
				new LineSeg(new Point(15, 0), new Point(15, 55)),
				new LineSeg(new Point(15, 55), new Point(50, 55)),
				new LineSeg(new Point(50, 55), new Point(50, 0)),
				new LineSeg(new Point(50, 0), new Point(15, 0))
				),
				new Point(0,0));
		
		

		setupMainMenu();
	}
	
	
	private void setupMainMenu() {
		// A skin can be loaded via JSON or defined programmatically, either is fine. Using a skin is optional but strongly
		// recommended solely for the convenience of getting a texture, region, etc as a drawable, tinted drawable, etc.
		skin = new Skin();

		// Generate a 1x1 white texture and store it in the skin named "white".
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		skin.add("white", new Texture(pixmap));

		// Store the default libgdx font under the name "default".
		skin.add("default", new BitmapFont());

		// Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
		final TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.checked = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.over = skin.newDrawable("white", Color.DARK_GRAY);
		textButtonStyle.font = skin.getFont("default");
		skin.add("default", textButtonStyle);
		
		TextFieldStyle textFieldStyle = new TextFieldStyle();
		textFieldStyle.background = skin.newDrawable("white", GREEN);
		textFieldStyle.font = skin.getFont("default");
		textFieldStyle.fontColor = Color.WHITE;
		textFieldStyle.cursor = skin.newDrawable("white", Color.WHITE);
		textFieldStyle.cursor.setMinWidth(2f);
		skin.add("default", textFieldStyle);
		
		labelStyle = new LabelStyle();
		labelStyle.font = skin.getFont("default");
		labelStyle.fontColor = Color.WHITE;
		skin.add("default", labelStyle);
		
		//http://www.vogella.com/tutorials/JavaPreferences/article.html
		preferences = Preferences.userRoot().node(this.getClass().getName()); //used to save/load fields on server connect page
		
		Label serverAddressLabel = new Label("Server Address: ", labelStyle);
	  
		// Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
		final TextButton connectButton = new TextButton(" Connect ", skin);
				
		final TextField serverPortField = new TextField(preferences.get("serverPortField", ""), skin);
		serverPortField.setWidth(70);
		serverPortField.setAlignment(Align.center);
		System.out.println(serverPortField.getWidth());
		serverPortField.setHeight(30);
		
		final TextField serverAddressField = new TextField(preferences.get("serverAddressField", ""), skin);
		serverAddressField.setWidth(200);
		serverAddressField.setHeight(30);
		serverAddressField.setAlignment(Align.center);
		serverAddressField.setTextFieldFilter(new TextFieldFilter() {

			@Override
			public boolean acceptChar(TextField textField, char c) {
				if ('.' == c || Character.isDigit(c) || Character.isAlphabetic(c)) {
					if (serverPortField.getText().length() > 0) { //highlight and enable connect button
						setEnabledAndHighlight(connectButton, true);
					}
					return true;
				}
				if (textField.getText().length() == 0) {
					setEnabledAndHighlight(connectButton, false);
				}
				return false;
			}
			
		});
		
		;
				
		Label serverPortLabel = new Label("Port: ", labelStyle);
		
		
		//only accept digits in port field
		serverPortField.setTextFieldFilter(new TextFieldFilter() {

			@Override
			public boolean acceptChar(TextField textField, char c) {
				if (Character.isDigit(c)) {
					if (serverAddressField.getText().length() > 0) { //highlight and enable connect button
						setEnabledAndHighlight(connectButton, true);
					}
					return true;
				}
				if (textField.getText().length() == 0) {
					setEnabledAndHighlight(connectButton, false);
				}
				return false;
			}
		});
		
		final TextField usernameField = new TextField(preferences.get("username", ""), skin);
		usernameField.setWidth(200);
		usernameField.setHeight(30);
		usernameField.setAlignment(Align.center);
		
		Label usernameLabel = new Label("Username: ", labelStyle);	

		
		//create a table that fills the screen
		mainMenuTable = new Table();
		mainMenuTable.setFillParent(true);
		mainMenuTable.setSize(200, 300);
		mainMenuTable.center();
		stage.addActor(mainMenuTable);
		
		//populate table
		mainMenuTable.add(serverAddressLabel);
		mainMenuTable.add(serverAddressField);
		mainMenuTable.add(serverPortLabel).padLeft(20);
		mainMenuTable.add(serverPortField).width(70);
		mainMenuTable.row();  //new row
		mainMenuTable.add(usernameLabel).padTop(20);
		mainMenuTable.add(usernameField).padTop(20);
		mainMenuTable.row();
		mainMenuTable.add(connectButton).colspan(4).center().padTop(40);
		//mainMenuTable.debugAll(); //show bounding boxes


		// Add a listener to the button. ChangeListener is fired when the button's checked state changes, eg when clicked,
		// Button#setChecked() is called, via a key press, etc. If the event.cancel() is called, the checked state will be reverted.
		// ClickListener could have been used, but would only fire when clicked. Also, canceling a ClickListener event won't
		// revert the checked state.
		connectButton.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				if (null != errorTextField) {
					errorTextField.remove();
				}
				if (serverAddressField.getText().length() > 0
						&& serverPortField.getText().length() > 0
						&& connectToServer(serverAddressField.getText(), Integer.parseInt(serverPortField.getText()), usernameField.getText())) {
					
					//save textFields for next game session
					preferences.put("username", usernameField.getText());
					preferences.put("serverPortField", serverPortField.getText());
					preferences.put("serverAddressField", serverAddressField.getText());
					
					
					
					
					
					setupLobby();
				}
			}
		});

		// Add an image actor. Have to set the size, else it would be the size of the drawable (which is the 1x1 texture).
		//table.add(new Image(skin.newDrawable("white", Color.RED))).size(64);
	}
	private void setupLobby() {
		gameState = GameState.IN_LOBBY;
		playerToCheckBoxMap = new HashMap<Player, CheckBox>();
		//create local player
		//currentMap.players.add(player);
		
		final CheckBoxStyle checkBoxStyle = new CheckBoxStyle();
		checkBoxStyle.checkboxOff = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("art/checkbox_unchecked.png"))));
		checkBoxStyle.checkboxOn = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("art/checkbox_checked.png"))));
		checkBoxStyle.font = skin.getFont("default");
		skin.add("default", checkBoxStyle);
		
		final CheckBox readyCheckBox = new CheckBox("", checkBoxStyle);
		playerToCheckBoxMap.put(currentMap.player, readyCheckBox);
		readyCheckBox.setPosition(600, 70);
		//readyCheckBox.setWidth(100);
		//readyCheckBox.setHeight(30);
		//readyCheckBox.debug();
		//readyCheckBox.setSize(100, 50);
		
		readyCheckBox.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) { //notify server of readynes or unreadyness
				JSONObject readyMessage = new JSONObject();
				readyMessage.put("type", "readyStatus");
				readyMessage.put("readyStatus", readyCheckBox.isChecked());
				out.println(readyMessage);
				
				//check the box by name as well
				playerToCheckBoxMap.get(localPlayer).setChecked(readyCheckBox.isChecked());
			}
		});
		final Label readyCheckBoxLabel = new Label("Ready?", skin);
		readyCheckBoxLabel.setPosition(520,  70);
		
		//Creating checkboxes for the costume options while in lobby
		final Label costumeCheckBoxLabel = new Label("Costumes", skin);
		final CheckBox costumeCheckBox1 = new CheckBox(" 1", checkBoxStyle);
		final CheckBox costumeCheckBox2 = new CheckBox(" 2", checkBoxStyle);
		final CheckBox costumeCheckBox3 = new CheckBox(" 3", checkBoxStyle);
		final CheckBox costumeCheckBox4 = new CheckBox(" 4", checkBoxStyle);
		
		
		costumeCheckBoxLabel.setPosition(600, 350);
		costumeCheckBox1.setPosition(600, 330);
		costumeCheckBox2.setPosition(600, 310);
		costumeCheckBox3.setPosition(600, 290);
		costumeCheckBox4.setPosition(600, 270);
		
		ButtonGroup<CheckBox> costumeButtons = new ButtonGroup<CheckBox>(costumeCheckBox1, costumeCheckBox2, costumeCheckBox3, costumeCheckBox4);
		costumeButtons.setMaxCheckCount(1);
		costumeButtons.setUncheckLast(true);
		costumeCheckBox1.setChecked(true);

			
		//applies costume change for localplayer. Might need to move this to another location.
		class CostumeChange extends ChangeListener{
			String sprite;
			CheckBox checkBox;
			public CostumeChange(CheckBox checkBox, String sprite){
				this.checkBox = checkBox;
				this.sprite = sprite;
			}
			public void changed(ChangeEvent event, Actor actor){
				if(!checkBox.isChecked()){
					localPlayer.sprite = sprite;
					localPlayer.changeAppearance();
				}
			}
			
		}
		
		costumeCheckBox1.addListener(new CostumeChange(costumeCheckBox1, "Costume1.png"));
		costumeCheckBox2.addListener(new CostumeChange(costumeCheckBox1, "Costume2.png"));
		costumeCheckBox3.addListener(new CostumeChange(costumeCheckBox1, "Costume3.png"));
		costumeCheckBox4.addListener(new CostumeChange(costumeCheckBox1, "Costume4.png"));
		
		
		stage.clear();
		stage.addActor(readyCheckBox);
		stage.addActor(readyCheckBoxLabel);
		stage.addActor(costumeCheckBoxLabel);
		stage.addActor(costumeCheckBox1);
		stage.addActor(costumeCheckBox2);
		stage.addActor(costumeCheckBox3);
		stage.addActor(costumeCheckBox4);
		
		lobbyTable = new Table();
		lobbyTable.debugAll();
		lobbyTable.setFillParent(true);
		lobbyTable.setSize(200, 300);
		lobbyTable.center();
		stage.addActor(lobbyTable);
		addPlayerToLobbyStage(localPlayer);
		lobbyTable.row();
		
		addChatboxToStage();
		
		inLobbyMessageTextFieldListener  = new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER) {
					JSONObject message = new JSONObject();
					message.put("type", "chatMessage");
					message.put("message", messageTextField.getText());
					out.println(message);
					addMessageToChatbox(localPlayer.username + ": " + messageTextField.getText());
					messageTextField.setText("");
					return true; //dont pass along the event
				}
				return false; //pass along the event
			}
		};
		
		messageTextField.addListener(inLobbyMessageTextFieldListener);
	}
	
	private void addChatboxToStage() {
		chatMessagesVGroup = new VerticalGroup();
		chatMessagesVGroup.debugAll();
		chatMessagesVGroup.setPosition(5, 35);
		chatMessagesVGroup.setSize(400, CHAT_BOX_HEIGHT);
		chatMessagesVGroup.left();
		chatMessagesVGroup.reverse();
		stage.addActor(chatMessagesVGroup);
		
		
		messageTextField = new TextField("", skin);
		messageTextField.setSize(400, 30);

		
		messageTextField.setPosition(5, 5);
		stage.addActor(messageTextField);
	}
	
	private void addMessageToChatbox(String message) {
		Label messageLabel = new Label(message, skin);
		while (messageLabel.getPrefWidth() > chatMessagesVGroup.getWidth()) {
			message = message.substring(0, message.length() - 1);
			messageLabel.setText(message);
		}
		chatMessagesVGroup.addActorAt(0, messageLabel);
		System.out.println("added: " + messageLabel);
		numChatLines++;
		BitmapFont font = messageLabel.getStyle().font;
		int maxNumChatLines = (int) (CHAT_BOX_HEIGHT / (font.getCapHeight() + font.getAscent() + -font.getDescent()));
		if (maxNumChatLines == numChatLines) {
			numChatLines -= 1;
			chatMessagesVGroup.getChildren().get(chatMessagesVGroup.getChildren().size - 1).remove(); //get rid of top chat line
		}
	}
	
	private void setEnabledAndHighlight(Button button, boolean enabled) {
		Button.ButtonStyle buttonStyle = button.getStyle();
		if (enabled) { //highlight connect button
			buttonStyle.up = skin.newDrawable("white", Color.LIGHT_GRAY);
			buttonStyle.down = skin.newDrawable("white", Color.LIGHT_GRAY);
			buttonStyle.checked = skin.newDrawable("white", Color.LIGHT_GRAY);
			buttonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
		} else {
			buttonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
			buttonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
			buttonStyle.checked = skin.newDrawable("white", Color.DARK_GRAY);
			buttonStyle.over = skin.newDrawable("white", Color.DARK_GRAY);
		}
		button.setDisabled(!enabled);
	}
	
	
	//attempts to connect to server, returns true for success
	private boolean connectToServer(String serverAddress, int port, String username) {
		try {
			socket = new Socket(serverAddress, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			
			
			localPlayer = new LocalPlayer(playerShape, false);
			localPlayer.username = username;
			setupForInGame();
			
			
			
			//send server player info, such as username
			JSONObject outObj = new JSONObject();
			outObj.put("type", "playerInfo");
			outObj.put("username", localPlayer.username);
			out.println(outObj);
			return true;
			
		} catch (Exception e) {
			errorTextField = new TextField("could not connect", skin);
			errorTextField.setAlignment(Align.center);
			displayConnectError(errorTextField);
			e.printStackTrace();
			return false;
		}
	}
	
	private void displayConnectError(TextField error) {
		error.setDisabled(true); //so it can't be edited
		mainMenuTable.addActorAt(2, error);
		//error.debug();
	}
	
	/**
	 * create player and map
	 */
	private void setupForInGame() {
		batch = new SpriteBatch();
		
		
		Scanner sc = new Scanner(System.in);
        System.out.println("Which map would you like to test?");
        String mapName = "1Square";//sc.nextLine();
        
        sc.close();
        
		try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            currentMap = new GameMap("../core/assets/" + mapName +".txt", "../core/assets/Tiles.txt", localPlayer);
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to create map object");
        }
		
		//player.create(); responsibilities for create() moved to constructor
		localPlayer.setFOV(localPlayer.sightX, localPlayer.sightY);
		
		//initialize various GuiManagers, giving them appropriate GuiElements
		mapGuiManager = new GuiManager();
		mainMenuGuiManager = new GuiManager();
		GuiManager.setCurrentManager(mapGuiManager);
		itemListExists = false;
		
		
		time = System.currentTimeMillis();
	}

	@Override
	public void render()
	{
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (GameState.GAME_STARTED == gameState) {
			batch.begin();
			
			currentMap.draw(batch);
			currentMap.update(batch);
			
			
			batch.end();
			
			for (Player player: currentMap.players) {
				if (player != currentMap.player) { //currentMap.player = this.player btw
					//adjust label position for remote players
					float xOffset = player.getWidth() / 2 - ((RemotePlayer) player).nameLabel.getWidth() / 2;
					System.out.println(xOffset);
					((RemotePlayer) player).nameLabel.setPosition((float) (player.getPos().getX() - currentMap.mapPosX) + xOffset, (float) (player.getPos().getY() - currentMap.mapPosY) - 18);
				}
			}
		}
		
		stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		stage.draw();
		
		doNetworking();
	}
	
	private void doNetworking() {
			//*******Networking*****
			if (GameState.MAIN_MENU != gameState) { 
				try {
					if (in.ready()) {
						//spin until receive message from server to start game (signaling that other client has connected, etc)
						if (GameState.IN_LOBBY == gameState) {
							System.out.println("ready");
							
							String receivedStr = in.readLine();
							//System.out.println("receivedStr: " + receivedStr);
							JSONObject received = (JSONObject) JSONValue.parse(receivedStr);
							//System.out.println("received: " + received);
							
							if (received.get("type").equals("gameStartSignal")) {
								gameState = GameState.GAME_STARTED;
								stage.clear();
								addInGameActors();
								
							} else if (received.get("type").equals("playerInfo")) {
								String playerName = (String) received.get("username");
								//System.out.println("playername: " + playerName);
								RemotePlayer remotePlayer = addRemotePlayerToList(playerName, ((Number) received.get("uid")).intValue());
								//System.out.println("remotePlayer info received: " + remotePlayer == null);
								addPlayerToLobbyStage(remotePlayer);
								lobbyTable.row();
								
							} else if (received.get("type").equals("readyStatus")) {
								int uid = ((Number) received.get("uid")).intValue();
								boolean isReady = (Boolean) received.get("readyStatus");
								for (Player player: playerToCheckBoxMap.keySet()) {
									if (player.uid == uid) {
										playerToCheckBoxMap.get(player).setChecked(isReady);
									}
								}
								
							} else if (received.get("type").equals("uidUpdate")) {
								localPlayer.uid = ((Number) received.get("uid")).intValue();
								
							} else if (received.get("type").equals("chatMessage")) {
								String message = (String) received.get("message");
								addMessageToChatbox(message);
							}
							
						} else if (GameState.GAME_STARTED == gameState) { //handle messages that come during game play, after the game has started
			        		String inputLine = in.readLine();
			        		JSONObject received = (JSONObject) JSONValue.parse(inputLine);
			        		String messageType = (String) received.get("type");
			        		//position updates
			        		if (messageType.equals("position")) {
				        		double otherPlayerX = ((Number) received.get("charX")).floatValue();
				        		double otherPlayerY = ((Number) received.get("charY")).floatValue();
				        		int uid = ((Number) received.get("uid")).intValue();
				        		currentMap.getPlayerByUid(uid).setPos(new Point(otherPlayerX, otherPlayerY));
				        		
			        		} else if (messageType.equals("animation")) { //animation updates
			        			int uid = ((Number) received.get("uid")).intValue();
			        			((RemotePlayer) currentMap.getPlayerByUid(uid)).setAnimation((String) received.get("animationName"));
			        		}
			                
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//sending messagse to server
				//TODO maybe need a more advanced queue so we arent sending every message type at the same time
				// maybe not, maybe tcp already handles queues of messages pretty well
				if (GameState.GAME_STARTED == gameState) {
			    	JSONObject obj = new JSONObject();
			        if (System.currentTimeMillis() - time >= SEND_SPACING) {
			        	//sending position
			        	obj.clear();
			        	if (!localPlayer.getPos().equals(oldPos)) { //don't send unnecessary updates
				        	float charX = (float) localPlayer.getPos().getX();
				        	float charY = (float) localPlayer.getPos().getY();
				        	obj.put("type", "position"); //let server know that this message specifies a position update
				            obj.put("charX", charX);
				            obj.put("charY", charY);
				            obj.put("uid", localPlayer.uid);
				        	out.println(obj.toString());
				        	oldPos = localPlayer.getPos();
			        	}
			        	//sending direction
			        	//note -- if not moving, all of these bools will be false
			        	if (localPlayer.direction != playerOldDirection) {
			        		System.out.println(localPlayer.direction.toString());
				        	obj.clear();
				        	obj.put("type", "direction");
				        	obj.put("direction", localPlayer.direction.toString());
				        	out.println(obj.toString());
			        	}
			        	playerOldDirection = localPlayer.direction;
			        	
			        	//update time
			        	time = System.currentTimeMillis();
			        }
				}
			}
				//**end networking******
	}
				
				
	public void addInGameActors() {
		addChatboxToStage();
		messageTextField.setVisible(false);
		messageTextField.removeListener(inLobbyMessageTextFieldListener);
		inGameMessageTextFieldListener = new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER) {
					if (messageTextField.isVisible()) { 
						//send the text as a message
						JSONObject message = new JSONObject();
						message.put("type", "chatMessage");
						message.put("message", messageTextField.getText());
						out.println(message);
						addMessageToChatbox(localPlayer.username + ": " + messageTextField.getText());
						messageTextField.setText("");
						messageTextField.setVisible(false); //close the text field
						messageTextField.setDisabled(true);
						event.setBubbles(false); //stop the event from bubbling back up to the stage, which will handle ENTER again (we only want ENTER to be handled once)
					}
					return true; // the event is "handled" -- no propogation outside of this stage
				}
				return false; // the event is not "handled" -- propogates outside of stage
			}
		};
		messageTextField.addListener(inGameMessageTextFieldListener);
/*		messageTextField.setTextFieldFilter(new TextFieldFilter() {
			@Override
			public boolean acceptChar(TextField textField, char c) {
				return Input.Keys.ENTER == c || messageTextField.isVisible(); //accept an input only if messageTextField is visible or if the input character is ENTER key (used to open/close the field)
			}
			
		});*/
		
		stage.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == Input.Keys.ENTER && !messageTextField.isVisible()) {
					messageTextField.setVisible(true); //open up text field for message entry
					messageTextField.setDisabled(false);
					stage.setKeyboardFocus(messageTextField);
					localPlayer.directionStack.clear();
					return true;
				}
				return false;
			}
		});
		
		
		for (Player player: currentMap.players) {
			if (player != this.localPlayer) {
				labelStyle = new LabelStyle();
				labelStyle.font = skin.getFont("default");
				labelStyle.fontColor = Color.WHITE;
				Label playerNameLabel = new Label(player.username, labelStyle);
				//System.out.println("position: " + player.getXPos());
				playerNameLabel.setPosition((float) player.getXPos(), (float) player.getYPos());
				((RemotePlayer) player).nameLabel = playerNameLabel;
				stage.addActor(playerNameLabel);
			}
		}
		
		gameInputProcessor = new GameInputProcessor(localPlayer);
		inputMultiplexer.addProcessor(gameInputProcessor);
	}
	
	private RemotePlayer addRemotePlayerToList(String playerName, int uid) {
		RemotePlayer remotePlayer = new RemotePlayer(playerShape, true);
		remotePlayer.uid = uid;
		remotePlayer.username = playerName;
		currentMap.players.add(remotePlayer);
		remotePlayer.setPos(new Point(-100, -100));
		
		return remotePlayer;
	}
	/** add player's info to lobby page**/
	private void addPlayerToLobbyStage(Player player) {
		Label playerNameLabel = new Label(player.username, labelStyle);
		final CheckBox readyCheckBox = new CheckBox("", skin);
		readyCheckBox.setDisabled(true);
		playerToCheckBoxMap.put(player, readyCheckBox);
		lobbyTable.add(playerNameLabel).padTop(15).padRight(20);
		lobbyTable.add(readyCheckBox);
		System.out.println("added player to lobby stage: " + player.username);
	}
	
	/*
	public void keyListening() {
		
		if (GuiManager.currentManager.equals(mapGuiManager)) 
		{
			if (Gdx.input.isKeyJustPressed(Keys.G)) {
				if (!itemListExists && !currentMap.getNearbyItemList().isEmpty()) {
					ItemCollector items = currentMap.getNearbyItemList();
					GuiItemList guiItemList = new GuiItemList(currentMap.player, 0);
					guiItemList.setItemList(items);
					
					GuiManager.currentManager.addElement(guiItemList);
					GuiManager.currentManager.setFocused(guiItemList);
					itemListExists = true;
					player.setCanMove(false);
					GuiManager.currentManager.listen();
					
					guiItemList.watchedList = currentMap.itemsOnField;
				
					
					
				} else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
					player.setCanMove(false);
					TextInputProcessor chatInputProcessor = new TextInputProcessor();
					Gdx.input.setInputProcessor(chatInputProcessor);
				} else {
					GuiManager.currentManager.clearElements();
					itemListExists = false;
					player.setCanMove(true);
				}
			}
			if (Gdx.input.isKeyJustPressed(Keys.C))
			{	
				GuiManager.currentManager = mainMenuGuiManager;
			}
		}
		else if (GuiManager.currentManager.equals(mainMenuGuiManager))
			{
				if (Gdx.input.isKeyJustPressed(Keys.C))
				{	
					GuiManager.currentManager = mapGuiManager;
				}
				
			}
		
		} else if (currentManager.equals(mainMenuGuiManager)) {
			some other behavior
		} else if (currentManager.equals(someOtherGuiManager)) {
			some other behavior
		}
		.
		.
		.
	}*/
}
