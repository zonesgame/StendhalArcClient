package stendhal.conversion;

import java.io.File;
import java.io.InputStream;

import arc.Core;
import arc.Files;
import arc.files.Fi;

/**
 *
 */
public class ReplaceFile {

    private final String PATH_PROPERTIES = "classResource" + File.separatorChar;

    private static ReplaceFile instance = null;

    private Files.FileType fileType;

    private ReplaceFile() {
        fileType = Files.FileType.internal;
    }

    public static ReplaceFile getInstance() {
        if (instance == null)
            instance = new ReplaceFile();

        return instance;
    }


    public ReplaceFile setFileType(Files.FileType type) {
        this.fileType = type;
        return this;
    }

    public Fi getFileHandle(String filePath) {
        Fi handle = null;
        if (Core.files == null)
            handle = new Fi(filePath);
        else
            handle = Core.files.get(filePath, fileType);

        return handle;
    }

    public InputStream getResourceAsStream(Class<?> c, String proFile) {
        String proFilePath = PATH_PROPERTIES + c.getName().replace('.', File.separatorChar) + File.separatorChar + proFile;
        return getResourceAsStream(proFilePath);
    }

    public InputStream getResourceAsStream(String filePath) {
        Fi handle = getFileHandle(filePath);

        if (handle != null && handle.exists())
            return handle.read();
        else
            return null;
    }

}
