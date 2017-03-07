package miner;

import java.util.List;

import miner.log.Log;
import miner.rule.Rule;

public abstract class Miner {

	private final Log log;

	public Miner(Log log) {
		super();
		this.log = log;
		log.trimToSize();
	}

	public Log getLog() {
		return log;
	}

	public abstract List<Rule> execute() throws Exception;
}