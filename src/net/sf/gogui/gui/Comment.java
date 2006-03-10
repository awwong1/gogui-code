//----------------------------------------------------------------------------
// $Id$
// $Source$
//----------------------------------------------------------------------------

package net.sf.gogui.gui;

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import net.sf.gogui.game.Node;

//----------------------------------------------------------------------------

/** Scroll pane for displaying a comment to the current game node. */
public class Comment
    extends JScrollPane
    implements DocumentListener
{
    /** Callback for events generated by Comment. */
    public interface Listener
    {
        void changed();

        /** Callback if some text is selected. */
        void textSelected(String text);
    }

    public Comment(Listener listener, boolean fastPaint)
    {
        m_listener = listener;
        m_textPane = new GuiTextPane(fastPaint);
        setFocusTraversalKeys(m_textPane);
        m_textPane.addStyle("marked", Color.white, Color.decode("#38d878"), 
                            false);
        int fontSize = GuiUtils.getDefaultMonoFontSize();
        setPreferredSize(new Dimension(20 * fontSize, 10 * fontSize));
        m_textPane.getDocument().addDocumentListener(this);
        CaretListener caretListener = new CaretListener()
            {
                public void caretUpdate(CaretEvent event)
                {
                    if (m_listener == null)
                        return;
                    m_listener.textSelected(m_textPane.getSelectedText());
                }
            };
        m_textPane.addCaretListener(caretListener);
        setViewportView(m_textPane);
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public void changedUpdate(DocumentEvent e) 
    {
        copyContentToNode();
    }

    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    public String getSelectedText()
    {
        return m_textPane.getSelectedText();
    }

    public void insertUpdate(DocumentEvent e)
    {
        copyContentToNode();
    }

    public void markAll(Pattern pattern)
    {        
        Document doc = m_textPane.getDocument();
        try
        {
            CharSequence text = doc.getText(0, doc.getLength());
            Matcher matcher = pattern.matcher(text);
            boolean firstMatch = true;
            while (matcher.find())
            {
                int start = matcher.start();
                int end = matcher.end();
                Style style = m_textPane.getStyle("marked");
                if (firstMatch)
                {
                    m_textPane.setStyle(0, doc.getLength(), null);
                    m_textPane.setCaretPosition(start);
                    firstMatch = false;
                }
                m_textPane.setStyle(start, end - start, "marked");
            }
        }
        catch (BadLocationException e)
        {
            assert(false);
        }
    }

    public void removeUpdate(DocumentEvent e)
    {
        copyContentToNode();
    }

    /** Enable/disable fixed size font. */
    public void setFontFixed(boolean fixed)
    {
        if (fixed)
            GuiUtils.setMonospacedFont(m_textPane);
        else
            m_textPane.setFont(UIManager.getFont("TextArea.font"));
        m_textPane.repaint();
    }

    public void setNode(Node node)
    {
        m_node = node;
        String text = node.getComment();
        if (text == null)
            text = "";
        // setText() generates a remove and insert event, and
        // we don't want to notify the listener about that yet.
        m_duringSetText = true;
        m_textPane.setText(text);
        m_textPane.setCaretPosition(0);
        m_duringSetText = false;
        copyContentToNode();
    }

    private boolean m_duringSetText;

    /** Serial version to suppress compiler warning.
        Contains a marker comment for serialver.sourceforge.net
    */
    private static final long serialVersionUID = 0L; // SUID

    private final GuiTextPane m_textPane;

    private final Listener m_listener;

    private Node m_node;

    private void copyContentToNode()
    {
        if (m_duringSetText)
            return;
        String text = m_textPane.getText().trim();
        if (m_node == null)
            return;
        String comment = m_node.getComment();
        if (comment == null)
            comment = "";
        else
            comment = comment.trim();
        if (! comment.equals(text))
        {
            m_node.setComment(text);
            m_listener.changed();
        }
    }

    private static void setFocusTraversalKeys(GuiTextPane textPane)
    {
        int id = KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS;
        Set keystrokes = new TreeSet();
        keystrokes.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        textPane.setFocusTraversalKeys(id, keystrokes);
    }
}

//----------------------------------------------------------------------------
