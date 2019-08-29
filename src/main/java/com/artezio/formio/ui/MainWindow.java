package com.artezio.formio.ui;

import com.artezio.formio.FormDownloader;
import com.artezio.formio.FormUploader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
    private JLabel formioLabel = new JLabel("FormioTool:");
    private JTextField formioApiInput = new JTextField("http://time-qa:3001", 1);
    private JLabel directoryLabel = new JLabel("Directory:");
    private JTextField directoryInput = new JTextField("C:\\formio-forms-mg\\", 1);
    private JLabel usernameLabel = new JLabel("Username:");
    private JTextField usernameInput = new JTextField("root@root.root", 1);
    private JLabel passwordLabel = new JLabel("Password:");
    private JTextField passwordInput = new JPasswordField("root", 1);
    private JLabel tagsLabel = new JLabel("Filter by tags (download only):");
    private JTextField tagsInput = new JTextField("", 1);
    private JButton downloadButton = new JButton("Download from Formio");
    private JButton uploadButton = new JButton("Upload to Formio");
    private JLabel statusLabel = new JLabel();

    public MainWindow() {
        super("Formio import/export tool");
        this.setBounds(100, 100, 450, 200);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
        container.add(tagsLabel);
        container.add(tagsInput);
        container.add(statusLabel);
        container.add(new JPanel());
        container.add(downloadButton);
        container.add(uploadButton);
        downloadButton.addActionListener(new DownloadFormsActionListener());
        uploadButton.addActionListener(new UploadFormsActionListener());
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
                statusLabel.setText("An Error occured");
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
                statusLabel.setText("An Error occured");
                t.printStackTrace();
            }
            statusLabel.setVisible(true);
        }
    }
}
