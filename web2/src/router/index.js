import Vue from "vue";
import Router from "vue-router";

Vue.use(Router);

const home = () => import("../Home");

const routes = [
  { path: "/", redirect: "/home/video/p/1" },
  {
    path: "/home/:typeId/p/:pageId",
    name: "home",
    props: true,
    component: home,
  },
];

export default new Router({ routes: routes });
