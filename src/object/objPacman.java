package object;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import interfaceUser.readMapFile;

public class objPacman extends objMove implements Runnable{
	private boolean dangerFlg = false;
	private int changeDestinationFlg = 0;
	private double []distanceToGhost = new double[foodList.size()];
	private objGhost nearestGhost = new objGhost();
	private boolean escapeFlg = false;

	/* Constructor */
	public objPacman(int coordX, int coordY, char content){
		super(coordX, coordY, content);
	}
	public objPacman(){};
	
	/* pacman run */
	public void run(){
		pacmanMoveAStar();
	}
	
	/* Pacman A star */
	public synchronized void pacmanMoveAStar(){
		
		objNode initNode = this;				// init node
		objNode nextNode = new objNode();		// next node pacman will go
		objNode initNodeFirst = new objNode();		// save initNode
		int countInitNodeSameDestinationNode = 0;
		objNode nextDestinationNode = new objNode();	// next destination node pacman will go
		int status;
		List<objNode> listMove = new ArrayList<objNode>();
		objNode objPrev = new objNode();
		double distFromDestinationNode;				// distance from initNode to destinationNode
		objNode objNotFood = new objNode();
		
		while(foodList.size() != 0){
			
			/* Dong bo cac doi tuong tren ban do */
			objNode tempNode = searchInMapList(mapList, initNode);
			if(tempNode.objContent != PACMAN_CONTENT){
				initNode = new objPacman(tempNode.coordX, tempNode.coordY, PACMAN_CONTENT);
				mapList.set(mapList.indexOf(tempNode), initNode);
			}else{
				initNode = tempNode;
			}
			
			/* Find Path */
			initNode.neighborListNode = this.findNeighbor(initNode);		// find neighbor of Node
			
			// calculate next destination for Pacman
			if(minDistanceToGhost(initNode) >= SAFE_DISTANCE2){
				this.dangerFlg = false;
			}
			// Call escape Node
//			if(cntNeighborAreWall(initNode) >= 3){
//				this.escapeFlg = true;
//				nextDestinationNode =  escapeDeadNode(initNode);
//				objNotFood = nextDestinationNode;
//			}
			if(objNotFood == initNode){
				this.escapeFlg = false;
				objNotFood = null;
			}
			// If pacman escape complete
//			if(this.escapeFlg){
//				nextDestinationNode = objNotFood;
//				if(initNode == objNotFood){
//					this.escapeFlg = true;
//				}
//			}
			if( this.dangerFlg == false && this.escapeFlg == false){
//				if(this.escapeFlg == false){
				if( countInitNodeSameDestinationNode >= 3){
					nextDestinationNode = calcNextDestinationNodeNotFood(initNode, nextDestinationNode);			// find next destination different food node
					countInitNodeSameDestinationNode = 0;
				}
				else{
					nextDestinationNode = calcNextDestinationNode(initNode);				// find next destination food node
				}
//				}
			}
			/*else if( this.dangerFlg && countInitNodeSameDestinationNode >= 3){
				
				nextDestinationNode = calcNextDestinationNodeNotFood(initNode, nextDestinationNode);			// find next destination different food node
				countInitNodeSameDestinationNode = 0;
			} else*/ 
			if(this.dangerFlg){
				/* In case Ghost is too close */
				if(sensor(initNode)){
					nextDestinationNode = avoidGhost(initNode, this.nearestGhost);
				}
			}
			
			distFromDestinationNode = calcDistaneAToB(initNode, nextDestinationNode);	// distance from initNode to destinationNode
			
			int sizeOfFoodList = foodList.size();
			if( sizeOfFoodList != 0){
				nextNode = nextNode(initNode, nextDestinationNode, sizeOfFoodList, distFromDestinationNode);		// find next node to go
			}
			
			if( nextNode.coordX == initNodeFirst.coordX && nextNode.coordY == initNodeFirst.coordY)
				countInitNodeSameDestinationNode ++;
			
			status = this.calcStatus(initNode, nextNode);
			if(status == 6)
				break;
			listMove = this.gotoAB(initNode, nextNode, status, objPrev);
			if(nextNode.objContent == FOOD_CONTENT){ 
				this.eatFood(nextNode);
			}
			initNodeFirst = initNode;
			initNode = listMove.get(1);
			
			readMapFile readFile = new readMapFile();
			for(int j = 0; j < 10; j++)
				System.out.println("\n");
			readFile.printListMap(mapList); 				// display to screen
		}
	}
	
