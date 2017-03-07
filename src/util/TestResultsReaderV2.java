package util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFileChooser;

import miner.Config;

public class TestResultsReaderV2 implements Runnable {

	public static void main(String[] args) {
		try {
			new TestResultsReaderV2(args).run();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private final File[] dirs_fullModel;
	private final File[] dirs_toTest;

	public TestResultsReaderV2(String[] args) {
		System.out.println("Select the directories containing the results to create the 'full' model...");
		this.dirs_fullModel = FileManager.selectOpenFile(null, new File("."), JFileChooser.DIRECTORIES_ONLY, null);
		if(dirs_fullModel == null || dirs_fullModel.length == 0)
			System.exit(0);
		System.out.println("dirs_fullModel = " + Arrays.toString(dirs_fullModel));
		Scanner s = new Scanner(System.in);
		System.out.println("Calculate the % of these same directories (Y/N)?");
		String ans = s.nextLine();
		s.close();
		if(ans != null && ans.equalsIgnoreCase("y"))
			dirs_toTest = dirs_fullModel;
		else if(ans != null && ans.equalsIgnoreCase("n")) {
			System.out.println("Select the directories containing the results for which the % need to be calculated...");
			this.dirs_toTest = FileManager.selectOpenFile(null, new File("."), JFileChooser.DIRECTORIES_ONLY, null);
			if(dirs_toTest == null || dirs_toTest.length == 0)
				System.exit(0);
			System.out.println("dirs_toTest = " + Arrays.toString(dirs_toTest));
		} else
			throw new RuntimeException();
	}

	@Override
	public void run() {
		try {
			HashMap<String, Integer> fullModel = loadDirectories(dirs_fullModel);

			int totalTracesToIdentify = 0;
			for(int v : fullModel.values())
				totalTracesToIdentify += v;

			String stringToWrite = processDirectories(dirs_toTest, fullModel, totalTracesToIdentify);

			System.out.println(stringToWrite.trim());
			File f = new File("status" + 0 + ".csv");
			int index = 1;
			while(f.exists()) {
				f = new File("status" + index + ".csv");
				index++;
			}
			FileManager.writeAll(f, stringToWrite.trim());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static HashMap<String, Integer> loadDirectories(File[] dirs) throws Exception {
		HashMap<String, Integer> fullModel = new HashMap<>();
		//			HashMap<String, HashSet<String>> fullModel_decs = new HashMap<>();
		for(int x = 0; x < dirs.length; x++) {
			System.out.println("\nLoading " + (x+1) + "/" + dirs.length
					+ " into full model (" + dirs[x].getName() + ")...");
			loadDirectory(dirs[x], fullModel);
		}
		return fullModel;
	}

	private static void loadDirectory(File dir, HashMap<String, Integer> model) throws Exception {
		System.out.println("Loading " + dir.getName() + " into full model...");
		boolean isPrint = false;
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				if(isPrint) {
					isPrint = false;
					System.out.println();
				}
				loadDirectory(f, model);
			} else
				isPrint = loadFile(f, model) || isPrint;
		}
		if(isPrint)
			System.out.println();
	}

	private static boolean loadFile(File f, HashMap<String, Integer> model) throws Exception {
		if(f.getName().toLowerCase().endsWith((" - " + Config.FILENAME_RESULT + "." + Config.FILE_CSV).toLowerCase())) {
			System.out.print("I");
			String[] resLines = FileManager.readAll(f).split("\n");
			for(int i = 1; i < resLines.length; i++) {
				String[] line = resLines[i].split(";");
				String constraint = line[line.length-1];
				if(constraint.contains(" activateIf[")) {
					//								HashSet<String> decs = new HashSet<>();
					//								String[] tmp = constraint.substring(constraint.indexOf(" activateIf[[") + " activateIf[[".length())
					//										.split("\\], \\[");
					//								tmp[tmp.length-1] = tmp[tmp.length-1].substring(0, tmp[tmp.length-1].length()-2);
					constraint = constraint.substring(0, constraint.indexOf(" activateIf["));
					int newNr = Integer.parseInt(line[1]);
					Integer nr = model.get(constraint);
					if(nr == null)
						model.put(constraint, newNr);
					else if(nr < newNr)
						model.put(constraint, newNr);
				}
			}
			return true;
		}
		return false;
	}

	private static String processDirectories(File[] dirs, HashMap<String, Integer> model,
			int totalTracesToIdentify) throws Exception {
		String stringToWrite = "";
		for(int x = 0; x < dirs.length; x++) {
			System.out.println("\nProcessing " + (x+1) + "/" + dirs.length
					+ " (" + dirs[x].getName() + ")...");
			ArrayList<Integer> identifiedTracesList = new ArrayList<>();
			ArrayList<Integer> nrOfRulesList = new ArrayList<>();
			processDirectory(dirs[x], model, identifiedTracesList, nrOfRulesList);
			//calc avg #traces
			double avgTraces = 0;
			for(int i : identifiedTracesList)
				avgTraces += i;
			avgTraces = avgTraces/identifiedTracesList.size();
			double percentageOfTraces = avgTraces/totalTracesToIdentify;
			//calc standDev #traces
			double stanDevTraces = 0;
			for(int i : identifiedTracesList)
				stanDevTraces += Math.pow(avgTraces - i, 2);
			stanDevTraces = Math.sqrt(stanDevTraces/identifiedTracesList.size());
			double stanDev_percentageOfTraces = stanDevTraces/totalTracesToIdentify;
			//calc avg #constraints
			double avgConstraints = 0;
			for(int i : nrOfRulesList)
				avgConstraints += i;
			avgConstraints = avgConstraints/nrOfRulesList.size();
			double percentageOfConstraints = avgConstraints/model.size();
			//calc standDev #constraints
			double stanDevConstraints = 0;
			for(int i : nrOfRulesList)
				stanDevConstraints += Math.pow(avgConstraints - i, 2);
			stanDevConstraints = Math.sqrt(stanDevConstraints/nrOfRulesList.size());
			double stanDev_percentageOfConstraints = stanDevConstraints/model.size();

			stringToWrite += "\n" + dirs[x].getName()
					+ ";" + percentageOfTraces
					+ ";" + stanDev_percentageOfTraces
					+ ";" + percentageOfConstraints
					+ ";" + stanDev_percentageOfConstraints
					+ ";" + identifiedTracesList.size();
		}
		return stringToWrite;
	}

	private static void processDirectory(File dir, HashMap<String, Integer> model,
			ArrayList<Integer> identifiedTracesList, ArrayList<Integer> nrOfRulesList) throws Exception {
		System.out.println("Processing " + dir.getName() + "...");
		boolean isPrint = false;
		for(File f : dir.listFiles()) {
			if(f.isDirectory()) {
				if(isPrint) {
					isPrint = false;
					System.out.println();
				}
				processDirectory(f, model, identifiedTracesList, nrOfRulesList);
			} else
				isPrint = processFile(f, model, identifiedTracesList, nrOfRulesList) || isPrint;
		}
		if(isPrint)
			System.out.println();
	}

	private static boolean processFile(File f, HashMap<String, Integer> model,
			ArrayList<Integer> identifiedTracesList, ArrayList<Integer> nrOfRulesList) throws Exception {
		if(f.getName().toLowerCase().endsWith((" - " + Config.FILENAME_RESULT + "." + Config.FILE_CSV).toLowerCase())) {
			System.out.print("I");
			int nrOfRules = 0;
			int identifiedTraces = 0;
			String[] resLines = FileManager.readAll(f).split("\n");
			for(int i = 1; i < resLines.length; i++) {
				String[] line = resLines[i].split(";");
				String constraint = line[line.length-1];
				if(constraint.contains(" activateIf[")) {
					constraint = constraint.substring(0, constraint.indexOf(" activateIf["));
					if(model.get(constraint) == null)
						throw new IllegalArgumentException("TODO: Niet in model opl????");
					identifiedTraces += Integer.parseInt(line[1]);
					nrOfRules++;
				}
			}
			identifiedTracesList.add(identifiedTraces);
			nrOfRulesList.add(nrOfRules);
			return true;
		}
		return false;
	}
}