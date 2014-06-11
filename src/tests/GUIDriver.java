package tests;
/*
/*
* driverGUI.java
* graphical user interface for phase three
*/


/**
* @author zelpha
*
*/

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.jws.soap.SOAPBinding.Style;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;




public class GUIDriver implements ActionListener {
	
	private static final String Sna = "0";
	private static final int maxCType = 6;
	private static final int maxCSize = 6;
	private static final int maxFiles = 9;
	private static final String colType_str[] = {" ", "Stri","Inte","Real","Sym","Null"};
	private static final String colSize_str[] = {" ","16","24","32","40","48","56"};
	private static final String topK_arr_str[] = {"1","2","3","4","5","6","7","8","9","10","11"};
	private static final String numOfConditions_arr_str[] = {"0","1","2","3","4"};
	private static final String matchValues[] = {" ","0","1","2","3","4","5","6"};

	
	private static JTextArea debug1;
	private static JTextPane debug;
	private static JTextPane analysis;
	private static StyledDocument doc;
	private static StyledDocument doc2;
	private static javax.swing.text.Style style;
	private static javax.swing.text.Style style2;
	private ButtonGroup[] bg;
	
	private String[] fileColumns_str = {"0","1", "2","3", "4", "5", "6", "7"};
	private int[]    fileColumns_int = {0,1,2,3,4,5,6,7};
	
	private ButtonGroup radioBG = new ButtonGroup();
	private JRadioButton totCols1 = new JRadioButton("1");
	private JRadioButton totCols2 = new JRadioButton("2");
	private JRadioButton totCols3 = new JRadioButton("3");
	private JRadioButton totCols4 = new JRadioButton("4");
	private JRadioButton totCols5 = new JRadioButton("5");
	private JRadioButton totCols6 = new JRadioButton("6");
	private JRadioButton totCols7 = new JRadioButton("7");
	
	
	private JCheckBoxMenuItem[] t2, t3, t4, t5, cspc,selectedImage;
	private JTextField path;
	private JButton btn_addAFile;
	private JButton btn_viewAfile;
	private JButton btn_addFileToDb;
	private JButton btn_viewADBFile;
	private JButton btn_vCfile;
	private JButton btn_vRfile;
	private JButton btn_clear;
	private JButton btn_printInfo;
	
	
	private JButton btn_dIfile;
	private JButton btn_dCfile;
	private JButton btn_dRfile;
	
	private JButton btn_fa;
	private JButton btn_ta;
	private JButton btn_nra;
	private JButton btn_sc;
	private JButton btn_scFa;
	private JButton btn_scTa;
	private JButton btn_scNa;
	private JButton btn_add;
	private JButton btn_sub;
	
	private int totalColumnsx = 3;
	private int totalFiles = 0;
	private int totalFilesCreated = 0;
	private int totalFilesResult = 0;
	private JLabel totalFiles_;
	private JLabel totalFilesCreated_;
	private JLabel totalFilesResult_;
	private int topK_int = 5;
	private int numOfConditions = 0;
	private int amountOfMemory = 50;
	private JCheckBox cbox_ignore = null;
	private JCheckBox cbox_useIndicator = null;
	
	private JTextField[] cType = new JTextField[maxCType];
	private JTextField[] cSize = new JTextField[maxCSize];
	
	private JComboBox[] cType1 = new JComboBox[maxCType];
	private JComboBox[] cSize1 = new JComboBox[maxCSize];
	private JComboBox[] match1 = new JComboBox[maxFiles];
	private JComboBox[] match2 = new JComboBox[maxFiles];
	private JComboBox[] match3 = new JComboBox[maxFiles];
	//private JComboBox topK_combo;
	private JTextField txf_topK;
	private JComboBox numOfConditions_combo;
	private JTextField txf_amountOfMemory;
	
	private JCheckBox[] fileSet = new JCheckBox[maxFiles];
	private JCheckBox[] cfiles  = new JCheckBox[maxFiles];
	private JCheckBox[] rfiles  = new JCheckBox[maxFiles];


	

	/*
	 * wrapper for appending string to debug window
	 */
	public void write(String str) {
		////debug.append(str + "\n");
		///style = debug.addStyle("Green",null);
		StyleConstants.setForeground(style,Color.black);
		try {
			doc.insertString(doc.getLength(), str, style);
		} catch (BadLocationException e1) {			
			e1.printStackTrace();
		}
		
	}
	
	public void writeColor(String str, Color fg ) {
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

	/*
	 * wrapper for appending string to debug window
	 */
	public void writeA(String str) {
		////debug.append(str + "\n");
		///style = debug.addStyle("Green",null);
		StyleConstants.setForeground(style,Color.black);	
		try {
			doc2.insertString(doc2.getLength(), str, style2);
		} catch (BadLocationException e1) {			
			e1.printStackTrace();
		}
		
	}
	
	public void writeColorA(String str, Color fg ) {
		////debug.append(str + "\n");
		///style = debug.addStyle("Green",null);
	   StyleConstants.setForeground(style2,fg);
		try {
			doc2.insertString(doc2.getLength(), str, style2);
		} catch (BadLocationException e1) {			
			e1.printStackTrace();
		}
	  // StyleConstants.setForeground(style,Color.black);
		
	}

	
	
	
	/*
	 * creates and populate the menu
	 */
	public JMenuBar createMenu() {

		JMenuBar bar;
		JMenu menu;
		JMenuItem item;
		JCheckBoxMenuItem checkBox;
		bg = new ButtonGroup[7];

		// create the menu bar.
		bar = new JMenuBar();

		/**
		 * add a file menu to the bar
		 */
		menu = new JMenu("file");
		menu.setMnemonic(KeyEvent.VK_F);
		bar.add(menu);


		// add open to the file checkBox
		item = new JMenuItem("add data File");
		item.setActionCommand("ADDDATAFILE");
		item.addActionListener(this);
		menu.add(item);
		
		// add open to the file checkBox
		item = new JMenuItem("View selected image");
		item.setActionCommand("VIEWSELECTEDIMAGE");
		item.addActionListener(this);
		menu.add(item);	
		
	
		
		// separator
		menu.addSeparator();

		// add close to the file checkBox
		item = new JMenuItem("close");
		item.setActionCommand("CLOSE");
		item.addActionListener(this);
		menu.add(item);

	


		return bar;
	}

	/*
	 * creates the container panels
	 */
	public Container createContentPane() {

		int xoff = 10;
		int yoff = 10;
		int half = 5;
		int aWidth =800;
		int aHeight = 250;
		int bWidth = 800;
		int bHeight = 250;
		JLabel col0,col1,col2, col3, col4, col5, col6, lbl_topK;
		JLabel colType, colSize, totalCols, columns, attrType;		
		
		
		
		// initializes the panel
		JPanel container = new JPanel();
		container.setLayout(null);
		// container.setBounds(0,0,300,300);
		// container.setOpaque(false);

		JPanel optionContainer = new JPanel();
		optionContainer.setLayout(null);
		optionContainer.setBounds(xoff, yoff, aWidth + 18 , aHeight);
		optionContainer.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createBevelBorder(0), "Program Inputs"));
		container.add(optionContainer);	
		

