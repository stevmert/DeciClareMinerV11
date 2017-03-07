package util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFileChooser;

import miner.Config;

public class IntermediateTestResultsReaderV2 implements Runnable {

	public static void main(String[] args) {
		try {
			new IntermediateTestResultsReaderV2(args).run();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private final File[] dirs_fullModel;
	private final File dir_intermediateResPops;

	public IntermediateTestResultsReaderV2(String[] args) {
		System.out.println("Select the directories containing the results to create the 'full' model...");
		this.dirs_fullModel = FileManager.selectOpenFile(null, new File("."), JFileChooser.DIRECTORIES_ONLY, null);
		System.out.println("dirs_fullModel = " + Arrays.toString(dirs_fullModel));
		if(dirs_fullModel == null || dirs_fullModel.length == 0)
			System.exit(0);
		System.out.println("Select the directory containing the intermediate ResPops...");
		this.dir_intermediateResPops = FileManager.selectOpenFile(null, new File("."), JFileChooser.DIRECTORIES_ONLY, null)[0];
		System.out.println("dir_intermediateResPops = " + dir_intermediateResPops);
		if(dir_intermediateResPops == null)
			System.exit(0);
	}

	@Override
	public void run() {
		try {
			HashMap<String, Integer> fullModel = loadDirectories(dirs_fullModel);

			int totalTracesToIdentify = 0;
			for(int v : fullModel.values())
				totalTracesToIdentify += v;

			//TODO
			//			String stringToWrite = processDirectories(dir_intermediateResPops, fullModel, totalTracesToIdentify);

			System.out.println("Processing intermediate ResPops...");
			String stringToWrite = "";
			for(File f : dir_intermediateResPops.listFiles())
				if(f.getName().toLowerCase().contains((" - " + Config.FILENAME_INTERMEDIATE_P2).toLowerCase())
						&& f.getName().toLowerCase().endsWith(("." + Config.FILE_CSV).toLowerCase())) {
					System.out.print("I");
					int nrOfRules = 0;
					int identifiedTraces = 0;
					String[] resLines = FileManager.readAll(f).split("\n");
					for(int i = 1; i < resLines.length; i++) {
						String[] line = resLines[i].split(";");
						String constraint = line[line.length-1];
						if(constraint.contains(" activateIf[")) {
							constraint = constraint.substring(0, constraint.indexOf(" activateIf["));
							identifiedTraces += Integer.parseInt(line[1]);
							nrOfRules++;
						}
					}
					stringToWrite += "\n" + f.getName() + ";"
							+ (((double) identifiedTraces)/totalTracesToIdentify) + ";"
							+  (((double) nrOfRules)/fullModel.size());
				}
			System.out.println();

			File f = new File("statusIntermediate.csv");
			FileManager.writeAll(f, stringToWrite.trim());
			System.out.println(stringToWrite.trim());
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
}