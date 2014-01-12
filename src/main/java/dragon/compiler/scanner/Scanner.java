package dragon.compiler.scanner;

import java.io.FileNotFoundException;

import dragon.compiler.reader.Reader;

public class Scanner {

    private Reader reader;
    private int sym;

    private int val;
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
    private void nextLine() {
        reader.nextLine();
        next();
    }

    // Advance to the next token
    public Token getNextToken() {
        Token curToken = null;
        // Skip space and comment
        if ((curToken = skipSpaceAndComment()) != null)
            return curToken;

        // Get next token
        if (sym >= '0' && sym <= '9') { // If digit, number
            this.val = sym - '0';
            while(sym != -1) { // TODO what value should return when reaching the end of file
                next();
                if(sym >= '0' && sym <= '9') {
                    // update val
                    this.val = 10 * this.val + sym - '0';
                } else {
                    return Token.NUMBER;
                }
            }
            return Token.EOF;
        } else if ((sym >= 'A' && sym <= 'Z') || (sym >= 'a' && sym <= 'z')) { // if letter, maybe ident or keyword
            
        } else{ // otherwise, other tokens
            switch(sym){
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
                    if(sym == '=') {
                        next();
                        return Token.EQL;
                    } else {
                        //error
                        printSyntaxError("\"=\" should be followed by \"=\"");
                        return Token.ERROR;
                    }
                case '!':
                    next();
                    if(sym == '=') {
                        next();
                        return Token.NEQ;
                    } else {
                        //error
                        printSyntaxError("\"!\" should be followed by \"=\"");
                        return Token.ERROR;
                    }
                case '>':
                    next();
                    if(sym == '=') {
                        next();
                        return Token.GEQ;
                    } else { // TODO: does it need to check syntax error?
                        return Token.GRE;
                    }   
                case '<':
                    next();
                    if(sym == '=') {
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
     */
    public Token skipSpaceAndComment() {
        while (sym == '\t' || sym == '\r' || sym == '\n' || sym == '\b' || sym == '#' || sym == '/') {
            if (sym == '\t' || sym == '\r' || sym == '\n' || sym == '\b') {
                next();
            } else if (sym == '#') {
                nextLine();
            } else if (sym == '/') {
                if (sym == '/') {
                    nextLine();
                } else {
                    return Token.DIVIDE;
                }
            }
        }
        return null;
    }
    
    private void printSyntaxError(String errMsg) {
        System.err.println("Syntax error at " + reader.getLineNumber() + ": " + errMsg);
    }
}
