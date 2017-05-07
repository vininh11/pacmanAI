package object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import interfaceUser.readMapFile;

public class objMove extends objNode {
	//public static boolean synchronized 
	
	/* Constructor */
	public objMove(int coordX, int coordY, char content){
		super(coordX, coordY, content);
	}
	public objMove(){}

	/* Calculate distance from Node A to Node B */
	public double calcDistaneAToB(objNode A, objNode B){
		double distance = Math.sqrt(Math.pow(A.coordX - B.coordX,2) + Math.pow(A.coordY - B.coordY,2) );
		return distance;
	}
	
	/* Convert object at Node :  8 status
	 * 1: Road -> Ghost, Food -> Ghost
	 * 2: Ghost -> Road, Pacman -> Road
	 * 3: Road -> Pacman, Food -> Pacman
	 * 4: Ghost -> Food
	 * */
	public List<objNode> gotoAB(objNode A, objNode B, int status, objNode objPrev){
		
		/*list return */
		List<objNode> listResult = new ArrayList<objNode>();
		/* find index in map List */
		int indexA = mapList.indexOf(A);
		int indexB = mapList.indexOf(B);
		
		/* time move */
		try{
			TimeUnit.MILLISECONDS.sleep(SPEED_MOVE);
		} catch( InterruptedException  e){
			System.out.println(e);
		}
		/* move method */
		switch(status){
			case 1:			//1: Ghost -> Road	
				if( searchInFoodList(foodList,objPrev)){			// case: 1:Ghost->Food->Road - 2:Food->Ghost->Road - 3: Food->Food->Ghost
					A = new objFood(A.coordX, A.coordY, FOOD_CONTENT);
					objPrev = B;
					B = new objGhost(B.coordX, B.coordY, GHOST_CONTENT);
				} else {											// case: 1:Ghost->Road->Road - 2:Road->Ghost->Road - 3: Road->Road->Ghost
					A = new objRoad(A.coordX, A.coordY, ROAD_CONTENT);
					objPrev = B;
					B = new objGhost(B.coordX, B.coordY, GHOST_CONTENT);
				}
				mapList.set(indexA, A);
				mapList.set(indexB, B);
				listResult.add(A);
				listResult.add(B);
				listResult.add(objPrev);
				break;
			case 2:			//1: Pacman -> Road
				A = new objRoad(A.coordX, A.coordY, ROAD_CONTENT);
				B = new objPacman(B.coordX, B.coordY, PACMAN_CONTENT);
				mapList.set(indexA, A);
				mapList.set(indexB, B);
				listResult.add(A);
				listResult.add(B);
				break;
			case 3:			//2: Ghost -> Food
				if( searchInFoodList(foodList,objPrev)){			// case: 1:Ghost->Food->Food - 2:Food->Ghost->Food - 3: Food->Food->Ghost
					A = new objFood(A.coordX, A.coordY, FOOD_CONTENT);
					objPrev = B;
					B = new objGhost(B.coordX, B.coordY, GHOST_CONTENT);
				} else {											// case: 1:Ghost->Road->Food - 2:Road->Ghost->Food - 3: Food->Road->Ghost
					A = new objRoad(A.coordX, A.coordY, ROAD_CONTENT);
					objPrev = B;
					B = new objGhost(B.coordX, B.coordY, GHOST_CONTENT);
				}
				mapList.set(indexA, A);
				mapList.set(indexB, B);
				listResult.add(A);
				listResult.add(B);
				listResult.add(objPrev);
				break;
			case 4:			//4: Pacman -> Food
				A = new objRoad(A.coordX, A.coordY, ROAD_CONTENT);
				B = new objPacman(B.coordX, B.coordY, PACMAN_CONTENT);
				mapList.set(indexA, A);
				mapList.set(indexB, B);
				listResult.add(A);
				listResult.add(B);
				break;
			case 5:			//5: Ghost -> Ghost
				 if( searchInFoodList(foodList,objPrev)){			// case: 1:Ghost->Food->Food - 2:Food->Ghost->Food - 3: Food->Food->Ghost
					A = new objFood(A.coordX, A.coordY, FOOD_CONTENT);
					objPrev = B;
					B = new objGhost(B.coordX, B.coordY, GHOST_CONTENT);
				} else {											// case: 1:Ghost->Road->Food - 2:Road->Ghost->Food - 3: Food->Road->Ghost
					A = new objRoad(A.coordX, A.coordY, ROAD_CONTENT);
					objPrev = B;
					B = new objGhost(B.coordX, B.coordY, GHOST_CONTENT);
				}
				mapList.set(indexA, A);
				mapList.set(indexB, B);
				listResult.add(A);
				listResult.add(B);
				listResult.add(objPrev);
				break;
		}
		return listResult;
	}
	