		JPanel colInfo, colLabels, otherData1, otherData2, otherData6;	
		
		

		totalCols = new JLabel("Total Columns:");	
		totalCols.setForeground(Color.gray);
		//totalColumnsx = new JTextField("3", 3);
		
		otherData1 = new JPanel();
		GridLayout a1 = new GridLayout(1,1);
		otherData1.setLayout(a1);
		otherData1.setBounds(2 * xoff, half + yoff,140 , 20);
		otherData1.add(totalCols);
		//otherData1.add(totalColumnsx);	
		//otherData1.add(radioBG);
		optionContainer.add(otherData1);			
		
		//attrType = new JLabel("Str:0 Int:1 Real:2 Sym:3 Nul:4");			
		otherData2 = new JPanel();
		GridLayout a2 = new GridLayout(1,9);
		otherData2.setLayout(a2);
		otherData2.setBounds((9 * xoff) + 60 , half + yoff, 300 , 20);
		//otherData2.add(attrType);
		
		totCols1.setActionCommand("1"); totCols1.setForeground(Color.gray);
		totCols2.setActionCommand("2"); totCols2.setForeground(Color.gray);
		totCols3.setActionCommand("3"); totCols3.setForeground(Color.gray);
			totCols3.setSelected(true);
		totCols4.setActionCommand("4"); totCols4.setForeground(Color.gray);
		totCols5.setActionCommand("5"); totCols5.setForeground(Color.gray);
		totCols6.setActionCommand("6"); totCols6.setForeground(Color.gray);
		//totCols7.setActionCommand("7");	
				
		radioBG.add(totCols1);
		radioBG.add(totCols2);
		radioBG.add(totCols3);
		radioBG.add(totCols4);
		radioBG.add(totCols5);
		radioBG.add(totCols6);
		//radioBG.add(totCols7);
		
		totCols1.addActionListener(this);
		totCols2.addActionListener(this);
		totCols3.addActionListener(this);
		totCols4.addActionListener(this);
		totCols5.addActionListener(this);
		totCols6.addActionListener(this);
		//totCols7.addActionListener(this);
				
		otherData2.add(totCols1);
		otherData2.add(totCols2);
		otherData2.add(totCols3);
		otherData2.add(totCols4);
		otherData2.add(totCols5);
		otherData2.add(totCols6);
		//otherData2.add(totCols7);		
		optionContainer.add(otherData2);	
		
		
		
		
		columns = new JLabel("Info");
		columns.setForeground(Color.gray);
		col0 = new JLabel("col0");
		col1 = new JLabel("col1");
		col2 = new JLabel("col2");
		col3 = new JLabel("col3");
		col4 = new JLabel("col4");
		col5 = new JLabel("col5");
		col6 = new JLabel("col6");
		col0.setForeground(Color.gray);
		col1.setForeground(Color.gray);
		col2.setForeground(Color.gray);
		col3.setForeground(Color.gray);
		col4.setForeground(Color.gray);
		col5.setForeground(Color.gray);
		
		colLabels = new JPanel();
		GridLayout b = new GridLayout(1,7);
		colLabels.setLayout(b);
		colLabels.setBounds(2* xoff, half + (3 * yoff), 400, 20);
		colLabels.add(columns);
		colLabels.add(col0);
		colLabels.add(col1);
		colLabels.add(col2);
		colLabels.add(col3);
		colLabels.add(col4);
		colLabels.add(col5);		
		optionContainer.add(colLabels);
		
	
		
		
		colType = new JLabel("Type");
		colType.setForeground(Color.gray);
		cType1[0] = new JComboBox(colType_str);
			cType1[0].setSelectedIndex(1);
		cType1[1] = new JComboBox(colType_str);
			cType1[1].setSelectedIndex(1);
		cType1[2] = new JComboBox(colType_str);
			cType1[2].setSelectedIndex(3);
		cType1[3] = new JComboBox(colType_str);
		cType1[4] = new JComboBox(colType_str);
		cType1[5] = new JComboBox(colType_str);
		cType[5] = new JTextField(Sna, 3);
		
		
		colSize = new JLabel("Size");
		colSize.setForeground(Color.gray);
		cSize1[0] = new JComboBox(colSize_str);
			cSize1[0].setSelectedIndex(3);
		cSize1[1] = new JComboBox(colSize_str);
			cSize1[1].setSelectedIndex(3);
		cSize1[2] = new JComboBox(colSize_str);
		cSize1[3] = new JComboBox(colSize_str);
		cSize1[4] = new JComboBox(colSize_str);
		cSize1[5] = new JComboBox(colSize_str);
		
		JLabel lblmatch1 = new JLabel("Join Columns");
		match1[0] = new JComboBox(matchValues);
		match1[1] = new JComboBox(matchValues);
		match1[2] = new JComboBox(matchValues);
		match1[3] = new JComboBox(matchValues);
		match1[4] = new JComboBox(matchValues);
		match1[5] = new JComboBox(matchValues);
		match1[6] = new JComboBox(matchValues);
		match1[7] = new JComboBox(matchValues);
		match1[8] = new JComboBox(matchValues);
		
		JLabel lblmatch2 = new JLabel("match2");
		match2[0] = new JComboBox(matchValues);
		match2[1] = new JComboBox(matchValues);
		match2[2] = new JComboBox(matchValues);
		match2[3] = new JComboBox(matchValues);
		match2[4] = new JComboBox(matchValues);
		match2[5] = new JComboBox(matchValues);

		JLabel lblmatch3 = new JLabel("match3");
		match3[0] = new JComboBox(matchValues);
		match3[1] = new JComboBox(matchValues);
		match3[2] = new JComboBox(matchValues);
		match3[3] = new JComboBox(matchValues);
		match3[4] = new JComboBox(matchValues);
		match3[5] = new JComboBox(matchValues);
		
		//cSize[5] = new JTextField(Sna, 3);
		
		//for (int ik =0; ik<6; ik++)
		//{cType[ik].setForeground(Color.gray);
		// cSize[ik].setForeground(Color.gray);
		//}
	
		
		colInfo = new JPanel();
		GridLayout c = new GridLayout(2,7);
		colInfo.setLayout(c);
		colInfo.setBounds(2* xoff,half + (5 * yoff), 400, 40);
		colInfo.add(colType);
		colInfo.add(cType1[0]);
		colInfo.add(cType1[1]);
		colInfo.add(cType1[2]);
		colInfo.add(cType1[3]);
		colInfo.add(cType1[4]);		
		colInfo.add(cType1[5]);

		colInfo.add(colSize);		
		colInfo.add(cSize1[0]);	
		colInfo.add(cSize1[1]);	
		colInfo.add(cSize1[2]);	
		colInfo.add(cSize1[3]);	
		colInfo.add(cSize1[4]);	
		colInfo.add(cSize1[5]);		
		optionContainer.add(colInfo);
		
		

