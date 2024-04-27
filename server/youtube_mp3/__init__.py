# Main file containing the code for youtube_mp3
import youtube_mp3.settings as settings
import youtube_mp3.utils as utils
from yt_dlp import YoutubeDL
import os
import glob
import json


class YouTubeMP3:
    """Class to download YouTube videos and extract audio into MP3 files.
    
    Args:
        output_dir (str): Path to the output directory
        archive_file (str): Path to the download archive text file
    """
    def __init__(self, output_dir=settings.OUTPUT_DIR, archive_file=settings.ARCHIVE_FILE, *args, **kwargs):
        self.options = settings.get_youtube_dl_opts(output_dir=output_dir, archive_file=archive_file)
        self.output_dir = output_dir
        self.archive_file = archive_file

    def download(self, url, rename_files=True):
        """Download and extract the audio from the YouTube video URL"""
        self._download(url)
        data_list = self.parse_info()
        new_data_list = self.update_mp3_files(data_list, rename_files)
        return new_data_list

    def _download(self, url):
        """Uses yt-dlp to do the heavy lifting of downloading and extracting audio using FFMPEG"""
        with YoutubeDL(self.options) as ytdl:
            ytdl.download([url])

    def parse_info(self, directory="", cleanup=True):
        """Reads all the info.json files created by Youtube-DL when downloading and parses out the song-related data.

        Args:
            directory (str): Path to the directory where the .info.json files are located. By default, will look
                        in the path stored in self.output
            cleanup (bool): Specifies whether to delete the .info.json files after parsing
        
        Returns:
            data_list (list): A list of dict for each song parsed
        """
        if not directory:
            directory = self.output_dir
        json_pattern = os.path.join(directory, '*.info.json')
        file_list = glob.glob(json_pattern)
        data_list = []
        for filename in file_list:
            with open(filename, 'r') as file_handle:
                data = file_handle.read()
                json_data = json.loads(data)
                yt_title = json_data.get('fulltitle', '')                
                yt_id = json_data.get('id', '')
                yt_thumb = json_data.get('thumbnail', '')
                yt_url = json_data.get('webpage_url', '')
                yt_album = json_data.get('album', '')
                if yt_id and yt_title:
                    # Try to parse artist and song title from video title
                    parts = yt_title.split('-')
                    if len(parts) == 2:
                        artist = parts[0]
                        title = parts[1]
                    else:
                        # Fallback to using the full title for both title and artist fields
                        artist = yt_title
                        title = yt_title
                else:
                    # Something went wrong, skip and continue with loop
                    continue
                # Add the data to the list
                data_list.append({
                    'id': yt_id,
                    'title': title,
                    'artist': artist,
                    'thumb_url': yt_thumb,
                    'url': yt_url,
                    'album': yt_album,
                })
                # Remove the info.json file
                if cleanup:
                    os.remove(filename)
        # Return the results
        return data_list

    def update_mp3_files(self, data_list, rename_files=True):
        """Loops through the data list returned by parse_info() and updates the MP3 files' 
        metadata and renames them from YouTube ID to song title.
        
        Args:
            data_list (list): List of dict containing the song metadata. 
        
        Returns:
            data_list (list): Updated list of dict
        """
        for data in data_list:
            try:
                mp3_file_path = "%s/%s.mp3" % (self.output_dir, data['id'])
                data['file_path'] = mp3_file_path
                utils.add_metadata_to_mp3(mp3_file_path, data)
                if rename_files:
                    # Rename the file
                    new_file_path = utils.rename_mp3(file_path=self.output_dir, old_name=data['id'], new_name=data['title'])
                    data['file_path'] = new_file_path
                data['downloaded'] = True
            except Exception as exc:
                print("Failed to update MP3 file for %s. Error: %s" % (str(data), str(exc)))
        return data_list
