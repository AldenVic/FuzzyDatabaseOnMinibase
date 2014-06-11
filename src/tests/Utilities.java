package tests;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.HFBufMgrException;
import heap.HFDiskMgrException;
import heap.HFException;
import heap.Heapfile;
import heap.InvalidSlotNumberException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.SpaceNotAvailableException;
import heap.Tuple;
import iterator.TopRankJoin;



import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JTextArea;

import com.sun.org.apache.xml.internal.resolver.helpers.Debug;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


public class Utilities {

	// data file  inof
	public static int _tableIndex =0;
	public static int _totalFiles = 0;
	public static int _totalFiles_add = 0;
	public static int _totalFiles_sub = 0;
	public static int _maxFiles = 9;

	public static int[] _filesToView = {0,0,0,0,0,0,0,0,0};
	public static int[] _filesToAdd =  {0,0,0,0,0,0,0,0,0};
	public static int[] _filesToSub =  {0,0,0,0,0,0,0,0,0};
	public static int[] _filesInSet = {0,0,0,0,0,0,0,0,0};
	public static int[] _filesInSet_add   = {0,0,0,0,0,0,0,0,0};
	public static int[] _filesInSet_sub   = {0,0,0,0,0,0,0,0,0};
	public static int[] _filesInDB   = {0,0,0,0,0,0,0,0,0};
	public static int[] _filesInDB_add   = {0,0,0,0,0,0,0,0,0};
	public static int[] _filesInDB_sub   = {0,0,0,0,0,0,0,0,0};
	public static int[] _rFileCheck   = {0,0,0,0,0,0,0,0,0};


	public static String _fileLocation = null;
	public static String _fileLocation_add= null;
	public static String _fileLocation_sub= null;
	public static String _fileName = null;
	public static String _fileName_add = null;
	public static String _fileName_sub = null;
	public static String[][] _loadedFile = null;
	public static int _rowsTotal;
	public static int _rowsTotal_add;
	public static int _rowsTotal_sub;
	// Algos info
	public static int _topK;
	public static int _columnsTotal;
	
	public static int _total_str_cols = 0;
	public static int[] _cType = new int[_maxFiles]; //_columnIndex_Type = null;
	public static short[] _cSize = new short[_maxFiles]; //_columnIndex_Size = null;	
	public static int _weAreAdding = 0;	
	public static int _weAreSubtracting = 0;
	public static CSVUtilities[] p3_csvFiles = new CSVUtilities[100];
	public static CSVUtilities[] p3_csvFiles_add = new CSVUtilities[100];
	public static CSVUtilities[] p3_csvFiles_sub = new CSVUtilities[100];
	public static MainDriver _db = new MainDriver();

	public static String addFileToSet_sub(ActionEvent e, javax.swing.text.Style style, StyledDocument doc,int tableChosen)  {

		String out = null;

		if (e.getActionCommand().equals("SUB")) {



			JFileChooser fileChooser = new JFileChooser(".");
			FileFilter filter = new ExtensionFileFilter("CSV,XLS,XLSX",
					new String[] { "CSV", "XLS", "XLSX" });

			fileChooser.setFileFilter(filter);

			int status = fileChooser.showOpenDialog(null);

			if (status == JFileChooser.APPROVE_OPTION) {
				out = fileChooser.getSelectedFile().getPath();
				_fileLocation = out;
				_fileName = fileChooser.getSelectedFile().getName();
				debug_Util("\n " +"table chosen :: "+tableChosen +" :   "+ 		
						"sub_Table Index  sub  :" + tableChosen,style, doc);				
				int x = loadCSVFile_in_2D_array(_fileLocation);
				if (x == -1) {
					debug_Util("\n error reading file from:" + _fileLocation, style, doc);
				} else {
					int columnsTotal=p3_csvFiles[tableChosen].get_ColumnsTotal();					
					debug_Util("\nTotal Rows   : " + x + "\nTotal Columns: "+ columnsTotal, style, doc);
					debug_Util("\nColumn Types :", style, doc);
					for(int i9 = 0; i9 < columnsTotal; i9++)
						  debug_Util(" " + _cType[i9], style, doc);
						//debug_Util(" " + p3_csvFiles[tableChosen].getType(i9), style, doc);
					debug_Util("\nColumn sizes :", style, doc);
					for (int i9 = 0; i9 < columnsTotal; i9 ++)
						  debug_Util(" " + _cSize[i9], style, doc);
						//debug_Util(" " + p3_csvFiles[tableChosen].getcSize(i9), style, doc);
					// -1 means this is not an add call, but a sub call
					CSVUtilities a = new CSVUtilities(-1,x,tableChosen);
					a.createFileInfo(style, doc );
					_filesInSet_sub[tableChosen] = 1;
					p3_csvFiles_sub[tableChosen] = a;
					_totalFiles_sub++;
				}
			}
		}

		return out;
	}

