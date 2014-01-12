package dragon.compiler.scanner;

import java.io.FileNotFoundException;
import java.io.IOException;

import dragon.compiler.reader.Reader;

public class Scanner {

    private Reader reader;
    private int inputSym;
    
    // Constructor: open file and scan the first token into 'inputSym'
    public Scanner(String path) throws FileNotFoundException {
        reader = new Reader(path);
        inputSym = -1;
        next();
    }
    
    // Advance to the next character
    private void next() { 
        inputSym = reader.next();
    }
    
    private void nextLine() throws IOException {
        reader.nextLine();
        next();
    }

}
