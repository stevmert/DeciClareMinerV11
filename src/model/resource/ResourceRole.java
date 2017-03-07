package model.resource;

import java.util.Arrays;
import java.util.HashSet;

public class ResourceRole extends Resource {

	private static final long serialVersionUID = 6568817569668087074L;

	private HashSet<DirectResource> containedResources;
	private HashSet<ResourceRole> subRoles;

	/**
	 * Resource roles
	 */
	public ResourceRole(String name) {
		super(name);
		this.containedResources = new HashSet<>();
		this.subRoles = new HashSet<>();
	}

	public ResourceRole(String name, HashSet<ResourceRole> subRoles) {
		super(name);
		this.containedResources = new HashSet<>();
		this.subRoles = subRoles;
	}

	public ResourceRole(String name, ResourceRole... subRoles) {
		super(name);
		this.containedResources = new HashSet<>();
		this.subRoles = new HashSet<>(Arrays.asList(subRoles));
	}

	public HashSet<DirectResource> getContainedResources() {
		return containedResources;
	}

	public void setContainedResources(HashSet<DirectResource> containedResources) {
		this.containedResources = containedResources;
	}

	public HashSet<ResourceRole> getSubRoles() {
		return subRoles;
	}

	public void setSubRoles(HashSet<ResourceRole> subRoles) {
		this.subRoles = subRoles;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean includeSubRoles) {
		if(!includeSubRoles
				|| getSubRoles().isEmpty())
			return super.toString();
		String subs = "";
		for(ResourceRole rr : getSubRoles())
			subs += "/" + rr.getName();
		return getName() + "(" + subs.substring(1) + ")";
	}
}