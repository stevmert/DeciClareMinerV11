package model.constraint;

import java.io.Serializable;

import model.Constraint;
import model.data.Decision;

public abstract class ResourceConstraint extends Constraint implements Serializable {

	private static final long serialVersionUID = -2683307304810371189L;

	public ResourceConstraint(Decision activationDecision, Decision deactivationDec, boolean isOptional) {
		super(activationDecision, deactivationDec, isOptional);
	}
}