	/* heuristic for Pacman*/
	public objNode nextNode(objNode currentNode,objNode destinationNode, int sizeOfListFood, double distFromDestinationNode){
		objNode nextNode = new objNode();
		double distance;

		// choose in neighbor node of current node
		distance = 1000;
		double []tempDistance = new double[4];		// array of disatance from four neighbors of currentNode to destinationNode
		
		for(int i = 0; i < currentNode.neighborListNode.size(); i++){
			if(currentNode.neighborListNode.get(i).objContent != WALL_CONTENT && sizeOfListFood != 0){
				tempDistance[i] = calcDistaneAToB(currentNode.neighborListNode.get(i), destinationNode);
				if(distance > tempDistance[i]){
					distance = tempDistance[i];
					nextNode = currentNode.neighborListNode.get(i);
				}
			}
		}
		
		int resultOfEstimate;
		if ( tempDistance[0] == tempDistance[1] && distance == tempDistance[0] && distance != 0){
			
			resultOfEstimate = estimateAbilityOfNode(currentNode.neighborListNode.get(0), currentNode.neighborListNode.get(1)
																			,currentNode,currentNode, destinationNode);
			return convertObject(resultOfEstimate, currentNode.neighborListNode.get(0), currentNode.neighborListNode.get(1));
		
		}
		else if ( tempDistance[2] == tempDistance[3] && distance == tempDistance[2] && distance != 0 ){
			
			resultOfEstimate = estimateAbilityOfNode(currentNode.neighborListNode.get(2), currentNode.neighborListNode.get(3)
																			,currentNode,currentNode, destinationNode);
			return convertObject(resultOfEstimate, currentNode.neighborListNode.get(2), currentNode.neighborListNode.get(3));
		
		}
		else if (tempDistance[2]== tempDistance[3] && tempDistance[2] == 0 && distance != 0 && distance <= distFromDestinationNode){
			
			resultOfEstimate = estimateAbilityOfNodeSecond(currentNode.neighborListNode.get(0), currentNode.neighborListNode.get(1)
					,currentNode,currentNode, destinationNode);			
			return convertObject(resultOfEstimate, currentNode.neighborListNode.get(0), currentNode.neighborListNode.get(1));
		
		}else if (tempDistance[0]== tempDistance[1] && tempDistance[1] == 0 && distance != 0 && distance <= distFromDestinationNode){
			
			resultOfEstimate = estimateAbilityOfNodeSecond(currentNode.neighborListNode.get(2), currentNode.neighborListNode.get(3)
					,currentNode,currentNode, destinationNode);
			return convertObject(resultOfEstimate, currentNode.neighborListNode.get(2), currentNode.neighborListNode.get(3));
		}
		
		return nextNode;
	}
	
	/* min distance from ghost to pacman*/
	private double minDistanceToGhost(objNode pacman){
		double minDistance = 1000;
		double temp;
		for(int i = 0; i < ghostList.size(); i++){
			temp = calcDistaneAToB(pacman, ghostList.get(i));
			if( minDistance > temp){
				minDistance = temp;
				this.nearestGhost = ghostList.get(i);
			}
		}
		return minDistance;
	}
	
	/* Sensor to Ghosts 
	 * Return True when: Ghost is dangerous, enough close to Pacman
	 * return False when: Ghost is not dangerous, no enough close to Pacman
	 * */
	public boolean sensor(objNode pacman){
		if ( minDistanceToGhost(pacman) <= SAFE_DISTANCE ){
			this.dangerFlg = true;
			return true;
		}
		return false;
	}
	
