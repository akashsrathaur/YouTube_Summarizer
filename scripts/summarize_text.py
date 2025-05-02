import sys
import os
from transformers import pipeline

if len(sys.argv) != 3:
    print("Usage: python summarize_text.py <input_path> <output_path>")
    sys.exit(1)

input_path = sys.argv[1]
output_path = sys.argv[2]

try:
    with open(input_path, "r", encoding="utf-8") as f:
        text = f.read()

    summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

    if len(text.strip()) < 30:
        summary = text
    else:
        word_count = len(text.split())
        max_length = min(150, word_count // 2 + 30)
        min_length = min(80, word_count // 4)

        result = summarizer(text, max_length=max_length, min_length=min_length, do_sample=False)
        summary = result[0]["summary_text"]

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        for line in summary.split('. '):
            f.write(line.strip() + '.\n')

    # ✅ Print summary in a way that avoids encoding issues
    sys.stdout.buffer.write(summary.encode("utf-8"))

except Exception as e:
    print(f"[ERROR] Summarization failed: {e}")
    sys.exit(1)
