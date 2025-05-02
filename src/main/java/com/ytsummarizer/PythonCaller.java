package com.ytsummarizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonCaller {
    public static String runPythonScript(String scriptPath, String args) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder("python", scriptPath);
            if (!args.isEmpty()) {
                for (String arg : args.split(" ")) {
                    builder.command().add(arg);
                }
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
        } catch (Exception e) {
            output.append("Error running script: ").append(e.getMessage());
        }
        return output.toString();
    }
}
