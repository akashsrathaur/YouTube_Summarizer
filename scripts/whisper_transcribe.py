import sys
import os
import whisper

if len(sys.argv) != 3:
    print("Usage: python whisper_transcribe.py <audio_path> <output_path>")
    sys.exit(1)

audio_path = sys.argv[1]
output_path = sys.argv[2]

try:
    model = whisper.load_model("tiny")  # Faster inference
    result = model.transcribe(audio_path)

    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        for sentence in result["text"].split('. '):
            f.write(sentence.strip() + '.\n')
    print(f"[INFO] Transcript saved to {output_path}")
except Exception as e:
    print(f"[ERROR] Transcription failed: {e}")
    sys.exit(1)
