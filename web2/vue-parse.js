var t, e;
(t =
  "undefined" != typeof self
    ? self
    : "undefined" != typeof window
    ? window
    : "undefined" != typeof global
    ? global
    : this),
  (e = function(e) {
    "use strict";
    var n,
      r = e.Base64;
    if ("undefined" != typeof module && module.exports)
      try {
        n = require("buffer").Buffer;
      } catch (k) {}
    function i(t) {
      if (t.length < 2)
        return (e = t.charCodeAt(0)) < 128
          ? t
          : e < 2048
          ? f(192 | (e >>> 6)) + f(128 | (63 & e))
          : f(224 | ((e >>> 12) & 15)) +
            f(128 | ((e >>> 6) & 63)) +
            f(128 | (63 & e));
      var e =
        65536 + 1024 * (t.charCodeAt(0) - 55296) + (t.charCodeAt(1) - 56320);
      return (
        f(240 | ((e >>> 18) & 7)) +
        f(128 | ((e >>> 12) & 63)) +
        f(128 | ((e >>> 6) & 63)) +
        f(128 | (63 & e))
      );
    }
    function o(t) {
      return t.replace(g, i);
    }
    function a(t) {
      var e = [0, 2, 1][t.length % 3],
        n =
          (t.charCodeAt(0) << 16) |
          ((1 < t.length ? t.charCodeAt(1) : 0) << 8) |
          (2 < t.length ? t.charCodeAt(2) : 0);
      return [
        d.charAt(n >>> 18),
        d.charAt((n >>> 12) & 63),
        2 <= e ? "=" : d.charAt((n >>> 6) & 63),
        1 <= e ? "=" : d.charAt(63 & n),
      ].join("");
    }
    function s(t, e) {
      return e
        ? m(String(t))
            .replace(/[+\/]/g, function(t) {
              return "+" == t ? "-" : "_";
            })
            .replace(/=/g, "")
        : m(String(t));
    }
    function u(t) {
      switch (t.length) {
        case 4:
          var e =
            (((7 & t.charCodeAt(0)) << 18) |
              ((63 & t.charCodeAt(1)) << 12) |
              ((63 & t.charCodeAt(2)) << 6) |
              (63 & t.charCodeAt(3))) -
            65536;
          return f(55296 + (e >>> 10)) + f(56320 + (1023 & e));
        case 3:
          return f(
            ((15 & t.charCodeAt(0)) << 12) |
              ((63 & t.charCodeAt(1)) << 6) |
              (63 & t.charCodeAt(2))
          );
        default:
          return f(((31 & t.charCodeAt(0)) << 6) | (63 & t.charCodeAt(1)));
      }
    }
    function c(t) {
      return t.replace(b, u);
    }
    function l(t) {
      var e = t.length,
        n = e % 4,
        r =
          (0 < e ? h[t.charAt(0)] << 18 : 0) |
          (1 < e ? h[t.charAt(1)] << 12 : 0) |
          (2 < e ? h[t.charAt(2)] << 6 : 0) |
          (3 < e ? h[t.charAt(3)] : 0),
        i = [f(r >>> 16), f((r >>> 8) & 255), f(255 & r)];
      return (i.length -= [0, 0, 2, 1][n]), i.join("");
    }
    function t(t) {
      return v(
        String(t)
          .replace(/[-_]/g, function(t) {
            return "-" == t ? "+" : "/";
          })
          .replace(/[^A-Za-z0-9\+\/]/g, "")
      );
    }
    var d = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
      h = (function(t) {
        for (var e = {}, n = 0, r = t.length; n < r; n++) e[t.charAt(n)] = n;
        return e;
      })(d),
      f = String.fromCharCode,
      g = /[\uD800-\uDBFF][\uDC00-\uDFFFF]|[^\x00-\x7F]/g,
      p = e.btoa
        ? function(t) {
            return e.btoa(t);
          }
        : function(t) {
            return t.replace(/[\s\S]{1,3}/g, a);
          },
      m = n
        ? n.from && Uint8Array && n.from !== Uint8Array.from
          ? function(t) {
              return (t.constructor === n.constructor ? t : n.from(t)).toString(
                "base64"
              );
            }
          : function(t) {
              return (t.constructor === n.constructor ? t : new n(t)).toString(
                "base64"
              );
            }
        : function(t) {
            return p(o(t));
          },
      b = new RegExp(
        ["[À-ß][-¿]", "[à-ï][-¿]{2}", "[ð-÷][-¿]{3}"].join("|"),
        "g"
      ),
      w = e.atob
        ? function(t) {
            return e.atob(t);
          }
        : function(t) {
            return t.replace(/[\s\S]{1,4}/g, l);
          },
      v = n
        ? n.from && Uint8Array && n.from !== Uint8Array.from
          ? function(t) {
              return (t.constructor === n.constructor
                ? t
                : n.from(t, "base64")
              ).toString();
            }
          : function(t) {
              return (t.constructor === n.constructor
                ? t
                : new n(t, "base64")
              ).toString();
            }
        : function(t) {
            return c(w(t));
          };
    if (
      ((e.Base64 = {
        VERSION: "2.4.5",
        atob: w,
        btoa: p,
        fromBase64: t,
        toBase64: s,
        utob: o,
        encode: s,
        encodeURI: function(t) {
          return s(t, !0);
        },
        btou: c,
        decode: t,
        noConflict: function() {
          var t = e.Base64;
          return (e.Base64 = r), t;
        },
      }),
      "function" == typeof Object.defineProperty)
    ) {
      var y = function(t) {
        return {
          value: t,
          enumerable: !1,
          writable: !0,
          configurable: !0,
        };
      };
      e.Base64.extendString = function() {
        Object.defineProperty(
          String.prototype,
          "fromBase64",
          y(function() {
            return t(this);
          })
        ),
          Object.defineProperty(
            String.prototype,
            "toBase64",
            y(function(t) {
              return s(this, t);
            })
          ),
          Object.defineProperty(
            String.prototype,
            "toBase64URI",
            y(function() {
              return s(this, !0);
            })
          );
      };
    }
    return (
      e.Meteor && (Base64 = e.Base64),
      "undefined" != typeof module && module.exports
        ? (module.exports.Base64 = e.Base64)
        : "function" == typeof define &&
          define.amd &&
          define([], function() {
            return e.Base64;
          }),
      {
        Base64: e.Base64,
      }
    );
  }),
  "object" == typeof exports && "undefined" != typeof module
    ? (module.exports = e(t))
    : "function" == typeof define && define.amd
    ? define(e)
    : e(t),
  (function(t) {
    "use strict";
    function d(t, e) {
      var n = (65535 & t) + (65535 & e);
      return (((t >> 16) + (e >> 16) + (n >> 16)) << 16) | (65535 & n);
    }
    function s(t, e, n, r, i, o) {
      return d(
        (function a(t, e) {
          return (t << e) | (t >>> (32 - e));
        })(d(d(e, t), d(r, o)), i),
        n
      );
    }
    function h(t, e, n, r, i, o, a) {
      return s((e & n) | (~e & r), t, e, i, o, a);
    }
    function f(t, e, n, r, i, o, a) {
      return s((e & r) | (n & ~r), t, e, i, o, a);
    }
    function g(t, e, n, r, i, o, a) {
      return s(e ^ n ^ r, t, e, i, o, a);
    }
    function p(t, e, n, r, i, o, a) {
      return s(n ^ (e | ~r), t, e, i, o, a);
    }
    function u(t, e) {
      (t[e >> 5] |= 128 << e % 32), (t[14 + (((e + 64) >>> 9) << 4)] = e);
      var n,
        r,
        i,
        o,
        a,
        s = 1732584193,
        u = -271733879,
        c = -1732584194,
        l = 271733878;
      for (n = 0; n < t.length; n += 16)
        (u = p(
          (u = p(
            (u = p(
              (u = p(
                (u = g(
                  (u = g(
                    (u = g(
                      (u = g(
                        (u = f(
                          (u = f(
                            (u = f(
                              (u = f(
                                (u = h(
                                  (u = h(
                                    (u = h(
                                      (u = h(
                                        (i = u),
                                        (c = h(
                                          (o = c),
                                          (l = h(
                                            (a = l),
                                            (s = h(
                                              (r = s),
                                              u,
                                              c,
                                              l,
                                              t[n],
                                              7,
                                              -680876936
                                            )),
                                            u,
                                            c,
                                            t[n + 1],
                                            12,
                                            -389564586
                                          )),
                                          s,
                                          u,
                                          t[n + 2],
                                          17,
                                          606105819
                                        )),
                                        l,
                                        s,
                                        t[n + 3],
                                        22,
                                        -1044525330
                                      )),
                                      (c = h(
                                        c,
                                        (l = h(
                                          l,
                                          (s = h(
                                            s,
                                            u,
                                            c,
                                            l,
                                            t[n + 4],
                                            7,
                                            -176418897
                                          )),
                                          u,
                                          c,
                                          t[n + 5],
                                          12,
                                          1200080426
                                        )),
                                        s,
                                        u,
                                        t[n + 6],
                                        17,
                                        -1473231341
                                      )),
                                      l,
                                      s,
                                      t[n + 7],
                                      22,
                                      -45705983
                                    )),
                                    (c = h(
                                      c,
                                      (l = h(
                                        l,
                                        (s = h(
                                          s,
                                          u,
                                          c,
                                          l,
                                          t[n + 8],
                                          7,
                                          1770035416
                                        )),
                                        u,
                                        c,
                                        t[n + 9],
                                        12,
                                        -1958414417
                                      )),
                                      s,
                                      u,
                                      t[n + 10],
                                      17,
                                      -42063
                                    )),
                                    l,
                                    s,
                                    t[n + 11],
                                    22,
                                    -1990404162
                                  )),
                                  (c = h(
                                    c,
                                    (l = h(
                                      l,
                                      (s = h(
                                        s,
                                        u,
                                        c,
                                        l,
                                        t[n + 12],
                                        7,
                                        1804603682
                                      )),
                                      u,
                                      c,
                                      t[n + 13],
                                      12,
                                      -40341101
                                    )),
                                    s,
                                    u,
                                    t[n + 14],
                                    17,
                                    -1502002290
                                  )),
                                  l,
                                  s,
                                  t[n + 15],
                                  22,
                                  1236535329
                                )),
                                (c = f(
                                  c,
                                  (l = f(
                                    l,
                                    (s = f(
                                      s,
                                      u,
                                      c,
                                      l,
                                      t[n + 1],
                                      5,
                                      -165796510
                                    )),
                                    u,
                                    c,
                                    t[n + 6],
                                    9,
                                    -1069501632
                                  )),
                                  s,
                                  u,
                                  t[n + 11],
                                  14,
                                  643717713
                                )),
                                l,
                                s,
                                t[n],
                                20,
                                -373897302
                              )),
                              (c = f(
                                c,
                                (l = f(
                                  l,
                                  (s = f(s, u, c, l, t[n + 5], 5, -701558691)),
                                  u,
                                  c,
                                  t[n + 10],
                                  9,
                                  38016083
                                )),
                                s,
                                u,
                                t[n + 15],
                                14,
                                -660478335
                              )),
                              l,
                              s,
                              t[n + 4],
                              20,
                              -405537848
                            )),
                            (c = f(
                              c,
                              (l = f(
                                l,
                                (s = f(s, u, c, l, t[n + 9], 5, 568446438)),
                                u,
                                c,
                                t[n + 14],
                                9,
                                -1019803690
                              )),
                              s,
                              u,
                              t[n + 3],
                              14,
                              -187363961
                            )),
                            l,
                            s,
                            t[n + 8],
                            20,
                            1163531501
                          )),
                          (c = f(
                            c,
                            (l = f(
                              l,
                              (s = f(s, u, c, l, t[n + 13], 5, -1444681467)),
                              u,
                              c,
                              t[n + 2],
                              9,
                              -51403784
                            )),
                            s,
                            u,
                            t[n + 7],
                            14,
                            1735328473
                          )),
                          l,
                          s,
                          t[n + 12],
                          20,
                          -1926607734
                        )),
                        (c = g(
                          c,
                          (l = g(
                            l,
                            (s = g(s, u, c, l, t[n + 5], 4, -378558)),
                            u,
                            c,
                            t[n + 8],
                            11,
                            -2022574463
                          )),
                          s,
                          u,
                          t[n + 11],
                          16,
                          1839030562
                        )),
                        l,
                        s,
                        t[n + 14],
                        23,
                        -35309556
                      )),
                      (c = g(
                        c,
                        (l = g(
                          l,
                          (s = g(s, u, c, l, t[n + 1], 4, -1530992060)),
                          u,
                          c,
                          t[n + 4],
                          11,
                          1272893353
                        )),
                        s,
                        u,
                        t[n + 7],
                        16,
                        -155497632
                      )),
                      l,
                      s,
                      t[n + 10],
                      23,
                      -1094730640
                    )),
                    (c = g(
                      c,
                      (l = g(
                        l,
                        (s = g(s, u, c, l, t[n + 13], 4, 681279174)),
                        u,
                        c,
                        t[n],
                        11,
                        -358537222
                      )),
                      s,
                      u,
                      t[n + 3],
                      16,
                      -722521979
                    )),
                    l,
                    s,
                    t[n + 6],
                    23,
                    76029189
                  )),
                  (c = g(
                    c,
                    (l = g(
                      l,
                      (s = g(s, u, c, l, t[n + 9], 4, -640364487)),
                      u,
                      c,
                      t[n + 12],
                      11,
                      -421815835
                    )),
                    s,
                    u,
                    t[n + 15],
                    16,
                    530742520
                  )),
                  l,
                  s,
                  t[n + 2],
                  23,
                  -995338651
                )),
                (c = p(
                  c,
                  (l = p(
                    l,
                    (s = p(s, u, c, l, t[n], 6, -198630844)),
                    u,
                    c,
                    t[n + 7],
                    10,
                    1126891415
                  )),
                  s,
                  u,
                  t[n + 14],
                  15,
                  -1416354905
                )),
                l,
                s,
                t[n + 5],
                21,
                -57434055
              )),
              (c = p(
                c,
                (l = p(
                  l,
                  (s = p(s, u, c, l, t[n + 12], 6, 1700485571)),
                  u,
                  c,
                  t[n + 3],
                  10,
                  -1894986606
                )),
                s,
                u,
                t[n + 10],
                15,
                -1051523
              )),
              l,
              s,
              t[n + 1],
              21,
              -2054922799
            )),
            (c = p(
              c,
              (l = p(
                l,
                (s = p(s, u, c, l, t[n + 8], 6, 1873313359)),
                u,
                c,
                t[n + 15],
                10,
                -30611744
              )),
              s,
              u,
              t[n + 6],
              15,
              -1560198380
            )),
            l,
            s,
            t[n + 13],
            21,
            1309151649
          )),
          (c = p(
            c,
            (l = p(
              l,
              (s = p(s, u, c, l, t[n + 4], 6, -145523070)),
              u,
              c,
              t[n + 11],
              10,
              -1120210379
            )),
            s,
            u,
            t[n + 2],
            15,
            718787259
          )),
          l,
          s,
          t[n + 9],
          21,
          -343485551
        )),
          (s = d(s, r)),
          (u = d(u, i)),
          (c = d(c, o)),
          (l = d(l, a));
      return [s, u, c, l];
    }
    function c(t) {
      var e,
        n = "",
        r = 32 * t.length;
      for (e = 0; e < r; e += 8)
        n += String.fromCharCode((t[e >> 5] >>> e % 32) & 255);
      return n;
    }
    function l(t) {
      var e,
        n = [];
      for (n[(t.length >> 2) - 1] = void 0, e = 0; e < n.length; e += 1)
        n[e] = 0;
      var r = 8 * t.length;
      for (e = 0; e < r; e += 8)
        n[e >> 5] |= (255 & t.charCodeAt(e / 8)) << e % 32;
      return n;
    }
    function o(t) {
      var e,
        n,
        r = "";
      for (n = 0; n < t.length; n += 1)
        (e = t.charCodeAt(n)),
          (r +=
            "0123456789abcdef".charAt((e >>> 2) & 15) +
            "0123456789abcdef".charAt(15 & e));
      return r;
    }
    function n(t) {
      return unescape(encodeURIComponent(t));
    }
    function a(t) {
      return (function e(t) {
        return c(u(l(t), 8 * t.length));
      })(n(t));
    }
    function m(t, e) {
      return (function s(t, e) {
        var n,
          r,
          i = l(t),
          o = [],
          a = [];
        for (
          o[15] = a[15] = void 0,
            16 < i.length && (i = u(i, 8 * t.length)),
            n = 0;
          n < 16;
          n += 1
        )
          (o[n] = 909522486 ^ i[n]), (a[n] = 1549556828 ^ i[n]);
        return (
          (r = u(o.concat(l(e)), 512 + 8 * e.length)), c(u(a.concat(r), 640))
        );
      })(n(t), n(e));
    }
    function e(t, e, n) {
      return e
        ? n
          ? m(e, t)
          : (function r(t, e) {
              return o(m(t, e));
            })(e, t)
        : n
        ? a(t)
        : (function i(t) {
            return o(a(t));
          })(t);
    }
    "function" == typeof define && define.amd
      ? define(function() {
          return e;
        })
      : "object" == typeof module && module.exports
      ? (module.exports = e)
      : (t.md5 = e);
  })(this),
  "undefined" != typeof Storage &&
    ((Storage.prototype.setObject = function(t, e) {
      this.setItem(t, JSON.stringify(e));
    }),
    (Storage.prototype.getObject = function(t) {
      var e = this.getItem(t);
      return e && JSON.parse(e);
    })),
  (Array.prototype.contains = function(t) {
    for (var e = this.length; e--; ) if (this[e] === t) return !0;
    return !1;
  }),
  (jQuery.support.cors = !0);
