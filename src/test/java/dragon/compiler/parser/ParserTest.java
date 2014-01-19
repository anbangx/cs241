package dragon.compiler.parser;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import dragon.compiler.data.SyntaxFormatException;

public class ParserTest {

    @Test
    public void test() throws Throwable {
        for (File file : new File("src/test/resources/testprogs").listFiles()) {
            if (!file.isFile()) {
                continue;
            }
            String path = file.getAbsolutePath();
            System.out.println("------------------------------------------------");
            System.out.println(path);
            try{
                Parser ps = new Parser(path);
                ps.parse();
            } catch(Exception e){
                System.out.println(path + " has error: " + e.getMessage());
            }
        }
    }

}
