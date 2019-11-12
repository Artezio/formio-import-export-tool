package com.artezio.formio.ui;

import com.artezio.formio.FormDeleter;
import com.artezio.formio.FormDownloader;
import com.artezio.formio.FormUploader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
    private JLabel formioLabel = new JLabel("Formio server url:");
    private JTextField formioApiInput = new JTextField("http://localhost:3001", 1);
    private JLabel directoryLabel = new JLabel("Directory:");
    private JTextField directoryInput = new JTextField("C:\\formio-forms-mg\\", 1);
    private JLabel usernameLabel = new JLabel("Username:");
    private JTextField usernameInput = new JTextField("root@root.root", 1);
    private JLabel passwordLabel = new JLabel("Password:");
    private JTextField passwordInput = new JPasswordField("root", 1);
    private JLabel formPathsLabel = new JLabel("Form paths (delete only):");
    private JTextField formPathsInput = new JTextField("", 1);
    private JLabel tagsLabel = new JLabel("Filter by tags (download, delete only):");
    private JTextField tagsInput = new JTextField("", 1);
    private JLabel statusLabel = new JLabel();

    public MainWindow() {
        super("Formio import/export tool");
        this.setBounds(100, 100, 450, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(buildMenu());
        Container container = this.getContentPane();
        container.setLayout(new GridLayout(7, 2, 2, 2));
        container.add(formioLabel);
        container.add(formioApiInput);
        container.add(directoryLabel);
        container.add(directoryInput);
        container.add(usernameLabel);
        container.add(usernameInput);
        container.add(passwordLabel);
        container.add(passwordInput);
        container.add(formPathsLabel);
        container.add(formPathsInput);
        container.add(tagsLabel);
        container.add(tagsInput);
        container.add(statusLabel);
    }

    private JMenuBar buildMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu actionMenu = new JMenu("Action");
        JMenuItem uploadMenu = new JMenuItem("Upload");
        uploadMenu.addActionListener(new UploadFormsActionListener());
        JMenuItem downloadMenu = new JMenuItem("Download");
        downloadMenu.addActionListener(new DownloadFormsActionListener());
        JMenuItem deleteMenu = new JMenuItem("Delete");
        deleteMenu.addActionListener(new DeleteFormsActionListener());

        actionMenu.add(uploadMenu);
        actionMenu.add(downloadMenu);
        actionMenu.add(deleteMenu);

        menuBar.add(actionMenu);

        return menuBar;
    }

    class DownloadFormsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                new FormDownloader().downloadAllForms(
                        formioApiInput.getText(),
                        usernameInput.getText(),
                        passwordInput.getText(),
                        directoryInput.getText(),
                        tagsInput.getText());
                statusLabel.setForeground(Color.GREEN);
                statusLabel.setText("Success");
            } catch (Throwable t) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("An error occurred");
                t.printStackTrace();
            }
            statusLabel.setVisible(true);
        }
    }

    class UploadFormsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                new FormUploader().upload(
                        formioApiInput.getText(),
                        usernameInput.getText(),
                        passwordInput.getText(),
                        directoryInput.getText());
                statusLabel.setForeground(Color.GREEN);
                statusLabel.setText("Success");
            } catch (Throwable t) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("An error occurred");
                t.printStackTrace();
            }
            statusLabel.setVisible(true);
        }
    }

    class DeleteFormsActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                new FormDeleter().delete(
                        formioApiInput.getText(),
                        usernameInput.getText(),
                        passwordInput.getText(),
                        formPathsInput.getText(),
                        tagsInput.getText()
                );
                statusLabel.setForeground(Color.GREEN);
                statusLabel.setText("Success");
            } catch (Throwable ex) {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("An error occurred");
            }
            statusLabel.setVisible(true);
        }
    }

}
