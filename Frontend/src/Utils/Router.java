package Utils;

import Controllers.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Router {
    private Node root;
    private Node directory;

    public Router() {
        root = new Node("root");
        root.controller = new Controller();
        directory = root;
    }

    public void addPath(String path) {
        addPath(path, null);
    }

    public void addPath(String path, Controller controller) {
        String[] pathArr = path.split("/");

        Node n = root;
        for(int i = 0; i < pathArr.length; i++) {
            boolean brk = false;
            for(Node child : n.children) {
                if (child.path.equals(pathArr[i])) {
                    n = child;
                    brk = true;
                    break;
                }
            }
            if(!brk) {
                Node newChild = new Node(pathArr[i]);
                n.addChild(newChild);
                n = newChild;
            }
            if(i == pathArr.length - 1) { //last element
                n.controller = controller;
            }
        }
    }

    public void goTo(String path) {
        String[] pathArr = path.split("/");

        Node n = root;
        for(String str : pathArr) {
            boolean brk = false;
            for(Node child : n.children) {
                if (child.path.equals(str)) {
                    n = child;
                    brk = true;
                    break;
                }
            }
            if(!brk) {
                return;
            }
        }
        directory = n;
    }

    public void goBack() {
        goBack(1);
    }

    public void goBack(int num) {
        Node n = directory;
        for(int i = 0; i < num; i++) {
            if(n.parent != null) {
                n = n.parent;
            }
            else {
                break;
            }
        }
        directory = n;
    }

    public String getPath() {
        String str = "";
        Node n = directory;
        while(!n.equals(root)) {
            str = n.path + "/" + str;
            n = n.parent;
        }
        str = str.substring(0, str.length() - 1); //Removes extra slash at end
        return str;
    }

    public Controller getController() {
        Node n = directory;
        while(n.controller == null) {
            n = n.parent;
        }
        return n.controller;
    }


    public String getPathPretty() {
        Node n = directory;
        String str = "";
        while(n != null) {
            str = " > " + n.path + str;
            n = n.parent;
        }
        str = "Famazon" + str;
        return str;
    }

    static class Node {
        String path;
        Node parent;
        List<Node> children;
        Controller controller;
        Node(String path) {
            this.path = path;
            this.children = new ArrayList<Node>();
        }

        void addChild(Node child) {
            children.add(child);
            if(child.parent != null && !child.parent.equals(this)) {
                child.setParent(this);
            }
        }

        void setParent(Node parent) {
            this.parent = parent;
            if(!parent.children.contains(this)) {
                parent.addChild(this);
            }
        }
    }
}
