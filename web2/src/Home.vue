<template>
  <div>
    <NavIndex :typeId="typeId" />
  </div>
</template>

<script>
import NavIndex from "./components/SwiperNavIndex.vue";
export default {
  name: "App",
  props: ["typeId"],
  data: () => {
    return {};
  },
  mounted() {},
  components: {
    NavIndex,
  },
};
</script>
