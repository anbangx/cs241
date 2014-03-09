package dragon.compiler.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import dragon.compiler.data.Token;

public class Scanner {
	
    private Reader reader;
    private Character curChar;

    public int val;
    public int id;

    public static ArrayList<String> existIdents; // keep current identIdx and List[existidents] to save space 

    // Constructor: open file and scan the first token into 'inputSym'
    public Scanner(String path) throws FileNotFoundException, IOException {
        reader = new Reader(path);
        curChar = null;
        id = -1;
        existIdents = new ArrayList<String>();
        existIdents.add("InputNum");
        existIdents.add("OutputNum");
        existIdents.add("OutputNewLine");;
    }
    
    public void open() throws IOException {
        reader.open();
        curChar = reader.getCurrentChar();
    }
    
    public void close() throws IOException {
        reader.close();
    }
    
    // Advance to the next character
    private void nextChar() throws IOException {
        curChar = reader.getNextChar();
    }
    
    // Advance to the first character of next line
    private void nextLine() throws IOException {
        reader.nextLine();
        curChar = reader.getCurrentChar();
    }

    // Advance to the next token
    public Token getNextToken() throws IOException {
        Token curToken = null;
        // Skip space and comment
        if ((curToken = skipSpaceAndComment()) != null)
            return curToken;
        
        // check if eof
        if (curChar == '~')
            return Token.EOF;
        
        // Check if it is number token
        if ((curToken = getNumberToken()) != null)
            return curToken;

        // Check if it is a letter token(including ident and keyword token)
        if ((curToken = getLetterToken()) != null)
            return curToken;

        // otherwise, other tokens
        switch (curChar) {
            // if operand(+,-,*,/)
            case '+':
                nextChar();
                return Token.PLUS;
            case '-':
                nextChar();
                return Token.MINUS;
            case '*':
                nextChar();
                return Token.TIMES;
            // if comparison, here pay attention to designator('<-')
            case '=':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.EQL;
                } else {
                    //error
                    printSyntaxError("\"=\" should be followed by \"=\"");
                    return Token.ERROR;
                }
            case '!':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.NEQ;
                } else {
                    //error
                    printSyntaxError("\"!\" should be followed by \"=\"");
                    return Token.ERROR;
                }
            case '>':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.GEQ;
                } else { // TODO: does it need to check syntax error?
                    return Token.GRE;
                }
            case '<':
                nextChar();
                if (curChar == '=') {
                    nextChar();
                    return Token.LEQ;
                } else if(curChar == '-'){
                    nextChar();
                    return Token.BECOMETO;
                } else {
                    return Token.LSS;
                }
            // if punctuation(. , ; :)
            case '.':
                nextChar();
                return Token.PERIOD;
            case ',':
                nextChar();
                return Token.COMMA;
            case ';':
                nextChar();
                return Token.SEMICOMA;
            case ':':
                nextChar();
                return Token.COLON;
            // if block (, ), [, ], {, }
            case '(':
                nextChar();
                return Token.BEGIN_PARENTHESIS;
            case ')':
                nextChar();
                return Token.END_PARENTHESIS;
            case '[':
                nextChar();
                return Token.BEGIN_BRACKET;
            case ']':
                nextChar();
                return Token.END_BRACKET;
            case '{':
                nextChar();
                return Token.BEGIN_BRACE;
            case '}':
                nextChar();
                return Token.END_BRACE;
        }

        return Token.ERROR;
    }

    /**
     * Skip space and comment
     * tab: \t;
     * carriage return: \r;
     * new line: \n;
     * space: \b or ' ';
     * in PL241, both # and / are used for comments
     * @throws IOException 
     */
    public Token skipSpaceAndComment() throws IOException {
        while (curChar == '\t' || curChar == '\r' || curChar == '\n' || curChar == ' ' || curChar == '#' || curChar == '/') {
            if (curChar == '\t' || curChar == '\r' || curChar == '\n' || curChar == ' ') {
                nextChar();
            } else if (curChar == '#') {
                nextLine();
            } else if (curChar == '/') {
                nextChar();
                if (curChar == '/') {
                    nextLine();
                } else {
                    return Token.DIVIDE;
                }
            }
        }
        return null;
    }

    public Token getNumberToken() throws IOException {
        boolean isNumber = false;
        this.val = 0;
        while (curChar >= '0' && curChar <= '9') { // If digit, number
            isNumber = true;
            // update val
            this.val = 10 * this.val + curChar - '0';
            nextChar();
        }
        return isNumber ? Token.NUMBER : null;
    }

    public Token getLetterToken() throws IOException{
        boolean isLetter = false;
        StringBuilder sb = null;
        while (Character.isLetterOrDigit(curChar)) { // if letter or digit, ident or keyword
            // The first letter should be a letter, actually digit is already filtered when 
            // checking number token
            if (!isLetter && Character.isDigit(curChar))
                return null; // TODO maybe throw an exception here is a better choice
            isLetter = true;
            if (sb == null){
                sb = new StringBuilder("");
                sb.append(curChar);
            }
            else
                sb.append(curChar);
            nextChar();
        }
        if (!isLetter) // no letter Token
            return null;

        String candidate = sb.toString();
        Token keywordToken = Token.getKeywordFromString(candidate);
        if (keywordToken != null) // found in keyword library, so it's keyword token
            return keywordToken;

        // otherwise, it's ident
        if (!existIdents.contains(candidate))
            existIdents.add(candidate);
        this.id = existIdents.indexOf(candidate);
        return Token.IDENTIFIER;
    }
    
    public int getLineNumber(){
        return reader.getLineNumber();
    }
    
    public void printSyntaxError(String errMsg) {
        System.err.println("Syntax error at " + reader.getLineNumber() + ": " + errMsg);
    }
}
