import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class name:   compiler
 * Description: This file contains main function
 * Programmers: Zhengsen Fu fu216@purdue.edu
 *              Yanjun Chen chen2620@purdue.edu
 */
public class compiler {
    public static void main(String[] args) throws IOException {
        File inputFile = new File(args[0]);
        DataOutputStream output = new DataOutputStream(new FileOutputStream(args[1]));
        byteCode bc = new byteCode(output);

        //Map symbolTable
        // key: symbol; value: Pair <offset on the stack, data type>
        //                     Pair <int, String>
        Map<String, Pair> symbolTable = new HashMap<>();

        // wait list of undefined jmp or call
        // Pair <byte offset, flabel + label>
        ArrayList<Pair> waitList = new ArrayList<>();

        String flabel = "main"; //label of current subroutine

        // hard coded call to main function
        bc.pushi(16);
        bc.pushi(17);
        bc.pushi(1);
        bc.call(0);
        bc.halt();

        // read every line in input text
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                if (line.matches("//")){
                    continue;
                }
                if(line.matches("decl[A-Za-z 0-9]+")) {
                    Pattern pattern = Pattern.compile("decl ([a-zA-Z]+) ([a-z]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("decl Error!");
                    }
                    String symbol = matcher.group(1);
                    String dataType = matcher.group(2);
                    if(dataType.equals("short")) {
                        bc.pushs((short) 0);
                    }
                    if(dataType.equals("int")) {
                        bc.pushi(0);
                    }
                    if(dataType.equals("float")) {
                        bc.pushf(0);
                    }
                    symbolTable.put(flabel + symbol,
                                    new Pair(bc.getStackPointer()  - bc.getfsp(), dataType));
                    continue;
                }

                if(line.matches("lab [A-Za-z0-9]+")){
                    Pattern pattern = Pattern.compile("lab ([A-Za-z0-9]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("lab Error!");
                    }
                    String label = matcher.group(1);
                    symbolTable.put(flabel + label, new Pair(bc.getPC() + 1, "int"));
                    continue;
                }

                if(line.matches("subr[A-Za-z 0-9]+")) { //unfinished
                    Pattern pattern = Pattern.compile("subr ([0-9]+) ([A-Za-z]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("subr Error!");
                    }
                    continue;
                }

                // if (line.matches("retr .*?")){
                //     Pattern pattern = Pattern.compile("retr ([A-Za-z]+)");
                //     Matcher matcher = pattern.matcher(line);
                //     if(!matcher.find()) {
                //         System.out.println("retr Error!");
                //     }
                //     String var = matcher.group(1);
                //     continue;
                // }

                if (line.matches("ret")){
                    bc.pushi(0);
                    bc.popa(0);
                    bc.ret();
                    continue;
                }

                if (line.matches("printv .*?")){
                    Pattern pattern = Pattern.compile("printv ([a-zA-Z]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("printv Error!");
                    }
                    String var = matcher.group(1);
                    Pair pair = symbolTable.get(flabel + var);
                    String dataType = pair.getValue();
                    int offset = pair.getKey();
                    if(dataType.equals("int")) {
                        bc.pushi(offset);
                        bc.pushvi();
                        bc.printi();
                    }
                    if(dataType.equals("short")) {
                        bc.pushi(offset);
                        bc.pushvs();
                        bc.prints();
                    }
                    if(dataType.equals("float")) {
                        bc.pushi(offset);
                        bc.pushvf();
                        bc.printf();
                    }
                    continue;
                }

                if (line.matches("print.*?")){
                    String[] allinfor = line.split(" ");
                    String type = allinfor[0];
                    String literal = allinfor[1];
                    if(type.equals("printi")) {
                        bc.pushi(Integer.parseInt(literal));
                        bc.printi();
                    }
                    if(type.equals("prints")) {
                        bc.pushs(Short.parseShort(literal));
                        bc.prints();
                    }
                    if(type.equals("printf")) {
                        bc.pushf(Float.parseFloat(literal));
                        bc.printf();
                    }
                    continue;
                }

                if (line.matches("jmp .*?")){
                    Pattern pattern = Pattern.compile("jmp ([a-zA-Z0-9]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("jmp Error!");
                    }
                    String label = matcher.group(1);
                    Pair pair = symbolTable.get(flabel + label);
                    if(pair == null) {
                        bc.pushi(0);
                        waitList.add(new Pair(bc.getPC() - 3, flabel + label));
                        bc.jmp();
                    } else {
                        bc.pushi(pair.getKey());
                        bc.jmp();
                    }
                    continue;
                }

                if (line.matches("jmpc .*?")){
                    Pattern pattern = Pattern.compile("jmpc ([a-zA-Z0-9]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("jmpc Error!");
                    }
                    String label = matcher.group(1);
                    Pair pair = symbolTable.get(flabel + label);
                    if(pair == null) {
                        bc.pushi(0);
                        waitList.add(new Pair(bc.getPC() - 3, flabel + label));
                        bc.jmpc();
                    } else {
                        bc.pushi(pair.getKey());
                        bc.jmpc();
                    }
                    continue;
                }

                if (line.matches("cmpe")){
                    bc.cmpe();
                    continue;
                }

                if (line.matches("cmplt")){
                    bc.cmplt();
                    continue;
                }

                if (line.matches("cmpgt")){
                    bc.cmpgt();
                    continue;
                }

//                if (line.matches("call .*?")){
//                    String[] allinfor = line.split(" ");
//                    List<String> vara = new ArrayList<String>();
//                    int cnt = Integer.parseInt(allinfor[1]);
//                    for (int i = 2; i < allinfor.length - 1; i++)
//                        vara.add(allinfor[i]);
//                    String flabelCalled = allinfor[allinfor.length - 1];
//                    bc.pushi(bc.getPC() + 1);
//                    bc.add();
//                    for (String varai: vara){
//                        bc.pushi(Integer.parseInt(varai));
//                        bc.pushv();
//                    }
//                    continue;
//                }

                // if (line.matches("callr .*?")){
                //     String[] allinfor = line.split(" ");
                //     List<String> vara = new ArrayList<String>();
                //     int cnt = Integer.parseInt(allinfor[1]);
                //     for (int i = 2; i < allinfor.length - 1; i++)
                //         vara.add(allinfor[i]);
                //     String flabelCalled = allinfor[allinfor.length - 1];
                //     continue;
                // }

                if (line.matches("push[a-z] .*?")){
                   Pattern pattern = Pattern.compile("push([a-z]) ([a-zA-Z0-9]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("push Error!");
                    }
                    String type = matcher.group(1);
                    String val = matcher.group(2);
                    if(type.equals("i")) {
                        bc.pushi(Integer.parseInt(val));
                    }
                    if(type.equals("s")) {
                        bc.pushs(Short.parseShort(val));
                    }
                    if(type.equals("f")) {
                        bc.pushf(Float.parseFloat(val));
                    }

                    // pushv
                    if (type.equals("v")){
                        Pair pair = symbolTable.get(flabel + val);
                        String datatype = pair.getValue();
                        int offset = pair.getKey();
                        if (datatype.equals("int")){
                            bc.pushi(offset);
                            bc.pushvi();
                        }
                        if (datatype.equals("short")){
                            bc.pushi(offset);
                            bc.pushvs();
                        }
                        if (datatype.equals("float")){
                            bc.pushi(offset);
                            bc.pushvf();
                        }
                    }
                    continue;
                }

                if (line.matches("popm .*?")){
                    Pattern pattern = Pattern.compile("popm ([0-9]+)");
                     Matcher matcher = pattern.matcher(line);
                     if(!matcher.find()) {
                         System.out.println("popm Error!");
                     }
                     int val = Integer.parseInt(matcher.group(1));
                     bc.pushi(val);
                     bc.popm(val);
                     continue;
                 }

                if (line.matches("popv .*?")){
                    Pattern pattern = Pattern.compile("popv ([a-zA-Z0-9]+)");
                    Matcher matcher = pattern.matcher(line);
                    if(!matcher.find()) {
                        System.out.println("popv Error!");
                    }
                    String var = matcher.group(1);//variable
                    Pair pair = symbolTable.get(flabel + var);
                    bc.pushi(pair.getKey());
                    bc.popv();
                    continue;
                 }

                if (line.matches("peek .*?")){
                    String[] allinfor = line.split(" ");
                    String var = allinfor[1]; // variable
                    int val = Integer.parseInt(allinfor[2]);
                    Pair pair = symbolTable.get(flabel + var);
                    int offset = pair.getKey();
                    String dataType = pair.getValue();
                    if(dataType.equals("int")) {
                        bc.pushi(offset);
                        bc.pushi(val);
                        bc.peeki();
                    }
                    if(dataType.equals("short")) {
                        bc.pushi(offset);
                        bc.pushi(val);
                        bc.peeks();
                    }
                    if(dataType.equals("float")) {
                        bc.pushi(offset);
                        bc.pushi(val);
                        bc.peekf();
                    }
                    continue;
                 }

                if (line.matches("poke .*?")){
                    String[] allinfor = line.split(" ");
                    String var = allinfor[2]; // variable
                    int val = Integer.parseInt(allinfor[1]);
                    Pair pair = symbolTable.get(flabel + var);
                    int offset = pair.getKey();
                    String dataType = pair.getValue();
                    if(dataType.equals("int")) {
                        bc.pushi(offset);
                        bc.pushi(val);
                        bc.pokei();
                    }
                    if(dataType.equals("short")) {
                        bc.pushi(offset);
                        bc.pushi(val);
                        bc.pokes();
                    }
                    if(dataType.equals("float")) {
                        bc.pushi(offset);
                        bc.pushi(val);
                        bc.pokef();
                    }
                    continue;
                }

                if (line.matches("swp")){
                    bc.swp();
                    continue;
                }

                if (line.matches("add")){
                    bc.add();
                    continue;
                }

                if (line.matches("sub")){
                    bc.sub();
                    continue;
                }

                if (line.matches("mul")){
                    bc.mul();
                    continue;
                }

                if (line.matches("div")){
                    bc.div();
                    continue;
                }
            }
        }
        catch (IOException e) {
            System.out.println("File not found!!!");
        }

        for(Pair each: waitList) {
            Pair pair = symbolTable.get(each.getValue());
            int offset = pair.getKey();
            bc.setArr(each.getKey(), offset);
        }
        bc.writeToFile();
        output.close();
    }
}
