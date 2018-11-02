/**
 * code by steve@zaske.com
 * 
 * limitations:
 * 1. All entered numbers are limited to 9 digits in length, be it whole numbers, numerators or denominators.
 * 
 */

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FractionCalc {

    public static boolean isValidFunction(String input){
        // Regex explained:
        // see: https://regex101.com/r/WQ6POg/2
        //
        // ^[\\d\\/_-]* = any set of digits underscores and hyphens
        // followed by
        // \\s+ = one or more spaces
        // followed by
        // [\\-+*\\/] = one -,+,* or /
        // followed by
        // ^[\\d\\/_-]* = any set of digits underscores and hyphens
        //
        return Pattern.matches("^[\\d\\/_-]*\\s+[\\-+*\\/]\\s+[\\d\\/_-]*$", input);
    }

    public static boolean isValidFractional(String input){
        return isWholeNumberOnly(input) || isFractionOnly(input) || isWholeWithFraction(input);
    }

    static boolean isWholeNumberOnly(String input){
        return Pattern.matches("^-?\\d{1,9}$", input);
    }

    static boolean isWholeWithFraction(String input){
        /** 
         * see: https://regex101.com/r/xeiO8N/3 
         * 
         * Regex explained:
         * 1. Cannot have a whole number that is 0
         * 2. Cannot have a whole number that is -0
         * 3. Whole number limited to 9 digits
         * 4. Must include a fraction
         * 5. fraction numerator or denominator cannot be negative
         * 
         **/

        return Pattern.matches("(?=-?\\d{1,9}_{1}\\d{1,9}\\/{1}\\d{1,9}$)(?=^(?!0_).+)(?=^(?!-0_).+).*$", input);
    }

    static boolean isFractionOnly(String input){
        // you cannot have a negative fraction
        return Pattern.matches("^-?\\d{1,9}\\/{1}\\d{1,9}$", input);
    }

    static FractionalNumber stringToFractional(String input)
        throws ArithmeticException {
        // the results returned
        FractionalNumber results = new FractionalNumber();

        // check for valid input
        if(isValidFractional(input)){

            // Three types of valid numbers can be entered, lets check for them...
            // a whole number only
            if(isWholeNumberOnly(input)){
                results.setNumerator((int)Integer.parseInt(input)).setDenominator(1);
            } else 
            if (isFractionOnly(input)){
                String[] fractionArr =input.split("/"); 
                int numerator = (int)Integer.parseInt(fractionArr[0]);
                int denominator = (int)Integer.parseInt(fractionArr[1]);
                results.setNumerator(numerator).setDenominator(denominator);
            } else 
            if(isWholeWithFraction(input)){
                // get whole number
                String[] numArr =input.split("_");
                int wholeNum = (int)Integer.parseInt(numArr[0]);
                if(wholeNum == 0) throw new ArithmeticException("Int overflow error"); // should never happen as my validation code is blocking it

                // get fraction
                String[] fractionArr =numArr[1].split("/");; 
                int numerator = (int)Integer.parseInt(fractionArr[0]);
                int denominator = (int)Integer.parseInt(fractionArr[1]);

                // add whole to fraction
                if(wholeNum < 0){
                    numerator *= -1; // converts numerator to same sign as whole
                }
                numerator += (wholeNum*denominator);
                results.setNumerator(numerator)
                    .setDenominator(denominator);
            }
        } else {
            throw new IllegalArgumentException("Invalid formula entered");
        }
        return results.simplify();
    }

   public static void main(String[] args) {
        Scanner scr = new Scanner(System.in);
        String originalEquationString;
        FractionalNumber first = new FractionalNumber();
        FractionalNumber second = new FractionalNumber();
        String operator; 
        FractionalNumber finalResults = new FractionalNumber();

        // Get input from user
        try {
            System.out.print("? ");           
            originalEquationString = scr.nextLine();
        } finally {
            scr.close();
        }

        // Convert input to FractionalNumber's
        if(isValidFunction(originalEquationString)){
            try {

                // retrieve variables
                String[] functionArr = originalEquationString.split("\\s+"); 

                first = stringToFractional(functionArr[0]);
                operator = functionArr[1];
                second = stringToFractional(functionArr[2]);

                // perform operation
                switch (operator) { 
                    case "+": 
                        finalResults = FractionalNumber.add(first, second); 
                        break; 
                    case "-" : 
                        finalResults = FractionalNumber.subtract(first, second); 
                        break; 
                    case "*": 
                        finalResults = FractionalNumber.multiply(first, second); 
                        break; 
                    case "/": 
                        finalResults = FractionalNumber.divide(first, second); 
                        break; 
                    default: 
                        System.out.println("invalid operator");
                        break; 
                    } 

                // deliver results
                System.out.println("= " + finalResults); 

            } catch (Exception e) {
                System.out.println("Oops, we encountered an error: " + e.getMessage());
           }
        } else {
            System.out.println("Sorry your formula was not valid");    
        }
   }
}