var o = "X-Client-Data",
  n = ".iiilab.com",
  a = location.protocol + "//service0" + n,
  s = {
    miaopai: "weibo",
    xiaokaxiu: "weibo",
    yixia: "weibo",
    weibo: "weibo",
    weico: "weibo",
    meipai: "meipai",
    xiaoying: "xiaoying",
    vivavideo: "xiaoying",
    immomo: "momo",
    momocdn: "momo",
    inke: "inke",
    163: "yunyinyue",
    "weishi.qq": "weishi",
    "qzone.qq": "weishi",
    "kg4.qq": "kg",
    "kg3.qq": "kg",
    "kg2.qq": "kg",
    "kg1.qq": "kg",
    "kg.qq": "kg",
    facebook: "facebook",
    fb: "facebook",
    youtube: "youtube",
    youtu: "youtube",
    vimeo: "vimeo",
    twitter: "twitter",
    instagram: "instagram",
    hao222: "quanmin",
    "haokan.baidu": "quanmin",
    quduopai: "quduopai",
    "3qtt": "quduopai",
    bilibili: "bilibili",
    b23: "bilibili",
    pearvideo: "pearvideo",
    tumblr: "tumblr",
    luisonte: "tumblr",
    acfun: "acfun",
    izuiyou: "zuiyou",
  };
