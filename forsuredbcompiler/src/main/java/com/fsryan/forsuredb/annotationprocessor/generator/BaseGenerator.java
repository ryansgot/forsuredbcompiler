package com.fsryan.forsuredb.annotationprocessor.generator;

import com.fsryan.forsuredb.annotationprocessor.util.APLog;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.Writer;

public abstract class BaseGenerator<F extends FileObject> {

    protected static final String JAVA_INDENT = "    ";

    private final ProcessingEnvironment processingEnv;

    public BaseGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * <p>
     *     Generates the code and writes it out to a file in the correct location
     * </p>
     * @return true if successful, false otherwise
     */
    public final boolean generate() {
        String code = getCode();
        if (code == null) {
            return false;
        }

        Writer writer = null;
        F fileObject = null;
        try {
            fileObject = createFileObject(processingEnv);
            APLog.i(this.getClass().getSimpleName(), "creating source file: " + fileObject.getName());

            writer = fileObject.openWriter();
            writer.write(code);
            return true;
        } catch (IOException ioe) {
            APLog.e(logTag(), "Failed to generate: " + (fileObject == null ? "FileObject was null" : fileObject.getName()));
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioe) {}
            }
        }

        return false;
    }

    /**
     * @param processingEnv use this to get the {@link javax.annotation.processing.Filer}
     *                      for creating the new class, source, or resource
     * @return Some extension of FileObject for a class, source, or resource file. This should be
     * JavaObject for generating java source files
     * @throws IOException if the file cannot be created
     */
    protected abstract F createFileObject(ProcessingEnvironment processingEnv) throws IOException;

    /**
     * @return a String representing the code to generate;
     */
    protected abstract String getCode();

    protected String logTag() {
        return this.getClass().getSimpleName();
    }
}