class FractionalNumber {

    // Instance fields
    int numerator; // top part
    int denominator; // bottom part

    public FractionalNumber(){
    }

    public FractionalNumber(int num, int den){
        this.numerator = num;
        this.denominator = den;
    }
  
    public FractionalNumber setNumerator(int num) 
    { 
        this.numerator = num; 
        return this; 
    } 
  
    public FractionalNumber setDenominator(int num) 
    { 
        this.denominator = num; 
        return this; 
    }

    // Return the greatest common divisor
    private static int greatestCommonDivisor(int first, int second){
        if (second==0) return Math.abs(first); // sign does not matter
        return greatestCommonDivisor(second,first%second);
    }

    public FractionalNumber simplify(){
        int num = this.numerator;
        int den = this.denominator;
        int gcd = greatestCommonDivisor(num, den);
        this.numerator = num/gcd;
        this.denominator = den/gcd;
        return this;
    }

    public static FractionalNumber multiply(FractionalNumber first, FractionalNumber second){
        return new FractionalNumber(multipleAndCheck(first.numerator,second.numerator), multipleAndCheck(first.denominator,second.denominator))
            .simplify();
    }

    public static FractionalNumber divide(FractionalNumber dividend, FractionalNumber divisor){
        return new FractionalNumber(multipleAndCheck(dividend.numerator, divisor.denominator), multipleAndCheck(dividend.denominator,divisor.numerator))
            .simplify();
    }

    public static FractionalNumber add(FractionalNumber first, FractionalNumber second){
        //get common denominator
        int cd = multipleAndCheck(first.denominator, second.denominator);
        int newFirstNumerator = multipleAndCheck(first.numerator,second.denominator);
        int newSecondNumerator = multipleAndCheck(second.numerator,first.denominator);
        return new FractionalNumber(newFirstNumerator + newSecondNumerator, cd)
            .simplify();
    }

    public static FractionalNumber subtract(FractionalNumber first, FractionalNumber second){
        //get common denominator
        int cd = multipleAndCheck(first.denominator, second.denominator);
        int newFirstNumerator = multipleAndCheck(first.numerator,second.denominator);
        int newSecondNumerator = multipleAndCheck(second.numerator,first.denominator);
        return new FractionalNumber(newFirstNumerator - newSecondNumerator, cd)
            .simplify();
    }

    // Safety check to see if our operation exceeds int limits
    public static int multipleAndCheck(int x, int y)
        throws ArithmeticException {
        long results = (long)x * (long)y;
        if (results < Integer.MIN_VALUE || results > Integer.MAX_VALUE) {
            throw new ArithmeticException("Int overflow error");
        }
        return (int)results;
    }

    @Override
    public String toString() { 
        String results = "";
        int wholePrint = 0;
        int numeratorPrint = this.numerator;
        int denominatorPrint = this.denominator;

        // construct whole number portion
        if(numeratorPrint==0 && denominatorPrint==1){
            // construct whole number portion - zero result
            results += Integer.toString(wholePrint);
        } else {
            // construct whole number portion - non-zero result
            if(Math.abs(numeratorPrint) >= Math.abs(denominatorPrint)){
                // we need to add a whole number
                wholePrint = numeratorPrint / denominatorPrint;
                results += Integer.toString(wholePrint);
    
                numeratorPrint = Math.abs(numeratorPrint % denominatorPrint); // sign moved to whole number
            }
    
            // Add fraction component
            if(numeratorPrint != 0){
                if(results!="") results+="_";
                results += String.format(Integer.toString(numeratorPrint) + "/" + Integer.toString(denominatorPrint)); 
            }   
        }
        return results;
    }
}