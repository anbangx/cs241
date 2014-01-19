package dragon.compiler.data;

public class Instruction {
    private static int pc = 0;
    private int Id;
    public static final int neg = 0;
    public static final int add = 1;
    public static final int sub = 2;
    public static final int mul = 3;
    public static final int div = 4;
    public static final int cmp = 5;
    
    public static final int adda = 6;
    public static final int load = 7;
    public static final int store = 8;
    public static final int move = 9;
    public static final int phi = 10;
    
    public static final int end = 11;
    public static final int bra = 12;
    public static final int bne = 13;
    public static final int beq = 14;
    public static final int ble = 15;
    public static final int blt = 16;
    public static final int bge = 17;
    public static final int bgt = 18;
    
    public static final int read = 19;
    public static final int write = 20;
    public static final int wln = 21;
    
    public static final int push = 22;
    public static final int pop = 23;
    public static final int subroutine = 24;
    public static final int retrn = 25;
    
    public String verbose(int opCode){
        switch(opCode){
            case neg:
                return "neg"; 
            case add:
                return "add";
            case sub:
                return "sub";
            case mul:
                return "mul";
            case div:
                return "div";
            case adda:
                return "adda";
            case load:
                return "load";
            case store:
                return "store";
            case move:
                return "move";
            case phi:
                return "phi";
            default:
                return "";
        }
    }
    
    private int operator; 
    private Result result1;
    private Result result2;
    
    public Instruction(int op, Result result1, Result result2){
        this.operator = op;
        this.result1 = result1;
        this.result2 = result2;
        this.Id = pc;
        Instruction.pc++;
    }
    
    public String toString(){
        StringBuilder sb = new StringBuilder("");
        sb.append(verbose(this.operator) + " ");
        sb.append(result1.toString() + " ");
        sb.append(result2.toString());

        return sb.toString();
    }
}