	/* calculate status: go to A -> B, B is A.neighbor */
	public int calcStatus( objNode A, objNode B){
		int result = 0;
		if (A.objContent == GHOST_CONTENT && B.objContent == ROAD_CONTENT)
			result = 1;   //1: Ghost -> Road 
		else if (A.objContent == PACMAN_CONTENT && B.objContent == ROAD_CONTENT)
			result = 2;		//2:  Pacman -> Road
		else if (A.objContent == GHOST_CONTENT && B.objContent == FOOD_CONTENT)
			result = 3;  //3: Ghost -> Food
		else if (A.objContent == PACMAN_CONTENT && B.objContent == FOOD_CONTENT)
			result = 4;  //4: Pacman -> Food
		else if (A.objContent == GHOST_CONTENT && B.objContent == GHOST_CONTENT)
			result = 5;  //5: GHOST -> Ghost
		else if ((A.objContent == PACMAN_CONTENT && B.objContent == GHOST_CONTENT) 
				|| (A.objContent == GHOST_CONTENT && B.objContent == PACMAN_CONTENT))
			result = 6;	//6: ghost -> pacman or pacman -> ghost
		;
		return result;	
	}

	/* Find neighbor of a node 
	 * Index 0: above neighbor
	 * Index 1: bottom neighbor
	 * Index 2: left neighbor
	 * Index 3: right neighbor
	 * */
	public List<objNode> findNeighbor(objNode object){
		object.neighborListNode = new ArrayList<objNode>();
		int indexOfOject;
		int indexOfTempObject;
		
		// find above neighbor
		if(object.coordY != 100){
			indexOfOject = mapList.indexOf(object);
			indexOfTempObject = indexOfOject - WIDTH_MAP;
			object.neighborListNode.add(mapList.get(indexOfTempObject));
		}
		
		// find bottom neighbor
		if(object.coordY != (100 - HEIGHT_MAP - 1) ){
			indexOfOject = mapList.indexOf(object);
			indexOfTempObject = indexOfOject + WIDTH_MAP;
			object.neighborListNode.add(mapList.get(indexOfTempObject));
		}

		// find left neighbor
		if(object.coordX == 100 && object.coordY == 90){
			indexOfOject = mapList.indexOf(object);
			indexOfTempObject = indexOfOject + WIDTH_MAP - 1;
			object.neighborListNode.add(mapList.get(indexOfTempObject));
		}
		else if(object.coordX != (100) ){
			indexOfOject = mapList.indexOf(object);
			indexOfTempObject = indexOfOject - 1;
			object.neighborListNode.add(mapList.get(indexOfTempObject));
		}

		// find right neighbor
		if(object.coordX == (100 + WIDTH_MAP - 1) && object.coordY == 90){
			indexOfOject = mapList.indexOf(object);
			indexOfTempObject = indexOfOject - WIDTH_MAP + 1;
			object.neighborListNode.add(mapList.get(indexOfTempObject));
		}
		else if(object.coordX != (100 + WIDTH_MAP - 1) ){
			indexOfOject = mapList.indexOf(object);
			indexOfTempObject = indexOfOject + 1;
			object.neighborListNode.add(mapList.get(indexOfTempObject));
		}
		return object.neighborListNode;
	}
	
	/* search coordX, coordY in Food List*/
	public boolean searchInFoodList(List<objFood> listName, objNode object){
		for(int i = 0; i < listName.size(); i++){
			if(listName.get(i).coordX == object.coordX && listName.get(i).coordY == object.coordY)
				return true;
		}
		return false;
	}
	
	/* search coordX, coordY in Map List*/
	public objNode searchInMapList(List<objNode> listName, objNode object){
		for(int i = 0; i < listName.size(); i++){
			if(listName.get(i).coordX == object.coordX && listName.get(i).coordY == object.coordY)
				return listName.get(i);
		}
		return object;
	}
	
	/* search coordX, coordY in Map List*/
	public objNode searchInMapListByCoord(int coordX, int coordY){
		objNode object = new objNode();
		for(int i = 0; i < mapList.size(); i++){
			if(mapList.get(i).coordX == coordX && mapList.get(i).coordY == coordY)
				object = mapList.get(i);
		}
		return object;
	}
}
