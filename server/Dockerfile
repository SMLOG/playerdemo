FROM ubuntu
RUN apt-get update && apt-get -y install python3-pip ffmpeg

RUN apt-get update && apt-get install -y \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*
	
ENV appdir "/opt/app"
# Mount volumes
VOLUME [${appdir}]

# Add and install app
ADD . ${appdir}/
WORKDIR ${appdir}
RUN pip3 install --no-cache-dir -r requirements.txt
RUN python3 setup.py install


# Default command
#ENV url ""
#CMD ["sh", "-c", "PYTHONIOENCODING=utf-8 python3 /${appdir}/youtube_mp3/main.py ${url}"]

# Copy the package.json and package-lock.json files
COPY package*.json ./

# Install the dependencies
RUN npm ci

# Copy the application code
COPY . .

# Build the application (if necessary)
#RUN npm run build

# Expose the port that the Express.js server will run on
EXPOSE 3000

# Start the Express.js server
CMD ["npm","run", "dev"]