	/* Find next destination node for Pacman
	 * Return next destination node for Pacman
	 *  */
	public objNode calcNextDestinationNode(objNode pacman){
		objNode nextDestinationNode = new objNode();
		double minDistance = 1000;
		
		/* In case Ghost is too close */
		if(sensor(pacman)){
			nextDestinationNode = avoidGhost(pacman, this.nearestGhost);
			return nextDestinationNode;
		}

		/*calculate distance from pacman to all food on the map*/
		int index = searchIndexInFoodList(foodList, pacman);
		if(index != -1){
			foodList.remove(index);
		}
		for(int i = 0; i < foodList.size(); i++){
			foodList.get(i).distanceToPacman = calcDistaneAToB(pacman, foodList.get(i));
			if( minDistance > foodList.get(i).distanceToPacman){
				minDistance = foodList.get(i).distanceToPacman;
				nextDestinationNode = foodList.get(i);
			}
		}
		
		return nextDestinationNode;
	}
	
	/* Find next destination node different a food node for Pacman 
	 * Return next destination node for Pacman
	 *  */
	public objNode avoidGhost(objNode pacman, objNode ghost){
		int rangeX;
		int rangeY;
		int rdCoordXOfNode;
		int rdCoordYOfNode;
		objNode objRandom = new objNode();
		Random rdCoordX = new Random();
		Random rdCoordY = new Random();
		this.changeDestinationFlg ++;

		if(this.changeDestinationFlg >= 5){
			rangeX = WIDTH_MAP;
			rdCoordXOfNode = 100 + rdCoordX.nextInt(rangeX);
			rangeY = HEIGHT_MAP;
			rdCoordYOfNode = 100 - rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			this.changeDestinationFlg = 0;
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(objRandom, this.nearestGhost);
			}
		}
		
