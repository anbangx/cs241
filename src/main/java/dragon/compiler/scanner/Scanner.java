package dragon.compiler.scanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import dragon.compiler.reader.Reader;

public class Scanner {

    private Reader reader;
    private int sym;

    private int val;
    private int id;

    private ArrayList<String> existIdents; // keep current identIdx and List[existidents] to save space 

    // Constructor: open file and scan the first token into 'inputSym'
    public Scanner(String path) throws FileNotFoundException, IOException {
        reader = new Reader(path);
        sym = -1;
        id = -1;
        existIdents = new ArrayList<String>();
        next();
    }

    // Advance to the next character
    private void next() throws IOException {
        sym = reader.next();
    }

    // Advance to the next token
    public Token getNextToken() throws IOException {
        // check if eof
        if (sym == -1) // TODO what value of 'sym' when reaching the end of file
            return Token.EOF;

        Token curToken = null;
        // Skip space and comment
        if ((curToken = skipSpaceAndComment()) != null)
            return curToken;

        // Check if it is number token
        if ((curToken = getNumberToken()) != null)
            return curToken;

        // Check if it is a letter token(including ident and keyword token)
        if ((curToken = getLetterToken()) != null)
            return curToken;

        // otherwise, other tokens
        switch (sym) {
        // if operand(+,-,*,/)
            case '+':
                next();
                return Token.PLUS;
            case '-':
                next();
                return Token.MINUS;
            case '*':
                next();
                return Token.TIMES;
            case '/':
                next();
                return Token.DIVIDE;
                // if comparison, here pay attention to designator('<-')
            case '=':
                next();
                if (sym == '=') {
                    next();
                    return Token.EQL;
                } else {
                    //error
                    printSyntaxError("\"=\" should be followed by \"=\"");
                    return Token.ERROR;
                }
            case '!':
                next();
                if (sym == '=') {
                    next();
                    return Token.NEQ;
                } else {
                    //error
                    printSyntaxError("\"!\" should be followed by \"=\"");
                    return Token.ERROR;
                }
            case '>':
                next();
                if (sym == '=') {
                    next();
                    return Token.GEQ;
                } else { // TODO: does it need to check syntax error?
                    return Token.GRE;
                }
            case '<':
                next();
                if (sym == '=') {
                    next();
                    return Token.LEQ;
                } else {
                    return Token.LSS;
                }
            // if punctuation(. , ; :)
            case '.':
                next();
                return Token.PERIOD;
            case ',':
                next();
                return Token.COMMA;
            case ';':
                next();
                return Token.SEMICOMA;
            case ':':
                next();
                return Token.COLON;
            // if block (, ), [, ], {, }
            case '(':
                next();
                return Token.BEGIN_PARENTHESIS;
            case ')':
                next();
                return Token.END_PARENTHESIS;
            case '[':
                next();
                return Token.BEGIN_BRACKET;
            case ']':
                next();
                return Token.END_BRACKET;
            case '{':
                next();
                return Token.BEGIN_BRACE;
            case '}':
                next();
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
        while (sym == '\t' || sym == '\r' || sym == '\n' || sym == '\b' || sym == '#' || sym == '/') {
            if (sym == '\t' || sym == '\r' || sym == '\n' || sym == '\b') {
                next();
            } else if (sym == '#') {
                reader.nextLine();
            } else if (sym == '/') {
                if (sym == '/') {
                    reader.nextLine();
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
        while (sym >= '0' && sym <= '9') { // If digit, number
            isNumber = true;
            // update val
            this.val = 10 * this.val + sym - '0';
            next();
        }
        return isNumber ? Token.NUMBER : null;
    }

    public Token getLetterToken() throws IOException{
        boolean isLetter = false;
        StringBuilder sb = null;
        while ((sym >= 'A' && sym <= 'Z') || (sym >= 'a' && sym <= 'z') || (sym >= '0' && sym <= '9')) { // if letter or digit, ident or keyword
            // The first letter should be a letter, actually digit is already filtered when 
            // checking number token
            if (!isLetter && (sym >= '0' && sym <= '9'))
                return null; // TODO maybe throw an exception here is a better choice
            isLetter = true;
            if (sb == null)
                sb = new StringBuilder(sym);
            else
                sb.append(sym);
            next();
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
        return Token.IDENTIRIER;
    }
    
    public int getLineNumber(){
        return reader.getLineNumber();
    }
    
    public void printSyntaxError(String errMsg) {
        System.err.println("Syntax error at " + reader.getLineNumber() + ": " + errMsg);
    }
}