		//totalCols = new JLabel("TotCols:");	
		//totalCols.setForeground(Color.gray);
		//totalColumnsx = new JTextField("3", 3);
		
		JPanel otherData1K = new JPanel();
		GridLayout a1K = new GridLayout(1,1);
		otherData1K.setLayout(a1K);
		otherData1K.setBounds(2* xoff,half + (15 * yoff), 440, 20);
		otherData1K.add(lblmatch1);
		//otherData1.add(totalColumnsx);	
		//otherData1.add(radioBG);
		optionContainer.add(otherData1K);	
		
		
		
		
		

		JPanel matchInfo = new JPanel();
		GridLayout c2 = new GridLayout(1,8);
		matchInfo.setLayout(c2);
		matchInfo.setBounds(2* xoff,half + (17 * yoff), 440, 20);
		//matchInfo.add(lblmatch1);
		matchInfo.add(match1[0]);
		matchInfo.add(match1[1]);
		matchInfo.add(match1[2]);
		matchInfo.add(match1[3]);
		matchInfo.add(match1[4]);		
		matchInfo.add(match1[5]);
		matchInfo.add(match1[6]);
		matchInfo.add(match1[7]);		
		matchInfo.add(match1[8]);

		/*	matchInfo.add(lblmatch2);		
		matchInfo.add(match2[0]);	
		matchInfo.add(match2[1]);	
		matchInfo.add(match2[2]);	
		matchInfo.add(match2[3]);	
		matchInfo.add(match2[4]);	
		matchInfo.add(match2[5]);	
		

		matchInfo.add(lblmatch3);		
		matchInfo.add(match3[0]);	
		matchInfo.add(match3[1]);	
		matchInfo.add(match3[2]);	
		matchInfo.add(match3[3]);	
		matchInfo.add(match3[4]);	
		matchInfo.add(match3[5]);	*/
		optionContainer.add(matchInfo);		
		
		lbl_topK = new JLabel("Top K");
		txf_topK = new JTextField("5", 3);
		//topK_combo = new JComboBox(topK_arr_str);
		//topK_combo.setSelectedIndex(4);
		//topK_combo.setActionCommand("TOPK");
		
		JLabel lbl_numOfConditions = new JLabel("noc");
		numOfConditions_combo = new JComboBox(numOfConditions_arr_str);		
		
		JLabel lbl_amtMem = new JLabel ("AmtOfMem");
		txf_amountOfMemory = new JTextField("" +amountOfMemory, 3);
		
		JLabel lbl_ignore = new JLabel ("add/sub");
		cbox_ignore  = new JCheckBox("ignore");
		JLabel lbl_useIndicator = new JLabel ("indicator");
		cbox_useIndicator = new JCheckBox("use");
		
		otherData6 = new JPanel();
		GridLayout a6 = new GridLayout(10,1);
		otherData6.setLayout(a6);
		otherData6.setBounds(5 * xoff + 680, half + yoff,60 + 20 , 170);
		otherData6.add(lbl_topK);
		otherData6.add(txf_topK);
		//otherData6.add(topK_combo);
		//otherData6.add(lbl_numOfConditions);
		//otherData6.add(numOfConditions_combo);
		otherData6.add(lbl_amtMem);
		otherData6.add(txf_amountOfMemory);
		/*otherData6.add(lbl_ignore);
		otherData6.add(cbox_ignore);
		otherData6.add(lbl_useIndicator);		
		otherData6.add(cbox_useIndicator);
		*/
		
		optionContainer.add(otherData6);
		
		
		
		
		
		
		JLabel files = new JLabel();	
		totalFiles_ = new JLabel(" " + totalFiles);
		fileSet[0] =  new JCheckBox("0");		
		fileSet[1] =  new JCheckBox("1");				
		fileSet[2] =  new JCheckBox("2");				
		fileSet[3] =  new JCheckBox("3");				
		fileSet[4] =  new JCheckBox("4");				
		fileSet[5] =  new JCheckBox("5");				
		fileSet[6] =  new JCheckBox("6");				
		fileSet[7] =  new JCheckBox("7");		
		fileSet[8] =  new JCheckBox("8");
		
		JLabel cnt = new JLabel("Table Count");
		
		JPanel otherData1K2 = new JPanel();
		GridLayout a1K2 = new GridLayout(1,2);
		otherData1K2.setLayout(a1K);
		otherData1K2.setBounds(2* xoff,half + (10 * yoff), 200, 20);
		otherData1K2.add(cnt);
		otherData1K2.add(totalFiles_);
		//otherData1.add(totalColumnsx);	
		//otherData1.add(radioBG);
		optionContainer.add(otherData1K2);	
		
		
		
	
		
		
		JPanel otherData3 = new JPanel();
		GridLayout a3 = new GridLayout(1, 11);
		otherData3.setLayout(a3);
		
		otherData3.setBounds(2* xoff,  12 * yoff,460 , 20);
		
			//cnt.setForeground(Color.magenta);
			//cnt.setFont(new Font(Font.SERIF, Font.BOLD, 16));

		for (int ik =0; ik<9; ik++)
		{//fileSet[ik].setForeground(Color.magenta);
		// cSize[ik].setForeground(Color.gray);
		}
		//totalFiles_.setForeground(Color.magenta);
		
						//otherData3.add(cnt);
						//otherData3.add(totalFiles_);
		otherData3.add(fileSet[0]);
		otherData3.add(fileSet[1]);
		otherData3.add(fileSet[2]);
		otherData3.add(fileSet[3]);
		otherData3.add(fileSet[4]);
		otherData3.add(fileSet[5]);
		otherData3.add(fileSet[6]);
		otherData3.add(fileSet[7]);
		otherData3.add(fileSet[8]);
		
		
		optionContainer.add(otherData3);
		
		
		
		///

		JLabel cfile = new JLabel();	
		totalFilesCreated_ = new JLabel(" " + totalFilesCreated);
		cfiles[0] =  new JCheckBox("0");		
		cfiles[1] =  new JCheckBox("1");				
		cfiles[2] =  new JCheckBox("2");				
		cfiles[3] =  new JCheckBox("3");				
		cfiles[4] =  new JCheckBox("4");				
		cfiles[5] =  new JCheckBox("5");				
		cfiles[6] =  new JCheckBox("6");				
		cfiles[7] =  new JCheckBox("7");		
		cfiles[8] =  new JCheckBox("8");					
		
		JPanel otherData4 = new JPanel();
		GridLayout a4 = new GridLayout(1, 11);
		otherData4.setLayout(a4);
		
		otherData4.setBounds(2* xoff,  12 * yoff,400 , 20);

		JLabel cC = new JLabel("cC:");
		cC.setForeground(Color.orange);
			//cC.setFont(new Font(Font.SERIF, Font.BOLD, 8));
		totalFilesCreated_.setForeground(Color.orange);

