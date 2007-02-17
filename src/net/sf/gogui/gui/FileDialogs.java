//----------------------------------------------------------------------------
// $Id: SimpleDialogs.java 4309 2007-02-14 22:49:26Z enz $
//----------------------------------------------------------------------------

package net.sf.gogui.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.prefs.Preferences;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.sf.gogui.sgf.SgfFilter;
import net.sf.gogui.thumbnail.ThumbnailCreator;
import net.sf.gogui.thumbnail.ThumbnailPlatform;
import net.sf.gogui.util.Platform;

/** File dialogs. */
public final class FileDialogs
{
    /** Dialog type for opening a file. */
    public static final int FILE_OPEN = 0;

    /** Dialog type for saving to a file. */
    public static final int FILE_SAVE = 1;

    /** Dialog type for selecting a file.
        Use this type, if a file name should be selected, but it is not known
        what the file name is used for and if the file already exists.
    */
    public static final int FILE_SELECT = 2;
    
    public static File showOpen(Component parent, String title)
    {
        return showFileChooser(parent, FILE_OPEN, null, false, title);
    }

    public static File showOpenSgf(Component parent)
    {
        return showFileChooser(parent, FILE_OPEN, null, true, null);
    }

    public static File showSave(Component parent, String title,
                                MessageDialogs messageDialogs)
    {
        return showFileChooserSave(parent, null, false, title,
                                   messageDialogs);
    }

    public static File showSaveSgf(Frame parent,
                                   MessageDialogs messageDialogs)
    {
        return showFileChooserSave(parent, s_lastFile, true, null,
                                   messageDialogs);
    }

    /** File selection, unknown whether for load or save. */
    public static File showSelectFile(Component parent, String title)
    {
        return showFileChooser(parent, FILE_SELECT, s_lastFile, false, title);
    }

    public static void setLastFile(File file)
    {
        s_lastFile = file;
    }

    private static File s_lastFile;

    /** Make constructor unavailable; class is for namespace only. */
    private FileDialogs()
    {
    }

    /** Find first parent that is a Frame.
        @return null If no such parent.
    */
    private static Frame findParentFrame(Component component)
    {
        while (component != null)
            if (component instanceof Frame)
                return (Frame)component;
            else
                component = component.getParent();
        return null;
    }

    private static File showFileChooser(Component parent, int type,
                                        File lastFile, boolean setSgfFilter,
                                        String title)
    {
        if (Platform.isMac() && type != FILE_SELECT)
        {
            Frame frame = findParentFrame(parent);
            return showFileChooserAWT(frame, type, title);
        }
        return showFileChooserSwing(parent, type, lastFile, setSgfFilter,
                                    title);
    }

    private static File showFileChooserSave(Component parent,
                                            File lastFile,
                                            boolean setSgfFilter,
                                            String title,
                                            MessageDialogs messageDialogs)
    {
        File file = showFileChooser(parent, FILE_SAVE, lastFile, setSgfFilter,
                                    title);
        if (Platform.isMac())
            // Overwrite warning is already part of FileDialog
            return file;
        while (file != null)
        {
            if (file.exists())
            {
                String mainMessage =
                    "Replace file \"" + file.getName() + "\"?";
                String optionalMessage =
                    "If you overwrite the file, the previous " +
                    "version will be lost.";
                if (! messageDialogs.showQuestion(parent, mainMessage,
                                                  optionalMessage,
                                                  "Replace", true))
                {
                    file = showFileChooser(parent, FILE_SAVE, lastFile,
                                           setSgfFilter, title);
                    continue;
                }
            }
            break;
        }
        return file;
    }

    private static File showFileChooserAWT(Frame parent, int type,
                                           String title)
    {
        FileDialog dialog = new FileDialog(parent);
        if (title == null)
        {
            switch (type)
            {
            case FILE_OPEN:
                title = "Open";
                break;
            case FILE_SAVE:
                title = "Save";
                break;
            default:
                assert(false);
            }
        }
        dialog.setTitle(title);
        int mode = FileDialog.LOAD;
        if (type == FILE_SAVE)
            mode = FileDialog.SAVE;
        dialog.setMode(mode);
        /* Commented out, because there is no way to change the filter by the
           user (at least not on Linux)
        if (setSgfFilter)
            dialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name)
                    {
                        return name.toLowerCase().endsWith("sgf");
                    }
                });
        */
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        if (dialog.getFile() == null)
            return null;
        return new File(dialog.getDirectory(), dialog.getFile());
    }

    private static File showFileChooserSwing(Component parent, int type,
                                             File lastFile,
                                             boolean setSgfFilter,
                                             String title)
    {
        JFileChooser chooser;
        if (s_lastFile == null)
        {
            if (Platform.isMac())
                // user.dir is application directory on Mac, which is bad
                // I have not found a way to set it to user home in Info.plist
                // so I use null here, which sets is to the user home
                chooser = new JFileChooser((String)null);
            else
                chooser = new JFileChooser(System.getProperty("user.dir"));
        }
        else
            chooser = new JFileChooser(s_lastFile);
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter sgfFilter = new SgfFilter();
        chooser.addChoosableFileFilter(sgfFilter);
        if (setSgfFilter)
        {
            chooser.setFileFilter(sgfFilter);
            if (ThumbnailPlatform.checkThumbnailSupport())
            {
                SgfPreview preview = new SgfPreview();
                chooser.setAccessory(preview);
                chooser.addPropertyChangeListener(preview);
            }
        }
        else
            chooser.setFileFilter(chooser.getAcceptAllFileFilter());
        if (type == FILE_SAVE)
        {
            if (lastFile != null && lastFile.isFile() && lastFile.exists())
                chooser.setSelectedFile(lastFile);
        }
        int ret;
        switch (type)
        {
        case FILE_SAVE:
            ret = chooser.showSaveDialog(parent);
            break;
        case FILE_OPEN:
            ret = chooser.showOpenDialog(parent);
            break;
        default:
            if (title != null)
                chooser.setDialogTitle(title);
            ret = chooser.showDialog(parent, "Select");
            break;
        }
        if (ret != JFileChooser.APPROVE_OPTION)
            return null;
        File file = chooser.getSelectedFile();
        s_lastFile = file;
        return file;
    }
}

