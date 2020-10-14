package Utils;

import java.util.List;
import java.util.Scanner;

public class IO {
    private Scanner in;

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
        System.out.print("> ");
        return in.nextLine();
    }

    public String question(String qstn) {
        System.out.println(qstn);
        return in.nextLine();
    }

    public String inlineQuestion(String qstn) {
        System.out.print(qstn + " ");
        return in.nextLine();
    }
}
