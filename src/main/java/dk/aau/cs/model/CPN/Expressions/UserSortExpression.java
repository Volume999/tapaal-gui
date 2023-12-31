package dk.aau.cs.model.CPN.Expressions;

import dk.aau.cs.model.CPN.Color;
import dk.aau.cs.model.CPN.ColorType;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprStringPosition;
import dk.aau.cs.model.CPN.ExpressionSupport.ExprValues;
import dk.aau.cs.model.CPN.Variable;

import java.util.Set;

public class UserSortExpression extends Expression {

    private final ColorType userSort;

    public UserSortExpression(ColorType usersort) {
        this.userSort = usersort;
    }

    public ColorType eval(ExpressionContext context) {
        return userSort;
    }

    @Override
    public Expression replace(Expression object1, Expression object2,boolean replaceAllInstances) {
        if (this == object1 && object2 instanceof Expression) {
            Expression obj2 = object2;
            obj2.setParent(parent);
            return obj2;
        }
        else {
            return this;
        }
    }
    @Override
    public Expression replace(Expression object1, Expression object2){
        return replace(object1,object2,false);
    }

    @Override
    public Expression copy() {
        return new UserSortExpression(userSort);
    }

    @Override
    public boolean containsPlaceHolder() {
        return false;
    }

    @Override
    public Expression findFirstPlaceHolder() {
        return null;
    }

    @Override
    public void getValues(ExprValues exprValues) { // UserSort, which contains ColorTypes is not currently used, if it is in the future Exprvalues will have to be expanded
        exprValues.addColorType(userSort);
    }

    public void getVariables(Set<Variable> variables) {

    }

    @Override
    public String toString() {
        return userSort.toString();
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }

    @Override
    public ExprStringPosition[] getChildren() {
        ExprStringPosition[] children = new ExprStringPosition[0];
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserSortExpression) {
            UserSortExpression expr = (UserSortExpression)o;
            return userSort.equals(expr);
        }
        return false;
    }

    @Override
    public boolean containsColor(Color color) {
        return userSort.contains(color);
    }
}
