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
		lineNumber = 0;
		charPosition = 0;
	}

	public boolean hasNext() throws IOException {
		if (line == null) {
			return false;
		}
		if (charPosition < line.length()) {
			return true;
		}
		line = br.readLine();
		lineNumber++;
		charPosition = 0;
		return hasNext();
	}

	public int next() {
		return line.charAt(charPosition++);
	}
	
	public void nextLine(){
	    try {
	        line = br.readLine();
	        lineNumber++;
	        charPosition = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
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
