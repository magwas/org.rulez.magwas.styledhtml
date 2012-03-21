package org.rulez.magwas.styledtextui;

/*

The following sources were used:
http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTStyledText.htm

SWT/JFace in Action
GUI Design with Eclipse 3.0
Matthew Scarpino, Stephen Holder, Stanford Ng, and Laurent Mihalkovic

ISBN: 1932394273

Publisher: Manning

http://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTTextEditorDemo.htm


/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************

*/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 */
public class StyledTextUi {
  Shell shell;

  ToolBar toolBar;

  StyledText text;

  Images images = new Images();

  Vector<StyleRange> cachedStyles = new Vector<StyleRange>();

  Color RED = null;

  Color BLUE = null;

  Color GREEN = null;

  Font font = null;

  ToolItem boldButton, italicButton, underlineButton, strikeoutButton;
  private static final String END_STYLES_MARK = "***EndStyles***";

  private static final String START_STYLES_MARK = "***Styles***";

  private static final String START_TEXT_MARK = "***Text***";

  private static final String FILE_NAME = "editorData.txt";

  Menu createEditMenu() {
    Menu bar = shell.getMenuBar();
    Menu menu = new Menu(bar);

    MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText("Cut_menuitem");
    item.setAccelerator(SWT.MOD1 + 'X');
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        handleCutCopy();
        text.cut();
      }
    });
    item = new MenuItem(menu, SWT.PUSH);
    item.setText("Copy_menuitem");
    item.setAccelerator(SWT.MOD1 + 'C');
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        handleCutCopy();
        text.copy();
      }
    });
    item = new MenuItem(menu, SWT.PUSH);
    item.setText("Paste_menuitem");
    item.setAccelerator(SWT.MOD1 + 'V');
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        text.paste();
      }
    });
    new MenuItem(menu, SWT.SEPARATOR);
    item = new MenuItem(menu, SWT.PUSH);
    item.setText("Font_menuitem");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setFont();
      }
    });
    return menu;
  }

  Menu createFileMenu() {
    Menu bar = shell.getMenuBar();
    Menu menu = new Menu(bar);

    MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText("Exit_menuitem");
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        shell.close();
      }
    });
    MenuItem item2 = new MenuItem(menu, SWT.PUSH);
    item2.setText("save");
    item2.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        save();
      }
    });
    MenuItem item3 = new MenuItem(menu, SWT.PUSH);
    item3.setText("load");
    item3.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        load();
      }
    });

    return menu;
  }

  /*
   * Set a style
   */
  void setStyle(Widget widget) {
    Point sel = text.getSelectionRange();
    if ((sel == null) || (sel.y == 0))
      return;
    StyleRange style;
    for (int i = sel.x; i < sel.x + sel.y; i++) {
      StyleRange range = text.getStyleRangeAtOffset(i);
      if (range != null) {
        style = (StyleRange) range.clone();
        style.start = i;
        style.length = 1;
      } else {
        style = new StyleRange(i, 1, null, null, SWT.NORMAL);
      }
      if (widget == boldButton) {
        style.fontStyle ^= SWT.BOLD;
      } else if (widget == italicButton) {
        style.fontStyle ^= SWT.ITALIC;
      } else if (widget == underlineButton) {
        style.underline = !style.underline;
      } else if (widget == strikeoutButton) {
        style.strikeout = !style.strikeout;
      }
      text.setStyleRange(style);
    }
    text.setSelectionRange(sel.x + sel.y, 0);
  }

  /*
   * Clear all style data for the selected text.
   */
  void clear() {
    Point sel = text.getSelectionRange();
    if ((sel != null) && (sel.y != 0)) {
      StyleRange style;
      style = new StyleRange(sel.x, sel.y, null, null, SWT.NORMAL);
      text.setStyleRange(style);
    }
    text.setSelectionRange(sel.x + sel.y, 0);
  }

  /*
   * Set the foreground color for the selected text.
   */
  void fgColor(Color fg) {
    Point sel = text.getSelectionRange();
    if ((sel == null) || (sel.y == 0))
      return;
    StyleRange style, range;
    for (int i = sel.x; i < sel.x + sel.y; i++) {
      range = text.getStyleRangeAtOffset(i);
      if (range != null) {
        style = (StyleRange) range.clone();
        style.start = i;
        style.length = 1;
        style.foreground = fg;
      } else {
        style = new StyleRange(i, 1, fg, null, SWT.NORMAL);
      }
      text.setStyleRange(style);
    }
    text.setSelectionRange(sel.x + sel.y, 0);
  }

  void createMenuBar() {
    Menu bar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(bar);

    MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
    fileItem.setText("File_menuitem");
    fileItem.setMenu(createFileMenu());

    MenuItem editItem = new MenuItem(bar, SWT.CASCADE);
    editItem.setText("edit");
    editItem.setMenu(createEditMenu());
  }

  void createShell(Display display) {
    shell = new Shell(display);
    shell.setText("Window");
    images.loadAll(display);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    shell.setLayout(layout);
    shell.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (font != null)
          font.dispose();
        //images.freeAll();
        RED.dispose();
        GREEN.dispose();
        BLUE.dispose();
      }
    });
  }

  
  void save() {
	    try {
	      PrintWriter writer = new PrintWriter(new BufferedWriter(
	          new FileWriter(FILE_NAME)));
	      String thetext = text.getText();
	      writer.println(START_TEXT_MARK + " " + thetext.length());
	      writer.println(thetext);
	      writer.println(START_STYLES_MARK);
	      StyleRange[] styles = text.getStyleRanges();
	      for (int i = 0; i < styles.length; i++) {
	        writer.println(styles[i].start + " " + styles[i].length);
	      }
	      writer.println(END_STYLES_MARK);
	      writer.flush();
	      writer.close();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  }
  
  private void load() {
	    File file = new File(FILE_NAME);
	    if (file.exists()) {
	      try {
	        BufferedReader reader = new BufferedReader(new FileReader(file));
	        String currLine = reader.readLine();

	        StringTokenizer tokenizer = new StringTokenizer(currLine);
	        tokenizer.nextToken(); //discard START_TEXT_MARKER
	        String contentLengthString = tokenizer.nextToken();
	        int contentLength = Integer.parseInt(contentLengthString);

	        readContent(reader, contentLength);

	        //find the beginning of the styles section
	        while (((currLine = reader.readLine()) != null)
	            && !START_STYLES_MARK.equals(currLine))
	          ;

	        readStyles(reader, currLine);
	        reader.close();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	  }
  

  private void readContent(BufferedReader reader, int contentLength)
      throws IOException {
    char[] buffer = new char[contentLength];
    reader.read(buffer, 0, contentLength);

    text.append(new String(buffer));
  }

  private void readStyles(BufferedReader reader, String currLine)
      throws IOException {
    while (!END_STYLES_MARK.equals(currLine)) {
      currLine = reader.readLine();
      if (!END_STYLES_MARK.equals(currLine))
        buildOneStyle(currLine);
    }
  }
  

  private void buildOneStyle(String styleText) {
    StringTokenizer tokenizer = new StringTokenizer(styleText);
    int startPos = Integer.parseInt(tokenizer.nextToken());
    int length = Integer.parseInt(tokenizer.nextToken());

    StyleRange style = new StyleRange(startPos, length, null, null,
        SWT.BOLD);
    text.setStyleRange(style);
  }
  
  void createStyledText() {
    initializeColors();
    text = new StyledText(shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
        | SWT.H_SCROLL);
    GridData spec = new GridData();
    spec.horizontalAlignment = GridData.FILL;
    spec.grabExcessHorizontalSpace = true;
    spec.verticalAlignment = GridData.FILL;
    spec.grabExcessVerticalSpace = true;
    text.setLayoutData(spec);
    text.addExtendedModifyListener(new ExtendedModifyListener() {
      public void modifyText(ExtendedModifyEvent e) {
        handleExtendedModify(e);
      }
    });
  }

  void createToolBar() {
    toolBar = new ToolBar(shell, SWT.NULL);
    SelectionAdapter listener = new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        setStyle(event.widget);
      }
    };
    boldButton = new ToolItem(toolBar, SWT.CHECK);
    boldButton.setImage(images.Bold);
    boldButton.setToolTipText("Bold");
    boldButton.addSelectionListener(listener);
    italicButton = new ToolItem(toolBar, SWT.CHECK);
    italicButton.setImage(images.Italic);
    italicButton.setToolTipText("Italic");
    italicButton.addSelectionListener(listener);
    underlineButton = new ToolItem(toolBar, SWT.CHECK);
    underlineButton.setImage(images.Underline);
    underlineButton.setToolTipText("Underline");
    underlineButton.addSelectionListener(listener);
    strikeoutButton = new ToolItem(toolBar, SWT.CHECK);
    strikeoutButton.setImage(images.Strikeout);
    strikeoutButton.setToolTipText("Strikeout");
    strikeoutButton.addSelectionListener(listener);

    ToolItem item = new ToolItem(toolBar, SWT.SEPARATOR);
    item = new ToolItem(toolBar, SWT.PUSH);
    item.setImage(images.Red);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        fgColor(RED);
      }
    });
    item = new ToolItem(toolBar, SWT.PUSH);
    item.setImage(images.Green);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        fgColor(GREEN);
      }
    });
    item = new ToolItem(toolBar, SWT.PUSH);
    item.setImage(images.Blue);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        fgColor(BLUE);
      }
    });
    item = new ToolItem(toolBar, SWT.SEPARATOR);
    item = new ToolItem(toolBar, SWT.PUSH);
    item.setImage(images.Erase);
    item.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        clear();
      }
    });
  }

  /*
   * Cache the style information for text that has been cut or copied.
   */
  void handleCutCopy() {
    // Save the cut/copied style info so that during paste we will maintain
    // the style information. Cut/copied text is put in the clipboard in
    // RTF format, but is not pasted in RTF format. The other way to
    // handle the pasting of styles would be to access the Clipboard
    // directly and
    // parse the RTF text.
    cachedStyles = new Vector<StyleRange>();
    Point sel = text.getSelectionRange();
    int startX = sel.x;
    for (int i = sel.x; i <= sel.x + sel.y - 1; i++) {
      StyleRange style = text.getStyleRangeAtOffset(i);
      if (style != null) {
        style.start = style.start - startX;
        if (!cachedStyles.isEmpty()) {
          StyleRange lastStyle = (StyleRange) cachedStyles
              .lastElement();
          if (lastStyle.similarTo(style)
              && lastStyle.start + lastStyle.length == style.start) {
            lastStyle.length++;
          } else {
            cachedStyles.addElement(style);
          }
        } else {
          cachedStyles.addElement(style);
        }
      }
    }
  }

  void handleExtendedModify(ExtendedModifyEvent event) {
    if (event.length == 0)
      return;
    StyleRange style;
    if (event.length == 1
        || text.getTextRange(event.start, event.length).equals(
            text.getLineDelimiter())) {
      // Have the new text take on the style of the text to its right
      // (during
      // typing) if no style information is active.
      int caretOffset = text.getCaretOffset();
      style = null;
      if (caretOffset < text.getCharCount())
        style = text.getStyleRangeAtOffset(caretOffset);
      if (style != null) {
        style = (StyleRange) style.clone();
        style.start = event.start;
        style.length = event.length;
      } else {
        style = new StyleRange(event.start, event.length, null, null,
            SWT.NORMAL);
      }
      if (boldButton.getSelection())
        style.fontStyle |= SWT.BOLD;
      if (italicButton.getSelection())
        style.fontStyle |= SWT.ITALIC;
      style.underline = underlineButton.getSelection();
      style.strikeout = strikeoutButton.getSelection();
      if (!style.isUnstyled())
        text.setStyleRange(style);
    } else {
      // paste occurring, have text take on the styles it had when it was
      // cut/copied
      for (int i = 0; i < cachedStyles.size(); i++) {
        style = (StyleRange) cachedStyles.elementAt(i);
        StyleRange newStyle = (StyleRange) style.clone();
        newStyle.start = style.start + event.start;
        text.setStyleRange(newStyle);
      }
    }
  }

  public static void main(String[] args) {
    Display display = new Display();
    StyledTextUi example = new StyledTextUi();
    Shell shell = example.open(display);
    while (!shell.isDisposed())
      if (!display.readAndDispatch())
        display.sleep();
    display.dispose();
  }

  public Shell open(Display display) {
    createShell(display);
    createMenuBar();
    createToolBar();
    createStyledText();
    shell.setSize(500, 300);
    shell.open();
    return shell;
  }

  void setFont() {
    FontDialog fontDialog = new FontDialog(shell);
    fontDialog.setFontList((text.getFont()).getFontData());
    FontData fontData = fontDialog.open();
    if (fontData != null) {
      if (font != null) {
        font.dispose();
      }
      font = new Font(shell.getDisplay(), fontData);
      text.setFont(font);
    }
  }

  void initializeColors() {
    Display display = Display.getDefault();
    RED = new Color(display, new RGB(255, 0, 0));
    BLUE = new Color(display, new RGB(0, 0, 255));
    GREEN = new Color(display, new RGB(0, 255, 0));
  }
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

