package miner.rule;

import java.util.ArrayList;
import java.util.Comparator;

import model.constraint.BoundedConstraint;
import model.constraint.ExistenceConstraint;
import model.constraint.Negatable;
import model.constraint.RelationConstraint;
import model.constraint.existence.AtLeast;
import model.constraint.existence.AtLeastChoice;
import model.constraint.existence.AtMost;
import model.constraint.existence.AtMostChoice;
import model.constraint.existence.extra.First;
import model.constraint.existence.extra.Last;
import model.constraint.relation.AlternatePrecedence;
import model.constraint.relation.AlternateResponse;
import model.constraint.relation.ChainPrecedence;
import model.constraint.relation.ChainResponse;
import model.constraint.relation.Precedence;
import model.constraint.relation.RespondedPresence;
import model.constraint.relation.Response;
import model.constraint.resource.ResourceAvailability;
import model.constraint.resource.ResourceEquality;
import model.constraint.resource.ResourceUsage;
import model.expression.AtomicExistenceExpression;
import model.expression.ExistenceExpression;
import model.expression.NonAtomicExistenceExpression;

public class RulePrintComparator implements Comparator<SingleRule> {

	@SuppressWarnings("rawtypes")
	public static final ArrayList<Class> ORDER = new ArrayList<>();
	static {
		ORDER.add(ResourceAvailability.class);
		ORDER.add(ResourceUsage.class);
		ORDER.add(ResourceEquality.class);
		ORDER.add(First.class);
		ORDER.add(Last.class);
		ORDER.add(AtLeast.class);
		ORDER.add(AtMost.class);
		ORDER.add(AtLeastChoice.class);
		ORDER.add(AtMostChoice.class);
		ORDER.add(RespondedPresence.class);
		ORDER.add(Response.class);
		ORDER.add(AlternateResponse.class);
		ORDER.add(ChainResponse.class);
		ORDER.add(Precedence.class);
		ORDER.add(AlternatePrecedence.class);
		ORDER.add(ChainPrecedence.class);
	}