		/* destination thuoc trai tren cua pacman */
		if ( ghost.coordX < pacman.coordX && ghost.coordY > pacman.coordY){
			rangeX = 100 + WIDTH_MAP - pacman.coordX;
			rdCoordXOfNode = pacman.coordX + rdCoordX.nextInt(rangeX);
			rangeY = pacman.coordY - HEIGHT_MAP;
			rdCoordYOfNode = HEIGHT_MAP + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, this.nearestGhost);
			}
		}
		
		/* destination thuoc phai tren cua pacman */
		else if ( ghost.coordX > pacman.coordX && ghost.coordY > pacman.coordY){
			rangeX = pacman.coordX - 100;
			rdCoordXOfNode = 100 + rdCoordX.nextInt(rangeX);
			rangeY = pacman.coordY - HEIGHT_MAP;
			rdCoordYOfNode = HEIGHT_MAP + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, this.nearestGhost);
			}
		}
		
		/* destination thuoc phai duoi cua pacman */
		else if ( ghost.coordX > pacman.coordX && ghost.coordY < pacman.coordY){
			rangeX =  pacman.coordX - 100;
			rdCoordXOfNode = 100 + rdCoordX.nextInt(rangeX);
			rangeY = 100 - pacman.coordY;
			rdCoordYOfNode = pacman.coordY + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, this.nearestGhost);
			}
		}

		/* destination thuoc trai duoi cua pacman */
		else if ( ghost.coordX < pacman.coordX && ghost.coordY < pacman.coordY){
			rangeX = 100 + WIDTH_MAP - pacman.coordX;
			rdCoordXOfNode = pacman.coordX + rdCoordX.nextInt(rangeX);
			rangeY = 100 - pacman.coordY;
			rdCoordYOfNode = pacman.coordY + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, this.nearestGhost);
			}
		}	else{
			rangeX = WIDTH_MAP;
			rdCoordXOfNode = 100 + rdCoordX.nextInt(rangeX);
			rangeY = HEIGHT_MAP;
			rdCoordYOfNode = 100 - rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			this.changeDestinationFlg = 0;
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(objRandom, this.nearestGhost);
			}
		}
	}
	
	/* Find next destination node different a food node for Pacman 
	 * Return next destination node for Pacman
	 *  */
	public objNode calcNextDestinationNodeNotFood(objNode pacman, objNode oldDestination){
		int rangeX;
		int rangeY;
		int rdCoordXOfNode;
		int rdCoordYOfNode;
		objNode objRandom = new objNode();
		Random rdCoordX = new Random();
		Random rdCoordY = new Random();
		this.changeDestinationFlg ++;
		
		/* avoid Ghost*/
		/* In case Ghost is too close */
		if(sensor(pacman)){
			objRandom = avoidGhost(pacman, this.nearestGhost);
			return objRandom;
		}
		
		if(oldDestination == null || this.changeDestinationFlg > 2){
			rangeX = WIDTH_MAP;
			rdCoordXOfNode = 100 + rdCoordX.nextInt(rangeX);
			rangeY = HEIGHT_MAP;
			rdCoordYOfNode = 100 - rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			this.changeDestinationFlg = 0;
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, oldDestination);
			}
		}
		
		/* destination thuoc trai tren cua pacman */
		if ( oldDestination.coordX < pacman.coordX && oldDestination.coordY > pacman.coordY){
			rangeX = pacman.coordX - oldDestination.coordX + 1;
			rdCoordXOfNode = oldDestination.coordX + rdCoordX.nextInt(rangeX);
			rangeY = oldDestination.coordY - pacman.coordY + 1;
			rdCoordYOfNode = pacman.coordY + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, oldDestination);
			}
		}
		
		/* destination thuoc phai tren cua pacman */
		if ( oldDestination.coordX > pacman.coordX && oldDestination.coordY > pacman.coordY){
			rangeX = oldDestination.coordX - pacman.coordX + 1;
			rdCoordXOfNode = pacman.coordX + rdCoordX.nextInt(rangeX);
			rangeY = oldDestination.coordY - pacman.coordY + 1;
			rdCoordYOfNode = pacman.coordY + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, oldDestination);
			}
		}
		
		/* destination thuoc phai duoi cua pacman */
		if ( oldDestination.coordX > pacman.coordX && oldDestination.coordY < pacman.coordY){
			rangeX = oldDestination.coordX - pacman.coordX + 1;
			rdCoordXOfNode = pacman.coordX + rdCoordX.nextInt(rangeX);
			rangeY =  pacman.coordY - oldDestination.coordY + 1;
			rdCoordYOfNode = oldDestination.coordY + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
				return objRandom;
			}else{
				return avoidGhost(pacman, oldDestination);
			}
		}

		/* destination thuoc trai duoi cua pacman */
		if ( oldDestination.coordX < pacman.coordX && oldDestination.coordY < pacman.coordY){
			rangeX =  pacman.coordX - oldDestination.coordX + 1;
			rdCoordXOfNode = oldDestination.coordX + rdCoordX.nextInt(rangeX);
			rangeY =  pacman.coordY - oldDestination.coordY + 1;
			rdCoordYOfNode  = oldDestination.coordY + rdCoordY.nextInt(rangeY);
			objRandom = searchInMapListByCoord(rdCoordXOfNode, rdCoordYOfNode);
			if(objRandom.objContent == ROAD_CONTENT || objRandom.objContent == FOOD_CONTENT){
			    return	objRandom;
			}else{
				return avoidGhost(pacman, oldDestination);
			}
		}
		
		return objRandom;
	}
	
	/* Count neighbors are Wall of Pacman*/
	public int cntNeighborAreWall(objNode pacman){
		int countWall = 0;
		for(int i = 0; i < pacman.neighborListNode.size(); i ++ ){
			if( pacman.neighborListNode.get(i).objContent == WALL_CONTENT ){
				countWall ++;
			}
		}
		return countWall;
	}
	
	/* Calculate for Pacman escape a Dead Node */