		for (int ik =0; ik<9; ik++)
		{cfiles[ik].setForeground(Color.orange);
		// cSize[ik].setForeground(Color.gray);
		}
		
		
		otherData4.add(cC);
		otherData4.add(totalFilesCreated_);
		otherData4.add(cfiles[0]);
		otherData4.add(cfiles[1]);
		otherData4.add(cfiles[2]);
		otherData4.add(cfiles[3]);
		otherData4.add(cfiles[4]);
		otherData4.add(cfiles[5]);
		otherData4.add(cfiles[6]);
		otherData4.add(cfiles[7]);
		otherData4.add(cfiles[8]);
		
		
		//optionContainer.add(otherData4);
		
		
		
		
		JLabel rfile = new JLabel();	
		totalFilesResult_ = new JLabel(" " + totalFilesResult);
		rfiles[0] =  new JCheckBox("0");		
		rfiles[1] =  new JCheckBox("1");				
		rfiles[2] =  new JCheckBox("2");				
		rfiles[3] =  new JCheckBox("3");				
		rfiles[4] =  new JCheckBox("4");				
		rfiles[5] =  new JCheckBox("5");				
		rfiles[6] =  new JCheckBox("6");				
		rfiles[7] =  new JCheckBox("7");		
		rfiles[8] =  new JCheckBox("8");
						
		
		JPanel otherData5 = new JPanel();
		GridLayout a5 = new GridLayout(1, 11);
		otherData5.setLayout(a5);
		
		otherData5.setBounds(2* xoff,  14 * yoff,400 , 20);
		otherData5.add(new JLabel("rC:"));
		otherData5.add(totalFilesResult_);
		otherData5.add(rfiles[0]);
		otherData5.add(rfiles[1]);
		otherData5.add(rfiles[2]);
		otherData5.add(rfiles[3]);
		otherData5.add(rfiles[4]);
		otherData5.add(rfiles[5]);
		otherData5.add(rfiles[6]);
		otherData5.add(rfiles[7]);
		otherData5.add(rfiles[8]);
		
		//optionContainer.add(otherData5);
		
		
		/////
		
		
		//valuesPanelD.setBorder(BorderFactory.createTitledBorder(
		//		BorderFactory.createLineBorder(Color.gray), "D2"));
					
		
		JPanel fileSet = new JPanel();
		fileSet.setBounds((8* xoff) + 400 , half + yoff, 140 + 3, 230);
		fileSet.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.black), "fileSet"));

		
		JPanel fileSet_ = new JPanel();
		GridLayout d = new GridLayout(11,1);
		fileSet_.setLayout(d);
		fileSet_.setBounds((8 * xoff) + 400  + 3, half + yoff + 3,70, 220);
	

//		btn_addAFile = new JButton("addFileToSet");
		btn_addAFile = new JButton("Add File To DB");
			//btn_addAFile.setBackground(Color.lightGray);
		btn_addAFile.setActionCommand("ADDFILETOSET");
		btn_addAFile.addActionListener(this);
				
//		btn_viewAfile = new JButton("viewFileInSet");
//			btn_viewAfile.setBackground(Color.lightGray);
//			btn_viewAfile.setForeground(Color.magenta);
//		btn_viewAfile.setActionCommand("VIEWFILEINSET");
//		btn_viewAfile.addActionListener(this);
		

