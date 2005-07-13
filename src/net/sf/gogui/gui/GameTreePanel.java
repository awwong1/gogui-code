//----------------------------------------------------------------------------
// $Id$
// $Source$
//----------------------------------------------------------------------------

package net.sf.gogui.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import net.sf.gogui.game.GameTree;
import net.sf.gogui.game.Node;
import net.sf.gogui.game.NodeUtils;

//----------------------------------------------------------------------------

public class GameTreePanel
    extends JPanel
    implements Scrollable
{
    public static final int LABEL_NUMBER = 0;

    public static final int LABEL_MOVE = 1;

    public static final int LABEL_NONE = 2;

    public static final int SIZE_LARGE = 0;

    public static final int SIZE_NORMAL = 1;

    public static final int SIZE_SMALL = 2;

    public static final int SIZE_TINY = 3;

    public static final Color m_background = new Color(192, 192, 192);

    public GameTreePanel(JDialog owner, GameTreeViewer.Listener listener,
                         boolean fastPaint, int labelMode, int sizeMode)
    {
        super(new SpringLayout());
        m_owner = owner;
        m_fastPaint = fastPaint;
        setBackground(m_background);
        m_labelMode = labelMode;
        m_sizeMode = sizeMode;
        computeSizes(sizeMode);
        setFocusable(false);
        setFocusTraversalKeysEnabled(false);
        setOpaque(true);
        setAutoscrolls(true);
        MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter()
            {
                public void mouseDragged(MouseEvent e)
                {
                    Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
                    ((JPanel)e.getSource()).scrollRectToVisible(r);
                }
            };
        addMouseMotionListener(doScrollRectToVisible);
        m_listener = listener;
        m_mouseListener = new MouseAdapter()
            {
                public void mouseClicked(MouseEvent event)
                {
                    if (event.getButton() != MouseEvent.BUTTON1)
                        return;
                    GameTreeNode gameNode = (GameTreeNode)event.getSource();
                    if (event.getClickCount() == 2)
                    {
                        Node node = gameNode.getNode();
                        if (node.getNumberChildren() > 1)
                        {
                            if (m_expanded.contains(node))
                                hideSubtree(node);
                            else
                                showVariations(node);
                        }
                    }
                    else
                        gotoNode(gameNode.getNode());
                }

                public void mousePressed(MouseEvent event)
                {
                    if (event.isPopupTrigger())
                    {
                        GameTreeNode gameNode
                            = (GameTreeNode)event.getSource();
                        int x = event.getX();
                        int y = event.getY();
                        showPopup(x, y, gameNode);
                    }
                }

                public void mouseReleased(MouseEvent event)
                {
                    if (event.isPopupTrigger())
                    {
                        GameTreeNode gameNode
                            = (GameTreeNode)event.getSource();
                        int x = event.getX();
                        int y = event.getY();
                        showPopup(x, y, gameNode);
                    }
                }
            };
    }

    public Node getCurrentNode()
    {
        return m_currentNode;
    }

    public boolean getFastPaint()
    {
        return m_fastPaint;
    }
    
    public int getLabelMode()
    {
        return m_labelMode;
    }
    
    public int getNodeHeight()
    {
        return m_nodeHeight;
    }
    
    public int getNodeWidth()
    {
        return m_nodeWidth;
    }
    
    public Dimension getPreferredNodeSize()
    {
        return m_preferredNodeSize;
    }

    public Dimension getPreferredScrollableViewportSize()
    {
        return new Dimension(m_nodeDist * 10, m_nodeDist * 3);
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation, int direction)
    {
        int result;
        if (orientation == SwingConstants.VERTICAL)
            result = visibleRect.height;
        else
            result = visibleRect.width;
        result = (result / m_nodeDist) * m_nodeDist;
        return result;
    }

    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }
    
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation, int direction)
    {
        return m_nodeDist;
    }

    public int getSizeMode()
    {
        return m_sizeMode;
    }
    
    public void gotoNode(Node node)
    {
        if (m_listener != null)
            m_listener.gotoNode(node);
    }

    public boolean isCurrent(Node node)
    {
        return node == m_currentNode;
    }

    public boolean isExpanded(Node node)
    {
        return m_expanded.contains(node);
    }

    public void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        if (m_gameTree == null)
            return;
        graphics.setColor(Color.DARK_GRAY);
        drawGrid(graphics, m_gameTree.getRoot(),
                 m_margin + m_nodeWidth / 2, m_margin + m_nodeWidth / 2);
    }

    public void redrawCurrentNode()
    {
        GameTreeNode gameNode = getGameTreeNode(m_currentNode);
        gameNode.repaint();
    }

    public void scrollToCurrent()
    {
        scrollRectToVisible(new Rectangle(m_currentNodeX - 2 * m_nodeWidth,
                                          m_currentNodeY,
                                          5 * m_nodeWidth, 3 * m_nodeWidth));
    }

    public void setLabelMode(int mode)
    {
        switch (mode)
        {
        case LABEL_NUMBER:
        case LABEL_MOVE:
        case LABEL_NONE:
            if (mode != m_labelMode)
            {
                m_labelMode = mode;
                if (m_currentNode != null)
                    update(m_gameTree, m_currentNode);
            }
            break;
        default:
            break;
        }
    }

    /** Only used for a workaround on Mac Java 1.4.2,
        which causes the scrollpane to lose focus after a new layout of
        this panel. If scrollPane is not null, a requestFocusOnWindow will
        be called after each new layout
    */
    public void setScrollPane(JScrollPane scrollPane)
    {
        m_scrollPane = scrollPane;
    }

    public void setSizeMode(int mode)
    {
        switch (mode)
        {
        case SIZE_LARGE:
        case SIZE_NORMAL:
        case SIZE_SMALL:
        case SIZE_TINY:
            if (mode != m_sizeMode)
            {
                m_sizeMode = mode;
                computeSizes(m_sizeMode);
                if (m_currentNode != null)
                    update(m_gameTree, m_currentNode);
            }
            break;
        default:
            break;
        }
    }

    public void update(GameTree gameTree, Node currentNode)
    {
        assert(currentNode != null);
        boolean gameTreeChanged = (gameTree != m_gameTree);
        if (gameTreeChanged)
            m_expanded.clear();
        ensureVisible(currentNode);
        m_gameTree = gameTree;
        m_currentNode = currentNode;
        removeAll();
        m_map.clear();
        m_maxX = 0;
        m_maxY = 0;
        try
        {
            Node root = m_gameTree.getRoot();
            createNodes(this, root, 0, 0, m_margin, m_margin, 0);
            if (gameTreeChanged)
            {
                if (NodeUtils.subtreeGreaterThan(root, 10000))
                    showVariations(root);
                else
                    showSubtree(root);
            }
        }
        catch (OutOfMemoryError e)
        {
            m_expanded.clear();
            removeAll();
            SimpleDialogs.showError(m_owner,
                                    "Could not show game tree\n" + 
                                    "Out of memory");
            update(gameTree, currentNode);
        }
        setPreferredSize(new Dimension(m_maxX + m_nodeDist + m_margin,
                                       m_maxY + m_nodeDist + m_margin));
        revalidate();
        scrollToCurrent();
        if (m_scrollPane != null)
            m_scrollPane.requestFocusInWindow();
    }

    public void update(Node currentNode)
    {
        assert(currentNode != null);
        if (ensureVisible(currentNode))
        {
            update(m_gameTree, currentNode);
            return;
        }
        GameTreeNode gameNode = getGameTreeNode(m_currentNode);
        gameNode.repaint();
        gameNode = getGameTreeNode(currentNode);
        Point location = gameNode.getLocation();
        m_currentNodeX = location.x;
        m_currentNodeY = location.y;
        gameNode.repaint();
        m_currentNode = currentNode;
        scrollToCurrent();
        if (m_scrollPane != null)
            m_scrollPane.requestFocusInWindow();
    }

    private final boolean m_fastPaint;

    private int m_currentNodeX;

    private int m_currentNodeY;

    private int m_labelMode;

    private int m_sizeMode;

    private int m_nodeWidth;

    private int m_nodeHeight;

    private static final int m_margin = 15;

    private int m_maxX;

    private int m_maxY;

    private int m_nodeDist;

    /** Serial version to suppress compiler warning.
        Contains a marker comment for serialver.sourceforge.net
    */
    private static final long serialVersionUID = 0L; // SUID

    private Dimension m_preferredNodeSize;

    private Font m_font;

    private GameTree m_gameTree;

    private final GameTreeViewer.Listener m_listener;

    private final JDialog m_owner;

    /** Used for focus workaround on Mac Java 1.4.2 if not null. */
    private JScrollPane m_scrollPane;

    private Node m_currentNode;

    private Node m_popupNode;

    private final HashMap m_map = new HashMap(500, 0.8f);

    private final HashSet m_expanded = new HashSet(200);

    private final MouseListener m_mouseListener;

    private Point m_popupLocation;

    private void computeSizes(int sizeMode)
    {
        double fontScale;
        switch (sizeMode)
        {
        case SIZE_LARGE:
            fontScale = 1.0;
            break;
        case SIZE_NORMAL:
            fontScale = 0.7;
            break;
        case SIZE_SMALL:
            fontScale = 0.5;
            break;
        case SIZE_TINY:
            fontScale = 0.2;
            break;
        default:
            fontScale = 0.7;
            assert(false);
        }
        m_nodeWidth = 25;
        m_nodeDist = 35;
        Font font = UIManager.getFont("Label.font");
        if (font != null)
        {
            Font derivedFont
                = font.deriveFont((float)(font.getSize() * fontScale));
            if (derivedFont != null)
                font = derivedFont;
        }
        if (font != null)
        {
            m_nodeWidth = font.getSize() * 2;
            if (m_nodeWidth % 2 == 0)
                ++m_nodeWidth;
            m_nodeDist = font.getSize() * 3;
            if (m_nodeDist % 2 == 0)
                ++m_nodeDist;
        }
        m_font = font;
        m_preferredNodeSize = new Dimension(m_nodeWidth, m_nodeDist);
    }

    private int createNodes(Component father, Node node, int x, int y,
                            int dx, int dy, int moveNumber)
    {
        m_maxX = Math.max(x, m_maxX);
        m_maxY = Math.max(y, m_maxY);
        if (node.getMove() != null)
            ++moveNumber;
        m_nodeHeight = m_nodeDist;
        GameTreeNode gameNode =
            new GameTreeNode(node, moveNumber, this, m_mouseListener, m_font);
        m_map.put(node, gameNode);
        add(gameNode);
        SpringLayout layout = (SpringLayout)getLayout();
        layout.putConstraint(SpringLayout.WEST, gameNode, dx,
                             SpringLayout.WEST, father);
        layout.putConstraint(SpringLayout.NORTH, gameNode, dy,
                             SpringLayout.NORTH, father);
        int numberChildren = node.getNumberChildren();
        dx = m_nodeDist;
        dy = 0;
        int maxChildren = numberChildren;
        boolean notExpanded =
            (numberChildren > 1 && ! m_expanded.contains(node));
        if (notExpanded)
            maxChildren = Math.min(numberChildren, 1);
        for (int i = 0; i < maxChildren; ++i)
        {
            dy += createNodes(gameNode, node.getChild(i),
                              x + dx, y + dy, dx, dy, moveNumber);
            if (! notExpanded && i < numberChildren - 1)
                dy += m_nodeDist;
        }
        if (node == m_currentNode)
        {
            m_currentNodeX = x;
            m_currentNodeY = y;
        }
        return dy;
    }

    private int drawGrid(Graphics graphics, Node node, int x, int y)
    {
        int numberChildren = node.getNumberChildren();
        int xChild = x + m_nodeDist;
        int yChild = y;
        int lastY = y + m_nodeWidth;
        boolean notExpanded =
            (numberChildren > 1 && ! m_expanded.contains(node));
        int maxChildren = numberChildren;
        if (notExpanded)
            maxChildren = Math.min(numberChildren, 1);
        for (int i = 0; i < maxChildren; ++i)
        {
            if (i > 0)
                graphics.drawLine(x, lastY, x, yChild);
            graphics.drawLine(x, yChild, xChild, yChild);
            lastY = yChild;
            yChild = drawGrid(graphics, node.getChild(i), xChild, yChild);
            if (! notExpanded && i < numberChildren - 1)
                yChild += m_nodeDist;
        }
        return yChild;
    }

    private GameTreeNode getGameTreeNode(Node node)
    {
        return (GameTreeNode)m_map.get(node);
    }

    private boolean ensureVisible(Node node)
    {
        boolean changed = false;
        while (node != null)
        {
            Node father = node.getFather();
            if (father != null && father.getChild() != node)
                if (m_expanded.add(father))
                    changed = true;
            node = father;
        }
        return changed;
    }

    private void hideOthers(Node node)
    {
        m_expanded.clear();
        ensureVisible(node);
        update(m_gameTree, m_currentNode);
    }

    private void hideSubtree(Node root)
    {
        boolean changed = false;
        boolean currentChanged = false;
        int depth = NodeUtils.getDepth(root);
        Node node = root;
        while (node != null)
        {
            if (node == m_currentNode)
            {
                m_currentNode = root;
                currentChanged = true;
                changed = true;
            }
            if (m_expanded.remove(node))
                changed = true;
            node = NodeUtils.nextNode(node, depth);
        }
        if (currentChanged)
        {
            gotoNode(m_currentNode);
            root = m_currentNode;
        }
        if (changed)
        {
            update(m_gameTree, m_currentNode);
            scrollTo(root);
        }
    }

    private void nodeInfo(Point location, Node node)
    {
        String nodeInfo = NodeUtils.nodeInfo(node);
        String title = "Node Info";
        TextViewer textViewer =
            new TextViewer(m_owner, title, nodeInfo, true, null);
        textViewer.setLocation(location);
        textViewer.setVisible(true);
    }

    private void scrollTo(Node node)
    {
        if (node == null)
            return;
        GameTreeNode gameNode = getGameTreeNode(node);
        Rectangle rectangle = new Rectangle();
        rectangle.x = gameNode.getLocation().x;
        rectangle.y = gameNode.getLocation().y;
        rectangle.width = m_nodeWidth;
        rectangle.height = m_nodeDist;
        scrollRectToVisible(rectangle);
    }

    private void showPopup(int x, int y, GameTreeNode gameNode)
    {
        Node node = gameNode.getNode();
        m_popupNode = node;
        ActionListener listener = new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    String command = event.getActionCommand();
                    if (command.equals("goto"))
                        gotoNode(m_popupNode);
                    else if (command.equals("scroll"))
                        scrollToCurrent();
                    else if (command.equals("show-variations"))
                        showVariations(m_popupNode);
                    else if (command.equals("show-subtree"))
                        showSubtree(m_popupNode);
                    else if (command.equals("hide-others"))
                        hideOthers(m_popupNode);
                    else if (command.equals("hide-subtree"))
                        hideSubtree(m_popupNode);
                    else if (command.equals("node-info"))
                        nodeInfo(m_popupLocation, m_popupNode);
                    else if (command.equals("tree-info"))
                        treeInfo(m_popupLocation, m_popupNode);
                    else
                        assert(false);
                }
            };
        JPopupMenu popup = new JPopupMenu();
        JMenuItem item;
        item = new JMenuItem("Go To");
        item.setActionCommand("goto");
        item.addActionListener(listener);
        popup.add(item);
        popup.addSeparator();
        item = new JMenuItem("Hide Variations");
        if (node.getNumberChildren() == 0)
            item.setEnabled(false);
        item.setActionCommand("hide-subtree");
        item.addActionListener(listener);
        popup.add(item);
        item = new JMenuItem("Hide Others");
        item.setActionCommand("hide-others");
        item.addActionListener(listener);
        popup.add(item);
        item = new JMenuItem("Show Variations");
        if (m_expanded.contains(node) || node.getNumberChildren() <= 1)
            item.setEnabled(false);
        item.setActionCommand("show-variations");
        item.addActionListener(listener);
        popup.add(item);
        item = new JMenuItem("Show Subtree");
        if (node.getNumberChildren() == 0)
            item.setEnabled(false);
        item.setActionCommand("show-subtree");
        item.addActionListener(listener);
        popup.add(item);
        popup.addSeparator();
        item = new JMenuItem("Node Info");
        item.setActionCommand("node-info");
        item.addActionListener(listener);
        popup.add(item);
        item = new JMenuItem("Subtree Statistics");
        item.setActionCommand("tree-info");
        item.addActionListener(listener);
        popup.add(item);
        popup.addSeparator();
        item = new JMenuItem("Scroll To Current");
        item.setActionCommand("scroll");
        item.addActionListener(listener);
        popup.add(item);
        popup.show(gameNode, x, y);
        m_popupLocation = popup.getLocationOnScreen();
    }

    private void showSubtree(Node root)
    {
        if (NodeUtils.subtreeGreaterThan(root, 10000)
            && ! SimpleDialogs.showQuestion(m_owner,
                                            "Really expand large subtree?"))
            return;
        boolean changed = false;
        Node node = root;
        int depth = NodeUtils.getDepth(node);
        while (node != null)
        {
            if (node.getNumberChildren() > 1 && m_expanded.add(node))
                changed = true;
            node = NodeUtils.nextNode(node, depth);
        }
        if (changed)
        {
            update(m_gameTree, m_currentNode);
            scrollTo(root);
        }
    }

    private void showVariations(Node node)
    {
        if (node.getNumberChildren() > 1 && m_expanded.add(node))
        {
            update(m_gameTree, m_currentNode);
            scrollTo(node);
        }
    }

    private void treeInfo(Point location, Node node)
    {
        String treeInfo = NodeUtils.treeInfo(node);
        String title = "Subtree Info";
        TextViewer textViewer =
            new TextViewer(m_owner, title, treeInfo, true, null);
        textViewer.setLocation(location);
        textViewer.setVisible(true);
    }
}

//----------------------------------------------------------------------------