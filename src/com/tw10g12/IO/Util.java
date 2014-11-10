package com.tw10g12.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Util 
{
	public static String readFileToEnd(File file) throws FileNotFoundException, IOException
	{
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		IOException thrownException = null;
		
		try
		{
			String line;
			String newLine = "\n";
			
			boolean eof = false;
			do
			{
				line = reader.readLine();
				if(line == null)
				{
					eof = true;
				}
				else
				{
					builder.append(line);
					builder.append(newLine);
				}
			}
			while(!eof);
			reader.close();
		}
		catch(IOException iEx)
		{
			reader.close();
			thrownException = iEx;
		}
		if(thrownException != null) throw thrownException;
		
		return builder.toString();
	}
}