//		btn_addFileToDb = new JButton("addFileToDB");
//		btn_addFileToDb.setBackground(Color.white);
//		btn_addFileToDb.setForeground(Color.magenta);
//		btn_addFileToDb.setActionCommand("ADDFILETODB");
//		btn_addFileToDb.addActionListener(this);
		
		btn_viewADBFile = new JButton("View File In DB");
		//btn_viewADBFile.setBackground(Color.white);
		//btn_viewADBFile.setForeground(Color.magenta);
		btn_viewADBFile.setActionCommand("VIEWFILEINDB");
		btn_viewADBFile.addActionListener(this);
		
		
		btn_vCfile = new JButton("vCrFile");
		btn_vCfile.setActionCommand("vCrFile");
		btn_vRfile = new JButton("vReFile");
		btn_vRfile.setActionCommand("vReFile");
		

		btn_dIfile = new JButton("dInFile");
		btn_dIfile.setActionCommand("dInFile");
		btn_dCfile = new JButton("dCrFile");
		btn_dCfile.setActionCommand("dCrFile");
		btn_dRfile = new JButton("dReFile");
		btn_dRfile.setActionCommand("dReFile");
		
		
		btn_clear = new JButton("clear");
		btn_clear.setActionCommand("CLEAR");
		btn_clear.addActionListener(this);
		
		btn_printInfo = new JButton("Info");
		btn_printInfo.setForeground(Color.blue);
		btn_printInfo.setActionCommand("PRINTFILEINFO");
		btn_printInfo.addActionListener(this);
		

		
		
		fileSet_.add(btn_addAFile);
		
		//fileSet_.add(new JLabel( "----"));
		//fileSet_.add(btn_viewAfile );
		//fileSet_.add(btn_addFileToDb);
		fileSet_.add(btn_viewADBFile);
	   // fileSet_.add(btn_vCfile );
		//fileSet_.add(btn_vRfile );
		//fileSet_.add(new JLabel("-----"));
		//fileSet_.add(btn_clear);
		fileSet_.add(btn_printInfo);
		fileSet_.add(new JLabel("--------------------------"));
		//fileSet_.add(btn_dIfile );
		//fileSet_.add(btn_dCfile );
		//fileSet_.add(btn_dRfile );
		fileSet_.add(lbl_ignore);
		fileSet_.add(cbox_ignore);
		fileSet_.add(lbl_useIndicator);		
		fileSet_.add(cbox_useIndicator);
				
		fileSet.add(fileSet_);
		optionContainer.add(fileSet);

			

		JPanel inAlgos = new JPanel();
		inAlgos.setBounds(half + (14 * xoff) + 500, half + yoff, 70 + 3, 130);
		inAlgos.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.black), "Algos"));

		
		JPanel inAlgos_ = new JPanel();
		GridLayout e = new GridLayout(4,1);
		inAlgos_.setLayout(e);
		inAlgos_.setBounds(half + (14 * xoff) + 500 + 3, half + yoff + 3,40, 120);
			
		
		btn_fa = new JButton("FA");
		btn_fa.setActionCommand("FA");
		btn_fa.addActionListener(this);
		//btn_fa.setPreferredSize(new Dimension(2,4));
		
		btn_ta = new JButton("TA");
		btn_ta.setActionCommand("TA");
		btn_ta.addActionListener(this);

		btn_nra = new JButton("NRA");
		btn_nra.setActionCommand("NRA");
		btn_nra.addActionListener(this);
		
		btn_sc = new JButton("SC");
		btn_sc.setActionCommand("SC");
		btn_sc.addActionListener(this);
		

		btn_scFa = new JButton("scFa");
		btn_scFa.setActionCommand("SCFA");
		btn_scTa = new JButton("SCTA");
		btn_scTa.setActionCommand("SCTA");
		btn_scNa = new JButton("SCNA");
		btn_scNa.setActionCommand("SCNA");
		
		
		btn_add = new JButton("ADD");
		btn_add.setActionCommand("ADD");
		btn_add.addActionListener(this);
		
		
		
		btn_sub = new JButton("SUB");
		btn_sub.setActionCommand("SUB");
		btn_sub.addActionListener(this);
		
		inAlgos_.add(btn_fa);
		//inAlgos_.add(new JLabel( "-----"));
		inAlgos_.add(btn_ta );
	    inAlgos_.add(btn_nra );
		//inAlgos_.add(new JLabel( "-----"));
		inAlgos_.add(btn_sc );
		//fileSet_.add(new JLabel("-----"));
		//fileSet_.add(btn_dIfile );
		//fileSet_.add(btn_dCfile );
		//fileSet_.add(btn_dRfile );
			
				
		inAlgos.add(inAlgos_);
		optionContainer.add(inAlgos);

		//////////////////////////////////////

		JPanel inSC = new JPanel();
		inSC.setBounds((6 * xoff) + 583, 140 + half + yoff, 80 + 3, 80);
		inSC.setBorder(BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(Color.black), "Update"));

		
		JPanel inSC_ = new JPanel();
		GridLayout f = new GridLayout(2,1);
		inSC_.setLayout(f);
		inSC_.setBounds((6 * xoff) + 580, 140 + half + yoff,50, 70);
			
		
		//btn_scFa.setPreferredSize(new Dimension(2,4));
		

		btn_scFa = new JButton("scFa");
		btn_scFa.setActionCommand("SCFA");
		btn_scTa = new JButton("SCTA");
		btn_scTa.setActionCommand("SCTA");
		btn_scNa = new JButton("SCNA");
		btn_scNa.setActionCommand("SCNA");
		
		/*
		btn_add = new JButton("add");
		btn_add.setActionCommand("ADD");
		*/
		
		/*btn_sub = new JButton("sub");
		btn_sub.setActionCommand("SUB");
		*/
		//inSC_.add(btn_scFa);
		//inSC_.add(btn_scTa );
	    //inSC_.add(btn_scNa );
		//inSC_.add(new JLabel("-----"));
		inSC_.add(btn_add);
		inSC_.add(btn_sub);
		//fileSet_.add(new JLabel("-----"));
		//fileSet_.add(btn_dIfile );
		//fileSet_.add(btn_dCfile );
		//fileSet_.add(btn_dRfile );
			
				
		inSC.add(inSC_);
		optionContainer.add(inSC);


		
		

		
		// create the debug area
		debug = new JTextPane(); //(10, 50);
		doc = (StyledDocument) debug.getStyledDocument();
		style = debug.addStyle("Blue",null);
		writeColor("By Group 4:       Nice of you to visit. Thanks\n", Color.blue);
		
		
		debug.setEditable(false);
		debug.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(debug);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(xoff, yoff * 4 + aHeight,  bWidth - bHeight - 30 + 300 , bHeight);
		scrollPane.setBorder(BorderFactory.createBevelBorder(1));

		// add the debug area to the container
		container.add(scrollPane);
		
		JLabel logInfo = new JLabel("Log Info");
		logInfo.setForeground(Color.blue);
		JPanel otherData11 = new JPanel();
		GridLayout a11 = new GridLayout(1,1);
		otherData11.setLayout(a11);
		otherData11.setBounds(xoff, yoff * 1 + aHeight + half,  60,20);
		otherData11.add(logInfo);
		container.add(otherData11);
		
		JLabel analysisInfo = new JLabel("Analysis Info");
		analysisInfo.setForeground(Color.blue);
		JPanel otherData12 = new JPanel();
		GridLayout a12 = new GridLayout(1,1);
		otherData12.setLayout(a12);
		otherData12.setBounds(xoff + bWidth - bHeight  - 20, yoff * 1 + aHeight + half,  100, 20);
		//otherData12.add(analysisInfo);
		
		container.add(otherData12);
		
		
		// create the Analysis area
		analysis = new JTextPane(); //(10, 50);
		doc2 = (StyledDocument) analysis.getStyledDocument();
		style2 = analysis.addStyle("Blue",null);
		writeColorA("Analysis\n", Color.blue);
		
		
		analysis.setEditable(false);
		analysis.setAutoscrolls(true);
		JScrollPane scrollPane2 = new JScrollPane(analysis);
		scrollPane2
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane2.setBounds(xoff + bWidth - bHeight  - 20,yoff * 4 + aHeight,  bHeight + 20, bHeight);
		scrollPane2.setBorder(BorderFactory.createBevelBorder(1));

		// add the debug area to the container
		//container.add(scrollPane2);
		
		
		


		return container;
	}
	
	

	/*
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {

		// create and set up the window
		JFrame frame = new JFrame("Group4: DBMI Sys, Spring 2013");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create and set up the content pane
		GUIDriver m = new GUIDriver();
		frame.setJMenuBar(m.createMenu());
		frame.setContentPane(m.createContentPane());

		// display the window.
		frame.setSize(840, 620);
		frame.setResizable(true);
		frame.setVisible(true);
	}

	/*
	 * main
	 */
	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {	public void run() {	createAndShowGUI();	}}     );
	}

	

	/*
	 * action performed by menu
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		String ts = null;
		String item = null;
		int go = 1;

		try {
			topK_int = Integer.parseInt(txf_topK.getText());//topK_combo.getSelectedIndex() + 1;
			totalColumnsx = Integer.parseInt(radioBG.getSelection().getActionCommand().toString());
			Utilities._topK = topK_int;
			amountOfMemory = Integer.parseInt(txf_amountOfMemory.getText().toString()); 
			MainDriver._amountOfMemory = amountOfMemory;
			MainDriver._ignore_increments = cbox_ignore.isSelected();
			MainDriver._useIndicator = cbox_useIndicator.isSelected();
			
			//write("\n " + amountOfMemory + " " + p3_mainDriver_db._ignore_increments + " " + p3_mainDriver_db._useIndicator);
			
			//write("inside Action Preformed function\n problem with top K input: "
					//+ topK_int + " " + totalColumnsx + "\n");
		} catch (Exception e1) {
			write("inside Action Preformed function\n problem with top K input: "
					+ topK_int + " " + totalColumnsx + "\n" + e1);
			go = 0;
		}

		if (go == 1) {
			int totalFiles = Utilities._totalFiles;
			switch (e.getActionCommand()) {

			case "ADDDATAFILE":
				//String ss = p3_Utilities_dmi.addAFile(e, debug);
				//if (ss != null) {
				//	write("\file added successfully");
				//}
				break;
			case "SC":
				int amtOfMemory = Integer.parseInt(txf_amountOfMemory.getText().toString());
				topK_int = Integer.parseInt(txf_topK.getText());
				if ((topK_int < 0) || (amtOfMemory < 1) || (Utilities._totalFiles < 2)) {
					writeColor("\n- you need to add files first, amt memory needs to be gt 0", Color.red);
				} else {
					try {
						int countx = 0;
						for (int i = 0; i < maxFiles; i++) {
							if ( (match1[i].getSelectedIndex() > 0) && fileSet[i].isSelected() && (Utilities._filesInDB[i] == 2)) {
								Utilities._filesToView[i] = 1;
								MainDriver._joinColIndices[i] = Integer.parseInt(match1[i].getSelectedItem().toString()) ;
								MainDriver._topK = topK_int;
								countx++;
							} else {
								Utilities._filesToView[i] = 0;
								MainDriver._joinColIndices[i]= -1;
								MainDriver._indexFiles[i] = "-1";
							}
						}
						
						if (countx > 0) {
							MainDriver._amountOfMemory = amtOfMemory;
							MainDriver.runSC(countx, style, doc);
						} else {
							writeColor("\n you need to choose\n amt memory, " +
									"file indexes, and select match columns before running TA", Color.red);
						}
					}

					catch (Exception e4) {
						e4.printStackTrace();
						write(" " + e4.toString());
					}
				}
				break;
			case "NRA":
				amtOfMemory = Integer.parseInt(txf_amountOfMemory.getText().toString());
				topK_int = Integer.parseInt(txf_topK.getText());
				if ((topK_int < 0) || (amtOfMemory < 1) || (Utilities._totalFiles < 2)) {
					writeColor("\n- you need to add files first, amt memory needs to be gt 0", Color.red);
				} else {
					try {
						int countx = 0;
						for (int i = 0; i < maxFiles; i++) {
							if ( (match1[i].getSelectedIndex() > 0) && fileSet[i].isSelected() && (Utilities._filesInDB[i] == 2)) {
								Utilities._filesToView[i] = 1;
								MainDriver._joinColIndices[i] = Integer.parseInt(match1[i].getSelectedItem().toString()) ;
								MainDriver._topK = topK_int;
								countx++;
							} else {
								Utilities._filesToView[i] = 0;
								MainDriver._joinColIndices[i]= -1;
								MainDriver._indexFiles[i] = "-1";
							}
						}
						
						if (countx > 0) {
							MainDriver._amountOfMemory = amtOfMemory;
							MainDriver.runNRA(countx, style, doc);
//							int taGoodToGo = p3_mainDriver_db.verifyTA_perameters(e, countx, style, doc);
//							if (taGoodToGo > 0) {
//								String zz1 = "ttest";// p3_mainDriver_db.runTopTAJoin1(e,
//														// style, doc);
//								if (zz1 != null) {
//									totalFiles_.setText("" + ++totalFiles);
//									writeColor("\nfile added successfully: "
//											+ zz1, Color.gray);
//								}
//
//							}

						} else {
							writeColor("\n you need to choose\n amt memory, " +
									"file indexes, and select match columns before running TA", Color.red);
						}
					}

					catch (Exception e4) {
						e4.printStackTrace();
						write(" " + e4.toString());
					}
				}
				break;
			case "FA":
				amtOfMemory = Integer.parseInt(txf_amountOfMemory.getText().toString());
				topK_int = Integer.parseInt(txf_topK.getText());
				if ((topK_int < 0) || (amtOfMemory < 1) || (Utilities._totalFiles < 2)) {
					writeColor("\n- you need to add files first, amt memory needs to be gt 0", Color.red);
				} else {
					try {
						int countx = 0;
						for (int i = 0; i < maxFiles; i++) {
							if ( (match1[i].getSelectedIndex() > 0) && fileSet[i].isSelected() && (Utilities._filesInDB[i] == 2)) {
								Utilities._filesToView[i] = 1;
								MainDriver._joinColIndices[i] = Integer.parseInt(match1[i].getSelectedItem().toString()) ;
								MainDriver._topK = topK_int;
								countx++;
							} else {
								Utilities._filesToView[i] = 0;
								MainDriver._joinColIndices[i]= -1;
								MainDriver._indexFiles[i] = "-1";
								//writeColor("\n- you need to add files to DB first or select from Match", Color.red);
							}
						}
						
						if (countx > 0) {
							MainDriver._amountOfMemory = amtOfMemory;
							MainDriver.runFA(countx, style, doc);
//							int taGoodToGo = p3_mainDriver_db.verifyTA_perameters(e, countx, style, doc);
//							if (taGoodToGo > 0) {
//								String zz1 = "ttest";// p3_mainDriver_db.runTopTAJoin1(e,
//														// style, doc);
//								if (zz1 != null) {
//									totalFiles_.setText("" + ++totalFiles);
//									writeColor("\nfile added successfully: "
//											+ zz1, Color.gray);
//								}
//
//							}

						} else {
							writeColor("\n you need to choose\n amt memory, " +
									"file indexes, and select match columns before running TA", Color.red);
						}
					}

					catch (Exception e4) {
						write(" " + e4.toString());
					}
				}
				break;
			case "TA":
				amtOfMemory = Integer.parseInt(txf_amountOfMemory.getText().toString());
				topK_int = Integer.parseInt(txf_topK.getText());
				if ((topK_int < 0) || (amtOfMemory < 1) || (Utilities._totalFiles < 2)) {
					writeColor("\n- you need to add files first, amt memory needs to be gt 0", Color.red);
				} else {
					try {
						int countx = 0;
						for (int i = 0; i < maxFiles; i++) {
							if ( (match1[i].getSelectedIndex() > 0) && fileSet[i].isSelected() && (Utilities._filesInDB[i] == 2)) {
								Utilities._filesToView[i] = 1;
								MainDriver._joinColIndices[i] = Integer.parseInt(match1[i].getSelectedItem().toString()) ;
								MainDriver._topK = topK_int;
								countx++;
							} else {
								Utilities._filesToView[i] = 0;
								MainDriver._joinColIndices[i]= -1;
								MainDriver._indexFiles[i] = "-1";
								//writeColor("\n- you need to add files to DB first or select from Match", Color.red);
							}
						}
						
						if (countx > 0) {
							MainDriver._amountOfMemory = amtOfMemory;
							MainDriver.runTA(countx, style, doc);
//							int taGoodToGo = p3_mainDriver_db.verifyTA_perameters(e, countx, style, doc);
//							if (taGoodToGo > 0) {
//								String zz1 = "ttest";// p3_mainDriver_db.runTopTAJoin1(e,
//														// style, doc);
//								if (zz1 != null) {
//									totalFiles_.setText("" + ++totalFiles);
//									writeColor("\nfile added successfully: "
//											+ zz1, Color.gray);
//								}
//
//							}

						} else {
							writeColor("\n you need to choose\n amt memory, " +
									"file indexes, and select match columns before running TA", Color.red);
						}
					}

					catch (Exception e4) {
						write(" " + e4.toString());
					}
				}
				break;
			case "ADD":
				write("\ngood so farh");

				Utilities._weAreAdding= 0;
				Utilities._weAreSubtracting=0;
				
				
				for (int i = 0; i < maxFiles; i++) {
					Utilities._filesToView[i] = 0;
					Utilities._filesToAdd[i] = 0;					
				}

				Utilities._weAreAdding=0;			
				int county=0,tableChosen=0;			
				int filesToAdd1 = 0;
				for (int i = 0; i < maxFiles; i++) {
					//write("\ngood so far9-");
					
					
					if ( fileSet[i].isSelected()) {
						
						filesToAdd1++;
						if (filesToAdd1 < 2)
						{
						Utilities._filesToView[i] = 1;
						Utilities._filesToAdd[i] = 1;
						Utilities._weAreAdding=1;
						Utilities._weAreSubtracting=0;
						
						
						county++;
						tableChosen=i;
						Utilities._columnsTotal = Utilities.p3_csvFiles[i].get_ColumnsTotal();
					    Utilities._total_str_cols = Utilities.p3_csvFiles[i].getTotal_str_cols();
					   
					    for (int h1 = 0; h1 < Utilities._columnsTotal; h1++) {
					    	Utilities._cType[h1] = Utilities.p3_csvFiles[i].getType(h1);
					    	Utilities._cSize[h1] = Utilities.p3_csvFiles[i].getcSize(h1);
					    	//write("\ngood so farh8");
							
							}
					 	//write("\ngood so farh48-");
						}
					   			
					    else {
						Utilities._filesToView[i] = 0;
						Utilities._filesToAdd[i] = 0;
						Utilities._weAreAdding=0;
						writeColor("\nYou need to select only one file : " , Color.orange);
						county = -1;
					      }
					//write("\ngood so far7-");
					}
					
				}/// end for
				//write("\ngood so far k" + county);
						
				
				if(county>0)
							{
					String zz2 = Utilities.addFileToSet_add(e, style, doc,tableChosen);
					if (zz2 != null) {
						//totalFiles_.setText("" + ++totalFiles);
						//fileSet[totalFiles-1].setSelected(true);
						writeColor("\nHeap file added successfully to file set: " + zz2, Color.orange);
						
						Utilities._filesInDB_add[tableChosen] = 1;
						
						write("\nFiles in DB before:"); 
					    for (int h1 = 0; h1 < Utilities._maxFiles; h1++) {
					    	write(" " + Utilities._filesInDB_add[h1]);
							}
					    write("\n");		
						
						
						
						
						
						String zz3 = MainDriver.add_NewRecordToTableInDB(e, style, doc,tableChosen);
						if (zz3 == null) 
						{						
							writeColor("\nerror while adding new table to DB", Color.gray);
							}
						else

							writeColor("\nadd_Heap file added successfully to db: " + zz3, Color.orange);
							
							}
					
					write("\nFiles in DB after:"); 
				    for (int h1 = 0; h1 < Utilities._maxFiles; h1++) {
				    	write(" " + Utilities._filesInDB_add[h1]);
						}
				    write("\n");	
					
					
					
					
							}
				else
				{ if (county != -1)
					writeColor("\nYou need to select a file to add to", Color.orange);}
				
				
				
				/////////////////////////
				break;
			case "SUB":
				write("\ngood so far sub called");

				Utilities._weAreAdding=0;
				Utilities._weAreSubtracting=0;
				
				
				for (int i = 0; i < maxFiles; i++) {
					Utilities._filesToView[i] = 0;
					Utilities._filesToSub[i] = 0;					
				}

				Utilities._weAreSubtracting=0;			
				int countys=0,tableChosens=0;			
				int filesToSub1 = 0;
				for (int i = 0; i < maxFiles; i++) {
					//write("\ngood so far9-");
					
					
					if ( fileSet[i].isSelected()) {
						
						filesToSub1++;
						if (filesToSub1 < 2)
						{
						Utilities._filesToView[i] = 1;
						Utilities._filesToSub[i] = 1;
						Utilities._weAreSubtracting=1;
						Utilities._weAreAdding = 0;
						
						
						countys++;
						tableChosens=i;
						Utilities._columnsTotal = Utilities.p3_csvFiles[i].get_ColumnsTotal();
					    Utilities._total_str_cols = Utilities.p3_csvFiles[i].getTotal_str_cols();
					   
					    for (int h1 = 0; h1 < Utilities._columnsTotal; h1++) {
					    	Utilities._cType[h1] = Utilities.p3_csvFiles[i].getType(h1);
					    	Utilities._cSize[h1] = Utilities.p3_csvFiles[i].getcSize(h1);
					    	//write("\ngood so farh8");
							
							}
					 	//write("\ngood so farh48-");
						}
					   			
					    else {
						Utilities._filesToView[i] = 0;
						Utilities._filesToSub[i] = 0;
						Utilities._weAreSubtracting=0;
						writeColor("\nYou need to select only one file : " , Color.orange);
						county = -1;
					      }
					//write("\ngood so far7-");
					}
					
				}/// end for
				//write("\ngood so far k" + county);
						
				
				if(countys>0)
							{
					String zz2 = Utilities.addFileToSet_sub(e, style, doc,tableChosens);
					if (zz2 != null) {
						//totalFiles_.setText("" + ++totalFiles);
						//fileSet[totalFiles-1].setSelected(true);
						writeColor("\nHeap file added successfully to file set: " + zz2, Color.orange);
						
						Utilities._filesInDB_sub[tableChosens] = 1;
						
						write("\nFiles in DB before:"); 
					    for (int h1 = 0; h1 < Utilities._maxFiles; h1++) {
					    	write(" " + Utilities._filesInDB_sub[h1]);
							}
					    write("\n");		
						
				
						
						
						String zz3 = MainDriver.sub_NewRecordToTableInDB(e, style, doc,tableChosens);
						if (zz3 == null) 
						{						
							writeColor("\nerror while adding new table to DB", Color.gray);
							}
						else

							writeColor("\nadd_Heap file added successfully to db: " + zz3, Color.orange);
							
							}
					
					write("\nFiles in DB after:"); 
				    for (int h1 = 0; h1 < Utilities._maxFiles; h1++) {
				    	write(" " + Utilities._filesInDB_sub[h1]);
						}
				    write("\n");	
					
					
					
					
							}
				else
				{ if (countys != -1)
					writeColor("\nYou need to select a file to add to", Color.orange);}
				
				
				
				/////////////////////////
				break;
			case "ADDFILETOSET":
				//write(cSize1[0].getSelectedItem().toString());
				if (totalColumnsx < 2) {
					writeColor("\n- you need to enter totalColumns >= 2, column types and string sizes", Color.red);
				} else
				{
					
					//write(cType1[0].getSelectedIndex() - 1 +  " ");
					try {

						int h1;
						int out = 0;
						int tIndx;
						int sIndx;

						for (h1 = 0; h1 < totalColumnsx; h1++) {
							tIndx = cType1[h1].getSelectedIndex();
							sIndx = cSize1[h1].getSelectedIndex();
							if (tIndx == 0 ) // cannot be empty
								out = 1;
							if ((tIndx == 1) && (sIndx == 0)) // string cannot have 0/blank length 
								out = 2;
							if (tIndx > 3) // cannot be symbol or null
								out = 4;
						}
						
						if (out >0 ){
							writeColor("\n our:" + out + " invalid type selection or String Size; please re-enter", Color.red);
						}
						else
						{	Utilities._columnsTotal = totalColumnsx;
						    Utilities._total_str_cols = 0;
						    
						    for (h1 = 0; h1 < Utilities._maxFiles; h1++) {
						    	Utilities._cType[h1] = 0;
						    	Utilities._cSize[h1] = (short) 0;
								}
						    
							for (h1 = 0; h1 < totalColumnsx; h1++) {
								int ctx = cType1[h1].getSelectedIndex() - 1;
								int stx = cSize1[h1].getSelectedIndex() - 1;
								Utilities._cType[h1] = ctx;
								if (ctx == 0)
									{short strSize = Short.parseShort(cSize1[h1].getSelectedItem().toString());
									Utilities._cSize[h1] = strSize;
									Utilities._total_str_cols ++;
									}
								}
							
							String zz1 = Utilities.addFileToSet(e, style, doc);
							if (zz1 != null) {
								totalFiles_.setText("" + ++totalFiles);
								fileSet[totalFiles-1].setSelected(true);
								writeColor("\nfile added successfully to file set: " + zz1, Color.gray);
								//writeColor("\nHF is allocated, File is not  in db", Color.gray);
							}
						}				
						
						
						
					}

					catch (Exception e4) {
						write(" ww" + e4.toString());
					}
				}
				
				//break;
				

			case "ADDFILETODB":
				if (Utilities._totalFiles < 1) {
					writeColor("\n- you need to add files first", Color.red);
				} else {

					try {
						int countx = 0;
						int out =0;
						for (int i = 0; i < fileSet.length; i++) {
							if ((fileSet[i].isSelected()) && (Utilities._filesInSet[i] == 1)) {
								if (Utilities._filesInDB[i] == 1)
								{ countx++;
									//writeColor("\nFile entry index:" +i + " already in Db", Color.red);
								}
								if (Utilities._filesInDB[i] == 0 && (out == 0))
									{Utilities._filesInDB[i] = 1; // add this file
									countx++;
									}
								if (Utilities._filesInDB[i] == 2 && (out == 0)) 
									writeColor("\nfile index: " + i + " already in DB: no action for " + Utilities.p3_csvFiles[i].getFileName(),Color.red);
							}
						}
						
						write("\nFiles in DB before:"); 
					    for (int h1 = 0; h1 < Utilities._maxFiles; h1++) {
					    	write(" " + Utilities._filesInDB[h1]);
							}
					    write("\n");
						

						if (countx > 0 && (out ==0)) { // files need to be added
							String zz1 = MainDriver.AddTableToDB(e, style, doc);
							if (zz1 != null) {
								write("\n" + zz1);
							}
						} else {
							writeColor("\nyou need to choose file index less than " + Utilities._totalFiles + " to add", Color.red);
						}
						write("\nFiles in DB after:"); 
					    for (int h1 = 0; h1 < Utilities._maxFiles; h1++) {
					    	write(" " + Utilities._filesInDB[h1]);
							}
					    write("\n");
						
						
					}

					catch (Exception e4) {
						write(" " + e4.toString());
					}
				}
				break;
			case "VIEWFILEINDB":
				if (Utilities._totalFiles < 1) {
					writeColor("\n- you need to add files first", Color.red);
				} else {
					try {
						int countx = 0;
						for (int i = 0; i < maxFiles; i++) {
							if (fileSet[i].isSelected() && (Utilities._filesInDB[i] == 2)) {
								Utilities._filesToView[i] = 1;
								countx++;
							} else
								Utilities._filesToView[i] = 0;
						}
						// debug.append("\n*done updating util check fields") ;

						if (countx > 0) {
							String zz1 = MainDriver.ViewFilesInDB(e, style, doc);
							if (zz1 != null)
								{write("\n" + zz1);	}
							} 
						else {
							writeColor("\n you need to choose file index less than " + countx + " to view", Color.red);
							}
					}

					catch (Exception e4) {
						write(" " + e4.toString());
					}
				}
				break;

			case "VIEWFILEINSET":

				if (Utilities._totalFiles < 1) {
					writeColor("\n- you need to add files first", Color.red);
				} else {
					try {
						int countx = 0;
						for (int i = 0; i < maxFiles; i++) {
							if (fileSet[i].isSelected() && (Utilities._filesInSet[i] == 1)) {
								Utilities._filesToView[i] = 1;
								countx++;
							} else
								Utilities._filesToView[i] = 0;
						}
						// debug.append("\n*done updating util check fields") ;

						if (countx > 0) {
							String zz1 = Utilities.ViewFiles(e, style, doc);
							if (zz1 != null)
								{write("\n" + zz1);	}
							} 
						else {
							writeColor("\n you need to choose file index less than " + Utilities._totalFiles + " to view", Color.red);
							}
					}

					catch (Exception e4) {
						write(" " + e4.toString());
					}
				}
				break;
			case "PRINTFILEINFO":

				if (Utilities._totalFiles < 1) {
					writeColor("\n- you need to add files first", Color.red);
				} else {
					try {						
						int countx = 0;
						for (int i = 0; i < maxFiles; i++) {
							if (fileSet[i].isSelected() && (Utilities._filesInSet[i] == 1)) {
								Utilities._filesToView[i] = 1;
								countx++;
							} else
								Utilities._filesToView[i] = 0;
						}
					
						if (countx > 0)
						{	write("\n");
							String zz1 = Utilities.PrintFilesInfo(e, style, doc);
							if (zz1 != null)
								{write(zz1);}
						} 
						else {
							writeColor("\n you need to choose file index/s less than " + Utilities._totalFiles + " to view info", Color.red);
							}	
							
					}

					catch (Exception e4) {
						write(" " + e4.toString());
					}
				}
				break;
			case "CLEAR":
				int h1;
				//totalColumnsx.setText("");
				totCols3.setSelected(true);
				
				
				topK_int = 5;
				
				

				for (h1 = 0; h1 < maxCType; h1++) {
					cType1[h1].setSelectedIndex(0);
				}

				for (h1 = 0; h1 < maxCSize; h1++) {
					cSize1[h1].setSelectedIndex(0);
				}

				for (h1 = 0; h1 < maxCType; h1++) {
					match1[h1].setSelectedIndex(0);
					match2[h1].setSelectedIndex(0);
					match3[h1].setSelectedIndex(0);
				}

				
				write("\nCleared/Reset from GUI only: cType, CSize, topK, total Columns, match columns");
				break;
			
			case "TOTALCOLUMNSXx":
				//totalColumnsx = x;
				break;
			case "TOPK":
				//topK_int =topK_combo.getSelectedIndex() + 1;
				break;
				
			case "VIEWSELECTEDIMAGE":
				if (totalFiles > 0) {
				} else {
				}
				break;
			case "CLOSE":
				System.exit(0);
				break;
			default:
			}
		}
	}// end go == 1
}