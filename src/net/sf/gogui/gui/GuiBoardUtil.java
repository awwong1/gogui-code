//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package net.sf.gogui.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import net.sf.gogui.game.MarkType;
import net.sf.gogui.game.Node;
import net.sf.gogui.go.ConstBoard;
import net.sf.gogui.go.CountScore;
import net.sf.gogui.go.GoColor;
import net.sf.gogui.go.GoPoint;
import net.sf.gogui.go.Move;

/** Utility functions for class GuiBoard. */
public final class GuiBoardUtil
{
    public static Color getColor(String string)
    {
        if (string.equals("blue"))
            return Color.blue;
        if (string.equals("cyan"))
            return Color.cyan;
        if (string.equals("green"))
            return Color.green;
        if (string.equals("gray"))
            return Color.lightGray;
        if (string.equals("magenta"))
            return Color.magenta;
        if (string.equals("pink"))
            return Color.pink;
        if (string.equals("red"))
            return Color.red;
        if (string.equals("yellow"))
            return Color.yellow;
        if (string.equals("black"))
            return Color.black;
        if (string.equals("white"))
            return Color.white;
        try
        {
            return Color.decode(string);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    public static boolean[][] getMarkSquare(GuiBoard guiBoard)
    {
        int size = guiBoard.getBoardSize();
        boolean[][] result = new boolean[size][size];
        for (int x = 0; x < size; ++x)
            for (int y = 0; y < size; ++y)
            {
                GoPoint point = GoPoint.get(x, y);
                result[x][y] = guiBoard.getMarkSquare(point);
            }
        return result;
    }

    public static String[][] getLabels(GuiBoard guiBoard)
    {
        int size = guiBoard.getBoardSize();
        String[][] result = new String[size][size];
        for (int x = 0; x < size; ++x)
            for (int y = 0; y < size; ++y)
            {
                GoPoint point = GoPoint.get(x, y);
                result[x][y] = guiBoard.getLabel(point);
            }
        return result;
    }

    public static boolean[][] getSelects(GuiBoard guiBoard)
    {
        int size = guiBoard.getBoardSize();
        boolean[][] result = new boolean[size][size];
        for (int x = 0; x < size; ++x)
            for (int y = 0; y < size; ++y)
            {
                GoPoint point = GoPoint.get(x, y);
                result[x][y] = guiBoard.getSelect(point);
            }
        return result;
    }

    public static void setSelect(GuiBoard guiBoard, ArrayList pointList,
                                 boolean select)
    {
        if (pointList == null)
            return;
        for (int i = 0; i < pointList.size(); ++i)
            guiBoard.setSelect((GoPoint)pointList.get(i), select);
    }

    public static void scoreBegin(GuiBoard guiBoard, CountScore countScore,
                                  ConstBoard board, GoPoint[] isDeadStone)
    {
        countScore.begin(board, isDeadStone);
        if (isDeadStone != null)
            for (int i = 0; i < isDeadStone.length; ++i)
                guiBoard.setCrossHair(isDeadStone[i], true);
        computeScore(guiBoard, countScore, board);
    }

    public static void scoreSetDead(GuiBoard guiBoard, CountScore countScore,
                                    ConstBoard board, GoPoint p)
    {
        GoColor c = board.getColor(p);
        if (c == GoColor.EMPTY)
            return;
        ArrayList stones = new ArrayList(board.getNumberPoints());
        board.getStones(p, c, stones);
        boolean dead = ! countScore.getDead((GoPoint)(stones.get(0)));
        for (int i = 0; i < stones.size(); ++i)
        {
            GoPoint stone = (GoPoint)stones.get(i);
            countScore.setDead(stone, dead);
            guiBoard.setCrossHair(stone, dead);
        }
        computeScore(guiBoard, countScore, board);
    }

    public static void showBWBoard(GuiBoard guiBoard, String[][] board)
    {
        for (int x = 0; x < board.length; ++x)
            for (int y = 0; y < board[x].length; ++y)
            {
                GoPoint point = GoPoint.get(x, y);
                String s = board[x][y].toLowerCase(Locale.ENGLISH);
                if (s.equals("b") || s.equals("black"))
                    guiBoard.setTerritory(point, GoColor.BLACK);
                else if (s.equals("w") || s.equals("white"))
                    guiBoard.setTerritory(point, GoColor.WHITE);
                else
                    guiBoard.setTerritory(point, GoColor.EMPTY);
            }
    }

    public static void showChildrenMoves(GuiBoard guiBoard,
                                         ArrayList childrenMoves)
    {
        guiBoard.clearAllLabels();
        int numberMarked = 0;
        char label = 'A';
        for (int i = 0; i < childrenMoves.size(); ++i)
        {
            GoPoint point = (GoPoint)childrenMoves.get(i);
            String s = guiBoard.getLabel(point);
            if (! s.equals(""))
            {
                if (! s.endsWith("."))
                    guiBoard.setLabel(point, s + ".");
                continue;
            }
            if (numberMarked >= 26)
                guiBoard.setLabel(point, "+");
            else
                guiBoard.setLabel(point, Character.toString(label));
            if (numberMarked < 26)
                ++label;
            ++numberMarked;            
        }
    }

    public static void showColorBoard(GuiBoard guiBoard, String[][] colors)
    {
        for (int x = 0; x < colors.length; ++x)
            for (int y = 0; y < colors[x].length; ++y)
            {
                GoPoint point = GoPoint.get(x, y);
                guiBoard.setFieldBackground(point, getColor(colors[x][y]));
            }
    }

    public static void showDoubleBoard(GuiBoard guiBoard, double[][] board)
    {
        for (int x = 0; x < board.length; ++x)
            for (int y = 0; y < board[x].length; ++y)
                guiBoard.setInfluence(GoPoint.get(x, y), board[x][y]);
    }

    public static void showStringBoard(GuiBoard guiBoard,
                                       String[][] board)
    {
        for (int x = 0; x < board.length; ++x)
            for (int y = 0; y < board[x].length; ++y)
            {
                GoPoint point = GoPoint.get(x, y);
                guiBoard.setLabel(point, board[x][y]);
            }
    }

    public static void showMarkup(GuiBoard guiBoard, Node node)
    {
        ArrayList mark;
        mark = node.getMarked(MarkType.MARK);
        if (mark != null)
            for (int i = 0; i < mark.size(); ++i)
                guiBoard.setMark((GoPoint)(mark.get(i)), true);
        mark = node.getMarked(MarkType.CIRCLE);
        if (mark != null)
            for (int i = 0; i < mark.size(); ++i)
                guiBoard.setMarkCircle((GoPoint)(mark.get(i)), true);
        mark = node.getMarked(MarkType.SQUARE);
        if (mark != null)
            for (int i = 0; i < mark.size(); ++i)
                guiBoard.setMarkSquare((GoPoint)(mark.get(i)), true);
        mark = node.getMarked(MarkType.TRIANGLE);
        if (mark != null)
            for (int i = 0; i < mark.size(); ++i)
                guiBoard.setMarkTriangle((GoPoint)(mark.get(i)), true);
        GuiBoardUtil.setSelect(guiBoard, node.getMarked(MarkType.SELECT),
                                true);
        mark = node.getMarked(MarkType.TERRITORY_BLACK);
        if (mark != null)
            for (int i = 0; i < mark.size(); ++i)
                guiBoard.setTerritory((GoPoint)(mark.get(i)), GoColor.BLACK);
        mark = node.getMarked(MarkType.TERRITORY_WHITE);
        if (mark != null)
            for (int i = 0; i < mark.size(); ++i)
                guiBoard.setTerritory((GoPoint)(mark.get(i)), GoColor.WHITE);
        Map labels = node.getLabels();
        if (labels != null)
        {
            Iterator i = labels.entrySet().iterator();
            while (i.hasNext())
            {
                Map.Entry entry = (Map.Entry)i.next();
                GoPoint point = (GoPoint)entry.getKey();
                String value = (String)entry.getValue();
                guiBoard.setLabel(point, value);
            }
        }
    }

    public static void showPointList(GuiBoard guiBoard, GoPoint pointList[])
    {
        guiBoard.clearAllMarkup();
        for (int i = 0; i < pointList.length; ++i)
        {
            GoPoint p = pointList[i];
            if (p != null && p.isOnBoard(guiBoard.getBoardSize()))
                guiBoard.setMarkSquare(p, true);
        }
    }

    public static void showPointStringList(GuiBoard guiBoard,
                                           ArrayList pointList,
                                           ArrayList stringList)
    {
        guiBoard.clearAllLabels();
        for (int i = 0; i < pointList.size(); ++i)
        {
            GoPoint point = (GoPoint)pointList.get(i);
            String string = (String)stringList.get(i);
            if (point != null)
                guiBoard.setLabel(point, string);
        }
    }

    /** Shows moves in variation as stones with move number labels on board.
        If there are several moves on the same point then the only first move
        is shown for short variations (less/equal ten moves); and only the
        last move for long variations.
    */
    public static void showVariation(GuiBoard guiBoard, Move[] variation)
    {
        guiBoard.clearAllLabels();        
        if (variation.length > 10)
            for (int i = 0; i < variation.length; ++i)
            {
                Move move = variation[i];
                if (move.getPoint() != null)
                {
                    String label = Integer.toString(i + 1);
                    guiBoard.setColor(move.getPoint(), move.getColor());
                    guiBoard.setLabel(move.getPoint(), label);
                }
            }
        else
            for (int i = variation.length - 1; i >= 0; --i)
            {
                Move move = variation[i];
                if (move.getPoint() != null)
                {
                    String label = Integer.toString(i + 1);
                    guiBoard.setColor(move.getPoint(), move.getColor());
                    guiBoard.setLabel(move.getPoint(), label);
                }
            }
    }

    public static void updateFromGoBoard(GuiBoard guiBoard, ConstBoard board,
                                         boolean markLastMove)
    {
        for (int i = 0; i < board.getNumberPoints(); ++i)
        {
            GoPoint point = board.getPoint(i);
            guiBoard.setColor(point, board.getColor(point));
        }
        GoPoint point = null;
        int numberPlacements = board.getNumberPlacements();
        if (numberPlacements > 0)
            point = board.getPlacement(numberPlacements - 1).getPoint();
        if (markLastMove)
            guiBoard.markLastMove(point);
        else
            guiBoard.markLastMove(null);
        if (point == null)
        {
            int size = guiBoard.getBoardSize();
            guiBoard.setCursor(GoPoint.get(size / 2, size / 2));
        }
        else
            guiBoard.setCursor(point);
    }

    /** Make constructor unavailable; class is for namespace only. */
    private GuiBoardUtil()
    {
    }

    private static void computeScore(GuiBoard guiBoard,
                                     CountScore countScore,
                                     ConstBoard board)
    {
        countScore.compute();
        for (int i = 0; i < board.getNumberPoints(); ++i)
        {
            GoPoint p = board.getPoint(i);
            GoColor c = countScore.getColor(p);
            guiBoard.setTerritory(p, c);
        }
    }
}
