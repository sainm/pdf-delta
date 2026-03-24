package org.sainm.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "pdf-compare", mixinStandardHelpOptions = true,
         subcommands = {CompareCommand.class})
public class PdfCompareCli {
    public static void main(String[] args) {
        System.exit(new CommandLine(new PdfCompareCli()).execute(args));
    }
}
