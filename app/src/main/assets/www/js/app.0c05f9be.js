(function(t){function e(e){for(var r,i,a=e[0],c=e[1],u=e[2],f=0,p=[];f<a.length;f++)i=a[f],Object.prototype.hasOwnProperty.call(o,i)&&o[i]&&p.push(o[i][0]),o[i]=0;for(r in c)Object.prototype.hasOwnProperty.call(c,r)&&(t[r]=c[r]);l&&l(e);while(p.length)p.shift()();return s.push.apply(s,u||[]),n()}function n(){for(var t,e=0;e<s.length;e++){for(var n=s[e],r=!0,a=1;a<n.length;a++){var c=n[a];0!==o[c]&&(r=!1)}r&&(s.splice(e--,1),t=i(i.s=n[0]))}return t}var r={},o={app:0},s=[];function i(e){if(r[e])return r[e].exports;var n=r[e]={i:e,l:!1,exports:{}};return t[e].call(n.exports,n,n.exports,i),n.l=!0,n.exports}i.m=t,i.c=r,i.d=function(t,e,n){i.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:n})},i.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},i.t=function(t,e){if(1&e&&(t=i(t)),8&e)return t;if(4&e&&"object"===typeof t&&t&&t.__esModule)return t;var n=Object.create(null);if(i.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var r in t)i.d(n,r,function(e){return t[e]}.bind(null,r));return n},i.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return i.d(e,"a",e),e},i.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},i.p="/";var a=window["webpackJsonp"]=window["webpackJsonp"]||[],c=a.push.bind(a);a.push=e,a=a.slice();for(var u=0;u<a.length;u++)e(a[u]);var l=c;s.push([0,"chunk-vendors"]),n()})({0:function(t,e,n){t.exports=n("56d7")},"034f":function(t,e,n){"use strict";n("85ec")},"14ef":function(t,e,n){"use strict";n("fe45")},"56d7":function(t,e,n){"use strict";n.r(e);n("e260"),n("e6cf"),n("cca6"),n("a79d");var r=n("2b0e"),o=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{attrs:{id:"app"}},[n("Main")],1)},s=[],i=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"hello"},[n("header",[n("h4",{staticStyle:{margin:"5px"}},[t._v("儿童视频管理后台"+t._s(t.bIndex))]),n("div",[n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 24")}}},[t._v("音量+")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 25")}}},[t._v("音量-")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 23")}}},[t._v("暂停")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("adb shell input keyevent 4")}}},[t._v("返回")]),t._m(0)])]),n("div",[n("ul",t._l(t.items,(function(e,r){return n("li",{key:e.aid,staticClass:"it"},[n("img",{staticClass:"coverUrl",attrs:{src:e.coverUrl,referrerPolicy:"no-referrer"}}),n("div",[n("div",[n("b",[t._v("#"+t._s(r))]),t._v(t._s(e.title||e.aid)+"("+t._s(e.aid)+") ")]),n("div",t._l(e.items,(function(o,s){return n("li",{key:o.path},[n("span",{staticClass:"url",class:{cur:t.aIndex==r&&t.bIndex==s},attrs:{title:o.title},on:{click:function(e){return t.play(r,s)}}},[t._v(t._s(e.title&&"dy"!=e.title?s+1:o.title))]),n("em",{staticClass:"url",on:{click:function(e){return t.openUrl(r,s)}}},[t._v("*")])])})),0)])])})),0)]),n("player",{attrs:{curProgress:t.progress/t.duration},on:{updateProgress:t.updateProgress}})],1)},a=[function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticStyle:{float:"left"}},[t._v(" MODE: "),n("span",{staticClass:"bt"},[t._v("顺序")]),n("span",{staticClass:"bt"},[t._v("随机")]),n("span",{staticClass:"bt"},[t._v("单个")])])}],c=n("2909"),u=(n("d3b7"),n("a15b"),n("d81d"),n("4de4"),n("a434"),n("25f0"),function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"x-footer"},[n("div",{staticClass:"progress"},[n("div",{staticClass:"progress_bg"},[n("div",{staticClass:"video_progress",style:{width:t.left+"px"}})]),n("div",{staticClass:"progress_btn",style:{left:t.left+"px"}})])])}),l=[],f=n("1157"),p=n.n(f);window.$=p.a;var d={props:["curProgress"],data:function(){return{isStore:!0,left:0}},mounted:function(){this.initProgressBar()},methods:{initProgressBar:function(){this.left=p()(".progress").width()*this.curProgress;var t=this,e=this.left;p()((function(){var n=!1,r=0,o=0,s=p()(".progress").width();p()(".progress_btn").mousedown((function(t){r=t.pageX-e,console.log(r),n=!0})),p()(document).mouseup((function(){n=!1,setTimeout((function(){return t.$emit("updateProgress",t.left/s)}),10)})),p()(".progress").mousemove((function(o){n&&(e=o.pageX-r,e<=0?e=0:e>s&&(e=s),console.log(e),t.left=e,p()(".text").html(parseInt(e/s*100)+"%"))})),p()(".progress_bg").click((function(r){n||(o=p()(".progress_bg").offset().left,e=r.pageX-o,console.log(e),e<=0?e=0:e>s&&(e=s),t.left=e,p()(".text").html(parseInt(e/s*100)+"%"))}))}))}},watch:{curProgress:function(t){this.left=p()(".progress").width()*t}}},h=d,v=(n("a1ca"),n("2877")),g=Object(v["a"])(h,u,l,!1,null,"8d0ed05e",null),m=g.exports,b={components:{Player:m},name:"Main",data:function(){return{items:[],aIndex:0,bIndex:0,progress:0,duration:0}},mounted:function(){var t=this;this.getDatas(),setInterval((function(){t.getStatus()}),2e3)},props:{msg:String},methods:{updateProgress:function(t){this.play(this.aIndex,this.bIndex,t)},openUrl:function(t,e){open("/api/url?aIndex="+t+"&bIndex="+e)},base64:function(t){var e="data:image/png;base64,"+t.thumb;return console.log(e),e},sendEvent:function(t){fetch("/api/event",{method:"post",body:JSON.stringify({event:t}),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},play:function(t,e){var n=arguments.length>2&&void 0!==arguments[2]?arguments[2]:0;this.aIndex=t,this.bIndex=e,fetch("/api/play",{method:"post",body:JSON.stringify({aIndex:t,bIndex:e,progress:n}),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},addOrUpdate:function(t){fetch("/api/addOrUpdate",{method:"post",body:JSON.stringify(t),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},remove:function(){var t=this;fetch("/api/delete?ids="+this.items.filter((function(t){return t.checked})).map((function(t){return t.id})).join(",")).then((function(){for(var e=t,n=e.items.length-1;n>=0;n--){var r=e.items[n];r.checked&&e.items.splice(n,1)}}))},getDatas:function(){var t=this;fetch("/api/list.json",{method:"GET",mode:"cors",credentials:"include"}).then((function(e){e.json().then((function(e){var n;t.items.length=0,(n=t.items).push.apply(n,Object(c["a"])(e))}))})).catch((function(t){console.log("error: "+t.toString())}))},reload:function(){var t=this;fetch("/api/reloadplaylist",{method:"GET"}).then((function(e){e.json().then((function(e){var n;t.items.length=0,(n=t.items).push.apply(n,Object(c["a"])(e))}))})).catch((function(t){console.log("error: "+t.toString())}))},getStatus:function(){var t=this;fetch("/api/status",{method:"GET"}).then((function(e){e.json().then((function(e){Object.assign(t,e)}))})).catch((function(t){console.log("error: "+t.toString())}))}}},y=b,_=(n("14ef"),Object(v["a"])(y,i,a,!1,null,"2f35e791",null)),x=_.exports,w={name:"App",components:{Main:x}},O=w,j=(n("034f"),Object(v["a"])(O,o,s,!1,null,null,null)),C=j.exports;r["a"].config.productionTip=!1,new r["a"]({render:function(t){return t(C)}}).$mount("#app")},"81f9":function(t,e,n){},"85ec":function(t,e,n){},a1ca:function(t,e,n){"use strict";n("81f9")},fe45:function(t,e,n){}});
//# sourceMappingURL=app.0c05f9be.js.map