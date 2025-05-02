import sys
import os
import subprocess
import re

def get_next_numbered_filename(directory, prefix, extension):
    i = 1
    while os.path.exists(os.path.join(directory, f"{prefix}{i}.{extension}")):
        i += 1
    return os.path.join(directory, f"{prefix}{i}.{extension}")

if len(sys.argv) != 3:
    print("Usage: python download_audio.py <url> <output_dir>")
    sys.exit(1)

url = sys.argv[1]
output_dir = sys.argv[2]
os.makedirs(output_dir, exist_ok=True)

# Generate numbered filename
output_path = get_next_numbered_filename(output_dir, "audio", "mp3")

print(f"[DEBUG] Downloading from: {url}")
print(f"[DEBUG] Saving to: {output_path}")

# Call yt-dlp to download and convert audio
try:
    result = subprocess.run([
        "yt-dlp",
        "-x", "--audio-format", "mp3",
        "-o", output_path,
        url
    ], capture_output=True, text=True)

    if result.returncode != 0:
        print(f"[ERROR] yt-dlp failed:\n{result.stderr}")
        sys.exit(1)

    print(f"[INFO] Audio downloaded to: {output_path}")
except Exception as e:
    print(f"[ERROR] Exception during download: {e}")
    sys.exit(1)
