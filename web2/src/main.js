import Vue from "vue";
import Vuex from "vuex";
import App from "./App.vue";

import { library } from "@fortawesome/fontawesome-svg-core";
import { fas } from "@fortawesome/free-solid-svg-icons";
import { far } from "@fortawesome/free-regular-svg-icons";
import { fab } from "@fortawesome/free-brands-svg-icons";
import $ from "jquery";

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.min";
import {
  FontAwesomeIcon,
  FontAwesomeLayers,
  FontAwesomeLayersText,
} from "@fortawesome/vue-fontawesome";

import Pagination from "vue-pagination-2";

import router from "./router";

import uploader from "vue-simple-uploader";

Vue.use(uploader);
Vue.component("pagination", Pagination);
library.add(fas, far, fab);
window.$ = $;
Vue.component("font-awesome-icon", FontAwesomeIcon);
Vue.component("font-awesome-layers", FontAwesomeLayers);
Vue.component("font-awesome-layers-text", FontAwesomeLayersText);

Vue.config.productionTip = false;
Vue.use(Vuex);

const store = new Vuex.Store({
  state: {
    playController: {
      showMask: false,
      playing: false,
      detach: false,
      curItem: { imgUrl: false, enTtext: "" },
      duration: false,
      currentPosition: false,
      aIndex: 0,
      bIndex: 0,
      mode: 0,
      coverUrl: "",
      name: "",
    },
    showEdit: false,
    showPlayer: true,
    searchValue: "",
  },
  mutations: {
    searchValue: (state, value) => {
      state.searchValue = value;
    },
    showEdit: (state, value) => {
      state.showEdit = value;
    },
    showPlayer: (state, value) => {
      state.showPlayer = value;
    },
    updateStatus: (state, st) => {
      Object.assign(state.playController, st);
    },
  },
  actions: {
    updateStatus({ commit, state }, playload) {
      if (playload.remote) {
        var params = Object.assign({}, state.playController);
        Object.assign(params, playload);
        $.get("/api/play", params);
      }
      commit("updateStatus", playload);
    },
    cmdAction({ commit, state }, playload) {
      $.get("/api/cmd", playload);
    },
  },
});
new Vue({
  render: (h) => h(App),
  store: store,
  router: router,
  data: {
    eventHub: new Vue(),
  },
}).$mount("#app");
