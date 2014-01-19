package dragon.compiler.register;

import dragon.compiler.data.Result;

public class RegisterAllocator {
    
    private boolean[] registers;
    private final static int NUM_OF_REGS = 10;
    
    public RegisterAllocator() {
        this.registers = new boolean[NUM_OF_REGS];
        
        //cannot use reg0
        registers[0] = true;
        for(int i = 1; i < NUM_OF_REGS; i++) {
            this.registers[i] = false;
        }
    }
    
    public void load(Result x){
        if(x.kind == Result.Type.var){
            x.kind = Result.Type.reg;
            x.regno = allocateReg();
            // TODO put into assembler format
        } else if(x.kind == Result.Type.constant){
            x.kind = Result.Type.reg;
            if(x.value == 0)
                x.regno = 0;
            else{
                x.regno = allocateReg();
                // TODO put into assembler format
            }
        }
    }
    
    public int allocateReg(){
        int i = 1;
        for(; i < NUM_OF_REGS; i++){
            if(registers[i] == false){
                registers[i] = true;
                return i;
            }
        }
        System.out.println("Exceed the maximum number of registers!");
        return -1;
    }
    
    public void deAllocate(int regno){
        registers[regno] = false;
    }
}
