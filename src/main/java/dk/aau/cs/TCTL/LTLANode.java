package dk.aau.cs.TCTL;

import dk.aau.cs.TCTL.visitors.ITCTLVisitor;

public class LTLANode extends TCTLAbstractPathProperty {
    TCTLAbstractStateProperty property;
    String trace;
    boolean propertyIsAbstractPath = false;

    public void setProperty(TCTLAbstractStateProperty property) {
        this.property = property;
        this.property.setParent(this);
    }

    public TCTLAbstractStateProperty getProperty() {
        return property;
    }

    public String getTrace() {
        return trace;
    }

    public LTLANode(TCTLAbstractStateProperty property, String trace) {
        this.property = property;
        this.property.setParent(this);
        this.trace = trace;
    }

    public LTLANode(TCTLAbstractStateProperty property) {
        this.property = property;
        this.property.setParent(this);
        this.trace = "";
    }

    public LTLANode(String trace) {
        this.property = new TCTLStatePlaceHolder();
        this.property.setParent(this);
        this.trace = trace;
    }

    public LTLANode() {
        this.property = new TCTLStatePlaceHolder();
        this.property.setParent(this);
        this.trace = "";
    }

    @Override
    public String toString() {
        String s = "";

        if(trace.equals("")) {
            s = property.isSimpleProperty() ? property.toString() : "("  + property.toString() + ")";
            return "A " + s;
        }

        String subString = property.toString().substring(0,1);
        if(subString.equals("A"))  {
            s = property.toString();
            propertyIsAbstractPath = true;
        } else {
            s = property.isSimpleProperty() ? property.toString() : "(" + property.toString() + ")";
            propertyIsAbstractPath = false;
        }

        return "A " + trace + " (" + s + ")";
    }

    @Override
    public boolean isSimpleProperty() {
        return false;
    }

    @Override
    public StringPosition[] getChildren() {
        int offset = 0;
        if(!trace.equals("")) {
            offset = trace.length() + 2;
        }

        int start = property.isSimpleProperty() ? 0 : 1;
        if(propertyIsAbstractPath) start -= 1;

        start = start + 2 + offset;
        int end = start + property.toString().length() + offset;

        StringPosition position = new StringPosition(start, end, property);

        StringPosition[] children = { position };
        return children;
    }

    @Override
    public TCTLAbstractPathProperty replace(TCTLAbstractProperty object1,
                                            TCTLAbstractProperty object2) {
        if (this == object1 && object2 instanceof TCTLAbstractPathProperty) {
            return (TCTLAbstractPathProperty) object2;
        } else {
            property = property.replace(object1, object2);
            return this;
        }
    }

    @Override
    public boolean containsPlaceHolder() {
        return property.containsPlaceHolder();
    }

    @Override
    public void convertForReducedNet(String templateName) {
        property.convertForReducedNet(templateName);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LTLANode) {
            LTLANode node = (LTLANode) o;
            return property.equals(node.getProperty()) && trace.equals(node.getTrace());
        }
        return false;
    }

    @Override
    public TCTLAbstractPathProperty copy() {
        return new LTLANode(property.copy(), trace);
    }

    @Override
    public boolean hasNestedPathQuantifiers() {
        return property instanceof TCTLPathToStateConverter || property.hasNestedPathQuantifiers();
    }

    public boolean containsAtomicPropositionWithSpecificPlaceInTemplate(String templateName, String placeName) {
        return property.containsAtomicPropositionWithSpecificPlaceInTemplate(templateName, placeName);
    }

    public boolean containsAtomicPropositionWithSpecificTransitionInTemplate(String templateName, String transitionName) {
        return property.containsAtomicPropositionWithSpecificTransitionInTemplate(templateName, transitionName);
    }

    @Override
    public TCTLAbstractProperty findFirstPlaceHolder() {
        return property.findFirstPlaceHolder();
    }

    @Override
    public void accept(ITCTLVisitor visitor, Object context) {
        visitor.visit(this, context);
    }
}
