package Utils;

import java.util.List;
import java.util.Scanner;

public class IO {
    private Scanner in;
    private String lastResponse;

    public IO() {
        in = new Scanner(System.in);
    }

    public void print(String str) {
        System.out.println(str);
    }

    public void print(List<String> list) {
        for(String str : list) {
            System.out.println(str);
        }
    }

    public void print(String[] list) {
        for(String str : list) {
            System.out.println(str);
        }
    }

    public void error(String str) {
        System.out.println("Error: " + str);
    }

    public void error(List<String> list) {
        for(String str : list) {
            System.out.println("Error: " + str);
        }
    }

    public void error(String[] list) {
        for(String str : list) {
            System.out.println("Error: " + str);
        }
    }

    public String prompt() {
        System.out.print("$ ");
        String str = in.nextLine();
        lastResponse = str;
        return str;
    }

    public String question(String qstn) {
        return question(qstn, "");
    }

    public String question(String qstn, String def) {
        System.out.println(qstn);
        String str = in.nextLine();
        lastResponse = (str.equals("") ? def : str);
        return str;
    }

    public String inlineQuestion(String qstn) {
        return inlineQuestion(qstn, "");
    }

    public String inlineQuestion(String qstn, String def) {
        System.out.print(qstn + " ");
        String str = in.nextLine();
        lastResponse = (str.equals("") ? def : str);
        return str;
    }

    public String getLastResponse() {
        return lastResponse;
    }
}
