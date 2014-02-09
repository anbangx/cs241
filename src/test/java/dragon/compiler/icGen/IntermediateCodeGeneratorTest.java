package dragon.compiler.icGen;

import java.io.File;

import org.junit.Test;

import dragon.compiler.parser.ControlFlowGraph;
import dragon.compiler.parser.Parser;
import dragon.compiler.parser.VCGPrinter;

public class IntermediateCodeGeneratorTest {

	@Test
	public void test() throws Throwable {
		for (File file : new File("src/test/resources/testprogs").listFiles()) {
			if (!file.isFile()) {
				continue;
			}
			String path = file.getAbsolutePath();
			System.out
					.println("------------------------------------------------");
			System.out.println(path);
			try {
				Parser ps = new Parser(path);
				ps.parse();
				ControlFlowGraph.printIntermediateCode();
				VCGPrinter printer = new VCGPrinter(path);
				printer.printCFG();
			} catch (Exception e) {
				System.out.println(path + " has error: " + e.getMessage());
			}
		}
	}

}