	public static String addFileToSet_add(ActionEvent e, javax.swing.text.Style style, StyledDocument doc,int tableChosen)  {

		String out = null;

		if (e.getActionCommand().equals("ADD")) {



			JFileChooser fileChooser = new JFileChooser(".");
			FileFilter filter = new ExtensionFileFilter("CSV,XLS,XLSX",
					new String[] { "CSV", "XLS", "XLSX" });

			fileChooser.setFileFilter(filter);

			int status = fileChooser.showOpenDialog(null);

			if (status == JFileChooser.APPROVE_OPTION) {
				out = fileChooser.getSelectedFile().getPath();
				_fileLocation = out;
				_fileName = fileChooser.getSelectedFile().getName();
				debug_Util("\n " +"table chosen :: "+tableChosen +" :   "+ 		
						"add_Table Index  add  :" + tableChosen,style, doc);				
				int x = loadCSVFile_in_2D_array(_fileLocation);
				if (x == -1) {
					debug_Util("\n error reading file from:" + _fileLocation, style, doc);
				} else {
					int columnsTotal=p3_csvFiles[tableChosen].get_ColumnsTotal();					
					debug_Util("\nTotal Rows   : " + x + "\nTotal Columns: "+ columnsTotal, style, doc);
					debug_Util("\nColumn Types :", style, doc);
					for(int i9 = 0; i9 < columnsTotal; i9++)
						  debug_Util(" " + _cType[i9], style, doc);
						//debug_Util(" " + p3_csvFiles[tableChosen].getType(i9), style, doc);
					debug_Util("\nColumn sizes :", style, doc);
					for (int i9 = 0; i9 < columnsTotal; i9 ++)
						  debug_Util(" " + _cSize[i9], style, doc);
						//debug_Util(" " + p3_csvFiles[tableChosen].getcSize(i9), style, doc);
					// -1 means this is not a sub call, but a add call
					CSVUtilities a = new CSVUtilities(tableChosen,x,-1);
					a.createFileInfo(style, doc );
					_filesInSet_add[tableChosen] = 1;
					p3_csvFiles_add[tableChosen] = a;
					_totalFiles_add++;
				}
			}
		}

		return out;
	}
	public static String addFileToSet(ActionEvent e, javax.swing.text.Style style, StyledDocument doc)  {

		String out = null;

		if (e.getActionCommand().equals("ADDFILETOSET")) {

			JFileChooser fileChooser = new JFileChooser(".");
			FileFilter filter = new ExtensionFileFilter("CSV,XLS,XLSX",
					new String[] { "CSV", "XLS", "XLSX" });

			fileChooser.setFileFilter(filter);

			int status = fileChooser.showOpenDialog(null);

			if (status == JFileChooser.APPROVE_OPTION) {
				out = fileChooser.getSelectedFile().getPath();
				_fileLocation = out;
				_fileName = fileChooser.getSelectedFile().getName();
				debug_Util("\nTable Index  :" + _totalFiles, style, doc);

				int x = loadCSVFile_in_2D_array(_fileLocation);
				if (x == -1) {
					debug_Util("\n error reading file from:" + _fileLocation, style, doc);

				} else {

					debug_Util("\nTotal Rows   : " + x + "\nTotal Columns: "+ _columnsTotal, style, doc);
					debug_Util("\nColumn Types :", style, doc);
					for(int i9 = 0; i9 < _columnsTotal; i9++)
						debug_Util(" " + _cType[i9], style, doc);
					debug_Util("\nColumn sizes :", style, doc);
					for (int i9 = 0; i9 < _columnsTotal; i9 ++)
						debug_Util(" " + _cSize[i9], style, doc);			

					CSVUtilities a = new CSVUtilities();
					a.createFileInfo(style, doc);
					_filesInSet[_totalFiles] = 1;
					p3_csvFiles[_totalFiles++] = a;
				}
			}
		}

		return out;
	}


