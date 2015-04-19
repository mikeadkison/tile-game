////////////////////////////////////////////////////////////////////////////////
//  File:     GuiItemList.java
//  
//  Name:     Bhavishya Shah
//  Email:    bhshah1@my.waketech.edu
////////////////////////////////////////////////////////////////////////////////
package com.mygdx.game;

import java.util.ArrayList;





import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
/**
 * (Insert a comment that briefly describes the purpose of this class definition.)
 *
 * <p/> Bugs: (List any known issues or unimplemented features here)
 * 
 * @author (Bhavishya Shah)
 *
 */
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GuiItemList extends GuiElement
{
    ArrayList<Item> itemList;
    private int numberOfItems = 9;
    private BitmapFont font;
    Player player;
    int selectedIndex;
    
    String arrowFileURI;
    Texture arrowTexture;
    
    public GuiItemList(Player player)
    {
    	this.player = player;
    	posX = player.drawPosX;
    	
    	if (player.drawPosY < 500)
    	{	
    		posY = player.drawPosY + player.up + 50;
    	}
    	else
    	{
    		posY = player.drawPosY - 100;
    	}
    	
    	font = new BitmapFont();
    	arrowFileURI = "itemlist_arrow.png";
    	arrowTexture = new Texture(Gdx.files.internal("itemlist_arrow.png"));
    }
    
    public void setItemList(ArrayList<Item> list)
    {
        itemList = new ArrayList<Item>(list);
    }
    
    @Override
    public void update()
    {
    	if (listeningForInput) {
    		if (Gdx.input.isKeyJustPressed(Keys.DOWN) && selectedIndex < itemList.size() - 1) {
    			selectedIndex++;
    		} else {
    			if (Gdx.input.isKeyJustPressed(Keys.UP) && selectedIndex > 0) {
	    			selectedIndex--;
    			}
    		}
    	}
    }
    
    
    public void displayItems(SpriteBatch batch)
    {
        String tempItemList = "";
        for (int x = 0; x < numberOfItems && x < itemList.size(); x++)
        {
            tempItemList += itemList.get(x).toString() + "\n";
        }
        font.drawMultiLine(batch, tempItemList, posX, posY);
    }
    
    @Override
    public void draw(SpriteBatch batch) {
        displayItems(batch);
        drawArrow(batch);
    }
    
    public void drawArrow(SpriteBatch batch) {
    	batch.draw(arrowTexture, posX - arrowTexture.getWidth() - 10, posY - selectedIndex * font.getLineHeight() - arrowTexture.getHeight());
    }
    
    public void giveInput(int key) {
    	
    }
}