//	public objNode escapeDeadNode(objNode pacman){
//		objNode destinationNode = new objNode();
//		Random rd = new Random();
//		int rdCoordX;
//		int rdCoordY;
//		int cntObjNeighborIsNotWall = 0;
//
//		// chon muc tieu cho pacman la duong di nguoc lai voi diem chet.
////		if( pacman.neighborListNode.get(0).objContent != WALL_CONTENT){
//			do{
//				if(destinationNode != null){
//					rdCoordY =  pacman.coordY + rd.nextInt(100 - pacman.coordY) - 1;
//					destinationNode = this.searchInMapListByCoord(pacman.coordX, rdCoordY);
//					destinationNode.neighborListNode = this.findNeighbor(destinationNode);
//					cntObjNeighborIsNotWall = numOfNeighborDiffWall(destinationNode, destinationNode.neighborListNode.get(1));
//					if( cntObjNeighborIsNotWall >= 2 || destinationNode.neighborListNode.get(0).objContent == WALL_CONTENT ){
//						if(destinationNode.neighborListNode.get(2).objContent == WALL_CONTENT){
//							return destinationNode.neighborListNode.get(2);
//						} else{
//							return destinationNode.neighborListNode.get(3);
//						}
//					}
//				}
//			} while(!(cntObjNeighborIsNotWall >= 2 || destinationNode.neighborListNode.get(2).objContent == WALL_CONTENT));
//		} else if( pacman.neighborListNode.get(1).objContent != WALL_CONTENT){
//			do{
//				if(destinationNode != null){
//					rdCoordY = pacman.coordY - rd.nextInt(pacman.coordY - HEIGHT_MAP) + 1;
//					destinationNode = this.searchInMapListByCoord(pacman.coordX, rdCoordY);
//					destinationNode.neighborListNode = this.findNeighbor(destinationNode);
//					cntObjNeighborIsNotWall = numOfNeighborDiffWall(destinationNode, destinationNode.neighborListNode.get(0));
//					if( cntObjNeighborIsNotWall >= 2 || destinationNode.neighborListNode.get(1).objContent == WALL_CONTENT ){
//						if(destinationNode.neighborListNode.get(2).objContent == WALL_CONTENT){
//							return destinationNode.neighborListNode.get(2);
//						} else if(destinationNode.neighborListNode.get(3).objContent != WALL_CONTENT){
//							return destinationNode.neighborListNode.get(3);
//						}
//					}
//				}
//			} while(!(cntObjNeighborIsNotWall >= 2 || destinationNode.neighborListNode.get(2).objContent == WALL_CONTENT));
//		} else if( pacman.neighborListNode.get(2).objContent != WALL_CONTENT){
//			do{
//				if(destinationNode != null){
//					rdCoordX = 101 + rd.nextInt(pacman.coordX  - 100);
//					destinationNode = this.searchInMapListByCoord(rdCoordX, pacman.coordY);
//					destinationNode.neighborListNode = this.findNeighbor(destinationNode);
//					cntObjNeighborIsNotWall = numOfNeighborDiffWall(destinationNode, destinationNode.neighborListNode.get(3));
//					if( cntObjNeighborIsNotWall >= 2 || destinationNode.neighborListNode.get(2).objContent == WALL_CONTENT ){
//						if(destinationNode.neighborListNode.get(0).objContent != WALL_CONTENT){
//							return destinationNode.neighborListNode.get(0);
//						} else if(destinationNode.neighborListNode.get(1).objContent != WALL_CONTENT){ 
//							return destinationNode.neighborListNode.get(1);
//						}
//					}
//				}
//			} while (!(cntObjNeighborIsNotWall >= 2 || destinationNode.neighborListNode.get(2).objContent == WALL_CONTENT));
//		} else if( pacman.neighborListNode.get(3).objContent != WALL_CONTENT){
//			do{
//				if(destinationNode != null){
//					rdCoordX = pacman.coordX + rd.nextInt(100 + WIDTH_MAP - pacman.coordX) - 1;		// random: destinationNode in right of pacman.
//					destinationNode = this.searchInMapListByCoord(rdCoordX, pacman.coordY);		// find destinationNode in Map
//					destinationNode.neighborListNode = this.findNeighbor(destinationNode);		// find neighbors of destinationNode.
//					cntObjNeighborIsNotWall = numOfNeighborDiffWall(destinationNode, destinationNode.neighborListNode.get(2));
//					if( cntObjNeighborIsNotWall >= 2 || destinationNode.neighborListNode.get(3).objContent == WALL_CONTENT ){
//						if(destinationNode.neighborListNode.get(0).objContent != WALL_CONTENT){
//							return destinationNode.neighborListNode.get(0);
//						} else if (destinationNode.neighborListNode.get(1).objContent != WALL_CONTENT){
//							return destinationNode.neighborListNode.get(1);
//						}
//					}
//				}
//			} while (!(destinationNode.objContent == ROAD_CONTENT || destinationNode.objContent == FOOD_CONTENT));
//		}
//		
//		return destinationNode;
//	}

	
	/* find number neighbor different Wall*/
	public int numOfNeighborDiffWall(objNode object, objNode parent){
		int count = 0;
		objMove temp = new objMove();
		
		object.neighborListNode = temp.findNeighbor(object);
		for(int i = 0; i < object.neighborListNode.size(); i++ ){
			if(object.neighborListNode.get(i).objContent != WALL_CONTENT && object.neighborListNode.get(i) != parent ){
				count ++;
			}
		}
		return count;
	}
	
	/* Convert 1 to object A, 2 to object B*/
	public objNode convertObject(int input, objNode A, objNode B){
		if( input == 1){
			return A;
		}else{
			return B;
		}
	}
	
	/* heuristic for case: have two similar distance to pacman 
	 * In this case: we estimate with neighbors of two node above.
	 * In neighbors of two node, we choose node have min distance from its neighbor to pacman 
	 * */
	public int estimateAbilityOfNode(objNode A, objNode B, objNode parentA, objNode parentB, objNode destinationNode){
		int resultNode;
		double minDistanceA = 1000;
		objNode tempObjNodeA = new objNode();
		double minDistanceB = 1000;
		objNode tempObjNodeB = new objNode();
		double temp;
		
		A.neighborListNode = this.findNeighbor(A);			// find neighbor of Node A
		B.neighborListNode = this.findNeighbor(B);			// find neighbor of Node A
		
		int cntNeighborA = 0;				// count neighbors of tempObjNodeA different wall and parent
		int cntNeighborB = 0;				// count neighbors of tempObjNodeB different wall and parent		
		
		/* Calculate from neighbors of A */
		for(int i = 0; i < A.neighborListNode.size(); i++ ){
			if(A.neighborListNode.get(i).objContent != WALL_CONTENT && A.neighborListNode.get(i) != parentA ){
				
				if(destinationNode.objContent == FOOD_CONTENT)
					temp = minDistanceFromAllFood(A.neighborListNode.get(i));
				else
					temp = calcDistaneAToB(A.neighborListNode.get(i), destinationNode);
				
				if( minDistanceA > temp ){
					minDistanceA = temp;
					tempObjNodeA = A.neighborListNode.get(i);
				}
			}
		}
		cntNeighborA = numOfNeighborDiffWall(tempObjNodeA, A);
		
		/* Calculate from neighbors of B */
		for(int i = 0; i < B.neighborListNode.size(); i++ ){
			if(B.neighborListNode.get(i).objContent != WALL_CONTENT  && B.neighborListNode.get(i) != parentB ){
	
				if(destinationNode.objContent == FOOD_CONTENT)
					temp = minDistanceFromAllFood(B.neighborListNode.get(i));
				else
					temp = calcDistaneAToB(B.neighborListNode.get(i), destinationNode);

				if( minDistanceB > temp ){
					minDistanceB = temp;
					tempObjNodeB = B.neighborListNode.get(i);
				}
			}
		}
		cntNeighborB = numOfNeighborDiffWall(tempObjNodeB, B);
		
		if( minDistanceA < minDistanceB && cntNeighborA >= cntNeighborB){
			resultNode = 1;
		} else if( minDistanceB < minDistanceA && cntNeighborB >= cntNeighborA){
			resultNode = 2;
		}else{
			resultNode = 1;
		}
		
		return resultNode;
	}
	
	public int estimateAbilityOfNodeSecond(objNode A, objNode B, objNode parentA, objNode parentB, objNode destinationNode){
		int resultNode;
		double minDistanceA = 1000;
		objNode tempObjNodeA = new objNode();
		double minDistanceB = 1000;
		objNode tempObjNodeB = new objNode();
		
		A.neighborListNode = this.findNeighbor(A);			// find neighbor of Node A
		B.neighborListNode = this.findNeighbor(B);			// find neighbor of Node A
		
		/* Calculate from neighbors of A */
		for(int i = 0; i < A.neighborListNode.size(); i++ ){
			if(A.neighborListNode.get(i).objContent != WALL_CONTENT && A.neighborListNode.get(i) != parentA ){
				double temp = calcDistaneAToB(A.neighborListNode.get(i),destinationNode);
				if( minDistanceA > temp ){
					minDistanceA = temp;
					tempObjNodeA = A.neighborListNode.get(i);
				}
			}
		}
		
		/* Calculate from neighbors of B */
		for(int i = 0; i < B.neighborListNode.size(); i++ ){
			if(B.neighborListNode.get(i).objContent != WALL_CONTENT  && B.neighborListNode.get(i) != parentB ){
				double temp = calcDistaneAToB(B.neighborListNode.get(i), destinationNode);
				if( minDistanceB > temp ){
					minDistanceB = temp;
					tempObjNodeB = B.neighborListNode.get(i);
				}
			}
		}
		
		if( minDistanceA < minDistanceB){
			resultNode = 1;
		} else if( minDistanceB < minDistanceA){
			resultNode = 2;
		}else{
			resultNode = estimateAbilityOfNode(tempObjNodeA, tempObjNodeB, A, B, destinationNode);
		}
		
		return resultNode;
	}

	
	/* find min distance from all Food to one Node*/
	public double minDistanceFromAllFood(objNode object){
		double minDistance = 1000;

		/*calculate distance from pacman to all food on the map*/
		for(int i = foodList.size() - 1; i >= 0; i--){
			foodList.get(i).distanceToPacman = calcDistaneAToB(object, foodList.get(i));
			if( minDistance > foodList.get(i).distanceToPacman){
				minDistance = foodList.get(i).distanceToPacman;
			}
		}
		return minDistance;
	}
	/* find max distance from all Food to one Node*/
	public double maxDistanceFromAllFood(objNode object){
		double minDistance = 1000;

		/*calculate distance from pacman to all food on the map*/
		for(int i = foodList.size() - 1; i >= 0; i--){
			foodList.get(i).distanceToPacman = calcDistaneAToB(object, foodList.get(i));
			if( minDistance < foodList.get(i).distanceToPacman){
				minDistance = foodList.get(i).distanceToPacman;
			}
		}
		return minDistance;
	}
	
	
	/* Pacman eat Food*/
	public void eatFood(objNode food){
		int indexOfFood;
		if(searchInFoodList(foodList,food)){
			indexOfFood = searchIndexInFoodList(foodList,food);
			if( indexOfFood != -1 )
				foodList.remove(indexOfFood);
			else
				System.out.println("Error: Can not find object in food list! ");
		}
	}
	
	/* get index in foodList */
	public int searchIndexInFoodList(List<objFood> listName, objNode object){
		for(int i = 0; i < listName.size(); i++){
			if(listName.get(i).coordX == object.coordX && listName.get(i).coordY == object.coordY)
				return i;
		}
		return -1;
	}
}