	public static String ViewFiles( ActionEvent e, javax.swing.text.Style style, StyledDocument doc)
	{ String out = " ";
	//debug.append("\n*inside util func");
	if (e.getActionCommand().equals("VIEWFILEINSET")) {
		//debug.append("\n*inside util func2");
		for (int i = 0; i < _maxFiles; i++) 
		{
			if (_filesToView[i] == 1)
			{ 
				String _fileLoc = p3_csvFiles[i].getFileLocation();
				String _fileName = p3_csvFiles[i].getFileName();
				int _Colcount = p3_csvFiles[i].getColCount();



				//debug.append("\n*inside util func3");
				String[] row = null;
				CSVReader csvReader = null;
				try {
					JTextArea fileContent  = new JTextArea(10,50);
					fileContent.setEditable(false);
					fileContent.setLineWrap(false);
					fileContent.setAutoscrolls(true);


					int recordCount = 0;
					csvReader = new CSVReader(new FileReader(new File(_fileLoc)));
					row = csvReader.readNext();		


					while (row != null) {
						recordCount++;
						//debug.append("\n" + recordCount + "\t");
						fileContent.append("\n" + recordCount + "\t");
						for (int k = 0; k < _Colcount; k++) {
							//debug.append(row[k] + "\t");
							fileContent.append(row[k] + "\t");
						}

						row = csvReader.readNext();
						// debug.append("\n");

					}
					csvReader.close();

					JScrollPane scrollPane = new JScrollPane(fileContent);
					scrollPane
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					scrollPane.setBounds(10, 10, 400, 400);
					scrollPane
					.setBorder(BorderFactory.createBevelBorder(2));

					JFrame f = new JFrame(_fileName);

					///
					//f.setUndecorated(true);
					//f.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
					///
					f.getContentPane().setBackground(Color.magenta);
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					f.getContentPane().add(scrollPane);
					f.pack();

					f.setVisible(true);
					debug_Util_color("\n displayed:" + _fileName, style, doc, Color.magenta);


				} catch (Exception e1) {
					debug_Util_color("error reading from file: " + _fileLoc + e.toString(), style, doc, Color.red);
					return " ";
				}

			}
			// i = _maxFiles + 1;// move after finish testing
		}


		out = "above file/s opened in new window/s on desktop for viewing\n";
		out = out + "you can close new window/s at will";
	}

	return out;
	}




