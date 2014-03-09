package dragon.compiler.generator;

import java.util.HashMap;

import dragon.compiler.data.Instruction;
import dragon.compiler.util.ControlFlowGraph;

public class CodeGenerator {
    
  //opcode
    private static final int ADD = 0;
    private static final int SUB = 1;
    private static final int MUL = 2;
    private static final int DIV = 3;
    private static final int MOD = 4;
    private static final int CMP = 5;
    private static final int OR = 8;
    private static final int AND = 9;
    private static final int BIC = 10;
    private static final int XOR = 11;
    
    private static final int LSH = 12;
    private static final int ASH = 13;
    
    private static final int CHK = 14;
    
    private static final int ADDI = 16;
    private static final int SUBI = 17;
    private static final int MULI = 18;
    private static final int DIVI = 19;
    private static final int MODI = 20;
    private static final int CMPI = 21;
    private static final int ORI = 24;
    private static final int ANDI = 25;
    private static final int BICI = 26;
    private static final int XORI = 27;
    
    private static final int LSHI = 28;
    private static final int ASHI = 29;
    
    private static final int CHKI = 30;
    
    private static final int LDW = 32;
    private static final int LDX = 33;
    private static final int POP = 34;
    private static final int STW = 36;
    private static final int STX = 37;
    private static final int PSH = 38;
    
    private static final int BEQ = 40;
    private static final int BNE = 41;
    private static final int BLT = 42;
    private static final int BGE = 43;
    private static final int BLE = 44;
    private static final int BGT = 45;
    
    private static final int BSR = 46;
    private static final int JSR = 48;
    private static final int RET = 49;
    
    private static final int RDI = 50;
    private static final int WRD = 51;
    private static final int WRH = 52;
    private static final int WRL = 53;
    
    private static final int WORDLEN = 4;
    private int[] buf;
    private int pc;
    private int fp;
    
    private HashMap<Integer, Integer> arithmeticIC2C;
    private HashMap<Integer, Integer> branchIC2C;
    
    public CodeGenerator(){
        this.pc = 0;
        this.buf = new int[ControlFlowGraph.allInstructions.size() + 1000];
        this.fp = ControlFlowGraph.allInstructions.size() * WORDLEN;
        arithmeticIC2C = new HashMap<Integer, Integer>();
        arithmeticIC2C.put(Instruction.add, ADD);
        arithmeticIC2C.put(Instruction.sub, SUB);
        arithmeticIC2C.put(Instruction.mul, MUL);
        arithmeticIC2C.put(Instruction.div, DIV);
        arithmeticIC2C.put(Instruction.cmp, CMP);
        branchIC2C = new HashMap<Integer, Integer>();
        branchIC2C.put(Instruction.beq, BEQ);
        branchIC2C.put(Instruction.bge, BGE);
        branchIC2C.put(Instruction.bgt, BGT);
        branchIC2C.put(Instruction.ble, BLE);
        branchIC2C.put(Instruction.blt, BLT);
        branchIC2C.put(Instruction.bne, BNE);
    }
    
    public void generateCode(){
        for(Instruction instr : ControlFlowGraph.allInstructions){
            
        }
    }
}
