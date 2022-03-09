package fr.kinsteen.easyencrypt;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class InvertedFileFilter extends FileFilter {
    private final FileFilter m_originalFilter;

    public InvertedFileFilter(final FileFilter originalFilter) {
        m_originalFilter = originalFilter;
    }

    public boolean accept(final File file) {
        return !m_originalFilter.accept(file);
    }

    @Override
    public String getDescription() {
        return "Invert filter passed in argument";
    }
}
