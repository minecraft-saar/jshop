package umd.cs.shop;

import java.util.Vector;


public class MCTSNode {
    static int NEXT_ID = 0;
    private int id;

    private JSTState tState;
    private JSTasks taskNetwork;
    private int visited;
    private int solvedVisits;
    private double cost;
    Vector<MCTSNode> children;
    private boolean inTree = false;
    JSPlan plan;
    private boolean deadEnd = false;

    private boolean fullyExplored = false;

    //private JSTaskAtom primitiveAction; // method that generated this state
    //boolean primitive = false;

    MCTSNode(JSTState state, JSTasks tasks, JSPlan plan) {
        this.tState = state;
        this.taskNetwork = tasks;
        this.visited = 0;
        this.solvedVisits = 0;
        this.cost = Double.POSITIVE_INFINITY;
        this.plan = new JSPlan();
        this.plan.addElements(plan);
        this.children = new Vector<MCTSNode>();
        this.id = NEXT_ID++;
    }

    MCTSNode(JSTState state, JSPlan plan) {
        this.tState = state;
        this.plan = new JSPlan();
        this.plan.addElements(plan);
        this.cost = Double.POSITIVE_INFINITY;
        this.visited = 0;
        this.solvedVisits = 0;
        this.id = NEXT_ID++;
    }

    JSTState tState() {
        return this.tState;
    }

    JSTasks taskNetwork() {
        return this.taskNetwork;
    }

    void incVisited() {
        this.visited += 1;
    }

    void incSolvedVisits() {
        this.solvedVisits += 1;
    }

    double getCost() {
        return this.cost;
    }

    int visited() {
        return this.visited;
    }

    int solvedVisits() {
        return this.solvedVisits;
    }

    /**
     * Also sets fullyExplored
     **/
    void setDeadEnd(JSJshopVars vars) {
        this.deadEnd = true;
        this.fullyExplored = true;
        if (vars.landmarks) {
            double sum = (tState().state().taskLandmarks.size() + tState.state().factLandmarks.size()) + this.plan.planCost;
            JSUtil.println("Sum: " + sum);
            double multiple = sum * 3;
            this.setCost(multiple);
            JSUtil.println("Task Landmark cost at Dead End: " + tState().state().taskLandmarks.size() + " Fact landmark cost: " + tState().state().factLandmarks.size() + " Plan Cost: " + this.plan.planCost + " Node ID: " + this.id);
            JSUtil.println("Node Cost: " + this.cost);
        } else {
            this.setCost(Double.POSITIVE_INFINITY);
        }
    }

    void setCost(double r) {
        if (Double.isNaN(r)) {
            for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                System.out.println(ste);
            }
            System.exit(0);
        }
        this.cost = r;
    }

    void setFullyExplored() {
        this.fullyExplored = true;
    }

    void setInTree() {
        this.inTree = true;
    }

    boolean isDeadEnd() {
        return this.deadEnd;
    }

    boolean isInTree() {
        return this.inTree;
    }

    public boolean isFullyExplored() {
        return fullyExplored || deadEnd;
    }

    public void checkFullyExplored(JSJshopVars vars) {
        this.fullyExplored = true;
        this.deadEnd = true;
        for (MCTSNode c : children) {
            if (!c.fullyExplored) {
                this.fullyExplored = false;
                this.deadEnd = false;
                return;
            }
            if (!c.deadEnd) {
                this.deadEnd = false;
            }
        }
        if (this.deadEnd){
            this.setDeadEnd(vars);
        }
    }

    void expand(JSJshopVars vars) {

        if (!this.children.isEmpty() || this.isDeadEnd()) {
            //Node was already expanded
            return;
        }
        Vector<MCTSNode> newChildren = vars.expansionPolicy.expand(this);
        this.children.addAll(newChildren);
        this.checkFullyExplored(vars);
        if (this.children.isEmpty()) {
            this.setDeadEnd(vars);
        }
    }


    public void setGoal(JSJshopVars vars) {
        vars.policy.computeCost(this); // sets cost
        this.setFullyExplored();
        if (this.isInTree()) {
            this.incSolvedVisits();
            this.incVisited();
        }
    }


    public String dotTree() {
        String result = "digraph UCT {";
        result += dotNode();
        result += "}";
        return result;

    }

    public String tasks_string() {
        String res = "";
        for (Object o : this.taskNetwork.predicates) {
            res += o.toString() + "   ";
        }
        return res;


    }

    public String dotNode() {
        String color = "";
        String taskName = "";
        if (!this.taskNetwork.predicates.isEmpty()) {
            taskName = this.taskNetwork.predicates.firstElement().toString() + " ";
            // for (Object o : this.taskNetwork) {
            //     taskName += o.toString() + " ";
            // }
        }
        String label = taskName + this.cost + " " + this.visited + " " + this.id;
        if (!this.inTree) {
            label = label + " n";
        }
        if (this.deadEnd) {
            color = " style=filled fillcolor=red";
        } else if (this.fullyExplored) {
            color = " style=filled fillcolor=green";
        } else if (!this.inTree) {
            color = " style=filled fillcolor=grey";
        }

        String result = this.id + " [label=\"" + label + "\"" + color + "];\n";

        int num_empty_children = 0;
        int empty_children_id = 0;

        for (MCTSNode child : children) {
            /*while (child.children.size() == 1 && child.children.get(0).inTree) {
                child = child.children.get(0);
            }*/

            if (child.inTree || !child.children.isEmpty() || child.isFullyExplored()) {
                result += this.id + " -> " + child.id + ";\n";
                result += child.dotNode();
            } else {
                num_empty_children++;
                empty_children_id = child.id; //Re-use the ID of some empty children for the empty_children node.
            }
        }

        if (num_empty_children > 0) {
            result += "" + empty_children_id + " [label=\"(" + num_empty_children + ")\", style= filled fillcolor=gray];\n";
            result += this.id + "->" + empty_children_id + ";\n";
        }

        return result;
    }

}
