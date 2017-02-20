import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bikash on 1/1/17.
 */
class Gen{
    int index=0;
    int memory =0;
    int ithLabel = 0;
    public static HashMap<String, String> mem = new HashMap<>();
    Stack<Character> operatorStack = new Stack<>();
    Stack<ExprNode> exprStack = new Stack<>();
    public static ArrayList<String> machinecode = new ArrayList<>();
    public static ArrayList<String> threeBitCode = new ArrayList<>();

    public static void main(String[] args) {
        HashMap<String,ExprNode> statement = new HashMap<>();
        Gen tree = new Gen();
        BufferedReader br = null;

        try{
            String sCurrentLine;
            br = new BufferedReader(new FileReader("/home/bikash/IdeaProjects/intermediateCodeGenetation/src/code.txt"));

            while ((sCurrentLine = br.readLine()) != null) {
                sCurrentLine = sCurrentLine.replace(" ","");
                String []tok = sCurrentLine.split("=");
                ExprNode parent = tree.statementParser("("+tok[1]+")");
                parent.name=tok[0];
                statement.put(tok[0],parent);
                tree.depthFirstSearch(parent);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (br!=null)
                    br.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }

        }


        String code = "";
        for (String machine: machinecode) {
            code= code+machine+"\n";
        }
        try(Writer writer = new BufferedWriter(new FileWriter("/home/bikash/IdeaProjects/intermediateCodeGenetation/src/machinecode"))) {
            writer.write(code);


        }catch (IOException e) {
            e.printStackTrace();
        }

        code = "";
        for (String threebit: threeBitCode) {
            code= code+threebit+"\n";
        }
        try(Writer writer = new BufferedWriter(new FileWriter("/home/bikash/IdeaProjects/intermediateCodeGenetation/src/3bitcode.txt"))) {
            writer.write(code);


        }catch (IOException e) {
            e.printStackTrace();
        }

        forParser("");

    }


    public ExprNode statementParser(String input)  {
        for (int i=0; i< input.length(); i++) {
            Character c = input.charAt(i);
            if(c=='(')
                operatorStack.push(c);
            else if(Character.isDigit(c)){
                String temp= c.toString();
                while (Character.isDigit(input.charAt(i+1))){
                    temp = temp+""+input.charAt(i+1);
                    i++;
                }
                exprStack.push(new ExprNode(temp));
            }else if(Character.isAlphabetic(c)){
                String temp= c.toString();
                while (Character.isAlphabetic(input.charAt(i+1))){
                    temp = temp+""+input.charAt(i+1);
                    i++;
                }
                exprStack.push(new ExprNode(temp));
            }

            else if(isOperator(c.toString())){
                while (precedence(getTop(operatorStack))>=precedence(c)){
                    Character operator;
                    ExprNode e2, e1;
                    operator = operatorStack.pop();
                    e2 = exprStack.pop();
                    e1 = exprStack.pop();
                    exprStack.push(new ExprNode(operator.toString(),e1,e2,"E"+index++));
                }
                operatorStack.push(c);
            }

            else if(c==')'){
                while (getTop(operatorStack)!='('){
                    Character operator = operatorStack.pop();
                    ExprNode e2 = exprStack.pop();
                    ExprNode e1 = exprStack.pop();
                    exprStack.push(new ExprNode(operator.toString(),e1,e2,"E"+index++));
                }
                operatorStack.pop();
            }
        }

        return exprStack.pop();
    }

    public static  ExprNode forParser(String code){
        String pattern = "for\\(.*?\\)\\{.*?\\}";
        code = "for(i=0;i<5; i++){a=2+3;b=4+5}";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(code);

        while (m.find()){
            System.out.println(m.group(0));
            pattern = "\\(.*?\\)";
            System.out.println(pattern);
            r = Pattern.compile(pattern);
            Matcher ma = r.matcher(code);
            if(ma.find()){
                //System.out.println(ma.group());
                String st = ma.group();
                String[] sta = st.substring(1,st.length()-1).replace(" ","").split(";");
                threeBitCode.add(sta[0]);
                pattern = "\\{.*?\\}";
                r=Pattern.compile(pattern);
                ma = r.matcher(code);
                if (ma.find()){
                    //System.out.println(ma.group());
                    String statements = ma.group();
                    String[] statement = statements.substring(1,statements.length()-1).replace(" ","").split(";");
                    for (String stat: statement) {
                        System.out.println(stat);
                        new Gen().statementParser("("+stat.split("=")[1]+")");
                    }
                    System.out.println(Arrays.asList(statement));
                }
                //System.out.println(pattern);
            }

        }
        return  new ExprNode("a");
    }



