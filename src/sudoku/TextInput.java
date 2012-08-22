package sudoku;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextInput {

	private static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
	
	public static String getString(){
		String s="";
		try {s=keyboard.readLine();}
		catch (IOException e){System.out.println("GetChar input error!");}
		return s;
	}
	
	public static char getChar(){
		String s=getString();
		while (s.equals("")){
			System.out.print("-Input Error-"); 
			s=getString();      
		}
		return s.charAt(0);
	}
}
