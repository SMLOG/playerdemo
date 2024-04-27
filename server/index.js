const express = require("express");
const fs = require("fs");
const { exec, execSync } = require("child_process");
//const githubapi = require('./github.api');
const app = express();
const port = process.env.PORT || 3000;

app.use(express.json()); // Parse JSON request bodies
app.use(express.urlencoded({ extended: true })); // Parse URL-encoded request bodies

// Routes
app.get("/", (req, res) => {
  //res.send('Hello, World!');
  const { url } = req.query;
  const appdir = process.cwd();

  appdir &&
    exec(
      `PYTHONIOENCODING=utf-8 python3 ${appdir}/youtube_mp3/main.py ${url}`,
      (error, stdout, stderr) => {
        if (error) {
          console.error(`exec error: ${error}`);
          return;
        }
        console.log(`stdout: ${stdout}`);
        console.error(`stderr: ${stderr}`);
        res.send(url);
      }
    );
});

const downloadMap = {};
app.get("/v", (req, res) => {
  const { typeId, bvid, p } = req.query;
  const appdir = process.cwd();
  let watchurl = "https://www.youtube.com/watch?v=" + bvid;
  console.log(watchurl, req.query);
  let filePath = appdir + "/" + bvid + ".mp4";

  if (!fs.existsSync(filePath)) {


    if (!downloadMap[bvid]) {
      try {
        downloadMap[bvid] = 1;
//yt-dlp -f best https://www.youtube.com/watch?v=UQyHkp8-B6E
		exec(
			`yt-dlp -f bestvideo[ext=webm]+bestaudio[ext=m4a]/best[ext=mp4] --merge-output-format mp4 -o "%(id)s" ${watchurl}`,
			(error, stdout, stderr) => {

			  console.log(`stdout: ${stdout}`,error);
			  console.error(`stderr: ${stderr}`);
			  delete downloadMap[bvid];

			}
		  );

      } catch (error) {}
    }

	try {
		console.log(filePath);
		setTimeout(() => {
		  console.log("send out retry for "+bvid);
		  res.redirect("/v?bvid=" + bvid + "&t=" + new Date().getTime()) +
			"&type=.mp4";
		}, 5000);
	  } catch (error) {
		console.error(error);
	  }

  } else if (fs.existsSync(filePath)) {
    res.sendFile(filePath, (err) => {
      if (err) {
        console.error("Error sendFile streaming file:", err);
        //res.status(500).send('Error streaming file');
      }
    });
  }
});

const axios = require("axios");
const cheerio = require("cheerio");

app.get("/phonetics", (req, res) => {
  //res.send('Hello, World!');
  const { q } = req.query;
  axios
    .get(
      `https://dictionary.cambridge.org/dictionary/english-chinese-traditional/` +
        encodeURIComponent(q),
      {
        headers: {
          Accept:
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
          "User-Agent":
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
        },
      }
    )
    .then((response) => response.data)
    .then((data) => {
      console.log(data);
      const $ = cheerio.load(data);

      res.json({
        q: q,
        uk: $(".uk .ipa").eq(0).text(), //.replaceAll(/'/,'')
        us: $(".us .ipa").eq(0).text(), //.replaceAll(/'/,'')
        trans: $(".trans.dtrans.dtrans-se.break-cj").eq(0).text(), //.replaceAll(/'/,'')
      });
    })
    .catch((error) => {
      console.error(error);
    });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).send("Something went wrong!");
});

app.listen(port, () => {
  console.log(`Server is running on port ${port}`);
});
