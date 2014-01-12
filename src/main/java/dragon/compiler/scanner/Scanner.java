package dragon.compiler.scanner;

import java.io.FileNotFoundException;
import java.io.IOException;

import dragon.compiler.reader.Reader;

public class Scanner {

    private Reader reader;
    private int sym;
    
    // Constructor: open file and scan the first token into 'inputSym'
    public Scanner(String path) throws FileNotFoundException {
        reader = new Reader(path);
        sym = -1;
        next();
    }
    
    // Advance to the next character
    private void next() { 
        sym = reader.next();
    }
    
    // Advance to the first character of next line
    private void nextLine(){
        reader.nextLine();
        next();
    }
    
    // Advance to the next token
    public Token getNextToken(){
        Token curToken = null;
        // Skip space and comment
        if((curToken = skipSpaceAndComment()) != null)
            return curToken;
        
        
        return Token.ERROR;
    }
    
    /** Skip space and comment
     *  tab: \t; 
     *  carriage return: \r;
     *  new line: \n;
     *  space: \b or ' ';
     *  in PL241, both # and / are used for comments 
     */
    public Token skipSpaceAndComment(){
        while(sym == '\t' || sym == '\r' || sym == '\n' || sym == '\b'
                || sym == '#' || sym == '/'){
            if(sym == '\t' || sym == '\r' || sym == '\n' || sym == '\b'){
                next();
            }
            else if(sym == '#'){
                nextLine();
            }
            else if(sym == '/'){
                if(sym == '/') {
                    nextLine();
                } else {
                    return Token.DIVIDE;
                }
            }
        }
        return null;
    }
}
