package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class InequalityExpression extends GuardExpression implements LeftRightGuardExpression{
    private ColorExpression left;
    private ColorExpression right;

    public InequalityExpression(ColorExpression left, ColorExpression right) {
        this(left, right, null);
    }
    public InequalityExpression(ColorExpression left, ColorExpression right, ColorType colorType) {
        this.left = left;
        this.right = right;
        this.colorType = colorType;
    }
    public ColorExpression getLeftExpression() {
        return this.left;
    }
    public ColorExpression getRightExpression() {
        return this.right;
    }

    @Override
    public boolean containsColor(Color color) {
        return left.containsColor(color) || right.containsColor(color);
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (this == object1 && object2 instanceof GuardExpression) {
            GuardExpression obj2 = (GuardExpression)object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            left = left.replace(object1, object2,replaceAllInstances);
            right = right.replace(object1, object2,replaceAllInstances);
            colorType = left.getColorType();
            return this;
        }
    }
    @Override
    public GuardExpression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public void getValues(ExprValues exprValues) {
        left.getValues(exprValues);
        right.getValues(exprValues);
    }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[2];
        int endPrev = 0;

        int start = 0;
        int end = 0;

        end = start + left.toString().length();
        endPrev = end;
        ExprStringPosition pos = new ExprStringPosition(start, end, left);
        children[0] = pos;
        start = endPrev + 4;
        end = start + right.toString().length();
        pos = new ExprStringPosition(start, end, right);
        children[1] = pos;

        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InequalityExpression) {
            InequalityExpression expr = (InequalityExpression) o;
            return (left.equals(expr.left) && right.equals(expr.right));
        }
        return false;
    }

    @Override
    public GuardExpression copy() {
        return new InequalityExpression(left.copy(), right.copy());
    }

    @Override
    public boolean containsPlaceHolder() {
        return left.containsPlaceHolder() || right.containsPlaceHolder();
    }

    @Override
    public Expression findFirstPlaceHolder() {
        if(left.containsPlaceHolder()){
            return left.findFirstPlaceHolder();
        } else if(right.containsPlaceHolder()){
            return right.findFirstPlaceHolder();
        } else{
            return null;
        }
    }

    public void getVariables(Set<Variable> variables) {
        left.getVariables(variables);
        right.getVariables(variables);
    }

    //Missing implementation for evaluation - might not be needed
    public Boolean eval(ExpressionContext context) {
        return null;
    }

    @Override
    public String toString() {
        return left.toString() + " != " + right.toString();
    }
}
