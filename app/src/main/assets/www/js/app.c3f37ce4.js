(function(t){function e(e){for(var i,r,a=e[0],c=e[1],l=e[2],d=0,f=[];d<a.length;d++)r=a[d],Object.prototype.hasOwnProperty.call(s,r)&&s[r]&&f.push(s[r][0]),s[r]=0;for(i in c)Object.prototype.hasOwnProperty.call(c,i)&&(t[i]=c[i]);u&&u(e);while(f.length)f.shift()();return o.push.apply(o,l||[]),n()}function n(){for(var t,e=0;e<o.length;e++){for(var n=o[e],i=!0,a=1;a<n.length;a++){var c=n[a];0!==s[c]&&(i=!1)}i&&(o.splice(e--,1),t=r(r.s=n[0]))}return t}var i={},s={app:0},o=[];function r(e){if(i[e])return i[e].exports;var n=i[e]={i:e,l:!1,exports:{}};return t[e].call(n.exports,n,n.exports,r),n.l=!0,n.exports}r.m=t,r.c=i,r.d=function(t,e,n){r.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:n})},r.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},r.t=function(t,e){if(1&e&&(t=r(t)),8&e)return t;if(4&e&&"object"===typeof t&&t&&t.__esModule)return t;var n=Object.create(null);if(r.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var i in t)r.d(n,i,function(e){return t[e]}.bind(null,i));return n},r.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return r.d(e,"a",e),e},r.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},r.p="/";var a=window["webpackJsonp"]=window["webpackJsonp"]||[],c=a.push.bind(a);a.push=e,a=a.slice();for(var l=0;l<a.length;l++)e(a[l]);var u=c;o.push([0,"chunk-vendors"]),n()})({0:function(t,e,n){t.exports=n("56d7")},"034f":function(t,e,n){"use strict";n("85ec")},"1ae8":function(t,e,n){"use strict";n("9c64")},"4e50":function(t,e,n){"use strict";n("4eab")},"4eab":function(t,e,n){},"56d7":function(t,e,n){"use strict";n.r(e);n("e260"),n("e6cf"),n("cca6"),n("a79d");var i=n("2b0e"),s=n("2f62"),o=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{attrs:{id:"app"}},[n("NavIndex")],1)},r=[],a=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("header",{staticStyle:{position:"fixed",left:"0",right:"0",top:"0",height:"90px",background:"rgba(255, 255, 255, 0.8)","z-index":"1000"}},[n("h4",{staticStyle:{margin:"5px"}},[t._v(" 宝贝计划"),n("font-awesome-icon",{attrs:{icon:"cog","fixed-width":""}})],1),n("div",{staticStyle:{clear:"both"}},[n("ul",t._l(t.tabs,(function(e,i){return n("li",{key:i,staticClass:"tab",class:{curTab:t.curTabIndex==i},on:{click:function(e){t.curTabIndex=i}}},[t._v(" "+t._s(e.name)+" ")])})),0)])]),n("div",{staticStyle:{"margin-top":"90px"}},[n(t.tabs[t.curTabIndex].comp,{tag:"component"})],1)])},c=[],l=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"hello"},[n("div",{staticStyle:{clear:"both"}},[n("ul",t._l(t.items,(function(e,i){return n("li",{key:e.aid,staticClass:"it",class:{showAll_k:t.showAll_k==i}},[n("img",{staticClass:"coverUrl",attrs:{src:e.coverUrl,referrerPolicy:"no-referrer"},on:{click:function(e){t.showAll_k=t.showAll_k==i?-1:i}}}),n("div",[n("div",[n("b",[t._v("#"+t._s(i))]),t._v(t._s(e.title||e.aid)+":"+t._s(e.aid)+":("+t._s(e.items.length)+") "),n("span",{staticStyle:{cursor:"pointer"},on:{click:function(e){return t.del(i,-1)}}},[t._v("x")])]),n("div",t._l(e.items,(function(s,o){return n("li",{key:s.path},[n("span",{staticClass:"url",class:{cur:t.aIndex==i&&t.bIndex==o},attrs:{title:s.title},on:{click:function(e){return t.play(i,o)}}},[t._v(t._s(e.title&&"dy"!=e.title?o+1:s.title))]),t.isPreview?n("em",{staticClass:"url",on:{click:function(e){return t.openUrl(i,o)}}},[t._v("*")]):t._e(),t.isEdit?n("em",{staticClass:"url",on:{click:function(e){return t.del(i,o)}}},[t._v("x")]):t._e()])})),0)])])})),0)]),n("player",{attrs:{curAid:t.items[t.aIndex]},on:{updateProgress:t.updateProgress,next:t.next,prev:t.prev}})],1)},u=[],d=n("2909"),f=(n("d3b7"),n("25f0"),function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{ref:"parent"},[n("div",{staticClass:"x-footer"},[n("div",[n("div",{staticStyle:{display:"flex","box-sizing":"border-box"}},[n("div",{staticClass:"duration"},[t._v(t._s(t.format(t.curPosition)))]),n("div",{staticClass:"progresswrap",staticStyle:{"flex-grow":"1"}},[n("div",{staticClass:"progress"},[n("div",{staticClass:"progress_bg"},[n("div",{staticClass:"video_progress",style:{width:t.left+"px"}})]),n("div",{staticClass:"progress_btn",style:{left:t.left+"px"}})])]),n("div",{staticClass:"duration"},[t._v(t._s(t.format(t.duration)))])]),n("div",{staticStyle:{display:"flex"}},[n("div",{staticClass:"x-meida"},[n("div",{staticClass:"x-meida-img"},[n("img",{attrs:{src:t.curAid&&t.curAid.coverUrl}})]),n("div",{staticClass:"x-media-name"},[n("h3",[t._v(t._s(t.curAid&&t.curAid.title))]),n("h5")])]),n("div",{staticClass:"x-media-btn"},[n("img",{attrs:{src:t.isPlaying?t.footer.stopIcon:t.footer.startIcon},on:{click:t.togglePlay}})]),n("div",{staticClass:"x-media-menu"})]),n("div",{staticClass:"ctrl",staticStyle:{display:"block","text-align":"left",margin:"5px"}},[n("span",{staticClass:"bt",on:{click:t.prev}},[t._v("上一个")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 24")}}},[t._v("音量+")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 25")}}},[t._v("音量-")]),n("span",{staticClass:"bt",staticStyle:{float:"right"},on:{click:t.next}},[t._v("下一个")]),n("span",{staticClass:"bt",staticStyle:{float:"right"},on:{click:t.load}},[t._v("加载")])])])])])}),p=[],h=(n("b65f"),n("99af"),n("1157")),g=n.n(h);window.$=g.a;var m={props:["curAid"],data:function(){return{isStore:!1,left:0,footer:{startIcon:"../static/img/start.svg",stopIcon:"../static/img/stop.svg"}}},mounted:function(){this.initProgressBar(),setTimeout((function(){g()("#app").css("margin-bottom",g()(".x-footer").outerHeight()+10)}),3e3)},computed:{isPlaying:function(){return this.$store.state.isPlaying},duration:function(){return this.$store.state.duration},curPosition:function(){return this.$store.state.curPosition}},methods:{next:function(){this.$emit("next")},load:function(){fetch("/api/reLoadPlayList",{method:"get"}).then((function(t){}))},prev:function(){this.$emit("prev")},togglePlay:function(){this.$emit("updateProgress",this.curPosition/this.duration,!this.isPlaying)},sendEvent:function(t){fetch("/api/event",{method:"post",body:JSON.stringify({event:t}),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},format:function(t){var e=t/1e3,n=Math.trunc(e%60),i=Math.trunc(e/60%60),s=Math.trunc(e/3600);return s>0?"".concat(s,":").concat(i,":").concat(n):"".concat(i,":").concat(n)},initProgressBar:function(){this.left=g()(".progress").width()*this.curPosition/this.duration;var t=this,e=this.left;g()((function(){var n=!1,i=0,s=0,o=g()(".progress").width();g()(".progress_btn").mousedown((function(t){i=t.pageX-e,console.log(i),n=!0})),g()(".progress").mouseup((function(){n=!1,setTimeout((function(){return t.$emit("updateProgress",t.left/o,t.isPlaying)}),10)})),g()(".progress").mousemove((function(s){n&&(e=s.pageX-i,e<=0?e=0:e>o&&(e=o),console.log(e),t.left=e,g()(".text").html(parseInt(e/o*100)+"%"))})),g()(".progress_bg").click((function(i){n||(s=g()(".progress_bg").offset().left,e=i.pageX-s,console.log(e),e<=0?e=0:e>o&&(e=o),t.left=e,g()(".text").html(parseInt(e/o*100)+"%"))}))}))}},watch:{curPosition:function(){this.left=g()(".progress").width()*this.curPosition/this.duration}}},v=m,y=(n("b6d5"),n("2877")),x=Object(y["a"])(v,f,p,!1,null,"c2708448",null),b=x.exports,_={components:{Player:b},name:"Main",data:function(){return{curTabIndex:0,tabs:[{name:"视频",comp:"Video"},{name:"按图识字",comp:"Image"},{name:"情景学习",comp:"byWhen"}],aIndex:0,bIndex:0,isPlaying:!1,curPosition:0,percent:0,duration:0,items:[],progress:0,updateRemote:0,showAll_k:-1,isEdit:!1,isPreview:!1}},computed:{},mounted:function(){var t=this;this.getDatas(),setInterval((function(){t.getStatus()}),2e3)},props:{msg:String},methods:{updateProgress:function(t,e){this.curPosition=t*this.duration,this.isPlaying=e,this.updateRemote++},openUrl:function(t,e){open("/api/url?aIndex="+t+"&bIndex="+e)},del:function(t,e){var n=this;confirm("delete it?")&&fetch("/api/delete?aIndex="+t+"&bIndex="+e,{method:"GET"}).then((function(t){n.getDatas()}))},base64:function(t){var e="data:image/png;base64,"+t.thumb;return console.log(e),e},sendEvent:function(t){fetch("/api/event",{method:"post",body:JSON.stringify({event:t}),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},prev:function(){0!=this.items.length&&(0==this.bIndex?(0==this.aIndex?this.aIndex=this.items.length-1:this.aIndex--,this.bindex=this.items[this.aIndex].length-1):this.bIndex--,this.updateRemote++)},next:function(){0!=this.items.length&&(this.bIndex>=this.items[this.aIndex].length-1?(this.aIndex>=this.items.length-1?this.aIndex=0:this.aIndex++,this.bindex=0):this.bIndex++,this.updateRemote++)},play:function(t,e){var n=arguments.length>2&&void 0!==arguments[2]?arguments[2]:0,i=!(arguments.length>3&&void 0!==arguments[3])||arguments[3];fetch("/api/play",{method:"post",body:JSON.stringify({aIndex:t,bIndex:e,progress:n,isPlaying:i})}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},getDatas:function(){var t=this;fetch("/api/list.json",{method:"GET",mode:"cors",credentials:"include"}).then((function(e){e.json().then((function(e){var n;t.items.length=0,(n=t.items).push.apply(n,Object(d["a"])(e))}))})).catch((function(t){console.log("error: "+t.toString())}))},getStatus:function(){var t=this;fetch("/api/status",{method:"GET"}).then((function(e){e.json().then((function(e){Object.assign(t,e),t.$store.commit("updateStatus",e)}))})).catch((function(t){console.log("error: "+t.toString())}))}},watch:{updateRemote:{handler:function(){this.play(this.aIndex,this.bIndex,this.curPosition/this.duration,this.isPlaying)},deep:!0}}},I=_,w=(n("4e50"),Object(y["a"])(I,l,u,!1,null,"67ab09a2",null)),k=w.exports,C=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",t._l(t.catItems,(function(e,i){return n("div",{key:i},[n("div",{staticStyle:{"text-align":"left"}},[t._v("类别:"+t._s(i))]),n("ul",{staticClass:"figure-list"},t._l(e,(function(e,i){return n("li",{key:i},[n("div",{staticStyle:{"text-align":"left"}},[t._v(" "+t._s(e.cnText)+"-"+t._s(e.enText)+" ")]),n("figure",{staticStyle:{cursor:"pointer"},style:{backgroundImage:"url(/api/proxy?url="+encodeURIComponent(e.imgUrl)+")"},on:{click:function(n){return t.playfrom(e)}}})])})),0)])})),0)},P=[],S=n("b85c");window.$=g.a;var O={data:function(){return{curTabIndex:0,isEdit:!1,showAll_k:!1,isPreview:!1,aIndex:0,bIndex:0,catItems:{}}},computed:{},mounted:function(){this.loadList()},methods:{playfrom:function(t){g.a.get("/api/playres",{id:t.id,typeId:t.typeId})},loadList:function(){var t=this;g.a.getJSON("/api/manRes").then((function(e){var n,i=Object(S["a"])(e);try{for(i.s();!(n=i.n()).done;){var s=n.value;t.catItems[s.cat]||(t.catItems[s.cat]=[]),t.catItems[s.cat].push(s)}}catch(o){i.e(o)}finally{i.f()}t.$forceUpdate(),console.log(t.catItems)}))}},watch:{}},T=O,j=(n("1ae8"),Object(y["a"])(T,C,P,!1,null,"2dddaf17",null)),$=j.exports,E=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t.rightCn>0?n("div",{staticStyle:{"text-align":"left",color:"red"}},[t._l(t.rightCn,(function(t){return n("font-awesome-icon",{key:t,attrs:{icon:"star","fixed-width":""}})})),t._v(" "+t._s(t.rightCn)+" ")],2):t._e(),t.wrongCn>0?n("div",{staticStyle:{"text-align":"left",color:"gray"}},[t._l(t.wrongCn,(function(t){return n("font-awesome-icon",{key:t,attrs:{icon:"star","fixed-width":""}})})),t._v(t._s(t.wrongCn)+" ")],2):t._e(),n("ul",{staticClass:"figure-list"},t._l(t.list,(function(e,i){return n("li",{key:i},[n("div",{staticStyle:{"text-align":"left"}},[t._v(t._s(e.cnText)+"-"+t._s(e.enText))]),n("figure",{staticClass:"animated",staticStyle:{cursor:"pointer"},style:{backgroundImage:"url(/api2/proxy?url="+encodeURIComponent(e.imgUrl)+"&enText="+encodeURIComponent(e.enText.trim())+")"},on:{click:function(n){t.select(i,e),t.clickIndex=i}}})])})),0),n("div",{staticStyle:{cursor:"pointer"},on:{click:t.replay}},[n("font-awesome-icon",{class:{speaking:t.speaking},attrs:{icon:"volume-up","fixed-width":""}}),t._v("Replay 重播("+t._s(t.seconds)+"s) "),n("audio",{directives:[{name:"show",rawName:"v-show",value:!1,expression:"false"}],ref:"audio",staticStyle:{height:"0px",width:"0px"}})],1)])},A=[],R=n("1da1");n("96cf");window.$=g.a;var U={data:function(){return{rightCn:0,wrongCn:0,curAct:0,list:[],curIndex:0,speaking:!1,seconds:15,timer:0}},computed:{},mounted:function(){this.randList(),this.resetClock()},beforeDestroy:function(){clearInterval(this.timer)},methods:{replay:function(){var t=this.list[this.curIndex];this.say("which is "+t.enText)},getIndicator:function(t){return""},select:function(t,e){var n=this;this.resetClock(!0),Object(R["a"])(regeneratorRuntime.mark((function i(){return regeneratorRuntime.wrap((function(i){while(1)switch(i.prev=i.next){case 0:if(t!=n.curIndex){i.next=7;break}return i.next=3,n.say(e.enText,"en").then((function(){return n.say(n.getRightSetence(),"en")})).then((function(){return n.randList()}));case 3:n.rightCn++,n.curAct=1,i.next=11;break;case 7:return i.next=9,n.say(e.enText,"en").then((function(){return n.say(n.getWrongSetence(),"en")}));case 9:n.wrongCn++,n.curAct=-1;case 11:n.resetClock();case 12:case"end":return i.stop()}}),i)})))()},resetClock:function(t){var e=this;t?clearInterval(this.timer):(this.timer&&clearInterval(this.timer),this.timer=setInterval((function(){e.seconds--,0==e.seconds&&(e.replay(),e.seconds=15)}),1e3))},randList:function(){var t=this;g.a.getJSON("/api2/randList",{n:4}).then((function(e){var n;t.list.length=0,(n=t.list).push.apply(n,Object(d["a"])(e)),t.curIndex=parseInt(4*Math.random()),t.replay()}))},say:function(t){var e=this,n=arguments.length>1&&void 0!==arguments[1]?arguments[1]:"en";this.speaking=!0,"en"==n?n=" -v Samantha ":"zh"==n&&(n=" -v Ting-Ting ");var i=!0;return i?g.a.get("/api2/say",{str:n+t}).then((function(){e.speaking=!1})):g.a.get("/api2/tts",{lan:"en",str:t}).then((function(t){return new Promise((function(n,i){t.URL&&(e.$refs.audio.src=t.URL,e.$refs.audio.addEventListener("ended",(function(){n(),this.speaking=!1})),e.$refs.audio.play())}))}))},getWrongSetence:function(){var t=["no no no","oh sorry.","incorrect","come on."],e=parseInt(Math.random()*t.length);return t[e]},getRightSetence:function(){var t=["yes yes yes","well done","good job"],e=parseInt(Math.random()*t.length);return t[e]}},watch:{}},L=U,M=(n("88b6"),Object(y["a"])(L,E,A,!1,null,"5812d99c",null)),B=M.exports,N={components:{ByVideo:k,ByImage:$,ByWhen:B},data:function(){return{curTabIndex:2,tabs:[{name:"视频学习",comp:"ByVideo"},{name:"看图识字",comp:"ByImage"},{name:"考考你",comp:"ByWhen"}]}}},J=N,W=(n("ed00"),Object(y["a"])(J,a,c,!1,null,"25f0e475",null)),D=W.exports,z={name:"App",components:{NavIndex:D}},G=z,V=(n("034f"),Object(y["a"])(G,o,r,!1,null,null,null)),X=V.exports,H=n("ecee"),q=n("c074"),F=n("b702"),K=n("21f8"),Q=n("ad3d");H["c"].add(q["a"],F["a"],K["a"]),i["a"].component("font-awesome-icon",Q["a"]),i["a"].component("font-awesome-layers",Q["b"]),i["a"].component("font-awesome-layers-text",Q["c"]),i["a"].config.productionTip=!1,i["a"].use(s["a"]);var Y=new s["a"].Store({state:{isPlaying:!1,duration:0,curProgress:0,curPosition:0,aIndex:0,bIndex:0},mutations:{updateStatus:function(t,e){Object.assign(t,e)}}});new i["a"]({render:function(t){return t(X)},store:Y}).$mount("#app")},"792a":function(t,e,n){},"85ec":function(t,e,n){},"88b6":function(t,e,n){"use strict";n("bd74")},"9c64":function(t,e,n){},b6d5:function(t,e,n){"use strict";n("792a")},bd74:function(t,e,n){},bd7c:function(t,e,n){},ed00:function(t,e,n){"use strict";n("bd7c")}});
//# sourceMappingURL=app.c3f37ce4.js.map