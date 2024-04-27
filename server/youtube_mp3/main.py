# coding=utf8

# Main executable for download
import sys, traceback
import pprint
from youtube_mp3 import YouTubeMP3


if __name__ == '__main__':
    if len(sys.argv) != 2:
        raise ValueError("Missing URL argument")
    yt = YouTubeMP3()
    url = sys.argv[1]
    data_list = yt.download(url)
    print(data_list)
    # try:
    #     data_list = yt.download(url)
    #     pprint.pprint(data_list)
    # except:
    #     print("Failed to download %s" % url)
    #     traceback.print_exc(file=sys.stdout)
