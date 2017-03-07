package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import miner.Config;

public class Logger {

	private static Logger instance;
	public static Logger getInstance() {
		if(instance == null)
			instance = new Logger();
		return instance;
	}

	private String logName;
	private BufferedWriter bw_log;
	private BufferedWriter bw_log_p1;
	private BufferedWriter bw_log_p2;
	private BufferedWriter bw_log_p2_bounded;
	private BufferedWriter bw_log_p2_relation;
	private BufferedWriter bw_log_p2_other;

	private Logger() {
		this.logName = "Unknown";
		this.bw_log = null;
		this.bw_log_p1 = null;
		this.bw_log_p2 = null;
		this.bw_log_p2_bounded = null;
		this.bw_log_p2_relation = null;
		this.bw_log_p2_other = null;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	private BufferedWriter getBufferedWriter(String fileName, String fileExtension) {
		BufferedWriter bw = null;
		for(int i = 0; bw == null && i < 50; i++) {
			try {
				String tmp = "";
				if(i > 0)
					tmp = "(" + i + ")";
				FileWriter fw = new FileWriter(getLogName() + " - " + fileName + tmp + "." + fileExtension);
				bw = new BufferedWriter(fw);
			} catch (IOException e) {}
		}
		return bw;
	}

	public void logConfig() {
		try {
			if(bw_log == null)
				bw_log = getBufferedWriter(Config.FILENAME_LOGGER, Config.FILE_CSV);
			String fieldsAndValues = "";
			for(Field f : Config.class.getDeclaredFields()) {
				try {
					f.setAccessible(true);
					fieldsAndValues += "\n" + f.getName() + Config.CSV_DELIMITER + f.get(null);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			bw_log.write(fieldsAndValues.trim() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logStat_general(String name, String... stats) {
		try {
			if(bw_log == null)
				bw_log = getBufferedWriter(Config.FILENAME_LOGGER, Config.FILE_CSV);
			String text = name + ":";
			for(String s : stats)
				text += Config.CSV_DELIMITER + s;
			bw_log.write(text + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logP1() {
		try {
			if(bw_log_p1 == null)
				bw_log_p1 = getBufferedWriter(Config.FILENAME_LOGGER_P1, Config.FILE_CSV);
			bw_log_p1.write("Types" + Config.CSV_DELIMITER
					+ "Atomic" + Config.CSV_DELIMITER
					+ "Atomic Evals" + Config.CSV_DELIMITER
					+ "Expanded" + Config.CSV_DELIMITER
					+ "Expanded Evals" + Config.CSV_DELIMITER
					+ "Duration" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logStat_p1(String types, int atomic, long nrOfEvaluations_atomic, 
			int expanded, long nrOfEvaluations_expansion,
			long duration) {
		try {
			if(bw_log_p1 == null)
				bw_log_p1 = getBufferedWriter(Config.FILENAME_LOGGER_P1, Config.FILE_CSV);
			bw_log_p1.write(types
					+ Config.CSV_DELIMITER + atomic
					+ Config.CSV_DELIMITER + nrOfEvaluations_atomic
					+ Config.CSV_DELIMITER + expanded
					+ Config.CSV_DELIMITER + nrOfEvaluations_expansion
					+ Config.CSV_DELIMITER + duration + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logP2(int popSize_bounded, int seeds_bounded,
			int popSize_relation, int seeds_relation,
			int popSize_other, int seeds_other) {
		try {
			if(bw_log_p2 == null)
				bw_log_p2 = getBufferedWriter(Config.FILENAME_LOGGER_P2, Config.FILE_CSV);
			bw_log_p2.write(
					"Type" + Config.CSV_DELIMITER
					+ "PopSize" + Config.CSV_DELIMITER
					+ "#Seeds" + Config.CSV_DELIMITER
					+ "\n"
					+ "Bounded" + Config.CSV_DELIMITER + popSize_bounded + Config.CSV_DELIMITER + seeds_bounded
					+ "\n"
					+ "Relation" + Config.CSV_DELIMITER + popSize_relation + Config.CSV_DELIMITER + seeds_relation
					+ "\n"
					+ "Other" + Config.CSV_DELIMITER + popSize_other + Config.CSV_DELIMITER + seeds_other);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logP2_bounded() {
		try {
			if(bw_log_p2_bounded == null)
				bw_log_p2_bounded = getBufferedWriter(Config.FILENAME_LOGGER_P2_BOUNDED, Config.FILE_CSV);
			bw_log_p2_bounded.write(
					"Iteration" + Config.CSV_DELIMITER
					+ "batchRulesAdded" + Config.CSV_DELIMITER
					+ "batchRulesRemoved" + Config.CSV_DELIMITER
					+ "singleRulesAdded" + Config.CSV_DELIMITER
					+ "singleRulesRemoved" + Config.CSV_DELIMITER
					+ "Duration" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logP2_relation() {
		try {
			if(bw_log_p2_relation == null)
				bw_log_p2_relation = getBufferedWriter(Config.FILENAME_LOGGER_P2_RELATION, Config.FILE_CSV);
			bw_log_p2_relation.write(
					"Iteration" + Config.CSV_DELIMITER
					+ "batchRulesAdded" + Config.CSV_DELIMITER
					+ "batchRulesRemoved" + Config.CSV_DELIMITER
					+ "singleRulesAdded" + Config.CSV_DELIMITER
					+ "singleRulesRemoved" + Config.CSV_DELIMITER
					+ "Duration" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logP2_other() {
		try {
			if(bw_log_p2_other == null)
				bw_log_p2_other = getBufferedWriter(Config.FILENAME_LOGGER_P2_OTHER, Config.FILE_CSV);
			bw_log_p2_other.write(
					"Iteration" + Config.CSV_DELIMITER
					+ "batchRulesAdded" + Config.CSV_DELIMITER
					+ "batchRulesRemoved" + Config.CSV_DELIMITER
					+ "singleRulesAdded" + Config.CSV_DELIMITER
					+ "singleRulesRemoved" + Config.CSV_DELIMITER
					+ "Duration" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logStat_p2_bounded(int iteration, int batchRulesAdded, int batchRulesRemoved,
			int singleRulesAdded, int singleRulesRemoved, long duration) {
		logStat_p2_bounded(iteration+"", batchRulesAdded, batchRulesRemoved,
				singleRulesAdded, singleRulesRemoved, duration);
	}

	public void logStat_p2_bounded(String step, int batchRulesAdded, int batchRulesRemoved,
			int singleRulesAdded, int singleRulesRemoved, long duration) {
		try {
			if(bw_log_p2_bounded == null)
				bw_log_p2_bounded = getBufferedWriter(Config.FILENAME_LOGGER_P2_BOUNDED, Config.FILE_CSV);
			bw_log_p2_bounded.write(step
					+ Config.CSV_DELIMITER + batchRulesAdded
					+ Config.CSV_DELIMITER + batchRulesRemoved
					+ Config.CSV_DELIMITER + singleRulesAdded
					+ Config.CSV_DELIMITER + singleRulesRemoved
					+ Config.CSV_DELIMITER + duration + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logStat_p2_relation(int iteration, int batchRulesAdded, int batchRulesRemoved,
			int singleRulesAdded, int singleRulesRemoved, long duration) {
		logStat_p2_relation(iteration+"", batchRulesAdded, batchRulesRemoved,
				singleRulesAdded, singleRulesRemoved, duration);
	}

	public void logStat_p2_relation(String step, int batchRulesAdded, int batchRulesRemoved,
			int singleRulesAdded, int singleRulesRemoved, long duration) {
		try {
			if(bw_log_p2_relation == null)
				bw_log_p2_relation = getBufferedWriter(Config.FILENAME_LOGGER_P2_RELATION, Config.FILE_CSV);
			bw_log_p2_relation.write(step
					+ Config.CSV_DELIMITER + batchRulesAdded
					+ Config.CSV_DELIMITER + batchRulesRemoved
					+ Config.CSV_DELIMITER + singleRulesAdded
					+ Config.CSV_DELIMITER + singleRulesRemoved
					+ Config.CSV_DELIMITER + duration + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void logStat_p2_other(int iteration, int batchRulesAdded, int batchRulesRemoved,
			int singleRulesAdded, int singleRulesRemoved, long duration) {
		logStat_p2_other(iteration+"", batchRulesAdded, batchRulesRemoved,
				singleRulesAdded, singleRulesRemoved, duration);
	}

	public void logStat_p2_other(String step, int batchRulesAdded, int batchRulesRemoved,
			int singleRulesAdded, int singleRulesRemoved, long duration) {
		try {
			if(bw_log_p2_other == null)
				bw_log_p2_other = getBufferedWriter(Config.FILENAME_LOGGER_P2_OTHER, Config.FILE_CSV);
			bw_log_p2_other.write(step
					+ Config.CSV_DELIMITER + batchRulesAdded
					+ Config.CSV_DELIMITER + batchRulesRemoved
					+ Config.CSV_DELIMITER + singleRulesAdded
					+ Config.CSV_DELIMITER + singleRulesRemoved
					+ Config.CSV_DELIMITER + duration + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		close_p1();
		close_p2();
	}

	public void close_p1() {
		try {
			if(bw_log_p1 != null) {
				bw_log_p1.close();
				bw_log_p1 = null;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void close_p2() {
		try {
			if(bw_log != null) {
				bw_log.close();
				bw_log = null;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		try {
			if(bw_log_p2 != null) {
				bw_log_p2.close();
				bw_log_p2 = null;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		try {
			if(bw_log_p2_bounded != null) {
				bw_log_p2_bounded.close();
				bw_log_p2_bounded = null;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		try {
			if(bw_log_p2_relation != null) {
				bw_log_p2_relation.close();
				bw_log_p2_relation = null;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		try {
			if(bw_log_p2_other != null) {
				bw_log_p2_other.close();
				bw_log_p2_other = null;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeToFile(String text, String filename, String extension) throws Exception {
		boolean written = false;
		int i = 0;
		while(!written) {
			try {
				if(i == 0)
					FileManager.writeAll(new File(getLogName() + " - " + filename + "." + extension), text.trim());
				//					FileManager.writeAll(new File(getLogName() + " - " + filename + "." + extension), text.trim());
				else
					FileManager.writeAll(new File(getLogName() + " - " + filename + "(" + i + ")." + extension), text.trim());
				written = true;
			} catch(FileNotFoundException e) {}
			i++;
		}
	}

	public void writeToFile(Serializable object, String filename, String extension) {
		for(int i = 0; i < 500; i++)
			try {
				if(i == 0)
					writeToFile(object, new File(getLogName() + " - " + filename + "." + extension));
				else
					writeToFile(object, new File(getLogName() + " - " + filename + "(" + i + ")." + extension));
				break;
			} catch(Exception e) {
				e.printStackTrace();
			}
	}

	private void writeToFile(Serializable object, File f) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(f));
			out.writeObject(object);
			out.close();
		} catch (Exception ex) {
			throw new RuntimeException("An error occurred when creating the data object for "
					+ f.toString() + "!", ex);
		}
	}
}