function u(t, e) {
  if (
    !0 === window.navigator.webdriver ||
    window.document.documentElement.getAttribute("webdriver") ||
    window.callPhantom ||
    window._phantom
  )
    return md5(o + t + o);
  var n = e.charAt(t.charCodeAt(0) % e.length),
    r = e.charAt(t.charCodeAt(t.length - 1) % e.length);
  return md5(n + t + r);
}
function c(t) {
  return t + n;
}
function l(t) {
  var e = document.createElement("a");
  e.href = t;
  var n = e.hostname.split(".");
  return n.length < 2 ? "" : n[n.length - 2];
}
function i(t) {
  return (
    "MP4" ===
    (function n(t) {
      var e = document.createElement("a");
      return (e.href = t), e.pathname.split(".").pop();
    })(t).toUpperCase()
  );
}
function d(t, e) {
  return -1 !== t.indexOf(e);
}

new Vue({
  el: "#app",
  data: {},

  methods: {
    unShortUrlAndParseVideo: function() {
      this.submitBtnClass.disabled = !0;
      var e = this,
        t = Math.random()
          .toString(10)
          .substring(2),
        n = this.generateStr(this.link + "@" + t).toString(10);
      $.ajax({
        type: "POST",
        beforeSend: function(t) {
          t.setRequestHeader(o, u(n, "unshort"));
        },
        url: a + "/url/short/unshort",
        xhrFields: {
          withCredentials: !0,
        },
        crossDomain: !0,
        data: {
          link: e.link,
          r: t,
          s: n,
        },
        dataType: "json",
        success: function(t) {
          t.succ
            ? ((e.link = t.data), e.parseVideo())
            : ((e.submitBtnClass.disabled = !1), (e.errorTip = t.retDesc));
        },
        error: function() {
          (e.submitBtnClass.disabled = !1), (e.errorTip = "处理失败,请重试!");
        },
      });
    },
    parseVideo: function() {
      var t = l(this.link);
      if (
        (s.hasOwnProperty(t) ||
          ((t = (function i(t) {
            var e = document.createElement("a");
            e.href = t;
            var n = e.hostname.split(".");
            return n.length < 3 ? "" : n[n.length - 3] + "." + n[n.length - 2];
          })(this.link)),
          s.hasOwnProperty(t) ||
            ((d(this.link, "huoshan") ||
              d(this.link, "hypstar") ||
              d(this.link, "hotsoon") ||
              d(this.link, "/share/item/")) &&
              (t = "huoshan"))),
        s.hasOwnProperty(t) && c(s[t]) !== location.hostname)
      )
        this.redirect(c(s[t]));
      else {
        "undefined" != typeof ga &&
          ga("send", "event", "analysis", "analysis-normal"),
          (this.submitBtnClass.disabled = !0);
        var r = this,
          e = Math.random()
            .toString(10)
            .substring(2),
          n = this.generateStr(this.link + "@" + e).toString(10);
        $.ajax({
          type: "POST",
          beforeSend: function(t) {
            t.setRequestHeader(
              o,
              (function e(t) {
                return u(t, site);
              })(n)
            );
          },
          url: a + "/video/web/" + site,
          xhrFields: {
            withCredentials: !0,
          },
          crossDomain: !0,
          data: {
            link: r.link,
            r: e,
            s: n,
          },
          dataType: "json",
          success: function(t) {
            if (((r.submitBtnClass.disabled = !1), t.succ))
              (r.requestResult = t.data),
                (r.requestSuccess = !0),
                r.cacheResult();
            else {
              if (300 === t.retCode && !r.linkFromInit)
                return void r.redirect(location.hostname);
              r.errorTip = t.retDesc;
            }
            r.linkFromInit = !1;
          },
          error: function(t, e, n) {},
        });
      }
    },
    generateStr: function(t) {
      var a = (function() {
        for (var t = 0, e = new Array(256), n = 0; 256 != n; ++n)
          (t =
            1 &
            (t =
              1 &
              (t =
                1 &
                (t =
                  1 &
                  (t =
                    1 &
                    (t =
                      1 &
                      (t =
                        1 & (t = 1 & (t = n) ? -306674912 ^ (t >>> 1) : t >>> 1)
                          ? -306674912 ^ (t >>> 1)
                          : t >>> 1)
                        ? -306674912 ^ (t >>> 1)
                        : t >>> 1)
                      ? -306674912 ^ (t >>> 1)
                      : t >>> 1)
                    ? -306674912 ^ (t >>> 1)
                    : t >>> 1)
                  ? -306674912 ^ (t >>> 1)
                  : t >>> 1)
                ? -306674912 ^ (t >>> 1)
                : t >>> 1)
              ? -306674912 ^ (t >>> 1)
              : t >>> 1),
            (e[n] = t);
        return "undefined" != typeof Int32Array ? new Int32Array(e) : e;
      })();
      return (
        (function(t) {
          for (var e, n, r = -1, i = 0, o = t.length; i < o; )
            r =
              (e = t.charCodeAt(i++)) < 128
                ? (r >>> 8) ^ a[255 & (r ^ e)]
                : e < 2048
                ? ((r = (r >>> 8) ^ a[255 & (r ^ (192 | ((e >> 6) & 31)))]) >>>
                    8) ^
                  a[255 & (r ^ (128 | (63 & e)))]
                : 55296 <= e && e < 57344
                ? ((e = 64 + (1023 & e)),
                  (n = 1023 & t.charCodeAt(i++)),
                  ((r =
                    ((r =
                      ((r =
                        (r >>> 8) ^ a[255 & (r ^ (240 | ((e >> 8) & 7)))]) >>>
                        8) ^
                      a[255 & (r ^ (128 | ((e >> 2) & 63)))]) >>>
                      8) ^
                    a[255 & (r ^ (128 | ((n >> 6) & 15) | ((3 & e) << 4)))]) >>>
                    8) ^
                    a[255 & (r ^ (128 | (63 & n)))])
                : ((r =
                    ((r =
                      (r >>> 8) ^ a[255 & (r ^ (224 | ((e >> 12) & 15)))]) >>>
                      8) ^
                    a[255 & (r ^ (128 | ((e >> 6) & 63)))]) >>>
                    8) ^
                  a[255 & (r ^ (128 | (63 & e)))];
          return -1 ^ r;
        })(t) >>> 0
      );
    },
  },
});
