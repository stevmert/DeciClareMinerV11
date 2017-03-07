package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class FileManager {

	public static FileFilter getFileExtensionFilter(final String description, final String... exts) {
		FileFilter filter = new FileFilter() {
			@Override
			public String getDescription() {
				return description;
			}
			@Override
			public boolean accept(File f) {
				if(!f.isFile())
					return false;
				String name = f.getName().toLowerCase();
				for(String ext : exts) {
					String tmp = ext.toLowerCase();
					if(!ext.startsWith("."))
						tmp = "." + tmp;
					if(name.endsWith(tmp))
						return true;
				}
				return false;
			}
		};
		return filter;
	}

	public static File[] selectOpenFile(JFrame $frame, File dir, int fileSelectionMode,
			FileFilter ff) {
		JFileChooser $fileChooser = new JFileChooser();
		$fileChooser.setMultiSelectionEnabled(true);//does not work if 'false'???
		if(dir == null || !dir.exists())
			$fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		$fileChooser.setCurrentDirectory(dir);
		$fileChooser.setFileSelectionMode(fileSelectionMode);
		if(ff != null)
			$fileChooser.setFileFilter(ff);
		int selectionResult = $fileChooser.showOpenDialog($frame);
		if (selectionResult == JFileChooser.APPROVE_OPTION)
			return $fileChooser.getSelectedFiles();
		else {
			return null;
		}
	}

	public static String readAll(File f) throws Exception {
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(new FileReader(f));
			String result="";
			String line="";
			line=reader.readLine();
			while(line!=null) {
				if(!line.equals("")) {
					if(result.equals(""))
						result=line;
					else
						result+="\n"+line;
				}
				line=reader.readLine();
			}
			reader.close();
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Schrijft een String weg naar het bestand.
	 * @throws Exception 
	 */
	public static void writeAll(File f, String stringToWrite) throws Exception {
		BufferedWriter $writer = null;
		try {
			if (!f.exists())
				f.createNewFile();

			$writer = new BufferedWriter(new FileWriter(f));
			StringBuffer sb = new StringBuffer(stringToWrite);
			$writer.write(sb.toString());
			$writer.flush();
			$writer.close();
		} catch (Exception e) {
			throw e;
		}
	}

	public static boolean copyfile(String srFile, String dtFile) throws Exception{
		return copyfile(new File(srFile), new File(dtFile));
	}

	public static boolean copyfile(File srFile, File dtFile) throws Exception{
		try {
			InputStream in = new FileInputStream(srFile);
			OutputStream out = new FileOutputStream(dtFile);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			return true;
		} catch(Exception e) {
			throw e;
		}
	}
}