class SgfPreview
    extends JPanel
    implements PropertyChangeListener
{    
    public SgfPreview()
    {
        setLayout(new BorderLayout());
        JPanel previewPanel = new JPanel();
        previewPanel.setBorder(GuiUtil.createEmptyBorder());
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.add(Box.createVerticalGlue());
        Dimension dimension = new Dimension(140, 140);
        previewPanel.setPreferredSize(dimension);
        add(previewPanel);
        m_imagePanel = new ImagePanel();
        previewPanel.add(m_imagePanel);
        previewPanel.add(Box.createVerticalGlue());
        JPanel buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        m_auto = new JCheckBox("Automatic preview");
        m_auto.setSelected(m_prefs.getBoolean("auto-preview", false));
        m_auto.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    boolean isSelected = m_auto.isSelected();
                    m_prefs.putBoolean("auto-preview", isSelected);
                    if (isSelected)
                        preview();
                } });
        buttonPanel.add(m_auto);
        m_preview = new JButton("Preview");
        m_preview.setActionCommand("preview");
        m_preview.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (event.getActionCommand().equals("preview"))
                        preview();
                } });
        m_preview.setEnabled(false);
        buttonPanel.add(m_preview);
    }
    
    public void propertyChange(PropertyChangeEvent event)
    {
        String propertyName = event.getPropertyName();
        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
        {
            m_file = null;
            m_preview.setEnabled(false);
            File file = (File)event.getNewValue();
            if (file != null && ! file.exists()) // user entered new filename
                file = null;
            if (file != null)
            {
                String name = file.getAbsolutePath();
                if (name == null
                    || ! name.toLowerCase(Locale.ENGLISH).endsWith(".sgf"))
                    file = null;
            }
            m_file = file;
            if (file != null)
                m_preview.setEnabled(true);
            if (m_auto.isSelected())
                preview();
        }
    }

    private class ImagePanel
        extends JPanel
    {
        public ImagePanel()
        {
            // Image size is 128x128
            Dimension dimension = new Dimension(140, 140);
            setPreferredSize(dimension);
            setMaximumSize(dimension);
        }

        public void paintComponent(Graphics graphics)
        {
            graphics.setColor(getBackground());
            graphics.fillRect(0, 0, getWidth(), getHeight());
            if (m_image != null)
            {
                int imageWidth = m_image.getWidth(null);
                int imageHeight = m_image.getHeight(null);
                int x = (getWidth() - imageWidth) / 2;
                int y = (getHeight() - imageHeight) / 2;
                graphics.drawImage(m_image, x, y, imageWidth, imageHeight,
                                   null);
            }
        }

        /** Serial version to suppress compiler warning.
            Contains a marker comment for serialver.sourceforge.net
        */
        private static final long serialVersionUID = 0L; // SUID
    }

    /** Serial version to suppress compiler warning.
        Contains a marker comment for serialver.sourceforge.net
    */
    private static final long serialVersionUID = 0L; // SUID

    private File m_file;

    private File m_lastFailure;

    private String m_lastError;

    private final JButton m_preview;

    private final JCheckBox m_auto;

    private Image m_image;

    private final ImagePanel m_imagePanel;

    private final ThumbnailCreator m_thumbnailCreator
        = new ThumbnailCreator(false);

    private final Preferences m_prefs =
        Preferences.userNodeForPackage(getClass());        

    private static final Image m_missingImage =
        GuiUtil.getIcon("image-missing", "No preview").getImage();

    public void preview()
    {
        if (m_file == null)
        {
            m_image = null;
            m_imagePanel.setToolTipText(null);
        }
        else if (m_file.equals(m_lastFailure))
        {
            m_image = m_missingImage;
            m_imagePanel.setToolTipText(m_lastError);
        }
        else
        {
            try
            {
                m_thumbnailCreator.create(m_file);
                File thumbnail = m_thumbnailCreator.getLastThumbnail();
                m_image = loadImage(thumbnail);
                String description = m_thumbnailCreator.getLastDescription();
                if (description != null && ! description.trim().equals(""))
                    m_imagePanel.setToolTipText(description);
                else
                    m_imagePanel.setToolTipText(null);
            }
            catch (ThumbnailCreator.Error e)
            {
                m_image = m_missingImage;
                m_lastError = e.getMessage();
                if (m_lastError != null && m_lastError.trim().equals(""))
                    m_lastError = null;
                m_imagePanel.setToolTipText(m_lastError);
                m_lastFailure = m_file;
            }
        }
        m_imagePanel.repaint();
        m_preview.setEnabled(false);
    }

    private static Image loadImage(File file)
    {
        URL url;
        try
        {
            // File.toURL() is deprecated in Java 1.6
            url = file.toURI().toURL();
        }
        catch (MalformedURLException e)
        {
            assert(false); // Cannot happen
            return null;
        }
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        MediaTracker mediaTracker = new MediaTracker(new Container());
        mediaTracker.addImage(image, 0);
        try
        {
            mediaTracker.waitForID(0);
        }
        catch (InterruptedException e)
        {
            return null;
        }
        return image;
    }
}
