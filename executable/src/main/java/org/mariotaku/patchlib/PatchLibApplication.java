package org.mariotaku.patchlib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.cli.*;
import org.mariotaku.patchlib.common.model.PatchClassInfo;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mariotaku on 15/12/1.
 */
public class PatchLibApplication {
    private final Options options;

    public PatchLibApplication() {
        // create Options object
        options = new Options();
        options.addOption("i", "in", true, "Input file");
        options.addOption("o", "out", true, "Output file");
        options.addOption("r", "rules", true, "Rules files in .yml format");
        options.addOption("c", "classpath", true, "Extra classpath");
    }

    public void start(String[] args) throws ParseException, IOException {
        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("in") && cmd.hasOption("out") && cmd.hasOption("rules")) {
            final File inFile = new File(cmd.getOptionValue("in"));
            final File outFile = new File(cmd.getOptionValue("out"));
            final File rulesFile = new File(cmd.getOptionValue("rules"));
            final String classpath = cmd.getOptionValue("classpath");
            if (checkParamFiles(inFile, outFile, rulesFile)) {
                if (startProcess(inFile, outFile, rulesFile, classpath)) {
                    System.out.println("Process finished!");
                }
            }
        } else {
            showUsage();
        }
    }

    private boolean startProcess(File inFile, File outFile, File rulesFile, String classpath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final Map<String, PatchClassInfo> rules = mapper.readValue(rulesFile, new TypeReference<Map<String, PatchClassInfo>>() {
        });
        try (InputStream is = new FileInputStream(inFile); OutputStream os = new FileOutputStream(outFile)) {
            LibraryProcessor processor = LibraryProcessor.get(is, os, rules, inFile.getName());
            if (processor == null) {
                throw new UnsupportedOperationException("Unsupported library " + inFile);
            }
            Set<File> files = new HashSet<>();
            if (classpath != null) {
                for (String item : classpath.split(":")) {
                    if (item.isEmpty()) continue;
                    files.add(new File(item));
                }
            }
            processor.setExtraClasspath(files);
            return processor.process();
        }
    }

    private boolean checkParamFiles(File inFile, File outFile, File rulesFile) {
        if (!inFile.exists()) {
            System.err.print("Input file not found");
            return false;
        }
        if (!rulesFile.exists()) {
            System.err.print("Rules file not found");
            return false;
        }
        final File outDir = outFile.getParentFile();
        if (outDir != null && !outDir.exists() && !outDir.mkdirs()) {
            System.err.print("Cannot open output dir");
            return false;
        }
        return true;
    }

    private void showUsage() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PatchLib", options);
    }
}