	@Override
	public int compare(SingleRule r1, SingleRule r2) {
		int o1 = ORDER.indexOf(r1.getConstraint().getClass());
		int o2 = ORDER.indexOf(r2.getConstraint().getClass());
		if(o1 < o2)
			return -1;
		if(o1 > o2)
			return 1;
		if(r1.getConstraint() instanceof Negatable) {
			if(((Negatable) r1.getConstraint()).isPositiveVersion()
					&& !((Negatable) r2.getConstraint()).isPositiveVersion())
				return -1;
			if(!((Negatable) r1.getConstraint()).isPositiveVersion()
					&& ((Negatable) r2.getConstraint()).isPositiveVersion())
				return 1;
		}
		if(r1.getConstraint() instanceof ExistenceConstraint) {
			if(((ExistenceConstraint) r1.getConstraint()).getActivityExpression().getNrOfElements()
					< ((ExistenceConstraint) r2.getConstraint()).getActivityExpression().getNrOfElements())
				return -1;
			if(((ExistenceConstraint) r1.getConstraint()).getActivityExpression().getNrOfElements()
					> ((ExistenceConstraint) r2.getConstraint()).getActivityExpression().getNrOfElements())
				return 1;
			int resString = ((ExistenceConstraint) r1.getConstraint()).getActivityExpression().toString().compareTo(
					((ExistenceConstraint) r2.getConstraint()).getActivityExpression().toString());
			if(resString != 0)
				return resString;
			if(r1 instanceof BoundedConstraint) {
				if(((BoundedConstraint) r1.getConstraint()).getBound()
						< ((BoundedConstraint) r2.getConstraint()).getBound())
					return -1;
				if(((BoundedConstraint) r1.getConstraint()).getBound()
						> ((BoundedConstraint) r2.getConstraint()).getBound())
					return 1;
			}
		} else if(r1.getConstraint() instanceof RelationConstraint) {
			if(r1.getConstraint() instanceof RespondedPresence
					|| r1.getConstraint() instanceof Response
					|| r1.getConstraint() instanceof ChainResponse
					|| r1.getConstraint() instanceof AlternateResponse) {
				int res = compareRelationConstraint(((RelationConstraint) r1.getConstraint()).getConditionExpression(),
						((RelationConstraint) r2.getConstraint()).getConditionExpression(),
						((RelationConstraint) r1.getConstraint()).getConsequenceExpression(),
						((RelationConstraint) r2.getConstraint()).getConsequenceExpression());
				if(res == -1)
					return -1;
				if(res == 1)
					return 1;
			} else {
				int res = compareRelationConstraint(((RelationConstraint) r1.getConstraint()).getConsequenceExpression(),
						((RelationConstraint) r2.getConstraint()).getConsequenceExpression(),
						((RelationConstraint) r1.getConstraint()).getConditionExpression(),
						((RelationConstraint) r2.getConstraint()).getConditionExpression());
				if(res == -1)
					return -1;
				if(res == 1)
					return 1;
			}
		}
		//		else if(r1.getConstraint() instanceof ResourceConstraint) {
		//			if(r1.getConstraint() instanceof ResourceUsagex) {
		//				int sTest = ((ResourceUsagex) r1.getConstraint()).getActivity().toString()
		//						.compareTo(((ResourceUsagex) r2.getConstraint()).getActivity().toString());
		//				if(sTest != 0)
		//					return sTest;
		//				if(((ResourceUsagex) r1.getConstraint()).getResources().size()
		//						< ((ResourceUsagex) r2.getConstraint()).getResources().size())
		//					return -1;
		//				if(((ResourceUsagex) r1.getConstraint()).getResources().size()
		//						> ((ResourceUsagex) r2.getConstraint()).getResources().size())
		//					return 1;
		//				return ((ResourceUsagex) r1.getConstraint()).getResources().toString().compareTo(
		//						((ResourceUsagex) r2.getConstraint()).getResources().toString());
		//			} else//TODOx: resource availability and exclusion?
		//				throw new IllegalArgumentException("TODOx?");
		//		}
		if(r1.getConformancePercentage() > r2.getConformancePercentage())
			return -1;
		if(r1.getConformancePercentage() < r2.getConformancePercentage())
			return 1;
		if(r1.getSupportPercentage() > r2.getSupportPercentage())
			return -1;
		if(r1.getSupportPercentage() < r2.getSupportPercentage())
			return 1;
		if(r1.getConstraint().getActivationDecision() == null
				&& r2.getConstraint().getActivationDecision() == null)
			return 0;
		if(r1.getConstraint().getActivationDecision() == null
				&& r2.getConstraint().getActivationDecision() != null)
			return -1;
		if(r1.getConstraint().getActivationDecision() != null
				&& r2.getConstraint().getActivationDecision() == null)
			return 1;
		if(r1.getConstraint().getActivationDecision().getRules().size()
				< r2.getConstraint().getActivationDecision().getRules().size())
			return -1;
		if(r1.getConstraint().getActivationDecision().getRules().size()
				> r2.getConstraint().getActivationDecision().getRules().size())
			return 1;
		if(r1.getConstraint().getActivationDecision().getRules().toString().length()
				< r2.getConstraint().getActivationDecision().getRules().toString().length())
			return -1;
		if(r1.getConstraint().getActivationDecision().getRules().toString().length()
				> r2.getConstraint().getActivationDecision().getRules().toString().length())
			return 1;
		return r1.getConstraint().getActivationDecision().getRules().toString().compareTo(
				r2.getConstraint().getActivationDecision().getRules().toString());
	}

	private int compareRelationConstraint(ExistenceExpression e11, ExistenceExpression e21,
			ExistenceExpression e12, ExistenceExpression e22) {
		if(e11 instanceof NonAtomicExistenceExpression || e21 instanceof NonAtomicExistenceExpression
				|| e12 instanceof NonAtomicExistenceExpression || e22 instanceof NonAtomicExistenceExpression)
			throw new IllegalArgumentException("TODO");
		AtomicExistenceExpression ae11 = (AtomicExistenceExpression) e11;
		AtomicExistenceExpression ae21 = (AtomicExistenceExpression) e21;
		AtomicExistenceExpression ae12 = (AtomicExistenceExpression) e12;
		AtomicExistenceExpression ae22 = (AtomicExistenceExpression) e22;
		int o11 = ORDER.indexOf(ae11.getExistenceConstraint().getClass());
		int o21 = ORDER.indexOf(ae21.getExistenceConstraint().getClass());
		if(o11 < o21)
			return -1;
		if(o11 > o21)
			return 1;
		int o12 = ORDER.indexOf(ae12.getExistenceConstraint().getClass());
		int o22 = ORDER.indexOf(ae22.getExistenceConstraint().getClass());
		if(o12 < o22)
			return -1;
		if(o12 > o22)
			return 1;
		if(ae11.getNrOfElements() < ae21.getNrOfElements())
			return -1;
		if(ae11.getNrOfElements() > ae21.getNrOfElements())
			return 1;
		if(ae12.getNrOfElements() < ae22.getNrOfElements())
			return -1;
		if(ae12.getNrOfElements() > ae22.getNrOfElements())
			return 1;
		int stringScore = ae11.toString().compareTo(ae21.toString());
		if(stringScore < 0)
			return -1;
		if(stringScore > 0)
			return 1;
		stringScore = ae12.toString().compareTo(ae22.toString());
		if(stringScore < 0)
			return -1;
		if(stringScore > 0)
			return 1;
		return 0;
	}
}