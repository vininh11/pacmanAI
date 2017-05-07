package object;

import java.util.ArrayList;
import java.util.List;

import interfaceUser.readMapFile;

public class objGhost extends objMove implements Runnable{
	private objNode []initNodeAttr;
	private objNode []destinationNodeAttr;
	private final int COUNT = foodList.size();
	
	public List<objNode> nodeWentThrough = new ArrayList<objNode>();
	
	/* Constructor */
	public objGhost(int coordX, int coordY, char content){
		super(coordX, coordY, content);
	}
	public objGhost(objNode []initNode, objNode []destinationNode){
		this.initNodeAttr = initNode;
		this.destinationNodeAttr = destinationNode;
	}
	public objGhost(){}
	
	/* Ghost run */
	public void run(){
		multiGhostsMoveAStar(initNodeAttr, destinationNodeAttr);
	}
	
	/* Multiple Ghosts A Star*/
	public synchronized void multiGhostsMoveAStar(objNode []initNode, objNode []destinationNode){
		objNode []nextNodeToGo = new objNode[COUNT];
		objNode []initNodeFirst = new objNode[COUNT];
		objNode []destinationNodeFirst = new objNode[COUNT];
		objNode []objTemp = new objNode[COUNT];
		readMapFile readFile = new readMapFile();		// init object print map to screen
		objGhost objGhost = new objGhost();
		int []countLoop = new int[COUNT];
		int []status = new int[COUNT];
 		List<List<objNode>> listMove = new ArrayList<List<objNode>>();
 		List<List<objNode>> listNodesWentThrough = new ArrayList<List<objNode>>();
 		
		for(int i = 0; i < COUNT; i++){
			initNodeFirst[i] = initNode[i];					// Save init Node for the Ghost
			destinationNodeFirst[i] = destinationNode[i];		// Save destinationNode for the Ghost
			countLoop[i] = 1;
			initNode[i].neighborListNode = new ArrayList<objNode>();	// init list neighborListNode
			listNodesWentThrough.add(new ArrayList<objNode>());
		}
		
		// Find road to destination node
		while(foodList.size() != 0){
			for(int i = 0; i < COUNT; i++){
				// Ghost can repeat it's move
				if((initNode[i].coordX == destinationNode[i].coordX) && (initNode[i].coordY == destinationNode[i].coordY)){
					if( countLoop[i] % 2 != 0)
						destinationNode = initNodeFirst;
					else
						destinationNode = destinationNodeFirst;
					countLoop[i] ++;
				}

				/* Dong bo cac doi tuong tren ban do */
				objNode tempNode = searchInMapList(mapList, initNode[i]);
				if(tempNode.objContent != GHOST_CONTENT){
					initNode[i] = new objGhost(tempNode.coordX, tempNode.coordY, GHOST_CONTENT);
					mapList.set(mapList.indexOf(tempNode), initNode[i]);
				}else{
					initNode[i] = tempNode;
				}
				
				/* Find path to destination Node */
				initNode[i].neighborListNode = this.findNeighbor(initNode[i]);			// find neighbor of Node
				nextNodeToGo[i] = nextNode(initNode[i], destinationNode[i], listNodesWentThrough.get(i));							// find next node to go
				if(objTemp[i] == null)
					objTemp[i] = nextNodeToGo[i];
				status[i] = this.calcStatus(initNode[i], nextNodeToGo[i]);// calculate status to go from initNode to nextNodeToGo
				if(status[i] == 6)
					break;
				if(listMove.size() < COUNT)
					listMove.add(this.gotoAB(initNode[i], nextNodeToGo[i], status[i], objTemp[i]));		// move from initNode to nextNodeToGo
				else
					listMove.set(i,this.gotoAB(initNode[i], nextNodeToGo[i], status[i], objTemp[i]));
				
				if(listMove.size() > 2)
					objTemp[i] = listMove.get(i).get(2);
				
				try {
					listNodesWentThrough.get(i).add(listMove.get(i).get(0));		// add currentNode to list node went through
				} catch (IndexOutOfBoundsException e) {
					listNodesWentThrough.get(i).add(listNodesWentThrough.get(i).get(0));
				}
				int maxSize = 5;
				if(listNodesWentThrough.get(i).size() > maxSize)		// max size for list listNodesWentThrough
					listNodesWentThrough.get(i).remove(0);
			
				initNode[i] = listMove.get(i).get(1);
				objGhost = new objGhost(initNode[i].coordX, initNode[i].coordY, GHOST_CONTENT);
				ghostList.set(i ,objGhost);
			}
			
			for(int j = 0; j < 10; j++)
				System.out.println("\n");
			readFile.printListMap(mapList); 				// display to screen
		}
	}
	
	/* heuristic
	 * Find in 4 neighbors of Node, return the neighbor has min distance to destination node.
	 *  */
	public objNode nextNode(objNode currentNode, objNode destinationNode, List<objNode> nodeWentThrough){
		if(currentNode == destinationNode){
			System.exit(0);
		}

		objNode nextNode = null;
		double distance;

		// choose in neighbor node of current node
		distance = 1000;
		double []tempDistance = new double[4];		// array of disatance from four neighbors of currentNode to destinationNode
		
		for(int i = 0; i < currentNode.neighborListNode.size(); i++){
			if(currentNode.neighborListNode.get(i).objContent != WALL_CONTENT && !searchInWalkThroughList(nodeWentThrough,currentNode.neighborListNode.get(i))){
				tempDistance[i] = calcDistaneAToB(currentNode.neighborListNode.get(i), destinationNode);
				if(distance > tempDistance[i]){
					distance = tempDistance[i];
					nextNode = currentNode.neighborListNode.get(i);
				}
			}
		}

		if (nextNode == null) {
			for(int i = 0; i < currentNode.neighborListNode.size(); i++){
				if(currentNode.neighborListNode.get(i).objContent != WALL_CONTENT){
					tempDistance[i] = calcDistaneAToB(currentNode.neighborListNode.get(i), destinationNode);
					if(distance > tempDistance[i]){
						distance = tempDistance[i];
						nextNode = currentNode.neighborListNode.get(i);
					}
				}
			}
		}
		return nextNode;
	}
	
	/* search coordX, coordY in nodeWalkThrough List*/
	public boolean searchInWalkThroughList(List<objNode> listName, objNode object){
		for(int i = 0; i < listName.size(); i++){
			if(listName.get(i).coordX == object.coordX && listName.get(i).coordY == object.coordY)
				return true;
		}
		return false;
	}
}
