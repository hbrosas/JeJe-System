import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class Main implements LineStyleListener, ModifyListener, Listener{
	protected Shell shell;
	private StyledText workspace, console;
	private Table errorTable;
	public ArrayList<JejeError> errors = new ArrayList<>();

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	@Override
	public void modifyText(ModifyEvent e) {
		// TODO Auto-generated method stub
		int maxLine = workspace.getLineCount();
        int lineCountWidth = Math.max(String.valueOf(maxLine).length(), 3);

        StyleRange style = new StyleRange();
        style.metrics = new GlyphMetrics(0, 0, lineCountWidth * 8 + 10);
        Bullet bullet = new Bullet(ST.BULLET_NUMBER, style);
        workspace.setLineBullet(0, workspace.getLineCount(), null);
        workspace.setLineBullet(0, workspace.getLineCount(), bullet);
		
	}

	@Override
	public void lineGetStyle(LineStyleEvent event) {
		// TODO Auto-generated method stub
		//Set the line number
        event.bulletIndex = workspace.getLineAtOffset(event.lineOffset);

        //Set the style, 12 pixles wide for each digit
//        Color bg = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
        Color bg = null;
        Color fg = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        StyleRange style = new StyleRange(0, 30, fg, bg, SWT.ITALIC);
        style.metrics = new GlyphMetrics(0, 0, 25);

        //Create and set the bullet
        event.bullet = new Bullet(ST.BULLET_NUMBER,style);
	}
	
	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		refreshCompile();
		String code = workspace.getText();
		ANTLRInputStream inputStream = new ANTLRInputStream(code);
		JejeLexer lexer = new JejeLexer(inputStream);
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JejeParser parser = new JejeParser(tokens);
		
		JejeErrorListener jejeErrorListener = new JejeErrorListener();
		parser.addErrorListener(jejeErrorListener);
		ParseTree tree = parser.compilationUnit();
        System.out.println(tree.toStringTree(parser));
        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
        JejeListener jejeListener = new JejeBaseListener();
        parseTreeWalker.walk(jejeListener, tree);
        
        errors = jejeErrorListener.getErrors();
        if(errors.size() > 0) {
        	console.append("Compilation Done.\n");
        	console.append("Error/s found: " + errors.size());
        	
        	for (int i = 0; i < errors.size(); i++) {
        		JejeError e = errors.get(i);
        		TableItem item = new TableItem(errorTable, SWT.Selection);
        		item.setText(0, e.getLine() + "");
        		item.setText(1, e.getErrorMsg());
        		workspace.setLineBackground(e.getLine()-1, 1, Display.getCurrent().getSystemColor(SWT.COLOR_RED));
    	    }

        } else {
        	console.append("Compilation Done.\n");
        	console.append("No Error/s found");
        }
        
        JFrame frame = new JFrame("Parse Tree");
        JPanel panel = new JPanel();
        TreeViewer viewr = new TreeViewer(Arrays.asList(
                parser.getRuleNames()),tree);
        viewr.setScale(1.5);//scale a little
        panel.add(viewr);
        JScrollPane jsp = new JScrollPane(panel);
        frame.add(jsp);
//        frame.add(panel);
        frame.setSize(500,500);
        frame.setVisible(true);
	}
	
	public void refreshCompile() {
		// Console
		console.setText("");
		
		// Error Table
		errorTable.removeAll();
		
		// Highlights
		int lineCount = workspace.getLineCount();
		workspace.setLineBackground(0, lineCount, null);
	}

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		shell.setSize(701, 518);
		shell.setText("JeJe | Interpreter System");
		shell.setLayout(null);
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		composite.setBounds(0, 0, 685, 35);
		
		Button btnNew = new Button(composite, SWT.FLAT);
		btnNew.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		btnNew.setBounds(77, 0, 75, 35);
		btnNew.setText("New File");
		
		Button btnOpen = new Button(composite, SWT.NONE);
		btnOpen.setBounds(154, 0, 75, 35);
		btnOpen.setText("Open File");
		
		Button btnRun = new Button(composite, SWT.PUSH);
		btnRun.addListener(SWT.Selection, this);
		btnRun.setBounds(232, 0, 75, 35);
		btnRun.setText("Run");
		
		Button btnCompile = new Button(composite, SWT.NONE);
		btnCompile.setBounds(310, 0, 75, 35);
		btnCompile.setText("Compile");
		
		Label lblJeje = new Label(composite, SWT.NONE);
		lblJeje.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BORDER));
		lblJeje.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		lblJeje.setAlignment(SWT.CENTER);
		lblJeje.setFont(SWTResourceManager.getFont("Hans Kendrick", 14, SWT.NORMAL));
		lblJeje.setBounds(10, 5, 61, 35);
		lblJeje.setText("JeJe");
		
		Composite composite_2 = new Composite(shell, SWT.NONE);
		composite_2.setBounds(160, 41, 515, 296);
		
		TabFolder tabFolder = new TabFolder(composite_2, SWT.NONE);
		tabFolder.setBounds(0, 0, 515, 296);
		
		TabItem tbtmUntitlejj = new TabItem(tabFolder, SWT.NONE);
		tbtmUntitlejj.setText("untitled.j3j3");
		
		workspace = new StyledText(tabFolder, SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		tbtmUntitlejj.setControl(workspace);
		
		workspace.addLineStyleListener(this);
		workspace.addModifyListener(this);
		
		Composite composite_3 = new Composite(shell, SWT.NONE);
		composite_3.setBounds(160, 343, 515, 126);
		
		TabFolder tabFolder_1 = new TabFolder(composite_3, SWT.NONE);
		tabFolder_1.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		tabFolder_1.setBounds(0, 0, 515, 126);
		
		TabItem consoleTab = new TabItem(tabFolder_1, SWT.NONE);
		consoleTab.setText("Console/Log");
		
		ScrolledComposite scrolledComposite_2 = new ScrolledComposite(tabFolder_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		consoleTab.setControl(scrolledComposite_2);
		scrolledComposite_2.setExpandHorizontal(true);
		scrolledComposite_2.setExpandVertical(true);
		
		console = new StyledText(scrolledComposite_2, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.WRAP);
		console.setEditable(false);
		scrolledComposite_2.setContent(console);
		scrolledComposite_2.setMinSize(console.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		TabItem errorTab = new TabItem(tabFolder_1, SWT.NONE);
		errorTab.setText("Compilation");
		
		ScrolledComposite scrolledComposite_1 = new ScrolledComposite(tabFolder_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		errorTab.setControl(scrolledComposite_1);
		scrolledComposite_1.setExpandHorizontal(true);
		scrolledComposite_1.setExpandVertical(true);

		errorTable = new Table(scrolledComposite_1, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		errorTable.setHeaderVisible(true);
		errorTable.setLinesVisible(true);
		scrolledComposite_1.setContent(errorTable);
		scrolledComposite_1.setMinSize(errorTable.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		String[] titles = {"Line", "Description"};
	    for (int i = 0; i < titles.length; i++) {
	      TableColumn column = new TableColumn(errorTable, SWT.RIGHT);
	      column.setText(titles[i]);
	      column.setAlignment(SWT.LEFT);
	    }
	    
	    errorTable.addListener(SWT.Selection, new Listener() {
	        public void handleEvent(Event e) {
	          String string = "";
	          TableItem[] selection = errorTable.getSelection();
	          for (int i = 0; i < selection.length; i++) {
	        	  int line = Integer.parseInt(selection[i].getText(0));
	        	  setCursorPosition(line, 0);
	          }
	        }
	      });
	    
	    for (int i=0; i<titles.length; i++) {
	    	errorTable.getColumn(i).pack ();
	    }
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setBounds(10, 41, 144, 428);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(composite_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(0, 0, 144, 428);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		List listFile = new List(scrolledComposite, SWT.BORDER);
		scrolledComposite.setContent(listFile);
		scrolledComposite.setMinSize(listFile.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}
	
	public void setCursorPosition(int lineNo, int charNo) {
	    if (lineNo < 1) {
	        return;
	    }
	    if (charNo < 1) {
	        charNo = 1;
	    }
	    
        int lineOffset = workspace.getOffsetAtLine(lineNo - 1);
        workspace.setCaretOffset(lineOffset + charNo - 1);
        workspace.setFocus();
        workspace.showSelection();
	}
}
