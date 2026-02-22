import os
import re

try:
    import emoji
except ImportError:
    import subprocess
    import sys
    subprocess.check_call([sys.executable, "-m", "pip", "install", "emoji"])
    import emoji

def remove_emojis(text):
    return emoji.replace_emoji(text, replace='')

def process_directory(directory):
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.md') or file.endswith('.kt'):
                filepath = os.path.join(root, file)
                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()
                    
                    new_content = remove_emojis(content)
                    
                    # There are also some symbols like ️ (U+FE0F) that might be left over
                    new_content = new_content.replace('\ufe0f', '')
                    
                    if new_content != content:
                        with open(filepath, 'w', encoding='utf-8') as f:
                            f.write(new_content)
                        print(f"Cleaned: {filepath}")
                except Exception as e:
                    print(f"Error processing {filepath}: {e}")

process_directory('./docs')
process_directory('./src/main/kotlin')
print("Done removing emojis.")
