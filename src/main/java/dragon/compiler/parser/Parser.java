package dragon.compiler.parser;

import dragon.compiler.scanner.Scanner;
import dragon.compiler.scanner.Token;

public class Parser {

    private Scanner scanner;
    private Token token;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        this.token = next();
    }

    public Token next() {
        return scanner.getNextToken();
    }
}
