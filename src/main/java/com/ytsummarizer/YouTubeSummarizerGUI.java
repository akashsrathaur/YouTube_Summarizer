package com.ytsummarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class YouTubeSummarizerGUI {

    private static SwingWorker<Void, String> worker;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(YouTubeSummarizerGUI::createGUI);
    }

    public static void createGUI() {
        JFrame frame = new JFrame("🎥 YouTube Video Summarizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 500);

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JTextField urlField = new JTextField();
        JButton summarizeButton = new JButton("▶ Summarize");
        JButton cancelButton = new JButton("❌ Cancel");
        JButton downloadAudioButton = new JButton("⬇ Download Audio");
        JButton downloadSummaryButton = new JButton("⬇ Download Summary");
        JButton downloadTranscriptButton = new JButton("⬇ Download Transcript");

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("YouTube URL: "), BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(summarizeButton, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(cancelButton);
        bottomPanel.add(downloadAudioButton);
        bottomPanel.add(downloadSummaryButton);
        bottomPanel.add(downloadTranscriptButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        cancelButton.setEnabled(false);
        downloadAudioButton.setEnabled(false);
        downloadSummaryButton.setEnabled(false);
        downloadTranscriptButton.setEnabled(false);

        summarizeButton.addActionListener((ActionEvent e) -> {
            String url = urlField.getText().trim();
            if (url.isEmpty()) {
                resultArea.setText("❗ Please enter a YouTube URL.");
                return;
            }

            summarizeButton.setEnabled(false);
            cancelButton.setEnabled(true);
            resultArea.setText("🔄 Starting summarization...\n");

            worker = new SwingWorker<>() {
                File audioFile;
                File transcriptFile;
                File summaryFile;

                @Override
                protected Void doInBackground() {
                    try {
                        publish("Step 1: Downloading audio...");
                        progressBar.setValue(10);
                        String audioOutput = PythonCaller.runPythonScript("scripts/download_audio.py", url + " downloads");
                        publish(audioOutput);

                        audioFile = getLatestFile("downloads", ".mp3");
                        if (audioFile == null) {
                            publish("❌ ERROR: Audio file not found.");
                            return null;
                        }

                        String outputDir = new File("output").getAbsolutePath();
                        String transcriptName = "transcript" + getNextNumber("output", "transcript", ".txt") + ".txt";
                        String transcriptPath = new File("output", transcriptName).getAbsolutePath();

                        publish("Step 2: Fetching transcript (YT API fallback to Whisper)...");
                        progressBar.setValue(40);

                        // ✅ FIXED LINE BELOW
                        String transcribeOut = PythonCaller.runPythonScript(
                                "scripts/fetch_transcript.py",
                                url + " " + audioFile.getAbsolutePath() + " " + transcriptPath
                        );
                        publish(transcribeOut);

                        transcriptFile = new File(transcriptPath);
                        if (!transcriptFile.exists()) {
                            publish("❌ ERROR: Transcript file not found.");
                            return null;
                        }

                        String summaryName = "summary" + getNextNumber("output", "summary", ".txt") + ".txt";
                        String summaryPath = new File("output", summaryName).getAbsolutePath();

                        publish("Step 3️: Summarizing text...");
                        progressBar.setValue(70);
                        String summaryOut = PythonCaller.runPythonScript("scripts/summarize_text.py", transcriptPath + " " + summaryPath);

                        summaryFile = new File(summaryPath);
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(summaryFile))) {
                            writer.write(summaryOut);
                        }

                        publish("✅ Done!\n\n=== Summary ===\n" + summaryOut);
                        progressBar.setValue(100);

                    } catch (Exception ex) {
                        publish("❌ ERROR: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<String> chunks) {
                    for (String msg : chunks) {
                        resultArea.append(msg + "\n");
                    }
                }

                @Override
                protected void done() {
                    summarizeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    downloadAudioButton.setEnabled(audioFile != null);
                    downloadSummaryButton.setEnabled(summaryFile != null);
                    downloadTranscriptButton.setEnabled(transcriptFile != null);
                }
            };
            worker.execute();
        });

        cancelButton.addActionListener(e -> {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
                resultArea.append("\n❌ Task canceled.\n");
                progressBar.setValue(0);
                summarizeButton.setEnabled(true);
                cancelButton.setEnabled(false);
            }
        });

        downloadAudioButton.addActionListener(e -> {
            if (worker != null && !worker.isDone()) return;
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("audio_downloaded.mp3"));
            if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.copy(getLatestFile("downloads", ".mp3").toPath(), fc.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                    resultArea.append("✅ Audio downloaded.\n");
                } catch (IOException ex) {
                    resultArea.append("❌ Failed to save audio.\n");
                }
            }
        });

        downloadSummaryButton.addActionListener(e -> {
            if (worker != null && !worker.isDone()) return;
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("summary.txt"));
            if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.copy(getLatestFile("output", "summary", ".txt").toPath(), fc.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                    resultArea.append("✅ Summary downloaded.\n");
                } catch (IOException ex) {
                    resultArea.append("❌ Failed to save summary.\n");
                }
            }
        });

        downloadTranscriptButton.addActionListener(e -> {
            if (worker != null && !worker.isDone()) return;
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("transcript.txt"));
            if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.copy(getLatestFile("output", "transcript", ".txt").toPath(), fc.getSelectedFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
                    resultArea.append("✅ Transcript downloaded.\n");
                } catch (IOException ex) {
                    resultArea.append("❌ Failed to save transcript.\n");
                }
            }
        });
    }

    private static File getLatestFile(String dir, String extension) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(extension));
        if (files == null || files.length == 0) return null;
        File latest = files[0];
        for (File f : files) {
            if (f.lastModified() > latest.lastModified()) latest = f;
        }
        return latest;
    }

    private static File getLatestFile(String dir, String prefix, String extension) {
        File[] files = new File(dir).listFiles((d, name) -> name.startsWith(prefix) && name.endsWith(extension));
        if (files == null || files.length == 0) return null;
        File latest = files[0];
        for (File f : files) {
            if (f.lastModified() > latest.lastModified()) latest = f;
        }
        return latest;
    }

    private static int getNextNumber(String dir, String prefix, String extension) {
        int max = 0;
        File folder = new File(dir);
        if (!folder.exists()) return 1;
        for (File file : folder.listFiles()) {
            String name = file.getName();
            if (name.startsWith(prefix) && name.endsWith(extension)) {
                try {
                    String numberStr = name.replace(prefix, "").replace(extension, "").trim();
                    int num = Integer.parseInt(numberStr);
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return max + 1;
    }
}
