package object;

import java.util.List;

public class objNode extends objAttributeCommon {
	public int coordX;
	public int coordY;
	public char objContent;
	public List<objNode> neighborListNode;
	
	/* Constructor for a Node*/
	public objNode(int coordX, int coordY, char content){
		this.coordX = coordX;
		this.coordY = coordY;
		this.objContent = content;
	}
	
	public objNode(){};
	
	/* object classification */
	public objNode objectClassification(int coordX, int coordY, char symbol, objNode object){
		switch(symbol){
			case WALL_CONTENT:
				object = new objWall(coordX, coordY, symbol);
				break;
			case ROAD_CONTENT:
				object = new objRoad(coordX, coordY, symbol);
				break;
			case FOOD_CONTENT:
				object = new objFood(coordX, coordY, symbol);
				break;
			case PACMAN_CONTENT:
				object = new objPacman(coordX, coordY, symbol);
				break;
			case GHOST_CONTENT:
				object = new objGhost(coordX, coordY, symbol);
				break;
		}
		return object;
	}
	
}
