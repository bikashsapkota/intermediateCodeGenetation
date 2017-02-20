
/**
 * Created by bikash on 12/15/16.
 */
public class ExprNode {

    String c;
    String name;
    ExprNode left;
    ExprNode right;

    ExprNode(String num){
        this.c=num;
        this.name= num+"";
    }

    ExprNode(String op, ExprNode e1, ExprNode e2, String name){
        this.name = name;
        this.c=op;
        this.left = e1;
        this.right = e2;
    }
}