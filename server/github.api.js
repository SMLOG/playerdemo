let token;

function utf8_to_b64(str) {
  return btoa(unescape(encodeURIComponent(str)));
}

function b64_to_utf8(base64) {
  return decodeURIComponent(escape(atob(base64)));

  // return atob(str);
}

exports.updateRepFile = async function (name, rep, path, content, gzip) {
  var auth = "token " + token;
  var url = `https://api.github.com/repos/${name}/${rep}/contents/${path}`;

  if (!token)
    return {
      error: "token?",
    };
  let json = await fetch(url, {
    method: "get",
    headers: {
      accept: "application/vnd.github.v3+json",
    },
  }).then((r) => r.json());
  let sha = json.sha;
  json = await fetch(url, {
    method: "put",
    body: JSON.stringify({
      message: "update" + new Date(),
      branch: "main",
      content: utf8_to_b64(content),
      sha: sha,
    }),
    headers: {
      accept: "application/vnd.github.v3+json",
      Authorization: auth,
    },
  }).then((response) => {
    return response.json();
  });

  return json;
}
exports.getBlobContent = async function (name, rep, path, sha, cacheTime) {
  var auth = "token " + token;

  let headers = {
    accept: "application/vnd.github.v3+json",
  };
  if (token) headers["Authorization"] = auth;
  let url =
    `https://api.github.com/repos/${name}/${rep}/contents/${path}` +
    (sha ? `?ref=${sha}` : "");

  url += cacheTime
    ? (url.indexOf("?") > -1 ? "&" : "?") + `cache=${cacheTime}`
    : "";
  let json = await fetchRequest(
    url,
    {
      method: "get",
      headers: headers,
      // cache: "force-cache",
    },
    10000
  ).then((r) => r.json());

  return json.content;
}
 async function fetchRequest(url, params = {}, timeout = 10000) {
  let isTimeout = false;

  return new Promise(function (resolve, reject) {
    const TO = setTimeout(function () {
      isTimeout = true;
      reject(new Error("Fetch timeout"));
    }, timeout);

    fetch(url, params)
      .then((res) => {
        clearTimeout(TO);
        if (!isTimeout) {
          resolve(res);
        }
      })
      .catch((e) => {
        if (isTimeout) {
          return;
        }
        reject(e);
      });
  });
}