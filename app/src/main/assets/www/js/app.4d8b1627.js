(function(t){function e(e){for(var r,o,a=e[0],c=e[1],u=e[2],d=0,f=[];d<a.length;d++)o=a[d],Object.prototype.hasOwnProperty.call(s,o)&&s[o]&&f.push(s[o][0]),s[o]=0;for(r in c)Object.prototype.hasOwnProperty.call(c,r)&&(t[r]=c[r]);l&&l(e);while(f.length)f.shift()();return i.push.apply(i,u||[]),n()}function n(){for(var t,e=0;e<i.length;e++){for(var n=i[e],r=!0,a=1;a<n.length;a++){var c=n[a];0!==s[c]&&(r=!1)}r&&(i.splice(e--,1),t=o(o.s=n[0]))}return t}var r={},s={app:0},i=[];function o(e){if(r[e])return r[e].exports;var n=r[e]={i:e,l:!1,exports:{}};return t[e].call(n.exports,n,n.exports,o),n.l=!0,n.exports}o.m=t,o.c=r,o.d=function(t,e,n){o.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:n})},o.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},o.t=function(t,e){if(1&e&&(t=o(t)),8&e)return t;if(4&e&&"object"===typeof t&&t&&t.__esModule)return t;var n=Object.create(null);if(o.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var r in t)o.d(n,r,function(e){return t[e]}.bind(null,r));return n},o.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return o.d(e,"a",e),e},o.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},o.p="/";var a=window["webpackJsonp"]=window["webpackJsonp"]||[],c=a.push.bind(a);a.push=e,a=a.slice();for(var u=0;u<a.length;u++)e(a[u]);var l=c;i.push([0,"chunk-vendors"]),n()})({0:function(t,e,n){t.exports=n("56d7")},"034f":function(t,e,n){"use strict";n("85ec")},"56d7":function(t,e,n){"use strict";n.r(e);n("e260"),n("e6cf"),n("cca6"),n("a79d");var r=n("2b0e"),s=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{attrs:{id:"app"}},[n("Main")],1)},i=[],o=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"hello"},[n("header",[n("h4",{staticStyle:{margin:"5px"}},[t._v("儿童视频管理后台"+t._s(t.bIndex))]),n("div",[n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 24")}}},[t._v("音量+")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 25")}}},[t._v("音量-")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 23")}}},[t._v("暂停")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("adb shell input keyevent 4")}}},[t._v("返回")]),t._m(0)])]),n("div",[n("ul",t._l(t.items,(function(e,r){return n("li",{key:e.aid,staticClass:"it"},[n("img",{staticClass:"coverUrl",attrs:{src:e.coverUrl,referrerPolicy:"no-referrer"}}),n("div",[n("div",[n("b",[t._v("#"+t._s(r))]),t._v(t._s(e.title||e.aid)+"("+t._s(e.aid)+") ")]),n("div",t._l(e.items,(function(s,i){return n("li",{key:s.path},[n("span",{staticClass:"url",class:{cur:t.aIndex==r&&t.bIndex==i},attrs:{title:s.title},on:{click:function(e){return t.play(r,i)}}},[t._v(t._s(e.title&&"dy"!=e.title?i+1:s.title))]),n("em",{staticClass:"url",on:{click:function(e){return t.openUrl(r,i)}}},[t._v("*")])])})),0)])])})),0)]),n("player",{attrs:{curProgress:t.progress/t.duration,duration:t.duration,curPostion:t.progress,curAid:t.items[t.aIndex]},on:{updateProgress:t.updateProgress}})],1)},a=[function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticStyle:{float:"left"}},[t._v(" MODE: "),n("span",{staticClass:"bt"},[t._v("顺序")]),n("span",{staticClass:"bt"},[t._v("随机")]),n("span",{staticClass:"bt"},[t._v("单个")])])}],c=n("2909"),u=(n("d3b7"),n("a15b"),n("d81d"),n("4de4"),n("a434"),n("25f0"),function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("div",{staticClass:"x-footer"},[n("div",[n("div",{staticStyle:{display:"flex"}},[n("div",{staticClass:"duration"},[t._v(t._s(t.format(t.curPostion)))]),n("div",{staticClass:"progresswrap"},[n("div",{staticClass:"progress"},[n("div",{staticClass:"progress_bg"},[n("div",{staticClass:"video_progress",style:{width:t.left+"px"}})]),n("div",{staticClass:"progress_btn",style:{left:t.left+"px"}})])]),n("div",{staticClass:"duration"},[t._v(t._s(t.format(t.duration)))])]),n("div",{staticStyle:{display:"flex"}},[n("div",{staticClass:"x-meida"},[n("div",{staticClass:"x-meida-img"},[n("img",{attrs:{src:t.curAid&&t.curAid.coverUrl}})]),n("div",{staticClass:"x-media-name"},[n("h3",[t._v(t._s(t.curAid&&t.curAid.title))]),n("h5")])]),n("div",{staticClass:"x-media-btn"},[n("img",{attrs:{src:t.isStore?t.footer.startIcon:t.footer.stopIcon}})]),n("div",{staticClass:"x-media-menu"})])])])])}),l=[],d=(n("b65f"),n("99af"),n("1157")),f=n.n(d);window.$=f.a;var p={props:["curProgress","duration","curPostion","curAid"],data:function(){return{isStore:!0,left:0,footer:{title:"一条狗的使命",subTitle:"俗事杂谈论坛直播",startIcon:"../static/img/start.svg",stopIcon:"../static/img/stop.svg"}}},mounted:function(){this.initProgressBar()},methods:{format:function(t){var e=t/1e3,n=Math.trunc(e%60),r=Math.trunc(e/60%60),s=Math.trunc(e/3600);return s>0?"".concat(s,":").concat(r,":").concat(n):"".concat(r,":").concat(n)},initProgressBar:function(){this.left=f()(".progress").width()*this.curProgress;var t=this,e=this.left;f()((function(){var n=!1,r=0,s=0,i=f()(".progress").width();f()(".progress_btn").mousedown((function(t){r=t.pageX-e,console.log(r),n=!0})),f()(document).mouseup((function(){n=!1,setTimeout((function(){return t.$emit("updateProgress",t.left/i)}),10)})),f()(".progress").mousemove((function(s){n&&(e=s.pageX-r,e<=0?e=0:e>i&&(e=i),console.log(e),t.left=e,f()(".text").html(parseInt(e/i*100)+"%"))})),f()(".progress_bg").click((function(r){n||(s=f()(".progress_bg").offset().left,e=r.pageX-s,console.log(e),e<=0?e=0:e>i&&(e=i),t.left=e,f()(".text").html(parseInt(e/i*100)+"%"))}))}))}},watch:{curProgress:function(t){this.left=f()(".progress").width()*t}}},h=p,v=(n("71f2"),n("2877")),g=Object(v["a"])(h,u,l,!1,null,"e57ef754",null),m=g.exports,b={components:{Player:m},name:"Main",data:function(){return{items:[],aIndex:0,bIndex:0,progress:0,duration:0}},mounted:function(){var t=this;this.getDatas(),setInterval((function(){t.getStatus()}),2e3)},props:{msg:String},methods:{updateProgress:function(t){this.play(this.aIndex,this.bIndex,t)},openUrl:function(t,e){open("/api/url?aIndex="+t+"&bIndex="+e)},base64:function(t){var e="data:image/png;base64,"+t.thumb;return console.log(e),e},sendEvent:function(t){fetch("/api/event",{method:"post",body:JSON.stringify({event:t}),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},play:function(t,e){var n=arguments.length>2&&void 0!==arguments[2]?arguments[2]:0;this.aIndex=t,this.bIndex=e,fetch("/api/play",{method:"post",body:JSON.stringify({aIndex:t,bIndex:e,progress:n}),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},addOrUpdate:function(t){fetch("/api/addOrUpdate",{method:"post",body:JSON.stringify(t),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},remove:function(){var t=this;fetch("/api/delete?ids="+this.items.filter((function(t){return t.checked})).map((function(t){return t.id})).join(",")).then((function(){for(var e=t,n=e.items.length-1;n>=0;n--){var r=e.items[n];r.checked&&e.items.splice(n,1)}}))},getDatas:function(){var t=this;fetch("/api/list.json",{method:"GET",mode:"cors",credentials:"include"}).then((function(e){e.json().then((function(e){var n;t.items.length=0,(n=t.items).push.apply(n,Object(c["a"])(e))}))})).catch((function(t){console.log("error: "+t.toString())}))},reload:function(){var t=this;fetch("/api/reloadplaylist",{method:"GET"}).then((function(e){e.json().then((function(e){var n;t.items.length=0,(n=t.items).push.apply(n,Object(c["a"])(e))}))})).catch((function(t){console.log("error: "+t.toString())}))},getStatus:function(){var t=this;fetch("/api/status",{method:"GET"}).then((function(e){e.json().then((function(e){Object.assign(t,e)}))})).catch((function(t){console.log("error: "+t.toString())}))}}},y=b,_=(n("b6f3"),Object(v["a"])(y,o,a,!1,null,"e81c6c08",null)),x=_.exports,w={name:"App",components:{Main:x}},C=w,O=(n("034f"),Object(v["a"])(C,s,i,!1,null,null,null)),P=O.exports;r["a"].config.productionTip=!1,new r["a"]({render:function(t){return t(P)}}).$mount("#app")},"709f":function(t,e,n){},"71f2":function(t,e,n){"use strict";n("bb68")},"85ec":function(t,e,n){},b6f3:function(t,e,n){"use strict";n("709f")},bb68:function(t,e,n){}});
//# sourceMappingURL=app.4d8b1627.js.map