    public boolean isOperator(String c) { // Tell whether c is an operator.
        return c.equals("+")   ||  c.equals("-")  ||  c.equals("*")  ||  c.equals("/") ||  c.equals("^");
    }

    public void depthFirstSearch(ExprNode exprNode){
        if(isOperator(exprNode.c)){
            depthFirstSearch(exprNode.left);
            depthFirstSearch(exprNode.right);
            threeBitCode.add(exprNode.name+"="+exprNode.left.name+exprNode.c+exprNode.right.name);
            toMachineCode(exprNode.name, exprNode.left.name, exprNode.c, exprNode.right.name);
        }

    }



    public void toMachineCode(String var , String oprand1, String operator, String oprand2){
        switch (operator){
            case "+":
                if(oprand1.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI A, "+Integer.toHexString(Integer.parseInt(oprand1))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand1));
                }

                if(oprand2.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI B, "+Integer.toHexString(Integer.parseInt(oprand2))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand2));
                    machinecode.add("MOV B,A");
                }

                machinecode.add("ADD B");
                machinecode.add("STA "+ addressBit(++memory));
                break;
            case "-":
                if(oprand1.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI A, "+Integer.toHexString(Integer.parseInt(oprand1))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand1));
                }

                if(oprand2.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI B, "+Integer.toHexString(Integer.parseInt(oprand2))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand2));
                    machinecode.add("MOV B,A");
                }
                machinecode.add("SUB B");
                machinecode.add("STA "+ addressBit(++memory));
                break;
            case "*":
                if(oprand1.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI B, "+Integer.toHexString(Integer.parseInt(oprand1))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand1));
                    machinecode.add("MOV B, A");
                }

                if(oprand2.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI C, "+Integer.toHexString(Integer.parseInt(oprand2))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand2));
                    machinecode.add("MOV C,A");
                }

                machinecode.add("MVI A, 00H");
                machinecode.add("REPEAT"+ ++ithLabel+": ADD B");
                machinecode.add("DCR C");
                machinecode.add("JZ DONE"+ithLabel);
                machinecode.add("JMP REPEAT"+ithLabel);
                machinecode.add("DONE"+ithLabel+": STA "+addressBit(++memory));
                break;
            case "/":
                if(oprand1.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI B, "+Integer.toHexString(Integer.parseInt(oprand1))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand1));
                    machinecode.add("MOV B, A");
                }

                if(oprand2.chars().allMatch(Character::isDigit))
                    machinecode.add("MVI C, "+Integer.toHexString(Integer.parseInt(oprand2))+"H");
                else {
                    machinecode.add("LDA "+mem.get(oprand2));
                    machinecode.add("MOV C,A");
                }

                machinecode.add("MVI A, 00H");

                machinecode.add("REPEAT"+ ++ithLabel+": SUB B");
                machinecode.add("DCR C");
                machinecode.add("JZ DONE"+ithLabel);
                machinecode.add("JMP REPEAT"+ithLabel);
                machinecode.add("DONE"+ithLabel+": STA "+addressBit(++memory));
                break;
            case "^":
                //mnemonics+= ": POW ";
                break;

        }

        mem.put(var,addressBit(memory));
    }

    public static String addressBit(int num){
        String binString = Integer.toBinaryString(num);

        for (int i = binString.length(); i < 4; i++) {
            binString = "0"+binString;
        }
        return binString;
    }

    public int precedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return 2;

            case '*':
            case '/':
                return 3;

            case '^':
                return 4;

            default:
                return 0;
        }

    }

    Character getTop(Stack<Character> stack){
        if(stack.size()==0){
            return 'ϵ';
        }

        Character top = stack.pop();
        stack.push(top);
        return top;
    }


}