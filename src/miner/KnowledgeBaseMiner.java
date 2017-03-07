package miner;

import miner.kb.KnowledgeBase;
import miner.log.Log;

public abstract class KnowledgeBaseMiner extends Miner {

	private final KnowledgeBase kb;

	public KnowledgeBaseMiner(Log log, KnowledgeBase kb) {
		super(log);
		this.kb = kb;
	}

	public KnowledgeBase getKB() {
		return kb;
	}
}