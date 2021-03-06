package umd.cs.shop;

import java.io.*;

import java.util.*;

public class JSOperator {

    /*==== instance variables ====*/

    private JSTaskAtom head;

    private JSListLogicalAtoms precondition;
    private JSListLogicalAtoms deleteList;

    private JSListLogicalAtoms addList;

    private double cost = 1;


    /**** constructor ****/

    JSOperator() {
        super();
    }


    JSOperator(StreamTokenizer tokenizer) {

        head = new JSTaskAtom(tokenizer);
        precondition = new JSListLogicalAtoms(tokenizer);
        deleteList = new JSListLogicalAtoms(tokenizer);
        addList = new JSListLogicalAtoms(tokenizer);


        if (!JSUtil.readToken(tokenizer, "Expecting ) or operator cost"))
            throw new JSParserError(); //return;

        if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
            cost = tokenizer.nval;
        else
            tokenizer.pushBack();
    }

    public void print() {

        JSUtil.print("(");
        JSUtil.print(":Operator ");
        head.print();
        precondition.print();
        JSUtil.print("-: ");
        deleteList.print();
        JSUtil.print("+: ");
        addList.print();
        //if (cost != 1)
        JSUtil.print(cost + " ");
        JSUtil.println(")");

//       JSUtil.flag("<-- operator");
    }

    public JSTaskAtom head() {
        return head;
    }

    public JSListLogicalAtoms addList() {
        return addList;
    }

    public JSListLogicalAtoms deleteList() {
        return deleteList;
    }

    public JSListLogicalAtoms precondition() {
        return precondition;
    }

    public double cost() {
        return cost;
    }

    public JSOperator standarizerOp(JSJshopVars vars) {
        JSOperator newOp = new JSOperator();
        JSTaskAtom ta = this.head();
        JSListLogicalAtoms pre = this.precondition();
        JSListLogicalAtoms addL = this.addList();
        JSListLogicalAtoms delL = this.deleteList();

        newOp.head = ta.standarizerTA(vars);
        newOp.deleteList = delL.standarizerListLogicalAtoms(vars);
        newOp.addList = addL.standarizerListLogicalAtoms(vars);
        newOp.precondition = pre.standarizerListLogicalAtoms(vars);
        newOp.cost = cost;
        return newOp;
    }

    public boolean equals(Object o) {
        if (!(o instanceof JSOperator))
            return false;

        JSOperator op = (JSOperator) o;

        if (!precondition.equals(op.precondition))
            return false;

        if (!addList.equals(op.addList)) {
            return false;
        }

        if (!deleteList.equals(op.deleteList)) {
            return false;
        }

        if (!head.equals(op.head))
            return false;

        Double thisCost = cost;
        Double otherCost = op.cost;
        return thisCost.equals(otherCost);
    }

    public int hashCode() {
        return Objects.hash(precondition, addList, deleteList, head, cost);
    }

}



