import java.awt.*;
import java.util.*;

public class Piece {
	
	public int minX;
	public int maxX;
	public boolean[][] blocks;
	public static Color GRAY1 = new Color(47, 47, 47);
	public static Color GRAY2 = new Color(43, 43, 43);
	public static Color GRAY3 = new Color(153, 153, 153);
	public static Color BLACK1 = new Color(255, 255, 255);
	public static Color BLUE1 = new Color(50, 213, 255);	//Long
	public static Color BLUE2 = new Color(72, 131, 255);	//L
	public static Color YELLOW1 = new Color(255, 218, 60);	//Square
	public static Color ORANGE1 = new Color(255, 162, 46);	//L
	public static Color GREEN1 = new Color(142, 238, 53);	//Z
	public static Color RED1 = new Color(255, 78, 106);		//Z
	public static Color PURPLE1 = new Color(235, 80, 205);	//T
	private static boolean blockRot = true;
	private static int xCheck = TetrisRobot.MINX + 234;
	private static int yCheck = TetrisRobot.MINY + 49;
	
	public Piece(){
		minX = 0;
		maxX = 0;
		blocks = new boolean[5][5];
		blocks[2][1] = true;
		blocks[2][2] = true;
		blocks[3][2] = true;
		blocks[3][3] = true;
	}
	
	public void getNextPiece(Robot r){
		blocks = new boolean[5][5];
		Color c = r.getPixelColor(xCheck, yCheck);
		if(c.equals(BLUE1)){
			blocks[3][1] = true;
			blocks[3][2] = true;
			blocks[3][3] = true;
			blocks[3][4] = true;
			blockRot = false;
		}
		else if(c.equals(BLUE2)){
			blocks[3][1] = true;
			blocks[2][1] = true;
			blocks[2][2] = true;
			blocks[2][3] = true;
			blockRot = true;
		}
		else if(c.equals(YELLOW1)){
			blocks[3][2] = true;
			blocks[3][3] = true;
			blocks[2][2] = true;
			blocks[2][3] = true;
			blockRot = false;
		}
		else if(c.equals(ORANGE1)){
			blocks[3][3] = true;
			blocks[2][1] = true;
			blocks[2][2] = true;
			blocks[2][3] = true;
			blockRot = true;
		}
		else if(c.equals(GREEN1)){
			blocks[2][1] = true;
			blocks[2][2] = true;
			blocks[3][2] = true;
			blocks[3][3] = true;
			blockRot = true;
		}
		else if(c.equals(RED1)){
			blocks[3][1] = true;
			blocks[3][2] = true;
			blocks[2][2] = true;
			blocks[2][3] = true;
			blockRot = true;
		}
		else if(c.equals(PURPLE1)){
			blocks[3][2] = true;
			blocks[2][1] = true;
			blocks[2][2] = true;
			blocks[2][3] = true;
			blockRot = true;
		}
	}
	
	public void findSelf(Robot r){
		blocks = new boolean[5][5];
		//Find which blocks are being used
		for(int i = 16; i < 20; i++){
			for(int j = 3; j <= 6; j++){
				int xCheck = TetrisRobot.MINX+j*TetrisRobot.OFFSET;
				int yCheck = TetrisRobot.MINY+(20-i)*TetrisRobot.OFFSET;
				Color c = r.getPixelColor(xCheck, yCheck);
				if(!c.equals(GRAY1) && !c.equals(GRAY2)){
					blocks[i-16][j-3] = true;
					if(c.equals(BLUE1) || c.equals(YELLOW1))
						blockRot = false;
					else
						blockRot = true;
				}
			}
		}
		//Move down all blocks to the appropriate height
		boolean[] rows = new boolean[5];
		for(int row = 0; row < 5; row++){
			for(int j = 0; j < 5; j++){
				rows[row] = rows[row] || blocks[row][j];
			}
		}
		if(rows[0]){
			for(int i = 0; i< 4; i++){
				for(int j = 0; j < 5; j++){
					blocks[i][j] = blocks[i+1][j];
				}
			}
			for(int j = 0; j < 5; j++)
				blocks[4][j] = false;
		}
	}
	
	public int[] detOptimal(boolean[][] field, boolean emergent){
		int[] ret = new int[6];
		boolean first = true;
		for(int rot = 0; rot < 4; rot++){
			for(int trans = -5; trans <= 5; trans++){
				if(!inBounds(trans))
					continue;
				int[] test = detMoveValue(field, rot, trans, emergent);
				if(smaller(ret, test, emergent) || first){
					ret = test;
					first = false;
				}
			}
			rotate();
		}
		return ret;
	}
	
	public int[] detMoveValue(boolean[][] field, int rot, int trans, boolean emergent){
		boolean[][] newField = iterate(field, trans);
		int[] ret = new int[6];
		ret[0] = rot;
		ret[1] = trans;
		if(emergent){
			ret[2] = detTotHeight(newField);
			ret[3] = detHoleNum(newField);
			ret[4] = detClearRight(newField);
			ret[5] = detHeight(field, newField);
		}
		else{
			ret[2] = clearLines(newField);
			ret[3] = detClearRight(newField);
			ret[4] = detHoleNum(newField);
			ret[5] = detHeight(field, newField);
		}
		return ret;
	}
	
