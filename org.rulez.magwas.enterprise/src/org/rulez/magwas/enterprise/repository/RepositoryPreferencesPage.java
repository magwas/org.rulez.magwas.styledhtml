package org.rulez.magwas.enterprise.repository;



import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.rulez.magwas.enterprise.EnterprisePlugin;
import org.rulez.magwas.styledhtml.IPreferenceConstants;

public class RepositoryPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, IPreferenceConstants {
		
		private static RepositoryPreferencesPage self = null;
		
	    public static String HELPID = "org.rulez.magwas.enterprise.RepositoryPreferencesPage"; //$NON-NLS-1$
	    
		private Table table;
		private TableViewer tableViewer;
		private final String NAME_COLUMN = "Name"; 

		private final String LOCATION_COLUMN = "URL"; 
		
		private final String USER_COLUMN = "Username";

	    static String CONNECTIONS_PREF = "org.rulez.magwas.enterprise.connections";
		
		// Set column names
		private String[] columnNames = new String[] {NAME_COLUMN,
				LOCATION_COLUMN, USER_COLUMN};

		private ConnContentProvider cp;
	    
		public RepositoryPreferencesPage() {
			setPreferenceStore(EnterprisePlugin.INSTANCE.getPreferenceStore());
			
			self = this;
		}
		
		public static RepositoryPreferencesPage getTools() {
			if(null == self) {
				self = new RepositoryPreferencesPage();
			}
			return self;
		}
		/**
		* Create the Table
		*/
		private void createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
			| SWT.FULL_SELECTION;
		
		table = new Table(parent, style);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		gridData.heightHint = table.getItemHeight();
		gridData.horizontalSpan = 1;
		table.setLayoutData(gridData);
		
			TableLayout tableLayout = new TableLayout();
			table.setLayout(tableLayout);
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			table.setFont(parent.getFont());
			
			ColumnLayoutData[] fTableColumnLayouts= {
			new ColumnWeightData(85),
			new ColumnWeightData(165),
			new ColumnWeightData(60)
			};
			
			TableColumn column;
			
			tableLayout.addColumnData(fTableColumnLayouts[0]);
			column = new TableColumn(table, SWT.NONE, 0);
			column.setResizable(fTableColumnLayouts[0].resizable);
			column.setText(NAME_COLUMN);
			tableLayout.addColumnData(fTableColumnLayouts[1]);
			column = new TableColumn(table, SWT.NONE, 1);
			column.setResizable(fTableColumnLayouts[1].resizable);
			column.setText(LOCATION_COLUMN);
			
			tableLayout.addColumnData(fTableColumnLayouts[2]);
			column = new TableColumn(table, SWT.NONE, 2);
			column.setResizable(fTableColumnLayouts[2].resizable);
			column.setText(USER_COLUMN);
			}
		/**
		 * Creates the group which will contain the buttons.
		 */
		private void createButtonGroup(Composite top) {
		Composite buttonGroup = new Composite(top, SWT.NONE);
		 	GridLayout layout = new GridLayout();
		 	layout.marginHeight = 0;
		 	layout.marginWidth = 0;
		 	buttonGroup.setLayout(layout);
		 	buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		 	buttonGroup.setFont(top.getFont());
		 	
		 	addButtonsToButtonGroup(buttonGroup);
		 }
		
		private void addButtonsToButtonGroup(Composite parent) {
			 	
			 	Button addNewICButton = new Button(parent,SWT.PUSH);
			 	addNewICButton.setText("add");
			 	addNewICButton.addSelectionListener(new SelectionAdapter() {
		            @Override
		            public void widgetSelected(SelectionEvent e) {
		            	editConnection(null);
		            }
		        });
			 	
			 	Button editICButton = new Button(parent,SWT.PUSH);
			 	editICButton.setText("edit");
			 	editICButton.addSelectionListener(new SelectionAdapter() {
		            @Override
		            public void widgetSelected(SelectionEvent e) {
		        		ISelection sel = tableViewer.getSelection();
		        		assert(sel instanceof IStructuredSelection);
		        		@SuppressWarnings("unchecked")
		        		Iterator<ConnPref> iter = ((IStructuredSelection)sel).iterator();
		        		if(iter.hasNext()){
		        			editConnection(iter.next());
		        		}
		            	
		            }
		        });

			 	Button removeICButton = new Button(parent,SWT.PUSH);
			 	removeICButton.setText("remove");
			 	removeICButton.addSelectionListener(new SelectionAdapter() {
		            @Override
		            public void widgetSelected(SelectionEvent e) {
		            	cp.removeSelected();
		            }
		        });
			 	
		}
	    @Override
	    protected Control createContents(Composite parent) {
	        // Help
	        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HELPID);

	        Composite client = new Composite(parent, SWT.NULL);
	        client.setLayout(new GridLayout());
	        
	        
	        Group connsGroup = new Group(client, SWT.NULL);
	        connsGroup.setText("Connections");
	        connsGroup.setLayout(new GridLayout(2, false));
	        connsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        Composite top = new Composite(connsGroup, SWT.NONE);
	        GridLayout layout = new GridLayout();
	        layout.numColumns = 2;
	        layout.marginHeight = 2;
	        layout.marginWidth = 2;
	        top.setLayout(layout);
	        top.setLayoutData(new GridData(GridData.FILL_BOTH));
	        createTable(top);
	        createTableViewer();
	        cp = new ConnContentProvider();
			tableViewer.setContentProvider(cp);
			tableViewer.setLabelProvider(new ConnLabelProvider());

			tableViewer.setInput(ConnContentProvider.ConnPrefList);
			createButtonGroup(top);
	        return client;
	    }

	    /**
		 * Create the TableViewer
		 */
		private void createTableViewer() {

			tableViewer = new TableViewer(table);
			tableViewer.setUseHashlookup(true);
			tableViewer.setColumnProperties(columnNames);

		}

	    private void setValues() {
	    	System.out.println("setValues");

	    }
	    
	    
/*	    @Override
	    public boolean performOk() {
	    	if (null != styleDir ) {
	    		getPreferenceStore().setValue(STYLE_PATH, styleDir.getAbsolutePath());
	    	}
	        getPreferenceStore().setValue(OUT_ASK, fOutDirCheckBox.getSelection());
	        getPreferenceStore().setValue(OUT_PATH, outDir.getAbsolutePath());
	        return true;
	    }
	    
	    @Override
	    protected void performDefaults() {
	    	String path = getPreferenceStore().getDefaultString(STYLE_PATH);
	    	styleDir = new File(path);
	    	fStyleDirPathLabel.setText(path);
	    	path = getPreferenceStore().getDefaultString(OUT_PATH);
	    	outDir = new File(path);
	    	fOutDirPathLabel.setText(path);
	    	fOutDirCheckBox.setSelection(getPreferenceStore().getDefaultBoolean(OUT_ASK));
	        super.performDefaults();
	    }
*/
	    public void init(IWorkbench workbench) {
	    }

	        
	    public void editConnection(ConnPref con) {
			Shell myshell = Display.getCurrent().getActiveShell();
	    	if (con == null) {
	    		con = new ConnPref(cp);
	    	}
	    	Dialog dialog = new ConnPrefDialog(myshell,con);
	    	dialog.open();
	    }
	    
}
