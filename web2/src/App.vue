<template>
  <div id="app">
    <router-view />
    <player v-show="showPlay"></player>
  </div>
</template>

<script>
import Player from "./components/Player";

export default {
  name: "App",
  data: () => {
    return { showPlay: true };
  },
  components: { Player },
  watch: {
    "$route.path": function (newVal, oldVal) {
      if (newVal === "/test2" || newVal === "/test3" || newVal === "/health") {
        this.showPlay = false;
      } else this.showPlay = true;
    },
  },
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
body {
  font-size: 16px;
  text-align: center;
  max-width: 100%;
  overflow-x: hidden;
  vertical-align: top;
  user-select: none;
}
body,
h1,
h2,
h3,
p,
div,
ol,
ul,
input,
button {
  margin: 0;
  padding: 0;
}
</style>
