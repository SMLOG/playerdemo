# Default settings for youtube-mp3
import os


# Default output directory where the MP3 files will be saved. 
# Looks for a value in OS environ variable (that can be set in 
# Dockerfile for example)
OUTPUT_DIR = os.environ.get('ytmp3out', '/tmp/youtube-mp3')

# A download archive file can be used to keep track of which 
# videos have previously been downloaded. 
ARCHIVE_FILE = '%s/archive.txt' % OUTPUT_DIR


def get_youtube_dl_opts(output_dir=OUTPUT_DIR, archive_file=ARCHIVE_FILE):
    """Returns a dictionary of options to be passed to YouTube-DL.
    
    Args:
        output_dir (str): Path to the output directory
        archive_file (str): Path to the download archive file to use.
                        If the file does not exist, it will be created.
    
    Returns:
        opts (dict)
    """
    # Options passed to YouTube-DL
    opts = {
        'nocheckcertificate': True,
        'format': 'bestaudio/best',
        'postprocessors': [{
            'key': 'FFmpegExtractAudio',
            'preferredcodec': 'mp3',
            'preferredquality': '192',
        }],
        'ignoreerrors': True,
        'writeinfojson': True,
        # 'format': 'mp4',
        'outtmpl': '{}/%(id)s.%(ext)s'.format(output_dir)
    }
    if archive_file:
        opts['download_archive'] = archive_file
    return opts
