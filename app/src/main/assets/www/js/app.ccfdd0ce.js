(function(t){function e(e){for(var i,s,r=e[0],c=e[1],l=e[2],d=0,p=[];d<r.length;d++)s=r[d],Object.prototype.hasOwnProperty.call(a,s)&&a[s]&&p.push(a[s][0]),a[s]=0;for(i in c)Object.prototype.hasOwnProperty.call(c,i)&&(t[i]=c[i]);u&&u(e);while(p.length)p.shift()();return o.push.apply(o,l||[]),n()}function n(){for(var t,e=0;e<o.length;e++){for(var n=o[e],i=!0,r=1;r<n.length;r++){var c=n[r];0!==a[c]&&(i=!1)}i&&(o.splice(e--,1),t=s(s.s=n[0]))}return t}var i={},a={app:0},o=[];function s(e){if(i[e])return i[e].exports;var n=i[e]={i:e,l:!1,exports:{}};return t[e].call(n.exports,n,n.exports,s),n.l=!0,n.exports}s.m=t,s.c=i,s.d=function(t,e,n){s.o(t,e)||Object.defineProperty(t,e,{enumerable:!0,get:n})},s.r=function(t){"undefined"!==typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(t,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(t,"__esModule",{value:!0})},s.t=function(t,e){if(1&e&&(t=s(t)),8&e)return t;if(4&e&&"object"===typeof t&&t&&t.__esModule)return t;var n=Object.create(null);if(s.r(n),Object.defineProperty(n,"default",{enumerable:!0,value:t}),2&e&&"string"!=typeof t)for(var i in t)s.d(n,i,function(e){return t[e]}.bind(null,i));return n},s.n=function(t){var e=t&&t.__esModule?function(){return t["default"]}:function(){return t};return s.d(e,"a",e),e},s.o=function(t,e){return Object.prototype.hasOwnProperty.call(t,e)},s.p="/";var r=window["webpackJsonp"]=window["webpackJsonp"]||[],c=r.push.bind(r);r.push=e,r=r.slice();for(var l=0;l<r.length;l++)e(r[l]);var u=c;o.push([0,"chunk-vendors"]),n()})({0:function(t,e,n){t.exports=n("56d7")},"0238":function(t,e,n){},"034f":function(t,e,n){"use strict";n("85ec")},1298:function(t,e,n){"use strict";n("22bb")},"22bb":function(t,e,n){},2939:function(t,e,n){},"3a8b":function(t,e,n){},"4e6c":function(t,e,n){"use strict";n("d907")},"56d7":function(t,e,n){"use strict";n.r(e);n("e260"),n("e6cf"),n("cca6"),n("a79d");var i=n("2b0e"),a=n("2f62"),o=n("1157"),s=n.n(o),r=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{attrs:{id:"app"}},[n("NavIndex"),n("player")],1)},c=[],l=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("header",{staticStyle:{position:"fixed",left:"0",right:"0",top:"0",height:"90px",background:"rgba(255, 255, 255, 0.8)","z-index":"1000"}},[n("h4",{staticStyle:{margin:"5px"}},[t._v(" 宝贝计划 "),n("span",{staticStyle:{position:"relative",display:"inline-block"}},[n("div",{staticStyle:{position:"fixed",background:"white","font-weight":"normal","font-size":"14px",right:"0",top:"0"}},[n("ul",{directives:[{name:"show",rawName:"v-show",value:t.showMenu,expression:"showMenu"}],staticStyle:{margin:"0",padding:"10px",border:"1px solid"}},[n("li",[n("input",{directives:[{name:"model",rawName:"v-model",value:t.showEdit,expression:"showEdit"}],attrs:{type:"checkbox"},domProps:{checked:Array.isArray(t.showEdit)?t._i(t.showEdit,null)>-1:t.showEdit},on:{change:function(e){var n=t.showEdit,i=e.target,a=!!i.checked;if(Array.isArray(n)){var o=null,s=t._i(n,o);i.checked?s<0&&(t.showEdit=n.concat([o])):s>-1&&(t.showEdit=n.slice(0,s).concat(n.slice(s+1)))}else t.showEdit=a}}}),t._v("显示管理按钮")])]),n("font-awesome-icon",{staticStyle:{position:"absolute",right:"0",top:"10px"},attrs:{icon:"cog","fixed-width":""},on:{click:function(e){t.showMenu=!t.showMenu}}})],1)])]),n("div",{staticStyle:{clear:"both"}},[n("ul",t._l(t.tabs,(function(e,i){return n("li",{key:i,staticClass:"tab",class:{curTab:t.curTabIndex==i},on:{click:function(e){t.curTabIndex=i}}},[t._v(" "+t._s(e.name)+" ")])})),0)])]),n("div",{staticStyle:{"margin-top":"110px"}},[n("keep-alive",[n(t.tabs[t.curTabIndex].comp,{tag:"component"})],1)],1)])},u=[],d=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"hello"},[n("div",{staticStyle:{clear:"both"}},[n("ul",t._l(t.items,(function(e,i){return n("li",{key:e.aid,staticClass:"it",class:{showAll_k:t.showAll_k==i,active:i==t.aIndex}},[n("img",{staticClass:"coverUrl",staticStyle:{cursor:"pointer"},attrs:{src:e.coverUrl,referrerPolicy:"no-referrer"},on:{click:function(e){t.showAll_k=t.showAll_k==i?-1:i,t.play(i,0)}}}),n("div",[n("div",[n("b",[t._v("#"+t._s(i))]),n("span",{on:{click:function(e){return t.play(i,0)}}},[t._v(t._s(e.title||e.aid)+":"+t._s(e.aid)+":("+t._s(e.items.length)+")")]),t.$store.state.showEdit?n("span",{staticStyle:{cursor:"pointer"},on:{click:function(e){return t.del(i,-1)}}},[t._v("x")]):t._e()]),n("div",t._l(e.items,(function(a,o){return n("li",{key:a.path},[n("span",{staticClass:"url",class:{cur:t.aIndex==i&&t.bIndex==o},attrs:{title:a.title},on:{click:function(e){return t.play(i,o)}}},[t._v(t._s(e.title&&"dy"!=e.title?o+1:a.title))]),t.isPreview?n("em",{staticClass:"url",on:{click:function(e){return t.openUrl(i,o)}}},[t._v("*")]):t._e(),t.$store.state.showEdit?n("em",{staticClass:"url",on:{click:function(e){return t.del(i,o)}}},[t._v("x")]):t._e()])})),0)])])})),0)]),n("pagination",{attrs:{options:{chunk:5},records:t.total},on:{paginate:t.loadList},model:{value:t.page,callback:function(e){t.page=e},expression:"page"}})],1)},p=[],f=n("2909"),m=n("5530"),h=(n("d3b7"),n("b9eb")),g=n.n(h),v={components:{Pagination:g.a},name:"Main",data:function(){return{page:1,total:0,isPlaying:!1,curPosition:0,percent:0,duration:0,items:[],progress:0,updateRemote:0,showAll_k:-1,isEdit:!1,isPreview:!1}},computed:{aIndex:function(){return this.$store.state.playController.aIndex},bIndex:function(){return this.$store.state.playController.bIndex}},activated:function(){this.loadList()},props:{msg:String},methods:Object(m["a"])(Object(m["a"])({},Object(a["b"])(["updateStatus"])),{},{updateProgress:function(t,e){this.curPosition=t*this.duration,this.isPlaying=e,this.updateRemote++},openUrl:function(t,e){open("/api/url?aIndex="+t+"&bIndex="+e)},del:function(t,e){var n=this;confirm("delete it?")&&fetch("/api/delete?aIndex="+t+"&bIndex="+e,{method:"GET"}).then((function(t){n.loadList()}))},base64:function(t){var e="data:image/png;base64,"+t.thumb;return console.log(e),e},play:function(t,e){this.$store.dispatch("cmdAction",{cmd:"play",aIndex:t,bIndex:e,typeId:0})},loadList:function(){var t=this;s.a.getJSON("/api/manRes",{page:this.page,typeId:0}).then((function(e){var n;t.items.length=0,t.total=e.total,(n=t.items).push.apply(n,Object(f["a"])(e.datas))}))}}),watch:{}},y=v,x=(n("761c"),n("2877")),w=Object(x["a"])(y,d,p,!1,null,"1b6da816",null),b=w.exports,I=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t._l(t.catItems,(function(e,i){return n("div",{key:i},[n("div",{staticStyle:{"text-align":"left"}},[t._v("类别:"+t._s(i))]),n("ul",{staticClass:"figure-list"},t._l(e,(function(e,a){return n("li",{key:a},[n("div",{staticStyle:{"text-align":"left"}},[n("span",[t._v(t._s(e.cnText)+"-"+t._s(e.enText))]),t.$store.state.showEdit?n("span",{staticStyle:{cursor:"pointer"},on:{click:function(n){return t.deleteIt(t.catItems,i,e)}}},[t._v("x")]):t._e()]),n("figure",{staticStyle:{cursor:"pointer"},style:{backgroundImage:"url(/api/proxy?url="+encodeURIComponent(e.imgUrl)+")"},on:{click:function(n){return t.play(e)}}})])})),0)])})),n("pagination",{attrs:{options:{chunk:5},records:t.total},on:{paginate:t.loadList},model:{value:t.page,callback:function(e){t.page=e},expression:"page"}})],2)},_=[],k=n("b85c"),S=(n("a434"),{data:function(){return{curTabIndex:0,isEdit:!1,showAll_k:!1,isPreview:!1,aIndex:0,bIndex:0,catItems:{},page:1,total:0,onPullDown:!0}},components:{Pagination:g.a},computed:{},activated:function(){this.loadList()},methods:Object(m["a"])(Object(m["a"])({},Object(a["b"])(["updateStatus"])),{},{deleteIt:function(t,e,n){s.a.post("/api/delRes",{id:n.id,typeId:n.typeId}),t[e].splice(t[e].indexOf(n),1),this.$forceUpdate()},play:function(t){this.$store.dispatch("cmdAction",{cmd:"play",iid:t.id,typeId:t.typeId})},loadList:function(){var t=this;s.a.getJSON("/api/manRes",{page:this.page,typeId:1}).then((function(e){t.catItems={},t.total=e.total;var n,i=Object(k["a"])(e.datas);try{for(i.s();!(n=i.n()).done;){var a=n.value;t.catItems[a.cat]||(t.catItems[a.cat]=[]),t.catItems[a.cat].push(a)}}catch(o){i.e(o)}finally{i.f()}t.$forceUpdate(),console.log(t.catItems)}))}}),watch:{}}),C=S,T=(n("1298"),Object(x["a"])(C,I,_,!1,null,"5949bded",null)),$=T.exports,P=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t._l(t.catItems,(function(e,i){return n("div",{key:i},[n("div",{staticStyle:{"text-align":"left"}},[t._v("类别:"+t._s(i))]),n("ul",{staticClass:"figure-list"},t._l(e,(function(e,a){return n("li",{key:a},[n("div",{staticStyle:{"text-align":"left"}},[n("span",{staticStyle:{cursor:"pointer"},on:{click:function(n){return t.playAsBackgroud(e)}}},[t._v(t._s(e.cnText))]),t.$store.state.showEdit?n("span",{staticStyle:{cursor:"pointer"},on:{click:function(n){return t.deleteIt(t.catItems,i,e)}}},[t._v("x")]):t._e()]),n("figure",{staticStyle:{cursor:"pointer"},style:{backgroundImage:"url(/api/proxy?url="+encodeURIComponent(e.imgUrl)+")"},on:{click:function(n){return t.play(e)}}})])})),0)])})),n("pagination",{attrs:{options:{chunk:5},records:t.total},on:{paginate:t.loadList},model:{value:t.page,callback:function(e){t.page=e},expression:"page"}})],2)},O=[];window.$=s.a;var j={data:function(){return{curTabIndex:0,isEdit:!1,showAll_k:!1,isPreview:!1,aIndex:0,bIndex:0,catItems:{},page:1,total:0,onPullDown:!0,curBgMedia:null}},components:{Pagination:g.a},computed:{},activated:function(){this.loadList()},methods:{deleteIt:function(t,e,n){s.a.post("/api/delRes",{id:n.id,typeId:n.typeId}),t[e].splice(t[e].indexOf(n),1),this.$forceUpdate()},play:function(t){this.$store.dispatch("cmdAction",{cmd:"play",id:t.id,typeId:t.typeId})},playAsBackgroud:function(t){if(t==this.curBgMedia)return this.curBgMedia=null,void s.a.get("/api/bgmedia",{cmd:"pause"});this.curBgMedia=t,s.a.get("/api/bgmedia",{cmd:"url",val:t.sound}).then((function(){return s.a.get("/api/bgmedia",{cmd:"volume",val:.4}).then((function(){}))}))},loadList:function(){var t=this;s.a.getJSON("/api/manRes",{page:this.page,typeId:2}).then((function(e){t.catItems={},t.total=e.total;var n,i=Object(k["a"])(e.datas);try{for(i.s();!(n=i.n()).done;){var a=n.value;t.catItems[a.cat]||(t.catItems[a.cat]=[]),t.catItems[a.cat].push(a)}}catch(o){i.e(o)}finally{i.f()}t.$forceUpdate(),console.log(t.catItems)}))}},watch:{}},E=j,U=(n("4e6c"),Object(x["a"])(E,P,O,!1,null,"f60525a2",null)),L=U.exports,A=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t.rightCn-t.wrongCn>0?n("div",{staticStyle:{"text-align":"left",color:"red"}},[t._l(t.rightCn-t.wrongCn,(function(t){return n("font-awesome-icon",{key:t,attrs:{icon:"star","fixed-width":""}})})),t._v(" "+t._s(t.rightCn-t.wrongCn)+" ")],2):t._e(),n("ul",{staticClass:"figure-list"},t._l(t.list,(function(e,i){return n("li",{key:i},[n("div",{staticClass:"itemText"},[n("span",[t._v(t._s(e.cnText))]),n("span",[t._v("-")]),n("span",[t._v(t._s(e.enText))])]),n("figure",{staticClass:"animated",class:{tada:t.clickIndex==i},staticStyle:{cursor:"pointer"},style:{backgroundImage:"url(/api2/proxy?url="+encodeURIComponent(e.imgUrl)+"&enText="+encodeURIComponent(e.enText.trim())+")"},on:{click:function(n){t.select(i,e),t.clickIndex=i}}}),t._e(),e.sound?n("div",{staticClass:"aDSsMd XniWcz aDSsMd-open",staticStyle:{display:"block"}}):t._e()])})),0),n("div",{staticStyle:{cursor:"pointer"}},[n("span",{on:{click:t.replay}},[n("font-awesome-icon",{class:{speaking:t.speaking},attrs:{icon:"volume-up","fixed-width":""}}),t._v("Replay 重播 ")],1),n("span",{on:{click:function(e){return t.randList()}}},[n("font-awesome-icon",{class:{speaking:t.speaking},attrs:{icon:"step-forward","fixed-width":""}}),t._v("下一个")],1),n("audio",{directives:[{name:"show",rawName:"v-show",value:!1,expression:"false"}],ref:"audio",staticStyle:{height:"0px",width:"0px"}})]),t.showGameIf?n("div",[n("GameList",{attrs:{id:"gamefr"}}),n("div",{staticStyle:{position:"fixed",top:"0",cursor:"pointer",right:"0","z-index":"10000"}},[n("font-awesome-icon",{attrs:{icon:"times-circle","fixed-width":""},on:{click:function(e){return t.closeGame()}}})],1)],1):t._e()])},R=[],M=n("1da1"),B=(n("96cf"),function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[n("ul",{staticClass:"figure-list"},t._l(t.items,(function(e,i){return n("li",{key:i},[n("div",{staticClass:"itemText"},[n("span",[t._v(t._s(e.name))])]),n("figure",{staticClass:"animated",class:{tada:t.clickIndex==i},staticStyle:{cursor:"pointer"},style:{backgroundImage:"url(/api2/proxy?url="+encodeURIComponent(e.imgUrl)+"&enText=null)"},on:{click:function(n){return t.select(i,e)}}})])})),0),t.item?n("iframe",{attrs:{id:"gamefr",src:t.item.url}}):t._e()])}),N=[];window.$=s.a;var G={data:function(){return{item:null,items:[{name:"捕鱼达人",imgUrl:"http://www.crge.cn:88/games/game/fish/ico.jpg",url:"http://www.crge.cn:88/games/game/fish/"},{name:"植物大战僵尸",imgUrl:"http://www.crge.cn:88/games/game/pvz/images/interface/Logo.jpg",url:"http://www.crge.cn:88/games/game/pvz/index.html"},{name:"前行的坦克",imgUrl:"http://imga3.5054399.com/upload_pic/2019/s/205232.jpg",url:"http://sxiao.4399.com/4399swf/upload_swf/ftp25/gamehwq/20180502/12/index.htm"},{name:"指挥小火车",imgUrl:"https://i3.7k7kimg.cn/game/7070/139/138792_188162.jpg",url:"http://flash.7k7k.com/cms/cms10/20140905/1641483541/55/1y/game.html"},{name:"直升机和坦克",imgUrl:"https://i1.7k7kimg.cn/game/7070/183/182562_929115.jpg",url:"http://flash.7k7k.com/cms/cms10/20170711/1656142555/12/index.htm"}]}},mounted:function(){},deactivated:function(){},computed:{},methods:{select:function(t,e){this.item=e}},watch:{}},z=G,J=(n("df99"),Object(x["a"])(z,B,N,!1,null,"e5a68fc0",null)),W=J.exports;window.$=s.a;var D={data:function(){return{rightCn:0,wrongCn:0,curAct:0,list:[],curIndex:0,speaking:!1,seconds:15,timer:0,clickIndex:-1,showGameIf:!1}},components:{GameList:W},mounted:function(){},activated:function(){this.randList()},deactivated:function(){clearInterval(this.timer)},computed:{listenChangeCn:function(){var t=this.rightCn,e=this.wrongCn;return{rightCn:t,wrongCn:e}}},methods:{sendFeedBack:function(t){var e=this.list[this.curIndex];null==e.rTimes&&(e.rTimes=0),t!=this.curIndex?(e.rTimes>0&&(e.rTimes=0),e.rTimes--):e.rTimes++,s.a.ajax({type:"post",url:"/api2/save",contentType:"application/json",data:JSON.stringify(e),success:function(t){}})},closeGame:function(){this.showGameIf=!1,clearTimeout(this.timer),this.randList()},startShowGame:function(){var t=this;this.rightCn=0,this.wrongCn=0,this.showGameIf=!0,this.timer=setTimeout((function(){t.showGameIf=!1,t.randList()}),3e5)},replay:function(){var t=this.list[this.curIndex];this.say(t.enText)},getIndicator:function(t){return""},select:function(t,e){var n=this;Object(M["a"])(regeneratorRuntime.mark((function i(){return regeneratorRuntime.wrap((function(i){while(1)switch(i.prev=i.next){case 0:if(n.sendFeedBack(t),t!=n.curIndex){i.next=14;break}return n.rightCn++,n.curAct=1,i.next=6,n.say(e.enText,"en").then((function(){return n.say(e.cnText,"zh")})).then((function(){return n.say(n.getRightSetence(),"en")})).then((function(){return n.loadSound(e)}));case 6:if(!(n.rightCn-n.wrongCn>=20)){i.next=10;break}return i.abrupt("return",n.startShowGame());case 10:return i.next=12,n.randList();case 12:i.next=18;break;case 14:return i.next=16,n.say(e.enText,"en").then((function(){return n.say(e.cnText,"zh")})).then((function(){return n.say(n.getWrongSetence(),"en")}));case 16:n.wrongCn++,n.curAct=-1;case 18:case"end":return i.stop()}}),i)})))()},randList:function(){var t=this;return this.$refs.audio.src=null,s.a.getJSON("/api2/randList",{n:3}).then((function(e){var n;t.list.length=0,(n=t.list).push.apply(n,Object(f["a"])(e)),t.curIndex=parseInt(3*Math.random()),t.replay(),t.clickIndex=-1}))},say:function(t){var e=this,n=arguments.length>1&&void 0!==arguments[1]?arguments[1]:"en";this.speaking=!0;var i=this;"en"==n?n=" -v Samantha ":"zh"==n&&(n=" -v Ting-Ting ");var a=!0;return a?s.a.get("/api2/say",{str:n+t}).then((function(){e.speaking=!1})):s.a.get("/api2/tts",{lan:"en",str:t}).then((function(t){return new Promise((function(e,n){t.URL&&(i.$refs.audio.src=t.URL,i.$refs.audio.addEventListener("ended",(function(){return i.speaking=!1,e()})),i.$refs.audio.play())}))}))},getWrongSetence:function(){var t=["no no no","oh sorry.","incorrect","come on."],e=parseInt(Math.random()*t.length);return t[e]},getRightSetence:function(){var t=["yes yes yes","well done","good job"],e=parseInt(Math.random()*t.length);return t[e]},loadSound:function(t){var e=this;return new Promise((function(n,i){var a=!0;if(a)return n();if(t.sound){e.$refs.audio.src=t.sound;var o=setTimeout(n,1e4);e.$refs.audio.addEventListener("ended",(function(){n(),this.speaking=!1,clearTimeout(o)})),e.$refs.audio.play()}else n()}))}},watch:{}},X=D,F=(n("dc93"),Object(x["a"])(X,A,R,!1,null,"51cdc726",null)),V=F.exports,q=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t._l(t.catItems,(function(e,i){return n("div",{key:i},[n("div",{staticStyle:{"text-align":"left"}},[t._v("类别:"+t._s(i))]),n("ul",{staticClass:"figure-list"},t._l(e,(function(e,a){return n("li",{key:a},[n("div",{staticStyle:{"text-align":"left"}},[n("div",[t._v(" 类别："),n("input",{directives:[{name:"model",rawName:"v-model.trim",value:e.cat,expression:"item.cat",modifiers:{trim:!0}}],domProps:{value:e.cat},on:{blur:[function(n){return t.saveItem(e)},function(e){return t.$forceUpdate()}],input:function(n){n.target.composing||t.$set(e,"cat",n.target.value.trim())}}})]),n("div",[t._v(" 中文："),n("input",{directives:[{name:"model",rawName:"v-model.trim",value:e.cnText,expression:"item.cnText",modifiers:{trim:!0}}],domProps:{value:e.cnText},on:{blur:[function(n){return t.saveItem(e)},function(e){return t.$forceUpdate()}],input:function(n){n.target.composing||t.$set(e,"cnText",n.target.value.trim())}}})]),n("div",[t._v(" 英文："),n("input",{directives:[{name:"model",rawName:"v-model.trim",value:e.enText,expression:"item.enText",modifiers:{trim:!0}}],domProps:{value:e.enText},on:{blur:[function(n){return t.saveItem(e)},function(e){return t.$forceUpdate()}],input:function(n){n.target.composing||t.$set(e,"enText",n.target.value.trim())}}})]),n("div",[n("font-awesome-icon",{staticStyle:{cursor:"pointer"},attrs:{icon:"trash","fixed-width":""},on:{click:function(n){return t.deleteIt(t.catItems,i,e)}}}),n("font-awesome-icon",{staticStyle:{float:"right",cursor:"pointer"},attrs:{icon:"save","fixed-width":""},on:{click:function(n){return t.saveItem(e)}}})],1)]),n("figure",{staticStyle:{cursor:"pointer"},style:{backgroundImage:"url(/api2/proxy?url="+encodeURIComponent(e.imgUrl)+"&enText="+encodeURIComponent(e.enText.trim())+")"},on:{click:function(n){return t.playfrom(e)}}})])})),0)])})),n("pagination",{attrs:{options:{chunk:5},records:t.total},on:{paginate:t.loadList},model:{value:t.page,callback:function(e){t.page=e},expression:"page"}})],2)},H=[];window.$=s.a;var K={data:function(){return{curTabIndex:0,isEdit:!1,showAll_k:!1,isPreview:!1,aIndex:0,bIndex:0,catItems:{},page:1,total:0,onPullDown:!0}},components:{Pagination:g.a},computed:{},mounted:function(){this.loadList()},methods:{editIt:function(){},saveItem:function(t){s.a.ajax({type:"post",url:"/api2/save",contentType:"application/json",data:JSON.stringify(t),success:function(t){}})},deleteIt:function(t,e,n){s.a.post("/api/delRes",{id:n.id,typeId:n.typeId}),t[e].splice(t[e].indexOf(n),1),this.$forceUpdate()},loadList:function(){var t=this;s.a.getJSON("/api2/subjectList",{pageNo:this.page}).then((function(e){t.catItems={},t.total=e.totalElements;var n,i=Object(k["a"])(e.content);try{for(i.s();!(n=i.n()).done;){var a=n.value;t.catItems[a.cat]||(t.catItems[a.cat]=[]),t.catItems[a.cat].push(a)}}catch(o){i.e(o)}finally{i.f()}t.$forceUpdate(),console.log(t.catItems)}))}},watch:{}},Q=K,Y=(n("7eba"),Object(x["a"])(Q,q,H,!1,null,"fd891290",null)),Z=Y.exports,tt={components:{ByVideo:b,ByImage:$,ByAudio:L,ByWhen:V,GameList:W,EditWords:Z},data:function(){return{curTabIndex:0,showMenu:0,tabs:[{name:"视频学习",comp:"ByVideo"},{name:"看图识字",comp:"ByImage"},{name:"音乐",comp:"ByAudio"},{name:"考考你",comp:"ByWhen"},{name:"EditWords",comp:"EditWords"}]}},computed:{showEdit:{get:function(){return this.$store.state.showEdit},set:function(t){this.$store.commit("showEdit",t)}}},watch:{curTabIndex:function(){["ByWhen","EditWords"].indexOf(this.tabs[this.curTabIndex].comp)>-1?this.$store.commit("showPlayer",!1):this.$store.commit("showPlayer",!0)}}},et=tt,nt=(n("62d1"),Object(x["a"])(et,l,u,!1,null,"cd62adcc",null)),it=nt.exports,at=function(){var t=this,e=t.$createElement,n=t._self._c||e;return t.$store.state.showPlayer?n("div",{ref:"parent"},[n("div",{staticClass:"x-footer"},[n("div",[n("div",{staticStyle:{display:"flex","box-sizing":"border-box"}},[n("div",{staticClass:"duration"},[t._v(t._s(t.format(t.currentPosition)))]),n("div",{staticClass:"progresswrap",staticStyle:{"flex-grow":"1"}},[n("div",{staticClass:"progress"},[n("div",{staticClass:"progress_bg"},[n("div",{staticClass:"video_progress",style:{width:t.left+"px"}})]),n("div",{staticClass:"progress_btn",style:{left:t.left+"px"}})])]),n("div",{staticClass:"duration"},[t._v(t._s(t.format(t.duration)))])]),n("div",{staticStyle:{display:"flex"}},[n("div",{staticClass:"x-meida"},[n("div",{staticClass:"x-meida-img"},[n("img",{class:{running:t.playing},staticStyle:{"border-radius":"50%",width:"80px",height:"80px"},attrs:{src:t.curItem.imgUrl}})]),n("div",{staticClass:"x-media-name"},[n("h3",[t._v(t._s(t.curItem.enText||t.curItem.cnText))]),n("h5")])]),n("div",{staticClass:"x-media-btn"},[n("img",{attrs:{src:t.playing?t.footer.stopIcon:t.footer.startIcon},on:{click:t.togglePlay}})]),n("div",{staticClass:"x-media-menu"})]),n("div",{staticClass:"ctrl",staticStyle:{display:"block","text-align":"left",margin:"5px"}},[n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 24")}}},[t._v("音量+")]),n("span",{staticClass:"bt",on:{click:function(e){return t.sendEvent("input keyevent 25")}}},[t._v("音量-")]),n("span",{staticClass:"bt",staticStyle:{float:"right"},on:{click:t.next}},[t._v("下一个")]),n("span",{staticClass:"bt",staticStyle:{float:"right"},on:{click:t.load}},[t._v("加载")])])])])]):t._e()},ot=[];n("25f0"),n("b65f"),n("99af");window.$=s.a;var st={props:["curAid"],data:function(){return{isStore:!1,left:0,footer:{startIcon:"../static/img/start.svg",stopIcon:"../static/img/stop.svg"}}},mounted:function(){var t=this;this.initProgressBar(),setTimeout((function(){s()("#app").css("margin-bottom",s()(".x-footer").outerHeight()+10)}),3e3),Object(M["a"])(regeneratorRuntime.mark((function e(){return regeneratorRuntime.wrap((function(e){while(1)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,t.getStatus();case 3:e.next=8;break;case 5:e.prev=5,e.t0=e["catch"](0),console.log(e.t0);case 8:return e.next=10,new Promise((function(t,e){setTimeout(t,2e3)}));case 10:e.next=0;break;case 12:case"end":return e.stop()}}),e,null,[[0,5]])})))()},computed:{curItem:function(){return this.$store.state.playController.curItem},playing:function(){return this.$store.state.playController.playing},duration:function(){return this.$store.state.playController.duration},currentPosition:function(){return this.$store.state.playController.currentPosition}},methods:Object(m["a"])(Object(m["a"])({},Object(a["b"])(["updateStatus"])),{},{getStatus:function(){var t=this;fetch("/api/status",{method:"GET"}).then((function(e){e.json().then((function(e){t.updateStatus(e)}))})).catch((function(t){console.log("error: "+t.toString())}))},next:function(){this.$store.dispatch("cmdAction",{cmd:"next"})},load:function(){fetch("/api/reLoadPlayList",{method:"get"}).then((function(t){}))},prev:function(){},onProgress:function(t){this.$store.dispatch("cmdAction",{cmd:"seekTo",val:t})},togglePlay:function(){this.$store.dispatch("cmdAction",{cmd:"toggle"})},sendEvent:function(t){fetch("/api/event",{method:"post",body:JSON.stringify({event:t}),headers:{"Content-type":"application/x-www-form-urlencoded"}}).then((function(t){return t.text()})).then((function(t){console.log(t)}))},format:function(t){var e=t/1e3,n=Math.trunc(e%60),i=Math.trunc(e/60%60),a=Math.trunc(e/3600);return a>0?"".concat(a,":").concat(i,":").concat(n):"".concat(i,":").concat(n)},initProgressBar:function(){var t=this,e=0;s()((function(){var n=!1,i=0,a=0,o=s()(".progress").width();s()(".progress_btn").mousedown((function(e){i=e.pageX-t.left,console.log(i),n=!0})),s()(".progress").mouseup((function(){n=!1,t.onProgress(parseInt(t.left/o*t.duration))})),s()(".progress").mousemove((function(a){n&&(e=a.pageX-i,e<=0?e=0:e>o&&(e=o),console.log(e),t.left=e,s()(".text").html(parseInt(e/o*100)+"%"))})),s()(".progress").click((function(i){n||(a=s()(".progress").offset().left,e=i.pageX-a,e<=0?e=0:e>o&&(e=o),t.left=e,t.onProgress(parseInt(t.left/o*t.duration)),s()(".text").html(parseInt(e/o*100)+"%"))}))}))}}),watch:{currentPosition:function(){this.left=s()(".progress").width()*this.currentPosition/this.duration}}},rt=st,ct=(n("56eb"),Object(x["a"])(rt,at,ot,!1,null,"0f6e7bd4",null)),lt=ct.exports,ut={name:"App",components:{NavIndex:it,Player:lt}},dt=ut,pt=(n("034f"),Object(x["a"])(dt,r,c,!1,null,null,null)),ft=pt.exports,mt=n("ecee"),ht=n("c074"),gt=n("b702"),vt=n("f2d1"),yt=n("ad3d");n("ab8b"),n("3e48");mt["c"].add(ht["a"],gt["a"],vt["a"]),i["a"].component("font-awesome-icon",yt["a"]),i["a"].component("font-awesome-layers",yt["b"]),i["a"].component("font-awesome-layers-text",yt["c"]),i["a"].config.productionTip=!1,i["a"].use(a["a"]);var xt=new a["a"].Store({state:{playController:{playing:!1,curItem:{imgUrl:!1,enTtext:""},duration:!1,currentPosition:!1,aIndex:0,bIndex:0},showEdit:!1,showPlayer:!0},mutations:{showEdit:function(t,e){t.showEdit=e},showPlayer:function(t,e){t.showPlayer=e},updateStatus:function(t,e){Object.assign(t.playController,e)}},actions:{updateStatus:function(t,e){var n=t.commit,i=t.state;if(e.remote){var a=Object.assign({},i.playController);Object.assign(a,e),s.a.get("/api/play",a)}n("updateStatus",e)},cmdAction:function(t,e){t.commit,t.state;s.a.get("/api/cmd",e)}}});new i["a"]({render:function(t){return t(ft)},store:xt}).$mount("#app")},"56eb":function(t,e,n){"use strict";n("83bc")},"62d1":function(t,e,n){"use strict";n("ebf0")},"761c":function(t,e,n){"use strict";n("0238")},"7eba":function(t,e,n){"use strict";n("868c")},"83bc":function(t,e,n){},"85ec":function(t,e,n){},"868c":function(t,e,n){},d907:function(t,e,n){},dc93:function(t,e,n){"use strict";n("3a8b")},df99:function(t,e,n){"use strict";n("2939")},ebf0:function(t,e,n){}});
//# sourceMappingURL=app.ccfdd0ce.js.map