class Images {

  // Bitmap Images
  public Image Bold;

  public Image Italic;

  public Image Underline;

  public Image Strikeout;

  public Image Red;

  public Image Green;

  public Image Blue;

  public Image Erase;

  Image[] AllBitmaps;

  Images() {
  }

  public void freeAll() {
    for (int i = 0; i < AllBitmaps.length; i++)
      AllBitmaps[i].dispose();
    AllBitmaps = null;
  }

  Image createBitmapImage(Display display, String fileName) {
    InputStream sourceStream = Images.class.getResourceAsStream(fileName
        + ".bmp");
    InputStream maskStream = Images.class.getResourceAsStream(fileName
        + "_mask.bmp");
    ImageData source = new ImageData(sourceStream);
    ImageData mask = new ImageData(maskStream);
    Image result = new Image(display, source, mask);
    try {
      sourceStream.close();
      maskStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  public void loadAll(Display display) {
    // Bitmap Images
/*    Bold = createBitmapImage(display, "bold");
    Italic = createBitmapImage(display, "italic");
    Underline = createBitmapImage(display, "underline");
    Strikeout = createBitmapImage(display, "strikeout");
    Red = createBitmapImage(display, "red");
    Green = createBitmapImage(display, "green");
    Blue = createBitmapImage(display, "blue");
    Erase = createBitmapImage(display, "erase");

    AllBitmaps = new Image[] { Bold, Italic, Underline, Strikeout, Red,
        Green, Blue, Erase, };
        */
  }
  
}