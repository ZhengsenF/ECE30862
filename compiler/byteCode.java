import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Class name:  byteCode
 * Description: This class is to put byte operation into a byte array
 *              and then finally write the entire array into output
 *              file.
 * Programmers: Zhengsen Fu fu216@purdue.edu
 *              Yanjun Chen chen2620@purdue.edu
 */
public class byteCode {
    private int stackPointer; //points to last item pushed in
    private int programCounter; //points to last byte written
    private Stack<Integer> frameStack; //points to first item in the frame
    private DataOutputStream file; //output file pointer
    private ArrayList<Byte> output; //list of byte to be outputted at the end fo the program

    public byteCode(DataOutputStream outputFile) {
        stackPointer = -1;
        programCounter = -1;
        output = new ArrayList<>();
        file = outputFile;
        frameStack = new Stack<Integer>();
        frameStack.push(0);
    }

    public void writeToFile() throws IOException {
        for(byte each: output) {
            file.writeByte(each);
        }
    }

    public void setArr(int index, int offset) {
        byte[] arr = IntToByte(offset);

        for (byte each : arr){
            output.set(index++, each);
        }
    }

    public byte[] IntToByte (int data){
        return new byte[] {(byte)((data) & 0xff),
            (byte)((data >> 8) & 0xff),
            (byte)((data >> 16) & 0xff),
            (byte)((data >> 24) & 0xff)};
    }

    public int getPC() { //get program counter
        return programCounter;
    }

    public int getStackPointer() {
        return stackPointer;
    }

    public int getfsp () { // get top frame stack pointer
        return frameStack.peek();
    }

    public void cmpe(){
        output.add((byte)132);
        programCounter++;
        stackPointer--;
    }

    public void cmplt(){
        output.add((byte)136);
        programCounter++;
        stackPointer--;
    }

    public void cmpgt(){
        output.add((byte)140);
        programCounter++;
        stackPointer--;
    }

    public void jmp(){
        output.add((byte)36);
        programCounter++;
        stackPointer--;
    }

    public void jmpc(){
        output.add((byte)40);
        programCounter++;
        stackPointer -= 2;
    }

    //val is the last item pushed into stack
    public void call(int val){
        output.add((byte)44);
        programCounter++;
        frameStack.push(stackPointer - val - 1);
        stackPointer -= 2;
    }

    public void ret(){
        output.add((byte)48);
        programCounter++;
        stackPointer = frameStack.pop();
        stackPointer--;
    }

    public void pushc(char data){
        output.add((byte)68);
        output.add((byte)data);
        programCounter += 2;
        stackPointer++;
    }

    public void pushs(short data){
        output.add((byte)69);
        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort(data);
        buf.flip();
        byte[] arr = buf.array();
        for (byte each: arr){
            output.add(each);
        }
        programCounter += 3;
        stackPointer++;
    }

    public void pushi(int data){
        output.add((byte)70);
        byte[] arr = IntToByte(data);

        for (byte each : arr){
            output.add(each);
        }
        programCounter += 5;
        stackPointer++;
    }

    public void pushf(float data){
        output.add((byte)71);
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putFloat(data);
        buf.flip();
        byte[] arr = buf.array();
        for (byte each: arr){
            output.add(each);
        }
        programCounter += 5;
        stackPointer++;
    }

    public void pushvc(){
        output.add((byte)72);
        programCounter++;
        //stackPointer?
    }

    public void pushvs(){
        output.add((byte)73);
        programCounter++;
        //stackPointer?
    }

    public void pushvi(){
        output.add((byte)74);
        programCounter++;
        //stackPointer?
    }

    public void pushvf(){
        output.add((byte)75);
        programCounter++;
        //stackPointer?
    }

    //val is the number of items to be discarded
    public void popm(int val){
        output.add((byte)76);
        programCounter++;
        stackPointer -= val + 1; //including the top item number indicater
    }

    public void popv(){
        output.add((byte)80);
        programCounter++;
        stackPointer -= 2;
    }

    //val is the number of item to be kept
    public void popa(int val){
        output.add((byte)77);
        programCounter++;
        stackPointer = frameStack.peek() + val - 1;
    }

    public void peekc(){
        output.add((byte)84);
        programCounter++;
    }

    public void peeks(){
        output.add((byte)85);
        programCounter++;
    }

    public void peeki(){
        output.add((byte)86);
        programCounter++;
    }

    public void peekf(){
        output.add((byte)87);
        programCounter++;
    }

    public void pokec(){
        output.add((byte)88);
        programCounter++;
    }

    public void pokes(){
        output.add((byte)89);
        programCounter++;
    }

    public void pokei(){
        output.add((byte)90);
        programCounter++;
    }

    public void pokef(){
        output.add((byte)91);
        programCounter++;
    }

    public void swp(){
        output.add((byte)94);
        programCounter++;
    }

    public void add(){
        output.add((byte)100);
        programCounter++;
        stackPointer--;
    }

    public void sub(){
        output.add((byte)104);
        programCounter++;
        stackPointer--;
    }

    public void mul(){
        output.add((byte)108);
        programCounter++;
        stackPointer--;
    }

    public void div(){
        output.add((byte)112);
        programCounter++;
        stackPointer--;
    }

    public void printc(){
        output.add((byte)144);
        programCounter++;
        stackPointer--;
    }

    public void prints(){
        output.add((byte)145);
        programCounter++;
        stackPointer--;
    }

    public void printi(){
        output.add((byte)146);
        programCounter++;
        stackPointer--;
    }

    public void printf(){
        output.add((byte)147);
        programCounter++;
        stackPointer--;
    }

    public void halt(){
        output.add((byte)0);
        programCounter++;
    }
}
