(function(e){function t(t){for(var r,a,c=t[0],i=t[1],s=t[2],l=0,f=[];l<c.length;l++)a=c[l],Object.prototype.hasOwnProperty.call(o,a)&&o[a]&&f.push(o[a][0]),o[a]=0;for(r in i)Object.prototype.hasOwnProperty.call(i,r)&&(e[r]=i[r]);d&&d(t);while(f.length)f.shift()();return u.push.apply(u,s||[]),n()}function n(){for(var e,t=0;t<u.length;t++){for(var n=u[t],r=!0,a=1;a<n.length;a++){var c=n[a];0!==o[c]&&(r=!1)}r&&(u.splice(t--,1),e=i(i.s=n[0]))}return e}var r={},a={app:0},o={app:0},u=[];function c(e){return i.p+"js/"+({}[e]||e)+"."+{"chunk-24d02887":"d9ced8e5","chunk-2af06a46":"02775f52","chunk-73ba3368":"7ea21278"}[e]+".js"}function i(t){if(r[t])return r[t].exports;var n=r[t]={i:t,l:!1,exports:{}};return e[t].call(n.exports,n,n.exports,i),n.l=!0,n.exports}i.e=function(e){var t=[],n={"chunk-24d02887":1,"chunk-2af06a46":1,"chunk-73ba3368":1};a[e]?t.push(a[e]):0!==a[e]&&n[e]&&t.push(a[e]=new Promise((function(t,n){for(var r="css/"+({}[e]||e)+"."+{"chunk-24d02887":"3336e2f6","chunk-2af06a46":"8cdd56c0","chunk-73ba3368":"8a533239"}[e]+".css",o=i.p+r,u=document.getElementsByTagName("link"),c=0;c<u.length;c++){var s=u[c],l=s.getAttribute("data-href")||s.getAttribute("href");if("stylesheet"===s.rel&&(l===r||l===o))return t()}var f=document.getElementsByTagName("style");for(c=0;c<f.length;c++){s=f[c],l=s.getAttribute("data-href");if(l===r||l===o)return t()}var d=document.createElement("link");d.rel="stylesheet",d.type="text/css",d.onload=t,d.onerror=function(t){var r=t&&t.target&&t.target.src||o,u=new Error("Loading CSS chunk "+e+" failed.\n("+r+")");u.code="CSS_CHUNK_LOAD_FAILED",u.request=r,delete a[e],d.parentNode.removeChild(d),n(u)},d.href=o;var p=document.getElementsByTagName("head")[0];p.appendChild(d)})).then((function(){a[e]=0})));var r=o[e];if(0!==r)if(r)t.push(r[2]);else{var u=new Promise((function(t,n){r=o[e]=[t,n]}));t.push(r[2]=u);var s,l=document.createElement("script");l.charset="utf-8",l.timeout=120,i.nc&&l.setAttribute("nonce",i.nc),l.src=c(e);var f=new Error;s=function(t){l.onerror=l.onload=null,clearTimeout(d);var n=o[e];if(0!==n){if(n){var r=t&&("load"===t.type?"missing":t.type),a=t&&t.target&&t.target.src;f.message="Loading chunk "+e+" failed.\n("+r+": "+a+")",f.name="ChunkLoadError",f.type=r,f.request=a,n[1](f)}o[e]=void 0}};var d=setTimeout((function(){s({type:"timeout",target:l})}),12e4);l.onerror=l.onload=s,document.head.appendChild(l)}return Promise.all(t)},i.m=e,i.c=r,i.d=function(e,t,n){i.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:n})},i.r=function(e){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},i.t=function(e,t){if(1&t&&(e=i(e)),8&t)return e;if(4&t&&"object"===typeof e&&e&&e.__esModule)return e;var n=Object.create(null);if(i.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var r in e)i.d(n,r,function(t){return e[t]}.bind(null,r));return n},i.n=function(e){var t=e&&e.__esModule?function(){return e["default"]}:function(){return e};return i.d(t,"a",t),t},i.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},i.p="/",i.oe=function(e){throw console.error(e),e};var s=window["webpackJsonp"]=window["webpackJsonp"]||[],l=s.push.bind(s);s.push=t,s=s.slice();for(var f=0;f<s.length;f++)t(s[f]);var d=l;u.push([0,"chunk-vendors"]),n()})({0:function(e,t,n){e.exports=n("56d7")},"034f":function(e,t,n){"use strict";n("85ec")},"56d7":function(e,t,n){"use strict";n.r(t);n("e260"),n("e6cf"),n("cca6"),n("a79d");var r=n("2b0e"),a=n("2f62"),o=n("1157"),u=n.n(o),c=function(){var e=this,t=e.$createElement,n=e._self._c||t;return n("div",{attrs:{id:"app"}},[n("router-view")],1)},i=[],s={name:"App"},l=s,f=(n("034f"),n("2877")),d=Object(f["a"])(l,c,i,!1,null,null,null),p=d.exports,h=n("ecee"),m=n("c074"),b=n("b702"),v=n("f2d1"),g=n("ad3d"),y=(n("ab8b"),n("3e48"),n("b9eb")),w=n.n(y),k=(n("d3b7"),n("3ca3"),n("ddb0"),n("8c4f"));r["a"].use(k["a"]);var O=function(){return Promise.all([n.e("chunk-2af06a46"),n.e("chunk-73ba3368")]).then(n.bind(null,"4a12"))},j=function(){return n.e("chunk-2af06a46").then(n.bind(null,"f2a3"))},P=function(){return n.e("chunk-24d02887").then(n.bind(null,"aa40"))},E=[{path:"/",redirect:"/home"},{path:"/home",name:"home",component:O},{path:"/test",name:"test",component:j},{path:"/edit",name:"edit",component:P}],S=new k["a"]({routes:E});r["a"].component("pagination",w.a),h["c"].add(m["a"],b["a"],v["a"]),r["a"].component("font-awesome-icon",g["a"]),r["a"].component("font-awesome-layers",g["b"]),r["a"].component("font-awesome-layers-text",g["c"]),r["a"].config.productionTip=!1,r["a"].use(a["a"]);var x=new a["a"].Store({state:{playController:{showMask:!1,playing:!1,curItem:{imgUrl:!1,enTtext:""},duration:!1,currentPosition:!1,aIndex:0,bIndex:0,mode:0},showEdit:!1,showPlayer:!0},mutations:{showEdit:function(e,t){e.showEdit=t},showPlayer:function(e,t){e.showPlayer=t},updateStatus:function(e,t){Object.assign(e.playController,t)}},actions:{updateStatus:function(e,t){var n=e.commit,r=e.state;if(t.remote){var a=Object.assign({},r.playController);Object.assign(a,t),u.a.get("/api/play",a)}n("updateStatus",t)},cmdAction:function(e,t){e.commit,e.state;u.a.get("/api/cmd",t)}}});new r["a"]({render:function(e){return e(p)},store:x,router:S,data:{eventHub:new r["a"]}}).$mount("#app")},"85ec":function(e,t,n){}});
//# sourceMappingURL=app.934c156c.js.map