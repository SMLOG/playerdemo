var md5 = (function (t) {
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
                                    (s = f(s, u, c, l, t[n + 1], 5, -165796510)),
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
      for (n[(t.length >> 2) - 1] = void 0, e = 0; e < n.length; e += 1) n[e] = 0;
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
  
    return e;
  })();

function generateStr(t) {
  var a = (function () {
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
    (function (t) {
      for (var e, n, r = -1, i = 0, o = t.length; i < o; )
        r =
          (e = t.charCodeAt(i++)) < 128
            ? (r >>> 8) ^ a[255 & (r ^ e)]
            : e < 2048
            ? ((r = (r >>> 8) ^ a[255 & (r ^ (192 | ((e >> 6) & 31)))]) >>> 8) ^
              a[255 & (r ^ (128 | (63 & e)))]
            : 55296 <= e && e < 57344
            ? ((e = 64 + (1023 & e)),
              (n = 1023 & t.charCodeAt(i++)),
              ((r =
                ((r =
                  ((r = (r >>> 8) ^ a[255 & (r ^ (240 | ((e >> 8) & 7)))]) >>>
                    8) ^
                  a[255 & (r ^ (128 | ((e >> 2) & 63)))]) >>>
                  8) ^
                a[255 & (r ^ (128 | ((n >> 6) & 15) | ((3 & e) << 4)))]) >>>
                8) ^
                a[255 & (r ^ (128 | (63 & n)))])
            : ((r =
                ((r = (r >>> 8) ^ a[255 & (r ^ (224 | ((e >> 12) & 15)))]) >>>
                  8) ^
                a[255 & (r ^ (128 | ((e >> 6) & 63)))]) >>>
                8) ^
              a[255 & (r ^ (128 | (63 & e)))];
      return -1 ^ r;
    })(t) >>> 0
  );
}

function u(t, e) {
  var n = e.charAt(t.charCodeAt(0) % e.length),
    r = e.charAt(t.charCodeAt(t.length - 1) % e.length);
  return md5(n + t + r);
}