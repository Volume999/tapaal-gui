package dk.aau.cs.TCTL.visitors;

import java.util.ArrayList;
import java.util.HashSet;

import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLTransitionNode;
import dk.aau.cs.util.Tuple;

public class VerifyTransitionNamesVisitor extends VisitorBase {
	private final ArrayList<Tuple<String, String>> templateTransitionNames;

	public VerifyTransitionNamesVisitor(ArrayList<Tuple<String, String>> templateTransitionNames) {
		this.templateTransitionNames = templateTransitionNames;
	}

	public Context verifyTransitionNames(TCTLAbstractProperty query) {
		Context c = new Context();

		query.accept(this, c);

		return c;
	}

	public void visit(TCTLTransitionNode transitionNode, Object context) {
		Context c = (Context) context;
		if (!templateTransitionNames.contains(new Tuple<String,String>(transitionNode.getTemplate(), transitionNode.getTransition()))) {
                        String temp = transitionNode.getTemplate() != "" ? transitionNode.getTemplate() + "." : "";
			c.addIncorrectTransitionName(temp + transitionNode.getTransition());
			c.setResult(false);
		}
	}

	public static class Context {
		private Boolean result;
		private final HashSet<String> incorrectTransitionNames;

		public Boolean getResult() {
			return result;
		}

		public void setResult(Boolean result) {
			this.result = result;
		}

		public HashSet<String> getIncorrectTransitionNames() {
			return incorrectTransitionNames;
		}

		public void addIncorrectTransitionName(String incorrectTransitionName) {
			incorrectTransitionNames.add(incorrectTransitionName);
		}

		public Context() {
			result = true;
			incorrectTransitionNames = new HashSet<String>();
		}
	}
}
