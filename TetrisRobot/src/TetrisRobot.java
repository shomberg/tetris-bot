import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class TetrisRobot //implements KeyListener
{

	private static boolean[][] field;
	public static final int MINX = 219;//380;
	public static final int MINY = 325;//305;
	public static final int OFFSET = 18;
	private static long startTime;
	//private boolean cont = false;
	
	public static void main(String[] args) throws AWTException{
		startTime = System.currentTimeMillis();
		field = new  boolean[20][10];
		field[0][2] = true;
		field[1][3] = true;
		Piece a = new Piece();
		int[] b = a.detOptimal(field, false);
		field = a.iterate(field, 0);
		int top = 0;
		boolean emergent = false;
		Robot r = new Robot();
		r.setAutoWaitForIdle(false);
		delay(5000);
		Piece p = new Piece();
		Piece q = new Piece();
		q.getNextPiece(r);
		keyType(r, KeyEvent.VK_SPACE);
		while(System.currentTimeMillis()-startTime < 10000){// && cont
			System.out.println("start cycle");
			//p.findSelf(r);
			p = q;
			q.getNextPiece(r);
			
			int[] optimal = p.detOptimal(field, emergent);
			System.out.println("make move");
			//Make move
			for(int i = 0; i < optimal[0]; i++){
				keyType(r, KeyEvent.VK_UP);
			}
			if(optimal[1] > 0){
				for(int i = 0; i < optimal[1]; i++)
					keyType(r, KeyEvent.VK_RIGHT);
			}
			else if(optimal[1] < 0){
				for(int i = 0; i > optimal[1]; i--)
					keyType(r, KeyEvent.VK_LEFT);
			}
			keyType(r, KeyEvent.VK_SPACE);
			System.out.println("move made");
			delay(100);
			//Reset data
			field = p.iterate(field, optimal[1]);
			p.clearLines(field);
			top = 0;
			while(true){
				boolean isIn = false;
				for(int i = 0; i < 10; i++)
					isIn = isIn || field[top][i];
				if(!isIn)
					break;
				top++;
				if(top == 20)
					break;
			}
			if(top > 15)
				emergent = true;
			else
				emergent = false;
			delay(400);
		}
	}
	
	private static void keyType(Robot r, int keycode){
		r.keyPress(keycode);
		delay(100);
		r.keyRelease(keycode);
	}
	
	private static void delay(int ms){
		try{
			Thread.sleep(ms);
		}
		catch(InterruptedException e){
		}
	}

	/*public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_CONTROL)
			cont = true;
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			cont = false;
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}*/
}
