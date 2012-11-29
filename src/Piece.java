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
	public static Color BLUE1 = new Color(15, 155, 215);
	public static Color YELLOW1 = new Color(227, 159, 2);
	private static boolean blockRot = true;
	
	public Piece(){
		minX = 0;
		maxX = 0;
		blocks = new boolean[5][5];
	}
	
	public void findSelf(Robot r, int top){
		blocks = new boolean[5][5];
		for(int i = 16; i < 20; i++){
			for(int j = 3; j <= 6; j++){
				int xCheck = TetrisRobot.MINX+j*TetrisRobot.OFFSET;
				int yCheck = TetrisRobot.MINY+(20-i)*TetrisRobot.OFFSET;
				Color c = r.getPixelColor(xCheck, yCheck);
				//System.out.println("check " + i);
				if(!c.equals(GRAY1) && !c.equals(GRAY2)){
					blocks[i-16][j-3] = true;
					if(c.equals(BLUE1) || c.equals(YELLOW1))
						blockRot = false;
					else
						blockRot = true;
				}
			}
		}
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
		for(int rot = 0; rot < 4; rot++){
			for(int trans = -5; trans <= 5; trans++){
				if(!inBounds(trans))
					continue;
				int[] test = detMoveValue(field, rot, trans, emergent);
				if(smaller(ret, test, emergent))
					ret = test;
			}
			rotate();
		}
		return ret;
	}
	
	public int[] detMoveValue(boolean[][] field, int rot, int trans, boolean emergent){
		boolean[][] newField = iterate(field);
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
				if(blocks[i][j] && (i+trans+2 < 0 || i+trans+2 >= 10))
					return false;
			}
		}
		return true;
	}
	
	private void rotate(){
		boolean[][] ret = new boolean[6][6];
		if(blockRot){
			for(int i = 0; i < 5; i++){
				for(int j = 0; j < 5; j++){
					ret[j][5-i] = blocks[i][j];
				}
			}
		}
		else{
			for(int i = 0; i < 5; i++){
				for(int j = 0; j < 5; j++){
					if(6-j < 5)
						ret[6-j][i] = blocks[i][j];
				}
			}
		}
		blocks = ret;
	}
	
	public boolean[][] iterate(boolean[][] field){
		boolean[][] ret = new boolean[field.length][field[0].length];
		for(int i = 0; i < field.length; i++){
			for(int j = 0; j < field[0].length; j++){
				ret[i][j] = field[i][j];
			}
		}
		int y = 0;
		boolean done = false;
		for(y = 0; y < 20; y++){
			int botY = 15-y;
			for(int i = 0; i < 5; i++){
				for(int j = 0; j < 10; j++){
					if(botY+i >= 0 && j >= 2 && j <= 6 && ret[botY+i][j] && blocks[i][j]){
						y--;
						done = true;
						break;
					}
					if(botY + i < 0 && j >= 2 && j <= 6 && ret[botY+i][j] && blocks[i][j]){
						y--;
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
		int botY = 15-y;
		for(int i = 0; i < 5; i++){
			for(int j = 0; j < 10; j++){
				if(j >= 2 && j <= 7 && blocks[i][j])
					ret[botY+i][j] = true;
			}
		}
		return ret;
	}
	
	public int clearLines(boolean[][] field){
		int ret = 0;
		for(int i = 0; i < 20; i++){
			boolean clear = true;
			for(int j = 0; j < 10; j++){
				clear = clear && field[i][j];
			}
			if(clear){
				for(int r = i; r < 19; r++){
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
			if(move1[2] < move2[2])
				return false;
			if(move1[2] > move2[2])
				return true;
			int[] ranks = {1, 2, 0, 3, 4};
			int check1 = -1, check2 = -1;
			for(int i = 0; i < 5; i++){
				if(ranks[i] == move1[3])
					check1 = i;
				if(ranks[i] == move2[3])
					check2 = i;
			}
			if(check1 > check2)
				return false;
			if(check1 < check2)
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