	public static String PrintFilesInfo(ActionEvent e, javax.swing.text.Style style, StyledDocument doc)
	{	String out = " ";

	if (e.getActionCommand().equals("PRINTFILEINFO")) {
		debug_Util("\n Files in Set:", style, doc);
		for (int i = 0; i < _maxFiles; i++) {
			debug_Util("" + _filesInSet[i], style, doc);
		}

		debug_Util("\n Files in DB :", style, doc);
		for (int i = 0; i < _maxFiles; i++) {
			debug_Util("" + _filesInDB[i], style, doc);
		}
		
		debug_Util("\n Files in ADD Set :", style, doc);
		for (int i = 0; i < _maxFiles; i++) {
			debug_Util("" + _filesInDB_add[i], style, doc);
		}
		
		debug_Util("\n Files in SUB Set :", style, doc);
		for (int i = 0; i < _maxFiles; i++) {
			debug_Util("" + _filesInDB_sub[i], style, doc);
		}
		
		for (int i = 0; i < _maxFiles; i++) {
			if (_filesToView[i]== 1) {
				p3_csvFiles[i].printInfo(style, doc);
				if (_filesInDB_add[i]== 2 )
					{p3_csvFiles_add[i].printInfo(style, doc);}
				if (_filesInDB_sub[i]== 2 )
				{p3_csvFiles_sub[i].printInfo(style, doc);}
			
				
			}
		}
		
		
		out = "\n-file info printed";
	}

	return out;
	}

	public static void debug_Util(String str,javax.swing.text.Style style, StyledDocument doc) {
		////debug.append(str + "\n");
		///style = debug.addStyle("Green",null);
		StyleConstants.setForeground(style,Color.black);
		try {
			doc.insertString(doc.getLength(), str, style);
		} catch (BadLocationException e1) {			
			e1.printStackTrace();
		}

	}


	public static void debug_Util_color(String str,javax.swing.text.Style style, StyledDocument doc, Color fg ) {
		////debug.append(str + "\n");
		///style = debug.addStyle("Green",null);
		StyleConstants.setForeground(style,fg);
		try {
			doc.insertString(doc.getLength(), str, style);
		} catch (BadLocationException e1) {			
			e1.printStackTrace();
		}
		// StyleConstants.setForeground(style,Color.black);

	}




	
	private static int loadCSVFile_in_2D_array(String _fileLoc) {

		String[] row = null;
		CSVReader csvReader = null;
		try {
			csvReader = new CSVReader(new FileReader(new File(_fileLoc)));

			row = csvReader.readNext();

			int recordCount = 0;
			while (row != null) {
				recordCount++;
				row = csvReader.readNext();

				// !!! this does not save the file in memory 
				// !! it only does a row count
				// the vInFile button displays the file from the 
				// original file location
			}
			if(_weAreAdding==1)
			{
				_rowsTotal_add = recordCount;
				
			}
			else
			{
				if(_weAreSubtracting==1)
				{_rowsTotal_sub = recordCount;}
				else
				{_rowsTotal = recordCount;}
			}	
			
			csvReader.close();

		} catch (Exception e) {
			// System.err.println("error reading from file: " + _fileLoc);
			return (-1);
		}
		

		if(_weAreSubtracting==1)
			{return _rowsTotal_sub;}
		else
		{

		if(_weAreAdding==1)
		{return _rowsTotal_add;	}
		else
			return (_rowsTotal);
		}
		
	

	}


}







////////////////////////////////////////////////////////////////////


class ExtensionFileFilter extends FileFilter {
	String description;

	String extensions[];

	public ExtensionFileFilter(String description, String extension) {
		this(description, new String[] { extension });
	}

	public ExtensionFileFilter(String description, String extensions[]) {
		if (description == null) {
			this.description = extensions[0];
		} else {
			this.description = description;
		}
		this.extensions = (String[]) extensions.clone();
		toLower(this.extensions);
	}

	private void toLower(String array[]) {
		for (int i = 0, n = array.length; i < n; i++) {
			array[i] = array[i].toLowerCase();
		}
	}

	public String getDescription() {
		return description;
	}

	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		} else {
			String path = file.getAbsolutePath().toLowerCase();
			for (int i = 0, n = extensions.length; i < n; i++) {
				String extension = extensions[i];
				if ((path.endsWith(extension) && (path.charAt(path.length()
						- extension.length() - 1)) == '.')) {
					return true;
				}
			}
		}
		return false;
	}
}
