const webpack = require("webpack");
module.exports = {
  configureWebpack: {
    //支持jquery
    plugins: [
      new webpack.ProvidePlugin({
        $: "jquery",
        jQuery: "jquery",
        "windows.jQuery": "jquery",
        Popper: ["popper.js", "default"],
      }),
    ],
  },
  devServer: {
    https: false,
    hotOnly: false,
    port: 8989,
    proxy: {
      "/api2": {
        target: "http://localhost:8081",
        changeOrigin: true,
        pathRewrite: {
          "^/api2": "/api2",
        },
      },
      "/api3": {
        target: "http://localhost:8081",
        changeOrigin: true,
        pathRewrite: {
          "^/api3": "/api3",
        },
      },

      "/api": {
        target: "http://192.168.0.102:9191",
        changeOrigin: true,
        pathRewrite: {
          "^/api": "/api",
        },
        logLevel: "debug",
      },
    },
  },
};
