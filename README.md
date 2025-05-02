# 🎥 YouTube Video Summarizer

This project allows users to input any **YouTube video URL** and receive a **summarized version** of the spoken content. It uses **Python scripts** for downloading, transcribing, and summarizing, along with a **Java Swing GUI** for a smooth user experience.

---

## 📂 Project Directory Structure

```
YouTubeVideoSummarizer/
│
├── scripts/                          # Python scripts for download, transcript, and summary
│   ├── download_audio.py             # Uses yt-dlp to download audio
│   ├── fetch_transcript.py           # Gets transcript via YouTube API or Whisper fallback
│   ├── summarize_text.py             # Summarizes transcript using Transformers
│   └── whisper_transcribe.py         # Standalone Whisper transcription
│
├── java_src/                         # Java source files for GUI and script execution
│   ├── App.java                      # Entry point for app (Hello World test)
│   ├── MainApp.java                  # Launches the GUI
│   ├── PythonCaller.java             # Java class to run Python scripts
│   └── YouTubeSummarizerGUI.java     # Main GUI with input, progress, and downloads
│
├── downloads/                        # Downloaded audio files (e.g., audio1.mp3)
├── output/                           # Transcripts and summaries (e.g., transcript1.txt)
```

---

## ✅ Features

- Extracts audio from any YouTube video
- Transcribes using YouTube API or OpenAI Whisper fallback
- Summarizes with HuggingFace BART transformer model
- Java GUI to manage input, progress, and file downloads
- File versioning to prevent overwriting

---

## ⚙️ Requirements

### 📌 Python (3.8+)
Install these dependencies:
```bash
pip install -r requirements.txt
```

**`requirements.txt`**
```
yt-dlp
transformers
torch
whisper
youtube_transcript_api
```

Also, install **ffmpeg** (used by `yt-dlp` and `whisper`):
- **Windows**: [https://ffmpeg.org/download.html](https://ffmpeg.org/download.html)
- **Linux**: `sudo apt install ffmpeg`
- **Mac**: `brew install ffmpeg`

---

### 📌 Java (8 or later)
Ensure Java is installed:
```bash
java -version
```

Compile the Java files:
```bash
javac -d . java_src/*.java
```

Run the app:
```bash
java com.ytsummarizer.MainApp
```

---

## 🏁 How to Run the Project

1. Clone or download this repository.
2. Make sure all requirements above are installed.
3. Run Java app:
   ```bash
   java com.ytsummarizer.MainApp
   ```
4. In the GUI, enter a YouTube URL and click "Summarize".
5. Wait for audio download → transcript → summary.
6. Download your summary, transcript, or audio.

---

## 🧪 Example

You enter:
```
https://www.youtube.com/watch?v=xyz123
```

Behind the scenes:
- `download_audio.py` gets the `.mp3`
- `fetch_transcript.py` pulls captions or uses Whisper
- `summarize_text.py` creates a short summary
- GUI displays it and lets you save everything

---

## 📄 Notes

- Output files are stored in `downloads/` and `output/`.
- File names auto-increment (e.g., `audio1.mp3`, `summary2.txt`) so no overwriting.
- Java communicates with Python using `ProcessBuilder`.

---

## 🙌 Credits

- [yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [OpenAI Whisper](https://github.com/openai/whisper)
- [HuggingFace Transformers](https://huggingface.co)
- [YouTube Transcript API](https://github.com/jdepoix/youtube-transcript-api)

Enjoy summarizing! 🎧📝
