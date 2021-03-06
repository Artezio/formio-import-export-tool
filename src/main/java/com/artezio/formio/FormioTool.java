package com.artezio.formio;

import com.artezio.formio.ui.MainWindow;
import org.apache.commons.cli.*;

import java.io.IOException;

public class FormioTool {

    private static final String API_URL_OPTION = "url";
    private static final String DIRECTORY_OPTION = "dir";
    private static final String USERNAME_OPTION = "user";
    private static final String PASSWORD_OPTION = "pass";
    private static final String FORM_PATHS_OPTION = "formPaths";
    private static final String TAGS_OPTION = "tags";
    private static final String UPLOAD_ACTION_OPTION = "u";
    private static final String DOWNLOAD_ACTION_OPTION = "d";
    private static final String DELETE_ACTION_OPTION = "r";
    private static final String HELP_OPTION = "h";

    private static final Options commandsWithOptions;
    private static final Options commandsWithoutOptions;
    static {
        Option apiUrl = Option.builder(null).hasArg().argName("URL").required()
                .longOpt(API_URL_OPTION).desc("formio api url").build();
        Option directory = Option.builder(null).hasArg().argName("DIRECTORY").required()
                .longOpt(DIRECTORY_OPTION).desc("directory in file system where forms are stored").build();
        Option username = Option.builder(null).hasArg().argName("USERNAME").required()
                .longOpt(USERNAME_OPTION).desc("username").build();
        Option password = Option.builder(null).hasArg().argName("PASSWORD").required()
                .longOpt(PASSWORD_OPTION).desc("password").build();
        Option formPaths = Option.builder(null).hasArg().argName("FORM_PATHS")
                .longOpt(FORM_PATHS_OPTION).desc("comma-separated list of form paths for filtering").build();
        Option tags = Option.builder(null).hasArg().argName("TAGS")
                .longOpt(TAGS_OPTION).desc("comma-separated list of tags for filtering").build();
        Option upload = Option.builder(UPLOAD_ACTION_OPTION).desc("upload form").build();
        Option download = Option.builder(DOWNLOAD_ACTION_OPTION).desc("download form").build();
        Option delete = Option.builder(DELETE_ACTION_OPTION).desc("delete form").build();
        Option help = Option.builder(HELP_OPTION).desc("print help").build();

        OptionGroup actionsOptionGroup = new OptionGroup()
                .addOption(download)
                .addOption(upload)
                .addOption(delete)
                .addOption(help);

        commandsWithoutOptions = new Options()
                .addOptionGroup(actionsOptionGroup);

        commandsWithOptions = new Options()
                .addOptionGroup(actionsOptionGroup)
                .addOption(apiUrl)
                .addOption(directory)
                .addOption(username)
                .addOption(password)
                .addOption(formPaths)
                .addOption(tags);
    }

    private CommandLineParser defaultParser = new DefaultParser();

    public static void main(String[] args) throws ParseException, IOException {
        FormioTool formioTool = new FormioTool();
        CommandLine parsedCommandLine = formioTool.parse(args);
        if (parsedCommandLine.hasOption(DOWNLOAD_ACTION_OPTION)) {
            formioTool.downloadAllForms(parsedCommandLine);
        } else if (parsedCommandLine.hasOption(UPLOAD_ACTION_OPTION)) {
            formioTool.upload(parsedCommandLine);
        } else if (parsedCommandLine.hasOption(DELETE_ACTION_OPTION)) {
            System.out.println("Are you sure that you want to delete specified forms (y/n)? [n]");
            char read = (char) System.in.read();
            if (read == 'y') {
                formioTool.delete(parsedCommandLine);
            }
        } else if (parsedCommandLine.hasOption(HELP_OPTION)) {
            formioTool.printHelp();
        } else {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        }
    }

    private void printHelp() {
        new HelpFormatter().printHelp(150, "formio", "", commandsWithOptions,
                "Note: if you pass -h, long options are ignored.", true);
    }

    private void upload(CommandLine commands) throws IOException {
        new FormUploader().upload(
                commands.getOptionValue(API_URL_OPTION),
                commands.getOptionValue(USERNAME_OPTION),
                commands.getOptionValue(PASSWORD_OPTION),
                commands.getOptionValue(DIRECTORY_OPTION));
    }

    private void downloadAllForms(CommandLine commands) {
        new FormDownloader().downloadAllForms(
                commands.getOptionValue(API_URL_OPTION),
                commands.getOptionValue(USERNAME_OPTION),
                commands.getOptionValue(PASSWORD_OPTION),
                commands.getOptionValue(DIRECTORY_OPTION),
                commands.getOptionValue(TAGS_OPTION, ""));
    }

    private void delete(CommandLine commands) {
        new FormDeleter().delete(commands.getOptionValue(API_URL_OPTION),
                commands.getOptionValue(USERNAME_OPTION),
                commands.getOptionValue(PASSWORD_OPTION),
                commands.getOptionValue(FORM_PATHS_OPTION, ""),
                commands.getOptionValue(TAGS_OPTION, ""));
    }

    private CommandLine parse(String[] args) throws ParseException {
        CommandLine parsedCommandLine = defaultParser.parse(commandsWithoutOptions, args, true);
        if (parsedCommandLine.hasOption(UPLOAD_ACTION_OPTION) || parsedCommandLine.hasOption(DOWNLOAD_ACTION_OPTION)
                || parsedCommandLine.hasOption(DELETE_ACTION_OPTION)) {
            return defaultParser.parse(commandsWithOptions, args);
        }
        return parsedCommandLine;
    }

}