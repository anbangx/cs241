package dragon.compiler.register;


public class RegisterAllocator {

    private boolean[] registers;
    private final static int NUM_OF_REGS = 10;

    public RegisterAllocator() {
        this.registers = new boolean[NUM_OF_REGS];

        //cannot use reg0
        registers[0] = true;
        for (int i = 1; i < NUM_OF_REGS; i++) {
            this.registers[i] = false;
        }
    }

    public int allocateReg() {
        int i = 1;
        for (; i < NUM_OF_REGS; i++) {
            if (registers[i] == false) {
                registers[i] = true;
                return i;
            }
        }
        System.out.println("Exceed the maximum number of registers!");
        return -1;
    }

    public void deAllocate(int regno) {
        registers[regno] = false;
    }
}
