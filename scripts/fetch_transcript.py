import sys
import os
from youtube_transcript_api import YouTubeTranscriptApi, TranscriptsDisabled, NoTranscriptFound
import whisper
from urllib.parse import urlparse, parse_qs

def extract_video_id(url):
    query = urlparse(url)
    if query.hostname == 'youtu.be':
        return query.path[1:]
    if query.hostname in ('www.youtube.com', 'youtube.com'):
        if query.path == '/watch':
            return parse_qs(query.query).get('v', [None])[0]
    return None

def transcribe_with_whisper(audio_path):
    print("[INFO] No transcript available. Falling back to Whisper...")
    model = whisper.load_model("tiny")
    result = model.transcribe(audio_path)
    return result["text"]

if len(sys.argv) != 4:
    print("Usage: python fetch_transcript.py <video_url> <audio_path> <output_path>")
    sys.exit(1)

video_url = sys.argv[1]
audio_path = sys.argv[2]
output_path = sys.argv[3]

video_id = extract_video_id(video_url)
if not video_id:
    print("[ERROR] Could not extract video ID.")
    sys.exit(1)

try:
    transcript = YouTubeTranscriptApi.get_transcript(video_id)
    text = "\n".join([entry['text'] for entry in transcript])
    print("[INFO] Transcript fetched from YouTube.")
except (TranscriptsDisabled, NoTranscriptFound):
    text = transcribe_with_whisper(audio_path)
except Exception as e:
    print(f"[ERROR] Failed to fetch transcript: {e}")
    sys.exit(1)

os.makedirs(os.path.dirname(output_path), exist_ok=True)
with open(output_path, "w", encoding="utf-8") as f:
    for line in text.split('. '):
        f.write(line.strip() + '.\n')

print(f"[INFO] Transcript saved to {output_path}")
