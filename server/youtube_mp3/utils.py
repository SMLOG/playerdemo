# Collection of utility functions
import os
from mutagen.easyid3 import EasyID3


def add_metadata_to_mp3(mp3_file_path, metadata):
    """Adds the metadata as EasyID3 tags to the MP3 file. Currently only sets the keys: title, artist, album, composer"""
    mp3_file = EasyID3(mp3_file_path)
    # List of valid EasyID3 keys
    key_list = ['title', 'artist', 'album', 'composer']
    for key in key_list:
        if key in metadata and metadata[key]:
            mp3_file[key] = metadata[key]
        # Fallback to using artist as album if available (makes it play nicer with iTunes)
        elif key == 'album' and 'artist' in metadata:
            mp3_file['album'] = metadata['artist']
    mp3_file.save()


def rename_mp3(file_path, old_name, new_name):
    """Utility function to rename a MP3 file. The file name parameters should NOT include `.mp3` extension."""
    old_file = '{path}/{name}.mp3'.format(path=file_path, name=old_name)
    # Remove chars in string that could cause issue with file name
    new_file = '%s/%s.mp3' % (file_path, new_name.replace('/', ' ').replace('(', '').replace(')', ''))
    # Try to encode strings
    old_file = old_file.encode('utf8')
    new_file = new_file.encode('utf8')
    try:
        os.rename(old_file, new_file)
    except (OSError, UnicodeDecodeError, UnicodeEncodeError) as exc:
        raise exc
    return new_file