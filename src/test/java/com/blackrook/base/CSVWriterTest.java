package com.blackrook.base;

public final class CSVWriterTest 
{

	public static void main(String[] args) throws Exception
	{
		CSVWriter csv = new CSVWriter("out.csv");
		csv.writeLine("a", "b", "c", "d");
		csv.writeLine("\"a\"", "b,", "c \"butt\"", "d,,\"fart\", \"junk,\"");
		csv.close();
		
		CSVWriter.escapeCell("\"a\"");
	}

}
