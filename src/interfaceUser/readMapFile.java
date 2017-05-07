package interfaceUser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import object.objAttributeCommon;
import object.objFood;
import object.objGhost;
import object.objNode;

public class readMapFile extends objAttributeCommon {

	/* Read file map */
	public List<objNode> readFile(){
		int coordX = 100;
		int coordY = 100;
		int numLine = 0;
		try {
			/* Read file link data*/
			BufferedReader reader = new BufferedReader(new InputStreamReader(
				        new FileInputStream("E:/HUST/HUST3/Artificial Intelligence/Pacman/DB/map1.txt"),
				        Charset.forName("UTF-8")));
				int c;
				while((c = reader.read()) != -1) {
					char character = (char) c;
					//System.out.print(character);
					objNode object = new objNode(coordX, coordY, character);
					if(character == WALL_CONTENT || character == ROAD_CONTENT || character == FOOD_CONTENT || character == PACMAN_CONTENT || character == GHOST_CONTENT){
						numLine = numLine + 1;
						mapList.add(object.objectClassification(coordX, coordY, character, object));		// add object to map list
						// add object to food list
						if(character == FOOD_CONTENT){
							objFood objectFood = new objFood(coordX, coordY, character);
							foodList.add(objectFood);
						}
						// add object to Ghost list
						if(character == GHOST_CONTENT){
							objGhost objectGhost = new objGhost(coordX, coordY, character);
							ghostList.add(objectGhost);
						}
						if(numLine < WIDTH_MAP){
							coordX = coordX + 1;
						}else{
							numLine = 0;
							coordX = 100;
							coordY = coordY - 1;
						}
					}
				}
			reader.close();
		}
		catch(IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapList;
	}
	
	/* Print list Map */
	public void printListMap(List<objNode> object){
		int countLine = 1;
		int size = object.size();
		for(int i = 0; i<size; i++){
			if(countLine < WIDTH_MAP){
				System.out.print(object.get(i).objContent);
				countLine ++ ;
			} else{
				countLine = 1;
				System.out.println(object.get(i).objContent);
			}
		}
	}
	
	
	
}
