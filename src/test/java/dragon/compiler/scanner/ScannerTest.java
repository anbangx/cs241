package dragon.compiler.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import dragon.compiler.data.Token;
import dragon.compiler.parser.Scanner;

public class ScannerTest {

	@Test
	public void test() throws FileNotFoundException, IOException {
		for (File file : new File("src/test/resources/testprogs").listFiles()) {
			if (!file.isFile()) {
				continue;
			}
			String path = file.getAbsolutePath();
			System.out
					.println("------------------------------------------------");
			System.out.println(path);
			Scanner scanner = new Scanner(path);
			scanner.open();
			Token token = null;
			while ((token = scanner.getNextToken()) != Token.EOF) {
				System.out.println(token.toString());
			}
			scanner.close();
		}
	}

}
