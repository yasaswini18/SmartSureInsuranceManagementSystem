package Questions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileWordCount {
	public static void main(String[] args)
	{
		File dir = new File("C:\\Users\\YASASWINI\\OneDrive\\Desktop\\JAVAFULLSTACK\\src\\day18");
		File[] list = dir.listFiles();
		for(File file : list)
		{
			if(file.getName().endsWith(".java"))
			{
				int count = countWord(file,"System");
				System.out.println(file.getName()+":"+count);
			}
		}
	}

	private static int countWord(File file, String word) {
		int count=0;
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while(line!=null)
			{
				if(line.contains(word)) count++;
				line = br.readLine();
			}
			return count;
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}catch(IOException o)
		{
			o.printStackTrace();
		}
		return count;
	}
	
}
