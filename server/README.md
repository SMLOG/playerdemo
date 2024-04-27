# README

## youtube-mp3

* Python wrapper around YouTube-DL that extracts audio from YouTube videos as MP3 files
* Version 0.2

## Getting Started

### Getting the Code

Clone this repository:

    git clone https://github.com/ewenchou/youtube-mp3.git

### Configuration

Configure variables in `youtube_mp3/settings.py`:

* OUTPUT_DIR: File path to the directory where you want files to be downloaded
* ARCHIVE_FILE: File path to the text file to use as download archive. This allows YouTube-DL to skip any videos that were previously downloaded already

### Dependencies

* Take a look at the Python dependencies in the `requirements.txt` file.
* The default configuration of YouTube-DL uses __FFMPEG__ to do the audio extraction and conversion to MP3 files.
* See the Docker section below on how to get it up and running with all dependencies in a container.
* Alternately, you can refer to the commands in the Dockerfile for an example of how to setup the dependencies in a Ubuntu environment.

## Installation

### Python

*Note: Skip to the Docker section below if you want to install and run the code in a container (instead of manually setting it up in a Python environment).*

Install the Python module:

        cd youtube-mp3
        pip3 install -r requirements.txt

Run the code:

        mkdir <output-path>
        python3 <path-to-code>/youtube_mp3/main.py "<YouTube URL to download>"

Example:

        mkdir /tmp/youtube-mp3
        python3 /Users/ewenchou/projects/youtube-mp3/youtube_mp3/main.py "https://youtu.be/dQw4w9WgXcQ"

*Note: If the titles contain non-ascii text, prefix the python3 command with* `PYTHONIOENCODING=utf-8`

The output MP3 file(s) will be located in the output directory (default: `/tmp/youtube-mp3`)

### Using Docker

The included `Dockerfile` can help you get setup with a container. 

* Build the Docker image

        docker build --tag <your-github-username>/youtube-mp3 .

* Run the Docker container

        docker run -v <host-output-path>:/tmp/youtube-mp3 -e url="<YouTube URL to download>" <your-github-username>/youtube-mp3

* Example:

        docker build --tag ewenchou/youtube-mp3 .
        docker run -v /Users/ewenchou/Downloads:/tmp/youtube-mp3 -e url="https://youtu.be/dQw4w9WgXcQ" ewenchou/youtube-mp3
