package calc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static java.lang.Double.NaN;
import static java.lang.Math.pow;

/*
 *   A calculator for rather simple arithmetic expressions
 *
 *   This is not the program, it's a class declaration (with methods) in it's
 *   own file (which must be named Calculator.java)
 *
 *   NOTE:
 *   - No negative numbers implemented
 */
class Calculator {

    // Here are the only allowed instance variables!
    // Error messages (more on static later)
    final static String MISSING_OPERAND = "Missing or bad operand";
    final static String DIV_BY_ZERO = "Division with 0";
    final static String MISSING_OPERATOR = "Missing operator or parenthesis";
    final static String OP_NOT_FOUND = "Operator not found";

    String url = "https://api.mathjs.org/v4/?expr=";

    // Definition of operators
    final static String OPERATORS = "+-*/^";

    // Method used in REPL
    double eval(String expr) {
        if (expr.length() == 0) {
            return NaN;
        }
        List<String> tokens = tokenize(expr);
        List<String> postfix = infix2Postfix(tokens);
        System.out.println(postfix);
        return evalPostfix(postfix);
    }

    String[] generateRandomMathProblem(){
        Random rand = new Random();
        int len = rand.nextInt(10) + 10;

        String problem = "";
        for(int i=0; i<=len; i++){
            if(i%2==0) {
                problem += rand.nextInt(8)+1;
            }else{
                problem += OPERATORS.charAt(rand.nextInt(OPERATORS.length()));
            }
        }
        if(OPERATORS.contains(""+ problem.charAt(problem.length()-1))){
            problem += rand.nextInt(8)+1;
        }
        //System.out.println(problem);
        String problem2 = "";
        for(char c : problem.toCharArray()){
            if(c == '+'){
                problem2 += "%2B";
            }else if(c == '/'){
                problem2 += "%2F";
            }else if(c == '^'){
                problem2 += "%5E";
            }else{
                problem2 += "" + c;
            }
        }
        return new String[] {problem, problem2};
    }

    String getAnswer(String problem){

        String sendUrl = url + problem + "&precision=8";

        try {
            URL obj = new URL(sendUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "-1";
    }

    // ------  Evaluate RPN expression -------------------

    public double evalPostfix(List<String> postfix) {
        Stack<Double> stack = new Stack();

        for(String token : postfix){
            if(OPERATORS.contains(token)){
                double op1 = stack.pop();
                double op2 = stack.pop();

                stack.push(applyOperator(token, op1, op2));

            }else{
                stack.push(Double.parseDouble(token));
            }
        }
        if(stack.size() == 1){
            return stack.pop();
        }else{
            return -1;
        }
    }


    double applyOperator(String op, double d1, double d2) {
        switch (op) {
            case "+":
                return d1 + d2;
            case "-":
                return d2 - d1;
            case "*":
                return d1 * d2;
            case "/":
                if (d1 == 0) {
                    throw new IllegalArgumentException(DIV_BY_ZERO);
                }
                return d2 / d1;
            case "^":
                return pow(d2, d1);
        }
        throw new RuntimeException(OP_NOT_FOUND);
    }

    // ------- Infix 2 Postfix ------------------------

    public List<String> infix2Postfix(List<String> tokens) {
        Stack<String> stack = new Stack<>();
        List<String> postFix = new ArrayList<>();

        for(String token : tokens){

            //Check if token is a operator
            if(OPERATORS.contains(token)){
                while(!stack.isEmpty() && !stack.peek().equals("(") && hasHigherPrecedence(stack.peek(), token)){
                    postFix.add(stack.pop());
                }
                stack.push(token);
            }else if(token.equals("(")){
                stack.push(token);
            }else if(token.equals(")")){
                while(!stack.isEmpty() && !stack.peek().equals("(")){
                    postFix.add(stack.pop());
                }
                stack.pop();
            }else{
                postFix.add(token);
            }

        }
        while(!stack.isEmpty()){
            postFix.add(stack.pop());
        }

        return postFix; // TODO
    }


    boolean hasHigherPrecedence(String topOfStack, String token){
        if(token.equals("^")) {
            return getPrecedence(topOfStack) > getPrecedence(token);
        }
        return getPrecedence(topOfStack) >= getPrecedence(token);
    }


    int getPrecedence(String op) {
        if ("+-".contains(op)) {
            return 2;
        } else if ("*/".contains(op)) {
            return 3;
        } else if ("^".contains(op)) {
            return 4;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    Assoc getAssociativity(String op) {
        if ("+-*/".contains(op)) {
            return Assoc.LEFT;
        } else if ("^".contains(op)) {
            return Assoc.RIGHT;
        } else {
            throw new RuntimeException(OP_NOT_FOUND);
        }
    }

    enum Assoc {
        LEFT,
        RIGHT
    }

    // ---------- Tokenize -----------------------

    public List<String> tokenize(String expr) {


        for(int i=0; i<20; i++) {
            String[] problems = generateRandomMathProblem();
            String problem1 = problems[0];
            String problem2 = problems[1];


            Double answer = Double.parseDouble(getAnswer(problem2));
            Double Myanswer = evalPostfix(infix2Postfix(Arrays.asList(problem1.split(""))));
            String temp = String.valueOf(Myanswer);
            if(temp.equals("-Infinity") || temp.equals("Infinity")){
                Myanswer = -1.0;
            }else {
                Myanswer = Double.parseDouble(temp.substring(0, Math.min(9, temp.length() - 1)));
            }

            System.out.println(problem1);
            System.out.println("MathJS: \t\t" + answer + "\nMyAnswer: \t\t" + Myanswer + "\n");
        }


        return new ArrayList<>(Arrays.asList(expr.split("")));
    }

    // TODO Possibly more methods
}
