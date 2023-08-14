import axiosa from "axios";

const httpclient = axiosa.create({
  timeout: 5000,
  retry: 5,
  retryDelay: 3000,
});
httpclient.defaults.retry = 5;
httpclient.defaults.retryDelay = 3000;
httpclient.defaults.timeout = 5000;

httpclient.interceptors.request.use((config) => {
  if (!config.headers["User-Agent"])
    config.headers = {
      "User-Agent": 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36',
    };
  return config;
});

httpclient.interceptors.request.use(function (config) {
  const CancelToken = axiosa.CancelToken;
  const source = CancelToken.source();
  let token = setTimeout(
    () => source.cancel({ message: "Timeout", config: config }),
    config.timeout
  );
  config.cancelToken = source.token;
  config.clearCancelToken = () => clearTimeout(token);
  return config;
});

function axiosRetryInterceptor2(err) {
  var config = err.config;
  console.error(err);
  // If config does not exist or the retry option is not set, reject
  if (!config || !config.retry) return Promise.reject(err);

  // Set the variable for keeping track of the retry count
  config.__retryCount = config.__retryCount || 0;

  // Check if we've maxed out the total number of retries
  if (config.__retryCount >= config.retry) {
    // Reject with the error
    return Promise.reject(err);
  }

  // Increase the retry count
  config.__retryCount += 1;

  // Create new promise to handle exponential backoff
  var backoff = new Promise(function (resolve) {
    setTimeout(function () {
      resolve();
    }, config.retryDelay || 1);
  });

  // Return the promise in which recalls axios to retry the request
  return backoff.then(function () {
    return httpclient(config);
  });
}

function axiosRetryInterceptor(err) {
  var message, config;
  console.error(err);
  if (axiosa.isCancel(err)) {
    message = err.message.message;
    config = err.message.config;
  } else {
    message = err.message;
    config = err.config;
  }
  config.clearCancelToken();
  // If config does not exist or the retry option is not set, reject
  if (!config || !config.retry) return Promise.reject(new Error(message));
  // Set the variable for keeping track of the retry count
  config.__retryCount = config.__retryCount || 0;
  // Check if we've maxed out the total number of retries
  if (config.__retryCount >= config.retry) {
    // Reject with the error
    return Promise.reject(new Error(message));
  }
  // Increase the retry count
  config.__retryCount += 1;
  // Create new promise to handle exponential backoff
  var backoff = new Promise(function (resolve) {
    setTimeout(function () {
      resolve();
    }, config.retryDelay || 1);
  });
  // Return the promise in which recalls axios to retry the request
  return backoff.then(function () {
    console.log(`请求失败，重试中：${config.url}`);
    return httpclient(config);
  });
}

httpclient.interceptors.response.use(function (response) {
  response.config.clearCancelToken();
  return response;
}, axiosRetryInterceptor);

export { httpclient as axios };
