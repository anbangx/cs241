package dragon.compiler.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Reader {
	private File file;
	private int lineNumber;
	private int charPosition;

	private BufferedReader br;
	private String line;

	public Reader(String path) throws FileNotFoundException {
		file = new File(path);
		br = new BufferedReader(new FileReader(file));
	}

	public void open() throws IOException {
		line = br.readLine();
		lineNumber = 1;
		charPosition = 0;
	}

	public Character getNextChar() throws IOException {
	    charPosition++;
        if (charPosition >= line.length()) {
            // trick to show the end of the line.
            return '#';
        }
        return line.charAt(charPosition);
	}
	
	public void nextLine() throws IOException{
	    line = br.readLine();
        if (line != null) {
            lineNumber++;
            charPosition = 0;
            while (line != null && line.trim().length() == 0) {
                line = br.readLine();
                lineNumber++;
                charPosition = 0;
            }
        }
	}
	
   public Character getCurrentChar() {
        if (line == null) {
            return '~';
        }
        if (charPosition >= line.length()) {
            // trick to show the end of the line.
            return '#';
        }
        return line.charAt(charPosition);
    }
   
	public void close() throws IOException {
		br.close();
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getPositionInLine() {
		return charPosition;
	}
}
