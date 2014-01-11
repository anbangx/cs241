package dragon.compiler.scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Scanner {
	private File file;
	private int lineNumber;
	private int charPosition;

	private BufferedReader br;
	private String line;

	public Scanner(String path) throws FileNotFoundException {
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

	public char next() {
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