	public int detTotHeight(boolean[][] field){
		int ret;
		for(ret = 19; ret >= 0; ret--){
			boolean done = false;
			for(int j = 0; j < 10; j++){
				done = field[ret][j] || done;
			}
			if(done)
				break;
		}
		return ret;
	}
	
	public int detHoleNum(boolean[][] field){
		int ret = 0;
		for(int j = 0; j < 10; j++){
			boolean isOne = false;
			for(int i = 19; i >= 0; i--){
				isOne = isOne || field[i][j];
				if(isOne && !field[i][j])
					ret++;
			}
		}
		return ret;
	}
	
	public int detClearRight(boolean[][] field){
		boolean isClear = true;
		for(int i = 0; i < 20; i++){
			isClear = isClear || field[i][9];
		}
		return isClear?1:0;
	}
	
	public int detHeight(boolean[][] oldField, boolean[][] newField){
		for(int i = 0; i < 20; i++){
			for(int j = 0; j < 10; j++){
				if(newField[i][j] && !oldField[i][j])
					return i;
			}
		}
		return 0;
	}
	
	public boolean inBounds(int trans){
		for(int i = 0; i < 5; i++){
			for(int j = 0; j < 5; j++){
				if(blocks[i][j] && (j+trans+2 < 0 || j+trans+2 >= 10))
					return false;
			}
		}
		return true;
	}
	
	private void rotate(){
		boolean[][] ret = new boolean[6][6];
		if(blockRot){	//If the piece rotates around a particular block
			for(int i = 0; i < 5; i++){
				for(int j = 0; j < 5; j++){
					ret[j][5-i] = blocks[i][j];
				}
			}
		}
		else{	//If the piece rotates around a point between four blocks
			for(int i = 0; i < 5; i++){
				for(int j = 0; j < 5; j++){
					if(6-j < 5)
						ret[6-j][i] = blocks[i][j];
				}
			}
		}
		blocks = ret;
	}
	
	public boolean[][] iterate(boolean[][] field, int trans){
		//Copy field to a new array to return
		boolean[][] ret = new boolean[field.length][field[0].length];
		for(int i = 0; i < field.length; i++){
			for(int j = 0; j < field[0].length; j++){
				ret[i][j] = field[i][j];
			}
		}
		//Determine the vertical drop distance in y
		int y = 0;
		boolean done = false;
		for(y = 0; y < 20; y++){	//Iterate through drop distances
			int botY = 15-y;	//Bottom row of block array is at this height in the field
			for(int i = 0; i < 5; i++){		//Iterate vertically through block array
				for(int j = 0; j < 5; j++){	//Iterate horizontally across the block array
					if(botY + i >= 0 && j + 2 + trans >= 0  && j + 2 + trans < 10 && ret[botY+i][j+2+trans] && blocks[i][j]){
					//block checked within field bounds, a block intersects an existing one in the field
						y--;	//go up one level to prevent intersection
						done = true;
						break;
					}
					if(botY + i < 0 && j + 2 + trans >= 0  && j + 6 + trans < 10 &&  blocks[i][j]){
					//block checked is below the field's bottom bound and exists
						y--;	//go up one level to prevent going past the bottom
						done = true;
						break;
					}
				}
				if(done)
					break;
			}
			if(done)
				break;
		}
		//Fill the new array with piece after the resulting drop
		int botY = 15-y;
		for(int i = 0; i < 5; i++){
			for(int j = 0; j < 5; j++){
				if(botY + i >= 0 && j + 2 + trans >= 0 && j + 2 + trans < 10 && blocks[i][j])
					ret[botY+i][j+2+trans] = true;
			}
		}
		return ret;
	}
	
	public int clearLines(boolean[][] field){
		int ret = 0;
		for(int i = 0; i < 20; i++){	//Iterate through possible full rows
			boolean clear = true;
			for(int j = 0; j < 10; j++){	//Check each block in the row
				clear = clear && field[i][j];
			}
			if(clear){
				for(int r = i; r < 19; r++){	//Move everything down from row i upward
					for(int j = 0; j < 10; j++){
						field[r][j] = field[r+1][j];
					}
				}
				ret++;
				i--;
			}
		}
		return ret;
	}
	
	private static boolean smaller(int[] move1, int[] move2, boolean emergent){
		if(emergent){
			if(move1[2] < move2[2])
				return false;
			if(move1[2] > move2[2])
				return true;
			if(move1[3] < move2[3])
				return false;
			if(move1[3] > move2[3])
				return true;
			if(move1[4] > move2[4])
				return false;
			if(move1[4] < move2[4])
				return true;
			if(move1[5] < move2[5])
				return false;
			if(move1[5] > move2[5])
				return true;
		}
		else{
			int[] ranks = {1, 2, 0, 3, 4};
			int check1 = -1, check2 = -1;
			for(int i = 0; i < 5; i++){
				if(ranks[i] == move1[2])
					check1 = i;
				if(ranks[i] == move2[2])
					check2 = i;
			}
			if(check1 > check2)
				return false;
			if(check1 < check2)
				return true;
			if(move1[3] < move2[3])
				return false;
			if(move1[3] > move2[3])
				return true;
			if(move1[4] > move2[4])
				return false;
			if(move1[4] < move2[4])
				return true;
			if(move1[5] < move2[5])
				return false;
			if(move1[5] > move2[5])
				return true;
		}
		return false;
	}
}
